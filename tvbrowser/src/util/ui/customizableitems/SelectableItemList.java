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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;

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
  
  private SelectableItemListModel mListModel;
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
    this(currSelection,allItems,null);
  }

  
  /**
   * Creates the SelectableItemList without the selection buttons.
   * 
   * @param currSelection The currently selected Objects.
   * @param allItems All Objects of the list.
   * @param notSelectableItems All Objects that could not be selected/deselected
   * 
   * @since 2.7.2
   */
  public SelectableItemList(Object[] currSelection, Object[] allItems, Object[] notSelectableItems) {
    this(currSelection, allItems, false, notSelectableItems);
  } 
  
  /**
   * Creates the SelectableItemList.
   * 
   * @param currSelection The currently selected Objects.
   * @param allItems All Objects of the list.
   * @param showSelectionButtons If the selection buttons should be shown.
   */
  public SelectableItemList(Object[] currSelection, Object[] allItems, boolean showSelectionButtons) {
    this(currSelection,allItems,showSelectionButtons,null);
  }
  
  /**
   * Creates the SelectableItemList.
   * 
   * @param currSelection The currently selected Objects.
   * @param allItems All Objects of the list.
   * @param showSelectionButtons If the selection buttons should be shown.
   * @param notSelectableItems All Objects that could not be selected/deselected
   * 
   * @since 2.7.2
   */
  public SelectableItemList(Object[] currSelection, Object[] allItems, boolean showSelectionButtons, Object[] notSelectableItems) {
    setLayout(new BorderLayout(0,3));
    
    mListModel = new SelectableItemListModel();
    setEntries(currSelection,allItems,notSelectableItems);
    
    mList = new JList(mListModel);
    mList.setCellRenderer(mItemRenderer = new SelectableItemRenderer());
    
    mScrollPane = new JScrollPane(mList);
    
    mScrollPane.getVerticalScrollBar().setBlockIncrement(50);
    mScrollPane.getVerticalScrollBar().setUnitIncrement(20);
    
    mItemRenderer.setScrollPane(mScrollPane);
    
    add(mScrollPane, BorderLayout.CENTER);
    
    mList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        if (evt.getX() < mItemRenderer.getSelectionWidth() && mIsEnabled) {
          int index = mList.locationToIndex(evt.getPoint());
             
          if (index != -1) {
            if(mList.getCellBounds(index,index).contains(evt.getPoint())) {
              SelectableItem item = (SelectableItem) mListModel.getElementAt(index);
              if(item.isSelectable()) {
                item.setSelected(! item.isSelected());
                handleItemSelectionChanged();
                mList.repaint();
              }
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
      
      public void keyReleased(KeyEvent e) {
        calculateSize();
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
  
  private void setEntries(Object[] currSelection, Object[] allItems, Object[] disabledItems) {
    mListModel.removeAllElements();
    
    ArrayList<Object> selectionList = new ArrayList<Object>();
    
    for (int i = 0; i < currSelection.length; i++)
      selectionList.add(currSelection[i]);
    
    for (int i = 0; i < allItems.length; i++) {
      SelectableItem item = new SelectableItem(allItems[i], selectionList.remove(allItems[i]),!arrayContainsItem(disabledItems,allItems[i]));
      mListModel.addElement(item);
    }
  }
  
  private boolean arrayContainsItem(Object[] itemArr, Object item) {
    if(item != null && itemArr != null) {
      for(Object arrayItem : itemArr) {
        if(arrayItem != null && arrayItem.equals(item)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  /**
   * Attention: This is not a List with all selected Items in the List. This List 
   * is a List with all checked Items
   * 
   * @return The selected Objects
   */
  public Object[] getSelection() {
    return mListModel.getSelection();
  }
  
  /**
   * @return The current selected value (value that has focus)
   * @since 2.5
   */
  public Object getSelectedValue() {
    return mList.getSelectedValue();
  }

  /**
   * set the (focus) selection to the item with the given index
   * 
   * @param index
   * @since 3.0
   */
  public void setSelectedIndex(int index) {
    mList.setSelectedIndex(0);
  }

  /**
   * Invert the selection
   */
  public void invertSelection() {
    if(mIsEnabled) {
      mListModel.invertSelection();
      mList.repaint();
    }
  }

  /**
   * Select all items.
   */
  public void selectAll() {
    if(mIsEnabled) {
      mListModel.selectAll();
      mList.repaint();
    }
  }
  
  /**
   * Clear the selection.
   */
  public void clearSelection() {
    if(mIsEnabled) {
      mListModel.clearSelection();
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
   * Calculates the size of the list.
   */
  public void calculateSize() {
    if(mList != null) {
      mList.setSize(mList.getPreferredSize());
      mList.ensureIndexIsVisible(mList.getSelectedIndex());
    }
    
    mList.repaint();
  }
  
  /**
   * Adds the render component that is to be used for the given class or it's super class.
   * <p>
   * @param clazz The class to use the render component for, the render component is also used for the super class of clazz.
   * @param component The render component.
   * @since 2.7
   */

  public void addCenterRendererComponent(Class clazz, SelectableItemRendererCenterComponentIf component) {
    mItemRenderer.setCenterRendererComponent(clazz,component);
  }
  
  /**
   * Sets the UI to be used for the list.
   * <p>
   * @param ui The list ui that should be used for the list.
   * @since 2.7
   */
  public void setListUI(ListUI ui) {
    mList.setUI(ui);
  }
  
  /**
   * Sets if the horizontal scroll policy.
   * <p>
   * @param value The values from ScrollPaneConstants.
   * @since 2.7
   */
  public void setHorizontalScrollBarPolicy(int value) {
    mScrollPane.setHorizontalScrollBarPolicy(value);
  }
  
  /**
   * Adds a mouse listener to the list.
   * <p>
   * @param listener The listener to add.
   * @since 2.7
   */
  public void addMouseListener(MouseListener listener) {
    mList.addMouseListener(listener);
  }
  
  /**
   * Sets the combo box that contains ItemFilters to filter
   * the shown values of the list.
   * <p>
   * @param filterBox The combo box with the ItemFilters.
   * @since 2.7
   */
  public void setFilterComboBox(JComboBox filterBox) {
    mListModel.setComboBox(filterBox);
  }
  
  private static class SelectableItemListModel extends AbstractListModel {
    private JComboBox mFilterBox;
    
    private ArrayList<Object> mFullList = new ArrayList<Object>();
    private ArrayList<Object> mFilteredList = new ArrayList<Object>();
    
    protected void setComboBox(JComboBox filterBox) {
      mFilterBox = filterBox;
      
      mFilterBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if(e.getStateChange() == ItemEvent.SELECTED) {
            mFilteredList.clear();
            
            Object filter = mFilterBox.getSelectedItem();
            
            for(Object o : mFullList) {
              if(filter instanceof ItemFilter) {
                if((((ItemFilter)filter).accept(((SelectableItem)o).getItem()))) {
                  mFilteredList.add(o);
                }
              }
              else {
                mFilteredList.add(o);
              }
            }
            
            fireIntervalRemoved(this,0,mFullList.size());
            fireIntervalAdded(this,0,mFilteredList.size());
          }
        }

      });
    }

    protected void addElement(Object o) {
      mFullList.add(o);
      
      if(mFilterBox != null) {
        Object filter = mFilterBox.getSelectedItem();
        
        if(filter instanceof ItemFilter) {
          if((((ItemFilter)filter).accept(((SelectableItem)o).getItem()))) {
            mFilteredList.add(o);
          }
        }
        else {
          mFilteredList.add(o);
        }
      }
      else {
        mFilteredList.add(o);
      }
    }

    public Object getElementAt(int index) {
      return mFilteredList.get(index);
    }

    public int getSize() {
      return mFilteredList.size();
    }
    
    protected int size() {
      return getSize();
    }
    
    protected void removeAllElements() {
      mFullList.clear();
      mFilteredList.clear();
    }
    
    protected Object[] getSelection() {
      ArrayList<Object> objList = new ArrayList<Object>();
      for (int i = 0; i < mFullList.size(); i++) {
        SelectableItem item = (SelectableItem) mFullList.get(i);
        if (item.isSelected()) {
          objList.add(item.getItem());
        }
      }

      Object[] asArr = new Object[objList.size()];
      objList.toArray(asArr);

      return asArr;
    }
    
    protected void selectAll() {
      for (int i = 0; i < mFullList.size(); i++) {
        SelectableItem item = (SelectableItem) mFullList.get(i);
        item.setSelected(true);
      }
    }
    
    protected void clearSelection() {
      for (int i = 0; i < mFullList.size(); i++) {
        SelectableItem item = (SelectableItem) mFullList.get(i);
        item.setSelected(false);
      }
    }
    
    protected void invertSelection() {
      for (int i = 0; i < mFullList.size(); i++) {
        SelectableItem item = (SelectableItem) mFullList.get(i);
        item.setSelected(!item.isSelected());
      }
    }
  }
}
