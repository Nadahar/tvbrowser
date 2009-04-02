package wirschauenplugin;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This Class implements a Sax Handler that parses the Data from
 * WirSchauen.de and creates a HashMap for the data.
 */
public class WirSchauenHandler extends DefaultHandler {
  private StringBuilder mCharacters = new StringBuilder();
  private HashMap<String, String> mData = new HashMap<String, String>();

  public WirSchauenHandler() {
  }

  @Override
  public void startElement(final String uri, final String localName,
      final String qName, final Attributes attributes) throws SAXException {
      mCharacters = new StringBuilder();
  }

  @Override
  public void endElement(final String uri, final String localName,
      final String qName) throws SAXException {
    mData.put(qName, mCharacters.toString());
  }

  @Override
  public void characters(final char ch[], final int start, final int length)
      throws SAXException {
      mCharacters.append(ch, start, length);
  }

  public HashMap<String, String> getData() {
    return mData;
  }

}
