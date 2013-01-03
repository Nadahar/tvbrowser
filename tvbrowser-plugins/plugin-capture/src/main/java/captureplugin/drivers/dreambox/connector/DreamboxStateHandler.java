package captureplugin.drivers.dreambox.connector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class reads dreambox state xml-data and returns the values
 */
public class DreamboxStateHandler extends DefaultHandler {

  private StringBuilder mCharacters = new StringBuilder();
    private String mState;
    private String mStatetext;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        mCharacters = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        mCharacters.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("e2state".equals(qName)) {
            mState = mCharacters.toString();
        } else if ("e2statetext".equals(qName)) {
            mStatetext = mCharacters.toString();
        }
    }

    /**
     * @return State
     */
    public String getState() {
        return mState;
    }

    /**
     * @return Statetext
     */
    public String getStatetext(){
        return mStatetext;
    }
}
