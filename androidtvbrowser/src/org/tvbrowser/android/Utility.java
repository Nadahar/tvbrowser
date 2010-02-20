package org.tvbrowser.android;

import org.tvbrowser.android.activities.reminder.ReminderAlarm;
import org.tvbrowser.android.data.DataLoader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

public class Utility {

   public static String getFormattedProgramInfo(Context context, long startDate, int startTime, long endDate, int endTime, String channel) {
      StringBuilder result = new StringBuilder();
      if (startDate != 0) {
         java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
         result.append(DateFormat.getMediumDateFormat(context).format(startDate)).append(' ');
         result.append(timeFormat.format(getTimeInMillis(startDate, startTime)))
                 .append('-')
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
      intent.putExtra(ReminderAlarm.EXTRA_TIME_IN_MILLIS, timeInMillis);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      if (alarmManager != null) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
      }
   }

   public static long getTimeInMillis(long date, int time) {
      return date + time * 60L * 1000;
   }


}
