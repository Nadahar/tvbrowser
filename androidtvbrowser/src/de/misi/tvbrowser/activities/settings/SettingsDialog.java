package de.misi.tvbrowser.activities.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import de.misi.tvbrowser.R;

public class SettingsDialog extends Activity {

   public static final String TVBROWSER_PREFERENCES = "tvbrowser_prefs";

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setContentView(R.layout.settings);
      SharedPreferences preferences = getSharedPreferences(TVBROWSER_PREFERENCES, MODE_PRIVATE);
   }

   @Override
   protected void onStop() {
      super.onStop();
      SharedPreferences preferences = getSharedPreferences(TVBROWSER_PREFERENCES, MODE_PRIVATE);
      SharedPreferences.Editor editor = preferences.edit();

      editor.commit();
   }
}
