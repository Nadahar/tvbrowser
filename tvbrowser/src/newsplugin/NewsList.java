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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package newsplugin;

import java.util.ArrayList;

/**
 * A class with the news in a backward time sorted list.
 * 
 * @author René Mach
 */
public class NewsList {
  private ArrayList<News> mList = new ArrayList<News>();
  
  /**
   * Adds a news sorted into the list.
   * 
   * @param news The news to add.
   */
  public void add(News news) {
    int i;
    
    for(i = 0; i < mList.size(); i++) {
      int compareValue = mList.get(i).compareTo(news);
      
      if(compareValue < 0) {
        mList.add(i,news);
        break;
      }
      else if(compareValue == 0) {
        return;
      }
    }
    
    if(i == mList.size()) {
      mList.add(news);
    }
  }
  
  /**
   * Gets the ArrayList with the news.
   * @return The ArrayList with the news
   */
  public ArrayList<News> getList() {
    return mList;
  }
  
  /**
   * Gets the last news time.
   * 
   * @param defaultValue The default value to return if there are
   *                     no news.
   * @return The last news time.
   */
  public long getLastNewsTime(long defaultValue) {
    if(mList.isEmpty()) {
      return defaultValue;
    }
    
    return mList.get(0).getTime().getTime();
  }
}
