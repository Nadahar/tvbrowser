/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui;

import java.awt.Color;

import javax.swing.UIManager;

import com.jgoodies.looks.plastic.theme.DarkStar;

public class DarkStarDark extends DarkStar {
  public DarkStarDark(boolean settings) {}
  
  public DarkStarDark() {
    setUiValues();
  }
  
  static void setUiValues() {
    UIManager.put("List.background",Color.black);
    UIManager.put("List.foreground",Color.white);

    UIManager.put("Tree.background",Color.black);
    UIManager.put("Tree.foreground",Color.white);

    UIManager.put("Table.background",Color.black);
    UIManager.put("Table.foreground",Color.white);
    
    UIManager.put("Tree.textBackground",Color.black);
    UIManager.put("Tree.textForeground",Color.white);
    
    UIManager.put("EditorPane.textBackground",Color.black);
    UIManager.put("EditorPane.textForeground",Color.white);
    
    UIManager.put("TextField.background",Color.black);
    UIManager.put("TextField.foreground",Color.white);
    UIManager.put("TextField.caretForeground",Color.white);
    
    UIManager.put("TextArea.background",Color.black);
    UIManager.put("TextArea.foreground",Color.white);
    UIManager.put("TextArea.caretForeground",Color.white);
    
    UIManager.put("PasswordField.background",Color.black);
    UIManager.put("PasswordField.foreground",Color.white);
    UIManager.put("PasswordField.caretForeground",Color.white);
    
    UIManager.put("FormattedTextField.background",Color.black);
    UIManager.put("FormattedTextField.foreground",Color.white);
  }
  
  public String getName() {
    return super.getName() + " Dark";
  }
}
