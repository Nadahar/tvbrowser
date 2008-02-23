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
package wirschauenplugin;

import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;

import java.awt.event.ActionEvent;
import java.awt.Window;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * With this Plugin it is possible to add informations about a program and send it to wirschauen.de
 */
public class WirSchauenPlugin extends Plugin {
  private static final Version mVersion = new Version(1, 0);

  private static Logger mLog = Logger.getLogger(WirSchauenPlugin.class.getName());

  private ArrayList<String> mAllowedChannels;

  /**
   * Localizer
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WirSchauenPlugin.class);

  private static WirSchauenPlugin INSTANCE;

  private PluginInfo mPluginInfo;

  /**
   * Creates the Plugin
   */
  public WirSchauenPlugin() {
    INSTANCE = this;

    mAllowedChannels = new ArrayList<String>(5);
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLLIVING");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLCRIME");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl2");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:superrtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:pro7");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:sat1");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:sat1comedy");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:vox");
  }

  /**
   * Returns the Instance of the Plugin
   *
   * @return Plugin-Instance
   */
  public static WirSchauenPlugin getInstance() {
    return INSTANCE;
  }

  public static Version getVersion() {
    return mVersion;
  }

  /**
   * Returns the Plugin-Info
   */
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      mPluginInfo = new PluginInfo(WirSchauenPlugin.class, mLocalizer.msg("name", "WirSchauenPlugin"),
              mLocalizer.msg("desc", "Makes it possbile to add descriptions to programs"), "TV-Browser Team");
    }

    return mPluginInfo;
  }

  /**
   * Creates the Context-Menu-Entries
   */
  public ActionMenu getContextMenuActions(final Program program) {

    String name = "";

    if (program.getChannel().getDataServiceProxy() != null) {
      name = program.getChannel().getDataServiceProxy().getId() + ":";
    }

    name = name + program.getChannel().getId();

    if (getPluginManager().getExampleProgram().equals(program) || mAllowedChannels.contains(name)) {

      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          showDescribeDialog(program);
        }
      };

      action.putValue(Action.NAME, mLocalizer.msg("contextMenu", "Recommend Text for this program"));

      return new ActionMenu(action);

    } else {
      mLog.log(Level.INFO, "Channel not allowed : " + name);
    }

    return null;
  }

  private void showDescribeDialog(Program program) {
    final Window parent = UiUtilities.getLastModalChildOf(getParentFrame());

    WirSchauenDialog dialog;

    if (parent instanceof JDialog) {
      dialog = new WirSchauenDialog((JDialog) parent, program);
    } else {
      dialog = new WirSchauenDialog((JFrame) parent, program);
    }

    UiUtilities.centerAndShow(dialog);
  }

}