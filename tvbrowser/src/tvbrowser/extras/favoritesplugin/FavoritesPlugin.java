/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.favoritesplugin;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataBaseListener;
import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataDeserializer;
import tvbrowser.extras.common.DataSerializer;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.core.ActorsFavorite;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
import tvbrowser.extras.favoritesplugin.dlgs.EditFavoriteDialog;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MutableChannelDayProgram;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.NullProgressMonitor;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ButtonAction;
import devplugin.ChannelDayProgram;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;
import devplugin.ThemeIcon;

/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin {
  public static final Logger mLog = Logger.getLogger(FavoritesPlugin.class.getName());
  /**
   * Tango category of the icon to be used in this plugin
   */
  private static final String ICON_CATEGORY = "emblems";

  /**
   * Tango name of the icon to be used in this plugin
   */
  private static final String ICON_NAME = "emblem-favorite";

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;
  
  private Properties mSettings = new Properties();

  private static String DATAFILE_PREFIX = "favoritesplugin.FavoritesPlugin";

  private ConfigurationHandler mConfigurationHandler;

  private PluginTreeNode mRootNode;

  private boolean mShowInfoOnNewProgramsFound = true;
  
  private boolean mHasRightToUpdate = false;

  private boolean mHasToUpdate = false;
  
  /**
   * do not save the favorite tree during TV data updates because it might not be consistent 
   */
  private boolean mHasRightToSave = true;
  private boolean mIsShuttingDown = false;

  private Hashtable<String,ReceiveTargetItem> mSendPluginsTable = new Hashtable<String,ReceiveTargetItem>();
  private ProgramReceiveTarget[] mClientPluginTargets;

  private ArrayList<AdvancedFavorite> mPendingFavorites;
  private int mMarkPriority = -2;
  
  private Exclusion[] mExclusions;
  private ArrayList<UpdateInfoThread> mUpdateInfoThreads;
  
  private boolean mShowInfoDialog = false;
  private ThreadPoolExecutor mThreadPool;
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  private FavoritesPlugin() {
    mInstance = this;
    mExclusions = new Exclusion[0];
    mPendingFavorites = new ArrayList<AdvancedFavorite>(0); 
    mClientPluginTargets = new ProgramReceiveTarget[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    mUpdateInfoThreads = new ArrayList<UpdateInfoThread>(0);
    load();
    mRootNode = new PluginTreeNode(mLocalizer.msg("manageFavorites","Favorites"));

    TvDataBase.getInstance().addTvDataListener(new TvDataBaseListener() {
      public void dayProgramTouched(final ChannelDayProgram removedDayProgram,
          final ChannelDayProgram addedDayProgram) {
        if(mThreadPool == null) {
          mThreadPool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        }
        
        Runnable update = new Runnable() {
          public void run() {
            if(removedDayProgram != null) {
              Iterator<Program> it = removedDayProgram.getPrograms();
              
              while (it.hasNext()) {
                try {
                  Program p = it.next();
                  
                  for (Favorite fav : FavoriteTreeModel.getInstance().getFavoriteArr()) {
                    fav.removeProgram(p);
                  }
                }catch(Throwable t) {
                  ErrorHandler.handle("Error in removing program from Favorites",t);
                }
              }
            }
            
            if(addedDayProgram != null) {
              Iterator<Program> it = addedDayProgram.getPrograms();
              while (it.hasNext()) {
                final Program p = it.next();
      
                for (Favorite fav : FavoriteTreeModel.getInstance().getFavoriteArr()) {                
                  try {
                    fav.tryToMatch(p);
                  } catch (TvBrowserException e) {
                    ErrorHandler.handle(e);
                  }                
                }
              }
            }
          }
        };
        
        mThreadPool.execute(update);
      }
      
      public void dayProgramAdded(ChannelDayProgram prog) {}
      public void dayProgramDeleted(ChannelDayProgram prog) {}
      public void dayProgramAdded(MutableChannelDayProgram prog) {}
    });

    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateStarted() {
        mHasRightToSave = false;
        mSendPluginsTable.clear();
        
        for (Favorite favorite : FavoriteTreeModel.getInstance().getFavoriteArr()) {
          favorite.clearNewPrograms();
          favorite.clearRemovedPrograms();
        }
      }

      public void tvDataUpdateFinished() {
        // only update the favorites if new data was downloaded
        if (TvDataUpdater.getInstance().tvDataWasChanged()) {
          if(!mSendPluginsTable.isEmpty()) {
            sendToPlugins();
          }
          handleTvDataUpdateFinished();
        }
      }
    });
  }
  
  /**
   * Waits for finishing the update threads.
   * @since 2.7.2
   */
  public void waitForFinishingUpdateThreads() {
    if (mThreadPool != null) {
      mLog.info("Favorites: Wait for update threads to finish");
      mThreadPool.shutdown();
      
      try {
        boolean success = mThreadPool.awaitTermination(Math.max(
            FavoriteTreeModel.getInstance().getFavoriteArr().length, 10),
            TimeUnit.SECONDS);

        if (success) {
          mLog.info("Favorites: Update threads were finished");
        } else {
          mLog
              .severe("Favorites: Timeout on waiting for update threads to finish was reached");
        }
      } catch (InterruptedException e) {
        ErrorHandler.handle(
            "Waiting for favorite update finishing was interrupted", e);
      }
      
      mThreadPool = null;
    }
  }

  private void handleTvDataUpdateFinished() {
    mHasToUpdate = true;

    if(mHasRightToUpdate) {
      Thread update = new Thread("Favorites: handle update finished") {
        public void run() {
          mHasToUpdate = false;
    
          ManageFavoritesDialog dlg = ManageFavoritesDialog.getInstance();
          
          if(dlg != null) {
            dlg.favoriteSelectionChanged();
          }
          
          //FavoriteTreeModel.getInstance().reload();
    
          mHasRightToSave = true;
          updateRootNode(true);
    
          ArrayList<Favorite> showInfoFavorites = new ArrayList<Favorite>(0);
    
          Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
    
          for (Favorite favorite : favoriteArr) {
            favorite.clearRemovedPrograms();
            
            if (favorite.isRemindAfterDownload() && favorite.getNewPrograms().length > 0) {
              showInfoFavorites.add(favorite);
            }
          }
    
          if(!showInfoFavorites.isEmpty()) {
            UpdateInfoThread thread = new UpdateInfoThread(showInfoFavorites.toArray(new Favorite[showInfoFavorites.size()]));
            thread.setPriority(Thread.MIN_PRIORITY);
    
            mUpdateInfoThreads.add(thread);
            thread.start();
          }
          
          Favorite[] favorites = FavoriteTreeModel.getInstance().getFavoriteArr();
          
          for(Favorite fav : favorites) {
            fav.revalidatePrograms();
          }
        }
      };
      update.start();
      
      try {
        update.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  
  public void handleTvBrowserIsShuttingDown() {
    mIsShuttingDown = true;
    
    try {
      if(!mUpdateInfoThreads.isEmpty()) {
        for(int i = mUpdateInfoThreads.size() - 1; i >= 0; i--) {
          UpdateInfoThread thread = mUpdateInfoThreads.get(i);
          
          thread.interrupt();
          thread.showDialog();
        }
      }
    }catch(Throwable t) {
      ErrorHandler.handle("Error during showing new Favorites on TV-Browser shutting down.",t);  
    }
  }
  
  public static synchronized FavoritesPlugin getInstance() {
    if (mInstance == null) {
      new FavoritesPlugin();
    }
    return mInstance;
  }
  
  public void handleTvBrowserStartFinished() {
    updateRootNode(false);
    if(!mPendingFavorites.isEmpty()) {
      for(AdvancedFavorite fav : mPendingFavorites) {
        fav.loadPendingFilter();
      }
      
      mPendingFavorites.clear();
      mPendingFavorites = null;
    }
    
    mHasRightToUpdate = true;
    
    if(mHasToUpdate) {
      handleTvDataUpdateFinished();
    }    
  }

  private void load() {
    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    }catch(IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavoritesSettings","Could not load settings for favorites"), e);
    }

    try {
      mConfigurationHandler.loadData(new DataDeserializer(){
        public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
          readData(in);
        }
      });
    }catch(IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavorites","Could not load favorites"), e);
    }
  }

  public synchronized void store() {
    try {
      mConfigurationHandler.storeData(new DataSerializer(){
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavorites","Could not store favorites"), e);
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavoritesSettings","Could not store settings for favorites"), e);
    }
  }

  public ImageIcon getIconFromTheme(String category, String Icon, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, Icon, size);
  }

  private void readData(ObjectInputStream in) throws IOException,
          ClassNotFoundException {
    int version = in.readInt();
    
    Favorite[] newFavoriteArr;
    
    if(version < 6) {
      int size = in.readInt();
      
      newFavoriteArr = new Favorite[size];
      for (int i = 0; i < size; i++) {
        if (version <= 2) {
          /* read favorites from older TV-Browser versions
           * this should stay at least until version 3.0
           * of TV-Browser */
          newFavoriteArr[i] = new AdvancedFavorite(null, in);
        }
        else {
          String typeID = (String)in.readObject();
          if (TopicFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new TopicFavorite(in);
          }
          else if (TitleFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new TitleFavorite(in);
          }
          else if (ActorsFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new ActorsFavorite(in);
          }
          else if (AdvancedFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new AdvancedFavorite(in);
          }
  
        }
      }
      FavoriteTreeModel.initInstance(newFavoriteArr);
    }
    else {
      FavoriteTreeModel.initInstance(in,version);
      newFavoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
    }

    for (Favorite newFavorite : newFavoriteArr) {
      Program[] programArr = newFavorite.getWhiteListPrograms();
      for (Program program : programArr) {
        program.mark(FavoritesPluginProxy.getInstance());
      }
    }

    boolean reminderFound = false;

    // Get the client plugins
    if(version <= 4) {
      int size = in.readInt();
      ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>(0); 
      for (int i = 0; i < size; i++) {
        String id = null;
        
        if (version == 1) {
          // In older versions of TV-Browser, not the plugin ID was saved,
          // but its class name.
          // -> We have to translate the class name into an ID.
          String className = (String) in.readObject();
          id = "java." + className;
        } else {
          id = (String) in.readObject();
        }

        if(version <= 2) {
          if(id.compareTo("java.reminderplugin.ReminderPlugin") == 0) {
            reminderFound = true;
          }
        }
        
        if(version > 2 || (version <= 2 && id.compareTo("java.reminderplugin.ReminderPlugin") != 0)) {
          list.add(ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(id));
        }
      }
      
      if(!list.isEmpty()) {
        mClientPluginTargets = list.toArray(new ProgramReceiveTarget[list.size()]);
      }
    }
    else {
      int n = in.readInt();
      mClientPluginTargets = new ProgramReceiveTarget[n];
      
      for (int i = 0; i < n; i++) {
        mClientPluginTargets[i] = new ProgramReceiveTarget(in);
      }
    }

    if(version <= 2 && reminderFound) {
      for (Favorite newFavorite : newFavoriteArr) {
        newFavorite.getReminderConfiguration().setReminderServices(new String[] {ReminderConfiguration.REMINDER_DEFAULT});
      }
      
      updateAllFavorites();
    }

    if(version >= 4) {
      this.mShowInfoOnNewProgramsFound = in.readBoolean();
    }
    
    if(version >= 7) {
      mExclusions = new Exclusion[in.readInt()];
      
      for(int i = 0; i < mExclusions.length; i++) {
        mExclusions[i] = new Exclusion(in);
      }
    }
  }


  private void updateAllFavorites() {
    mSendPluginsTable.clear();

    ProgressMonitor monitor;

    Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

    if (favoriteArr.length > 5) {    // if we have more then 5 favorites, we show a progress bar
      try {
        monitor = MainFrame.getInstance().createProgressMonitor();
      }catch(Exception e) {e.printStackTrace();
        monitor = new NullProgressMonitor();
      }
    }
    else {
      monitor = new NullProgressMonitor();
    }
    monitor.setMaximum(favoriteArr.length);
    monitor.setMessage(mLocalizer.msg("updatingFavorites","Updating favorites"));

    for (int i=0;i<favoriteArr.length; i++) {
      monitor.setValue(i);

      try {
        favoriteArr[i].refreshBlackList();
        favoriteArr[i].updatePrograms(true,true);
      } catch (TvBrowserException e) {
        ErrorHandler.handle(e);
      }
    }
    monitor.setMessage("");
    monitor.setValue(0);

    if(!mSendPluginsTable.isEmpty()) {
      sendToPlugins();
    }
  }

  private void sendToPlugins() {
    Collection<ReceiveTargetItem> targets = mSendPluginsTable.values();
    StringBuffer buffer = new StringBuffer();
    ArrayList<Favorite> errorFavorites = new ArrayList<Favorite>(0);
    
    for(ReceiveTargetItem target : targets) {
      if(!target.getReceiveTarget().getReceifeIfForIdOfTarget().receivePrograms(target.getPrograms(), target.getReceiveTarget())) {
        Favorite[] favs =FavoriteTreeModel.getInstance().getFavoritesContainingReceiveTarget(target.getReceiveTarget());
        
        for(Favorite fav : favs) {
          if(!errorFavorites.contains(fav)) {
            errorFavorites.add(fav);
          }
        }
        
        buffer.append(
            target.getReceiveTarget().getReceifeIfForIdOfTarget().toString())
            .append(" - ").append(target.toString()).append('\n');
      }
    }
    
    if(buffer.length() > 0) {
      buffer.insert(0,mLocalizer.msg("sendError","Error by sending programs to other plugins.\n\nPlease check the favorites that should send\nprograms to the following plugins:\n"));
      buffer.append(mLocalizer.msg("sendErrorFavorites","\nThe following Favorites are affected by this:\n"));
      
      ScrollableJPanel panel = new ScrollableJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(0,1,0,1));
      panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
      
      for(Favorite fav : errorFavorites) {
        final Favorite finalFav = fav;
        panel.add(UiUtilities.createHtmlHelpTextArea("<a href=\"#link\">" + fav.getName() + "</a>",new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent e) {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              editFavorite(finalFav);
            }
          }
        }));
      }
      
      JScrollPane pane = new JScrollPane(panel);
      pane.setPreferredSize(new Dimension(0,100));
      
      Object[] msg = {buffer.toString(),pane};
      
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),msg,Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
    }
  }
  
  /**
   * Add the programs to send to other Plugins to a Hashtable.
   * 
   * @param targets The ProgramReceiveTargets to send the programs for.
   * @param programs The Programs to send.
   */
  public void addProgramsForSending(ProgramReceiveTarget[] targets, Program[] programs) {
    for(ProgramReceiveTarget target : targets) {
      if(target != null && target.getReceifeIfForIdOfTarget() != null) {
        synchronized(mSendPluginsTable) {
          ReceiveTargetItem item = mSendPluginsTable.get(getKeyForReceiveTarget(target));
          
          if(item == null) {
            item = new ReceiveTargetItem(target);
            mSendPluginsTable.put(getKeyForReceiveTarget(target), item);
          }
          
          item.addPrograms(programs);
        }
      }
    }
  }

  /**
   * @return If the management dialog should show the 
   * programs on the black list too.
   */
  public boolean isShowingBlackListEntries() {
    return mSettings.getProperty("showBlackEntries","false").compareTo("true") == 0;
  }
  
  /**
   * Set the value for showing black list entries
   * in the management dialog.
   * 
   * @param value If the programs are to show.
   */
  public void setIsShowingBlackListEntries(boolean value) {
    mSettings.setProperty("showBlackEntries",String.valueOf(value));
  }

  private void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(7); // version

    FavoriteTreeModel.getInstance().storeData(out);

    out.writeInt(mClientPluginTargets.length);
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      target.writeData(out);
    }

    out.writeBoolean(mShowInfoOnNewProgramsFound);
    
    out.writeInt(mExclusions.length);
    
    for (Exclusion exclusion : mExclusions) {
      exclusion.writeData(out);
    }
  }

  /**
   * Called by the host-application during start-up. Implements this method to
   * load your plugins settings from the file system.
   */
  private void loadSettings(Properties settings) {
    mSettings = settings;
    if (settings == null) {
      throw new IllegalArgumentException("settings is null");
    }
  }

  private int getIntegerSetting(Properties prop, String key, int defaultValue) {
    int res = defaultValue;
    try {
      res = Integer.parseInt(prop.getProperty(key, Integer
          .toString(defaultValue)));
    } catch (NumberFormatException e) {
      // ignore
    }
    return res;
  }

  protected ActionMenu getButtonAction() {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog(false, null);
      }
    });

    action.setBigIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 22));
    action.setSmallIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 16));
    action.setShortDescription(mLocalizer.msg("favoritesManager",
            "Manage favorite programs"));
    action.setText(mLocalizer.msg("manageFavorites", "Favorites"));

    return new ActionMenu(action);
  }


  protected ActionMenu getContextMenuActions(Program program) {
    return new ContextMenuProvider(FavoriteTreeModel.getInstance().getFavoriteArr()).getContextMenuActions(program);
  }



  public void editFavorite(Favorite favorite) {

    Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
    UiUtilities.centerAndShow(dlg);
    if (dlg.getOkWasPressed()) {
      updateRootNode(true);
    }
  }

  private void showManageFavoritesDialog(final boolean showNew, Favorite[] favoriteArr) {
    int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",
            200);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(MainFrame.getInstance(), favoriteArr, splitPanePosition, showNew);
    dlg.setModal(!showNew);
    
    if(mShowInfoOnNewProgramsFound) {
      dlg.addComponentListener(new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          if(showNew) {
            JCheckBox chb = new JCheckBox(mLocalizer.msg("dontShow","Don't show this description again."));
            Object[] o = {mLocalizer.msg("newPrograms-description","Favorites that contains new programs will be shown in this dialog.\nWhen you click on a Favorite you can see the new programs in the right list.\n\n"),
                chb
            };
            
            JOptionPane.showMessageDialog(e.getComponent(),o);

            if(chb.isSelected()) {
              mShowInfoOnNewProgramsFound = false;
            }
          }    
        }
      });
    }

    Settings.layoutWindow("extras.manageFavoritesDlg",dlg,new Dimension(650,450));
    dlg.setVisible(true);
    
    splitPanePosition = dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition", Integer
        .toString(splitPanePosition));
    
    if (!showNew) {
      updateRootNode(true);
    }
  }

  public boolean isUsingExpertMode() {
    return mSettings.getProperty("expertMode","false").compareTo("true") == 0;
  }

  public void setIsUsingExpertMode(boolean value) {
    mSettings.setProperty("expertMode",String.valueOf(value));
  }

  public boolean isShowingPictures() {
    return mSettings.getProperty("showPictures","false").compareTo("true") == 0;
  }
  
  public void setIsShowingPictures(boolean value) {
    mSettings.setProperty("showPictures",String.valueOf(value));
  }

  public void showCreateFavoriteWizard(Program program) {
    showCreateFavoriteWizard(program, null);
  }

  public void showCreateFavoriteWizard(Program program, String path) {
    showCreateFavoriteWizardInternal(program, null, null);
  }
  
  public void showCreateActorFavoriteWizard(Program program, String actor) {
    showCreateFavoriteWizardInternal(program, actor, null);
  }

  public void showCreateTopicFavoriteWizard(Program program, String topic) {
    showCreateFavoriteWizardInternal(program, null, topic);
  }
  
  private void showCreateFavoriteWizardInternal(Program program, String actor,
      String topic) {
    Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    Favorite favorite;
    if (isUsingExpertMode()) {
      favorite = new AdvancedFavorite(program != null ? program.getTitle() : "");
      EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
      UiUtilities.centerAndShow(dlg);
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler; 
      TypeWizardStep initialStep = new TypeWizardStep(program);
      if (topic != null) {
        initialStep.setTopic(topic);
      } else if (actor != null) {
        initialStep.setActor(actor);
      }
      handler = new WizardHandler(parent, initialStep);
      favorite = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    }

    if (favorite != null) {
      try {        
        favorite.updatePrograms();
        FavoriteTreeModel.getInstance().addFavorite(favorite);
        
        if(ManageFavoritesDialog.getInstance() != null) {
          ManageFavoritesDialog.getInstance().addFavorite(favorite, null);
        }
        
      }catch (TvBrowserException exc) {
        ErrorHandler.handle(mLocalizer.msg("couldNotUpdateFavorites","Could not update favorites."), exc);
      }

      if (program != null && favorite.getPrograms().length == 0 && !favorite.isRemindAfterDownload()) {
        Object[] options = {mLocalizer.msg("btn.notifyMe","Notify Me"), mLocalizer.msg("btn.editFavorite","Edit Favorite"), mLocalizer.msg("btn.ignore","Ignore")};
        int option = JOptionPane.showOptionDialog(parent, mLocalizer.msg("dlg.noMatchingPrograms","Currently no program matches the newly created favorite.\n\nDo you want TV-Browser to notify you when any program matches this favorite?"),
                  mLocalizer.msg("dlg.title.information","Information"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.INFORMATION_MESSAGE,
                  null,
                  options,
                  options[0]);
        if (option == JOptionPane.YES_OPTION) {
         favorite.setRemindAfterDownload(true);
        }
        else if (option == JOptionPane.NO_OPTION) {
          editFavorite(favorite);
        }
      }

      else if (program != null && !favorite.contains(program)) {
        // only show a warning for non matching favorites if the program is older than today
        if (program.getDate().compareTo(new devplugin.Date()) >= 0) {
          Object[] options = {mLocalizer.msg("btn.editFavorite","Edit Favorite"), mLocalizer.msg("btn.ignore","Ignore")};
          if (JOptionPane.showOptionDialog(parent, mLocalizer.msg("dlg.programDoesntMatch","The currently selected program does not belong to the newly created favorite.\n\nDo you want to edit the favorite?"),
              mLocalizer.msg("dlg.title.warning","Warning"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null,
              options,
              options[1]) == JOptionPane.YES_OPTION) {
            editFavorite(favorite);
          }
        }
      }
    }
    
    saveFavorites();
  }

  protected void saveFavorites() {
    Thread thread = new Thread("Save favorites") {
      public void run() {
        store();
      }
    };
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }


  public void showExcludeProgramsDialog(Favorite fav, Program program) {
    WizardHandler handler = new WizardHandler(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), new ExcludeWizardStep(fav, program));
    Object exclusion = handler.show();
    
    if (exclusion != null) {
      if(fav == null) {
        Exclusion[] exclusionArr = new Exclusion[mExclusions.length + 1];
        System.arraycopy(mExclusions,0,exclusionArr,0,mExclusions.length);
        exclusionArr[mExclusions.length] = (Exclusion)exclusion;
        
        setGlobalExclusions(exclusionArr);
      }else {
        if(exclusion instanceof Exclusion) {
          fav.addExclusion((Exclusion)exclusion);
        }
        else if(exclusion instanceof String && exclusion.equals("blacklist")) {
          fav.addToBlackList(program);
        }
      }
    }
  }


  public void askAndDeleteFavorite(Favorite fav) {
    if (JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
              mLocalizer.msg("reallyDelete", "Really delete favorite '{0}'?",fav.getName()),
              Localizer.I18N_DELETE,
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      FavoriteTreeModel.getInstance().deleteFavorite(fav);
      
      saveFavorites();
    }
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon(ICON_CATEGORY, ICON_NAME, 16);
  }


  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public void updateRootNode(boolean save) {
    mRootNode.removeAllActions();
    mRootNode.getMutableTreeNode().setIcon(getFavoritesIcon(16));

    Action manageFavorite = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog(false, null);
      }
    };
    manageFavorite.putValue(Action.SMALL_ICON, getFavoritesIcon(16));
    manageFavorite.putValue(Action.NAME, mLocalizer.msg("favoritesManager", "Manage Favorites"));


    Action addFavorite = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        showCreateFavoriteWizard(null);
      }
    };
    addFavorite.putValue(Action.SMALL_ICON, getIconFromTheme("actions", "document-new", 16));
    addFavorite.putValue(Action.NAME, mLocalizer.msg("new", "Create new favorite"));

    Action openSettings = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.FAVORITE);
      }
    };
    openSettings.putValue(Action.SMALL_ICON, getIconFromTheme("categories", "preferences-system", 16));
    openSettings.putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_SETTINGS));
    
    mRootNode.addAction(manageFavorite);
    mRootNode.addAction(addFavorite);
    mRootNode.addAction(null);
    mRootNode.addAction(openSettings);
    mRootNode.removeAllChildren();

    FavoriteTreeModel.getInstance().updatePluginTree(mRootNode);

    mRootNode.update();
    ReminderPlugin.getInstance().updateRootNode(mHasRightToSave);
    
    if(save && mHasRightToSave) {
      saveFavorites();
    }
  }

  public ImageIcon getFavoritesIcon(int size) {
    return getIconFromTheme(ICON_CATEGORY, ICON_NAME, size);
  }

	public ProgramReceiveTarget[] getClientPluginTargetIds() {
    return mClientPluginTargets;
  }
  
  public void setClientPluginTargets(ProgramReceiveTarget[] clientPluginTargetArr) {
    mClientPluginTargets = clientPluginTargetArr;
  }

  public ProgramReceiveTarget[] getDefaultClientPluginsTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>(0);
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && (plugin.canReceivePrograms() || plugin.canReceiveProgramsWithTarget())) {
        list.add(target);
      }
    }
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  public String getId() {
    return DATAFILE_PREFIX;
  }
  
  /**
   * @return The settings of the FavoritesPlugin.
   * @since 2.2.2
   */
  public Properties getSettings() {
    return mSettings;
  }
  
  /**
   * Adds a AdvancedFavorite to the pending list (for loading filters after TV-Browser start was finished).
   * 
   * @param fav The AdvancedFavorite to add.
   * @since 2.5.1
   */
  public void addPendingFavorite(AdvancedFavorite fav) {
    mPendingFavorites.add(fav);
  }
  
  protected boolean isShowingRepetitions() {
    return mSettings.getProperty("showRepetitions","true").compareTo("true") == 0;
  }

  protected void setShowRepetitions(boolean value) {
    mSettings.setProperty("showRepetitions", String.valueOf(value));
  }
  
  /**
   * Gets if reminder should be automatically selected for new Favorites
   * 
   * @return If the reminder should be selected.
   */
  public boolean isAutoSelectingRemider() {
    return mSettings.getProperty("autoSelectReminder","true").compareTo("true") == 0;
  }

  protected void setAutoSelectingReminder(boolean value) {
    mSettings.setProperty("autoSelectReminder", String.valueOf(value));
  }
  
  protected int getMarkPriority() {
    if(mMarkPriority == - 2 && mSettings != null) {
      mMarkPriority = Integer.parseInt(mSettings.getProperty("markPriority",String.valueOf(Program.MIN_MARK_PRIORITY)));
      return mMarkPriority;
    } else {
      return mMarkPriority;
    }
  }
  
  protected void setMarkPriority(int priority) {
    mMarkPriority = priority;
    
    Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
    
    for(Favorite favorite: favoriteArr) {
      Program[] programs = favorite.getWhiteListPrograms();
      
      for(Program program : programs) {
        program.validateMarking();
      }
    }
    
    mSettings.setProperty("markPriority",String.valueOf(priority));
    
    saveFavorites();
  }
  
  public String toString() {
    return mLocalizer.msg("manageFavorites","Favorites");
  }
  
  private static class ReceiveTargetItem {
    private ProgramReceiveTarget mTarget;
    private ArrayList<Program> mProgramsList;
    
    protected ReceiveTargetItem(ProgramReceiveTarget target) {
      mTarget = target;
      mProgramsList = new ArrayList<Program>(0);
    }
    
    protected void addPrograms(Program[] programs) {
      for(Program p : programs) {
        if(!mProgramsList.contains(p)) {
          mProgramsList.add(p);
        }
      }
    }
    
    protected ProgramReceiveTarget getReceiveTarget() {
      return mTarget;
    }
    
    protected Program[] getPrograms() {
      return mProgramsList.toArray(new Program[mProgramsList.size()]);
    }
    
    public String toString() {
      return mTarget.toString();
    }
  }
  
  private String getKeyForReceiveTarget(ProgramReceiveTarget target) {
    if(target != null) {
      return target.getReceiveIfId() + "###" + target.getTargetId();
    }
    
    return null;
  }
  
  protected void setGlobalExclusions(Exclusion[] exclusions) {
    mExclusions = exclusions;
    
    new Thread("globalFavoriteExclusionRefreshThread") {
      public void run() {
        setPriority(Thread.MIN_PRIORITY);
        handleTvDataUpdateFinished();
      }
    }.start();
  }
  
  /**
   * Gets the global exclusions.
   * <p>
   * @return The global exclusions.
   */
  public Exclusion[] getGlobalExclusions() {
    return mExclusions;
  }
  
  private class UpdateInfoThread extends Thread {
    private Favorite[] mFavoriteArr;
    
    protected UpdateInfoThread(Favorite[] favArr) {
      super("Manage favorites");
      mFavoriteArr = favArr;
    }
    
    public void run() {
      while(Settings.propAutoDataDownloadEnabled.getBoolean() && (!MainFrame.getInstance().isVisible() || MainFrame.getInstance().getExtendedState() == JFrame.ICONIFIED) && !mIsShuttingDown && !mShowInfoDialog) {
        try {
          sleep(5000);
        }catch(Exception e) {
          mUpdateInfoThreads.remove(this);
          
          if(mUpdateInfoThreads.isEmpty()) {
            mShowInfoDialog = false;
          }
          
          return;
        }
      }
      
      showDialog();
      
      mUpdateInfoThreads.remove(this);
      
      if(mUpdateInfoThreads.isEmpty()) {
        mShowInfoDialog = false;
      }
    }
    
    protected void showDialog() {
      showManageFavoritesDialog(true, mFavoriteArr);
    }
  }
  
  public void showInfoDialog() {
    mShowInfoDialog = true;
  }
}
