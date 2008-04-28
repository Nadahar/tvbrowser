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

import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.utils.ProgramTime;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;
import util.io.IOUtilities;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connector for the Dreambox
 */
public class DreamboxConnector {
  /** get list of bouquets */
  private final String BOUQUETLIST = "1:7:1:0:0:0:0:0:0:0:(type == 1) || (type == 17) || (type == 195) || (type == 25)FROM BOUQUET \"bouquets.tv\" ORDER BY bouquet";
  /** Config of the Dreambox */
  private DreamboxConfig mConfig;

  private static final int WEBIFMINIMUMVERSION = 20070701;

  /**
     * Constructor
     * @param config Config of the dreambox
     */
    public DreamboxConnector(DreamboxConfig config) {
        mConfig = config;
    }

    /**
     * @param service Service-ID
     * @return Data of specific service
     */
    public TreeMap<String, String> getServiceDataBonquets(String service) {
        try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/getservices?bRef=" + service);

            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxHandler handler = new DreamboxHandler();

            saxParser.parse(stream, handler);

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
     * @param service Service-ID
     * @return Data of specific service
     */
    public TreeMap<String, String> getServiceData(String service) {
        try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/getservices?sRef=" + service);

            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxHandler handler = new DreamboxHandler();

            saxParser.parse(stream, handler);

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

            TreeMap<String, String> bouquets = getServiceDataBonquets(URLEncoder.encode(BOUQUETLIST, "UTF8"));

            for (String key : bouquets.keySet()) {
                String bouqetName = bouquets.get(key);
                TreeMap<String, String> map = getServiceData(URLEncoder.encode(key, "UTF8"));

                for (String mkey : map.keySet()) {
                    allChannels.add(new DreamboxChannel(mkey, map.get(mkey), bouqetName));
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
     * @param channel switch to this channel
     */
    public void switchToChannel(DreamboxChannel channel) {
        try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/zap?sRef=" + URLEncoder.encode(channel.getReference(), "UTF8"));
            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();
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
    private ArrayList<HashMap<String, String>> getTimers() {
        try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/timerlist");
            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxTimerHandler handler = new DreamboxTimerHandler();

            saxParser.parse(stream, handler);

            return handler.getTimers();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param config DreamboxConfig
     * @return List of recordings on the dreambox
     */
    public ProgramTime[] getRecordings(DreamboxConfig config) {
        ArrayList<ProgramTime> programs = new ArrayList<ProgramTime>();

        ArrayList<HashMap<String, String>> timers = getTimers();

        if (timers == null) {
            return new ProgramTime[0];
        }

        for(HashMap<String, String> timer:timers) {

            DreamboxChannel channel = config.getDreamboxChannelForRef(timer.get("e2servicereference"));

            if (channel != null) {
                Channel tvbchannel = config.getChannel(channel);
                if (tvbchannel != null) {
                    Calendar begin = Calendar.getInstance();
                    begin.setTimeInMillis(getLong(timer.get("e2timebegin"))*1000);
                    int beginMinutes = begin.get(Calendar.HOUR_OF_DAY) * 60 + begin.get(Calendar.MINUTE);

                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(getLong(timer.get("e2timeend"))*1000);

                    int endMinutes = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE);

                    if (endMinutes < beginMinutes) {
                        endMinutes += 24*60;
                    }

                    Calendar runner = (Calendar) begin.clone();

                    long days = end.get(Calendar.DAY_OF_YEAR) - begin.get(Calendar.DAY_OF_YEAR);

                    if (end.get(Calendar.YEAR) != begin.get(Calendar.YEAR)) {
                        days = 1;
                    }

                    for (int i=0;i<=days;i++) {
                        Iterator<Program> it = CapturePlugin.getPluginManager()
                                    .getChannelDayProgram(new Date(runner), tvbchannel);
                        if (it != null) {
                            boolean found = false;

                            while (it.hasNext() && !found) {
                                Program prog = it.next();
                                int progTime = prog.getHours() * 60 + prog.getMinutes() + (i*24*60);

                                if (progTime >= beginMinutes - 15 &&
                                    progTime <= endMinutes + 15
                                    && prog.getTitle().trim().equalsIgnoreCase(timer.get("e2name").trim())
                                    ) {

                                    found = true;
                                    programs.add(new ProgramTime(prog, begin.getTime(), end.getTime()));
                                }
                            }
                        }

                        runner.add(Calendar.HOUR_OF_DAY, 24);
                    }


                }
            }
        }

        return programs.toArray(new ProgramTime[0]);
    }

    /**
     * Tries to parse a Long
     * @param longStr String with Long-Value
     * @return long-Value or -1
     */
    private long getLong(String longStr) {
        if (longStr.contains(".")) {
            longStr = longStr.substring(0, longStr.indexOf('.'));
        }

        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Add a recording to the Dreambox
     * @param dreamboxChannel the DreamboxChannel for the Program
     * @param prgTime add this ProgramTime @return true, if succcesfull
     * @param afterEvent 0=nothing, 1=standby, 2=deepstandby
     * @param timezone TimeZone to use for recording
     * @return True, if successfull
     */
    public boolean addRecording(DreamboxChannel dreamboxChannel, ProgramTime prgTime, int afterEvent, TimeZone timezone) {
        try {
            Calendar start = prgTime.getStartAsCalendar();
            start.setTimeZone(timezone);

            Calendar end = prgTime.getEndAsCalendar();
            end.setTimeZone(timezone);

            String shortInfo = prgTime.getProgram().getShortInfo();
            if (shortInfo == null) {
                shortInfo = "";
            }

            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/tvbrowser?&command=add&action=0" +
                    "&syear=" + start.get(Calendar.YEAR) +
                    "&smonth=" + (start.get(Calendar.MONTH)+1) +
                    "&sday=" + start.get(Calendar.DAY_OF_MONTH) +
                    "&shour=" + start.get(Calendar.HOUR_OF_DAY)+
                    "&smin=" + start.get(Calendar.MINUTE)+

                    "&eyear=" + end.get(Calendar.YEAR) +
                    "&emonth=" + (end.get(Calendar.MONTH)+1) +
                    "&eday=" + end.get(Calendar.DAY_OF_MONTH) +
                    "&ehour=" + end.get(Calendar.HOUR_OF_DAY)+
                    "&emin=" + end.get(Calendar.MINUTE)+

                    "&sRef=" + URLEncoder.encode(dreamboxChannel.getName() +"|"+dreamboxChannel.getReference(), "UTF8") +
                    "&name=" + URLEncoder.encode(prgTime.getProgram().getTitle(), "UTF8") +
                    "&description=" + URLEncoder.encode(shortInfo, "UTF8") +

                    "&afterevent="+afterEvent+
                    "&eit=&disabled=0&justplay=0&repeated=0");

            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxStateHandler handler = new DreamboxStateHandler();

            saxParser.parse(stream, handler);

            return (handler.getState().equalsIgnoreCase("true"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Remove a recording from the Dreambox
     * @param dreamboxChannel the DreamboxChannel for the Program
     * @param prgTime ProgramTime to remove @return true, if successfull
     * @param timezone Timezone to use for recording
     * @return True, if successfull
     */
    public boolean removeRecording(DreamboxChannel dreamboxChannel, ProgramTime prgTime, TimeZone timezone) {
        try {
            Calendar start = prgTime.getStartAsCalendar();
            start.setTimeZone(timezone);

            Calendar end = prgTime.getEndAsCalendar();
            end.setTimeZone(timezone);

            String shortInfo = prgTime.getProgram().getShortInfo();
            if (shortInfo == null) {
                shortInfo = "";
            }

            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/tvbrowser?&command=del&action=0" +
                    "&syear=" + start.get(Calendar.YEAR) +
                    "&smonth=" + (start.get(Calendar.MONTH)+1) +
                    "&sday=" + start.get(Calendar.DAY_OF_MONTH) +
                    "&shour=" + start.get(Calendar.HOUR_OF_DAY)+
                    "&smin=" + start.get(Calendar.MINUTE)+

                    "&eyear=" + end.get(Calendar.YEAR) +
                    "&emonth=" + (end.get(Calendar.MONTH)+1) +
                    "&eday=" + end.get(Calendar.DAY_OF_MONTH) +
                    "&ehour=" + end.get(Calendar.HOUR_OF_DAY)+
                    "&emin=" + end.get(Calendar.MINUTE)+

                    "&sRef=" + URLEncoder.encode(dreamboxChannel.getName() +"|"+dreamboxChannel.getReference(), "UTF8") +
                    "&name=" + URLEncoder.encode(prgTime.getProgram().getTitle(), "UTF8") +
                    "&description=" + URLEncoder.encode(shortInfo, "UTF8") +

                    "&afterevent=0&eit=&disabled=0&justplay=0&repeated=0");

            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxStateHandler handler = new DreamboxStateHandler();

            saxParser.parse(stream, handler);

            return (handler.getState().equalsIgnoreCase("true"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sends a message to the screen of the dreambox
     * @param message Message to send
     */
    public void sendMessage(String message) {
        try {
            final URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/message?type=2&timeout=" + mConfig.getTimeout() + "&text=" + URLEncoder.encode(message, "UTF8"));
            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            final InputStream stream = connection.getInputStream();
            stream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  public boolean testDreamboxVersion() throws IOException {
    URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/ipkg?command=info&package=enigma2-plugin-extensions-webinterface");

    URLConnection connection = url.openConnection();

    String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
    String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
    connection.setRequestProperty  ("Authorization", "Basic " + encoded);

    connection.setConnectTimeout(mConfig.getTimeout());
    InputStream stream = connection.getInputStream();

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    IOUtilities.pipeStreams(stream, bytes);

    String version = bytes.toString();

    Pattern p = Pattern.compile("Version:.*cvs(\\d{8}).*");
    Matcher match = p.matcher(version);

    if (match.find()) {
      if (new Integer(match.group(1)) >= WEBIFMINIMUMVERSION) {
        return true;
      }
    }

    return false;
  }

  public boolean streamChannel(DreamboxChannel channel) {
    boolean success = false;
    
    if (new File(mConfig.getMediaplayer()).exists()) {
      try {
        final URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/stream.m3u?ref=" + URLEncoder.encode(channel.getReference(), "UTF8"));
        String cmd[] = {mConfig.getMediaplayer(),  url.toString()};
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
}
