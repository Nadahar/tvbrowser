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
package newsplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;

/**
 * A plugin that gets after each TV data update news from the TV-Browser website
 * and shows them to the user.
 *
 * @author Til Schneider, www.murfman.de
 */
public class NewsPlugin extends Plugin {

  /** The localizer used by this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(NewsPlugin.class);
  
  /** The URL of the news skript. */
  private static final String NEWS_URL
    = "http://murf.mine.nu/tvbrowser-news/newsplugin-get.php";
  
  /** When we have no news get the news of the last week. */
  private static int FIRST_NEWS_DAYS = 7;
  
  /** The maximum age of news. Older news will be removed. */
  private static int MAX_NEWS_AGE = 90;
  
  /** The news. */
  private ArrayList mNewsList;
  
  
  /**
   * Creates a new instance of NewsPlugin.
   */
  public NewsPlugin() {
    mNewsList = new ArrayList();
  }
  

  /**
   * Gets the mark icon.
   * 
   * @return The mark icon.
   */
  public String getMarkIconName() {
    return "newsplugin/Information16.gif";
  }


  /**
   * Gets the button text.
   * 
   * @return The button text.
   */
  public String getButtonText() {
    return mLocalizer.msg("news", "News");
  }
  

  /**
   * Gets the button icon.
   * 
   * @return The button icon.
   */
  public String getButtonIconName() {
    return "newsplugin/Information16.gif";
  }

  
  /**
   * Opens the news dialog.
   */
  public void execute() {
    NewsDialog dlg = new NewsDialog(getParentFrame(), mNewsList, -1);
    dlg.centerAndShow();
  }
  
  
  /**
   * Gets the plugin info.
   * 
   * @return The plugin info.
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "news" ,"News" );
    String desc = mLocalizer.msg( "description" ,"Gets the TV-Browser news after each TV data update." );
    String author = "Til Schneider, www.murfman.de";
    
    return new PluginInfo(name, desc, author, new Version(1, 0));
  }
  

  /**
   * Checks for new news.
   */
  public void handleTvDataChanged() {
    try {
      Date lastNews;
      if (mNewsList.isEmpty()) {
        // We have no news
        lastNews = new Date(System.currentTimeMillis() - FIRST_NEWS_DAYS * 24 * 60 * 60 * 1000);
      } else {
        News last = (News) mNewsList.get(mNewsList.size() - 1);
        lastNews = last.getTime();
      }
      Calendar cal = Calendar.getInstance();
      cal.setTime(lastNews);
      
      // Create a String out of the date. Format, e.g.: "2004-09-17%2000:00"
      String asString = fill(cal.get(Calendar.YEAR), 4) + "-"
        + fill(cal.get(Calendar.MONTH) + 1, 2) + "-"
        + fill(cal.get(Calendar.DAY_OF_MONTH), 2) + "%20"
        + fill(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
        + fill(cal.get(Calendar.MINUTE), 2) + ":"
        + fill(cal.get(Calendar.SECOND), 2);
      
      URL url = new URL(NEWS_URL + "?lastNews=" + asString);
      System.out.println("url: " + url);
      byte[] newsData = IOUtilities.loadFileFromHttpServer(url);
      String news = new String(newsData, "ISO-8859-1");
      if (news.startsWith("<?xml version=\"1.0\" ")) {
        // There are new news
        final News[] newsArr = parseNews(news);
        
        // Add the new news
        for (int i = 0; i < newsArr.length; i++) {
          mNewsList.add(newsArr[i]);
        }
        
        // Show the dialog
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            NewsDialog dlg = new NewsDialog(getParentFrame(), mNewsList, newsArr.length);
            dlg.centerAndShow();
          }
        });
      }
      else if (news.equals("No news available")) {
        // There are no new news
      }
      else {
        // There was an error
        throw new IOException("News script returned error: " + news);
      }
    }
    catch (Exception exc) {
      String msg = mLocalizer.msg("error.1", "Getting news failed.");
      ErrorHandler.handle(msg, exc);
    }
  }
  
  
  /**
   * Fills zeros to a number until it has a certain length
   * 
   * @param number The number to fill with zeros
   * @param charCount The number of chars the number string should have
   * @return A number String filled with zeros if nessesary.
   */
  private String fill(int number, int charCount) {
    String str = Integer.toString(number);
    while (str.length() < charCount) {
      str = "0" + str;
    }
    return str;
  }  

  
  private News[] parseNews(String news) {
    // Create a regex that extracts news
    String regex = "<news date=\"([^\"]*)\" author=\"([^\"]*)\">"
      + "\\s*<title>([^<]*)</title>"
      + "\\s*<text>([^<]*)</text>";
    Matcher matcher = Pattern.compile(regex).matcher(news);
    
    // Extract the news
    ArrayList list = new ArrayList();
    int lastPos = 0;
    while (matcher.find(lastPos)) {
      String dateAsString = matcher.group(1);
      String author = matcher.group(2);
      String title = matcher.group(3);
      String text = matcher.group(4);
      
      // Convert the date to a real date. E.g.: "2004-09-17 15:38:31"
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR,  Integer.parseInt(dateAsString.substring(0, 4)));
      cal.set(Calendar.MONTH, Integer.parseInt(dateAsString.substring(5, 7)) - 1);
      cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateAsString.substring(8, 10)));
      cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateAsString.substring(11, 13)));
      cal.set(Calendar.MINUTE, Integer.parseInt(dateAsString.substring(14, 16)));
      cal.set(Calendar.SECOND, Integer.parseInt(dateAsString.substring(17, 19)));
      Date date = cal.getTime();
      
      list.add(new News(date, author, title, text));
      
      lastPos = matcher.end();
    }
    
    // Convert the list to an array
    News[] newsArr = new News[list.size()];
    list.toArray(newsArr);
    
    return newsArr;
  }

  
  /**
   * Saves the news.
   * 
   * @param out The stream to save to
   * @throws IOException When saving failed.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    out.writeInt(mNewsList.size());
    for (int i = 0; i < mNewsList.size(); i++) {
      News news = (News) mNewsList.get(i);
      news.writeData(out);
    }
  }

  
  /**
   * Loads the news.
   * 
   * @param in The stream to read from.
   * @throws IOException If reading failed
   * @throws ClassNotFoundException If the given stream is in a wrong format.
   */
  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    int size = in.readInt();
    mNewsList.clear();
    mNewsList.ensureCapacity(size);
    Date deadline = new Date(System.currentTimeMillis() - MAX_NEWS_AGE * 24 * 60 * 60 * 1000);
    for (int i = 0; i < size; i++) {
      News news = News.readData(in, version);
      
      // Don't add the old news
      if (deadline.before(news.getTime())) {
        mNewsList.add(news);
      }
    }
  }
  
}
