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

import util.ui.UiUtilities;
import tvbrowser.ui.filter.filters.*;
import tvbrowser.ui.filter.*;


public class EditFilterDlg extends JDialog implements ActionListener, DocumentListener {
	
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(EditFilterDlg.class);
  
  private JButton mNewBtn, mEditBtn, mRemoveBtn, mOkBtn, mCancelBtn;  
  private JFrame mParent;
  private JList mRuleListBox;
  private JTextField mFilterNameTF, mFilterRuleTF;
  private DefaultListModel mRuleListModel;
  private Filter mFilter=null;
    
	public EditFilterDlg(JFrame parent, Filter filter) {
	
		super(parent,true);
		mParent=parent;
    mFilter=filter;
       
        
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout(7,7));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    
    
    if (filter==null) {
        setTitle(mLocalizer.msg("titleNew", "Create filter"));
    }else{
        setTitle(mLocalizer.msg("titleEdit", "Edit filter {0}", filter.getName()));
    }
    
    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
    
    mFilterNameTF=new JTextField(30);
    mFilterNameTF.getDocument().addDocumentListener(this);
    JPanel panel=new JPanel(new BorderLayout(7,7));
    panel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
    panel.add(new JLabel(mLocalizer.msg("filterName", "Filter name:")),BorderLayout.WEST);
    JPanel panel1=new JPanel(new BorderLayout());
    panel1.add(mFilterNameTF,BorderLayout.WEST);
    panel.add(panel1,BorderLayout.CENTER);
    northPanel.add(panel);
    
    mFilterRuleTF=new JTextField();
    mFilterRuleTF.getDocument().addDocumentListener(this);
    panel=new JPanel(new BorderLayout(7,7));
    panel1=new JPanel(new BorderLayout());
    panel.add(new JLabel(mLocalizer.msg("ruleString", "Filter rule:")),BorderLayout.WEST);
    JLabel exampleLb=new JLabel(mLocalizer.msg("ruleExample", "example: component1 or (component2 and not component3)"));
    Font f=exampleLb.getFont();
    exampleLb.setFont(new Font(f.getName(),Font.ITALIC|Font.PLAIN,f.getSize()));
    panel1.add(exampleLb,BorderLayout.WEST);
    panel.add(panel1,BorderLayout.CENTER);
    northPanel.add(panel);
    northPanel.add(mFilterRuleTF);
    
   // JLabel exampleLb=new JLabel("example: component1 or (component2 and not component3)");
    //exampleLb.setBorder(BorderFactory.createEmptyBorder(0,30,0,0));
    //northPanel.add(exampleLb);
    
    
    JPanel filterComponentsPanel=new JPanel(new BorderLayout(7,7));
    filterComponentsPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("componentsTitle", "Available filter components:")));
    JPanel btnPanel=new JPanel(new BorderLayout());
    panel1=new JPanel(new GridLayout(0,1,0,7));
    
    mNewBtn=new JButton(mLocalizer.msg("newButton", "new"));
    mEditBtn=new JButton(mLocalizer.msg("editButton", "edit"));
    mRemoveBtn=new JButton(mLocalizer.msg("removeButton", "remove"));
    
    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    
    panel1.add(mNewBtn);
    panel1.add(mEditBtn);    
    panel1.add(mRemoveBtn);
    
    btnPanel.add(panel1,BorderLayout.NORTH);
    
    mRuleListModel=new DefaultListModel();
    
    
    mRuleListBox=new JList(mRuleListModel);
    mRuleListBox.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateBtns();    
      }
    });
    
    mRuleListBox.setCellRenderer(new FilterRuleListCellRenderer());
    
    JPanel ruleListBoxPanel=new JPanel(new BorderLayout());
    ruleListBoxPanel.setBorder(BorderFactory.createEmptyBorder(0,13,7,0));
    ruleListBoxPanel.add(new JScrollPane(mRuleListBox),BorderLayout.CENTER);
    
    filterComponentsPanel.add(btnPanel,BorderLayout.EAST);
    filterComponentsPanel.add(ruleListBoxPanel,BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn=new JButton(mLocalizer.msg("okButton", "OK"));
    buttonPanel.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn=new JButton(mLocalizer.msg("cancelButton", "Cancel"));
    mCancelBtn.addActionListener(this);
    buttonPanel.add(mCancelBtn);
    
    
    contentPane.add(northPanel,BorderLayout.NORTH);
    contentPane.add(filterComponentsPanel,BorderLayout.CENTER);
    contentPane.add(buttonPanel,BorderLayout.SOUTH);
    
    /*
    JPanel northPanel=new JPanel(new BorderLayout());
    JPanel filterNamePanel=new JPanel();
    filterNamePanel.setLayout(new BoxLayout(filterNamePanel,BoxLayout.X_AXIS));
    
    filterNamePanel.add(new JLabel("Filter name:"));
    mFilterNameTF=new JTextField(30);
    mFilterNameTF.getDocument().addDocumentListener(this);
    filterNamePanel.add(mFilterNameTF);
    northPanel.add(filterNamePanel,BorderLayout.WEST);
    
    JPanel southPanel=new JPanel();
    southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.Y_AXIS));
    
    JPanel rulePanel=new JPanel(new BorderLayout());    
    rulePanel.add(new JLabel("Filter rule:"),BorderLayout.WEST);
    mFilterRuleTF=new JTextField();
    mFilterRuleTF.getDocument().addDocumentListener(this);
    rulePanel.add(mFilterRuleTF,BorderLayout.CENTER);
    rulePanel.add(new JLabel("example: rule1 or (rule2 and not rule3)"),BorderLayout.EAST);
    
    southPanel.add(rulePanel);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

    mOkBtn=new JButton("OK");
    buttonPn.add(mOkBtn);
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn=new JButton("Cancel");
    mCancelBtn.addActionListener(this);
    buttonPn.add(mCancelBtn);
        
    southPanel.add(buttonPn);    
        
        
    JPanel centerPanel =new JPanel(new BorderLayout());
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    JPanel panel1=new JPanel(new GridLayout(0,1));
    
    mNewBtn=new JButton("new");
    mEditBtn=new JButton("edit");
    mRemoveBtn=new JButton("remove");
    
    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);
    
    panel1.add(mNewBtn);
    panel1.add(mEditBtn);    
    panel1.add(mRemoveBtn);
    
    btnPanel.add(panel1,BorderLayout.NORTH);
    
    centerPanel.add(btnPanel,BorderLayout.EAST);
    
    mRuleListModel=new DefaultListModel();
    
    
    mRuleListBox=new JList(mRuleListModel);
    mRuleListBox.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            updateBtns();    
        }
    });
    
    mRuleListBox.setCellRenderer(new FilterRuleListCellRenderer());
    
    centerPanel.add(mRuleListBox,BorderLayout.CENTER);    
        
        
    contentPane.add(northPanel,BorderLayout.NORTH);
    contentPane.add(southPanel,BorderLayout.SOUTH);
    contentPane.add(centerPanel,BorderLayout.CENTER);
   
   */
   
   
    if (mFilter!=null) {
        mFilterNameTF.setText(mFilter.getName());
        mFilterRuleTF.setText(mFilter.getRule());
    }    
        java.util.Iterator it=FilterComponentList.iterator();
        while (it.hasNext()) {
          mRuleListModel.addElement(it.next());
        }
       
        
   
    updateBtns();
		setSize(600,300);
    UiUtilities.centerAndShow(this);
		
	}
   
  private void updateBtns() {
      if (mRuleListBox==null) {
          return;
      }
      Object item=mRuleListBox.getSelectedValue();
      mEditBtn.setEnabled(item!=null);
      mRemoveBtn.setEnabled(item!=null);
      
      boolean validRule=true;
      try {
        Filter.testTokenTree(mFilterRuleTF.getText());
        mFilterRuleTF.setBackground(Color.white);
        //          System.out.println("ok");
        //          mOkBtn.setEnabled(true);
           // mFilterRuleTF.updateUI();
      }catch(ParserException e) {
        mFilterRuleTF.setBackground(Color.red); 
        validRule=false;
      }
      
      
      
      mOkBtn.setEnabled(
        !("".equals(mFilterNameTF.getText()))
        &&!("".equals(mFilterRuleTF.getText()))
        && mRuleListModel.getSize()>0
        && validRule
      );
      
      
      
       
  }
   
  public void actionPerformed(ActionEvent e) {
      
      Object o=e.getSource();
      if (o==mNewBtn) {
        EditFilterComponentDlg dlg=new EditFilterComponentDlg(mParent,null);
        FilterComponent rule=dlg.getFilterComponent();
        if (rule!=null) {
            mRuleListModel.addElement(rule);
            FilterComponentList.add(rule);
            String text=mFilterRuleTF.getText();
            if (text.length()>0) {
                text+=" or ";
            }
            text+=rule.getName();
            mFilterRuleTF.setText(text);
            
          
            
        }
      }else if (o==mEditBtn) {
          int inx=mRuleListBox.getSelectedIndex();
          FilterComponent rule=(FilterComponent)mRuleListBox.getSelectedValue();
          EditFilterComponentDlg dlg=new EditFilterComponentDlg(mParent,rule);
          rule=dlg.getFilterComponent();
          if (rule!=null) {
              mRuleListModel.setElementAt(rule,inx);
          }
              
      }else if (o==mRuleListBox) {
          updateBtns();
      }else if (o==mRemoveBtn) {
        boolean allowRemove=true;
        Filter[] filter=FilterList.getFilterList();
        FilterComponent fc=(FilterComponent)mRuleListBox.getSelectedValue();
        for (int i=0;i<filter.length && allowRemove;i++) {
          if (filter[i]!=mFilter && filter[i].containsRuleComponent(fc.getName())) {
            allowRemove=false;
            JOptionPane.showMessageDialog(this,"This filter component is used by filter '"+filter[i].getName()+"\nRemove the filter first.");
          }
        }
        if (allowRemove) {
          FilterComponentList.remove(fc);
          mRuleListModel.remove(mRuleListBox.getSelectedIndex());
          updateBtns();
        }
      }else if (o==mOkBtn) {
          if (mFilter==null) {
              mFilter=new Filter(mFilterNameTF.getText());
          }
          else {
              mFilter.setName(mFilterNameTF.getText());
          }
          
          java.util.Enumeration enum=mRuleListModel.elements();
          while (enum.hasMoreElements()) {
              //mFilter.addRule((FilterComponent)enum.nextElement());
              FilterComponentList.add((FilterComponent)enum.nextElement());
          }
         
          try {
            mFilter.setRule(mFilterRuleTF.getText());
            hide();
          }catch(ParserException exc) {              
            JOptionPane.showMessageDialog(this,mLocalizer.msg("invalidRule", "Invalid rule: ") +exc.getMessage());
          }
          
          
      }else if (o==mCancelBtn) {
          hide();
      }
      
      
  }
  
  public Filter getFilter() {
      return mFilter;
  }
  
  public void changedUpdate(DocumentEvent e) {
          updateBtns();
          
      }
        
      public void insertUpdate(DocumentEvent e) {
          updateBtns();
      }
    
      public void removeUpdate(DocumentEvent e) {
          updateBtns();
      }
	
}


