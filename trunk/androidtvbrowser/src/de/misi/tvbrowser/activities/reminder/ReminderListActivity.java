package de.misi.tvbrowser.activities.reminder;

import android.database.Cursor;
import de.misi.tvbrowser.activities.AbstractBroadcastListActivity;
import de.misi.tvbrowser.data.DataLoader;

import java.util.ArrayList;

public class ReminderListActivity extends AbstractBroadcastListActivity {

   public static final String EXTRA_TIMEINMILLIS = "de.misi.tvbrowser.activities.reminder.ReminderListActivity.timeInMillis";

   @Override
   protected Cursor createQuery() {
      long timeInMillits = getIntent().getLongExtra(EXTRA_TIMEINMILLIS, 0);
      Cursor cursor = DataLoader.getReminderByTimeInMillis(timeInMillits);
      if (cursor != null) {
         StringBuilder where = new StringBuilder();
         ArrayList<String> whereArgs = new ArrayList<String>();
         boolean first = true;
         int startDateIndex = cursor.getColumnIndex(DataLoader.REMINDER_STARTDATE);
         int startTimeIndex = cursor.getColumnIndex(DataLoader.REMINDER_STARTTIME);
         int channelIndex = cursor.getColumnIndex(DataLoader.REMINDER_CHANNEL);
         while (cursor.moveToNext()) {
            if (first) {
               first = false;
            } else {
               where = where.append(" OR ");
            }
            where.append("(" + DataLoader.TABLENAME_BROADCAST + "." + DataLoader.BROADCAST_START_DATE_ID + "=?" +
                         " AND " + DataLoader.TABLENAME_BROADCAST + "." + DataLoader.BROADCAST_STARTTIME + "=?" +
                         " AND " + DataLoader.TABLENAME_CHANNEL + "." + DataLoader.CHANNEL_ID + "=?)");
            whereArgs.add(Long.toString(cursor.getLong(startDateIndex)));
            whereArgs.add(Integer.toString(cursor.getInt(startTimeIndex)));
            whereArgs.add(Long.toString(cursor.getLong(channelIndex)));
         }
         cursor.close();
         return DataLoader.createSearchQuery(where.toString(), whereArgs.toArray(new String[whereArgs.size()]));
      }
      return null;
   }
}
