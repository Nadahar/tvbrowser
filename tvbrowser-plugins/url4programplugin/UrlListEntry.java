/*
 * Copyright René Mach
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
 * 
 * SVN information:
 *       $Id:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package url4programplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A class that contrains a list with Urls
 * 
 * @author Ren� Mach
 */
public class UrlListEntry {
  private String mTitle;
  private boolean mIsShortLink;
  private String[] mUrlList;
  
  /**
   * Creates an instance of this class for the first entry of a program title.
   * 
   * @param title The title of the program.
   * @param entries The url list entries.
   * @param isShortLink If the short link should be shown.
   */
  public UrlListEntry(String title, String[] entries,boolean isShortLink) {
    mTitle = title;
    mUrlList = entries;
    mIsShortLink = isShortLink;
  }
  
  /**
   * Gets the title for this list entry
   * @return The title of the program entry.
   */
  public String getProgramTitle() {
    return mTitle;
  }
  
  /**
   * Gets if the program enty should be shown
   * with a short Link entry.
   * @return <code>True</code> if the link should be short.
   */
  public boolean isShortLinkEntry() {
    return mIsShortLink;
  }
  
  /**
   * Gets the stored urls for this program entry.
   * @return The urls for this program entry
   */
  public String[] getUrls() {
    return mUrlList;
  }
  
  /**
   * Sets the urls for this program entry.
   * 
   * @param urls The new urls for this program entry.
   */
  public void setUrls(String[] urls) {
    mUrlList = urls;
  }
  
  /**
   * Adds the given url to the list.
   * @param url The url to add.
   * @param isShortLink If the shown link should be short
   */
  public void addUrl(String url, boolean isShortLink) {
    mIsShortLink = isShortLink;
    String[] newList = new String[mUrlList.length+1];
    
    System.arraycopy(mUrlList,0,newList,0,mUrlList.length);
    
    newList[newList.length-1] = url;
    
    mUrlList = newList;
  }
  
  /**
   * Writes the values of this entry. 
   * @param out The stream to write to.
   * @throws IOException Thrown if something went wrong.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeUTF(mTitle);
    out.writeBoolean(isShortLinkEntry());
    
    out.writeInt(mUrlList.length);
    
    for(String url : mUrlList) {
      out.writeUTF(url);
    }
  }
  
  /**
   * Reads the values of this entry.
   * @param in The stream to read from.
   * @param version The file version.
   * @throws IOException Thrown if something went wrong.
   */
  public UrlListEntry(ObjectInputStream in, int version) throws IOException {
    mTitle = in.readUTF();
    mIsShortLink = in.readBoolean();
    
    mUrlList = new String[in.readInt()];
    
    for(int i = 0; i < mUrlList.length; i++) {
      mUrlList[i] = in.readUTF();
    }
  }
}
