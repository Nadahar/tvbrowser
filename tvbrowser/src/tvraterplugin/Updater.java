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
 */

package tvraterplugin;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xalan.serialize.SerializerToXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * This class contains the communication with the server
 * @author bodo tasche
 */
public class Updater {
	private static String LOCATION = "http://localhost/test/xmltest.php"; 

	private TVRaterPlugin _tvraterPlugin;

	public Updater(Frame parent, TVRaterPlugin tvraterPlugin) {
		_tvraterPlugin = tvraterPlugin;
	}

	public void doUpdate() throws Exception {
		System.out.println("Update!!");

		URL url = new URL(LOCATION);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);

		OutputStream out = connection.getOutputStream();
		
		GZIPOutputStream outZipped = new GZIPOutputStream(out);
		writeData(outZipped);
		outZipped.close();

		String data = readURLConnection(connection);
		System.out.println(data);
		readData(data);
		out.close();
	}

	/**
	 * @param data
	 */
	private void readData(String data) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new StringBufferInputStream(data));

		showAll(document.getDocumentElement());
	}
	
	private void showAll(Node el) {
		System.out.println(el.getNodeName() + "--" + el.getNodeValue());
		
		Node child = el.getFirstChild();
		while (child != null) {
			showAll(child);
			child = child.getNextSibling(); 
		}
	}

	/**
	 * Writes the Data into the Outputstream 
	 * @param output the Outputstream
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 */
	private void writeData(OutputStream output) throws ParserConfigurationException, IOException{
		Hashtable table = createUpdateList();
		
		Properties props = new Properties();
		props.put( "encoding", "UTF-8" );

		SerializerToXML serializer = new SerializerToXML();
		serializer.init( output, props );
						
		Document document;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.newDocument();
		Element tvrater = document.createElement("tvrater");
		document.appendChild(tvrater);

		// User		
		Element user = document.createElement("user");

		Element name = createNodeWithTextValue(document, "name", _tvraterPlugin.getSettings().getProperty("name"));
		user.appendChild(name);

		Element password = createNodeWithTextValue(document, "password",IOUtilities.xorEncode(_tvraterPlugin.getSettings().getProperty("password"), 21));
		user.appendChild(password);
		
		tvrater.appendChild(user);

		// Command
		Element command = createNodeWithTextValue(document, "command", "Update");
		tvrater.appendChild(command);

		// Data
		Element data = document.createElement("data");
		tvrater.appendChild(data);

		// Setratings
		Element setratings = document.createElement("setratings");
		data.appendChild(setratings);

		ArrayList list = _tvraterPlugin.getDatabase().getChangedPersonal();
		for (int i=0; i < list.size(); i++) {
			Element ratingElement = document.createElement("rating");
			setratings.appendChild(ratingElement);
			
			Rating rating = (Rating)list.get(i);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "title", rating.getTitle())
			);

			ratingElement.appendChild(
				createNodeWithTextValue(document, "overall", rating.getIntValue(Rating.OVERALL))
			);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "action", rating.getIntValue(Rating.ACTION))
			);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "entitlement", rating.getIntValue(Rating.ENTITLEMENT))
			);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "fun", rating.getIntValue(Rating.FUN))
			);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "tension", rating.getIntValue(Rating.TENSION))
			);
			ratingElement.appendChild(
				createNodeWithTextValue(document, "erotic", rating.getIntValue(Rating.EROTIC))
			);
			
		}
	//	_tvraterPlugin.getDatabase().emptyChangedPersonal();

		// GetRatings
		Element getratings = document.createElement("getratings");
		data.appendChild(getratings);

		Enumeration enum = table.elements();
		while (enum.hasMoreElements()) {
			Element program = document.createElement("program");
			getratings.appendChild(program);

			Program prog = (Program) enum.nextElement();

			program.appendChild(
				createNodeWithTextValue(document, "title", prog.getTitle())
			);
			
		}

		serializer.serialize(document);
	}

	private Element createNodeWithTextValue(Document doc, String nodename, String value) {
		Element el = doc.createElement(nodename);
		el.appendChild(doc.createTextNode(value));
		return el;
	}

	private Element createNodeWithTextValue(Document doc, String nodename, int value) {
		return createNodeWithTextValue(doc, nodename, Integer.toString(value));
	}

	private static String readURLConnection(URLConnection uc) throws Exception {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
//			reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(uc.getInputStream())));
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = null;
			int letter = 0;
			while ((letter = reader.read()) != -1)
				buffer.append((char) letter);
		} catch (Exception e) {
			System.out.println("Cannot read from URL" + e.toString());
			throw e;
		} finally {
			try {
				reader.close();
			} catch (IOException io) {
				System.out.println("Error closing URLReader!");
				throw io;
			}
		}
		return buffer.toString();
	}

	private Hashtable createUpdateList() {
		Hashtable table = new Hashtable();

		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

		Date date = new Date();
		for (int d = 0; d < 31; d++) {
			for (int i = 0; i < channels.length; i++) {
						Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
						while ((it != null) && (it.hasNext())) {
							Program program = (Program) it.next();
							if (program.getLength() >= 75) {
								if (!table.containsKey(program.getTitle())) {
									table.put(program.getTitle(), program);
								}
							}
						}
					}

					date = date.addDays(1);
				}

				return table;
			}
		}			 