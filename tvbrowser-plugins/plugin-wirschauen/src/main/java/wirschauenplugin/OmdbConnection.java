/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wirschauenplugin;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.util.EntityUtils;


/**
 * this is the connection object to communicate with the omdb. it uses a
 * HttpClient internally. as long as the object exists it will hold the http
 * session. CAUTION: omdb might kill the session. this class is not aware of
 * those cases.
 *
 * @author uzi
 */
public class OmdbConnection
{
  /**
   * language de. use with setLanguage().
   */
  public static final byte DE = 1;

  /**
   * language en. use with setLanguage().
   */
  public static final byte EN = 2;

  /**
   * base url to the movies. append the id of the movie.
   */
  public static final String MOVIE_URL = "http://www.omdb.org/movie/";

  /**
   * url to perform a search. append the url-encoded search params, separated by
   * space (%20).
   */
  public static final String SEARCH_URL = "http://www.omdb.org/search?search%5Btext%5D=";

  /**
   * sets the language of the session to de.
   */
  private static final String LANGUAGE_URL_DE = "http://www.omdb.org/session/set_language?language=1556";

  /**
   * sets the language of the session to en.
   */
  private static final String LANGUAGE_URL_EN = "http://www.omdb.org/session/set_language?language=1819";

  /**
   * url to save the abstract. %d will be replaced by the movie id.
   */
  private static final String SET_ABSTRACT_URL = "http://www.omdb.org/movie/%d/set_abstract";
  
  /**
   * url to save the rating/vote. %d will be replaced by the movie id.
   */
  private static final String SET_VOTE_URL = "http://www.omdb.org/vote/%d/register_vote?vote_for=movie";

  /**
   * url to get the abstract or other data. %d will be replaced by the movie id.
   */
  private static final String GET_MOVIA_DATA_URL = "http://www.omdb.org/movie/%d/embed_data";

  /**
   * url to login into omdb.
   */
  private static final String LOGIN_URL = "http://www.omdb.org/account/login";

  /**
   * pattern to select the abstract (matching group 1).
   */
  private static final Pattern ABSTRACT_PATTERN = Pattern.compile("(?sm).*?<abstract>(.*?)</abstract>.*?");
  
  /**
   * pattern to select the rating/vote (matching group 1).
   */
  private static final Pattern VOTE_PATTERN = Pattern.compile("(?sm).*?<vote>(.*?)</vote>.*?");

  /**
   * this pattern selects the id of the movie (matching group 1) from the omdb
   * movie url.
   */
  private static final Pattern ID_PATTERN = Pattern.compile(".*/(\\d*).*");



  /**
   * logging for this class.
   */
  private static final Logger LOG = Logger.getLogger(OmdbConnection.class.getName());



  /**
   * the http client holds the http session und simplifies the communication.
   */
  private DefaultHttpClient mHttpClient;

  /**
   * current language. default is en. is used to remember the selected language
   * between subsequent calls. therefore we don't have to set the language on
   * omdb each time. saves some slow http-request-response-cycles.
   */
  private byte mCurrentLanguage = EN;



  /**
   * Creates a new OmdbConnection with the Proxy Settings from the System Properties.
   */
  public OmdbConnection() {
    super();
    mHttpClient = new DefaultHttpClient();
    //the proxy settings for the tvbrowser are saved in the system properties. see tvbrowser.TVBrowser.updateProxySettings().
    //the ProxySelectorRoutePlanner will take these into account.
    ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(mHttpClient.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());
    mHttpClient.setRoutePlanner(routePlanner);
  }


  /**
   * Creates a new OmdbConnection with the given proxy.
   *
   * @param proxy proxy url
   * @param port proxy port
   */
  public OmdbConnection(final String proxy, final int port) {
    super();
    mHttpClient = new DefaultHttpClient();
    mHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxy, port));
  }
  
  
  /**
   * saves a rating for a movie. the rating must be between 1 (crap) and 10 (great).
   *
   * the method is blocking, so it returns, as soon as omdb sends the http response but not before.
   *
   * @param movieId the omdb-id of the movie
   * @param rating the rating to set
   * @return true if the response code was 200 (ok), false otherwise
   * @throws IOException if the communication with omdb failed
   */
  public boolean saveRating(final long movieId, final byte rating) throws IOException
  {
    HttpPost postMethod = new HttpPost(String.format(OmdbConnection.SET_VOTE_URL, movieId));
    postMethod.setEntity(new StringEntity("commit=speichern&vote%5Bvote%5D=" + rating, "UTF-8"));

    // omdb need this headers, it wont work without them
    postMethod.setHeader("X-Requested-With", "XMLHttpRequest");
    postMethod.setHeader("X-Prototype-Version", "1.5.0");

    HttpResponse response = mHttpClient.execute(postMethod);
    boolean result = response.getStatusLine().getStatusCode() == 200;
    postMethod.abort();
    return result;
  }
  
  
  /**
   * returns the average community rating for the movie with the given id. it's 0.0 if not rated at all.
   * 
   * @param movieId the omdb id for the movie
   * @return the rating
   * @throws IOException if anything went wrong
   */
  public float loadRating(final long movieId) throws IOException {
    HttpGet getMethod = new HttpGet(String.format(OmdbConnection.GET_MOVIA_DATA_URL, movieId));
    HttpResponse response = mHttpClient.execute(getMethod);
    String content = null;
    if (response.getStatusLine().getStatusCode() == 200) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        content = EntityUtils.toString(entity);
        // response is utf-8 encoded but response header is not set. hence http client uses the
        // default encoding iso-8859-1 for getResponseBodyAsString(), which is wrong.
        content = new String(content.getBytes("ISO8859-1"), "UTF-8");
        Matcher matcher = VOTE_PATTERN.matcher(content);
        if (matcher.matches()) {
          return Float.parseFloat(matcher.group(1));
        }
      }
    }
    if (content != null) {
      LOG.log(Level.WARNING, "content: " + content);
    }
    getMethod.abort();
    throw new IOException(response.getStatusLine().getReasonPhrase() + ", response code: " + response.getStatusLine().getStatusCode());
  }



  /**
   * this method does a login to omdb. certain actions are only available while logged in (post an abstract for
   * instance).
   *
   * @param login the login to be used
   * @param password the password to be used
   * @return true, if the response code was 302 (yeah, i know - but omdb returns 302 for 'ok' and 200 for 'login faild'... wtf!?)
   * @throws IOException if something went wrong
   */
  public boolean login(final String login, final String password) throws IOException
  {
    HttpPost postMethod = new HttpPost(OmdbConnection.LOGIN_URL);
    postMethod.setEntity(new StringEntity("login=" + URLEncoder.encode(login, "UTF-8")
        + "&password=" + URLEncoder.encode(password, "UTF-8")
        + "&commit=Login", "UTF-8"));

    HttpResponse response = mHttpClient.execute(postMethod);
    //302: login correct, 200: login wrong
    boolean result = response.getStatusLine().getStatusCode() == 302;
    postMethod.abort();
    return result;
  }



  /**
   * sets the language of this session. the default language for omdb is
   * English. so every data saved will be used for the English sub-page of the
   * movie. if you want to save german data, eg an abstract, call this method
   * first to set the language to de.
   *
   * the method is blocking, so it returns, as soon as omdb sends the http
   * response but not before.
   *
   * the omdbConnection class remembers the selected language. if this method is
   * called with the language already selected, it returns instantly true.
   *
   * @param language OmdbConnection.EN or OmdbConnection.DE
   * @return true if the response code was 200 (OK), false otherwise or if the
   *         language is not supported
   * @throws IOException if the communication with omdb failed
   */
  public boolean setLanguage(final byte language) throws IOException
  {
    if (language == mCurrentLanguage)
    {
      // the language is already set
      return true;
    }
    HttpGet getMethod;
    if (language == OmdbConnection.DE)
    {
      getMethod = new HttpGet(OmdbConnection.LANGUAGE_URL_DE);
    }
    else if (language == OmdbConnection.EN)
    {
      getMethod = new HttpGet(OmdbConnection.LANGUAGE_URL_EN);
    }
    else
    {
      // unsupported language
      return false;
    }
    HttpResponse response = mHttpClient.execute(getMethod);
    boolean result = response.getStatusLine().getStatusCode() == 200;
    getMethod.abort();
    return result;
  }



  /**
   * saves an abstract for a movie. the language is controlled via setLanguage.
   * the abstract is saved with the current selected language (ie on the omdb
   * subpage for that language - no auto-translation involved here ;)).
   *
   * the abstract must not have more than 400 characters.
   *
   * the method is blocking, so it returns, as soon as omdb sends the http
   * response but not before.
   *
   * @param movieId the omdb-id of the movie
   * @param movieAbstract the abstract
   * @return true if the response code was 200 (ok), false otherwise
   * @throws IOException if the communication with omdb failed
   */
  public boolean saveAbstract(final long movieId, final String movieAbstract) throws IOException
  {
    HttpPost postMethod = new HttpPost(String.format(OmdbConnection.SET_ABSTRACT_URL, movieId));
    postMethod.setEntity(new StringEntity("movie%5Babstract%5D=" + URLEncoder.encode(movieAbstract, "UTF-8") + "&commit=Ok", "UTF-8"));

    // omdb need this headers, it wont work without them
    postMethod.setHeader("X-Requested-With", "XMLHttpRequest");
    postMethod.setHeader("X-Prototype-Version", "1.5.0");

    HttpResponse response = mHttpClient.execute(postMethod);
    boolean result = response.getStatusLine().getStatusCode() == 200;
    postMethod.abort();
    return result;
  }



  /**
   * just a convenience method which calls setLanguage and saveAbstract(id,
   * abstract).
   *
   * @param movieId the omdb-id of the movie
   * @param movieAbstract the abstract
   * @param language OmdbConnection.EN or OmdbConnection.DE
   * @return true if both calls returned true, false otherwise
   * @throws IOException if the communication with omdb failed
   */
  public boolean saveAbstract(final long movieId, final String movieAbstract, final byte language) throws IOException
  {
    if (!setLanguage(language))
    {
      return false;
    }
    return saveAbstract(movieId, movieAbstract);
  }



  /**
   * loads the abstract for a movie from omdb. the language is controlled via
   * setLanguage.
   *
   * @param movieId the id of the movie
   * @return the abstract or null if no abstract was found
   * @throws IOException if the communication with omdb failed
   */
  public String loadAbstract(final long movieId) throws IOException
  {
    HttpGet getMethod = new HttpGet(String.format(OmdbConnection.GET_MOVIA_DATA_URL, movieId));
    HttpResponse response = mHttpClient.execute(getMethod);
    String content = null;
    if (response.getStatusLine().getStatusCode() == 200)
    {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        content = EntityUtils.toString(entity);
        // response is utf-8 encoded but response header is not set. hence http client uses the
        // default encoding iso-8859-1 for getResponseBodyAsString(), which is wrong.
        content = new String(content.getBytes("ISO8859-1"), "UTF-8");
        Matcher matcher = ABSTRACT_PATTERN.matcher(content);
        if (matcher.matches()) {
          String movieAbstract = matcher.group(1);
          if ("".equals(movieAbstract) || "no abstract defined".equals(movieAbstract)
              || "Es wurde noch keine Kurzbeschreibung eingegeben".equals(movieAbstract)) {
            return null;
          } else {
            return movieAbstract.trim();
          }
        }
      }
    }
    if (content != null) {
      LOG.log(Level.WARNING, "content: " + content);
    }
    getMethod.abort();
    throw new IOException(response.getStatusLine().getReasonPhrase() + ", response code: " + response.getStatusLine().getStatusCode());
  }



  /**
   * just a convenience method which calls setLanguage and loadAbstract(id, abstract).
   *
   * @param movieId the omdb-id of the movie
   * @param language OmdbConnection.EN or OmdbConnection.DE
   * @return the abstract or null if no abstract was found
   * @throws IOException if the communication with omdb failed
   */
  public String loadAbstract(final long movieId, final byte language) throws IOException
  {
    if (!setLanguage(language))
    {
      throw new IOException("Could not set language");
    }
    return loadAbstract(movieId);
  }



  /**
   * extracts the movie id from a omdb-url.
   *
   * @param url the movie-url, must not be null
   * @return the movie id or -1, if the extraction failed
   */
  public static int getIdFromUrl(final String url)
  {
    Matcher matcher = ID_PATTERN.matcher(url);
    if (matcher.matches())
    {
      return Integer.parseInt(matcher.group(1));
    }
    return -1;
  }



  /**
   * @return the currentLanguage
   */
  public byte getCurrentLanguage()
  {
    return mCurrentLanguage;
  }
}
