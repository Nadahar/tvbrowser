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
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * this is the connection object to communicate with the omdb. it uses
 * a HttpClient internally. as long as the object exists it will hold the
 * http session. CAUTION: omdb might kill the session. this class is not
 * aware of those cases.
 *
 * @author uzi
 * @date 06.08.2009
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
     * url to perform a search. apend the url-encoded search params, separated by space (%20).
     */
    public static final String SEARCH_URL = "http://www.omdb.org/search?search%5Btext%5D=";



    /**
     * sets the language of the session to de
     */
    private static final String LANGUAGE_URL_DE = "http://www.omdb.org/session/set_language?language=1556";

    /**
     * sets the language of the session to en
     */
    private static final String LANGUAGE_URL_EN = "http://www.omdb.org/session/set_language?language=1819";

    /**
     * url to save the abscract. %d will be replaced by the movie id.
     */
    private static final String SET_ABSTRACT_URL = "http://www.omdb.org/movie/%d/set_abstract";

    /**
     * url to get the abstract. %d will be replaced by the movie id.
     */
    private static final String GET_ABSTRACT_URL = "http://www.omdb.org/movie/%d/embed_data";

    /**
     * pattern to select the abstract (matching group 1).
     */
    private static final Pattern ABSTRACT_PATTERN = Pattern.compile("(?sm).*?<abstract>(.*?)</abstract>.*?");

    /**
     * this pattern selects the id of the movie (matching group 1) from the omdb movie url.
     */
    private static Pattern ID_PATTERN = Pattern.compile(".*/(\\d*).*");




    /**
     * the http client holds the http session und simplifies the communication.
     */
    private HttpClient httpClient = new HttpClient();

    /**
     * current language. default is en. is used to remember the selected language
     * between subsequent calls. therefore we dont have to set the language on omdb
     * each time. saves some slow http-request-response-cycles.
     */
    private byte currentLanguage = EN;



    /**
     * sets the language of this session. the default language for omdb is english. so every
     * data saved will be used for the english sub-page of the movie. if you want to save
     * german data, eg an abstract, call this method first to set the language to de.
     *
     * the method is blocking, so it returns, as soon as omdb sends the http response but
     * not before.
     *
     * the omdbConnection class remembers the selected language. if this method is called
     * with the language already selected, it returns instantly true.
     *
     * @param language OmdbConnection.EN or OmdbConnection.DE
     * @return true if the response code was 200 (ok), false otherwise or if the language is not supported
     * @throws IOException if the communication with omdb failed
     */
    public boolean setLanguage(byte language)
    throws IOException
    {
        if (language == currentLanguage)
        {
            //the language is already set
            return true;
        }
        GetMethod getMethod;
        if (language == OmdbConnection.DE)
        {
            getMethod = new GetMethod(OmdbConnection.LANGUAGE_URL_DE);
        }
        else if (language == OmdbConnection.EN)
        {
            getMethod = new GetMethod(OmdbConnection.LANGUAGE_URL_EN);
        }
        else
        {
            //unsupported language
            return false;
        }
        int statusCode = httpClient.executeMethod(getMethod);
        return (statusCode == 200);
    }


    /**
     * saves an abstract for a movie. the language is controlled via setLanguage.
     * the abstract is saved with the current selected language (ie on the omdb
     * subpage for that language - no auto-translation involved here ;)).
     *
     * the abstract must not have more than 400 characters.
     *
     * the method is blocking, so it returns, as soon as omdb sends the http response but
     * not before.
     *
     * @param movieId the omdb-id of the movie
     * @param movieAbstract the abstract
     * @return true if the response code was 200 (ok), false otherwise
     * @throws IOException if the communication with omdb failed
     */
    public boolean saveAbstract(long movieId, String movieAbstract)
    throws IOException
    {
        PostMethod postMethod = new PostMethod(String.format(OmdbConnection.SET_ABSTRACT_URL, movieId));
        postMethod.setRequestEntity(new StringRequestEntity("movie%5Babstract%5D=" + URLEncoder.encode(movieAbstract, "UTF-8") + "&commit=Ok", "application/x-www-form-urlencoded; charset=UTF-8", "UTF-8"));

        //omdb need this headers, it wont work without them
        postMethod.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        postMethod.setRequestHeader("X-Prototype-Version", "1.5.0");

        int statusCode = httpClient.executeMethod(postMethod);
        return (statusCode == 200);
    }


    /**
     * just a convinience method wich calls setLanguage and saveAbstract(id, abstract).
     *
     * @param movieId the omdb-id of the movie
     * @param movieAbstract the abstract
     * @param language OmdbConnection.EN or OmdbConnection.DE
     * @return true if both calls returned true, false otherwise
     * @throws IOException if the communication with omdb failed
     */
    public boolean saveAbstract(long movieId, String movieAbstract, byte language)
    throws IOException
    {
        if (!setLanguage(language))
        {
            return false;
        }
        return saveAbstract(movieId, movieAbstract);
    }


    /**
     * loads the abstract for a movie from omdb. the language is controlled via setLanguage.
     *
     * @param movieId the id of the movie
     * @return the abstract or null if no abstract was found
     * @throws IOException if the communication with omdb failed
     */
    public String loadAbstract(long movieId)
    throws IOException
    {
      GetMethod getMethod = new GetMethod(String.format(OmdbConnection.GET_ABSTRACT_URL, movieId));
      int statusCode = httpClient.executeMethod(getMethod);
      if (statusCode == 200)
      {
        //response is utf-8 encoded but response header ist not set. hence http client uses the
        //default encoding iso-8859-1 for getResponseBodyAsString(), wich is wrong.
        Matcher matcher = ABSTRACT_PATTERN.matcher(new String(getMethod.getResponseBody(), "UTF-8"));
        if (matcher.matches())
        {
          String movieAbstract = matcher.group(1);
          if (movieAbstract == null || movieAbstract.equals("") || movieAbstract.equals("no abstract defined") || movieAbstract.equals("Es wurde noch keine Kurzbeschreibung eingegeben"))
          {
            return null;
          }
          else
          {
            return movieAbstract;
          }
        }
      }
      return null;
    }


    /**
     * just a convinience method wich calls setLanguage and loadAbstract(id, abstract).
     *
     * @param movieId the omdb-id of the movie
     * @param language OmdbConnection.EN or OmdbConnection.DE
     * @return the abstract or null if no abstract was found or setLanguage returned false
     * @throws IOException if the communication with omdb failed
     */
    public String loadAbstract(long movieId, byte language)
    throws IOException
    {
        if (!setLanguage(language))
        {
            return null;
        }
        return loadAbstract(movieId);
    }


    /**
     * extracts the movie id from a omdb-url.
     *
     * @param url the movie-url, must not be null
     * @return the movie id or -1, if the extraction failed
     */
    public static int getIdFromUrl(String url)
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
        return currentLanguage;
    }
}
