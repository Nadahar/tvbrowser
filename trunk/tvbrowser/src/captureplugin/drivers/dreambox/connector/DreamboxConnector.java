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

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.CapturePlugin;
import devplugin.Program;
import devplugin.Channel;
import devplugin.Date;

/**
 * Connector for the Dreambox
 */
public class DreamboxConnector {
    /** get list of bouquets */
    private final String BOUQUETLIST = "1:7:1:0:0:0:0:0:0:0:(type == 1) || (type == 17) || (type == 195) || (type == 25)FROM BOUQUET \"bouquets.tv\" ORDER BY bouquet";
    /** IP-Address of the dreambox */
    private String mAddress;

    /**
     * Constructor
     * @param address IP-Address of the dreambox
     */
    public DreamboxConnector(String address) {
        mAddress = address;
    }

    /**
     * @param service Service-ID
     * @return Data of specific service
     */
    public TreeMap<String, String> getServiceData(String service) {
        try {
            URL url = new URL("http://" + mAddress + "/web/fetchchannels?ServiceListBrowse=" + service);
            InputStream stream = url.openStream();

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

            TreeMap<String, String> bouquets = getServiceData(URLEncoder.encode(BOUQUETLIST, "UTF8"));
            Iterator<String> it = bouquets.keySet().iterator();

            for (String key : bouquets.keySet()) {
                TreeMap<String, String> map = getServiceData(URLEncoder.encode(key, "UTF8"));

                for (String mkey : map.keySet()) {
                    allChannels.add(new DreamboxChannel(mkey, map.get(mkey)));
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
            URL url = new URL("http://" + mAddress + "/web/zap?ZapTo=" + URLEncoder.encode(channel.getReference(), "UTF8"));
            url.openStream();
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
            URL url = new URL("http://" + mAddress + "/web/timerlist");
            InputStream stream = url.openStream();

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
        }
        return null;
    }

    /**
     * @param config DreamboxConfig
     * @return List of recordings on the dreambox
     */
    public Program[] getRecordings(DreamboxConfig config) {
        ArrayList<Program> programs = new ArrayList<Program>();

        ArrayList<HashMap<String, String>> timers = getTimers();

        for(HashMap<String, String> timer:timers) {

            DreamboxChannel channel = config.getDreamboxChannelForRef(timer.get("e2servicereference"));

            if (channel != null) {
                Channel tvbchannel = config.getChannel(channel);
                Calendar begin = Calendar.getInstance();
                begin.setTimeInMillis(Long.parseLong(timer.get("e2timebegin"))*1000);

                Calendar end = Calendar.getInstance();
                end.setTimeInMillis(Long.parseLong(timer.get("e2timeend"))*1000);

                Date date = new Date(begin);

                Iterator it = CapturePlugin.getPluginManager()
                            .getChannelDayProgram(date, tvbchannel);
                if (it != null) {
                    boolean found = false;

                    while (it.hasNext() && !found) {
                        Program prog = (Program) it.next();

                        if (prog.getHours() >= begin.get(Calendar.HOUR_OF_DAY) &&
                            prog.getMinutes() >= begin.get(Calendar.MINUTE) &&
                            prog.getHours() <= end.get(Calendar.HOUR_OF_DAY) &&
                            prog.getMinutes() <= end.get(Calendar.MINUTE)
                            && prog.getTitle().trim().equalsIgnoreCase(timer.get("e2name").trim())
                            ) {
                            found = true;
                            programs.add(prog);
                        }
                    }
                }

            }

        }

        return programs.toArray(new Program[0]);
    }
}
