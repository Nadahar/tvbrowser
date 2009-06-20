package de.misi.tvbrowser.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.misi.tvbrowser.TVBrowser;
import de.misi.tvbrowser.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DataLoader {

   private static final String TVBROWSER_DATADIRECTORY = File.separator + "sdcard" + File.separator + "TVBrowser";

   private static final String DOT_TVBFILESUFFIX = ".tvd";

   private static final String TVBROWSER_DATABASEFILENAME = TVBROWSER_DATADIRECTORY + File.separator + "data" + DOT_TVBFILESUFFIX;

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
   private static final String BROADCAST_ID = "id";
   private static final String BROADCAST_CHANNEL_ID = "channel_id";
   public static final String BROADCAST_TITLE = "title";
   public static final String BROADCAST_START_DATE_ID = "start_date_id";
   public static final String BROADCAST_END_DATE_ID = "end_date_id";
   public static final String BROADCAST_STARTTIME = "starttime";
   public static final String BROADCAST_ENDTIME = "endtime";
   private static final String TABLENAME_INFO = "info";
   private static final String INFO_BROADCASTID = "broadcast_id";
   public static final String INFO_DESCRIPTION = "description";
   public static final String INFO_SHORTDESCRIPTION = "shortdescription";
   public static final String INFO_GENRE = "genre";
   private static final String INFO_PRODUCED = "produced";
   private static final String INFO_LOCATION = "location";
   private static final String INFO_DIRECTOR = "director";
   private static final String INFO_SCRIPT = "script";
   public static final String INFO_ACTOR = "actor";
   private static final String INFO_MUSIC = "music";
   private static final String INFO_ORIGINALTITEL = "originaltitel";
   private static final String INFO_FSK = "fsk";
   public static final String INFO_FORM = "form";
   private static final String INFO_SHOWVIEW = "showview";
   private static final String INFO_EPISODE = "episode";
   private static final String INFO_ORIGINALEPISODE = "originalepisode";
   private static final String INFO_MODERATION = "moderation";
   public static final String INFO_WEBSITE = "webside";
   private static final String INFO_VPS = "vps";
   public static final String INFO_REPETITIONON = "repetitionon";

   public static final String INFO_REPETITIONOF = "repetitionof";

   private static final String[] allInfoSearchFields = {
           INFO_DESCRIPTION,
           INFO_SHORTDESCRIPTION,
           INFO_GENRE,
           INFO_PRODUCED,
           INFO_LOCATION,
           INFO_DIRECTOR,
           INFO_SCRIPT,
           INFO_ACTOR,
           INFO_MUSIC,
           INFO_ORIGINALTITEL,
           INFO_FSK,
           INFO_FORM,
           INFO_SHOWVIEW,
           INFO_EPISODE,
           INFO_ORIGINALEPISODE,
           INFO_MODERATION,
           INFO_WEBSITE,
           INFO_VPS,
           INFO_REPETITIONON,
           INFO_REPETITIONOF
   };
   private static SQLiteDatabase database;
   private static SQLiteDatabase internalDatabase;

   private static SQLiteDatabase openDatabase() {
      if (database == null) {
         File file = new File(TVBROWSER_DATABASEFILENAME);
         if (file.exists())
            database = SQLiteDatabase.openDatabase(TVBROWSER_DATABASEFILENAME, null, SQLiteDatabase.OPEN_READWRITE);
      }
      return database;
   }

   public static Cursor getBroadcastDates() {
      SQLiteDatabase database = openDatabase();
      Cursor result = null;
      if (database != null) {
         result = database.query(TABLENAME_BROADCAST, new String[]{"DISTINCT " + BROADCAST_START_DATE_ID}, null, null, null, null, BROADCAST_START_DATE_ID);
      }
      return result;
   }

   public static ArrayList<Channel> loadChannelsFromDatabase(Calendar date) {
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MILLISECOND, 0);
      return loadChannelsFromDatabase(date.getTimeInMillis());
   }

   public static ArrayList<Channel> loadChannelsFromDatabase(long timeInMillis) {
      SQLiteDatabase database = openDatabase();
      ArrayList<Channel> result = new ArrayList<Channel>();
      if (database != null) {
         Cursor cursor = database.query(TABLENAME_CHANNEL, new String[]{CHANNEL_ID, CHANNEL_NAME}, null, null, null, null, null);
         int channelIdIndex = cursor.getColumnIndex(CHANNEL_ID);
         int channelNameIndex = cursor.getColumnIndex(CHANNEL_NAME);
         while (cursor.moveToNext()) {
            Channel channel = new Channel(cursor.getInt(channelIdIndex),
                    cursor.getString(channelNameIndex));
            channel.loadBroadcasts(timeInMillis);
            result.add(channel);
         }
         cursor.close();
      }
      return result;
   }

   public static ArrayList<Broadcast> loadBroadcastsFromDatabase(long currentDateInMillis, int channelId) {
      SQLiteDatabase database = openDatabase();
      ArrayList<Broadcast> broadcasts = new ArrayList<Broadcast>();
      Utility.tempCalendar.setTimeInMillis(currentDateInMillis);
      Utility.tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
      long nextDay = Utility.tempCalendar.getTimeInMillis();
      String startDateIdStr = Long.toString(currentDateInMillis);
      Cursor cursor = database.query(TABLENAME_BROADCAST,
              new String[]{BROADCAST_ID, BROADCAST_TITLE, BROADCAST_START_DATE_ID, BROADCAST_STARTTIME, BROADCAST_END_DATE_ID, BROADCAST_ENDTIME},
              BROADCAST_CHANNEL_ID + "=? AND" +
              " (" + BROADCAST_START_DATE_ID + "=? OR " + BROADCAST_END_DATE_ID + "=?)",
              new String[]{Integer.toString(channelId),
                           startDateIdStr, startDateIdStr
              },
              null, null, BROADCAST_START_DATE_ID + "," + BROADCAST_STARTTIME);
      if (cursor != null && cursor.getCount() > 0) {
         int broadcastIdIndex = cursor.getColumnIndex(BROADCAST_ID);
         int broadcastTitleIndex = cursor.getColumnIndex(BROADCAST_TITLE);
         int broadcastStartDateIndex = cursor.getColumnIndex(BROADCAST_START_DATE_ID);
         int broadcastStartIndex = cursor.getColumnIndex(BROADCAST_STARTTIME);
         int broadcastEndDateIndex = cursor.getColumnIndex(BROADCAST_END_DATE_ID);
         int broadcastEndIndex = cursor.getColumnIndex(BROADCAST_ENDTIME);
         while (cursor.moveToNext()) {
            broadcasts.add(new Broadcast(cursor.getInt(broadcastIdIndex),
                    cursor.getString(broadcastTitleIndex),
                    currentDateInMillis,
                    nextDay,
                    cursor.getLong(broadcastStartDateIndex),
                    cursor.getInt(broadcastStartIndex),
                    cursor.getLong(broadcastEndDateIndex),
                    cursor.getInt(broadcastEndIndex)
            ));
         }
         cursor.close();
      }
      return broadcasts;
   }

   public static long getBroadcastId(long startDate, int startTime, String channel) {
      SQLiteDatabase database = openDatabase();
      long result = 0;
      if (database != null) {
         Cursor cursor = database.query(TABLENAME_BROADCAST + "," + TABLENAME_CHANNEL,
                 new String[]{TABLENAME_BROADCAST + "." + BROADCAST_ID},
                 TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND " +
                 TABLENAME_CHANNEL + "." + CHANNEL_NAME + "=? AND " +
                 TABLENAME_BROADCAST + "." + BROADCAST_START_DATE_ID + "=? AND " +
                 TABLENAME_BROADCAST + "." + BROADCAST_STARTTIME + "=?",
                 new String[]{channel, Long.toString(startDate), Integer.toString(startTime)},
                 null, null, null, null);
         if (cursor != null) {
            if (cursor.moveToFirst())
               result = cursor.getLong(cursor.getColumnIndex(BROADCAST_ID));
            cursor.close();
         }
      }
      return result;
   }

   public static Cursor createSearchQuery(String searchText, int searchType, boolean onlyFuture) {
      SQLiteDatabase database = openDatabase();
      if (database != null) {
         String where = TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID;
         if (searchType == 1)
            where = where + " AND " + TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + TABLENAME_INFO + "." + INFO_BROADCASTID;
         String tables = TABLENAME_BROADCAST + ", " + TABLENAME_CHANNEL;
         String[] whereArgs;
         if (onlyFuture) {
            where = where + " AND " + BROADCAST_START_DATE_ID + ">?";
            whereArgs = new String[]{
                    Long.toString(System.currentTimeMillis()),
                    "%" + searchText + "%"
            };
         } else {
            whereArgs = new String[]{
                    "%" + searchText + "%"
            };
         }
         where = where + " AND (" + BROADCAST_TITLE + " LIKE ?";
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
         return createSearchQuery(database,
                 TABLENAME_BROADCAST + ", " + TABLENAME_CHANNEL,
                 TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND " + where,
                 whereArgs);
      }
      return null;
   }

   private static Cursor createSearchQuery(SQLiteDatabase database, String tables, String where, String[] whereArgs) {
      return database.query(tables,
              new String[]{BROADCAST_TITLE,
                           BROADCAST_STARTTIME,
                           CHANNEL_NAME,
                           BROADCAST_START_DATE_ID,
                           TABLENAME_BROADCAST + "." + BROADCAST_ID + " AS _id"},
              where,
              whereArgs,
              null,
              null,
              BROADCAST_START_DATE_ID + "," + BROADCAST_STARTTIME);
   }

   public static Cursor getAllBroadcastInfos(long broadcastid) {
      SQLiteDatabase database = openDatabase();
      if (database != null) {
         return database.query(TABLENAME_BROADCAST + "," + TABLENAME_CHANNEL + "," + TABLENAME_INFO,
                 new String[]{TABLENAME_BROADCAST + ".*",
                              TABLENAME_INFO + ".*",
                              TABLENAME_CHANNEL + "." + CHANNEL_NAME
                 },
                 TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + TABLENAME_INFO + "." + INFO_BROADCASTID + " AND " +
                 TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND " +
                 TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + Long.toString(broadcastid),
                 null,
                 null,
                 null,
                 null);
      }
      return null;
   }

   private static String encrypt(String text) {
      String result = "";

      if (text != null)
         try {
            //Copyright by tvbrowser.org - Please do not change this part!
            for (int i = 0; i < text.length(); i++) {
               int position = (int) text.charAt(i);
               position = position + 7;
               result += (char) position;
            }
         }
         catch (Exception e) {
            result = "error";
         }
      result = result.replace("'", "@_@");
      return result;
   }

   public static String decrypt(String value) {
      String result = "";
      if (value != null) {
         value = value.replaceAll("@_@", "'");
         for (int i = 0; i < value.length(); i++) {
            int position = value.charAt(i);
            position = position - 7;
            result += (char) position;
         }
      }
      return result;
   }

   private static SQLiteDatabase openInternalDatabase() {
      if (internalDatabase == null) {
         File file = new File(TVBROWSER_DATADIRECTORY + File.separator + INTERNALDATABASE);
         Log.d(TVBrowser.LOGTAG, file.getAbsolutePath());
         if (!file.exists()) {
            internalDatabase = SQLiteDatabase.openOrCreateDatabase(file, null);
            internalDatabase.execSQL("CREATE TABLE " + TABLENAME_REMINDER + " (" +
                                     REMINDER_ID + " INTEGER NOT NULL PRIMARY KEY," +
                                     REMINDER_CHANNEL + " VARCHAR(50) NOT NULL," +
                                     REMINDER_STARTDATE + " INTEGER NOT NULL," +
                                     REMINDER_STARTTIME + " INTEGER NOT NULL," +
                                     REMINDER_REMINDERTIME + " INTEGER NOT NULL," +
                                     REMINDER_TITLE + " VARCHAR(500) NOT NULL," +
                                     REMINDER_TIMEINMILLIS + " INTEGER NOT NULL)");
            internalDatabase.execSQL("CREATE UNIQUE INDEX idx_reminder ON " + TABLENAME_REMINDER + "(" + REMINDER_CHANNEL + "," + REMINDER_STARTDATE + "," + REMINDER_STARTTIME + ")");
         } else {
            internalDatabase = SQLiteDatabase.openDatabase(TVBROWSER_DATADIRECTORY + File.separator + INTERNALDATABASE, null, SQLiteDatabase.OPEN_READWRITE);
         }
      }
      deleteOldReminder(internalDatabase);
      return internalDatabase;
   }

   public static long getReminderId(long broadcastId) {
      Cursor cursor = getAllBroadcastInfos(broadcastId);
      long result = 0;
      if (cursor != null) {
         if (cursor.moveToNext()) {
            result = getReminderId(cursor.getString(cursor.getColumnIndex(CHANNEL_NAME)),
                    Utility.getTimeInMillis(cursor.getLong(cursor.getColumnIndex(BROADCAST_START_DATE_ID)),
                            cursor.getInt(cursor.getColumnIndex(BROADCAST_STARTTIME))));
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
         Cursor cursor = database.query(TABLENAME_REMINDER,
                 new String[]{REMINDER_ID},
                 REMINDER_CHANNEL + "=? AND " + REMINDER_STARTDATE + "=?",
                 new String[]{channel, Long.toString(startDate)},
                 null, null, null);
         if (cursor != null) {
            if (cursor.moveToNext())
               result = cursor.getInt(cursor.getColumnIndex(REMINDER_ID));
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
         Cursor cursor = database.query(TABLENAME_REMINDER,
                 new String[]{REMINDER_REMINDERTIME},
                 REMINDER_CHANNEL + "=? AND " + REMINDER_STARTDATE + "=?",
                 new String[]{channel, Long.toString(startDate)},
                 null, null, null);
         if (cursor != null) {
            if (cursor.moveToNext())
               result = cursor.getInt(cursor.getColumnIndex(REMINDER_REMINDERTIME));
            cursor.close();
         }
      }
      return result;
   }

   public static long getNextReminderTime() {
      long result = 0;
      SQLiteDatabase database = openInternalDatabase();
      if (database != null) {
         Cursor cursor = database.query(TABLENAME_REMINDER,
                 new String[]{REMINDER_TIMEINMILLIS},
                 REMINDER_TIMEINMILLIS + ">?",
                 new String[]{Long.toString(System.currentTimeMillis())},
                 null, null,
                 REMINDER_TIMEINMILLIS);
         if (cursor != null) {
            if (cursor.moveToNext())
               result = cursor.getLong(cursor.getColumnIndex(REMINDER_TIMEINMILLIS));
            cursor.close();
         }
      }
      return result;
   }

   public static Cursor getReminderByTimeInMillis(long timeInMillis) {
      SQLiteDatabase database = openInternalDatabase();
      if (database != null) {
         return database.query(TABLENAME_REMINDER,
                 new String[]{TABLENAME_REMINDER + ".*"},
                 REMINDER_TIMEINMILLIS + "=" + Long.toString(timeInMillis),
                 null, null, null, null);
      }
      return null;
   }

   public static void writeReminder(String broadcastTitle, long startDate, int startTime, String channel, int selectedReminderTime) {
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
         if (lastId == 0)
            database.insert(TABLENAME_REMINDER, null, contentValues);
         else
            database.update(TABLENAME_REMINDER, contentValues, REMINDER_ID + "=" + Long.toString(lastId), null);
      }
   }

   public static void deleteOldReminder() {
      deleteOldReminder(openInternalDatabase());
   }

   private static void deleteOldReminder(SQLiteDatabase database) {
      if (database != null) {
         database.execSQL("DELETE FROM " + TABLENAME_REMINDER + " WHERE " + REMINDER_TIMEINMILLIS + "<" + System.currentTimeMillis());
      }
   }
}
