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
package searchplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ButtonAction;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * Provides a dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchPlugin extends Plugin {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer =
    util.ui.Localizer.getLocalizerFor(SearchPlugin.class);

  private static SearchPlugin mInstance;

  private static SearchFormSettings[] mSearchHistory;


  /**
   * Creates a new instance of SearchPlugin.
   */
  public SearchPlugin() {
    mInstance = this;
  }

  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    int historySize = in.readInt();
    mSearchHistory = new SearchFormSettings[historySize];
    for (int i = 0; i < historySize; i++) {
      SearchFormSettings settings;

      if (version > 1) {
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
          case 0: settings.setMatch(SearchFormSettings.MATCH_EXACTLY); break;
          case 1: settings.setMatch(SearchFormSettings.MATCH_KEYWORD); break;
          case 2: settings.setMatch(SearchFormSettings.MATCH_REGULAR_EXPRESSION); break;
        }
      }
      
      mSearchHistory[i] = settings;
    }
  }


  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    
    if (mSearchHistory == null) {
      out.writeInt(0); // length
    } else {
      out.writeInt(mSearchHistory.length);
      for (int i = 0; i < mSearchHistory.length; i++) {
        mSearchHistory[i].writeData(out);
      }
    }
  }



  public ActionMenu getButtonAction() {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        SearchDialog dlg = new SearchDialog(SearchPlugin.this, getParentFrame());
        UiUtilities.centerAndShow(dlg);
      }
    });

    action.setBigIcon(createImageIcon("searchplugin/Find24.gif"));
    action.setSmallIcon(createImageIcon("searchplugin/Find16.gif"));
    action.setShortDescription(mLocalizer.msg("description","Allows searching programs containing a certain text."));
    action.setText(mLocalizer.msg("searchPrograms", "Search programs"));

    return new ActionMenu(action);
  }

  public ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
     action.setText(mLocalizer.msg("searchRepetion", "Search repetition"));
     action.setSmallIcon(createImageIcon("searchplugin/Find16.gif"));
     action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
          SearchDialog dlg = new SearchDialog(SearchPlugin.this, getParentFrame());
          dlg.setPatternText(program.getTitle());
          UiUtilities.centerAndShow(dlg);
        }
      });
      return new ActionMenu(action);
  }







  public String getMarkIconName() {
    return "searchplugin/Find16.gif";
  }
  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("searchPrograms", "Search programs");
    String desc =
      mLocalizer.msg(
        "description",
        "Allows searching programs containing a certain text.");
    String author = "Til Schneider, www.murfman.de";

    return new PluginInfo(name, desc, author, new Version(1, 5));
  }

  public static SearchPlugin getInstance() {
    return mInstance;
  }
  
  
  public static SearchFormSettings[] getSearchHistory() {
    return mSearchHistory;
  }


  public static void setSearchHistory(SearchFormSettings[] history) {
    mSearchHistory = history;
  }

}