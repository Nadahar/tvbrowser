package org.tvbrowser.android.activities.reminder;

import org.tvbrowser.android.R;
import org.tvbrowser.android.TVBrowser;
import org.tvbrowser.android.Utility;
import org.tvbrowser.android.activities.info.InfoActivity;
import org.tvbrowser.android.data.DataLoader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;


public class ReminderAlarm extends BroadcastReceiver {

   public static final String EXTRA_TIME_IN_MILLIS = "org.tvbrowser.android.activities.reminder.ReminderAlarm.timeinmillis";

   @Override
   public void onReceive(Context context, Intent intent) {
      Log.d(TVBrowser.LOGTAG, "Reminder Alarm onReceive");
      long timeInMillis = intent.getLongExtra(EXTRA_TIME_IN_MILLIS, 0);
      if (timeInMillis != 0) {
         Cursor cursor = DataLoader.getReminderByTimeInMillis(timeInMillis);
         if (cursor != null) {
            if (cursor.moveToFirst()) {
               long startDate = cursor.getLong(cursor.getColumnIndex(DataLoader.REMINDER_STARTDATE));
               int startTime = cursor.getInt(cursor.getColumnIndex(DataLoader.REMINDER_STARTTIME));
               Intent infoIntent = null;
               int count = cursor.getCount();
               String tickerText = null;
               if (count == 1) {
                  infoIntent = new Intent(context, InfoActivity.class);
                  infoIntent.putExtra(InfoActivity.EXTRA_ID,
                          DataLoader.getBroadcastId(startDate, startTime,
                                  cursor.getString(cursor.getColumnIndex(DataLoader.REMINDER_CHANNEL))));
                  tickerText = context.getResources().getString(R.string.reminder_tickertext,
                          cursor.getString(cursor.getColumnIndex(DataLoader.REMINDER_TITLE)),
                                       DateFormat.getTimeFormat(context).format(Utility.getTimeInMillis(startDate, startTime)));
               } else if (count > 1) {
                  infoIntent = new Intent(context, ReminderListActivity.class);
                  infoIntent.putExtra(ReminderListActivity.EXTRA_TIMEINMILLIS, timeInMillis);
                  tickerText = context.getResources().getString(R.string.reminder_tickertextmultiple,
                          count,
                          DateFormat.getTimeFormat(context)
                                  .format(Utility.getTimeInMillis(startDate, startTime)));
               }
               if (infoIntent != null) {
                  Notification notification = new Notification(R.drawable.notification, tickerText, System.currentTimeMillis());
                  PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, infoIntent, 0);
                  notification.setLatestEventInfo(context,
                          context.getResources().getString(R.string.reminder_from),
                          tickerText, pendingIntent);
                  ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
               }
            }
            cursor.close();
         }
      }
   }
}
