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
*
* CVS information:
*  $RCSfile$
*   $Source$
*     $Date$
*   $Author$
* $Revision$
*/
package webplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;

import util.browserlauncher.Launch;
import util.paramhandler.ParamParser;
import util.program.ProgramUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin is a generic Web-Tool.
 * A User can configure his favorite Search-Engines and search for the given Movie
 */
public class WebPlugin extends Plugin {
  private static final Version mVersion = new Version(3,0);

  private static final Logger mLog = java.util.logging.Logger
  .getLogger(WebPlugin.class.getName());

  private static final String CHANNEL_SITE = "channelSite";
  private static final String PROGRAM_SITE = "programSite";

/** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(WebPlugin.class);

  /** parameter to be replaced by all searchable strings */
  private static final String WEBSEARCH_ALL = "anytext";

  /** Default-Addresses */
  final static WebAddress[] DEFAULT_ADRESSES = {
      new WebAddress("OFDb", "http://www.ofdb.de/view.php?page=suchergebnis&Kat=All&SText={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", null, false, true),
      new WebAddress("IMDb", "http://akas.imdb.com/find?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", null, false, true),
      new WebAddress("Zelluloid", "http://zelluloid.de/suche/index.php3?qstring={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, true),
      new WebAddress("Google", "http://www.google.com/search?q=%22{urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}%22", null, false, true),
      new WebAddress("Altavista", "http://de.altavista.com/web/results?q=%22{urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}%22", null, false, true),
      new WebAddress("Yahoo", "http://search.yahoo.com/search?p={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, true),
      new WebAddress("Wikipedia (DE)", "http://de.wikipedia.org/wiki/Spezial:Search?search={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.GERMAN)),
      new WebAddress("Wikipedia (EN)", "http://en.wikipedia.org/wiki/Spezial:Search?search={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.ENGLISH)),
      new WebAddress(mLocalizer.msg("programPage", "Open website of program"),PROGRAM_SITE,null,false,true),
      new WebAddress(mLocalizer.msg("channelPageGeneral", "Open website of channel"),CHANNEL_SITE,null,false,true),
      new WebAddress("moviepilot", "http://www.moviepilot.de/searches?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", null, false, true),
  };

  /** The WebAddresses */
  private ArrayList<WebAddress> mAddresses;

  private boolean mHasRightToDownload = false;

  private static WebPlugin INSTANCE;

  /** list of items to be searched if any searchable item shall be put into the context menu */
  private ArrayList<String> listActors = null;
  private ArrayList<String> listScripts = null;
  private ArrayList<String> listDirectors = null;

  private PluginInfo mPluginInfo;

  /**
   * show all available search items in menu, not only title search
   */
  private boolean mShowDetails = true;

  /**
   * Creates the Plugin
   */
  public WebPlugin() {
    INSTANCE = this;
  }

  /**
   * Returns the Instance of the Plugin
   * @return Plugin-Instance
   */
  public static WebPlugin getInstance() {
    return INSTANCE;
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "web-search", 16);
  }

  public static Version getVersion() {
    return mVersion;
  }

  /**
   * Returns the Plugin-Info
   */
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      mPluginInfo = new PluginInfo(WebPlugin.class, mLocalizer.msg("name", "WebPlugin"),
          mLocalizer.msg("desc","Searches on the Web for a Program"),
          "Bodo Tasche");
    }

    return mPluginInfo;
  }

  /**
   * Loads the Data
   */
  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    mAddresses = new ArrayList<WebAddress>();

    final int version = in.readInt();

    final int size = in.readInt();

    final ArrayList<WebAddress> defaults = new ArrayList<WebAddress>(Arrays
        .asList(DEFAULT_ADRESSES));

    for (int i = 0; i < size;i++) {
      WebAddress newone = new WebAddress(in);

      if (!newone.isUserEntry()) {
        for (int v = 0; v < defaults.size(); v++) {
          final WebAddress def = defaults.get(v);
          // Replace Default Webaddresses with Default Settings
          if (def.getName().equals(newone.getName()) || def.getUrl().equals(newone.getUrl())) {
            // Copy needed Data
            def.setActive(newone.isActive());
            def.setIconFile(newone.getIconFile());
            newone = def;
            defaults.remove(v);
          }
        }
      }

      mAddresses.add(newone);
    }

    for (int i = 0; i < defaults.size();i++) {
      mAddresses.add(defaults.get(i));
    }

    if (version >= 2) {
      mShowDetails = in.readBoolean();
    }
  }

  /**
   * Saves the Data
   */
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(2);
    if (mAddresses == null) {
      createDefaultSettings();
    }

    out.writeInt(mAddresses.size());

    for (int i = 0; i < mAddresses.size(); i++) {
      (mAddresses.get(i)).writeData(out);
    }

    out.writeBoolean(mShowDetails);
  }

  /**
   * Creates the Settings-Tab
   */
  public SettingsTab getSettingsTab() {
    if (mAddresses == null) {
      createDefaultSettings();
    }
    return new WebSettingsTab((JFrame)getParentFrame(), mAddresses, this);
  }


  /**
   * Create the Default-Settings
   */
  private void createDefaultSettings() {
    mAddresses = new ArrayList<WebAddress>();
    final WebAddress test = new WebAddress("Test",
        "http://akas.imdb.com/Tsearch?title={urlencode(title, \"UTF-8\")}",
        null, true, false);
    mAddresses.addAll(Arrays.asList(DEFAULT_ADRESSES));
    mAddresses.add(test);
  }

  /**
   * Creates the Context-Menu-Entries
   */
  public ActionMenu getContextMenuActions(final Program program) {

    if (mAddresses == null) {
      createDefaultSettings();
    }
    Action mainAction = getMainContextMenuAction();
    if (program == getPluginManager().getExampleProgram()) {
    	return new ActionMenu(mainAction);
    }

    final ArrayList<Object> actionList = new ArrayList<Object>();
    listActors = null;

    for (int i = 0; i < mAddresses.size(); i++) {
    	try {
        WebAddress address = mAddresses.get(i);
        String actionName = mLocalizer.msg("SearchOn", "Search on ") + " " + address.getName();

        if (address.getUrl().equals(PROGRAM_SITE)) {
          final String url = program.getTextField(ProgramFieldType.URL_TYPE);
          if (url != null && url.length() > 0) {
            address = new WebAddress(mLocalizer.msg("programPage", "Open page of program"),url,null,false,address.isActive());
            actionName = address.getName();
          }
          else {
            address = null;
          }
        }
        // create address of channel on the fly
        if (address != null && address.getUrl().equals(CHANNEL_SITE)) {
        	final Channel channel = program.getChannel();
          address = new WebAddress(mLocalizer.msg("channelPage",
              "Open page of {0}", channel.getName()), channel.getWebpage(),
              null, false, address.isActive());
        	actionName = address.getName();
/*
        	// automatically add separator if it is the last menu item (as it is by default)
        	if (i == mAddresses.size() - 1) {
        	  actionList.add(ContextMenuSeparatorAction.getInstance());
        	}
*/
        }
        if (address != null && address.isActive()) {
          // create items for a possible sub menu
          if (address.getUrl().contains(WEBSEARCH_ALL) && listActors == null) {
            findSearchItems(program);
          }
          if (address.getUrl().contains(WEBSEARCH_ALL) && (listActors.size() + listDirectors.size() + listScripts.size() > 0) && mShowDetails) {
            final ArrayList<Object> categoryList = new ArrayList<Object>();
            // title
            final WebAddress adrTitle = new WebAddress(address.getName(), address.getUrl().replace(WEBSEARCH_ALL, "\"" + program.getTitle() + "\""), null, false, true);
            categoryList.add(createSearchAction(program, adrTitle, program.getTitle()));
            categoryList.add(ContextMenuSeparatorAction.getDisabledOnTaskMenuInstance());
            createSubMenu(program, address, categoryList, mLocalizer.msg("actor", "Actor"), listActors);
            createSubMenu(program, address, categoryList, mLocalizer.msg("director","Director"), listDirectors);
            createSubMenu(program, address, categoryList, mLocalizer.msg("script","Script"), listScripts);
            if (categoryList.size() == 2) {
              categoryList.remove(1);
            }

            final ActionMenu searchMenu = new ActionMenu(actionName, address.getIcon(), categoryList.toArray());
            actionList.add(searchMenu);
          }
          // create only a single menu item for this search
          else {
            final WebAddress adrTitle = new WebAddress(address.getName(), address.getUrl().replace(WEBSEARCH_ALL, "\"" + program.getTitle() + "\""), null, false, true);
            final AbstractAction action = createSearchAction(program, adrTitle,
                actionName);
            action.putValue(Action.SMALL_ICON, address.getIcon());
            actionList.add(action);
          }
        }
      } catch (RuntimeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    if (actionList.size() == 1) {
      final Object action = actionList.get(0);
      if (action instanceof ActionMenu) {
        return (ActionMenu) action;
      }
      else if (action instanceof Action) {
        return new ActionMenu((Action)action);
      }
    }

    final Object[] actions = new Object[actionList.size()];
    actionList.toArray(actions);
    return new ActionMenu((String)mainAction.getValue(Action.SMALL_ICON), (Icon)mainAction.getValue(Action.NAME), actions);
  }

	private Action getMainContextMenuAction() {
		final Action mainAction = new devplugin.ContextMenuAction();
    mainAction.putValue(Action.NAME, mLocalizer.msg("contextMenu", "Web search"));
    mainAction.putValue(Action.SMALL_ICON, createImageIcon("actions", "web-search", 16));
		return mainAction;
	}

  private void createSubMenu(final Program program, final WebAddress address,
      final ArrayList<Object> categoryList, final String label,
      final ArrayList<String> subItems) {
    if (subItems.size() > 0) {
      AbstractAction[] subActions = new AbstractAction[subItems.size()];
      for (int index = 0; index < subActions.length; index++) {
        final WebAddress modifiedAddress = new WebAddress(address.getName(), address.getUrl().replace(WEBSEARCH_ALL, "\"" + subItems.get(index) + "\""), null, false, true);
        subActions[index] = createSearchAction(program, modifiedAddress, subItems.get(index));
        subActions[index].putValue(Plugin.DISABLED_ON_TASK_MENU, true);
      }
      if (subItems.size() > 1) {
        final ContextMenuAction menuAction = new ContextMenuAction(label);
        final ActionMenu menu = new ActionMenu((String)menuAction.getValue(Action.NAME), (Icon)menuAction.getValue(Action.SMALL_ICON), subActions);
        menuAction.putValue(Plugin.DISABLED_ON_TASK_MENU, true);
        categoryList.add(menu);
      }
      else {
        subActions[0].putValue(Action.NAME, subActions[0].getValue(Action.NAME) + " (" + label +")");
        categoryList.add(subActions[0]);
      }
    }
  }

  private AbstractAction createSearchAction(final Program program,
      final WebAddress address, final String actionName) {
    final WebAddress adr = address;
    final AbstractAction action = new AbstractAction() {

      public void actionPerformed(final ActionEvent evt) {
        openUrl(program, adr);
      }
    };
    action.putValue(Action.NAME, actionName);
    return action;
  }

  private void findSearchItems(final Program program) {
    listActors = new ArrayList<String>();
    listDirectors = new ArrayList<String>();
    listScripts = new ArrayList<String>();
    // director
    final String directorField = program
        .getTextField(ProgramFieldType.DIRECTOR_TYPE);
    if (directorField != null) {
      final String[] directors = directorField.split(",");
      for (String director : directors) {
        addSearchItem(listDirectors, director);
      }
    }
    // script
    final String scriptField = program
        .getTextField(ProgramFieldType.SCRIPT_TYPE);
    if (scriptField != null) {
      final String[] scripts = scriptField.split(",");
      for (String script : scripts) {
        addSearchItem(listScripts, script);
      }
    }
    // actors
    final String[] actors = ProgramUtilities.getActorNames(program);
    if (actors != null) {
      Arrays.sort(actors);
      listActors = new ArrayList<String>();
      // build the final list of sub menus
      for (String actor : actors) {
        if (actor.contains(" ") && !actor.equalsIgnoreCase("und andere") && !listActors.contains(actor)) {
          addSearchItem(listActors, actor);
        }
      }
    }
  }

  private void addSearchItem(final ArrayList<String> list, String search) {
    if (search != null) {
      // remove additional bracket parts from script and director fields
      final int leftBracket = search.indexOf('(');
      final int rightBracket = search.lastIndexOf(')');
      if (leftBracket > 0 && rightBracket > leftBracket) {
        search = search.substring(0, leftBracket);
      }
      search = search.trim();
      if (search.length() > 0) {
        list.add(search);
      }
    }
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    final ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();

    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive()) {
        list.add(new ProgramReceiveTarget(this,mLocalizer.msg("SearchOn", "Search on ") + " " + adr.getName(),adr.getName() + "." + adr.getUrl()));
      }
    }

    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  public boolean receiveValues(final String[] values,
      final ProgramReceiveTarget target) {
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive() && target.isReceiveTargetWithIdOfProgramReceiveIf(this,adr.getName() + "." + adr.getUrl())) {
        for(String value : values) {
          try {
            final String url = adr.getUrl().replaceAll("[{].*[}]",
                URLEncoder.encode(value, "UTF-8").replace("+", "%20"));

            if(url.startsWith("http://")) {
              Launch.openURL(url);
            }
          } catch (UnsupportedEncodingException e) {}
        }

        return true;
      }
    }

    return false;
  }

  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget target) {
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive() && target.isReceiveTargetWithIdOfProgramReceiveIf(this,adr.getName() + "." + adr.getUrl())) {
        for(Program p : programArr) {
          openUrl(p, adr);
        }

        return true;
      }
    }

    return false;
  }

  /**
   * Opens the Address in a browser
   * @param program Program to search on the Web
   * @param address Search-Engine to use
   */
  protected void openUrl(final Program program, final WebAddress address) {
    try {
      final ParamParser parser = new ParamParser();
      final String result = parser.analyse(address.getUrl(), program);

      if (parser.hasErrors()) {
        final String errorString = parser.getErrorString();
        mLog.warning("URL parse error " + errorString+ " in " + address.getUrl());
        parser.showErrors(UiUtilities.getLastModalChildOf(getParentFrame()));
      } else {
        Launch.openURL(result);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handleTvBrowserStartFinished() {
    mHasRightToDownload = true;
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if(mHasRightToDownload) {
      final FavIconFetcher fetcher = new FavIconFetcher();

      if (mAddresses != null) {
        for (WebAddress address : mAddresses) {
          if ((address.getIconFile() == null) && ! address.getUrl().equals(CHANNEL_SITE) && ! address.getUrl().equals(PROGRAM_SITE)) {
            final String file = fetcher.fetchFavIconForUrl(address.getUrl());
            if (file != null) {
              address.setIconFile(file);
            } else {
              address.setIconFile("");
            }
          }
        }
      }

    }
  }

  protected boolean getShowDetailMenus() {
    return mShowDetails;
  }

  protected void setShowDetailMenus(final boolean showDetails) {
    mShowDetails = showDetails;
  }
}