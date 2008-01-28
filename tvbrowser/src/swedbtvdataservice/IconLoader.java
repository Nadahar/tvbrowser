package swedbtvdataservice;

import util.ui.ImageUtilities;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;

class IconLoader {
  private static Logger mLog = Logger.getLogger(IconLoader.class.getName());

  private File mIconDir;

  private File mIconIndexFile;

  private String mGroup;

  private Properties mProperties;
  private SweDBTvDataService mDataHydraTvDataService;

  public IconLoader(SweDBTvDataService DataHydraDataService, String group, File dir) throws IOException {
    mDataHydraTvDataService = DataHydraDataService;
    mGroup = group;
    mIconDir = new File(dir + "/icons_" + mGroup);
    if (!mIconDir.exists()) {
      mIconDir.mkdirs();
    }
    mIconIndexFile = new File(mIconDir, "index.txt");
    mProperties = new Properties();
    if (mIconIndexFile.exists()) {
      mProperties.load(new BufferedInputStream(new FileInputStream(
              mIconIndexFile), 0x1000));
    } else {
      mLog.severe("index.txt not found in: " + mIconIndexFile.toString());
      // System.exit(-1);
    }
  }

  public Icon getIcon(String channelId, String url) throws IOException {
    String key = new StringBuffer("icons_").append(mGroup).append("_")
            .append(channelId).toString();
    String prevUrl = (String) mProperties.get(key);
    Icon icon = null;
    File iconFile = new File(mIconDir, channelId);

    if (url.equals(prevUrl)) {
      // the url hasn't changed; we should have the icon locally
      mLog.info("Found iconUrl in cache for channelId:" + channelId);
      icon = getIconFromFile(iconFile);
      return icon;
    } else {
      mLog.warning("iconUrl is not in cache for channelId " + channelId
              + ". prevUrl=" + prevUrl + ". currentUrl=" + url);
    }

    if (mDataHydraTvDataService.hasRightToDownloadIcons()) {
      if (mDataHydraTvDataService.getIconCache().containsKey(url)) {
        try {
          if (!mDataHydraTvDataService.getIconCache().get(url).equals(iconFile)) {
            copyFile(mDataHydraTvDataService.getIconCache().get(url), iconFile);
            icon = getIconFromFile(iconFile);
          }
        } catch (Exception e) {
          mLog.log(Level.SEVERE, "Problem while copying File from Cache", e);
        }

      }
    }

    if (icon == null) {
      // download the icon
      try {
        util.io.IOUtilities.download(new URL(url), iconFile);
        icon = getIconFromFile(iconFile);
        mDataHydraTvDataService.getIconCache().put(url, iconFile);
      } catch (IOException e) {
        mLog.warning("channel " + channelId
                + ": could not download icon from " + url);
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
   *
   * @param source Source File
   * @param dest   Destination File
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
      mLog.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private Icon getIconFromFile(File file) {
    Image img = ImageUtilities.createImage(file.getAbsolutePath());
    if (img != null) {
      return new ImageIcon(img);
    }
    return null;
  }

  public void close() throws IOException {
    mProperties.store(new FileOutputStream(mIconIndexFile), null);
  }
}
