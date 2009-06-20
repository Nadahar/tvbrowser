package de.misi.tvbrowser;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.TimePicker;
import de.misi.tvbrowser.activities.info.InfoActivity;
import de.misi.tvbrowser.activities.search.SearchDialog;
import de.misi.tvbrowser.activities.settings.SettingsDialog;
import de.misi.tvbrowser.data.DataLoader;
import de.misi.tvbrowser.widgets.TVBrowserGridView;

import java.util.ArrayList;
import java.util.Calendar;

public class TVBrowser extends Activity implements TimePickerDialog.OnTimeSetListener {

   public static final String LOGTAG = "tvbrowser";

   private static final int MENU_SETTINGS = 1;
   private static final int MENU_SEARCH = 2;
   private static final int MENU_SELECTTIME = 3;
   public static final int MENU_ADDREMINDER = 4;
   public static final int MENU_EDITREMINDER = 5;
   public static final int MENU_DELETEREMINDER = 6;
   public static final int MENU_SHOWDETAIL = 7;
   public static final int MENU_SEARCHFORREPEAT = 8;
   private static final int MENU_SELECTDATE = 100;
   private static final int MENU_JUMPTOHOUR = 200;

   private static final int TIME_DIALOG_ID = 0;
   //   private static final int PROGRESSDIALOG_ID = 1;
   public static final int REMINDER_DIALOG_ID = 2;
   //   private ProgressDialog progressDialog = null;

   public static final int SEARCH_DIALOG_ID = 3;
   public GestureDetector mGestureDetector;
   private TVBrowserGridView mView;
   private ArrayList<Long> mFoundFiles;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
//      showDialog(PROGRESSDIALOG_ID);
      mView = new TVBrowserGridView(this);
      mView.setChannels(DataLoader.loadChannelsFromDatabase(Calendar.getInstance()));
//      if (progressDialog != null) {
//         progressDialog.dismiss();
//         progressDialog = null;
//      }
      setContentView(mView);
      mGestureDetector = new GestureDetector(this, new TVBrowserGestureListener());
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      SubMenu selectDateMenu = menu.addSubMenu(Menu.NONE, MENU_SELECTDATE, Menu.NONE, R.string.main_menu_selectdate);
      addSelectDateMenus(selectDateMenu);
      SubMenu jumpHourMenu = menu.addSubMenu(Menu.NONE, MENU_JUMPTOHOUR, Menu.NONE, R.string.main_menu_jumphour);
      addJumpHours(jumpHourMenu);
      menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.main_menu_search).setIcon(android.R.drawable.ic_menu_search);
//      menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.main_menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
      return true;
   }

   private void addSelectDateMenus(SubMenu subMenu) {
      Cursor cursor = DataLoader.getBroadcastDates();
      mFoundFiles = new ArrayList<Long>();
      if (cursor != null) {
         int index = 0;
         int datumColumnIndex = cursor.getColumnIndex(DataLoader.BROADCAST_START_DATE_ID);
         java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(this);
         while (cursor.moveToNext()) {
            long timeInMillis = cursor.getLong(datumColumnIndex);
            mFoundFiles.add(timeInMillis);
            subMenu.add(Menu.NONE, MENU_SELECTDATE + index + 1, index + 1, dateFormat.format(timeInMillis));
            index++;
         }
         cursor.close();
      }
   }

   private static void addJumpHours(SubMenu subMenu) {
      subMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 1, 1, "10:00");
      subMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 2, 2, "16:00");
      subMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 3, 3, "20:15");
      subMenu.add(Menu.NONE, MENU_SELECTTIME, 4, R.string.main_menu_selecttime);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem menuItem) {
      int itemId = menuItem.getItemId();
      switch (itemId) {
         case MENU_SETTINGS:
            startActivity(new Intent(this, SettingsDialog.class));
            return true;
         case MENU_SEARCH:
            showDialog(SEARCH_DIALOG_ID);
            return true;
         case MENU_SELECTTIME:
            showDialog(TIME_DIALOG_ID);
            return true;
      }
      if (itemId > MENU_SELECTDATE && itemId < MENU_JUMPTOHOUR) {
         int index = itemId - MENU_SELECTDATE - 1;
         if (index >= 0 && index < mFoundFiles.size()) {
            //               showDialog(PROGRESSDIALOG_ID);
            mView.setChannels(DataLoader.loadChannelsFromDatabase(mFoundFiles.get(index)));
//               if (progressDialog != null) {
//                  progressDialog.dismiss();
//                  progressDialog = null;
//               }
         }
      }
      if (itemId > MENU_JUMPTOHOUR) {
         int index = itemId - MENU_JUMPTOHOUR;
         switch (index) {
            case 1:
               mView.setVisiblePosition(10, 0);
               return true;
            case 2:
               mView.setVisiblePosition(16, 0);
               return true;
            case 3:
               mView.setVisiblePosition(20, 15);
               return true;
         }
      }
      return super.onOptionsItemSelected(menuItem);
   }

   @Override
   public boolean onTouchEvent(MotionEvent motionEvent) {
      return mGestureDetector.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
   }

   @Override
   protected Dialog onCreateDialog(int id) {
      switch (id) {
         case TIME_DIALOG_ID:
            return new TimePickerDialog(this, this, 12, 0, true);
//         case PROGRESSDIALOG_ID:
//            progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Indeterminate");
//            progressDialog.setMessage("Please wait while loading...");
//            progressDialog.setIndeterminate(true);
//            progressDialog.setCancelable(false);
//            return progressDialog;
         case REMINDER_DIALOG_ID:
            return Utility.createReminderDialog(this, mView.mSelectedBroadcast);
         case SEARCH_DIALOG_ID:
            SearchDialog searchDialog = Utility.createSearchDialog(this);
            searchDialog.setPredefinedText(null);
            return searchDialog;
      }
      return null;
   }

   public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      mView.setVisiblePosition(hourOfDay, minute);
   }

   @Override
   public boolean onContextItemSelected(MenuItem menuItem) {
      switch (menuItem.getItemId()) {
         case MENU_SHOWDETAIL:
            if (mView.mSelectedBroadcast != null) {
               Intent intent = new Intent(this, InfoActivity.class);
               intent.putExtra(InfoActivity.EXTRA_ID, (long) mView.mSelectedBroadcast.mId);
               startActivity(intent);
            }
            return true;
         case MENU_SEARCHFORREPEAT:
            if (mView.mSelectedBroadcast != null) {
               SearchDialog searchDialog = Utility.createSearchDialog(this);
               searchDialog.setPredefinedText(mView.mSelectedBroadcast.mTitle);
               showDialog(SEARCH_DIALOG_ID);
            }
            return true;
         case MENU_ADDREMINDER:
         case MENU_EDITREMINDER:
            if (mView.mSelectedBroadcast != null) {
               if (Utility.reminderDialog != null)
                  Utility.reminderDialog.setBroadcastId(mView.mSelectedBroadcast.mId);
               showDialog(REMINDER_DIALOG_ID);
            }
            return true;
      }
      return super.onContextItemSelected(menuItem);
   }

   private class TVBrowserGestureListener extends GestureDetector.SimpleOnGestureListener {
      @Override
      public boolean onDown(MotionEvent motionEvent) {
         mView.doDown(motionEvent);
         return true;
      }

      @Override
      public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX, float velocityY) {
         mView.doFling(motionEvent1, motionEvent2, velocityX, velocityY);
         return true;
      }

      @Override
      public boolean onScroll(MotionEvent motionEvent1, MotionEvent motionEvent2, float distanceX, float distanceY) {
         mView.doScroll(motionEvent1, motionEvent2, distanceX, distanceY);
         return true;
      }

      @Override
      public void onShowPress(MotionEvent motionEvent) {
         mView.doShowPress(motionEvent);
      }

      @Override
      public void onLongPress(MotionEvent motionEvent) {
         mView.doLongPress(motionEvent);
      }
   }
}
