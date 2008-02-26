/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.extras.searchplugin;

import devplugin.ActionMenu;
import devplugin.ButtonAction;
import devplugin.ContextMenuAction;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataDeserializer;
import tvbrowser.extras.common.DataSerializer;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.KeyStroke;

/**
 * Provides a dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchPlugin {

    /**
     * The localizer for this class.
     */
    public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SearchPlugin.class);
    private static String DATAFILE_PREFIX = "searchplugin.SearchPlugin";

    private static SearchPlugin mInstance;

    private ConfigurationHandler mConfigurationHandler;

    private static SearchFormSettings[] mSearchHistory;

    /**
     * Select time in repetition-dialog
     */
    private int mRepetitionTimeSelect = 3;

    /**
     * Creates a new instance of SearchPlugin.
     */
    private SearchPlugin() {
        mInstance = this;
        mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
        load();
    }

    private void load() {
        try {
            mConfigurationHandler.loadData(new DataDeserializer() {
                public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
                    readData(in);
                }
            });
        } catch (IOException e) {
            ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavorites", "Could not load favorites"), e);
        }
    }

    public void store() {
        try {
            mConfigurationHandler.storeData(new DataSerializer() {
                public void write(ObjectOutputStream out) throws IOException {
                    writeData(out);
                }
            });
        } catch (IOException e) {
            ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavorites", "Could not store favorites"), e);
        }
    }

    private void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();

        int historySize = in.readInt();
        mSearchHistory = new SearchFormSettings[historySize];
        for (int i = 0; i < historySize; i++) {
            SearchFormSettings settings;

            if (version > 1) {
                // version 2
                settings = new SearchFormSettings(in);
            } else {
                // version 1
                String searchText = (String) in.readObject();
                in.readBoolean(); // searchInTitle
                boolean searchInInfoText = in.readBoolean();
                boolean caseSensitive = in.readBoolean();
                int option = in.readInt();

                settings = new SearchFormSettings(searchText);
                if (searchInInfoText) {
                    settings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
                } else {
                    settings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
                }
                settings.setCaseSensitive(caseSensitive);
                switch (option) {
                    case 0:
                        settings.setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY);
                        break;
                    case 1:
                        settings.setSearcherType(PluginManager.SEARCHER_TYPE_KEYWORD);
                        break;
                    case 2:
                        settings.setSearcherType(PluginManager.SEARCHER_TYPE_REGULAR_EXPRESSION);
                        break;
                }
            }

            mSearchHistory[i] = settings;
        }

        if (version >= 3 && version <= 4) {
            in.readInt();
            in.readInt();
            in.readInt();
            in.readInt();
            in.readBoolean();

            int n = in.readInt();

            for (int i = 0; i < n; i++) {
                in.readUTF();
            }
        }
        
        if (version >= 4) {
          mRepetitionTimeSelect = in.readInt();
        }

    }

    private void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(5); // version

        if (mSearchHistory == null) {
            out.writeInt(0); // length
        } else {
            out.writeInt(mSearchHistory.length);
            for (int i = 0; i < mSearchHistory.length; i++) {
                mSearchHistory[i].writeData(out);
            }
        }

        out.writeInt(mRepetitionTimeSelect);
    }

    protected ActionMenu getButtonAction() {
        ButtonAction action = new ButtonAction();
        action.setActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchDialog dlg;

                Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());

                if (w instanceof Dialog)
                    dlg = new SearchDialog((Dialog) w);
                else
                    dlg = new SearchDialog((Frame) w);

                UiUtilities.centerAndShow(dlg);
            }
        });

        action.setBigIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 22));
        action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16));
        action.setShortDescription(mLocalizer.msg("description", "Allows searching programs containing a certain text."));
        action.setText(mLocalizer.msg("searchPrograms", "Search programs..."));
        action.putValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));

        return new ActionMenu(action);
    }

    protected ActionMenu getContextMenuActions(final Program program) {
        ContextMenuAction action = new ContextMenuAction();
        action.setText(mLocalizer.msg("searchRepetion", "Search repetition"));
        action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16));
        action.setActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                RepetitionDialog dlg;

                Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());

                if (w instanceof Dialog)
                    dlg = new RepetitionDialog((Dialog) w);
                else
                    dlg = new RepetitionDialog((Frame) w);

                dlg.setPatternText(program.getTitle());
                UiUtilities.centerAndShow(dlg);
            }
        });
        return new ActionMenu(action);
    }

    public ThemeIcon getMarkIconFromTheme() {
        return new ThemeIcon("actions", "system-search", 16);
    }

    public static synchronized SearchPlugin getInstance() {
        if (mInstance == null)
            new SearchPlugin();

        return mInstance;
    }

    public static SearchFormSettings[] getSearchHistory() {
        return mSearchHistory;
    }

    public static void setSearchHistory(SearchFormSettings[] history) {
        mSearchHistory = history;
    }

    /*
    * (non-Javadoc)
    *
    * @see devplugin.Plugin#getSettingsTab()
    */
    public SettingsTab getSettingsTab() {
        return new SearchSettingsTab();
    }

    public String getId() {
        return DATAFILE_PREFIX;
    }

    public String toString() {
        return mLocalizer.msg("title", "Search");
    }

    /**
     * Selec this entry in the RepetitionDialog
     * @return
     */
    public int getRepetitionTimeSelection() {
        return mRepetitionTimeSelect;
    }

    /**
     * Set the selected entry in the RepetitionDialog
     * @param selectedIndex selected entry
     */
    public void setRepetitionTimeSelection(int selectedIndex) {
        mRepetitionTimeSelect = selectedIndex;
    }
}