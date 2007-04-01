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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tvbrowser.core.plugin.PluginProxy;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The CellRenderer for the Plugin-List
 * 
 * @author bodum
 */
public class PluginListCellRenderer extends DefaultListCellRenderer {
  /** Translation */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(PluginListCellRenderer.class);
  /** Panel that shows the Informations*/
  private JPanel panel;
  /** Description */
  private JTextArea desc;
  /** Icon */
  private JLabel icon;
  /** Name */
  private JLabel name;
  /** Cell-Constraints*/
  private CellConstraints cc = new CellConstraints();
  
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {

    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    
    if (value instanceof PluginProxy) {
      PluginProxy plugin = (PluginProxy) value;

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
      icon.setIcon(plugin.getPluginIcon());

      if (desc != null)
        panel.remove(desc);
      desc = UiUtilities.createHelpTextArea(plugin.getInfo().getDescription());
      desc.setMinimumSize(new Dimension(100, 10));
      desc.setOpaque(false);
      desc.setForeground(label.getForeground());
      desc.setBackground(label.getBackground());
      desc.setEnabled(plugin.isActivated());
      panel.add(desc, cc.xy(3,3));

      name.setOpaque(false);
      name.setForeground(label.getForeground());
      name.setBackground(label.getBackground());
      
      if (plugin.isActivated()) {
        name.setText(plugin.getInfo().getName() + " " + plugin.getInfo().getVersion());
        name.setEnabled(true);        
      } else {
        name.setText(plugin.getInfo().getName() + " " + plugin.getInfo().getVersion() + " ["+mLocalizer.msg("deactivated", "Deactivated")+"]");
        name.setEnabled(false);
      }
      
      panel.setOpaque(label.isOpaque());
      panel.setBackground(label.getBackground());
      
      return panel;
    }

    return label;
  }


}
