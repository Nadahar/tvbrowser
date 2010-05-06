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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.ui.TVBrowserIcons;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.ProgramFilter;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
final public class FilterViewPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 1);

  private PluginInfo mPluginInfo;

  private ImageIcon mIcon;

  private PluginTreeNode mRootNode;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(FilterViewPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Filter view");
      final String desc = mLocalizer.msg("description",
          "Shows matches of filters in the plugins tree.");
      mPluginInfo = new PluginInfo(FilterViewPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  private Icon getPluginIcon() {
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
      for (ProgramFilter filter : getPluginManager().getFilterManager().getAvailableFilters()) {
        mRootNode.addNode(filter.getName());
      }
    }
    return mRootNode;
  }

}
