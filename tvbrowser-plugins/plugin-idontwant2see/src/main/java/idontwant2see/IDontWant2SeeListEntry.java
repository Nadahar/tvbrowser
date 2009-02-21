/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package idontwant2see;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devplugin.Date;
import devplugin.Program;

/**
 * A class that contains the search settings for the not to show values.
 * 
 * @author René Mach
 */
public class IDontWant2SeeListEntry {
  private String mSearchText;
  private String mPreSearchPart;
  private Pattern mSearchPattern;
  private boolean mCaseSensitive;
  private boolean mDateWasSet;
  private Date mLastMatchedDate;
  
  protected IDontWant2SeeListEntry(String searchText, boolean caseSensitive) {
    setValues(searchText,caseSensitive);
    mDateWasSet = true;
  }
  
  protected IDontWant2SeeListEntry(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    mSearchText = in.readUTF();
    mCaseSensitive = in.readBoolean();
    
    if(in.readBoolean()) {
      mPreSearchPart = in.readUTF();
      mSearchPattern = createSearchPattern(mSearchText, mCaseSensitive);
    }
    
    if(version >= 6) {
      mLastMatchedDate = new Date(in);
    }
    else {
      mLastMatchedDate = IDontWant2See.getCurrentDate();
    }
    
    mDateWasSet = false;
  }
  
  private boolean matchesTitle(String title) {
    boolean matches = false;
    
    if(mPreSearchPart == null) {
      // match full title
      matches = mCaseSensitive ? title.equals(mSearchText) : title
          .equalsIgnoreCase(mSearchText);
    } else {
      // or match with wild card
      String preSearchValue = mCaseSensitive ? title : title.toLowerCase();
      if (preSearchValue.indexOf(mPreSearchPart) != -1) {
        Matcher match = mSearchPattern.matcher(title);
        matches = match.matches();
      }
    }
    
    // update the "last found" date
    if (!mDateWasSet && matches) {
      mLastMatchedDate = IDontWant2See.getCurrentDate();
      mDateWasSet = false;
    }
    
    return matches;
  }
  
  protected boolean matches(Program p) {
    String title = p.getTitle();
    boolean found = matchesTitle(title);
    final String suffix = " (Fortsetzung)";
    if ((!found) && title.endsWith(suffix)) {
      found = matchesTitle(title.substring(0, title.length() - suffix.length()));
    }
    return found;
  }
  
  protected void resetDateWasSetFlag() {
    mDateWasSet = false;
  }
  
  protected String getSearchText() {
    return mSearchText;
  }
  
  protected boolean isCaseSensitive() {
    return mCaseSensitive;
  }

  protected void setValues(String searchText, boolean caseSensitive) {
    mPreSearchPart = null;
    mSearchPattern = null;
    
    if((mSearchText == null || !mSearchText.equals(searchText)) || mCaseSensitive != caseSensitive) {
      mLastMatchedDate = IDontWant2See.getCurrentDate();
    }
    
    mSearchText = searchText;
    mCaseSensitive = caseSensitive;
    
    if (searchText.indexOf('*') != -1) {
      String[] searchParts = searchText.split("\\*");
      
      if(searchParts != null && searchParts.length > 0) {
        mPreSearchPart = searchParts[0];
        for(int i = 1; i < searchParts.length; i++) {
          if(mPreSearchPart.length() < searchParts[i].length()) {
            mPreSearchPart = searchParts[i];
          }
        }
        
        if(!caseSensitive) {
          mPreSearchPart = mPreSearchPart.toLowerCase();
        }
        
        mSearchPattern = createSearchPattern(searchText,caseSensitive);
      }
    }
  }
  
  private Pattern createSearchPattern(String searchText, boolean caseSensitive) {
    int flags = Pattern.DOTALL;
    if (! caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
      flags |= Pattern.UNICODE_CASE;
    }
    
    // Comment copied from tvbrowser.core.search.regexsearch.RegexSearcher.java:
    // NOTE: All words are quoted with "\Q" and "\E". This way regex code will
    //       be ignored within the search text. (A search for "C++" will not
    //       result in an syntax error)
    return Pattern.compile("\\Q" + searchText.replace("*","\\E.*\\Q") + "\\E",flags);
  }
    
  protected void writeData(ObjectOutputStream out) throws IOException {
    // version 6, keep syncron with data file version from IDontWant2See.java
    out.writeUTF(mSearchText);
    out.writeBoolean(mCaseSensitive);
    
    out.writeBoolean(mPreSearchPart != null);
    
    if(mPreSearchPart != null) {
      out.writeUTF(mPreSearchPart);
    }
    
    mLastMatchedDate.writeData(out);
  }
  
  protected Date getLastMatchedDate() {
    return mLastMatchedDate;
  }
}
