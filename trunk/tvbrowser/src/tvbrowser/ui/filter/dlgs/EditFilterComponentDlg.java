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

import tvbrowser.core.filters.*;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.KeywordFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramInfoFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramLengthFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramRunningFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;


import util.ui.*;

public class EditFilterComponentDlg extends JDialog implements ActionListener, DocumentListener {
    
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(EditFilterComponentDlg.class);
  
    private tvbrowser.core.filters.FilterComponent mSelectedFilterComponent;
    private JComboBox mRuleCb;
    private JPanel mCenterPanel, mRulePanel=null, mContentPane;
    private JButton mOkBtn, mCancelBtn;
    private JTextField mDescTF, mNameTF;
   
    
    public EditFilterComponentDlg(JFrame parent) {
      this(parent, null);
    }
    
    public EditFilterComponentDlg(JFrame parent, FilterComponent comp) {
        super(parent,true);
        
        mContentPane=(JPanel)getContentPane();
        mContentPane.setLayout(new BorderLayout(7,7));
        mContentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setTitle(mLocalizer.msg("title", "Edit filter component"));
            
        JPanel northPanel=new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));    
            
        JPanel namePanel=new JPanel(new BorderLayout());
        namePanel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
        JPanel descPanel=new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
        
        JPanel typePanel=new JPanel(new BorderLayout());
        
        namePanel.add(new JLabel(mLocalizer.msg("componentName", "Component name:")),BorderLayout.WEST);
        mNameTF=new JTextField(20);
        //mNameTF.addActionListener(this);
        
        mNameTF.getDocument().addDocumentListener(this);
        
        namePanel.add(mNameTF,BorderLayout.EAST);
        mDescTF=new JTextField(20);
        descPanel.add(new JLabel(mLocalizer.msg("componentDescription", "Description:")),BorderLayout.WEST);
        descPanel.add(mDescTF,BorderLayout.EAST);
        typePanel.add(new JLabel(mLocalizer.msg("componentType", "Type:")),BorderLayout.WEST);
        
      mRuleCb=new JComboBox();
      mRuleCb.addActionListener(this);
      mRuleCb.addItem(mLocalizer.msg("hint", "must choose one"));
      mRuleCb.addItem(new KeywordFilterComponent());
      mRuleCb.addItem(new PluginFilterComponent());
      mRuleCb.addItem(new ChannelFilterComponent());   
      mRuleCb.addItem(new TimeFilterComponent());
      mRuleCb.addItem(new ProgramInfoFilterComponent());
      mRuleCb.addItem(new ProgramLengthFilterComponent());
      mRuleCb.addItem(new ProgramRunningFilterComponent());
      
        typePanel.add(mRuleCb,BorderLayout.EAST);
        
        northPanel.add(namePanel);
        northPanel.add(descPanel);
        northPanel.add(typePanel);
        
        
        JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

          mOkBtn=new JButton(mLocalizer.msg("okButton", "OK"));
          mOkBtn.addActionListener(this);
          buttonPn.add(mOkBtn);
       
        getRootPane().setDefaultButton(mOkBtn);

        mCancelBtn=new JButton(mLocalizer.msg("cancelButton", "Cancel"));
        mCancelBtn.addActionListener(this);
        buttonPn.add(mCancelBtn);
        
       
        JPanel panel=new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("componentSettings", "Component settings:")));
       
        mCenterPanel=new JPanel(new BorderLayout());
        panel.add(mCenterPanel);
        
        mContentPane.add(northPanel,BorderLayout.NORTH);
        mContentPane.add(buttonPn,BorderLayout.SOUTH);
        mContentPane.add(panel,BorderLayout.CENTER);
         
        if (comp!=null) {
          this.setFilterComponent(comp); 
        }
         
        
           
        updateOkBtn();
                    
        setSize(500, 500);
        UiUtilities.centerAndShow(this);
        
    }
    
    
    private void setFilterComponent(FilterComponent comp) {
      for (int i=1;  // index 0 does not contain a FilterComponent object
           i<mRuleCb.getItemCount(); i++) {
        System.out.println(mRuleCb.getItemAt(i));
        FilterComponent c = (FilterComponent)mRuleCb.getItemAt(i);
        if (c.toString().equals(comp.toString())) {
          DefaultComboBoxModel model=(DefaultComboBoxModel)mRuleCb.getModel();
          model.removeElementAt(i);
          model.insertElementAt(comp,i);
          mRuleCb.setSelectedIndex(i);
          mNameTF.setText(comp.getName());
          mDescTF.setText(comp.getDescription());
          break;
        }
      }
      
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o=e.getSource();
        if (o==mRuleCb) {
            if (mRulePanel!=null) {
                mCenterPanel.remove(mRulePanel);
            }
            Object item=mRuleCb.getSelectedItem();
            if (item instanceof FilterComponent) {                
                FilterComponent fItem=(FilterComponent)item;
                mRulePanel=fItem.getPanel();
                mRulePanel.setBorder(BorderFactory.createEmptyBorder(0,17,0,0));
                mCenterPanel.add(mRulePanel,BorderLayout.NORTH);
            }
            mContentPane.updateUI();
            updateOkBtn();
            
        }
        else if (o==mOkBtn) {
          
          String compName=mNameTF.getText();
          
          
          
          if (FilterComponentList.getInstance().exists(compName)) {           
          
            JOptionPane.showMessageDialog(this,"Component '"+compName+"' already exists");
          }
          else {
            
            FilterComponent c =(FilterComponent)mRuleCb.getSelectedItem();
            c.ok();
            mSelectedFilterComponent = c;
            mSelectedFilterComponent.setName(compName);
            mSelectedFilterComponent.setDescription(mDescTF.getText());
            hide();
          }
        }  
        else if (o==mCancelBtn) {
          mSelectedFilterComponent = null;
            hide();
        }
        
    }
    
    public FilterComponent getFilterComponent() {
      return mSelectedFilterComponent;
    }
    
    private void updateOkBtn() {
        if (mOkBtn!=null)
        mOkBtn.setEnabled(
            !("".equals(mNameTF.getText()))
            && mRuleCb.getSelectedItem() instanceof FilterComponent
        );
    }
    
    public void changedUpdate(DocumentEvent e) {
        updateOkBtn();
    }
        
    public void insertUpdate(DocumentEvent e) {
        updateOkBtn();
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateOkBtn();
    }
    
    
    
}