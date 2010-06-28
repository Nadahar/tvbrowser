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
 * SVN information:
 *       $Id$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import tvbrowser.ui.mainframe.SoftwareUpdater;
import util.settings.Property;
import util.settings.PropertyManager;
import devplugin.PluginAccess;
import devplugin.Version;

/**
 * A array with the blocked plugins.
 *
 * @author René Mach
 */
public final class BlockedPluginArrayProperty extends Property {
  /** The array with the blocked plugins */
  private BlockedPlugin[] mCachedValue;
  private BlockedPlugin[] mOldValue;
  private ArrayList<BlockedPlugin> mNewValues;

  /**
   * Creates the blocked plugins array.
   * @param manager Manager
   * @param key Key for this Property
   */
  public BlockedPluginArrayProperty(PropertyManager manager, String key) {
    super(manager, key);

    mCachedValue = null;
    mOldValue = null;
    mNewValues = new ArrayList<BlockedPlugin>(0);
  }

  /**
   * @return The currently blocked plugins.
   */
  public BlockedPlugin[] getBlockedPluginArray() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if (StringUtils.isBlank(asString)) {
        mCachedValue = new BlockedPlugin[0];
      } else {
        mCachedValue = loadBlockedPlugins(asString);
      }
    }

    return mCachedValue;
  }

  /**
   * @param plugin The plugin to test.
   * @return <code>True</code> if the plugin is blocked.
   */
  public boolean isBlocked(PluginAccess plugin) {
    if(mCachedValue == null) {
      getBlockedPluginArray();
    }

    for(BlockedPlugin blocked : mCachedValue) {
      if(blocked.isBlockedVersion(plugin)) {
        return true;
      }
    }

    return false;
  }

  /**
   * (Even if the block start version is higher than 0.0.0.0, the
   * 0.0.0.0 version will always been acknowledged as blocked.)
   *
   * @param pluginId The id to test.
   * @param version The version to test.
   * @return <code>True</code> if the given plugin id version combination is blocked.
   */
  public boolean isBlocked(String pluginId, Version version) {
    if(mCachedValue == null) {
      getBlockedPluginArray();
    }

    for(BlockedPlugin blocked : mCachedValue) {
      if(blocked.isBlockedVersion(pluginId, version)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Adds a plugin to the block array.
   * <p>
   * @param updater The updater that is requesting the block.
   * @param pluginId The plugin id that should be blocked.
   * @param blockStart The version from which the block starts.
   * @param blockEnd The version to which the block reaches.
   */
  public void addBlockedPlugin(SoftwareUpdater updater, String pluginId, Version blockStart, Version blockEnd) {
    if(mCachedValue == null) {
      if(updater != null && updater.isRequestingToBlockAPlugin(pluginId)) {
        mCachedValue = new BlockedPlugin[] {new BlockedPlugin(blockStart,blockEnd,pluginId)};
      }
    }
    else if(updater != null && updater.isRequestingToBlockAPlugin(pluginId)) {
      BlockedPlugin[] blockedArray = new BlockedPlugin[mCachedValue.length+1];

      System.arraycopy(mCachedValue,0,blockedArray,0,mCachedValue.length);
      blockedArray[mCachedValue.length] = new BlockedPlugin(blockStart,blockEnd,pluginId);

      mCachedValue = blockedArray;
    }

    if (mCachedValue != null && mCachedValue.length > 0) {
      if(!checkAndRemoveValueFromOld(mCachedValue[mCachedValue.length-1])) {
        mNewValues.add(mCachedValue[mCachedValue.length-1]);
      }
    }

    setBlockedPluginArray(mCachedValue);
  }

  /**
   * Checks if the given plugin is already blocked and
   * removes it from the old value array if so.
   *
   * @param plugin The plugin to check if already blocked.
   * @return If the given id was already blocked.
   */
  private boolean checkAndRemoveValueFromOld(BlockedPlugin plugin) {
    for(int i = 0; i < mOldValue.length; i++) {
      if(mOldValue[i] != null && mOldValue[i].equals(plugin)) {
        mOldValue[i] = null;
        return true;
      }
    }

    return false;
  }

  /**
   * Clears the blocked plugin array.
   *
   * @param updater The software updater that is requesting the
   * clearance of the array.
   */
  public void clear(SoftwareUpdater updater) {
    if(updater != null && updater.isRequestingBlockArrayClear()) {
      mOldValue = mCachedValue;
      mCachedValue = null;
      mNewValues = new ArrayList<BlockedPlugin>(0);
      setProperty("");
    }
  }

  private void setBlockedPluginArray(BlockedPlugin[] blockedPluginArray) {
    if(blockedPluginArray != null) {
      StringBuilder asString = new StringBuilder();

      asString.append(blockedPluginArray[0].getPropertyString());

      for(int i = 1; i < blockedPluginArray.length; i++) {
        asString.append("#_#");
        asString.append(blockedPluginArray[i].getPropertyString());
      }

      setProperty(asString.toString());
    }
  }

  private BlockedPlugin[] loadBlockedPlugins(String settingsValue) {
    BlockedPlugin[] blockedPlugins;

    if(settingsValue != null) {
      String[] parts = settingsValue.split("#_#");

      blockedPlugins = new BlockedPlugin[parts.length];

      for(int i = 0; i < parts.length; i++) {
        blockedPlugins[i] = new BlockedPlugin(parts[i]);
      }
    }
    else {
      blockedPlugins = new BlockedPlugin[0];
    }

    return blockedPlugins;
  }

  @Override
  protected void clearCache() {
    mCachedValue = null;
  }

  /**
   * Gets the plugins that were newly blocked at the last update.
   * <p>
   * @return The array with the new blocked plugins.
   */
  public BlockedPlugin[] getNewBlockedPlugins() {
    return mNewValues.toArray(new BlockedPlugin[mNewValues.size()]);
  }
}
