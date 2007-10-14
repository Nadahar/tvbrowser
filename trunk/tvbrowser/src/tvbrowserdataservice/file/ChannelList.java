/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowserdataservice.file;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowserdataservice.TvBrowserDataService;
import tvdataservice.TvDataService;
import util.io.FileFormatException;
import util.ui.ImageUtilities;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ChannelList {

  public static final String FILE_NAME = "channellist.gz";

  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ChannelList.class.getName());

  private ArrayList<ChannelItem> mChannelList;

  private ChannelGroup mGroup;

  /**
   * Icon Cache
   */
  private static WeakHashMap<String, File> ICON_CACHE = new WeakHashMap<String, File>();

  public ChannelList(final String groupName) {
    mChannelList = new ArrayList<ChannelItem>();
    mGroup = new ChannelGroupImpl(groupName, groupName, "");
  }

  public ChannelList(ChannelGroup group) {
    mChannelList = new ArrayList<ChannelItem>();
    mGroup = group;
  }

  public void addChannel(Channel channel) {
    mChannelList.add(new ChannelItem(channel, null));
  }

  public void addChannel(Channel channel, String iconUrl) {
    mChannelList.add(new ChannelItem(channel, iconUrl));
  }

  public int getChannelCount() {
    return mChannelList.size();
  }

  public Channel getChannelAt(int index) {
    ChannelItem item = mChannelList.get(index);
    return item.getChannel();
  }

  public Channel[] createChannelArray() {
    Channel[] channelArr = new Channel[mChannelList.size()];
    for (int i = 0; i < channelArr.length; i++) {
      channelArr[i] = (mChannelList.get(i)).getChannel();
    }
    return channelArr;
  }

  public void readFromStream(InputStream stream, TvDataService dataService) throws IOException, FileFormatException {
    GZIPInputStream gIn = new GZIPInputStream(stream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(gIn, "ISO-8859-15"));

    String line;
    int lineCount = 1;

    /**
     * ChannelList.readFromStream is called by both MirrorUpdater and
     * TvBrowserDataService. The MirrorUpdater calls this method without
     * DataService and doesn't need the IconLoader
     */
    IconLoader iconLoader = null;
    if (dataService != null && dataService instanceof TvBrowserDataService) {
      File dataDir = ((TvBrowserDataService) dataService).getWorkingDirectory();
      iconLoader = new IconLoader(mGroup.getId(), dataDir);
    }

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        // This is not an empty line -> read it
        /*
         * Instead of StringTokenizer (previous implementation) we use
         * String#split(), because StringTokenizer ignores empty tokens.
         */
        String[] tokens = line.split(";");
        if (tokens.length < 4) {
          throw new FileFormatException("Syntax error in mirror file line " + lineCount + ": '" + line + "'");
        }

        String country = null, timezone = null, id = null, name = null, copyright = null, webpage = null, iconUrl = null, categoryStr = null;
        try {
          country = tokens[0];
          timezone = tokens[1];
          id = tokens[2];
          name = tokens[3];
          copyright = tokens[4];
          webpage = tokens[5];
          iconUrl = tokens[6];
          categoryStr = tokens[7];
        } catch (ArrayIndexOutOfBoundsException e) {
          // ignore
        }

        int categories = Channel.CATEGORY_NONE;
        if (categoryStr != null) {
          try {
            categories = Integer.parseInt(categoryStr);
          } catch (NumberFormatException e) {
            categories = Channel.CATEGORY_NONE;
          }
        }
        Channel channel = new Channel(dataService, name, id, TimeZone.getTimeZone(timezone), country, copyright,
            webpage, mGroup, null, categories);
        if (iconLoader != null && iconUrl != null && iconUrl.length() > 0) {
          Icon icon = iconLoader.getIcon(id, iconUrl);
          if (icon != null) {
            channel.setDefaultIcon(icon);
          }
        }
        addChannel(channel);
      }
      lineCount++;
    }

    gIn.close();
    if (iconLoader != null) {
      iconLoader.close();
    }
  }

  public void readFromFile(File file, TvDataService dataService) throws IOException, FileFormatException {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 0x2000);

      readFromStream(stream, dataService);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  public void writeToStream(OutputStream stream) throws IOException, FileFormatException {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    PrintWriter writer = new PrintWriter(gOut);
    for (int i = 0; i < getChannelCount(); i++) {
      ChannelItem channelItem = mChannelList.get(i);
      Channel channel = channelItem.getChannel();
      StringBuffer line = new StringBuffer();
      line.append(channel.getCountry()).append(";").append(channel.getTimeZone().getID());
      line.append(";").append(channel.getId());
      line.append(";").append(channel.getName());
      line.append(";").append(channel.getCopyrightNotice());
      line.append(";").append(channel.getWebpage() == null ? "http://tvbrowser.org" : channel.getWebpage());
      line.append(";").append(channelItem.getIconUrl() == null ? "" : channelItem.getIconUrl());
      line.append(";").append(channel.getCategories());
      writer.println(line.toString());
    }
    writer.close();

    gOut.close();
  }

  public void writeToFile(File file) throws IOException, FileFormatException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    // outer block.

    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);

        writeToStream(stream);
      } finally {
        // Close the file in every case
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException exc) {
          }
        }
      }
    } catch (IOException exc) {
      file.delete();
      throw exc;
    } catch (FileFormatException exc) {
      file.delete();
      throw new FileFormatException("Writing file failed " + file.getAbsolutePath(), exc);
    }
  }

  class IconLoader {
    private File mIconDir;

    private File mIconIndexFile;

    private String mGroup;

    private Properties mProperties;

    public IconLoader(String group, File dir) throws IOException {
      mGroup = group;
      mIconDir = new File(dir + "/icons_" + mGroup);
      if (!mIconDir.exists()) {
        mIconDir.mkdirs();
      }
      mIconIndexFile = new File(mIconDir, "index.txt");
      mProperties = new Properties();
      if (mIconIndexFile.exists()) {
        mProperties.load(new BufferedInputStream(new FileInputStream(mIconIndexFile), 0x1000));
      } else {
        mLog.severe("index.txt not found");
        // System.exit(-1);
      }
    }

    public Icon getIcon(String channelId, String url) throws IOException {
      String key = new StringBuffer("icons_").append(mGroup).append("_").append(channelId).toString();
      String prevUrl = (String) mProperties.get(key);
      Icon icon = null;
      File iconFile = new File(mIconDir, channelId);

      if (url.equals(prevUrl)) {
        // the url hasn't changed; we should have the icon locally
        icon = getIconFromFile(iconFile);
        return icon;
      }

      if (icon == null) {
        if (ICON_CACHE.containsKey(url)) {
          try {
            if (!ICON_CACHE.get(url).equals(iconFile)) {
              copyFile(ICON_CACHE.get(url), iconFile);
              icon = getIconFromFile(iconFile);
            }
          } catch (Exception e) {
            mLog.log(Level.SEVERE, "Problem while copying File from Cache", e);
          }

        }
      }

      if (icon == null && TvBrowserDataService.getInstance().hasRightToDownloadIcons()) {
        // download the icon
        try {
          util.io.IOUtilities.download(new URL(url), iconFile);
          icon = getIconFromFile(iconFile);
          ICON_CACHE.put(url, iconFile);
        } catch (IOException e) {
          mLog.warning("channel " + channelId + ": could not download icon from " + url);
        } catch (Exception e) {
          mLog.severe("Could not extract icon file");
        }
      }
      if (icon != null) {
        mProperties.setProperty(key, url);
      }

      return icon;
    }

    /**
     * Fast Copy of a File
     * @param source Source File
     * @param dest Destination File
     */
    private void copyFile(File source, File dest) {
      try {
        // Create channel on the source
        FileChannel srcChannel = new FileInputStream(source).getChannel();

        // Create channel on the destination
        FileChannel dstChannel = new FileOutputStream(dest).getChannel();

        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        // Close the channels
        srcChannel.close();
        dstChannel.close();
      } catch (IOException e) {
      }
    }

    private Icon getIconFromFile(File file) {
      Image img = ImageUtilities.createImage(file.getAbsolutePath());
      if (img != null) {
        return new ImageIcon(img);
      }
      return null;
    }

    private void close() throws IOException {
      mProperties.store(new FileOutputStream(mIconIndexFile), null);
    }
  }

  class ChannelItem {
    private Channel mChannel;

    private String mIconUrl;

    public ChannelItem(Channel ch, String iconUrl) {
      mChannel = ch;
      mIconUrl = iconUrl;
    }

    public Channel getChannel() {
      return mChannel;
    }

    public String getIconUrl() {
      return mIconUrl;
    }

  }
}
