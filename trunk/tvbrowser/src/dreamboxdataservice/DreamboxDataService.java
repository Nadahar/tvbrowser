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
package dreamboxdataservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;


public class DreamboxDataService extends AbstractTvDataService {
    /**
     * get list of bouquets
     */
    private final static String BOUQUETLIST = "1:7:1:0:0:0:0:0:0:0:(type == 1) || (type == 17) || (type == 195) || (type == 25)FROM BOUQUET \"bouquets.tv\" ORDER BY bouquet";

    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxDataService.class);

    private static final Version VERSION = new Version(0, 6, 2);

    private ChannelGroup mChannelGroup = new ChannelGroupImpl("Dreambox", "Dreambox", "Dreambox", "Dreambox");

    private Properties mProperties;
    /**
     * List of Channels
     */
    private ArrayList<Channel> mChannels = new ArrayList<Channel>();


    public void setWorkingDirectory(File dataDir) {
    }

    public ChannelGroup[] getAvailableGroups() {
        return new ChannelGroup[]{mChannelGroup};
    }

    public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
        String ip = mProperties.getProperty("ip", "").trim();

        if (StringUtils.isNotEmpty(ip)) {
            int max = channelArr.length;

            monitor.setMaximum(max);
            monitor.setMessage(mLocalizer.msg("parsing", "Parsing Dreambox Data"));

            int pos = 0;
            for (Channel ch : channelArr) {
                pos++;
                monitor.setValue(pos);
                getEPGData(updateManager, ch);
            }

            monitor.setMessage("");

        }
    }

    public void loadSettings(Properties settings) {
        mProperties = settings;

        int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));

        mChannels = new ArrayList<Channel>();

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1:00");
        for (int i = 0; i < numChannels; i++) {
            Channel ch = new Channel(this, settings.getProperty("ChannelTitle-" + i, ""), settings.getProperty("ChannelId-"
                    + i, ""), timeZone, "de", "Imported from Dreambox", "", mChannelGroup, null, Channel.CATEGORY_TV);

            mChannels.add(ch);
        }
    }

    public Properties storeSettings() {
      Properties prop = new Properties();

      if (mProperties == null) {
        mProperties = new Properties();
      }
      prop.setProperty("ip", mProperties.getProperty("ip",""));
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
        return new DreamboxSettingsPanel(mProperties);
    }

    public Channel[] getAvailableChannels(ChannelGroup group) {
        return mChannels.toArray(new Channel[mChannels.size()]);
    }

    public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
        String ip = mProperties.getProperty("ip", "").trim();
        if (StringUtils.isNotEmpty(ip)) {
            mChannels = getChannels();
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
        return new PluginInfo(DreamboxDataService.class, mLocalizer.msg("name", "Dreambox Data"), mLocalizer.msg("desc", "Loads data from the dreambox."),
                "Bodo Tasche");
    }

    /**
     * @return All channels available in the dreambox
     */
    public ArrayList<Channel> getChannels() {
        try {
            ArrayList<Channel> allChannels = new ArrayList<Channel>();

            TreeMap<String, String> bouquets = getServiceDataBonquets(URLEncoder.encode(BOUQUETLIST, "UTF8"));

            if (bouquets != null) {
                for (String key : bouquets.keySet()) {
                    TreeMap<String, String> map = getServiceData(URLEncoder.encode(key, "UTF8"));

                    for (String mkey : map.keySet()) {
                        Channel ch = new Channel(this, map.get(mkey), "DREAM" + StringUtils.replace(mkey, ":", "_"), TimeZone.getTimeZone("GMT+1:00"), "de",
                                "Imported from Dreambox", "", mChannelGroup, null, Channel.CATEGORY_TV);
                        allChannels.add(ch);
                    }
                }
            }

            return allChannels;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new ArrayList<Channel>();
    }

    /**
     * @param service Service-ID
     * @return Data of specific service
     */
    public TreeMap<String, String> getServiceDataBonquets(String service) {
        try {
            URL url = new URL("http://" + mProperties.getProperty("ip","") + "/web/getservices?bRef=" + service);

            URLConnection connection = url.openConnection();

            String userpassword = mProperties.getProperty("username", "") + ":" + IOUtilities.xorEncode(mProperties.getProperty("password", ""), DreamboxSettingsPanel.PASSWORDSEED);
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(10);
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
            URL url = new URL("http://" + mProperties.getProperty("ip", "") + "/web/getservices?sRef=" + service);
            URLConnection connection = url.openConnection();

            String userpassword = mProperties.getProperty("username", "") + ":" + IOUtilities.xorEncode(mProperties.getProperty("password", ""), DreamboxSettingsPanel.PASSWORDSEED);
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(10);
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

    private void getEPGData(TvDataUpdateManager updateManager, Channel ch) {
        try {
            URL url = new URL("http://" + mProperties.getProperty("ip", "") + "/web/epgservice?sRef=" + StringUtils.replace(StringUtils.replace(ch.getId().substring(5), "_", ":"), " ", "%20"));

            URLConnection connection = url.openConnection();

            String userpassword = mProperties.getProperty("username", "") + ":" + IOUtilities.xorEncode(mProperties.getProperty("password", ""), DreamboxSettingsPanel.PASSWORDSEED);
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(60);
            InputStream stream = connection.getInputStream();

            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

            DreamboxChannelHandler handler = new DreamboxChannelHandler(updateManager, ch);

            saxParser.parse(stream, handler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
