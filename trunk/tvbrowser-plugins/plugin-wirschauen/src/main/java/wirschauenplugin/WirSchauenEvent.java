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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * models a wirschauen event and is the sax handler for parsing the xml
 * from wirschauen into an object.
 *
 * @author uzi
 * @date 30.08.2009
 */
public class WirSchauenEvent
extends DefaultHandler
{
  /**
   * movie category.
   */
  public static final byte CATEGORY_MOVIE = 1;

  /**
   * series category.
   */
  public static final byte CATEGORY_SERIES = 2;

  /**
   * other category.
   */
  public static final byte CATEGORY_OTHER = 3;


  /**
   * wirschauen event id
   */
  private long eventId;

  /**
   * link to omdb
   */
  private String omdbUrl;

  /**
   * genre of the event
   */
  private String genre;

  /**
   * is this event a tv premiere?
   */
  private boolean premiere;

  /**
   * has this event subtitles?
   */
  private boolean subtitles;

  /**
   * is this event an 'original with subtitles'?
   */
  private boolean omu;

  /**
   * the category (see constants of this class) of this event
   */
  private byte category;


  private int descCounter;
  private long descId;
  private String desc;

  private boolean eventFound = false;

  /**
   * holds the content of the xml elements
   */
  private StringBuilder characters;


  public WirSchauenEvent()
  {
    super();
  }

  public WirSchauenEvent(String omdbUrl, byte category)
  {
    this.omdbUrl = omdbUrl;
    this.category = category;
  }

  public WirSchauenEvent(int omdbId, byte category)
  {
    this(OmdbConnection.MOVIE_URL + omdbId, category);
  }


  /**
   * for debugging
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("event id: ").append(eventId).append("\r\n");
    builder.append("omdb url: ").append(omdbUrl).append("\r\n");
    builder.append("genre: ").append(genre).append("\r\n");
    builder.append("premiere: ").append(premiere).append("\r\n");
    builder.append("subtitles: ").append(subtitles).append("\r\n");
    builder.append("omu: ").append(omu).append("\r\n");
    builder.append("category: ").append(category).append("\r\n");
    builder.append("desc counter: ").append(category).append("\r\n");
    builder.append("desc id: ").append(descId).append("\r\n");
    builder.append("desc: ").append(desc);
    return builder.toString();
  }


  /**
   * compares two events. if changeableFieldsOnly = true, it compares category,
   * description, genre, omu, premiere, subtitles. it calls standard equals
   * otherwise.
   *
   * @param event the event to compare to
   * @param changeableFieldsOnly if true, only the mentioned fields are compared.
   * @return true if the events are equal as defined
   */
  public boolean equals(WirSchauenEvent event, boolean changeableFieldsOnly)
  {
    if (!changeableFieldsOnly || event ==  null)
    {
      return equals(event);
    }
    else
    {
      return event.getCategory() == category &&
             event.getDesc().equals(desc) &&
             event.getGenre().equals(genre) &&
             event.isOmu() == omu &&
             event.isPremiere() == premiere &&
             event.hasSubtitles() == subtitles;
    }
  }



  /**
   * called by the sax parser. create a new container for the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
  throws SAXException
  {
      characters = new StringBuilder();
  }

  /**
   * called by the sax parser. copy and save the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(final String uri, final String localName, final String qName)
  throws SAXException
  {
    /*
     * <event>
     *   <event_id>7519</event_id>
     *   <desc_counter>1</desc_counter>
     *   <desc_id>8066</desc_id>
     *   <url>http://www.omdb.org/movie/12394</url>
     *   <genre></genre>
     *   <desc></desc>
     *   <premiere>false</premiere>
     *   <subtitle>false</subtitle>
     *   <omu>false</omu>
     *   <category>2</category>
     * </event>
     */
    //if the event was not on the server, it returns <event/>
    eventFound = !qName.equals("event");

    if (qName.equals("event_id"))
    {
      eventId = Long.parseLong(characters.toString().trim());
    }
    else if (qName.equals("desc_counter"))
    {
      descCounter = Integer.parseInt(characters.toString().trim());
    }
    else if (qName.equals("desc_id"))
    {
      descId = Long.parseLong(characters.toString().trim());
    }
    else if (qName.equals("url"))
    {
      omdbUrl = characters.toString().trim();
    }
    else if (qName.equals("genre"))
    {
      genre = characters.toString().trim();
    }
    else if (qName.equals("desc"))
    {
      desc = characters.toString().trim();
    }
    else if (qName.equals("premiere"))
    {
      premiere = Boolean.parseBoolean(characters.toString().trim());
    }
    else if (qName.equals("subtitle"))
    {
      subtitles = Boolean.parseBoolean(characters.toString().trim());
    }
    else if (qName.equals("omu"))
    {
      omu = Boolean.parseBoolean(characters.toString().trim());
    }
    else if (qName.equals("category"))
    {
      category = Byte.parseByte(characters.toString().trim());
    }
  }


  /**
   * called by the sax parser. saves the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters(final char ch[], final int start, final int length)
  throws SAXException
  {
      characters.append(ch, start, length);
  }




  /**
   * @return the eventId
   */
  public long getEventId()
  {
    return eventId;
  }
  /**
   * @param eventId the eventId to set
   */
  public void setEventId(long eventId)
  {
    this.eventId = eventId;
  }
  /**
   * @return the omdbUrl
   */
  public String getOmdbUrl()
  {
    return omdbUrl;
  }
  /**
   * @param omdbUrl the omdbUrl to set
   */
  public void setOmdbUrl(String omdbUrl)
  {
    this.omdbUrl = omdbUrl;
  }
  /**
   * @return the genre
   */
  public String getGenre()
  {
    return genre;
  }
  /**
   * @param genre the genre to set
   */
  public void setGenre(String genre)
  {
    this.genre = genre;
  }
  /**
   * @return the premiere
   */
  public boolean isPremiere()
  {
    return premiere;
  }
  /**
   * @param premiere the premiere to set
   */
  public void setPremiere(boolean premiere)
  {
    this.premiere = premiere;
  }
  /**
   * @return the subtitles
   */
  public boolean hasSubtitles()
  {
    return subtitles;
  }
  /**
   * @param subtitles the subtitles to set
   */
  public void setSubtitles(boolean subtitles)
  {
    this.subtitles = subtitles;
  }
  /**
   * @return the omu
   */
  public boolean isOmu()
  {
    return omu;
  }
  /**
   * @param omu the omu to set
   */
  public void setOmu(boolean omu)
  {
    this.omu = omu;
  }
  /**
   * @return the category
   */
  public byte getCategory()
  {
    return category;
  }
  /**
   * @param category the category to set
   */
  public void setCategory(byte category)
  {
    this.category = category;
  }
  /**
   * @return the descCounter
   */
  public int getDescCounter()
  {
    return descCounter;
  }
  /**
   * @param descCounter the descCounter to set
   */
  public void setDescCounter(int descCounter)
  {
    this.descCounter = descCounter;
  }
  /**
   * @return the descId
   */
  public long getDescId()
  {
    return descId;
  }
  /**
   * @param descId the descId to set
   */
  public void setDescId(long descId)
  {
    this.descId = descId;
  }
  /**
   * @return the desc
   */
  public String getDesc()
  {
    return desc;
  }
  /**
   * @param desc the desc to set
   */
  public void setDesc(String desc)
  {
    this.desc = desc;
  }

  /**
   * @return true if the event was found on the server, false otherwise
   */
  public boolean eventFound()
  {
    return eventFound;
  }
}
