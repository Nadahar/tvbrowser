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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xalan.serialize.SerializerToXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.progress.Progress;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * This class contains the communication with the server
 * @author bodo tasche
 */
public class Updater implements Progress {
	/** Localizer */
	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(Updater.class);
	/** Location of Update-Skript */
//	private static String LOCATION = "http://localhost/wannawork3/tvaddicted/updater.php";
	private static String LOCATION = "http://tvaddicted.wannawork.de/updater.php";
	/** The Plugin */
	private TVRaterPlugin _tvraterPlugin;

	/**
	 * Creates the Updater
	 * @param tvraterPlugin Plugin that uses the Updater
	 */
	public Updater(TVRaterPlugin tvraterPlugin) {
		_tvraterPlugin = tvraterPlugin;
	}

	/**
	 * Does the Update
	 * @throws Exception IOException
	 */
	public void run() {
		try {
			URL url = new URL(LOCATION);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);

			OutputStream out = connection.getOutputStream();

			GZIPOutputStream outZipped = new GZIPOutputStream(out);
			writeData(outZipped);
			outZipped.close();

			Node data = readURLConnection(connection);
			
			if (data.getNodeName().equals("error")) {
			    String message = getTextFromNode(data);
			    
			    JOptionPane.showMessageDialog(
					_tvraterPlugin.getParentFrameForTVRater(), 
					_mLocalizer.msg("serverError","The Server has send the following error:")+ "\n" +
					message, 
					_mLocalizer.msg("error", "Error while updating TV Rater"), 
					JOptionPane.ERROR_MESSAGE);
			} else {
				readData(data);
			}
			
			out.close();
		} catch (Exception e) {
			ErrorHandler.handle(_mLocalizer.msg("updateError", "An error occured while updateting the TVRater Database"), e);
			e.printStackTrace();
		}

	}

	/**
	 * Gets the Text within a Node
     * @param data Node to rip the Text from
     * @return Text in the Node
     */
    private String getTextFromNode(Node data) {
        Node child = data.getFirstChild();
        StringBuffer text = new StringBuffer();
        
        while (child != null) {
            
            if (child.getNodeType() == Node.TEXT_NODE) {
                text.append(child.getNodeValue());
            }
            
            child = child.getNextSibling();
        }
        
        return text.toString();
    }

    /**
	 * Reads the String returned by the PHP-Skript and parses the
	 * DOM
	 * @param data String-DOM representation
	 */
	private void readData(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("data")) {
				readRatingData(child);
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * Reads the Data in this Node
	 * @param node Node to analyse
	 */
	private void readRatingData(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("rating")) {
				readRating(child);
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * Reads a single Rating
	 * @param node Rating as DOM-Node
	 */
	private void readRating(Node node) {
		Rating rating = new Rating();

		Node child = node.getFirstChild();
		while (child != null) {
			String nodename = child.getNodeName();

			if (nodename.equals("title")) {
				rating.setTitle(getNodeValue(child));
			} else if (nodename.equals("overall")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.OVERALL, value);
			} else if (nodename.equals("action")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.ACTION, value);
			} else if (nodename.equals("entitlement")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.ENTITLEMENT, value);
			} else if (nodename.equals("fun")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.FUN, value);
			} else if (nodename.equals("tension")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.TENSION, value);
			} else if (nodename.equals("erotic")) {
				int value = new Double(Double.parseDouble(getNodeValue(child))).intValue();
				rating.setValue(Rating.EROTIC, value);
			}

			child = child.getNextSibling();
		}

		_tvraterPlugin.getDatabase().setOverallRating(rating);
	}

	/**
	 * Returns the Text-Value in this Node
	 * @param node get Text from this Node
	 * @return Text in this Node
	 */
	private String getNodeValue(Node node) {
		StringBuffer value = new StringBuffer();

		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				value.append(child.getNodeValue());
			}
			child = child.getNextSibling();
		}

		return value.toString();
	}

	/**
	 * Writes the Data into the Outputstream 
	 * @param output the Outputstream
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 */
	private void writeData(OutputStream output) throws ParserConfigurationException, IOException {
		Hashtable table = createUpdateList();

		Properties props = new Properties();
		props.put("encoding", "UTF-8");

		SerializerToXML serializer = new SerializerToXML();
		serializer.init(output, props);

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

		Element password =
			createNodeWithTextValue(document, "password", IOUtilities.xorEncode(_tvraterPlugin.getSettings().getProperty("password"), 21));
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
		for (int i = 0; i < list.size(); i++) {
			Element ratingElement = document.createElement("rating");
			setratings.appendChild(ratingElement);

			Rating rating = (Rating) list.get(i);
			ratingElement.appendChild(createNodeWithTextValue(document, "title", rating.getTitle()));

			ratingElement.appendChild(createNodeWithTextValue(document, "overall", rating.getIntValue(Rating.OVERALL)));
			ratingElement.appendChild(createNodeWithTextValue(document, "action", rating.getIntValue(Rating.ACTION)));
			ratingElement.appendChild(createNodeWithTextValue(document, "entitlement", rating.getIntValue(Rating.ENTITLEMENT)));
			ratingElement.appendChild(createNodeWithTextValue(document, "fun", rating.getIntValue(Rating.FUN)));
			ratingElement.appendChild(createNodeWithTextValue(document, "tension", rating.getIntValue(Rating.TENSION)));
			ratingElement.appendChild(createNodeWithTextValue(document, "erotic", rating.getIntValue(Rating.EROTIC)));

		}
		_tvraterPlugin.getDatabase().emptyChangedPersonal();

		// GetRatings
		Element getratings = document.createElement("getratings");
		data.appendChild(getratings);

		Enumeration enum = table.elements();
		while (enum.hasMoreElements()) {
			Element program = document.createElement("program");
			getratings.appendChild(program);

			Program prog = (Program) enum.nextElement();

			program.appendChild(createNodeWithTextValue(document, "title", prog.getTitle()));

		}

		serializer.serialize(document);
	}

	/**
	 * Creates a Node with a filled TextNode
	 * @param doc Create Node with this Document
	 * @param nodename Name of the Node to create
	 * @param value Text-Value in this Node
	 * @return Node with a filled TextNode
	 */
	private Element createNodeWithTextValue(Document doc, String nodename, String value) {
		Element el = doc.createElement(nodename);
		el.appendChild(doc.createTextNode(value));
		return el;
	}

	/**
	 * Creates a Node with a filled TextNode
	 * @param doc Create Node with this Document
	 * @param nodename Name of the Node to create
	 * @param value Text-Value in this Node
	 * @return Node with a filled TextNode
	 */
	private Element createNodeWithTextValue(Document doc, String nodename, int value) {
		return createNodeWithTextValue(doc, nodename, Integer.toString(value));
	}

	/**
	 * Reads the Data in a URLConnection
	 * @param uc Connection
	 * @return Data returned from the URL
	 * @throws Exception IOException etc...
	 */
	private static Node readURLConnection(URLConnection uc) throws Exception {
	    Document document;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new GZIPInputStream(uc.getInputStream()));
		} catch (Exception e) {
			throw e;
		}
		return document.getDocumentElement();
	}

	/**
	 * Runs thru all Days and Channels, creates a List of
	 * Programs that need to get a rating
	 * @return Hashtable filled with Programs to rate
	 */
	private Hashtable createUpdateList() {
		Hashtable table = new Hashtable();

		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

		Date date = new Date();
		for (int d = 0; d < 31; d++) {
			for (int i = 0; i < channels.length; i++) {
				Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);
				while ((it != null) && (it.hasNext())) {
					Program program = (Program) it.next();
					if (program.getLength() >= TVRaterPlugin.MINLENGTH) {
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