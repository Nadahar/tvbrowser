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
 *     $Date: 2003-10-19 00:23:53 +0200 (So, 19 Okt 2003) $
 *   $Author: darras $
 * $Revision: 246 $
 */

package primarydatamanager.primarydataservice;

public class Entry {
 
  private int mStyle;
  private String mLine;
  
  public static final int NONE=0;         // xxx
  public static final int BOLD=1;        // <b>xxx</b>
  public static final int ITALIC=2;      // <i>xxx</i>
  public static final int UNDERLINED=4;  // <u>xxx</u>
  public static final int LINK=8;        // <a href=xxx>
  public static final int IMG_ALT=9;    // <img alt=xxx>
  public static final int IMG_SRC=10;    // <img src=xxx>
 
  public Entry(int style, String line) {
    mStyle=style;
    mLine=line;
  }
   
  public int getStyle() {
    return mStyle;
  }
  
  public String getLine() {
    return mLine;
  }
  
}
