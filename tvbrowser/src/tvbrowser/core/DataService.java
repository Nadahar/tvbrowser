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

package tvbrowser.core;

import java.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import java.util.zip.*;
import java.util.logging.Level;
import java.util.regex.*;

import javax.swing.JProgressBar;

import util.exc.*;
import util.io.IOUtilities;
import util.ui.progress.ProgressMonitorGroup;

import java.awt.Font;

import devplugin.*;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;

import tvdataservice.MutableProgram;
import tvdataservice.TvDataBase;
import tvdataservice.TvDataService;
import tvdataservice.MutableChannelDayProgram;

/**
 * The DataService is a singletons that manages the TV data.
 *
 * @author Martin Oberhauser
 */
public class DataService implements devplugin.PluginManager {

	private static Font PLAINFONT=new Font("Dialog",Font.PLAIN,12);
	private static Font BOLDFONT=new Font("Dialog",Font.BOLD,12);
	

  /** The logger for this class. */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(DataService.class.getName());

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DataService.class);

  /** The singleton. */
  private static DataService mSingleton;

  /** <CODE>true</CODE> if we are in online mode. */
  //private boolean onlineMode=false;

  /** The progress bar to use for showing the update process. */
  private JProgressBar progressBar;

  /** <CODE>True</CODE> if we are currently downloading TV data. */
  private boolean mIsDownloading = false;

  /** Holds for a Date (key) a DayProgram (value). Used as cache. */
  private HashMap mDayProgramHash;



  /**
   * Creates a new instance of DataService.
   */
  private DataService() {
    mDayProgramHash = new HashMap();

    progressBar = new JProgressBar();
    progressBar.setStringPainted(false);

    //loadTvDataService();
  }



  /**
   * Gets the DataService singleton.
   *
   * @return the DataService singleton.
   */
  public static DataService getInstance() {
    if (mSingleton == null) {
      mSingleton = new DataService();
    }
    return mSingleton;
  }



  /**
   * Gets whether we are in online mode.
   *
   * @return whether we are in online mode.
   */
  
	public void stopDownload() {
		mIsDownloading=false;
	}

  /**
   * Starts the download of new TV data
   *
   * @param daysToDownload The number of days until today to download the
   *        program for.
   */
  public void startDownload(int daysToDownload) {
    // TODO: Update the progress bar
    
    // Set the download flag
    mIsDownloading = true;
    
    // Limit the days to download to 3 weeks
    if (daysToDownload > 21) {
      daysToDownload = 21;
    }

    // Add a day to the daysToDownload for yesterday and today
    daysToDownload += 2;
    
    // Ensure that the tvdata directory exists
    File tvdataDir = new File(Settings.getTVDataDirectory());
    if (! tvdataDir.exists()) {
      tvdataDir.mkdir();
    }
    
    // Create a interactor that translates the database orders
    TvDataBase dataBaseInteractor = new TvDataBase() {
      public void updateDayProgram(MutableChannelDayProgram program) {
        correctChannelDayProgram(program, false);
        updateChannelDayProgram(program);
      }
  
      public boolean isDayProgramAvailable(Date date, Channel channel) {
        return isChannelDayProgramAvailable(date, channel);
      }
      
      public boolean cancelDownload() {
        return ! mIsDownloading;
      }
    };
    
    // Get the start date
    devplugin.Date startDate = new Date().addDays(-1);
    
    // Split the subsribed channels by data service
    Channel[] subscribedChannels = ChannelList.getSubscribedChannels();
    UpdateJob[] jobArr = toUpdateJobArr(subscribedChannels);
    
    // Create the ProgressMonitorGroup
    ProgressMonitorGroup monitorGroup
      = new ProgressMonitorGroup(progressBar, subscribedChannels.length);
    
    // Work on the job list
    Throwable downloadException = null;
    for (int i = 0; i < jobArr.length; i++) {
      TvDataService dataService = jobArr[i].getDataService();
      Channel[] channelArr = jobArr[i].getChannelList();
      ProgressMonitor monitor = monitorGroup.getNextProgressMonitor(channelArr.length);
      try {
        dataService.updateTvData(dataBaseInteractor, channelArr, startDate,
          daysToDownload, monitor);
      }
      catch (Throwable thr) {
        mLog.log(Level.WARNING, "Updating the TV data for TV data service "
          + dataService.getInfo().getName() + " failed", thr);
        
        downloadException = thr;
      }
      
      // Check whether the download was canceled
      if (! mIsDownloading) {
        break;
      }
    }

    // Show the exception if there was one
    if (downloadException != null) {
      String msg = mLocalizer.msg("error.7", "Couldn't download the whole program!");
      ErrorHandler.handle(msg, downloadException);
    }

    // Let the plugins react on the new data
    PluginManager.fireTvDataChanged();
    
    // Reset the download flag
    mIsDownloading = false;
  }



  private UpdateJob[] toUpdateJobArr(Channel[] subscribedChannels) {
    ArrayList jobList = new ArrayList();
    for (int channelIdx = 0; channelIdx < subscribedChannels.length; channelIdx++) {
      Channel channel = subscribedChannels[channelIdx];
      
      // Get the UpdateJob for this channel
      UpdateJob job = null;
      for (int i = 0; i < jobList.size(); i++) {
        UpdateJob currJob = (UpdateJob) jobList.get(i);
        if (currJob.getDataService().equals(channel.getDataService())) {
          job = currJob;
          break;
        }
      }
      if (job == null) {
        // There is no job fo this channel -> create one
        job = new UpdateJob(channel.getDataService());
        jobList.add(job);
      }
      
      // Add the channel to the UpdateJob
      job.addChannel(channel);
    }
    
    // Convert the list into an array and return it
    UpdateJob[] jobArr = new UpdateJob[jobList.size()];
    jobList.toArray(jobArr);
    return jobArr;
  }



  /**
   * Gets the day program for the specified date.
   *
   * @param date The date to get the day program for.
   * @param allowDownload false, if no dataservice call is required
   * @return the day program for the specified date.
   */
  public DayProgram getDayProgram(devplugin.Date date, int progressStart,
    int progressEnd)
  {
    // if date is null throw a NullPointerException
    if (date == null) {
      throw new NullPointerException("date is null!");
    }

    // try to get the DayProgram from the cache.
    DayProgram dayProgram = (DayProgram) mDayProgramHash.get(date);

    if (dayProgram == null) {
      try {
        // The program is not in the cache -> try to load it
        dayProgram = loadDayProgram(date, progressStart, progressEnd);
        // mLog.info("Loading program for " + date + " (" + date.hashCode() + ") "
        //   + ((dayProgram == null) ? "failed" : "suceed"));
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
    progressBar.setValue(progressEnd);

    return dayProgram;
  }


  private MutableChannelDayProgram loadChannelDayProgramFromDisk(
    File file) {
    MutableChannelDayProgram prog = null;
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(file));
      prog = new MutableChannelDayProgram(in);
    } catch (Exception exc) {
      //throw new TvBrowserException(getClass(), "error.1",
      //	   "Error when reading program of {0} on {1}!\n({2})",
      //	   "unknown", "unknown", file.getAbsolutePath(), exc);
      file.delete();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException exc) {
        }
      }
    }
    return prog;
  }


  /**
   * Loads the day program for the specified date. If the program could not be
   * found on the disk, it will be downloaded if we are in online mode or if
   * there is currently an update running.
   *
   * @param date The date to download the program for.
   * @throws TvBrowserException If the download failed.
   * @return The DayProgram for the specified day.
   */
  protected DayProgram loadDayProgram(devplugin.Date date, int progressStart,
    int progressEnd)
    throws TvBrowserException
  {
    Channel[] channels=ChannelList.getSubscribedChannels();

    boolean useProgressBar=false;
   
    // Get the day program for the specified date from the cache
    DayProgram dayProgram = (DayProgram) mDayProgramHash.get(date);
    if (dayProgram == null) {
      dayProgram = new DayProgram(date);
    }

    // Load or download all missing channel day programs
    boolean someDataWasDownloaded = false;
    for (int i = 0; i < channels.length; i++) {
      // Update the progress bar
      TvDataService dataService = channels[i].getDataService();
      if (useProgressBar) {
        progressBar.setValue((progressEnd-progressStart)/channels.length*(i+1)+progressStart);
      }

      // Check whether this channel day program is already present
      if (dayProgram.getChannelDayProgram(channels[i]) != null) {
        continue;
      }

      // Check whether we have it on disk
      File file = getChannelDayProgramFile(date, channels[i]);
      if (file.exists()) {
        // We have it on disk -> load it
        MutableChannelDayProgram prog = loadChannelDayProgramFromDisk(file);
        if (prog != null) {
          // NOTE: We have to correct the ChannelDayProgram every time we load
          //       it because it may be that we couldn't get the length of the
          //       last program last time (because we didn't know the program
          //       of the next day)
          //       If there is nothing to correct this is fast anyway.
          correctChannelDayProgram(prog, true);
          
          // Add the loaded ChannelDayProgram
          dayProgram.addChannelDayProgram(prog);
        }
      }
    }

    // If the day program is not empty -> return it and put it in the cache
    if (! dayProgram.iterator().hasNext()) {
      // day program is empty -> return null
      return null;
    } else {
      mDayProgramHash.put(date, dayProgram);

      // Let the plugins react on the new data
      if (someDataWasDownloaded) {
        PluginManager.fireTvDataChanged();
      }

      return dayProgram;
    }
  }



  /**
   * Checks whether all programs have a length. If not the length will be
   * calculated
   * 
   * @param prog The program to correct
   */
  private void correctChannelDayProgram(MutableChannelDayProgram channelProg,
    boolean updateOnChange)
  {
    boolean somethingWasChanged = false;

    // Go through all programs and correct them
    // (This is fast, if no correction is needed)
    for (int progIdx = 0; progIdx < channelProg.getProgramCount(); progIdx++) {
      Program program = channelProg.getProgramAt(progIdx);
      if (! (program instanceof MutableProgram)) {
        continue;
      }
      
      MutableProgram prog = (MutableProgram) program;
      
      if (prog.getLength() <= 0) {
        // Try to get the next program
        Program nextProgram = null;
        if ((progIdx + 1) < channelProg.getProgramCount()) {
          // Try to get it from this ChannelDayProgram
          nextProgram = channelProg.getProgramAt(progIdx + 1);
        } else {
          // This is the last program -> Try to get the first program of the
          // next ChannelDayProgram
          Date nextDate = channelProg.getDate().addDays(1);
          Channel channel = channelProg.getChannel();
          Iterator nextIter = getChannelDayProgram(nextDate, channel);
          if ((nextIter != null) && (nextIter.hasNext())) {
            nextProgram = (Program) nextIter.next();
          }
        }
        
        // Calculate the Length
        if (nextProgram != null) {
          int startTime = prog.getHours() * 60 + prog.getMinutes();
          int endTime = nextProgram.getHours() * 60 + nextProgram.getMinutes();
          if (endTime < startTime) {
            // The program ends the next day
            endTime += 24 * 60;
          }
          
          int length = endTime - startTime;
          // Only allow a maximum length of 12 hours
          if (length < 12 * 60) {
            prog.setLength(length);
            somethingWasChanged = true;
          }
        }
      }
    }

    if (somethingWasChanged && updateOnChange) {
      // Update the changes
      updateChannelDayProgram(channelProg);
    }
  }



  /**
   * Returns true, if tv data is available on disk for the given date.
   *
   * @param date The date to check.
   * @return if the data is available.
   */
  public static boolean dataAvailable(devplugin.Date date) {
    
    final String dateStr=date.getDateString();

    String fList[] = new File(Settings.getTVDataDirectory()).list(
      new java.io.FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(dateStr);
        }
      }
    );

    if (fList == null) return false;
    return fList.length > 0;
  }

/**
 * Deletes expired tvdata files older then lifespan days.
 * @param lifespan
 */
  public static void deleteExpiredFiles(int lifespan) {
	
    if (lifespan<0) {
      return;  // manually
    }
    devplugin.Date d1=new devplugin.Date();
    final devplugin.Date d=d1.addDays(-lifespan);
    
		File fList[]=new File(Settings.getTVDataDirectory()).listFiles(
			new java.io.FilenameFilter() {
				public boolean accept(File dir, String name) {				
					int p=name.lastIndexOf('.');
					String s=name.substring(p+1,name.length());
          int val;
          try {
					   val=Integer.parseInt(s);
          }catch(NumberFormatException e) {
            return false;
          }
          int year=val/10000;
          int r=val%10000;
          int month=r/100;
          int day=r%100;
          Date curDate=new Date(val/10000,r/100,r%100);
          return curDate.getValue()<d.getValue();
				}
			}	
		);
		if (fList!=null) {
			for (int i=0;i<fList.length;i++) {
				fList[i].delete();
			}
		}
	
	
}




  /**
   * Gets a program.
   * <p>
   * Returns <CODE>null</CODE>, if the specified program could not be found.
   *
   * @param date The date to get the data for.
   * @param progID The ID of the program to get the data for.
   * @return The program for the specified channel and date.
   */
  public devplugin.Program getProgram(devplugin.Date date, String progID) {
  	
  	DayProgram dayProgram = getDayProgram(date, -1, -1);

    if (dayProgram == null) {
      return null;
    } else {
      return dayProgram.getProgram(progID);
    }
  }



  /**
   * Gets the progress bar used to show the download progress.
   *
   * @return the progress bar used to show the download progress.
   */
  public JProgressBar getProgressBar() {
    return progressBar;
  }



  /**
   * Gets whether the data service is currently downloading data.
   *
   * @return <CODE>true</CODE>, if the data service is currently downloading data.
   */
  public boolean isDownloading() {
    return mIsDownloading;
  }



  /**
   * Gets the subscribed channels.
   *
   * @return the subscribed channels.
   */
  public devplugin.Channel[] getSubscribedChannels() {
    return ChannelList.getSubscribedChannels();
  }



  /**
   * Called, when the ChannelList has changed.
   */
  public void subscribedChannelsChanged() {
    Iterator dayProgramIter = mDayProgramHash.values().iterator();
    while (dayProgramIter.hasNext()) {
      DayProgram dayProgram = (DayProgram) dayProgramIter.next();
      try {
        loadDayProgram(dayProgram.getDate(), -1,-1);
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }



  /**
   * Gets an iterator through all programs of the specified channel at the
   * specified date.
   * <p>
   * If the requested data is not available, null is returned.
   *
   * @param date The date to get the programs for.
   * @param channel The channel to get the programs for.
   * @return the programs of the specified channel and date.
   */
  public Iterator getChannelDayProgram(devplugin.Date date, devplugin.Channel channel) {
    DayProgram dayProgram = getDayProgram(date, -1, -1);
    if (dayProgram == null) {
      return null;
    }

    ChannelDayProgram channelDayProgram = dayProgram.getChannelDayProgram(channel);
    if (channelDayProgram == null) {
      return null;
    }

    return channelDayProgram.getPrograms();
  }



  /**
   * Imports previously exported tv data.
   * <p>
   * In fact the files that are not already present in the tvdata directory
   * are extracted from the zip file.
   *
   * @param srcFile The file to import the tv data from.
   * @throws TvBrowserException If the import failed.
   */
  public void importTvData(File srcFile) throws TvBrowserException {
    ZipFile zipFile = null;
    InputStream in = null;

    try {
      zipFile = new ZipFile(srcFile);
      Enumeration entryEnum = zipFile.entries();
      while (entryEnum.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entryEnum.nextElement();

        File tvdataFile = new File(Settings.getTVDataDirectory(), entry.getName());
        if (! tvdataFile.exists()) {
          in = zipFile.getInputStream(entry);
          IOUtilities.saveStream(in, tvdataFile);
          in.close();
        }
      }
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.3",
        "Importing tv data failed!\n({0})", srcFile.getAbsolutePath(), exc);
    }
    finally {
      if (zipFile != null) {
        try { zipFile.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }

    // Let the plugins react on the new data
    PluginManager.fireTvDataChanged();
  }



  /**
   * Exports all known tv data to the specified file.
   * <p>
   * In fact the tvdata directory is packed into a zip file.
   *
   * @param targetFile The file to export the tv data to.
   * @throws TvBrowserException If the export failed.
   */
  public void exportTvData(File targetFile) throws TvBrowserException {
    File tvdataDir = new File(Settings.getTVDataDirectory());
    File[] children = tvdataDir.listFiles();

    // When the tvdata directory is empty -> do nothing
    if (children == null) {
      // No data available
      return;
    }

    FileOutputStream out = null;
    ZipOutputStream zipOut = null;
    FileInputStream in = null;

    try {
      out = new FileOutputStream(targetFile);
      zipOut = new ZipOutputStream(out);

      // Add all files in the tvdata directory to the zip file
      for (int i = 0; i < children.length; i++) {
        String fileName = children[i].getName();
        ZipEntry entry = new ZipEntry(fileName);

        zipOut.putNextEntry(entry);
        in = new FileInputStream(children[i]);
        IOUtilities.pipeStreams(in, zipOut);
        in.close();
      }
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.4",
        "Exporting tv data failed!\n({0})", targetFile.getAbsolutePath(), exc);
    }
    finally {
      if (zipOut != null) {
        try { zipOut.close(); } catch (IOException exc) {}
      }
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }


public boolean search(Program prog, Pattern pattern, boolean inTitle, boolean inText) {
    
    //Program prog = (Program) programIter.next();
    boolean matches = false;

    if (inTitle) {
        Matcher matcher = pattern.matcher(prog.getTitle());
        matches = matcher.matches();
    }
    
    if ((! matches) && inText) {
        Matcher matcher = pattern.matcher(prog.getDescription());
        matches = matcher.matches();
    }
    
    return matches;
}

  /**
   * Searches the data for programs which match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param inTitle Should be searched in the title?
   * @param inText Should be searched in the desription?
   * @param caseSensitive Should the search be case sensitive?
   * @param channels The channels to search in.
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   * @return The matching programs.
   */
  public devplugin.Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, devplugin.Channel[] channels,
    devplugin.Date startDate, int nrDays)
    throws TvBrowserException
  {
    int flags = 0;
    if (! caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
    }

    Pattern pattern;
    try {
      pattern = Pattern.compile(regex, flags);
    }
    catch (PatternSyntaxException exc) {
      throw new TvBrowserException(getClass(), "error.8",
        "Syntax error in the regualar expression of the search pattern!", exc);
    }

    if (nrDays < 0) {
      //startDate.addDays(nrDays);
      /*startDate=*/startDate.addDays(nrDays);
      nrDays = 0 - nrDays;
    }

    ArrayList hitList = new ArrayList();
    int missingDataCount = 0;
    for (int day = 0; day <= nrDays; day++) {
      for (int channelIdx = 0; channelIdx < channels.length; channelIdx++) {
        devplugin.Channel channel = channels[channelIdx];
        Iterator programIter = getChannelDayProgram(startDate, channel);
        if (programIter == null) {
          // Give up if we didn't get data for tenth time
          missingDataCount++;
          if (missingDataCount > 10) {
            // There is no more data -> stop
            day = nrDays;
          }
        } else {
          while (programIter.hasNext()) {
            Program prog = (Program) programIter.next();
            boolean matches = false;

            if (inTitle) {
              Matcher matcher = pattern.matcher(prog.getTitle());
              matches = matcher.matches();
            }
            if ((! matches) && inText) {
              Matcher matcher = pattern.matcher(prog.getDescription());
              matches = matcher.matches();
            }

            if (matches) {
              hitList.add(prog);
            }
          }
        }
      }

      // The next day
      startDate=startDate.addDays(1);
     //startDate=startDate.addDays(1);
    }

    Program[] hitArr = new Program[hitList.size()];
    hitList.toArray(hitArr);

    return hitArr;
  }


  public boolean isChannelDayProgramAvailable(Date date, Channel channel) {
    File file = getChannelDayProgramFile(date, channel);
    return file.exists();
  }
  
  
  protected void updateChannelDayProgram(MutableChannelDayProgram prog) {
    mLog.info("Updating day prog: " + prog.getChannel().getName()
      + " " + prog.getDate());
    
    // save the program to disk
    File file = getChannelDayProgramFile(prog.getDate(), prog.getChannel());
    
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file));
      prog.writeData(out);
    }
    catch (IOException exc) {
      String msg = mLocalizer.msg("error.2",
        "Error when saving program of {0} on {1}!\n({2})",
      prog.getChannel().getName(), prog.getDate(), file.getAbsolutePath());
      ErrorHandler.handle(msg, exc);

      prog = null;
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
    
    // Check whether its dayProgram is in the cache
    DayProgram dayProgram = (DayProgram) mDayProgramHash.get(prog.getDate());
    if (dayProgram != null) {
      // It is in the cache -> Update it
      dayProgram.addChannelDayProgram(prog);
    }
  }
  

  public devplugin.Plugin[] getInstalledPlugins() {
  	return PluginManager.getInstalledPlugins();
  }



  private File getChannelDayProgramFile(devplugin.Date date,
    devplugin.Channel channel)
  {
   /* String fileName = "" + channel.getId()
      + "_" + channel.getDataService().getClass().getName()
      + "." + date.getDaysSince1970();
   */   
    String fileName = "" + channel.getCountry() + "_" + channel.getId()
          + "_" + channel.getDataService().getClass().getPackage().getName()
          + "." + date.getDateString();   
      
      
    return new File(Settings.getTVDataDirectory(), fileName);
  }



  public TvDataService getDataService(String className) {
    return TvDataServiceManager.getInstance().getDataService(className);
  }


  /**
   * Creates a context menu containg all subscribed plugins that support context
   * menues.
   *
   * @return a plugin context menu.
   */
  public javax.swing.JPopupMenu createPluginContextMenu(final Program program, devplugin.Plugin caller) {
	Font font=BOLDFONT;
	javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
	devplugin.Plugin[] pluginArr = PluginManager.getInstalledPlugins();
	for (int i = 0; i < pluginArr.length; i++) {
	  final devplugin.Plugin plugin = pluginArr[i];
	  if (!plugin.equals(caller)) {
		String text = plugin.getContextMenuItemText();
		if (text != null) {
		  javax.swing.JMenuItem item = new javax.swing.JMenuItem(text);
		  item.setFont(font);
		  font=PLAINFONT;
		  item.setIcon(plugin.getMarkIcon());
		  item.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
			  plugin.execute(program);
			}
		  });
		  menu.add(item);
		}
	  }
	}
	return menu;
  }


  // inner class UpdateJob


  private class UpdateJob {
    
    private TvDataService mTvDataService;
    private ArrayList mChannelList;
    
    
    public UpdateJob(TvDataService dataService) {
      mTvDataService = dataService;
      mChannelList = new ArrayList();
    }
    
    
    public void addChannel(Channel channel) {
      mChannelList.add(channel);
    }
    
    
    public TvDataService getDataService() {
      return mTvDataService;
    }
    
    
    public Channel[] getChannelList() {
      Channel[] channelArr = new Channel[mChannelList.size()];
      mChannelList.toArray(channelArr);
      return channelArr;
    }

  }

}