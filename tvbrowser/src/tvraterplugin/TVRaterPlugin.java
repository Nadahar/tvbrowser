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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.Icon;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin gives the User the possibility to rate a Movie
 * 
 * TODO: Get Personal Ratings from Server
 * TODO: Send Original-Titles to Server 
 * TODO: Rate Episodes not the whole Series 
 * 
 * @author Bodo Tasche
 */
public class TVRaterPlugin extends devplugin.Plugin {
    public final static int MINLENGTH = 15;
    
    private Properties _settings;

    private Point _locationRaterDialog = null;

    private Point _locationOverviewDialog = null;

    private Dimension _dimensionOverviewDialog = null;

    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(TVRaterPlugin.class);

    private Database _tvraterDB = new Database();

    /** Instance of this Plugin */
    private static TVRaterPlugin _tvRaterInstance;

    public TVRaterPlugin() {
        _tvRaterInstance = this;
    }
    
    public String getContextMenuItemText() {
        return mLocalizer.msg("contextMenuText", "View rating");
    }

    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "TV Rater");
        String desc = mLocalizer
                .msg(
                        "description",
                        "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 57));
    }

    /**
     * This method is invoked by the host-application if the user has choosen
     * your plugin from the menu.
     */
    public void execute() {
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
            dlg.show();
        } else {
            dlg.setSize(350, 250);
            UiUtilities.centerAndShow(dlg);
            _locationOverviewDialog = dlg.getLocation();
            _dimensionOverviewDialog = dlg.getSize();
        }

    }

    public void execute(Program program) {
        DialogRating dlg = new DialogRating(getParentFrame(), this, program);
        dlg.pack();
        dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentMoved(ComponentEvent e) {
                e.getComponent().getLocation(_locationRaterDialog);
            }
        });

        if (_locationRaterDialog != null) {
            dlg.setLocation(_locationRaterDialog);
            dlg.show();
        } else {
            UiUtilities.centerAndShow(dlg);
            _locationRaterDialog = dlg.getLocation();
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

		System.out.println("Starte TVRaterPlugin");
        if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) == 2) {
            updateDB();
        }
    }

    public SettingsTab getSettingsTab() {
        return new TVRaterSettingsTab(_settings);
    }

    public String getMarkIconName() {
        return "tvraterplugin/tvrater.gif";
    }

    public String getButtonText() {
        return mLocalizer.msg("pluginName", "TV Rater");
    }

    public String getButtonIconName() {
        return "tvraterplugin/tvrater.gif";
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
        Rating rating;

        if (_settings.getProperty("ownRating", "").equalsIgnoreCase("true")) {
            rating = _tvraterDB.getPersonalRating(program);
            if (rating != null) {
                Icon[] iconArray = { RatingIconTextFactory.getImageIconForRating(rating.getIntValue(Rating.OVERALL))}; 
                return iconArray;
            }
        }

        rating = _tvraterDB.getOverallRating(program);
        if (rating != null) {
            Icon[] iconArray = { RatingIconTextFactory.getImageIconForRating(rating.getIntValue(Rating.OVERALL))};
            return iconArray;
        }

        if (_settings.getProperty("ownRating", "").equalsIgnoreCase("false")) {
            rating = _tvraterDB.getPersonalRating(program);
            if (rating != null) {
                Icon[] iconArray = {RatingIconTextFactory.getImageIconForRating(rating.getIntValue(Rating.OVERALL))};
                return iconArray;
            }
        }

        return null;
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

    /**
     * Calls Update-Thread when the TvData has Changed
     */
    public void handleTvDataChanged() {
        if (Integer.parseInt(_settings.getProperty("updateIntervall", "3")) < 3) {
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
    
}