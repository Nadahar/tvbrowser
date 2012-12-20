/*
 * CapturePlugin by
 *  Andreas Hessel (Vidrec@gmx.de),
 *  Bodo Tasche,
 *  Markus Ruderman
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
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.JAXB;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.jaxb.e2abouts.generated.E2About;
import captureplugin.drivers.dreambox.jaxb.e2abouts.generated.E2Abouts;
import captureplugin.drivers.utils.ProgramTime;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Connector for the Dreambox
 */
public class DreamboxConnector {

    /** Dreambox Web Interface Version Separator - e.g. "Major.Minor.Patch" */
    private static final String WEBINTERFACE_VERSION_SEPARATOR = "\\.";

    /** Minimum required version of the Dreambox Web Interface */
    private static final String WEBINTERFACE_REQUIRED_MIN_VERSION = "1.6.0";

    /** Get list of bouquets */
    private final static String BOUQUET_LIST = "1:7:1:0:0:0:0:0:0:0:(type == 1) || (type == 17) || (type == 195) || (type == 25)FROM BOUQUET \"bouquets.tv\" ORDER BY bouquet";

    /** Config of the Dreambox */
    private DreamboxConfig mConfig;

    /** Holds the last state text of the {@link DreamboxStateHandler} query */
    private String mLastStateText="";

    /**
     * Constructor
     *
     * @param config
     *            Config of the dreambox
     */
    public DreamboxConnector(DreamboxConfig config) {
        mConfig = config;
    }

    /**
     * @param service
     *            Service-ID
     * @return Data of specific service
     */
    private TreeMap<String, String> getServiceDataBouquets(String service) {
        if (!mConfig.hasValidAddress()) {
            return null;
        }
        try {
            InputStream stream = openStreamForLocalUrl("/web/getservices?bRef="
                    + service);
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

    private InputStream openStreamForLocalUrl(final String localUrl)
            throws MalformedURLException, IOException {
        URL url = new URL("http://" + mConfig.getDreamboxAddress() + localUrl);
        URLConnection connection = url.openConnection();

        // set user and password
        String userpassword = mConfig.getUserName() + ":"
                + mConfig.getPassword();
        String encoded = new String(
                Base64.encodeBase64(userpassword.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + encoded);

        // set timeout
        connection.setConnectTimeout(mConfig.getTimeout());
        InputStream stream = connection.getInputStream();
        return stream;
    }

    /**
     * @param service
     *            Service-ID
     * @return Data of specific service
     */
    private TreeMap<String, String> getServiceData(String service) {
        if (!mConfig.hasValidAddress()) {
            return null;
        }
        try {
            InputStream stream = openStreamForLocalUrl("/web/getservices?sRef="
                    + service);
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

            TreeMap<String, String> bouquets = getServiceDataBouquets(URLEncoder
                    .encode(BOUQUET_LIST, "UTF8"));
            for (Entry<String, String> entry : bouquets.entrySet()) {
                String key = entry.getKey();
                String bouqetName = entry.getValue();

                TreeMap<String, String> map = getServiceData(URLEncoder.encode(
                        key, "UTF8"));

                for (Entry<String, String> mEntry : map.entrySet()) {
                    String mkey = mEntry.getKey();
                    allChannels.add(new DreamboxChannel(mkey,
                            mEntry.getValue(), bouqetName));
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
     *            switch to this channel
     */
    public void switchToChannel(DreamboxChannel channel) {
        try {
            InputStream stream = openStreamForLocalUrl("/web/zap?sRef="
                    + URLEncoder.encode(channel.getReference(), "UTF8"));
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
        if (!mConfig.hasValidAddress()) {
            return null;
        }
        try {
            InputStream stream = openStreamForLocalUrl("/web/timerlist");
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
     * @param config
     *            DreamboxConfig
     * @return List of recordings on the dreambox
     */
    public ProgramTime[] getRecordings(DreamboxConfig config) {
        ArrayList<ProgramTime> programs = new ArrayList<ProgramTime>();

        ArrayList<HashMap<String, String>> timers = getTimers();

        if (timers == null) {
            return new ProgramTime[0];
        }

        for (HashMap<String, String> timer : timers) {

            DreamboxChannel channel = config.getDreamboxChannelForRef(timer
                    .get("e2servicereference"));

            if (channel != null) {
                Channel tvbchannel = config.getChannel(channel);
                if (tvbchannel != null) {
                    Calendar begin = Calendar.getInstance();
                    begin.setTimeInMillis(getLong(timer.get("e2timebegin")) * 1000);
                    int beginMinutes = begin.get(Calendar.HOUR_OF_DAY) * 60
                            + begin.get(Calendar.MINUTE);

                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(getLong(timer.get("e2timeend")) * 1000);

                    int endMinutes = end.get(Calendar.HOUR_OF_DAY) * 60
                            + end.get(Calendar.MINUTE);
                    if (endMinutes < beginMinutes) {
                        endMinutes += 24 * 60;
                    }

                    Calendar runner = (Calendar) begin.clone();

                    long days = end.get(Calendar.DAY_OF_YEAR)
                            - begin.get(Calendar.DAY_OF_YEAR);
                    if (end.get(Calendar.YEAR) != begin.get(Calendar.YEAR)) {
                        days = 1;
                    }

                    for (int i = 0; i <= days; i++) {
                        Iterator<Program> it = Plugin.getPluginManager()
                                .getChannelDayProgram(new Date(runner),
                                        tvbchannel);
                        boolean found = false;

                        while (it.hasNext() && !found) {
                            Program prog = it.next();
                            int progTime = prog.getHours() * 60
                                    + prog.getMinutes() + (i * 24 * 60);

                            if (progTime >= beginMinutes - 15
                                    && progTime <= endMinutes + 15
                                    && prog.getTitle()
                                            .trim()
                                            .equalsIgnoreCase(
                                                    timer.get("e2name").trim())) {

                                found = true;
                                programs.add(new ProgramTime(prog, begin
                                        .getTime(), end.getTime()));
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
     *            String with Long-Value
     * @return long-Value or -1
     */
    private long getLong(String longStr) {
        if (longStr.contains(".")) {
            longStr = StringUtils.substringBefore(longStr, ".");
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
     *
     * @param dreamboxChannel
     *            the DreamboxChannel for the Program
     * @param prgTime
     *            add this ProgramTime
     * @param afterEvent
     *            0=nothing, 1=standby, 2=deepstandby, 3=auto
     * @param timezone
     *            TimeZone to use for recording
     * @return True, if successful
     */
    public boolean addRecording(DreamboxChannel dreamboxChannel,
            ProgramTime prgTime, int afterEvent, TimeZone timezone) {
        if (!mConfig.hasValidAddress()) {
            return false;
        }
        try {
            Calendar start = prgTime.getStartAsCalendar();
            start.setTimeZone(timezone);

            Calendar end = prgTime.getEndAsCalendar();
            end.setTimeZone(timezone);

            String shortInfo = prgTime.getProgram().getShortInfo();
            if (shortInfo == null) {
                shortInfo = "";
            }

            InputStream stream = openStreamForLocalUrl("/web/tvbrowser?&command=add&action=0"
                    + "&syear="
                    + start.get(Calendar.YEAR)
                    + "&smonth="
                    + (start.get(Calendar.MONTH) + 1)
                    + "&sday="
                    + start.get(Calendar.DAY_OF_MONTH)
                    + "&shour="
                    + start.get(Calendar.HOUR_OF_DAY)
                    + "&smin="
                    + start.get(Calendar.MINUTE)
                    +

                    "&eyear="
                    + end.get(Calendar.YEAR)
                    + "&emonth="
                    + (end.get(Calendar.MONTH) + 1)
                    + "&eday="
                    + end.get(Calendar.DAY_OF_MONTH)
                    + "&ehour="
                    + end.get(Calendar.HOUR_OF_DAY)
                    + "&emin="
                    + end.get(Calendar.MINUTE)
                    +

                    "&sRef="
                    + URLEncoder.encode(dreamboxChannel.getName() + "|"
                            + dreamboxChannel.getReference(), "UTF8")
                    + "&name="
                    + URLEncoder
                            .encode(prgTime.getProgram().getTitle(), "UTF8")
                    + "&description="
                    + URLEncoder.encode(shortInfo, "UTF8")
                    +

                    "&afterevent="
                    + afterEvent
                    + "&eit=&disabled=0&justplay=0&repeated=0");

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            DreamboxStateHandler handler = new DreamboxStateHandler();
            saxParser.parse(stream, handler);
            mLastStateText = handler.getStatetext();
            return (Boolean.valueOf(handler.getState()));
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
     *
     * @param dreamboxChannel
     *            the DreamboxChannel for the Program
     * @param prgTime
     *            ProgramTime to remove @return true, if successful
     * @param timezone
     *            Timezone to use for recording
     * @return True, if successful
     */
    public boolean removeRecording(DreamboxChannel dreamboxChannel,
            ProgramTime prgTime, TimeZone timezone) {
        if (!mConfig.hasValidAddress()) {
            return false;
        }
        try {
            Calendar start = prgTime.getStartAsCalendar();
            start.setTimeZone(timezone);

            Calendar end = prgTime.getEndAsCalendar();
            end.setTimeZone(timezone);

            String shortInfo = prgTime.getProgram().getShortInfo();
            if (shortInfo == null) {
                shortInfo = "";
            }

            InputStream stream = openStreamForLocalUrl("/web/tvbrowser?&command=del&action=0"
                    + "&syear="
                    + start.get(Calendar.YEAR)
                    + "&smonth="
                    + (start.get(Calendar.MONTH) + 1)
                    + "&sday="
                    + start.get(Calendar.DAY_OF_MONTH)
                    + "&shour="
                    + start.get(Calendar.HOUR_OF_DAY)
                    + "&smin="
                    + start.get(Calendar.MINUTE)
                    +

                    "&eyear="
                    + end.get(Calendar.YEAR)
                    + "&emonth="
                    + (end.get(Calendar.MONTH) + 1)
                    + "&eday="
                    + end.get(Calendar.DAY_OF_MONTH)
                    + "&ehour="
                    + end.get(Calendar.HOUR_OF_DAY)
                    + "&emin="
                    + end.get(Calendar.MINUTE)
                    +

                    "&sRef="
                    + URLEncoder.encode(dreamboxChannel.getName() + "|"
                            + dreamboxChannel.getReference(), "UTF8")
                    + "&name="
                    + URLEncoder
                            .encode(prgTime.getProgram().getTitle(), "UTF8")
                    + "&description=" + URLEncoder.encode(shortInfo, "UTF8") +

                    "&afterevent=0&eit=&disabled=0&justplay=0&repeated=0");

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            DreamboxStateHandler handler = new DreamboxStateHandler();
            saxParser.parse(stream, handler);
            mLastStateText = handler.getStatetext();
            return Boolean.valueOf(handler.getState());
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
     *
     * @param message
     *            Message to send
     */
    public void sendMessage(String message) {
        if (!mConfig.hasValidAddress()) {
            return;
        }
        try {
            InputStream stream = openStreamForLocalUrl("/web/message?type=2&timeout="
                    + mConfig.getTimeout()
                    + "&text="
                    + URLEncoder.encode(message, "UTF8"));
            stream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the minimum required web interface version number.
     *
     * @return The minimum required web interface version number.
     */
    public String getMinRequiredWebIfVersion() {
    	return WEBINTERFACE_REQUIRED_MIN_VERSION;
    }

    /**
     * Get the web interface information from http://<dreambox IP>/web/about
     * which returns as result an XML answer in the format as described at
     * http://e2devel.com/apidoc/webif/#about <br/>
     * For parsing of the XML answer the implementation uses JAXB with
     * generated sources out of the self designed e2abouts.xsd located
     * in the project at /xsd/dreambox.
     *
     * @return The current web interface version number.
     * @throws IOException if the answer can't be read
     */
    public String getWebIfVersion() throws IOException {

        InputStream xmlStream = openStreamForLocalUrl("/web/about");

        E2Abouts e2abouts = JAXB.unmarshal(xmlStream, E2Abouts.class);
        E2About e2about = null;
        String webIfVersion = "";

        if (e2abouts != null) {
            e2about = e2abouts.getE2About();
        }

        if (e2about != null) {
            webIfVersion = e2about.getE2Webifversion();
        }

        return webIfVersion;
    }

    /**
     * Checks if the specified web interface version number is
     * valid for the usage of the plugin. Therefore the specified
     * version number will be compared with minimum required
     * version number which currently {@value #WEBINTERFACE_REQUIRED_MIN_VERSION}.
     *
     * @param currentVersion
     * 			the web interface version number received
     * 		  	from the Dreambox via {@link #getWebIfVersion()}
     *
     * @return {@code true} if the specified version number is valid
     * 		   for the plugin usage, otherwise {@code false}.
     *
     * @see #getWebIfVersion()
     */
    public boolean isValidWebIfVersion(String currentVersion) {

        boolean result = false;

    	if ( (currentVersion != null) &&
    		 (!currentVersion.isEmpty()) ) {

            int compareResult =
            	compareVersions(currentVersion, WEBINTERFACE_REQUIRED_MIN_VERSION);

            if (compareResult >= 0) {
            	result = true;
            } else {
            	result = false;
            }
    	}

        return result;

    }

    /**
     * Compares this version string to another version string by dotted
     * ordinals.
     *
     * @param str1
     * 			first version string to compare
     * @param str2
     * 			second version string to compare
     *
     * @return 0 if equal, -1 if {@code str1} &lt; {@code str2}
     * 		   and 1 otherwise.
     *
     * @see http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
     */
    public int compareVersions(String str1, String str2){

    	// Short circuit when you shoot for efficiency
    	if (str1.equals(str2)) {
    		return 0;
    	}

	    String[] vals1 = str1.split(WEBINTERFACE_VERSION_SEPARATOR);
	    String[] vals2 = str2.split(WEBINTERFACE_VERSION_SEPARATOR);

	    int i=0;

	    // Most efficient way to skip past equal version sub-parts
	    while ( (i < vals1.length) &&
	    	    (i < vals2.length) &&
	    	    ( vals1[i].equals(vals2[i]) )
	    	  ) {

	    	i++;
	    }

	    // If we didn't reach the end,

	    if( (i < vals1.length) && (i < vals2.length) ) {
	        // have to use integer comparison to avoid the "10"<"1" problem
	        return Integer.valueOf(vals1[i]).compareTo( Integer.valueOf(vals2[i]) );
	    }

	    // end of str2, check if str1 is all 0's
	    if (i < vals1.length) {
	        boolean allZeros = true;
	        for (int j = i; allZeros & (j < vals1.length); j++) {
	            allZeros &= ( Integer.parseInt( vals1[j] ) == 0 );
	        }
	        return (allZeros ? 0 : -1);
	    }

	    // end of str1, check if str2 is all 0's
	    if (i < vals2.length) {
	        boolean allZeros = true;
	        for(int j = i; allZeros & (j < vals2.length); j++) {
	            allZeros &= ( Integer.parseInt( vals2[j] ) == 0 );
	        }
	        return (allZeros ? 0 : 1);
	    }

	    return 0;
    }

    public boolean streamChannel(final DreamboxChannel channel) {
        if (!mConfig.hasValidAddress()) {
            return false;
        }
        if (StringUtils.isEmpty(mConfig.getMediaplayer())) {
            return false;
        }

        if (new File(mConfig.getMediaplayer()).exists()) {
            try {
                final URL url = new URL("http://"
                        + mConfig.getDreamboxAddress() + "/web/stream.m3u?ref="
                        + URLEncoder.encode(channel.getReference(), "UTF8"));
                String[] cmd = { mConfig.getMediaplayer(), url.toString() };
                try {
                    Runtime.getRuntime().exec(cmd);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Get the last state text of the {@link DreamboxStateHandler} query.
     *
     * @return The text of last state query.
     */
    public String getLastStateText() {
    	return mLastStateText;
    }

}
