

package tvbrowser.ui.customizableitems;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.UiUtilities;

import java.awt.*;
import java.awt.event.*;

public class SortableItemList extends JPanel implements ActionListener {
  
  protected JButton mUpBt;
  protected JButton mDownBt;
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
    
    mUpBt = new JButton(new ImageIcon("imgs/Up24.gif"));
    String msg = mLocalizer.msg("tooltip.up", "Move selected rows up");
    mUpBt.setToolTipText(msg);
    mUpBt.setMargin(UiUtilities.ZERO_INSETS);
    mUpBt.addActionListener(this);

    mDownBt = new JButton(new ImageIcon("imgs/Down24.gif"));
    msg = mLocalizer.msg("tooltip.down", "Move selected rows down");
    mDownBt.setToolTipText(msg);
    mDownBt.setMargin(UiUtilities.ZERO_INSETS);
    mDownBt.addActionListener(this);
    
    mList=new JList(objects);
    mListModel=new DefaultListModel();
    mList.setModel(mListModel);
    mList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        updateBtns();
      }
    });
    
    mTitleLb=new JLabel(title);
    
    mBtnPanel=new JPanel();
    mBtnPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
    mBtnPanel.setLayout(new BoxLayout(mBtnPanel,BoxLayout.Y_AXIS));
    mBtnPanel.add(mUpBt);
    mBtnPanel.add(mDownBt);
    
    
    setLayout(new BorderLayout());
    add(mTitleLb,BorderLayout.NORTH);
    add(new JScrollPane(mList),BorderLayout.CENTER);
    add(mBtnPanel,BorderLayout.EAST);
    
    updateBtns();
        
  }
  
  public void addButton(Component comp) {
    mBtnPanel.add(comp);
  }
  
  private void updateBtns() {
    mUpBt.setEnabled(mList.getSelectedIndex()>0);
    mDownBt.setEnabled(mList.getSelectedIndex()<mListModel.size()-1);
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
  
  public Object[] getItems() {
    return mListModel.toArray();
  }
  
  public void actionPerformed(ActionEvent event) {
    Object o=event.getSource();
    if (o==mUpBt) {
      int inx=mList.getSelectedIndex();
      Object selectedItem=mListModel.getElementAt(inx);
      mListModel.removeElementAt(inx);
      mListModel.add(inx-1,selectedItem);
      mList.setSelectedIndex(inx-1);
    }
    else if (o==mDownBt) {
      int inx=mList.getSelectedIndex();
      Object selectedItem=mListModel.getElementAt(inx);
      mListModel.removeElementAt(inx);
      mListModel.add(inx+1,selectedItem);
      mList.setSelectedIndex(inx+1);
    }
    
    
  }
  
  
}