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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.misc;

/**
 * This class detects the Java-Version
 */
public class JavaVersion {
  
  public static final int VERSION_NOTFOUND = 0;

  public static final int VERSION_1_0 = 1;

  public static final int VERSION_1_1 = 2;

  public static final int VERSION_1_2 = 3;

  public static final int VERSION_1_3 = 4;

  public static final int VERSION_1_4 = 5;

  public static final int VERSION_1_5 = 6;

  public static final int VERSION_1_6 = 7;

  public static final int VERSION_1_7 = 8;

  /**
   * Returns the Version of Java.
   * @return VERSION_ - Value acording to the Java-Version
   */
  public static int getVersion() {

    String[] ver = System.getProperty("java.version").split("\\.");

    if (ver.length < 2) {
      return -1;
    }

    try {

      int major = Integer.parseInt(ver[0]);
      int minor = Integer.parseInt(ver[1]);

      if (major == 1) {

        switch (minor) {
        case 0:
          return VERSION_1_0;
        case 1:
          return VERSION_1_1;
        case 2:
          return VERSION_1_2;
        case 3:
          return VERSION_1_3;
        case 4:
          return VERSION_1_4;
        case 5:
          return VERSION_1_5;
        case 6:
          return VERSION_1_6;
        case 7:
          return VERSION_1_7;
        default:
          break;
        }

        if (minor > 7) {
          return VERSION_1_7;
        }
      }

    } catch (Exception e) {
      // TODO: handle exception
    }

    return -1;
  }
}
