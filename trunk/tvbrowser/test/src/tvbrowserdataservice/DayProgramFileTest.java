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
package tvbrowserdataservice;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;
import devplugin.ProgramFieldType;

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
      testBinary[i] = (byte) RandomUtils.nextInt(256);
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
    String testString = "This is a test with \u00fcml\u00e4\u00fcts";
    field.setTextData(testString);
    String readString = field.getTextData();
    assertEquals(testString, readString);
  }


  public void testTrimTextField() {
    ProgramField field = new ProgramField();
    String testString = " Test with blanks ";
    field.setTextData(testString);
    String readString = field.getTextData();
    assertNotNull(readString);
    assertFalse(readString.startsWith(" "));
    assertFalse(testString.equalsIgnoreCase(readString));
  }


  public void testIntField() {
    ProgramField field = new ProgramField();

    // Test int field
    int testInt = RandomUtils.nextInt(Integer.MAX_VALUE);
    field.setIntData(testInt);
    int readInt = field.getIntData();
    assertEquals(testInt, readInt);
  }



  public void testNegativeIntField() {
    ProgramField field = new ProgramField();

    // Test int field
    int testInt = (int) (Math.random() * Integer.MIN_VALUE);
    field.setIntData(testInt);
    int readInt = field.getIntData();
    assertEquals(testInt, readInt);
  }



  public void testTimeField() {
    ProgramField field = new ProgramField();

    // Test time field
    int testTime = RandomUtils.nextInt(Integer.MAX_VALUE);
    field.setTimeData(testTime);
    int readTime = field.getTimeData();
    assertEquals(testTime, readTime);
  }



  public void testSaveAndLoad() throws IOException, FileFormatException {
    File file = null;
    try {
      file = File.createTempFile("tvbrowser", ".test.prog.gz");

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



  public void testMerge() throws FileFormatException {
    // Create the first file
    DayProgramFile file1 = new DayProgramFile();

    file1.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file1.addProgramFrame(createProgramFrame(3, 1050, 1100, "Spitzensendung"));
    file1.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));

    // Create the second file
    DayProgramFile file2 = new DayProgramFile();

    file2.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spitzensendung"));

    // Add a frame with the same id, but with an extra field
    ProgramFrame frame = new ProgramFrame(3);
    frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, "Beschreibung"));
    file2.addProgramFrame(frame);

    file2.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Merge the files
    file1.merge(file2);

    // Create the expected file
    DayProgramFile file3 = new DayProgramFile();

    file3.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file3.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spitzensendung"));

    frame = createProgramFrame(3, 1050, 1100, "Spitzensendung");
    frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, "Beschreibung"));
    file3.addProgramFrame(frame);

    file3.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));
    file3.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Compare the result
    assertFilesAreEqual(file1, file3);
  }



  public void testUpdateCompleteFile() throws FileFormatException {
    // Create the complete file
    DayProgramFile file1 = new DayProgramFile();

    file1.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file1.addProgramFrame(createProgramFrame(3, 1050, 1100, "Spitzensendung"));
    file1.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));

    // Create the update file
    DayProgramFile file2 = new DayProgramFile();
    file2.setVersion(2);

    // Add two programs
    file2.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spitzensendung"));
    file2.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Remove a program
    file2.addProgramFrame(new ProgramFrame(5));

    // Change a field
    ProgramFrame frame = new ProgramFrame(3);
    frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, "Super-Spitzensendung"));
    file2.addProgramFrame(frame);

    // Update the complete file
    file1.updateCompleteFile(file2);

    // Create the expected file
    DayProgramFile file3 = new DayProgramFile();
    file3.setVersion(2);

    file3.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file3.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spitzensendung"));
    file3.addProgramFrame(createProgramFrame(3, 1050, 1100, "Super-Spitzensendung"));
    file3.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Compare the result
    assertFilesAreEqual(file1, file3);
  }



  public void testUpdateUpdateFile() throws FileFormatException {
    // Create the first update file
    DayProgramFile file1 = new DayProgramFile();

    file1.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file1.addProgramFrame(new ProgramFrame(3));
    file1.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));

    // Create the second update file
    DayProgramFile file2 = new DayProgramFile();
    file2.setVersion(2);

    // Add a frame
    file2.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Delete a frame that is in the first update file
    file2.addProgramFrame(new ProgramFrame(5));

    // Delete a frame that is not in the first update file
    file2.addProgramFrame(new ProgramFrame(4));

    // Change a field
    ProgramFrame frame = new ProgramFrame(1);
    frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, "Super-Spitzensendung"));
    file2.addProgramFrame(frame);

    // Update the update file
    file1.updateUpdateFile(file2);

    // Create the expected file
    DayProgramFile file3 = new DayProgramFile();
    file3.setVersion(2);

    file3.addProgramFrame(createProgramFrame(1, 1024, 1050, "Super-Spitzensendung"));
    file3.addProgramFrame(new ProgramFrame(3));
    file3.addProgramFrame(new ProgramFrame(4));
    file3.addProgramFrame(new ProgramFrame(5));
    file3.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));

    // Compare the result
    assertFilesAreEqual(file1, file3);
  }



  private void assertFilesAreEqual(DayProgramFile file1, DayProgramFile file2) {
    assertEquals(file1.getVersion(), file2.getVersion());
    assertEquals(file1.getProgramFrameCount(), file2.getProgramFrameCount());

    // Check all the frames
    for (int frame1Idx = 0; frame1Idx < file1.getProgramFrameCount(); frame1Idx++) {
      // Get the frame from file 1
      ProgramFrame frame1 = file1.getProgramFrameAt(frame1Idx);

      // Get the frame from file 2
      int frame2Idx = file2.getProgramFrameIndexForId(frame1.getId());
      ProgramFrame frame2 = file2.getProgramFrameAt(frame2Idx);
      assertNotNull(frame2);

      // Check the frames
      assertFramesAreEqual(frame1, frame2);
    }
  }


  private void assertFramesAreEqual(ProgramFrame frame1, ProgramFrame frame2) {
    assertEquals(frame1.getProgramFieldCount(), frame2.getProgramFieldCount());

    // Check all the fields
    for (int field1Idx = 0; field1Idx < frame1.getProgramFieldCount(); field1Idx++) {
      // Get the field from frame 1
      ProgramField field1 = frame1.getProgramFieldAt(field1Idx);

      // Get the field from frame 2
      ProgramField field2 = frame2.getProgramFieldOfType(field1.getType());
      assertNotNull(field2);

      // Check the fields
      assertFieldsAreEqual(field1, field2);
    }
  }


  private void assertFieldsAreEqual(ProgramField field1, ProgramField field2) {
    if (field1.getTypeId() != field2.getTypeId()) {
      fail("Fields have different type: " + field1.getType().getName() + " != "
          + field2.getType().getName());
    }

    byte[] data1 = field1.getBinaryData();
    byte[] data2 = field2.getBinaryData();

    if (! Arrays.equals(data1, data2)) {
      fail("Values of field " + field1.getType().getName() + " are different: '"
          + field1.getTextData() + "' != '" + field2.getTextData() + "'");
    }
  }


  private DayProgramFile createTestDayProgramFile() {
    DayProgramFile file = new DayProgramFile();

    file.setVersion(4);

    file.addProgramFrame(createProgramFrame(1, 1024, 1050, "Supersendung"));
    file.addProgramFrame(createProgramFrame(2, 1050, 1100, "Spitzensendung"));
    file.addProgramFrame(createProgramFrame(5, 1212, 1400, "Laaangweilig"));
    file.addProgramFrame(createProgramFrame(6, 1100, 1212, "Nachrichten"));
    file.addProgramFrame(createProgramFrame(7, 1400, 1650, "S\u00e4nd\u00fcng m\u00fct \u00dcml\u00e4\u00fct\u00f6n"));
    file.addProgramFrame(createProgramFrame(8, 1650, 1700, "Sendeende"));

    return file;
  }



  public static ProgramFrame createProgramFrame(int id, int startTime,
    int endTime, String title)
  {
    ProgramFrame frame = new ProgramFrame(id);

    frame.addProgramField(ProgramField.create(ProgramFieldType.START_TIME_TYPE, startTime));
    frame.addProgramField(ProgramField.create(ProgramFieldType.END_TIME_TYPE, endTime));
    frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, title));

    return frame;
  }

}
