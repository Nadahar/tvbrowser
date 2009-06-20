package de.misi.tvbrowser;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.ContextMenu;
import de.misi.tvbrowser.activities.reminder.ReminderAlarm;
import de.misi.tvbrowser.activities.reminder.ReminderDialog;
import de.misi.tvbrowser.data.DataLoader;
import de.misi.tvbrowser.data.Broadcast;
import de.misi.tvbrowser.widgets.TVBrowserGridView;
import de.misi.tvbrowser.activities.search.SearchDialog;

import java.util.Calendar;

public class Utility {

   public static final Calendar tempCalendar;
   public static ReminderDialog reminderDialog = null;
   public static SearchDialog searchDialog = null;

   static {
      tempCalendar = Calendar.getInstance();
      tempCalendar.set(Calendar.MILLISECOND, 0);
   }

   public static int getX(Calendar now) {
      return getX(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
   }

   public static int getX(int hour, int minute) {
      return (hour * 60 + minute) * TVBrowserGridView.MINUTE_WIDTH;
   }

   public static String getFormatedBroadcastInfo(Context context, long startDate, int startTime, long endDate, int endTime, String channel) {
      StringBuilder result = new StringBuilder();
      if (startDate != 0) {
         java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
         result.append(DateFormat.getMediumDateFormat(context).format(startDate)).append(" ");
         result.append(timeFormat.format(getTimeInMillis(startDate, startTime)))
                 .append("-")
                 .append(timeFormat.format(getTimeInMillis(endDate, endTime)))
                 .append(", ");
      }
      if (channel != null) {
         result.append(channel);
      }
      return result.toString();
   }

   public static void initializeReminder(Context context) {
      long nextTime = DataLoader.getNextReminderTime();
      if (nextTime != 0) {
         startReminderAlarm(context, nextTime);
      }
   }

   private static void startReminderAlarm(Context context, long timeInMillis) {
      Log.d(TVBrowser.LOGTAG, "startReminderAlarm");
      Intent intent = new Intent(context, ReminderAlarm.class);
      intent.putExtra(ReminderAlarm.EXTRA_TIMEINMILLIS, timeInMillis);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
      ((AlarmManager) context.getSystemService(Activity.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
   }

   public static long getTimeInMillis(long date, int time) {
      return date + time * 60 * 1000;
   }

   public static void createBroadcastContextMenu(Broadcast broadcast, ContextMenu contextMenu) {
      if (broadcast != null) {
         contextMenu.setHeaderTitle(broadcast.mTitle);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_SHOWDETAIL, Menu.NONE, R.string.menu_showdetail);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_SEARCHFORREPEAT, Menu.NONE, R.string.menu_searchforrepeat);
         createBroadcastReminderContextMenu(contextMenu, broadcast.getReminderId());
      }
   }

   public static void createBroadcastReminderContextMenu(Menu menu, long id) {
      if (id == 0) {
         menu.add(Menu.NONE, TVBrowser.MENU_ADDREMINDER, Menu.NONE, R.string.menu_addreminder);
      } else {
         menu.add(Menu.NONE, TVBrowser.MENU_EDITREMINDER, Menu.NONE, R.string.menu_editreminder);
         menu.add(Menu.NONE, TVBrowser.MENU_DELETEREMINDER, Menu.NONE, R.string.menu_deletereminder);
      }
   }

   public static ReminderDialog createReminderDialog(Context context, Broadcast broadcast) {
      if (reminderDialog == null)
         reminderDialog = new ReminderDialog(context, broadcast);
      else
         reminderDialog.setBroadcastId(broadcast.mId);
      return reminderDialog;
   }

   public static ReminderDialog createReminderDialog(Context context, long broadcastId) {
      if (reminderDialog == null)
         reminderDialog = new ReminderDialog(context, broadcastId);
      else
         reminderDialog.setBroadcastId(broadcastId);
      return reminderDialog;
   }

   public static SearchDialog createSearchDialog(Context context) {
      if (searchDialog == null)
         searchDialog = new SearchDialog(context);
      return searchDialog;
   }
}
