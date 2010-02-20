package org.tvbrowser.android.activities.info;

import org.tvbrowser.android.R;
import org.tvbrowser.android.TVBrowser;
import org.tvbrowser.android.Utility;
import org.tvbrowser.android.activities.reminder.ReminderDialog;
import org.tvbrowser.android.activities.search.SearchDialog;
import org.tvbrowser.android.data.DataLoader;
import org.tvbrowser.android.data.Program;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class InfoActivity extends Activity {

  public static final String EXTRA_ID = "org.tvbrowser.android.info.id";

  private static final FormatData[] FORMATS = new FormatData[] {
      new FormatData(1 << 1, R.string.info_format_black_white), new FormatData(1 << 2, R.string.info_format_4_3),
      new FormatData(1 << 3, R.string.info_format_16_9), new FormatData(1 << 4, R.string.info_format_audio_mono),
      new FormatData(1 << 5, R.string.info_format_audio_stereo),
      new FormatData(1 << 6, R.string.info_format_audio_dolby_surround),
      new FormatData(1 << 7, R.string.info_format_audio_dolby_digital_5_1),
      new FormatData(1 << 8, R.string.info_format_audio_two_channel),
      new FormatData(1 << 9, R.string.info_format_aurally_handicaped),
      new FormatData(1 << 10, R.string.info_format_live),
      new FormatData(1 << 11, R.string.info_format_original_with_subtitle), };

  private long programId;
  private String programTitle;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.info);
    programId = getIntent().getLongExtra(EXTRA_ID, 0);
    if (programId != 0) {
      Cursor cursor = DataLoader.getAllProgramInfos(programId);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          programTitle = cursor.getString(cursor.getColumnIndex(DataLoader.PROGRAM_TITLE));
          setTitle(programTitle);
          setTextViewData(cursor);
          setDecryptedTextViewData(cursor, R.id.info_shortdescription, 0, DataLoader.INFO_SHORT_DESCRIPTION);
          setDecryptedTextViewData(cursor, R.id.info_description, 0, DataLoader.INFO_DESCRIPTION);
          setDecryptedTextViewData(cursor, R.id.info_actor, R.id.info_lbl_actor, DataLoader.INFO_ACTOR);
          setDecryptedTextViewData(cursor, R.id.info_genre, R.id.info_lbl_genre, DataLoader.INFO_GENRE);
          setInfoTextViewData(cursor);
          setDecryptedTextViewData(cursor, R.id.info_repetitionon, R.id.info_lbl_repetitionon,
              DataLoader.INFO_REPETITION_ON);
          setDecryptedTextViewData(cursor, R.id.info_repetitionof, R.id.info_lbl_repetitionof,
              DataLoader.INFO_REPETITION_OF);
          setDecryptedTextViewData(cursor, R.id.info_website, R.id.info_lbl_website, DataLoader.INFO_WEBSITE);
          ((TextView) findViewById(R.id.info_broadcastdata)).setText(getMainBroadcastData(cursor));
        }
        cursor.close();
      }
    }
  }

  private String getMainBroadcastData(Cursor cursor) {
    return Utility.getFormattedProgramInfo(this, cursor
        .getLong(cursor.getColumnIndex(DataLoader.PROGRAM_START_DATE_ID)), cursor.getInt(cursor
        .getColumnIndex(DataLoader.PROGRAM_STARTTIME)), cursor.getLong(cursor
        .getColumnIndex(DataLoader.PROGRAM_END_DATE_ID)), cursor.getInt(cursor
        .getColumnIndex(DataLoader.PROGRAM_ENDTIME)), cursor.getString(cursor.getColumnIndex(DataLoader.CHANNEL_NAME)));
  }

  private void setTextViewData(Cursor cursor) {
    ((TextView) findViewById(R.id.info_title)).setText(cursor
        .getString(cursor.getColumnIndex(DataLoader.PROGRAM_TITLE)));
  }

  private void setDecryptedTextViewData(Cursor cursor, int resourceId, int labelResourceId, String columnName) {
    String dbValue = cursor.getString(cursor.getColumnIndex(columnName));
    TextView textView = (TextView) findViewById(resourceId);
    if (dbValue != null && dbValue.length() > 0) {
      textView.setText(DataLoader.decrypt(dbValue));
    } else {
      hideTextView(labelResourceId, textView);
    }
  }

  private void setInfoTextViewData(Cursor cursor) {
    int value = cursor.getInt(cursor.getColumnIndex(DataLoader.INFO_FORM));
    TextView textView = (TextView) findViewById(R.id.info_formatinformation);
    if (value > 0) {
      textView.setText(getInfo(value));
    } else {
      hideTextView(R.id.info_lbl_formatinformation, textView);
    }
  }

  private void hideTextView(int labelResourceId, TextView textView) {
    textView.setVisibility(View.INVISIBLE);
    textView.setHeight(0);
    if (labelResourceId != 0) {
      View view = findViewById(labelResourceId);
      view.setVisibility(View.INVISIBLE);
      ((TextView) view).setHeight(0);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu optionsMenu) {
    optionsMenu.add(Menu.NONE, TVBrowser.MENU_SEARCH_REPETITION, Menu.NONE, R.string.menu_searchforrepeat);
    Program.createProgramReminderContextMenu(optionsMenu, DataLoader.getReminderId(programId));
    return super.onCreateOptionsMenu(optionsMenu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch (menuItem.getItemId()) {
    case TVBrowser.MENU_SEARCH_REPETITION:
      SearchDialog searchDialog = SearchDialog.getInstance(this);
      searchDialog.setPredefinedText(programTitle);
      showDialog(TVBrowser.SEARCH_DIALOG_ID);
      return true;
    case TVBrowser.MENU_ADD_REMINDER:
    case TVBrowser.MENU_EDIT_REMINDER:
      showDialog(TVBrowser.REMINDER_DIALOG_ID);
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case TVBrowser.REMINDER_DIALOG_ID:
      return ReminderDialog.getInstance(this, programId);
    case TVBrowser.SEARCH_DIALOG_ID:
      return SearchDialog.getInstance(this);
    }
    return null;
  }

  private String getInfo(int info) {
    StringBuilder builder = new StringBuilder();
    Resources resources = getResources();
    for (FormatData formatData : FORMATS) {
      if (formatData.isBitSet(info)) {
        if (builder.length() > 0) {
          builder.append("|");
        }
        builder.append(resources.getString(formatData.mResourceId));
      }
    }
    return builder.toString();
  }

  private static class FormatData {

    private int mBit;

    private int mResourceId;

    private FormatData(int bit, int resourceId) {
      mBit = bit;
      mResourceId = resourceId;
    }

    private boolean isBitSet(int info) {
      return (info & mBit) == mBit;
    }
  }
}
