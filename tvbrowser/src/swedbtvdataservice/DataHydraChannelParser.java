/*
 * DataHydraChannelParser.java
 *
 * Created on March 5, 2005, 8:15 PM
 */

package swedbtvdataservice;

import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

/**
 * @author pumpkin
 */
class DataHydraChannelParser extends org.xml.sax.helpers.DefaultHandler {

  private Vector<DataHydraChannelContainer> saveIn;
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
   * Creates a new instance of DataHydraChannelParser
   */
  private DataHydraChannelParser(Vector<DataHydraChannelContainer> v) {
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
          name= "";
        }
        if ("base-url".equals(qName)) {
          state = STATUS_CONSTRUCTION_URL;
          url = "";
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
          if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(url)) {
            DataHydraChannelContainer cc = new DataHydraChannelContainer(id, name, url, iconUrl, "");
            saveIn.add(cc);
          }
          state = DataHydraChannelParser.STATUS_WAITING;
        }
        break;
      }
    }
  }

  protected static DataHydraChannelContainer[] parse(InputStream in) throws Exception {
    Vector<DataHydraChannelContainer> v = new Vector<DataHydraChannelContainer>();
    DataHydraChannelParser handler = new DataHydraChannelParser(v);
    SAXParserFactory.newInstance().newSAXParser().parse(in, handler);
    return v.toArray(new DataHydraChannelContainer[v.size()]);
  }

}
