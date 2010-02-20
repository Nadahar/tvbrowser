package org.tvbrowser.android.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.tvbrowser.android.TVBrowser;
import org.tvbrowser.android.Utility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataLoader {

  private static final String TVBROWSER_DATADIRECTORY = File.separator + "sdcard" + File.separator + "TVBrowser";

  private static final String DOT_TVBFILESUFFIX = ".tvd";

  private static final String TVBROWSER_DATABASEFILENAME = TVBROWSER_DATADIRECTORY + File.separator + "data"
      + DOT_TVBFILESUFFIX;

  private static final String INTERNALDATABASE = "tvbrowser.db";
  private static final String TABLENAME_REMINDER = "reminder";
  private static final String REMINDER_ID = "_id";
  public static final String REMINDER_CHANNEL = "channel";
  public static final String REMINDER_STARTDATE = "startdate";
  public static final String REMINDER_STARTTIME = "starttime";
  public static final String REMINDER_TITLE = "title";
  private static final String REMINDER_REMINDERTIME = "remindertime";

  private static final String REMINDER_TIMEINMILLIS = "timeinmillis";
  public static final String TABLENAME_CHANNEL = "channel";
  public static final String CHANNEL_ID = "id";
  public static final String CHANNEL_NAME = "name";
  public static final String TABLENAME_BROADCAST = "broadcast";
  private static final String PROGRAM_ID = "id";
  private static final String PROGRAM_CHANNEL_ID = "channel_id";
  public static final String PROGRAM_TITLE = "title";
  public static final String PROGRAM_START_DATE_ID = "start_date_id";
  public static final String PROGRAM_END_DATE_ID = "end_date_id";
  public static final String PROGRAM_STARTTIME = "starttime";
  public static final String PROGRAM_ENDTIME = "endtime";
  private static final String TABLENAME_INFO = "info";
  private static final String INFO_PROGRAM_ID = "broadcast_id";
  public static final String INFO_DESCRIPTION = "description";
  public static final String INFO_SHORT_DESCRIPTION = "shortdescription";
  public static final String INFO_GENRE = "genre";
  private static final String INFO_PRODUCED = "produced";
  private static final String INFO_LOCATION = "location";
  private static final String INFO_DIRECTOR = "director";
  private static final String INFO_SCRIPT = "script";
  public static final String INFO_ACTOR = "actor";
  private static final String INFO_MUSIC = "music";
  private static final String INFO_ORIGINAL_TITEL = "originaltitel";
  private static final String INFO_FSK = "fsk";
  public static final String INFO_FORM = "form";
  private static final String INFO_SHOWVIEW = "showview";
  private static final String INFO_EPISODE = "episode";
  private static final String INFO_ORIGINAL_EPISODE = "originalepisode";
  private static final String INFO_MODERATION = "moderation";
  public static final String INFO_WEBSITE = "webside";
  private static final String INFO_VPS = "vps";
  public static final String INFO_REPETITION_ON = "repetitionon";

  public static final String INFO_REPETITION_OF = "repetitionof";

  private static final String[] allInfoSearchFields = { INFO_DESCRIPTION, INFO_SHORT_DESCRIPTION, INFO_GENRE,
      INFO_PRODUCED, INFO_LOCATION, INFO_DIRECTOR, INFO_SCRIPT, INFO_ACTOR, INFO_MUSIC, INFO_ORIGINAL_TITEL, INFO_FSK,
      INFO_FORM, INFO_SHOWVIEW, INFO_EPISODE, INFO_ORIGINAL_EPISODE, INFO_MODERATION, INFO_WEBSITE, INFO_VPS,
      INFO_REPETITION_ON, INFO_REPETITION_OF };
  private static SQLiteDatabase database;
  private static SQLiteDatabase internalDatabase;

  private static SQLiteDatabase openDatabase() {
    if (database == null) {
      File file = new File(TVBROWSER_DATABASEFILENAME);
      if (file.exists()) {
        database = SQLiteDatabase.openDatabase(TVBROWSER_DATABASEFILENAME, null, SQLiteDatabase.OPEN_READWRITE);
      }
    }
    return database;
  }

  public static ArrayList<Long> getBroadcastDates() {
    ArrayList<Long> result = new ArrayList<Long>();
    SQLiteDatabase database = openDatabase();
    if (database != null) {
      Cursor cursor = database.query(TABLENAME_BROADCAST, new String[] { "DISTINCT " + PROGRAM_START_DATE_ID }, null,
          null, null, null, PROGRAM_START_DATE_ID);
      if (cursor != null) {
        int index = 0;
        int datumColumnIndex = cursor.getColumnIndex(DataLoader.PROGRAM_START_DATE_ID);
        while (cursor.moveToNext()) {
          long timeInMillis = cursor.getLong(datumColumnIndex);
          result.add(timeInMillis);
          index++;
        }
        cursor.close();
      }
    }
    return result;
  }

  public static ArrayList<ChannelDayProgram> loadDayProgramsFromDatabase(long timeInMillis) {
    SQLiteDatabase database = openDatabase();
    ArrayList<ChannelDayProgram> result = new ArrayList<ChannelDayProgram>();
    if (database != null) {
      Cursor cursor = database.query(TABLENAME_CHANNEL, new String[] { CHANNEL_ID, CHANNEL_NAME }, null, null, null,
          null, null);
      int channelIdIndex = cursor.getColumnIndex(CHANNEL_ID);
      int channelNameIndex = cursor.getColumnIndex(CHANNEL_NAME);
      while (cursor.moveToNext()) {
        ChannelDayProgram channel = new ChannelDayProgram(cursor.getInt(channelIdIndex), cursor.getString(channelNameIndex));
        channel.loadPrograms(timeInMillis);
        result.add(channel);
      }
      cursor.close();
    }
    return result;
  }

  protected static ArrayList<Program> loadProgramsFromDatabase(long currentDateInMillis, int channelId) {
    SQLiteDatabase database = openDatabase();
    ArrayList<Program> programs = new ArrayList<Program>();
    Calendar tempCalendar = Calendar.getInstance();
    tempCalendar.set(Calendar.MILLISECOND, 0);
    tempCalendar.setTimeInMillis(currentDateInMillis);
    tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
    long nextDay = tempCalendar.getTimeInMillis();
    String startDateIdStr = Long.toString(currentDateInMillis);
    Cursor cursor = database.query(TABLENAME_BROADCAST, new String[] { PROGRAM_ID, PROGRAM_TITLE,
        PROGRAM_START_DATE_ID, PROGRAM_STARTTIME, PROGRAM_END_DATE_ID, PROGRAM_ENDTIME }, PROGRAM_CHANNEL_ID + "=? AND"
        + " (" + PROGRAM_START_DATE_ID + "=? OR " + PROGRAM_END_DATE_ID + "=?)", new String[] {
        Integer.toString(channelId), startDateIdStr, startDateIdStr }, null, null, PROGRAM_START_DATE_ID + ","
        + PROGRAM_STARTTIME);
    if (cursor != null && cursor.getCount() > 0) {
      int broadcastIdIndex = cursor.getColumnIndex(PROGRAM_ID);
      int broadcastTitleIndex = cursor.getColumnIndex(PROGRAM_TITLE);
      int broadcastStartDateIndex = cursor.getColumnIndex(PROGRAM_START_DATE_ID);
      int broadcastStartIndex = cursor.getColumnIndex(PROGRAM_STARTTIME);
      int broadcastEndDateIndex = cursor.getColumnIndex(PROGRAM_END_DATE_ID);
      int broadcastEndIndex = cursor.getColumnIndex(PROGRAM_ENDTIME);
      while (cursor.moveToNext()) {
        programs.add(new Program(cursor.getInt(broadcastIdIndex), cursor.getString(broadcastTitleIndex),
            currentDateInMillis, nextDay, cursor.getLong(broadcastStartDateIndex), cursor.getInt(broadcastStartIndex),
            cursor.getLong(broadcastEndDateIndex), cursor.getInt(broadcastEndIndex)));
      }
      cursor.close();
    }
    return programs;
  }

  public static long getBroadcastId(long startDate, int startTime, String channel) {
    SQLiteDatabase database = openDatabase();
    long result = 0;
    if (database != null) {
      Cursor cursor = database.query(TABLENAME_BROADCAST + "," + TABLENAME_CHANNEL, new String[] { TABLENAME_BROADCAST
          + "." + PROGRAM_ID }, TABLENAME_BROADCAST + "." + PROGRAM_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "."
          + CHANNEL_ID + " AND " + TABLENAME_CHANNEL + "." + CHANNEL_NAME + "=? AND " + TABLENAME_BROADCAST + "."
          + PROGRAM_START_DATE_ID + "=? AND " + TABLENAME_BROADCAST + "." + PROGRAM_STARTTIME + "=?", new String[] {
          channel, Long.toString(startDate), Integer.toString(startTime) }, null, null, null, null);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          result = cursor.getLong(cursor.getColumnIndex(PROGRAM_ID));
        }
        cursor.close();
      }
    }
    return result;
  }

  public static Cursor createSearchQuery(String searchText, int searchType, boolean onlyFuture) {
    SQLiteDatabase database = openDatabase();
    if (database != null) {
      String where = TABLENAME_BROADCAST + "." + PROGRAM_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID;
      if (searchType == 1) {
        where = where + " AND " + TABLENAME_BROADCAST + "." + PROGRAM_ID + "=" + TABLENAME_INFO + "." + INFO_PROGRAM_ID;
      }
      String tables = TABLENAME_BROADCAST + ", " + TABLENAME_CHANNEL;
      String[] whereArgs;
      if (onlyFuture) {
        where = where + " AND " + PROGRAM_START_DATE_ID + ">?";
        whereArgs = new String[] { Long.toString(System.currentTimeMillis()), "%" + searchText + "%" };
      } else {
        whereArgs = new String[] { "%" + searchText + "%" };
      }
      where = where + " AND (" + PROGRAM_TITLE + " LIKE ?";
      if (searchType == 1) {
        tables = tables + ", " + TABLENAME_INFO;
        String encryptedtext = encrypt(searchText);
        for (String infoSearchField : allInfoSearchFields) {
          where = where + " OR " + TABLENAME_INFO + "." + infoSearchField + " LIKE '%" + encryptedtext + "%'";
        }
      }
      where = where + ")";
      return createSearchQuery(database, tables, where, whereArgs);
    }
    return null;
  }

  public static Cursor createSearchQuery(String where, String[] whereArgs) {
    SQLiteDatabase database = openDatabase();
    if (database != null) {
      return createSearchQuery(database, TABLENAME_BROADCAST + ", " + TABLENAME_CHANNEL, TABLENAME_BROADCAST + "."
          + PROGRAM_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND " + where, whereArgs);
    }
    return null;
  }

  private static Cursor createSearchQuery(SQLiteDatabase database, String tables, String where, String[] whereArgs) {
    return database.query(tables, new String[] { PROGRAM_TITLE, PROGRAM_STARTTIME, CHANNEL_NAME, PROGRAM_START_DATE_ID,
        TABLENAME_BROADCAST + "." + PROGRAM_ID + " AS _id" }, where, whereArgs, null, null, PROGRAM_START_DATE_ID + ","
        + PROGRAM_STARTTIME);
  }

  public static Cursor getAllProgramInfos(long broadcastid) {
    SQLiteDatabase database = openDatabase();
    if (database != null) {
      return database.query(TABLENAME_BROADCAST + "," + TABLENAME_CHANNEL + "," + TABLENAME_INFO, new String[] {
          TABLENAME_BROADCAST + ".*", TABLENAME_INFO + ".*", TABLENAME_CHANNEL + "." + CHANNEL_NAME },
          TABLENAME_BROADCAST + "." + PROGRAM_ID + "=" + TABLENAME_INFO + "." + INFO_PROGRAM_ID + " AND "
              + TABLENAME_BROADCAST + "." + PROGRAM_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND "
              + TABLENAME_BROADCAST + "." + PROGRAM_ID + "=" + Long.toString(broadcastid), null, null, null, null);
    }
    return null;
  }

  private static String encrypt(String input) {
    if (input == null) {
      return "";
    }
    StringBuilder buffer = new StringBuilder();
    try {
      // Copyright by tvbrowser.org - Please do not change this part!
      int length = input.length();
      for (int i = 0; i < length; i++) {
        char position = input.charAt(i);
        position += 7;
        buffer.append(position);
      }
    } catch (Exception e) {
      return "error";
    }
    return buffer.toString().replace("'", "@_@");
  }

  public static String decrypt(String input) {
    if (input == null) {
      return "";
    }
    input = input.replaceAll("@_@", "'");
    StringBuilder buffer = new StringBuilder();
    int length = input.length();
    for (int i = 0; i < length; i++) {
      char c = input.charAt(i);
      c -= 7;
      buffer.append(c);
    }
    return buffer.toString();
  }

  private static SQLiteDatabase openInternalDatabase() {
    if (internalDatabase == null) {
      File file = new File(TVBROWSER_DATADIRECTORY + File.separator + INTERNALDATABASE);
      Log.d(TVBrowser.LOGTAG, file.getAbsolutePath());
      if (!file.exists()) {
        internalDatabase = SQLiteDatabase.openOrCreateDatabase(file, null);
        internalDatabase.execSQL("CREATE TABLE " + TABLENAME_REMINDER + " (" + REMINDER_ID
            + " INTEGER NOT NULL PRIMARY KEY," + REMINDER_CHANNEL + " VARCHAR(50) NOT NULL," + REMINDER_STARTDATE
            + " INTEGER NOT NULL," + REMINDER_STARTTIME + " INTEGER NOT NULL," + REMINDER_REMINDERTIME
            + " INTEGER NOT NULL," + REMINDER_TITLE + " VARCHAR(500) NOT NULL," + REMINDER_TIMEINMILLIS
            + " INTEGER NOT NULL)");
        internalDatabase.execSQL("CREATE UNIQUE INDEX idx_reminder ON " + TABLENAME_REMINDER + "(" + REMINDER_CHANNEL
            + "," + REMINDER_STARTDATE + "," + REMINDER_STARTTIME + ")");
      } else {
        internalDatabase = SQLiteDatabase.openDatabase(TVBROWSER_DATADIRECTORY + File.separator + INTERNALDATABASE,
            null, SQLiteDatabase.OPEN_READWRITE);
      }
    }
    deleteOldReminders(internalDatabase);
    return internalDatabase;
  }

  public static long getReminderId(long broadcastId) {
    Cursor cursor = getAllProgramInfos(broadcastId);
    long result = 0;
    if (cursor != null) {
      if (cursor.moveToNext()) {
        result = getReminderId(cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)), Utility.getTimeInMillis(cursor
            .getLong(cursor.getColumnIndex(PROGRAM_START_DATE_ID)), cursor.getInt(cursor
            .getColumnIndex(PROGRAM_STARTTIME))));
      }
      cursor.close();
    }
    return result;
  }

  private static long getReminderId(String channel, long startDate) {
    Log.d(TVBrowser.LOGTAG, "channel " + channel + ", startDate " + startDate);
    SQLiteDatabase database = openInternalDatabase();
    long result = 0;
    if (database != null) {
      Cursor cursor = database.query(TABLENAME_REMINDER, new String[] { REMINDER_ID }, REMINDER_CHANNEL + "=? AND "
          + REMINDER_STARTDATE + "=?", new String[] { channel, Long.toString(startDate) }, null, null, null);
      if (cursor != null) {
        if (cursor.moveToNext()) {
          result = cursor.getInt(cursor.getColumnIndex(REMINDER_ID));
        }
        cursor.close();
      }
    }
    return result;
  }

  public static int getReminderTime(String channel, long startDate) {
    Log.d(TVBrowser.LOGTAG, "channel " + channel + ", startDate " + startDate);
    SQLiteDatabase database = openInternalDatabase();
    int result = 0;
    if (database != null) {
      Cursor cursor = database
          .query(TABLENAME_REMINDER, new String[] { REMINDER_REMINDERTIME }, REMINDER_CHANNEL + "=? AND "
              + REMINDER_STARTDATE + "=?", new String[] { channel, Long.toString(startDate) }, null, null, null);
      if (cursor != null) {
        if (cursor.moveToNext()) {
          result = cursor.getInt(cursor.getColumnIndex(REMINDER_REMINDERTIME));
        }
        cursor.close();
      }
    }
    return result;
  }

  public static long getNextReminderTime() {
    long result = 0;
    SQLiteDatabase database = openInternalDatabase();
    if (database != null) {
      Cursor cursor = database.query(TABLENAME_REMINDER, new String[] { REMINDER_TIMEINMILLIS }, REMINDER_TIMEINMILLIS
          + ">?", new String[] { Long.toString(System.currentTimeMillis()) }, null, null, REMINDER_TIMEINMILLIS);
      if (cursor != null) {
        if (cursor.moveToNext()) {
          result = cursor.getLong(cursor.getColumnIndex(REMINDER_TIMEINMILLIS));
        }
        cursor.close();
      }
    }
    return result;
  }

  public static Cursor getReminderByTimeInMillis(long timeInMillis) {
    SQLiteDatabase database = openInternalDatabase();
    if (database != null) {
      return database.query(TABLENAME_REMINDER, new String[] { TABLENAME_REMINDER + ".*" }, REMINDER_TIMEINMILLIS + "="
          + Long.toString(timeInMillis), null, null, null, null);
    }
    return null;
  }

  public static void writeReminder(String broadcastTitle, long startDate, int startTime, String channel,
      int selectedReminderTime) {
    SQLiteDatabase database = openInternalDatabase();
    if (database != null) {
      long time = Utility.getTimeInMillis(startDate, startTime);
      long lastId = getReminderId(channel, time);
      ContentValues contentValues = new ContentValues();
      contentValues.put(REMINDER_CHANNEL, channel);
      contentValues.put(REMINDER_STARTDATE, startDate);
      contentValues.put(REMINDER_STARTTIME, startTime);
      contentValues.put(REMINDER_TITLE, broadcastTitle);
      contentValues.put(REMINDER_REMINDERTIME, selectedReminderTime);
      contentValues.put(REMINDER_TIMEINMILLIS, Utility.getTimeInMillis(time, -selectedReminderTime));
      if (lastId == 0) {
        database.insert(TABLENAME_REMINDER, null, contentValues);
      } else {
        database.update(TABLENAME_REMINDER, contentValues, REMINDER_ID + "=" + Long.toString(lastId), null);
      }
    }
  }

  public static void deleteOldReminder() {
    deleteOldReminders(openInternalDatabase());
  }

  private static void deleteOldReminders(SQLiteDatabase database) {
    if (database != null) {
      database.execSQL("DELETE FROM " + TABLENAME_REMINDER + " WHERE " + REMINDER_TIMEINMILLIS + "<"
          + System.currentTimeMillis());
    }
  }
}
