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
import java.util.Date;

/**
 * One news.
 *
 * @author Til Schneider, www.murfman.de
 */
public class News implements Comparable<News> {

  /** The timestamp of the news */
  private Date mTime;

  /** The author */
  private String mAuthor;

  /** The title */
  private String mTitle;

  /** The text */
  private String mText;
  
  
  /**
   * Creates a new instance of News.
   * 
   * @param time The timestamp of the news
   * @param author The author
   * @param title The title
   * @param text The text
   */
  public News(Date time, String author, String title, String text) {
    mTime = time;
    mAuthor = author;
    mTitle = title;
    mText = text;
  }


  /**
   * Gets the timestamp of the news.
   * 
   * @return The timestamp of the news.
   */
  public Date getTime() {
    return mTime;
  }
  
  
  /**
   * Gets the author.
   * 
   * @return The author.
   */
  public String getAuthor() {
    return mAuthor;
  }

  
  /**
   * Gets the title.
   * 
   * @return The title.
   */
  public String getTitle() {
    return mTitle;
  }
  
  
  /**
   * Gets the text.
   * 
   * @return The text.
   */
  public String getText() {
    return mText;
  }

  
  /**
   * Saves the news.
   * 
   * @param out The stream to save to
   * @throws IOException When saving failed.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeObject(mTime);
    out.writeObject(mAuthor);
    out.writeObject(mTitle);
    out.writeObject(mText);
  }

  
  /**
   * Loads the news.
   * 
   * @param in The stream to read from.
   * @param version The file version
   * @return The read news.
   * @throws IOException If reading failed
   * @throws ClassNotFoundException If the given stream is in a wrong format.
   */
  public static News readData(ObjectInputStream in, int version)
    throws IOException, ClassNotFoundException
  {
    Date time = (Date) in.readObject();
    String author = (String) in.readObject();
    String title = (String) in.readObject();
    String text = (String) in.readObject();
    
    return new News(time, author, title, text);
  }

  
  public int compareTo(News other) {
    return mTime.compareTo(other.mTime);
  }
}
