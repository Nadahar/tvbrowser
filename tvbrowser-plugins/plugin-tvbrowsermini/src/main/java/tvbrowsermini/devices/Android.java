package tvbrowsermini.devices;

import java.awt.Frame;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import tvbrowsermini.TVBrowserMiniSettings;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class Android extends AbstractExportDevice {

  private Map<String, Integer> dbChannelIds;

  public Android(TVBrowserMiniSettings settings, Channel[] mSelectedChannels, JProgressBar progress) {
    super(settings, mSelectedChannels, progress);
  }

  protected void createTables(Connection connection, Statement stmt) throws SQLException {
    stmt.execute("CREATE TABLE channel (id INTEGER NOT NULL PRIMARY KEY, channelid VARCHAR(100), name VARCHAR(50), category VARCHAR(1))");
    stmt.execute("CREATE TABLE broadcast (" +
            "id INTEGER NOT NULL PRIMARY KEY, " +
            "channel_id INTEGER NOT NULL, " +
            "title VARCHAR(100), " +
            "start_date_id INTEGER NOT NULL, " +
            "end_date_id INTEGER NOT NULL, " +
            "starttime INTEGER, " +
            "endtime INTEGER, " +
            "favorite BOOLEAN, " +
            "reminder BOOLEAN)");
    stmt.execute("CREATE TABLE info (" +
            "broadcast_id INTEGER NOT NULL PRIMARY KEY, " +
            "originaltitel VARCHAR(100), " +
            "episode VARCHAR(100), " +
            "originalepisode VARCHAR(100), " +
            "shortdescription VARCHAR(400), " +
            "description TEXT, " +
            "genre VARCHAR(150), " +
            "produced VARCHAR(100), " +
            "location VARCHAR(100), " +
            "director VARCHAR(200), " +
            "moderation VARCHAR(200), " +
            "script VARCHAR(200), " +
            "actor VARCHAR(2000), " +
            "music VARCHAR(150), " +
            "fsk VARCHAR(5), " +
            "form INTEGER, " +
            "showview VARCHAR(12), " +
            "webside VARCHAR(150), " +
            "vps VARCHAR(100), " +
            "repetitionon VARCHAR(100), " +
            "repetitionof VARCHAR(100))");
  }

  protected void createIndices(Connection conn, Statement stmt) throws SQLException {
    stmt.execute("CREATE INDEX reminder ON broadcast(reminder)");
    stmt.execute("CREATE INDEX favorite ON broadcast(favorite)");
    stmt.execute("CREATE INDEX channelid ON broadcast(channel_id)");
    stmt.execute("CREATE INDEX genre ON info(genre)");
    stmt.execute("CREATE INDEX startid ON broadcast(start_date_id)");
    stmt.execute("CREATE INDEX endid ON broadcast(end_date_id)");
  }

  /**
   * Export the Data to a file
   *
   * @throws java.io.IOException Exception while writing
   */
  protected void exportFile(Connection connection, Frame parentFrame, Statement stmt) throws IOException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      exportChannels(connection);
      createDateIds();
      Date date = new Date();
      date = date.addDays(-2);
      Calendar calStart = Calendar.getInstance();
      Calendar calEnd;

      int counter = 0;

      int maxDays = mSettings.getDaysToExport();

      PreparedStatement broadcastStatement = connection.prepareStatement("INSERT INTO broadcast (id, channel_id, title, start_date_id, end_date_id, starttime, endtime, favorite, reminder) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
      PreparedStatement infoStatement = connection.prepareStatement("INSERT INTO info " +
              "(broadcast_id, description, shortdescription, genre, produced, location, director, script, actor, music, originaltitel, fsk, form, showview, episode, originalepisode, moderation, webside, vps, repetitionon, repetitionof) VALUES " +
              "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      int broadcastId = 0;
      for (int d = 0; d < maxDays; d++) {
        date = date.addDays(1);
        for (int i = 0; i < mSelectedChannels.length; i++) {
          progress.setValue(progress.getValue() + 1);
          progress.setString(date.getLongDateString() + ": " + (i+1) + "/" + (mSelectedChannels.length));

          Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, mSelectedChannels[i]);
          if (it != null) {
            while (it.hasNext()) {
              Program program = it.next();
              if (program != null) {
                boolean isReminder = false;
                boolean isFavorite = false;

                Marker[] markerArr = program.getMarkerArr();
                for (Marker marker : markerArr) {
                  if (marker.getId().equals("reminderplugin.ReminderPlugin")) {
                    isReminder = true;
                  } else if (marker.getId().equals("favoritesplugin.FavoritesPlugin")) {
                    isFavorite = true;
                  }
                }

                calStart.set(date.getYear(), date.getMonth() - 1, date.getDayOfMonth(), program.getHours(), program.getMinutes(), 0);
                calEnd = (Calendar) calStart.clone();
                calEnd.add(Calendar.MINUTE, program.getLength());

                broadcastStatement.clearParameters();
                infoStatement.clearParameters();

                broadcastStatement.setInt(1, ++broadcastId);
                broadcastStatement.setInt(2, findDBChannel(mSelectedChannels[i]));
                broadcastStatement.setString(3, program.getTitle());
                broadcastStatement.setLong(4, findDBDate(dateFormat.format(calStart.getTime())));
                broadcastStatement.setLong(5, findDBDate(dateFormat.format(calEnd.getTime())));
                broadcastStatement.setInt(6, calStart.get(Calendar.HOUR_OF_DAY)*60+calStart.get(Calendar.MINUTE));
                broadcastStatement.setInt(7, calEnd.get(Calendar.HOUR_OF_DAY)*60+calEnd.get(Calendar.MINUTE));
                broadcastStatement.setInt(8, isFavorite ? 1 : 0);
                broadcastStatement.setInt(9, isReminder ? 1 : 0);

                infoStatement.setInt(1, broadcastId);
                boolean description = false;
                if (elementDescription && program.getTextField(ProgramFieldType.DESCRIPTION_TYPE) != null) {
                  infoStatement.setString(2, encrypt(program.getTextField(ProgramFieldType.DESCRIPTION_TYPE)));
                  description = true;
                }
                if (elementShortDescription && program.getShortInfo() != null) {
                  if (description && elementDescription) {
                    if (program.getShortInfo().length() <= 3) {
                      infoStatement.setString(3, encrypt(program.getShortInfo()));
                    }
                  } else {
                    infoStatement.setString(3, encrypt(program.getShortInfo()));
                  }
                }
                setValueToInfoStatement(infoStatement, elementGenre, 4, program.getTextField(ProgramFieldType.GENRE_TYPE));
                if (elementProductionTime && program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE) >= 0) {
                  infoStatement.setString(5, encrypt(String.valueOf(program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE))));
                }
                setValueToInfoStatement(infoStatement, elementProductionLocation, 6, program.getTextField(ProgramFieldType.ORIGIN_TYPE));
                setValueToInfoStatement(infoStatement, elementDirector, 7, program.getTextField(ProgramFieldType.DIRECTOR_TYPE));
                setValueToInfoStatement(infoStatement, elementScript, 8, program.getTextField(ProgramFieldType.SCRIPT_TYPE));
                setValueToInfoStatement(infoStatement, elementActor, 9, program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE));
                setValueToInfoStatement(infoStatement, elementMusic, 10, program.getTextField(ProgramFieldType.MUSIC_TYPE));
                setValueToInfoStatement(infoStatement, elementOriginalTitel, 11, program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
                if (elementFSK && program.getIntField(ProgramFieldType.AGE_LIMIT_TYPE) >= 0) {
                  infoStatement.setString(12, encrypt(String.valueOf(program.getIntField(ProgramFieldType.AGE_LIMIT_TYPE))));
                }
                if (elementForminformation) {
                  infoStatement.setInt(13, program.getInfo());
                }
                setValueToInfoStatement(infoStatement, elementEpisode, 15, program.getTextField(ProgramFieldType.EPISODE_TYPE));
                setValueToInfoStatement(infoStatement, elementOriginalEpisode, 16, program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
                setValueToInfoStatement(infoStatement, elementModeration, 17, program.getTextField(ProgramFieldType.MODERATION_TYPE));
                setValueToInfoStatement(infoStatement, elementWebside, 18, program.getTextField(ProgramFieldType.URL_TYPE));
                if (elementVPS && program.getTimeFieldAsString(ProgramFieldType.VPS_TYPE) != null) {
                  infoStatement.setString(19, encrypt(program.getTimeFieldAsString(ProgramFieldType.VPS_TYPE)));
                }
                setValueToInfoStatement(infoStatement, elementRepetitionOn, 20, program.getTextField(ProgramFieldType.REPETITION_ON_TYPE));
                setValueToInfoStatement(infoStatement, elementRepetitionOf, 21, program.getTextField(ProgramFieldType.REPETITION_OF_TYPE));

                broadcastStatement.execute();
                infoStatement.execute();
                counter++;
                if (counter >= 500) {
                  try {
                    counter = 0;
                    Thread.sleep(100);
                  }
                  catch (Exception e) {
                    counter = 0;
                    Thread.sleep(100);
                    JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(parentFrame), e.getMessage());
                    d = 32;
                    i = mSelectedChannels.length;
                    break;
                  }
                }
              }
            }
          }
        }
      }
      try {
        progress.setValue(progress.getMaximum());
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(parentFrame), e.getMessage());
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(parentFrame), e.getMessage());
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.out.println(e.toString());
    }
  }

  private void setValueToInfoStatement(PreparedStatement infoStatement, boolean isValueNeeded, int parameterIndex, String value) throws SQLException {
    if (isValueNeeded && value != null) {
      infoStatement.setString(parameterIndex, encrypt(value));
    }
  }

  protected void exportChannels(Connection connection) throws SQLException {
    PreparedStatement statement = connection
        .prepareStatement("INSERT INTO channel (id, channelid, name, category) VALUES (?, ?, ?, ?)");
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

  private int findDBChannel(Channel selectedChannel) {
    return dbChannelIds.get(getChannelId(selectedChannel));
  }

  private long findDBDate(String date) {
    return mDateIds.get(date);
  }

  private Map<String, Long> mDateIds;

  private void createDateIds() throws SQLException {
    mDateIds = new HashMap<String, Long>();
    Date date = new Date();
    Calendar currentCalendar = date.getCalendar();
    currentCalendar.set(Calendar.HOUR, 0);
    currentCalendar.set(Calendar.MINUTE, 0);
    currentCalendar.set(Calendar.SECOND, 0);
    currentCalendar.set(Calendar.MILLISECOND, 0);
    date = new Date(currentCalendar);
    date = date.addDays(-2);
    int maxDays = mSettings.getDaysToExport();
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

}
