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

package util.ui.toolbar;

import javax.swing.Icon;


public class ToolBarButton extends ToolBarItem {
  
  private String mName, mDescription;
  private Icon mIcon;
    
  public ToolBarButton(String id, String name, String description, Icon icon) {
    super(id);
    mName = name;
    mDescription = description;
    mIcon = icon;
  }
  
 
  public void setName(String name) {
    mName = name;
  }
  
  public void setDescription(String description) {
    mDescription = description;
  }
  
  public void setIcon(Icon icon) {
    mIcon = icon;
  }
  
  public String getName() {
    return mName;
  }
  
  public String getDescription() {
    return mDescription;
  }
  
  public Icon getIcon() {
    return mIcon;
  }
}