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

package util.ui.customizableitems;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.update.SoftwareUpdateItem;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.Channel;
import devplugin.Version;

/**
 * A ListCellRenderer for SelectableItems.
 * 
 * @author René Mach
 */
public class SelectableItemRenderer implements ListCellRenderer {
  private static final ImageIcon NEW_VERSION_ICON = IconLoader.getInstance().getIconFromTheme("status", "software-update-available", 16);
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SelectableItemRenderer.class);
  private int mSelectionWidth;
  private boolean mIsEnabled = true;
  
  public Component getListCellRendererComponent(JList list, Object value,
  int index, boolean isSelected, boolean cellHasFocus) {
    JPanel p = new JPanel(new BorderLayout(2,0));
    p.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
    
    SelectableItem selectableItem = (SelectableItem) value;

    JCheckBox cb = new JCheckBox("",selectableItem.isSelected());
    mSelectionWidth = cb.getPreferredSize().width;
    
    cb.setOpaque(false);
    
    p.add(cb, BorderLayout.WEST);
    
    if(selectableItem.getItem() instanceof Channel) {
      JLabel l = new JLabel();
      
      if(Settings.propShowChannelNamesInChannellist.getBoolean()) {
        l.setText(selectableItem.getItem().toString());
      }
      
      if(!mIsEnabled)
        l.setEnabled(false);
      
      l.setOpaque(false);
      
      if(Settings.propShowChannelIconsInChannellist.getBoolean()) {
        l.setIcon(UiUtilities.createChannelIcon(((Channel)selectableItem.getItem()).getIcon()));
      }
      
      p.add(l, BorderLayout.CENTER);
      
      if(isSelected && mIsEnabled)
        l.setForeground(list.getSelectionForeground());
      else
        l.setForeground(list.getForeground());
    } else if(selectableItem.getItem() instanceof SoftwareUpdateItem) {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("default,5dlu,default:grow","2dlu,default,2dlu,default,2dlu"));
      pb.getPanel().setOpaque(false);
      
      SoftwareUpdateItem item = (SoftwareUpdateItem)selectableItem.getItem();
      
      JLabel label = pb.addLabel(item.getName() + " " + item.getVersion(), cc.xy(1,2));
      label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+2));
      
      JLabel label2 = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getDescription().length() > 100 ? item.getDescription().substring(0,100) + "..." : item.getDescription()), cc.xyw(1,4,3));
      
      JLabel label3 = new JLabel();
      
      Version installedVersion = item.getInstalledVersion();
      if ((installedVersion != null) && (installedVersion.compareTo(item.getVersion()) < 0)) { 
        label.setIcon(NEW_VERSION_ICON);
        
        label3.setText("(" + mLocalizer.msg("installedVersion","Installed version: ") + installedVersion.toString()+")");
        label3.setForeground(Color.gray);
        label3.setFont(label3.getFont().deriveFont((float)label3.getFont().getSize2D()+2));
        
        pb.add(label3, cc.xy(3,2));
      }
      
      if (isSelected && mIsEnabled) {
        label.setForeground(list.getSelectionForeground());
        label2.setForeground(list.getSelectionForeground());
        label3.setForeground(list.getSelectionForeground());
      } else {
       label.setForeground(list.getForeground());
       label2.setForeground(list.getForeground());
      }
      
      p.add(pb.getPanel(), BorderLayout.CENTER);
    } else {
      cb.setText(selectableItem.getItem().toString());
    }
    
    if (isSelected && mIsEnabled) {
      p.setOpaque(true);
      p.setBackground(list.getSelectionBackground());
      cb.setForeground(list.getSelectionForeground());
      
    } else {
      p.setOpaque(false);
      p.setForeground(list.getForeground());
      cb.setForeground(list.getForeground());
    }
    cb.setEnabled(list.isEnabled());

    return p;
  }
  
  /**
   * @param value If the renderer should be enabled
   */
  public void setEnabled(boolean value) {
    mIsEnabled = value;
  }
  
  /**
   * @return The selection width.
   */
  public int getSelectionWidth() {
    return mSelectionWidth;
  }

}