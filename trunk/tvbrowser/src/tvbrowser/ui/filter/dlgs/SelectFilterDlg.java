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
import java.util.*;

import tvbrowser.ui.filter.*;


public class SelectFilterDlg extends JDialog implements ActionListener {
	
  private JList mFilterList;
  private JFrame mParent;
  private JButton mEditBtn, mRemoveBtn, mNewBtn, mCancelBtn, mOkBtn;
  private DefaultListModel mFilterListModel;  
    
	public SelectFilterDlg(JFrame parent) {
	
		super(parent,true);
		mParent=parent;
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(7,7));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setTitle("Edit Filters");

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
    mNewBtn=new JButton("new");
    mEditBtn=new JButton("edit");
    mRemoveBtn=new JButton("delete");
    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    panel1.add(mNewBtn);
    panel1.add(mEditBtn);
    panel1.add(mRemoveBtn);
    btnPanel.add(panel1,BorderLayout.NORTH);    
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn=new JButton("OK");
    buttonPn.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn=new JButton("Cancel");
    mCancelBtn.addActionListener(this);
    buttonPn.add(mCancelBtn);
    
    
    contentPane.add(new JScrollPane(mFilterList),BorderLayout.CENTER);
    contentPane.add(btnPanel,BorderLayout.EAST);
    contentPane.add(buttonPn,BorderLayout.SOUTH);
		
        
    updateBtns();
		setSize(200,250);
		
		
	}
    
  public void updateBtns() {
      
      Object item=mFilterList.getSelectedValue();
      mEditBtn.setEnabled(item!=null);
      mRemoveBtn.setEnabled(item!=null);  
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
        else if (e.getSource()==mOkBtn) {
            Enumeration enum=mFilterListModel.elements();
            while (enum.hasMoreElements()) {
                Filter filter=(Filter)enum.nextElement();
                FilterList.add(filter);
            }
            FilterList.store();            
            hide();
        }
        else if (e.getSource()==mCancelBtn) {
            hide();
        }
      
  }
	
}