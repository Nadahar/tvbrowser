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
package util.ui;

import java.io.*;

import devplugin.ProgramFieldType;

/**
 * Settings for the SearchForm
 * 
 * @see util.ui.SearchForm
 * @author Til Schneider, www.murfman.de
 */
public class SearchFormSettings {

  /** Specifies, that the search term has to match exacly. */
  public static final int MATCH_EXACTLY = 1;
  /** Specifies, that the search term is a keyword (= substring). */
  public static final int MATCH_KEYWORD = 2;
  /** Specifies, that the search term is a regular expression. */
  public static final int MATCH_REGULAR_EXPRESSION = 3;

  /** Specifies, that only titles should be searched. */
  public static final int SEARCH_IN_TITLE = 1;
  /** Specifies, that all fields should be searched. */
  public static final int SEARCH_IN_ALL = 2;
  /**
   * Specifies, that a user-defined set of fields should be searched.
   * 
   * @see #setUserDefinedFieldTypes(ProgramFieldType[])
   */
  public static final int SEARCH_IN_USER_DEFINED = 3;

  private String mSearchText;
  private int mSearchIn;
  private ProgramFieldType[] mUserDefinedFieldTypes;
  private int mMatch;
  private boolean mCaseSensitive;


  /**
   * Creates a new SearchFormSettings instance.
   * 
   * @param searchText The search text
   */
  public SearchFormSettings(String searchText) {
    mSearchText = searchText;
    mSearchIn = SEARCH_IN_TITLE;
    mMatch = MATCH_KEYWORD;
    mCaseSensitive = false;
  }


  /**
   * Loads a SearchFormSettings instance from a stream.
   * 
   * @param in The stream to read from
   * @throws IOException If reading failed
   * @throws ClassNotFoundException If the read data has the wrong format
   * @see #writeData(ObjectOutputStream)
   */
  public SearchFormSettings(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    in.readInt(); // version
    
    mSearchText = (String) in.readObject();
    mSearchIn = in.readInt();
    mMatch = in.readInt();
    mCaseSensitive = in.readBoolean();
    
    int fieldTypeCount = in.readInt();
    if (fieldTypeCount > 0) {
      mUserDefinedFieldTypes = new ProgramFieldType[fieldTypeCount];
      for (int i = 0; i < mUserDefinedFieldTypes.length; i++) {
        int typeId = in.readInt();
        mUserDefinedFieldTypes[i] = ProgramFieldType.getTypeForId(typeId);
      }
    } else {
      mUserDefinedFieldTypes = null;
    }
  }


  /**
   * Writes the settings into a stream
   * 
   * @param out The stream to write to
   * @throws IOException If writing failed
   * @see #SearchFormSettings(ObjectInputStream)
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeObject(mSearchText);
    out.writeInt(mSearchIn);
    out.writeInt(mMatch);
    out.writeBoolean(mCaseSensitive);
    
    if ((mSearchIn == SEARCH_IN_USER_DEFINED) && (mUserDefinedFieldTypes != null)) {
      out.writeInt(mUserDefinedFieldTypes.length);
      for (int i = 0; i < mUserDefinedFieldTypes.length; i++) {
        out.writeInt(mUserDefinedFieldTypes[i].getTypeId());
      }
    } else {
      out.writeInt(0); // No field types
    }
  }


  /**
   * Gets the search text.
   * 
   * @return The search text
   */
  public String toString() {
    return mSearchText;
  }


  /**
   * Gets the search text.
   * 
   * @return The search text
   */
  public String getSearchText() {
    return mSearchText;
  }
  
  
  /**
   * Sets the search text.
   * 
   * @param searchText The search text
   */
  public void setSearchText(String searchText) {
    mSearchText = searchText;
  }


  /**
   * Gets the search text as regular expression.
   * 
   * @return The search text as regular expression
   */
  public String getSearchTextAsRegex() {
    switch (mMatch) {
      case MATCH_EXACTLY: return "\\Q" + mSearchText + "\\E";
      case MATCH_KEYWORD: return ".*\\Q" + mSearchText + "\\E.*";
      default: return mSearchText;
    }
  }


  /**
   * Gets the field types to search for.
   * 
   * @return The field types to search for.
   */
  public ProgramFieldType[] getFieldTypes() {
    switch (mSearchIn) {
      case SEARCH_IN_TITLE:
        return new ProgramFieldType[] { ProgramFieldType.TITLE_TYPE };
      case SEARCH_IN_ALL: return SearchForm.getSearchableFieldTypes();
      default:
        if (mUserDefinedFieldTypes != null) {
          return mUserDefinedFieldTypes;
        } else {
          return SearchForm.getSearchableFieldTypes();
        }
    }
  }


  ProgramFieldType[] getUserDefinedFieldTypes() {
    return mUserDefinedFieldTypes;
  }


  /**
   * Sets the user-defined field types to search for.
   * 
   * @param typeArr The field types to search for
   */
  public void setUserDefinedFieldTypes(ProgramFieldType[] typeArr) {
    mUserDefinedFieldTypes = typeArr;
  }


  /**
   * Gets where to search. Either {@link #SEARCH_IN_TITLE},
   * {@link #SEARCH_IN_ALL} or {@link #SEARCH_IN_USER_DEFINED}.
   * 
   * @return Where to search
   */
  public int getSearchIn() {
    return mSearchIn;
  }


  /**
   * Sets where to search. Must be either {@link #SEARCH_IN_TITLE},
   * {@link #SEARCH_IN_ALL} or {@link #SEARCH_IN_USER_DEFINED}.
   * 
   * @param searchIn Where to search
   */
  public void setSearchIn(int searchIn) {
    mSearchIn = searchIn;
  }


  /**
   * Gets what how the search text has to match. Either {@link #MATCH_EXACTLY},
   * {@link #MATCH_KEYWORD} or {@link #MATCH_REGULAR_EXPRESSION}.
   * 
   * @return How the search text has to match
   */
  public int getMatch() {
    return mMatch;
  }


  /**
   * Sets what how the search text has to match. Must be Either
   * {@link #MATCH_EXACTLY}, {@link #MATCH_KEYWORD} or
   * {@link #MATCH_REGULAR_EXPRESSION}.
   * 
   * @param match How the search text has to match
   */
  public void setMatch(int match) {
    mMatch = match;
  }


  /**
   * Gets whether to search case sensitive.
   * 
   * @return Whether to search case sensitive
   */
  public boolean getCaseSensitive() {
    return mCaseSensitive;
  }


  /**
   * Sets whether to search case sensitive.
   * 
   * @param caseSensitive Whether to search case sensitive
   */
  public void setCaseSensitive(boolean caseSensitive) {
    mCaseSensitive = caseSensitive;
  }

}