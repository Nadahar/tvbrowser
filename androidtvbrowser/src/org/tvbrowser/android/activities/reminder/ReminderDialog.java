package org.tvbrowser.android.activities.reminder;

import org.tvbrowser.android.R;
import org.tvbrowser.android.Utility;
import org.tvbrowser.android.data.DataLoader;
import org.tvbrowser.android.data.Program;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ReminderDialog extends Dialog {

  /**
   * selection of minutes to remind
   */
  private static final int[] MINUTES = new int[] { 10, 30, 60, 12 * 60, 24 * 60, 2 * 24 * 60, 7 * 24 * 60 };

  private long mProgramId;
  private String mProgramTitle;
  private long mStartDate;
  private int mStartTime;
  private String mChannel;

  private TextView mBroadcastInfo;
  private Spinner mTimeSpinner;

  private static ReminderDialog instance = null;

  /**
   * singleton access method
   * @param context
   * @param program
   * @return
   */
  public static ReminderDialog getInstance(Context context, Program program) {
    return getInstance(context, program.getId());
  }

  /**
   * singleton access method
   * @param context
   * @param programId
   * @return
   */
  public static ReminderDialog getInstance(Context context, long programId) {
    if (instance == null) {
      instance = new ReminderDialog(context, programId);
    }
    instance.setProgramId(programId);
    return instance;
  }

  /**
   * singleton constructor
   * @param context
   * @param broadcastId
   */
  private ReminderDialog(Context context, long broadcastId) {
    super(context);
    mProgramId = broadcastId;
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
    mTimeSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, context
        .getResources().getStringArray(R.array.reminder_times)));
    updateContent();
  }

  public void setProgramId(long programId) {
    mProgramId = programId;
    updateContent();
  }

  private void updateContent() {
    Cursor cursor = DataLoader.getAllProgramInfos(mProgramId);
    if (cursor != null) {
      if (cursor.moveToNext()) {
        mProgramTitle = cursor.getString(cursor.getColumnIndex(DataLoader.PROGRAM_TITLE));
        mStartDate = cursor.getLong(cursor.getColumnIndex(DataLoader.PROGRAM_START_DATE_ID));
        mStartTime = cursor.getInt(cursor.getColumnIndex(DataLoader.PROGRAM_STARTTIME));
        mChannel = cursor.getString(cursor.getColumnIndex(DataLoader.CHANNEL_NAME));
        setTitle(mProgramTitle);
        mBroadcastInfo.setText(Utility.getFormattedProgramInfo(getContext(), mStartDate, mStartTime, cursor
            .getLong(cursor.getColumnIndex(DataLoader.PROGRAM_END_DATE_ID)), cursor.getInt(cursor
            .getColumnIndex(DataLoader.PROGRAM_ENDTIME)), mChannel));
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
    DataLoader.writeReminder(mProgramTitle, mStartDate, mStartTime, mChannel, getSelectedReminderTime());
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
