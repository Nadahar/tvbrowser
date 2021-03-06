/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import captureplugin.drivers.dreambox.connector.cs.E2ListMapHandler;

/**
 * This class parses the timerevent-xml-stream from a dreambox
 * 
 * adopted by fishhead
 */
public class DreamboxTimerHandler extends DefaultHandler {
    // fishhead ---------------------
    private List<Map<String, String>> mTimers = new ArrayList<Map<String, String>>();
    private Map<String, String> mCurrentHashMap = new TreeMap<String, String>();
    // fishhead ---------------------
    private StringBuilder mCharacters = new StringBuilder();

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!"e2timer".equals(qName) && !"e2timerlist".equals(qName)) {
      // fishhead ---------------------
      String data = E2ListMapHandler.convertToString(mCharacters);
      mCurrentHashMap.put(qName, data);
      // fishhead ---------------------
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        mCharacters = new StringBuilder();

        if ("e2timer".equals(qName)) {
            mCurrentHashMap = new HashMap<String, String>();
            mTimers.add(mCurrentHashMap);
        }

    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        mCharacters.append(ch, start, length);
    }

    /**
     * @return List of Timers
     */
    // fishhead ---------------------
    public List<Map<String, String>> getTimers() {
    // fishhead ---------------------
        return mTimers;
    }
}
