/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package util.tvdataservice;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import util.io.IOUtilities;
import util.misc.ChangeTrackingProperties;
import util.misc.SoftReferenceCache;
import util.ui.LazyImageIcon;

import com.l2fprod.util.StringUtils;

import devplugin.AbstractTvDataService;

/**
 * A class that loads for Channels from a server.
 */
public class IconLoader {
  private static final Logger mLog = Logger.getLogger(IconLoader.class.getName());

  private File mIconDir;

  private File mIconIndexFile;

  private String mChannelGroup;

  private ChangeTrackingProperties mProperties;
  
  private AbstractTvDataService mDataService;
  

  /**
   * A cache for icons.
   */
  private static final SoftReferenceCache<String, File> ICON_CACHE = new SoftReferenceCache<String, File>();

  /**
   * A list with servers that don't respond on last access.
   */
  private static final ArrayList<String> BLOCKED_SERVERS = new ArrayList<String>(0);

  public IconLoader(final AbstractTvDataService service, final String channelGroup, final File dir) throws IOException {
    mDataService = service;
    mChannelGroup = channelGroup;
    mIconDir = new File(dir + "/icons_" + mChannelGroup);
    if (!mIconDir.exists()) {
      mIconDir.mkdirs();
    }
    mIconIndexFile = new File(mIconDir, "index.txt");
    mProperties = new ChangeTrackingProperties();
    if (mIconIndexFile.exists()) {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(
          mIconIndexFile), 0x1000);
      mProperties.load(in);
      in.close();
    } else {
      mLog.warning("no icons known for channel group " + channelGroup);
    }
  }

  public Icon getIcon(final String channelId, final String url) throws IOException {
    String key = new StringBuilder("icons_").append(mChannelGroup).append("_")
        .append(channelId).toString();
    String prevUrl = (String) mProperties.get(key);
    Icon icon = null;
    File iconFile = new File(mIconDir, escapedName(channelId));

    if (url.equals(prevUrl)) {
      // the url hasn't changed; we should have the icon locally
      icon = getIconFromFile(iconFile);
      return icon;
    } else {
      mLog.warning("iconUrl is not in cache for channelId " + channelId
              + ". prevUrl=" + prevUrl + ". currentUrl=" + url);
    }

    if (ICON_CACHE.containsKey(url)) {
      try {
        File iconCacheFile = ICON_CACHE.get(url);
        if (iconCacheFile != null && !iconCacheFile.equals(iconFile)) {
          IOUtilities.copy(ICON_CACHE.get(url), iconFile);
          icon = getIconFromFile(iconFile);
        }
      } catch (Exception e) {
        mLog.log(Level.SEVERE, "Problem while copying File from Cache", e);
      }
    }

    if (icon == null && mDataService.hasRightToDownloadIcons()
        && !BLOCKED_SERVERS.contains(url)) {
      // download the icon
      try {
        util.io.IOUtilities.download(new URL(url), iconFile);
        icon = getIconFromFile(iconFile);
        ICON_CACHE.put(url, iconFile);
      } catch (IOException e) {
        BLOCKED_SERVERS.add(url);
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

  private String escapedName(final String channelId) {
    return StringUtils.replace(channelId, ".", "_");
  }

  private String unescape(final String fileName) {
    return StringUtils.replace(fileName, "_", ".");
  }

  private Icon getIconFromFile(final File iconFile) {
    // rename old icon files on the fly to avoid file names ending in
    // ".com" which is reserved for executables on Microsoft platforms
    if (!iconFile.exists()) {
      String namePart = iconFile.getName();
      String unescapedPath = iconFile.getAbsolutePath().replace(namePart,
          unescape(namePart));
      File unescapedFile = new File(unescapedPath);
      if (unescapedFile.exists()) {
        unescapedFile.renameTo(iconFile);
      }
    }
    // now load the (renamed) image
    try {
      return new LazyImageIcon(iconFile.toURI().toURL());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void close() throws IOException {
    FileOutputStream out = new FileOutputStream(mIconIndexFile);
    out.getChannel().lock();
    mProperties.store(out, null);
    out.close();
  }
}