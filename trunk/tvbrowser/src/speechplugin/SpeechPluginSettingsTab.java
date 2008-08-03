/*
 * SpeechPlugin for TV-Browser
 * Copyright Michael Keppler
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
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package speechplugin;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import tvbrowser.ui.mainframe.MainFrame;
import util.misc.OperatingSystem;
import util.ui.ExecuteSettingsDialog;
import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class SpeechPluginSettingsTab implements SettingsTab {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SpeechPluginSettingsTab.class);

  private JList mVoicesList;

  private SpeechPluginSettings mSettings;

  private JRadioButton mEngineJava;

  private JRadioButton mEngineMicrosoft;

  private JRadioButton mEngineMac;

  private JRadioButton mEngineOther;

  private JButton mExecFileDialogBtn;

  private String mExecutable;

  private String mParameters;

  private JPanel mPanel;

  private PluginProgramConfigurationPanel mConfigPanel;

  public SpeechPluginSettingsTab(SpeechPluginSettings settings) {
    mSettings = settings;
    mExecutable = mSettings.getOtherEngineExecutable();
    mParameters = mSettings.getOtherEngineParameters();
  }

  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,fill:default:grow",
        "2dlu,pref,2dlu,pref,2dlu,50dlu,2dlu,pref,2dlu,fill:default:grow"));
    CellConstraints cc = new CellConstraints();

    // speech engines

    int engine = mSettings.getEngine();
    ButtonGroup bg = new ButtonGroup();
    mEngineMicrosoft = new JRadioButton(mLocalizer.msg("engine.Microsoft",
        "SAPI compatible speech engine (Windows only)"),
        engine == SpeechPluginSettings.ENGINE_MICROSOFT);
    bg.add(mEngineMicrosoft);
    mEngineJava = new JRadioButton(mLocalizer.msg("engine.Java",
        "JSAPI compatible speech engine"),
        engine == SpeechPluginSettings.ENGINE_JAVA);
    bg.add(mEngineJava);
    mEngineMac = new JRadioButton(mLocalizer.msg("engine.Mac",
        "'say' command line (MacOS only)"),
        engine == SpeechPluginSettings.ENGINE_MAC);
    bg.add(mEngineMac);
    mEngineOther = new JRadioButton(mLocalizer.msg("engine.other",
        "other command line"), engine == SpeechPluginSettings.ENGINE_OTHER);
    bg.add(mEngineOther);
    mExecFileDialogBtn = new JButton(Localizer
        .getLocalization(Localizer.I18N_SETTINGS));
    mExecFileDialogBtn.setEnabled(mEngineOther.isSelected());

    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showAvailableVoices();
        mExecFileDialogBtn.setEnabled(mEngineOther.isSelected());
        if (mEngineOther.isEnabled() && e.getSource().equals(mEngineOther)
            && (mExecutable == null || mExecutable.length() == 0)) {
          showFileSettingsDialog();
        }
      }
    };
    mEngineMicrosoft.addActionListener(listener);
    mEngineJava.addActionListener(listener);
    mEngineMac.addActionListener(listener);
    mEngineOther.addActionListener(listener);

    mExecFileDialogBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showFileSettingsDialog();
      }
    });

    PanelBuilder detailsBuilder = new PanelBuilder(new FormLayout(
        "pref,2dlu,pref,2dlu,default:grow",
        "pref,2dlu,pref,2dlu,pref,2dlu,pref"));
    detailsBuilder.add(mEngineMicrosoft, cc.xyw(1, 1, 1));
    detailsBuilder.add(mEngineJava, cc.xyw(1, 3, 1));
    detailsBuilder.add(mEngineMac, cc.xyw(1, 5, 1));
    detailsBuilder.add(mEngineOther, cc.xyw(1, 7, 1));
    detailsBuilder.add(mExecFileDialogBtn, cc.xyw(3, 7, 1));
    mEngineMicrosoft.setEnabled(OperatingSystem.isWindows());
    mEngineMac.setEnabled(OperatingSystem.isMacOs());

    pb.add(detailsBuilder.getPanel(), cc.xy(2, 2));

    ItemListener execListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mExecFileDialogBtn.setEnabled(mEngineOther.isSelected());
      }
    };

    mEngineJava.addItemListener(execListener);
    mEngineMicrosoft.addItemListener(execListener);
    mEngineMac.addItemListener(execListener);
    mEngineOther.addItemListener(execListener);

    // voices

    List<String> voices = SpeechPlugin.getInstance().getAvailableVoices();
    mVoicesList = new JList(voices.toArray());
    mVoicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 5, 5);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JScrollPane(mVoicesList), c);
    pb.addSeparator(mLocalizer.msg("voices", "Voices"), cc.xyw(1, 4, 2));
    pb.add(panel, cc.xy(2, 6));

    // formattings

    pb.addSeparator(mLocalizer.msg("formattings", "Formattings"), cc.xyw(1, 8,
        2));
    mConfigPanel = new PluginProgramConfigurationPanel(
        SpeechPlugin.getInstance().getSelectedPluginProgramFormattings(),
        SpeechPlugin.getInstance().getAvailableLocalPluginProgramFormattings(),
        SpeechPlugin.getDefaultFormatting(), true, false);
    pb.add(mConfigPanel, cc.xyw(1, 10, 2));

    mPanel = pb.getPanel();

    // fill language box with initial list
    showAvailableVoices();
    return mPanel;
  }

  protected void showFileSettingsDialog() {
    ExecuteSettingsDialog execDialog;

    Window wnd = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    if (wnd instanceof JDialog) {
      execDialog = new ExecuteSettingsDialog((JDialog) wnd, mExecutable,
          mParameters, new SpeechParamLibrary("Test"));
    } else {
      execDialog = new ExecuteSettingsDialog((JFrame) wnd, mExecutable,
          mParameters, new SpeechParamLibrary("Test"));
    }
    execDialog.setVisible(true);
    if (execDialog.wasOKPressed()) {
      mExecutable = execDialog.getExecutable();
      mParameters = execDialog.getParameters();
    }
  }

  public Icon getIcon() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTitle() {
    // ignored since 2.7
    return null;
  }

  /**
   * get currently selected speech engine from radio buttons
   * 
   * @return
   */
  private int getSelectedEngine() {
    int engine = SpeechPluginSettings.ENGINE_NONE;
    if (mEngineMicrosoft.isSelected()) {
      engine = SpeechPluginSettings.ENGINE_MICROSOFT;
    } else if (mEngineJava.isSelected()) {
      engine = SpeechPluginSettings.ENGINE_JAVA;
    } else if (mEngineMac.isSelected()) {
      engine = SpeechPluginSettings.ENGINE_MAC;
    } else if (mEngineOther.isSelected()) {
      engine = SpeechPluginSettings.ENGINE_OTHER;
    }
    return engine;
  }

  public void saveSettings() {
    Object voice = mVoicesList.getSelectedValue();
    if (voice != null) {
      mSettings.setVoice(voice.toString());
    } else {
      mSettings.setVoice("");
    }
    mSettings.setEngine(getSelectedEngine());
    mSettings.setOtherEngineExecutable(mExecutable);
    mSettings.setOtherEngineParameters(mParameters);
    SpeechPlugin.getInstance().setAvailableLocalPluginProgramFormatings(
        mConfigPanel.getAvailableLocalPluginProgramFormatings());
    SpeechPlugin.getInstance().setSelectedPluginProgramFormatings(
        mConfigPanel.getSelectedPluginProgramFormatings());
  }

  /**
   * show available voices for selected engine
   */
  private void showAvailableVoices() {
    Cursor cursor = mPanel.getCursor();
    mPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    SpeechPlugin.getInstance().startEngine(getSelectedEngine());
    List<String> voices = SpeechPlugin.getInstance().getAvailableVoices();
    DefaultListModel model = new DefaultListModel();
    if (voices != null) {
      for (String voice : voices) {
        model.addElement(voice);
      }
    }
    mVoicesList.setModel(model);
    String voice = mSettings.getVoice();
    if (voice != null) {
      int index = voices.indexOf(voice);
      if (index >= 0) {
        mVoicesList.setSelectedIndex(index);
      }
    }
    mPanel.setCursor(cursor);
  }

}
