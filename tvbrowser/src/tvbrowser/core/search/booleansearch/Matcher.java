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
public class Matcher implements Block, StringCompare {

  private String toTest;

  private Hashtable<String, Object> multiTable;

  private boolean caseSen;


  public Matcher(String s, boolean caseSensitive, Hashtable<String, Object> matcherTab) {
    caseSen = caseSensitive;
    toTest = s;
    if (!caseSensitive) {
      toTest = toTest.toLowerCase();
    }
    multiTable = matcherTab;
    Object O = multiTable.get(toTest);
    if (O == null) {
      multiTable.put(toTest, this);
    } else {
      if (O instanceof Matcher) {
        MatcherReuse mr = new MatcherReuse(toTest, caseSen);
        multiTable.put(toTest, mr);
      }
    }
  }


  public boolean test(String s) {
    return s.indexOf(toTest) != -1;
  }


  public String toString() {
    return toTest;
  }


  public int size() {
    return toTest.length();
  }


  public Block finish() {
    Block b = (Block) multiTable.get(toTest);
    multiTable = null;
    return b;
  }
}