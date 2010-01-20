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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package util.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvbrowser.core.Settings;

/**
 * This is a special Classloader for the Translation-Files.
 * 
 * It checks for Resources in 3 Locations:
 * 
 *  1. HOME-Dir/lang/
 *  2. TV-Browser-Dir/lang/
 *  3. Jar of the Class
 * 
 * @author bodum
 * @since 2.5
 */
public class LocalizerClassloader extends ClassLoader {
  private static final Logger mLog = Logger.getLogger(LocalizerClassloader.class.getName());
  /**
   * Create Localizer-Classloader
   * 
   * @param parent Fallback-Classloader
   */
  public LocalizerClassloader(ClassLoader parent) {
    super(parent);
  }
 
  @Override
  public InputStream getResourceAsStream(String name) {
    try {
      // Check User-Home
      File file = new File(Settings.getUserSettingsDirName() + "/lang/" +name);
    
      if (file.exists()) {
        try {
          return new FileInputStream(file);
        } catch (FileNotFoundException e) {
          mLog.log(Level.WARNING, "Could not open language properties found in user settings directory.", e);
        }
      }
    
      // Check TV-Browser Location
      file = new File("lang/" + name);
    
      if (file.exists()) {
        try {
          return new FileInputStream(file);
        } catch (FileNotFoundException e) {
          mLog.log(Level.WARNING, "Could not open language properties found in program directory.", e);
        }
      }
    }catch(Throwable e) {
      mLog.log(Level.SEVERE, "Could not load user defined language properties, using default instead.", e);
    }
    
    // Check Jar
    return getParent().getResourceAsStream(name);
  }
}