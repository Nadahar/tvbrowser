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

import junit.framework.TestCase;
import tvbrowserdataservice.file.SummaryFile;
import util.io.FileFormatException;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class SummaryFileTest extends TestCase {

  public void testSummaryFile()
    throws IOException, FileFormatException
  {
    Date date1 = new Date(2003, 12, 28);
    Date date2 = new Date(2003, 12, 31);
    Date date3 = new Date(2004, 1, 1);
    Date date4 = new Date(2004, 1, 2);
    Date date5 = new Date(2004, 1, 5);
    Date date6 = new Date(2004, 1, 6);
    
    String country = "de";
    
    String channelId1 = "Ch1";
    String channelId2 = "Ch2";
    String channelId3 = "Ch3";
    
    // Create a test file
    SummaryFile summary1 = new SummaryFile();
    
    summary1.setDayProgramVersion(date3, country, channelId1, 0, 1);
    summary1.setDayProgramVersion(date3, country, channelId1, 1, 2);
    summary1.setDayProgramVersion(date3, country, channelId1, 2, 3);
    summary1.setDayProgramVersion(date2, country, channelId1, 0, 4);
    summary1.setDayProgramVersion(date2, country, channelId1, 2, 5);
    summary1.setDayProgramVersion(date4, country, channelId1, 0, 6);
    summary1.setDayProgramVersion(date4, country, channelId1, 1, 7);
    summary1.setDayProgramVersion(date5, country, channelId1, 0, 8);

    summary1.setDayProgramVersion(date4, country, channelId2, 1, 9);
    summary1.setDayProgramVersion(date4, country, channelId2, 2, 10);
    
    // Save and load the file
    File file = null;
    SummaryFile summary2;
    try {
      file = File.createTempFile("tvbrowser", ".test_summary.gz");

      summary1.writeToFile(file);

      // System.out.println("Test file saved: " + file.getAbsolutePath());
      
      summary2 = new SummaryFile();
      summary2.readFromFile(file);
    }
    finally {
      if (file != null) {
        // file.delete();
      }
    }
    
    // Check the set fields
    assertEquals(summary2.getDayProgramVersion(date3, country, channelId1, 0), 1);
    assertEquals(summary2.getDayProgramVersion(date3, country, channelId1, 1), 2);
    assertEquals(summary2.getDayProgramVersion(date3, country, channelId1, 2), 3);
    assertEquals(summary2.getDayProgramVersion(date2, country, channelId1, 0), 4);
    assertEquals(summary2.getDayProgramVersion(date2, country, channelId1, 2), 5);
    assertEquals(summary2.getDayProgramVersion(date4, country, channelId1, 0), 6);
    assertEquals(summary2.getDayProgramVersion(date4, country, channelId1, 1), 7);
    assertEquals(summary2.getDayProgramVersion(date5, country, channelId1, 0), 8);

    assertEquals(summary2.getDayProgramVersion(date4, country, channelId2, 1), 9);
    assertEquals(summary2.getDayProgramVersion(date4, country, channelId2, 2), 10);
    
    // Check unset version
    assertEquals(summary2.getDayProgramVersion(date2, country, channelId1, 1), -1);

    // Check unset days
    assertEquals(summary2.getDayProgramVersion(date1, country, channelId1, 0), -1);
    assertEquals(summary2.getDayProgramVersion(date6, country, channelId1, 2), -1);
    
    // Check unset channel
    assertEquals(summary2.getDayProgramVersion(date3, country, channelId3, 1), -1);
  }

}
