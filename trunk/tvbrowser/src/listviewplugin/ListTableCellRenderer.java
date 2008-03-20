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

  private ProgramPanel mProgramPanel;

  /**
   * Creates the Component
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {

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

      if (mChannelLabel.getHeight()+2 > table.getRowHeight(row)) {
        table.setRowHeight(row, mChannelLabel.getHeight()+2);
      }

      label = mChannelLabel;
    } else if (value instanceof Program) {

      if (mProgramPanel == null) {
        mProgramPanel = new ProgramPanel((Program)value, new ProgramPanelSettings(ListViewPlugin.getInstance().getPictureSettings(),false, ProgramPanelSettings.X_AXIS));
      }

      JPanel rpanel = new JPanel(new BorderLayout());
      rpanel.add(mProgramPanel, BorderLayout.CENTER);
      rpanel.setBackground(label.getBackground());

      mProgramPanel.setProgram((Program) value);
      mProgramPanel.setTextColor(label.getForeground());

      if (mProgramPanel.getHeight() > table.getRowHeight(row)) {
        table.setRowHeight(row, mProgramPanel.getHeight());
      } else if (mProgramPanel.getHeight() < table.getRowHeight(row)) {
        if (column == 1) {
          table.setRowHeight(row, mProgramPanel.getHeight());
        }
        else {
          mProgramPanel.setHeight(table.getRowHeight(row));
        }
      }

      if (ListViewPlugin.PROGRAMTABLEWIDTH > table.getColumnModel().getColumn(column).getMinWidth()) {
        int width = ListViewPlugin.PROGRAMTABLEWIDTH;
        table.getColumnModel().getColumn(column).setMinWidth(width);
      }

      return rpanel;
    }

    return label;
  }

}