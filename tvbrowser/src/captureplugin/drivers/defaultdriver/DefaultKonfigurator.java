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
package captureplugin.drivers.defaultdriver;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.defaultdriver.configpanels.ApplicationPanel;
import captureplugin.drivers.defaultdriver.configpanels.ChannelPanel;
import captureplugin.drivers.defaultdriver.configpanels.ParameterPanel;
import captureplugin.drivers.defaultdriver.configpanels.SettingsPanel;
import captureplugin.drivers.defaultdriver.configpanels.VariablePanel;

/**
 * The Configuration-Dialog for this Device
 */
public class DefaultKonfigurator extends JDialog {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DefaultKonfigurator.class);
 
    /** Config */
    private DeviceConfig mConfig;
    /** Which Button was pressed */
    private int mButtonPressed = JOptionPane.CANCEL_OPTION;
    /** Tab-Pane */
    private JTabbedPane mTab;
    /** Name */
    private JTextField mName;
    
    public final static int TAB_PATH = 0;
    public final static int TAB_PARAMETER = 1;
    public final static int TAB_SETTINGS = 2;
    public final static int TAB_CHANNELS = 3;
    
    /**
     * Creates the Dialog
     * @param owner Parent-Frame
     * @param config Config
     */
    public DefaultKonfigurator(JFrame owner, DeviceConfig config) {
      super(owner, true);
      mConfig = config;
      createGui();
    }
    
    /**
     * Creates the Dialog
     * @param owner Parent-Frame
     * @param config Config
     */
    public DefaultKonfigurator(JDialog owner, DeviceConfig config) {
      super(owner, true);
      mConfig = config;
      createGui();
    }    
    
    /**
     * Create the GUI
     */
    private void createGui() {
        setTitle(mLocalizer.msg("Settings", "Settings"));
        
        JPanel panel = (JPanel) getContentPane();
        
        panel.setLayout(new BorderLayout());
        
        
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        
        namePanel.add(new JLabel(mLocalizer.msg("Name", "Name")+":"), BorderLayout.WEST);
        
        mName = new JTextField();
        
        mName.setText(mConfig.getName());
        
        namePanel.add(mName, BorderLayout.CENTER);
        
        panel.add(namePanel, BorderLayout.NORTH);
        
        
        mTab = new JTabbedPane();
        
        mTab.add(mLocalizer.msg("Application", "Application"), new ApplicationPanel(mConfig));
        mTab.add(mLocalizer.msg("Parameter", "Parameter"), new ParameterPanel(this, mConfig));
        mTab.add(mLocalizer.msg("Settings", "Settings"), new SettingsPanel(mConfig));
        mTab.add(mLocalizer.msg("Channels", "Channels"), new ChannelPanel(mConfig));
        mTab.add(mLocalizer.msg("Variables", "Variables"), new VariablePanel(mConfig));
        
        panel.add(mTab, BorderLayout.CENTER);
     
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        
        JButton ok = new JButton(mLocalizer.msg("OK", "OK"));

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mConfig.setName(mName.getText());
                mButtonPressed = JOptionPane.OK_OPTION;
                hide();
            }
        });
        
        JButton cancel = new JButton(mLocalizer.msg("Cancel", "Cancel"));
        
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(600, 550);
    }

    /**
     * Was OK pressed ?
     * @return true if OK was pressed
     */
    public boolean okWasPressed() {
        return mButtonPressed == JOptionPane.OK_OPTION;
    }

    /**
     * Returns the Config
     * @return Config
     */
    public DeviceConfig getConfig() {
        return mConfig;
    }

    /**
     * Shows a specifig Tab (TAB_*)
     * @param num Tab to show
     */
    public void show(int num) {
        
        mTab.setSelectedIndex(num);
        UiUtilities.centerAndShow(this);
    }
}