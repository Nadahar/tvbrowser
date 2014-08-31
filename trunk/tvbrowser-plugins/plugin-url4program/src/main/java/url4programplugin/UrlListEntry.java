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
 *       $Id: UrlListEntry.java 5832 2009-07-27 19:49:24Z ds10 $
 *     $Date: 2009-07-27 21:49:24 +0200 (Mo, 27 Jul 2009) $
 *   $Author: ds10 $
 * $Revision: 5832 $
 */
package url4programplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A class that contrains a list with Urls
 * 
 * @author René Mach
 */
public class UrlListEntry implements Comparable<UrlListEntry> {
  public static final int TITLE_TYPE = 0;
  public static final int REGEX_TYPE = 1;
  public static final int FILTER_TYPE = 2;
  
  private int mType;
  
  private String mTitle;
  private boolean mIsShortLink;
  private String[] mUrlList;
  private ProgramFilter mFilter;
  
  /**
   * Creates an instance of this class for the first entry of a program title.
   *
   * @param type The type of this entry.
   * @param title The title of the program.
   * @param entries The url list entries.
   * @param isShortLink If the short link should be shown.
   */
  public UrlListEntry(int type, String title, String[] entries, boolean isShortLink) {
    mType = type;
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
    
    out.writeInt(mType);
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
    
    if(version == 4) {
      if(in.readBoolean()) {
        mType = REGEX_TYPE;
      }
    }
    else if(version >= 5) {
      mType = in.readInt();
    }
  }
  
  /**
   * Gets if this entry is a title type entry.
   * <p>
   * @return <code>true</code> if this entry is a title type entry.
   */
  public boolean isTitleType() {
    return mType == TITLE_TYPE;
  }
  
  /**
   * If the title match is using a regular expression.
   * <p>
   * @return <code>true</code> if the title match is using a regular expression, <code>false</code> if not.
   */
  public boolean isRegExType() {
    return mType == REGEX_TYPE;
  }
  
  /**
   * If this entry is using a program filter for matching of programs.
   * <p>
   * @return <code>true</code> if this entry is using a program filter.
   */
  public boolean isFilterType() {
    return mType == FILTER_TYPE;
  }
  
  /**
   * Sets the new title match value.
   * <p>
   * @param title The new title to match.
   */
  public void setTitle(String title) {
    mTitle = title;
    
    if(isFilterType()) {
      mFilter = null;
    }
  }
  
  /**
   * Set if this entry shows short links
   * <p>
   * @param shortLink <code>true</code> if the entry should show short link-
   */
  public void setShortLink(boolean shortLink) {
    mIsShortLink = shortLink;
  }
  
  /**
   * Sets the url list of this entry.
   * <p>
   * @param urlList The new url list of this entry.
   */
  public void setUrlList(String[] urlList) {
    mUrlList = urlList;
  }
  
  /**
   * Sets the type of this entry.
   * <p>
   * @param type The new type of this entry.
   */
  public void setType(int type) {
    mType = type;
  }
  
  /**
   * Checks if a REGEX_TYPE or FILTER_TYPE matches the program.
   * A TITLE_TPYE is never matched by this method.
   * <p>
   * @param p The program to check
   * @return <code>true</code> if the program matches.
   */
  public boolean matches(Program p) {
    switch (mType) {
      case REGEX_TYPE: return p.getTitle().matches(mTitle);
      case FILTER_TYPE: {
        if(mFilter == null) {
          ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
          
          for(ProgramFilter test : filters) {
            if(test != null && test.getName().equals(mTitle)) {
              mFilter = test;
              break;
            }
          }
        }
        
        return mFilter != null && mFilter.accept(p);
      }
    }
    
    return false;
  }
  
  public int compareTo(UrlListEntry o) {
    return mTitle.replaceAll("\\p{Punct}", "").compareToIgnoreCase(o.mTitle.replaceAll("\\p{Punct}", ""));
  }
  
  @Override
  public String toString() {
    return mType + " " + mTitle + " " + isShortLinkEntry();
  }
}
