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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui.customizableitems;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import tvbrowser.core.Settings;
import util.ui.UiUtilities;
import devplugin.Channel;

/**
 * A ListCellRenderer for SelectableItems.
 * 
 * @author René Mach
 */
public class SelectableItemRenderer implements ListCellRenderer {
  private int mSelectionWidth;
  private boolean mIsEnabled = true;
  
  private HashMap<Class,SelectableItemRendererCenterComponentIf> mCenterComponentMap = new HashMap<Class,SelectableItemRendererCenterComponentIf>();
  
  private JScrollPane mParentScrollPane;
  
  public Component getListCellRendererComponent(JList list, Object value,
  int index, boolean isSelected, boolean cellHasFocus) {
    JPanel p = new JPanel(new BorderLayout(2,0));
    try { p.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
    
    SelectableItem selectableItem = (SelectableItem) value;

    JCheckBox cb = new JCheckBox("",selectableItem.isSelected());
    mSelectionWidth = cb.getPreferredSize().width;
    
    cb.setOpaque(false);
    
    p.add(cb, BorderLayout.WEST);
    
    SelectableItemRendererCenterComponentIf renderIf = mCenterComponentMap.get(selectableItem.getItem().getClass());
    
    if(renderIf == null) {
      renderIf = mCenterComponentMap.get(selectableItem.getItem().getClass().getSuperclass());
    }
    
    if(renderIf != null) {
      cb.setVerticalAlignment(SwingConstants.TOP);
      cb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,0,0),cb.getBorder()));
      
      p.add(renderIf.createCenterPanel(list,selectableItem.getItem(),index,isSelected,mIsEnabled, mParentScrollPane, mSelectionWidth + 2), BorderLayout.CENTER);
      renderIf.calculateSize(list, index, p);
    }
    else if(selectableItem.getItem() instanceof Channel) {
      JLabel l = new JLabel();
      
      if(Settings.propShowChannelNamesInChannellist.getBoolean()) {
        l.setText(selectableItem.getItem().toString());
      }
      
      if(!mIsEnabled) {
        l.setEnabled(false);
      }
      
      l.setOpaque(false);
      
      if(Settings.propShowChannelIconsInChannellist.getBoolean()) {
        l.setIcon(UiUtilities.createChannelIcon(((Channel)selectableItem.getItem()).getIcon()));
      }
      
      p.add(l, BorderLayout.CENTER);
      
      if(isSelected && mIsEnabled) {
        l.setForeground(list.getSelectionForeground());
      } else {
        l.setForeground(list.getForeground());
      }
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
    cb.setEnabled(list.isEnabled() && selectableItem.isSelectable());
    }catch(Throwable t){t.printStackTrace();}
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
  
  /**
   * Sets the render component that is to be used for the given class or it's super class.
   * <p>
   * @param clazz The class to use the render component for, the render component is also used for the super class of clazz.
   * @param component The render component.
   * @since 2.7
   */
  public void setCenterRendererComponent(Class clazz, SelectableItemRendererCenterComponentIf component) {
    mCenterComponentMap.put(clazz,component);
  }

  protected void setScrollPane(JScrollPane parentScrollPane) {
    mParentScrollPane = parentScrollPane;
  }
}