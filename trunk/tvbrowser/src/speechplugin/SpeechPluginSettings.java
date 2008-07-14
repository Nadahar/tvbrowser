package speechplugin;

import java.util.List;
import java.util.Properties;

public class SpeechPluginSettings {

  private static final String SELECTED_VOICE = "VOICE";

  private Properties mSettings;
  public SpeechPluginSettings(Properties settings) {
    mSettings = settings;
  }
  
  protected String getVoice() {
    String voice = (String) mSettings.get(SELECTED_VOICE);
    if (voice == null) {
      List<String> voices = SpeechPlugin.getAvailableVoices("general");
      if (voices.size() > 0) {
        return voices.get(0);
      }
    }
    return voice;
  }
  
  protected void setVoice(String voice) {
    mSettings.put(SELECTED_VOICE, voice);
  }

  public Properties storeSettings() {
    return mSettings;
  }
}
