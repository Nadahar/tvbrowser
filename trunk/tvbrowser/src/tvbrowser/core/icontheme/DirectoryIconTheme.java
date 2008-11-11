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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import util.ui.ImageUtilities;

/**
 * This Class implements the IconTheme for a Directory.
 * @author bodum
 */
public class DirectoryIconTheme extends IconTheme {

  /**
   * Create the Directory Icon Theme
   * 
   * @param iconDir Directory with Theme
   */
  public DirectoryIconTheme(File iconDir) {
    super(iconDir);
  }
  
  /**
   * Get an InputStream from the Icon-Theme.
   * @param entry File/Entry to load
   * @return InputStream of specific Entry
   */
  protected InputStream getInputStream(String entry) {
    try {
      return new BufferedInputStream(new FileInputStream(new File(getBase(), entry)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Tests if an Entry exists in the Icon-Theme
   * @param entry check for this Entry
   * @return True, if the Entry exists
   */
  protected boolean entryExists(String entry) {
    return new File(getBase(), entry).exists();
  }

  /**
   * Get an Image from the Icon-Theme
   * @param image get this Image
   * @return Image
   */
  protected ImageIcon getImageFromTheme(String image) {
    return new ImageIcon(ImageUtilities.createImage(new File(getBase(), image).getAbsolutePath()));
  }

}