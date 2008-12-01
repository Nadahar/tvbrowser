/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin.parser;

import java.util.regex.Pattern;

import mediathekplugin.MediathekPlugin;
import devplugin.Channel;

public class ARDParser extends AbstractParser {

  private static final String MAIN_URL = "http://www.ardmediathek.de/ard/servlet/content/";
  private static final String[] SUPPORTED_CHANNELS = { "das erste", "ard",
      "br", "dwtv", "einsextra", "einsfestival", "einsplus", "mdr", "ndr",
      "rb", "rbb", "swr", "wdr", "zdf" };

  public void readContents() {
    // <option value="http://www.ardmediathek.de/ard/servlet/content/967542">3
    // nach 9 (RB)</option>
    Pattern pattern = Pattern.compile(Pattern.quote("option value=\""
        + MAIN_URL)
            + "([^\"]+)"
            + Pattern.quote("\">")
            + "([^<]+)"
            + Pattern.quote("</option>"));
    readContents("http://www.ardmediathek.de/ard/servlet/content/2570",
        pattern, "ARD");
  }

  public boolean isSupportedChannel(Channel channel) {
    return isSupportedChannel(channel, SUPPORTED_CHANNELS);
  }

  public String fixTitle(String title) {
    if (title.endsWith(")")) {
      return title.replace(" (BR)", "").replace(" (DW-TV)", "").replace(
          " (MDR)", "").replace(" (NDR)", "").replace(" (RB)", "").replace(
          " (SWR)", "").replace(" (WDR)", "").trim();
    }
    return title;
  }

  protected void addProgram(String title, String relativeUrl) {
    MediathekPlugin.getInstance().addProgram(this, title,
        MAIN_URL + relativeUrl);
  }

}
