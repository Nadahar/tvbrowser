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

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
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


  public void registerTvDataService(TvDataServiceProxy service) {
     mProxyList.add(service);
  }

  private void loadServiceSettings(TvDataServiceProxy service) {
    Class c=service.getClass();
    String dir=Settings.getUserDirectoryName();
    File f=new File(dir,service.getId()+".service");
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
      f=new File(dir,service.getId()+".service");
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
    TvDataServiceProxy[] services = getDataServices();
    for (int i=0; i<services.length; i++) {
      File dataServiceDir=new File(dir,services[i].getId());
      if (!dataServiceDir.exists()) {
        dataServiceDir.mkdirs();
      }
      services[i].setWorkingDirectory(dataServiceDir);
    }
  }

  /**
   * Loads and initializes all available TvDataServices
   */
  public void init() {

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
