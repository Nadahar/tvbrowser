/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;
/**
 * A compatibily class for Apache StringUtils.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class StringUtils {
  public static boolean isEmpty(final String value) {
    return value != null && !value.equals("");
  }
  
  public static boolean isBlank(final String value) {
    return value != null && value.trim().equals("");
  }
  
  public static String removeEnd(String haystack, final String needle) {
    if(haystack != null && needle != null && needle.length() > 0 && haystack.endsWith(needle)) {
      haystack = haystack.substring(0, haystack.length()-needle.length());
    }
    
    return haystack;
  }
}
