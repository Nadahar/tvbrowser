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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import util.io.IOUtilities;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * this class is the connection to the wirschauen backend.
 *
 * @author uzi
 */
public final class WirSchauenConnection
{
  /**
   * base url to wirschauen.
   */
  private static final String BASE_URL = "http://www.wirschauen.de/events/";



  /**
   * util class, so hide the default constructor.
   */
  private WirSchauenConnection()
  {
    super();
  }





  /**
   * loads the wirschauen event for a program. if the program has no entry
   * at wirschauen, this method will return a wirschauen event nonetheless.
   * this event is 'empty', though.
   *
   * @param program the program to load the wirschauen event for
   * @return the wirschauen event or null if wirschauen returns malformed data
   * @throws IOException if the connection failed
   */
  public static WirSchauenEvent getEvent(final Program program) throws IOException
  {
    try
    {
      //build and call the url
      StringBuilder url = new StringBuilder(BASE_URL);
      url.append("findDescription/?");
      url.append("channel=").append(URLEncoder.encode(program.getChannel().getId(), "UTF-8"));
      url.append("&day=").append(program.getDate().getDayOfMonth());
      url.append("&month=").append(program.getDate().getMonth());
      url.append("&year=").append(program.getDate().getYear());
      url.append("&hour=").append(program.getHours());
      url.append("&minute=").append(program.getMinutes());
      url.append("&length=").append(program.getLength());
      url.append("&title=").append(URLEncoder.encode(program.getTitle(), "UTF-8"));

      //wirschauen returns xml, so parse it. WirSchauenEvent itself is the sax handler.
      WirSchauenEvent event = new WirSchauenEvent();
      SAXParserFactory.newInstance().newSAXParser().parse(new URL(url.toString()).openStream(), event);
      return event;
    }
    catch (final UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
    catch (final ParserConfigurationException e)
    {
      e.printStackTrace();
    }
    catch (final SAXException e)
    {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * saves the wirschauen event for the program.
   *
   * @param event the event to save
   * @param program the corresponding program
   * @throws IOException if the connection failed
   */
  public static void saveEvent(final WirSchauenEvent event, final Program program) throws IOException
  {
    //build and call the url
    StringBuilder url = new StringBuilder(BASE_URL).append("addTVBrowserEvent/?");
    url.append("channel=").append(URLEncoder.encode(program.getChannel().getId(), "UTF-8"));
    url.append("&day=").append(program.getDate().getDayOfMonth());
    url.append("&month=").append(program.getDate().getMonth());
    url.append("&year=").append(program.getDate().getYear());
    url.append("&hour=").append(program.getHours());
    url.append("&minute=").append(program.getMinutes());
    url.append("&length=").append(program.getLength());
    url.append("&title=").append(URLEncoder.encode(program.getTitle(), "UTF-8"));
    url.append("&category=").append(event.getCategory());

    final String episodeField = program.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episodeField != null)
    {
      url.append("&episode=").append(URLEncoder.encode(episodeField, "UTF-8"));
    }
    if (event.getOmdbUrl() != null && event.getOmdbUrl().length() > 0)
    {
      url.append("&url=").append(URLEncoder.encode(event.getOmdbUrl(), "UTF-8"));
    }
    if (event.getGenre() != null && event.getGenre().length() > 0)
    {
      url.append("&genre=").append(URLEncoder.encode(event.getGenre(), "UTF-8"));
    }
    if (event.getDesc() != null && event.getDesc().length() > 0)
    {
      url.append("&description=").append(URLEncoder.encode(event.getDesc(), "UTF-8"));
    }
    url.append("&subtitle=").append(URLEncoder.encode(Boolean.toString(event.hasSubtitles()), "UTF-8"));
    url.append("&omu=").append(URLEncoder.encode(Boolean.toString(event.isOmu()), "UTF-8"));
    url.append("&premiere=").append(URLEncoder.encode(Boolean.toString(event.isPremiere()), "UTF-8"));

    //TODO process return code?
    IOUtilities.loadFileFromHttpServer(new URL(url.toString()));
  }
}
