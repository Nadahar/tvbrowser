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

package util.ui.view;


public class SplitViewProperties extends ViewProperties {

  private int mDividerLocation;
  private boolean mVerticalSplit;
  private int mFixedComponent;
  public static final int LEFT = 0, RIGHT = 1;
  
  public SplitViewProperties(boolean verticalSplit, int fixedComponent, int width) {
    mVerticalSplit = verticalSplit;
    mFixedComponent = fixedComponent;
    mDividerLocation = width;
  }

  public void setVerticalSplit(boolean verticalSplit) {
    mVerticalSplit=verticalSplit;
  }

  public boolean getVerticalSplit() {
    return mVerticalSplit;
  }
  
  public int getDividerLocation() {
    return mDividerLocation;
  }

  public int getFixedComponent() {
    return mFixedComponent;
  }
    
}