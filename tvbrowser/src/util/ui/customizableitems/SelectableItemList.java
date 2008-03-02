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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A class that provides a list that contains selectable items.
 *  
 * @author René Mach
 * 
 */
public class SelectableItemList extends JPanel {
  
  private static final long serialVersionUID = 1L;

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SelectableItemList.class);
  
  private DefaultListModel mListModel;
  private SelectableItemRenderer mItemRenderer;
  
  private JButton mSelectAllBt;
  private JButton mDeSelectAllBt; 
  
  private JList mList;
  private boolean mIsEnabled = true;
  private JScrollPane mScrollPane;
  
  /**
   * Creates the SelectableItemList without the selection buttons.
   * 
   * @param currSelection The currently selected Objects.
   * @param allItems All Objects of the list.
   */
  public SelectableItemList(Object[] currSelection, Object[] allItems) {
    this(currSelection, allItems, false);
  } 
  
  /**
   * Creates the SelectableItemList.
   * 
   * @param currSelection The currently selected Objects.
   * @param allItems All Objects of the list.
   * @param showSelectionButtons If the selection buttons should be shown.
   */
  public SelectableItemList(Object[] currSelection, Object[] allItems, boolean showSelectionButtons) {
    setLayout(new BorderLayout(0,3));
    
    mListModel = new DefaultListModel();
    setEntries(currSelection,allItems);
    mList = new JList(mListModel);
    mList.setUI(new MyListUI());
    mList.setCellRenderer(mItemRenderer = new SelectableItemRenderer());

    mList.addListSelectionListener(new ListSelectionListener() {
      private int mLastIndex = -1;
      
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          if(mLastIndex != -1 && mList.getSelectedIndex() != mLastIndex) {
            ((MyListUI)mList.getUI()).setCellHeight(mLastIndex,mList.getCellRenderer().getListCellRendererComponent(mList, mList.getModel().getElementAt(mLastIndex),
                mLastIndex, false, false).getPreferredSize().height);
          }
          
          mLastIndex = mList.getSelectedIndex();
        }
      }
    });
    
    mScrollPane = new JScrollPane(mList);
    
    mScrollPane.getVerticalScrollBar().setBlockIncrement(50);
    mScrollPane.getVerticalScrollBar().setUnitIncrement(20);
    
    add(mScrollPane, BorderLayout.CENTER);
    
    mList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        if (evt.getX() < mItemRenderer.getSelectionWidth() && mIsEnabled) {
          int index = mList.locationToIndex(evt.getPoint());
          
          if (index != -1) {
            if(mList.getCellBounds(index,index).contains(evt.getPoint())) {
              SelectableItem item = (SelectableItem) mListModel.elementAt(index);
              item.setSelected(! item.isSelected());
              handleItemSelectionChanged();
              mList.repaint();
            }
          }
        }
      }
    });
    mList.addKeyListener(new KeyAdapter(){
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          Object[] objs = mList.getSelectedValues();
          for (int i=0;i<objs.length;i++){
            if (objs[i] instanceof SelectableItem) {
              SelectableItem item = (SelectableItem) objs[i];
              item.setSelected(!item.isSelected());
            }
          }
          handleItemSelectionChanged();
          mList.repaint();
        }
      }
    });
    
    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("pref,3dlu:grow,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    
    JPanel p3 = new JPanel(layout);
    
    mSelectAllBt = new JButton(mLocalizer.msg("addAll", "Select all items"));
    mSelectAllBt.setToolTipText(mLocalizer.msg("tooltip.all", "Select all items in the list."));
    mSelectAllBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectAll();
      }      
    });
    p3.add(mSelectAllBt, cc.xy(1,1));

    mDeSelectAllBt = new JButton(mLocalizer.msg("delAll", "Deselect all items"));
    mDeSelectAllBt.setToolTipText(mLocalizer.msg("tooltip.none", "Deselect all items in the list."));
    mDeSelectAllBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearSelection();
      }      
    });
    p3.add(mDeSelectAllBt, cc.xy(3,1));
    
    if(showSelectionButtons)
      add(p3, BorderLayout.SOUTH);
    
  }
  
  private void handleItemSelectionChanged() {
    ListSelectionListener[] listeners = mList.getListSelectionListeners();
    if(listeners != null) {
      for(ListSelectionListener listener : listeners) {
        if(mList.getSelectedValue() != null) {
          int[] indices = mList.getSelectedIndices();
          listener.valueChanged(new ListSelectionEvent(mList.getSelectedValue(), indices[0], indices[indices.length-1], false));
        }
      }
    }
  }
  
  /**
   * @since 2.5
   * @param listener Add this Listener
   */
  public void addListSelectionListener(ListSelectionListener listener) {
    mList.addListSelectionListener(listener);
  }
  
  /**
   * @since 2.5
   * @param listener Remove this Listener
   */
  public void removeListSelectionListener(ListSelectionListener listener) {
    mList.removeListSelectionListener(listener);
  }

  /**
   * @param selectionMode The selection mode of the list.
   * @since 2.5
   */
  public void setSelectionMode(int selectionMode) {
    mList.setSelectionMode(selectionMode);
  }
  
  /**
   * Current selected Items in the List.
   * 
   * Attention: This is not a List with all activated Items.
   * 
   * @since 2.5
   * @return Current selected Items in the List
   */
  public Object[] getListSelection() {
    Object[] values = mList.getSelectedValues();
    
    Object[] items = new Object[values.length];
    
    int max = values.length;
    for (int i=0;i< max;i++) {
      items[i] = ((SelectableItem)values[0]).getItem();
    }
    
    return items;
  }
  
  private void setEntries(Object[] currSelection, Object[] allItems) {
    mListModel.removeAllElements();
    
    ArrayList<Object> selectionList = new ArrayList<Object>();
    
    for (int i = 0; i < currSelection.length; i++)
      selectionList.add(currSelection[i]);
    
    for (int i = 0; i < allItems.length; i++) {
      SelectableItem item = new SelectableItem(allItems[i], selectionList.remove(allItems[i]));
      mListModel.addElement(item);
    }
  }
  
  /**
   * Attention: This is not a List with all selected Items in the List. This List 
   * is a List with all checked Items
   * 
   * @return The selected Objects
   */
  public Object[] getSelection() {
    ArrayList<Object> objList = new ArrayList<Object>();
    for (int i = 0; i < mListModel.size(); i++) {
      SelectableItem item = (SelectableItem) mListModel.elementAt(i);
      if (item.isSelected()) {
        objList.add(item.getItem());
      }
    }

    Object[] asArr = new Object[objList.size()];
    objList.toArray(asArr);

    return asArr;
  }
  
  /**
   * @return The current selected value (value that has focus)
   * @since 2.5
   */
  public Object getSelectedValue() {
    return mList.getSelectedValue();
  }
  
  /**
   * Invert the selection
   */
  public void invertSelection() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
        item.setSelected(!item.isSelected());
      }
      mList.repaint();
    }
  }

  /**
   * Select all items.
   */
  public void selectAll() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
        item.setSelected(true);
      }
      mList.repaint();
    }
  }
  
  /**
   * Clear the selection.
   */
  public void clearSelection() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
        item.setSelected(false);
      }
      mList.repaint();
    }
  }
  
  public void setEnabled(boolean value) {
    mIsEnabled = value;
    mItemRenderer.setEnabled(value);
    mList.setEnabled(value);
    mSelectAllBt.setEnabled(value);
    mDeSelectAllBt.setEnabled(value);
    mScrollPane.getVerticalScrollBar().setEnabled(value);
    mScrollPane.setWheelScrollingEnabled(value);
  }
  
  /**
   * Calcualtes the size of the list.
   */
  public void calculateSize() {
    if(mList != null) {
      mList.setSize(mList.getPreferredSize());
      mList.ensureIndexIsVisible(mList.getSelectedIndex());
    }
  }
  
  protected static class MyListUI extends javax.swing.plaf.basic.BasicListUI {
    protected synchronized void setCellHeight(int row, int height) {
      cellHeights[row] = height;      
    }
    
    public Dimension getPreferredSize(JComponent c) {
      int width = super.getPreferredSize(c).width;
      int height = 0;
      
      Insets i = c.getInsets();
      
      height += i.top + i.bottom;
      
      for(int cellHeight : cellHeights) {
        height += cellHeight;
      }
      
      return new Dimension(width,height);
    }
  }
}
