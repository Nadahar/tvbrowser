package tvbrowsermini.devices;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JProgressBar;

import tvbrowsermini.TVBrowserMini;
import tvbrowsermini.TVBrowserMiniSettings;
import util.exc.ErrorHandler;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

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

  protected TVBrowserMiniSettings mSettings;
  protected Channel[] mSelectedChannels;
  protected JProgressBar progress;

  protected abstract void createTables(Connection connection, Statement stmt) throws SQLException;

  protected abstract void createIndices(Connection conn, Statement stmt) throws SQLException;

  protected abstract void exportFile(Connection connection, Frame parentFrame, Statement stmt) throws IOException;

  protected abstract void exportChannels(Connection connection) throws SQLException;

  protected AbstractExportDevice(final TVBrowserMiniSettings settings, final Channel[] selectedChannels,
      final JProgressBar progress) {
    this.mSettings = settings;
    this.mSelectedChannels = selectedChannels;
    this.progress = progress;
    loadSettings();
  }

  private void loadSettings() {
    elementDescription = isExportField(ProgramFieldType.DESCRIPTION_TYPE);
    elementShortDescription = isExportField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
    elementGenre = isExportField(ProgramFieldType.GENRE_TYPE);
    elementProductionTime = isExportField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
    elementProductionLocation = isExportField(ProgramFieldType.ORIGIN_TYPE);
    elementDirector = isExportField(ProgramFieldType.DIRECTOR_TYPE);
    elementScript = isExportField(ProgramFieldType.SCRIPT_TYPE);
    elementActor = isExportField(ProgramFieldType.ACTOR_LIST_TYPE);
    elementMusic = isExportField(ProgramFieldType.MUSIC_TYPE);
    elementOriginalTitel = isExportField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    elementFSK = isExportField(ProgramFieldType.AGE_LIMIT_TYPE);
    elementForminformation = isExportField(ProgramFieldType.INFO_TYPE);
    elementEpisode = isExportField(ProgramFieldType.EPISODE_TYPE);
    elementOriginalEpisode = isExportField(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    elementModeration = isExportField(ProgramFieldType.MODERATION_TYPE);
    elementWebside = isExportField(ProgramFieldType.URL_TYPE);
    elementVPS = isExportField(ProgramFieldType.VPS_TYPE);
    elementRepetitionOn = isExportField(ProgramFieldType.REPETITION_ON_TYPE);
    elementRepetitionOf = isExportField(ProgramFieldType.REPETITION_OF_TYPE);
  }

  private boolean isExportField(final ProgramFieldType fieldType) {
    return mSettings.getProgramField(fieldType);
  }

  public void export(Frame parentFrame) {
    File file = new File(mSettings.getPath() + "_temp");
    // Wenn bereits eine DB-Datei besteht einfach l�schen - das geht schneller
    // als leeren
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
    String path = mSettings.getPath();
    path = path.replace('\\', '/');
    if (path.charAt(1) == ':') {
      char first = path.charAt(0);
      first = Character.toLowerCase(first);
      path = first + path.substring(1, path.length());
    } else {
      path = "/" + path;
    }
    // SQL-Verbindung zur neuen DB-Datei
    Connection conn = null;
    Statement stmt = null;
    try {

      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:/" + path + "_temp");// +mSettings.getProperty("path"));
      System.setProperty("user.dir", oldUserDir);

      stmt = conn.createStatement();
      createTables(conn, stmt);

      try {
        exportFile(conn, parentFrame, stmt);
        File temp = new File(mSettings.getPath());
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

  protected String getChannelId(Channel channel) {
    return new StringBuffer(channel.getDataServiceProxy().getId()).append(":").append(channel.getGroup().getId())
        .append(":").append(channel.getCountry()).append(":").append(channel.getId()).toString();
  }

  protected String encrypt(String text) {
    int length = text.length();
    StringBuilder result = new StringBuilder(length);
    // Copyright by tvbrowser.org - Please do not change this part!
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      c += 7;
      result.append(c);
    }
    return result.toString().replace("'", "@_@");
  }

  private boolean bitSet(int num, int pattern) {
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
        info = info.deleteCharAt(0); // Delete first separator
      }
    }
    return info;
  }
}
