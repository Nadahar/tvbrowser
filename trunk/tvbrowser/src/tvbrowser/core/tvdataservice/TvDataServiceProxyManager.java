/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.core.tvdataservice;

import tvdataservice.TvDataService;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLClassLoader;
import tvbrowser.core.Settings;
import util.exc.ErrorHandler;


/**
 * Manages the TvDataServices
 */
public class TvDataServiceProxyManager {

  public static final String PLUGIN_DIRECTORY = "tvdataservice";

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(TvDataServiceProxyManager.class);

  private static java.util.logging.Logger mLog
      = Logger.getLogger(TvDataServiceProxyManager.class.getName());

  private static TvDataServiceProxyManager mInstance;

  /**
   * The list of all installed TvDataServices
   */
  private ArrayList mProxyList;

  private TvDataServiceProxyManager() {
    mProxyList = new ArrayList();
  }

  public static TvDataServiceProxyManager getInstance() {
    if (mInstance == null) {
      mInstance = new TvDataServiceProxyManager();
    }
    return mInstance;
  }

  /**
   * If a TvDataService is downloaded and installed by the TV-Browser's
   * online updater, the TvDataService is stored as a ".jar.inst" files.
   * During start up, befor loading the plugins, we rename the files to ".jar"

  private void installPendingDataServices() {
    File file=new File(TV_DATA_SERVICE_DIRECTORY);
    if (!file.exists()) {
      return;
    }
    File[] fileList=file.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".jar.inst");
      }
    });

    for (int i=0;i<fileList.length;i++) {
      String fName=fileList[i].getAbsolutePath();
      fileList[i].renameTo(new File(fName.substring(0,fName.length()-5)));
    }
  }

         */
  public void registerTvDataService(TvDataServiceProxy service) {
     mProxyList.add(service);
  }

  /**
   * @param file
   * @return TvDataServiceProxy object
   */
  private TvDataServiceProxy loadTvDataService(File file) {
    String fileName = file.getName();
    if (fileName.length()>4) {
      String className = fileName.substring(0,fileName.length()-4);
      className = className.toLowerCase()+"."+className;
      try {
        URL[] urls= new URL[] { file.toURL() };
        ClassLoader dataserviceClassLoader=new URLClassLoader(urls,ClassLoader.getSystemClassLoader());
        Class c=dataserviceClassLoader.loadClass(className);
        Object dataservice = c.newInstance();
        if (dataservice instanceof TvDataService) {
          return new DeprecatedTvDataServiceProxy((TvDataService)dataservice);
        }
      } catch (Exception exc) {
        mLog.log(Level.SEVERE, "File '+file.getAbsolutePath()+' is not a valid TvDataService", exc);
      }
    }
    else {
      mLog.warning("File '"+file.getAbsolutePath()+"' is not a valid TvDataService");
    }
    return null;
  }

                /*
  private void loadAllTvDataServices() {
    if (mProxyList != null) {
      throw new IllegalArgumentException("The data services are already loaded!");
    }

    mProxyList = new ArrayList();

    // Get the tv data service jar file
    File[] fList=new File(TV_DATA_SERVICE_DIRECTORY).listFiles(new FilenameFilter() {
      public boolean accept(File dir, String fName) {
        return fName.endsWith(".jar");
      }
    });

    if (fList!=null) {
      for (int i=0; i<fList.length; i++) {
        TvDataServiceProxy proxy = loadTvDataService(fList[i]);
        if (proxy != null) {
          mProxyList.add(proxy);
        }
      }
    }
  }
                  */

  private void loadServiceSettings(TvDataServiceProxy service) {
    Class c=service.getClass();
    String dir=Settings.getUserDirectoryName();
    File f=new File(dir,c.getName()+".service");
    if (f.exists()) {
      try {
        Properties p=new Properties();
        FileInputStream in=new FileInputStream(f);
        p.load(in);
        in.close();
        service.loadSettings(p);
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.3", "Loading settings for plugin {0} failed!\n({1})",
            service.getInfo().getName(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }else{
      service.loadSettings(new Properties());
    }
  }

  private void storeServiceSettings(TvDataServiceProxy service) {
    Properties prop=service.storeSettings();
    if (prop!=null) {
      String dir=Settings.getUserDirectoryName();
      File f=new File(dir);
      if (!f.exists()) {
        f.mkdir();
      }
      f=new File(dir,service.getClass().getName()+".service");
      try {
        FileOutputStream out=new FileOutputStream(f);
        prop.store(out,"settings");
        out.close();
      } catch (IOException exc) {
        String msg = mLocalizer.msg("error.4", "Saving settings for plugin {0} failed!\n({1})",
            service.getInfo().getName(), f.getAbsolutePath(), exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }


  /**
   * Changes the TvDataService working directory to the specified folder.
   * @param dir
   */
  public void setTvDataDir(File dir) {
    TvDataServiceProxy[] proxies = getDataServices();
    for (int i=0; i<proxies.length; i++) {
      proxies[i].setWorkingDirectory(dir);
    }
  }

  /**
   * Loads and initializes all available TvDataServices
   */
  public void init() {
   // installPendingDataServices();

   // loadAllTvDataServices();

    String tvdataRoot= Settings.propTVDataDirectory.getString();
    File rootDir=new File(tvdataRoot);
    if (!rootDir.exists()) {
      rootDir.mkdirs();
    }
    setTvDataDir(rootDir);

    TvDataServiceProxy[] proxies = getDataServices();
    for (int i=0; i<proxies.length; i++) {
      loadServiceSettings(proxies[i]);
    }


  }

  public boolean licensesAccepted(TvDataServiceProxy services[]) {
    return true;
  }


  public void shutDown() {
    TvDataServiceProxy[] proxies = getDataServices();
    for (int i=0; i<proxies.length; i++) {
      storeServiceSettings(proxies[i]);
    }
  }

  public TvDataServiceProxy findDataServiceById(String id) {
    TvDataServiceProxy[] proxies = getDataServices();
    for (int i=0; i<proxies.length; i++) {
      if (id.equals(proxies[i].getId())) {
        return proxies[i];
      }
    }
    return null;
  }


  public TvDataServiceProxy[] getTvDataServices(String[] idArr) {
    ArrayList list = new ArrayList();
    for (int i=0; i<idArr.length; i++) {
      TvDataServiceProxy p = findDataServiceById(idArr[i]);
      if (p != null) {
        list.add(p);
      }
    }
    return (TvDataServiceProxy[])list.toArray(new TvDataServiceProxy[list.size()]);
  }


  public TvDataServiceProxy[] getDataServices() {
    if (mProxyList == null) {
      return new TvDataServiceProxy[]{};
    }
    return (TvDataServiceProxy[])mProxyList.toArray(new TvDataServiceProxy[mProxyList.size()]);
  }

}
