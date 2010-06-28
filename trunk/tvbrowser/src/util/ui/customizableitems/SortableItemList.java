/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.ui.settings.channel.ChannelJList;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.TVBrowserIcons;
import util.ui.TabLayout;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.Sizes;

/**
 * A sortable List
 */
public class SortableItemList extends JPanel implements ActionListener, ListDropAction {
  
  protected JButton mUpBt;
  protected JButton mDownBt;
  protected JButton mTopBtn;
  protected JButton mBottomBtn;
  protected JList mList;
  protected JLabel mTitleLb;
  protected DefaultListModel mListModel;
  protected JPanel mBtnPanel;
  
  public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(CustomizableItemsPanel.class);

  
  public SortableItemList() {
    this("");
  }
  
  public SortableItemList(String title) {
    this(title, new Object[]{});
  }

  public SortableItemList(String title, Object[] objects) {
    this(title, objects, new JList());
  }

  /**
   * Create a new SortableItemList
   * 
   * @param list List to use
   * @since 2.2
   */
  public SortableItemList(ChannelJList list) {
    this("", new Object[]{}, list);
  }
  
  /**
   * Create a new SortableItemList
   * @param title Title of the List
   * @param objects List-Items
   * @param list List to use
   * 
   * @since 2.2
   */
  public SortableItemList(String title, Object[] objects, JList list) {
    mUpBt = new JButton(TVBrowserIcons.up(TVBrowserIcons.SIZE_LARGE));
    String msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    mUpBt.setToolTipText(msg);
    mUpBt.addActionListener(this);

    mDownBt = new JButton(TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE));
    msg = mLocalizer.msg("tooltip.down", "Move selected rows down");
    mDownBt.setToolTipText(msg);
    mDownBt.addActionListener(this);
    
    mTopBtn = new JButton(TVBrowserIcons.top(TVBrowserIcons.SIZE_LARGE));
    msg = mLocalizer.msg("tooltip.top", "Move selected rows to top");
    mTopBtn.setToolTipText(msg);
    mTopBtn.addActionListener(this);

    mBottomBtn = new JButton(TVBrowserIcons.bottom(TVBrowserIcons.SIZE_LARGE));
    msg = mLocalizer.msg("tooltip.bottom", "Move selected rows to bottom");
    mBottomBtn.setToolTipText(msg);
    mBottomBtn.addActionListener(this);

    mList = list;
    mListModel = new DefaultListModel();
    mList.setModel(mListModel);
    
    for (Object object : objects) {
      mListModel.addElement(object);
    }
    
    mList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        updateBtns();
      }
    });
 
    //Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mList, mList, this);
    new DragAndDropMouseListener(mList,mList,this,dnDHandler);
    
    mTitleLb=new JLabel(title);
    
    mBtnPanel=new JPanel();
    mBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, Sizes.dialogUnitXAsPixel(3, mBtnPanel),0,0));
    mBtnPanel.setLayout(new TabLayout(1));
    mBtnPanel.add(mTopBtn);
    mBtnPanel.add(mUpBt);
    mBtnPanel.add(mDownBt);
    mBtnPanel.add(mBottomBtn);
    
    setLayout(new BorderLayout());
    add(mTitleLb,BorderLayout.NORTH);
    add(new JScrollPane(mList),BorderLayout.CENTER);
    
    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(mBtnPanel, BorderLayout.NORTH);
    add(p1,BorderLayout.EAST);
    
    updateBtns();
        
  }
  
  public void addButton(Component comp) {
    mBtnPanel.add(comp);
  }
  
  private void updateBtns() {
    mUpBt.setEnabled(mList.getSelectedIndex()>0);
    mDownBt.setEnabled(mList.getSelectedIndex()<mListModel.size()-1);
    mTopBtn.setEnabled(mUpBt.isEnabled());
    mBottomBtn.setEnabled(mDownBt.isEnabled());
  }
  
  public void setTitle(String title) {
    mTitleLb.setText(title);
  }
  
  public void setCellRenderer(ListCellRenderer renderer) {
    mList.setCellRenderer(renderer);
  }
  
  public JList getList() {
    return mList;
  }
  
  public void addElement(Object o) {
    mListModel.addElement(o);
  }
  
  public void addElement(int inx, Object o) {
    mListModel.add(inx,o);
  }
  
  public void removeElementAt(int inx) {
    mListModel.removeElementAt(inx);
  }
  
  public void removeElement(Object o) {
    mListModel.removeElement(o);
  }
  
  public void removeAllElements() {
    mListModel.removeAllElements();
  }
  
  public boolean contains(Object o) {
    return mListModel.contains(o);
  }
   
  public Object[] getItems() {
    return mListModel.toArray();
  }
  
  public void actionPerformed(ActionEvent event) {
    Object o=event.getSource();
    if (o==mUpBt) {
      UiUtilities.moveSelectedItems(mList, -1);
      if (mUpBt.isEnabled()) {
    	  mUpBt.requestFocusInWindow();
      }
    }
    else if (o==mDownBt) {
      UiUtilities.moveSelectedItems(mList, 1);
      if (mDownBt.isEnabled()) {
    	  mDownBt.requestFocusInWindow();
      }
    }
    else if (o==mTopBtn) {
      UiUtilities.moveSelectedItems(mList, -10000);
      if (mTopBtn.isEnabled()) {
    	  mTopBtn.requestFocusInWindow();
      }
    }
    else if (o==mBottomBtn) {
      UiUtilities.moveSelectedItems(mList, +10000);
      if (mBottomBtn.isEnabled()) {
    	  mBottomBtn.requestFocusInWindow();
      }
    }
  }
  
  /**
   * Returns the Up-Button
   * @return Up-Button
   */
  public JButton getUpButton() {
    return mUpBt;
  }
  
  /**
   * Returns the Down-Button
   * @return Down-Button
   */
  public JButton getDownButton() {
    return mDownBt;
  }

  /**
   * Returns the Top-Button
   * @return Top-Button
   */
  public JButton getTopButton() {
    return mTopBtn;
  }

  /**
   * Returns the Bottom-Button
   * @return Bottom-Button
   */
  public JButton getBottomButton() {
    return mBottomBtn;
  }

  public void drop(JList source, JList target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
  }
  
}