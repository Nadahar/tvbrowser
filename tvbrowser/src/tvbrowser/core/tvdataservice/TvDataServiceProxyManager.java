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

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.exc.ErrorHandler;
import util.io.stream.OutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.settings.StringArrayProperty;
import devplugin.AbstractTvDataService;


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
  private ArrayList<TvDataServiceProxy> mProxyList;

  private TvDataServiceProxyManager() {
    mProxyList = new ArrayList<TvDataServiceProxy>();
    AbstractTvDataService.setPluginManager(PluginManagerImpl.getInstance());
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
    String dir=Settings.getUserSettingsDirName();
    File f=new File(dir,service.getId()+".service");
    if (f.exists()) {
      try {
        Properties p=new Properties();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), 0x1000);
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
    final Properties prop = service.storeSettings();
    if (prop!=null) {
      String dir=Settings.getUserSettingsDirName();
      File f=new File(dir);
      if (!f.exists()) {
        f.mkdir();
      }
      f=new File(dir,service.getId()+".service");
      try {
        StreamUtilities.outputStream(f, new OutputStreamProcessor() {
          public void process(OutputStream outputStream) throws IOException {
            prop.store(outputStream, "settings");
          }
        });
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
    for (TvDataServiceProxy proxy : services) {
      File dataServiceDir=new File(dir,proxy.getId());
      if (!dataServiceDir.exists()) {
        dataServiceDir.mkdirs();
      }
      proxy.setWorkingDirectory(dataServiceDir);
    }
  }

  /**
   * Loads and initializes all available TvDataServices
   */
  public void init() {
    try {
      String tvdataRoot = Settings.propTVDataDirectory.getString();
      File rootDir = new File(tvdataRoot);
      if (!rootDir.exists()) {
        rootDir.mkdirs();
      }
      setTvDataDir(rootDir);

      // load only the settings of services with current subscription
      TvDataServiceProxy[] proxies = getDataServices();
      String[] subscribedServices = Settings.propCurrentlyUsedDataServiceIds
          .getStringArray();
      java.util.List<String> list = Arrays.asList(subscribedServices);

      for (TvDataServiceProxy proxy : proxies) {
        if (list.size() == 0 || list.contains(proxy.getId())) {
          loadServiceSettings(proxy);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public boolean licensesAccepted(TvDataServiceProxy services[]) {
    return true;
  }


  public void shutDown() {
    TvDataServiceProxy[] proxies = getDataServices();
    for (TvDataServiceProxy proxy : proxies) {
      storeServiceSettings(proxy);
    }
  }

  public TvDataServiceProxy findDataServiceById(String id) {
    TvDataServiceProxy[] proxies = getDataServices();
    for (TvDataServiceProxy proxy : proxies) {
      if (id.equals(proxy.getId())) {
        return proxy;
      }
    }
    return null;
  }


  public TvDataServiceProxy[] getTvDataServices(String[] idArr) {
    ArrayList<TvDataServiceProxy> list = new ArrayList<TvDataServiceProxy>();
    for (String id : idArr) {
      TvDataServiceProxy proxy = findDataServiceById(id);
      if (proxy != null) {
        list.add(proxy);
      }
    }
    if(list.size() > 0) {
      return list.toArray(new TvDataServiceProxy[list.size()]);
    } else {
      return getDataServices();
    }
  }


  public TvDataServiceProxy[] getDataServices() {
    if (mProxyList == null) {
      return new TvDataServiceProxy[]{};
    }
    return mProxyList.toArray(new TvDataServiceProxy[mProxyList.size()]);
  }

  /**
   * Set the Parent-Frame for all Dataservices
   * @param frame Parentframe
   */
  public void setParamFrame(JFrame frame) {
      TvDataServiceProxy[] services = getDataServices();
      for (TvDataServiceProxy proxy : services) {
        proxy.setParent(frame);
      }

  }

  public void fireTvBrowserStartFinished() {
    TvDataServiceProxy[] services = getDataServices();
    for (TvDataServiceProxy proxy : services) {
        proxy.handleTvBrowserStartFinished();    
    }
  }

  public void loadNotSubscribed() {
    try {
      // load only the settings of services WITHOUT subscription
      TvDataServiceProxy[] proxies = getDataServices();
      String[] subscribedServices = Settings.propCurrentlyUsedDataServiceIds
          .getStringArray();
      if (subscribedServices.length == 0) {
        return;
      }

      java.util.List<String> list = Arrays.asList(subscribedServices);
      for (TvDataServiceProxy proxy : proxies) {
        if (!list.contains(proxy.getId())) {
          loadServiceSettings(proxy);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
