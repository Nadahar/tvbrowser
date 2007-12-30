/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */

package tvraterplugin;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This Plugin gives the User the possibility to rate a Movie
 * 
 * TODO: Get Personal Ratings from Server
 * TODO: Send Original-Titles to Server 
 * 
 * @author Bodo Tasche
 */
public class TVRaterPlugin extends devplugin.Plugin {
  private static final Version mVersion = new Version(2,60);
    
    public final static int MINLENGTH = 15;
    
    private Properties _settings;

    private Point _locationRaterDialog = null;

    private Point _locationOverviewDialog = null;

    /**
     * Root-Node for the Program-Tree
     */
    private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

    private Dimension _dimensionOverviewDialog = null;
    
    private Dimension _dimensionRaterDialog = null;

    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(TVRaterPlugin.class);

    /**
     * ID of the favorites plugin for recognition of unrated favorites
     */
    private static final String FAVORITES_PLUGIN_ID = "favoritesplugin.FavoritesPlugin";

    private Database _tvraterDB = new Database();
    
    private boolean hasRightToDownload = false;

    /** Instance of this Plugin */
    private static TVRaterPlugin _tvRaterInstance;
    
    private PluginInfo mPluginInfo;

    /**
     * this class triggers an event when the main frame gets available, that is after
     * activation of all plugins
     * 
     * @author bananeweizen
     *
     */
    private class LateActivationAction implements ActionListener {
      public void actionPerformed(ActionEvent e) {
        if (null == getParentFrame()) {
          return;
        }
        lateActivationSwingTimer.stop();
        onLateActivation();
      }
    }
    
    private final Timer lateActivationSwingTimer = new Timer(200, new LateActivationAction());

    /**
     * flag indicating that the host program has started
     */
    private boolean startFinished;

    public TVRaterPlugin() {
        _tvRaterInstance = this;
        mRootNode.getMutableTreeNode().setIcon(new ImageIcon(ImageUtilities.createImageFromJar("tvraterplugin/imgs/missingrating.png", TVRaterPlugin.class)));
    }
    
    public static Version getVersion() {
      return mVersion;
    }
    
    public PluginInfo getInfo() {
      if(mPluginInfo == null) {
        String name = mLocalizer.msg("pluginName", "TV Rater");
        String desc = mLocalizer
                .msg(
                        "description",
                        "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
        String author = "Bodo Tasche";
        
        mPluginInfo = new PluginInfo(TVRaterPlugin.class, name, desc, author);
      }
      
      return mPluginInfo;
    }

    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getButtonAction()
     */
    public ActionMenu getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("pluginName", "TV Rater"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("tvraterplugin/imgs/tvrater.png", TVRaterPlugin.class)));
        action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("tvraterplugin/imgs/tvrater22.png", TVRaterPlugin.class)));
        
        return new ActionMenu(action);
    }    
    
    /**
     * This method is invoked by the host-application if the user has choosen
     * your plugin from the menu.
     */
    public void showDialog() {
      if ((_settings.getProperty("name", "").length() == 0) || (_settings.getProperty("password", "").length() == 0)) {
        showNotConfigured();
      } else {
        DialogOverview dlg = new DialogOverview(getParentFrame(), this);
        dlg.pack();
        dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                _dimensionOverviewDialog = e.getComponent().getSize();
            }

            public void componentMoved(ComponentEvent e) {
                e.getComponent().getLocation(_locationOverviewDialog);
            }
        });

        if ((_locationOverviewDialog != null)
                && (_dimensionOverviewDialog != null)) {
            dlg.setLocation(_locationOverviewDialog);
            dlg.setSize(_dimensionOverviewDialog);
            dlg.setVisible(true);
        } else {
            dlg.setSize(350, 250);
            UiUtilities.centerAndShow(dlg);
            _locationOverviewDialog = dlg.getLocation();
            _dimensionOverviewDialog = dlg.getSize();
        }
      }
    }

    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showRatingDialog(program);
            }
        };
        if (getRating(program) != null) {
          action.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "View rating"));
        }
        else {
          action.putValue(Action.NAME, mLocalizer.msg("contextNoRating", "Rate program"));
        }
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("tvraterplugin/imgs/tvrater.png", TVRaterPlugin.class)));
        
        return new ActionMenu(action);
    }
    
    
    public void showRatingDialog(Program program) {
        if ((_settings.getProperty("name", "").length() == 0) || (_settings.getProperty("password", "").length() == 0)) {
          showNotConfigured();
        } else {
          DialogRating dlg = new DialogRating(getParentFrame(), this, program);
          dlg.pack();
          dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

              public void componentMoved(ComponentEvent e) {
                  e.getComponent().getLocation(_locationRaterDialog);
                  e.getComponent().getSize(_dimensionRaterDialog);
              }
          });

          if (_locationRaterDialog != null && _dimensionRaterDialog != null && _dimensionRaterDialog.width >= dlg.getWidth() && _dimensionRaterDialog.height >= dlg.getHeight()) {
              dlg.setLocation(_locationRaterDialog);
              dlg.setSize(_dimensionRaterDialog);
              dlg.setVisible(true);
          } else {
              UiUtilities.centerAndShow(dlg);
              _locationRaterDialog = dlg.getLocation();
              _dimensionRaterDialog = dlg.getSize();
          }
        }
      
    }

    /**
     * Show a Information-Dialog if the Plugin was not configured yet
     */
    private void showNotConfigured() {
      int ret = JOptionPane.showConfirmDialog(getParentFrame(), 
          mLocalizer.msg("noUserText", "No User specified. Do you want to do this now?"), 
          mLocalizer.msg("noUserTitle", "No User specified"),
          JOptionPane.YES_NO_OPTION);
      
      if (ret == JOptionPane.YES_OPTION) {
        getPluginManager().showSettings(this);
      }
    }

    public Properties storeSettings() {
        return _settings;
    }

    public void loadSettings(Properties settings) {
        if (settings == null) {
            settings = new Properties();
        }

        this._settings = settings;

		    if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) == 2) {
            updateDB();
        }
        
        int x = Integer.parseInt(_settings.getProperty("mOverviewXPos","-1"));
        int y = Integer.parseInt(_settings.getProperty("mOverviewYPos","-1"));
        int width = Integer.parseInt(_settings.getProperty("mOverviewWidth","-1"));
        int height = Integer.parseInt(_settings.getProperty("mOverviewHeight","-1"));
        
        if(x != -1 && y != -1) {
          _locationOverviewDialog = new Point(x,y);
        }
        if(width != -1 && height != -1) {
          _dimensionOverviewDialog = new Dimension(width,height);
        }

        x = Integer.parseInt(_settings.getProperty("mRaterXPos","-1"));
        y = Integer.parseInt(_settings.getProperty("mRaterYPos","-1"));
        width = Integer.parseInt(_settings.getProperty("mRaterWidth","-1"));
        height = Integer.parseInt(_settings.getProperty("mRaterHeight","-1"));
        
        if(x != -1 && y != -1) {
          _locationRaterDialog = new Point(x,y);
        }
        if(width != -1 && height != -1) {
          _dimensionRaterDialog = new Dimension(width,height);
        }
    }

    public SettingsTab getSettingsTab() {
        return new TVRaterSettingsTab(_settings);
    }

    public String getMarkIconName() {
        return "tvraterplugin/imgs/missingrating.png";
    }

    /**
     * Gets the description text for the program table icons provided by this
     * Plugin.
     * <p>
     * Return <code>null</code> if your plugin does not provide this feature.
     * 
     * @return The description text for the program table icons.
     * @see #getProgramTableIcons(Program)
     */
    public String getProgramTableIconText() {
        return mLocalizer.msg("icon", "Rating");
    }

    /**
     * Gets the icons this Plugin provides for the given program. These icons
     * will be shown in the program table under the start time.
     * <p>
     * Return <code>null</code> if your plugin does not provide this feature.
     * 
     * @param program
     *            The programs to get the icons for.
     * @return The icons for the given program or <code>null</code>.
     * @see #getProgramTableIconText()
     */
    public Icon[] getProgramTableIcons(Program program) {
        Rating  rating =getRating(program);

        if (rating != null) {
            return new Icon[] { RatingIconTextFactory.getImageIconForRating(rating.getIntValue(Rating.OVERALL))};
        }

        return null;
    }

    /**
     * Returns the Rating for a program.
     * This Function returns the personal rating if the settings say these ratings
     * are preferred
     *
     * @param program Get rating for this program
     * @return Rating
     */
    public Rating getRating(Program program) {
        Rating rating;

        if (_settings.getProperty("ownRating", "").equalsIgnoreCase("true")) {
          rating = getPersonalRating(program);
          if (rating != null) {
            return rating;
          }
        }

        return  _tvraterDB.getOverallRating(program);
    }

    /**
     * Get the personal rating for the given program
     * 
     * @param program
     * @return personal rating or <code>null</code> if no personal rating is available
     * @since 2.6 
     */
    private Rating getPersonalRating(Program program) {
      return _tvraterDB.getPersonalRating(program);
    }

    /**
     * Returns the Database for the Ratings
     * 
     * @return Rating-Database
     */
    public Database getDatabase() {
        return _tvraterDB;
    }

    /**
     * Returns the Settings for this Plugin
     * 
     * @return Settings
     */
    public Properties getSettings() {
      if (_locationOverviewDialog != null) {
        _settings.setProperty("mOverviewXPos",String.valueOf(_locationOverviewDialog.x));
        _settings.setProperty("mOverviewYPos",String.valueOf(_locationOverviewDialog.y));
      }
      if (_dimensionOverviewDialog != null) {
      _settings.setProperty("mOverviewWidth",String.valueOf(_dimensionOverviewDialog.width));
      _settings.setProperty("mOverviewHeight",String.valueOf(_dimensionOverviewDialog.height));
      }
      if (_locationRaterDialog != null) {
        _settings.setProperty("mRaterXPos",String.valueOf(_locationRaterDialog.x));
        _settings.setProperty("mRaterYPos",String.valueOf(_locationRaterDialog.y));
      }
      if (_dimensionRaterDialog != null) {
        _settings.setProperty("mRaterWidth",String.valueOf(_dimensionRaterDialog.width));
        _settings.setProperty("mRaterHeight",String.valueOf(_dimensionRaterDialog.height));
      }
      
      return _settings;
    }

    /**
     * Called by the host-application during start-up.
     * 
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        _tvraterDB.readData(in);
    }

    /**
     * Counterpart to loadData. Called when the application shuts down.
     * 
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        _tvraterDB.writeData(out);
    }

    /**
     * Gets the parent frame.
     * <p>
     * The parent frame may be used for showing dialogs.
     * 
     * @return The parent frame.
     */
    public java.awt.Frame getParentFrameForTVRater() {

        return getParentFrame();
    }

    public void handleTvBrowserStartFinished() {
      hasRightToDownload = true;
      startFinished = true;
    }
    
    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#handleTvDataUpdateFinished()
     */
    public void handleTvDataUpdateFinished() {
      if (!((_settings.getProperty("name", "").length() == 0) || (_settings.getProperty("password", "").length() == 0)) 
          && hasRightToDownload) {
        if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) < 3) {
          updateDB();
        }
      }
    }


    /**
     * Updates the Database
     */
    private void updateDB() {
        final TVRaterPlugin tvrater = this;

        Thread updateThread = new Thread("TV Rater update") {
            public void run() {
                Updater up = new Updater(tvrater);
                up.run();
            }
        };
        updateThread.start();
    }
    
    /**
     * Returns an Instance of this Plugin
     * 
     * @return Instance of this Plugin
     */
    public static TVRaterPlugin getInstance() {
        return _tvRaterInstance;
    }

    /**
     * Returns true if Program is rateable (Length > MINLENGTH or last Program of Day) 
     * @param program Program to check
     * @return true if program is rateable
     */
    public boolean isProgramRateable(Program program) {
        if ((program.getTitle() != null) && (program.getLength() >= TVRaterPlugin.MINLENGTH)) {
            return true;
        }
        
        if ((program.getTitle() != null) && (program.getLength() <= 0)) {
            program.getChannel();
            
            Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(program.getDate(), program.getChannel());
            
            Program last = null;
            
            while ((it != null) && (it.hasNext())) {
                last = it.next();
            }
            
            if (program == last) {
                return true;
            }
            
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
      //manually cast to avoid unsafe compiler cast
      return (Class<? extends PluginsFilterComponent>[]) new Class[] {TVRaterFilter.class};
    }

    @Override
    public PluginsProgramFilter[] getAvailableFilter() {
        return new PluginsProgramFilter[] {new TVRaterProgramFilter(this)};
    }

    /**
     * Force an update of the currently shown programs in the program table
     * where we need to add/update a TV rating.
     * 
     * Internally called after a successful update of the TV ratings database.
     * 
     * @since 2.6
     */
    public void updateCurrentDate() {
      // dont update the UI if the rating updater runs on TV-Browser start
      if (! startFinished) {
        return;
      }
      Date currentDate = getPluginManager().getCurrentDate();
      final Channel[] channels = getPluginManager().getSubscribedChannels();
      for (int i = 0; i < channels.length; ++i) {
        final Iterator<Program> iter = getPluginManager().getChannelDayProgram(currentDate, channels[i]);
        if (null == iter) {
          continue;
        }
        while (iter.hasNext()) {
          Program prog = iter.next();
          if (getRating(prog) != null) { 
            prog.validateMarking();
          }
        }
      }
    }

    @Override
    public boolean canUseProgramTree() {
      return true;
    }

    @Override
    public PluginTreeNode getRootNode() {
      return mRootNode;
    }

    /**
     * collect all expired favorites without rating for the plugin tree
     * 
     * @since 2.6
     */
    protected void updateRootNode() {
      mRootNode.removeAllChildren();
      mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);
      PluginTreeNode favoritesNode = mRootNode.addNode(mLocalizer.msg("unratedFavorites", "Unrated favorites"));
      favoritesNode.setGroupingByDateEnabled(false);
      Program[] programs = getPluginManager().getMarkedPrograms();
      
      // search all unrated favorites
      List<Program> unratedFavs = new ArrayList<Program>();
      for (int progIndex = 0; progIndex < programs.length; progIndex++) {
        Program program = programs[progIndex];
        if (program.isExpired()) {
          Marker[] markers = program.getMarkerArr();
          for (int markerIndex = 0; markerIndex < markers.length; markerIndex++) {
            if (markers[markerIndex].getId().equalsIgnoreCase(FAVORITES_PLUGIN_ID)) {
              if (getPersonalRating(program) == null) {
                unratedFavs.add(program);
              }
              break;
            }
          }
        }
      }
      favoritesNode.addPrograms(unratedFavs);
      mRootNode.update();
    }

    @Override
    public void handleTvDataDeleted(ChannelDayProgram oldProg) {
      updateRootNode();
    }

    public void onLateActivation() {
      updateRootNode();
    }

    @Override
    public void onActivation() {
      // from now on check regularly for the existence of the main frame
      lateActivationSwingTimer.start();
    }
}