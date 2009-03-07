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

import java.util.List;

import speechplugin.quadmore.SAPI;

public class SAPISpeechEngine extends AbstractSpeechEngine {

  private SAPI mSapi;
  private Thread mThread;

  @Override
  public List<String> getVoices() {
    initialize();
    return mSapi.getVoices();
  }

  @Override
  public boolean isSpeaking() {
    // no status known
    return false;
  }

  @Override
  public void setVoice(final String voiceName) {
    final boolean result = mSapi.setVoice(voiceName);
    if (!result) {
      System.out.println("\nSet voice token NOT successful.");
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

    final boolean result = mSapi.speak(text);
    if (!result) {
      System.out.println("\nSpeak text NOT successful.");
    }
    /*
     * // run synthesizer in new thread to avoid GUI blocking mThread = new
     * Thread("Speech via SAPI") {
     * 
     * @Override public void run() { boolean result = mSapi.speak(text); if
     * (!result) { System.out.println("\nSpeak text NOT successful."); } mThread
     * = null; } }; mThread.start();
     */
  }

  @Override
  public void initialize() {
    if (mSapi == null) {
      mSapi = new SAPI();
    }
  }

  @Override
  public void stopSpeaking() {
    // no status known, nothing to do
  }
}
