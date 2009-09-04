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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TimeZone;

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

import util.ui.Localizer;
import util.ui.ScrollableJPanel;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * Creates the Settings-Panel
 * 
 * @author bodum
 */
public class SettingsPanel extends ScrollableJPanel implements ActionListener, ChangeListener {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(SettingsPanel.class);

    /** GUI */
    private JSpinner mPreTimeSpinner;

    private JSpinner mPostTimeTextField;
    
    private JTextField mUserName = new JTextField();
    private JPasswordField mUserPwd = new JPasswordField();
    
    
    private JSpinner mMaxTimeout;
    
    private JSpinner mMaxSimult;
    
    
    /** Settings */
    private DeviceConfig mData;
    
    private JCheckBox mCheckReturn, mShowOnError, mShowTitleAndTimeDialog, mOldPrograms,
                      mUseTime, mDeleteRemovedPrograms;
    
    private JComboBox mTimeZones;

    private JLabel mTimeZoneLabel;

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
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,12dlu,pref:grow,5dlu,pref:grow,5dlu",
      "pref,5dlu,pref,1dlu,pref,10dlu,pref,5dlu,pref,1dlu,"+
      "pref,10dlu,pref,5dlu,pref,1dlu,pref,7dlu,pref,pref," +
      "pref,pref,pref,7dlu,pref,pref"),this);
      pb.setDefaultDialogBorder();
      
      mPreTimeSpinner = new JSpinner(new SpinnerNumberModel(mData.getPreTime(), 0, null, 1));
      mPostTimeTextField = new JSpinner(new SpinnerNumberModel(mData.getPostTime(), 0, null, 1));
      
      mUserName.setText(mData.getUserName());
      mUserPwd.setText(mData.getPassword());
      
      mMaxSimult = new JSpinner(new SpinnerNumberModel(mData.getMaxSimultanious(), 1, null, 1));
      mMaxTimeout = new JSpinner(new SpinnerNumberModel(mData.getTimeOut(), -1, 999, 1));

      mCheckReturn = new JCheckBox(mLocalizer.msg("CheckError", "Check if returns Error"), mData.useReturnValue());
      mShowOnError = new JCheckBox(mLocalizer.msg("ShowResultOnError","Show Result-Dialog only on Error"), mData.getDialogOnlyOnError());
      mShowTitleAndTimeDialog = new JCheckBox(mLocalizer.msg("showTitleAndTime", "Show title and time settings dialog"), mData.getShowTitleAndTimeDialog());
      mDeleteRemovedPrograms = new JCheckBox(mLocalizer.msg("autoDeletePrograms", "Automatically delete programs that were removed during a data update"), mData.getDeleteRemovedPrograms());
      mOldPrograms = new JCheckBox(mLocalizer.msg("OnlyFuture", "Only allow Programs that are in the future"), mData.getOnlyFuturePrograms());
      
      mUseTime = new JCheckBox(mLocalizer.msg("useSystemTimezone","Use timezone provided by OS"), !mData.useTimeZone());
      
      String[] zoneIds = new String[0];
      try {
        zoneIds = TimeZone.getAvailableIDs();
      } catch (Exception e) {
        e.printStackTrace();
      }
      mTimeZones = new JComboBox(zoneIds);
      mTimeZones.setEnabled(mData.useTimeZone() && mTimeZones.getItemCount() > 0);
      
      for (int i=0; i<zoneIds.length; i++) {
        if (zoneIds[i].equals(mData.getTimeZone().getID())) {
          mTimeZones.setSelectedIndex(i); break;
        }
      }
      
      pb.addSeparator(mLocalizer.msg("TimeSettings", "Timesettings"), cc.xyw(1,1,6));
      
      pb.addLabel(mLocalizer.msg("Earlier", "Number of minutes to start erlier"),cc.xyw(2,3,2));
      pb.add(mPreTimeSpinner, cc.xy(5,3));
      
      pb.addLabel(mLocalizer.msg("Later", "Number of minutes to stop later"),cc.xyw(2,5,2));
      pb.add(mPostTimeTextField, cc.xy(5,5));
      
      pb.addSeparator(mLocalizer.msg("User", "User"), cc.xyw(1,7,6));
      
      pb.addLabel(mLocalizer.msg("Username", "Username") + ":", cc.xyw(2,9,2));
      pb.add(mUserName, cc.xy(5,9));
      
      pb.addLabel(mLocalizer.msg("Password", "Password") + ":", cc.xyw(2,11,2));
      pb.add(mUserPwd, cc.xy(5,11));
            
      pb.addSeparator(mLocalizer.msg("Additional", "Additional"), cc.xyw(1,13,6));

      pb.addLabel(mLocalizer.msg("MaxSimult","Maximum simultaneous recordings")+ ":" , cc.xyw(2,15,2));
      pb.add(mMaxSimult,cc.xy(5,15));
      
      pb.addLabel(mLocalizer.msg("Timeout","Wait sec. until Timeout (-1 = disabled)")+ ":", cc.xyw(2,17,2));
      pb.add(mMaxTimeout,cc.xy(5,17));

      pb.add(mCheckReturn, cc.xyw(2,19,4));
      pb.add(mShowOnError, cc.xyw(2,20,4));
      pb.add(mShowTitleAndTimeDialog, cc.xyw(2,21,4));
      pb.add(mDeleteRemovedPrograms, cc.xyw(2,22,4));
      pb.add(mOldPrograms, cc.xyw(2,23,4));
      
      pb.add(mUseTime, cc.xyw(2,25,4));
      
      JPanel timeZonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
      mTimeZoneLabel = new JLabel(mLocalizer.msg("Timezone","Timezone")+": ");
      mTimeZoneLabel.setEnabled(mTimeZones.isEnabled());
      timeZonePanel.add(mTimeZoneLabel);
      timeZonePanel.add(mTimeZones);
      
      pb.add(timeZonePanel, cc.xyw(3,26,3));
      
      // add ChangeListener to the spinners
      mPreTimeSpinner.addChangeListener(this);
      mPostTimeTextField.addChangeListener(this);
      mMaxSimult.addChangeListener(this);
      mMaxTimeout.addChangeListener(this);
      
      // add ActionListener to the check boxes
      mCheckReturn.addActionListener(this);
      mShowOnError.addActionListener(this);
      mShowTitleAndTimeDialog.addActionListener(this);
      mDeleteRemovedPrograms.addActionListener(this);
      mOldPrograms.addActionListener(this);
      mUseTime.addActionListener(this);
      
      mUserName.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setUserName(mUserName.getText());
        }
      });
      
      mUserPwd.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setPassword(new String(mUserPwd.getPassword()));
        }
      });
    
      mTimeZones.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mData.setTimeZone(TimeZone.getTimeZone((String)mTimeZones.getSelectedItem()));
        }
      });
    }

    public void actionPerformed(ActionEvent e) {
      if(e.getSource().equals(mCheckReturn)) {
        mData.setUseReturnValue(mCheckReturn.isSelected());
      } else if(e.getSource().equals(mShowOnError)) {
        mData.setDialogOnlyOnError(mShowOnError.isSelected());
      } else if(e.getSource().equals(mShowTitleAndTimeDialog)) {
        mData.setShowTitleAndTimeDialog(mShowTitleAndTimeDialog.isSelected());
      } else if(e.getSource().equals(mDeleteRemovedPrograms)) {
        mData.setDeleteRemovedPrograms(mDeleteRemovedPrograms.isSelected());
      } else if(e.getSource().equals(mOldPrograms)) {
        mData.setOnlyFuturePrograms(mOldPrograms.isSelected());
      } else if(e.getSource().equals(mUseTime)) {
        mData.setUseTimeZone(!mUseTime.isSelected());
        mTimeZones.setEnabled(!mUseTime.isSelected());
        mTimeZoneLabel.setEnabled(mTimeZones.isEnabled());
      }
      
    }

    public void stateChanged(ChangeEvent e) {
      if(e.getSource().equals(mMaxSimult)) {
        mData.setMaxSimultanious((Integer) mMaxSimult.getValue());
      } else if(e.getSource().equals(mMaxTimeout)) {
        mData.setTimeOut((Integer) mMaxTimeout.getValue());
      } else if(e.getSource().equals(mPreTimeSpinner)) {
        mData.setPreTime((Integer) mPreTimeSpinner.getValue());
      } else if(e.getSource().equals(mPostTimeTextField)) {
        mData.setPostTime((Integer) mPostTimeTextField.getValue());
      }
    }

}