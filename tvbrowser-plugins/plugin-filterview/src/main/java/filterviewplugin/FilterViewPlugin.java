/*
 * Copyright Michael Keppler
 *
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
package filterviewplugin;

import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.ui.TVBrowserIcons;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
final public class FilterViewPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 1);

  private PluginInfo mPluginInfo;

  private static ImageIcon mIcon;

  private static FilterViewPlugin instance;

  private PluginTreeNode mRootNode;

  private FilterViewSettings mSettings;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FilterViewPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Filter view");
      final String desc = mLocalizer.msg("description", "Shows matches of filters in the plugins tree.");
      mPluginInfo = new PluginInfo(FilterViewPlugin.class, name, desc, "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  static Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL);
    }
    return mIcon;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this);
      mRootNode.getMutableTreeNode().setIcon(getPluginIcon());
    }
    return mRootNode;
  }

  void updateRootNode() {
    int progCount = 0;
    PluginTreeNode root = getRootNode();
    root.clear();
    for (ProgramFilter filter : mSettings.getActiveFilters()) {
      PluginTreeNode filterNode = root.addNode(filter.getName());
      filterNode.addAction(new SetFilterAction(filter));
      final Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
      Date date = Date.getCurrentDate();
      final int maxDays = mSettings.getDays();
      for (int days = 0; days < maxDays; days++) {
        for (int i = 0; i < channels.length; ++i) {
          for (Iterator<Program> iter = devplugin.Plugin.getPluginManager().getChannelDayProgram(date, channels[i]); iter
              .hasNext();) {
            final Program prog = iter.next();
            if (filter.accept(prog)) {
              filterNode.addProgramWithoutCheck(prog);
              progCount++;
            }
          }
        }
        date = date.addDays(1);
      }
      // stop if there are to many nodes for the tree
      if (progCount > 10000) {
        break;
      }
    }
    root.update();
  }

  @Override
  public void handleTvBrowserStartFinished() {
    updateRootNode();
  }

  @Override
  public void handleTvDataUpdateFinished() {
    updateRootNode();
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new FilterViewSettingsTab(mSettings);
  }

  @Override
  public void loadSettings(Properties properties) {
    mSettings = new FilterViewSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public static FilterViewPlugin getInstance() {
    return instance;
  }

  public FilterViewPlugin() {
    setInstance(this);
  }

  private static void setInstance(final FilterViewPlugin plugin) {
    instance = plugin;
  }
}
