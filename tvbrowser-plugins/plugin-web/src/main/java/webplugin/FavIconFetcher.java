/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2005-12-19 22:18:29 +0100 (Mo, 19 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1745 $
 */
package webplugin;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import util.io.IOUtilities;
import util.ui.UiUtilities;

import com.ctreber.aclib.image.ico.BitmapDescriptor;
import com.ctreber.aclib.image.ico.ICOFile;

import devplugin.Plugin;

/**
 * This Class tries to get the Favicon for a Website
 * 
 * @author bodum
 */
public class FavIconFetcher {

  /**
   * Try to load an FavIcon and store the Icon in the Setting of the User.
   * 
   * @param urlString
   *          Load FavIcon for this URL
   * @return Filename, or null if error
   */
  public String fetchFavIconForUrl(String urlString) {
    Logger logger = Logger.getRootLogger();
    if (logger != null) {
      logger.setLevel(Level.ERROR);
    }
    int pos = urlString.indexOf("//");
    int firstslash = urlString.indexOf("/", pos + 2);
    if (firstslash >= 0) {
      urlString = urlString.substring(0, firstslash);
    }

    int params = urlString.indexOf("?", pos + 2);
    if (params >= 0) {
      urlString = urlString.substring(0, params);
    }

    String fav = urlString + "/favicon.ico";
    URL url;
    try {
      url = new URL(fav);

      File temp = File.createTempFile("tvbrowser", "webplugin");
      temp.deleteOnExit();

      try {
        IOUtilities.download(url, temp);
      } catch (IOException e) {
        // could not download, ignore this error
        return null;
      }

      Image img = null;
      try {
        ICOFile ico = new ICOFile(new FileInputStream(temp));
        img = getBestIcon(ico, 16, 16);
      } catch (Exception e) {
        // Couldn't parse Icon-File. Maybe it's an Image
        img = ImageIO.read(temp);
      }

      if (img != null) {
        StringBuilder filename = new StringBuilder(Plugin.getPluginManager()
            .getTvBrowserSettings().getTvBrowserUserHome());
        filename.append(File.separator).append("WebFavIcons").append(
            File.separator).append(urlString.substring(pos + 2)).append(".png");

        File file = new File(filename.toString());

        if (!file.getParentFile().exists()) {
          try {
            file.getParentFile().mkdirs();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if ((img.getWidth(null) != 16) || (img.getHeight(null) != 16)) {
          img = ((ImageIcon) UiUtilities.scaleIcon(new ImageIcon(img), 16, 16))
              .getImage();
        }

        ImageIO.write(renderImage(img), "png", file);
        try {
          temp.delete();
        } catch (Exception e) {
          e.printStackTrace();
        }

        return file.getName();
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Find the best Icon
   * 
   * @param ico
   *          Icon
   * @param x
   *          Width
   * @param y
   *          Height
   * @return Icon that fits best
   */
  private Image getBestIcon(ICOFile ico, int x, int y) {
    BitmapDescriptor entry = null;

    int curwidth = Integer.MAX_VALUE;
    int curheight = Integer.MAX_VALUE;
    int curdepth = 0;

    List<BitmapDescriptor> descriptors = ico.getDescriptors();
    for (BitmapDescriptor desc : descriptors) {
      if (Math.abs(x - curwidth) >= Math.abs(x - desc.getWidth())
          && Math.abs(y - curheight) >= Math.abs(y - desc.getHeight())
          && desc.getBPP() > curdepth) {
        entry = desc;
      }

    }

    return entry.getImageRGB();
  }

  /**
   * @param img
   *          Create RenderImage for this Image
   * @return BufferedImage
   */
  public RenderedImage renderImage(Image img) {
    int width = img.getWidth(null);
    int height = img.getHeight(null);

    // Create a buffered image in which to draw
    BufferedImage bufferedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);

    // Create a graphics contents on the buffered image
    Graphics2D g2d = bufferedImage.createGraphics();

    g2d.drawImage(img, 0, 0, null);

    // Graphics context no longer needed so dispose it
    g2d.dispose();

    return bufferedImage;
  }

}