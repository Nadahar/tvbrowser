/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package listviewplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.settings.ProgramPanelSettings;
import util.ui.ChannelLabel;
import util.ui.ProgramPanel;
import devplugin.Channel;
import devplugin.Program;

/**
 * The CellRenderer for the Table
 */
public class ListTableCellRenderer extends DefaultTableCellRenderer {

  private ChannelLabel mChannelLabel;

  private ProgramPanel[] mProgramPanel = new ProgramPanel[2];

  /**
   * Creates the Component
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {

    Dimension gaps = table.getIntercellSpacing();
    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof Channel) {

      Channel channel = (Channel) value;

      if (mChannelLabel == null) {
        mChannelLabel = new ChannelLabel();
      }

      mChannelLabel.setChannel(channel);
      mChannelLabel.setOpaque(label.isOpaque());
      mChannelLabel.setForeground(label.getForeground());
      mChannelLabel.setBackground(label.getBackground());

      if (channel.getIcon() != null) {
        if (getSize().height < channel.getIcon().getIconHeight()) {

          Dimension dim = getSize();
          setSize(dim.width, channel.getIcon().getIconHeight());
        }
      }

      // do all height calculation for the complete row
      int height = mChannelLabel.getHeight();
      
      for (int i = 0; i < 2; i++) {
        Program program = (Program) table.getValueAt(row, 1 + i);
        
        if(program != null) {
          if (mProgramPanel[i] == null) {
            mProgramPanel[i] = new ProgramPanel(program, new ProgramPanelSettings(ListViewPlugin.getInstance().getPictureSettings(),false, ProgramPanelSettings.X_AXIS));
          }

          mProgramPanel[i].setProgram(program);
          height = Math.max(height, mProgramPanel[i].getHeight());
        }
      }
      
      for (int i = 0; i < 2; i++) {
        if (mProgramPanel[i] != null) { 
          mProgramPanel[i].setHeight(height);
        }
      }
      
      height += gaps.height;

      if (height != table.getRowHeight(row)) {
        table.setRowHeight(row, height);
      }

      return mChannelLabel;
    } else if (value instanceof Program) {

      int index = column - 1;

      JPanel rpanel = new JPanel(new BorderLayout());
      rpanel.add(mProgramPanel[index], BorderLayout.CENTER);
      rpanel.setBackground(label.getBackground());

      mProgramPanel[index].setTextColor(label.getForeground());

      if (ListViewPlugin.PROGRAMTABLEWIDTH > table.getColumnModel().getColumn(column).getMinWidth()) {
        int width = ListViewPlugin.PROGRAMTABLEWIDTH;
        table.getColumnModel().getColumn(column).setMinWidth(width);
      }

      return rpanel;
    }
    
    return label;
  }

}