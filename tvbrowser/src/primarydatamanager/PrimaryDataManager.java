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

import primarydatamanager.primarydataservice.PrimaryDataService;

import tvbrowserdataservice.file.*;
import util.io.IOUtilities;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PrimaryDataManager {
  
  private static final String[] LEVEL_ARR = {
    "base", "image16-00", "image00-16", "desc16-00", "desc00-16"
  };
  
  private File mRawDir;
  private File mPreparedDir;
  private File mWorkDir;
  private File mBackupDir;
  
  private PrimaryDataService[] mDataServiceArr;
  
  
  
  public PrimaryDataManager() {
    setBaseDir(".");
  }
  
  
  
  public void setBaseDir(String dir) {
    mRawDir      = new File(dir + File.separator + "raw");
    mPreparedDir = new File(dir + File.separator + "prepared");
    mWorkDir     = new File(dir + File.separator + "temp");
    mBackupDir   = new File(dir + File.separator + "backup");
  }



  public void setDataServiceArr(PrimaryDataService[] dataServiceArr) {
    mDataServiceArr = dataServiceArr;
  }



  public void updateRawDataDir() throws PreparationException {
    // Delete the old work directory
    try {
      IOUtilities.deleteDirectory(mWorkDir);
    }
    catch (IOException exc) {
      throw new PreparationException("Deleting old work directory failed", exc);
    }
    
    // Create a new work directory
    if (! mWorkDir.mkdir()) {
      throw new PreparationException("Could not create work directory: "
        + mWorkDir.getAbsolutePath());
    }

    // Delete the old raw directory
    try {
      IOUtilities.deleteDirectory(mRawDir);
    }
    catch (IOException exc) {
      throw new PreparationException("Deleting old raw directory failed", exc);
    }
    
    // Create a new raw directory
    if (! mRawDir.mkdir()) {
      throw new PreparationException("Could not create raw directory: "
        + mRawDir.getAbsolutePath());
    }

    // Get the new raw data
    loadNewRawData();
    
    // Process the new raw data
    processRawDataDir();
    
    // Delete the old backup
    try {
      IOUtilities.deleteDirectory(mBackupDir);
    }
    catch (IOException exc) {
      throw new PreparationException("Deleting old backup directory failed", exc);
    }
    
    // Let the prepared dir become the new backup
    if (mPreparedDir.exists()) {
      if (! mPreparedDir.renameTo(mBackupDir)) {
        throw new PreparationException("Renaming file '"
          + mPreparedDir.getAbsolutePath() + "' to '"
          + mBackupDir.getAbsolutePath() + "' failed");
      }
    }

    // Let the work dir become the new prepared dir
    if (! mWorkDir.renameTo(mPreparedDir)) {
      throw new PreparationException("Renaming file '"
        + mWorkDir.getAbsolutePath() + "' to '"
        + mPreparedDir.getAbsolutePath() + "' failed");
    }
  }



  private void loadNewRawData()
    throws PreparationException
  {
    if (mDataServiceArr == null) {
      throw new PreparationException("No primary data services specified");
    }
    
    String dir = mRawDir.getAbsolutePath();
    for (int i = 0; i < mDataServiceArr.length; i++) {
      boolean thereWereErrors = mDataServiceArr[i].execute(dir, System.err);
      if (thereWereErrors) {
        throw new PreparationException("Getting raw data from primary data "
          + " service " + mDataServiceArr[i].getClass().getName() + " failed");
      }
    }
  }



  private void processRawDataDir()
    throws PreparationException
  {
    if (! mRawDir.exists()) {
      throw new PreparationException("Raw directory does not exist: "
        + mRawDir.getAbsolutePath());
    }
    
    // Go through the raw files and process them one by one
    File[] fileArr = mRawDir.listFiles();
    for (int i = 0; i < fileArr.length; i++) {
      String fileName = fileArr[i].getName();
      
      // Extract the information from the file name
      // Pattern: <yyyy>-<mm>-<dd>_<country>_<channel>_raw_full.gz
      if (fileName.endsWith("_raw_full.gz")) {
        System.out.println("Processing raw file " + fileArr[i].getAbsolutePath());
        
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
        }
        catch (Exception exc) {
          throw new PreparationException("Raw file name has wrong pattern: "
            + fileName, exc);
        }
        
        // Load the file
        DayProgramFile rawFile;
        try {
          rawFile = new DayProgramFile();
          rawFile.readFromFile(fileArr[i]);
        }
        catch (Exception exc) {
          throw new PreparationException("Loading raw file failed: "
            + fileName, exc);
        }
        
        // Process the file
        try {
          processRawFile(rawFile, date, country, channel);
        }
        catch (PreparationException exc) {
          throw new PreparationException("Processing raw file failed: "
            + fileArr[i].getAbsolutePath(), exc);
        }
      }
    }
  }



  private void processRawFile(DayProgramFile rawProg, Date date,
    String country, String channel)
    throws PreparationException
  {
    // Remove all empty fields from the day program
    removeEmptyFields(rawProg);
    
    // Get the file names of the level-complete-files
    String[] levelFileNameArr = new String[LEVEL_ARR.length];
    for (int i = 0; i < levelFileNameArr.length; i++) {
      levelFileNameArr[i] = DayProgramFile.getProgramFileName(date, country,
        channel, LEVEL_ARR[i]);
    }
    
    // Load the level files if they exist
    DayProgramFile[] levelProgArr = new DayProgramFile[levelFileNameArr.length];
    for (int i = 0; i < levelProgArr.length; i++) {
      File file = new File(mPreparedDir, levelFileNameArr[i]);
      if (file.exists()) {
        levelProgArr[i] = new DayProgramFile();
        try {
          levelProgArr[i].readFromFile(file);
        }
        catch (Exception exc) {
          throw new PreparationException("Loading complete file for level "
            + LEVEL_ARR[i] + " failed: " + levelFileNameArr[i], exc);
        }
      }
    }
    
    // Create a file that contains all complete files
    DayProgramFile preparedProg = new DayProgramFile();
    for (int i = 0; i < levelProgArr.length; i++) {
      if (levelProgArr[i] != null) {
        try {
          preparedProg.merge(levelProgArr[i]);
        }
        catch (FileFormatException exc) {
          throw new PreparationException("Complete file has wrong format: "
            + mPreparedDir + File.separator + levelFileNameArr[i], exc);
        }
      }
    }
    
    // Map the programs of the raw file to the programs of the prepared file
    // in order to give them the right IDs
    mapDayProgram(rawProg, preparedProg, date, country, channel);
    
    // Split the raw file in the levels
    DayProgramFile newLevelProgArr[] = new DayProgramFile[LEVEL_ARR.length];
    for (int i = 0; i < newLevelProgArr.length; i++) {
      newLevelProgArr[i] = extractLevel(rawProg, i);
    }
    
    // Write the files into the work dir
    for (int i = 0; i < newLevelProgArr.length; i++) {
      if (levelProgArr[i] == null) {
        // We don't have an old program file
        // -> Create a new complete file if we have data now
        if (newLevelProgArr[i].getProgramFrameCount() != 0) {
          File file = new File(mWorkDir, levelFileNameArr[i]);
          try {
            newLevelProgArr[i].writeToFile(file);
          }
          catch (Exception exc) {
            throw new PreparationException("Writing prepared file failed: "
              + file.getAbsolutePath(), exc);
          }
        }
      } else {
        // We already have an old program file
        // -> Check whether something changed
        if (levelProgArr[i].equals(newLevelProgArr[i])) {
          // Nothing changed -> Just copy the files
          copyFiles(levelProgArr[i], date, country, channel, LEVEL_ARR[i]);
        } else {
          // Something changed -> Create an update
          createUpdate(levelProgArr[i], newLevelProgArr[i], date, country,
                       channel, LEVEL_ARR[i]);
        }
      }
    }
  }



  private void removeEmptyFields(DayProgramFile prog) {
    for (int frameIdx = 0; frameIdx < prog.getProgramFrameCount(); frameIdx++) {
      ProgramFrame frame = prog.getProgramFrameAt(frameIdx);
      
      for (int fieldIdx = frame.getProgramFieldCount() - 1; fieldIdx >= 0; fieldIdx--) {
        ProgramField field = frame.getProgramFieldAt(fieldIdx);
        byte[] data = field.getBinaryData();
        
        if ((data == null) || (data.length == 0)) {
          frame.removeProgramFieldAt(fieldIdx);
        }
      }
    }
  }



  private void copyFiles(DayProgramFile prog, Date date, String country,
    String channel, String level)
    throws PreparationException
  {
    // Copy the complete file
    String fileName = DayProgramFile.getProgramFileName(date, country, channel, level);
    File prepFile = new File(mPreparedDir, fileName);
    File workFile = new File(mWorkDir, fileName);
    try {
      IOUtilities.copy(prepFile, workFile);
    }
    catch (IOException exc) {
      throw new PreparationException("Copying complete file from '"
        + prepFile.getAbsolutePath() + "' to '" + workFile.getAbsolutePath()
        + "' failed!", exc);
    }
    
    // Copy the update files
    for (int version = 1; version < prog.getVersion(); version++) {
      fileName = DayProgramFile.getProgramFileName(date, country, channel, level, version);
      prepFile = new File(mPreparedDir, fileName);
      workFile = new File(mWorkDir, fileName);
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
    Date date, String country, String channel, String level)
    throws PreparationException
  {
    newProg.setVersion(lastProg.getVersion() + 1);
    String completeFilename = DayProgramFile.getProgramFileName(date, country,
      channel, level);
    File file = new File(mWorkDir, completeFilename);
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
    file = new File(mWorkDir, newUpdateFileName);
    try {
      newUpdateFile.writeToFile(file);
    }
    catch (Exception exc) {
      throw new PreparationException("Writing new update file failed: "
        + file.getAbsolutePath(), exc);
    }

    // Update the other update files
    updateOldUpdateFiles(newUpdateFile, date, country, channel, level);
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



  private void mapDayProgram(DayProgramFile rawProg, DayProgramFile preparedFile,
    Date date, String country, String channel)
    throws PreparationException
  {
    // Map the programs
    new DayProgramMapper().map(rawProg, preparedFile);
    
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
      int maxId = getMaxId(preparedFile);
      for (int i = 0; i < LEVEL_ARR.length; i++) {
        int updateMaxId = getMaxIdOfUpdateFiles(date, country, channel, LEVEL_ARR[i]);
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



  private int getMaxIdOfUpdateFiles(Date date, String country,
    String channel, String level)
    throws PreparationException
  {
    int maxId = 0;
    
    // Get the max IDs of all update files
    int updateVersion = 1;
    boolean finished = false;
    while (! finished) {
      String filename = DayProgramFile.getProgramFileName(date, country,
        channel, level, updateVersion);
      File file = new File(mPreparedDir, filename);
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
        int updateMaxId = getMaxId(updateFile);
        if (updateMaxId > maxId) {
          maxId = updateMaxId;
        }
      }
      
      updateVersion++;
    } 
    
    return maxId;
  }



  private int getMaxId(DayProgramFile file) {
    int maxId = 1;
    for (int i = 0; i < file.getProgramFrameCount(); i++) {
      int id = file.getProgramFrameAt(i).getId();
      if (id > maxId) {
        maxId = id;
      }
    }
    
    
    return maxId;
  }



  private void updateOldUpdateFiles(DayProgramFile newUpdateFile, Date date,
    String country, String channel, String level)
    throws PreparationException
  {
    int newUpdateFileFromVersion = newUpdateFile.getVersion() - 1;
    for (int version = 1; version < newUpdateFileFromVersion; version++) {
      // Load the update file
      String updateFileName = DayProgramFile.getProgramFileName(date, country,
        channel, level, version);
      File file = new File(mPreparedDir, updateFileName);
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
        updateFile.update(newUpdateFile);
      }
      catch (FileFormatException exc) {
        throw new PreparationException("Updating the update file for version "
          + version + " failed: " + updateFileName, exc);
      }
      
      // Save the update file
      file = new File(mWorkDir, updateFileName);
      try {
        updateFile.writeToFile(file);
      }
      catch (Exception exc) {
        throw new PreparationException("Writing update file for version "
          + version + " failed: " + file.getAbsolutePath(), exc);
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
        levelFrame.removeProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
      } else {
        ProgramField levelField = null;

        switch (levelIdx) {        
          case 1:
            // image16-00: Only the image between 16 pm and midnight
            if (getProgramStartTime(frame) > (16 * 60)) {
              levelField = frame.getProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
            }
            break;
          case 2:
            // image00-16: Only the image between midnight and 16 pm
            if (getProgramStartTime(frame) <= (16 * 60)) {
              levelField = frame.getProgramFieldOfType(ProgramFieldType.IMAGE_TYPE);
            }
            break;
          case 3:
            // desc16-00: Only the descriptions between 16 pm and midnight
            if (getProgramStartTime(frame) > (16 * 60)) {
              levelField = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
            }
            break;
          case 4:
            // desc00-16: Only the descriptions between midnight and 16 pm
            if (getProgramStartTime(frame) <= (16 * 60)) {
              levelField = frame.getProgramFieldOfType(ProgramFieldType.DESCRIPTION_TYPE);
            }
            break;
        }
        
        if (levelField != null) {
          levelFrame = new ProgramFrame(frame.getId());
          levelFrame.addProgramField(levelField);
        }
      }
      
      if (levelFrame != null) {
        levelProg.addProgramFrame(levelFrame);
      }
    }
    
    return levelProg;
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



  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Please specify at least one primary data service");
      System.exit(1);
    } else {
      try {
        PrimaryDataManager manager = new PrimaryDataManager();

        PrimaryDataService[] dataServiceArr = new PrimaryDataService[args.length];        
        for (int i = 0; i < args.length; i++) {
          dataServiceArr[i] = createPrimaryDataService(args[i]);
        }
        manager.setDataServiceArr(dataServiceArr);
        
        manager.updateRawDataDir();
      }
      catch (PreparationException exc) {
        exc.printStackTrace();
        System.exit(1);
      }
    }
  }



  private static PrimaryDataService createPrimaryDataService(String className)
    throws PreparationException
  {
    Class clazz;
    try {
      clazz = Class.forName(className);
    }
    catch (ClassNotFoundException exc) {
      throw new PreparationException("Primary data service class does not exist: "
        + className, exc);
    }
    
    Object obj;
    try {
      obj = clazz.newInstance();
    }
    catch (Exception exc) {
      throw new PreparationException("Can't create instance of primary data "
        + "service class: " + className, exc);
    }
    
    if (obj instanceof PrimaryDataService) {
      return (PrimaryDataService) obj;
    } else {
      throw new PreparationException("Class " + className + " does not implement "
        + PrimaryDataService.class.getName());
    }
  }

}
