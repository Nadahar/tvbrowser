/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import java.util.zip.*;

import java.net.URL;

import javax.swing.JProgressBar;

import util.io.IOUtilities;

import devplugin.Date;
import tvdataloader.*;

/**
 * The DataService is a singletons that manages the TV data.
 *
 * @author Martin Oberhauser
 */
public class DataService implements devplugin.PluginManager {

  private static DataService mSingleton;
  
  private ProgramSelection programSelection=null;
  private boolean onlineMode=false;
  private TVDataServiceInterface tvdataloader=null;
  private JProgressBar progressBar;
  private boolean isDownloading=false;
  
  /** Holds for a Date (key) a DayProgram (value). Used as cache. */
  private HashMap mDayProgramHash;

  
  
  private DataService() {
    mDayProgramHash = new HashMap();
    
    progressBar = new JProgressBar();
    progressBar.setStringPainted(false);
    
    loadTvDataLoader();
  }



  private void loadTvDataLoader() {   
    // Get the tv data loader jar file
    String[] fList=new File("tvdataloader").list();  
    String className="";
    if (fList!=null && fList.length==1) {
      className=fList[0];
      if (className.length()>4) {
        className=className.substring(0,className.length()-4);
      }
    }

    String fName=className+".jar";
    File f=new File("tvdataloader",fName);  

    // create the tv data loader
    try {
      URL[] urls={ f.toURL() };
      ClassLoader dataloaderClassLoader=new java.net.URLClassLoader(urls,ClassLoader.getSystemClassLoader());

      Class c=dataloaderClassLoader.loadClass(className.toLowerCase()+"."+className);
      tvdataloader=(tvdataloader.TVDataServiceInterface)c.newInstance();
    } catch(java.net.MalformedURLException e) {
      e.printStackTrace();
    } catch(ClassNotFoundException e) {
      e.printStackTrace();
    } catch(IllegalAccessException e) {
      e.printStackTrace();
    } catch(InstantiationException e) {
      e.printStackTrace();
    }
  }
  
  
  
  /**
   * Gets the DataService singleton.
   */
  public static DataService getInstance() {
    if (mSingleton == null) {
      mSingleton = new DataService();
    }
    return mSingleton;
  }

  

  /**
   * 
   * @param newMode
   */
  public void setOnlineMode(boolean newMode) {
    if ((newMode == onlineMode) || (tvdataloader == null)) {
      return;
    }
    
    onlineMode = newMode;
    
    if (newMode) {
      try {
        tvdataloader.connect();
      } catch(IOException exc) {
        exc.printStackTrace();
      }
    } else {
      try {
        tvdataloader.disconnect();
      } catch(IOException exc) {
        exc.printStackTrace();
      }
    }
  }
  
 

  public boolean isOnlineMode() {
    return onlineMode;
  }

  

  public void startDownload(int daysToDownload) {
    if (tvdataloader == null) {
      return;
    }
    
    progressBar.setString("connecting...");
    progressBar.setStringPainted(true);
    try {
      tvdataloader.connect();
    }
    catch (IOException exc) {
      System.err.println("Connecting to server failed: " + exc);
      exc.printStackTrace();
      return;
    }
    finally {
      progressBar.setStringPainted(false);
    }
    
    tvdataloader.AbstractChannelDayProgram prog;
    ObjectOutputStream out;
    Channel[] subscribedChannels=ChannelList.getSubscribedChannels();
    progressBar.setMaximum((daysToDownload + 2) * subscribedChannels.length);
    devplugin.Date date=new Date();
    date.addDays(-1); // get yesterday too
    for (int i = 0; i < daysToDownload + 2; i++) {
      for (int j = 0; (j < subscribedChannels.length) && isDownloading; j++) {
        progressBar.setValue(i * subscribedChannels.length + j + 1);
        
        devplugin.Channel channel=subscribedChannels[j];
        File file=new File(Settings.DATA_DIR,""+channel.getId()+"_"+date.getDaysSince1970());
        if (file.exists()) {
          continue;
        }
        
        try {
          prog = tvdataloader.downloadDayProgram(date,channel);
        }
        catch (IOException exc) {
          System.err.println("Downloading day program for " + channel.getName()
            + " on " + date + " failed: " + exc);
          exc.printStackTrace();
          continue;
        }

        if (prog == null) {
          continue;
        }

        try {
          out=new ObjectOutputStream(new FileOutputStream(file));
          out.writeObject(prog);
          out.close();
        }catch(IOException e) {
          e.printStackTrace();
        }
      }
      date.addDays(1);
    }
    try {
      tvdataloader.disconnect();
    }
    catch (IOException exc) {
      System.err.println("Disonnecting from server failed: " + exc);
      exc.printStackTrace();
    }
  }

  
  
  /**
   * Returns the day program for the specified date.
   * 
   * @param date The date to get the day program for.
   * @throws NullPointerException if date is null.
   */
  public DayProgram getDayProgram(devplugin.Date date) {
    // if date is null throw a NullPointerException
    if (date == null) {
      throw new NullPointerException("date is null!");
    }

    // try to get the DayProgram from the cache.
    DayProgram dayProgram = (DayProgram) mDayProgramHash.get(date);
    
    if (dayProgram == null) {
      try {
        // The program is not in the cache -> try to load it
        System.out.println("Loading program for " + date + " (" + date.hashCode() + ")");
        dayProgram = loadDayProgram(date);
        System.out.println("Loading program " + ((dayProgram == null) ? "failed" : "suceed"));

        // put it in the cache
        if (dayProgram != null) {
          mDayProgramHash.put(date, dayProgram);
        }
      }
      catch (IOException exc) {
        exc.printStackTrace();
      }
    }
    
    return dayProgram;
  }
    

  
  /**
   * Loads the day program for the specified date.
   */
  protected DayProgram loadDayProgram(devplugin.Date date) throws IOException {
    if (tvdataloader == null) {
      return null;
    }

    Channel[] channels=ChannelList.getSubscribedChannels();
    
    boolean useProgressBar=false;
    if (isOnlineMode() && !dataAvailable(date)) {
      useProgressBar=true;
      progressBar.setMaximum(channels.length);
    }
    
    DayProgram result = new DayProgram();    
    for (int i=0;i<channels.length;i++) {
      if (useProgressBar) {
        progressBar.setValue(i+1);
      }
      File file=new File(Settings.DATA_DIR,""+channels[i].getId()+"_"+date.getDaysSince1970());
      if (file.exists()) {
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(file));
        try {
          tvdataloader.AbstractChannelDayProgram prog=tvdataloader.readChannelDayProgram(in);

          if (prog!=null) {
            result.addChannelDayProgram(prog);
          }
        }catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        in.close();
      }else if (isOnlineMode()&&isDownloading) {
        tvdataloader.AbstractChannelDayProgram prog=tvdataloader.downloadDayProgram(date, channels[i]);

        if (prog!=null) {
          ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(file));
          out.writeObject(prog);
          out.close();
          result.addChannelDayProgram(prog);
        }
      }
    }

    if (! result.iterator().hasNext()) {
      // day program is empty -> return null
      return null;
    } else {
      return result;
    }
  }



  /**
   * returns true, if tv data are available for the given date.
   * @param date
   */
  public static boolean dataAvailable(devplugin.Date date) {
    final String dateStr = "" + date.getDaysSince1970();
    
    String fList[] = new File(Settings.DATA_DIR).list(
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
   * Gets a program.
   * <p>
   * Returns null, if the specified program could not be found.
   */
  public devplugin.Program getProgram(devplugin.Date date, String progID) {
    DayProgram dayProgram = getDayProgram(date);
    
    if (dayProgram == null) {
      return null;
    } else {
      return dayProgram.getProgram(progID);
    }
  }



  public void setSelection(ProgramSelection selection) {
    programSelection=selection;
  }



  public ProgramSelection getSelection() {
    return programSelection;
  }



  public void markProgram(devplugin.Program prog, devplugin.Plugin plugin) {
    if (programSelection==null) {
      programSelection=new ProgramSelection();
    }
    programSelection.addProgram(prog,plugin);
  }



  public javax.swing.JComponent createProgramPanel(devplugin.Program prog) {
    return new tvbrowser.ui.programtable.ProgramPanel(prog);
  }
  
  
  
  /**
   * Gets the progress bar used to show the download progress.
   */
  public JProgressBar getProgressBar() {
    return progressBar;
  }
  
  
  
  /**
   * Gets whether the data service is currently downloading data.
   */
  public boolean isDownloading() {
    return isDownloading;
  }

  
  
  /**
   * Sets whether the data service is currently downloading data.
   */
  public void setIsDownloading(boolean isDownloading) {
    // TODO: the data service should not have to told whether it is
    //       downloading. (It should know it by itself).
    this.isDownloading = isDownloading;
  }
  
  
  
  /**
   * Gets the subscribed channels.
   */
  public devplugin.Channel[] getSubscribedChannels() {
    return ChannelList.getSubscribedChannels();
  }
  
  
  
  /**
   * Gets an iterator through all programs of the specified channel at the
   * specified date.
   * <p>
   * If the requested data is not available, null is returned.
   */
  public Iterator getChannelDayProgram(devplugin.Date date, devplugin.Channel channel) {
    DayProgram dayProgram = getDayProgram(date);
    if (dayProgram == null) {
      return null;
    }
    
    Iterator dayProgramIter = dayProgram.iterator();
    while (dayProgramIter.hasNext()) {
      AbstractChannelDayProgram channelDayProgram
        = (AbstractChannelDayProgram) dayProgramIter.next();
      if (channelDayProgram.getChannel().equals(channel)) {
        return channelDayProgram.getPrograms();
      }
    }
    
    return null;
  }

  
  
  /**
   * Imports previously exported tv data.
   * <p>
   * In fact the files that are not already present in the tvdata directory
   * are extracted from the zip file.
   *
   * @param targetFile The file to export the tv data to.
   */
  public void importTvData(File srcFile) throws IOException {
    ZipFile zipFile = null;
    InputStream in = null;
    
    try {
      zipFile = new ZipFile(srcFile);
      Enumeration entryEnum = zipFile.entries();
      while (entryEnum.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entryEnum.nextElement();

        if (entry.getName().startsWith(Settings.DATA_DIR)) {
          File tvdataFile = new File(entry.getName());
          if (! tvdataFile.exists()) {
            in = zipFile.getInputStream(entry);
            IOUtilities.saveStream(in, tvdataFile);
            in.close();
          }
        }
      }
    }
    finally {
      if (zipFile != null) {
        try { zipFile.close(); } catch (IOException exc) {}
      }
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }
  
  
  
  /**
   * Exports all known tv data to the specified file.
   * <p>
   * In fact the tvdata directory is packed into a zip file.
   *
   * @param targetFile The file to export the tv data to.
   */
  public void exportTvData(File targetFile) throws IOException {
    File tvdataDir = new File(Settings.DATA_DIR);
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
        String fileName = Settings.DATA_DIR + File.separator + children[i].getName();
        ZipEntry entry = new ZipEntry(fileName);
        
        zipOut.putNextEntry(entry);
        in = new FileInputStream(children[i]);
        IOUtilities.pipeStreams(in, zipOut);
        in.close();
      }
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

}