/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

/**
 * This Class represents a Filter-Item in the ChannelsSettingsTab
 */
public class FilterItem implements Comparable<FilterItem> {
  /** Name of the Item */
  private String mName;
  /** Value of the Item */
  private Object mValue;

  /**
   * Create a FilterItem
   * @param name Name of Filter
   * @param value Object to Filter
   */
  public FilterItem(String name, Object value) {
    mName = name;
    mValue = value;
  }

  /**
   * Get the Name of the Filter
   * @return Name of the Filter
   */
  public String getName() {
    return mName;
  }

  /**
   * Get the Value
   * @return Value of the Filter
   */
  public Object getValue() {
    return mValue;
  }

  /**
   * @return Name of the Filter
   */
  public String toString() {
    return mName;
  }

  public int compareTo(FilterItem other) {
    return mName.compareTo(other.mName);
  }

}