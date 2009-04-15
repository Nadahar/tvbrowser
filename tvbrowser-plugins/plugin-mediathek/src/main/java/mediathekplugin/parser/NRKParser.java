package mediathekplugin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mediathekplugin.MediathekPlugin;
import mediathekplugin.MediathekProgram;
import mediathekplugin.MediathekProgramItem;
import devplugin.Channel;

public class NRKParser extends AbstractParser {

  private static final String SITE_URL = "http://www1.nrk.no";
  private static final String MAIN_URL = "http://www1.nrk.no/nett-tv/";
  //private static final String CONTENT_URL = MAIN_URL + "bokstav/@";
  private static final String CONTENT_URL = MAIN_URL + "DynamiskLaster.aspx?LiveContent$letter:@";

  private static final String[] SUPPORTED_CHANNELS = { "nrk1", "nrk2", "nrk3", "nrk super" ,
    "nrk super tv", "nrk p1","nrk p2","nrk p3", "nrk alltid nyheter"};

  private static final Pattern EPISODE_PATTERN 
  = Pattern.compile("<li .*?<a href=\"([^\"]+?)\".*?class=\"(.*?)\">([^<]+?)</a>",Pattern.DOTALL);

  //private static final Pattern DATE_PATTERN = Pattern.compile("(\\d\\d)\\.(\\d\\d)\\.(?:20|)(\\d\\d)$");
  //private static final String[] MONTHS_NO = {"januar","februar","mars","april","mai",
  //  "juni","juli","august","september","oktober","november","desember"};
  
  public boolean canReadEpisodes() {
    return true;
  }

  public boolean isSupportedChannel(final Channel channel) {
    return isSupportedChannel(channel, SUPPORTED_CHANNELS);
  }

  protected boolean addProgram(final String title, final String relativeUrl) {
    MediathekPlugin.getInstance().addProgram(this, title,
        SITE_URL + relativeUrl);
    return true;
  }

  public String fixTitle(String title) {
    title = title.replace("Forbruker- inspektørene", "Forbrukerinspektørene");
    return title;
  }

  public void readContents() {

    final Pattern pattern = Pattern.compile("<h2>.*?<a href=\"([^\"]+?)\" .*?>([^<]+?)</a>",Pattern.DOTALL);
    logInfo("Read NRK: " + CONTENT_URL);

    readContents(CONTENT_URL, pattern, "NRK");
  }

  public void parseEpisodes(final MediathekProgram program) {
    String pageUrl = program.getUrl();
    // get page of program
    Pattern IdPattern = Pattern.compile("prosjekt/(\\d+)");
    Matcher IdMatcher = IdPattern.matcher(pageUrl);
    if (IdMatcher.find()) {
      String content = readUrl("http://www1.nrk.no/nett-tv/DynamiskLaster.aspx?ProjectList$project:"+IdMatcher.group(1));

      Matcher matcher = EPISODE_PATTERN.matcher(content);

      int count =0;
      while (matcher.find() && count<30) {
        String type = "video";
        final String url = SITE_URL + matcher.group(1);
        final String title = MediathekPlugin.getInstance().convertHTML(
            matcher.group(3));
        String icon = matcher.group(2);
        if (icon.equals("icon-folder-black")) {
          type = "link"; //folder
        } else if (icon.equals("icon-video-black")) {
          type = "video";
        } else if (icon.equals("icon-sound-black")) {
          type = "audio";
        }
        program.addItem(new MediathekProgramItem(title, url, type));
        count++;
      }
      logInfo("Read " + count + " episodes for " + program.getTitle());
      program.updatePluginTree(true);
    }
  }

}
