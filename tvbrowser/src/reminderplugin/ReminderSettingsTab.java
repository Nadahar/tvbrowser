/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package reminderplugin;

import java.util.Properties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import devplugin.*;

import util.ui.*;

/**
 * TV-Browser
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
  private JButton mSoundTestBt;
  private FileCheckBox mExecFileChB;
  private JSpinner mAutoCloseReminderTimeSp;

  
  
  public ReminderSettingsTab(Properties settings) {
    this.mSettings = settings;
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
    String execFName=mSettings.getProperty("execfile","/");

    File soundFile=new File(soundFName);
    File execFile=new File(execFName);

    msg = mLocalizer.msg("playlingSound", "Play sound");
    mSoundFileChB = new FileCheckBox(msg, soundFile, 0);
    msg = mLocalizer.msg("executeProgram", "Execute program");
    mExecFileChB = new FileCheckBox(msg, execFile, 0);

    JFileChooser soundChooser=new JFileChooser("sound/");
    JFileChooser execChooser=new JFileChooser("/");

    String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
    msg = mLocalizer.msg("soundFileFilter", "Sound file ({0})",
      "*.wav, *.aif, *.rmf, *.au, *.mid");
    soundChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));

    mReminderWindowChB.setSelected(mSettings.getProperty("usemsgbox","true").equals("true"));
    mSoundFileChB.setSelected(mSettings.getProperty("usesound","false").equals("true"));
    mExecFileChB.setSelected(mSettings.getProperty("useexec","false").equals("true"));

    mSoundFileChB.setFileChooser(soundChooser);
    mExecFileChB.setFileChooser(execChooser);
    
    JPanel soundPn = new JPanel(new BorderLayout(5, 0));
    soundPn.add(mSoundFileChB, BorderLayout.CENTER);
    msg = mLocalizer.msg("test", "Test");
    mSoundTestBt = new JButton(msg);
    mSoundTestBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ReminderPlugin.playSound(mSoundFileChB.getTextField().getText());
      }
    });
    soundPn.add(mSoundTestBt, BorderLayout.EAST);

    reminderPn.add(soundPn);
    reminderPn.add(mExecFileChB);

    // Auto close time of the reminder frame
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    msg = mLocalizer.msg("autoCloseReminder", "Automatically close reminder after");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    int autoCloseReminderTime = 0;
    try {
      String asString = mSettings.getProperty("autoCloseReminderTime", "0");
      autoCloseReminderTime = Integer.parseInt(asString);
    } catch (Exception exc) {}
    mAutoCloseReminderTimeSp = new JSpinner(new SpinnerNumberModel(autoCloseReminderTime,0,600,1));
    mAutoCloseReminderTimeSp.setBorder(null);
    mAutoCloseReminderTimeSp.setPreferredSize(mAutoCloseReminderTimeSp.getPreferredSize());
    p1.add(mAutoCloseReminderTimeSp);

    p1.add(new JLabel(mLocalizer.msg("seconds", "seconds (0 = off)")));
    
    return mSettingsPn;
  }

  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    mSettings.setProperty("soundfile",mSoundFileChB.getTextField().getText());
    mSettings.setProperty("execfile",mExecFileChB.getTextField().getText());

    mSettings.setProperty("usemsgbox",new Boolean(mReminderWindowChB.isSelected()).toString());
    mSettings.setProperty("usesound",new Boolean(mSoundFileChB.isSelected()).toString());
    mSettings.setProperty("useexec",new Boolean(mExecFileChB.isSelected()).toString());
    
    mSettings.setProperty("autoCloseReminderTime", mAutoCloseReminderTimeSp.getValue().toString());
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    String iconName = "reminderplugin/TipOfTheDay16.gif";
    return ImageUtilities.createImageIconFromJar(iconName, getClass());
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("tabName", "Reminder");
  }

}