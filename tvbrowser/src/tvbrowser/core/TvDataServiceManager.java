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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import tvbrowser.ui.licensebox.LicenseBox;
import tvdataservice.TvDataService;
import util.exc.ErrorHandler;

public class TvDataServiceManager {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(TvDataServiceManager.class);

  private static TvDataServiceManager mSingleton;

  private HashMap mTvDataServiceHash;



  private TvDataServiceManager() {
  }

  public static void installPendingDataServices() {
	File file=new File("tvdataservice");
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


  public static TvDataServiceManager getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataServiceManager();
    }

    return mSingleton;
  }


  public boolean licensesAccepted(TvDataService services[]) {
    for (int i=0;i<services.length;i++) {
      TvDataService s=services[i];
      String license=s.getInfo().getLicense();
      if (license!=null) {
        Properties p=s.storeSettings();
      
        if (p==null || !"false".equals(p.getProperty("showLicenseBox"))) {        
          LicenseBox box=new LicenseBox(null, license, true);
          util.ui.UiUtilities.centerAndShow(box);
          if (!box.agreed()) {
            return false;
          }
          p.setProperty("showLicenseBox","false");
        }
        s.loadSettings(p);
      }
    }    
    return true;
  }


  /**
   * Loads the TV data service.
   */
  private void loadTvDataServices() {
    if (mTvDataServiceHash != null) {
      throw new IllegalArgumentException("The data services are already loaded!");
    }

    mTvDataServiceHash = new HashMap();

    // Get the tv data service jar file
    String[] fList=new File("tvdataservice").list(new FilenameFilter() {
      public boolean accept(File dir, String fName) {
        return fName.endsWith(".jar");
      }
    });

	if (fList!=null) {
    	for (int i=0;i<fList.length;i++) {
      		String className=fList[i];
      		if (className.length()>4) {
        		className = className.substring(0,className.length()-4);
        		TvDataService cur = loadDataService(className);
        		if (cur != null) {
       		   		mTvDataServiceHash.put(cur.getClass().getName(), cur);
        		}
      		}
    	}
	}
  }



  private TvDataService loadDataService(String name) {
    TvDataService result=null;
    String fName=name+".jar";

    File f=new File("tvdataservice",fName);

    try {
      URL[] urls={ f.toURL() };
      ClassLoader dataserviceClassLoader=new java.net.URLClassLoader(urls,ClassLoader.getSystemClassLoader());

      Class c=dataserviceClassLoader.loadClass(name.toLowerCase()+"."+name);
      result=(tvdataservice.TvDataService)c.newInstance(); 
      
      
    } catch (Exception exc) {
      String msg = mLocalizer.msg("error.5", "Loading tv data service failed!\n({0})",
      f.getAbsolutePath(), exc);
      ErrorHandler.handle(msg, exc);
      return null;  
    }
    
    try {
      devplugin.Version v=result.getAPIVersion();
      // version must be at least 1.0
      if ((new devplugin.Version(1,0).compareTo(v)<0)) throw new AbstractMethodError();
      
      String root=Settings.propTVDataDirectory.getString();
      File rootDir=new File(root);
      if (!rootDir.exists()) {
        rootDir.mkdirs();
      }
      File tvDataDir=new File(rootDir,result.getClass().getName());
      if (!tvDataDir.exists()) {
        tvDataDir.mkdirs();
      } 
      result.setWorkingDirectory(tvDataDir);    
      
    }catch(Throwable t) {
      String msg = mLocalizer.msg("error.6", "Tv data service {0} is not compatible to this version of TV-Browser",
      name, t);
      ErrorHandler.handle(msg, t);
      return null;
    }
    
    
    
    return result;
  }



  private void loadServiceSettings(TvDataService service) {
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



  private void storeServiceSettings(TvDataService service) {
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



  public void finalizeDataServices() {
    Object obj[]=mTvDataServiceHash.values().toArray();
    for (int i=0;i<obj.length;i++) {
      storeServiceSettings((TvDataService)obj[i]);
    }
  }



  public TvDataService getDataService(String className) {
    return (TvDataService) mTvDataServiceHash.get(className);
  }

  public TvDataService[] getTvDataServices(String[] classNames) {
    if (classNames == null) {
      return getDataServices();
    }
    ArrayList list = new ArrayList();
    for (int i=0; i<classNames.length; i++) {
      TvDataService service = getDataService(classNames[i]);
      if (service!=null) {
        list.add(service); 
      }
    }
    TvDataService[] result = new TvDataService[list.size()];
    list.toArray(result);
    return result;
  }

  public void initDataServices() {
    loadTvDataServices();
    Object obj[]=mTvDataServiceHash.values().toArray();
    for (int i=0;i<obj.length;i++) {
      loadServiceSettings((TvDataService)obj[i]);
    }
  }



  public TvDataService[] getDataServices() {
    Collection dataServiceColl = mTvDataServiceHash.values();
    TvDataService[] result = new TvDataService[dataServiceColl.size()];
    dataServiceColl.toArray(result);

    return result;
  }

}