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
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import util.io.FileFormatException;
import util.io.IOUtilities;
import static junit.framework.Assert.assertEquals;

public class ChannelListTest {

  @Test
  public void testEscapedChannelNames() throws FileFormatException, IOException, URISyntaxException {
    ChannelList list = new ChannelList("testGroup");

    list.readFromStream(getClass().getResourceAsStream("test_channellist.txt"), null, false);

    assertEquals(6, list.getChannelCount());

    assertEquals("DMAX", list.getChannelAt(0).getName());
    assertEquals("Eurosport", list.getChannelAt(1).getName());
    assertEquals("AXN", list.getChannelAt(2).getName());
    assertEquals("W\u00f6hnungss\u00fcche", list.getChannelAt(3).getName());
    assertEquals("K\u00fcchendi\u00dfteln", list.getChannelAt(4).getName());
    assertEquals("\u00c4lbert \u7360", list.getChannelAt(5).getName());

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    list.writeToStream(stream);

    assertEquals("de;GMT+01:00;DMAX;DMAX;(c) by DMAX;http://www.dmaxtv.de/;http://sender.wannawork.de/logos/DMAX.png;257;\"DMAX\"\n" +
        "de;GMT+01:00;EUROSPORT;Eurosport;(c) by Eurosport;;;0;\"Eurosport\"\n" +
        "de;GMT+01:00;AXN;AXN;(c) by AXN;http://www.axntv.de;;273;\"AXN\"\n" +
        "de;GMT+01:00;DMAXPUNNY;DMAX PUNNY;(c) by DMAX;http://www.dmaxtv.de/;http://sender.wannawork.de/logos/DMAX.png;257;\"W&ouml;hnungss&uuml;che\"\n" +
        "de;GMT+01:00;EUROSPORTPUNNY;Eurosport PUNNY;(c) by Eurosport;;;0;\"K&uuml;chendi&szlig;teln\"\n" +
        "de;GMT+01:00;AXNPUNNY;AXN PUNNY;(c) by AXN;http://www.axntv.de;;273;\"&Auml;lbert &#29536;\"",
        toString(new GZIPInputStream(new ByteArrayInputStream(stream.toByteArray()))));
  }


  /**
   * Writes the content of the input stream to a <code>String<code>.
   */
  private String toString(InputStream inputStream) throws IOException {
    String string;
    StringBuilder outputBuilder = new StringBuilder();
    if (inputStream != null) {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(inputStream));
      while (null != (string = reader.readLine())) {
        outputBuilder.append(string).append('\n');
      }
    }
    return outputBuilder.toString().trim();
  }

}
