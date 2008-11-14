package movieawardplugin;

import org.apache.xerces.parsers.SAXParser;
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
      SAXParser parser = new SAXParser();
      parser.setContentHandler(new MovieAwardHandler(award));

      // Complete list of features of the xerces parser:
      // http://xml.apache.org/xerces2-j/features.html
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      parser.parse(new InputSource(stream));
    } catch (SAXNotRecognizedException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (SAXNotSupportedException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    } catch (SAXException e) {
      mLog.log(Level.SEVERE, "Could not parse Movie Award", e);
    }

    return award;
  }


}
