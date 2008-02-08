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
package util.io;

import java.io.IOException;

import junit.framework.TestCase;
import util.io.IniFileReader;

/**
 * Tests the Ini-Reader
 * @author bodum
 */
public class IniFileReaderTest extends TestCase {
  
  public void testIniReader() throws IOException {
    IniFileReader reader = new IniFileReader(this.getClass().getResourceAsStream("index.theme"));
    
    assertEquals(81, reader.getAllSections().length);
    
    assertNull(reader.getSection("nadaSection"));
    
    assertEquals(5, reader.getSection("scalable/emotes").size());
    assertEquals(3, reader.getSection("96x96/devices").size());
    
    assertEquals("Tango", reader.getSection("Icon Theme").get("_Name"));
    assertEquals("gnome,crystalsvg", reader.getSection("Icon Theme").get("Inherits"));
  }
  
}