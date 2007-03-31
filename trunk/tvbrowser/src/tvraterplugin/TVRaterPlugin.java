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
import devplugin.Plugin;
import devplugin.PluginInfo;
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
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
    
    public final static int MINLENGTH = 15;
    
    private Properties _settings;

    private Point _locationRaterDialog = null;

    private Point _locationOverviewDialog = null;

    private Dimension _dimensionOverviewDialog = null;
    
    private Dimension _dimensionRaterDialog = null;

    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(TVRaterPlugin.class);

    private Database _tvraterDB = new Database();
    
    private boolean hasRightToDownload = false;

    /** Instance of this Plugin */
    private static TVRaterPlugin _tvRaterInstance;

    public TVRaterPlugin() {
        _tvRaterInstance = this;
    }
    
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "TV Rater");
        String desc = mLocalizer
                .msg(
                        "description",
                        "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
        String author = "Bodo Tasche";
        String helpUrl = mLocalizer.msg("helpUrl", "http://enwiki.tvbrowser.org/index.php/TV_Rater");
        
        return new PluginInfo(name, desc, author, helpUrl, new Version(1, 00));
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
        action.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "View rating"));
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

          if (_locationRaterDialog != null && _dimensionRaterDialog != null) {
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
        
        if(x != -1 && y != -1)
          _locationOverviewDialog = new Point(x,y);
        if(width != -1 && height != -1)
          _dimensionOverviewDialog = new Dimension(width,height);

        x = Integer.parseInt(_settings.getProperty("mRaterXPos","-1"));
        y = Integer.parseInt(_settings.getProperty("mRaterYPos","-1"));
        width = Integer.parseInt(_settings.getProperty("mRaterWidth","-1"));
        height = Integer.parseInt(_settings.getProperty("mRaterHeight","-1"));
        
        if(x != -1 && y != -1)
          _locationRaterDialog = new Point(x,y);
        if(width != -1 && height != -1)
          _dimensionRaterDialog = new Dimension(width,height);
    }

    public SettingsTab getSettingsTab() {
        return new TVRaterSettingsTab(_settings);
    }

    public String getMarkIconName() {
        return "tvraterplugin/imgs/tvrater.png";
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
            rating = _tvraterDB.getPersonalRating(program);
            if (rating != null) {
                return rating;
            }
        }

        return  _tvraterDB.getOverallRating(program);
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
    }
    
    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#handleTvDataUpdateFinished()
     */
    public void handleTvDataUpdateFinished() {
      if (!((_settings.getProperty("name", "").length() == 0) || (_settings.getProperty("password", "").length() == 0)) 
          && hasRightToDownload) {
        if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) < 3)
          updateDB();
      }
    }


    /**
     * Updates the Database
     */
    private void updateDB() {
        final TVRaterPlugin tvrater = this;

        Thread updateThread = new Thread() {
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
    public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
        return new Class[] {TVRaterFilter.class};
    }

    @Override
    public PluginsProgramFilter[] getAvailableFilter() {
        return new PluginsProgramFilter[] {new TVRaterProgramFilter(this)};
    }
}