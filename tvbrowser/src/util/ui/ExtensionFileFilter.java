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

import java.io.*;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter implements FilenameFilter {

  String[] mExtenstionList;
  String mName;



  public ExtensionFileFilter(String extension, String name) {
    this(new String[] { extension }, name);
  }



  public ExtensionFileFilter(String[] extenstionList, String name) {
    super();

    // Make all extensions lowercase
    for (int i = 0; i < extenstionList.length; i++) {
      extenstionList[i] = extenstionList[i].toLowerCase();
    }
    
    mExtenstionList = extenstionList;
    mName = name;
  }



  public String toString() {
    return mName;
  }


  // extends FileFilter


  public boolean accept(File f) {
    if (f.isDirectory()) return true;

    // Get the lowercase name
    String name = f.getName().toLowerCase();

    // check whether the name ends with one of the extensions
    for (int i = 0; i < mExtenstionList.length; i++) {
      if (name.endsWith(mExtenstionList[i])) {
        return true;
      }
    }

    return false;
  }



  public String getDescription() {
    return mName;
  }


  // implements FilenameFilter


  public boolean accept(File dir, String mName) {
    return accept(new File(dir.getPath() + File.separator + mName));
  }

}