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
package captureplugin.tabs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceCreatorDialog;
import captureplugin.drivers.DeviceIf;

import util.ui.Localizer;
import util.ui.UiUtilities;


/**
 * Panel for creating/deleting of Devices
 * 
 * @author bodum
 */
public class DevicePanel extends JPanel {

    /** List of Devices */
    private JList mDeviceList;
    /** ParentFrame */
    private JFrame mOwner;
    /** Settings */
    private CapturePluginData mData;
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DevicePanel.class);
       

    /**
     * Creates the Panel
     * @param owner ParentFrame
     * @param data Settings
     */
    public DevicePanel(final JFrame owner, CapturePluginData data) {
        mData = data;
        mOwner = owner;
        createGui();
    }
    
    /**
     * Creates the GUI
     */
    public void createGui() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mDeviceList = new JList(new Vector(mData.getDevices()));
        mDeviceList.setCellRenderer(new DeviceCellRenderer());
        
        mDeviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        mDeviceList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    configDevice();
                }
            }
        });
        
        
        add(new JScrollPane(mDeviceList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(0, 0, 5, 0);
        
        JButton addDevice = new JButton(mLocalizer.msg("Add", "Add Device"));

        addDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addDevice();
            }
        });
        
        buttonPanel.add(addDevice, c);
        
        JButton configDevice = new JButton(mLocalizer.msg("Config", "Configure Device"));
        
        configDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configDevice();
            }
        });
        
        buttonPanel.add(configDevice, c);
        
        JButton removeDevice = new JButton(mLocalizer.msg("Remove", "Delete Device"));
        
        removeDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeDevice();
            }
        });
        
        buttonPanel.add(removeDevice, c);

        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        buttonPanel.add(new JPanel(), c);
        
        add(buttonPanel, BorderLayout.EAST);
    }
    
    private void addDevice() {
        
        DeviceCreatorDialog dialog = new DeviceCreatorDialog(mOwner);
        UiUtilities.centerAndShow(dialog);
        
        DeviceIf device = dialog.createDevice();
        
        if (device != null) {
            System.out.println(device.getDriver() + ":" + device);
            mData.getDevices().add(device);
            
            mDeviceList.setListData(new Vector(mData.getDevices()));
        }
        
    }

    private void configDevice() {
        DeviceIf device = (DeviceIf) mDeviceList.getSelectedValue();
        
        if (device != null) {
            device.configDevice(mOwner);
            mDeviceList.repaint();
        }
    }

    private void removeDevice() {
        DeviceIf device = (DeviceIf) mDeviceList.getSelectedValue();
        
        if (device != null) {
            int result = JOptionPane.showConfirmDialog(this, 
                    mLocalizer.msg("AskRemove", "Delete selected Device?"),
                    mLocalizer.msg("Remove", "Delete Device"),
                    JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                mData.getDevices().remove(device);
                mDeviceList.setListData(new Vector(mData.getDevices()));
            }
        }
    }

}