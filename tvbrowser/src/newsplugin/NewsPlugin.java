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
  private static final Version mVersion = new Version(3,2,1);

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(NewsPlugin.class);

  /** The URL of the news skript. */
  private static final String NEWS_URL = "http://www.tvbrowser.org/newsplugin/newsplugin-get.php";

  /** The maximum age of news. Older news will be removed. */
  private static int MAX_NEWS_AGE = 90;

  /** The news. */
  private NewsList mNewsList;

  /** Instance of this Plugin */
  private static NewsPlugin mInstance;

  private boolean hasRightToDownload = false;
  private NewsDialog mNewsDialog;
    
  private long mNoConnectionTime = 0;
  private long mLastNewsFileModified = 0;

  private PluginInfo mPluginInfo;
  
  /**
   * Creates a new instance of NewsPlugin.
   */
  public NewsPlugin() {
    mNewsList = new NewsList();
    mInstance = this;
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance
   */
  public static NewsPlugin getInstance() {
    return mInstance;
  }

  public void handleTvBrowserStartFinished() {
    hasRightToDownload = true;
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        if(mNewsDialog == null || !mNewsDialog.isVisible()) {
          mNewsDialog = new NewsDialog(getParentFrame(), mNewsList.getList(), -1);
          mNewsDialog.show();
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

  public static Version getVersion() {
    return mVersion;
  }
  
  /**
   * Gets the plugin info.
   * 
   * @return The plugin info.
   */
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      String name = mLocalizer.msg("news", "News");
      String desc = mLocalizer.msg("description",
          "Gets the TV-Browser news after each TV data update.");
      String author = "Til Schneider, www.murfman.de";
      
      mPluginInfo = new PluginInfo(NewsPlugin.class, name, desc, author);
    }
    
    return mPluginInfo;
  }

  /**
   * Checks for new news.
   */
  public void handleTvDataUpdateFinished() {
    long currentTime = System.currentTimeMillis();
    if (hasRightToDownload && mNoConnectionTime < currentTime && getPluginManager().getTvBrowserSettings().getLastDownloadDate().equals(devplugin.Date.getCurrentDate())) {
      int serverWaitDays = -1;
      try {
        long lastNews = mNewsList.getLastNewsTime(mLastNewsFileModified);
        
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
            
            // Add the new news
            for (News element : newsArr) {
              if((element.getTime().getTime() - lastNews) > 5000) {
                mNewsList.add(element);
                addCount++;
              }
            }
            
            if(addCount > 0) {
              // Show the dialog
              final int newNewsCount = addCount;
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  NewsDialog dlg = new NewsDialog(getParentFrame(), mNewsList.getList(),
                      newNewsCount);
                  dlg.show();
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
      long serverNoConnectionTime = ((long) serverWaitDays * 24 * 60 * 60 * 1000)
          + currentTime;
            
      mNoConnectionTime = Math.max(randomNoConnectionTime, serverNoConnectionTime);
    }
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
        int startPos = news.indexOf('"', index1) + 1;
        int endPos = news.indexOf('"', startPos);
      
        return Integer.parseInt(news.substring(startPos,endPos));
      }
      else if(index2 != -1) {
        int startPos = news.indexOf('"', index1) + 1;
        int endPos = news.indexOf('"', startPos);
        
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
    String regex =
            "<news date=\"([^\"]*)\" author=\"([^\"]*)\">"
            + "\\s*<title>([^<]*)</title>" + "\\s*<text>([^<]*)</text>";

    if(news.indexOf("<title-en>") != -1) {
      regex += "\\s*<title-en>([^<]*)</title-en>" + "\\s*<text-en>([^<]*)</text-en>";
    }

    Matcher matcher = Pattern.compile(regex).matcher(news);

    // Extract the news
    ArrayList<News> list = new ArrayList<News>();
    int lastPos = 0;
    while (matcher.find(lastPos)) {
      String dateAsString = matcher.group(1);
      String author = matcher.group(2);
      String title = matcher.group(3);
      String text = matcher.group(4);
      String engTitle = null;
      String engText = null;
      
      if(matcher.groupCount() > 4) {
        engTitle = matcher.group(5);
        engText = matcher.group(6);
      }
      
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
      
      list.add(new News(date, author, title, text, engTitle, engText));

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
    
    out.writeInt(mNewsList.getList().size());
    for (int i = 0; i < mNewsList.getList().size(); i++) {
      News news = mNewsList.getList().get(i);
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
    mNewsList.getList().clear();
    mNewsList.getList().ensureCapacity(size);
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
  
  /**
   * Saves the settings of the NewsPlugin.
   * <p>
   * @return If the settings could be saved.
   */
  public boolean saveMeInternal() {
    return super.saveMe();
  }
}
