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
package tvbrowser.core.search.booleansearch;

import java.util.Hashtable;

/**
 * Erkennt Wörter ohne Leerzeichen
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
class StringMatcher implements IMatcher, IStringSize {

  private String mMatchString;
  private Hashtable<String, Object> mMultiTable;
  private boolean mCaseSensitive;

  public StringMatcher(String s, boolean caseSensitive, Hashtable<String, Object> matcherTab) {
    mCaseSensitive = caseSensitive;
    mMatchString = s;
    if (!caseSensitive) {
      mMatchString = mMatchString.toLowerCase();
    }
    mMultiTable = matcherTab;
    Object o = mMultiTable.get(mMatchString);
    if (o == null) {
      mMultiTable.put(mMatchString, this);
    } else {
      if (o instanceof StringMatcher) {
        MatcherReuse mr = new MatcherReuse(mMatchString, mCaseSensitive);
        mMultiTable.put(mMatchString, mr);
      }
    }
  }

  public boolean matches(final String searchTerm) {
    return searchTerm.indexOf(mMatchString) != -1;
  }

  public String toString() {
    return mMatchString;
  }

  public int size() {
    return mMatchString.length();
  }

  public IMatcher optimize() {
    IMatcher b = (IMatcher) mMultiTable.get(mMatchString);
    mMultiTable = null;
    return b;
  }
}