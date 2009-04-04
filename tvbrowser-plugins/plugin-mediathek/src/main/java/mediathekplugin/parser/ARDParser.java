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

import mediathekplugin.MediathekPlugin;
import mediathekplugin.MediathekProgram;
import mediathekplugin.MediathekProgramItem;
import devplugin.Channel;

public final class ARDParser extends AbstractParser {

  private static final String SITE_URL = "http://www.ardmediathek.de";
  private static final String CONTENT_URL = SITE_URL + "/ard/servlet/content/";
  /**
   * ARD und alle Regionalsender, ZDF (gemeinsames Vormittagsprogram), Phoenix,
   * Kika und 3sat in Gemeinschaftsproduktion
   */
  private static final String[] SUPPORTED_CHANNELS = { "3sat", "das erste",
      "ard", "br", "dwtv", "einsextra", "einsfestival", "einsplus", "hr",
      "kika", "mdr", "ndr", "phoenix", "rb", "rbb", "swr", "wdr", "zdf" };

  /**
   * pattern for the (popup) link from the original program page
   */
  private static final Pattern POPUP_LINK_PATTERN = Pattern.compile(Pattern
      .quote("<a href=\"")
      + "([^\"]*)" + Pattern.quote("\" onclick=\"popupPodcast"));

  /**
   * pattern for the (textual) RSS link from the popup page
   */
  private static final Pattern RSS_LINK_PATTERN = Pattern.compile(Pattern
      .quote("<input name=\"\" type=\"text\" value=\"")
      + "([^\"]*)" + Pattern.quote("\""));

  /**
   * pattern to match for episodes on those pages which do not support RSS
   */
  private static final Pattern EPISODE_URL_PATTERN = Pattern.compile(Pattern
      .quote("<a href=\"/ard/servlet/content/")
      + "([^\"]+)"
      + Pattern.quote("\" class=\"highlight\""));
  
  private static final Pattern EPISODE_TITLE_PATTERN = Pattern.compile(Pattern
      .quote("\"beitragstitel\"><strong>")
      + "([^<]*)" + Pattern.quote("</strong>"));

  private enum TitleFix {
    ARD_RATGEBER("ARD-Ratgeber", "ARD Ratgeber: "), LANDESSCHAU("Landesschau",
        "Landesschau "), LAENDERSACHE("Ländersache", "LÄNDERSACHE "), SUEDWILD(
        "Südwild", "Südwild ");
    private static TitleFix currentFix;
    private String mSearch;
    private String mReplacement;

    TitleFix(final String search, final String replacement) {
      mSearch = search;
      mReplacement = replacement;
    }

    public static String fixTitle(String title) {
      title = title.trim();
      if (title.startsWith("- ") && currentFix != null) {
        return currentFix.mReplacement + title.substring(2).trim();
      }
      for (TitleFix fix : values()) {
        if (title.startsWith(fix.mSearch)) {
          currentFix = fix;
        }
      }
      final int index = title.indexOf('|');
      if (index > 0) {
        title = title.substring(0, index).trim();
      }
      return title;
    }
  };

  public void readContents() {
    // <option value="1082266"
    // title="ARD-Brennpunkt (Das Erste)">ARD-Brennpunkt</option>
    final Pattern pattern = Pattern.compile(Pattern.quote("option value=\"")
        + "([^\"]+)"
        + Pattern.quote("\" title=\"") + "([^\"]+)"
        + Pattern.quote("\">")
        + "([^<]+)"
        + Pattern.quote("</option>"));
    readContents(CONTENT_URL + "2570", pattern, "ARD");
  }

  protected String getTitleFromMatcher(final Matcher matcher) {
    return matcher.group(3);
  }

  public boolean isSupportedChannel(final Channel channel) {
    return isSupportedChannel(channel, SUPPORTED_CHANNELS);
  }

  public String fixTitle(final String title) {
    if (title.endsWith(")")) {
      return title.replace(" (BR)", "").replace(" (DW-TV)", "").replace(
          " (MDR)", "").replace(" (NDR)", "").replace(" (RB)", "").replace(
          " (SWR)", "").replace(" (WDR)", "").trim();
    }
    return title;
  }

  protected boolean addProgram(String title, final String relativeUrl) {
    title = TitleFix.fixTitle(title);
    if (relativeUrl.length() <= 1) {
      return false;
    }
    MediathekPlugin.getInstance().addProgram(this, title,
        "http://www.ardmediathek.de/ard/servlet/content/1214?moduleId="
            + relativeUrl);
    return true;
  }

  public boolean canReadEpisodes() {
    return true;
  }

  public void parseEpisodes(final MediathekProgram mediathekProgram) {
    String url = mediathekProgram.getUrl();
    // get page of program
    String content = readUrl(url);
    Matcher matcher = POPUP_LINK_PATTERN.matcher(content);
    if (matcher.find()) {
      // get rss description page
      url = matcher.group(1);
      content = readUrl(SITE_URL + url);
      matcher = RSS_LINK_PATTERN.matcher(content);
      // finally read the RSS itself
      if (matcher.find()) {
        readRSS(mediathekProgram, matcher.group(1));
      }
    } else {
      parseEpisodesWithoutRSS(mediathekProgram, content);
    }
  }

  private void parseEpisodesWithoutRSS(final MediathekProgram program,
      final String content) {
    final Matcher matcher = EPISODE_URL_PATTERN.matcher(content);
    int count = 0;
    while (matcher.find()) {
      String type = null;
      final String url = SITE_URL + "/" + matcher.group(1);
      final String nextPart = content.substring(matcher.end(1),
          matcher.end(1) + 1024 * 2);
      final Matcher titleMatcher = EPISODE_TITLE_PATTERN.matcher(nextPart);
      if (titleMatcher.find()) {
        final String title = MediathekPlugin.getInstance().convertHTML(
            titleMatcher.group(1));
        /*
         * int index = nextPart.indexOf(matcher.group(1) + "\" class=\""); if
         * (index > 0) { index += matcher.group(1).length() +
         * "\" class=\"".length(); final int stop = nextPart.indexOf('"', index
         * + 1); if (stop > 0) { type = nextPart.substring(index, stop); } }
         */
        program.addItem(new MediathekProgramItem(title, url, type));
        count++;
      }
    }
    logInfo("Read " + count + " episodes for " + program.getTitle());
    program.updatePluginTree(true);
  }

}
