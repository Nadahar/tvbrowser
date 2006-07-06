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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * This interface is important for the treecellrenderer. All TreeNodes that implement
 * this will be colored acording the result of the method allTranslationsAvailableFor 
 * 
 * @author bodum
 */
public interface LanguageNodeIf {

  /**
   * This method returns true if all translations are available for a certain locale.
   * 
   * The implementation has to go thru all child-nodes.
   * 
   * @param locale Locale
   * @return true, if translations are available
   */
  public boolean allTranslationsAvailableFor(Locale locale);
  
  /**
   * Saves all changes to the User-Directory
   */
  public void save() throws FileNotFoundException, IOException;
  
}