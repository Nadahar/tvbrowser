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
package captureplugin.drivers;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.ui.Localizer;

/**
 * A Dialog for creating new Devices
 */
public class DeviceCreatorDialog extends JDialog {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceCreatorDialog.class);
 
    /** Available Drivers */
    private JComboBox mDriverCombo;

    /** Description of Driver */
    private JTextArea mDesc;

    /** Name of Device */
    private JTextField mName;

    /** Which button was pressed ? */
    private int mRetmode = JOptionPane.CANCEL_OPTION;

    /**
     * Creates the Dialog
     * 
     * @param owner Parent-Frame
     */
    public DeviceCreatorDialog(JFrame owner) {
        super(owner, true);
        createGUI();
        setTitle(mLocalizer.msg("Title", "Create Device"));
    }

    /**
     * Create the GUI
     */
    private void createGUI() {

        DriverIf[] drivers = DriverFactory.getInstance().getDrivers();

        mDriverCombo = new JComboBox(drivers);

        JPanel panel = (JPanel) getContentPane();

        panel.setLayout(new GridBagLayout());

        GridBagConstraints label = new GridBagConstraints();

        label.insets = new Insets(5, 5, 5, 5);
        label.anchor = GridBagConstraints.NORTHWEST;

        GridBagConstraints input = new GridBagConstraints();

        input.fill = GridBagConstraints.HORIZONTAL;
        input.weightx = 1.0;
        input.gridwidth = GridBagConstraints.REMAINDER;
        input.insets = new Insets(5, 5, 5, 5);

        panel.add(new JLabel(mLocalizer.msg("Name", "Name") + ":"), label);

        mName = new JTextField();
        
        panel.add(mName, input);

        panel.add(new JLabel(mLocalizer.msg("Driver", "Driver") +":"), label);

        panel.add(mDriverCombo, input);

        mDesc = new JTextArea();
        mDesc.setLineWrap(true);
        mDesc.setWrapStyleWord(true);
        
        panel.add(new JLabel(mLocalizer.msg("Description", "Description") +":"), input);

        GridBagConstraints descC = new GridBagConstraints();
        descC.weightx = 1.0;
        descC.weighty = 1.0;
        descC.fill = GridBagConstraints.BOTH;
        descC.gridwidth = GridBagConstraints.REMAINDER;
        descC.insets = new Insets(5, 5, 5, 5);

        panel.add(new JScrollPane(mDesc,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), descC);

        String desc = ((DriverIf) mDriverCombo.getSelectedItem()).getDriverDesc();
        mDesc.setText(desc);

        mDriverCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                String desc = ((DriverIf) mDriverCombo.getSelectedItem()).getDriverDesc();
                mDesc.setText(desc);
            }

        });

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton ok = new JButton(mLocalizer.msg("OK", "OK"));
        JButton cancel = new JButton(mLocalizer.msg("Cancel", "Cancel"));

        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        getRootPane().setDefaultButton(ok);

        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        input.insets = new Insets(0, 5, 5, 0);

        panel.add(buttonPanel, input);

        setSize(400, 300);

    }

    /**
     * OK was pressed
     */
    private void okPressed() {

        if (mName.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, mLocalizer.msg("NoName", "No Name was entered"), mLocalizer.msg("Error", "Error"), JOptionPane.ERROR_MESSAGE);

        } else {
            mRetmode = JOptionPane.OK_OPTION;
            hide();
        }
    }

    /**
     * Create the Device
     * 
     * @return Device
     */
    public DeviceIf createDevice() {

        if (mRetmode != JOptionPane.OK_OPTION) { return null; }

        return ((DriverIf) mDriverCombo.getSelectedItem()).createDevice(mName.getText().trim());
    }

    /**
     * Which Button was pressed ? JOptionPane.OK_OPTION / CANCEL_OPTION
     * 
     * @return Button that was pressed (JOptionPane.OK_OPTION / CANCEL_OPTION)
     */
    public int getReturnValue() {
        return mRetmode;
    }
}