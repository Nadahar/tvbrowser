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
 *     $Date: 2008-04-05 21:36:41 +0200 (Sa, 05 Apr 2008) $
 *   $Author: Bananeweizen $
 * $Revision: 4469 $
 */
package speechplugin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author Bananeweizen
 * 
 */
public class SpeechPlugin extends Plugin {

  private static Version mVersion = new Version(2, 70);

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SpeechPlugin.class);

  private static final java.util.logging.Logger mLog = java.util.logging.Logger
  .getLogger(SpeechPlugin.class.getName());

  private static final String TARGET_SPEAK_TITLE = "speakTitle";

  private static final String TARGET_SPEAK_DESCRIPTION = "speakDescription";

  public static Version getVersion() {
    return mVersion;
  }

  private static SpeechPlugin mInstance;

  private Synthesizer mSynthesizer;

  private SpeechPluginSettings mSettings;

  private Thread mThread;
  
  public PluginInfo getInfo() {
    return new PluginInfo(SpeechPlugin.class, mLocalizer.msg("name", "Speech"),
        mLocalizer.msg("description", "Speaks program text"), "Michael Keppler");
  }

  /**
   * Creates an instance of this class.
   */
  public SpeechPlugin() {
    mInstance = this;
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

  public void loadSettings(Properties settings) {
    if (settings == null) {
      mSettings = new SpeechPluginSettings(new Properties());
    } else {
      mSettings = new SpeechPluginSettings(settings);
    }
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  /**
   * Returns a "no synthesizer" message, and asks the user to check if the
   * "speech.properties" file is at <code>user.home</code> or
   * <code>java.home/lib</code>.
   * 
   * @return a no synthesizer message
   */
  static private String noSynthesizerMessage() {
    String message = "No synthesizer created.  This may be the result of any\n"
        + "number of problems.  It's typically due to a missing\n"
        + "\"speech.properties\" file that should be at either of\n"
        + "these locations: \n\n";
    message += "user.home    : " + System.getProperty("user.home") + "\n";
    message += "java.home/lib: " + System.getProperty("java.home")
        + File.separator + "lib\n\n"
        + "Another cause of this problem might be corrupt or missing\n"
        + "voice jar files in the freetts lib directory.  This problem\n"
        + "also sometimes arises when the freetts.jar file is corrupt\n"
        + "or missing.  Sorry about that.  Please check for these\n"
        + "various conditions and then try again.\n";
    return message;
  }

  /**
   * Example of how to list all the known voices for a specific mode using just
   * JSAPI. FreeTTS maps the domain name to the JSAPI mode name. The currently
   * supported domains are "general," which means general purpose synthesis for
   * tasks such as reading e-mail, and "time" which means a domain that's only
   * good for speaking the time of day.
   */
  public static List<String> getAvailableVoices(String modeName) {
    ArrayList<String> result = new ArrayList<String>();
    
    // get all synthesizers without any restrictions (locale, domain, voice etc.)
    SynthesizerModeDesc unspecifiedSynthesizer = new SynthesizerModeDesc();
    EngineList engineList = Central.availableSynthesizers(unspecifiedSynthesizer);
    
    // get all voices
    for (int i = 0; i < engineList.size(); i++) {
      SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
      if (desc.getModeName().equals(modeName)) {
        Voice[] voices = desc.getVoices();
        for (Voice voice : voices) {
          result.add(voice.getName());
        }
      }
    }
    return result;
  }
  
  private void startSynthesizer(String voiceName) {
    if (mSynthesizer != null) {
      return;
    }
    try {
      Voice selectedVoice = null;
      SynthesizerModeDesc unspecifiedSynthesizer = new SynthesizerModeDesc();
      EngineList engineList = Central.availableSynthesizers(unspecifiedSynthesizer);
      for (int i = 0; i < engineList.size(); i++) {
        SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
        Voice[] voices = desc.getVoices();
        for (Voice voice : voices) {
          if (voice.getName().equals(voiceName)) {
            mSynthesizer = Central.createSynthesizer(desc);
            selectedVoice = voice;
            break;
          }
        }
      }

      /*
       * Just an informational message to guide users that didn't set up their
       * speech.properties file.
       */
      if (mSynthesizer == null) {
        mLog.severe(noSynthesizerMessage());
        return;
      }

      // get synthesizer in ready state
      mSynthesizer.allocate();
      mSynthesizer.resume();

      if (selectedVoice == null) {
        System.err.println("Synthesizer does not have a voice named "
            + voiceName + ".");
      }
      mSynthesizer.getSynthesizerProperties().setVoice(selectedVoice);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void deallocateSynthesizer() {
    if (mSynthesizer == null) {
      return;
    }
    stopSpeech();
    try {
      mSynthesizer.deallocate();
    } catch (EngineException e) {
      e.printStackTrace();
    } catch (EngineStateError e) {
      e.printStackTrace();
    }
  }
  
  private void speak(final String text) {
    if (text == null) {
      return;
    }
    
    // if synthesizer is still running, stop it first
    if (mThread != null) {
      mThread.interrupt();
    }
    final String voice = mSettings.getVoice();
    if (voice == null) {
      return;
    }
    
    // run synthesizer in new thread to avoid GUI blocking
    mThread = new Thread("Speech") {
      @Override
      public void run() {
        startSynthesizer(voice);
        mSynthesizer.speakPlainText(text, null);
        try {
          mSynthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          mSynthesizer.cancel();
        }
        mThread = null;
      }
    };
    mThread.start();
  }

  @Override
  public void onDeactivation() {
    deallocateSynthesizer();
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
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
        new ProgramReceiveTarget(this, mLocalizer.msg("speakTitle", "Speak title"), TARGET_SPEAK_TITLE),
        new ProgramReceiveTarget(this, mLocalizer.msg("speakDescription", "Speak description"), TARGET_SPEAK_DESCRIPTION)
    };
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new SpeechPluginSettingsTab(mSettings);
  }

  @Override
  public ActionMenu getButtonAction() {
    // TODO Auto-generated method stub
    return super.getButtonAction();
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    Action contextMenuAction = new ContextMenuAction(mLocalizer.msg("contextMenu", "Speak"));
    ArrayList<Action> actions = new ArrayList<Action>();
    actions.add(new AbstractAction(mLocalizer.msg("speakTitle", "Speak title")) {
      public void actionPerformed(ActionEvent e) {
        speak(program.getTitle());
      }});

    final String description = program.getDescription();
    if (description != null && description.length() > 0) {
    actions.add(new AbstractAction(mLocalizer.msg("speakDescription", "Speak description")) {
      public void actionPerformed(ActionEvent e) {
        speak(description);
      }});
    }
    
    if (mThread != null) {
      actions.add(ContextMenuSeparatorAction.getInstance());
      actions.add(new AbstractAction(mLocalizer.msg("stop", "Stop speech")) {
        public void actionPerformed(ActionEvent e) {
          stopSpeech();
        }});
    }
    Action[] result = new Action[actions.size()];
    actions.toArray(result );
    return new ActionMenu(contextMenuAction, result);
  }

  protected void stopSpeech() {
    if (mThread != null) {
      mThread.interrupt();
    }
  }

}
