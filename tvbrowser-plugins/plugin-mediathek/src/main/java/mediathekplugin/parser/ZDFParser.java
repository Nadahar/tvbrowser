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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import mediathekplugin.MediathekPlugin;
import mediathekplugin.MediathekProgram;
import devplugin.Channel;

public class ZDFParser extends AbstractParser {

  private static final String MAIN_URL = "http://www.zdf.de/ZDFmediathek/content/";
  private static String[] SUPPORTED_CHANNELS = { "ard", "zdf", "3sat" };

  public void readContents() {
    Pattern pattern = Pattern.compile(Pattern
        .quote("<a href=\"/ZDFmediathek/content/")
        + "([^?]+)"
        + Pattern.quote("?reset=true\">")
        + "([^<]+)"
        + Pattern.quote("</a>"));
    readContents("http://www.zdf.de/ZDFmediathek/inhalt?inPopup=true", pattern,
        "ZDF");
  }

  public boolean canReadEpisodes() {
    return true;
  }
  
  public boolean isSupportedChannel(Channel channel) {
    return isSupportedChannel(channel, SUPPORTED_CHANNELS);
  } 

  protected void addProgram(String title, String relativeUrl) {
    MediathekPlugin.getInstance().addProgram(this, title,
        MAIN_URL + relativeUrl);
  }

  public String fixTitle(String title) {
    if (title.endsWith(")")) {
      return title.replace(" (3sat)", "").replace(" (tivi)", "").trim();
    }
    return title;
  }

  public void parseEpisodes(MediathekProgram mediathekProgram) {
    final String url = mediathekProgram.getUrl();
    int num = Integer.parseInt(url.substring(url.lastIndexOf("/") + 1));
    String rssUrl = "http://www.zdf.de/ZDFMediathek/content/"
        + Integer.toString(num) + "?view=rss";
    if (rssUrl != null) {
      readRSS(mediathekProgram, rssUrl);
    }
  }

  protected void readContents(String webPage, Pattern pattern, String name) {
    final MediathekPlugin plugin = MediathekPlugin.getInstance();
    String startPage = readUrl(webPage);
    Matcher matcher = pattern.matcher(startPage);
    int count = 0;
    int endPos = Integer.MAX_VALUE;
    while (matcher.find() && matcher.start() < endPos) {
      // dynamically find the end of the first column in the 3 column layout
      if (count == 0) {
        endPos = startPage.indexOf("</ul>", matcher.start());
      }
      String relativeUrl = matcher.group(1);
      String title = plugin.convertHTML(matcher.group(2));
      addProgram(title, relativeUrl);
      count++;
    }
    final String msg = "Read " + count + " programs from " + name
        + " Mediathek";
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        plugin.getLogger().info(msg);
      }
    });
  }

}
