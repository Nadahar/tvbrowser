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

/**
 * Wird eingesetzt wenn ein und derselbe matcher mehrmals in einer Suche
 * vorkommt. ! Ersetzt keine matchEx !
 * 
 * Speichert eine Referenz zur letzten Suchstring. Dadurch kann dieser nicht
 * gc-ed werden. Wenn dies ein Problem ist muss der gesamte Suchbaum
 * weggeschmissen werden.
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class MatcherReuse implements IMatcher, IStringSize {

  private String toTest;

  private String lasttest;

  private boolean lastResult;

  public MatcherReuse(String s, boolean caseSensitive) {
    toTest = s;
    if (!caseSensitive) {
      toTest = toTest.toLowerCase();
    }
  }

  public boolean matches(String s) {
    if (lasttest == null || !lasttest.equals(s)) {
      lasttest = s;
      lastResult = s.indexOf(toTest) != -1;
    }
    return lastResult;
  }

  public String toString() {
    return toTest;
  }

  public int size() {
    return toTest.length();
  }

  public IMatcher optimize() {
    return this;
  }
}