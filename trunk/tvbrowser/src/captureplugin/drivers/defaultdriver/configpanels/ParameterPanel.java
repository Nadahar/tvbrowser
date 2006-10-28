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
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.paramhandler.ParamDescriptionPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.defaultdriver.AdditionalParams;
import captureplugin.drivers.defaultdriver.CaptureParamLibrary;
import captureplugin.drivers.defaultdriver.DefaultKonfigurator;
import captureplugin.drivers.defaultdriver.DeviceConfig;

/**
 * Enter the Parameters
 */
public class ParameterPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParameterPanel.class);

    /** GUI */
    private JTextArea mAddFormatTextField = new JTextArea();

    private JTextArea mRemFormatTextField = new JTextArea();

    /** Data for the Panel */
    private DeviceConfig mData;

    /** Konfigurator-Dialog */
    private DefaultKonfigurator mKonfigurator;
    
    /**
     * Creates the Panel
     * @param data Settings
     */
    public ParameterPanel(DefaultKonfigurator dialog, DeviceConfig data) {
        mKonfigurator = dialog;
        mData = data;
        
        createPanel();
    }

    /**
     * creates a JPanel for getting the parameterformat
     */
    private void createPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Parameters", "Parameters")));

        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(2, 5, 2, 2);

        // Record
        JLabel addFormatLabel = new JLabel(mLocalizer.msg("Record", "record"));

        c.anchor = GridBagConstraints.NORTHWEST;

        c.weightx = 0;
        c.weighty = 0;
        add(addFormatLabel, c);

        c.weightx = 1;
        c.weighty = 0.3;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;

        mAddFormatTextField.setLineWrap(true);
        // Consume Enter-Key
        mAddFormatTextField.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    ke.consume();
                }
            }
        });
        mAddFormatTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                addFormatChanged();
            }
        });

        mAddFormatTextField.setText(mData.getParameterFormatAdd());

        JScrollPane scroll = new JScrollPane(mAddFormatTextField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, c);

        // Remove
        JLabel remFormatLabel = new JLabel(Localizer.getLocalization(Localizer.I18N_DELETE));

        c = new GridBagConstraints();

        c.insets = new Insets(2, 5, 2, 2);
        c.anchor = GridBagConstraints.NORTHWEST;

        c.weightx = 0;
        c.weighty = 0;
        add(remFormatLabel, c);

        c.weightx = 1;
        c.weighty = 0.3;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;

        mRemFormatTextField.setLineWrap(true);
        // Consume Enter-Key
        mRemFormatTextField.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    ke.consume();
                }
            }
        });
        mRemFormatTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                remFormatChanged();
            }
        });

        mRemFormatTextField.setText(mData.getParameterFormatRem());
            
        scroll = new JScrollPane(mRemFormatTextField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, c);
        
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton additional = new JButton(mLocalizer.msg("Additional", "Additional Parameters"));
        additional.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                additionalPressed();
            }
        });
        addPanel.add(additional);
        
        GridBagConstraints b = new GridBagConstraints();
        b.weightx = 1;
        b.gridwidth = GridBagConstraints.REMAINDER;
        b.fill = GridBagConstraints.HORIZONTAL;
        add(addPanel, b);
        
        // Description

        c.weighty = 1.0;
        
        add(new ParamDescriptionPanel(new CaptureParamLibrary(mData)), c);
    }

    /**
     * invoked when the addFormat - TextField losts the Focus, the value of the
     * TextField will then be stored.
     */
    public void addFormatChanged() {
        mData.setParameterFormatAdd(mAddFormatTextField.getText());
    }

    /**
     * invoked when the remFormat - TextField losts the Focus, the value of the
     * TextField will then be stored.
     */
    public void remFormatChanged() {
        mData.setParameterFormatRem(mRemFormatTextField.getText());
    }

    /**
     * Additional Parameters was pressed
     */
    private void additionalPressed() {
        AdditionalParams params = new AdditionalParams(mKonfigurator, mData);
        UiUtilities.centerAndShow(params);
    }

}