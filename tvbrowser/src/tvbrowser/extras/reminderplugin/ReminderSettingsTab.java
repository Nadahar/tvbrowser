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

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

import javax.sound.midi.Sequencer;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ExtensionFileFilter;
import util.ui.FileCheckBox;
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

  private JCheckBox mReminderWindowChB;
  private FileCheckBox mSoundFileChB;
  private JCheckBox mExecChB;
  private JCheckBox mShowTimeSlectionDlg;
  private JButton mExecFileDialogBtn;
  private JSpinner mAutoCloseReminderTimeSp;

  private JCheckBox mSendToPlugin;
  private JComboBox mAvailabePlugins;
  private JComboBox mDefaultReminderEntryList;

  private String mExecFileStr, mExecParamStr;
  private Object mTestSound;

  /**
   * Constructor.
   */
  public ReminderSettingsTab() {
    mSettings = ReminderPlugin.getInstance().getSettings();
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,pref:grow,3dlu,pref,3dlu,pref,5dlu",
        "pref,5dlu,pref,1dlu,pref,1dlu,pref,1dlu,pref,10dlu," +
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu,pref");
    layout.setColumnGroups(new int[][] {{7,9}});
    PanelBuilder pb = new PanelBuilder(layout);
    pb.setDefaultDialogBorder();    
    
    CellConstraints cc = new CellConstraints();
    
    final String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
    String soundFName=mSettings.getProperty("soundfile","/");
    String msg = mLocalizer.msg("soundFileFilter", "Sound file ({0})",
        "*.wav, *.aif, *.rmf, *.au, *.mid");
    
    
    mReminderWindowChB = new JCheckBox(mLocalizer.msg("reminderWindow", "Reminder window"));
    mSoundFileChB = new FileCheckBox(mLocalizer.msg("playlingSound", "Play sound"), new File(soundFName), 0);
    
    JFileChooser soundChooser=new JFileChooser("sound/");
    soundChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));

    mSoundFileChB.setFileChooser(soundChooser);
    
    mReminderWindowChB.setSelected(mSettings.getProperty("usemsgbox","false").equals("true"));
    mSoundFileChB.setSelected(mSettings.getProperty("usesound","false").equals("true"));

    mExecFileStr = mSettings.getProperty("execfile", "");
    mExecParamStr = mSettings.getProperty("execparam", "");    
    
    final JButton soundTestBt = new JButton(mLocalizer.msg("test", "Test"));
    
    mExecChB = new JCheckBox(mLocalizer.msg("executeProgram", "Execute program"));
    mExecChB.setSelected(mSettings.getProperty("useexec","false").equals("true"));
    
    mExecFileDialogBtn = new JButton(mLocalizer.msg("executeConfig", "Configure"));
    mExecFileDialogBtn.setEnabled(mExecChB.isSelected());
    
    mSendToPlugin = new JCheckBox(mLocalizer.msg("sendToPlugin", "Send to Plugin:"));
    mSendToPlugin.setSelected(mSettings.getProperty("usesendplugin","false").equals("true"));
    
    mAvailabePlugins = new JComboBox(getAvailablePlugins());
    mAvailabePlugins.setEnabled(mSendToPlugin.isSelected() && mAvailabePlugins.getItemCount() > 0);

    for (int i = 0; i < mAvailabePlugins.getItemCount(); i++) {
      PluginAccess plugin = (PluginAccess) mAvailabePlugins.getItemAt(i);
      if (plugin.getId().equals(mSettings.getProperty("usethisplugin", ""))) {
        mAvailabePlugins.setSelectedIndex(i);
      }
    }
    
    int autoCloseReminderTime = 0;
    try {
      String asString = mSettings.getProperty("autoCloseReminderTime", "0");
      autoCloseReminderTime = Integer.parseInt(asString);
    } catch (Exception exc) {
      // ignore
    }
    
    mAutoCloseReminderTimeSp = new JSpinner(new SpinnerNumberModel(autoCloseReminderTime,0,600,1));
    
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
    
    mShowTimeSlectionDlg = new JCheckBox(mLocalizer.msg("showTimeSelectionDialog","Show time selection dialog"));    
    mShowTimeSlectionDlg.setSelected(mSettings.getProperty("showTimeSelectionDialog","true").compareTo("true") == 0);
    
    pb.addSeparator(mLocalizer.msg("remindBy", "Remind me by"), cc.xyw(1,1,10));
    pb.add(mReminderWindowChB, cc.xyw(2,3,4));
    pb.add(mSoundFileChB, cc.xyw(2,5,4));
    pb.add(mSoundFileChB.removeButton(), cc.xy(7,5));
    pb.add(soundTestBt, cc.xy(9,5));
    pb.add(mExecChB, cc.xyw(2,7,4));
    pb.add(mExecFileDialogBtn, cc.xyw(7,7,3));
    pb.add(mSendToPlugin, cc.xyw(2,9,4));
    pb.add(mAvailabePlugins, cc.xyw(7,9,3));
    
    pb.addSeparator(mLocalizer.msg("autoCloseReminder", "Automatically close reminder after"), cc.xyw(1,11,10));
    pb.add(mAutoCloseReminderTimeSp, cc.xy(2,13));
    pb.addLabel(mLocalizer.msg("seconds", "seconds (0 = off)"), cc.xy(4,13));
    
    JPanel reminderEntry = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
    reminderEntry.add(mDefaultReminderEntryList);
    
    pb.addSeparator(mLocalizer.msg("defaltReminderEntry","Default reminder time"), cc.xyw(1,15,10));
    pb.add(reminderEntry, cc.xyw(2,17,4));
    
    pb.addSeparator(mLocalizer.msg("miscSettings","Misc settings"), cc.xyw(1,19,10));    
    pb.add(mShowTimeSlectionDlg, cc.xyw(2,21,4));
    
    soundTestBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(evt.getActionCommand().compareTo(mLocalizer.msg("test", "Test")) == 0) {
          mTestSound = ReminderPlugin.playSound(mSoundFileChB.getTextField().getText());
          if(mTestSound != null)
            soundTestBt.setText(mLocalizer.msg("stop", "Stop"));
          if(mTestSound != null)
            if(mTestSound instanceof Clip) {
            ((Clip)mTestSound).addLineListener(new LineListener() {
              public void update(LineEvent event) {
                if(mTestSound == null || !((Clip)mTestSound).isRunning())
                  soundTestBt.setText(mLocalizer.msg("test", "Test"));
              }
            });
            }
            else if(mTestSound instanceof Sequencer) {
              new Thread() {
                public void run() {
                  setPriority(Thread.MIN_PRIORITY);
                  while(((Sequencer)mTestSound).isRunning()) {
                    try {
                      Thread.sleep(100);
                    }catch(Exception ee) {}
                  }
                  
                  soundTestBt.setText(mLocalizer.msg("test", "Test"));
                }
              }.start();
            }
        }
        else if(mTestSound != null)
          if(mTestSound instanceof Clip && ((Clip)mTestSound).isRunning())
            ((Clip)mTestSound).stop();
          else if(mTestSound instanceof Sequencer && ((Sequencer)mTestSound).isRunning())
            ((Sequencer)mTestSound).stop();
      }
    });
    
    mSoundFileChB.getCheckBox().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        soundTestBt.setEnabled(mSoundFileChB.isSelected());
      }
    });
    
    mSoundFileChB.getTextField().addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        String text = mSoundFileChB.getTextField().getText();        
        if((new File(text)).isFile()) {
          boolean notFound = true;
          for(int i = 0; i < extArr.length; i++)
            if(text.toLowerCase().endsWith(extArr[i])) {
              notFound = false;
              break;
            }
          
          if(notFound)
            soundTestBt.setEnabled(false);
          else
            soundTestBt.setEnabled(true);
        }
        else
          soundTestBt.setEnabled(false);
      }
    });
    mSoundFileChB.getTextField().getKeyListeners()[0].keyReleased(null);
    
    mExecChB.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mExecFileDialogBtn.setEnabled(mExecChB.isSelected());
      }
    });
    
    mExecFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showFileSettingsDialog();
      }
    });
    
    mSendToPlugin.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mAvailabePlugins.setEnabled(mSendToPlugin.isSelected()  && mAvailabePlugins.getItemCount() > 0);
      }
    });

    return pb.getPanel();
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

    Window wnd = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    
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

    PluginAccess sendToPlugin = (PluginAccess)mAvailabePlugins.getSelectedItem();
    if (sendToPlugin != null && mSendToPlugin.isSelected()) {
      mSettings.setProperty("usethisplugin", sendToPlugin.getId());
    }
    mSettings.setProperty("autoCloseReminderTime", mAutoCloseReminderTimeSp.getValue().toString());

    mSettings.setProperty("defaultReminderEntry",""+mDefaultReminderEntryList.getSelectedIndex());
    
    mSettings.setProperty("showTimeSelectionDialog", String.valueOf(mShowTimeSlectionDlg.isSelected()));
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