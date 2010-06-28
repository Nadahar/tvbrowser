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
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.io.NetworkUtilities;
import util.io.XMLWriter;
import util.ui.Localizer;
import util.ui.progress.Progress;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * This class contains the communication with the server
 *
 * @author bodo tasche
 */
public class Updater implements Progress {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(Updater.class);

  /** Location of Update-Skript */
  // private static String LOCATION =
  // "http://localhost/~bodum/wannawork3/tvaddicted/updater.php";
  private static String LOCATION = "http://tvaddicted.de/updater.php";

  /** The Plugin */
  private TVRaterPlugin mPlugin;

  /** Update Successfull ? */
  private boolean mWasSuccessfull = false;

  private Hashtable<String, Program> mUpdateList;

  private TVRaterSettings mSettings;

  /**
   * Creates the Updater
   *
   * @param tvraterPlugin Plugin that uses the Updater
   */
  public Updater(final TVRaterPlugin tvraterPlugin, final TVRaterSettings settings) {
    mPlugin = tvraterPlugin;
    this.mSettings = settings;
  }

  /**
   * Does the Update
   */
  public void run() {
    String name = mSettings.getName();
    String password = mSettings.getPassword();
    if (StringUtils.isEmpty(name) || (StringUtils.isEmpty(password))) {

      JOptionPane.showMessageDialog(mPlugin.getParentFrameForTVRater(), mLocalizer.msg("noUser",
          "Please Enter your Userdata in the\nconfiguration of this Plugin"), mLocalizer.msg("error",
          "Error while updating TV Rater"), JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      if (!NetworkUtilities.checkConnection(new URL("http://www.tvaddicted.de"))) {
        JOptionPane.showMessageDialog(null,
            mLocalizer.msg("noConnectionMessage", "No Connection!"),
            mLocalizer.msg("noConnectionTitle", "No Connection!"),
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      mUpdateList = createUpdateList();
      if (mUpdateList.size() == 0) {
        mWasSuccessfull = true;
        return;
      }

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

        JOptionPane.showMessageDialog(mPlugin.getParentFrameForTVRater(), mLocalizer.msg("serverError",
            "The Server has send the following error:")
            + "\n" + message, mLocalizer.msg("error", "Error while updating TV Rater"), JOptionPane.ERROR_MESSAGE);
      } else {
        readData(data);
        mWasSuccessfull = true;
        mPlugin.updateCurrentDate();
      }

      out.close();
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("updateError", "An error occured while updating the TVRater Database"), e);
      e.printStackTrace();
    }
  }

  /**
   * Was the update successfull?
   *
   * @return Successfully updated ?
   */
  public boolean wasSuccessfull() {
    return mWasSuccessfull;
  }

  /**
   * Gets the Text within a Node
   *
   * @param data Node to rip the Text from
   * @return Text in the Node
   */
  private String getTextFromNode(Node data) {
    Node child = data.getFirstChild();
    StringBuilder text = new StringBuilder();

    while (child != null) {

      if (child.getNodeType() == Node.TEXT_NODE) {
        text.append(child.getNodeValue());
      }

      child = child.getNextSibling();
    }

    return text.toString();
  }

  /**
   * Reads the String returned by the PHP-Skript and parses the DOM
   *
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
   *
   * @param node Node to analyse
   */
  private void readRatingData(Node node) {
    mPlugin.getDatabase().clearServer();
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
   *
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
        int overall = Integer.parseInt(getNodeValue(child));
        rating.setOverallRating(overall);
      } else if (nodename.equals("action")) {
        int action = Integer.parseInt(getNodeValue(child));
        rating.setActionRating(action);
      } else if (nodename.equals("entitlement")) {
        int entitlement = Integer.parseInt(getNodeValue(child));
        rating.setEntitlementRating(entitlement);
      } else if (nodename.equals("fun")) {
        int fun = Integer.parseInt(getNodeValue(child));
        rating.setFunRating(fun);
      } else if (nodename.equals("tension")) {
        int tension = Integer.parseInt(getNodeValue(child));
        rating.setTensionRating(tension);
      } else if (nodename.equals("erotic")) {
        int erotic = Integer.parseInt(getNodeValue(child));
        rating.setEroticRating(erotic);
      } else if (nodename.equals("count")) {
        int userCount = Integer.parseInt(getNodeValue(child));
        rating.setUserCount(userCount);
      } else if (nodename.equals("genre")) {
        int genre = Integer.parseInt(getNodeValue(child));
        rating.setGenre(genre);
      } else if (nodename.equals("id")) {
        int onlineID = Integer.parseInt(getNodeValue(child));
        rating.setOnlineID(onlineID);

        if (rating.getTitle() != null) {
          Rating personal = mPlugin.getDatabase().getPersonalRating(rating.getTitle());

          if (personal != null) {
            personal.setOnlineID(onlineID);
          }
        } else {
          System.out.println("No Title");
        }
      }

      child = child.getNextSibling();
    }

    mPlugin.getDatabase().setServerRating(rating);
  }

  /**
   * Returns the Text-Value in this Node
   *
   * @param node get Text from this Node
   * @return Text in this Node
   */
  private String getNodeValue(Node node) {
    StringBuilder value = new StringBuilder();

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
   *
   * @param output the Outputstream
   * @throws ParserConfigurationException
   * @throws IOException
   */
  private void writeData(OutputStream output) throws ParserConfigurationException, IOException {
    Document document;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();
    Element tvrater = document.createElement("tvrater");
    document.appendChild(tvrater);

    // User
    Element user = document.createElement("user");

    Element name = createNodeWithTextValue(document, "name", mSettings.getName());
    user.appendChild(name);

    Element password = createNodeWithTextValue(document, "password", IOUtilities.xorEncode(mSettings
        .getPassword(), 21));
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

    ArrayList<Rating> list = mPlugin.getDatabase().getChangedPersonal();
    for (int i = 0; i < list.size(); i++) {
      Element ratingElement = document.createElement("rating");
      setratings.appendChild(ratingElement);

      Rating rating = list.get(i);
      ratingElement.appendChild(createNodeWithTextValue(document, "title", rating.getTitle()));

      ratingElement.appendChild(createNodeWithTextValue(document, "overall", rating.getOverallRating()));
      ratingElement.appendChild(createNodeWithTextValue(document, "action", rating.getActionRating()));
      ratingElement
          .appendChild(createNodeWithTextValue(document, "entitlement", rating.getEntitlementRating()));
      ratingElement.appendChild(createNodeWithTextValue(document, "fun", rating.getFunRating()));
      ratingElement.appendChild(createNodeWithTextValue(document, "tension", rating.getTensionRating()));
      ratingElement.appendChild(createNodeWithTextValue(document, "erotic", rating.getEroticRating()));

      ratingElement.appendChild(createNodeWithTextValue(document, "genre", rating.getGenre()));

    }
    mPlugin.getDatabase().clearChangedPersonal();

    // GetRatings
    Element getratings = document.createElement("getratings");
    data.appendChild(getratings);

    Enumeration<Program> en = mUpdateList.elements();
    while (en.hasMoreElements()) {
      Element program = document.createElement("program");
      getratings.appendChild(program);

      Program prog = en.nextElement();

      program.appendChild(createNodeWithTextValue(document, "title", prog.getTitle()));

    }

    XMLWriter writer = new XMLWriter();
    writer.writeDocumentToOutputStream(document, output, "UTF-8");
  }

  /**
   * Creates a Node with a filled TextNode
   *
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
   *
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
   *
   * @param uc Connection
   * @return Data returned from the URL
   * @throws Exception IOException etc...
   */
  private static Node readURLConnection(URLConnection uc) throws Exception {
    Document document;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      // System.out.println(new BufferedInputStream(uc.getInputStream()).);

      /*
       * DataInputStream dis = new DataInputStream (uc.getInputStream()); String
       * line; try { do { line = dis.readLine(); System.out.println(line);
       * }while (line != null); } catch (IOException e) { line = "0";}
       */
      document = builder.parse(IOUtilities.openSaveGZipInputStream(uc.getInputStream()));
    } catch (Exception e) {
      throw e;
    }
    return document.getDocumentElement();
  }

  /**
   * Runs thru all Days and Channels, creates a List of Programs that need to
   * get a rating
   *
   * @return Hashtable filled with Programs to rate
   */
  private Hashtable<String, Program> createUpdateList() {
    Hashtable<String, Program> table = new Hashtable<String, Program>();

    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

    Date date = new Date();
    date = date.addDays(-1);
    for (int d = 0; d < 32; d++) {
      for (Channel channel : channels) {
        for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel); it.hasNext();) {
          Program program = it.next();
          if ((program != null) && mPlugin.isProgramRateable(program)) {
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