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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.Sequencer;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ExtensionFileFilter;
import util.ui.FileCheckBox;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;


/**
 *
 * @author Martin Oberhauser
 */
public class ReminderSettingsTab implements SettingsTab {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderSettingsTab.class);

  private Properties mSettings;

  private JCheckBox mReminderWindowChB;
  private FileCheckBox mSoundFileChB;
  private JCheckBox mExecChB;
  private JCheckBox mShowTimeSelectionDlg;
  private JCheckBox mShowRemovedDlg;  
  private JCheckBox mShowTimeCounter;
  private JButton mExecFileDialogBtn;
  private JCheckBox mShowAlwaysOnTop;
  private JSpinner mAutoCloseReminderTimeSp;
  private JRadioButton mCloseOnEnd, mCloseNever, mCloseOnTime;
  
  private JComboBox mDefaultReminderEntryList;

  private String mExecFileStr, mExecParamStr;
  private Object mTestSound;

  private JLabel mPluginLabel;
  private ProgramReceiveTarget[] mClientPluginTargets;
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
        "pref,5dlu,pref,1dlu,pref,1dlu,pref,10dlu,pref,5dlu," +
        "pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu," +
        "pref,5dlu,pref,3dlu,pref");
    layout.setColumnGroups(new int[][] {{7,9}});
    PanelBuilder pb = new PanelBuilder(layout, new ScrollabelJPanel());
    pb.setDefaultDialogBorder();    
    
    CellConstraints cc = new CellConstraints();
    
    final String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
    String soundFName=mSettings.getProperty("soundfile","/");
    String msg = mLocalizer.msg("soundFileFilter", "Sound file ({0})",
        "*.wav, *.aif, *.rmf, *.au, *.mid");
    
    
    mReminderWindowChB = new JCheckBox(mLocalizer.msg("reminderWindow", "Reminder window"), mSettings.getProperty("usemsgbox","false").equalsIgnoreCase("true"));
    
    mShowAlwaysOnTop = new JCheckBox(mLocalizer.msg("alwaysOnTop","Show always on top"), mSettings.getProperty("alwaysOnTop","true").equalsIgnoreCase("true"));
    mShowAlwaysOnTop.setEnabled(mReminderWindowChB.isSelected());

    JPanel reminderWindowCfg = new JPanel(new FormLayout("12dlu,default:grow","pref,1dlu,pref"));
    reminderWindowCfg.add(mReminderWindowChB, cc.xyw(1,1,2));
    reminderWindowCfg.add(mShowAlwaysOnTop, cc.xy(2,3));
        
    mSoundFileChB = new FileCheckBox(mLocalizer.msg("playlingSound", "Play sound"), new File(soundFName), 0, false);
    
    JFileChooser soundChooser=new JFileChooser("sound/");
    soundChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));

    mSoundFileChB.setFileChooser(soundChooser);
        
    mSoundFileChB.setSelected(mSettings.getProperty("usesound","false").equals("true"));

    mExecFileStr = mSettings.getProperty("execfile", "");
    mExecParamStr = mSettings.getProperty("execparam", "");    
    
    final JButton soundTestBt = new JButton(mLocalizer.msg("test", "Test"));
    
    mExecChB = new JCheckBox(mLocalizer.msg("executeProgram", "Execute program"));
    mExecChB.setSelected(mSettings.getProperty("useexec","false").equals("true"));
    
    mExecFileDialogBtn = new JButton(mLocalizer.msg("executeConfig", "Configure"));
    mExecFileDialogBtn.setEnabled(mExecChB.isSelected());
    
    mPluginLabel = new JLabel();
    JButton choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));
        
    mClientPluginTargets = ReminderPlugin.getInstance().getClientPluginsTargets();
    
    handlePluginSelection();
    
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {try{
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        PluginChooserDlg chooser = null;
        if(w instanceof JDialog)
          chooser = new PluginChooserDlg((JDialog)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        else
          chooser = new PluginChooserDlg((JFrame)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        
        chooser.setLocationRelativeTo(w);
        chooser.setVisible(true);
        
        if(chooser.getReceiveTargets() != null)
          mClientPluginTargets = chooser.getReceiveTargets();
        
        handlePluginSelection();}catch(Exception ee) {ee.printStackTrace();}
      }
    });
    
    int autoCloseReminderTime = 10;
    try {
      String asString = mSettings.getProperty("autoCloseReminderTime", "10");
      autoCloseReminderTime = Integer.parseInt(asString);      
    } catch (Exception exc) {
      // ignore
    }
    
    mCloseOnEnd = new JRadioButton(mLocalizer.msg("autoCloseReminderAtProgramEnd","Program end"), mSettings.getProperty("autoCloseBehaviour","onEnd").equals("onEnd"));
    mCloseOnEnd.setEnabled(mReminderWindowChB.isSelected());
    
    mCloseNever = new JRadioButton(mLocalizer.msg("autoCloseNever","Never close"), mSettings.getProperty("autoCloseBehaviour","onEnd").equals("never"));
    mCloseNever.setEnabled(mReminderWindowChB.isSelected());
    
    mCloseOnTime = new JRadioButton(mLocalizer.msg("autoCloseAfterTime","After time ..."), mSettings.getProperty("autoCloseBehaviour","onEnd").equals("onTime"));
    mCloseOnTime.setEnabled(mReminderWindowChB.isSelected());
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mCloseOnEnd);
    bg.add(mCloseNever);
    bg.add(mCloseOnTime);
    
    mAutoCloseReminderTimeSp = new JSpinner(new SpinnerNumberModel(autoCloseReminderTime,autoCloseReminderTime < 5 ? 1 : 5,600,1));
    mAutoCloseReminderTimeSp.setEnabled(mCloseOnTime.isSelected() && mReminderWindowChB.isSelected());
    
    mShowTimeCounter = new JCheckBox(mLocalizer.msg("showTimeCounter","Show time counter"),mSettings.getProperty("showTimeCounter","false").compareTo("true") == 0);
    mShowTimeCounter.setEnabled(!mCloseNever.isSelected() && mReminderWindowChB.isSelected());    
    
    PanelBuilder autoClosePanel = new PanelBuilder(new FormLayout("12dlu,default,2dlu,default:grow","pref,2dlu,pref,2dlu,pref,2dlu,pref,10dlu,pref"));
    autoClosePanel.add(mCloseOnEnd, cc.xyw(1,1,4));
    autoClosePanel.add(mCloseNever, cc.xyw(1,3,4));
    autoClosePanel.add(mCloseOnTime, cc.xyw(1,5,4));
    autoClosePanel.add(mAutoCloseReminderTimeSp, cc.xy(2,7));
    
    final JLabel secondsLabel = autoClosePanel.addLabel(mLocalizer.msg("seconds", "seconds (0 = off)"), cc.xy(4,7));
    
    autoClosePanel.add(mShowTimeCounter, cc.xyw(1,9,4));
    
    secondsLabel.setEnabled(mCloseOnTime.isSelected() && mReminderWindowChB.isSelected());
    
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
        
    mShowTimeSelectionDlg = new JCheckBox(mLocalizer.msg("showTimeSelectionDialog","Show time selection dialog"));    
    mShowTimeSelectionDlg.setSelected(mSettings.getProperty("showTimeSelectionDialog","true").compareTo("true") == 0);
    mShowRemovedDlg = new JCheckBox(mLocalizer.msg("showRemovedDialog","Show removed reminders after data update"));    
    mShowRemovedDlg.setSelected(mSettings.getProperty("showRemovedDialog","true").compareTo("true") == 0);
        
    pb.addSeparator(mLocalizer.msg("remindBy", "Remind me by"), cc.xyw(1,1,10));
    
    pb.add(reminderWindowCfg, cc.xyw(2,3,4));
    pb.add(mSoundFileChB, cc.xyw(2,5,4));
    pb.add(mSoundFileChB.getButton(), cc.xy(7,5));
    pb.add(soundTestBt, cc.xy(9,5));
    pb.add(mExecChB, cc.xyw(2,7,4));
    pb.add(mExecFileDialogBtn, cc.xyw(7,7,3));
    
    pb.addSeparator(mLocalizer.msg("sendToPlugin", "Send reminded program to"), cc.xyw(1,9,10));
    
    pb.add(mPluginLabel, cc.xyw(2,11,4));
    pb.add(choose, cc.xyw(7,11,3));
    
    final JLabel c = (JLabel) pb.addSeparator(mLocalizer.msg("autoCloseReminder", "Automatically close reminder"), cc.xyw(1,13,10)).getComponent(0);
    c.setEnabled(mReminderWindowChB.isSelected());
    
    pb.add(autoClosePanel.getPanel(), cc.xyw(2,15,5));
    
    JPanel reminderEntry = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
    reminderEntry.add(mDefaultReminderEntryList);
    
    pb.addSeparator(mLocalizer.msg("defaltReminderEntry","Default reminder time"), cc.xyw(1,17,10));
    pb.add(reminderEntry, cc.xyw(2,19,4));
    
    pb.addSeparator(mLocalizer.msg("miscSettings","Misc settings"), cc.xyw(1,21,10));    
    pb.add(mShowTimeSelectionDlg, cc.xyw(2,23,7));
    pb.add(mShowRemovedDlg, cc.xyw(2,25,7));
    
    mReminderWindowChB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mShowAlwaysOnTop.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        c.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        secondsLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED && mCloseOnTime.isSelected());
        mCloseOnEnd.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mCloseNever.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mCloseOnTime.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mShowTimeCounter.setEnabled(e.getStateChange() == ItemEvent.SELECTED && !mCloseNever.isSelected());
        mAutoCloseReminderTimeSp.setEnabled(e.getStateChange() == ItemEvent.SELECTED && mCloseOnTime.isSelected());
      }
    });
    
    soundTestBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(evt.getActionCommand().compareTo(mLocalizer.msg("test", "Test")) == 0) {
          mTestSound = ReminderPlugin.playSound(mSoundFileChB.getTextField().getText());
          if(mTestSound != null)
            soundTestBt.setText(mLocalizer.msg("stop", "Stop"));
          if(mTestSound != null)
            if(mTestSound instanceof SourceDataLine) {
              ((SourceDataLine)mTestSound).addLineListener(new LineListener() {
                public void update(LineEvent event) {
                  if(event.getType() == Type.CLOSE)
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
          if(mTestSound instanceof SourceDataLine && ((SourceDataLine)mTestSound).isRunning())
            ((SourceDataLine)mTestSound).stop();
          else if(mTestSound instanceof Sequencer && ((Sequencer)mTestSound).isRunning())
            ((Sequencer)mTestSound).stop();
      }
    });
    
    mSoundFileChB.getCheckBox().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
    
    mExecChB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mExecFileDialogBtn.setEnabled(mExecChB.isSelected());
      }
    });
    
    mExecFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showFileSettingsDialog();
      }
    });
    
    ItemListener autoCloseListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mAutoCloseReminderTimeSp.setEnabled(mCloseOnTime.isSelected());
        secondsLabel.setEnabled(mCloseOnTime.isSelected());
        mShowTimeCounter.setEnabled(mCloseOnTime.isSelected() || mCloseOnEnd.isSelected());        
      }
    };
    
    mCloseOnEnd.addItemListener(autoCloseListener);
    mCloseOnTime.addItemListener(autoCloseListener);
    
    mCloseOnTime.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mShowTimeCounter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);        
      }
    });
    
    JScrollPane scrollPane = new JScrollPane(pb.getPanel());
    scrollPane.setBorder(null);
    scrollPane.setViewportBorder(null);
    
    JPanel scrollPanel = new JPanel(new FormLayout("default:grow","default"));
    scrollPanel.add(scrollPane,cc.xy(1,1));
    
    return scrollPanel ;
  }

  private void handlePluginSelection() {
    ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();
    
    if(mClientPluginTargets != null) {
      for(int i = 0; i < mClientPluginTargets.length; i++) {
        if(!plugins.contains(mClientPluginTargets[i].getReceifeIfForIdOfTarget()))
          plugins.add(mClientPluginTargets[i].getReceifeIfForIdOfTarget());
      }
    
      ProgramReceiveIf[] mClientPlugins = plugins.toArray(new ProgramReceiveIf[plugins.size()]);

      if(mClientPlugins.length > 0) {
        mPluginLabel.setText(mClientPlugins[0].toString());
        mPluginLabel.setEnabled(true);
      }
      else {
        mPluginLabel.setText(mLocalizer.msg("noPlugins","No Plugins choosen"));
        mPluginLabel.setEnabled(false);
      }
    
      for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
        mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
      }
    
      if(mClientPlugins.length > 4)
        mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins","others...") + ")");
    }
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

    ReminderPlugin.getInstance().setClientPluginsTargets(mClientPluginTargets);
    
    mSettings.setProperty("autoCloseBehaviour", mCloseOnEnd.isSelected() ? "onEnd" : mCloseNever.isSelected() ? "never" : "onTime");
    
    mSettings.setProperty("autoCloseReminderTime", mAutoCloseReminderTimeSp.getValue().toString());
    mSettings.setProperty("defaultReminderEntry",""+mDefaultReminderEntryList.getSelectedIndex());
    mSettings.setProperty("showTimeSelectionDialog", String.valueOf(mShowTimeSelectionDlg.isSelected()));
    mSettings.setProperty("showRemovedDialog", String.valueOf(mShowRemovedDlg.isSelected()));
    
    mSettings.setProperty("showTimeCounter", String.valueOf(!mCloseNever.isSelected() && mShowTimeCounter.isSelected()));
    mSettings.setProperty("alwaysOnTop", String.valueOf(mShowAlwaysOnTop.isSelected()));    
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
    return mLocalizer.msg("basicSettings", "Basic settings");
  }
  
  private class ScrollabelJPanel extends JPanel implements Scrollable {

    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 50;
    }

    public boolean getScrollableTracksViewportHeight() {
      return false;
    }

    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 20;
    }
    
  }
}