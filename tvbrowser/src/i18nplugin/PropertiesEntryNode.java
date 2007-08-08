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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry for a Property
 * 
 * @author bodum
 */
public class PropertiesEntryNode extends DefaultMutableTreeNode implements LanguageNodeIf {

  /**
   * @param name Name of this Entry
   */
  public PropertiesEntryNode(String name) {
    super(name);
  }

  /**
   * @return Name of this Entry
   */
  public String getPropertyName() {
    return toString();
  }

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#allTranslationsAvailableFor(java.util.Locale)
   */
  public boolean allTranslationsAvailableFor(Locale locale) {
    if (getParent() == null)
      return false;
    if (!(getParent() instanceof PropertiesNode))
      return false;
    String translated = ((PropertiesNode)getParent()).getPropertyValue(locale, getPropertyName());
    // check existence of translation
    if (translated.length() == 0) {
      return false;
    }
    // check same number of arguments
    String original = ((PropertiesNode) getParent()).getPropertyValue(getPropertyName());
    List<String> originalArgs = getArgumentList(original);
    List<String> translatedArgs = getArgumentList(translated);
    if (originalArgs.size() != translatedArgs.size()) {
      return false;
    }
    // check same arguments
    for (int i = 0; i < originalArgs.size(); i++) {
      if (!originalArgs.get(i).equals(translatedArgs.get(i))) {
        return false;
      }
      // now remove args to be to compare punctuaction afterwards
      String arg = originalArgs.get(i);
      while (original.indexOf(arg) >= 0) {
        original = original.replace(arg, "");
      }
      while (translated.indexOf(arg) >= 0) {
        translated = translated.replace(arg, "");
      }
    }
    // check that the strings have the same non alphanumeric ends, e.g. "..." in menu items
    Pattern lastChars = Pattern.compile(".*[\\w\\sﬂ](\\W*)",Pattern.DOTALL);
    Matcher matcher = lastChars.matcher(original);
    if (matcher.matches()) {
      String endOriginal = matcher.group(1);
      matcher = lastChars.matcher(translated);
      if (matcher.matches()) {
        String endTranslated = matcher.group(1);
        if (!endOriginal.equals(endTranslated)) {
          return false;
        }
      }
    }
    return true;
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

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#save()
   */
  public void save() {
  }
  
}