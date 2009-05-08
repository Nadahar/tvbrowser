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
package tvbrowser.core.icontheme;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.ImageIcon;

/**
 * This Class implements the IconTheme for a Zip-File.
 * 
 * @author bodum
 */
public class ZipIconTheme extends IconTheme {
  /** All Entries in the Zip-File */
  private HashMap<String, ZipEntry> mZipFileEntries;

  /**
   * Create the Zip Icon Theme
   * 
   * @param iconZip Zip with Theme
   */
  public ZipIconTheme(File iconZip) {
    super(iconZip);
    loadEntries();
  }

  /**
   * Load all Entries in the Zipfile
   */
  private void loadEntries() {
    mZipFileEntries = new HashMap<String, ZipEntry>();
    try {
      // Open the ZIP file
      JarFile zf = new JarFile(getBase());

      // Enumerate each entry
      for (Enumeration<JarEntry> entries = zf.entries(); entries.hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        mZipFileEntries.put(entry.getName(), entry);
      }
    } catch (IOException e) {
      // If something goes wrong, reset Theme
      mZipFileEntries = new HashMap<String, ZipEntry>();
    }

  }

  /**
   * Get an InputStream from the Icon-Theme.
   * 
   * @param string File/Entry to load
   * @return InputStream of specific Entry
   */
  protected InputStream getInputStream(String entry) {
    ZipEntry zipEntry = mZipFileEntries.get(entry);
    try {
      return new JarFile(getBase()).getInputStream(zipEntry);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Tests if an Entry exists in the Icon-Theme
   * 
   * @param entry check for this Entry
   * @return True, if the Entry exists
   */
  protected boolean entryExists(String entry) {
    return mZipFileEntries.containsKey(entry);
  }

  /**
   * Get an Image from the Icon-Theme
   * 
   * @param image get this Image
   * @return Image
   */
  protected ImageIcon getImageFromTheme(String image) {
    try {
      ZipEntry zipEntry = mZipFileEntries.get(image);
      InputStream in = new JarFile(getBase()).getInputStream(zipEntry);

      //  Create the byte array to hold the data
      byte[] bytes = new byte[(int)zipEntry.getSize()];
      //  Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
             && (numRead=in.read(bytes, offset, bytes.length-offset)) >= 0) {
          offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
          throw new IOException("Could not completely read file "+image);
      }
      
      return new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
    } catch (Exception e) {
    }
    return null;
  }

}