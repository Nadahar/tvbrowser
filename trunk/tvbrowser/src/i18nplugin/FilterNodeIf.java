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

import java.util.Locale;

/**
 * A Node implementing this interface should be able to filter it's children. 
 * 
 * @author Torsten Keil
 *
 */
public interface FilterNodeIf {
  
  /**
   * Sets a new filter.
   * 
   * @param locale - the local to check.
   * @param filter - the filter value to check for.
   */
  void setFilter(Locale locale, String filter);
  
  /**
   * Returns true if the node (or any sub node) matches the filter.
   * 
   * @return
   */
  boolean matches();
  
  /**
   * Returns the total number of matches in all sub nodes.
   * 
   * @return
   */
  int getMatchCount();

}
