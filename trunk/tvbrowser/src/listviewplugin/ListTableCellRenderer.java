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
import java.awt.Rectangle;

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

  private ProgramPanel[][] mProgramPanel;

  private Component[][] mCache;

  /**
   * Creates an instance of this class.
   * <p>
   * @param rowCount The number of rows.
   */
  public ListTableCellRenderer(int rowCount) {
    mProgramPanel = new ProgramPanel[rowCount][2];
    mCache = new Component[rowCount][3];
  }


  private void updateHeight(JTable table, int row) {
    Dimension gaps = table.getIntercellSpacing();
    int height = 0;
    if (mCache[row][0] != null) {
        height = mCache[row][0].getPreferredSize().height;
    }

    for (int i = 0; i < 2; i++) {
      Program program = (Program) table.getValueAt(row, 1 + i);
      Rectangle rect = table.getCellRect(row, i + 1, true);
      if(program != null) {
        if (mProgramPanel[row][i] != null) {
          mProgramPanel[row][i].setWidth(rect.width);
          height = Math.max(height, mProgramPanel[row][i].getPreferredHeight());
        }
      }
    }

    for (int i = 0; i < 2; i++) {
      if (mProgramPanel[row][i] != null) {
        mProgramPanel[row][i].setHeight(height);
      }
    }

    height += gaps.height;

    if (height != table.getRowHeight(row)) {
      table.setRowHeight(row, height);
    }
  }

  /**
   * Creates the Component
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (mCache[row][column] != null) {
      if (value instanceof Channel && ((ChannelLabel)mCache[row][column]).getChannel().equals(value)) {
        mCache[row][column].setForeground(label.getForeground());
        mCache[row][column].setBackground(label.getBackground());
        return mCache[row][column];
      } else if (value instanceof Program && mProgramPanel[row][column - 1].getProgram().equals(value)) {
        mProgramPanel[row][column - 1].setTextColor(label.getForeground());
        mCache[row][column].setForeground(label.getForeground());
        mCache[row][column].setBackground(label.getBackground());
        return mCache[row][column];
      } else {
        mCache[row][column] = null;
      }
    }

    if (value instanceof Channel) {
      Channel channel = (Channel) value;

      ChannelLabel channelLabel = new ChannelLabel();

      channelLabel.setChannel(channel);
      channelLabel.setOpaque(label.isOpaque());
      channelLabel.setForeground(label.getForeground());
      channelLabel.setBackground(label.getBackground());

      mCache[row][0] = channelLabel;
      updateHeight(table, row);

      return channelLabel;
    } else if (value instanceof Program) {
      int index = column - 1;

      Program program = (Program) value;

      mProgramPanel[row][index] = new ProgramPanel(program, new ProgramPanelSettings(ListViewPlugin.getInstance().getPictureSettings(),false, ProgramPanelSettings.X_AXIS));

      JPanel rpanel = new JPanel(new BorderLayout());
      rpanel.add(mProgramPanel[row][index], BorderLayout.CENTER);

      mProgramPanel[row][index].setTextColor(label.getForeground());

      if (ListViewPlugin.PROGRAMTABLEWIDTH > table.getColumnModel().getColumn(column).getMinWidth()) {
        int width = ListViewPlugin.PROGRAMTABLEWIDTH;
        table.getColumnModel().getColumn(column).setMinWidth(width);
      }

      mCache[row][column] = rpanel;
      mCache[row][column].setForeground(label.getForeground());
      mCache[row][column].setBackground(label.getBackground());

      updateHeight(table, row);

      return rpanel;
    }
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(label, BorderLayout.CENTER);
    panel.setBackground(label.getBackground());
    
    return panel;
  }

}