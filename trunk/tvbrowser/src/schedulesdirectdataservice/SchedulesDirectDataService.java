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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Map.Entry;

import net.sf.xtvdclient.xtvd.DataDirectException;
import net.sf.xtvdclient.xtvd.SOAPRequest;
import net.sf.xtvdclient.xtvd.datatypes.Crew;
import net.sf.xtvdclient.xtvd.datatypes.CrewMember;
import net.sf.xtvdclient.xtvd.datatypes.Genre;
import net.sf.xtvdclient.xtvd.datatypes.MovieAdvisories;
import net.sf.xtvdclient.xtvd.datatypes.ProgramGenre;
import net.sf.xtvdclient.xtvd.datatypes.Schedule;
import net.sf.xtvdclient.xtvd.datatypes.StarRating;
import net.sf.xtvdclient.xtvd.datatypes.Station;
import net.sf.xtvdclient.xtvd.datatypes.Xtvd;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
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

  private SchedulesDirectChannelGroup mChannelGroup = new SchedulesDirectChannelGroup();

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
    if (mProperties.getProperty("username", "").trim().length() != 0) {
      int max = channelArr.length;

      monitor.setMaximum(max);
      monitor.setMessage(mLocalizer.msg("loading","Loading SchedulesDirect data"));

      try {
        SOAPRequest soapRequest = new SOAPRequest(
                mProperties.getProperty("username", "").trim(),
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
        for (Schedule s : schedules) {
          Channel ch = channelMap.get(Integer.toString(s.getStation()));
          if (ch != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(s.getTime().getLocalDate());
            devplugin.Date programDate = new Date(cal);
            MutableChannelDayProgram chDayProgram = getMutableDayProgram(ch, programDate);

            MutableProgram prog = new MutableProgram(ch, programDate, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);

            int info = 0;
            if (s.getCloseCaptioned()) {
              info |= Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
            }
            if (s.getStereo()) {
              info |= Program.INFO_AUDIO_STEREO;
            }
            if (s.getSubtitled()) {
              info |= Program.INFO_ORIGINAL_WITH_SUBTITLE;
            }
            if (s.getHdtv()) {
              info |= Program.INFO_VISION_HD;
            }

            prog.setInfo(info);

            net.sf.xtvdclient.xtvd.datatypes.Program xtvdProgram = xtvd.getPrograms().get(s.getProgram());

            prog.setTitle(xtvdProgram.getTitle());

            if (xtvdProgram.getSyndicatedEpisodeNumber() != null) {
              try {
                prog.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, Integer.parseInt(xtvdProgram.getSyndicatedEpisodeNumber()));
              } catch (NumberFormatException e) {
                e.printStackTrace();
              }
            }

            if (xtvdProgram.getSubtitle() != null) {
              prog.setTextField(ProgramFieldType.EPISODE_TYPE, xtvdProgram.getSubtitle());
            }

            if (xtvdProgram.getYear() != null) {
              try {
                prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, Integer.parseInt(xtvdProgram.getYear()));
              } catch (NumberFormatException e) {
                e.printStackTrace();
              }
            } else if (xtvdProgram.getOriginalAirDate() != null) {
              Calendar aircal = Calendar.getInstance();
              aircal.setTime(xtvdProgram.getOriginalAirDate().getDate());
              prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, aircal.get(Calendar.YEAR));
            }

            StringBuilder desc = new StringBuilder();
            if (xtvdProgram.getDescription() != null) {
              desc.append(xtvdProgram.getDescription()).append("\n");
            }
            if (xtvdProgram.getStarRating() != null) {
              prog.setIntField(ProgramFieldType.RATING_TYPE, parseStarRating(xtvdProgram.getStarRating()));
            }
            if (xtvdProgram.getMpaaRating() != null) {
              desc.append("\nMPAA Rating: ").append(xtvdProgram.getMpaaRating());
            }
            if (xtvdProgram.getAdvisories() != null && !xtvdProgram.getAdvisories().isEmpty()) {
              desc.append("\nAdvisories : ");

              for (MovieAdvisories str : xtvdProgram.getAdvisories()) {
                desc.append(str.toString()).append(" ");
              }
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
            if (xtvdProgram.getColorCode() != null) {
              desc.append("\nColorCode : ").append(xtvdProgram.getColorCode());
            }
            if (xtvdProgram.getSeries() != null) {
              desc.append("\nSeries : ").append(xtvdProgram.getSeries());
            }
            if (xtvdProgram.getShowType()!= null) {
              desc.append("\nShowType : ").append(xtvdProgram.getShowType());
            } */

            Crew crew = xtvd.getProductionCrew().get(xtvdProgram.getId());
            if (crew != null) {
              StringBuilder actors = new StringBuilder();
              StringBuilder director = new StringBuilder();
              StringBuilder host = new StringBuilder();
              StringBuilder producer = new StringBuilder();
              StringBuilder writer = new StringBuilder();

              for (CrewMember member : crew.getMember()) {
                if ("Actor".equals(member.getRole()) || "Guest Star".equals(member.getRole())) {
                  actors.append(member.getGivenname()).append(" ").append(member.getSurname()).append(", ");
                } else if ("Director".equals(member.getRole())) {
                  director.append(member.getGivenname()).append(" ").append(member.getSurname()).append(", ");
                } else if ("Host".equals(member.getRole())) {
                  host.append(member.getGivenname()).append(" ").append(member.getSurname()).append(", ");
                } else if ("Producer".equals(member.getRole())||"Executive Producer".equals(member.getRole())) {
                  producer.append(member.getGivenname()).append(" ").append(member.getSurname()).append(", ");
                } else if ("Writer".equals(member.getRole())) {
                  writer.append(member.getGivenname()).append(" ").append(member.getSurname()).append(", ");
                }
              }
              if (actors.length() > 0) {
                prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, actors.toString().substring(0, actors.toString().length()-2));
              }
              if (director.length() > 0) {
                prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, director.toString().substring(0, director.toString().length()-2));
              }
              if (host.length() > 0) {
                prog.setTextField(ProgramFieldType.MODERATION_TYPE, host.toString().substring(0, host.toString().length()-2));
              }
              if (producer.length() > 0) {
                prog.setTextField(ProgramFieldType.PRODUCER_TYPE, producer.toString().substring(0, producer.toString().length()-2));
              }
              if (writer.length() > 0) {
                prog.setTextField(ProgramFieldType.SCRIPT_TYPE, writer.toString().substring(0, writer.toString().length()-2));
              }
            }

            ProgramGenre programGenre = xtvd.getGenres().get( xtvdProgram.getId() );
            if (programGenre != null) {
              StringBuilder genreStr = new StringBuilder();
              for (Genre genre : programGenre.getGenres()) {
                genreStr.append(genre.getClassValue()).append(", ");
              }
              prog.setTextField(ProgramFieldType.GENRE_TYPE, genreStr.toString().substring(0, genreStr.toString().length()-2));
            }

            prog.setDescription(desc.toString());
            prog.setShortInfo(MutableProgram.generateShortInfoFromDescription(desc.toString()));

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

  private int parseStarRating(StarRating starRating) {
    String value = starRating.toString();

    int ret = -1;

    if (value.equals("")) {
      ret = 0;
    } else if (value.equals("+")) {
      ret = 10;
    } else if (value.equals("*")) {
      ret = 20;
    } else if (value.equals("*+")) {
      ret = 30;
    } else if (value.equals("**")) {
      ret = 40;
    } else if (value.equals("**+")) {
      ret = 50;
    } else if (value.equals("***")) {
      ret = 60;
    } else if (value.equals("***+")) {
      ret = 70;
    } else if (value.equals("****")) {
      ret = 80;
    } else if (value.equals("****+")) {
      ret = 90;
    } else if (value.equals("*****")) {
      ret = 100;
    } else {
      System.out.println(value);
    }

    return ret;
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

        Iterator<Program> itCurrProg = SchedulesDirectDataService.getPluginManager().getChannelDayProgram(newDayProg.getDate(), ch);
        Iterator<Program> itNewProg = newDayProg.getPrograms();
        if (itCurrProg != null && itNewProg != null) {
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
        }
        if (update) {
          updateManager.updateDayProgram(newDayProg);
        }
      }
    }
  }

  /*
  * (non-Javadoc)
  *
  * @see devplugin.TvDataService#loadSettings(java.util.Properties)
  */
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

  /*
  * (non-Javadoc)
  *
  * @see devplugin.TvDataService#storeSettings()
  */
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
    if (username.length() != 0) {
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
    return new Version(0, 6, 1);
  }

  /*
  * (non-Javadoc)
  *
  * @see devplugin.TvDataService#getInfo()
  */
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

      Map stations = xtvd.getStations();

      for (final Object entry : stations.entrySet()) {
        final Station station = (Station) (((Entry) entry).getValue());
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
