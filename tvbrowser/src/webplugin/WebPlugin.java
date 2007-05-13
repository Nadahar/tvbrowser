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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.browserlauncher.Launch;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
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

  private static java.util.logging.Logger mLog = java.util.logging.Logger
  .getLogger(WebPlugin.class.getName());
  
  private static final String CHANNEL_SITE = "channelSite";

/** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(WebPlugin.class);

  /** parameter to be replaced by all searchable strings */
  private static final String WEBSEARCH_ALL = "anytext";

  /** Default-Addresses */
  final static WebAddress[] DEFAULT_ADRESSES = {
      new WebAddress("OFDb", "http://www.ofdb.de/view.php?page=suchergebnis&Kat=All&SText={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, true),
      new WebAddress("IMDb", "http://akas.imdb.com/find?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", null, false, true),
      new WebAddress("Zelluloid", "http://zelluloid.de/suche/index.php3?qstring={urlencode(title, \"ISO-8859-1\")}", null, false, true),
      new WebAddress("Google", "http://www.google.com/search?q=%22{urlencode(title, \"UTF-8\")}%22", null, false, true),
      new WebAddress("Altavista", "http://de.altavista.com/web/results?q=%22{urlencode(title, \"UTF-8\")}%22", null, false, true),
      new WebAddress("Yahoo", "http://search.yahoo.com/search?p={urlencode(title, \"ISO-8859-1\")}", null, false, true),
      new WebAddress("Wikipedia (DE)", "http://de.wikipedia.org/wiki/Spezial:Search?search={urlencode(title, \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.GERMAN)),
      new WebAddress("Wikipedia (EN)", "http://en.wikipedia.org/wiki/Spezial:Search?search={urlencode(title, \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.ENGLISH)),
      new WebAddress(mLocalizer.msg("channelPageGeneral", "Open website of channel"),CHANNEL_SITE,null,false,true)
  };

  /** The WebAddresses */
  private ArrayList<WebAddress> mAddresses;

  private boolean mHasRightToDownload = false;
  
  private static WebPlugin INSTANCE;
  
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
  
  /**
   * Returns the Plugin-Info
   */
  public PluginInfo getInfo() {
    return new PluginInfo("WebPlugin",
        mLocalizer.msg("desc","Searches on the Web for a Program"),
        "Bodo Tasche",mLocalizer.msg("helpUrl", "http://enwiki.tvbrowser.org/index.php/WebPlugin"),new Version(1, 2));
  }

  /**
   * Loads the Data
   */
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    mAddresses = new ArrayList<WebAddress>();

    int version = in.readInt();

    int size = in.readInt();

    ArrayList<WebAddress> defaults = new ArrayList<WebAddress>(Arrays.asList(DEFAULT_ADRESSES));
    
    for (int i = 0; i < size;i++) {
      WebAddress newone = new WebAddress(in);

      if (!newone.isUserEntry()) {
        for (int v = 0; v < defaults.size(); v++) {
          WebAddress def = defaults.get(v);
          // Replace Default Webaddresses with Default Settings
          if (def.getName().equals(newone.getName())) {
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
  }

  /**
   * Saves the Data
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);
    if (mAddresses == null) {
      createDefaultSettings();
    }

    out.writeInt(mAddresses.size());

    for (int i = 0; i < mAddresses.size(); i++) {
      (mAddresses.get(i)).writeData(out);
    }

  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "web-search", 16);
  }
  
  /**
   * Creates the Settings-Tab
   */
  public SettingsTab getSettingsTab() {
    if (mAddresses == null) {
      createDefaultSettings();
    }
    return new WebSettingsTab((JFrame)getParentFrame(), mAddresses);
  }


  /**
   * Create the Default-Settings
   */
  private void createDefaultSettings() {
    mAddresses = new ArrayList<WebAddress>();
    WebAddress test = new WebAddress("Test", "http://akas.imdb.com/Tsearch?title={urlencode(title, \"UTF-8\")}", null, true, false);
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

    Action mainAction = new devplugin.ContextMenuAction();
    mainAction.putValue(Action.NAME, "Online-Suche");
    mainAction.putValue(Action.SMALL_ICON, createImageIcon("actions", "web-search", 16));

    ArrayList<Object> actionList = new ArrayList<Object>();
    ArrayList<String> searchItems = null;

    for (int i = 0; i < mAddresses.size(); i++) {

    	WebAddress address = mAddresses.get(i);
    	String actionName = mLocalizer.msg("SearchOn", "Search on ") + " " + address.getName();

    	//create adress of channel on the fly
    	if (address.getUrl().equals(CHANNEL_SITE)) {
    		address = new WebAddress(mLocalizer.msg("channelPage", "Open page of {0}",program.getChannel().getName()),program.getChannel().getWebpage(),null,false,address.isActive());
    		actionName = address.getName();
    	}
      if (address.isActive()) {
        // create items for a possible sub menu
        if (address.getUrl().contains(WEBSEARCH_ALL) && searchItems == null) {
          searchItems = new ArrayList<String>();
          searchItems.add(program.getTitle());
          String directorField = program.getTextField(ProgramFieldType.DIRECTOR_TYPE);
          if (directorField != null) {
            String[] directors = directorField.split(",");
            for (String director : directors) {
              searchItems.add(director.trim());
            }
          }
          String scriptField = program.getTextField(ProgramFieldType.SCRIPT_TYPE);
          if (scriptField != null) {
            String[] scripts = scriptField.split(",");
            for (String script : scripts) {
              searchItems.add(script.trim());
            }
          }
          String actorsField = program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE);
          if (actorsField != null) {
            String[] actors = actorsField.split(",");
            for (String actor : actors) {
              if (actor.contains("(") && actor.contains(")")) {
                String part = actor.substring(0, actor.indexOf("(")).trim();
                if (part != "") {
                  searchItems.add(part);
                }
                part = actor.substring(actor.indexOf("(")+1,actor.indexOf(")")).trim();
                if (part != "") {
                  searchItems.add(part);
                }
              }
              else {
                searchItems.add(actor.trim());
              }
            }
          }
        }
        if (address.getUrl().contains(WEBSEARCH_ALL) && searchItems.size() > 1) {
          AbstractAction[] subAction = new AbstractAction[searchItems.size()];
          for (int searchIndex = 0; searchIndex < subAction.length; searchIndex++) {
            final WebAddress adr = new WebAddress(address.getName(), address.getUrl().replace(WEBSEARCH_ALL, "\"" + searchItems.get(searchIndex) + "\""), null, false, true);
            subAction[searchIndex] = new AbstractAction() {
              
              public void actionPerformed(ActionEvent evt) {
                openUrl(program, adr);
              }
            };
            subAction[searchIndex].putValue(Action.NAME, searchItems.get(searchIndex));
          }
          ContextMenuAction action = new ContextMenuAction(address.getName(), address.getIcon());
          ActionMenu searchMenu = new ActionMenu(action, subAction);
          actionList.add(searchMenu);
        }
        // create only a single menu item for this search
        else {
          final WebAddress adr = address;
          AbstractAction action = new AbstractAction() {
  
            public void actionPerformed(ActionEvent evt) {
              openUrl(program, adr);
            }
          };
          action.putValue(Action.NAME, actionName);
          action.putValue(Action.SMALL_ICON, adr.getIcon());
  
          actionList.add(action);
        }
      }
    }
    Object[] actions = new Object[actionList.size()];
    actionList.toArray(actions);
    ActionMenu result = new ActionMenu(mainAction, actions);

    return result;
  }
  
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);
      
      if (adr.isActive())
        list.add(new ProgramReceiveTarget(this,mLocalizer.msg("SearchOn", "Search on ") + " " + adr.getName(),adr.getName() + "." + adr.getUrl()));      
    }
    
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }
  
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget target) {
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);
      
      if (adr.isActive() && target.isReceiveTargetWithIdOfProgramReceiveIf(this,adr.getName() + "." + adr.getUrl())) {
        for(Program p : programArr)
          openUrl(p, adr);
        
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
  protected void openUrl(Program program, WebAddress address) {
    try {
      ParamParser parser = new ParamParser();
      String result = parser.analyse(address.getUrl(), program);
      
      if (parser.hasErrors()) {
        String errorString = parser.getErrorString();
        mLog.warning("URL parse error " + errorString+ " in " + address.getUrl());
        JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), errorString, 
            Localizer.getLocalization(Localizer.I18N_ERROR),
            JOptionPane.ERROR_MESSAGE);

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
      FavIconFetcher fetcher = new FavIconFetcher();
    
      if (mAddresses != null) {
        for (WebAddress address : mAddresses) {
          
          if ((address.getIconFile() == null) && (! address.getUrl().equals(CHANNEL_SITE))) {
            String file = fetcher.fetchFavIconForUrl(address.getUrl());
          
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

}