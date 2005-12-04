/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package tvbrowser.core.icontheme;

/**
 * An Directory-Entry in a Theme-FIle
 */
public class Directory {
  private String mName;
  private String mContext;
  private String mType;
  private int mSize;
  private int mMaxSize;
  private int mMinSize;
  private int mThreshold;
  
  public Directory(String name, String context, String type, int size, int maxsize, int minsize, int threshold) {
    mName = name;
    mContext = context;
    mType = type;
    mSize = size;
    mMinSize = minsize;
    mMaxSize = maxsize;
    mThreshold = threshold;
  }

  public String getName() {
    return mName;
  }
  
  public void setName(String name) {
    mName = name;
  }
  
  /**
   * @return Returns the mContext.
   */
  public String getContext() {
    return mContext;
  }

  /**
   * @param context The mContext to set.
   */
  public void setContext(String context) {
    mContext = context;
  }

  /**
   * @return Returns the mMaxSize.
   */
  public int getMaxSize() {
    return mMaxSize;
  }

  /**
   * @param maxSize The mMaxSize to set.
   */
  public void setMaxSize(int maxSize) {
    mMaxSize = maxSize;
  }

  /**
   * @return Returns the mMinSize.
   */
  public int getMinSize() {
    return mMinSize;
  }

  /**
   * @param minSize The mMinSize to set.
   */
  public void setMinSize(int minSize) {
    mMinSize = minSize;
  }

  /**
   * @return Returns the mSize.
   */
  public int getSize() {
    return mSize;
  }

  /**
   * @param size The mSize to set.
   */
  public void setSize(int size) {
    mSize = size;
  }

  /**
   * @return Returns the mThreshold.
   */
  public int getThreshold() {
    return mThreshold;
  }

  /**
   * @param threshold The mThreshold to set.
   */
  public void setThreshold(int threshold) {
    mThreshold = threshold;
  }

  /**
   * @return Returns the mType.
   */
  public String getType() {
    return mType;
  }

  /**
   * @param type The mType to set.
   */
  public void setType(String type) {
    mType = type;
  }
  
  
}
