package captureplugin.drivers.dreambox.connector.cs;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author fishhead
 * 
 */
public class E2ListItemHandler extends DefaultHandler {

  private final String nodeElement;
  private final List<String> mList = new ArrayList<String>();
  private StringBuilder mCharacters;

  /**
   * @param nodeElement
   */
  public E2ListItemHandler(String nodeElement) {
    this.nodeElement = nodeElement;
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    mCharacters.append(ch, start, length);
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    mCharacters = new StringBuilder();
  }

  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {

    if (qName.equals(nodeElement)) {
      String data = E2ListMapHandler.convertToString(mCharacters);
      mList.add(data);
    }
  }

  /**
   * @return List
   */
  public List<String> getList() {
    return mList;
  }

}
