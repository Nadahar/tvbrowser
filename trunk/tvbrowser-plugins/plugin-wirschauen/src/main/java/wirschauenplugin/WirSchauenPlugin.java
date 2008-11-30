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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * With this plugin it is possible to add information about a program and send it to wirschauen.de
 */
public class WirSchauenPlugin extends Plugin {
  private static final Version mVersion = new Version(0, 9, 0, false);

  private static Logger mLog = Logger.getLogger(WirSchauenPlugin.class.getName());

  private ArrayList<String> mAllowedChannels;

  /**
   * Localizer
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WirSchauenPlugin.class);

  private static WirSchauenPlugin INSTANCE;

  private PluginInfo mPluginInfo;

  private Icon mIcon;
  public static final String BASE_URL = "http://www.wirschauen.de/events/";

  /**
   * Creates the Plugin
   */
  public WirSchauenPlugin() {
    INSTANCE = this;

    mAllowedChannels = new ArrayList<String>(5);
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl2");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:superrtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:pro7");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:sat1");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:vox");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:COMEDYCENTRAL");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:dsf");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:kabel1");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:MTV");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:n24");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:NICK");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:NTV");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:VIVA");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:puls4");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLPASSION");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLLIVING");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLCRIME");
  
    mIcon = new ImageIcon(getClass().getResource("icons/16x16/apps/wirschauen.png"));
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
              mLocalizer.msg("desc", "Makes it possible to add descriptions to programs"), "TV-Browser Team");
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
    
    if (getPluginManager().getExampleProgram().equals(program) ||
        mAllowedChannels.contains(name)) {

      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          showDescribeDialog(program);
        }
      };

      action.putValue(Action.NAME, mLocalizer.msg("contextMenu", "Recommend text for this program"));

      action.putValue(Action.SMALL_ICON, mIcon);

      return new ActionMenu(action);

    } else {
      // mLog.log(Level.INFO, "Channel not allowed for WirSchauen: " + name);
    }

    return null;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "wirschauen", 16);
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

    if (dialog.getButtonPressed() == JOptionPane.OK_OPTION) {
      // check whether we got any input at all
      if (!dialog.hasChanged()) {
        return;
      }
      
      // we have new input, so store it on the server
      String category = dialog.getCategory();
      String genre = dialog.getGenre();
      String description = dialog.getDescription();
      String flagSubtitle = dialog.getSubtitle();
      String flagOws = dialog.getOws();
      String flagPremiere = dialog.getPremiere();
      String omdbUrl = dialog.getUrl();

      StringBuilder url = new StringBuilder();
      try {
        url.append("channel=").append(URLEncoder.encode(program.getChannel().getId(), "UTF-8"));
        url.append("&day=").append(program.getDate().getDayOfMonth());
        url.append("&month=").append(program.getDate().getMonth());
        url.append("&year=").append(program.getDate().getYear());
        url.append("&hour=").append(program.getHours());
        url.append("&minute=").append(program.getMinutes());
        url.append("&length=").append(program.getLength());
        url.append("&title=").append(URLEncoder.encode(program.getTitle(), "UTF-8"));
        url.append("&category=").append(category);
        
        String episodeField = program.getTextField(ProgramFieldType.EPISODE_TYPE);
        if (episodeField != null) {
          url.append("&episode=").append(URLEncoder.encode(episodeField, "UTF-8"));
        }
        if (omdbUrl.length() > 0) {
          url.append("&url=").append(URLEncoder.encode(omdbUrl, "UTF-8"));
        }
        if (genre.length() > 0) {
          url.append("&genre=").append(URLEncoder.encode(genre, "UTF-8"));
        }
        if (description.length() > 0) {
          url.append("&description=").append(URLEncoder.encode(description, "UTF-8"));
        }
        url.append("&subtitle=").append(URLEncoder.encode(flagSubtitle, "UTF-8"));
        url.append("&omu=").append(URLEncoder.encode(flagOws, "UTF-8"));
        url.append("&premiere=").append(URLEncoder.encode(flagPremiere, "UTF-8"));

        URL u = new URL(BASE_URL + "addTVBrowserEvent/?"+ url);
        IOUtilities.loadFileFromHttpServer(u);

        getRootNode().addProgram(program);
        getRootNode().update();

        JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("success", "Thank you for submitting a description!"), mLocalizer.msg("successTitle", "Thank you"), JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        ErrorHandler.handle(mLocalizer.msg("problem", "Sorry, a problem occured during the upload"), e);
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    getRootNode();
    storeRootNode();
  }
}
