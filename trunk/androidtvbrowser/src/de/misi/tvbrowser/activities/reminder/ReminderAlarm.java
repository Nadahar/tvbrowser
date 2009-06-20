package de.misi.tvbrowser.activities.reminder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;
import de.misi.tvbrowser.R;
import de.misi.tvbrowser.TVBrowser;
import de.misi.tvbrowser.Utility;
import de.misi.tvbrowser.activities.info.InfoActivity;
import de.misi.tvbrowser.data.DataLoader;


public class ReminderAlarm extends BroadcastReceiver {

   public static final String EXTRA_TIMEINMILLIS = "de.misi.tvbrowser.activities.reminder.ReminderAlarm.timeinmillis";

   @Override
   public void onReceive(Context context, Intent intent) {
      Log.d(TVBrowser.LOGTAG, "Reminder Alarm onReceive");
      long timeInMillis = intent.getLongExtra(EXTRA_TIMEINMILLIS, 0);
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
                          new String[]{cursor.getString(cursor.getColumnIndex(DataLoader.REMINDER_TITLE)),
                                       DateFormat.getTimeFormat(context)
                                               .format(Utility.getTimeInMillis(startDate, startTime))});
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
                  ((NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE)).notify(0, notification);
               }
            }
            cursor.close();
         }
      }
   }
}
