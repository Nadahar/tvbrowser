/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.core.plugin;


import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 07.03.2005
 * Time: 18:15:49
 */
public class DefaultSettings {

  private static final String FILENAME = System.getProperty("propertiesfile","default.properties");

  private Properties mProperties;

  public DefaultSettings() {
    mProperties = new Properties();
    try {
      mProperties.load(new FileInputStream(new File(FILENAME)));
    } catch (IOException e) {
      //ignore
    }
  }

  public String getProperty(String key, String defaultString) {
    return mProperties.getProperty(key, defaultString);
  }

}
