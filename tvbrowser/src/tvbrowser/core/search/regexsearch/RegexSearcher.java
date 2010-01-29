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
package tvbrowser.core.search.regexsearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.search.AbstractSearcher;
import util.exc.TvBrowserException;

/**
 * Searches for programs using a regular expression.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RegexSearcher extends AbstractSearcher {

  /** The regex pattern. Is null if the pattern would match everything. */
  private Pattern mPattern;
  /**
   * the (non regex) search term to search first.
   * only if this is found, the regex search is done
   */
  private String preFilter;

  /**
   * Creates a new instance of RegexSearcher.
   *
   * @param pattern The regex pattern to use.
   */
  public RegexSearcher(Pattern pattern) {
    mPattern = pattern;
  }


  /**
   * Creates a new instance of RegexSearcher.
   *
   * @param regex
   * @param caseSensitive
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   */
  public RegexSearcher(String regex, boolean caseSensitive)
    throws TvBrowserException
  {
    // Check whether the pattern matches everything
    if (StringUtils.isBlank(regex)) {
      // It does -> Use a null pattern
      mPattern = null;
    } else {
      mPattern = createSearchPattern(regex, caseSensitive);
    }
  }

  /**
   * Creates a new instance of RegexSearcher.
   *
   * @param regex
   * @param caseSensitive
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   */
  public RegexSearcher(String regex, boolean caseSensitive, String searchTerm)
    throws TvBrowserException
  {
    this(regex, caseSensitive);
    // use largest word part for a first pass filter
    String[] parts = searchTerm.split("\\s");
    preFilter = parts[0];
    for (String part : parts) {
      if (part.length() > preFilter.length()) {
        preFilter = part;
      }
    }
    preFilter = preFilter.toLowerCase();
  }

  /**
   * Creates a pattern for a regular expression.
   *
   * @param regex The regular expression
   * @param caseSensitive Should the search be case sensitive?
   * @return The pattern
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   */
  public static Pattern createSearchPattern(String regex, boolean caseSensitive)
    throws TvBrowserException
  {
    // Get the flags for the regex
    int flags = Pattern.DOTALL;
    if (! caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
      flags |= Pattern.UNICODE_CASE;
    }

    // Compile the regular expression
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex, flags);
    }
    catch (PatternSyntaxException exc) {
      throw new TvBrowserException(RegexSearcher.class, "error.1",
        "Syntax error in the regualar expression of the search pattern!", exc);
    }

    return pattern;
  }


  /**
   * Creates a regex from a search text.
   * <p>
   * All regex code in the search text will be quoted. The returned regex will
   * ignore differences in whitespace.
   *
   * @param searchText The search text to create a regex for.
   * @param matchKeyword Specifies whether the regex should match a keyword
   *        (= substring). If false the returned regex will only match if the
   *        checked String matches exactly
   *
   * @return The search text as regular expression
   */
  public static String searchTextToRegex(String searchText, boolean matchKeyword) {
    // TODO: To avoid that the a search pattern matches everything (which takes
    //       a long time and may mess everything up), we return an empty String
    //       if the search text is empty.
    //       -> An empty pattern will cause an empty result.
    //          (See RegexSearcher(String, boolean))
    if (StringUtils.isBlank(searchText)) {
      return "";
    }

    // NOTE: We replace all whitespace with a regex that matches whitespace.
    //       This way the search hits will contain "The film", when the user
    //       entered "The    film"
    // NOTE: All words are quoted with "\Q" and "\E". This way regex code will
    //       be ignored within the search text. (A search for "C++" will not
    //       result in an syntax error)
    String regex = "\\Q" + searchText.replaceAll("\\s+", "\\\\E\\\\s+\\\\Q") + "\\E";

    // Add '.*' to beginning an end to match keywords
    if (matchKeyword) {
      regex = ".*" + regex + ".*";
    }

    return regex;
  }

  /**
   * Checks whether a value matches to the criteria of this searcher.
   *
   * @param value The value to check
   * @return Whether the value matches.
   */
  protected boolean matches(String value) {
    // Check whether the pattern matches everything
    if (mPattern == null) {
      // (This avoids that a pattern matches everything)
      return false;
    } else {
      // first do a quick string search
      if (preFilter != null) {
        if (value.toLowerCase().indexOf(preFilter) < 0) {
          return false;
        }
      }
      // second step: regex search
      Matcher matcher = mPattern.matcher(value);
      return matcher.matches();
    }
  }


  /**
   * get the pattern used by this searcher
   * @return the pattern
   * @since 3.0
   */
  public Pattern getPattern() {
    return mPattern;
  }

}
