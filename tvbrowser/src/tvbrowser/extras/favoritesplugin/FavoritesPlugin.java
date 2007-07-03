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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

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
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTree;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.NullProgressMonitor;
import devplugin.*;

/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin {

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
  
  private boolean mHasRightToSave = true;

  private Favorite[] mUpdateFavorites;

  private Hashtable<ProgramReceiveTarget,ArrayList<Program>> mSendPluginsTable = new Hashtable<ProgramReceiveTarget,ArrayList<Program>>();
  private ProgramReceiveTarget[] mClientPluginTargets;

  private ArrayList<AdvancedFavorite> mPendingFavorites;
  private int mMarkPriority = -2;
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  private FavoritesPlugin() {
    mInstance = this;
    mPendingFavorites = new ArrayList<AdvancedFavorite>(); 
    mClientPluginTargets = new ProgramReceiveTarget[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    load();
    mRootNode = new PluginTreeNode(mLocalizer.msg("manageFavorites","Favorites"));
    updateRootNode(false);

    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateStarted() {
        mHasRightToSave = false;
      }

      public void tvDataUpdateFinished() {
        handleTvDataUpdateFinsihed();
      }
    });
  }

  private void handleTvDataUpdateFinsihed() {
    mHasToUpdate = true;

    if(mHasRightToUpdate) {
      mHasToUpdate = false;
      
      updateAllFavorites();
      FavoriteTree.getInstance().updateUI();

      mHasRightToSave = true;      
      updateRootNode(true);

      ArrayList<Favorite> showInfoFavorites = new ArrayList<Favorite>();

      Favorite[] favoriteArr = FavoriteTree.getInstance().getFavoriteArr();
      
      for (Favorite favorite : favoriteArr) {
        if (favorite.isRemindAfterDownload() && favorite.getNewPrograms().length > 0) {
          showInfoFavorites.add(favorite);
        }
      }

      if(!showInfoFavorites.isEmpty()) {
        mUpdateFavorites = showInfoFavorites.toArray(new Favorite[showInfoFavorites.size()]);

        new Thread("Manage favorites") {
          public void run() {
            showManageFavoritesDialog(true, mUpdateFavorites);
          }
        }.start();
      }
    }
  }
  
  public static FavoritesPlugin getInstance() {
    if (mInstance == null) {
      new FavoritesPlugin();
    }
    return mInstance;
  }
  
  public void handleTvBrowserStartFinished() {
    if(!mPendingFavorites.isEmpty()) {
      for(AdvancedFavorite fav : mPendingFavorites) {
        fav.loadPendingFilter();
      }
      
      mPendingFavorites.clear();
    }
    
    mHasRightToUpdate = true;
    
    if(mHasToUpdate) {
      handleTvDataUpdateFinsihed();
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
           * this should be stay at least until version 3.0
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
        FavoriteTree.create(newFavoriteArr);
    }
    else {
      FavoriteTree.create(in,version);
      newFavoriteArr = FavoriteTree.getInstance().getFavoriteArr();
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
      ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>(); 
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
  }


  private void updateAllFavorites() {
    mSendPluginsTable.clear();
    
    ProgressMonitor monitor;
    
    Favorite[] favoriteArr = FavoriteTree.getInstance().getFavoriteArr();
    
    if (favoriteArr.length > 5) {    // if we have more then 5 favorites, we show a progress bar
      try {
        monitor = MainFrame.getInstance().createProgressMonitor();
      }catch(Exception e) {
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
    Set<ProgramReceiveTarget> targets = mSendPluginsTable.keySet();
    StringBuffer buffer = new StringBuffer();
    
    for(ProgramReceiveTarget target : targets) {
      ArrayList<Program> list = mSendPluginsTable.get(target);
      
      if(!target.getReceifeIfForIdOfTarget().receivePrograms(list.toArray(new Program[list.size()]),target)) {
        buffer.append(target.getReceifeIfForIdOfTarget().toString()).append(" - ").append(target.toString()).append("\n");
      }
    }
    
    if(buffer.length() > 0) {
      buffer.insert(0,mLocalizer.msg("sendError","Error by sending programs to other plugins.\n\nPlease check the favorites that should send\nprograms to the following plugins:\n"));
      
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),buffer.toString(),Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
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
        ArrayList<Program> list = mSendPluginsTable.get(target);
      
        if(list == null) {
          list = new ArrayList<Program>();        
          mSendPluginsTable.put(target, list);
        }
      
        for(Program program : programs) {
          if(!list.contains(program)) {
            list.add(program);
          }
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
    out.writeInt(6); // version

    FavoriteTree.getInstance().storeData(out);

    out.writeInt(mClientPluginTargets.length);
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      target.writeData(out);
    }

    out.writeBoolean(mShowInfoOnNewProgramsFound);
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
      res = Integer.parseInt(prop.getProperty(key, "" + defaultValue));
    } catch (NumberFormatException e) {
      // ignore
    }
    return res;
  }

  public ActionMenu getButtonAction(final Frame parentFrame) {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog(false, null);
      }
    });

    action.setBigIcon(getIconFromTheme("apps", "bookmark", 22));
    action.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
    action.setShortDescription(mLocalizer.msg("favoritesManager",
            "Manage favorite programs"));
    action.setText(mLocalizer.msg("buttonText", "Manage Favorites"));

    return new ActionMenu(action);
  }


  protected ActionMenu getContextMenuActions(Program program) {
    return new ContextMenuProvider(FavoriteTree.getInstance().getFavoriteArr()).getContextMenuActions(program);
  }



  public void editFavorite(Favorite favorite) {

    Component parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    EditFavoriteDialog dlg;
    if (parent instanceof Dialog) {
      dlg = new EditFavoriteDialog((Dialog)parent, favorite);
    }
    else {
      dlg = new EditFavoriteDialog((Frame)parent, favorite);
    }
    UiUtilities.centerAndShow(dlg);
    if (dlg.getOkWasPressed()) {
      updateRootNode(true);
    }
  }

  private void showManageFavoritesDialog(final boolean showNew, Favorite[] favoriteArr) {
    int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",
            200);
    int width = getIntegerSetting(mSettings, "width", 500);
    int height = getIntegerSetting(mSettings, "height", 300);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(MainFrame.getInstance(), favoriteArr, splitPanePosition, showNew);
    dlg.setSize(new Dimension(width, height));
        
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

    UiUtilities.centerAndShow(dlg);

    splitPanePosition = dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition", "" + splitPanePosition);
    mSettings.setProperty("width", "" + dlg.getWidth());
    mSettings.setProperty("height", "" + dlg.getHeight());
    
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

    Component parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    Favorite favorite;
    if (isUsingExpertMode()) {
      favorite = new AdvancedFavorite(program != null ? program.getTitle() : "");
      EditFavoriteDialog dlg;
      if (parent instanceof Dialog) {
        dlg = new EditFavoriteDialog((Dialog)parent, favorite);
      }
      else {
        dlg = new EditFavoriteDialog((Frame)parent, favorite);
      }
      UiUtilities.centerAndShow(dlg);
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler = new WizardHandler(parent, new TypeWizardStep(program));
      favorite = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    }

    if (favorite != null) {
      try {        
        favorite.updatePrograms();
        FavoriteTree.getInstance().addFavorite(favorite);
        
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
    
    new Thread("Save favorites") {
      public void run() {
        store();
      }
    }.start();
  }


  public void showExcludeProgramsDialog(Favorite fav, Program program) {
    WizardHandler handler = new WizardHandler(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), new ExcludeWizardStep(fav, program));
    Object exclusion = handler.show();    
    if (exclusion != null) {
      if(exclusion instanceof Exclusion) {
        fav.addExclusion((Exclusion)exclusion);
        try {
          fav.refreshPrograms();
          updateRootNode(true);
        } catch (TvBrowserException exc) {
          ErrorHandler.handle(mLocalizer.msg("couldNotUpdateFavorites","Could not update favorites."), exc);
        }
      }
      else if(exclusion instanceof String && exclusion.equals("blacklist")) {
        fav.addToBlackList(program);
      }
    }
  }


  public void askAndDeleteFavorite(Favorite fav) {
    if (JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
              mLocalizer.msg("reallyDelete", "Really delete favorite '{0}'?",fav.getName()),
              mLocalizer.msg("delete", "Delete selected favorite..."),
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      FavoriteTree.getInstance().deleteFavorite(fav);
      
      new Thread("Save favorites") {
        public void run() {
          store();
        }
      }.start();
    }
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "bookmark", 16);
  }


  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public void updateRootNode(boolean save) {
    mRootNode.removeAllActions();

    Action manageFavorite = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog(false, null);
      }
    };
    manageFavorite.putValue(Action.SMALL_ICON, getIconFromTheme("action", "bookmark-new", 16));
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
    openSettings.putValue(Action.SMALL_ICON, getIconFromTheme("categories", "preferences-desktop", 16));
    openSettings.putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_SETTINGS));
    
    mRootNode.addAction(manageFavorite);
    mRootNode.addAction(addFavorite);
    mRootNode.addAction(null);
    mRootNode.addAction(openSettings);
    mRootNode.removeAllChildren();

    FavoriteTree.getInstance().updatePluginTree(mRootNode,null);

    mRootNode.update();
    ReminderPlugin.getInstance().updateRootNode(mHasRightToSave);
    
    if(save && mHasRightToSave) {
      new Thread("Save favorites") {
        public void run() {
          store();
        }
      }.start();
    }
  }
  

	public ProgramReceiveTarget[] getClientPluginTargetIds() {
    return mClientPluginTargets;
  }
  
  public void setClientPluginTargets(ProgramReceiveTarget[] clientPluginTargetArr) {
    mClientPluginTargets = clientPluginTargetArr;
  }

  public ProgramReceiveTarget[] getDefaultClientPluginsTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
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
   * @return The program panel settings for the program list.
   * @since 2.2.2
   */
  public ProgramPanelSettings getProgramPanelSettings() {
    return new ProgramPanelSettings(Integer.parseInt(mSettings.getProperty("pictureType","0")), Integer.parseInt(mSettings.getProperty("pictureTimeRangeStart","1080")), Integer.parseInt(mSettings.getProperty("pictureTimeRangeEnd","1380")), false, mSettings.getProperty("pictureShowsDescription","true").compareTo("true") == 0, Integer.parseInt(mSettings.getProperty("pictureDuration","10")), mSettings.getProperty("picturePlugins","").split(";;"));
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
    
    Favorite[] favoriteArr = FavoriteTree.getInstance().getFavoriteArr();
    
    for(Favorite favorite: favoriteArr) {
      Program[] programs = favorite.getWhiteListPrograms();
      
      for(Program program : programs) {
        program.validateMarking();
      }
    }
    
    mSettings.setProperty("markPriority",String.valueOf(priority));
    
    new Thread("Save favorites") {
      public void run() {
        store();
      }
    }.start();
  }
  
  public String toString() {
    return mLocalizer.msg("manageFavorites","Favorites");
  }  
}
