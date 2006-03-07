/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.extras.reminderplugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.ExtensionFileFilter;
import util.ui.FileCheckBox;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.SettingsTab;


/**
 *
 * @author Martin Oberhauser
 */
public class ReminderSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderSettingsTab.class);

  private Properties mSettings;

  private JPanel mSettingsPn;

  private JCheckBox mReminderWindowChB;
  private FileCheckBox mSoundFileChB;
  private JCheckBox mExecChB;
  private JButton mExecFileDialogBtn;
  private JSpinner mAutoCloseReminderTimeSp;

  private JCheckBox mSendToPlugin;
  private JComboBox mAvailabePlugins;
  private JComboBox mDefaultReminderEntryList;

  private String mExecFileStr, mExecParamStr;

 // private ReminderPlugin mPlugin;

  public ReminderSettingsTab() {
    mSettings = ReminderPlugin.getInstance().getSettings();
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1;

    mSettingsPn = new JPanel(new BorderLayout());

    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);

    JPanel reminderPn = new JPanel(new TabLayout(1));
    main.add(reminderPn);

    msg = mLocalizer.msg("remindBy", "Remind me by");
    reminderPn.setBorder(BorderFactory.createTitledBorder(msg));

    msg = mLocalizer.msg("reminderWindow", "Reminder window");
    mReminderWindowChB = new JCheckBox(msg);
    reminderPn.add(mReminderWindowChB);

    String soundFName=mSettings.getProperty("soundfile","/");

    File soundFile=new File(soundFName);

    msg = mLocalizer.msg("playlingSound", "Play sound");
    mSoundFileChB = new FileCheckBox(msg, soundFile, 0);

    msg = mLocalizer.msg("executeProgram", "Execute program");
    mExecChB = new JCheckBox(msg);

    msg = mLocalizer.msg("executeConfig", "Configure");
    mExecFileDialogBtn = new JButton(msg);

    mExecFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showFileSettingsDialog();
      }
    });

    mExecChB.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        mExecFileDialogBtn.setEnabled(mExecChB.isSelected());
      }

    });

    JFileChooser soundChooser=new JFileChooser("sound/");

    String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
    msg = mLocalizer.msg("soundFileFilter", "Sound file ({0})",
        "*.wav, *.aif, *.rmf, *.au, *.mid");
    soundChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));

    mReminderWindowChB.setSelected(mSettings.getProperty("usemsgbox","false").equals("true"));
    mSoundFileChB.setSelected(mSettings.getProperty("usesound","false").equals("true"));
    mExecChB.setSelected(mSettings.getProperty("useexec","false").equals("true"));
    mExecFileDialogBtn.setEnabled(mExecChB.isSelected());

    mExecFileStr = mSettings.getProperty("execfile", "");
    mExecParamStr = mSettings.getProperty("execparam", "");

    mSoundFileChB.setFileChooser(soundChooser);

    JPanel soundPn = new JPanel(new BorderLayout(5, 0));
    soundPn.add(mSoundFileChB, BorderLayout.CENTER);
    msg = mLocalizer.msg("test", "Test");
    final JButton soundTestBt = new JButton(msg);
    soundTestBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ReminderPlugin.playSound(mSoundFileChB.getTextField().getText());
      }
    });
    soundPn.add(soundTestBt, BorderLayout.EAST);

    reminderPn.add(soundPn);

    JPanel execPanel = new JPanel(new BorderLayout());

    execPanel.add(mExecChB, BorderLayout.WEST);
    execPanel.add(mExecFileDialogBtn, BorderLayout.EAST);

    reminderPn.add(execPanel);

    JPanel pluginPn = new JPanel(new BorderLayout(5, 0));

    mSendToPlugin = new JCheckBox(mLocalizer.msg("sendToPlugin", "Send to Plugin:"));
    mAvailabePlugins = new JComboBox(getAvailablePlugins());
    mSendToPlugin.setSelected(mSettings.getProperty("usesendplugin","false").equals("true"));
    mAvailabePlugins.setEnabled(mSendToPlugin.isSelected());

    for (int i = 0; i < mAvailabePlugins.getItemCount(); i++) {
      PluginAccess plugin = (PluginAccess) mAvailabePlugins.getItemAt(i);
      if (plugin.getId().equals(mSettings.getProperty("usethisplugin", ""))) {
        mAvailabePlugins.setSelectedIndex(i);
      }
    }

    pluginPn.add(mSendToPlugin, BorderLayout.WEST);

    mSendToPlugin.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mAvailabePlugins.setEnabled(mSendToPlugin.isSelected());
      }
    });

    pluginPn.add(mAvailabePlugins, BorderLayout.CENTER);

    reminderPn.add(pluginPn);

    // Auto close time of the reminder frame
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    msg = mLocalizer.msg("autoCloseReminder", "Automatically close reminder after");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);

    int autoCloseReminderTime = 0;
    try {
      String asString = mSettings.getProperty("autoCloseReminderTime", "0");
      autoCloseReminderTime = Integer.parseInt(asString);
    } catch (Exception exc) {
      // ignore
    }
    mAutoCloseReminderTimeSp = new JSpinner(new SpinnerNumberModel(autoCloseReminderTime,0,600,1));
    mAutoCloseReminderTimeSp.setBorder(null);
    mAutoCloseReminderTimeSp.setPreferredSize(mAutoCloseReminderTimeSp.getPreferredSize());
    p1.add(mAutoCloseReminderTimeSp);

    p1.add(new JLabel(mLocalizer.msg("seconds", "seconds (0 = off)")));


    JPanel reminderMinutesPn = new JPanel(new FlowLayout(FlowLayout.LEADING));
    reminderMinutesPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("defaltReminderEntry","Default reminder time")));

    String defaultReminderEntryStr = (String)mSettings.get("defaultReminderEntry");
    mDefaultReminderEntryList =new JComboBox(ReminderDialog.SMALL_REMIND_MSG_ARR);
    if (defaultReminderEntryStr != null) {
      try {
        int inx = Integer.parseInt(defaultReminderEntryStr);
        if (inx < ReminderDialog.SMALL_REMIND_MSG_ARR.length) {
          mDefaultReminderEntryList.setSelectedIndex(inx);
        }
      }catch(NumberFormatException e) {
        // ignore
      }
    }
    reminderMinutesPn.add(mDefaultReminderEntryList);

    main.add(reminderMinutesPn);

    return mSettingsPn;
  }



  /**
   * Gets the Availabe Plugins who can receive Programs
   * @return available Plugins 
   */
  private Vector getAvailablePlugins() {
    // get the installed plugins
    PluginAccess[] installedPluginArr = Plugin.getPluginManager().getActivatedPlugins();
    PluginAccess[] copy = new PluginAccess[installedPluginArr.length];

    System.arraycopy(installedPluginArr, 0, copy, 0, installedPluginArr.length);

    Arrays.sort(copy, new ObjectComperator());

    // create a list of those who support multiple program execution
    Vector selectablePluginList = new Vector();
    for (int i = 0; i < copy.length; i++) {
      if (copy[i].canReceivePrograms()) {
        selectablePluginList.add(copy[i]);
      }
    }

    return selectablePluginList;
  }

  /**
   * Shows the Settings-Dialog for the Executable
   */
  private void showFileSettingsDialog() {
    ExecuteSettingsDialog execSettingsDialog;

    Window wnd = UiUtilities.getBestDialogParent(mSettingsPn);

    if (wnd instanceof JDialog) {
      execSettingsDialog = new ExecuteSettingsDialog((JDialog) wnd, mExecFileStr, mExecParamStr);
    } else {
      execSettingsDialog = new ExecuteSettingsDialog((JFrame) wnd, mExecFileStr, mExecParamStr);
    }

    execSettingsDialog.setVisible(true);

    if (execSettingsDialog.wasOKPressed()) {
      mExecFileStr = execSettingsDialog.getExecutable();
      mExecParamStr = execSettingsDialog.getParameters();
    }

  }


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    mSettings.setProperty("soundfile",mSoundFileChB.getTextField().getText());
    mSettings.setProperty("execfile",mExecFileStr);
    mSettings.setProperty("execparam",mExecParamStr);

    mSettings.setProperty("usemsgbox", Boolean.valueOf(mReminderWindowChB.isSelected()).toString());
    mSettings.setProperty("usesound", Boolean.valueOf(mSoundFileChB.isSelected()).toString());
    mSettings.setProperty("useexec", Boolean.valueOf(mExecChB.isSelected()).toString());

    mSettings.setProperty("usesendplugin", Boolean.valueOf(mSendToPlugin.isSelected()).toString());
    mSettings.setProperty("usethisplugin", ((PluginAccess)mAvailabePlugins.getSelectedItem()).getId());

    mSettings.setProperty("autoCloseReminderTime", mAutoCloseReminderTimeSp.getValue().toString());

    mSettings.setProperty("defaultReminderEntry",""+mDefaultReminderEntryList.getSelectedIndex());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("tabName", "Reminder");
  }

  /**
   * Comperator needed to Sort List of Plugins
   */
  private class ObjectComperator implements Comparator {

    public int compare(Object o1, Object o2) {
      return o1.toString().compareTo(o2.toString());
    }

  }
}