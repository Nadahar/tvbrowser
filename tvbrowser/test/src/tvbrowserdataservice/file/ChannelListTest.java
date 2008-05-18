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
 *     $Date: 2008-02-08 20:29:06 +0100 (Fr, 08 Feb 2008) $
 *   $Author: bananeweizen $
 * $Revision: 4256 $
 */
package tvbrowserdataservice.file;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import util.io.FileFormatException;
import static junit.framework.Assert.assertEquals;

public class ChannelListTest {

  @Test
  public void testReadPunyCode() throws FileFormatException, IOException, URISyntaxException {
    ChannelList list = new ChannelList("testGroup");

    list.readFromStream(getClass().getResourceAsStream("test_channellist.gz"), null);

    assertEquals(6, list.getChannelCount());

    assertEquals("DMAX", list.getChannelAt(0).getName());
    assertEquals("Eurosport", list.getChannelAt(1).getName());
    assertEquals("AXN", list.getChannelAt(2).getName());
    assertEquals("w\u00f6hnungss\u00fcche", list.getChannelAt(3).getName());
    assertEquals("k\u00fcchendissteln", list.getChannelAt(4).getName());
    assertEquals("\u00e4lbertstrasse", list.getChannelAt(5).getName());
  }
  
}
