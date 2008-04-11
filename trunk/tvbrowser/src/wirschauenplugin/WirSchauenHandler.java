package wirschauenplugin;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.TvDataUpdateManager;
import tvdataservice.MutableProgram;
import devplugin.Date;
import devplugin.Channel;
import devplugin.Program;

import java.util.HashMap;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Collection;

import dreamboxdataservice.DreamboxDataService;

/**
 * This Class implements a Sax Handler that parses the Data from
 * WirSchauen.de and creates a HashMap for the data.
 */
public class WirSchauenHandler extends DefaultHandler {
  private StringBuffer mCharacters = new StringBuffer();
  private HashMap<String, String> mData = new HashMap<String, String>();

  public WirSchauenHandler() {
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      mCharacters = new StringBuffer();
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    mData.put(qName, mCharacters.toString());
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
      mCharacters.append(ch, start, length);
  }

  public HashMap<String, String> getData() {
    return mData;
  }

}
