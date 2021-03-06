/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import util.ui.Localizer;

/**
 * Contains all the field types of a program.
 *
 * @see Program#getBinaryField(ProgramFieldType)
 * @see Program#getTextField(ProgramFieldType)
 * @see Program#getIntField(ProgramFieldType)
 * @see Program#getTimeField(ProgramFieldType)
 * @author Til Schneider, www.murfman.de
 */
public class ProgramFieldType {

  private static Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramFieldType.class);

  private static final ArrayList<ProgramFieldType> mKnownTypeList = new ArrayList<ProgramFieldType>();
  private static ProgramFieldType[] mKnownTypeArray;
  private static final Comparator<ProgramFieldType> COMPARATOR_LOCAL_NAMES = new Comparator<ProgramFieldType>() {
    @Override
    public int compare(ProgramFieldType o1, ProgramFieldType o2) {
      return o1.getLocalizedName().compareToIgnoreCase(o2.getLocalizedName());
    }
  };
  
  public static final Comparator<ProgramFieldType> getComparatorLocalizedNames() {
    return COMPARATOR_LOCAL_NAMES;
  }

  /**
   * unknown field format, should not occur
   */
  public static final int FORMAT_UNKNOWN = 1;
  /**
   * program field format for binary fields (like pictures)
   */
  public static final int FORMAT_BINARY = 2;
  /**
   * program field format for strings
   */
  public static final int FORMAT_TEXT = 3;
  /**
   * program field format for numbers
   */
  public static final int FORMAT_INT = 4;
  /**
   * program field format for times (in numbers after midnight)
   */
  public static final int FORMAT_TIME = 5;
  
  /**
   * unknown field format, should not occur
   * @deprecated since 3.4.5, use {@link #FORMAT_UNKNOWN}
   */
  public static final int UNKNOWN_FORMAT = FORMAT_UNKNOWN;
  
  /**
   * program field format for binary fields (like pictures)
   * @deprecated since 3.4.5, use {@link #FORMAT_BINARY}
   */
  public static final int BINARY_FORMAT = FORMAT_BINARY;
  /**
   * program field format for strings
   * @deprecated since 3.4.5, use {@link #FORMAT_TEXT}
   */
  public static final int TEXT_FORMAT = FORMAT_TEXT;
  /**
   * program field format for numbers
   * @deprecated since 3.4.5, use {@link #FORMAT_INT}
   */
  public static final int INT_FORMAT = FORMAT_INT;
  /**
   * program field format for times (in numbers after midnight)
   * @deprecated since 3.4.5, use {@link #FORMAT_TIME}
   */
  public static final int TIME_FORMAT = FORMAT_TIME;
  
  /**
   * number of Object fields (TEXT and BINARY format)
   */
  private static final int OBJECT_FIELDS_COUNT = 34;

  /**
   * number of int fields (INT and TIME format)
   */
  private static final int INT_FIELDS_COUNT = 15;

  /**
   * sanity check to control that no int storage index is used twice
   */
  private static final boolean[] usedIntField = new boolean[INT_FIELDS_COUNT];

  /**
   * sanity check to control that no object storage index is used twice
   */
  private static final boolean[] usedObjectField = new boolean[OBJECT_FIELDS_COUNT];


  public static final ProgramFieldType START_TIME_TYPE
    = new ProgramFieldType(1, FORMAT_TIME, true, "start time",
                           "startTime", "Start time", 0);

  public static final ProgramFieldType END_TIME_TYPE
    = new ProgramFieldType(2, FORMAT_TIME, true, "end time",
                           "endTime", "End time", 1);

  /**
   * title
   */
  public static final ProgramFieldType TITLE_TYPE
    = new ProgramFieldType(3, FORMAT_TEXT, true, "title",
                           "title", "Title", 0);

  /**
   * original language title
   */
  public static final ProgramFieldType ORIGINAL_TITLE_TYPE
    = new ProgramFieldType(4, FORMAT_TEXT, true, "original title",
                           "originalTitle", "Original title", 1);

  /**
   * episode title
   */
  public static final ProgramFieldType EPISODE_TYPE
    = new ProgramFieldType(5, FORMAT_TEXT, true, "episode",
                           "episode", "Episode", 2);

  /**
   * original language episode title
   */
  public static final ProgramFieldType ORIGINAL_EPISODE_TYPE
    = new ProgramFieldType(6, FORMAT_TEXT, true, "original episode",
                           "originalEpisode", "Original episode", 3);

  public static final ProgramFieldType SHORT_DESCRIPTION_TYPE
    = new ProgramFieldType(7, FORMAT_TEXT, true, "short description",
                           "shortDescription", "Short description", 4);

  public static final ProgramFieldType DESCRIPTION_TYPE
    = new ProgramFieldType(8, FORMAT_TEXT, true, "description",
                           "description", "Description", 5);
  
  /**
   * Actor-List. Should be in this Format:
   *
   * ActorName\t\t-\t\tRole,\n
   * ActorName2\t\t-\t\tRole2\n
   *
   */
  public static final ProgramFieldType ACTOR_LIST_TYPE
    = new ProgramFieldType(10, FORMAT_TEXT, true, "actor list",
                           "actors", "Actors", 6);

  public static final ProgramFieldType DIRECTOR_TYPE
    = new ProgramFieldType(11, FORMAT_TEXT, true, "director",
                           "director", "Director", 7);

  public static final ProgramFieldType CUSTOM_TYPE
  = new ProgramFieldType(12, FORMAT_TEXT, true, "custom field",
                         "custom", "Custom information", 8);

  public static final ProgramFieldType INFO_TYPE
    = new ProgramFieldType(13, FORMAT_INT, true, "info bits",
                           "formatInfo", "Format information", 2);

  public static final ProgramFieldType AGE_LIMIT_TYPE
    = new ProgramFieldType(14, FORMAT_INT, true, "age limit",
                           "ageLimit", "Age limit", 3);

  public static final ProgramFieldType URL_TYPE
    = new ProgramFieldType(15, FORMAT_TEXT, true, "film URL",
                           "filmUrl", "Website", 9);

  public static final ProgramFieldType GENRE_TYPE
    = new ProgramFieldType(16, FORMAT_TEXT, true, "genre",
                           "genre", "Genre", 10);

  public static final ProgramFieldType ORIGIN_TYPE
    = new ProgramFieldType(17, FORMAT_TEXT, true, "origin",
                           "origin", "Origin", 11);

  /**
   * net playing time in minutes (may be shorter than end time-start time
   * due to commercials in between)
   */
  public static final ProgramFieldType NET_PLAYING_TIME_TYPE
    = new ProgramFieldType(18, FORMAT_INT, true, "net playing time",
                           "netPlayingTime", "Net playing time", 4);

  public static final ProgramFieldType VPS_TYPE
    = new ProgramFieldType(19, FORMAT_TIME, true, "vps",
                           "vps", "VPS", 5);

  public static final ProgramFieldType SCRIPT_TYPE
    = new ProgramFieldType(20, FORMAT_TEXT, true, "script",
                           "script", "Script", 12);

  /**
   * program is a repetition of this past date/time
   */
  public static final ProgramFieldType REPETITION_OF_TYPE
    = new ProgramFieldType(21, FORMAT_TEXT, true, "repetition of",
                           "repetitionOf", "Repetition of", 13);

  public static final ProgramFieldType MUSIC_TYPE
    = new ProgramFieldType(22, FORMAT_TEXT, true, "music",
                           "music", "Music", 33);

  public static final ProgramFieldType MODERATION_TYPE
    = new ProgramFieldType(23, FORMAT_TEXT, true, "moderation",
                           "moderation", "Moderation", 15);

  public static final ProgramFieldType PRODUCTION_YEAR_TYPE
    = new ProgramFieldType(24, FORMAT_INT, true, "production year",
                           "productionYear", "Production year", 6);

  /**
   * program will be repeated at this future date/time
   */
  public static final ProgramFieldType REPETITION_ON_TYPE
      = new ProgramFieldType(25, FORMAT_TEXT, true, "repetition on",
                           "repetitionOn", "Repetition on", 16);

  public static final ProgramFieldType PICTURE_TYPE
      = new ProgramFieldType(26, FORMAT_BINARY, true, "picture",
                            "picture", "Picture", 17);

  public static final ProgramFieldType PICTURE_COPYRIGHT_TYPE
       = new ProgramFieldType(27, FORMAT_TEXT, true, "picture copyright",
                              "pictureCopyright", "Picture copyright", 18);

  public static final ProgramFieldType PICTURE_DESCRIPTION_TYPE
       = new ProgramFieldType(28, FORMAT_TEXT, true, "picture description",
                              "pictureDescription", "Picture description", 19);

  /**
   * number of this episode
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType EPISODE_NUMBER_TYPE
       = new ProgramFieldType(29, FORMAT_INT, true, "number of episode",
                              "episodeNumber", "Number of Episode", 7);

  /**
   * total number of episodes
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType EPISODE_TOTAL_NUMBER_TYPE
       = new ProgramFieldType(30, FORMAT_INT, true, "total number of episodes",
                              "episodeNumberTotal", "Total number of Episodes", 8);

  /**
   * number of season
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType SEASON_NUMBER_TYPE
       = new ProgramFieldType(31, FORMAT_INT, true, "number of season",
                              "seasonNumber", "Number of Season", 9);

  /**
   * Producer, <b>this is not the producing company</b>
   *
   * @since 2.6/2.2.4
   * @see #PRODUCTION_COMPANY_TYPE
   */
  public static final ProgramFieldType PRODUCER_TYPE
       = new ProgramFieldType(32, FORMAT_TEXT, true, "producer",
                              "producer", "Producer", 20);

  /**
   * Camera
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType CAMERA_TYPE
       = new ProgramFieldType(33, FORMAT_TEXT, true, "camera",
                              "camera", "Camera", 21);

  /**
   * Cutter
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType CUTTER_TYPE
       = new ProgramFieldType(34, FORMAT_TEXT, true, "cutter",
                              "cutter", "Cutter", 22);

  /**
   * Additional persons (Stunt men etc.)
   *
   * Should be in this format:
   *
   * Person\t(Role),\n
   * Person2\t(Role)
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType ADDITIONAL_PERSONS_TYPE
       = new ProgramFieldType(35, FORMAT_TEXT, true, "additional persons",
                              "additionalPersons", "additional persons", 23);

  /**
   * Rating for program. Must be between 0-100
   *
   * @since 2.7
   */
  public static final ProgramFieldType RATING_TYPE
       = new ProgramFieldType(36, FORMAT_INT, true, "rating",
                              "rating", "Rating", 10);

  /**
   * Production Company, <b>this is not the producer name</b>
   *
   * @since 2.7
   * @see #PRODUCER_TYPE
   */
  public static final ProgramFieldType PRODUCTION_COMPANY_TYPE
       = new ProgramFieldType(37, FORMAT_TEXT, true, "production company",
                              "productionCompany", "Production company", 24);

  /**
   * Age rating. This is the text version, whereas "age limit" should
   * contain the age in years.
   *
   * @since 3.0
   * @see #AGE_LIMIT_TYPE
   */
  public static final ProgramFieldType AGE_RATING_TYPE
  = new ProgramFieldType(38, FORMAT_TEXT, true, "age rating",
                         "ageRating", "Age rating", 25);

  /**
   * Last production year if a program was produced over several years.
   * First production year has then to be added to #{@link #FIRST_PRODUCTION_YEAR}.
   *
   * @since 3.0
   * @see #FIRST_PRODUCTION_YEAR
   * @see #PRODUCTION_YEAR_TYPE
   */
  public static final ProgramFieldType LAST_PRODUCTION_YEAR_TYPE
  = new ProgramFieldType(39, FORMAT_INT, true, "last production year",
                         "lastProductionYear", "Last year of production", 11);

  /**
   * Background information that does not belong into the description of
   * the program. E.g. star portraits, awards, reception.
   *
   * @since 3.0
   */
  public static final ProgramFieldType ADDITIONAL_INFORMATION_TYPE
  = new ProgramFieldType(40, FORMAT_TEXT, true, "additional information",
                         "additionalInformation", "Additional information", 26);
  
  /**
   * Title of the series (German: Reihe), if the program belongs to one
   *
   * @since 3.2
   */
  public static final ProgramFieldType SERIES_TYPE
  = new ProgramFieldType(41, FORMAT_TEXT, true, "series",
                         "series", "Series", 27);

  public static final ProgramFieldType PART_NUMBER_TYPE
  = new ProgramFieldType(42, FORMAT_INT, true, "part number",
                         "partNumber", "Part number", 12);

  public static final ProgramFieldType PART_NUMBER_TOTAL_TYPE
  = new ProgramFieldType(43, FORMAT_INT, true, "total number of parts",
                         "totalPartNumber", "Total number of parts", 13);
  
  /**
   * An entry with the original duration and
   * the type of the original of the program
   * 
   * @since 3.4.5
   */
  public static final ProgramFieldType ORIGINAL_DURATION 
  = new ProgramFieldType(9, FORMAT_TEXT, true, "original duration",
                       "originalDuration", "Original duration", 28);
  
  /**
   * List with keywords for program
   * 
   * @since 3.4.5
   */
  public static final ProgramFieldType KEYWORD_LIST 
  = new ProgramFieldType(44, FORMAT_TEXT, true, "keyword list",
                       "keywords", "Keywords", 29);

  /**
   * Info about VOD availability
   * 
   * @since 3.4.5
   */
  public static final ProgramFieldType VOD_INFO 
  = new ProgramFieldType(45, FORMAT_TEXT, true, "vodinfo",
                        "vodinfo", "VOD availability", 30);
  
  /**
   * Link to VOD entry
   * 
   * @since 3.4.5
   */
  public static final ProgramFieldType VOD_LINK 
  = new ProgramFieldType(46, FORMAT_TEXT, true, "vodlink",
                       "vodlink", "Link to VOD", 31);
  
  /**
   * List with sub genres.
   * 
   * @since 3.4.5
   */
  public static final ProgramFieldType GENRE_SUB_LIST
  = new ProgramFieldType(47, FORMAT_TEXT, true, "subgenre",
                       "subgenre", "Subgenres", 32);
  
  /**
   * First production year if a program was produced over several years.
   * Last production year has then to be added to {@link #LAST_PRODUCTION_YEAR_TYPE}.
   *
   * @since 3.4.5
   * @see #LAST_PRODUCTION_YEAR_TYPE
   * @see #PRODUCTION_YEAR_TYPE
   */
  public static final ProgramFieldType FIRST_PRODUCTION_YEAR
  = new ProgramFieldType(48, FORMAT_INT, true, "first production year",
                         "firstProductionYear", "First Year of Production", 14);
  
  private int mTypeId;

  private String mName;

  private String mLocalizedName;

  private String mLocalizerKey, mLocalizerDefaultMsg;

  private int mFormat;

  /**
   * index into the int/Object storage field of MutablePrograms
   */
  private int mStorageIndex;
  
  private static boolean mNameWasSet = false;



  /**
   * @param typeId
   * @param name
   */
  private ProgramFieldType(int typeId, int format, boolean isKnownType,
    String name, String localizerKey, String localizerDefaultMsg, int storageIndex)
  {
    mTypeId = typeId;
    mFormat = format;
    mName = name;
    mLocalizedName = null;
    mLocalizerKey = localizerKey;
    mLocalizerDefaultMsg = localizerDefaultMsg;
    mStorageIndex = storageIndex;

    if (isKnownType) {
      mKnownTypeList.add(this);
      int maxTypeId = 0;
      for (int i=0;i<mKnownTypeList.size();i++) {
      	maxTypeId = Math.max(maxTypeId, (mKnownTypeList.get(i)).getTypeId());
      }
      mKnownTypeArray=new ProgramFieldType[maxTypeId+1];
      for (int i=0;i<mKnownTypeList.size();i++) {
        ProgramFieldType type=mKnownTypeList.get(i);
        mKnownTypeArray[type.getTypeId()] = type;
      }
      // check for invalid/duplicate storage index fields
      if (((format == TEXT_FORMAT || format == BINARY_FORMAT) && (storageIndex >= OBJECT_FIELDS_COUNT))
        || ((format == INT_FORMAT || format == TIME_FORMAT) && (storageIndex >= INT_FIELDS_COUNT))) {
        System.err.println("ProgramFieldType " + name + " cannot use storage index "+ storageIndex +".");
        System.err.println("Increase either OBJECT_FIELDS_COUNT or INT_FIELDS_COUNT (depending on the format).");
        System.exit(1);
      }
      if (((format == TEXT_FORMAT || format == BINARY_FORMAT) && (usedObjectField[storageIndex]))
          || ((format == INT_FORMAT || format == TIME_FORMAT) && (usedIntField[storageIndex]))) {
        System.err.println("ProgramFieldType " + name + " cannot use storage index "+ storageIndex +".");
        System.err.println("Another program field already uses that index. Use another number.");
        System.exit(1);
      }
      if (format == TEXT_FORMAT || format == BINARY_FORMAT) {
        usedObjectField[storageIndex] = true;
      }
      if (format == INT_FORMAT || format == TIME_FORMAT) {
        usedIntField[storageIndex] = true;
      }
    }
  }



  public static ProgramFieldType getTypeForId(int typeId) {
    if (typeId< mKnownTypeArray.length) {
      return mKnownTypeArray[typeId];
    }

    return new ProgramFieldType(typeId, FORMAT_UNKNOWN, false,
                                "unknown (" + typeId + ")","unknown", "Unknown", -1);
  }


  public static String getFormatName(int format) {
    switch (format) {
      case FORMAT_BINARY: return "binary format";
      case FORMAT_TEXT: return "text format";
      case FORMAT_INT: return "int format";
      case FORMAT_TIME: return "time format";
      default: return "unknown format";
    }
  }


  public static Iterator<ProgramFieldType> getTypeIterator() {
    return mKnownTypeList.iterator();
  }



  public int getTypeId() {
    return mTypeId;
  }



  public String getName() {
    return mName;
  }


  public String getLocalizedName() {
    if(mLocalizedName == null) {
      mLocalizedName = mLocalizer.msg(mLocalizerKey, mLocalizerDefaultMsg);
    }

    return mLocalizedName;
  }


  public int getFormat() {
    return mFormat;
  }


  public boolean isRightFormat(int format) {
    return (mFormat == format)
      || (format == UNKNOWN_FORMAT) || (mFormat == UNKNOWN_FORMAT);
  }


  public String toString() {
    // We return the localized name here. This way ProgramFieldType objects
    // can be used directly in GUI components like JLists etc.
    return getLocalizedName();
  }


  /**
   * get the index of this field type in the MutablePrograms field storage
   * @return index
   */
  public int getStorageIndex() {
    return mStorageIndex;
  }

  /**
   * for use by MutableProgram implementation only
   * @return field count for int fields
   */
  public static int getIntFieldCount() {
    return INT_FIELDS_COUNT;
  }

  /**
   * for use by MutableProgram implementation only
   * @return field count for Object fields
   */
  public static int getObjectFieldCount() {
    return OBJECT_FIELDS_COUNT;
  }



  public static void resetLocalizer() {
    mLocalizer = Localizer.getLocalizerFor(ProgramFieldType.class);
  }

  /**
   * Sets the name of the custom field type
   * 
   * @param name The name of the free field type
   * @return If the name could be set (the name can only be set once).
   * @since 3.3.3
   */
  public static boolean setLocalizedCustomFieldName(String name) {
    if(!mNameWasSet) {
      CUSTOM_TYPE.mLocalizedName = name;
      mNameWasSet = true;
      return true;
    }
    
    return false;
  }
}
