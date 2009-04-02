/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package util.io;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Creates a String representation of a XML-Document.
 * Really simple, no Attributes, only Nodes and TextNodes, no PI's and stuff
 * 
 * @author bodum
 */
public class XMLWriter {
    
    /**
     * Returns a String containg the XML-Document
     * @param doc Document
     * @param encoding Encoding
     * @return Document as String
     */
    public String getStringForDocument(Document doc, String encoding) {
        String result = "<?xml version=\"1.0\" encoding=\""+ encoding + "\"?>\n";
        result += getStringForNode(doc.getDocumentElement());
        return result;
    }
    
    /**
     * Returns a String for a Node
     * @param node Node
     * @return String-Represenation of Node
     */
    public String getStringForNode(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return addEntities(node.getNodeValue());
        }
        
        StringBuilder result = new StringBuilder("<" + node.getNodeName() + ">");
        
        Node child = node.getFirstChild();
        while (child != null) {
            result.append(getStringForNode(child));
            child = child.getNextSibling();
        }
        
        result.append("</" + node.getNodeName() + ">");
        
        return result.toString();
    }
    
    /**
     * Replaces the Basic-Entities (eg. from & into &amp);
     * @param string String with &
     * @return String with Entities
     */
    private String addEntities(String string) {
        string = replaceAll(string, "&", "&amp;");
        string = replaceAll(string, "<", "&lt;");
        string = replaceAll(string, ">", "&gt;");
        string = replaceAll(string, "\"","&quot;");
        return string;
    }


    /**
     * Replaces Stings
     * 
     * @param intothis replace here
     * @param old this
     * @param newstr into this
     * @return new String
     */
    private String replaceAll(String intothis, String old, String newstr) {
        int pos = 0;
        int newpos;
        
        while ((newpos = intothis.indexOf(old, pos)) > -1) {
            String oldone = intothis;
            intothis = intothis.substring(0, newpos);
            intothis += newstr;
            intothis += oldone.substring(newpos + old.length());
            pos = newpos + newstr.length();
        }
        
        return intothis;
    }

    /**
     * Writes a Document into an Outputstream
     * @param doc Document
     * @param out Outputstream
     * @param encoding Encoding to use
     * @throws IOException Exception if write failed
     */
    public void writeDocumentToOutputStream(Document doc, OutputStream out, String encoding) throws IOException {
        out.write(getStringForDocument(doc, encoding).getBytes(encoding));
    }

}