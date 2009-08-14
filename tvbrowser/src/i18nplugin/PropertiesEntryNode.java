/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package i18nplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import util.i18n.WritingConversion;

/**
 * Entry for a Property
 * 
 * @author bodum
 */
public class PropertiesEntryNode extends DefaultMutableTreeNode implements LanguageNodeIf, FilterNodeIf {
  
  private String filter;
  
  private boolean matches;

  /**
   * @param name Name of this Entry
   */
  public PropertiesEntryNode(String name) {
    super(name, false);
  }

  /**
   * @return Name of this Entry
   */
  public String getPropertyName() {
    return toString();
  }

  public int translationStateFor(Locale locale) {
    if (getParent() == null) {
      return STATE_MISSING_TRANSLATION;
    }
    if (!(getParent() instanceof PropertiesNode)) {
      return STATE_MISSING_TRANSLATION;
    }
    String translated = ((PropertiesNode)getParent()).getPropertyValue(locale, getPropertyName());
    String original = ((PropertiesNode) getParent()).getPropertyValue(getPropertyName());
    return getTranslationState(original, translated);
  }

  protected int getTranslationState(String original, String translated) {
    // if no original is available, we don't need a translation, too
    if (original.length() == 0 && translated.length() == 0) {
      return STATE_OK;
    }
    // check existence of translation
    if (translated.length() == 0) {
      return STATE_MISSING_TRANSLATION;
    }
    // check same number of arguments
    List<String> originalArgs = getArgumentList(original);
    List<String> translatedArgs = getArgumentList(translated);
    if (originalArgs.size() != translatedArgs.size()) {
      return STATE_NON_WELLFORMED_ARG_COUNT;
    }
    // check same arguments
    for (int i = 0; i < originalArgs.size(); i++) {
      if (!originalArgs.get(i).equals(translatedArgs.get(i))) {
        return STATE_NON_WELLFORMED_ARG_FORMAT;
      }
      // now remove format arguments to compare punctuation afterwards
      String arg = originalArgs.get(i);
      while (original.indexOf(arg) >= 0) {
        original = original.replace(arg, "");
      }
      while (translated.indexOf(arg) >= 0) {
        translated = translated.replace(arg, "");
      }
    }
    original = WritingConversion.reduceToASCIILetters(original, false);
    translated = WritingConversion.reduceToASCIILetters(translated, false);
    // check that the strings have the same non alphanumeric ends, e.g. "..." in menu items
    Pattern lastChars = Pattern.compile(".*[\\w\\sﬂ](\\W*)",Pattern.DOTALL);
    Matcher matcher = lastChars.matcher(original);
    if (matcher.matches()) {
      String endOriginal = matcher.group(1);
      matcher = lastChars.matcher(translated);
      if (matcher.matches()) {
        String endTranslated = matcher.group(1);
        if (!endOriginal.equals(endTranslated)) {
          return STATE_NON_WELLFORMED_PUNCTUATION_END;
        }
      }
    }
    return STATE_OK;
  }

  private List<String> getArgumentList(String input) {
    List<String> args = new ArrayList<String>();
    Pattern argumentPattern = Pattern.compile(".*?(['\"]\\{\\d\\}['\"]|\\{\\d\\}).*");
    Matcher argumentMatcher = argumentPattern.matcher(input);
    int index = 0;
    while (argumentMatcher.matches()) {
      String argument = argumentMatcher.group(1);
      index+= argumentMatcher.end(1);
      args.add(argument);
      argumentMatcher = argumentPattern.matcher(input.substring(index));
    }
    Collections.sort(args);
    return args;
  }

  public void save() {
  }
  
  @Override
  public boolean isLeaf() {
    return true;
  }

  public int getMatchCount() {
    return matches ? 1 : 0;
  }

  public boolean matches() {
    return matches;
  }

  public void setFilter(Locale locale, String filter) {
    this.filter = filter;
    matches = false;
    if (filter != null) {
      if (getParent() == null) {
        return;
      }
      if (!(getParent() instanceof PropertiesNode)) {
        return;
      }
      
      String text = null;
      String translated = ((PropertiesNode)getParent()).getPropertyValue(locale, getPropertyName());
      // check existence of translation
      if (translated.length() != 0) {
        text = translated;
      } else {
        // check original
        text = ((PropertiesNode) getParent()).getPropertyValue(getPropertyName());
      }
      
      if (text != null && text.toLowerCase().indexOf(filter.toLowerCase()) != -1) {
        matches = true;
      }
    }
  }
}