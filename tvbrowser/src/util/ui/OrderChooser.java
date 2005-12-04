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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tvbrowser.core.icontheme.IconLoader;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class OrderChooser extends JPanel {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(OrderChooser.class);

  /**
   * Der Bereich, in dem ein Mausklick als Selektion/Deselektion und nicht als
   * Markierung aufgefasst wird.
   * (Wird vom Renderer gesetzt)
   */
  private int mSelectionWidth = 0;
  private JList mList;
  private DefaultListModel mListModel;
  private JButton mUpBt;
  private JButton mDownBt;



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
   */
  public OrderChooser(Object[] currOrder, Object[] allItems) {
    super(new BorderLayout());

    JPanel p1, p2;

    mListModel = new DefaultListModel();
    for (int i = 0; i < currOrder.length; i++) {
      if (contains(allItems, currOrder[i])) {
        SelectableItem item = new SelectableItem(currOrder[i], true);
        mListModel.addElement(item);
      }
    }
    for (int i = 0; i < allItems.length; i++) {
      if (! contains(currOrder, allItems[i])) {
        SelectableItem item = new SelectableItem(allItems[i], false);
        mListModel.addElement(item);
      }
    }
    mList = new JList(mListModel);
    mList.setCellRenderer(new SelectableItemRenderer());

    // MouseListener hinzuf�gen, der das Selektieren/Deselektieren �bernimmt
    mList.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mSelectionWidth) {
          int index = mList.locationToIndex(evt.getPoint());
          if (index != -1) {
            SelectableItem item = (SelectableItem) mListModel.elementAt(index);
            item.setSelected(! item.isSelected());
            mList.repaint();
          }
        }
      }
    });

    add(new JScrollPane(mList), BorderLayout.CENTER);

    p1 = new JPanel();
    add(p1, BorderLayout.EAST);

    p2 = new JPanel(new GridLayout(0, 1));
    p1.add(p2);

    mUpBt = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "go-up", 22));
    mUpBt.setToolTipText(mLocalizer.msg("tooltip.up", "Move selected rows up"));
    mUpBt.setMargin(UiUtilities.ZERO_INSETS);
    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveSelectedItems(-1);
      }
    });
    p2.add(mUpBt);

    mDownBt = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "go-down", 22));
    mDownBt.setToolTipText(mLocalizer.msg("tooltip.down", "Move selected rows down"));
    mDownBt.setMargin(UiUtilities.ZERO_INSETS);
    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveSelectedItems(1);
      }
    });
    p2.add(mDownBt);
  }


  public JButton getUpButton() {
    return mUpBt;
  }
  
  public JButton getDownButton() {
    return mDownBt;
  }

  private boolean contains(Object[] array, Object obj) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(obj)) {
        return true;
      }
    }
    
    return false;
  }
  

  public Object[] getOrder() {
    ArrayList objList = new ArrayList();
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

  public void invertSelection() {
    for (int i = 0; i < mListModel.size(); i++) {
      SelectableItem item = (SelectableItem) mListModel.elementAt(i);
      item.setSelected(!item.isSelected());
    }
  }

  public void selectAll() {
    for (int i = 0; i < mListModel.size(); i++) {
      SelectableItem item = (SelectableItem) mListModel.elementAt(i);
      item.setSelected(true);
    }
  }

  protected void moveSelectedItems(int nrRows) {
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

    // Zeilen wieder einf�gen
    for (int i = 0; i < selectedRows.length; i++) {
      mListModel.insertElementAt(selectedRows[i], insertPos + i);
    }

    // Zeilen markieren
    mList.getSelectionModel().setSelectionInterval(insertPos, insertPos + selection.length - 1);

    // Scrollen
    int scrollPos = insertPos;
    if (nrRows > 0) scrollPos += selection.length;
    mList.ensureIndexIsVisible(scrollPos);
  }


  class SelectableItem {
    private Object mItem;
    private boolean mSelected;

    public SelectableItem(Object item, boolean selected) {
      mItem = item;
      mSelected = selected;
    }
    
    void setSelected(boolean selected) {
      mSelected = selected;
    }

    boolean isSelected() {
      return mSelected;
    }
    
    Object getItem() {
      return mItem;
    }
  } // class SelectableItem


  class SelectableItemRenderer extends JCheckBox implements ListCellRenderer {
    public SelectableItemRenderer() {
      super();

      mSelectionWidth = getPreferredSize().height;
    }

    public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus)
    {
      SelectableItem selectableItem = (SelectableItem) value;

      setSelected(selectableItem.mSelected);
      setText(selectableItem.mItem.toString());

      if (isSelected) {
        setOpaque(true);
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setOpaque(false);
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());

      return this;
    }

  } // class SelectableItemRenderer

}
