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

package tvbrowser.ui.customizableitems;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

import util.ui.UiUtilities;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class CustomizableItemsPanel extends JPanel {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(CustomizableItemsPanel.class);

  private final DefaultListModel mLeftListModel, mRightListModel;
  private final JList mLeftList, mRightList;
  private JLabel mRightLabel, mLeftLabel;

  private JButton mRightBt, mLeftBt, mUpBt, mDownBt;
  private ArrayList mListeners;
  

  public CustomizableItemsPanel(String leftText, String rightText) {
    super(new GridLayout(1,2));

    mListeners = new ArrayList();
    String msg;

    JPanel leftPanel=new JPanel(new BorderLayout());
    JPanel rightPanel=new JPanel(new BorderLayout());

    mLeftListModel = new DefaultListModel();
    mLeftList = new JList(mLeftListModel);
    mLeftList.setVisibleRowCount(10);
    mLeftList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mLeftList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateEnabled();
      }
    });

    mRightListModel = new DefaultListModel();
    mRightList = new JList(mRightListModel);
    mRightList.setVisibleRowCount(10);
    mRightList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mRightList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateEnabled();
      }
    });

    mLeftList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        fireLeftListSelectionChanged(e);
      }
    }
    );

    mRightList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        fireRightListSelectionChanged(e);
      }
    }
    );

    mLeftLabel=new JLabel(leftText);
    mRightLabel=new JLabel(rightText);

    mLeftLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
    mRightLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

    leftPanel.add(mLeftLabel,BorderLayout.NORTH);
    rightPanel.add(mRightLabel,BorderLayout.NORTH);

    leftPanel.add(new JScrollPane(mLeftList),BorderLayout.CENTER);
    rightPanel.add(new JScrollPane(mRightList),BorderLayout.CENTER);

    JPanel leftButtons=new JPanel(new GridLayout(2,1));
    JPanel rightButtons=new JPanel(new GridLayout(2,1));

    JPanel panel2=new JPanel(new BorderLayout());
    JPanel panel3=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    JPanel panel5=new JPanel(new BorderLayout());

    mRightBt = new JButton(new ImageIcon("imgs/Forward24.gif"));
    msg = mLocalizer.msg("tooltip.right", "Move selected rows in right list");
    mRightBt.setToolTipText(msg);
    mRightBt.setMargin(UiUtilities.ZERO_INSETS);

    mLeftBt = new JButton(new ImageIcon("imgs/Back24.gif"));
    msg = mLocalizer.msg("tooltip.left", "Move selected rows in left list");
    mLeftBt.setToolTipText(msg);
    mLeftBt.setMargin(UiUtilities.ZERO_INSETS);

    mUpBt = new JButton(new ImageIcon("imgs/Up24.gif"));
    msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    mUpBt.setToolTipText(msg);
    mUpBt.setMargin(UiUtilities.ZERO_INSETS);

    mDownBt = new JButton(new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.down", "Move selected rows down");
    mDownBt.setToolTipText(msg);
    mDownBt.setMargin(UiUtilities.ZERO_INSETS);

    panel2.add(mRightBt,BorderLayout.SOUTH);
    panel3.add(mLeftBt,BorderLayout.NORTH);
    panel4.add(mUpBt,BorderLayout.SOUTH);
    panel5.add(mDownBt,BorderLayout.NORTH);

    leftButtons.add(panel2);
    leftButtons.add(panel3);

    rightButtons.add(panel4);
    rightButtons.add(panel5);

    leftButtons.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    rightButtons.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));

    leftPanel.add(leftButtons,BorderLayout.EAST);
    rightPanel.add(rightButtons,BorderLayout.EAST);

    add(leftPanel);
    add(rightPanel);


    mRightBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Object[] items = moveSelectedItems(mLeftList, mRightList);
        if (items != null) {
          fireItemTransferredToRightList(items);
        }
      }
    });

    mLeftBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Object[] items = moveSelectedItems(mRightList, mLeftList);
        if (items != null) {
          fireItemTransferredToLeftList(items);
        }
      }
    });

    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        moveSelectedItems(mRightList, -1);
      }
    });

    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        moveSelectedItems(mRightList, 1);
      }
    });
    
    updateEnabled();
  }


  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mLeftList.setEnabled(enabled);
    mRightList.setEnabled(enabled);
    mRightBt.setEnabled(enabled);
    mLeftBt.setEnabled(enabled);
    mUpBt.setEnabled(enabled);
    mDownBt.setEnabled(enabled);
    mRightLabel.setEnabled(enabled);
    mLeftLabel.setEnabled(enabled);
  }

  public void clearLeft() {
    mLeftListModel.clear();
  }
  
  public void clearRight() {
    mRightListModel.clear();
  }
  
  public JList getLeftList() {
    return mLeftList;
  }
  
  public JList getRightList() {
    return mRightList;
  }

  public void addElementLeft(Object item) {
    mLeftListModel.addElement(item);
  }

  public void addElementRight(Object item) {
    mRightListModel.addElement(item);
  }

  public void insertElementLeft(int index, Object item) {
    mLeftListModel.add(index, item);
  }

  public void insertElementRight(int index, Object item) {
    mRightListModel.add(index, item);
  }

  public void setElementsLeft(Object[] items) {
    mLeftListModel.clear();
    for (int i=0; i<items.length; i++) {
      mLeftListModel.addElement(items[i]);
    }
  }

  public void setElementsRight(Object[] items) {
    mRightListModel.clear();
    for (int i=0; i<items.length; i++) {
      mRightListModel.addElement(items[i]);
    }
  }

  public Object[] getElementsLeft() {
    return mLeftListModel.toArray();
  }

  public Object[] getElementsRight() {
    return mRightListModel.toArray();
  }

  public Object getElementAtRight(int inx) {
    return mRightListModel.getElementAt(inx);
  }

  public Object getElementAtLeft(int inx) {
    return mLeftListModel.getElementAt(inx);
  }

  public void removeLeft(Object o) {
    mLeftListModel.removeElement(o);
  }

  public void removeRight(Object o) {
    mRightListModel.removeElement(o);
  }

  public Object getLeftSelection() {
    return mLeftList.getSelectedValue();
  }

  public Object getRightSelection() {
    return mRightList.getSelectedValue();
  }
  
  public Object[] getRightSelections() {
    return getSelectedValues(mRightList);
  }
  
  public Object[] getLeftSelections() {
      return getSelectedValues(mLeftList);
  }

  private Object[] getSelectedValues(JList list) {
    int[] inx=list.getSelectedIndices();
    Object[] res=new Object[inx.length];
    for (int i=0;i<inx.length;i++) {
      res[i]=list.getModel().getElementAt(inx[i]);
    }
    return res;
  }


  public void setCellRenderer(ListCellRenderer renderer) {
    mLeftList.setCellRenderer(renderer);
    mRightList.setCellRenderer(renderer);
  }


  public void addCustomizableItemsListener(CustomizableItemsListener listener) {
    mListeners.add(listener);
  }

  private void fireLeftListSelectionChanged(final ListSelectionEvent e) {
    Iterator it = mListeners.iterator();
    while (it.hasNext()) {
      CustomizableItemsListener listener = (CustomizableItemsListener)it.next();
      listener.leftListSelectionChanged(e);
    }
  }

   private void fireRightListSelectionChanged(final ListSelectionEvent e) {
    Iterator it = mListeners.iterator();
    while (it.hasNext()) {
      CustomizableItemsListener listener = (CustomizableItemsListener)it.next();
      listener.rightListSelectionChanged(e);
    }
  }

  private void fireItemTransferredToLeftList(Object[] items) {
    Iterator it = mListeners.iterator();
    while (it.hasNext()) {
      CustomizableItemsListener listener = (CustomizableItemsListener)it.next();
      listener.itemsTransferredToLeftList(items);
    }
  }

  private void fireItemTransferredToRightList(Object[] items) {
    Iterator it = mListeners.iterator();
    while (it.hasNext()) {
      CustomizableItemsListener listener = (CustomizableItemsListener)it.next();
      listener.itemsTransferredToRightList(items);
    }
  }

        /*
  public void addListSelectionListenerLeft(final CustomizableItemsListener listener) {
    mLeftList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listener.leftListSelectionChanged(e);
      }
    }
    );
  }

  public void addListSelectionListenerRight(final CustomizableItemsListener listener) {
    mRightList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listener.rightListSelectionChanged(e);
      }
    }
    );
  }
       */


  public static CustomizableItemsPanel createCustomizableItemsPanel(String leftText, String rightText) {
    return new CustomizableItemsPanel(leftText, rightText);
  }
  
  

  private Object[] moveSelectedItems(JList fromList, JList toList) {
    DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
    DefaultListModel toModel = (DefaultListModel) toList.getModel();
    
    // get the selection
    int[] selection = fromList.getSelectedIndices();



    if (selection.length == 0) {
      return null;
    }

    Object[] objects = new Object[selection.length];
    for (int i=0; i<selection.length; i++) {
      objects[i] = fromModel.getElementAt(selection[i]);
    }

    // get the target insertion position
    int targetPos = toList.getMaxSelectionIndex();
    if (targetPos == -1) {
      targetPos = toModel.getSize();
    } else {
      targetPos++;
    }
    
    // move the elements
    for (int i = selection.length - 1; i >= 0; i--) {
      Object value = fromModel.remove(selection[i]);
      toModel.add(targetPos, value);
    }
    
    // change selection of the fromList
    if (fromModel.getSize() > 0) {
      int newSelection = selection[0];
      if (newSelection >= fromModel.getSize()) {
        newSelection = fromModel.getSize() - 1;
      }
      fromList.setSelectedIndex(newSelection);
    }

    // change selection of the toList
    toList.setSelectionInterval(targetPos, targetPos + selection.length - 1);
    
    // ensure the selection is visible
    toList.ensureIndexIsVisible(toList.getMaxSelectionIndex());
    toList.ensureIndexIsVisible(toList.getMinSelectionIndex());


    return objects;
  }




  
  private void moveSelectedItems(JList list, int nrRows) {
    DefaultListModel model = (DefaultListModel) list.getModel();
    
    // get the selection
    int[] selection = list.getSelectedIndices();
    if (selection.length == 0) {
      return;
    }
    
    // Remove the selected items
    Object[] items = new Object[selection.length];
    for (int i = selection.length - 1; i >= 0; i--) {
      items[i] = model.remove(selection[i]);
    }
    
    // insert the elements at the target position
    int targetPos = selection[0] + nrRows;
    targetPos = Math.max(targetPos, 0);
    targetPos = Math.min(targetPos, model.getSize());
    for (int i = 0; i < items.length; i++) {
      model.add(targetPos + i, items[i]);
    }
    
    // change selection of the toList
    list.setSelectionInterval(targetPos, targetPos + selection.length - 1);
    
    // ensure the selection is visible
    list.ensureIndexIsVisible(list.getMaxSelectionIndex());
    list.ensureIndexIsVisible(list.getMinSelectionIndex());
  }
  
  
  
  private void updateEnabled() {
    mRightBt.setEnabled(mLeftList.getSelectedIndex() != -1);
    mLeftBt.setEnabled(mRightList.getSelectedIndex() != -1);
    
    mUpBt.setEnabled(mRightList.getMinSelectionIndex() > 0);
    int maxIdx = mRightList.getMaxSelectionIndex();
    mDownBt.setEnabled((maxIdx != -1) && (maxIdx < mRightListModel.getSize() - 1));
  }
  
}