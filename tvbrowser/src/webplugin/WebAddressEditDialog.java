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
package webplugin;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.Localizer;


/**
 * A Dialog for editing the WebAddress
 */
public class WebAddressEditDialog extends JDialog {

    /** Localizer */
    private static final Localizer mLocalizer = Localizer
    .getLocalizerFor(WebAddressEditDialog.class);
    
    /** The Address to Edit */
    private WebAddress mWebAddress;
    
    /** Name for the Address */
    private JTextField mName;
    /** Url for the Address */
    private JTextField mUrl;
    /** A Combobox */
    private JComboBox mEncoding;
    
    /** The return-value for this Dialog */
    private int returnValue;
    
    /**
     * Creates the Dialog
     * @param parent Parent 
     * @param adr Address to edit
     */
    public WebAddressEditDialog(JFrame parent, WebAddress adr) {
        super(parent, true);
        mWebAddress = adr;
        
        createGui();
    }

    /**
     * Creates the Dialog
     * @param parent Parent 
     * @param adr Address to edit
     */
    public WebAddressEditDialog(JDialog parent, WebAddress adr) {
        super(parent, true);
        mWebAddress = adr;
        
        createGui();
    }

    /**
     * creates the Gui 
     */
    private void createGui() {
        
        setTitle(mLocalizer.msg("EditWebAddress", "Edit WebAddress"));
        
        JPanel panel = (JPanel) getContentPane();
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints cLabel = new GridBagConstraints();
        cLabel.anchor = GridBagConstraints.NORTHWEST;
        cLabel.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints cEdit = new GridBagConstraints();
        
        cEdit.gridwidth = GridBagConstraints.REMAINDER;
        cEdit.fill = GridBagConstraints.HORIZONTAL;
        cEdit.insets = new Insets(5, 0, 5, 5);
        cEdit.weightx = 1.0;
        
        JLabel nameLabel = new JLabel(mLocalizer.msg("Name", "Name") + ":");
        panel.add(nameLabel, cLabel);
        
        mName = new JTextField();
        mName.setText(mWebAddress.getName());

        panel.add(mName, cEdit);

        panel.add(new JLabel(mLocalizer.msg("Url", "Url") + ":"), cLabel);
        
        mUrl = new JTextField();
        mUrl.setText(mWebAddress.getUrl());
        
        panel.add(mUrl, cEdit);
    
        panel.add(new JLabel(mLocalizer.msg("Encoding", "Encoding") + ":"), cLabel);

        mEncoding = new JComboBox(new String[] {"UTF-8", "ISO-8859-1"});
        mEncoding.setSelectedItem(mWebAddress.getEncoding());
        panel.add(mEncoding, cEdit);
        
        GridBagConstraints cSpacer = new GridBagConstraints();
        cSpacer.fill = GridBagConstraints.BOTH;
        cSpacer.weightx = 1.0;
        cSpacer.weighty = 1.0;
        cSpacer.gridwidth = GridBagConstraints.REMAINDER;
        cSpacer.insets = new Insets(0, 5, 0, 5);
        
        panel.add(new JPanel(), cSpacer);
        
        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));        
        
        JButton okButton = new JButton(mLocalizer.msg("OK", "OK") + ":");
        
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
            
        });
        
        JButton cancelButton = new JButton(mLocalizer.msg("Cancel", "Cancel") + ":");

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
            
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        GridBagConstraints cButtons = new GridBagConstraints();
        
        cButtons.gridwidth = GridBagConstraints.REMAINDER;
        cButtons.fill = GridBagConstraints.HORIZONTAL;
        cButtons.insets = new Insets(5, 5, 5, 0);
        cButtons.weightx = 1.0;
        cButtons.anchor = GridBagConstraints.SOUTHEAST;

        panel.add(buttonPanel, cButtons);

        getRootPane().setDefaultButton(okButton);
        
        pack();
        
        if (getSize().width < 400) {
            Dimension dim = getSize();
            dim.width = 400;
            setSize(dim);
        }
        
    }

    /**
     * OK was pressed
     */
    private void okPressed() {
        mWebAddress.setName(mName.getText());
        mWebAddress.setUrl(mUrl.getText());
        mWebAddress.setEncoding((String)mEncoding.getSelectedItem());
        returnValue = JOptionPane.OK_OPTION;
        hide();
    }

    /**
     * Cancel was pressed
     */
    private void cancelPressed() {
        returnValue = JOptionPane.CANCEL_OPTION;
        hide();
    }

    /**
     * Returns the Button that was pressed (JOptionPane.OK_OPTION / JOptionPane.CANCEL_OPTION)
     * @return Button that was pressed
     */
    public int getReturnValue() {
        return returnValue;
    }
}