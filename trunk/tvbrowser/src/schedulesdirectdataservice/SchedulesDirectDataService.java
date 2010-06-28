/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2007-09-20 23:45:38 +0200 (Do, 20 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 3894 $
 */
package schedulesdirectdataservice;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.xtvdclient.xtvd.DataDirectException;
import net.sf.xtvdclient.xtvd.SOAPRequest;
import net.sf.xtvdclient.xtvd.datatypes.Crew;
import net.sf.xtvdclient.xtvd.datatypes.CrewMember;
import net.sf.xtvdclient.xtvd.datatypes.Genre;
import net.sf.xtvdclient.xtvd.datatypes.MovieAdvisories;
import net.sf.xtvdclient.xtvd.datatypes.Part;
import net.sf.xtvdclient.xtvd.datatypes.ProgramGenre;
import net.sf.xtvdclient.xtvd.datatypes.Schedule;
import net.sf.xtvdclient.xtvd.datatypes.StarRating;
import net.sf.xtvdclient.xtvd.datatypes.Station;
import net.sf.xtvdclient.xtvd.datatypes.Xtvd;

import org.apache.commons.lang.StringUtils;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgressMonitor;
import devplugin.Version;


public class SchedulesDirectDataService extends AbstractTvDataService {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SchedulesDirectDataService.class);

  /** The logger for this class */
  private static final Logger mLog
    = Logger.getLogger(SchedulesDirectDataService.class.getName());

  private static final Version VERSION = new Version(3,0);

  private ChannelGroup mChannelGroup = new ChannelGroupImpl("SchedulesDirect", "SchedulesDirect", "SchedulesDirect", "SchedulesDirect");

  private Properties mProperties;
  /**
   * List of Channels
   */
  private ArrayList<Channel> mChannels = new ArrayList<Channel>();
  private static final String SCHEDULESDIRECT_SERVICE = "http://webservices.schedulesdirect.tmsdatadirect.com/schedulesdirect/tvlistings/xtvdService";
  private HashMap<Channel, Map<Date, MutableChannelDayProgram>> mChannelMap;


  public void setWorkingDirectory(File dataDir) {
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[]{mChannelGroup};
  }

  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
    // Check for connection
    if (!updateManager.checkConnection()) {
      return;
    }

    final String userName = mProperties.getProperty("username", "").trim();
    if (!userName.isEmpty()) {
      int max = channelArr.length;

      monitor.setMaximum(max);
      monitor.setMessage(mLocalizer.msg("loading","Loading SchedulesDirect data"));

      try {
        SOAPRequest soapRequest = new SOAPRequest(
                userName,
                IOUtilities.xorDecode(mProperties.getProperty("password", ""), SchedulesDirectSettingsPanel.PASSWORDSEED).trim(),
                SCHEDULESDIRECT_SERVICE);
        Calendar start = startDate.getCalendar();
        Calendar end = (Calendar) startDate.getCalendar().clone();
        end.add(Calendar.DAY_OF_MONTH, dateCount);

        final Xtvd xtvd = new Xtvd();
        soapRequest.getData(start, end, xtvd);

        monitor.setMessage(mLocalizer.msg("parsing", "Parsing SchedulesDirect Data"));

        Collection<Schedule> schedules = xtvd.getSchedules();

        HashMap<String, Channel> channelMap = new HashMap<String, Channel>();
        for (Channel ch : channelArr) {
          channelMap.put(ch.getId(), ch);
        }

        mChannelMap = new HashMap<Channel, Map<devplugin.Date, MutableChannelDayProgram>>();
        for (Schedule schedule : schedules) {
          Channel ch = channelMap.get(Integer.toString(schedule.getStation()));
          if (ch != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(schedule.getTime().getLocalDate());
            devplugin.Date programDate = new Date(cal);
            MutableChannelDayProgram chDayProgram = getMutableDayProgram(ch, programDate);

            MutableProgram prog = new MutableProgram(ch, programDate, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            net.sf.xtvdclient.xtvd.datatypes.Program xtvdProgram = xtvd.getPrograms().get(schedule.getProgram());

            int info = 0;
            if (schedule.getCloseCaptioned()) {
              info |= Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
            }
            if (schedule.getStereo()) {
              info |= Program.INFO_AUDIO_STEREO;
            }
            if (schedule.getSubtitled()) {
              info |= Program.INFO_ORIGINAL_WITH_SUBTITLE;
            }
            if (schedule.getHdtv()) {
              info |= Program.INFO_VISION_HD;
            }
            if (xtvdProgram.getColorCode() != null) {
              String code = xtvdProgram.getColorCode().toLowerCase();
              if (code.contains("black") || code.contains("bw")) {
                info |= Program.INFO_VISION_BLACK_AND_WHITE;
              }
              else {
                mLog.warning("Unknown color code: " + xtvdProgram.getColorCode());
              }
            }

            prog.setInfo(info);

            prog.setTitle(xtvdProgram.getTitle());

            if (xtvdProgram.getSyndicatedEpisodeNumber() != null) {
              try {
                prog.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, Integer.parseInt(xtvdProgram.getSyndicatedEpisodeNumber()));
              } catch (NumberFormatException e) {
                // ignore, the syndicated episode number may also be an arbitrary string
              }
            }

            if (xtvdProgram.getSubtitle() != null) {
              prog.setTextField(ProgramFieldType.EPISODE_TYPE, xtvdProgram.getSubtitle());
            }

            if (xtvdProgram.getYear() != null) {
              try {
                prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, Integer.parseInt(xtvdProgram.getYear()));
                // year is only set for movies
                setMovieType(prog);
              } catch (NumberFormatException e) {
                e.printStackTrace();
              }
            } else if (xtvdProgram.getOriginalAirDate() != null) {
              Calendar aircal = Calendar.getInstance();
              java.util.Date date = xtvdProgram.getOriginalAirDate().getDate();
              aircal.setTime(date);
              prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, aircal.get(Calendar.YEAR));
              String repetition = new SimpleDateFormat().format(date);
              if (repetition.endsWith("00:00")) {
                repetition = repetition.substring(0, repetition.length() - "00:00".length()).trim();
              }
              prog.setTextField(ProgramFieldType.REPETITION_OF_TYPE, repetition);
            }

            StringBuilder desc = new StringBuilder();
            if (xtvdProgram.getDescription() != null) {
              desc.append(xtvdProgram.getDescription()).append('\n');
            }
            if (xtvdProgram.getStarRating() != null) {
              int rating = parseStarRating(xtvdProgram.getStarRating());
              if (rating >= 0) {
                prog.setIntField(ProgramFieldType.RATING_TYPE, rating);
              }
              setMovieType(prog);
            }
            if (xtvdProgram.getMpaaRating() != null) {
              String rating = xtvdProgram.getMpaaRating().toString();
              prog.setTextField(ProgramFieldType.AGE_RATING_TYPE, rating);
              int ageLimit = ProgramUtilities.getAgeLimit(rating);
              if (ageLimit >= 0) {
                prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, ageLimit);
              }
              setMovieType(prog);
            }
            if (schedule.getTvRating() != null) {
              String rating = schedule.getTvRating().toString();
              String ratingString = prog.getTextField(ProgramFieldType.AGE_RATING_TYPE);
              if (ratingString != null) {
                ratingString = ratingString + ", " + rating;
              }
              else {
                ratingString = rating;
              }
              prog.setTextField(ProgramFieldType.AGE_RATING_TYPE, ratingString);
              int ageLimit = ProgramUtilities.getAgeLimit(rating);
              if (ageLimit >= 0) {
                prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, ageLimit);
              }
            }
            if (xtvdProgram.getAdvisories() != null && !xtvdProgram.getAdvisories().isEmpty()) {
              StringBuilder advBuilder = new StringBuilder();

              for (MovieAdvisories advisory : xtvdProgram.getAdvisories()) {
                if (advBuilder.length() > 0) {
                  advBuilder.append(", ");
                }
                advBuilder.append(advisory.toString());
              }
              desc.append("\nAdvisories: ").append(advBuilder.toString());
            }

            if (xtvdProgram.getRunTime() != null) {
              try {
                int length = Integer.parseInt(xtvdProgram.getRunTime().getHours())*60 + Integer.parseInt(xtvdProgram.getRunTime().getMinutes());
                prog.setIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE, length);
              } catch (NumberFormatException e) {
                e.printStackTrace();
              }
            }
            /*
            if (xtvdProgram.getSeries() != null) {
              desc.append("\nSeries : ").append(xtvdProgram.getSeries());
            }
            if (xtvdProgram.getShowType()!= null) {
              desc.append("\nShowType : ").append(xtvdProgram.getShowType());
            } */

            if (schedule.getPart() != null) {
              final Part part = schedule.getPart();
              desc.append("\nPart " + part.getNumber() + " of " + part.getTotal());
            }
            if (schedule.getRepeat() && xtvdProgram.getOriginalAirDate() == null) {
              prog.setTextField(ProgramFieldType.REPETITION_OF_TYPE, "unknown previous program");
            }


            Crew crew = xtvd.getProductionCrew().get(xtvdProgram.getId());
            if (crew != null) {
              StringBuilder actors = new StringBuilder();
              StringBuilder director = new StringBuilder();
              StringBuilder host = new StringBuilder();
              StringBuilder producer = new StringBuilder();
              StringBuilder writer = new StringBuilder();
              StringBuilder additional = new StringBuilder();

              for (CrewMember member : crew.getMember()) {
                final String role = member.getRole();
                if ("Actor".equalsIgnoreCase(role) || "Guest Star".equals(role)) {
                  appendPerson(actors, member);
                } else if ("Director".equalsIgnoreCase(role)) {
                  appendPerson(director, member);
                } else if ("Host".equalsIgnoreCase(role) || "Anchor".equalsIgnoreCase(role)) {
                  appendPerson(host, member);
                } else if (role.toLowerCase().contains("producer")) {
                  appendPerson(producer, member);
                } else if ("Writer".equalsIgnoreCase(role)) {
                  appendPerson(writer, member);
                } else if (role.toLowerCase().contains("guest")) {
                  appendPerson(additional, member);
                }
                else if ("Narrator".equalsIgnoreCase(role)) {
                  appendPersonWithRole(additional, member);
                }
                else {
                  appendPersonWithRole(additional, member);
                  mLog.warning("Unknown crew member role: " + role);
                }
              }
              if (actors.length() > 0) {
                prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, actors.toString());
              }
              if (director.length() > 0) {
                prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, director.toString());
              }
              if (host.length() > 0) {
                prog.setTextField(ProgramFieldType.MODERATION_TYPE, host.toString());
              }
              if (producer.length() > 0) {
                prog.setTextField(ProgramFieldType.PRODUCER_TYPE, producer.toString());
              }
              if (writer.length() > 0) {
                prog.setTextField(ProgramFieldType.SCRIPT_TYPE, writer.toString());
              }
              if (additional.length() > 0) {
                prog.setTextField(ProgramFieldType.ADDITIONAL_PERSONS_TYPE, additional.toString());
              }
            }

            ProgramGenre programGenre = xtvd.getGenres().get( xtvdProgram.getId() );
            if (programGenre != null) {
              StringBuilder genreStr = new StringBuilder();
              for (Genre genre : programGenre.getGenres()) {
                if (genreStr.length() > 0) {
                  genreStr.append(", ");
                }
                genreStr.append(genre.getClassValue());
              }
              prog.setTextField(ProgramFieldType.GENRE_TYPE, genreStr.toString());
            }

            final String description = desc.toString().trim();
            if (!description.isEmpty()) {
              prog.setDescription(description);
              prog.setShortInfo(MutableProgram.generateShortInfoFromDescription(description));
            }

            chDayProgram.addProgram(prog);
          }

        }
        storeDayPrograms(updateManager);
      } catch (DataDirectException e) {
        ErrorHandler.handle(mLocalizer.msg("problems","Problems loading the data, maybe Username/Passwort wrong or Communication Error"), e);
        e.printStackTrace();
      }

      monitor.setMessage("");
    }
  }

  private void setMovieType(MutableProgram prog) {
    prog.setInfo(prog.getInfo() | Program.INFO_CATEGORIE_MOVIE);
  }

  private void appendPersonWithRole(final StringBuilder personField, final CrewMember member) {
    if (personField.length() > 0) {
      personField.append('\n');
    }
    personField.append(member.getGivenname()).append(' ').append(member.getSurname());
    personField.append("\t\t-\t\t").append(member.getRole());
  }

  private void appendPerson(final StringBuilder personField, final CrewMember member) {
    if (personField.length() > 0) {
      personField.append('\n');
    }
    personField.append(member.getGivenname()).append(' ').append(member.getSurname());
  }

  private int parseStarRating(final StarRating starRating) {
    final String value = starRating.toString();
    int rating = 0;
    for (int i = 0; i < value.length(); i++) {
      switch (value.charAt(i)) {
      case '+' : {
        rating += 10;
        break;
      }
      case '*' : {
        rating += 20;
        break;
      }
      default:
        mLog.warning("Unknown rating: " + value);
        return -1;
      }
    }
    return rating;
  }

  private MutableChannelDayProgram getMutableDayProgram(Channel ch, devplugin.Date date) {
    MutableChannelDayProgram dayProgram;
    if (mChannelMap.get(ch) != null) {
      dayProgram = mChannelMap.get(ch).get(date);

      if (dayProgram == null) {
        dayProgram = new MutableChannelDayProgram(date, ch);
        mChannelMap.get(ch).put(date, dayProgram);
      }
    } else {
      Map<devplugin.Date, MutableChannelDayProgram> map = new HashMap<Date, MutableChannelDayProgram>();
      dayProgram = new MutableChannelDayProgram(date, ch);
      map.put(date, dayProgram);
      mChannelMap.put(ch, map);
    }

    return dayProgram;
  }

  private void storeDayPrograms(TvDataUpdateManager updateManager) {
    for (final Channel ch:mChannelMap.keySet()) {
      for (final MutableChannelDayProgram newDayProg : mChannelMap.get(ch).values()) {
        // compare new and existing programs to avoid unnecessary updates
        boolean update = true;

        Iterator<Program> itCurrProg = AbstractTvDataService.getPluginManager().getChannelDayProgram(newDayProg.getDate(), ch);
        Iterator<Program> itNewProg = newDayProg.getPrograms();
        update = false;
        while (itCurrProg.hasNext() && itNewProg.hasNext()) {
          MutableProgram currProg = (MutableProgram) itCurrProg.next();
          MutableProgram newProg = (MutableProgram) itNewProg.next();
          if (!currProg.equalsAllFields(newProg)) {
            update = true;
          }
        }
        // not the same number of programs ?
        if (itCurrProg.hasNext() != itNewProg.hasNext()) {
          update = true;
        }
        if (update) {
          updateManager.updateDayProgram(newDayProg);
        }
      }
    }
  }

  public void loadSettings(Properties settings) {
    mProperties = settings;

    int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));

    mChannels = new ArrayList<Channel>();

    for (int i = 0; i < numChannels; i++) {
      Channel ch = new Channel(this, settings.getProperty("ChannelTitle-" + i, ""), settings.getProperty("ChannelId-"
              + i, ""),  TimeZone.getDefault(), "US", "(c) SchedulesDirect", "", mChannelGroup, null, Channel.CATEGORY_TV);

      mChannels.add(ch);
    }
  }

  public Properties storeSettings() {
    Properties prop = new Properties();

    if (mProperties == null) {
      mProperties = new Properties();
    }
    prop.setProperty("username", mProperties.getProperty("username", ""));
    prop.setProperty("password", mProperties.getProperty("password", ""));

    if (mChannels != null) {
      prop.setProperty("NumberOfChannels", Integer.toString(mChannels.size()));
      int max = mChannels.size();
      for (int i = 0; i < max; i++) {
        Channel ch = mChannels.get(i);
        prop.setProperty("ChannelId-" + i, ch.getId());
        prop.setProperty("ChannelTitle-" + i, ch.getName());
      }
    } else {
      prop.setProperty("NumberOfChannels", "0");
    }

    return prop;
  }


  public boolean hasSettingsPanel() {
    return true;
  }

  public SettingsPanel getSettingsPanel() {
    return new SchedulesDirectSettingsPanel(mProperties);
  }

  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    String username = mProperties.getProperty("username", "").trim();
    if (StringUtils.isNotEmpty(username)) {
      monitor.setMessage(mLocalizer.msg("loadingChannels","Loading SchedulesDirect-Channels"));
      mChannels = getChannels();
      monitor.setMessage(mLocalizer.msg("loadingChannelsDone","Done loading SchedulesDirect-Channels"));
    } else {
      mChannels = new ArrayList<Channel>();
    }
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[]{mChannelGroup};
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public boolean supportsDynamicChannelGroups() {
    return false;
  }


  public static Version getVersion() {
    return VERSION;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(SchedulesDirectDataService.class, mLocalizer.msg("name", "Schedules Direct Data"), mLocalizer.msg("desc", "Loads data from Schedules Direct."),
            "TV-Browser Team");
  }

  /**
   * @return All channels available
   */
  public ArrayList<Channel> getChannels() {
    ArrayList<Channel> allChannels = new ArrayList<Channel>();
    try {
      SOAPRequest soapRequest = new SOAPRequest(
              mProperties.getProperty("username", "").trim(),
              IOUtilities.xorDecode(mProperties.getProperty("password", ""), SchedulesDirectSettingsPanel.PASSWORDSEED).trim(),
              SCHEDULESDIRECT_SERVICE);
      Calendar start = Calendar.getInstance();
      Calendar end = Calendar.getInstance();
      end.add(Calendar.DAY_OF_MONTH, 1);

      Xtvd xtvd = new Xtvd();
      soapRequest.getData(start, end, xtvd);

      Map<Integer, Station> stations = xtvd.getStations();

      for (final Entry<Integer, Station> entry : stations.entrySet()) {
        final Station station = entry.getValue();
        allChannels.add(
                new Channel(this, station.getName(), Integer.toString(station.getId()), TimeZone.getTimeZone("UTC"), "US", "(c) SchedulesDirect", "", mChannelGroup, null, Channel.CATEGORY_TV)
        );
      }

    } catch (DataDirectException e) {
      e.printStackTrace();
      ErrorHandler.handle(mLocalizer.msg("problems","Problems loading the data, maybe Username/Passwort wrong or Communication Error"), e);
    }

    return allChannels;
  }

}
