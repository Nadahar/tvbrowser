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

import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.*;

import primarydatamanager.primarydataservice.PrimaryDataService;
import tvbrowserdataservice.file.*;
import util.io.IOUtilities;
import util.io.VerySimpleFormatter;
import devplugin.Date;
import devplugin.*;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class PrimaryDataManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PrimaryDataManager.class.getName());
  
  private File mRawDir;
  private File mPreparedDir;
  private File mWorkDir;
  private File mBackupDir;
  
  private Channel[] mChannelArr;
  private String[] mGroupArr;
  
  private PrimaryDataService[] mDataServiceArr;
  
  private RawDataProcessor mRawDataProcessor;
  
  private static final int CONCURRENT_DOWNLOADS=5;
  private LinkedList mDataServiceList;
  private int mActiveThreadCount;
  private Thread mWaitingThread;
  
  private int mReadBytesCount;

	private HashMap mGroupNameHashMap;
  
   
  
  public PrimaryDataManager(File baseDir) throws PreparationException {
    mRawDir      = new File(baseDir, "raw");
    mPreparedDir = new File(baseDir, "prepared");
    mWorkDir     = new File(baseDir, "temp");
    mBackupDir   = new File(baseDir, "backup");
//    mPDSLogDir   = new File(baseDir, "pdslog");
    
    mRawDataProcessor = new RawDataProcessor();
  }



  public void setDataServiceArr(PrimaryDataService[] dataServiceArr) {
    mDataServiceArr = dataServiceArr;
    
    /* create an array of all available channels */
    ArrayList channels=new ArrayList();
    
    for (int serviceIdx = 0; serviceIdx < mDataServiceArr.length; serviceIdx++) {
      Channel[] channelArr = mDataServiceArr[serviceIdx].getAvailableChannels();
      for (int i = 0; i < channelArr.length; i++) {
        channels.add(channelArr[i]);
      }
    }
    
    mChannelArr=new Channel[channels.size()];
    channels.toArray(mChannelArr);
    
    /* create an array of all available groupnames */
    HashSet groupSet=new HashSet();
    for (int i=0;i<mChannelArr.length;i++) {
      devplugin.ChannelGroup group=mChannelArr[i].getGroup();
      if (group!=null) {
        String gn=group.getId();
        if (!groupSet.contains(gn)) {
          if (gn!=null) groupSet.add(gn);
        }
      }
    }
    mGroupArr=new String[groupSet.size()];
    groupSet.toArray(mGroupArr);


		createGroupNameHashMap();

  }
  
  
  
  public void forceCompleteUpdateFor(String channel) {
    mRawDataProcessor.forceCompleteUpdateFor(channel);
  }


  public void createGroupnameFiles() throws PreparationException {
   
    /* A groupname file contains the name of the group in
     * an internationalized form */
    for (int i=0;i<mGroupArr.length;i++) {      
      File fromFile=new File(mPreparedDir,mGroupArr[i]+".txt");
      File toFileTxt=new File(mWorkDir,mGroupArr[i]+".txt");
      File toFile=new File(mWorkDir,mGroupArr[i]+"_info");
      try {
				util.io.IOUtilities.copy(fromFile,toFile);
        util.io.IOUtilities.copy(fromFile,toFileTxt);
			} catch (IOException exc) {
				throw new PreparationException("Could not copy file from "+fromFile.getAbsolutePath()+" to "+toFile.getAbsolutePath(),exc);
			}     
    }    
  }
  
 
  public void updateRawDataDir(/*boolean doUpdateOnly, boolean nodata*/) throws PreparationException {
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
    /*
    // Create the pds log directory
    if (! mPDSLogDir.exists()) {
      if (! mPDSLogDir.mkdir()) {
        throw new PreparationException("Could not create log directory: "
           + mPDSLogDir.getAbsolutePath());
        }   
    }
    
    // Make sure that the raw directory exists
    if (!mRawDir.exists()) {
      if (! mRawDir.mkdir()) {
        throw new PreparationException("Could not create raw directory: "
          + mRawDir.getAbsolutePath());
      }
    }
*/
    // Create a new raw directory if it does not exist
    if (! mRawDir.exists()) {
      if (! mRawDir.mkdir()) {
        throw new PreparationException("Could not create raw directory: "
          + mRawDir.getAbsolutePath());
      }
    }

    // Update the mirror lists
    updateMirrorLists();
  
  /*  
    if (!nodata) {
      // Get the new raw data
      loadNewRawData();
    }
   */
    
    // Process the new raw data
    mRawDataProcessor.processRawDataDir(mRawDir, mPreparedDir, mWorkDir);
    
    // Create a summary files
    createSummaryFiles();
    
 //   if (doUpdateOnly) {
 //     copyGroupnameFiles();
 //     copyChannelLists();      
 //   }
 //   else {
      // Create the group files
      createGroupnameFiles();
    
      // Create the channel list
      createChannelLists();
 //   }
/*
    if (doUpdateOnly) {
      // keep the old channel list file    
      File fromFile = new File(mPreparedDir, ChannelList.FILE_NAME);
      File toFile = new File(mWorkDir, ChannelList.FILE_NAME);    
      if (!fromFile.renameTo(toFile)) {
        throw new PreparationException("Renaming file '"
                  + fromFile.getAbsolutePath() + "' to '"
                  + toFile.getAbsolutePath() + "' failed");
      }
    }
    */
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
    mLog.info("In total there were "
      + NumberFormat.getInstance().format(mReadBytesCount)
      + " bytes read by the data services");
      
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

/*
  private void copyChannelLists() {
    
    // copy the channel list for the old system 
    copyChannelList(null);
    
    // copy the channel list of each group 
    for (int i=0;i<mGroupArr.length;i++) {
      copyChannelList(mGroupArr[i]);
    }    
  }
  */
  
  
  private void copyChannelList(String groupName)  {
    
   // keep the old channel list file    
   File fromFile = new File(mPreparedDir, (groupName!=null?groupName+"_":"")+ChannelList.FILE_NAME);
   File toFile = new File(mWorkDir, (groupName!=null?groupName+"_":"")+ChannelList.FILE_NAME);    
   if (!fromFile.renameTo(toFile)) {
         mLog.warning("Renaming file '"
                   + fromFile.getAbsolutePath() + "' to '"
                   + toFile.getAbsolutePath() + "' failed");
       }
    
    
  }


  private void createChannelLists() throws PreparationException {
    
    /* create the channel list for the old system */
    createChannelList(null);
    
    /* create the channel list of each group */
    for (int i=0;i<mGroupArr.length;i++) {
      createChannelList(mGroupArr[i]);
    }
  }

  private void createChannelList(String groupName) throws PreparationException {
    Date today = new Date();
    
    ChannelList list = new ChannelList(groupName);
    for (int serviceIdx = 0; serviceIdx < mDataServiceArr.length; serviceIdx++) {
      Channel[] channelArr = mDataServiceArr[serviceIdx].getAvailableChannels();
      for (int i = 0; i < channelArr.length; i++) {
        
        /* We are interested in channels of this group only */
        if (groupName!=null && !groupName.equals(channelArr[i].getGroup().getId())) {
          continue; 
        }
        
        list.addChannel(channelArr[i]);
        
        // Check whether the data service delivered up-to-date data for this
        // channel
        String rawFileName = DayProgramFile.getProgramFileName(today,
          channelArr[i].getCountry(), channelArr[i].getId());
        File rawFile = new File(mRawDir, rawFileName);
        if (! rawFile.exists()) {
          mLog.warning("Data service "
            + mDataServiceArr[serviceIdx].getClass().getName() + " did not "
            + "deliver up-to-date data for channel " + channelArr[i].getName()
            + ". (File " + rawFile.getAbsolutePath() + " does not exist)");
        }
      }
    }
    
    File file = new File(mWorkDir, (groupName!=null?groupName+"_":"")+ChannelList.FILE_NAME);
    try {
      list.writeToFile(file);
    }
    catch (Exception exc) {
      throw new PreparationException("Writing channel list for group "+groupName+" failed: "
        + file.getAbsolutePath(), exc);
    }
  }


  private void createSummaryFiles() throws PreparationException {

    /* create summary files for the new system */
    for (int i=0;i<mGroupArr.length;i++) {
			System.out.println("creating summary file for group "+mGroupArr[i]+"...");
      createSummaryFile(mGroupArr[i]);
    }

		System.out.println("summary files created.");
    
  }


  private void createGroupNameHashMap() {

		mGroupNameHashMap = new HashMap();

		for (int i=0; i<mDataServiceArr.length; i++) {
      Channel[] channelArr = mDataServiceArr[i].getAvailableChannels();
			for (int chInx=0; chInx<channelArr.length; chInx++) {
				ChannelGroup group = channelArr[chInx].getGroup();
				if (group!=null) {
          mGroupNameHashMap.put(channelArr[chInx].getId(), group.getId());
				}
			}

		}

  }
	
  private boolean channelBelongsToGroup(String channelId, String groupName) {

		String s = (String)mGroupNameHashMap.get(channelId);
		return groupName.equals(s);
  }
	
	
  private void createSummaryFile(String groupName)
    throws PreparationException
  {
    // Create the file
    SummaryFile summary = new SummaryFile();
    File[] fileArr = mWorkDir.listFiles();
		for (int fileIdx = 0; fileIdx < fileArr.length; fileIdx++) {
      String fileName = fileArr[fileIdx].getName();
			if (fileName.endsWith("_full.prog.gz")) {
        // This is a complete file -> Put its version to the summary
        try {
          Date date = DayProgramFile.getDateFromFileName(fileName);
          String country = DayProgramFile.getCountryFromFileName(fileName);
          String channelId = DayProgramFile.getChannelNameFromFileName(fileName);
          String levelName = DayProgramFile.getLevelFromFileName(fileName);

					if (channelBelongsToGroup(channelId, groupName)) {
						int level = DayProgramFile.getLevelIndexForId(levelName);
            if (level == -1) {
              throw new PreparationException("Day program file has unknown level '"
                + levelName + "': " + fileArr[fileIdx].getAbsolutePath());
            }
            int version = DayProgramFile.readVersionFromFile(fileArr[fileIdx]);
          
            summary.setDayProgramVersion(date, country, channelId, level, version);
					}
        }
        catch (Exception exc) {
          throw new PreparationException("Adding day program file to summary " +
            "failed: " + fileArr[fileIdx].getAbsolutePath(), exc);
        }
      }
    }

    System.out.println("writing summary to file...");
		
    // Save the file
    File file = new File(mWorkDir, (groupName==null?"":groupName+"_")+SummaryFile.SUMMARY_FILE_NAME);
    try {
      summary.writeToFile(file);
    }
    catch (Exception exc) {
      throw new PreparationException("Writing summary file failed: "
        + file.getAbsolutePath(), exc);
    }

		System.out.println("done.");
  }

  
  private void updateMirrorLists() throws PreparationException {    
    
    /* update the mirror list of the old system */
    updateMirrorList(null);
    
    /* update the mirror lists of each group */
    for (int i=0;i<mGroupArr.length;i++) {
      updateMirrorList(mGroupArr[i]);      
    }
    
  }
  
  private void updateMirrorList(String groupName) throws PreparationException {
    mLog.fine("Updating the mirror list");
    
    // Load the mirrorlist.txt
    Mirror[] mirrorArr = loadMirrorListTxt((groupName==null?"":groupName+"_")+"mirrorlist.txt");
        
    // Now update the weights. Use the old mirror list if a mirror is not
    // available
    Mirror[] oldMirrorArr = null;
    for (int mirrorIdx = 0; mirrorIdx < mirrorArr.length; mirrorIdx++) {
      Mirror mirror = mirrorArr[mirrorIdx];
          
      int weight = getMirrorWeight(mirror, groupName);
      if (weight >= 0) {
        mirror.setWeight(weight);
      } else {
        // We didn't get the weight -> Try to get the old weight
        if (oldMirrorArr == null) {
          oldMirrorArr = loadOldMirrorList(groupName);
        }
            
        for (int i = 0; i < oldMirrorArr.length; i++) {
              if (oldMirrorArr[i].getUrl().equals(mirror.getUrl())) {
                // This is the same mirror -> use the old weight
                mirror.setWeight(oldMirrorArr[i].getWeight());
              }
            }
          }
        }
    
        // Save the mirrorlist.gz
        File toFile = new File(mWorkDir, (groupName==null?"":groupName+"_")+Mirror.MIRROR_LIST_FILE_NAME);
        try {
          Mirror.writeMirrorListToFile(toFile, mirrorArr);
        }
        catch (IOException exc) {
          throw new PreparationException("Writing mirror list for group "+groupName+" failed", exc);
        }

        // Copy the mirrorlist.txt
        File fromFile = new File(mPreparedDir, (groupName==null?"":groupName+"_")+"mirrorlist.txt");
        toFile = new File(mWorkDir, (groupName==null?"":groupName+"_")+"mirrorlist.txt");
        try {
          IOUtilities.copy(fromFile, toFile);
        }
        catch (IOException exc) {
          throw new PreparationException("Copying mirrorlist.txt for group "+groupName+" failed", exc);
        }
  }
 

  private Mirror[] loadMirrorListTxt(String fileName) throws PreparationException {
    ArrayList mirrorList = new ArrayList();

    File fromFile = new File(mPreparedDir, fileName);
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(fromFile);
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() != 0) {
          mirrorList.add(new Mirror(line));
        }
      }
    }
    catch (Exception exc) {
      throw new PreparationException("Loading "+fileName+" failed", exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      } 
    }
        
    // Convert the list into an array
    Mirror[] mirrorArr = new Mirror[mirrorList.size()];
    mirrorList.toArray(mirrorArr);
    
    return mirrorArr;
  }


  private int getMirrorWeight(Mirror mirror, String groupName) {
    try {
      String url = mirror.getUrl() + "/weight"+(groupName!=null?"_"+groupName:"");
      byte[] data = IOUtilities.loadFileFromHttpServer(new URL(url));
      String asString = new String(data).trim();
      return Integer.parseInt(asString);
    }
    catch (Exception exc) {
      mLog.warning("Getting mirror weight of " + mirror.getUrl()
        + " for group "+groupName+" failed");
      return -1;
    }
  }


  private Mirror[] loadOldMirrorList(String groupName) {
    File fromFile = new File(mPreparedDir, (groupName==null?"":groupName+"_")+Mirror.MIRROR_LIST_FILE_NAME);
    try {
      return Mirror.readMirrorListFromFile(fromFile);
    }
    catch (Exception exc) {
      // There is no old mirror list -> return an empty array
      return new Mirror[0];
    }
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
      System.out.println("USAGE: PrimaryDataManager [-forceCompleteUpdate [channel{;channel}]] pds ...");
   /*   System.out.println("\nEXAMPLES:");
      System.out.println("Update tv data from ArdPDS and ZdfPDS:");
      System.out.println("PrimaryDataManager -update .ArdPDS .ZdfPDS");
      System.out.println("\nUpdate tv data from ArdPDS and ZdfPDS; force a complete update of channel zdf and ndr");
      System.out.println("PrimaryDataManager -update -forceCompleteUpdate zdf:ndr .ArdPDS .ZdfPDS");
      System.out.println("\nUpdate all channels; write new channel list file");
      System.out.println("PrimaryDataManager .ArdPDS .ZdfPDS .RtlPDS .Pro7PDS\n");
     */ 
      System.exit(1);
    } else {
      try {
        PrimaryDataManager manager = new PrimaryDataManager(new File("."));

        ArrayList pdsList = new ArrayList();
     //   boolean update=false;
     //   boolean nodata=false;
        for (int i = 0; i < args.length; i++) {
      //    if (args[i].equalsIgnoreCase("-update")) {
      //      update=true;            
      //    }
      //    else if (args[i].equalsIgnoreCase("-nodata")) {
      //      nodata=true;
      //    }
         /* else*/ if (args[i].equalsIgnoreCase("-forceCompleteUpdate")) {
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
            pdsList.add(createPrimaryDataService(args[i]));
          }
        }
        
        if (pdsList.size()==0) {
          System.out.println("Please specify at least one primary data service");
          System.exit(-1);
        }
        
        PrimaryDataService[] pdsArr = new PrimaryDataService[pdsList.size()];
        pdsList.toArray(pdsArr);        
        manager.setDataServiceArr(pdsArr);
        
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
