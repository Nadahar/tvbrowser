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

import devplugin.PluginAccess;
import devplugin.Version;

/**
 * A class that contains informations about a blocked plugin.
 * 
 * @author René Mach
 */
public final class BlockedPlugin {
  private Version mBlockStart;
  private Version mBlockEnd;
  private String mPluginId;
  
  /**
   * Creates an instance of a blocked plugin.
   * 
   * @param blockStart The version from which the block begins.
   * @param blockEnd The version to which the block reach.
   * @param pluginId The id of the blocked plugin.
   */
  protected BlockedPlugin(Version blockStart, Version blockEnd, String pluginId) {
    mBlockStart = blockStart;
    mBlockEnd = blockEnd;
    mPluginId = pluginId;
  }
  
  /**
   * Creates an instance of a blocked plugins
   * from the given String.
   * 
   * @param asString The String that contains the information about the blocked plugin.
   */
  protected BlockedPlugin(String asString) {
    String[] partA = asString.split(";");
    
    mPluginId = partA[0];
    
    if(partA[1].equals("null")) {
      mBlockStart = null;
    }
    else {
      String[] versionPart = partA[1].split(",");
      
      mBlockStart = new Version(Integer.valueOf(versionPart[0]),Integer.valueOf(versionPart[1]),Integer.valueOf(versionPart[2]),Boolean.valueOf(versionPart[3]));
    }
    
    if(partA[2].equals("null")) {
      mBlockStart = null;
    }
    else {
      String[] versionPart = partA[2].split(",");
      
      mBlockEnd = new Version(Integer.valueOf(versionPart[0]),Integer.valueOf(versionPart[1]),Integer.valueOf(versionPart[2]),Boolean.valueOf(versionPart[3]));
    }
  }
  
  /**
   * (Even if the block start version is higher than 0.0.0.0, the
   * 0.0.0.0 version will always been acknowledged as blocked.)
   * 
   * @param plugin The plugin to check.
   * @return <code>True</code> if the given plugin is blocked.
   */
  public boolean isBlockedVersion(PluginAccess plugin) {
     return plugin != null && plugin.getId().equals(mPluginId) &&
     (((mBlockEnd == null || plugin.getInfo().getVersion().compareTo(mBlockEnd) <= 0) && 
     (mBlockStart == null || plugin.getInfo().getVersion().compareTo(mBlockStart) >= 0)) 
     || plugin.getInfo().getVersion().compareTo(new Version(0,0,0,true)) <= 0) ;
  }
  
  /**
   * @param pluginId The id to test. 
   * @param version The version to test.
   * @return <code>True</code> if the given plugin id version combination is blocked.
   */
  public boolean isBlockedVersion(String pluginId, Version version) {
    return pluginId != null && version != null && pluginId.equals(mPluginId) &&
    (((mBlockEnd == null || version.compareTo(mBlockEnd) <= 0) && 
    (mBlockStart == null || version.compareTo(mBlockStart) >= 0)) 
    || version.compareTo(new Version(0,0,0,true)) <= 0) ;    
  }
  
  protected String getPropertyString() {
    StringBuilder asString = new StringBuilder(mPluginId);
    
    asString.append(";");
    
    if(mBlockStart == null) {
      asString.append("null");
    }
    else {
      asString.append(mBlockStart.getMajor());
      asString.append(",");
      asString.append(mBlockStart.getMinor());
      asString.append(",");
      asString.append(mBlockStart.getSubMinor());
      asString.append(",");
      asString.append(mBlockStart.isStable());
    }
    
    asString.append(";");
    
    if(mBlockEnd == null) {
      asString.append("null");
    }
    else {
      asString.append(mBlockEnd.getMajor());
      asString.append(",");
      asString.append(mBlockEnd.getMinor());
      asString.append(",");
      asString.append(mBlockEnd.getSubMinor());
      asString.append(",");
      asString.append(mBlockEnd.isStable());
    }
    
    return asString.toString();
  }
  
  public String toString() {
    return new StringBuilder("Blocked from: '").append(mBlockStart).append("' to: '").append(mBlockEnd).append("' for ID: '").append(mPluginId).append("'.").toString();
  }
}
