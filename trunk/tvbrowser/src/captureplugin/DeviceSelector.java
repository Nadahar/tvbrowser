/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
package captureplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import util.ui.Localizer;
import util.ui.ProgramList;
import captureplugin.drivers.DeviceIf;
import devplugin.Program;


/**
 * A selector for the Action in the Devices
 */
public class DeviceSelector extends JDialog {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceSelector.class);
 
    /** ParentFrame */
    private Window mParent;
    /** List of Devices */
    private DeviceIf[] mDevices;
    /** Program */
    private Program[] mProgram;
    /** The Program-List */
    private ProgramList mProgramList;
    
    /** Return-Value */
    private int mReturn = JOptionPane.CANCEL_OPTION;
    /** The Device-Selector */
    private JComboBox mDeviceSelector;
    /** The Funciton-Selector */
    private JComboBox mFunction;
    
    /**
     * Creates the Selector
     * @param frame Parent-Frame
     * @param devices Devices to select from
     * @param prg Program to use
     */
    public DeviceSelector(JFrame parent, DeviceIf[] devices, Program[] prg) {
        super((JFrame) parent);
        mParent = parent;
        mDevices = devices;
        mProgram = prg;
        createGUI();
    }

    /**
     * Creates the Selector
     * @param frame Parent-Frame
     * @param devices Devices to select from
     * @param prg Program to use
     */
    public DeviceSelector(JDialog parent, DeviceIf[] devices, Program[] prg) {
        super((JDialog) parent);
        mParent = parent;
        mDevices = devices;
        mProgram = prg;
        createGUI();
    }

    
    /**
     * Creates the GUI
     * @param prg Program to use
     */
    private void createGUI() {
        setLocationRelativeTo(getParent());
        
        setTitle(mLocalizer.msg("title", "CapturePlugin"));
        
        JPanel panel = (JPanel) getContentPane();
        
        panel.setLayout(new GridBagLayout());
        
        mProgramList= new ProgramList(new DefaultListModel());
        mProgramList.addMouseListeners(CapturePlugin.getInstance());
        
        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints d = new GridBagConstraints();
        
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;

        d.weightx = 1.0;
        d.weighty = 0;
        d.fill = GridBagConstraints.HORIZONTAL;
        d.gridwidth = GridBagConstraints.REMAINDER;

        mDeviceSelector = new JComboBox(mDevices);
        mDeviceSelector.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              
              if (value instanceof DeviceIf) {
                value = ((DeviceIf)value).getName();
              }
              
              return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
          });
        
        mDeviceSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillFunctionList();
            }
        });
        
        panel.add(createNamedPanel(mDeviceSelector, mLocalizer.msg("device", "Device")+ ":"), d);
        
        mFunction = new JComboBox();
        fillFunctionList();

        mFunction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillProgramList();
            }
            
        });
        
        fillProgramList();

        if (mProgramList.getModel().getSize() == 0) {
            mFunction.setSelectedIndex(1);
            fillProgramList();
        }
        
        panel.add(createNamedPanel(mFunction, mLocalizer.msg("chooseFunction" , "Choose Function") + ":"), d);

        JScrollPane scpane = new JScrollPane(mProgramList);
        scpane.setPreferredSize(new Dimension(400, 200));
        
        panel.add(createNamedPanel(scpane, mLocalizer.msg("usingPrograms", "Programs that are used") + ":"), c);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton ok = new JButton (Localizer.getLocalization(Localizer.I18N_OK));
        
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okPressed();
                setVisible(false);
            }
            
        });
        
        JButton cancel = new JButton (Localizer.getLocalization(Localizer.I18N_CANCEL));
        
        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
              setVisible(false); 
            }
            
        });
        
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        
        
        panel.add(buttonPanel, d);
            
        pack();
    }
    
    
    /**
     * Fills the ProgramList of the Dialog
     */
    protected void fillProgramList() {
        DefaultListModel model = new DefaultListModel();
        
        DeviceIf device = (DeviceIf) mDeviceSelector.getSelectedItem();
        
        if (device.isAbleToAddAndRemovePrograms() && (mFunction.getSelectedIndex() < 2)) {
            
            if (mFunction.getSelectedIndex() == 0) {
                for (int i = 0; i < mProgram.length; i++) {
                    if (!device.isInList(mProgram[i])) {
                        model.addElement(mProgram[i]);
                    }
                }
            } else {
                for (int i = 0; i < mProgram.length; i++) {
                    if (device.isInList(mProgram[i])) {
                        model.addElement(mProgram[i]);
                    }
                }
                
            }
        } else {
            for (int i = 0; i < mProgram.length; i++) {
                model.addElement(mProgram[i]);
            }
        }
        
        mProgramList.setModel(model);
    }

    /**
     * Fills the Function-List 
     */
    protected void fillFunctionList() {
        
        DeviceIf device = (DeviceIf) mDeviceSelector.getSelectedItem();
        
        String[] commands = device.getAdditionalCommands();
        int num = commands.length;
        
        int start = 0;

        if (device.isAbleToAddAndRemovePrograms()) {
            num+=2;
            start = 2;
        }

        String[] str = new String[num];
        
        if (device.isAbleToAddAndRemovePrograms()) {
            str[0] = Localizer.getLocalization(Localizer.I18N_ADD);
            str[1] = Localizer.getLocalization(Localizer.I18N_DELETE);
        }
        
        for (int i=start;i < commands.length+start;i++) {
            System.out.println(i-start+"--"+commands[i-start]);
            str[i] = commands[i-start]; 
        }
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(str);
        mFunction.setModel(model);
    }

    /**
     * Creates the Panel with the Name
     * 
     * @param c add Component to the Panel
     * @param string name to add
     * @return new JPanel
     */
    private JPanel createNamedPanel(Component c, String string) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(string));
        panel.add(c, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * OK was pressed
     */
    private void okPressed() {
        
        DeviceIf device = (DeviceIf) mDeviceSelector.getSelectedItem();
        
        ListModel model = mProgramList.getModel();
        
        int num = mFunction.getSelectedIndex();
        
        if (device.isAbleToAddAndRemovePrograms()) {
            
            if (num == 0) {
                for (int i=0; i < model.getSize();i ++) {
                    device.add((Window)getParent(), (Program) model.getElementAt(i));
                }
                
            } else if (num == 1) {
                for (int i=0; i < model.getSize();i ++) {
                    device.remove((Window)getParent(), (Program) model.getElementAt(i));
                }
                
            } else {
                for (int i=0; i < model.getSize();i ++) {
                    device.executeAdditionalCommand((Window)getParent(), num-2, (Program) model.getElementAt(i));
                }
            }
            
        } else {
            
            for (int i=0; i < model.getSize();i ++) {
                device.executeAdditionalCommand((Window)getParent(), num, (Program) model.getElementAt(i));
            }
            
        }
        CapturePlugin.getInstance().updateMarkedPrograms();
        
    }
}