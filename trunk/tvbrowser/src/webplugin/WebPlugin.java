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
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin is a generic Web-Tool. 
 * A User can configure his favorite Search-Engines and search for the given Movie
 */
public class WebPlugin extends Plugin {
  /** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(WebPlugin.class);

  /** Default-Addresses */
  final static WebAddress[] DEFAULT_ADRESSES = {
      new WebAddress("OFDb", "http://www.ofdb.de/view.php?page=suchergebnis&Kat=DTitel&SText={urlencode(title, \"ISO-8859-1\")}", "webplugin/ofdb.gif", false, true),
      new WebAddress("IMDb", "http://akas.imdb.com/Tsearch?title={urlencode(title, \"UTF-8\")}", "webplugin/imdb.gif", false, true),
      new WebAddress("Zelluloid", "http://zelluloid.de/suche/index.php3?qstring={urlencode(title, \"ISO-8859-1\")}", "webplugin/zelluloid.png", false, true),
      new WebAddress("Google", "http://www.google.com/search?q=%22{urlencode(title, \"UTF-8\")}%22", "webplugin/google.gif", false, true),
      new WebAddress("Altavista", "http://de.altavista.com/web/results?q=%22{urlencode(title, \"UTF-8\")}%22", "webplugin/altavista.gif", false, true),
      new WebAddress("Yahoo", "http://search.yahoo.com/search?p={urlencode(title, \"ISO-8859-1\")}", null, false, true),
      new WebAddress("Wikipedia (DE)", "http://de.wikipedia.org/wiki/Spezial:Search?search={urlencode(title, \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.GERMAN)),
      new WebAddress("Wikipedia (EN)", "http://en.wikipedia.org/wiki/Spezial:Search?search={urlencode(title, \"ISO-8859-1\")}", null, false, Locale.getDefault().equals(Locale.ENGLISH))
  };

  /** The WebAddresses */
  private ArrayList mAddresses;

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
        "Bodo Tasche",new Version(1, 0));
  }

  /**
   * Loads the Data
   */
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    mAddresses = new ArrayList();

    int version = in.readInt();

    int size = in.readInt();

    ArrayList defaults = new ArrayList(Arrays.asList(DEFAULT_ADRESSES));
    
    for (int i = 0; i < size;i++) {
      WebAddress newone = new WebAddress(in);

      if (!newone.isUserEntry()) {
        for (int v = 0; v < defaults.size(); v++) {
          WebAddress def = (WebAddress) defaults.get(v);

          if (def.getName().equals(newone.getName())) {
            def.setActive(newone.isActive());
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
      System.out.println(mAddresses.get(i).getClass().getName());
      ((WebAddress)mAddresses.get(i)).writeData(out);
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
    mAddresses = new ArrayList();
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

    ArrayList actionList = new ArrayList();

    for (int i = 0; i < mAddresses.size(); i++) {

      final WebAddress adr = (WebAddress) mAddresses.get(i);

      if (adr.isActive()) {
        AbstractAction action = new AbstractAction() {

          public void actionPerformed(ActionEvent evt) {
            openUrl(program, adr);
          }
        };
        action.putValue(Action.NAME, mLocalizer.msg("SearchOn", "Search on ") + " " + adr.getName());
        action.putValue(Action.SMALL_ICON, adr.getIcon());

        actionList.add(action);
      }
    }
    Action[] actions = new Action[actionList.size()];
    actionList.toArray(actions);
    ActionMenu result = new ActionMenu(mainAction, actions);

    return result;
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
        JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), parser.getErrorString(), 
            mLocalizer.msg("Error", "Error"),
            JOptionPane.ERROR_MESSAGE);

      } else {
        Launch.openURL(result);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}