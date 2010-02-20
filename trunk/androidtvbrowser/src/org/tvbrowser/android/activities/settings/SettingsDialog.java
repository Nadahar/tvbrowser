package org.tvbrowser.android.activities.settings;

import org.tvbrowser.android.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsDialog extends Activity {

   private static final String TVBROWSER_PREFERENCES = "tvbrowser_prefs";

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
