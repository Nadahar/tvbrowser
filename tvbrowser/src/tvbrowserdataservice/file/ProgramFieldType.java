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
package tvbrowserdataservice.file;

import java.util.ArrayList;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramFieldType {

  private static final ArrayList mKnownTypeList = new ArrayList();
  
  public static final int UNKOWN_FORMAT = 1;
  public static final int BINARY_FORMAT = 2;
  public static final int TEXT_FORMAT = 3;
  public static final int INT_FORMAT = 4;
  public static final int TIME_FORMAT = 5;

  public static final ProgramFieldType START_TIME_TYPE
    = new ProgramFieldType(1, "start time", TIME_FORMAT, true);
                           
  public static final ProgramFieldType END_TIME_TYPE
    = new ProgramFieldType(2, "end time", TIME_FORMAT, true);
                           
  public static final ProgramFieldType TITLE_TYPE
    = new ProgramFieldType(3, "title", TEXT_FORMAT, true);

  public static final ProgramFieldType ORIGINAL_TITLE_TYPE
    = new ProgramFieldType(4, "original title", TEXT_FORMAT, true);
    
  public static final ProgramFieldType EPISODE_TYPE
    = new ProgramFieldType(5, "episode", TEXT_FORMAT, true);
    
  public static final ProgramFieldType ORIGINAL_EPISODE_TYPE
    = new ProgramFieldType(6, "original episode", TEXT_FORMAT, true);
    
  public static final ProgramFieldType SHORT_DESCRIPTION_TYPE
    = new ProgramFieldType(7, "short description", TEXT_FORMAT, true);

  public static final ProgramFieldType DESCRIPTION_TYPE
    = new ProgramFieldType(8, "description", TEXT_FORMAT, true);

  public static final ProgramFieldType IMAGE_TYPE
    = new ProgramFieldType(9, "image", BINARY_FORMAT, true);

  public static final ProgramFieldType ACTOR_LIST_TYPE
    = new ProgramFieldType(10, "actor list", TEXT_FORMAT, true);

  public static final ProgramFieldType DIRECTOR_TYPE
    = new ProgramFieldType(11, "director", TEXT_FORMAT, true);

  public static final ProgramFieldType SHOWVIEW_NR_TYPE
    = new ProgramFieldType(12, "showview number", INT_FORMAT, true);

  public static final ProgramFieldType INFO_TYPE
    = new ProgramFieldType(13, "info bits", INT_FORMAT, true);

  public static final ProgramFieldType AGE_LIMIT_TYPE
    = new ProgramFieldType(14, "age limit", INT_FORMAT, true);

  public static final ProgramFieldType URL_TYPE
    = new ProgramFieldType(15, "film url", TEXT_FORMAT, true);
                           

  private int mTypeId;

  private String mName;

  private int mFormat;



  /**
   * @param typeId
   * @param name
   */
  private ProgramFieldType(int typeId, String name, int format,
    boolean isKnownType)
  {
    mTypeId = typeId;
    mName = name;
    mFormat = format;
    
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
    
    return new ProgramFieldType(typeId, "unknown", UNKOWN_FORMAT, false);
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



  /**
   * @return
   */
  public int getTypeId() {
    return mTypeId;
  }



  /**
   * @return
   */
  public String getName() {
    return mName;
  }
  


  public int getFormat() {
    return mFormat;
  }
  
  
  
  public boolean isRightFormat(int format) {
    return (mFormat == format)
      || (format == UNKOWN_FORMAT) || (mFormat == UNKOWN_FORMAT);
  }
  
}
