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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;


/**
 * Creates the Settings-Panel
 * 
 * @author bodum
 */
public class SettingsPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(SettingsPanel.class);

    /** GUI */
    private JSpinner mPreTimeTextField;

    private JSpinner mPostTimeTextField;
    
    private JTextField mUserName = new JTextField();
    private JPasswordField mUserPwd = new JPasswordField();
    
    
    private JSpinner mMaxSimult;
    
    
    /** Settings */
    private DeviceConfig mData;

    /**
     * Creates the SettingsPanel
     * @param data Settings
     */
    public SettingsPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for getting the time offsets
     */
    private void createPanel() {
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(createTimePanel(), c);
        
        add(createUserPanel(), c);
        
        add(createAdditionalPanel(), c);
        
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(new JPanel(), c);
    }

    
    /**
     * Panel with additional Settings
     * @return JPanel
     */
    private JPanel createAdditionalPanel() {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Additional", "Additonal")));

        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(2, 5, 2, 2);

        GridBagConstraints lc = new GridBagConstraints();
        
        lc.weightx = 0.2;
        lc.fill = GridBagConstraints.NONE;
        lc.insets = new Insets(2, 5, 2, 2);
        lc.anchor = GridBagConstraints.WEST;        
        
        final JCheckBox checkReturn = new JCheckBox(mLocalizer.msg("CheckError", "Check if returns Error"));
        
        checkReturn.setSelected(mData.useReturnValue());
        
        checkReturn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mData.setUseReturnValue(checkReturn.isSelected());
            }
            
        });
        panel.add(checkReturn, c);

        
        final JCheckBox showOnError = new JCheckBox(mLocalizer.msg("ShowResultOnError","Show Result-Dialog only on Error"));
        
        showOnError.setSelected(mData.getDialogOnlyOnError());
        
        showOnError.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mData.setDialogOnlyOnError(showOnError.isSelected());
            }
            
        });

        panel.add(showOnError, c);

        final JCheckBox oldPrograms = new JCheckBox(mLocalizer.msg("OnlyFuture", "Only allow Programs that are in the future"));
        
        oldPrograms.setSelected(mData.getOnlyFuturePrograms());
        
        oldPrograms.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mData.setOnlyFuturePrograms(oldPrograms.isSelected());
            }
            
        });
        
        panel.add(oldPrograms, c);
        
        
        Integer value = new Integer(mData.getMaxSimultanious()); 
        Integer min = new Integer(1);
        Integer step = new Integer(1); 
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, null, step); 
        int fifty = model.getNumber().intValue(); 
        
        mMaxSimult = new JSpinner(model);

        mMaxSimult.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setMaxSimultanious(((Integer)mMaxSimult.getValue()).intValue());
            }
            
        });

        panel.add(new JLabel(mLocalizer.msg("MaxSimult","Maximum simultaneous recordings")+ ":") , lc);
        
        panel.add(mMaxSimult,c);
        
        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("User", "User")));
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints lc = new GridBagConstraints();
        
        lc.weightx = 0.2;
        lc.fill = GridBagConstraints.NONE;
        lc.insets = new Insets(2, 5, 2, 2);
        lc.anchor = GridBagConstraints.WEST;
        
        GridBagConstraints fc = new GridBagConstraints();
        
        fc.weightx = 0.8;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(2, 2, 2, 2);
        
        panel.add(new JLabel(mLocalizer.msg("Username", "Username") + ":"), lc);
        
        mUserName.setText(mData.getUserName());
        mUserName.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                mData.setUserName(mUserName.getText());
            }
        });
        
        panel.add(mUserName, fc);
        
        panel.add(new JLabel(mLocalizer.msg("Password", "Password") + ":"), lc);
        
        mUserPwd.setText(mData.getPassword());
        mUserPwd.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                mData.setPassword(new String(mUserPwd.getPassword()));
            }
        });
        
        panel.add(mUserPwd, fc);
        
        return panel;
    }
    

    /**
     * Creates a Panel for Time-Settings
     * @return Time-Settings Panel
     */
    private JPanel createTimePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("TimeSettings", "Timesettings")));
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints lc = new GridBagConstraints();
        
        lc.weightx = 0.2;
        lc.fill = GridBagConstraints.NONE;
        lc.insets = new Insets(2, 5, 2, 2);
        lc.anchor = GridBagConstraints.WEST;
        
        GridBagConstraints fc = new GridBagConstraints();
        
        fc.weightx = 0.8;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(2, 2, 2, 2);

        panel.add(new JLabel(mLocalizer.msg("Earlier", "Number of minutes to start erlier")),lc);

        Integer value = new Integer(mData.getPreTime()); 
        Integer min = new Integer(0);
        Integer step = new Integer(1); 

        mPreTimeTextField = new JSpinner(new SpinnerNumberModel(value, min, null, step));

        mPreTimeTextField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setPreTime(((Integer)mPreTimeTextField.getValue()).intValue());
            }
            
        });
        
        panel.add(mPreTimeTextField, fc);

        panel.add(new JLabel(mLocalizer.msg("Later", "Number of minutes to stop later")),lc);

        value = new Integer(mData.getPostTime()); 

        mPostTimeTextField = new JSpinner(new SpinnerNumberModel(value, min, null, step));

        mPostTimeTextField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setPostTime(((Integer)mPostTimeTextField.getValue()).intValue());
            }
            
        });

        
        panel.add(mPostTimeTextField, fc);
        
        return panel;
    }

}