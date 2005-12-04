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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.SettingsTab;


/**
 * The Settings-Tab
 */
public class WebSettingsTab implements SettingsTab {
    /** Localizer */
    private static final Localizer mLocalizer = Localizer
    .getLocalizerFor(WebSettingsTab.class);
    
    /** The original List */
    private Vector mOriginal;
    /** Work-List */
    private Vector mCloned;

    /** JList */
    private JList mAddressList;
    
    /** A few Buttons */
    private JButton mStartStop;
    private JButton mNew;
    private JButton mEdit;
    private JButton mDelete;
    private JButton mUp;
    private JButton mDown;

    /** Start-Icon */
    private Icon mStartIcon = ImageUtilities.createImageIconFromJar("webplugin/Refresh16.gif", WebSettingsTab.class);
    /** Stop-Icon */
    private Icon mStopIcon = ImageUtilities.createImageIconFromJar("webplugin/Stop16.gif", WebSettingsTab.class);
    
    /** Parent */
    private JFrame mParent;
    
    /**
     * Create the Tab
     * 
     * @param frame Parent-Frame
     * @param addresses List of Addresses
     */
    public WebSettingsTab(JFrame frame, Vector addresses) {
        mParent = frame;
        mOriginal = addresses;

        mCloned = new Vector();
        
        for (int i = 0; i < mOriginal.size();i++) {
            mCloned.add( ((WebAddress)mOriginal.get(i)).clone());
        }
        
    }

    /* (non-Javadoc)
     * @see devplugin.SettingsTab#createSettingsPanel()
     */
    public JPanel createSettingsPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("WebPages", "Web Pages")));
        
        mAddressList = new JList(mCloned);
        mAddressList.setSelectedIndex(0);
        mAddressList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mAddressList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                listSelectionChanged();
            }
        });
        mAddressList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                    editPressed();
                }
                super.mouseClicked(e);
            }
        });        
        
        mAddressList.setCellRenderer(new WebAddressRenderer());
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        
        panel.add(new JScrollPane(mAddressList), c);
        
        c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;

        createButtons();
        
        panel.add(mStartStop, c);
        c.insets = new Insets(5, 0, 5, 0);
        panel.add(mNew, c);
        panel.add(mEdit, c);
        panel.add(mDelete, c);
        
        GridBagConstraints filler = new GridBagConstraints();
        
        filler.weightx = 1.0;
        filler.fill = GridBagConstraints.HORIZONTAL;
        
        panel.add(new JPanel(), filler);
        
        panel.add(mUp, c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 0, 5, 5);
        
        panel.add(mDown, c);

        listSelectionChanged();
        
        return panel;
    }

    /**
     * Create the Buttons
     */
    private void createButtons() {
        mStartStop = new JButton(mStartIcon);
        mStartStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                WebAddress adr = (WebAddress)mAddressList.getSelectedValue();
                adr.setActive(!adr.isActive());
                mAddressList.updateUI();
                listSelectionChanged();
            }
            
        });
        
        mNew = new JButton(ImageUtilities.createImageIconFromJar("webplugin/New16.gif", WebSettingsTab.class));
        mNew.setToolTipText(mLocalizer.msg("New", "Add a new Site"));
        mNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPressed();
            }
        });

        mEdit = new JButton(ImageUtilities.createImageIconFromJar("webplugin/Edit16.gif", WebSettingsTab.class));
        mEdit.setToolTipText(mLocalizer.msg("Edit", "Edit Site"));
        
        mEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editPressed();
            }
        });
        
        mDelete = new JButton(ImageUtilities.createImageIconFromJar("webplugin/Delete16.gif", WebSettingsTab.class));
        mDelete.setToolTipText(mLocalizer.msg("DeleteSite", "Delete Site"));
        
        mDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }
            
        });
        
        mUp = new JButton(WebPlugin.getPluginManager().getIconFromTheme(WebPlugin.getInstance(), "actions", "go-up", 16));
        mUp.setToolTipText(mLocalizer.msg("Up", "Move selected Site up"));

        mUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveSelectedItem(-1);
            }
            
        });
        
        
        mDown = new JButton(WebPlugin.getPluginManager().getIconFromTheme(WebPlugin.getInstance(), "actions", "go-down", 16));
        mDown.setToolTipText(mLocalizer.msg("Down", "Move selected Site down"));

        mDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveSelectedItem(1);
            }
            
        });
    
    }

    /**
     * The Selection was changed
     */
    private void listSelectionChanged() {
        
        WebAddress adr = (WebAddress)mAddressList.getSelectedValue();

        if (!adr.isActive()) {
            mStartStop.setIcon(mStartIcon);
            mStartStop.setToolTipText(mLocalizer.msg("Enable", "Enable Site"));
        } else {
            mStartStop.setIcon(mStopIcon);
            mStartStop.setToolTipText(mLocalizer.msg("Disable", "Disable Site"));
        }

        mEdit.setEnabled(adr.isUserEntry());
        mDelete.setEnabled(adr.isUserEntry());

        mUp.setEnabled(mAddressList.getSelectedIndex() != 0);
        mDown.setEnabled(mAddressList.getSelectedIndex() < mCloned.size()-1);
    }

    /**
     * Move a selected Item #rows
     * @param rows Rows to move the selected Item
     */
    private void moveSelectedItem(int rows) {
        
        int selected = mAddressList.getSelectedIndex();
        
        WebAddress adr = (WebAddress)mAddressList.getSelectedValue();
        
        mCloned.remove(adr);
        
        mCloned.insertElementAt(adr, selected + rows);
        
        mAddressList.setSelectedValue(adr, true);
    }    
    
    /**
     * Delete was pressed
     */
    private void deletePressed() {
        
        int result = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(mParent), 
                mLocalizer.msg("DeleteQuesiton", "Delete selected Item?"), 
                mLocalizer.msg("Delete", "Delete?"), 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        int num = mAddressList.getSelectedIndex();
        
        mCloned.remove(num);
        
        num--;
        
        if (num >= mCloned.size()-1) {
            num = mCloned.size()-1;
        }

        mAddressList.setSelectedIndex(num);      
        mAddressList.updateUI();
    }

    /**
     * New was pressed
     */
    private void newPressed() {
        WebAddress newadr = new WebAddress("", null, null, true, true);

        WebAddressEditDialog editor;
        
        Window win = (Window) UiUtilities.getLastModalChildOf(mParent); 
        
        if (win instanceof JDialog) {
            editor = new WebAddressEditDialog((JDialog)win, newadr);
        } else {
            editor = new WebAddressEditDialog((JFrame)win, newadr);
        }
        
        UiUtilities.centerAndShow(editor);

        if (editor.getReturnValue() == JOptionPane.OK_OPTION) {
            mCloned.add(newadr);
            mAddressList.setSelectedIndex(mCloned.size()-1);
            mAddressList.updateUI();
        }
        
    }
    
    /**
     * Edit was pressed
     */
    private void editPressed() {
        WebAddress seladr = (WebAddress) mAddressList.getSelectedValue();
        
        if (!seladr.isUserEntry()) {
            return;
        }
        
        WebAddressEditDialog editor;
        
        Window win = (Window) UiUtilities.getLastModalChildOf(mParent); 
        
        if (win instanceof JDialog) {
            editor = new WebAddressEditDialog((JDialog)win, seladr);
        } else {
            editor = new WebAddressEditDialog((JFrame)win, seladr);
        }
        
        UiUtilities.centerAndShow(editor);
        
        mAddressList.updateUI();
    }
    
    /* (non-Javadoc)
     * @see devplugin.SettingsTab#saveSettings()
     */
    public void saveSettings() {
        mOriginal.removeAllElements();
        mOriginal.addAll(mCloned);
    }

    /* (non-Javadoc)
     * @see devplugin.SettingsTab#getIcon()
     */
    public Icon getIcon() {
        return ImageUtilities.createImageIconFromJar("webplugin/Search16.gif", WebSettingsTab.class);
    }

    /* (non-Javadoc)
     * @see devplugin.SettingsTab#getTitle()
     */
    public String getTitle() {
        return mLocalizer.msg("WebPlugin", "WebPlugin");
    }

}