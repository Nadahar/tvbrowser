/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox.connector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;

import util.io.IOUtilities;
import util.ui.Localizer;
import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.connector.cs.DreamboxOptionPane;
import captureplugin.drivers.dreambox.connector.cs.E2LocationHelper;
import captureplugin.drivers.dreambox.connector.cs.E2MovieHelper;
import captureplugin.drivers.dreambox.connector.cs.E2ServiceHelper;
import captureplugin.drivers.dreambox.connector.cs.E2TimerHelper;
import captureplugin.drivers.utils.ProgramTime;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * Connector for the Dreambox
 * 
 * adopted by fishhead
 */
public class DreamboxConnector {
  // fishhead -------------------------
  // Logger
  private static final Logger mLog = Logger.getLogger(DreamboxConnector.class
      .getName());
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DreamboxConnector.class);
  // fishhead -------------------------
  /** get list of bouquets */
  private final static String BOUQUET_LIST = "1:7:1:0:0:0:0:0:0:0:(type == 1) || (type == 17) || (type == 195) || (type == 25)FROM BOUQUET \"bouquets.tv\" ORDER BY bouquet";
  /** Config of the Dreambox */
  private DreamboxConfig mConfig;

  private static final int WEBIF_MINIMUM_VERSION = 20070701;

  /**
   * Constructor
   * 
   * @param config
   *          Config of the dreambox
   */
  public DreamboxConnector(DreamboxConfig config) {
    mConfig = config;
  }

  /**
   * @param service
   *          Service-ID
   * @return Data of specific service
   */
  private TreeMap<String, String> getServiceDataBouquets(String service) {
    if (!mConfig.hasValidAddress()) {
      return null;
    }
    try {
      final Calendar cal = new GregorianCalendar();
      InputStream stream = openStreamForLocalUrl("/web/getservices?bRef=" + service);
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      DreamboxHandler handler = new DreamboxHandler();
      saxParser.parse(stream, handler);
      mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "GET bouquets - "
          + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())
          + " ms");
      return handler.getData();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private InputStream openStreamForLocalUrl(final String localUrl) throws MalformedURLException, IOException {
    URL url = new URL("http://" + mConfig.getDreamboxAddress() + localUrl);
    URLConnection connection = url.openConnection();

    // set user and password
    String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
    String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
    connection.setRequestProperty("Authorization", "Basic " + encoded);

    // set timeout
    connection.setConnectTimeout(mConfig.getTimeout());
    InputStream stream = connection.getInputStream();
    return stream;
  }

  /**
   * @param service
   *          Service-ID
   * @return Data of specific service
   */
  private TreeMap<String, String> getServiceData(String service) {
    if (!mConfig.hasValidAddress()) {
      return null;
    }
    try {
      final Calendar cal = new GregorianCalendar();
      InputStream stream = openStreamForLocalUrl("/web/getservices?sRef=" + service);
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      DreamboxHandler handler = new DreamboxHandler();
      saxParser.parse(stream, handler);
      mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "GET services - "
          + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())
          + " ms");
      return handler.getData();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * @return All channels available in the dreambox
   */
  public Collection<DreamboxChannel> getChannels() {
    try {
      ArrayList<DreamboxChannel> allChannels = new ArrayList<DreamboxChannel>();

      TreeMap<String, String> bouquets = getServiceDataBouquets(URLEncoder.encode(BOUQUET_LIST, "UTF8"));
      for (Entry<String, String> entry : bouquets.entrySet()) {
        String key = entry.getKey();
        String bouqetName = entry.getValue();

        TreeMap<String, String> map = getServiceData(URLEncoder.encode(key, "UTF8"));

        for (Entry<String, String> mEntry : map.entrySet()) {
          String mkey = mEntry.getKey();
          allChannels.add(new DreamboxChannel(mkey, mEntry.getValue(), bouqetName));
        }
      }

      return allChannels;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Switch to channel on Dreambox
   * 
   * @param channel
   *          switch to this channel
   */
  public void switchToChannel(DreamboxChannel channel) {
    try {
      InputStream stream = openStreamForLocalUrl("/web/zap?sRef=" + URLEncoder.encode(channel.getReference(), "UTF8"));
      stream.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @return List of Timers
   */
//  now E2TimerHelper ...
//  private List<Map<String, String>> getTimers() {
//    if (!mConfig.hasValidAddress()) {
//      return null;
//    }
//    try {
//      InputStream stream = openStreamForLocalUrl("/web/timerlist");
//      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
//      DreamboxTimerHandler handler = new DreamboxTimerHandler();
//      saxParser.parse(stream, handler);
//      return handler.getTimers();
//    } catch (ParserConfigurationException e) {
//      e.printStackTrace();
//    } catch (SAXException e) {
//      e.printStackTrace();
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (IllegalArgumentException e) {
//      e.printStackTrace();
//    }
//    return null;
//  }

  /**
   * @param config
   *          DreamboxConfig
   * @return List of recordings on the dreambox
   */
  public ProgramTime[] getRecordings(DreamboxConfig config) {
    ArrayList<ProgramTime> programs = new ArrayList<ProgramTime>();

    // fishhead -----------------------
    // prefetch
    E2TimerHelper timerHelper = E2TimerHelper.getInstance(mConfig);
    List<Map<String, String>> timers = timerHelper.getRepeatedTimers();
    // fishhead -----------------------
    if (timers == null) {
      return new ProgramTime[0];
    }
    // fishhead -----------------------
    E2LocationHelper locationHelper = E2LocationHelper.getInstance(mConfig,
        timerHelper.getThread());
    E2MovieHelper.getInstance(mConfig, locationHelper.getThread());
    // fishhead -----------------------

    for (Map<String, String> timer : timers) {

      // immer den SD-Sender zurueckliefern, wenn SD_HD.properties vorhanden
      String reference = E2ServiceHelper.getServiceRef(timer
          .get(E2TimerHelper.SERVICEREFERENCE), false);

      DreamboxChannel channel = config.getDreamboxChannelForRef(reference);

      if (channel != null) {
        Channel tvbchannel = config.getChannel(channel);
        if (tvbchannel != null) {
          Calendar calBeg = E2TimerHelper.getAsCalendar(timer
              .get(E2TimerHelper.TIMEBEGIN));
          int beginMinutes = calBeg.get(Calendar.HOUR_OF_DAY) * 60
              + calBeg.get(Calendar.MINUTE);

          Calendar calEnd = E2TimerHelper.getAsCalendar(timer
              .get(E2TimerHelper.TIMEEND));

          int endMinutes = calEnd.get(Calendar.HOUR_OF_DAY) * 60
              + calEnd.get(Calendar.MINUTE);

          if (endMinutes < beginMinutes) {
            endMinutes += 24 * 60;
          }

          Calendar runner = (Calendar) calBeg.clone();

          long days = calEnd.get(Calendar.DAY_OF_YEAR)
              - calBeg.get(Calendar.DAY_OF_YEAR);

          if (calEnd.get(Calendar.YEAR) != calBeg.get(Calendar.YEAR)) {
            days = 1;
          }

          for (int i = 0; i <= days; i++) {
            Iterator<Program> it = CapturePlugin.getPluginManager()
                .getChannelDayProgram(new Date(runner), tvbchannel);
            if (it != null) {
              boolean found = false;

              while (it.hasNext() && !found) {
                Program prog = it.next();
                int progTime = prog.getHours() * 60 + prog.getMinutes()
                    + (i * 24 * 60);

                if (progTime >= beginMinutes - 15
                    && progTime <= endMinutes + 15
                    && prog.getTitle().trim().equalsIgnoreCase(
                        timer.get(E2TimerHelper.NAME).trim())) {

                  found = true;
                  programs.add(new ProgramTime(prog, calBeg.getTime(), calEnd
                      .getTime()));
                }
              }
            }

            runner.add(Calendar.HOUR_OF_DAY, 24);
          }

        }
      }
    }

    return programs.toArray(new ProgramTime[programs.size()]);
  }

  /**
   * Tries to parse a Long
   * 
   * @param longStr
   *          String with Long-Value
   * @return long-Value or -1
   */
  // now E2TimerHelper ...
  // private long getLong(String longStr) {
  // if (longStr.contains(".")) {
  // longStr = longStr.substring(0, longStr.indexOf('.'));
  // }
  //  
  // try {
  // return Long.parseLong(longStr);
  // } catch (NumberFormatException e) {
  // e.printStackTrace();
  // }
  //
  // return -1;
  // }

  /**
   * Add a recording to the Dreambox Enigma2
   * 
   * @param recTimer
   *          Timer to add
   * @param timerHelper
   *          Helper for reading timers in background
   * 
   * @return True if successfull
   */
  public boolean addRecording(Map<String, String> recTimer,
      E2TimerHelper timerHelper) {

    // adopted by fishhead

    // Alte Timer holen
    int oldTimerCount = timerHelper.getTimerCount();

    // Timer hinzufuegen
    boolean state = timerHelper.timerAdd(recTimer);

    // Neue Timer lesen
    timerHelper.refresh();

    // Neue Timer holen
    int newTimerCount = timerHelper.getTimerCount();

    // pruefen
    if (oldTimerCount == newTimerCount) {
      // Timer could not be added
      state = false;

      Calendar calRecTimeBegin = E2TimerHelper.getAsCalendar(recTimer
          .get(E2TimerHelper.TIMEBEGIN));
      Calendar calRecTimeEnd = E2TimerHelper.getAsCalendar(recTimer
          .get(E2TimerHelper.TIMEEND));

      String s = String.format("%1$tH:%1$tM - %2$tH:%2$tM : %3$s  [%4$s]\n\n"
          + mLocalizer.msg("errorMessage", "already added ...") + "\n\n",
          calRecTimeBegin, // Anfangszeit
          calRecTimeEnd, // Endzeit
          recTimer.get(E2TimerHelper.NAME), // Titel
          recTimer.get(E2TimerHelper.SERVICENAME) // Programmname
          );

      for (Map<String, String> timer : timerHelper.getTimers()) {

        Calendar calTimeBegin = E2TimerHelper.getAsCalendar(timer
            .get(E2TimerHelper.TIMEBEGIN));
        Calendar calTimeEnd = E2TimerHelper.getAsCalendar(timer
            .get(E2TimerHelper.TIMEEND));

        if ((calRecTimeBegin.getTimeInMillis() <= calTimeEnd.getTimeInMillis())
            && (calRecTimeEnd.getTimeInMillis() >= calTimeBegin
                .getTimeInMillis()) && !recTimer.equals(timer)) {
          // Ueberschneidung
          s += String.format("%1$tR - %2$tR : %3$s  [%4$s]\n", calTimeBegin
              .getTime(), calTimeEnd.getTime(), timer.get(E2TimerHelper.NAME),
              timer.get(E2TimerHelper.SERVICENAME));
          ;
        }
      }

      Object[] options = { "OK", mLocalizer.msg("cancel", "Timer-Allocation") };
      int n = JOptionPane.showOptionDialog(null, s, mLocalizer.msg(
          "errorTimer", "Timer could not be added!"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
          options, options[0]);
      if (n == 1) {
        DreamboxOptionPane.showTimer(this);
      }
    }

    return state;
  }

  /**
   * Remove a recording from the Dreambox
   * 
   * @param dreamboxChannel
   *          the DreamboxChannel for the Program
   * @param prgTime
   *          ProgramTime to remove @return true, if successfull
   * @param timezone
   *          Timezone to use for recording
   * 
   * @return True, if successfull
   */
  public boolean removeRecording(DreamboxChannel dreamboxChannel,
      ProgramTime prgTime, TimeZone timezone) {

    E2TimerHelper timerHelper = E2TimerHelper.getInstance(mConfig);

    // REC remove
    String reference = dreamboxChannel.getReference();
    String begin = E2TimerHelper.getAsSeconds(prgTime.getStartAsCalendar());
    String end = E2TimerHelper.getAsSeconds(prgTime.getEndAsCalendar());
    String serviceRefHd = E2ServiceHelper.getServiceRef(reference, true);
    boolean useHdService = timerHelper.indexOfTimer(serviceRefHd, begin, end) != -1;

    // delete REC
    Map<String, String> recTimer = timerHelper.createRecTimer(dreamboxChannel,
        prgTime, 0/* afterevent */, 0/* repeated */, timezone,
        ""/* location */, ""/* tags */, useHdService);
    boolean state = timerHelper.timerDelete(recTimer);

    // delete ZAP before
    Map<String, String> zapBeforeTimer = timerHelper
        .createZapBeforeTimer(recTimer);
    if (timerHelper.indexOfTimer(zapBeforeTimer) != -1) {
      timerHelper.timerDelete(zapBeforeTimer);
    }

    // delete ZAP after
    Map<String, String> zapAfterTimer = timerHelper
        .createZapAfterTimer(recTimer);
    if (timerHelper.indexOfTimer(zapAfterTimer) != -1) {
      timerHelper.timerDelete(zapAfterTimer);
    }

    timerHelper.refresh();

    return state;
  }

  /**
   * Sends a message to the screen of the dreambox
   * 
   * @param message
   *          Message to send
   */
  public void sendMessage(String message) {
    try {
      final URL url = new URL("http://" + mConfig.getDreamboxAddress()
          + "/web/message?type=2&timeout=" + mConfig.getTimeout() + "&text="
          + URLEncoder.encode(message, "UTF8"));
      URLConnection connection = url.openConnection();

      String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
      String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
      connection.setRequestProperty("Authorization", "Basic " + encoded);

      connection.setConnectTimeout(mConfig.getTimeout());
      final InputStream stream = connection.getInputStream();
      stream.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * testing version of dreambox
   * 
   * @return version ok
   * @throws IOException
   */
  public boolean testDreamboxVersion() throws IOException {
    URL url = new URL("http://" + mConfig.getDreamboxAddress()
        + "/ipkg?command=info&package=enigma2-plugin-extensions-webinterface");

    URLConnection connection = url.openConnection();

    String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
    String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
    connection.setRequestProperty("Authorization", "Basic " + encoded);

    connection.setConnectTimeout(mConfig.getTimeout());
    InputStream stream = connection.getInputStream();

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    IOUtilities.pipeStreams(stream, bytes);

    String version = bytes.toString();

    Pattern p = Pattern.compile("Version:.*cvs(\\d{8}).*");
    Matcher match = p.matcher(version);

    if (match.find()) {
      if (Integer.parseInt(match.group(1)) >= WEBIF_MINIMUM_VERSION) {
        return true;
      }
    }

    return true; // immer true bei Antwort
  }

  /**
   * stream channel
   * 
   * @param channel
   * @return success
   */
  public boolean streamChannel(DreamboxChannel channel) {
    boolean success = false;

    if (new File(mConfig.getMediaplayer()).exists()) {
      try {
        final URL url = new URL("http://" + mConfig.getDreamboxAddress()
            + "/web/stream.m3u?ref="
            + URLEncoder.encode(channel.getReference(), "UTF8"));
        String cmd[] = { mConfig.getMediaplayer(), url.toString() };
        try {
          Runtime.getRuntime().exec(cmd);
          success = true;
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    return success;
  }

  /**
   * @return config
   */
  public DreamboxConfig getConfig() {
    // adopted by fishhead
    return mConfig;
  }

  // fishhead -------------------------
}
