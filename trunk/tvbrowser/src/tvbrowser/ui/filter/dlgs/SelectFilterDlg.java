/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.filter.dlgs;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import tvbrowser.ui.filter.*;


public class SelectFilterDlg extends JDialog implements ActionListener {
	
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SelectFilterDlg.class);
  
  
  private JList mFilterList;
  private JFrame mParent;
  private JButton mEditBtn, mRemoveBtn, mNewBtn, mCancelBtn, mOkBtn, mUpBtn, mDownBtn;
  private DefaultListModel mFilterListModel;  
    
	public SelectFilterDlg(JFrame parent) {
	
		super(parent,true);
		mParent=parent;
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(7,13));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setTitle(mLocalizer.msg("title","Edit Filters"));

    mFilterListModel=new DefaultListModel();
    mFilterList=new JList(mFilterListModel);
    Filter[] fList=FilterList.getFilterList();
    for (int i=0;i<fList.length;i++) {
        mFilterListModel.addElement(fList[i]);
    }
    mFilterList.setVisibleRowCount(5);
    
    mFilterList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            updateBtns();    
        }
    });
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    JPanel panel1=new JPanel(new GridLayout(0,1,0,7));
    mNewBtn=new JButton(mLocalizer.msg("newButton", "new"));
    mEditBtn=new JButton(mLocalizer.msg("editButton", "edit"));
    mRemoveBtn=new JButton(mLocalizer.msg("deleteButton", "delete"));
    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    panel1.add(mNewBtn);
    panel1.add(mEditBtn);
    panel1.add(mRemoveBtn);
    btnPanel.add(panel1,BorderLayout.NORTH); 
    
    
    JPanel panel2=new JPanel(new GridLayout(0,1,0,7));
    mUpBtn = new JButton(new ImageIcon("imgs/Up16.gif"));
    mDownBtn=new JButton(new ImageIcon("imgs/Down16.gif"));
    mUpBtn.addActionListener(this);
    mDownBtn.addActionListener(this);   
    panel2.add(mUpBtn);
    panel2.add(mDownBtn);
    
    btnPanel.add(panel2,BorderLayout.SOUTH);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn=new JButton(mLocalizer.msg("okButton", "OK"));
    buttonPn.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn=new JButton(mLocalizer.msg("cancelButton", "Cancel"));
    mCancelBtn.addActionListener(this);
    buttonPn.add(mCancelBtn);
    
    String txt="Choose a filter to edit or create a new one.";
    JTextArea ta=new JTextArea(mLocalizer.msg("hint", txt));
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setOpaque(false);
    ta.setEditable(false);
    ta.setFocusable(false);
    
    contentPane.add(new JScrollPane(mFilterList),BorderLayout.CENTER);
    contentPane.add(btnPanel,BorderLayout.EAST);
    contentPane.add(buttonPn,BorderLayout.SOUTH);
    contentPane.add(ta,BorderLayout.NORTH);
		
        
    updateBtns();
		setSize(280,280);
		
		
	}
    
  public void updateBtns() {
      
      Object item=mFilterList.getSelectedValue();
      mEditBtn.setEnabled(item!=null && !(item instanceof ShowAllFilter));
      mRemoveBtn.setEnabled(item!=null && !(item instanceof ShowAllFilter));  

      int inx=mFilterList.getSelectedIndex();
      mUpBtn.setEnabled(inx>0);
      mDownBtn.setEnabled(inx>=0 && inx<mFilterListModel.getSize()-1);
  }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==mNewBtn) {
            EditFilterDlg dlg=new EditFilterDlg(mParent, null);
            Filter filter=dlg.getFilter();
            if (filter!=null) {
                mFilterListModel.addElement(filter);              
            }
        }
        else if (e.getSource()==mEditBtn) {
            Filter filter=(Filter)mFilterList.getSelectedValue();
            EditFilterDlg dlg=new EditFilterDlg(mParent, filter);
        }
        else if (e.getSource()==mRemoveBtn) {
            Object o=mFilterList.getSelectedValue();
            int i=mFilterList.getSelectedIndex();
            mFilterListModel.remove(i);
            FilterList.remove((Filter)o);
            updateBtns();  
        }
        else if (e.getSource()==mUpBtn) {
            int fromInx=mFilterList.getSelectedIndex();
            Object o=mFilterList.getSelectedValue();
            mFilterListModel.removeElementAt(fromInx);
            mFilterListModel.insertElementAt(o,fromInx-1);  
            mFilterList.setSelectedIndex(fromInx-1);
        }
        else if (e.getSource()==mDownBtn) {
            int fromInx=mFilterList.getSelectedIndex();
            Object o=mFilterList.getSelectedValue();
            mFilterListModel.removeElementAt(fromInx);
            mFilterListModel.insertElementAt(o,fromInx+1);  
            mFilterList.setSelectedIndex(fromInx+1);
        }
        else if (e.getSource()==mOkBtn) {
            Object o[]=mFilterListModel.toArray();
            FilterList.clear();
            for (int i=0;i<o.length;i++) {
              FilterList.add((Filter)o[i]);  
            }
         
          /*
            Enumeration enum=mFilterListModel.elements();
            while (enum.hasMoreElements()) {
                Filter filter=(Filter)enum.nextElement();
                FilterList.add(filter);
            }
            */
            FilterList.store();   
            FilterComponentList.store();         
            hide();
        }
        else if (e.getSource()==mCancelBtn) {
            hide();
        }
      
  }
	
}