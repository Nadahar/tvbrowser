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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings.channel;

import devplugin.Channel;

/**
 * Filters for a specific Country, Category and/or Channelname 
 */
public class ChannelFilter {
  private String mCountry;
  private int mCategories;
  private String mChannelName;

  public ChannelFilter(String country, int categories, String name) {
    mCountry = country;
    mCategories = categories;
    mChannelName = name.toLowerCase();
  }

  public boolean accept(Channel channel) {
    if (mCountry != null) {
      String country = channel.getCountry();
      if (country!=null) {
        if (!country.equals(mCountry)) {
          return false;
        }
      }
      else {
        return false;
      }
    }

    if (mCategories > 0) {
      if ((channel.getCategories() & mCategories) == 0) {
        return false;
      }
    } else if ((mCategories == 0) && (channel.getCategories() != 0)){
      return false;
    }

    if (mChannelName.length() > 0) {
      if (!channel.getName().toLowerCase().contains(mChannelName)) {
        return false;
      }
    }
    
    return true;
  }
}
