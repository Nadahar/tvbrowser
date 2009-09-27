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
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Erkennt Wörter mit " "-Zeichen.
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class MatcherEx implements Block {

  private Block pretest;

  private Pattern pattern;

  /** Wird nur während des Baumaufbaus benutzt und danach entsorgt. */
  private Vector<String> vtemp = new Vector<String>();

  private boolean caseSensitive;

  private Hashtable<String, Object> matcherTab;


  public MatcherEx(String s1, String s2, boolean CaseSensitive,
      Hashtable<String, Object> matcherTable) {
    matcherTab = matcherTable;
    caseSensitive = CaseSensitive;
    pretest = new And(new Matcher(s1, caseSensitive, matcherTable),
        new Matcher(s2, caseSensitive, matcherTable));
    vtemp.add(s1);
    vtemp.add(s2);
  }


  public void addPart(String s) {
    pretest = new And(pretest, new Matcher(s, caseSensitive, matcherTab));
    vtemp.add(s);
  }


  public Block finish() {
    String[] toTest = vtemp.toArray(new String[vtemp.size()]);
    int flags = Pattern.DOTALL;
    if (!caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
    }

    toTest = vtemp.toArray(new String[vtemp.size()]);
    StringBuilder regex = new StringBuilder(100);
    regex.append(".*(").append(toTest[0]);
    for (int i = 1; i < toTest.length; i++) {
      regex.append("\\s").append(toTest[i]);
    }
    regex.append(").*");
    pattern = Pattern.compile(regex.toString(), flags);

    //mal kucken ob sich der pretest optimierten lässt:
    pretest = pretest.finish();

    vtemp = null;
    return this;
  }


  /**
   * Zuerst wird schnell über AND-verknüpfte Matcher getestet ob alle Elemente
   * vorhanden sind. Danach kommt ein Regex zum Einsatz.
   */
  public boolean test(String s) {
    if (pretest.test(s)) {
      return pattern.matcher(s).matches();
    }
    return false;
  }


  public String toString() {
    if (pattern != null) {
      return "(" + pattern.pattern() + "[" + pretest.toString() + "])";
    } else {
      return "([" + pretest.toString() + "])";
    }
  }

}