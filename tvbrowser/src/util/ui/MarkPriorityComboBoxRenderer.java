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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;

/**
 * A renderer class for the mark priority selection combo box.
 * 
 * @author René Mach
 * @since 2.6
 */
public class MarkPriorityComboBoxRenderer extends DefaultListCellRenderer {
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
    
    if(!isSelected) {
      JPanel colorPanel = new JPanel(new FormLayout("default:grow","fill:default:grow"));
      ((JLabel)c).setOpaque(true);
      
      int colorIndex = index;
      
      if(index == -1) {
        colorIndex = list.getSelectedIndex();
      }
      
      if(list.getModel().getSize() == 6) {
        colorIndex--;
      }
      
      Color color = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(colorIndex);
      
      if(color != null) {
        c.setBackground(color);
      }
      
      colorPanel.setOpaque(false);
      colorPanel.add(c, new CellConstraints().xy(1,1));
      
      c = colorPanel;
    }
    
    return c;
  }
}
