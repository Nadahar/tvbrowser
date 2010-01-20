/*
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
 *
 * jbUtilities.java
 *
 * Created on 25. April 2007, 13:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package nextviewdataservice;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import util.misc.OperatingSystem;

/**
 * A collection of static methods collected by the author
 * to be used somewhere else in the plugin.
 */
public class jbUtilities {

  private static final Logger mLog = java.util.logging.Logger.getLogger(NextViewDataService.class.getName());

  /**
   * Creates a new instance of jbUtilities
   */
  public jbUtilities() {
  }

  /**
   * Starts an external process and waits until the process is finished.
   * @param cmdString ; an array containing the command itself and the command options
   * @param UserDir ; the working directory used by the external command,
   * @throws IOException
   */
  public static void runExtProcess(String[] cmdString, String UserDir) throws IOException {

    ProcessBuilder cmdPR = new ProcessBuilder(cmdString);
    cmdPR.directory(new File(UserDir));
    Process cmdP = cmdPR.start();

    try {
      cmdP.waitFor();
    } catch (InterruptedException e) {
    }

  }

  /**
   * Copy internal file from actual jar-file to the file system
   * @param clazz ; class of the calling object, 
   * @param fileName ; the inner path name of the file to be copied
   * @param destination ; the full pathname of the location to be copied to.
   */
  public static void getFileFromThisJar(Class<?> clazz, String fileName, String destination) {

    fileName = clazz.getPackage().toString().split(" ", 2)[1] + "/" + fileName;
    String jarFile = clazz.getResource("/" + fileName).toString();

    jarFile = jarFile.split("/", 2)[1];

    if (!OperatingSystem.isWindows()) {
      jarFile = "/" + jarFile;
    }

    jarFile = jarFile.split("!")[0];
    jarFile = jarFile.replace("%20", " ");

    try {
      getFileFromJar(new File(jarFile), fileName, destination);
    } catch (Exception e) {
      mLog.warning(e.toString());
    }
  }

  /**
   * Copies a file out of a jar file to the file system
   * @param theJarFile ; the jar file to be copied from
   * @param entryName ;, the full pathname to the file to be copied. (see getFileFromThisJar above for an example, how the pathname should be build!)
   * @param destFile ; the full pathname of the location to be copied to.
   * @throws Exception
   */
  public static void getFileFromJar(File theJarFile, String entryName,
      String destFile) throws Exception {


    JarFile jarFile = new JarFile(theJarFile);

    ZipEntry zipEntry = jarFile.getEntry(entryName);

    FileOutputStream destOutputStream = new FileOutputStream(destFile);

    destOutputStream.getChannel().transferFrom(
        Channels.newChannel(jarFile.getInputStream(zipEntry)), 0,
        zipEntry.getSize());

    destOutputStream.close();

    jarFile.close();
  }
  
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
