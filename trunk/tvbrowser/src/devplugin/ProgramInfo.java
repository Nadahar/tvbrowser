/*
* TV-Browser
* Copyright (C) 2016 TV-Browser team (dev@tvbrowser.org)
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
*     $Date$
*   $Author$
* $Revision$
*/
package devplugin;

/**
 * A class with additonal infos for a program.
 * 
 * @author René Mach
 * @since 3.4.4
 */
public class ProgramInfo extends UniqueIdNameGenericValue<String> {
  private Plugin mPlugin;
  
  public ProgramInfo(Plugin plugin, String uniqueId, String name, String value) throws NullPointerException {
    super(uniqueId, name, value);
    
    if(plugin == null) {
      throw new NullPointerException("plugin must not be null");
    }
    
    mPlugin = plugin;
  }
  
  public void setValue(Plugin plugin, String value) {
    if(mPlugin.equals(plugin)) {
      setValue(value);
    }
  }
  
  public String getPluginUniqueId() {
    return "plugin:"+mPlugin.getId()+":"+super.getUniqueId();
  }
  
  public String getPluginId() {
    return mPlugin.getId();
  }
  
  @Override
  public String toString() {
    return mPlugin.getInfo().getName()+": " + getName();
  }
}
