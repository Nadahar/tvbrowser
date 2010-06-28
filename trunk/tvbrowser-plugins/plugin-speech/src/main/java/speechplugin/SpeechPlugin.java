/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package speechplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import speechplugin.engine.AbstractSpeechEngine;
import speechplugin.engine.CommandLineSpeechEngine;
import speechplugin.engine.JSAPISpeechEngine;
import speechplugin.engine.MacSpeechEngine;
import speechplugin.engine.SAPISpeechEngine;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author Bananeweizen
 *
 */
public final class SpeechPlugin extends Plugin {

  private static Version mVersion = new Version(2, 70);

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SpeechPlugin.class);

  private static final String TARGET_SPEAK_TITLE = "speakTitle";

  private static final String TARGET_SPEAK_DESCRIPTION = "speakDescription";

  public static Version getVersion() {
    return mVersion;
  }

  private static SpeechPlugin mInstance;

  private SpeechPluginSettings mSettings;

  private AbstractSpeechEngine mEngine;

  private String mVoice = null;

  private LocalPluginProgramFormating[] mLocalFormattings;

  private AbstractPluginProgramFormating[] mConfigs;

  private static LocalPluginProgramFormating FORMATTING_TITLE = new LocalPluginProgramFormating(
      mLocalizer.msg("format.title", "SpeechPlugin - Title"),
      ProgramFieldType.TITLE_TYPE.getLocalizedName(), "{title}", "UTF-8");
  private static LocalPluginProgramFormating FORMATTING_DESCRIPTION = new LocalPluginProgramFormating(
      mLocalizer.msg("format.description", "SpeechPlugin - Short description"),
      ProgramFieldType.SHORT_DESCRIPTION_TYPE.getLocalizedName(),
      "{short_info}", "UTF-8");

  public PluginInfo getInfo() {
    return new PluginInfo(SpeechPlugin.class, mLocalizer.msg("name", "Speech"),
        mLocalizer.msg("description", "Speaks program text"),
        "Michael Keppler", "GPL 3");
  }

  /**
   * Creates an instance of this class.
   */
  public SpeechPlugin() {
    mInstance = this;
    createDefaultConfig();
    createDefaultAvailable();
  }

  /**
   * Gets the instance of this plugin.
   *
   * @return The instance of this plugin.
   */
  public static SpeechPlugin getInstance() {
    if (mInstance == null) {
      new SpeechPlugin();
    }

    return mInstance;
  }

  public void loadSettings(final Properties settings) {
    if (settings == null) {
      mSettings = new SpeechPluginSettings(new Properties());
    } else {
      mSettings = new SpeechPluginSettings(settings);
    }
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  @Override
  public void onDeactivation() {
    if (mEngine != null) {
      mEngine.stopSpeaking();
      mEngine.shutdown();
    }
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    for (Program program : programArr) {
      if (receiveTarget.getTargetId().equals(TARGET_SPEAK_TITLE)) {
        speak(program.getTitle());
      }
      if (receiveTarget.getTargetId().equals(TARGET_SPEAK_DESCRIPTION)) {
        speak(program.getDescription());
      }
    }
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] {
        new ProgramReceiveTarget(this, mLocalizer.msg("speakTitle",
            "Speak title"), TARGET_SPEAK_TITLE),
        new ProgramReceiveTarget(this, mLocalizer.msg("speakDescription",
            "Speak description"), TARGET_SPEAK_DESCRIPTION) };
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new SpeechPluginSettingsTab(mSettings);
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    final ArrayList<Action> actions = new ArrayList<Action>();

    final ParamParser parser = new ParamParser();

    // add all selected formattings
    final AbstractPluginProgramFormating[] formattings = getSelectedPluginProgramFormattings();
    for (AbstractPluginProgramFormating formatting : formattings) {
      final String content = parser.analyse(formatting.getContentValue(),
          program);
      if (content != null && content.length() > 0) {
        actions.add(new AbstractAction(formatting.getTitleValue()) {
          public void actionPerformed(final ActionEvent e) {
            speak(content);
          }
        });
      }
    }

    // if the engine supports state awareness, we can also stop speaking
    if (mEngine != null && mEngine.isSpeaking()) {
      actions.add(ContextMenuSeparatorAction.getInstance());
      actions.add(new AbstractAction(mLocalizer.msg("stop", "Stop speech")) {
        public void actionPerformed(final ActionEvent e) {
          stopSpeaking();
        }
      });
    }

    // no cascaded menu for single action
    if (actions.size() == 1) {
      final Action action = actions.get(0);
      action.putValue(Action.NAME, mLocalizer.msg("contextMenu", "Speak"));
      return new ActionMenu(action);
    }

    // build cascaded menu
    final Action[] result = new Action[actions.size()];
    actions.toArray(result);
    final Action contextMenuAction = new ContextMenuAction(mLocalizer.msg(
        "contextMenu", "Speak"));
    return new ActionMenu(contextMenuAction, result);
  }

  List<String> getAvailableVoices() {
    if (mEngine != null) {
      final List<String> result = mEngine.getVoices();
      if (result != null) {
        return result;
      }
    }
    return new ArrayList<String>();
  }

  void speak(final String text) {
    startEngine();
    if (mEngine != null) {
      // switch voice after changed settings
      final String voice = mSettings.getVoice();
      if (mVoice == null || (voice != null && !mVoice.equals(voice))) {
        mVoice = voice;
        mEngine.setVoice(mVoice);
      }
      mEngine.speak(text);
    }
  }

  private void startEngine() {
    startEngine(mSettings.getEngine());
  }

  void startEngine(final int engine) {
    final AbstractSpeechEngine oldEngine = mEngine;
    if (engine == SpeechPluginSettings.ENGINE_JAVA
        && !(mEngine instanceof JSAPISpeechEngine)) {
      mEngine = new JSAPISpeechEngine();
    } else if (engine == SpeechPluginSettings.ENGINE_MICROSOFT
        && !(mEngine instanceof SAPISpeechEngine)) {
      mEngine = new SAPISpeechEngine();
    } else if (engine == SpeechPluginSettings.ENGINE_MAC
        && !(mEngine instanceof MacSpeechEngine)) {
      mEngine = new MacSpeechEngine();
    } else if (engine == SpeechPluginSettings.ENGINE_OTHER
        && !(mEngine instanceof CommandLineSpeechEngine)) {
      final String executable = mSettings.getOtherEngineExecutable();
      final String parameters = mSettings.getOtherEngineParameters();
      if (executable != null && executable.length() > 0) {
        mEngine = new CommandLineSpeechEngine(executable, parameters);
      }
    }
    if (mEngine == null) {
      notConfigured();
    } else {
      // switch engine after changed settings
      if (oldEngine != mEngine) {
        if (oldEngine != null) {
          oldEngine.shutdown();
        }
        mEngine.initialize();
        mVoice = null;
      }
    }
  }

  private void notConfigured() {
    if (JOptionPane
        .showConfirmDialog(
            null,
            mLocalizer
                .msg(
                    "configure.message",
                    "The speech plugin is not yet configured correctly.\nDo you want to open the settings dialog?"),
            mLocalizer.msg("configure.title", "Not configured"),
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      getPluginManager().showSettings(this);
    }
  }

  void stopSpeaking() {
    if (mEngine != null) {
      mEngine.stopSpeaking();
    }
  }

  static LocalPluginProgramFormating getDefaultFormatting() {
    return new LocalPluginProgramFormating(mLocalizer.msg("defaultFormatName",
        "SpeechPlugin - Default"), ProgramFieldType.TITLE_TYPE
        .getLocalizedName(), "{title}", "UTF-8");
  }

  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version

    if (mConfigs != null) {
      final ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for (AbstractPluginProgramFormating config : mConfigs) {
        if (config != null) {
          list.add(config);
        }
      }

      out.writeInt(list.size());

      for (AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    } else {
      out.writeInt(0);
    }

    if (mLocalFormattings != null) {
      final ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for (AbstractPluginProgramFormating config : mLocalFormattings) {
        if (config != null) {
          list.add(config);
        }
      }

      out.writeInt(list.size());

      for (AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    }

  }

  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    try {
      in.readInt();

      final int n = in.readInt();

      final ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for (int i = 0; i < n; i++) {
        final AbstractPluginProgramFormating value = AbstractPluginProgramFormating
            .readData(in);

        if (value != null) {
          if (value.equals(FORMATTING_TITLE)) {
            FORMATTING_TITLE = (LocalPluginProgramFormating) value;
          }

          list.add(value);
        }
      }

      mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);

      mLocalFormattings = new LocalPluginProgramFormating[in.readInt()];

      for (int i = 0; i < mLocalFormattings.length; i++) {
        final LocalPluginProgramFormating value = (LocalPluginProgramFormating) AbstractPluginProgramFormating
            .readData(in);
        final LocalPluginProgramFormating loadedInstance = getInstanceOfFormattingFromSelected(value);

        mLocalFormattings[i] = loadedInstance == null ? value : loadedInstance;
      }
    } catch (Exception e) {
      e.printStackTrace();
      // Empty
    }
  }

  private LocalPluginProgramFormating getInstanceOfFormattingFromSelected(
      final LocalPluginProgramFormating value) {
    for (AbstractPluginProgramFormating config : mConfigs) {
      if (config.equals(value)) {
        return (LocalPluginProgramFormating) config;
      }
    }

    return null;
  }

  static LocalPluginProgramFormating getDefaultFormating() {
    return FORMATTING_TITLE;
  }

  LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormattings() {
    return mLocalFormattings;
  }

  void setAvailableLocalPluginProgramFormattings(
      final LocalPluginProgramFormating[] value) {
    if (value == null || value.length < 1) {
      createDefaultAvailable();
    } else {
      mLocalFormattings = value;
    }
  }

  AbstractPluginProgramFormating[] getSelectedPluginProgramFormattings() {
    return mConfigs;
  }

  void setSelectedPluginProgramFormatings(
      final AbstractPluginProgramFormating[] value) {
    if (value == null || value.length < 1) {
      createDefaultConfig();
    } else {
      mConfigs = value;
    }
  }

  private void createDefaultConfig() {
    mConfigs = new AbstractPluginProgramFormating[1];
    mConfigs[0] = FORMATTING_TITLE;
  }

  private void createDefaultAvailable() {
    mLocalFormattings = new LocalPluginProgramFormating[2];
    mLocalFormattings[0] = FORMATTING_TITLE;
    mLocalFormattings[1] = FORMATTING_DESCRIPTION;
  }
}
