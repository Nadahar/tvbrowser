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
import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import tvdataloader.TVDataServiceInterface;
import util.exc.*;

//import util.exc.*;
 
public class DataLoaderManager {
  
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(DataLoaderManager.class);
  
  private static DataLoaderManager mSingleton;
  
  private HashMap mTvDataLoaderHash;

  
  
  private DataLoaderManager() {
  }
  

  
  public static DataLoaderManager getInstance() {
    if (mSingleton == null) {
      mSingleton = new DataLoaderManager();
    }
    
    return mSingleton;
  }

  

  /**
   * Loads the TV data loader.
   */
  private void loadTvDataLoaders() {
    if (mTvDataLoaderHash != null) {
      throw new IllegalArgumentException("The data loaders are already loaded!");
    }
    
    mTvDataLoaderHash = new HashMap();
    
    // Get the tv data loader jar file
    String[] fList=new File("tvdataloader").list(new FilenameFilter() {
      public boolean accept(File dir, String fName) {
        return fName.endsWith(".jar");
      }
    });
    
    for (int i=0;i<fList.length;i++) {
      String className=fList[i];
      if (className.length()>4) {
        className = className.substring(0,className.length()-4);
        TVDataServiceInterface cur = loadDataLoader(className);
        if (cur != null) {
          mTvDataLoaderHash.put(cur.getClass().getName(), cur);
        }
      }
    }
  }
  
  
  
  private TVDataServiceInterface loadDataLoader(String name) {
    TVDataServiceInterface result=null;
    String fName=name+".jar";
    
    File f=new File("tvdataloader",fName);
    
    try {
      URL[] urls={ f.toURL() };
      ClassLoader dataloaderClassLoader=new java.net.URLClassLoader(urls,ClassLoader.getSystemClassLoader());
      
      Class c=dataloaderClassLoader.loadClass(name.toLowerCase()+"."+name);
      result=(tvdataloader.TVDataServiceInterface)c.newInstance();
    } catch (Exception exc) {
      //String msg = mLocalizer.msg("error.5", "Loading tv data service failed!\n({0})",
      //f.getAbsolutePath(), exc);
      //ErrorHandler.handle(msg, exc);
      exc.printStackTrace();
    }
    return result;
  }
  

  
  private void loadServiceSettings(TVDataServiceInterface service) {
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
        exc.printStackTrace();
        //String msg = mLocalizer.msg("error.3", "Loading settings for plugin {0} failed!\n({1})",
        //plugin.getButtonText(), f.getAbsolutePath(), exc);
        //ErrorHandler.handle(msg, exc);
      }
    }else{
      service.loadSettings(new Properties());
    }
  }

  
  
  private void storeServiceSettings(TVDataServiceInterface service) {
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
        exc.printStackTrace();
        //String msg = mLocalizer.msg("error.4", "Saving settings for plugin {0} failed!\n({1})",
        //  service.getButtonText(), f.getAbsolutePath(), exc);
        //ErrorHandler.handle(msg, exc);
      }
    }
  }
  
  
  
  public void finalizeDataLoaders() {
    Object obj[]=mTvDataLoaderHash.values().toArray();
    for (int i=0;i<obj.length;i++) {
      storeServiceSettings((TVDataServiceInterface)obj[i]);
    }
  }
  
  
  
  public TVDataServiceInterface getDataLoader(String className) {
    return (TVDataServiceInterface) mTvDataLoaderHash.get(className);
  }
  
    /*
    public static String[] getDataLoaderNames() {
        Iterator it=mTvDataLoaderHash.keySet().iterator();
        java.util.ArrayList list=new java.util.ArrayList();
        while (it.hasNext()) {
            list.add(it.next());
        }
        Object[] obj=list.toArray();
        String[] result=new String[obj.length];
        for (int i=0;i<obj.length;i++) {
            result[i]=(String)obj[i];
        }
        return result;
    }
     */
  
  
  public void initDataLoaders() {
    loadTvDataLoaders();
    Object obj[]=mTvDataLoaderHash.values().toArray();
    for (int i=0;i<obj.length;i++) {
      loadServiceSettings((TVDataServiceInterface)obj[i]);
    }
  }
  
  
  
  public TVDataServiceInterface[] getDataLoaders() {
    Collection dataServiceColl = mTvDataLoaderHash.values();
    TVDataServiceInterface[] result = new TVDataServiceInterface[dataServiceColl.size()];
    dataServiceColl.toArray(result);
    
    return result;
  }
  
  
  
  public void connect() {
    TVDataServiceInterface[] dl=getDataLoaders();
    for (int i=0;i<dl.length;i++) {
      try {
        dl[i].connect();
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }
  
  
  
  public void disconnect() {
    TVDataServiceInterface[] dl=getDataLoaders();
    for (int i=0;i<dl.length;i++) {
      try {
        dl[i].disconnect();
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }

}