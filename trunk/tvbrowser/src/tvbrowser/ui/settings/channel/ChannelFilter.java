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

  private int[] mCategories;

  private String[] mChannelName;

  /**
   * Creates an empty Filter
   */
  public ChannelFilter() {
    setFilter(null, new int[]{Integer.MAX_VALUE}, null);
  }
  
  /**
   * Creates the Filter
   * @param country Country to use or NULL
   * @param categories Category to use, if &lt; 0 use exact category, if MAX_INT don't use category 
   * @param name Name to search for. This is an "and" Search. Search-Terms are separated by Whitespace
   */
  public ChannelFilter(String country, int categories, String name) {
    setFilter(country, new int[] {categories}, name);
  }

  /**
   * Set the Values in the Filter
   * @param country
   * @param category
   * @param name
   */
  public void setFilter(String country, int category, String name) {
    setFilter(country, new int[] {category}, name);
  }

  /**
   * Set the Values in the Filter
   * @param country
   * @param categories
   * @param name
   */
  public void setFilter(String country, int[] categories, String name) {
    mCountry = country;
    mCategories = categories;
    if (mChannelName != null) {
      mChannelName = name.trim().split("\\s");
      for (int i = 0; i < mChannelName.length; i++) {
        mChannelName[i] = normalizeCharacters(mChannelName[i]);
      }
    } else {
      mChannelName = new String[]{};
    }
  }
  
  /**
   * @param channel Channel to check
   * @return True if Channel is accepted by this Filter
   */
  public boolean accept(Channel channel) {
    if (mCountry != null) {
      String country = channel.getCountry();
      if (country != null) {
        if (!country.equalsIgnoreCase(mCountry)) {
          return false;
        }
      } else {
        return false;
      }
    }

    if (mChannelName.length > 0) {
      String channelName = normalizeCharacters(channel.getName());
      for (String name:mChannelName) {
          if (!channelName.contains(name))
            return false;
      }
    }

    boolean categoryTest = false;
    int i = 0;
    int max = mCategories.length;
    
    while (i < max && !categoryTest) {
      int category = mCategories[i];

      if (category != Integer.MAX_VALUE) {
        if ((category < 0)) {
          category *= -1;
          if (channel.getCategories() == category) {
            categoryTest = true;
          }
        } else if (category == 0) {
          if (channel.getCategories() == 0)
            categoryTest = true;
        } else if ((channel.getCategories() & category) != 0) {
          categoryTest = true;
        }
      } else {
        categoryTest = true;
      }

      i++;
    }
    
    if (!categoryTest) 
      return false;
    
    return true;
  }

  /**
   * Normalizes the Text for better Search results
   * 
   * @param text Text to normalize
   * @return normalized Text
   */
  private String normalizeCharacters(String text) {
    text = text.toLowerCase().trim();

    text = text.replaceAll("�", "o").replaceAll("�", "a").replaceAll("�", "u").replaceAll("�", "s").replaceAll("oe",
        "o").replaceAll("ae", "a").replaceAll("ue", "u").replaceAll("ss", "s");

    return text;
  }

}
