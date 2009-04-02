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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.io.IniFileReader;
import util.ui.UiUtilities;
import devplugin.ThemeIcon;

/**
 * This class implements the IconTheme-Loading
 * 
 * Most of the Code is based on the FreeDesktop Specs
 * http://standards.freedesktop.org/icon-theme-spec/icon-theme-spec-latest.html
 */
abstract public class IconTheme {
  /** Base-Directory of the IconTheme */
  private File mIconBase;
  /** Logger */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(IconTheme.class.getName());
  /** Name and Comment */
  private String mThemeName, mThemeComment;
  /** Directory-Entries in the index.theme File*/
  private ArrayList<Directory> mDirectories;
  
  /**
   * Create the IconTheme
   * 
   * The Directory must contain a index.theme-File. 
   * For Details please look into the Specs.
   * 
   * @param iconDir Directory for this Theme
   */
  public IconTheme(File iconDir) {
    mIconBase = iconDir;
  }

  /**
   * Load the Theme-File
   * @return true if successfull
   */
  public boolean loadTheme() {
    return loadThemeFile();
  }

  /**
   * Load the .theme-File and parses it 
   * @return true if successfull
   */
  private boolean loadThemeFile() {
    try {
      mDirectories = new ArrayList<Directory>();
      
      if (!entryExists("index.theme")) {
        return false;
      }

      IniFileReader iniReader = new IniFileReader(getInputStream("index.theme"));

      HashMap<String, String> iconSection = iniReader.getSection("Icon Theme");
      
      mThemeName = iconSection.get("Name");
      mThemeComment = iconSection.get("Comment");

      String[] directories = (iconSection.get("Directories")).split(",");
      
      int len = directories.length;
      for (int i = 0;i<len;i++) {
        HashMap<String, String> dirMap = iniReader.getSection(directories[i]);
        
        String context = dirMap.get("Context"); 
        String type = dirMap.get("Type");
        int size = parseInt(dirMap.get("Size"));
        int maxsize = parseInt(dirMap.get("MaxSize"));
        int minsize = parseInt(dirMap.get("MinSize"));
        int threshold = parseInt(dirMap.get("Threshold"));
        
        Directory dir = new Directory(directories[i],context, type, size, maxsize, minsize, threshold);
        mDirectories.add(dir);
      }
      
      return true;
    } catch (Exception e) {
      mLog.log(Level.SEVERE, "Problems loading Icon theme", e);
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Parse an Integer
   * @param str String that contains an Integer
   * @return -1 if wrong, otherwise the Value of the String
   */
  private int parseInt(String str) {
    if (str == null) {
      return -1;
    }
    try {
      int num = Integer.parseInt(str);
      return num;
    } catch (Exception e) {
    }
    return -1;
  }
  
  /**
   * Get the Name of the Theme
   * @return Name
   */
  public String getName() {
    return mThemeName;
  }
  
  /**
   * Get the Comment of the Theme
   * @return Comment
   */
  public String getComment() {
    return mThemeComment;
  }
 
  /**
   * Get the Icon-Base. This can be a File or a Directory
   * @return Icon-Base
   */
  public File getBase() {
    return mIconBase;
  }
  
  /**
   * Get an Icon from this Theme.
   * This Method tries to find an exact size match, if it wasn't found
   * it tries to find another Version of the Icon and rescales it.
   * 
   * @param icon Icon that should be loaded
   * @return Icon or Null if Icon was not found
   */
  public ImageIcon getIcon(ThemeIcon icon) {
    // a) best match, find right context and right size
    Iterator<Directory> it = mDirectories.iterator();
    while (it.hasNext()) {
      Directory dir = it.next();
      if (dir.getName().toLowerCase().contains("/" + icon.getCategory().toLowerCase()) && sizeMatches(dir, icon.getSize())) {
        StringBuilder iconFile = new StringBuilder(dir.getName()).append("/")
            .append(icon.getName()).append(".png");
        if (entryExists(iconFile.toString())) {
          return getImageFromTheme(iconFile.toString());
        }
      }
    }
    
    // b) find exact size matching Icon, independent of context
    it = mDirectories.iterator();
    while (it.hasNext()) {
      Directory dir = it.next();
      if (sizeMatches(dir, icon.getSize())) {
        StringBuilder iconFile = new StringBuilder(dir.getName()).append("/")
            .append(icon.getName()).append(".png");
        if (entryExists(iconFile.toString())) {
          return getImageFromTheme(iconFile.toString());
        }
      }
    }
    
    // c) find best fitting Icon
    int minSize = Integer.MAX_VALUE;
    String closestMatch = null;
    
    it = mDirectories.iterator();
    while (it.hasNext()) {
      Directory dir = it.next();
      int distance = sizeDistance(dir, icon.getSize()); 
      if (distance < minSize) {
        StringBuilder iconFile = new StringBuilder(dir.getName()).append("/")
            .append(icon.getName()).append(".png");
        if (entryExists(iconFile.toString())) {
          closestMatch = iconFile.toString();
          minSize = distance;
        }
      }
    }

    // Found closest match, resize it
    if (closestMatch != null) {
      Icon closestIcon = getImageFromTheme(closestMatch);
      return (ImageIcon) UiUtilities.scaleIcon(closestIcon, icon.getSize(), icon.getSize());
    }
    
    return null;
  }

  /**
   * Tests if the Size of the Directory matches 
   * @param dir Directory to test
   * @param size Size that is needed
   * @return true if Size matches
   */
  private boolean sizeMatches(Directory dir, int size) {
    if (dir.getType().equals("Fixed")) {
      return size == dir.getSize();
    }
    
    if (dir.getType().equals("Scaled")) {
      return (dir.getMinSize() <= size) && (size <= dir.getMaxSize());
    }
    
    if (dir.getType().equals("Threshold")) {
      return (dir.getSize() - dir.getThreshold() <= size) && (size <= dir.getSize() + dir.getThreshold()); 
    }
    
    return false;
  }
  
  /**
   * Returns the Distance between the size and the size of the Directory
   * @param dir Directory to test
   * @param size Size that is needed
   * @return Distance between needed Size and the Size of the Directory
   */
  private int sizeDistance(Directory dir, int size) {

    if (dir.getType().equals("Fixed")) {
      return Math.abs(dir.getSize()-size);
    }
    
    if (dir.getType().equals("Scaled")) {
      if (size < dir.getMinSize()) {
        return dir.getMinSize() - size;
      }
      if (size > dir.getMaxSize()) {
        return size - dir.getMaxSize();
      }
      return 0;
    }
    
    if (dir.getType().equals("Threshold")) {
      if (size < (dir.getSize() - dir.getThreshold())) {
        return dir.getMinSize() - size;
      }
      if (size > dir.getSize() + dir.getThreshold()) {
        return size - dir.getMaxSize();
      }
      return 0;
    }
    
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getBase().hashCode();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IconTheme other = (IconTheme) obj;
    if (getBase() == null) {
      if (other.getBase() != null) {
        return false;
      }
    } else if (!getBase().getAbsolutePath().equals(other.getBase().getAbsolutePath())) {
      return false;
    }
    return true;
  }

  /**
   * Get an InputStream from the Icon-Theme.
   * @param entry File/Entry to load
   * @return InputStream of specific Entry
   */
  protected abstract InputStream getInputStream(String entry);

  /**
   * Tests if an Entry exists in the Icon-Theme
   * @param entry check for this Entry
   * @return True, if the Entry exists
   */
  protected abstract boolean entryExists(String entry);

  /**
   * Get an Image from the Icon-Theme
   * @param image get this Image
   * @return Image
   */
  protected abstract ImageIcon getImageFromTheme(String image);
  
}