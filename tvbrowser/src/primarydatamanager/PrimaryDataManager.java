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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import primarydatamanager.primarydataservice.PrimaryDataService;
import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.SummaryFile;
import util.io.FileFormatException;
import util.io.IOUtilities;
import util.io.Mirror;
import util.io.VerySimpleFormatter;
import devplugin.ChannelGroup;
import devplugin.Date;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class PrimaryDataManager {

  private static final Logger mLog = Logger.getLogger(PrimaryDataManager.class.getName());

  private File mRawDir;
  private File mPreparedDir;
  private File mWorkDir;
  private File mBackupDir;
  private File mConfigDir;

  private ChannelList[] mChannelListArr;
  private String[] mGroupNameArr;

  private RawDataProcessor mRawDataProcessor;



  public PrimaryDataManager(File baseDir) throws PreparationException {
    Logger.getLogger("sun.awt.X11.timeoutTask.XToolkit").setLevel(Level.INFO);
    mRawDir      = new File(baseDir, "raw");
    mPreparedDir = new File(baseDir, "prepared");
    mWorkDir     = new File(baseDir, "temp");
    mBackupDir   = new File(baseDir, "backup");
    mConfigDir   = new File(baseDir, "config");
    mRawDataProcessor = new RawDataProcessor();
  }

  public void setGroupNames(String[] groupNames) {
		mChannelListArr = new ChannelList[groupNames.length];
    mGroupNameArr = groupNames;
  }


  public void forceCompleteUpdateFor(String channel) {
    mRawDataProcessor.forceCompleteUpdateFor(channel);
  }


  public void createGroupFiles() throws PreparationException {

    /* A groupname file contains the name of the group in
     * an internationalized form */
    for (String group : mGroupNameArr) {
      File fromFile = new File(mConfigDir, group + ".txt");
      File toFile = new File(mWorkDir, group + "_info");
      try {
        IOUtilities.copy(fromFile, toFile);
      } catch (IOException exc) {
        throw new PreparationException("Could not copy file from " + fromFile.getAbsolutePath() + " to " + toFile.getAbsolutePath(), exc);
      }
    }

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

    // Create a new raw directory if it does not exist
    if (! mRawDir.exists()) {
      if (! mRawDir.mkdir()) {
        throw new PreparationException("Could not create raw directory: "
          + mRawDir.getAbsolutePath());
      }
    }

    //Create the channel list file
    createChannelList();

    // Update the mirror lists
    updateMirrorList();

    // Process the new raw data
		for (int i=0; i<mGroupNameArr.length; i++) {
      mRawDataProcessor.processRawDataDir(mRawDir, mPreparedDir, mWorkDir, mChannelListArr[i]);
    }

    // Create a summary files
    createSummaryFile();
    createGroupFiles();

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

    // Print out the statistics

    int quarantineCount = mRawDataProcessor.getQuarantineCount();
    if (quarantineCount > 0) {
      mLog.warning(quarantineCount + " day programs where put into quarantine");

      File txtDir = new File(mPreparedDir, "quarantine");
      if (!txtDir.exists()) {
        System.out.println("file "+txtDir.getAbsolutePath()+" does not exist");
      }
      if (!txtDir.isDirectory()) {
        System.out.println("file "+txtDir.getAbsolutePath()+" is not a directory");
      }

      if (txtDir.exists() && txtDir.isDirectory()) {
        File destDir=new File(txtDir,"txt");
        destDir.mkdirs();
        try {
          DayProgramFileTranslator.translateAllDayPrograms(txtDir);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (FileFormatException e) {
          e.printStackTrace();
        }
      }

    }
  }


  public static PrimaryDataService createPrimaryDataService(String className)
    throws PreparationException
  {
    Class clazz;

    if (className.startsWith(".")) {
      className="primarydatamanager.primarydataservice.secret"+className;
    }

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



  private void createChannelList() throws PreparationException {
    mLog.fine("Updating the channel list");

		for (int i=0; i<mGroupNameArr.length; i++) {
      // Load the channellist.txt
      mChannelListArr[i] = loadChannelListTxt(mGroupNameArr[i]+"_channellist.txt");

      // Save the mirrorlist.gz
      File toFile = new File(mWorkDir, mGroupNameArr[i]+"_"+ChannelList.FILE_NAME);
      try {
        mChannelListArr[i].writeToFile(toFile);
      }
      catch (Exception exc) {
        throw new PreparationException("Writing channel list for group "+mGroupNameArr[i]+" failed", exc);
      }
    }
  }




  private void createSummaryFile()
    throws PreparationException
  {


    for (int i=0; i<mGroupNameArr.length; i++) {

      // Create the file
      SummaryFile summary = new SummaryFile();
      File[] fileArr = mWorkDir.listFiles();
      for (File file : fileArr) {
        String fileName = file.getName();
        if (fileName.endsWith("_full.prog.gz")) {
          // This is a complete file -> Put its version to the summary
          try {
            Date date = DayProgramFile.getDateFromFileName(fileName);
            String country = DayProgramFile.getCountryFromFileName(fileName);
            String channelId = DayProgramFile.getChannelNameFromFileName(fileName);
            String levelName = DayProgramFile.getLevelFromFileName(fileName);

            //if (channelBelongsToGroup(channelId, country)) {
            if (RawDataProcessor.channelBelongsToGroup(mChannelListArr[i], channelId, country)) {
              int level = DayProgramFile.getLevelIndexForId(levelName);
              if (level == -1) {
                throw new PreparationException("Day program file has unknown level '"
                    + levelName + "': " + file.getAbsolutePath());
              }
              int version = DayProgramFile.readVersionFromFile(file);

              summary.setDayProgramVersion(date, country, channelId, level, version);
            }
          }
          catch (Exception exc) {
            throw new PreparationException("Adding day program file to summary " +
                "failed: " + file.getAbsolutePath(), exc);
          }
        }
      }

      System.out.println("writing summary to file...");

      // Save the file
      File file = new File(mWorkDir, mGroupNameArr[i]+"_"+SummaryFile.SUMMARY_FILE_NAME);
      try {
        summary.writeToFile(file);
      }
      catch (Exception exc) {
        throw new PreparationException("Writing summary file failed: "
          + file.getAbsolutePath(), exc);
      }
		}

		System.out.println("done.");
  }


  private void updateMirrorList() throws PreparationException {
    mLog.fine("Updating the mirror list");

    for (String groupNameArr : mGroupNameArr) {
      // Load the mirrorlist.txt
      Mirror[] mirrorArr = loadMirrorListTxt(groupNameArr + "_mirrorlist.txt");

      // Save the mirrorlist.gz
      File toFile = new File(mWorkDir, groupNameArr + "_" + Mirror.MIRROR_LIST_FILE_NAME);
      try {
        Mirror.writeMirrorListToFile(toFile, mirrorArr);
      }
      catch (IOException exc) {
        throw new PreparationException("Writing mirror list for group " + groupNameArr + " failed", exc);
      }
    }
  }


  private ChannelList loadChannelListTxt(String fileName) throws PreparationException {
    ChannelList result = new ChannelList((ChannelGroup)null);

    File fromFile = new File(mConfigDir, fileName);
    try {
      result.readFromStream(new FileInputStream(fromFile), null, false);
    } catch (IOException e) {
      throw new PreparationException("Loading "+fileName+" failed", e);
    } catch (FileFormatException e) {
      throw new PreparationException("Loading "+fileName+" failed", e);
    }

    return result;

  }

  private Mirror[] loadMirrorListTxt(String fileName) throws PreparationException {
    ArrayList<Mirror> mirrorList = new ArrayList<Mirror>();

    File fromFile = new File(mConfigDir, fileName);
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(fromFile), 0x4000);
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
				int weight = 100;
        if (StringUtils.isNotEmpty(line)) {
					String url = line;
					String[] s = url.split(";");
					if (s.length==2) {
						url = s[0];
						String weightStr = s[1];
						try {
              weight = Integer.parseInt(weightStr);
						}catch(NumberFormatException e) {
							weight = 100;
							mLog.warning("Invalid weight entry in "+fileName+": "+line+" - set to 100 instead");
						}
					}
          mirrorList.add(new Mirror(url, weight));
        }
      }
    }
    catch (Exception exc) {
      throw new PreparationException("Loading "+fileName+" failed", exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {
          //Empty
        }
      }
    }

    // Convert the list into an array
    Mirror[] mirrorArr = new Mirror[mirrorList.size()];
    mirrorList.toArray(mirrorArr);

    return mirrorArr;
  }

  /**
   * @return <code>true</code>, if the prepared directory exists
   */
  private boolean doesPreparedExist() {
     return mPreparedDir.exists() && mPreparedDir.isDirectory();
  }

  public static void main(String[] args) {
    // setup logging
    try {
      // Get the default Logger
      Logger mainLogger = Logger.getLogger("");
      mainLogger.setLevel(Level.FINEST);

      Handler consoleHandler = mainLogger.getHandlers()[0];
      consoleHandler.setLevel(Level.FINEST);
      consoleHandler.setFormatter(new VerySimpleFormatter());

      // Add a file handler
      new File("log").mkdir();
      Handler fileHandler = new FileHandler("log/datamanager.log", 50000, 2, true);
      fileHandler.setLevel(Level.INFO);
      mainLogger.addHandler(fileHandler);
    }
    catch (IOException exc) {
      System.out.println("Can't create log file");
      exc.printStackTrace();
    }

    // Start the update
    if (args.length == 0) {
      System.out.println("USAGE: PrimaryDataManager [-forceCompleteUpdate [channel{;channel}]] groups...");
      System.exit(1);
    } else {
      try {
        PrimaryDataManager manager = new PrimaryDataManager(new File("."));
        ArrayList<String> groupNames = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
          if (args[i].equalsIgnoreCase("-forceCompleteUpdate")) {
            if ((i + 1) >= args.length) {
              System.out.println("You have to specify a colon separated " +
                "list of channels after -forceCompleteUpdate");
              System.exit(1);
            } else {
              i++;
              StringTokenizer tokenizer = new StringTokenizer(args[i], ":");
              while (tokenizer.hasMoreTokens()) {
                manager.forceCompleteUpdateFor(tokenizer.nextToken());
              }
            }
          } else {
            final String[] groups = args[i].split(",");
            groupNames.addAll(Arrays.asList(groups));
          }
        }

        if (groupNames.size() == 0) {
          System.out.println("Please specify at least one channel group");
          System.exit(-1);
        }

        if (!manager.doesPreparedExist()) {
          System.out.println("The prepared directory is missing, this directory is very important and shouldn't " +
              "be deleted, because this leeds to massiv problems.");
          System.exit(-1);
        }

        String[] groupNamesArr = new String[groupNames.size()];
				groupNames.toArray(groupNamesArr);
        manager.setGroupNames(groupNamesArr);

        manager.updateRawDataDir();

        // Exit with error code 2 if some day programs were put into quarantine
        if (manager.mRawDataProcessor.getQuarantineCount() != 0) {
          System.exit(2);
        }
      }
      catch (PreparationException exc) {
        exc.printStackTrace();
        System.exit(1);
      }
    }
  }


}
