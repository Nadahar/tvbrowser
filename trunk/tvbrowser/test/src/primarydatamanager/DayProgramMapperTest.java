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
 *     $Date: 2008-02-08 20:29:06 +0100 (Fr, 08 Feb 2008) $
 *   $Author: bananeweizen $
 * $Revision: 4256 $
 */
package primarydatamanager;

import junit.framework.TestCase;
import primarydatamanager.DayProgramMapper;
import primarydatamanager.PreparationException;
import tvbrowserdataservice.DayProgramFileTest;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import devplugin.ProgramFieldType;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramMapperTest extends TestCase {
  
  public void testSetIDs() throws PreparationException {
    DayProgramFile rawFile = new DayProgramFile();
    DayProgramFile cmplFile = new DayProgramFile();
    
    ProgramFrame cmplFrame1 = createProgramFrame(1, 10, 20, "Laangweilig");
    ProgramFrame cmplFrame2 = createProgramFrame(4, 21, 30, "Nachrichten");
    ProgramFrame cmplFrame3 = createProgramFrame(3, 41, 50, "Otto III");
    ProgramFrame cmplFrame4 = createProgramFrame(5, 31, 40, "Karl Franzon I");
    ProgramFrame cmplFrame5 = createProgramFrame(8, 51, 70, "Affenhaus");
    cmplFrame5.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, "Super Film!"));
    ProgramFrame cmplFrame6 = createProgramFrame(8, 71, 80, "Rambo III");

    cmplFile.addProgramFrame(cmplFrame1);
    cmplFile.addProgramFrame(cmplFrame2);
    cmplFile.addProgramFrame(cmplFrame3);
    cmplFile.addProgramFrame(cmplFrame4);
    cmplFile.addProgramFrame(cmplFrame5);
    cmplFile.addProgramFrame(cmplFrame6);
    
    // start time changed
    ProgramFrame rawFrame1 = createProgramFrame(-1, 5, 20, "Laangweilig");
    ProgramFrame rawFrame2 = createProgramFrame(-1, 21, 30, "Nachrichten");
    // title changed
    ProgramFrame rawFrame3 = createProgramFrame(-1, 41, 50, "Rambo III");
    ProgramFrame rawFrame4 = createProgramFrame(-1, 31, 40, "Karl Franzon I");
    // all changed but description changed
    ProgramFrame rawFrame5 = createProgramFrame(-1, 80, 90, "Gier-Affenhaus");
    rawFrame5.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, "Super Film!"));
    // all changed
    ProgramFrame rawFrame6 = createProgramFrame(8, 91, 99, "Flugschau");

    // Mixed order    
    rawFile.addProgramFrame(rawFrame2);
    rawFile.addProgramFrame(rawFrame3);
    rawFile.addProgramFrame(rawFrame1);
    rawFile.addProgramFrame(rawFrame6);
    rawFile.addProgramFrame(rawFrame5);
    rawFile.addProgramFrame(rawFrame4);
    
    // Let's run the mapping
    new DayProgramMapper().map(rawFile, cmplFile);
    
    // Now chck the versions
    assertEquals(rawFrame1.getId(), cmplFrame1.getId());
    assertEquals(rawFrame2.getId(), cmplFrame2.getId());
    assertEquals(rawFrame3.getId(), cmplFrame3.getId());
    assertEquals(rawFrame4.getId(), cmplFrame4.getId());
    assertEquals(rawFrame5.getId(), cmplFrame5.getId());
    assertEquals(rawFrame6.getId(), -1);
  }



  private ProgramFrame createProgramFrame(int id, int startTime,
    int endTime, String title)
  {
    return DayProgramFileTest.createProgramFrame(id, startTime, endTime, title);
  }

}
