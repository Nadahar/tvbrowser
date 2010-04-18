/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 * Zentrale Kontrollklasse f�r boolsche Suchen.
 *
 * Muster: WOrt AND WOrt OR NOT (WOrt AND WOrt). Jedes Wort kann alle
 * Zeichenfolgen inklusive " " enthalten.
 *
 * Ausnahmen (casesensitive): "AND", "OR", "&&", "||", "NOT", "(", ")" und
 * regex-Ausdr�cke. " "-Zeichen werden durch den regex-Ausdr�ck "\s" ersetzt.
 *
 * regex-Ausdr�ck in den W�rtern f�hren zu unvOrhersagbarem Verhalten
 *
 * PriOrit�tsreihenfolge: NOT, AND, OR. Beispiele: WOrt AND WOrt OR WOrt =>
 * (WOrt AND WOrt) OR WOrt WOrt AND NOT WOrt OR WOrt => (WOrt AND (NOT WOrt)) OR
 * WOrt
 *
 * Klammern m�ssen nicht geschlossen werden.
 *
 * Die Klammerung kann beliebig verschaltet werden. (was aber schwer auf die
 * Performance der Kompilierung schlagen kann)
 *
 * @author Gilson Laurent, pumpkin@gmx.de
 */
public class BooleanSearcher extends AbstractSearcher {

  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(BooleanSearcher.class);


  private Block root;

  private boolean caseSensitive;


  /**
   * Checks whether a value matches to the criteria of this searcher.
   *
   * @param value The value to check
   * @return Whether the value matches.
   */
  protected boolean matches(String value) {
    if (!caseSensitive){
      value = value.toLowerCase();
    }
    return root.test(value.replaceAll("\\s+"," "));
  }


  /** Debug-Methode: Gibt den Suchbaum als String zurueck. */
  public String toString() {
    return root.toString();
  }


  /**
   * Erzeugt einen neuen Suchbaum. Der Baum wird automatisch optimiert. Es kann
   * immer nur ein Konstruktor gleichzeitig laufen. Fuer Synchronization ist
   * gesorgt.
   * @throws ParserException
   */
  public BooleanSearcher(String pattern, boolean CaseSensitive) throws ParserException {
    Hashtable<String, Object> matcherTab = new Hashtable<String, Object>();
    caseSensitive = CaseSensitive;
    mReplaceSpCh = true;

    pattern = pattern.trim();
    pattern = pattern.replaceAll("\\\"", " ");
    pattern = pattern.replaceAll("\\(", " ( ");
    pattern = pattern.replaceAll("\\)", " ) ");
    pattern = pattern.replaceAll("[\\p{Punct}&&[^()]]", " AND ");

    StringTokenizer ST = new StringTokenizer(pattern);
    Vector<Object> part = new Vector<Object>();
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

  private static Object expect(Vector<Object> part, int index, Class<Block> expectedClass, String expectedName) throws ParserException {
    Object o = part.get(index);
    if (expectedClass.isInstance(o)) {
      return o;
    }
    else {
      throw new ParserException(mLocalizer.msg("expectFailed","Expected {0}, but found '{1}')", expectedName, o.toString()));
    }
  }

  private static Block getBlock(Vector<Object> part, boolean caseSensitive,
                                Hashtable<String, Object> matcherTable) throws ParserException {
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
            i--;
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
        @SuppressWarnings("unchecked")
        Vector<Object> v = (Vector<Object>) O;
        part.set(i, getBlock(v, caseSensitive, matcherTable));
      }
    }

    boolean found = true;
    while (found) {
      found = false;
      for (int i = 0; i < part.size(); i++) {
        Object O = part.get(i);
        if ((O instanceof String) && (O.toString().equals("NOT"))) {
          if (i + 1 >= part.size()) {
            throw new ParserException(mLocalizer.msg("unexpectedEndOfInput","Unexpected end of input"));
          }
          Object O2 = expect(part, i+1, Block.class, mLocalizer.msg("expression","expression"));
          Not n = new Not((Block) O2);
          part.remove(i);
          part.remove(i);

          /*
          * If the previous Element is not "AND" insert an "AND"-Element
          */
          if ((i>0) && !(part.get(i-1) instanceof And) &&
              !((part.get(i-1) instanceof String) && ((String)part.get(i-1)).equals("AND"))) {
            part.insertElementAt("AND", i);
            i++;
          }
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

          if (i <= 0) {
            throw new ParserException(mLocalizer.msg("missingExprBeforeAND","Missing expression before 'AND'"));
          }
          if (i + 1 >= part.size()) {
            throw new ParserException(mLocalizer.msg("unexpectedEndOfInput","Unexpected end of input"));
          }
          Block O2 = (Block) expect(part, i-1, Block.class, mLocalizer.msg("expression","expression"));
          Block O1 = (Block) expect(part, i+1, Block.class, mLocalizer.msg("expression","expression"));
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
          if (i <= 0) {
            throw new ParserException("Missing expression before \"OR\"");
          }
          if (i + 1 >= part.size()) {
            throw new ParserException("Unexpected end of input");
          }
          Block O2 = (Block) expect(part, i-1, Block.class, mLocalizer.msg("expression","expression"));
          Block O1 = (Block) expect(part, i+1, Block.class, mLocalizer.msg("expression","expression"));
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


  private static Vector<Object> subPart(StringTokenizer ST) throws ParserException {
    Vector<Object> v = new Vector<Object>();
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
    throw new ParserException(mLocalizer.msg("parenthesisExpected","'(' expected"));
  }

}
