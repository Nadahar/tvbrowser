/*
 * SoundReminder - Plugin for TV-Browser
 * Copyright (C) 2009 Ren� Mach
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
 *     $Date: 2009-03-01 09:56:39 +0100 (So, 01 Mrz 2009) $
 *   $Author: ds10 $
 * $Revision: 5521 $
 */

package soundreminder;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The renderer class for the settings table.
 * 
 * @author Ren� Mach
 */
public class SoundReminderSettingsTableRenderer extends
    DefaultTableCellRenderer {
  protected final static Color NOT_VALID_COLOR = new Color(220,0,0,60);
  
  public Component getTableCellRendererComponent(final JTable table,
      final Object value, final boolean isSelected, final boolean hasFocus,
      final int row, final int column) {
    if(column == 0 || column == 2) {
      final JPanel background = new JPanel(new FormLayout("fill:0dlu:grow",
          "fill:default:grow"));
      background.setOpaque(true);
      
      final JLabel label = new JLabel((String) value);
      
      if(!((SoundReminderSettingsTableModel)table.getModel()).rowIsValid(row) && !isSelected) {
        background.setBackground(NOT_VALID_COLOR);
      }
      else if(!isSelected) {
        background.setBackground(table.getBackground());
        label.setForeground(table.getForeground());
      }
      else {
        background.setBackground(table.getSelectionBackground());
        label.setForeground(table.getSelectionForeground());
      }
      
      background.add(label, new CellConstraints().xy(1,1));
      
      return background;
    }
    else {
      final JPanel background = new JPanel(new FormLayout(
          "0dlu:grow,default,0dlu:grow", "0dlu:grow,default,0dlu:grow"));
      background.setOpaque(true);
      
      if(!isSelected) {
        background.setBackground(table.getBackground());
      }
      else {
        background.setBackground(table.getSelectionBackground());
      }
      
      final JCheckBox checkBox = new JCheckBox();
      checkBox.setSelected((Boolean)value);
      checkBox.setOpaque(false);
      checkBox.setContentAreaFilled(false); 

      if(!((SoundReminderSettingsTableModel)table.getModel()).rowIsValid(row) && !isSelected) {
        background.setBackground(NOT_VALID_COLOR);
      }
      
      background.add(checkBox, new CellConstraints().xy(2,2));
      
      return background;
    }
  }
}
