package de.misi.tvbrowser.activities.info;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import de.misi.tvbrowser.R;
import de.misi.tvbrowser.activities.search.SearchActivity;
import de.misi.tvbrowser.data.DataLoader;
import de.misi.tvbrowser.dialogs.ReminderDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoActivity extends Activity {

   public static final String EXTRA_ID = "de.misi.tvbrowser.info.id";

   private static final int MENU_ADDREMINDER = 1;
   private static final int MENU_EDITREMINDER = 2;
   private static final int MENU_DELETEREMINDER = 3;
   private static final int MENU_SEARCHFORREPEAT = 4;

   private static final int REMINDERDLG_ID = 0;

   private long broadcastId;
   private String broadcastTitle;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setContentView(R.layout.info);
      broadcastId = getIntent().getLongExtra(EXTRA_ID, 0);
      if (broadcastId != 0) {
         Cursor cursor = DataLoader.getAllBroadcastInfos(broadcastId);
         if (cursor != null) {
            if (cursor.moveToFirst()) {
               broadcastTitle = cursor.getString(cursor.getColumnIndex(DataLoader.BROADCAST_TITLE));
               setTextViewData(cursor, R.id.info_title, DataLoader.BROADCAST_TITLE);
               setDecryptedTextViewData(cursor, R.id.info_shortdescription, 0, DataLoader.INFO_SHORTDESCRIPTION);
               setDecryptedTextViewData(cursor, R.id.info_description, 0, DataLoader.INFO_DESCRIPTION);
               setDecryptedTextViewData(cursor, R.id.info_actor, R.id.info_lbl_actor, DataLoader.INFO_ACTOR);
               setDecryptedTextViewData(cursor, R.id.info_genre, R.id.info_lbl_genre, DataLoader.INFO_GENRE);
               setDecryptedTextViewData(cursor, R.id.info_formatinformation, R.id.info_lbl_formatinformation, DataLoader.INFO_FORM);
               setDecryptedTextViewData(cursor, R.id.info_repetitionon, R.id.info_lbl_repetitionon, DataLoader.INFO_REPETITIONON);
               setDecryptedTextViewData(cursor, R.id.info_repetitionof, R.id.info_lbl_repetitionof, DataLoader.INFO_REPETITIONOF);
               setDecryptedTextViewData(cursor, R.id.info_website, R.id.info_lbl_website, DataLoader.INFO_WEBSITE);
               ((TextView) findViewById(R.id.info_broadcastdata)).setText(getMainBroadcastData(cursor));
            }
            cursor.close();
         }
      }
   }

   private String getMainBroadcastData(Cursor cursor) {
      StringBuilder result = new StringBuilder();
      long startDateId = cursor.getLong(cursor.getColumnIndex(DataLoader.BROADCAST_START_DATE_ID));
      Date date = DataLoader.getDateByDataId(startDateId);
      if (date != null) {
         result.append(new SimpleDateFormat(getResources().getString(R.string.outputdateformat)).format(date)).append(" ");
      }
      result.append(cursor.getString(cursor.getColumnIndex(DataLoader.BROADCAST_STARTTIME)))
              .append("-").append(cursor.getString(cursor.getColumnIndex(DataLoader.BROADCAST_ENDTIME)))
              .append(", ").append(cursor.getString(cursor.getColumnIndex(DataLoader.CHANNEL_NAME)));
      return result.toString();
   }

   private void setTextViewData(Cursor cursor, int resourceId, String columnName) {
      ((TextView) findViewById(resourceId)).setText(cursor.getString(cursor.getColumnIndex(columnName)));
   }

   private void setDecryptedTextViewData(Cursor cursor, int resourceId, int labelResourceId, String columnName) {
      String dbValue = cursor.getString(cursor.getColumnIndex(columnName));
      TextView textView = (TextView) findViewById(resourceId);
      if (dbValue != null && dbValue.length() > 0) {
         textView.setText(DataLoader.decrypt(dbValue));
      } else {
         textView.setVisibility(View.INVISIBLE);
         textView.setHeight(0);
         if (labelResourceId != 0) {
            View view = findViewById(labelResourceId);
            view.setVisibility(View.INVISIBLE);
            ((TextView) view).setHeight(0);
         }
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, MENU_SEARCHFORREPEAT, Menu.NONE, R.string.menu_searchforrepeat);
      menu.add(Menu.NONE, MENU_ADDREMINDER, Menu.NONE, R.string.menu_addreminder);
      menu.add(Menu.NONE, MENU_EDITREMINDER, Menu.NONE, R.string.menu_editreminder);
      menu.add(Menu.NONE, MENU_DELETEREMINDER, Menu.NONE, R.string.menu_deletereminder);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      switch (menuItem.getItemId()) {
         case MENU_SEARCHFORREPEAT:
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.SEARCHTEXT, broadcastTitle);
            startActivity(intent);
            return true;
         case MENU_ADDREMINDER:
         case MENU_EDITREMINDER:
            showDialog(REMINDERDLG_ID);
            return true;
      }
      return super.onOptionsItemSelected(menuItem);
   }

   @Override
   protected Dialog onCreateDialog(int id) {
      switch (id) {
         case REMINDERDLG_ID:
            ReminderDialog dialog = new ReminderDialog(this);
            dialog.broadcastId = broadcastId;
            return dialog;
      }
      return null;
   }
}
