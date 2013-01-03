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

import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import captureplugin.drivers.dreambox.connector.cs.E2ListMapHandler;
import captureplugin.drivers.dreambox.connector.cs.E2TimerHelper;

/**
 * This class reads dreambox xml-data and maps it into a map
 * 
 * adopted by fishhead
 */
public class DreamboxHandler extends DefaultHandler {

    private TreeMap<String, String> mData = new TreeMap<String, String>();
    private StringBuilder mCharacters = new StringBuilder();
    private String mKey;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        mCharacters = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        mCharacters.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // fishhead -----------------------
        String data = E2ListMapHandler.convertToString(mCharacters);
        if (E2TimerHelper.SERVICEREFERENCE.equals(qName)) {
            mKey = data;
        } else if (E2TimerHelper.SERVICENAME.equals(qName)) {
            mData.put(mKey, data);
        // fishhead -----------------------
        }
    }

    /**
     * @return data from xml-stream as Map
     */
    public TreeMap<String, String> getData() {
        return mData;
    }
}
