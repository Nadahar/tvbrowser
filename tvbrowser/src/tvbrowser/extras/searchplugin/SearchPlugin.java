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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataDeserializer;
import tvbrowser.extras.common.DataSerializer;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.settings.ProgramPanelSettings;
import util.ui.PictureSettingsPanel;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ButtonAction;
import devplugin.ContextMenuAction;
import devplugin.PluginInfo;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * Provides a dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchPlugin {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SearchPlugin.class);
  private static String DATAFILE_PREFIX = "searchplugin.SearchPlugin";
  
  private static SearchPlugin mInstance;
  
  private ConfigurationHandler mConfigurationHandler;

  private static SearchFormSettings[] mSearchHistory;
  private ProgramPanelSettings mProgramPanelSettings;

  /**
   * Creates a new instance of SearchPlugin.
   */
  private SearchPlugin() {
    mInstance = this;
    mProgramPanelSettings = new ProgramPanelSettings(PictureSettingsPanel.SHOW_IN_TIME_RANGE,1080,1380,false,true,90,new String[] {});
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    load();
  }

  private void load() {
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
  
  public void store() {
    try {
      mConfigurationHandler.storeData(new DataSerializer(){
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavorites","Could not store favorites"), e);
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

    if(version >= 3) {
      int type = in.readInt();
      int startTime = in.readInt();
      int endTime = in.readInt();
      int duration = in.readInt();
      boolean desc = in.readBoolean();

      String[] ids = new String[in.readInt()];

      for(int i = 0; i < ids.length; i++)
        ids[i] = in.readUTF();

      mProgramPanelSettings = new ProgramPanelSettings(type,startTime,endTime,false,desc,duration,ids);
    }
  }

  private void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version

    if (mSearchHistory == null) {
      out.writeInt(0); // length
    } else {
      out.writeInt(mSearchHistory.length);
      for (int i = 0; i < mSearchHistory.length; i++) {
        mSearchHistory[i].writeData(out);
      }
    }
    out.writeInt(mProgramPanelSettings.getPictureShowingType());
    out.writeInt(mProgramPanelSettings.getPictureTimeRangeStart());
    out.writeInt(mProgramPanelSettings.getPictureTimeRangeEnd());
    out.writeInt(mProgramPanelSettings.getDuration());
    out.writeBoolean(mProgramPanelSettings.isShowingPictureDescription());
    out.writeInt(mProgramPanelSettings.getPluginIds().length);

    for(int i = 0; i < mProgramPanelSettings.getPluginIds().length; i++)
      out.writeUTF(mProgramPanelSettings.getPluginIds()[i]);
  }

  public ActionMenu getButtonAction() {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {        
        SearchDialog dlg;
        
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        
        if(w instanceof Dialog)
          dlg = new SearchDialog((Dialog)w);
        else
          dlg = new SearchDialog((Frame)w);
        
        UiUtilities.centerAndShow(dlg);
      }
    });

    action.setBigIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 22));
    action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16));
    action.setShortDescription(mLocalizer.msg("description", "Allows searching programs containing a certain text."));
    action.setText(mLocalizer.msg("searchPrograms", "Search programs"));

    return new ActionMenu(action);
  }

  protected ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
    action.setText(mLocalizer.msg("searchRepetion", "Search repetition"));
    action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions", "system-search", 16));
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        SearchDialog dlg;
        
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        
        if(w instanceof Dialog)
          dlg = new SearchDialog((Dialog)w);
        else
          dlg = new SearchDialog((Frame)w);
        
        dlg.setPatternText(program.getTitle());
        UiUtilities.centerAndShow(dlg);
      }
    });
    return new ActionMenu(action);
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "system-search", 16);
  }

  public PluginInfo getInfo() {
    String name = mLocalizer.msg("searchPrograms", "Search programs");
    String desc = mLocalizer.msg("description", "Allows searching programs containing a certain text.");
    String author = "Til Schneider, www.murfman.de";

    return new PluginInfo(name, desc, author, new Version(1, 6));
  }

  public static SearchPlugin getInstance() {
    if(mInstance == null)
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

  /**
   * Return the program panel settings for the
   * result list.
   *
   * @return The program panel settings for the list.
   * @since 2.2.2
   */
  public ProgramPanelSettings getProgramPanelSettings() {
    return mProgramPanelSettings;
  }

  /**
   * Sets the program panel settings for the
   * result list.
   *
   * @param value The new program panel settings for the list.
   * @since 2.2.2
   */
  protected void setProgramPanelSettings(ProgramPanelSettings value) {
    mProgramPanelSettings = value;
  }

  public String getId() {
    return DATAFILE_PREFIX;
  }
  public String toString() {
    return mLocalizer.msg("title","Search");
  }
}