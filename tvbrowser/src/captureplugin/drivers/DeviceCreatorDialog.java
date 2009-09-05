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

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;

/**
 * A Dialog for creating new Devices
 */
public class DeviceCreatorDialog extends JDialog implements WindowClosingIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceCreatorDialog.class);
 
    /** Available Drivers */
    private JComboBox mDriverCombo;

    /** Description of Driver */
    private JEditorPane mDesc;

    /** Name of Device */
    private JTextField mName;

    /** Which button was pressed ? */
    private int mRetmode = JOptionPane.CANCEL_OPTION;

  /**
   * Creates the Dialog
   * 
   * @param parent
   *          Parent-Frame
   */
    public DeviceCreatorDialog(Window parent) {
    super(parent);
    setModal(true);
        createGUI();
        setTitle(mLocalizer.msg("Title", "Create Device"));
    }
    
    /**
     * Create the GUI
     */
    private void createGUI() {
        UiUtilities.registerForClosing(this);
      
        DriverIf[] drivers = DriverFactory.getInstance().getDrivers();

        mDriverCombo = new JComboBox(drivers);
        mDriverCombo.setRenderer(new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof DriverIf) {
              value = ((DriverIf)value).getDriverName();
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          }
        });
        
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

        panel.add(new JLabel(mLocalizer.msg("Name", "Name")), label);

        mName = new JTextField();
        panel.add(mName, input);

        panel.add(new JLabel(mLocalizer.msg("Driver", "Driver")), label);
        panel.add(mDriverCombo, input);

        mDesc = UiUtilities.createHtmlHelpTextArea("");
        mDesc.setEditable(false);
        
        panel.add(new JLabel(mLocalizer.msg("Description", "Description")), input);

        GridBagConstraints descC = new GridBagConstraints();
        descC.weightx = 1.0;
        descC.weighty = 1.0;
        descC.fill = GridBagConstraints.BOTH;
        descC.gridwidth = GridBagConstraints.REMAINDER;
        descC.insets = new Insets(5, 5, 5, 5);

        panel.add(new JScrollPane(mDesc,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), descC);

        final Font font = new JLabel().getFont();

        String desc = ((DriverIf) mDriverCombo.getSelectedItem()).getDriverDesc();
        desc = "<html><div style=\"color:#000000;font-family:"+ font.getName() +"; font-size:"+font.getSize()+";\">"+desc+"</div></html>";
        mDesc.setText(desc);
        mDesc.setFont(font);
        
        mDriverCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                String description = ((DriverIf) mDriverCombo.getSelectedItem()).getDriverDesc();
                description = "<html><div style=\"color:#000000;font-family:"+ font.getName() +"; font-size:"+font.getSize()+";\">"+description+"</div></html>";
                mDesc.setText(description);
                mDesc.setFont(font);
            }

        });

        final JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.setEnabled(false);
        final JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
        ok.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e) {
              okPressed();
          }
        });

        cancel.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });

        mName.getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void removeUpdate(DocumentEvent e) {
            updateButtons();
          }
          
          @Override
          public void insertUpdate(DocumentEvent e) {
            updateButtons();
          }
          
          @Override
          public void changedUpdate(DocumentEvent e) {
            updateButtons();
          }

          private void updateButtons() {
            ok.setEnabled(!mName.getText().trim().isEmpty());
          }
        });

        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addGlue();
        builder.addButton(new JButton[] {ok, cancel});
        
        getRootPane().setDefaultButton(ok);

        input.insets = new Insets(5, 5, 5, 5);

        panel.add(builder.getPanel(), input);

        setSize(400, 300);

    }

    /**
     * OK was pressed
     */
    private void okPressed() {

        if (mName.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, mLocalizer.msg("NoName", "No Name was entered"), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);

        } else {
            mRetmode = JOptionPane.OK_OPTION;
            setVisible(false);
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

    public void close() {
      setVisible(false);
    }
}