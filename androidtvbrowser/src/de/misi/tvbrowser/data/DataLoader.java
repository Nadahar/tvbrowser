package de.misi.tvbrowser.data;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.misi.tvbrowser.activities.search.SearchResult;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataLoader {

   public static final String TVBROWSER_DATADIRECTORY = File.separator + "sdcard" + File.separator + "TVBrowser";
   public static final SimpleDateFormat SQLDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
   public static final SimpleDateFormat SQLTIMEFORMAT = new SimpleDateFormat("HH:mm");
   public static final String DOT_TVBFILESUFFIX = ".tvd";

   private static final String TABLENAME_CHANNEL = "channel";
   private static final String CHANNEL_ID = "id";
   public static final String CHANNEL_NAME = "name";
   private static final String TABLENAME_BROADCAST = "broadcast";
   public static final String BROADCAST_ID = "id";
   private static final String BROADCAST_CHANNEL_ID = "channel_id";
   public static final String BROADCAST_TITLE = "title";
   public static final String BROADCAST_START_DATE_ID = "start_date_id";
   public static final String BROADCAST_END_DATE_ID = "end_date_id";
   public static final String BROADCAST_STARTTIME = "starttime";
   public static final String BROADCAST_ENDTIME = "endtime";
   private static final String TABLENAME_DATES = "dates";
   private static final String DATES_ID = "id";
   public static final String DATES_DATUM = "datum";
   public static final String TABLENAME_INFO = "info";
   public static final String INFO_BROADCASTID = "broadcast_id";
   public static final String INFO_DESCRIPTION = "description";
   public static final String INFO_SHORTDESCRIPTION = "shortdescription";
   public static final String INFO_GENRE = "genre";
   public static final String INFO_PRODUCED = "produced";
   public static final String INFO_LOCATION = "location";
   public static final String INFO_DIRECTOR = "director";
   public static final String INFO_SCRIPT = "script";
   public static final String INFO_ACTOR = "actor";
   public static final String INFO_MUSIC = "music";
   public static final String INFO_ORIGINALTITEL = "originaltitel";
   public static final String INFO_FSK = "fsk";
   public static final String INFO_FORM = "form";
   public static final String INFO_SHOWVIEW = "showview";
   public static final String INFO_EPISODE = "episode";
   public static final String INFO_ORIGINALEPISODE = "originalepisode";
   public static final String INFO_MODERATION = "moderation";
   public static final String INFO_WEBSITE = "webside";
   public static final String INFO_VPS = "vps";
   public static final String INFO_REPETITIONON = "repetitionon";
   public static final String INFO_REPETITIONOF = "repetitionof";

   public static final String[] allInfoSearchFields = {
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

   private static SQLiteDatabase database = null;

   private static Map<Long, Date> dateCache = new HashMap<Long, Date>();

   private static SQLiteDatabase openDatabase() {
      if (database == null) {
         database = SQLiteDatabase.openDatabase(TVBROWSER_DATADIRECTORY + File.separator + "data" + DOT_TVBFILESUFFIX, null, SQLiteDatabase.OPEN_READWRITE);
      }
      return database;
   }

   public static Cursor getBroadcastDates() {
      SQLiteDatabase database = openDatabase();
      Cursor result = null;
      if (database != null) {
         result = database.query(TABLENAME_DATES, new String[]{DATES_DATUM}, null, null, null, null, DATES_DATUM);
      }
      return result;
   }

   public static ArrayList<Channel> loadChannelsFromDatabase(Calendar date) {
      SQLiteDatabase database = openDatabase();
      ArrayList<Channel> result = new ArrayList<Channel>();
      if (database != null) {
         Cursor cursor = database.query(TABLENAME_CHANNEL, new String[]{CHANNEL_ID, CHANNEL_NAME}, null, null, null, null, null);
         int channelIdIndex = cursor.getColumnIndex(CHANNEL_ID);
         int channelNameIndex = cursor.getColumnIndex(CHANNEL_NAME);
         while (cursor.moveToNext()) {
            Channel channel = new Channel(cursor.getInt(channelIdIndex),
                    cursor.getString(channelNameIndex));
            channel.loadBroadcasts(date);
            result.add(channel);
         }
         cursor.close();
      }
      return result;
   }

   public static ArrayList<Broadcast> loadBroadcastsFromDatabase(Calendar date, int id) {
      SQLiteDatabase database = openDatabase();
      ArrayList<Broadcast> broadcasts = new ArrayList<Broadcast>();
      int startDateId = getDateId(date);
      String startDateIdStr = Integer.toString(startDateId);
      Cursor cursor = database.query(TABLENAME_BROADCAST,
              new String[]{BROADCAST_ID, BROADCAST_TITLE, BROADCAST_STARTTIME, BROADCAST_ENDTIME},
              BROADCAST_CHANNEL_ID + "=? AND" +
                      " (" + BROADCAST_START_DATE_ID + "=?" +
                      " OR (" + BROADCAST_START_DATE_ID + "=? AND " + BROADCAST_END_DATE_ID + "=?)" +
                      " OR (" + BROADCAST_START_DATE_ID + "=? AND " + BROADCAST_END_DATE_ID + "=?))",
              new String[]{Integer.toString(id),
                      startDateIdStr,
                      Integer.toString(startDateId - 1), startDateIdStr,
                      startDateIdStr, Integer.toString(startDateId + 1)
              },
              null, null, BROADCAST_START_DATE_ID + "," + BROADCAST_STARTTIME);
      if (cursor != null) {
         int broadcastIdIndex = cursor.getColumnIndex(BROADCAST_ID);
         int broadcastTitleIndex = cursor.getColumnIndex(BROADCAST_TITLE);
         int broadcastStartIndex = cursor.getColumnIndex(BROADCAST_STARTTIME);
         int broadcastEndIndex = cursor.getColumnIndex(BROADCAST_ENDTIME);
         boolean firstBroadcastOfDay = true;
         while (cursor.moveToNext()) {
            broadcasts.add(new Broadcast(cursor.getInt(broadcastIdIndex),
                    cursor.getString(broadcastTitleIndex),
                    date,
                    cursor.getString(broadcastStartIndex),
                    cursor.getString(broadcastEndIndex),
                    firstBroadcastOfDay));
            firstBroadcastOfDay = false;
         }
         cursor.close();
      }
      return broadcasts;
   }

   private static int getDateId(Calendar date) {
      return getDateId(SQLDATEFORMAT.format(date.getTime()));
   }

   private static int getDateId(String date) {
      SQLiteDatabase database = openDatabase();
      Cursor cursor = database.query(TABLENAME_DATES, new String[]{DATES_ID}, DATES_DATUM + "=?", new String[]{date}, null, null, null, null);
      int result = 0;
      if (cursor != null) {
         if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(DATES_ID));
         cursor.close();
      }
      return result;
   }

   public static Cursor createSearchQuery(Intent intent) {
      SQLiteDatabase database = openDatabase();
      if (database != null) {
         String searchtext = intent.getStringExtra(SearchResult.SEARCHTEXT);
         int searchtype = intent.getIntExtra(SearchResult.SEARCHTYPE, 0);
         String where = TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID;
         if (searchtype == 1)
            where = where + " AND " + TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + TABLENAME_INFO + "." + INFO_BROADCASTID;
         where = where + " AND (" + BROADCAST_TITLE + " LIKE ?";
         String tables = TABLENAME_BROADCAST + ", " + TABLENAME_CHANNEL;
         String[] whereArgs = {
                 "%" + searchtext + "%"
         };
         if (searchtype == 1) {
            tables = tables + ", " + TABLENAME_INFO;
            String encryptedtext = encrypt(searchtext);
            for (String infoSearchField : allInfoSearchFields) {
               where = where + " OR " + TABLENAME_INFO + "." + infoSearchField + " LIKE '%" + encryptedtext + "%'";
            }
         }
         where = where + ")";
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
      return null;
   }

   public static Cursor getAllBroadcastInfos(long broadcastid) {
      SQLiteDatabase database = openDatabase();
      if (database != null) {
         return database.query(TABLENAME_BROADCAST + "," + TABLENAME_CHANNEL + "," + TABLENAME_INFO,
                 new String[]{TABLENAME_BROADCAST + ".*",
                         TABLENAME_INFO + ".*",
                         TABLENAME_CHANNEL + "." + CHANNEL_NAME},
                 TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + TABLENAME_INFO + "." + INFO_BROADCASTID + " AND " + TABLENAME_BROADCAST + "." + BROADCAST_CHANNEL_ID + "=" + TABLENAME_CHANNEL + "." + CHANNEL_ID + " AND " + TABLENAME_BROADCAST + "." + BROADCAST_ID + "=" + Long.toString(broadcastid),
                 null,
                 null,
                 null,
                 null);
      }
      return null;
   }

   public static Date getDateByDataId(long dateId) {
      if (dateCache.containsKey(dateId)) {
         return dateCache.get(dateId);
      } else {
         SQLiteDatabase database = openDatabase();
         if (database != null) {
            Cursor cursor = database.query(TABLENAME_DATES, new String[]{DATES_DATUM}, DATES_ID + "=" + Long.toString(dateId), null, null, null, null);
            if (cursor != null) {
               try {
                  if (cursor.moveToFirst()) {
                     Date date = SQLDATEFORMAT.parse(cursor.getString(cursor.getColumnIndex(DATES_DATUM)));
                     dateCache.put(dateId, date);
                     return date;
                  }
               } catch (ParseException e) {
               } finally {
                  cursor.close();
               }
            }
         }
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
}
