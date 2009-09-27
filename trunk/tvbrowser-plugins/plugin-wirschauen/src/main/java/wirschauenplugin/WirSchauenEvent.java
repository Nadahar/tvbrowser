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
 */
public class WirSchauenEvent extends DefaultHandler
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
   * wirschauen event id.
   */
  private long mEventId;

  /**
   * link to omdb.
   */
  private String mOmdbUrl;

  /**
   * genre of the event.
   */
  private String mGenre;

  /**
   * is this event a tv premiere?
   */
  private boolean mPremiere;

  /**
   * has this event subtitles?
   */
  private boolean mSubtitles;

  /**
   * is this event an 'original with subtitles'?
   */
  private boolean mOmu;

  /**
   * the category (see constants of this class) of this event.
   */
  private byte mCategory;


  /**
   * counter.
   */
  private int mDescCounter;

  /**
   * desc id.
   */
  private long mDescId;

  /**
   * description/abstract.
   */
  private String mDesc;


  /**
   * true, if a event was found, false otherwise.
   */
  private boolean mEventFound;

  /**
   * holds the content of the xml elements.
   */
  private StringBuilder mCharacters;



  /**
   * default contructor.
   */
  public WirSchauenEvent()
  {
    super();
  }


  /**
   * creates a event and sets some properties.
   *
   * @param omdbUrl the link to omdb.
   * @param category the category (see constants in this class)
   */
  public WirSchauenEvent(final String omdbUrl, final byte category)
  {
    this.mOmdbUrl = omdbUrl;
    this.mCategory = category;
  }


  /**
   * creates a event and sets some properties.
   *
   * @param omdbId omdb id of the program.
   * @param category the category (see constants in this class)
   */
  public WirSchauenEvent(final int omdbId, final byte category)
  {
    this(OmdbConnection.MOVIE_URL + omdbId, category);
  }


  /**
   * for debugging.
   *
   * @see java.lang.Object#toString()
   * @return string representation of the event
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("event id: ").append(mEventId).append("\r\n");
    builder.append("omdb url: ").append(mOmdbUrl).append("\r\n");
    builder.append("genre: ").append(mGenre).append("\r\n");
    builder.append("premiere: ").append(mPremiere).append("\r\n");
    builder.append("subtitles: ").append(mSubtitles).append("\r\n");
    builder.append("omu: ").append(mOmu).append("\r\n");
    builder.append("category: ").append(mCategory).append("\r\n");
    builder.append("desc counter: ").append(mCategory).append("\r\n");
    builder.append("desc id: ").append(mDescId).append("\r\n");
    builder.append("desc: ").append(mDesc);
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
  public boolean equals(final WirSchauenEvent event, final boolean changeableFieldsOnly)
  {
    if (!changeableFieldsOnly || event ==  null)
    {
      return equals(event);
    }
    else
    {
      return event.getCategory() == mCategory
            && event.getDesc().equals(mDesc)
            && event.getGenre().equals(mGenre)
            && event.isOmu() == mOmu
            && event.isPremiere() == mPremiere
            && event.hasSubtitles() == mSubtitles;
    }
  }



  /**
   * called by the sax parser. create a new container for the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
  {
    mCharacters = new StringBuilder();
  }

  /**
   * called by the sax parser. copy and save the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(final String uri, final String localName, final String qName) throws SAXException
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
    mEventFound = !"event".equals(qName);

    if ("event_id".equals(qName))
    {
      setEventId(Long.parseLong(getCharacters()));
    }
    else if ("desc_counter".equals(qName))
    {
      setDescCounter(Integer.parseInt(getCharacters()));
    }
    else if ("desc_id".equals(qName))
    {
      setDescId(Long.parseLong(getCharacters()));
    }
    else if ("url".equals(qName))
    {
      setOmdbUrl(getCharacters());
    }
    else if ("genre".equals(qName))
    {
      setGenre(mCharacters.toString());
    }
    else if ("desc".equals(qName))
    {
      setDesc(mCharacters.toString());
    }
    else if ("premiere".equals(qName))
    {
      setPremiere(Boolean.parseBoolean(getCharacters()));
    }
    else if ("subtitle".equals(qName))
    {
      setSubtitles(Boolean.parseBoolean(getCharacters()));
    }
    else if ("omu".equals(qName))
    {
      setOmu(Boolean.parseBoolean(getCharacters()));
    }
    else if ("category".equals(qName))
    {
      setCategory(Byte.parseByte(getCharacters()));
    }
  }

  /**
   * @return the element content
   */
  private String getCharacters()
  {
    return mCharacters.toString().trim();
  }


  /**
   * called by the sax parser. saves the element content.
   *
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters(final char ch[], final int start, final int length) throws SAXException
  {
    mCharacters.append(ch, start, length);
  }




  /**
   * @return the eventId
   */
  public long getEventId()
  {
    return mEventId;
  }
  /**
   * @param eventId the eventId to set
   */
  public void setEventId(final long eventId)
  {
    this.mEventId = eventId;
  }
  /**
   * @return the omdbUrl
   */
  public String getOmdbUrl()
  {
    return mOmdbUrl;
  }
  /**
   * @param omdbUrl the omdbUrl to set
   */
  public void setOmdbUrl(final String omdbUrl)
  {
    if (omdbUrl != null) {
      this.mOmdbUrl = omdbUrl.trim();
    }
    else {
      this.mOmdbUrl = null;
    }
  }
  /**
   * @return the genre
   */
  public String getGenre()
  {
    return mGenre;
  }
  /**
   * @param genre the genre to set
   */
  public void setGenre(final String genre)
  {
    if (genre != null) {
      this.mGenre = genre.trim();
    }
    else {
      this.mGenre = null;
    }
  }
  /**
   * @return the premiere
   */
  public boolean isPremiere()
  {
    return mPremiere;
  }
  /**
   * @param premiere the premiere to set
   */
  public void setPremiere(final boolean premiere)
  {
    this.mPremiere = premiere;
  }
  /**
   * @return the subtitles
   */
  public boolean hasSubtitles()
  {
    return mSubtitles;
  }
  /**
   * @param subtitles the subtitles to set
   */
  public void setSubtitles(final boolean subtitles)
  {
    this.mSubtitles = subtitles;
  }
  /**
   * @return the omu
   */
  public boolean isOmu()
  {
    return mOmu;
  }
  /**
   * @param omu the omu to set
   */
  public void setOmu(final boolean omu)
  {
    this.mOmu = omu;
  }
  /**
   * @return the category
   */
  public byte getCategory()
  {
    return mCategory;
  }
  /**
   * @param category the category to set
   */
  public void setCategory(final byte category)
  {
    this.mCategory = category;
  }
  /**
   * @return the descCounter
   */
  public int getDescCounter()
  {
    return mDescCounter;
  }
  /**
   * @param descCounter the descCounter to set
   */
  public void setDescCounter(final int descCounter)
  {
    this.mDescCounter = descCounter;
  }
  /**
   * @return the descId
   */
  public long getDescId()
  {
    return mDescId;
  }
  /**
   * @param descId the descId to set
   */
  public void setDescId(final long descId)
  {
    this.mDescId = descId;
  }
  /**
   * @return the desc
   */
  public String getDesc()
  {
    return mDesc;
  }
  /**
   * @param desc the desc to set
   */
  public void setDesc(final String desc)
  {
    if (desc != null) {
      this.mDesc = desc.trim();
    }
    else {
      this.mDesc = null;
    }
  }

  /**
   * @return true if the event was found on the server, false otherwise
   */
  public boolean eventFound()
  {
    return mEventFound;
  }
}
