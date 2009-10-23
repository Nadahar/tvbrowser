/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
 * With this plugin it is possible to add information about a program and send it to wirschauen.de.
 */
public final class WirSchauenPlugin extends Plugin
{
  /**
   * the url to the wirschauen server.
   */
  public static final String BASE_URL = "http://www.wirschauen.de/events/";

  /**
   * Localizer.
   */
  protected static final Localizer LOCALIZER = Localizer.getLocalizerFor(WirSchauenPlugin.class);

  /**
   * true, if this plugin is stable. false otherwise. used for the version-object.
   */
  private static final boolean IS_STABLE = true;

  /**
   * the version of this plugin.
   */
  private static final Version VERSION = new Version(0, 14, 0, IS_STABLE);


  /**
   * this class is a singleton. kind of. the constructor is not restricted so
   * there might be more than one instance. but the getInstance method will
   * alway return the last created instance.
   */
  private static WirSchauenPlugin mInstance;

  /**
   * the plugin info. see getInfo. lazy init.
   */
  private static PluginInfo mPluginInfo;

  /**
   * the app icon for the context menu, lazy init.
   */
  private static Icon mIcon;

  /**
   * the icon for a missing description, shown in the program table.
   */
  private static Icon mMissingDescriptionIcon;

  /**
   * the plugin is restricted to specific channels, ie vg media. this might be
   * removed in future versions.
   */
  private static ArrayList<String> mAllowedChannels;

  /**
   * all the filters defined by this plugin (for now its one). lazy init, see getAvailableFilter.
   */
  private static PluginsProgramFilter mFilter;

  /**
   * the filter implementation. lazy init, see getAvailableFilter.
   */
  private static WirSchauenFilterComponent mComponent;


  /**
   * Creates the Plugin.
   */
  public WirSchauenPlugin()
  {
    mInstance = this;

    //TODO thats static so we should do this in a static init block
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
   * Returns the last created Instance of the Plugin.
   *
   * @return Plugin-Instance
   */
  public static WirSchauenPlugin getInstance()
  {
    return mInstance;
  }


  /**
   * Gets the version of this plugin.
   *
   * @return the version
   * @see devplugin.Plugin#getVersion
   */
  public static Version getVersion()
  {
    return VERSION;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public PluginInfo getInfo()
  {
    if (mPluginInfo == null)
    {
      mPluginInfo = new PluginInfo(WirSchauenPlugin.class, LOCALIZER.msg("name", "WirSchauenPlugin"), LOCALIZER.msg("desc", "Makes it possible to add descriptions to programs"), "TV-Browser Team", "GPL 3");
    }
    return mPluginInfo;
  }


  /**
   * {@inheritDoc}
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
      action.putValue(Action.NAME, LOCALIZER.msg("contextMenu", "Recommend text for this program"));

      //TODO put that in a method for better encapsulation of lazy init
      if (mIcon == null)
      {
        mIcon = createImageIcon("apps", "wirschauen", 16);
      }

      action.putValue(Action.SMALL_ICON, mIcon);

      return new ActionMenu(action);
    }
    return null;
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  @Override
  public ThemeIcon getMarkIconFromTheme()
  {
    return new ThemeIcon("apps", "wirschauen", 16);
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#canUseProgramTree()
   */
  @Override
  public boolean canUseProgramTree()
  {
    return true;
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#writeData(java.io.ObjectOutputStream)
   */
  @Override
  public void writeData(final ObjectOutputStream out) throws IOException
  {
    getRootNode();
    storeRootNode();
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getAvailableFilterComponentClasses()
   */
  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses()
  {
    return new Class[] {WirSchauenFilterComponent.class};
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getAvailableFilter()
   */
  @Override
  public PluginsProgramFilter[] getAvailableFilter()
  {
    if (mFilter == null)
    {
      //TODO verstehen und verbessern. wieso gibt es 2 geschachtelte filter-objekte??
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
    return new PluginsProgramFilter[] {mFilter};
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getProgramTableIcons(devplugin.Program)
   */
  @Override
  public Icon[] getProgramTableIcons(final Program program)
  {
    // show the icon for a missing description
    if (isProgramAllowed(program)
        && (program.getShortInfo() == null
        || (program.getShortInfo().indexOf("keine Beschreibung") != -1
        && program.getShortInfo().indexOf("WirSchauen") != -1)))
    {
      //TODO put that in a method for better encapsulation of lazy init
      if (mMissingDescriptionIcon == null)
      {
        mMissingDescriptionIcon = createImageIcon("apps", "wirschauen_noDesc", 16);
      }
      return new Icon[] {mMissingDescriptionIcon};
    }
    if (program == getPluginManager().getExampleProgram())
    {
      //FIXME that might be null! use a method for lazy init!
      return new Icon[] {mMissingDescriptionIcon};
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getProgramTableIconText()
   */
  @Override
  public String getProgramTableIconText()
  {
    return LOCALIZER.msg("icon", "WirSchauen: Description for this program is missing");
  }

  /**
   * checks if a program is allowed to be processed by wirschauen. the method
   * checks, if the programs channel is in the vg media. if so, the program is
   * allowed. im not sure if this restriction makes sense anymore.
   *
   * @param program the program to be checked
   * @return true, if the program is allowed to be processed by wirschauen
   */
  private boolean isProgramAllowed(final Program program)
  {
    String name = "";

    if (program.getChannel().getDataServiceProxy() != null)
    {
      name = program.getChannel().getDataServiceProxy().getId() + ":";
    }

    name = name + program.getChannel().getId();

    return (getPluginManager().getExampleProgram().equals(program) || mAllowedChannels.contains(name));
  }
}
