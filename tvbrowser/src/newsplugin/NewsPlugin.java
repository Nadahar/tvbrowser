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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import devplugin.ActionMenu;
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
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(NewsPlugin.class);

  /** The URL of the news skript. */
  private static final String NEWS_URL = "http://www.tvbrowser.org/newsplugin/newsplugin-get.php";

  /** When we have no news get the news of the last week. */
  private static int FIRST_NEWS_DAYS = 7;

  /** The maximum age of news. Older news will be removed. */
  private static int MAX_NEWS_AGE = 90;

  /** The news. */
  private ArrayList<News> mNewsList;

  /** Instance of this Plugin */
  private static NewsPlugin mInstance;

  private boolean hasRightToDownload = false;
  private NewsDialog mNewsDialog;
    
  private long mNoConnectionTime = 0;
  private long mLastNewsFileModified = 0;

  /**
   * Creates a new instance of NewsPlugin.
   */
  public NewsPlugin() {
    mNewsList = new ArrayList<News>();
    mInstance = this;
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance
   */
  public static Plugin getInstance() {
    return mInstance;
  }

  public void handleTvBrowserStartFinished() {
    hasRightToDownload = true;
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        if(mNewsDialog == null || !mNewsDialog.isVisible()) {
          mNewsDialog = new NewsDialog(getParentFrame(), mNewsList, -1);
          mNewsDialog.centerAndShow();
        }
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("news", "News"));
    action.putValue(Action.SMALL_ICON, createImageIcon("apps",
        "internet-news-reader", 16));
    action.putValue(BIG_ICON, createImageIcon("apps", "internet-news-reader",
        22));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }

  /**
   * Gets the plugin info.
   * 
   * @return The plugin info.
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("news", "News");
    String desc = mLocalizer.msg("description",
        "Gets the TV-Browser news after each TV data update.");
    String author = "Til Schneider, www.murfman.de";
    String helpUrl = mLocalizer.msg("helpUrl", "http://enwiki.tvbrowser.org/index.php/News");
    
    return new PluginInfo(name, desc, author, helpUrl, new Version(1, 6));
  }

  /**
   * Checks for new news.
   */
  public void handleTvDataUpdateFinished() {
    long currentTime = System.currentTimeMillis();
    if (hasRightToDownload && mNoConnectionTime < currentTime) {
      int serverWaitDays = -1;
      try {
        long lastNews;
        if (mNewsList.isEmpty()) {
          // We have no news
          lastNews = mLastNewsFileModified;
        } else {
          News last = mNewsList.get(mNewsList.size() - 1);
          lastNews = last.getTime().getTime();
        }

        URL url = new URL(NEWS_URL);
        URLConnection conn = url.openConnection();
        long lastModified = conn.getLastModified();
        
        if(lastModified > mLastNewsFileModified) {
          mLastNewsFileModified = lastModified;
          byte[] newsData = IOUtilities.loadFileFromHttpServer(url, 60000);
          
          String news = new String(newsData, "ISO-8859-1");
          if (news.startsWith("<?xml version=\"1.0\" ")) {
            // There are new news
            final News[] newsArr = parseNews(news);
            
            int addCount = 0;
            
            for (News newsItem : newsArr) {
              if((newsItem.getTime().getTime() - lastNews) > 5000) {
                mNewsList.add(newsItem);
                addCount++;
              }
            }
            
            if(addCount > 0) {
              // Show the dialog
              final int newNewsCount = addCount; 
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  NewsDialog dlg = new NewsDialog(getParentFrame(), mNewsList,
                      newNewsCount);
                  dlg.centerAndShow();
                }
              });
            }
            
            serverWaitDays = getServerNoConnectionDays(news);
          } else if (news.startsWith("No news available")) {
            serverWaitDays = getServerNoConnectionDays(news);
            // There are no new news
          } else {
            // There was an error
            throw new IOException("News script returned error: " + news);
          }
        }
        else {
          serverWaitDays = getServerNoConnectionDays("");
        }
      } catch (Exception exc) {
        String msg = mLocalizer.msg("error.1", "Getting news failed.");
        ErrorHandler.handle(msg, exc);
      }
      
      long randomNoConnectionTime = (long)((1.0 + Math.random() * 2) * 24 * 60 * 60 * 1000) + currentTime;
      long serverNoConnectionTime = (serverWaitDays * 24 * 60 * 60 * 1000) + currentTime;
            
      mNoConnectionTime = Math.max(randomNoConnectionTime, serverNoConnectionTime);
    }
  }

  /**
   * Fills zeros to a number until it has a certain length
   * 
   * @param number
   *          The number to fill with zeros
   * @param charCount
   *          The number of chars the number string should have
   * @return A number String filled with zeros if nessesary.
   */
  private String fill(int number, int charCount) {
    String str = Integer.toString(number);
    while (str.length() < charCount) {
      str = "0" + str;
    }
    return str;
  }
  
  /**
   * Gets the number of days the news shouldn't be received.
   * 
   * @param news The news text.
   * @return The days the news shouldn't be received.
   * @since 2.2.2
   */
  private int getServerNoConnectionDays(String news) {
    try {
      int index1 = news.indexOf("<noconnection days");
      int index2 = news.indexOf("<noconnection date");
    
      if(index1 != -1) {
        int startPos = news.indexOf("\"",index1) + 1;
        int endPos = news.indexOf("\"",startPos);
      
        return Integer.parseInt(news.substring(startPos,endPos));
      }
      else if(index2 != -1) {
        int startPos = news.indexOf("\"",index1) + 1;
        int endPos = news.indexOf("\"",startPos);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long value = format.parse(news.substring(startPos,endPos)).getTime();
        value -= System.currentTimeMillis();
        value /= (60000 * 60 * 24);
        
        return (int)value+1;
      }
    }catch(Exception e) {}
    
    return -1;
  }

  private News[] parseNews(String news) {
    // Create a regex that extracts news
    String regex = "<news date=\"([^\"]*)\" author=\"([^\"]*)\">"
        + "\\s*<title>([^<]*)</title>" + "\\s*<text>([^<]*)</text>";
    Matcher matcher = Pattern.compile(regex).matcher(news);

    // Extract the news
    ArrayList<News> list = new ArrayList<News>();
    int lastPos = 0;
    while (matcher.find(lastPos)) {
      String dateAsString = matcher.group(1);
      String author = matcher.group(2);
      String title = matcher.group(3);
      String text = matcher.group(4);
      
      // Convert the date to a real date. E.g.: "2004-09-17 15:38:31"
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, Integer.parseInt(dateAsString.substring(0, 4)));
      cal.set(Calendar.MONTH,
          Integer.parseInt(dateAsString.substring(5, 7)) - 1);
      cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateAsString.substring(8,
          10)));
      cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateAsString.substring(11,
          13)));
      cal
          .set(Calendar.MINUTE, Integer
              .parseInt(dateAsString.substring(14, 16)));
      cal
          .set(Calendar.SECOND, Integer
              .parseInt(dateAsString.substring(17, 19)));
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
   * @param out
   *          The stream to save to
   * @throws IOException
   *           When saving failed.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version
    
    out.writeInt(mNewsList.size());
    for (int i = 0; i < mNewsList.size(); i++) {
      News news = mNewsList.get(i);
      news.writeData(out);
    }
    
    out.writeLong(mNoConnectionTime);
    out.writeLong(mLastNewsFileModified);
  }

  /**
   * Loads the news.
   * 
   * @param in
   *          The stream to read from.
   * @throws IOException
   *           If reading failed
   * @throws ClassNotFoundException
   *           If the given stream is in a wrong format.
   */
  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int version = in.readInt();

    int size = in.readInt();
    mNewsList.clear();
    mNewsList.ensureCapacity(size);
    Date deadline = new Date(System.currentTimeMillis() - MAX_NEWS_AGE
        * 24L * 60L * 60L * 1000L);
    for (int i = 0; i < size; i++) {
      News news = News.readData(in, version);

      // Don't add the old news
      if (news.getTime().after(deadline)) {
        mNewsList.add(news);
      }
    }
    
    if(version >= 2) {
      mNoConnectionTime = in.readLong();
    }
    if(version >= 3) {
      mLastNewsFileModified = in.readLong();
    }
  }

}
