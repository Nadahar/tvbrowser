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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.exc.ErrorHandler;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.CapturePluginData;
import captureplugin.drivers.DeviceCreatorDialog;
import captureplugin.drivers.DeviceIf;
import captureplugin.utils.DeviceImportAndExport;


/**
 * Panel for creating/deleting of Devices
 * 
 * @author bodum
 */
public class DevicePanel extends JPanel {

    /** List of Devices */
    private JList mDeviceList;
    /** ParentWindow */
    private Window mOwner;
    /** Settings */
    private CapturePluginData mData;
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DevicePanel.class);


    private JButton mAddDevice, mRemoveDevice, mExportDevice, mImportDevice, mConfigDevice;

    /**
     * Creates the Panel
     * @param owner ParentFrame
     * @param data Settings
     */
    public DevicePanel(final Window owner, CapturePluginData data) {
        mData = data;
        mOwner = owner;
        createGui();
    }
    
    /**
     * Creates the GUI
     */
    private void createGui() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mDeviceList = new JList(new Vector<DeviceIf>(mData.getDevices()));
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

        mAddDevice = new JButton(mLocalizer.msg("Add", "Add Device"));

        mAddDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              SwingUtilities.invokeLater(new Runnable()  {
                public void run() {
                  addDevice();
                }
              });
            }
        });
        
        buttonPanel.add(mAddDevice, c);
        
        mConfigDevice = new JButton(mLocalizer.msg("Config", "Configure Device"));
        
        mConfigDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configDevice();
            }
        });
        
        buttonPanel.add(mConfigDevice, c);
        
        mRemoveDevice = new JButton(mLocalizer.msg("Remove", "Delete Device"));
        
        mRemoveDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeDevice();
            }
        });
        
        buttonPanel.add(mRemoveDevice, c);

        mExportDevice= new JButton(mLocalizer.msg("Export", "Export Device"));
        
        mExportDevice.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            exportDevice();
          }
        });
        
        buttonPanel.add(mExportDevice, c);
        
        mImportDevice= new JButton(mLocalizer.msg("Import", "Import Device"));
        
        mImportDevice.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            importDevice();
          }
        });

        buttonPanel.add(mImportDevice, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        buttonPanel.add(new JPanel(), c);
        
        add(buttonPanel, BorderLayout.EAST);
        
        mDeviceList.addListSelectionListener(new ListSelectionListener() {

          public void valueChanged(ListSelectionEvent e) {
            setButtonState();
          }
          
        });
        if (mDeviceList.getModel().getSize() > 0) {
          mDeviceList.setSelectedIndex(0);
        }
        
        setButtonState();
    }

    /**
     * Sets the Button-State.
     * If no Device is selected, a few Buttons are disabled
     */
    private void setButtonState() {
      boolean state = true;
      
      if (mDeviceList.getSelectedIndex() == -1) {
        state = false;
      }
      
      mRemoveDevice.setEnabled(state);
      mExportDevice.setEnabled(state);
      mConfigDevice.setEnabled(state);
    }
    
    /**
     * Add a Device
     */
    private void addDevice() {
        Window parent = UiUtilities.getLastModalChildOf(mOwner);
        DeviceCreatorDialog dialog = new DeviceCreatorDialog(parent);
        UiUtilities.centerAndShow(dialog);
        
        DeviceIf device = dialog.createDevice();
        
        if (device != null) {
            mData.getDevices().add(device);
            device.configDevice(UiUtilities.getLastModalChildOf(mOwner));
            mDeviceList.setListData(new Vector<DeviceIf>(mData.getDevices()));
        }
    }

    /**
     * Configure a selected Device
     */
    private void configDevice() {
        DeviceIf device = (DeviceIf) mDeviceList.getSelectedValue();
        
        if (device != null) {
            device.configDevice(UiUtilities.getLastModalChildOf(mOwner));
            mDeviceList.repaint();
        }
    }

    /**
     * Remove a selected Device
     */
    private void removeDevice() {
        DeviceIf device = (DeviceIf) mDeviceList.getSelectedValue();
        
        if (device != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    mLocalizer.msg("AskRemove", "Delete selected Device?"),
                    mLocalizer.msg("Remove", "Delete Device"),
                    JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                mData.getDevices().remove(device);
                mDeviceList.setListData(new Vector<DeviceIf>(mData.getDevices()));
            }
        }
    }
    
    /**
     * Imports a Device
     */
    private void importDevice() {
      JFileChooser chooser = new JFileChooser();
      
      ExtensionFileFilter filter = new ExtensionFileFilter("tcf", mLocalizer.msg("FileType", "TV-Browser Capture-Device File (*.tcf)"));

      chooser.addChoosableFileFilter(filter);
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        DeviceImportAndExport importer = new DeviceImportAndExport();
        
        DeviceIf device = importer.importDevice(mData, this, chooser.getSelectedFile());
        
        if (device == null) {
          ErrorHandler.handle(importer.getError(), importer.getException());
        } else {
          mData.getDevices().add(device);
        }

        mDeviceList.setListData(new Vector<DeviceIf>(mData.getDevices()));
      }
    }

    /**
     * Exports a Device to a File
     *
     */
    private void exportDevice() {
      JFileChooser chooser = new JFileChooser();
      
      ExtensionFileFilter filter = new ExtensionFileFilter("tcf", mLocalizer.msg("FileType", "TV-Browser Capture-Device File (*.tcf)"));

      chooser.addChoosableFileFilter(filter);

      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        
        File file = chooser.getSelectedFile();
        
        if (!file.getName().endsWith(".tcf")) {
          file = new File(file.getAbsolutePath() + ".tcf");
        }
        
        DeviceImportAndExport export = new DeviceImportAndExport();
        
        DeviceIf device = (DeviceIf) mDeviceList.getSelectedValue();
        
        if (!export.exportDevice(this, device, file)) {
          ErrorHandler.handle(export.getError(), export.getException());
        }

        mDeviceList.setListData(new Vector<DeviceIf>(mData.getDevices()));
      }
    }
    
}