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
 *     $Date: 2003-11-24 18:22:01 +0100 (Mo, 24 Nov 2003) $
 *   $Author: til132 $
 * $Revision: 276 $
 */
package primarydatamanager;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import devplugin.ProgramFieldType;

/**
 * Utilities class for the primarydatamanager package.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PrimaryDataUtilities {

  public static int getMaxId(DayProgramFile file) {
    int maxId = 1;
    for (int i = 0; i < file.getProgramFrameCount(); i++) {
      int id = file.getProgramFrameAt(i).getId();
      if (id > maxId) {
        maxId = id;
      }
    }
    
    
    return maxId;
  }


  public static int getProgramStartTime(ProgramFrame frame)
    throws PreparationException
  {
    ProgramField field = frame.getProgramFieldOfType(ProgramFieldType.START_TIME_TYPE);
    
    if (field == null) {
      throw new PreparationException("program frame with ID " + frame.getId()
        + " has no start time.");
    } else {
      return field.getIntData();
    }
  }
  
  
  
  public static String getProgramTitle(ProgramFrame frame)
    throws PreparationException
  {
    ProgramField field = frame.getProgramFieldOfType(ProgramFieldType.TITLE_TYPE);
    
    if (field == null) {
      throw new PreparationException("program frame with ID " + frame.getId()
        + " has no title.");
    } else {
      return field.getTextData();
    }
  }

}
