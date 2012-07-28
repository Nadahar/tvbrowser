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

import org.apache.commons.lang.StringUtils;

import devplugin.Channel;

/**
 * Filters for a specific Country, Category and/or Channelname
 */
public class ChannelFilter {
  private static final String[] NORMALIZE_REPLACE = new String[] {"o", "a", "u", "s", "o", "a", "u", "s"};

  private static final String[] NORMALIZE_SEARCH = new String[] {"ö", "ä", "ü", "ß", "oe", "ae", "ue", "ss"};

  private String mCountry;
  
  private String mPlugin;

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
   * @param categories Category to use, if &lt; 0 don't use category, if MAX_INT don't use category
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
    setFilter(country, new int[] {category}, name, null);
  }
  
  public void setFilter(String country, int category, String name, String plugin) {
    setFilter(country, new int[] {category}, name, plugin);
  }

  /**
   * Set the Values in the Filter
   * @param country
   * @param categories
   * @param name
   */
  public void setFilter(String country, int[] categories, String name) {
    setFilter(country, categories, name, null);
  }
  
  public void setFilter(String country, int[] categories, String name, String plugin) {
    mCountry = country;
    mPlugin = plugin;
    mCategories = categories.clone();
    if ((mChannelName != null) && (StringUtils.isNotBlank(name))) {
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
    
    if (mPlugin != null) {
      String plugin = channel.getDataServiceProxy().getInfo().getName();
      if (plugin != null) {
        if (!plugin.equalsIgnoreCase(mPlugin)) {
          return false;
        }
      } else {
        return false;
      }
    }

    if (mChannelName.length > 0) {
      String channelName = normalizeCharacters(channel.getName());
      for (String name:mChannelName) {
          if (!channelName.contains(name)) {
            return false;
          }
      }
    }

    boolean categoryTest = false;
    int i = 0;
    int max = mCategories.length;

    while (i < max) {
      int category = mCategories[i];

      if (category != Integer.MAX_VALUE) {
        if ((category < 0)) {
          category *= -1;
          if ((channel.getCategories() & category) != 0) {
            categoryTest = false;
            break;
          }
        } else if (category == 0) {
          if (channel.getCategories() == 0) {
            categoryTest = true;
          }
        } else if ((channel.getCategories() & category) != 0) {
          categoryTest = true;
        }
      } else {
        categoryTest = true;
      }

      i++;
    }

    if (!categoryTest) {
      return false;
    }

    return true;
  }

  /**
   * Normalizes the Text for better Search results
   *
   * @param text Text to normalize
   * @return normalized Text
   */
  private String normalizeCharacters(String text) {
    return StringUtils.replaceEach(text.toLowerCase().trim(), NORMALIZE_SEARCH, NORMALIZE_REPLACE);
  }

}
