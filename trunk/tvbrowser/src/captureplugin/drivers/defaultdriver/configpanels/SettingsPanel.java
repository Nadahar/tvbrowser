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

import captureplugin.drivers.defaultdriver.DeviceConfig;
import util.ui.Localizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.TimeZone;


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
    
    
    private JSpinner mMaxTimeout;
    
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
        
        
        Integer value = mData.getMaxSimultanious();
        Integer min = 1;
        Integer step = 1;
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, null, step); 

        mMaxSimult = new JSpinner(model);

        mMaxSimult.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setMaxSimultanious((Integer) mMaxSimult.getValue());
            }
            
        });
        
        // Dispache the KeyEvent to the RootPane for Closing the Dialog.
        // Needed for Java 1.4.
        mMaxSimult.getEditor().getComponent(0).addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
              mMaxSimult.getRootPane().dispatchEvent(e);
          }
        });


        panel.add(new JLabel(mLocalizer.msg("MaxSimult","Maximum simultaneous recordings")+ ":") , lc);
        
        panel.add(mMaxSimult,c);
        
        
        model = new SpinnerNumberModel(mData.getTimeOut(), -1, 999, 1);

        
        mMaxTimeout = new JSpinner(model);
        mMaxTimeout.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setTimeOut((Integer) mMaxTimeout.getValue());
            }
            
        });
        
        // Dispache the KeyEvent to the RootPane for Closing the Dialog.
        // Needed for Java 1.4.
        mMaxTimeout.getEditor().getComponent(0).addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
              mMaxTimeout.getRootPane().dispatchEvent(e);
          }
        });
        
        panel.add(new JLabel(mLocalizer.msg("Timeout","Wait sec. until Timeout (-1 = disabled)")+ ":") , lc);
        
        panel.add(mMaxTimeout,c);
        
        final JCheckBox useTime = new JCheckBox(mLocalizer.msg("useSystemTimezone","Use timezone provided by OS"), mData.useTimeZone());
        
        panel.add(useTime, c);

        String[] zoneIds = TimeZone.getAvailableIDs();
        final JComboBox timeZones = new JComboBox(zoneIds);
        for (int i=0; i<zoneIds.length; i++) {
          if (zoneIds[i].equals(mData.getTimeZone().getID())) {
            timeZones.setSelectedIndex(i); break;
          }
        }

        useTime.setSelected(!mData.useTimeZone());
        
        useTime.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            mData.setUseTimeZone(!useTime.isSelected());
            timeZones.setEnabled(!useTime.isSelected());
          }
        });
        
        timeZones.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            mData.setTimeZone(TimeZone.getTimeZone((String)timeZones.getSelectedItem()));
          }
        });
        
        JPanel timeZonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        timeZonePanel.add(new JLabel(mLocalizer.msg("Timezone","Timezone")+": "));
        timeZonePanel.add(timeZones);
        
        panel.add(timeZonePanel, c);
        timeZones.setEnabled(mData.useTimeZone());
        
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

        Integer value = mData.getPreTime();
        Integer min = 0;
        Integer step = 1;

        mPreTimeTextField = new JSpinner(new SpinnerNumberModel(value, min, null, step));

        mPreTimeTextField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setPreTime((Integer) mPreTimeTextField.getValue());
            }
            
        });
        
        // Dispache the KeyEvent to the RootPane for Closing the Dialog.
        // Needed for Java 1.4.
        mPreTimeTextField.getEditor().getComponent(0).addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
              mPreTimeTextField.getRootPane().dispatchEvent(e);
          }
        });
        
        panel.add(mPreTimeTextField, fc);

        panel.add(new JLabel(mLocalizer.msg("Later", "Number of minutes to stop later")),lc);

        value = mData.getPostTime();

        mPostTimeTextField = new JSpinner(new SpinnerNumberModel(value, min, null, step));

        mPostTimeTextField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                mData.setPostTime((Integer) mPostTimeTextField.getValue());
            }
            
        });

        // Dispache the KeyEvent to the RootPane for Closing the Dialog.
        // Needed for Java 1.4.
        mPostTimeTextField.getEditor().getComponent(0).addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
              mPostTimeTextField.getRootPane().dispatchEvent(e);
          }
        });
        
        panel.add(mPostTimeTextField, fc);
        
        return panel;
    }

}