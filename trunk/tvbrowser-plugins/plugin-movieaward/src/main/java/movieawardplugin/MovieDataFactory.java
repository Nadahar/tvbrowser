package movieawardplugin;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class loads data from a xml file and creates the movieawards
 */
public class MovieDataFactory {
  private static Logger mLog = Logger.getLogger(MovieDataFactory.class.getName());

  public static MovieAward loadMovieDataFromStream(final InputStream stream) {
    final MovieAward award = new MovieAward();

    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(new InputSource(stream), new MovieAwardHandler(award));
    } catch (SAXNotRecognizedException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (SAXNotSupportedException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (SAXException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (ParserConfigurationException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    }

    return award;
  }


}
