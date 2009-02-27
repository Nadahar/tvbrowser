package movieawardplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class loads data from a XML file and creates the movie awards
 */
public class MovieDataFactory {
  private static Logger mLog = Logger.getLogger(MovieDataFactory.class.getName());

  public static MovieAward loadMovieDataFromStream(final InputStream stream,
      final MovieAward award) {

    try {
      final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
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

  public static MovieAward loadMovieDataFromStream(final InputStream stream,
      final MovieDatabase database) {
    return loadMovieDataFromStream(stream, new MovieAward(database));
  }


  public static void loadMovieDatabase(final MovieDatabase movieDatabase, final InputStream stream) {
    movieDatabase.clear();

    try {
      final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(new InputSource(stream), new MovieDatabaseHandler(movieDatabase));
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

    mLog.fine("Es wurden daten geladen!");

  }
}
