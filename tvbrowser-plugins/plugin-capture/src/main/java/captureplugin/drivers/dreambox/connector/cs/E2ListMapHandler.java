package captureplugin.drivers.dreambox.connector.cs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author fishhead
 * 
 */
public class E2ListMapHandler extends DefaultHandler {

  private final String rootElement;
  private final String nodeElement;
  private final List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
  private Map<String, String> mCurrentHashMap = new TreeMap<String, String>();
  private StringBuilder mCharacters = new StringBuilder();
  private String mKey = "";
  private final String mTrenner = "/";

  /**
   * @param rootElement
   * @param nodeElement
   */
  public E2ListMapHandler(String rootElement, String nodeElement) {
    this.rootElement = rootElement;
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
    if (qName.equals(nodeElement)) {
      mCurrentHashMap = new HashMap<String, String>();
      mList.add(mCurrentHashMap);
      mKey = "";
    } else if (!qName.equals(rootElement)) {
      mKey += (mKey.length() == 0 ? "" : mTrenner) + qName;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (!qName.equals(nodeElement) && !qName.equals(rootElement)) {
      String data = E2ListMapHandler.convertToString(mCharacters);
      if (mCurrentHashMap.containsKey(mKey)) {
        mCurrentHashMap.put(mKey, mCurrentHashMap.get(mKey) + mTrenner + data);
      } else {
        mCurrentHashMap.put(mKey, data);
      }
      if (mKey.length() >= qName.length()) {
        mKey = mKey.substring(0, mKey.length() - qName.length());
      }
      if (mKey.length() >= 1) {
        mKey = mKey.substring(0, mKey.length() - mTrenner.length());
      }
    }
    mCharacters = new StringBuilder();
  }

  /**
   * Konvertiert StringBuilder in einen String, ersetzt dabei bestimmte Zeichen
   * @param sb 
   * @return str
   */
  public static String convertToString(StringBuilder sb) {
    int i = 0;
    while (i < sb.length()) {
      byte b = (byte)sb.charAt(i);
      if ((b == -122) || (b == -121)) {
        // Sonderzeichen entfernen
        sb.deleteCharAt(i);
      } else if (b == -118) {
        // Zeilenumbruch ersetzen
        sb.deleteCharAt(i).insert(i, '\n');
      }        
      i++;
    }
    return sb.toString().trim();
  }

  /**
   * @return List
   */
  public List<Map<String, String>> getList() {
    return mList;
  }

}
