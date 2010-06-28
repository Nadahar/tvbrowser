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

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter that allows to specify a set of extension for determining,
 * whether a file should be shown.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ExtensionFileFilter extends FileFilter implements FilenameFilter {

  /** The file extensions this filter should let pass. */
  String[] mExtenstionList;
  /** The localized name for this filter. */
  String mName;



  /**
   * Creates a new instance of ExtensionFileFilter.
   *
   * @param extension The file extension this filter should let pass.
   * @param name The localized name for this filter.
   */
  public ExtensionFileFilter(String extension, String name) {
    this(new String[] { extension }, name);
  }



  /**
   * Creates a new instance of ExtensionFileFilter.
   *
   * @param extenstionList The file extensions this filter should let pass.
   * @param name The localized name for this filter.
   */
  public ExtensionFileFilter(String[] extenstionList, String name) {
    super();

    // Make all extensions lowercase
    for (int i = 0; i < extenstionList.length; i++) {
      extenstionList[i] = extenstionList[i].toLowerCase();
    }
    
    mExtenstionList = extenstionList;
    mName = name;
  }



  /**
   * Gets the localized name of this filter.
   *
   * @return the localized name of this filter.
   */
  public String toString() {
    return mName;
  }


  // extends FileFilter


  /**
   * Returns whether this given File is accepted by this filter.
   *
   * @param file The file to check.
   * @return whether the specified file is accepted.
   */
  public boolean accept(File file) {
    if (file.isDirectory()) {
      return true;
    }

    // Get the lowercase name
    String name = file.getName().toLowerCase();

    // check whether the name ends with one of the extensions
    for (String element : mExtenstionList) {
      if (name.endsWith(element)) {
        return true;
      }
    }

    return false;
  }



  /**
   * Gets the localized name of this filter.
   *
   * @return the localized name of this filter.
   */
  public String getDescription() {
    return mName;
  }


  // implements FilenameFilter


  /**
   * Returns whether this given File is accepted by this filter.
   *
   * @param dir The directory of the file to check.
   * @param name The name of the file to check.
   * @return whether the specified file is accepted.
   */
  public boolean accept(File dir, String name) {
    return accept(new File(dir.getPath() + File.separator + name));
  }

}