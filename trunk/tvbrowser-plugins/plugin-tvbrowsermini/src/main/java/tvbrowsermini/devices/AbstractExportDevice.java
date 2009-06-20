package tvbrowsermini.devices;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import util.exc.ErrorHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.awt.*;

import tvbrowsermini.TVBrowserMini;

public abstract class AbstractExportDevice {

  protected boolean elementDescription;
  protected boolean elementShortDescription;
  protected boolean elementGenre;
  protected boolean elementProductionTime;
  protected boolean elementProductionLocation;
  protected boolean elementDirector;
  protected boolean elementScript;
  protected boolean elementActor;
  protected boolean elementMusic;
  protected boolean elementOriginalTitel;
  protected boolean elementFSK;
  protected boolean elementForminformation;
  protected boolean elementShowView;
  protected boolean elementEpisode;
  protected boolean elementOriginalEpisode;
  protected boolean elementModeration;
  protected boolean elementWebside;
  protected boolean elementVPS;
  protected boolean elementRepetitionOn;
  protected boolean elementRepetitionOf;

  protected Properties mSettings;
  protected Channel[] mSelectedChannels;
  protected JProgressBar progress;

  private Map<String, Integer> dbChannelIds;
  private Map<String, Long> mDateIds;

   protected AbstractExportDevice(Properties mSettings, Channel[] mSelectedChannels, JProgressBar progress) {
    this.mSettings = mSettings;
    this.mSelectedChannels = mSelectedChannels;
    this.progress = progress;
    loadSettings();
  }

  private void loadSettings() {
    elementDescription = isPropertyTrue("elementDescription");
    elementShortDescription = isPropertyTrue("elementShortDescription");
    elementGenre = isPropertyTrue("elementGenre");
    elementProductionTime = isPropertyTrue("elementProductionTime");
    elementProductionLocation = isPropertyTrue("elementProductionLocation");
    elementDirector = isPropertyTrue("elementDirector");
    elementScript = isPropertyTrue("elementScript");
    elementActor = isPropertyTrue("elementActor");
    elementMusic = isPropertyTrue("elementMusic");
    elementOriginalTitel = isPropertyTrue("elementOriginalTitel");
    elementFSK = isPropertyTrue("elementFSK");
    elementForminformation = isPropertyTrue("elementForminformation");
    elementShowView = isPropertyTrue("elementShowView");
    elementEpisode = isPropertyTrue("elementEpisode");
    elementOriginalEpisode = isPropertyTrue("elementOriginalEpisode");
    elementModeration = isPropertyTrue("elementModeration");
    elementWebside = isPropertyTrue("elementWebside");
    elementVPS = isPropertyTrue("elementVPS");
    elementRepetitionOn = isPropertyTrue("elementRepetitionOn");
    elementRepetitionOf = isPropertyTrue("elementRepetitionOf");
  }

  private boolean isPropertyTrue(String propertyName) {
    return mSettings.getProperty(propertyName).equals("true");
  }

  public void export(Frame parentFrame) {
    File file = new File(mSettings.getProperty("path") + "_temp");
    //Wenn bereits eine DB-Datei besteht einfach löschen - das geht schneller als leeren
    if (file.exists()) {
      file.delete();
    }
    try {
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String oldUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome());
    String pfad = mSettings.getProperty("path");
    pfad = pfad.replace('\\', '/');
    if (pfad.charAt(1) == ':') {
      char first = pfad.charAt(0);
      first = Character.toLowerCase(first);
      pfad = first + pfad.substring(1, pfad.length());
    } else {
      pfad = "/" + pfad;
    }
    //SQL-Verbindung zur neuen DB-Datei
    Connection conn = null;
    Statement stmt = null;
    try {

      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:/" + pfad + "_temp");//+mSettings.getProperty("path"));
      System.setProperty("user.dir", oldUserDir);

      stmt = conn.createStatement();
      createTables(conn, stmt);

      try {
        exportFile(conn, parentFrame, stmt);
        File temp = new File(mSettings.getProperty("path"));
        if (temp.exists()) {
          temp.delete();
        }
        file.renameTo(temp);
      } catch (IOException e) {
        e.printStackTrace();
        ErrorHandler.handle(TVBrowserMini.mLocalizer.msg("error", "Error while exporting tv-data!"), e);
      }
      progress.setString(TVBrowserMini.mLocalizer.msg("creatingIndices", "Creating indices"));
      progress.setStringPainted(true);
      createIndices(conn, stmt);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.out.println(e.toString());
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  protected abstract void createTables(Connection connection, Statement stmt) throws SQLException;

  protected abstract void createIndices(Connection conn, Statement stmt) throws SQLException;

  protected abstract void exportFile(Connection connection, Frame parentFrame, Statement stmt) throws IOException;

  protected void createDateIds() throws SQLException {
    mDateIds = new HashMap<String, Long>();
    Date date = new Date();
    Calendar currentCalendar = date.getCalendar();
    currentCalendar.set(Calendar.HOUR, 0);
    currentCalendar.set(Calendar.MINUTE, 0);
    currentCalendar.set(Calendar.SECOND, 0);
    currentCalendar.set(Calendar.MILLISECOND, 0);
    date = new Date(currentCalendar);
    date = date.addDays(-2);
    int maxDays = Integer.parseInt(mSettings.getProperty("exportDays"));
    if (maxDays == 0)
      maxDays = 32;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    for (int d = 0; d <= maxDays; d++) {
      date = date.addDays(1);
      Calendar calendar = date.getCalendar();
      String formattedDate = dateFormat.format(calendar.getTime());
      try {
         calendar.setTime(dateFormat.parse(formattedDate));
      } catch (ParseException e) {
      }
      long id = calendar.getTimeInMillis();
      mDateIds.put(formattedDate, id);
    }
  }

  protected void exportChannels(Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("INSERT INTO channel (id, channelid, name, category) VALUES (?, ?, ?, ?)");
    int id = 1;
    dbChannelIds = new HashMap<String, Integer>();
    for (Channel channel : mSelectedChannels) {
      int categories = channel.getCategories();
      String channelId = getChannelId(channel);
      statement.setInt(1, id);
      statement.setString(2, channelId);
      statement.setString(3, channel.getName());
      statement.setString(4, categories == 0 || categories == 2 ? "r" : "t");
      statement.execute();
      dbChannelIds.put(channelId, id);
      id++;
    }
    statement.close();
  }

  protected int findDBChannel(Channel selectedChannel) {
    return dbChannelIds.get(getChannelId(selectedChannel));
  }

  protected String getChannelId(Channel channel) {
    return new StringBuffer(channel.getDataServiceProxy().getId()).append(":").append(channel.getGroup().getId()).append(":").append(channel.getCountry()).append(":").append(channel.getId()).toString();
  }

  protected long findDBDate(String date) {
    return mDateIds.get(date);
  }

  protected String encrypt(String text) {
    String result = "";

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

  protected boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  protected StringBuilder getFormatInformation(Program program, StringBuilder info) {
    int inf = program.getInfo();

    if (inf > 0) {
      if (bitSet(inf, Program.INFO_AUDIO_DOLBY_SURROUND)) {
        info.append("|Dolby Surround");
      }
      if (bitSet(inf, Program.INFO_AUDIO_DOLBY_DIGITAL_5_1)) {
        info.append("|Dolby Digital 5.1");
      }
      if (bitSet(inf, Program.INFO_AUDIO_MONO)) {
        info.append("|Mono");
      }
      if (bitSet(inf, Program.INFO_AUDIO_TWO_CHANNEL_TONE)) {
        info.append("|Two Channel Tone");
      }
      if (bitSet(inf, Program.INFO_LIVE)) {
        info.append("|Live");
      }
      if (bitSet(inf, Program.INFO_ORIGINAL_WITH_SUBTITLE)) {
        info.append("|Subtitel");
      }
      if (bitSet(inf, Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED)) {
        info.append("|Aurally Handicapped");
      }
      if (bitSet(inf, Program.INFO_VISION_16_TO_9)) {
        info.append("|16:9");
      }
      if (bitSet(inf, Program.INFO_VISION_4_TO_3)) {
        info.append("|4:3");
      }
      if (bitSet(inf, Program.INFO_VISION_BLACK_AND_WHITE)) {
        info.append("|black and white");
      }
      if (info.length() > 0) {
        info = info.deleteCharAt(0); //Delete first seperator
      }
    }
    return info;
  }
}
