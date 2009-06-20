package de.misi.tvbrowser.activities.reminder;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import de.misi.tvbrowser.R;
import de.misi.tvbrowser.Utility;
import de.misi.tvbrowser.data.Broadcast;
import de.misi.tvbrowser.data.DataLoader;

public class ReminderDialog extends Dialog {

   private static final int[] MINUTES = new int[]{
           10, 30, 60, 720, 24 * 60, 2 * 24 * 60, 7 * 24 * 60
   };

   private long mBroadcastId;
   private String mBroadcastTitle;
   private long mStartDate;
   private int mStartTime;
   private String mChannel;

   private TextView mBroadcastInfo;
   private Spinner mTimeSpinner;

   public ReminderDialog(Context context, Broadcast broadcast) {
      super(context);
      if (broadcast != null)
         mBroadcastId = broadcast.mId;
      initialize(context);
   }

   public ReminderDialog(Context context, long broadcastId) {
      super(context);
      mBroadcastId = broadcastId;
      initialize(context);
   }

   private void initialize(Context context) {
      setContentView(R.layout.reminderdlg);
      findViewById(R.id.reminder_ok).setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            doOk();
         }
      });
      findViewById(R.id.reminder_cancel).setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            doCancel();
         }
      });
      mBroadcastInfo = (TextView) findViewById(R.id.reminder_text);
      mTimeSpinner = (Spinner) findViewById(R.id.reminder_time);
      mTimeSpinner.setAdapter(new ArrayAdapter<String>(getContext(),
              android.R.layout.simple_spinner_item,
              context.getResources().getStringArray(R.array.reminder_times)));
      updateContent();
   }

   public void setBroadcastId(long broadcastId) {
      mBroadcastId = broadcastId;
      updateContent();
   }

   private void updateContent() {
      Cursor cursor = DataLoader.getAllBroadcastInfos(mBroadcastId);
      if (cursor != null) {
         if (cursor.moveToNext()) {
            mBroadcastTitle = cursor.getString(cursor.getColumnIndex(DataLoader.BROADCAST_TITLE));
            mStartDate = cursor.getLong(cursor.getColumnIndex(DataLoader.BROADCAST_START_DATE_ID));
            mStartTime = cursor.getInt(cursor.getColumnIndex(DataLoader.BROADCAST_STARTTIME));
            mChannel = cursor.getString(cursor.getColumnIndex(DataLoader.CHANNEL_NAME));
            setTitle(mBroadcastTitle);
            mBroadcastInfo.setText(Utility.getFormatedBroadcastInfo(getContext(),
                    mStartDate,
                    mStartTime,
                    cursor.getLong(cursor.getColumnIndex(DataLoader.BROADCAST_END_DATE_ID)),
                    cursor.getInt(cursor.getColumnIndex(DataLoader.BROADCAST_ENDTIME)), mChannel));
         }
         cursor.close();
      }
      mTimeSpinner.setSelection(0);
      int time = DataLoader.getReminderTime(mChannel, Utility.getTimeInMillis(mStartDate, mStartTime));
      if (time != 0) {
         for (int i = 0; i < MINUTES.length; i++) {
            if (MINUTES[i] == time) {
               mTimeSpinner.setSelection(i);
               break;
            }
         }
      }
   }

   private void doOk() {
      DataLoader.writeReminder(mBroadcastTitle, mStartDate, mStartTime, mChannel, getSelectedReminderTime());
      Utility.initializeReminder(getContext());
      dismiss();
   }

   private int getSelectedReminderTime() {
      return MINUTES[mTimeSpinner.getSelectedItemPosition()];
   }

   private void doCancel() {
      dismiss();
   }
}
