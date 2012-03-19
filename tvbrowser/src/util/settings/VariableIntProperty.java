/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

import javax.swing.UIManager;

/**
 * 
 * @author René Mach
 */
public class VariableIntProperty extends IntProperty {

  public VariableIntProperty(PropertyManager manager, String key, int defaultValue) {
    super(manager, key, defaultValue);
  }
  
  public int getDefault() {
    mIsCacheFilled = false;
    int size = UIManager.getFont("MenuItem.font").getSize();
    
    return (int)(super.getDefault() * (size / 11.));
  }

}
