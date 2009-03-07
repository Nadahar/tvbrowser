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

import java.util.List;
import java.util.Properties;

public class SpeechPluginSettings {

  private static final String KEY_SELECTED_VOICE = "VOICE";

  private static final String KEY_SELECTED_ENGINE = "SELECTED_ENGINE";

  protected static final int ENGINE_NONE = -1;
  protected static final int ENGINE_MICROSOFT = 1;
  protected static final int ENGINE_JAVA = 2;
  protected static final int ENGINE_MAC = 3;
  protected static final int ENGINE_OTHER = 4;

  private static final String KEY_OTHER_ENGINE_EXECUTABLE = "ENGINE_EXECUTABLE";

  private static final String KEY_OTHER_ENGINE_PARAMETERS = "ENGINE_PARAMETERS";

  private Properties mSettings;

  public SpeechPluginSettings(final Properties settings) {
    mSettings = settings;
  }

  /**
   * get currently selected voice
   * 
   * @return voice name or <code>null</code>
   */
  protected String getVoice() {
    final String voice = mSettings.getProperty(KEY_SELECTED_VOICE, "");
    if (voice.length() == 0) {
      final List<String> voices = SpeechPlugin.getInstance()
          .getAvailableVoices();
      if (voices.size() > 0) {
        return voices.get(0);
      }
      return null;
    }
    return voice;
  }

  /**
   * get currently selected speech engine to be used
   */
  protected int getEngine() {
    final String engine = mSettings.getProperty(KEY_SELECTED_ENGINE, Integer
        .toString(ENGINE_NONE));
    return Integer.valueOf(engine);
  }

  protected void setEngine(final int engine) {
    mSettings.setProperty(KEY_SELECTED_ENGINE, Integer.toString(engine));
  }

  protected void setVoice(final String voice) {
    mSettings.setProperty(KEY_SELECTED_VOICE, voice);
  }

  public Properties storeSettings() {
    return mSettings;
  }

  public String getOtherEngineExecutable() {
    return mSettings.getProperty(KEY_OTHER_ENGINE_EXECUTABLE, "");
  }

  public void setOtherEngineExecutable(final String executable) {
    mSettings.setProperty(KEY_OTHER_ENGINE_EXECUTABLE, executable);
  }

  public String getOtherEngineParameters() {
    return mSettings.getProperty(KEY_OTHER_ENGINE_PARAMETERS, "");
  }

  public void setOtherEngineParameters(final String parameters) {
    mSettings.setProperty(KEY_OTHER_ENGINE_PARAMETERS, parameters);
  }
}
