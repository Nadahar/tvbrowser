/*
 * DataFoxChannelParser.java
 *
 * Created on March 5, 2005, 8:15 PM
 */

package swedbtvdataservice;

import org.xml.sax.Attributes;

import java.io.InputStream;
import java.util.Vector;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author pumpkin
 */
public class DataFoxChannelParser extends org.xml.sax.helpers.DefaultHandler {

  private Vector<DataFoxChannelContainer> saveIn;
  private final static int STATUS_CONSTRUCTION = 0;
  private final static int STATUS_CONSTRUCTION_NAME = 1;
  private final static int STATUS_CONSTRUCTION_URL = 2;
  @SuppressWarnings("unused")
  private final static int STATUS_ICONURL = 3;
  private final static int STATUS_WAITING = 4;
  private int state = STATUS_WAITING;

  private String name;
  private String url;
  private String id;
  private String iconUrl;

  /**
   * Creates a new instance of DataFoxChannelParser
   */
  private DataFoxChannelParser(Vector<DataFoxChannelContainer> v) {
    saveIn = v;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    switch (state) {
      case (STATUS_WAITING): {
        if ("channel".equals(qName)) {
          state = STATUS_CONSTRUCTION;
          id = attributes.getValue("id");
          name = "";
          url = "";
          iconUrl = "";
        }
        break;
      }
      case (STATUS_CONSTRUCTION): {
        if ("display-name".equals(qName)) {
          state = STATUS_CONSTRUCTION_NAME;
        }
        if ("base-url".equals(qName)) {
          state = STATUS_CONSTRUCTION_URL;
        }
        if ("icon".equals(qName)) {
          iconUrl = attributes.getValue("src");
        }
        break;
      }
    }
  }

  public void characters(char[] ch, int start, int length) {
    switch (state) {
      case (STATUS_CONSTRUCTION_NAME): {
        name = name + new String(ch, start, length);
        break;
      }
      case (STATUS_CONSTRUCTION_URL): {
        url = url + new String(ch, start, length);
        break;
      }
    }
  }

  public void endElement(String uri, String localName, String qName) {
    switch (state) {
      case (STATUS_CONSTRUCTION_NAME): {
        if ("display-name".equals(qName)) {
          state = STATUS_CONSTRUCTION;
        }
        break;
      }
      case (STATUS_CONSTRUCTION_URL): {
        if ("base-url".equals(qName)) {
          state = STATUS_CONSTRUCTION;
        }
        break;
      }
      case (STATUS_CONSTRUCTION): {
        if ("channel".equals(qName)) {
          if ((id.length() != 0) && (name.length() != 0) && (url.length() != 0)) {
            DataFoxChannelContainer cc = new DataFoxChannelContainer(id, name, url, iconUrl, "");
            saveIn.add(cc);
          }
          state = DataFoxChannelParser.STATUS_WAITING;
        }
        break;
      }
    }
  }

  public static DataFoxChannelContainer[] parse(InputStream in) throws Exception {
    Vector<DataFoxChannelContainer> v = new Vector<DataFoxChannelContainer>();
    DataFoxChannelParser handler = new DataFoxChannelParser(v);
    SAXParserFactory.newInstance().newSAXParser().parse(in, handler);
    return v.toArray(new DataFoxChannelContainer[v.size()]);
  }

}
