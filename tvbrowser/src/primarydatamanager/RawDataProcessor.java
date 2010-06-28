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
package primarydatamanager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import tvdataservice.MutableProgram;
import util.io.FileFormatException;
import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;

/**
 * Compares the new raw data with the last prepared data and creates update
 * files if nessesary.
 *
 * @author Til Schneider, www.murfman.de
 */
public class RawDataProcessor {

  private static final Logger mLog = Logger.getLogger(RawDataProcessor.class.getName());

  /**
   * @Deprecated use #{tvdataservice.MutableProgram#MAX_SHORT_INFO_LENGTH}
   */
  final public static int MAX_SHORT_DESCRIPTION_LENGTH = MutableProgram.MAX_SHORT_INFO_LENGTH;

  /**
   * The percentage of the number of frames that must stay in the version
   * of a program to come into quarantine.
   */
  private static double MAX_DELETED_FRAMES = 0.25;

  /**
   * The deadline day. All data files that contain TV data for days before that
   * day count as outdated.
   */
  private Date mDeadlineDay;

  private HashSet<String> mForceCompleteUpdateChannelSet;

  private int mQuarantineCount;


  public RawDataProcessor() {
    mDeadlineDay = new Date().addDays(-2);
  }


  public void forceCompleteUpdateFor(String channel) {
    if (mForceCompleteUpdateChannelSet == null) {
      mForceCompleteUpdateChannelSet = new HashSet<String>();
    }

    mForceCompleteUpdateChannelSet.add(channel);
  }


  public int getQuarantineCount() {
    return mQuarantineCount;
  }


  public void processRawDataDir(File rawDir, File preparedDir, File workDir, ChannelList channelList)
    throws PreparationException
  {
    if (! rawDir.exists()) {
      throw new PreparationException("Raw directory does not exist: "
        + rawDir.getAbsolutePath());
    }

    // Go through the raw files and process them one by one
    File[] fileArr = rawDir.listFiles();
    if (fileArr == null) {
      return;
    }
    for (File element : fileArr) {
      String fileName = element.getName();

      // Extract the information from the file name
      // Pattern: <yyyy>-<mm>-<dd>_<country>_<channel>_raw_full.prog.gz
      if (fileName.endsWith("_raw_full.prog.gz")) {
        // Extract the information from the file name
        Date date;
        String country, channel;
        try {
          int year = Integer.parseInt(fileName.substring(0, 4));
          int month = Integer.parseInt(fileName.substring(5, 7));
          int day = Integer.parseInt(fileName.substring(8, 10));
          date = new Date(year, month, day);

          int countryEnd = fileName.indexOf('_', 11);
          country = fileName.substring(11, countryEnd);

          int channelEnd = fileName.indexOf('_', countryEnd + 1);
          channel = fileName.substring(countryEnd + 1, channelEnd);

          // if the file does not belong to our group, we ignore it
          if (!RawDataProcessor.channelBelongsToGroup(channelList, channel, country)) {
            continue;
          }

        }
        catch (Exception exc) {
          throw new PreparationException("Raw file name has wrong pattern: "
            + fileName, exc);
        }

        // Ensure that the file is not outdated
        if (date.compareTo(mDeadlineDay) >= 0) {
          // Load the file
          DayProgramFile rawFile;
          try {
            rawFile = new DayProgramFile();
            rawFile.readFromFile(element);
          }
          catch (Exception exc) {
            throw new PreparationException("Loading raw file failed: "
              + fileName, exc);
          }

          // Process the file
          try {
            processRawFile(rawFile, date, country, channel, preparedDir, workDir);
          }
          catch (PreparationException exc) {
            throw new PreparationException("Processing raw file failed: "
              + element.getAbsolutePath(), exc);
          }
        }
        else {
          element.delete();
        }
      }
    }

  }

  public static boolean channelBelongsToGroup(ChannelList list, String channelId, String country) {

      for (int i=0; i<list.getChannelCount(); i++) {
        Channel ch = list.getChannelAt(i);
        if (ch.getId().equals(channelId) && ch.getCountry().equals(country)) {
          return true;
        }
      }
        return false;
    }


  private void processRawFile(DayProgramFile rawProg, Date date,
    String country, String channel, File preparedDir, File workDir)
    throws PreparationException
  {

		System.out.println(date+"; "+channel+"...");


    // Prepare the raw file (check mandatory fields, remove empty fields, ...)
    prepareDayProgram(rawProg, date, country, channel);

    // Get the file names of the level-complete-files
    String[] levelFileNameArr = new String[DayProgramFile.getLevels().length];
    for (int i = 0; i < levelFileNameArr.length; i++) {
      levelFileNameArr[i] = DayProgramFile.getProgramFileName(date, country,
        channel, DayProgramFile.getLevels()[i].getId());
    }

    // Load the level files if they exist
    DayProgramFile[] levelProgArr = new DayProgramFile[levelFileNameArr.length];
    for (int i = 0; i < levelProgArr.length; i++) {
      File file = new File(preparedDir, levelFileNameArr[i]);
      System.err.println(file);
      if (file.exists()) {
        levelProgArr[i] = new DayProgramFile();
        try {
          levelProgArr[i].readFromFile(file);
        }
        catch (Exception exc) {
          throw new PreparationException("Loading complete file for level "
            + DayProgramFile.getLevels()[i] + " failed: " + levelFileNameArr[i],
            exc);
        }
      }
    }

    // Create a file that contains all level files
    DayProgramFile preparedProg = new DayProgramFile();
    for (int i = 0; i < levelProgArr.length; i++) {
      if (levelProgArr[i] != null) {
        try {
          preparedProg.merge(levelProgArr[i]);
        }
        catch (FileFormatException exc) {
          throw new PreparationException("Complete file has wrong format: "
            + preparedDir + File.separator + levelFileNameArr[i], exc);
        }
      }
    }

    // Map the programs of the raw file to the programs of the prepared file
    // in order to give them the right IDs
    mapDayProgram(rawProg, preparedProg, date, country, channel, preparedDir);

    // Check whether the new version should be in quarantine
    File targetDir = workDir;
    boolean quarantine = checkForQuarantine(preparedProg, rawProg);
    if (quarantine) {
      mLog.warning("Putting day program file in quarantine: "
        + date + ", " + country + ", " + channel);
      mQuarantineCount++;

      targetDir = new File(workDir, "quarantine");
      if (! targetDir.exists()) {
        targetDir.mkdir();
      }
    }

    // Split the raw file in the levels
    DayProgramFile[] newLevelProgArr
      = new DayProgramFile[DayProgramFile.getLevels().length];
    for (int i = 0; i < newLevelProgArr.length; i++) {
      newLevelProgArr[i] = extractLevel(rawProg, i);
    }

    // Write the files into the work dir
    for (int i = 0; i < newLevelProgArr.length; i++) {
      if (levelProgArr[i] == null) {
        // We don't have an old program file
        // -> Create a new complete file if we have data now
        if (newLevelProgArr[i].getProgramFrameCount() != 0) {
          File file = new File(targetDir, levelFileNameArr[i]);
          try {
            newLevelProgArr[i].writeToFile(file);

            mLog.fine("Created new day program file: " + levelFileNameArr[i]);
          }
          catch (Exception exc) {
            throw new PreparationException("Writing prepared file failed: "
              + file.getAbsolutePath(), exc);
          }
        }
      } else {
        // We already have an old program file

        // Check whether the new version should come into quarantine
        String level = DayProgramFile.getLevels()[i].getId();
        if (quarantine) {
          // Copy the old files
          copyFiles(levelProgArr[i], date, country, channel, level,
            preparedDir, workDir);
        }

        // Check whether something changed
        if (levelProgArr[i].equals(newLevelProgArr[i])) {
          // Nothing changed -> Just copy the files
          copyFiles(levelProgArr[i], date, country, channel, level,
            preparedDir, targetDir);

          mLog.finest("Nothing to do for day program file: " + levelFileNameArr[i]);
        } else {
          // Something changed -> Create an update
          createUpdate(levelProgArr[i], newLevelProgArr[i], date, country,
                       channel, level, preparedDir, targetDir);

          mLog.finest("Updated day program file to version "
            + (levelProgArr[i].getVersion() + 1) + ": " + levelFileNameArr[i]);
        }
      }
    }
  }


  /**
   * Prepares a day program.
   * <p>
   * Checks mandatory fields, removes empty fields, ...
   */
  private void prepareDayProgram(DayProgramFile prog, Date date,
    String country, String channel)
    throws PreparationException
  {
    // Go through the program frames and pepare them
    for (int frameIdx = 0; frameIdx < prog.getProgramFrameCount(); frameIdx++) {
      ProgramFrame frame = prog.getProgramFrameAt(frameIdx);

      // Trim text fields and remove all empty fields from the day program
      for (int fieldIdx = frame.getProgramFieldCount() - 1; fieldIdx >= 0; fieldIdx--) {
        ProgramField field = frame.getProgramFieldAt(fieldIdx);

        // Trim text fields
        if (field.getType().getFormat() == ProgramFieldType.TEXT_FORMAT) {
          String oldText = field.getTextData();
          if (oldText != null) {
            String newText = oldText.trim();
            if (oldText.length() != newText.length()) {
              field.setTextData(newText);
            }
          }
        }

        // Remove empty fields
        byte[] data = field.getBinaryData();
        if ((data == null) || (data.length == 0)) {
          frame.removeProgramFieldAt(fieldIdx);
        }
      }

      // Check the mandatory fields (start time and title)
      ProgramField startTimeField = frame.getProgramFieldOfType(ProgramFieldType.START_TIME_TYPE);
      if (startTimeField == null) {
        throw new PreparationException("Program frame has no start time. "
          + "ID: " + frame.getId() + ", Day program: " + date + ", " + country + ", " + channel);
      }
      ProgramField titleField = frame.getProgramFieldOfType(ProgramFieldType.TITLE_TYPE);
      if (titleField == null) {
        frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, "[Unknown title]"));
        System.out.println("Program frame has no title. "
          + "ID: " + frame.getId() + ", Day program: " + date + ", " + country + ", " + channel);
      }

      // Try to create a short description
      ProgramField shortDescField = frame.getProgramFieldOfType(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
      if (shortDescField == null) {
        ProgramField descField = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
        if (descField != null) {
          // Generate a short description from the description
          String shortDesc = descField.getTextData();
          if (shortDesc.length() > MutableProgram.MAX_SHORT_INFO_LENGTH) {
            int lastSpacePos = shortDesc.lastIndexOf(' ', MutableProgram.MAX_SHORT_INFO_LENGTH);
            if (lastSpacePos == -1) {
              shortDesc = shortDesc.substring(0, MutableProgram.MAX_SHORT_INFO_LENGTH);
            } else {
              shortDesc = shortDesc.substring(0, lastSpacePos);
            }
          }

          // Add the short description to the frame
          shortDescField = ProgramField.create(ProgramFieldType.SHORT_DESCRIPTION_TYPE, shortDesc);
          frame.addProgramField(shortDescField);
        }
      }
    }
  }


  private void mapDayProgram(DayProgramFile rawProg, DayProgramFile preparedFile,
    Date date, String country, String channel, File preparedDir)
    throws PreparationException
  {
    // Map the programs
    if ((mForceCompleteUpdateChannelSet != null)
      && mForceCompleteUpdateChannelSet.contains(channel))
    {
      // We should force a complete update for this channel
      // We do this by giving each program in the rawProg a ID of -1.
      // This way, it will get a new ID
      mLog.warning("Forcing complete update for Day program: " + date + ", "
        + country + ", " + channel);

      for (int i = 0; i < rawProg.getProgramFrameCount(); i++) {
        rawProg.getProgramFrameAt(i).setId(-1);
      }
    } else {
      new DayProgramMapper().map(rawProg, preparedFile);
    }

    // Check whether we have programs that did not find a match
    boolean someProgramsAreUnmapped = false;
    for (int i = 0; i < rawProg.getProgramFrameCount(); i++) {
      if (rawProg.getProgramFrameAt(i).getId() == -1) {
        someProgramsAreUnmapped = true;
        break;
      }
    }

    // If we have unmapped programs -> give them new IDs
    if (someProgramsAreUnmapped) {
      // Get the maximum ID from the complete file and all update files
      int maxId = PrimaryDataUtilities.getMaxId(preparedFile);
      for (int i = 0; i < DayProgramFile.getLevels().length; i++) {
        String level = DayProgramFile.getLevels()[i].getId();
        int updateMaxId = getMaxIdOfUpdateFiles(date, country, channel, level,
                                                preparedDir);
        if (updateMaxId > maxId) {
          maxId = updateMaxId;
        }
      }

      int nextId = maxId + 1;
      for (int i = 0; i < rawProg.getProgramFrameCount(); i++) {
        ProgramFrame frame = rawProg.getProgramFrameAt(i);
        if (frame.getId() == -1) {
          frame.setId(nextId++);
        }
      }
    }
  }


  private DayProgramFile extractLevel(DayProgramFile prog, int levelIdx)
    throws PreparationException
  {
    DayProgramFile levelProg = new DayProgramFile();

    for (int i = 0; i < prog.getProgramFrameCount(); i++) {
      ProgramFrame frame = prog.getProgramFrameAt(i);
      ProgramFrame levelFrame = null;

      if (levelIdx == 0) {
        // base: All information but description and the image
        levelFrame = (ProgramFrame) frame.clone();
        levelFrame.removeProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
        levelFrame.removeProgramFieldOfType(ProgramFieldType.ACTOR_LIST_TYPE);
        levelFrame.removeProgramFieldOfType(ProgramFieldType.ADDITIONAL_INFORMATION_TYPE);
       // levelFrame.removeProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
        levelFrame.removeProgramFieldOfType(ProgramFieldType.PICTURE_TYPE);
        levelFrame.removeProgramFieldOfType(ProgramFieldType.PICTURE_DESCRIPTION_TYPE);
        levelFrame.removeProgramFieldOfType(ProgramFieldType.PICTURE_COPYRIGHT_TYPE);

      } else {
        ProgramField levelField1 = null;
        ProgramField levelField2 = null;
        ProgramField levelField3 = null;

        switch (levelIdx) {
          case 1:
            // more00-16: Only the descriptions and the actor list
            //            between midnight and 16 pm
            if (PrimaryDataUtilities.getProgramStartTime(frame) < (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
              levelField2 = frame.getProgramFieldOfType(ProgramFieldType.ACTOR_LIST_TYPE);
              levelField3 = frame.getProgramFieldOfType(ProgramFieldType.ADDITIONAL_INFORMATION_TYPE);
            }
            break;
          case 2:
            // more16-00: Only the descriptions and the actor list
            //            between 16 pm and midnight
            if (PrimaryDataUtilities.getProgramStartTime(frame) >= (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
              levelField2 = frame.getProgramFieldOfType(ProgramFieldType.ACTOR_LIST_TYPE);
              levelField3 = frame.getProgramFieldOfType(ProgramFieldType.ADDITIONAL_INFORMATION_TYPE);
            }
            break;
         /* case 3:
            // image00-16: Only the image between midnight and 16 pm
            if (PrimaryDataUtilities.getProgramStartTime(frame) < (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
            }
            break;
          case 4:
            // image16-00: Only the image between 16 pm and midnight
            if (PrimaryDataUtilities.getProgramStartTime(frame) >= (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
            }
            break;*/

          case 3:
            // picture00-16: Only the picture between midnight and 16 pm
            if (PrimaryDataUtilities.getProgramStartTime(frame) < (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_TYPE);
              levelField2 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_DESCRIPTION_TYPE);
              levelField3 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_COPYRIGHT_TYPE);
            }
            break;

          case 4:
            // picture16-00: Only the picture between 16 pm and midnight
            if (PrimaryDataUtilities.getProgramStartTime(frame) >= (16 * 60)) {
              levelField1 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_TYPE);
              levelField2 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_DESCRIPTION_TYPE);
              levelField3 = frame.getProgramFieldOfType(ProgramFieldType.PICTURE_COPYRIGHT_TYPE);
            }
            break;


        }

        if ((levelField1 != null) || (levelField2 != null) || (levelField3 != null)) {
          levelFrame = new ProgramFrame(frame.getId());
          if (levelField1 != null) {
            levelFrame.addProgramField(levelField1);
          }
          if (levelField2 != null) {
            levelFrame.addProgramField(levelField2);
          }
          if (levelField3 != null) {
            levelFrame.addProgramField(levelField3);
          }
        }
      }

      if (levelFrame != null) {
        levelProg.addProgramFrame(levelFrame);
      }
    }

    return levelProg;
  }


  private void copyFiles(DayProgramFile prog, Date date, String country,
    String channel, String level, File fromDir, File toDir)
    throws PreparationException
  {
    // Copy the complete file
    String fileName = DayProgramFile.getProgramFileName(date, country, channel, level);
    String additionalName = fileName.substring(0,fileName.indexOf(".prog.gz")) + "_additional.prog.gz";

    File prepFile = new File(fromDir, fileName);
    File additionalFile = new File(fromDir, additionalName);
    File workFile = new File(toDir, fileName);
    try {
      IOUtilities.copy(prepFile, workFile);

      if(additionalFile.isFile()) {
        IOUtilities.copy(additionalFile, new File(toDir, additionalName));
      }
    }
    catch (IOException exc) {
      throw new PreparationException("Copying complete file from '"
        + prepFile.getAbsolutePath() + "' to '" + workFile.getAbsolutePath()
        + "' failed!", exc);
    }

    // Copy the update files
    for (int version = 1; version < prog.getVersion(); version++) {
      fileName = DayProgramFile.getProgramFileName(date, country, channel, level, version);
      prepFile = new File(fromDir, fileName);
      workFile = new File(toDir, fileName);
      try {
        IOUtilities.copy(prepFile, workFile);
      }
      catch (IOException exc) {
        throw new PreparationException("Copying update file #" + version + " from '"
          + prepFile.getAbsolutePath() + "' to '" + workFile.getAbsolutePath()
          + "' failed!", exc);
      }
    }
  }


  private void createUpdate(DayProgramFile lastProg, DayProgramFile newProg,
    Date date, String country, String channel, String level, File preparedDir,
    File targetDir)
    throws PreparationException
  {
    newProg.setVersion(lastProg.getVersion() + 1);
    String completeFilename = DayProgramFile.getProgramFileName(date, country,
      channel, level);
    File file = new File(targetDir, completeFilename);
    try {
      newProg.writeToFile(file);
    }
    catch (Exception exc) {
      throw new PreparationException("Writing complete file failed: "
        + file.getAbsolutePath(), exc);
    }

    // Create the new update file that updates from the last version to the
    // new version
    DayProgramFile newUpdateFile = createUpdateFile(lastProg, newProg);

    // Save the update file
    String newUpdateFileName = DayProgramFile.getProgramFileName(date, country,
      channel, level, lastProg.getVersion());
    file = new File(targetDir, newUpdateFileName);
    try {
      newUpdateFile.writeToFile(file);
    }
    catch (Exception exc) {
      throw new PreparationException("Writing new update file failed: "
        + file.getAbsolutePath(), exc);
    }

    // Update the other update files
    updateOldUpdateFiles(newUpdateFile, date, country, channel, level,
                         preparedDir, targetDir);
  }


  private DayProgramFile createUpdateFile(DayProgramFile lastCompleteFile,
    DayProgramFile newCompleteFile)
  {
    DayProgramFile updateFile = new DayProgramFile();
    updateFile.setVersion(newCompleteFile.getVersion());

    // Determine the programs that should be deleted
    for (int lastIdx = 0; lastIdx < lastCompleteFile.getProgramFrameCount(); lastIdx++) {
      int id = lastCompleteFile.getProgramFrameAt(lastIdx).getId();

      // Check whether this frame has an counterpart in the new file
      int newIdx = newCompleteFile.getProgramFrameIndexForId(id);
      if (newIdx == -1) {
        // It has not -> Add an empty frame with this ID as a order for deletion
        ProgramFrame frame = new ProgramFrame(id);
        updateFile.addProgramFrame(frame);
      }
    }

    // Determine the programs that should be inserted or updated
    for (int newIdx = 0; newIdx < newCompleteFile.getProgramFrameCount(); newIdx++) {
      ProgramFrame newFrame = newCompleteFile.getProgramFrameAt(newIdx);

      // Check whether there is a counterpart in the last file
      int lastIdx = lastCompleteFile.getProgramFrameIndexForId(newFrame.getId());
      if (lastIdx == -1) {
        // There is no counterpart -> this is an insert
        ProgramFrame insertFrame = (ProgramFrame) newFrame.clone();
        updateFile.addProgramFrame(insertFrame);
      } else {
        // There is a counterpart -> this is an update
        // -> Determine the changed fields and add them
        ProgramFrame lastFrame = lastCompleteFile.getProgramFrameAt(lastIdx);
        ProgramFrame updateFrame = createUpdateFrame(lastFrame, newFrame);

        if (updateFrame != null) {
          updateFile.addProgramFrame(updateFrame);
        }
      }
    }

    return updateFile;
  }


  private ProgramFrame createUpdateFrame(ProgramFrame lastFrame,
    ProgramFrame newFrame)
  {
    ProgramFrame updateFrame = null;

    // Determine the fields that should be deleted
    for (int lastIdx = 0; lastIdx < lastFrame.getProgramFieldCount(); lastIdx++) {
      ProgramField lastField = lastFrame.getProgramFieldAt(lastIdx);

      // Check whether this field has an counterpart in the new frame
      int newIdx = newFrame.getProgramFieldIndexForTypeId(lastField.getTypeId());
      if (newIdx == -1) {
        // It has not -> Add an empty field with this type ID as a order for
        // deletion
        ProgramField updateField = (ProgramField) lastField.clone();
        updateField.removeData();

        if (updateFrame == null) {
          updateFrame = new ProgramFrame(newFrame.getId());
        }
        updateFrame.addProgramField(updateField);
      }
    }

    // Determine the programs that should be inserted or updated
    for (int newIdx = 0; newIdx < newFrame.getProgramFieldCount(); newIdx++) {
      ProgramField newField = newFrame.getProgramFieldAt(newIdx);

      // Check whether there is a counterpart in the last file and whether it
      // has changed
      int lastIdx = lastFrame.getProgramFieldIndexForTypeId(newField.getTypeId());

      boolean hasChanged;
      if (lastIdx == -1) {
        // There is no counterpart -> It has changed
        hasChanged = true;
      } else {
        // There is a counterpart -> Check whether the content changed
        ProgramField lastField = lastFrame.getProgramFieldAt(lastIdx);

        hasChanged = ! newField.equals(lastField);
      }

      // Add the field if it has changed
      if (hasChanged) {
        ProgramField field = (ProgramField) newField.clone();

        if (updateFrame == null) {
          updateFrame = new ProgramFrame(newFrame.getId());
        }
        updateFrame.addProgramField(field);
      }
    }

    return updateFrame;
  }


  private int getMaxIdOfUpdateFiles(Date date, String country,
    String channel, String level, File preparedDir)
    throws PreparationException
  {
    int maxId = 0;

    // Get the max IDs of all update files
    int updateVersion = 1;
    boolean finished = false;
    while (! finished) {
      String filename = DayProgramFile.getProgramFileName(date, country,
        channel, level, updateVersion);
      File file = new File(preparedDir, filename);
      if (! file.exists()) {
        finished = true;
      } else {
        DayProgramFile updateFile;
        try {
          updateFile = new DayProgramFile();
          updateFile.readFromFile(file);
        }
        catch (Exception exc) {
          throw new PreparationException("Loading prepared file failed: "
            + filename, exc);
        }

        // Get the max ID
        int updateMaxId = PrimaryDataUtilities.getMaxId(updateFile);
        if (updateMaxId > maxId) {
          maxId = updateMaxId;
        }
      }

      updateVersion++;
    }

    return maxId;
  }


  private void updateOldUpdateFiles(DayProgramFile newUpdateFile, Date date,
    String country, String channel, String level, File preparedDir, File targetDir)
    throws PreparationException
  {
    int newUpdateFileFromVersion = newUpdateFile.getVersion() - 1;
    for (int version = 1; version < newUpdateFileFromVersion; version++) {
      // Load the update file
      String updateFileName = DayProgramFile.getProgramFileName(date, country,
        channel, level, version);
      File file = new File(preparedDir, updateFileName);
      DayProgramFile updateFile;
      try {
        updateFile = new DayProgramFile();
        updateFile.readFromFile(file);
      }
      catch (Exception exc) {
        throw new PreparationException("Loading update file for version "
          + version + " failed: " + file.getAbsolutePath(), exc);
      }

      // Update the update file
      try {
        updateFile.updateUpdateFile(newUpdateFile);
      }
      catch (FileFormatException exc) {
        throw new PreparationException("Updating the update file for version "
          + version + " failed: " + updateFileName, exc);
      }

      // Save the update file
      file = new File(targetDir, updateFileName);
      try {
        updateFile.writeToFile(file);
      }
      catch (Exception exc) {
        throw new PreparationException("Writing update file for version "
          + version + " failed: " + file.getAbsolutePath(), exc);
      }
    }
  }


  private boolean checkForQuarantine(DayProgramFile oldProg, DayProgramFile newProg) {
    int oldFrameCount = oldProg.getProgramFrameCount();

    // Count the number of frames that will be deleted
    int deletedCount = 0;
    for (int i = 0; i < oldFrameCount; i++) {
      int oldFrameId = oldProg.getProgramFrameAt(i).getId();

      // Check whether there is a frame with this ID in the new program
      if (newProg.getProgramFrameIndexForId(oldFrameId) == -1) {
        // This frame is missing in the new program
        deletedCount++;
      }
    }

    int maxDeletedFrames = (int) (oldFrameCount * MAX_DELETED_FRAMES);
    return (deletedCount > maxDeletedFrames);
  }

}