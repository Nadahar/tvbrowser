/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
 * 
 */

package swedbtvdataservice;





/**
 * Each channel should belong to exactly one channel group. The ChannelGroup interface
 * represents a channel goup.
 */
public class SweDBChannelGroup implements devplugin.ChannelGroup {

    private String mName;
    private String mId;
    private String mDescription;
    private String mProviderName;
    
    
  public SweDBChannelGroup(String name, String id, String description, String providername){
      mName = name;
      mId = id;
      mDescription = description;
      mProviderName = providername;
  }  
  /**
   * @return the title of this group
   */
  public String getName(){
      return mName;
  }

  /**
   *
   * @return unique identifier of this group
   */
  public String getId(){
      return mId;
  }

  /**
   *
   * @return short description of this group
   */
  public String getDescription(){
      return mDescription;
  }

  /**
   * @since 1.1
   * @return the name of the provider
   */
  public String getProviderName(){
      return mProviderName;
  }
  
  public boolean equals(Object obj) {

    if (obj instanceof devplugin.ChannelGroup) {
      devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
      return group.getId().equalsIgnoreCase(mId);
    }
    return false;

  }
}