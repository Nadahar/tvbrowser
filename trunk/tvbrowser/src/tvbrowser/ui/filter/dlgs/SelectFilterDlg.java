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

import devplugin.ProgramFilter;

import java.awt.*;
import java.awt.event.*;


import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.filters.PluginFilter;


public class SelectFilterDlg extends JDialog implements ActionListener {
	
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SelectFilterDlg.class);
  
  
  private JList mFilterListBox;
  private JFrame mParent;
  private JButton mEditBtn, mRemoveBtn, mNewBtn, mCancelBtn, mOkBtn, mUpBtn, mDownBtn;
  private DefaultListModel mFilterListModel;  
 // private FilterListModel mFilterListModel; 
  private FilterList mFilterList;
    
	public SelectFilterDlg(JFrame parent) {
	
		super(parent,true);
    
    mFilterList=FilterList.getInstance();
		mParent=parent;
		JPanel contentPane=(JPanel)getContentPane();
		contentPane.setLayout(new BorderLayout(7,13));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setTitle(mLocalizer.msg("title","Edit Filters"));

    mFilterListModel=new DefaultListModel();
    
    ProgramFilter[] filterArr=mFilterList.getFilterArr();
    for (int i=0;i<filterArr.length;i++) {
      mFilterListModel.addElement(filterArr[i]);
    }
   // mFilterListModel.
    //File dir=new File(Settings.getUserDirectoryName()+"/filters");
    //if (!dir.exists()) {
    //  dir.mkdirs();
    //}
  //  mFilterListModel=FilterListModel.getInstance();
    mFilterListBox=new JList(mFilterListModel);
  /*  AbstractFilter[] fList=FilterList.getFilterList();
    for (int i=0;i<fList.length;i++) {
        mFilterListModel.addElement(fList[i]);
    }*/
    mFilterListBox.setVisibleRowCount(5);
    
    mFilterListBox.addListSelectionListener(new ListSelectionListener() {
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
    mUpBtn = new JButton(new ImageIcon("imgs/up16.gif"));
    mDownBtn=new JButton(new ImageIcon("imgs/down16.gif"));
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
    
    contentPane.add(new JScrollPane(mFilterListBox),BorderLayout.CENTER);
    contentPane.add(btnPanel,BorderLayout.EAST);
    contentPane.add(buttonPn,BorderLayout.SOUTH);
    contentPane.add(ta,BorderLayout.NORTH);
		
        
    updateBtns();
		setSize(280,350);
		
		
	}
    
  public void updateBtns() {
      
      Object item=mFilterListBox.getSelectedValue();
      System.out.println("updateBtns()");
      System.out.println("selected obj: "+item);
      if (item instanceof ShowAllFilter) {
        System.out.println("show all filter");
      }
      System.out.println("result: "+(item instanceof ShowAllFilter || item instanceof PluginFilter));
      
      mEditBtn.setEnabled(item!=null && !(item instanceof ShowAllFilter || item instanceof PluginFilter));
      mRemoveBtn.setEnabled(item!=null && !(item instanceof ShowAllFilter || item instanceof PluginFilter));  

      int inx=mFilterListBox.getSelectedIndex();
      mUpBtn.setEnabled(inx>0);
      mDownBtn.setEnabled(inx>=0 && inx<mFilterListModel.getSize()-1);
  }
    
   public FilterList getFilterList() {
     return mFilterList;
   }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==mNewBtn) {
            EditFilterDlg dlg=new EditFilterDlg(mParent, mFilterList, null);
            UserFilter filter=dlg.getUserFilter();
            if (filter!=null) {
                mFilterListModel.addElement(filter);
            }
        }
        else if (e.getSource()==mEditBtn) {
            ProgramFilter filter=(ProgramFilter)mFilterListBox.getSelectedValue();
            if (filter instanceof UserFilter) {
              EditFilterDlg dlg=new EditFilterDlg(mParent, mFilterList, (UserFilter)filter);
            }  
        }
        else if (e.getSource()==mRemoveBtn) {          
          mFilterListModel.removeElement(mFilterListBox.getSelectedValue());
          mFilterList.remove((ProgramFilter)mFilterListBox.getSelectedValue()); 
          //mFilterListModel.remove((ProgramFilter)mFilterListBox.getSelectedValue().);
            updateBtns();  
        }
        else if (e.getSource()==mUpBtn) {
            int fromInx=mFilterListBox.getSelectedIndex();
            Object o=mFilterListBox.getSelectedValue();
            mFilterListModel.removeElementAt(fromInx);
            mFilterListModel.insertElementAt(o,fromInx-1);  
            mFilterListBox.setSelectedIndex(fromInx-1);
           
        }
        else if (e.getSource()==mDownBtn) {
            int fromInx=mFilterListBox.getSelectedIndex();
            Object o=mFilterListBox.getSelectedValue();
            mFilterListModel.removeElementAt(fromInx);
            mFilterListModel.insertElementAt(o,fromInx+1);  
            mFilterListBox.setSelectedIndex(fromInx+1);
        }
        else if (e.getSource()==mOkBtn) {
          /*
            Object o[]=mFilterListModel.toArray();
            FilterList.clear();
            for (int i=0;i<o.length;i++) {
              FilterList.add((AbstractFilter)o[i]);  
            }
         
          
            FilterList.store();   
            FilterComponentList.store();   
            */
            //mFilterListModel.store();
            
            
            
                Object[] o = mFilterListModel.toArray();
                ProgramFilter[] filters = new ProgramFilter[o.length];
                for (int i=0; i<o.length; i++) {
                  filters[i]=(ProgramFilter)o[i];
                }
                mFilterList.setProgramFilterArr(filters);            


              mFilterList.store();

            //FilterList.getInstance().store();  
            hide();
        }
        else if (e.getSource()==mCancelBtn) {
          mFilterList.create();
          hide();
        }
      
  }
	
}