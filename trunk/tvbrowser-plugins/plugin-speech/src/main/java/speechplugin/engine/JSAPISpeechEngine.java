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
package speechplugin.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

public class JSAPISpeechEngine extends AbstractSpeechEngine {

  private static final String MODE_NAME = "general";

  private static final java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(JSAPISpeechEngine.class.getName());

  private Synthesizer mSynthesizer;

  private Thread mThread;

  private String mVoice;

  /**
   * Example of how to list all the known voices for a specific mode using just
   * JSAPI. FreeTTS maps the domain name to the JSAPI mode name. The currently
   * supported domains are "general," which means general purpose synthesis for
   * tasks such as reading e-mail, and "time" which means a domain that's only
   * good for speaking the time of day.
   */
  @Override
  public List<String> getVoices() {
    final ArrayList<String> result = new ArrayList<String>();

    // get all synthesizers without any restrictions (locale, domain, voice
    // etc.)
    final SynthesizerModeDesc unspecifiedSynthesizer = new SynthesizerModeDesc();
    final EngineList engineList = Central
        .availableSynthesizers(unspecifiedSynthesizer);

    // get all voices
    for (int i = 0; i < engineList.size(); i++) {
      final SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
      if (desc.getModeName().equals(MODE_NAME)) {
        final Voice[] voices = desc.getVoices();
        for (Voice voice : voices) {
          result.add(voice.getName());
        }
      }
    }
    return result;
  }

  @Override
  public void setVoice(final String voiceName) {
    mVoice = voiceName;
  }

  private void startSynthesizer(final String voiceName) {
    if (mSynthesizer != null) {
      return;
    }
    try {
      Voice selectedVoice = null;
      final SynthesizerModeDesc unspecifiedSynthesizer = new SynthesizerModeDesc();
      final EngineList engineList = Central
          .availableSynthesizers(unspecifiedSynthesizer);
      for (int i = 0; i < engineList.size(); i++) {
        final SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList
            .get(i);
        final Voice[] voices = desc.getVoices();
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

  @Override
  public void speak(final String text) {
    if (text == null) {
      return;
    }

    // if synthesizer is still running, stop it first
    if (mThread != null) {
      mThread.interrupt();
    }

    // run synthesizer in new thread to avoid GUI blocking
    mThread = new Thread("Speech") {
      @Override
      public void run() {
        startSynthesizer(mVoice);
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
  public void shutdown() {
    if (mSynthesizer == null) {
      return;
    }
    stopSpeaking();
    try {
      mSynthesizer.deallocate();
    } catch (EngineException e) {
      e.printStackTrace();
    } catch (EngineStateError e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isSpeaking() {
    return mThread != null;
  }

  @Override
  public void stopSpeaking() {
    if (mThread != null) {
      mThread.interrupt();
    }
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

}
