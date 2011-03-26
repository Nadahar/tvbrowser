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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

import junit.framework.TestCase;
import primarydatamanager.primarydataservice.PrimaryDataService;
import tvbrowserdataservice.DayProgramFileTest;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;
import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.ProgramFieldType;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class PrimaryDataManagerTest extends TestCase {

  public void testUpdate()
    throws IOException, FileFormatException, PreparationException
  {
    File testDir = new File("testdata");
    File prepDir = new File(testDir, "prepared");

    // Delete the old test environment
    IOUtilities.deleteDirectory(testDir);

    // Create a new test environment
    testDir.mkdir();

    //@SuppressWarnings("unused")
    //PrimaryDataManager manager =
    new PrimaryDataManager(new File("test"));

    // Create the prepared data
    prepDir.mkdir();

    // Create a dummy mirrorlist.txt with an invalid mirror
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(new File(prepDir, "mirrorlist.txt"));
      stream.write("http://gibt.es.net".getBytes());
    }
    finally {
      try { stream.close(); } catch (Exception exc) {}
    }

    DayProgramFile prepProg1 = new DayProgramFile();
    prepProg1.setVersion(1);
    prepProg1.addProgramFrame(createProgramFrame(1, 20, 30, "Otto 1"));
    prepProg1.addProgramFrame(createProgramFrame(2, 31, 50, "Karlson vom Dach"));
    prepProg1.addProgramFrame(createProgramFrame(3, 51, 80, "Nixfürungut"));
    prepProg1.writeToFile(new File(prepDir, "2010-01-01_de_test_base_full.prog.gz"));

    DayProgramFile prepProg3 = new DayProgramFile();
    prepProg3.setVersion(1);
    prepProg3.addProgramFrame(createProgramFrame(1, 10, 20, "Vorher"));
    ProgramFrame frame = createProgramFrame(2, 21, 50, "Lange Sendung");
    frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, "Bla bla"));
    prepProg3.addProgramFrame(frame);
    prepProg3.addProgramFrame(createProgramFrame(3, 51, 60, "Danach"));
    prepProg3.writeToFile(new File(prepDir, "2010-01-03_de_test_base_full.prog.gz"));

    // Start the update
    //manager.setGroupNames(new String[]{"testgroup"});
    //manager.updateRawDataDir();
    // TODO: Check the result
  }



  private ProgramFrame createProgramFrame(int id, int startTime,
    int endTime, String title)
  {
    return DayProgramFileTest.createProgramFrame(id, startTime, endTime, title);
  }



  private class TestPDS implements PrimaryDataService {

    public boolean execute(String dir, java.io.PrintStream err) {
      File targetDir = new File(dir);

      try {
        // An unchanged day program except for the program order
        DayProgramFile rawProg1 = new DayProgramFile();
        rawProg1.addProgramFrame(createProgramFrame(10, 51, 80, "Nixfürungut"));
        rawProg1.addProgramFrame(createProgramFrame(11, 20, 30, "Otto 1"));
        rawProg1.addProgramFrame(createProgramFrame(12, 31, 50, "Karlson vom Dach"));
        rawProg1.writeToFile(new File(targetDir, "2010-01-01_de_test_raw_full.gz"));

        // A new day program
        DayProgramFile rawProg2 = new DayProgramFile();
        rawProg2.addProgramFrame(createProgramFrame(10, 50, 70, "Nachrichten"));
        rawProg2.writeToFile(new File(targetDir, "2010-01-02_de_test_raw_full.gz"));

        // A day program that needs an update
        DayProgramFile rawProg3 = new DayProgramFile();
        rawProg3.addProgramFrame(createProgramFrame(12, 21, 30, "Kurze Sendung I"));
        rawProg3.addProgramFrame(createProgramFrame(10, 10, 20, "Vorher"));
        rawProg3.addProgramFrame(createProgramFrame(11, 51, 60, "Danach"));
        rawProg3.addProgramFrame(createProgramFrame(13, 31, 50, "Kurze Sendung II"));
        rawProg3.writeToFile(new File(targetDir, "2010-01-03_de_test_raw_full.gz"));

        // TODO: A day program that has an old update with a higher frame version
        //       that needs a second update

        return false;
      }
      catch (Exception exc) {
        exc.printStackTrace(err);
        return true;
      }
    }

    public Channel[] getAvailableChannels() {
      return new Channel[] {
        new Channel(null, "Test", "test", TimeZone.getTimeZone("MET"), "de",null)
      };
    }

    public int getReadBytesCount() {
      return 0;
    }
    
    public void setParameters(Properties parameters) {
      
    }

  }

}
