package org.tvbrowser.android;

import java.util.ArrayList;
import java.util.Calendar;

import org.tvbrowser.android.activities.info.InfoActivity;
import org.tvbrowser.android.activities.reminder.ReminderDialog;
import org.tvbrowser.android.activities.search.SearchDialog;
import org.tvbrowser.android.activities.settings.SettingsDialog;
import org.tvbrowser.android.data.DataLoader;
import org.tvbrowser.android.widgets.TVBrowserGridView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.widget.TimePicker;

/**
 * the initial activity to be run after application start
 *
 */
public class TVBrowser extends Activity implements TimePickerDialog.OnTimeSetListener {

  public static final String LOGTAG = "tvbrowser";

  private static final int MENU_SETTINGS = 1;
  private static final int MENU_SEARCH = 2;
  private static final int MENU_SELECT_TIME = 3;
  public static final int MENU_ADD_REMINDER = 4;
  public static final int MENU_EDIT_REMINDER = 5;
  public static final int MENU_DELETE_REMINDER = 6;
  public static final int MENU_SHOWDETAIL = 7;
  public static final int MENU_SEARCH_REPETITION = 8;
  private static final int MENU_SELECT_DATE = 100;
  private static final int MENU_JUMPTOHOUR = 200;

  private static final int TIME_DIALOG_ID = 0;
  public static final int REMINDER_DIALOG_ID = 2;
  public static final int SEARCH_DIALOG_ID = 3;
  private static final int NO_CURRENT_DATA_DIALOG = 4;
  private static final int NO_DATA_DIALOG = 5;

  public GestureDetector mGestureDetector;
  private TVBrowserGridView mView;
  private ArrayList<Long> mAvailableDates;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // showDialog(PROGRESSDIALOG_ID);
    // if (progressDialog != null) {
    // progressDialog.dismiss();
    // progressDialog = null;
    // }
    mGestureDetector = new GestureDetector(this, new TVBrowserGestureListener());
    checkDataAvailability();
  }

  private void checkDataAvailability() {
    if (mAvailableDates == null) {
      mAvailableDates = DataLoader.getBroadcastDates();
    }
    // data at all
    if (mAvailableDates.isEmpty()) {
      showDialog(NO_DATA_DIALOG);
    } else {
      Calendar todayCal = Calendar.getInstance();
      todayCal.set(Calendar.HOUR_OF_DAY, 0);
      todayCal.set(Calendar.MINUTE, 0);
      todayCal.set(Calendar.SECOND, 0);
      todayCal.set(Calendar.MILLISECOND, 0);
      long today = todayCal.getTimeInMillis();
      long firstDay = mAvailableDates.get(0);
      long lastDay = mAvailableDates.get(mAvailableDates.size() - 1);
      if (!(firstDay <= today && today <= lastDay)) {
        // data, but not for today
        showDialog(NO_CURRENT_DATA_DIALOG);
      } else {
        // current data
        initializeGridWith(today);
      }
    }
  }

  private void initializeGridWith(long timeInMillis) {
    mView = new TVBrowserGridView(this);
    mView.setDayPrograms(DataLoader.loadDayProgramsFromDatabase(timeInMillis));
    setContentView(mView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu optionsMenu) {
    SubMenu selectDateMenu = optionsMenu.addSubMenu(Menu.NONE, MENU_SELECT_DATE, Menu.NONE,
        R.string.main_menu_selectdate);
    createSelectDateMenus(selectDateMenu);
    SubMenu jumpHourMenu = optionsMenu.addSubMenu(Menu.NONE, MENU_JUMPTOHOUR, Menu.NONE, R.string.main_menu_jumphour);
    createHoursMenu(jumpHourMenu);
    optionsMenu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.main_menu_search).setIcon(
        android.R.drawable.ic_menu_search);
    optionsMenu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.main_menu_settings).setIcon(
        android.R.drawable.ic_menu_preferences);
    return true;
  }

  private void createSelectDateMenus(SubMenu selectDateMenu) {
    mAvailableDates = DataLoader.getBroadcastDates();
    java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(this);
    for (int i = 0; i < mAvailableDates.size(); i++) {
      long timeInMillis = mAvailableDates.get(i);
      selectDateMenu.add(Menu.NONE, MENU_SELECT_DATE + i + 1, i + 1, dateFormat.format(timeInMillis));
    }
  }

  private void createHoursMenu(SubMenu hoursMenu) {
    hoursMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 1, 1, "10:00");
    hoursMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 2, 2, "16:00");
    hoursMenu.add(Menu.NONE, MENU_JUMPTOHOUR + 3, 3, "20:15");
    hoursMenu.add(Menu.NONE, MENU_SELECT_TIME, 4, R.string.main_menu_selecttime);
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
    case MENU_SELECT_TIME:
      showDialog(TIME_DIALOG_ID);
      return true;
    case MENU_JUMPTOHOUR + 1:
      mView.setVisiblePosition(10, 0);
      return true;
    case MENU_JUMPTOHOUR + 2:
      mView.setVisiblePosition(16, 0);
      return true;
    case MENU_JUMPTOHOUR + 3:
      mView.setVisiblePosition(20, 15);
      return true;
    }
    if (itemId > MENU_SELECT_DATE && itemId < MENU_JUMPTOHOUR) {
      int index = itemId - MENU_SELECT_DATE - 1;
      if (index >= 0 && index < mAvailableDates.size()) {
        // showDialog(PROGRESSDIALOG_ID);
        mView.setDayPrograms(DataLoader.loadDayProgramsFromDatabase(mAvailableDates.get(index)));
        // if (progressDialog != null) {
        // progressDialog.dismiss();
        // progressDialog = null;
        // }
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
      // case PROGRESSDIALOG_ID:
      // progressDialog = new ProgressDialog(this);
      // progressDialog.setTitle("Indeterminate");
      // progressDialog.setMessage("Please wait while loading...");
      // progressDialog.setIndeterminate(true);
      // progressDialog.setCancelable(false);
      // return progressDialog;
    case REMINDER_DIALOG_ID:
      return ReminderDialog.getInstance(this, mView.mSelectedBroadcast);
    case SEARCH_DIALOG_ID:
      SearchDialog searchDialog = SearchDialog.getInstance(this);
      searchDialog.setPredefinedText(null);
      return searchDialog;
    case NO_CURRENT_DATA_DIALOG:
      return createNoCurrentDataDialog();
    case NO_DATA_DIALOG:
      return createNoDataDialog();
    }
    return null;
  }

  private Dialog createNoDataDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("There is no data available. Please export data with TV-Browser on your PC first.")
        .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });
    return builder.create();
  }

  private Dialog createNoCurrentDataDialog() {
    java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(this);
    ArrayList<CharSequence> list = new ArrayList<CharSequence>(mAvailableDates.size());
    for (Long date : mAvailableDates) {
      list.add(dateFormat.format(date));
    }
    CharSequence[] items = list.toArray(new CharSequence[list.size()]);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("No data for today. Please select another date.").setCancelable(false).setItems(items,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int dateIndex) {
            if (dateIndex >= 0 && dateIndex < mAvailableDates.size()) {
              initializeGridWith(mAvailableDates.get(dateIndex));
            }
          }
        });
    AlertDialog dialog = builder.create();
    return dialog;
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
        intent.putExtra(InfoActivity.EXTRA_ID, (long) mView.mSelectedBroadcast.getId());
        startActivity(intent);
      }
      return true;
    case MENU_SEARCH_REPETITION:
      if (mView.mSelectedBroadcast != null) {
        SearchDialog searchDialog = SearchDialog.getInstance(this);
        searchDialog.setPredefinedText(mView.mSelectedBroadcast.getTitle());
        showDialog(SEARCH_DIALOG_ID);
      }
      return true;
    case MENU_ADD_REMINDER:
    case MENU_EDIT_REMINDER:
      if (mView.mSelectedBroadcast != null) {
        ReminderDialog.getInstance(null, mView.mSelectedBroadcast.getId());
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
