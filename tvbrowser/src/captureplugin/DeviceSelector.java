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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import devplugin.Program;


/**
 * A selector for the Action in the Devices
 */
public class DeviceSelector extends JPopupMenu {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceSelector.class);
 
    /** ParentFrame */
    private Window mParent;
    /** List of Devices */
    private DeviceIf[] mDevices;
    /** Program */
    private Program mProgram;
    /** Return-Value */
    private int mReturn = JOptionPane.CANCEL_OPTION;
    
    /**
     * Creates the Selector
     * @param frame Parent-Frame
     * @param devices Devices to select from
     * @param prg Program to use
     */
    public DeviceSelector(Window parent, DeviceIf[] devices, Program prg) {
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
        
        if (mDevices.length <= 0) {
            return;
        }else if (mDevices.length == 1) {
            JMenu menu = createMenuForDevice(mDevices[0]);

            while (menu.getItemCount() > 0) {
                add((JMenuItem) menu.getItem(0));
            }
            
        } else {
        
            for (int i = 0; i < mDevices.length; i++) {
                add(createMenuForDevice(mDevices[i]));
            }
        }
    }
    
    
    /**
     * Create the JMenu for a Device
     * @param device 
     * @return JMenu for Device
     */
    private JMenu createMenuForDevice(final DeviceIf device) {
        
        String deviceName = device.getName() + " (" + device.getDriver().getDriverName() + ")"; 
        
        JMenu menu = new JMenu(deviceName);

        if (device.isAbleToAddAndRemovePrograms()) {
            if (!device.isInList(mProgram)) {
                JMenuItem item = new JMenuItem(mLocalizer.msg("Add","Add {0}.",mProgram.getTitle()));
                menu.add(item);
                
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        device.add(mParent, mProgram);
                        CapturePlugin.getInstance().updateMarkedPrograms();
                    }
                });
                
            } else {
                JMenuItem item = new JMenuItem(mLocalizer.msg("Remove","Remove {0}.",mProgram.getTitle()));
                menu.add(item);

                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        device.remove(mParent, mProgram);
                        CapturePlugin.getInstance().updateMarkedPrograms();
                    }
                });
            }
        }
        
        String[] menuItems = device.getAdditionalCommands(mProgram);
        
        for (int i = 0; i < menuItems.length; i++) {
            
            final int num = i;
            JMenuItem item = new JMenuItem(menuItems[i]); 
            menu.add(item);

            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    device.executeAdditionalCommand(mParent, num, mProgram);
                    CapturePlugin.getInstance().updateMarkedPrograms();
                }
            });
            
            
        }
        
        return menu;
    }

}