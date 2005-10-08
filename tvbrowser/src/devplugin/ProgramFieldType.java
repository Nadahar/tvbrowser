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

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramFieldType.class);

  private static final ArrayList mKnownTypeList = new ArrayList();
  
  public static final int UNKOWN_FORMAT = 1;
  public static final int BINARY_FORMAT = 2;
  public static final int TEXT_FORMAT = 3;
  public static final int INT_FORMAT = 4;
  public static final int TIME_FORMAT = 5;

  public static final ProgramFieldType START_TIME_TYPE
    = new ProgramFieldType(1, TIME_FORMAT, true, "start time",
                           mLocalizer.msg("startTime", "Start time"));
                           
  public static final ProgramFieldType END_TIME_TYPE
    = new ProgramFieldType(2, TIME_FORMAT, true, "end time",
                           mLocalizer.msg("endTime", "End time"));
                           
  public static final ProgramFieldType TITLE_TYPE
    = new ProgramFieldType(3, TEXT_FORMAT, true, "title",
                           mLocalizer.msg("title", "Title"));
    
  public static final ProgramFieldType ORIGINAL_TITLE_TYPE
    = new ProgramFieldType(4, TEXT_FORMAT, true, "original title",
                           mLocalizer.msg("originalTitle", "Original title"));
    
  public static final ProgramFieldType EPISODE_TYPE
    = new ProgramFieldType(5, TEXT_FORMAT, true, "episode",
                           mLocalizer.msg("episode", "Episode"));
    
  public static final ProgramFieldType ORIGINAL_EPISODE_TYPE
    = new ProgramFieldType(6, TEXT_FORMAT, true, "original episode",
                           mLocalizer.msg("originalEpisode", "Original episode"));
    
  public static final ProgramFieldType SHORT_DESCRIPTION_TYPE
    = new ProgramFieldType(7, TEXT_FORMAT, true, "short description",
                           mLocalizer.msg("shortDescriprion", "Short description"));

  public static final ProgramFieldType DESCRIPTION_TYPE
    = new ProgramFieldType(8, TEXT_FORMAT, true, "description",
                           mLocalizer.msg("descriprion", "Description"));

  public static final ProgramFieldType IMAGE_TYPE
    = new ProgramFieldType(9, BINARY_FORMAT, true, "image",
                           mLocalizer.msg("image", "Image"));

  public static final ProgramFieldType ACTOR_LIST_TYPE
    = new ProgramFieldType(10, TEXT_FORMAT, true, "actor list",
                           mLocalizer.msg("actors", "Actors"));

  public static final ProgramFieldType DIRECTOR_TYPE
    = new ProgramFieldType(11, TEXT_FORMAT, true, "director",
                           mLocalizer.msg("director", "Director"));

  public static final ProgramFieldType SHOWVIEW_NR_TYPE
    = new ProgramFieldType(12, TEXT_FORMAT, true, "showview number",
                           mLocalizer.msg("showview", "Showview"));

  public static final ProgramFieldType INFO_TYPE
    = new ProgramFieldType(13, INT_FORMAT, true, "info bits",
                           mLocalizer.msg("formatInfo", "Format information"));

  public static final ProgramFieldType AGE_LIMIT_TYPE
    = new ProgramFieldType(14, INT_FORMAT, true, "age limit",
                           mLocalizer.msg("ageLimit", "Age limit"));

  public static final ProgramFieldType URL_TYPE
    = new ProgramFieldType(15, TEXT_FORMAT, true, "film url",
                           mLocalizer.msg("filmUrl", "Website"));
                           
  public static final ProgramFieldType GENRE_TYPE
    = new ProgramFieldType(16, TEXT_FORMAT, true, "genre",
                           mLocalizer.msg("genre", "Genre"));                         

  public static final ProgramFieldType ORIGIN_TYPE
    = new ProgramFieldType(17, TEXT_FORMAT, true, "origin",
                           mLocalizer.msg("origin", "Origin"));

  public static final ProgramFieldType NET_PLAYING_TIME_TYPE
    = new ProgramFieldType(18, INT_FORMAT, true, "net playing time",
                           mLocalizer.msg("netPlayingTime", "Net playing time"));

  public static final ProgramFieldType VPS_TYPE
    = new ProgramFieldType(19, TIME_FORMAT, true, "vps",
                           mLocalizer.msg("vps", "VPS"));

  public static final ProgramFieldType SCRIPT_TYPE
    = new ProgramFieldType(20, TEXT_FORMAT, true, "script",
                           mLocalizer.msg("script", "Script"));

  public static final ProgramFieldType REPETITION_OF_TYPE
    = new ProgramFieldType(21, TEXT_FORMAT, true, "repition of",
                           mLocalizer.msg("repetitionOf", "Repetition of"));
   
  public static final ProgramFieldType MUSIC_TYPE
    = new ProgramFieldType(22, TEXT_FORMAT, true, "music",
                           mLocalizer.msg("music", "Music"));
    
  public static final ProgramFieldType MODERATION_TYPE
    = new ProgramFieldType(23, TEXT_FORMAT, true, "moderation",
                           mLocalizer.msg("moderation", "Moderation"));
    
  public static final ProgramFieldType PRODUCTION_YEAR_TYPE
    = new ProgramFieldType(24, INT_FORMAT, true, "production year",
                           mLocalizer.msg("productionYear", "Production year"));

  public static final ProgramFieldType REPETITION_ON_TYPE
      = new ProgramFieldType(25, TEXT_FORMAT, true, "repition on",
                           mLocalizer.msg("repetitionOn", "Repetition on"));

  private int mTypeId;

  private String mName;

  private String mLocalizedName;

  private int mFormat;



  /**
   * @param typeId
   * @param name
   */
  private ProgramFieldType(int typeId, int format, boolean isKnownType,
    String name, String localizedName)
  {
    mTypeId = typeId;
    mFormat = format;
    mName = name;
    mLocalizedName = localizedName;
    
    if (isKnownType) {
      mKnownTypeList.add(this);
    }
  }
  
  
  
  public static ProgramFieldType getTypeForId(int typeId) {
    for (int i = 0; i < mKnownTypeList.size(); i++) {
      ProgramFieldType type = (ProgramFieldType) mKnownTypeList.get(i);
      
      if (type.getTypeId() == typeId) {
        return type;
      }     
    }
    
    return new ProgramFieldType(typeId, UNKOWN_FORMAT, false,
                                "unknown (" + typeId + ")",
                                mLocalizer.msg("unknown", "Unknown"));
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
  
  
  public static Iterator getTypeIterator() {
    return mKnownTypeList.iterator();
  }


 
  public int getTypeId() {
    return mTypeId;
  }


 
  public String getName() {
    return mName;
  }
  

  public String getLocalizedName() {
    return mLocalizedName;
  }


  public int getFormat() {
    return mFormat;
  }
  
  
  public boolean isRightFormat(int format) {
    return (mFormat == format)
      || (format == UNKOWN_FORMAT) || (mFormat == UNKOWN_FORMAT);
  }
  
  
  public String toString() {
    // We return the localized name here. This way ProgramFieldType objects
    // can be used directly in GUI components like JLists etc.
    return getLocalizedName();
  }
  
}
