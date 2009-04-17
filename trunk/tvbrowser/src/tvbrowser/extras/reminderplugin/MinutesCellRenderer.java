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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.reminderplugin;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.ui.TVBrowserIcons;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The cell renderer for the minutest column of the remider list.
 */
public class MinutesCellRenderer extends DefaultTableCellRenderer {
  private JPanel mPanel;
  private JLabel mTextLabel, mIconLabel;
  
  /**
   * Creates an instance of this class.
   */
  public MinutesCellRenderer() {
    mPanel = new JPanel(new FormLayout("pref,pref:grow,pref,2dlu","pref:grow"));
    CellConstraints cc = new CellConstraints();
    mTextLabel = new JLabel();
    mIconLabel = new JLabel(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    
    mPanel.add(mTextLabel, cc.xy(1,1));
    mPanel.add(mIconLabel, cc.xy(3,1));
  }
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component def = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof Integer) {
      Integer minutes = (Integer) value;
      
      mTextLabel.setText(ReminderFrame.getStringForMinutes(minutes.intValue()));
      
      mTextLabel.setOpaque(def.isOpaque());
      mTextLabel.setForeground(def.getForeground());
      mTextLabel.setBackground(def.getBackground());
      
      mPanel.setOpaque(def.isOpaque());
      mPanel.setBackground(def.getBackground());
      
      return mPanel;
    }
    
    return def;
  }
  
  /**
   * Tracks a single click on the minutes entry and opens
   * the cell editor if the point is on the edit picture.
   * 
   * @param p The point on that was clicked.
   * @param table The table that was clicked
   * @param y The height of the clicked cell.
   * @param row The row the click was in.
   * @param column The column the click was in.
   */
  public void trackSingleClick(Point p, JTable table, int y, int row,int column) {
    for(int i = 0; i < row; i++)
      p.move(p.x,p.y - table.getRowHeight(i));      
    
    Rectangle rect = new Rectangle(table.getColumnModel().getColumn(0).getWidth() + mIconLabel.getLocation().x
        ,y/2-8,16,16);
    
    if(rect.contains(p)) {
      table.editCellAt(row,column);
      ((MinutesCellEditor)table.getCellEditor()).getComboBox().showPopup(); 
    }
  }
  
}
