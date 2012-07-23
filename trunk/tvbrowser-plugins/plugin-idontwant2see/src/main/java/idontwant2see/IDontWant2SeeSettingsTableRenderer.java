/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
package idontwant2see;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Date;

/**
 * The renderer class for the settings table.
 * 
 * @author René Mach
 */
public class IDontWant2SeeSettingsTableRenderer extends
    DefaultTableCellRenderer {
  protected final static Color NOT_VALID_COLOR = new Color(220,0,0,60);
  protected final static Color LAST_CHANGED_COLOR = new Color(72,116,241,100);
  protected final static Color LAST_USAGE_7_COLOR = new Color(255,255,0,60);
  protected final static Color LAST_USAGE_30_COLOR = new Color(255,176,39,80);
  protected final static short OUTDATED_7_DAY_COUNT = 7;
  protected final static short OUTDATED_30_DAY_COUNT = 30;
  
  transient private Date mLastUsedDate;
  
  protected IDontWant2SeeSettingsTableRenderer(final Date lastUsedDate) {
    mLastUsedDate = lastUsedDate;
  }
  
  public Component getTableCellRendererComponent(final JTable table,
      final Object value, final boolean isSelected, final boolean hasFocus,
      final int row, final int column) {
    if(column == 0) {
      final JPanel background = new JPanel(new FormLayout("fill:0dlu:grow",
          "fill:default:grow")) {
        protected void paintComponent(Graphics g) {
          if(getBackground().equals(NOT_VALID_COLOR) || getBackground().equals(LAST_CHANGED_COLOR) ||
              getBackground().equals(LAST_USAGE_7_COLOR) || getBackground().equals(LAST_USAGE_30_COLOR)) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
          }
          
          g.setColor(getBackground());
          g.fillRect(0, 0, getWidth(), getHeight());
        }
      };
      background.setOpaque(true);
      
      final JLabel label = new JLabel((String) value);
      
      if(!((IDontWant2SeeSettingsTableModel)table.getModel()).rowIsValid(row) && !isSelected) {
        background.setBackground(NOT_VALID_COLOR);
        label.setForeground(Color.black);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isLastChangedRow(row) && !isSelected) {
        background.setBackground(LAST_CHANGED_COLOR);
        label.setForeground(Color.black);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isRowOutdated(row,mLastUsedDate,OUTDATED_30_DAY_COUNT) && !isSelected) {
        background.setBackground(LAST_USAGE_30_COLOR);
        label.setForeground(Color.black);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isRowOutdated(row,mLastUsedDate,OUTDATED_7_DAY_COUNT) && !isSelected) {
        background.setBackground(LAST_USAGE_7_COLOR);
        label.setForeground(Color.black);
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
          "0dlu:grow,default,0dlu:grow", "0dlu:grow,default,0dlu:grow")){
        protected void paintComponent(Graphics g) {
          if(getBackground().equals(NOT_VALID_COLOR) || getBackground().equals(LAST_CHANGED_COLOR) ||
              getBackground().equals(LAST_USAGE_7_COLOR) || getBackground().equals(LAST_USAGE_30_COLOR)) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
          }
          
          g.setColor(getBackground());
          g.fillRect(0, 0, getWidth(), getHeight());
        }
      };
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

      if(!((IDontWant2SeeSettingsTableModel)table.getModel()).rowIsValid(row) && !isSelected) {
        background.setBackground(NOT_VALID_COLOR);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isLastChangedRow(row) && !isSelected) {
        background.setBackground(LAST_CHANGED_COLOR);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isRowOutdated(row,mLastUsedDate,OUTDATED_30_DAY_COUNT) && !isSelected) {
        background.setBackground(LAST_USAGE_30_COLOR);
      }
      else if(((IDontWant2SeeSettingsTableModel)table.getModel()).isRowOutdated(row,mLastUsedDate,OUTDATED_7_DAY_COUNT) && !isSelected) {
        background.setBackground(LAST_USAGE_7_COLOR);
      }
      
      background.add(checkBox, new CellConstraints().xy(2,2));
      
      return background;
    }
  }
}
