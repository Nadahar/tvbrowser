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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * With this plugin it is possible to add information about a program and send it to wirschauen.de
 */
public final class WirSchauenPlugin extends Plugin {
  private static final boolean IS_STABLE = true;

  private static final Version mVersion = new Version(0, 14, 0, IS_STABLE);

  private ArrayList<String> mAllowedChannels;

  /**
   * Localizer
   */
  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(WirSchauenPlugin.class);

  private static WirSchauenPlugin INSTANCE;

  private PluginInfo mPluginInfo;

  private static Icon mIcon;

  /**
   * the icon for a missing description, shown in the program table.
   */
  private static Icon mMissingDescriptionIcon;

  private PluginsProgramFilter mFilter;

  private WirSchauenFilterComponent mComponent;
  public static final String BASE_URL = "http://www.wirschauen.de/events/";

  /**
   * Creates the Plugin
   */
  public WirSchauenPlugin() {
    INSTANCE = this;

    mAllowedChannels = new ArrayList<String>(20);
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
  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      mPluginInfo = new PluginInfo(WirSchauenPlugin.class, mLocalizer.msg("name", "WirSchauenPlugin"),
              mLocalizer.msg("desc",
          "Makes it possible to add descriptions to programs"),
          "TV-Browser Team", "GPL 3");
    }

    return mPluginInfo;
  }

  /**
   * Creates the Context-Menu-Entries
   */
  @Override
  public ActionMenu getContextMenuActions(final Program program)
  {
    if (isProgramAllowed(program))
    {
      @SuppressWarnings("serial")
      final AbstractAction action = new AbstractAction()
      {
        public void actionPerformed(final ActionEvent event)
        {
          new DialogController(UiUtilities.getLastModalChildOf(getParentFrame())).startDialogs(program);
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenu", "Recommend text for this program"));

      if (mIcon == null)
      {
          mIcon = createImageIcon("apps", "wirschauen", 16);
      }

      action.putValue(Action.SMALL_ICON, mIcon);

      return new ActionMenu(action);
    }

    return null;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "wirschauen", 16);
  }


  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public void writeData(final ObjectOutputStream out) throws IOException {
    getRootNode();
    storeRootNode();
  }

  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return new Class[] { WirSchauenFilterComponent.class };
  }

  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    if (mFilter == null) {
      mComponent = new WirSchauenFilterComponent();
      mFilter = new PluginsProgramFilter(this) {
        @Override
        public String getSubName() {
          return mComponent.getUserPresentableClassName();
        }

        public boolean accept(final Program program) {
          return mComponent.accept(program);
        }
      };
    }
    return new PluginsProgramFilter[] { mFilter };
  }

  @Override
  public Icon[] getProgramTableIcons(Program program) {
    // show the icon for a missing description
    if (isProgramAllowed(program)
        && (program.getShortInfo() == null || (program.getShortInfo().indexOf("keine Beschreibung") != -1 && program
            .getShortInfo().indexOf("WirSchauen") != -1))) {
      if (mMissingDescriptionIcon == null) {
        mMissingDescriptionIcon = createImageIcon("apps", "wirschauen_noDesc", 16);
      }
      return new Icon[] { mMissingDescriptionIcon };
    }
    if (program == getPluginManager().getExampleProgram()) {
      return new Icon[] { mMissingDescriptionIcon };
    }
    return null;
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("icon", "WirSchauen: Description for this program is missing");
  }

  /**
   * checks if a program is allowed to be processed by wirschauen. the method
   * checks, if the programs channel is in the vg media. if so, the program is
   * allowed. im not sure if this restriction makes sense anymore.
   * 
   * @param program
   *          the program to be checked
   * @return true, if the program is allowed to be processed by wirschauen
   */
  private boolean isProgramAllowed(Program program) {
    String name = "";

    if (program.getChannel().getDataServiceProxy() != null) {
      name = program.getChannel().getDataServiceProxy().getId() + ":";
    }

    name = name + program.getChannel().getId();

    return (getPluginManager().getExampleProgram().equals(program) || mAllowedChannels.contains(name));
  }
}
