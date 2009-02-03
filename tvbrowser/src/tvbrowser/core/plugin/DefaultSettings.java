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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.io.stream.InputStreamProcessor;
import util.io.stream.StreamUtilities;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 07.03.2005
 */
public class DefaultSettings {

  private static final String FILENAME = System.getProperty("propertiesfile","default.properties");

  private Properties mProperties;

  public DefaultSettings() {
    mProperties = new Properties();
    File settingsFile = new File(FILENAME);
    if (settingsFile.canRead()) {
      StreamUtilities.inputStreamIgnoringExceptions(settingsFile,
          new InputStreamProcessor() {

            @Override
            public void process(InputStream input) throws IOException {
              mProperties.load(input);
            }
          });
    }
  }

  public String getProperty(String key, String defaultString) {
    String value = mProperties.getProperty(key, defaultString);
    if (value == null) {
      return null;
    }
    String pre = "\\$\\{";
    String post = "\\}";
    String regex = pre+"(.*?)"+post;
    Pattern pattern = Pattern.compile(regex);

    Matcher matcher = pattern.matcher(value);

    while (matcher.find()) {
      String sysKey = matcher.group(1);
      String p = pre + sysKey + post;
      String v = System.getProperty(sysKey,"UNKNOWN");

      // We habe to replace '\' and '$' signs before replacement
      // see: http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Matcher.html#replaceAll(java.lang.String)
      v = v.replaceAll("\\\\","/");
      v = v.replaceAll("\\$","**");
      value = value.replaceAll(p, v);
    }
    return value;
  }

}
