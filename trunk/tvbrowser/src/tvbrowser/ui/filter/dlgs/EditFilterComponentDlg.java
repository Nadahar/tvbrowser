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

public class EditFilterComponentDlg extends JDialog implements ActionListener, DocumentListener {
    
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(EditFilterComponentDlg.class);
  
    private FilterComponent mComp;
    private JComboBox mRuleList;
    private JPanel mCenterPanel, mRulePanel=null, mContentPane;
    private JButton mOkBtn, mCancelBtn;
    private JTextField mDescTF, mNameTF;
    
    public EditFilterComponentDlg(JFrame parent, FilterComponent comp) {
        super(parent,true);
        mComp=comp;
        
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
        
        mRuleList=new JComboBox();
        mRuleList.addActionListener(this);
        mRuleList.addItem(mLocalizer.msg("hint", "must choose one"));
        mRuleList.addItem(new KeywordFilterComponent(mLocalizer.msg("keywordName", "no name"),mLocalizer.msg("keywordDescription", "no desc")));
        mRuleList.addItem(new PluginFilterComponent(mLocalizer.msg("pluginName", "no name"), mLocalizer.msg("pluginDescription", "no desc")));
           
        typePanel.add(mRuleList,BorderLayout.EAST);
        
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
           
        if (mComp!=null) {
            mNameTF.setText(mComp.getName());
            mDescTF.setText(mComp.getDescription());
            
            int itemsCnt=mRuleList.getItemCount();
            for (int i=0;i<itemsCnt;i++) {
                Object o=mRuleList.getItemAt(i);
                if (o instanceof FilterComponent) {
                    FilterComponent r=(FilterComponent)o;
                    if (r.toString().equals(mComp.toString())) {
                        DefaultComboBoxModel model=(DefaultComboBoxModel)mRuleList.getModel();
                        model.removeElementAt(i);
                        model.insertElementAt(mComp,i);
                        break;
                    }
                }
            }
            
            mRuleList.setSelectedItem(mComp);       
        }
           
        updateOkBtn();
                    
        setSize(500,440);
        UiUtilities.centerAndShow(this);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o=e.getSource();
        if (o==mRuleList) {
            if (mRulePanel!=null) {
                mCenterPanel.remove(mRulePanel);
            }
            Object item=mRuleList.getSelectedItem();
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
          mComp=(FilterComponent)mRuleList.getSelectedItem();
          mComp.setName(mNameTF.getText());
          mComp.setDescription(mDescTF.getText());
          mComp.ok();
            hide();
        }
        else if (o==mCancelBtn) {
            hide();
        }
        
    }
    
    public FilterComponent getFilterComponent() {
        return mComp;
    }
    
    private void updateOkBtn() {
        if (mOkBtn!=null)
        mOkBtn.setEnabled(
            !("".equals(mNameTF.getText()))
            && mRuleList.getSelectedItem() instanceof FilterComponent
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