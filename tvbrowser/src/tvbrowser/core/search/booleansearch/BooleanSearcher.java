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

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.search.AbstractSearcher;

/**
 * Zentrale Kontrollklasse für boolsche Suchen.
 *
 * Muster: WOrt AND WOrt OR NOT (WOrt AND WOrt). Jedes Wort kann alle
 * Zeichenfolgen inklusive " " enthalten.
 *
 * Ausnahmen (casesensitive): "AND", "OR", "&&", "||", "NOT", "(", ")" und
 * regex-Ausdrücke. " "-Zeichen werden durch den regex-Ausdruck "\s"
 * ersetzt.
 *
 * regex-Ausdruck in den Wörtern führen zu unvOrhersagbarem Verhalten
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

  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(BooleanSearcher.class);

  private IMatcher mRootMatcher;

  private boolean mCaseSensitive;
  
  private String mPattern;

  /**
   * Checks whether a value matches to the criteria of this searcher.
   *
   * @param searchTerm
   *          The value to check
   * @return Whether the value matches.
   */
  protected boolean matches(String searchTerm) {
    if (!mCaseSensitive) {
      searchTerm = searchTerm.toLowerCase();
    }
    return mRootMatcher.matches(searchTerm.replaceAll("\\s+", " "));
  }

  public String toString() {
    return mRootMatcher.toString();
  }

  /**
   * Erzeugt einen neuen Suchbaum. Der Baum wird automatisch optimiert. Es kann
   * immer nur ein Konstruktor gleichzeitig laufen. Fuer Synchronization ist
   * gesorgt.
   *
   * @throws ParserException
   */
  public BooleanSearcher(String pattern, boolean caseSensitive) throws ParserException {
    Hashtable<String, Object> matcherTab = new Hashtable<String, Object>();
    mCaseSensitive = caseSensitive;
    mPattern = pattern;
    mReplaceSpCh = true;

    pattern = pattern.trim();
    int braceDifference = StringUtils.countMatches(pattern, "(") - StringUtils.countMatches(pattern, ")");
    if (braceDifference > 0) {
      pattern = pattern + StringUtils.repeat(")", braceDifference);
    }
    pattern = pattern.replaceAll("\\\"", " ");
    pattern = pattern.replaceAll("\\(", " ( ");
    pattern = pattern.replaceAll("\\)", " ) ");

    StringTokenizer tokenizer = new StringTokenizer(pattern);
    Vector<Object> parts = new Vector<Object>();
    while (tokenizer.hasMoreElements()) {

      String s = tokenizer.nextToken();
      if (s.equals("(")) {
        parts.add(subPart(tokenizer));
      } else {
        parts.add(s);
      }
    }
    mRootMatcher = getMatcher(parts, mCaseSensitive, matcherTab);
    mRootMatcher = mRootMatcher.optimize();
  }

  private static Object expect(Vector<Object> part, int index, Class<IMatcher> expectedClass, String expectedName)
      throws ParserException {
    Object o = part.get(index);
    if (expectedClass.isInstance(o)) {
      return o;
    } else {
      throw new ParserException(mLocalizer.msg("expectFailed", "Expected {0}, but found '{1}')", expectedName, o
          .toString()));
    }
  }

  private static IMatcher getMatcher(Vector<Object> part, boolean caseSensitive, Hashtable<String, Object> matcherTable)
      throws ParserException {
    boolean lastWasMatch = false;
    for (int i = 0; i < part.size(); i++) {
      Object o = part.get(i);
      if (o instanceof String) {
        String s = (String) o;
        if (!isKeyWord(s)) {
          if (lastWasMatch) {
            Object Otemp = part.get(i - 1);
            if (Otemp instanceof StringMatcher) {
              StringMatcherRegEx ME = new StringMatcherRegEx(((StringMatcher) Otemp).toString(), s, caseSensitive,
                  matcherTable);
              part.set(i - 1, ME);
            } else {
              StringMatcherRegEx ME = (StringMatcherRegEx) Otemp;
              ME.addPart(s);
            }
            part.remove(i);
            i--;
            continue;
          }
          StringMatcher m = new StringMatcher(s, caseSensitive, matcherTable);
          part.set(i, m);
          lastWasMatch = true;
          continue;
        }
      }
      if (o instanceof Vector) {
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
        part.set(i, getMatcher(v, caseSensitive, matcherTable));
      }
    }

    boolean found = true;
    while (found) {
      found = false;
      for (int i = 0; i < part.size(); i++) {
        Object O = part.get(i);
        if ((O instanceof String) && (O.toString().equals("NOT"))) {
          if (i + 1 >= part.size()) {
            throw new ParserException(mLocalizer.msg("unexpectedEndOfInput", "Unexpected end of input"));
          }
          Object O2 = expect(part, i + 1, IMatcher.class, mLocalizer.msg("expression", "expression"));
          NotMatcher n = new NotMatcher((IMatcher) O2);
          part.remove(i);
          part.remove(i);

          /*
           * If the previous Element is not "AND" insert an "AND"-Element
           */
          if ((i > 0) && !(part.get(i - 1) instanceof AndMatcher)
              && !((part.get(i - 1) instanceof String) && ((String) part.get(i - 1)).equals("AND"))) {
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
        if ((O instanceof String) && ((O.toString().equals("AND")) || ((O.toString().equals("&&"))))) {

          if (i <= 0) {
            throw new ParserException(mLocalizer.msg("missingExprBeforeAND", "Missing expression before 'AND'"));
          }
          if (i + 1 >= part.size()) {
            throw new ParserException(mLocalizer.msg("unexpectedEndOfInput", "Unexpected end of input"));
          }
          IMatcher O2 = (IMatcher) expect(part, i - 1, IMatcher.class, mLocalizer.msg("expression", "expression"));
          IMatcher O1 = (IMatcher) expect(part, i + 1, IMatcher.class, mLocalizer.msg("expression", "expression"));
          AndMatcher a = new AndMatcher(O1, O2);
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
        if ((O instanceof String) && ((O.toString().equals("OR")) || (O.toString().equals("||")))) {
          if (i <= 0) {
            throw new ParserException("Missing expression before \"OR\"");
          }
          if (i + 1 >= part.size()) {
            throw new ParserException("Unexpected end of input");
          }
          IMatcher O2 = (IMatcher) expect(part, i - 1, IMatcher.class, mLocalizer.msg("expression", "expression"));
          IMatcher O1 = (IMatcher) expect(part, i + 1, IMatcher.class, mLocalizer.msg("expression", "expression"));
          OrMatcher a = new OrMatcher(O1, O2);
          part.remove(i - 1);
          part.remove(i - 1);
          part.remove(i - 1);
          part.insertElementAt(a, i - 1);
          found = true;
          break;
        }
      }
    }
    return (IMatcher) part.get(0);
  }

  private static boolean isKeyWord(String s) {
    return (s.equals("AND") || s.equals("OR") || s.equals("&&") || s.equals("||") || s.equals("NOT"));
  }

  private static Vector<Object> subPart(StringTokenizer tokenizer) throws ParserException {
    Vector<Object> v = new Vector<Object>();
    while (tokenizer.hasMoreElements()) {
      String s = tokenizer.nextToken();
      if (s.equals("(")) {
        v.add(subPart(tokenizer));
      } else {
        if (s.equals(")")) {
          return v;
        } else {
          v.add(s);
        }
      }
    }
    throw new ParserException(mLocalizer.msg("parenthesisExpected", "'(' expected"));
  }
  
  /**
   * get the pattern used by this searcher
   * @return the pattern as string
   * @since 3.2
   */
  public String getPattern() {
    return mPattern;
  }
  
  /**
   * is this searcher case sensitive
   * @return true if case sensitive
   * @since 3.2
   */
  public boolean isCaseSensitive() {
    return mCaseSensitive;
  }

}
