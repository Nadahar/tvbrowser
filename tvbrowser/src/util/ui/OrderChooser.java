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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.layout.Sizes;

import tvbrowser.core.icontheme.IconLoader;
import devplugin.Channel;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class OrderChooser extends JPanel implements ListDropAction{

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
  private JButton mSelectAllBt;
  private JButton mDeSelectAllBt;  
  private boolean mIsEnabled = true;
  private JScrollPane mScrollPane;


  /**
   * Construcst an OrderChooser without selection Buttons.
   *  
   * @param currOrder Die aktuelle Reihenfolge
   * @param allItems Alle moeglichen Objekte (die Objekte der aktuellen Reihenfolge
   *        eingeschlossen)
   */
  public OrderChooser(Object[] currOrder, Object[] allItems) {
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
  public OrderChooser(Object[] currOrder, Object[] allItems, boolean showSelectionButtons) {
    super(new BorderLayout());

    JPanel p1, p2, p3, main;
    
    main = new JPanel(new BorderLayout(0,3));

    mListModel = new DefaultListModel();
    setEntries(currOrder,allItems);
    mList = new JList(mListModel);
    mList.setCellRenderer(new SelectableItemRenderer());

    // Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mList,mList,this);    
    new DragAndDropMouseListener(mList,mList,this,dnDHandler);
    
    // MouseListener hinzuf�gen, der das Selektieren/Deselektieren �bernimmt
    mList.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mSelectionWidth && mIsEnabled) {
          int index = mList.locationToIndex(evt.getPoint());
          if (index != -1) {
            SelectableItem item = (SelectableItem) mListModel.elementAt(index);
            item.setSelected(! item.isSelected());
            mList.repaint();
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
          mList.repaint();
        }
      }
    });

    mScrollPane = new JScrollPane(mList);
    main.add(mScrollPane, BorderLayout.CENTER);
    add(main, BorderLayout.CENTER);
    
    p1 = new JPanel(new BorderLayout());
    p1.setBorder(BorderFactory.createEmptyBorder(0, Sizes.dialogUnitXAsPixel(3, p1), 0, 0));
    p2 = new JPanel(new TabLayout(1));
    add(p1, BorderLayout.EAST);
    p1.add(p2, BorderLayout.NORTH);

    mUpBt = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "go-up", 22));
    mUpBt.setToolTipText(mLocalizer.msg("tooltip.up", "Move selected rows up"));
    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        UiUtilities.moveSelectedItems(mList,-1);
      }
    });
    p2.add(mUpBt);

    mDownBt = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "go-down", 22));
    mDownBt.setToolTipText(mLocalizer.msg("tooltip.down", "Move selected rows down"));
    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        UiUtilities.moveSelectedItems(mList,1);
      }
    });
    p2.add(mDownBt);
    
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
    
    if(showSelectionButtons)
      main.add(p3, BorderLayout.SOUTH);
  }

  private void setEntries(Object[] currOrder, Object[] allItems) {
    mListModel.removeAllElements();
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
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
        item.setSelected(!item.isSelected());
      }
      mList.repaint();
    }
  }

  public void selectAll() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
        item.setSelected(true);
      }
      mList.repaint();
    }
  }
  
  public void setOrder(Object[] currOrder, Object[] allItems) {
    setEntries(currOrder, allItems);
    
    mList.repaint();    
  }
  
  public void clearSelection() {
    if(mIsEnabled) {
      for (int i = 0; i < mListModel.size(); i++) {
        SelectableItem item = (SelectableItem) mListModel.elementAt(i);
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
  }*/


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


  class SelectableItemRenderer implements ListCellRenderer {    
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
        JLabel l = new JLabel(selectableItem.mItem.toString());
        
        if(!mIsEnabled)
          l.setEnabled(false);
        
        l.setOpaque(false);
        l.setIcon(UiUtilities.createChannelIcon(((Channel)selectableItem.getItem()).getIcon()));
        p.add(l, BorderLayout.CENTER);
        
        if(isSelected && mIsEnabled)
          l.setForeground(list.getSelectionForeground());
        else
          l.setForeground(list.getForeground());
      }
      else
        cb.setText(selectableItem.mItem.toString());
      
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

  } // class SelectableItemRenderer


  public void drop(JList source, JList target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
  }
  
  public void setEnabled(boolean value) {
    mIsEnabled = value;
    mList.setEnabled(value);
    mUpBt.setEnabled(value);
    mDownBt.setEnabled(value);
    mSelectAllBt.setEnabled(value);
    mDeSelectAllBt.setEnabled(value);
    mScrollPane.getVerticalScrollBar().setEnabled(value);
    mScrollPane.setWheelScrollingEnabled(value);
  }
}
