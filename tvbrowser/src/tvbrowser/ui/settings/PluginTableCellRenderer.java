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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.extras.common.InternalPluginProxyIf;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The CellRenderer for the plugin table
 * 
 * @author Bananeweizen
 */
public class PluginTableCellRenderer extends DefaultTableCellRenderer {

  /** Panel that shows the Information*/
  private JPanel panel;
  /** Description */
  private JTextArea desc;
  /** Icon */
  private JLabel icon;
  /** Name */
  private JLabel name;
  /** Cell-Constraints*/
  private CellConstraints cc = new CellConstraints();
  private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
  
  /**
   * empty label for active column of core plugins
   */
  private static final JLabel EMPTY_LABEL = new JLabel();
  
  /**
   * checkBox to return for the first column (plugin active)
   */
  private static JCheckBox checkBox;
  
  /**
   * singleton implementation
   */
  private static PluginTableCellRenderer instance;
  
  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    
    if (column == 0) {
      if (value == null) {
        return EMPTY_LABEL;
      }
      if (checkBox == null) {
        checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(JLabel.CENTER);
        checkBox.setBorderPainted(true);
      }
      if (isSelected) {
        checkBox.setForeground(table.getSelectionForeground());
        checkBox.setBackground(table.getSelectionBackground());
      } else {
        checkBox.setForeground(table.getForeground());
        checkBox.setBackground(table.getBackground());
      }
      checkBox.setSelected((value != null && ((Boolean) value).booleanValue()));

      if (hasFocus) {
        checkBox.setBorder(UIManager
            .getBorder("Table.focusCellHighlightBorder"));
      } else {
        checkBox.setBorder(noFocusBorder);
      }

      return checkBox;
    }

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof PluginProxy || value instanceof InternalPluginProxyIf) {
      Icon iconValue = null;
      String nameValue = null;
      String descValue = null;
      boolean isActivated = true;
      
      if(value instanceof PluginProxy) {
        PluginProxy plugin = (PluginProxy) value;
        
        iconValue = plugin.getPluginIcon();
        descValue = plugin.getInfo().getDescription().replace('\n', ' ');
        
        isActivated = plugin.isActivated();
        
        nameValue = plugin.getInfo().getName() + " " + plugin.getInfo().getVersion();
      }
      else {
        InternalPluginProxyIf plugin = (InternalPluginProxyIf)value;
        
        nameValue = plugin.getName();
        descValue = plugin.getDescription().replace('\n', ' ');
        iconValue = plugin.getIcon();
      }
      
      
      if (panel == null) {
        icon = new JLabel();
        name = new JLabel();
        name.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+2));
        
        panel = new JPanel(new FormLayout("default, 2dlu, fill:pref:grow","default, 2dlu, default"));
        panel.setBorder(Borders.DLU2_BORDER);
        
        panel.add(icon, cc.xy(1,1));
        panel.add(name, cc.xy(3,1));
      }

      icon.setOpaque(label.isOpaque());
      icon.setBackground(label.getBackground());
      icon.setIcon(iconValue);

      if (desc != null)
        panel.remove(desc);
      desc = UiUtilities.createHelpTextArea(descValue);
      desc.setMinimumSize(new Dimension(100, 10));
      desc.setOpaque(false);
      desc.setForeground(label.getForeground());
      desc.setBackground(label.getBackground());
      desc.setEnabled(isActivated);
      panel.add(desc, cc.xy(3,3));

      name.setOpaque(false);
      name.setForeground(label.getForeground());
      name.setBackground(label.getBackground());     
      
      name.setText(nameValue);
      name.setEnabled(isActivated);        
      
      panel.setOpaque(label.isOpaque());
      panel.setBackground(label.getBackground());
      
      panel.setToolTipText(descValue);
     
      int rowHeight = panel.getPreferredSize().height + table.getRowMargin();
      if (table.getRowHeight() < rowHeight) {
        table.setRowHeight(rowHeight);
      }
      return panel;
    }

    return label;
  }

  private PluginTableCellRenderer() {
    super();
  }

  public static PluginTableCellRenderer getInstance() {
    if (instance == null) {
      instance = new PluginTableCellRenderer();
    }
    return instance;
  }


}
