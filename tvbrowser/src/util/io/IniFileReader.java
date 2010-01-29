/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package util.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * This class reads Ini-Files.
 *
 * Ini-Files are Property-Files with [sections] and Key=Value Text
 *
 * Each [Section] is loaded into an unique HashMap.
 */
public class IniFileReader {
  /** HashMap for the Sections */
  private HashMap<String, HashMap<String, String>> mSections;

  /**
   * Loads the Ini-File
   *
   * @param iniFile File to load
   * @throws IOException
   */
  public IniFileReader(File iniFile) throws IOException{
    readIni(new BufferedInputStream(new FileInputStream(iniFile)));
  }

  /**
   * Loads the Ini from a InputStream
   *
   * @param stream Stream with Ini-File
   * @throws IOException
   */
  public IniFileReader(InputStream stream) throws IOException {
    readIni(stream);
  }

  /**
   * Reads the Ini from a Stream
   *
   * @param stream Stream with Ini-File
   * @throws IOException
   */
  private void readIni(InputStream stream) throws IOException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    String str;

    mSections = new HashMap<String, HashMap<String, String>>();

    HashMap<String, String> map = new HashMap<String, String>();

    Pattern section = Pattern.compile("^\\[.*\\]\\w*");
    Pattern keyvalue = Pattern.compile("^.*=.*\\w*");

    while ((str = reader.readLine()) != null) {

      if (section.matcher(str).matches()) {
        str = str.trim();
        str = str.substring(1, str.length()-1);

        map = new HashMap<String, String>();
        mSections.put(str, map);
      } else if (keyvalue.matcher(str).matches()){
        String key = StringUtils.substringBefore(str,"=");
        String value = StringUtils.substringAfter(str,"=");
        map.put(key, value);
      }
    }

  }

  /**
   * Get a specific "Section" of the Ini-File. Each Key=Value is stored
   * in the HashMap as Key = Value
   *
   * @param section Section to return
   * @return Specific Section as HashMap
   */
  public HashMap<String, String> getSection(String section) {
    return mSections.get(section);
  }

  /**
   * Gets all available Sections in the loaded Ini-File
   * @return
   */
  public String[] getAllSections() {
    ArrayList<String> list = new ArrayList<String>(mSections.keySet());
    return list.toArray(new String[list.size()]);
  }

}