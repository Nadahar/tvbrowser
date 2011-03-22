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

  /**
   * unknown field format, should not occur
   */
  public static final int UNKNOWN_FORMAT = 1;
  /**
   * @deprecated since 3.0, use {@link #UNKNOWN_FORMAT} instead
   */
  @Deprecated
  public static final int UNKOWN_FORMAT = UNKNOWN_FORMAT;
  /**
   * program field format for binary fields (like pictures)
   */
  public static final int BINARY_FORMAT = 2;
  /**
   * program field format for strings
   */
  public static final int TEXT_FORMAT = 3;
  /**
   * program field format for numbers
   */
  public static final int INT_FORMAT = 4;
  /**
   * program field format for times (in numbers after midnight)
   */
  public static final int TIME_FORMAT = 5;

  /**
   * number of Object fields (TEXT and BINARY format)
   */
  private static final int OBJECT_FIELDS_COUNT = 27;

  /**
   * number of int fields (INT and TIME format)
   */
  private static final int INT_FIELDS_COUNT = 12;

  /**
   * sanity check to control that no int storage index is used twice
   */
  private static final boolean[] usedIntField = new boolean[INT_FIELDS_COUNT];

  /**
   * sanity check to control that no object storage index is used twice
   */
  private static final boolean[] usedObjectField = new boolean[OBJECT_FIELDS_COUNT];


  public static final ProgramFieldType START_TIME_TYPE
    = new ProgramFieldType(1, TIME_FORMAT, true, "start time",
                           "startTime", "Start time", 0);

  public static final ProgramFieldType END_TIME_TYPE
    = new ProgramFieldType(2, TIME_FORMAT, true, "end time",
                           "endTime", "End time", 1);

  /**
   * title
   */
  public static final ProgramFieldType TITLE_TYPE
    = new ProgramFieldType(3, TEXT_FORMAT, true, "title",
                           "title", "Title", 0);

  /**
   * original language title
   */
  public static final ProgramFieldType ORIGINAL_TITLE_TYPE
    = new ProgramFieldType(4, TEXT_FORMAT, true, "original title",
                           "originalTitle", "Original title", 1);

  /**
   * episode title
   */
  public static final ProgramFieldType EPISODE_TYPE
    = new ProgramFieldType(5, TEXT_FORMAT, true, "episode",
                           "episode", "Episode", 2);

  /**
   * original language episode title
   */
  public static final ProgramFieldType ORIGINAL_EPISODE_TYPE
    = new ProgramFieldType(6, TEXT_FORMAT, true, "original episode",
                           "originalEpisode", "Original episode", 3);

  public static final ProgramFieldType SHORT_DESCRIPTION_TYPE
    = new ProgramFieldType(7, TEXT_FORMAT, true, "short description",
                           "shortDescription", "Short description", 4);

  public static final ProgramFieldType DESCRIPTION_TYPE
    = new ProgramFieldType(8, TEXT_FORMAT, true, "description",
                           "description", "Description", 5);

  /**
   * Actor-List. Should be in this Format:
   *
   * ActorName\t\t-\t\tRole,\n
   * ActorName2\t\t-\t\tRole2\n
   *
   */
  public static final ProgramFieldType ACTOR_LIST_TYPE
    = new ProgramFieldType(10, TEXT_FORMAT, true, "actor list",
                           "actors", "Actors", 6);

  public static final ProgramFieldType DIRECTOR_TYPE
    = new ProgramFieldType(11, TEXT_FORMAT, true, "director",
                           "director", "Director", 7);

  public static final ProgramFieldType CUSTOM_TYPE
  = new ProgramFieldType(12, TEXT_FORMAT, true, "custom field",
                         "custom", "Custom information", 8);

  public static final ProgramFieldType INFO_TYPE
    = new ProgramFieldType(13, INT_FORMAT, true, "info bits",
                           "formatInfo", "Format information", 2);

  public static final ProgramFieldType AGE_LIMIT_TYPE
    = new ProgramFieldType(14, INT_FORMAT, true, "age limit",
                           "ageLimit", "Age limit", 3);

  public static final ProgramFieldType URL_TYPE
    = new ProgramFieldType(15, TEXT_FORMAT, true, "film URL",
                           "filmUrl", "Website", 9);

  public static final ProgramFieldType GENRE_TYPE
    = new ProgramFieldType(16, TEXT_FORMAT, true, "genre",
                           "genre", "Genre", 10);

  public static final ProgramFieldType ORIGIN_TYPE
    = new ProgramFieldType(17, TEXT_FORMAT, true, "origin",
                           "origin", "Origin", 11);

  /**
   * net playing time in minutes (may be shorter than end time-start time
   * due to commercials in between)
   */
  public static final ProgramFieldType NET_PLAYING_TIME_TYPE
    = new ProgramFieldType(18, INT_FORMAT, true, "net playing time",
                           "netPlayingTime", "Net playing time", 4);

  public static final ProgramFieldType VPS_TYPE
    = new ProgramFieldType(19, TIME_FORMAT, true, "vps",
                           "vps", "VPS", 5);

  public static final ProgramFieldType SCRIPT_TYPE
    = new ProgramFieldType(20, TEXT_FORMAT, true, "script",
                           "script", "Script", 12);

  /**
   * program is a repetition of this past date/time
   */
  public static final ProgramFieldType REPETITION_OF_TYPE
    = new ProgramFieldType(21, TEXT_FORMAT, true, "repetition of",
                           "repetitionOf", "Repetition of", 13);

  public static final ProgramFieldType MUSIC_TYPE
    = new ProgramFieldType(22, TEXT_FORMAT, true, "music",
                           "music", "Music", 14);

  public static final ProgramFieldType MODERATION_TYPE
    = new ProgramFieldType(23, TEXT_FORMAT, true, "moderation",
                           "moderation", "Moderation", 15);

  public static final ProgramFieldType PRODUCTION_YEAR_TYPE
    = new ProgramFieldType(24, INT_FORMAT, true, "production year",
                           "productionYear", "Production year", 6);

  /**
   * program will be repeated at this future date/time
   */
  public static final ProgramFieldType REPETITION_ON_TYPE
      = new ProgramFieldType(25, TEXT_FORMAT, true, "repetition on",
                           "repetitionOn", "Repetition on", 16);

  public static final ProgramFieldType PICTURE_TYPE
      = new ProgramFieldType(26, BINARY_FORMAT, true, "picture",
                            "picture", "Picture", 17);

  public static final ProgramFieldType PICTURE_COPYRIGHT_TYPE
       = new ProgramFieldType(27, TEXT_FORMAT, true, "picture copyright",
                              "pictureCopyright", "Picture copyright", 18);

  public static final ProgramFieldType PICTURE_DESCRIPTION_TYPE
       = new ProgramFieldType(28, TEXT_FORMAT, true, "picture description",
                              "pictureDescription", "Picture description", 19);

  /**
   * number of this episode
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType EPISODE_NUMBER_TYPE
       = new ProgramFieldType(29, INT_FORMAT, true, "number of episode",
                              "episodeNumber", "Number of Episode", 7);

  /**
   * total number of episodes
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType EPISODE_TOTAL_NUMBER_TYPE
       = new ProgramFieldType(30, INT_FORMAT, true, "total number of episodes",
                              "episodeNumberTotal", "Total number of Episodes", 8);

  /**
   * number of season
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType SEASON_NUMBER_TYPE
       = new ProgramFieldType(31, INT_FORMAT, true, "number of season",
                              "seasonNumber", "Number of Season", 9);

  /**
   * Producer, <b>this is not the producing company</b>
   *
   * @since 2.6/2.2.4
   * @see #PRODUCTION_COMPANY_TYPE
   */
  public static final ProgramFieldType PRODUCER_TYPE
       = new ProgramFieldType(32, TEXT_FORMAT, true, "producer",
                              "producer", "Producer", 20);

  /**
   * Camera
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType CAMERA_TYPE
       = new ProgramFieldType(33, TEXT_FORMAT, true, "camera",
                              "camera", "Camera", 21);

  /**
   * Cutter
   *
   * @since 2.6/2.2.4
   */
  public static final ProgramFieldType CUTTER_TYPE
       = new ProgramFieldType(34, TEXT_FORMAT, true, "cutter",
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
       = new ProgramFieldType(35, TEXT_FORMAT, true, "additional persons",
                              "additionalPersons", "additional persons", 23);

  /**
   * Rating for program. Must be between 0-100
   *
   * @since 2.7
   */
  public static final ProgramFieldType RATING_TYPE
       = new ProgramFieldType(36, INT_FORMAT, true, "rating",
                              "rating", "Rating", 10);

  /**
   * Production Company, <b>this is not the producer name</b>
   *
   * @since 2.7
   * @see #PRODUCER_TYPE
   */
  public static final ProgramFieldType PRODUCTION_COMPANY_TYPE
       = new ProgramFieldType(37, TEXT_FORMAT, true, "production company",
                              "productionCompany", "Production company", 24);

  /**
   * Age rating. This is the text version, whereas "age limit" should
   * contain the age in years.
   *
   * @since 3.0
   * @see #AGE_LIMIT_TYPE
   */
  public static final ProgramFieldType AGE_RATING_TYPE
  = new ProgramFieldType(38, TEXT_FORMAT, true, "age rating",
                         "ageRating", "Age rating", 25);

  /**
   * Last production year if a program was produced over several years.
   * First production year has then to be added to PRODUCTION_YEAR_TYPE.
   *
   * @since 3.0
   * @see #PRODUCTION_YEAR_TYPE
   */
  public static final ProgramFieldType LAST_PRODUCTION_YEAR_TYPE
  = new ProgramFieldType(39, INT_FORMAT, true, "last production year",
                         "lastProductionYear", "Last production year", 11);

  /**
   * Background information that does not belong into the description of
   * the program. E.g. star portraits, awards, reception.
   *
   * @since 3.0
   */
  public static final ProgramFieldType ADDITIONAL_INFORMATION_TYPE
  = new ProgramFieldType(40, TEXT_FORMAT, true, "additional information",
                         "additionalInformation", "Additional information", 26);


  private int mTypeId;

  private String mName;

  private String mLocalizedName;

  private String mLocalizerKey, mLocalizerDefaultMsg;

  private int mFormat;

  /**
   * index into the int/Object storage field of MutablePrograms
   */
  private int mStorageIndex;



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

    return new ProgramFieldType(typeId, UNKNOWN_FORMAT, false,
                                "unknown (" + typeId + ")","unknown", "Unknown", -1);
  }


  public static String getFormatName(int format) {
    switch (format) {
      case BINARY_FORMAT: return "binary format";
      case TEXT_FORMAT: return "text format";
      case INT_FORMAT: return "int format";
      case TIME_FORMAT: return "time format";
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

}
