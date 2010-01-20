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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import util.io.IOUtilities;

/**
 * Provides utilities for images.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ImageUtilities {
  
  private static final Logger mLog
    = Logger.getLogger(ImageUtilities.class.getName());

  /** The helper label. */  
  private static final JLabel HELPER_LABEL = new JLabel();

  /**
   * Returns the image in the specified file.
   * <p>
   * If the file does not exist null is returned.
   */
  public static Image createImage(String fileName, boolean waitUntilLoaded) {
    if (! new File(fileName).exists()) {
      mLog.warning("File does not exist: '" + fileName + "'");
      return null;
    }
    Image img = Toolkit.getDefaultToolkit().createImage(fileName);
    if (waitUntilLoaded) {
      waitForImageData(img, null);
    }
    
    return img;
  }
  
  /**
   * Returns the image in the specified file.
   * <p>
   * If the file does not exist <code>null</code> is returned.
   * </p>
   * <p>Do NOT use this method for images used with ImageIcons! Always use {@link #createImageAsynchronous(String)} with ImageIcons!</p>
   */
  public static Image createImage(String fileName) {
    return createImage(fileName, true);
  }

  public static Image createImageAsynchronous(String fileName) {
    return createImage(fileName, false);
  }
  
  /**
   * Lädt ein ImageIcon aus einem Jar-File und gibt es zurück.
   * <P>
   * Ist kein ImageIcon mit diesem Namen im Jar-File, so wird versucht, es vom
   * Dateisystem zu laden.
   * <P>
   * Wird die ImageIcon-Datei nicht gefunden, so wird <CODE>null</CODE> zurück gegeben.
   *
   * @param fileName Der Name der ImageIcon-Datei.
   * @param srcClass Eine Klasse, aus deren Jar-File das ImageIcon geladen werden soll.
   */
  public static ImageIcon createImageIconFromJar(String fileName, Class srcClass) {
    Image img = createImageFromJar(fileName, srcClass);
    
    if (img == null) {
      return null;
    } else {
      return new ImageIcon(img);
    }
  }
  
  
  
  /**
   * Lädt ein Image aus einem Jar-File und gibt es zurück.
   * <P>
   * Ist kein Image mit diesem Namen im Jar-File, so wird versucht, es vom
   * Dateisystem zu laden.
   * <P>
   * Wird die Image-Datei nicht gefunden, so wird <CODE>null</CODE> zurück gegeben.
   *
   * @param fileName Der Name der Image-Datei.
   * @param srcClass Eine Klasse, aus deren Jar-File das Image geladen werden soll.
   */
  public static Image createImageFromJar(String fileName, Class srcClass) {
    Image image = null;
    try {
      byte[] data = IOUtilities.loadFileFromJar(fileName, srcClass);
      image = Toolkit.getDefaultToolkit().createImage(data);
    }
    catch (Throwable thr) {
      String msg = "Loading '" + fileName + "' failed!";
      mLog.log(java.util.logging.Level.WARNING, msg, thr);
      return null;
    }
    
    if (image != null) {
      waitForImageData(image, null);
    }

    return image;
  }

  
  
  /**
   * Waits until all the data of an Image is present.
   * <p>
   * An Image is after construction only loaded when the data is used. The loading
   * occurs in an extra Thread and can take some time, so it might happen, that
   * an Image is not present after construction (it will be painted partly).
   * This method waits until the whole Image is loaded. The Component
   * <code>comp</code> is needed to monitor the preparing of the Image. You can
   * pass any Component (it will not be changed).
   * </p>
   * <p>You should not use this method when creating an ImageIcon. ImageIcons already use a MediaTracker internally.</p>
   *
   * @param image
   * @param comp
   */
  public static void waitForImageData(Image image, Component comp) {
    if (comp == null) {
      comp = HELPER_LABEL;
    }
    
    MediaTracker tracker = new MediaTracker(comp);
    tracker.addImage(image, 0);
    try {
      tracker.waitForID(0);
    }
    catch (Exception ex) {
    }
  } // waitForImageData (Image, Component)
  
  
}
