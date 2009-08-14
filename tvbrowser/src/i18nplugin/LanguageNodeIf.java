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

import java.io.IOException;
import java.util.Locale;

/**
 * This interface is important for the treecellrenderer. All TreeNodes that implement
 * this will be colored according the result of the method allTranslationsAvailableFor
 * 
 * @author bodum
 */
public interface LanguageNodeIf {
  
  int STATE_MISSING_TRANSLATION = 0;
  int STATE_NON_WELLFORMED = 1;
  int STATE_NON_WELLFORMED_ARG_COUNT = STATE_NON_WELLFORMED + 1;
  int STATE_NON_WELLFORMED_ARG_FORMAT = STATE_NON_WELLFORMED + 2;
  int STATE_NON_WELLFORMED_PUNCTUATION_END = STATE_NON_WELLFORMED + 3;
  int STATE_OK = 10;

  /**
   * This method returns the translation state, if all translations are available for a certain locale.
   * 
   * The implementation has to go thru all child-nodes.
   * 
   * @param locale Locale
   * @return combined translation state for all sub nodes
   */
  public int translationStateFor(Locale locale);
  
  /**
   * Saves all changes to the User-Directory
   *
   * @throws java.io.IOException saving went wrong
   */
  public void save() throws IOException;
  
}