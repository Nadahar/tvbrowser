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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.misc;

import java.util.HashMap;

public class StringPool {

  private static HashMap<String, String> stringMap = new HashMap<String, String>(
      100);

  /**
   * an estimation of the savings for duplicate strings.
   * the additionally needed size of the hashmap is not included in this calculation
   */
  private static int savedBytes = 0;
//  private static int lastOutput = 0;

  public static String getString(String input) {
    if (input == null) {
      return null;
    }

    synchronized(stringMap) {
      String cached = stringMap.get(input);
      if (cached != null) {
        savedBytes += stringBytes(input);
  /*
        if (savedBytes >= lastOutput + 2000) {
          lastOutput = savedBytes;
          System.out.println("saved " + savedBytes + " bytes using String pool with " + stringMap.size() + " elements");
        }
  */
        return cached;
      }
      else {
        savedBytes -= stringBytes(input);
        stringMap.put(input, input);
        return input;
      }
    }
  }

  private static int stringBytes(String input) {
    return (((input.length() + 2) >> 2) + 4) * 8;
  }

}
