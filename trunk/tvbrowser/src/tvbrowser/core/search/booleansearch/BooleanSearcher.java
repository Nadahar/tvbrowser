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
import java.util.StringTokenizer;
import java.util.Vector;

import tvbrowser.core.search.AbstractSearcher;

/**
 * Zentrale Kontrollklasse für boolsche Suchen.
 * 
 * Muster: WOrt AND WOrt OR NOT (WOrt AND WOrt). Jedes Wort kann alle
 * Zeichenfolgen inklusive " " enthalten.
 * 
 * Ausnahmen (casesensitive): "AND", "OR", "&&", "||", "NOT", "(", ")" und
 * regex-Ausdrücke. " "-Zeichen werden durch den regex-Ausdrück "\s" ersetzt.
 * 
 * regex-Ausdrück in den Wörtern führen zu unvOrhersagbarem Verhalten
 * 
 * PriOritätsreihenfolge: NOT, AND, OR. Beispiele: WOrt AND WOrt OR WOrt =>
 * (WOrt AND WOrt) OR WOrt WOrt AND NOT WOrt OR WOrt => (WOrt AND (NOT WOrt)) OR
 * WOrt
 * 
 * Klammern müssen nicht geschlossen werden.
 * 
 * Die Klammerung kann beliebig verschaltet werden. (was aber schwer auf die
 * Performance der Kompilierung schlagen kann)
 * 
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class BooleanSearcher extends AbstractSearcher {

  Block root;

  boolean caseSensitive;


  /**
   * Checks whether a value matches to the criteria of this searcher.
   * 
   * @param value The value to check
   * @return Whether the value matches.
   */
  protected boolean matches(String value) {
    return root.test(value);
  }


  /** Debug-Methode: Gibt den Suchbaum als String zurück. */
  public String toString() {
    return root.toString();
  }


  /**
   * Erzeugt einen neuen Suchbaum. Der Baum wird automatisch optimiert. Es kann
   * immer nur ein KonstruktOr gleichzeitig laufen. Für Synchronization ist
   * gesOrgt.
   */
  public BooleanSearcher(String pattern, boolean CaseSensitive) {
    Hashtable matcherTab = new Hashtable();
    caseSensitive = CaseSensitive;
    pattern = pattern.replaceAll("\\\"", " ");
    pattern = pattern.replaceAll("\\(", " ( ");
    pattern = pattern.replaceAll("\\)", " ) ");
    pattern = pattern.replaceAll("\\.", " ");
    pattern = pattern.replaceAll("\\,", " ");
    StringTokenizer ST = new StringTokenizer(pattern);
    Vector part = new Vector();
    while (ST.hasMoreElements()) {
      String S = ST.nextToken();
      if (S.equals("(")) {
        part.add(subPart(ST));
      } else {
        part.add(S);
      }
    }
    root = getBlock(part, caseSensitive, matcherTab);
    root = root.finish();
  }


  private static Block getBlock(Vector part, boolean caseSensitive,
      Hashtable matcherTable) {
    boolean lastWasMatch = false;
    for (int i = 0; i < part.size(); i++) {
      Object O = part.get(i);
      if (O instanceof String) {
        String s = (String) O;
        if (!isKey(s)) {
          if (lastWasMatch) {
            Object Otemp = part.get(i - 1);
            if (Otemp instanceof Matcher) {
              MatcherEx ME = new MatcherEx(((Matcher) Otemp).toString(), s,
                  caseSensitive, matcherTable);
              part.set(i - 1, ME);
            } else {
              MatcherEx ME = (MatcherEx) Otemp;
              ME.addPart(s);
            }
            part.remove(i);
            i = 0;
            continue;
          }
          Matcher m = new Matcher(s, caseSensitive, matcherTable);
          part.set(i, m);
          lastWasMatch = true;
          continue;
        }
      }
      if (O instanceof Vector) {
        if (lastWasMatch) {
          part.insertElementAt("AND", i);
          i = 0;
          continue;
        }
      }
      lastWasMatch = false;
    }

    for (int i = 0; i < part.size(); i++) {
      Object O = part.get(i);
      if (O instanceof Vector) {
        part.set(i, getBlock((Vector) O, caseSensitive, matcherTable));
      }
    }

    boolean found = true;
    while (found) {
      found = false;
      for (int i = 0; i < part.size(); i++) {
        Object O = part.get(i);
        if ((O instanceof String) && (O.toString().equals("NOT"))) {
          Object O2 = part.get(i + 1);
          Not n = new Not((Block) O2);
          part.remove(i);
          part.remove(i);
          part.insertElementAt(n, i);
          found = true;
          break;
        }
      }
    }

    found = true;
    while (found) {
      found = false;
      for (int i = 0; i < part.size(); i++) {
        Object O = part.get(i);
        if ((O instanceof String)
            && ((O.toString().equals("AND")) || ((O.toString().equals("&&"))))) {

          Block O2 = (Block) part.get(i - 1);
          Block O1 = (Block) part.get(i + 1);
          And a = new And(O1, O2);
          part.remove(i - 1);
          part.remove(i - 1);
          part.remove(i - 1);
          part.insertElementAt(a, i - 1);
          found = true;
          break;
        }
      }
    }

    found = true;
    while (found) {
      found = false;
      for (int i = 0; i < part.size(); i++) {
        Object O = part.get(i);
        if ((O instanceof String)
            && ((O.toString().equals("OR")) || (O.toString().equals("||")))) {
          Block O2 = (Block) part.get(i - 1);
          Block O1 = (Block) part.get(i + 1);
          Or a = new Or(O1, O2);
          part.remove(i - 1);
          part.remove(i - 1);
          part.remove(i - 1);
          part.insertElementAt(a, i - 1);
          found = true;
          break;
        }
      }
    }
    return (Block) part.get(0);
  }


  private static boolean isKey(String s) {
    return (s.equals("AND") || s.equals("OR") || s.equals("&&")
        || s.equals("||") || s.equals("NOT"));
  }


  private static Vector subPart(StringTokenizer ST) {
    Vector v = new Vector();
    while (ST.hasMoreElements()) {
      String S = ST.nextToken();
      if (S.equals("(")) {
        v.add(subPart(ST));
      } else {
        if (S.equals(")")) {
          return v;
        } else {
          v.add(S);
        }
      }
    }
    return v;
  }

}