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


import tvbrowser.ui.filter.filters.*;
import util.ui.*;

public class FilterRuleDlg extends JDialog implements ActionListener, DocumentListener {
    
    private FilterRule mRule;
    private JComboBox mRuleList;
    private JPanel mCenterPanel, mRulePanel=null, mContentPane;
    private JButton mOkBtn, mCancelBtn;
    private JTextField mDescTF, mNameTF;
    
    public FilterRuleDlg(JFrame parent, FilterRule rule) {
        super(parent,true);
        mRule=rule;
        /*if (mRule==null) {       
        }*/
        mContentPane=(JPanel)getContentPane();
        mContentPane.setLayout(new BorderLayout(7,7));
        mContentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setTitle("Edit filter rule");
            
        JPanel northPanel=new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));    
            
        JPanel namePanel=new JPanel(new BorderLayout());
        JPanel descPanel=new JPanel(new BorderLayout());
        JPanel typePanel=new JPanel(new BorderLayout());
        
        namePanel.add(new JLabel("Rule name:"),BorderLayout.WEST);
        mNameTF=new JTextField(20);
        //mNameTF.addActionListener(this);
        
        mNameTF.getDocument().addDocumentListener(this);
        
        namePanel.add(mNameTF,BorderLayout.EAST);
        mDescTF=new JTextField(20);
        descPanel.add(new JLabel("Description:"),BorderLayout.WEST);
        descPanel.add(mDescTF,BorderLayout.EAST);
        typePanel.add(new JLabel("Type:"),BorderLayout.WEST);
        
        mRuleList=new JComboBox();
        mRuleList.addActionListener(this);
        mRuleList.addItem("must choose one");
        mRuleList.addItem(new KeywordFilterRule("no name","no desc"));
        mRuleList.addItem(new PluginFilterRule("no name","no desc"));
           
        typePanel.add(mRuleList,BorderLayout.EAST);
        
        northPanel.add(namePanel);
        northPanel.add(descPanel);
        northPanel.add(typePanel);
        
        
        JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        mOkBtn=new JButton("OK");
        mOkBtn.addActionListener(this);
        buttonPn.add(mOkBtn);
        getRootPane().setDefaultButton(mOkBtn);

        mCancelBtn=new JButton("Cancel");
        mCancelBtn.addActionListener(this);
        buttonPn.add(mCancelBtn);
        
       
        mCenterPanel=new JPanel(new BorderLayout());
        
        mContentPane.add(northPanel,BorderLayout.NORTH);
        mContentPane.add(buttonPn,BorderLayout.SOUTH);
        mContentPane.add(mCenterPanel,BorderLayout.CENTER);
           
        if (mRule!=null) {
            mNameTF.setText(mRule.getName());
            mDescTF.setText(mRule.getDescription());
            
            int itemsCnt=mRuleList.getItemCount();
            for (int i=0;i<itemsCnt;i++) {
                Object o=mRuleList.getItemAt(i);
                if (o instanceof FilterRule) {
                    FilterRule r=(FilterRule)o;
                    if (r.toString().equals(mRule.toString())) {
                        DefaultComboBoxModel model=(DefaultComboBoxModel)mRuleList.getModel();
                        model.removeElementAt(i);
                        model.insertElementAt(mRule,i);
                        break;
                    }
                }
            }
            
            mRuleList.setSelectedItem(mRule);       
        }
           
        updateOkBtn();
                    
        setSize(400,350);
        UiUtilities.centerAndShow(this);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o=e.getSource();
        if (o==mRuleList) {
            if (mRulePanel!=null) {
                mCenterPanel.remove(mRulePanel);
            }
            Object item=mRuleList.getSelectedItem();
            if (item instanceof FilterRule) {                
                FilterRule fItem=(FilterRule)item;
                mRulePanel=fItem.getPanel();
                mRulePanel.setBorder(BorderFactory.createEmptyBorder(0,30,0,0));
                mCenterPanel.add(mRulePanel,BorderLayout.NORTH);
            }
            mContentPane.updateUI();
            updateOkBtn();
            
        }
        
        else if (o==mOkBtn) {
            mRule=(FilterRule)mRuleList.getSelectedItem();
            mRule.setName(mNameTF.getText());
            mRule.setDescription(mDescTF.getText());
            mRule.ok();
            hide();
        }
        else if (o==mCancelBtn) {
            hide();
        }
        
    }
    
    public FilterRule getRule() {
        return mRule;
    }
    
    private void updateOkBtn() {
        if (mOkBtn!=null)
        mOkBtn.setEnabled(
            !("".equals(mNameTF.getText()))
            && mRuleList.getSelectedItem() instanceof FilterRule
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