/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
package tvbrowser.ui.settings;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.extras.common.InternalPluginProxyIf;
import util.ui.FixedSizeIcon;
import util.ui.html.HTMLTextHelper;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The CellRenderer for the plugin table
 *
 * @author Bananeweizen
 */
public class PluginTableCellRenderer extends DefaultTableCellRenderer {

  /** Panel that shows the Information */
  private JPanel mPanel;
  /** Description */
  private JLabel mDesc;
  /** Icon */
  private JLabel mIcon;
  /** Name */
  private JLabel mName;
  /** Cell-Constraints */
  private CellConstraints mCC = new CellConstraints();
  private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

  /**
   * default icon for items without an own icon
   */
  private static final Icon DEFAULT_ICON = new ImageIcon("imgs/Jar16.gif");

  /**
   * singleton implementation
   */
  private static PluginTableCellRenderer mInstance;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {

    if (column == 0) {
      try {
        // substance LNF has issues, if the checkbox and panel are cached, therefore recreate them during each rendering
        JPanel checkBoxPanel = new JPanel(new FormLayout("0dlu:grow,default,0dlu:grow",
            "fill:0dlu:grow,default,fill:0dlu:grow"));
        boolean checked = ((Boolean) value).booleanValue();

        JCheckBox checkBox = new JCheckBox("", checked);
        checkBox.setOpaque(false);
        checkBox.setContentAreaFilled(false);
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        checkBox.setBorderPainted(true);
        if (isSelected) {
          checkBoxPanel.setForeground(table.getSelectionForeground());
          checkBoxPanel.setBackground(table.getSelectionBackground());
        } else {
          checkBoxPanel.setForeground(table.getForeground());
          checkBoxPanel.setBackground(table.getBackground());
        }
        checkBox.setSelected(checked);
        checkBox.setEnabled(table.getModel().isCellEditable(row, column));

        if (hasFocus) {
          checkBox.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
          checkBox.setBorder(NO_FOCUS_BORDER);
        }

        checkBoxPanel.add(checkBox, mCC.xy(2, 2));

        return checkBoxPanel;
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value instanceof PluginProxy || value instanceof InternalPluginProxyIf || value instanceof TvDataServiceProxy) {
      Icon iconValue = null;
      String nameValue = null;
      String descValue = null;
      boolean isActivated = true;

      if (value instanceof PluginProxy) {
        PluginProxy plugin = (PluginProxy) value;

        iconValue = plugin.getPluginIcon();
        if (iconValue != null) {
          iconValue = new FixedSizeIcon(16, 16, iconValue);
        }
        descValue = plugin.getInfo().getDescription().replace('\n', ' ');

        isActivated = plugin.isActivated();

        nameValue = plugin.getInfo().getName() + " " + plugin.getInfo().getVersion();
      } else if (value instanceof InternalPluginProxyIf) {
        InternalPluginProxyIf plugin = (InternalPluginProxyIf) value;

        nameValue = plugin.getName();
        descValue = plugin.getButtonActionDescription().replace('\n', ' ');
        iconValue = plugin.getIcon();
      } else if (value instanceof TvDataServiceProxy) {
        TvDataServiceProxy service = (TvDataServiceProxy) value;
        nameValue = service.getInfo().getName() + " " + service.getInfo().getVersion();
        descValue = HTMLTextHelper.convertHtmlToText(service.getInfo().getDescription()).replace('\n', ' ');
      }

      if (iconValue == null) {
        iconValue = DEFAULT_ICON;
      }

      if (mPanel == null) {
        mIcon = new JLabel();
        mName = new JLabel();
        mName.setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize2D() + 2));

        mPanel = new JPanel(new FormLayout("default, 2dlu, fill:0dlu:grow", "default, 2dlu, default"));
        mPanel.setBorder(Borders.DLU2_BORDER);

        mPanel.add(mIcon, mCC.xy(1, 1));
        mPanel.add(mName, mCC.xy(3, 1));
      }

      mIcon.setOpaque(false);
      mIcon.setIcon(iconValue);

      if (mDesc != null) {
        mPanel.remove(mDesc);
      }
      mDesc = new JLabel(HTMLTextHelper.convertHtmlToText(descValue));
      mDesc.setMinimumSize(new Dimension(100, 10));
      mDesc.setOpaque(false);
      mPanel.add(mDesc, mCC.xy(3, 3));

      mName.setOpaque(false);
      mName.setForeground(table.getForeground());
      mName.setText(nameValue);

      mPanel.setOpaque(true);

      if (isSelected) {
        mName.setEnabled(true);
        mDesc.setEnabled(true);
        mName.setForeground(table.getSelectionForeground());
        mDesc.setForeground(table.getSelectionForeground());
        mPanel.setBackground(table.getSelectionBackground());
      } else {
        mName.setEnabled(isActivated);
        mDesc.setEnabled(isActivated);
        mName.setForeground(table.getForeground());
        mDesc.setForeground(table.getForeground());
        mPanel.setBackground(table.getBackground());
      }

      mPanel.setToolTipText(descValue);

      int rowHeight = mPanel.getPreferredSize().height + table.getRowMargin();
      if (table.getRowHeight() < rowHeight) {
        table.setRowHeight(rowHeight);
      }
      return mPanel;
    }

    return label;
  }

  private PluginTableCellRenderer() {
    super();
  }

  /**
   * Gets the instance of this class.
   * <p>
   *
   * @return The instance of this class.
   */
  public static PluginTableCellRenderer getInstance() {
    if (mInstance == null) {
      mInstance = new PluginTableCellRenderer();
    }
    return mInstance;
  }

}
