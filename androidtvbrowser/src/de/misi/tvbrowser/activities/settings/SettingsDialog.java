package de.misi.tvbrowser.activities.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.misi.tvbrowser.R;

public class SettingsDialog extends Activity {

   private static final int MENU_SELECTGROUP = 0;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);    //To change body of overridden methods use File | Settings | File Templates.
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.addSubMenu(Menu.NONE, MENU_SELECTGROUP, Menu.NONE, R.string.settings_select_groups);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      switch(menuItem.getItemId()) {
         case MENU_SELECTGROUP :
            return true;
      }
      return super.onOptionsItemSelected(menuItem);
   }
}
