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
package test.tvbrowserdataservice;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import tvbrowserdataservice.file.*;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFileTest extends TestCase {
  
  public void testBinaryField() {
    ProgramField field = new ProgramField();
    
    // Test binary field
    byte[] testBinary = new byte[100];
    for (int i = 0; i < testBinary.length; i++) {
      testBinary[i] = (byte) (Math.random() * 256);
    }
    field.setBinaryData(testBinary);
    byte[] readBinary = field.getBinaryData();
    
    assertEquals(testBinary.length, readBinary.length);
    for (int i = 0; i < testBinary.length; i++) {
      assertEquals(testBinary[i], readBinary[i]);
    }
  }
  
  
    
  public void testTextField() {
    ProgramField field = new ProgramField();
    
    // Test text field
    String testString = "This is a test with Ümläüts";
    field.setTextData(testString);
    String readString = field.getTextData();
    assertEquals(testString, readString);
  }
  
  
    
  public void testIntField() {
    ProgramField field = new ProgramField();
    
    // Test int field
    int testInt = (int)(Math.random() * Integer.MAX_VALUE);
    field.setIntData(testInt);
    int readInt = field.getIntData();
    assertEquals(testInt, readInt);
  }
  
  
    
  public void testNegativeIntField() {
    ProgramField field = new ProgramField();
    
    // Test int field
    int testInt = (int)(Math.random() * Integer.MIN_VALUE);
    field.setIntData(testInt);
    int readInt = field.getIntData();
    assertEquals(testInt, readInt);
  }
  
  
    
  public void testTimeField() {
    ProgramField field = new ProgramField();
    
    // Test time field
    int testTime = (int)(Math.random() * Integer.MAX_VALUE);
    field.setTimeData(testTime);
    int readTime = field.getTimeData();
    assertEquals(testTime, readTime);
  }



  public void testSaveAndLoad() throws IOException, FileFormatException {
    File file = null;
    try {
      file = File.createTempFile("tvbrowser", ".test.gz");

      DayProgramFile origProgFile = createTestDayProgramFile();
      origProgFile.writeToFile(file);

      // System.out.println("Test file saved: " + file.getAbsolutePath());
      
      DayProgramFile readProgFile = new DayProgramFile();
      readProgFile.readFromFile(file);
    }
    finally {
      if (file != null) {
        file.delete();
      }
    }
  }
  
  
  
  private DayProgramFile createTestDayProgramFile() {
    DayProgramFile file = new DayProgramFile();
    
    file.setVersion(4);
    
    file.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spizensendung"));
    file.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));
    file.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));
    file.addProgramFrame(createProgramFrame(7, 1400, 1650, "Sändüng müt Ümlütén"));
    file.addProgramFrame(createProgramFrame(8, 1650, 1700, "Sendeende"));
    
    return file;
  }



  public static ProgramFrame createProgramFrame(int id, int startTime,
    int endTime, String title)
  {
    ProgramFrame frame = new ProgramFrame(id);
    
    ProgramField field = new ProgramField();
    field.setType(ProgramFieldType.START_TIME_TYPE);
    field.setTimeData(startTime);
    frame.addProgramField(field);

    field = new ProgramField();
    field.setType(ProgramFieldType.END_TIME_TYPE);
    field.setTimeData(endTime);
    frame.addProgramField(field);

    frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, title));
    
    return frame;
  }

}
