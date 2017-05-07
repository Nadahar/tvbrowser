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
package util.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.Sizes;

import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemRenderer;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class OrderChooser<E> extends JPanel implements ListDropAction<SelectableItem<E>>{

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(OrderChooser.class);

  /**
   * Der Bereich, in dem ein Mausklick als Selektion/Deselektion und nicht als
   * Markierung aufgefasst wird.
   * (Wird vom Renderer gesetzt)
   */
  private JList<SelectableItem<E>> mList;
  private DefaultListModel<SelectableItem<E>> mListModel;
  private SelectableItemRenderer<E> mItemRenderer;
  private JButton mUpBt;
  private JButton mDownBt;
  private JButton mSelectAllBt;
  private JButton mDeSelectAllBt;
  private boolean mIsEnabled = true;
  private JScrollPane mScrollPane;
  private JPanel mButtonPanel;

  /**
   * Constructs an OrderChooser without selection Buttons.
   * 
   * @param currOrder Die aktuelle Reihenfolge
   * @param allItems Alle moeglichen Objekte (die Objekte der aktuellen Reihenfolge
   *        eingeschlossen)
   */
  public OrderChooser(E[] currOrder, E[] allItems) {
    this(currOrder, allItems, false);
  }

  /**
   * Konstruiert einen OrderChooser.
   * <P>
   * Die Reihenfolge wird aus currOrder Uebernommen. Dann wird das Array
   * allItems durchgegangen und jedes Objekt, das nicht in der Reihenfolge
   * vorkommt, wird aufgenommen.
   *
   * @param currOrder Die aktuelle Reihenfolge
   * @param allItems Alle moeglichen Objekte (die Objekte der aktuellen Reihenfolge
   *        eingeschlossen)
   * @param showSelectionButtons Shows the selection buttons.
   */
  public OrderChooser(E[] currOrder, E[] allItems, boolean showSelectionButtons) {
    this(currOrder, allItems, showSelectionButtons, null, null);
  }

  public OrderChooser(E[] currOrder, E[] allItems, final Class<?> renderClass, final SelectableItemRendererCenterComponentIf<E> renderComponent) {
    this(currOrder, allItems, false, renderClass, renderComponent);
  }
  
  public OrderChooser(E[] currOrder, E[] allItems, boolean showSelectionButtons, final Class<?> renderClass, final SelectableItemRendererCenterComponentIf<E> renderComponent) {
    super(new BorderLayout());

    JPanel p1, p3, main;
    
    main = new JPanel(new BorderLayout(0,3));

    mListModel = new DefaultListModel<>();
    setEntries(currOrder,allItems);
    mList = new JList<>(mListModel);
    mList.setCellRenderer(mItemRenderer = new SelectableItemRenderer<>());
    if (renderClass != null && renderComponent != null) {
      mItemRenderer.setCenterRendererComponent(renderClass, renderComponent);
    }

    // Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mList,mList,this);
    new DragAndDropMouseListener<SelectableItem<E>>(mList,mList,this,dnDHandler);
    
    // MouseListener hinzufügen, der das Selektieren/Deselektieren übernimmt
    mList.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mItemRenderer.getSelectionWidth() && mIsEnabled) {
          int index = mList.locationToIndex(evt.getPoint());
          if (index != -1) {
            SelectableItem<E> item = mListModel.elementAt(index);
            item.setSelected(! item.isSelected());
            mList.repaint();
          }
        }
      }
    });
    mList.addKeyListener(new KeyAdapter(){
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          List<SelectableItem<E>> objs = mList.getSelectedValuesList();
          for (SelectableItem<E> obj : objs) {
            obj.setSelected(!obj.isSelected());
          }
          mList.repaint();
        }
      }
    });

    mScrollPane = new JScrollPane(mList);
    main.add(mScrollPane, BorderLayout.CENTER);
    add(main, BorderLayout.CENTER);
    
    p1 = new JPanel(new BorderLayout());
    p1.setBorder(BorderFactory.createEmptyBorder(0, Sizes.dialogUnitXAsPixel(3, p1), 0, 0));
    mButtonPanel = new JPanel(new TabLayout(1));
    add(p1, BorderLayout.EAST);
    p1.add(mButtonPanel, BorderLayout.NORTH);

    mUpBt = new JButton(TVBrowserIcons.up(TVBrowserIcons.SIZE_LARGE));
    mUpBt.setToolTipText(mLocalizer.msg("tooltip.up", "Move selected rows up"));
    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        UiUtilities.moveSelectedItems(mList,-1);
      }
    });
    mButtonPanel.add(mUpBt);

    mDownBt = new JButton(TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE));
    mDownBt.setToolTipText(mLocalizer.msg("tooltip.down", "Move selected rows down"));
    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        UiUtilities.moveSelectedItems(mList,1);
      }
    });
    mButtonPanel.add(mDownBt);
    
    p3 = new JPanel(new BorderLayout());
    
    mSelectAllBt = new JButton(mLocalizer.msg("addAll", "Select all items"));
    mSelectAllBt.setToolTipText(mLocalizer.msg("tooltip.all", "Select all items in the list."));
    mSelectAllBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectAll();
      }
    });
    p3.add(mSelectAllBt, BorderLayout.WEST);

    mDeSelectAllBt = new JButton(mLocalizer.msg("delAll", "Deselect all items"));
    mDeSelectAllBt.setToolTipText(mLocalizer.msg("tooltip.none", "Deselect all items in the list."));
    mDeSelectAllBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearSelection();
      }
    });
    p3.add(mDeSelectAllBt, BorderLayout.EAST);
    
    if(showSelectionButtons) {
      main.add(p3, BorderLayout.SOUTH);
    }
  }

  private void setEntries(E[] currOrder, E[] allItems) {
    mListModel.removeAllElements();
    for (E element : currOrder) {
      if (contains(allItems, element)) {
        SelectableItem<E> item = new SelectableItem<>(element, true);
        mListModel.addElement(item);
      }
    }
    for (int i = 0; i < allItems.length; i++) {
      if (! contains(currOrder, allItems[i])) {
        SelectableItem<E> item = new SelectableItem<>(allItems[i], false);
        mListModel.addElement(item);
      }
    }
  }

  public JButton getUpButton() {
    return mUpBt;
  }
  
  public JButton getDownButton() {
    return mDownBt;
  }

  private boolean contains(Object[] array, Object obj) {
    for (Object element : array) {
      if (element.equals(obj)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * @return The order of selected items.
   * @deprecated since 3.4.5 use {@link #getOrderList()} instead
   */
  public Object[] getOrder() {
    ArrayList<E> objList = new ArrayList<E>();
    for (int i = 0; i < mListModel.size(); i++) {
      SelectableItem<E> item = mListModel.elementAt(i);
      if (item.isSelected()) {
        objList.add(item.getItem());
      }
    }

    Object[] asArr = new Object[objList.size()];
    objList.toArray(asArr);

    return asArr;
  }
  
  /**
   * @return A list with the selected items in order.
   * @since 3.4.5
   */
  public List<E> getOrderList() {
    ArrayList<E> objList = new ArrayList<E>();
    for (int i = 0; i < mListModel.size(); i++) {
      SelectableItem<E> item = mListModel.elementAt(i);
      if (item.isSelected()) {
        objList.add(item.getItem());
      }
    }
    
    return objList;
  }

  public void invertSelection() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem<E> item = mListModel.elementAt(i);
        item.setSelected(!item.isSelected());
      }
      mList.repaint();
    }
  }

  public void selectAll() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem<E> item = mListModel.elementAt(i);
        item.setSelected(true);
      }
      mList.repaint();
    }
  }
  
  public void setOrder(E[] currOrder, E[] allItems) {
    setEntries(currOrder, allItems);
    
    mList.repaint();
  }
  
  public void clearSelection() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem<E> item = mListModel.elementAt(i);
        item.setSelected(false);
      }
      mList.repaint();
    }
  }

 /* protected void moveSelectedItems(int nrRows) {
    int[] selection = mList.getSelectedIndices();
    if (selection.length == 0) return;

    int insertPos = selection[0] + nrRows;
    if (insertPos < 0) {
      insertPos = 0;
    }
    if (insertPos > mListModel.size() - selection.length) {
      insertPos = mListModel.size() - selection.length;
    }

    // Markierte Zeilen merken und entfernen
    Object[] selectedRows = new Object[selection.length];
    for (int i = selectedRows.length - 1; i >= 0; i--) {
      selectedRows[i] = mListModel.elementAt(selection[i]);
      mListModel.removeElementAt(selection[i]);
    }

    // Zeilen wieder einfügen
    for (int i = 0; i < selectedRows.length; i++) {
      mListModel.insertElementAt(selectedRows[i], insertPos + i);
    }

    // Zeilen markieren
    mList.getSelectionModel().setSelectionInterval(insertPos, insertPos + selection.length - 1);

    // Scrollen
    int scrollPos = insertPos;
    if (nrRows > 0) scrollPos += selection.length;
    mList.ensureIndexIsVisible(scrollPos);
  }*/


  public void drop(JList<SelectableItem<E>> source, JList<SelectableItem<E>> target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
  }
  
  public void setEnabled(boolean value) {
    mIsEnabled = value;
    mItemRenderer.setEnabled(value);
    mList.setEnabled(value);
    mUpBt.setEnabled(value);
    mDownBt.setEnabled(value);
    mSelectAllBt.setEnabled(value);
    mDeSelectAllBt.setEnabled(value);
    mScrollPane.getVerticalScrollBar().setEnabled(value);
    mScrollPane.setWheelScrollingEnabled(value);
  }
  
  /**
   * Set the selection mode of the list.
   * 
   * @param value The new selection mode.
   * @since 2.5.1
   */
  public void setSelectionMode(int value) {
    mList.setSelectionMode(value);
  }
  
  /**
   * Add a list selection listener to the list.
   * 
   * @param listener The listener to add.
   * @since 2.5.1
   */
  public void addListSelectionListener(ListSelectionListener listener) {
    mList.addListSelectionListener(listener);
  }
  
  public void addMouseListener(MouseListener listener) {
    mList.addMouseListener(listener);
  }
  
  /**
   * Refreshes the list UI.
   * @since 2.5.1
   */
  public void refreshList() {
    mList.repaint();
  }
  
  /**
   * Add a value to the end of the list.
   * 
   * @param value
   * @since 2.5.1
   */
  public void addElement(E value) {
    SelectableItem<E> item = new SelectableItem<>(value,true);
    mListModel.addElement(item);
    mList.repaint();
  }
  
  /**
   * Remove the value at the given index
   * 
   * @param index The index to remove.
   * @since 2.5.1
   */
  public void removeElementAt(int index) {
    mListModel.removeElementAt(index);
    mList.repaint();
  }
  
  /**
   * Gets the selected index of the list.
   * 
   * @return The selected index of the list.
   * @since 2.5.1
   */
  public int getSelectedIndex() {
    return mList.getSelectedIndex();
  }
  
  /**
   * Gets the selected value of this list.
   * 
   * @return The selected value of this list.
   * @since 2.5.1
   */
  public Object getSelectedValue() {
    if(mList.getSelectedValue() != null) {
      SelectableItem<E> item = mList.getSelectedValue();
      return item.getItem();
    }
    return null;
  }
  
  /**
   * Set the selection index
   * 
   * @param index index of to be selected item
   * @since 2.6
   */
  public void setSelectedIndex(int index) {
    mList.setSelectedIndex(index);
  }
  
  /**
   * Get the number of items in the order chooser
   * 
   * @return number of items in the list
   * @since 2.6
   */
  public int getItemCount() {
    return mList.getModel().getSize();
  }
  
  /**
   * Adds a button to the button panel.
   * <p>
   * @param button The button to add to the panel.
   * @since 3.3.4
   */
  public void addButton(JButton button) {
    mButtonPanel.add(button);
  }
  
  /**
   * Adds an element to the list of items at the given index (or at the end if index not available).
   * <p>
   * @param value The element to add.
   * @param index The index to insert the element.
   * @param selected If the value should be selected.
   */
  public void addElement(E value, int index, boolean selected) {
    SelectableItem<E> item = new SelectableItem<>(value,selected);
    
    if(index < mListModel.getSize()) {
      mListModel.add(index, item);
    }
    else {
      mListModel.addElement(item);
    }
    
    mList.repaint();
  }
}
