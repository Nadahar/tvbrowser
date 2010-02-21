package tvbrowsermini.devices;

import java.awt.Frame;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

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

public class PDA extends AbstractExportDevice {

  public PDA(final TVBrowserMiniSettings settings, final Channel[] selectedChannels, final JProgressBar progress) {
    super(settings, selectedChannels, progress);
  }

  protected void createTables(Connection connection, Statement stmt) throws SQLException {
    stmt.addBatch("CREATE TABLE channel (id VARCHAR(100) NOT NULL PRIMARY KEY, name VARCHAR(50), category VARCHAR(1))");
    stmt.addBatch("CREATE TABLE broadcast (id INTEGER NOT NULL PRIMARY KEY, channel_id VARCHAR(100), title VARCHAR(100), start DATETIME, end DATETIME, favorite BOOLEAN, reminder BOOLEAN)");
    stmt.addBatch("CREATE TABLE info (broadcast_id INTEGER NOT NULL PRIMARY KEY, originaltitel VARCHAR(100), episode VARCHAR(100), originalepisode VARCHAR(100), shortdescription VARCHAR(400), description TEXT, genre VARCHAR(150), produced VARCHAR(100), location VARCHAR(100), director VARCHAR(200), moderation VARCHAR(200), script VARCHAR(200), actor VARCHAR(2000), music VARCHAR(150), fsk VARCHAR(5), form VARCHAR(400), showview VARCHAR(12), webside VARCHAR(150), vps VARCHAR(100), repetitionon VARCHAR(100), repetitionof VARCHAR(100))");
    connection.setAutoCommit(false);
    stmt.executeBatch();
    connection.setAutoCommit(true);
  }

  protected void createIndices(Connection connection, Statement stmt) throws SQLException {
    stmt.addBatch("CREATE INDEX reminder ON broadcast(reminder)");
    stmt.addBatch("CREATE INDEX favorite ON broadcast(favorite)");
    stmt.addBatch("CREATE INDEX channelid ON broadcast(channel_id)");
    stmt.addBatch("CREATE INDEX end ON broadcast(end)");
    stmt.addBatch("CREATE INDEX start ON broadcast(start)");
    stmt.addBatch("CREATE INDEX genre ON info(genre)");
    //stmt.addBatch("CREATE INDEX shortd ON info(shortdescription)");
    //stmt.addBatch("CREATE INDEX desc ON info(description)");
    //stmt.addBatch("CREATE INDEX episo ON info(episode)");
    connection.setAutoCommit(false);
    stmt.executeBatch();
    connection.setAutoCommit(true);
  }

  protected void exportFile(Connection connection, Frame parentFrame, Statement stmt) throws IOException {
    int[] positions = new int[mSelectedChannels.length];
    int index = 0;
    for (int i = 0; i < mSelectedChannels.length; i++) {
      positions[i] = index;
      index += 20000;
    }


    DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      Class.forName("org.sqlite.JDBC");
      exportChannels(connection);
      Date date = new Date();
      date = date.addDays(-2);
      Calendar calStart = Calendar.getInstance();
      Calendar calEnd = Calendar.getInstance();

      int counter = 0;

      int maxDays = mSettings.getDaysToExport();

      for (int d = 0; d < maxDays; d++) {
        date = date.addDays(1);
        for (int i = 0; i < mSelectedChannels.length; i++) {
          //for (int d = 0; d < maxDays; d++) {
          //	date = date.addDays(1);
          progress.setValue(progress.getValue() + 1);
          float percent = (float) ((float) i / (float) mSelectedChannels.length) * 100;
          percent++;
          Channel c = mSelectedChannels[i];

          Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, mSelectedChannels[i]);
          ArrayList<Program> programs = new ArrayList<Program>();
          while ((it != null) && (it.hasNext())) {
            programs.add(it.next());
          }
          for (int j = 0; j < programs.size(); j++) {
            progress.setString(date.getLongDateString() + " " + Math.round(percent) + "%" + " (" + c.getName() + ")");
            progress.setStringPainted(true);
            Program program = programs.get(j);
            if (program != null) {
              int reminder = 0;
              int favorite = 0;

              Marker[] markerArr = program.getMarkerArr();
              for (Marker aMarkerArr : markerArr) {
                if (aMarkerArr.getId().equals("reminderplugin.ReminderPlugin")) {
                  reminder = 1;
                } else if (aMarkerArr.getId().equals("favoritesplugin.FavoritesPlugin")) {
                  favorite = 1;
                }
              }

              calStart.set(date.getYear(), date.getMonth() - 1, date.getDayOfMonth(), program.getHours(), program.getMinutes(), 0);
              calEnd = (Calendar) calStart.clone();
              calEnd.add(Calendar.MINUTE, program.getLength());

              StringBuffer statementBroadcast = new StringBuffer("INSERT INTO broadcast (id, channel_id, title, start, end, favorite, reminder) VALUES ('" + positions[i] + "','" + getChannelId(mSelectedChannels[i]) + "', '" + program.getTitle().replace("'", "''") + "', '" + formater.format(calStart.getTime()) + "', '" + formater.format(calEnd.getTime()) + "'," + favorite + "," + reminder + ")");

              StringBuffer statementPart1 = new StringBuffer("INSERT INTO info (broadcast_id");
              StringBuffer statementPart2 = new StringBuffer(") VALUES (");
              statementPart2.append("'" + positions[i] + "'");
              boolean description = false;
              if (elementDescription) {
                if (program.getTextField(ProgramFieldType.DESCRIPTION_TYPE) != null) {
                  statementPart1.append(", description");
                  String desc = encrypt(program.getTextField(ProgramFieldType.DESCRIPTION_TYPE)).replace("'", "''");
                  statementPart2.append(", '" + desc + "'");
                  description = true;
                }
              }
              if (elementShortDescription) {
                if (program.getShortInfo() != null) {
                  if (description && elementDescription) {
                    if (program.getShortInfo().length() > 3) {
                      if (program.getTextField(ProgramFieldType.DESCRIPTION_TYPE).indexOf(program.getShortInfo().substring(1, program.getShortInfo().length() - 3)) > 0) {

                      }
                    } else {
                      statementPart1.append(", shortdescription");
                      statementPart2.append(", '" + encrypt(program.getShortInfo()) + "'");
                    }
                  } else {
                    statementPart1.append(", shortdescription");
                    statementPart2.append(", '" + encrypt(program.getShortInfo()) + "'");
                  }
                }
              }
              if (elementGenre) {
                if (program.getTextField(ProgramFieldType.GENRE_TYPE) != null) {
                  statementPart1.append(", genre");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.GENRE_TYPE)) + "'");
                }
              }
              if (elementProductionTime) {
                if (program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE) >= 0) {
                  statementPart1.append(", produced");
                  statementPart2.append(", '" + encrypt(String.valueOf(program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE))) + "'");
                }
              }
              if (elementProductionLocation) {
                if (program.getTextField(ProgramFieldType.ORIGIN_TYPE) != null) {
                  statementPart1.append(", location");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.ORIGIN_TYPE)) + "'");
                }
              }
              if (elementDirector) {
                if (program.getTextField(ProgramFieldType.DIRECTOR_TYPE) != null) {
                  statementPart1.append(", director");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.DIRECTOR_TYPE)) + "'");
                }
              }
              if (elementScript) {
                if (program.getTextField(ProgramFieldType.SCRIPT_TYPE) != null) {
                  statementPart1.append(", script");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.SCRIPT_TYPE)) + "'");
                }
              }
              if (elementActor) {
                if (program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE) != null) {
                  statementPart1.append(", actor");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE)) + "'");
                }
              }
              if (elementMusic) {
                if (program.getTextField(ProgramFieldType.MUSIC_TYPE) != null) {
                  statementPart1.append(", music");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.MUSIC_TYPE)) + "'");
                }
              }
              if (elementOriginalTitel) {
                if (program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE) != null) {
                  statementPart1.append(", originaltitel");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE)) + "'");
                }
              }
              if (elementFSK) {
                if (program.getIntField(ProgramFieldType.AGE_LIMIT_TYPE) >= 0) {
                  statementPart1.append(", fsk");
                  statementPart2.append(", '" + encrypt(String.valueOf(program.getIntField(ProgramFieldType.AGE_LIMIT_TYPE))) + "'");
                }
              }
              if (elementForminformation) {
                statementPart1.append(", form");
                StringBuilder info = new StringBuilder("");
                info = getFormatInformation(program, info);
                statementPart2.append(", '" + encrypt(info.toString()) + "'");
              }
              if (elementEpisode) {
                if (program.getTextField(ProgramFieldType.EPISODE_TYPE) != null) {
                  statementPart1.append(", episode");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.EPISODE_TYPE)) + "'");
                }
              }
              if (elementOriginalEpisode) {
                if (program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE) != null) {
                  statementPart1.append(", originalepisode");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE)) + "'");
                }
              }
              if (elementModeration) {
                if (program.getTextField(ProgramFieldType.MODERATION_TYPE) != null) {
                  statementPart1.append(", moderation");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.MODERATION_TYPE)) + "'");
                }
              }
              if (elementWebside) {
                if (program.getTextField(ProgramFieldType.URL_TYPE) != null) {
                  statementPart1.append(", webside");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.URL_TYPE)) + "'");
                }
              }
              if (elementVPS) {
                if (program.getTextField(ProgramFieldType.URL_TYPE) != null) {
                  statementPart1.append(", vps");
                  statementPart2.append(", '" + encrypt(program.getTimeFieldAsString(ProgramFieldType.VPS_TYPE)) + "'");
                }
              }
              if (elementRepetitionOn) {
                if (program.getTextField(ProgramFieldType.REPETITION_ON_TYPE) != null) {
                  statementPart1.append(", repetitionon");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.REPETITION_ON_TYPE)) + "'");
                }
              }
              if (elementRepetitionOf) {
                if (program.getTextField(ProgramFieldType.REPETITION_OF_TYPE) != null) {
                  statementPart1.append(", repetitionof");
                  statementPart2.append(", '" + encrypt(program.getTextField(ProgramFieldType.REPETITION_OF_TYPE)) + "'");
                }
              }

              statementPart1.append(statementPart2);
              statementPart1.append(")");
              stmt.addBatch(statementBroadcast.toString());
              stmt.addBatch(statementPart1.toString());
              positions[i]++;
              counter++;
              if (counter >= 500) {
                try {
                  counter = 0;
                  connection.setAutoCommit(false);
                  stmt.executeBatch();
                  connection.setAutoCommit(true);
                  Thread.sleep(100);
                }
                catch (Exception e) {
                  counter = 0;
                  Thread.sleep(100);
                  JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(parentFrame), e.getMessage());
                  d = 32;
                  j = programs.size();
                  i = mSelectedChannels.length;
                }
              }
            }
          }
        }
      }
      try {
        progress.setValue(progress.getMaximum());
        connection.setAutoCommit(false);
        stmt.executeBatch();
        connection.setAutoCommit(true);
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

  protected void exportChannels(final Connection connection) throws SQLException {
    PreparedStatement statement = connection
    .prepareStatement("INSERT INTO channel (id, name, category) VALUES (?, ?, ?)");
    for (Channel channel : mSelectedChannels) {
      int categories = channel.getCategories();
      String channelId = getChannelId(channel);
      statement.setString(1, channelId);
      statement.setString(2, channel.getName());
      statement.setString(3, categories == 0 || categories == 2 ? "r" : "t");
      statement.execute();
    }
    statement.close();
  }
}
