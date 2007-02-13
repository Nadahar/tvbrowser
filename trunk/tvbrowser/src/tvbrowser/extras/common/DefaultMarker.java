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

package tvbrowser.extras.common;

import javax.swing.Icon;

import devplugin.Program;


public class DefaultMarker implements devplugin.Marker {

  private String mId;
  private Icon mIcon;
  private String mName;
  private int mMarkPriority;

  public DefaultMarker(String id, Icon icon, String name, int markPriority) {
    mId = id;
    mIcon = icon;
    mName = name;
    mMarkPriority = markPriority;
  }

  public String getId() {
    return mId;
  }

  public Icon getMarkIcon() {
    return mIcon;
  }
  
  public String toString() {
    return mName;
  }

  public Icon[] getMarkIcons(Program p) {
    return new Icon[] {mIcon};
  }

  public int getMarkPriorityForProgram(Program p) {
    return mMarkPriority;
  }
}