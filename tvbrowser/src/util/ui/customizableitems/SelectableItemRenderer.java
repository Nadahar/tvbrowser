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

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * A ListCellRenderer for SelectableItems.
 * 
 * @author René Mach
 */
public class SelectableItemRenderer<E> implements ListCellRenderer<SelectableItem<E>> {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(SelectableItemRenderer.class);
  
  private int mSelectionWidth;
  private boolean mIsEnabled = true;
  
  private HashMap<Class<?>,SelectableItemRendererCenterComponentIf<E>> mCenterComponentMap = new HashMap<Class<?>,SelectableItemRendererCenterComponentIf<E>>();
  
  private JScrollPane mParentScrollPane;
  
  public JPanel getListCellComponent(JList<? extends SelectableItem<E>> list, SelectableItem<E> value,
  int index, boolean isSelected, boolean cellHasFocus) {
    SelectableItem<E> selectableItem = value;
    JCheckBox cb = new JCheckBox("",selectableItem.isSelected());
    
    JPanel p = new JPanel(new FormLayout("default,1dlu,default:grow","default"));
    try { p.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
    mSelectionWidth = cb.getPreferredSize().width + 2 + p.getInsets().left;
    
    p.add(cb, CC.xy(1, 1));
    
    SelectableItemRendererCenterComponentIf<E> renderIf = mCenterComponentMap.get(selectableItem.getItem().getClass());
    
    if(renderIf == null) {
      renderIf = mCenterComponentMap.get(selectableItem.getItem().getClass().getSuperclass());
    }
    
    if(renderIf != null) {
      cb.setVerticalAlignment(SwingConstants.TOP);
      cb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,0,0),cb.getBorder()));
      
      p.add(renderIf.createCenterPanel(list,selectableItem.getItem(),index,isSelected,mIsEnabled, mParentScrollPane, mSelectionWidth + 2), CC.xy(3, 1));
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
      
      p.add(l, CC.xy(3, 1));
      
      if(isSelected && mIsEnabled) {
        l.setForeground(list.getSelectionForeground());
      } else {
        l.setForeground(list.getForeground());
      }
    } else if(selectableItem.getItem() instanceof String && selectableItem.getItem().equals("\n")) {
      cb.setText(LOCALIZER.msg("lineFeed", "Line feed"));
    } else {
      cb.setText(selectableItem.getItem().toString());
    }
    
    if (isSelected && mIsEnabled) {
      p.setOpaque(true);
      p.setBackground(list.getSelectionBackground());
      cb.setForeground(list.getSelectionForeground());
      cb.setBackground(list.getSelectionBackground());
    } else {
      p.setOpaque(false);
      p.setForeground(list.getForeground());
      cb.setForeground(list.getForeground());
      cb.setBackground(list.getBackground());
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
  public void setCenterRendererComponent(Class<?> clazz, SelectableItemRendererCenterComponentIf<E> component) {
    mCenterComponentMap.put(clazz,component);
    component.initialize();
  }

  protected void setScrollPane(JScrollPane parentScrollPane) {
    mParentScrollPane = parentScrollPane;
  }

  public Component getListCellRendererComponent(JList<? extends SelectableItem<E>> list, SelectableItem<E> value, int index, boolean isSelected,
      boolean cellHasFocus) {
      return getListCellComponent( list, value, index, isSelected, cellHasFocus);
  }
}