package sharedchanneldataservice;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Icon;


public class jbUtilities {

  private static final Logger mLog = java.util.logging.Logger.getLogger(jbUtilities.class.getName());

  public static void storeIcon (Icon icon, String type, String fileName){
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    if (h>0 && w>0) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      BufferedImage image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
      Graphics2D g = image.createGraphics();
      icon.paintIcon(null, g, 0, 0);
      icon.paintIcon(null, g, 0, 0);
      g.dispose();
      try {
        ImageIO.write(image, type, new File(fileName));
      } catch (IOException ioe) {
        mLog.severe(ioe.toString());
      }
    }
  }
}