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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import mediathekplugin.MediathekPlugin;
import mediathekplugin.MediathekProgram;
import mediathekplugin.MediathekProgramItem;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

import devplugin.Channel;

public abstract class AbstractParser implements IParser {
  
  protected void addProgram(String title, String relativeUrl) {
    MediathekPlugin.getInstance().addProgram(this, title, relativeUrl);
  }

  public String readUrl(String urlString) {
    try {
      URL url = new URL(urlString);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url
          .openStream(), "utf-8"));
      String inputLine;
      StringBuffer buffer = new StringBuffer();
      while ((inputLine = reader.readLine()) != null) {
        buffer.append(inputLine);
      }
      reader.close();
      return buffer.toString();
    } catch (MalformedURLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

  protected void readContents(String webPage, Pattern pattern, String name) {
    final MediathekPlugin plugin = MediathekPlugin.getInstance();
    String startPage = readUrl(webPage);
    Matcher matcher = pattern.matcher(startPage);
    int count = 0;
    while (matcher.find()) {
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

  public boolean canReadEpisodes() {
    return false;
  }

  public void parseEpisodes(MediathekProgram mediathekProgram) {
    // do not parse anything
  }
  
  protected boolean isSupportedChannel(Channel channel,
      String[] supportedChannels) {
    final String name = channel.getName().toLowerCase();
    final String id = channel.getId().toLowerCase();
    for (String supported : supportedChannels) {
      if (name.contains(supported) || id.contains(supported)) {
        return true;
      }
    }
    return false;
  }

  public String fixTitle(String title) {
    return title;
  }

  protected void logInfo(String string) {
    MediathekPlugin.getInstance().getLogger().info(string);
  }
  
  protected void readRSS(MediathekProgram program, String rssUrl) {
    FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
    feedFetcher.setUserAgent("TV-Browser Mediathek Plugin "
        + MediathekPlugin.getVersion().toString());
    try {
      int count = 0;
      SyndFeed feed = feedFetcher.retrieveFeed(new URL(rssUrl));
      Iterator<?> iterator = feed.getEntries().iterator();
      while (iterator.hasNext()) {
        SyndEntry entry = (SyndEntry) iterator.next();
        final String link = entry.getLink();
        if (link != null) {
          program.addItem(new MediathekProgramItem(entry.getTitle(), link));
          count++;
        }
      }
      logInfo("Read " + count + " episodes for " + program.getTitle());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FeedException e) {
      e.printStackTrace();
    } catch (FetcherException e) {
      e.printStackTrace();
    }
    program.updatePluginTree(true);
  }
}
