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

import java.util.*;
import java.awt.Font;
import java.io.*;
import javax.swing.*;

import util.exc.TvBrowserException;

import tvdataservice.TvDataService;
import devplugin.Channel;
import tvbrowser.ui.SkinPanel;

class TVBrowserProperties extends java.util.Properties {

  private HashSet unconfirmedSettingItems=new HashSet();

  public TVBrowserProperties() {
    super();
  }

  public Object setProperty(String key, String value) {
    String oldVal=getProperty(key);
    if (oldVal!=null && !oldVal.equals(value)) {
      unconfirmedSettingItems.add(key);
    }else if (oldVal==null && value!=null) {
      unconfirmedSettingItems.add(key);
    }

    return super.setProperty(key,value);

  }

  public boolean isUnconfirmedSettingItem(String key) {
    boolean result=false;
    if (unconfirmedSettingItems.contains(key)) {
      unconfirmedSettingItems.remove(key);
      result=true;
    }
    return result;
  }
  
  
  public void setStringList(String key, String[] values) {
	if (values==null || values.length==0) {
				setProperty(key,"");
				return;
			}

			String line="";

			for (int i=0;i<values.length-1;i++) {
				line+=values[i]+",";
			}
			line+=values[values.length-1];
			setProperty(key,line);	
  }
  
  
  public String[] getStringList(String key) {
	String s=getProperty(key);
		 if (s==null) return new String[0];

		 ArrayList list=new ArrayList();
		 int cur=0, last=0;
		 String a;
		 while (cur<s.length()) {
		   cur=s.indexOf(',',last);
		   if (cur==-1) {
			 cur=s.length();
		   }
		   list.add(s.substring(last,cur).trim());
		   cur++;
		   last=cur;
		 }

		 String[] result=new String[list.size()];
		 for (int i=0;i<list.size();i++) {
			 result[i]=(String)list.get(i);
		 }
		 return result;
  }
  
	public void setFont(String key, Font f) {
		String fStr[]=new String[3];
		fStr[0]=f.getName();
		fStr[1]=""+f.getStyle();
		fStr[2]=""+f.getSize();		
		setStringList(key,fStr);
	}
	
	public Font getFont(String key) {
		String fStr[];
		fStr=getStringList(key);
		if (fStr.length>=3) {
			String fName=fStr[0];
			int fStyle=Integer.parseInt(fStr[1]);
			int fSize=Integer.parseInt(fStr[2]);
			return new Font(fName,fStyle,fSize);			
		}
		return null;	
	}

}



/**
 * The Settings class provides access to the settings of the whole application
 * (except the plugins).
 *
 * @author Martin Oberhauser
 */
public class Settings {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Settings.class.getName());

  private static TVBrowserProperties settings=null;
  public static final int GET_DATA_FROM_SERVER=0, GET_DATA_FROM_LOCAL_DISK=1;
  public static final int TEXT_ONLY=0, ICON_ONLY=1, TEXT_AND_ICON=2;


  private static final String SETTINGS_FILE="settings.prop";
  private static final String USER_DIR="tvbrowser";
  //public static final String DATA_DIR="tvdata";

  public static boolean settingHasChanged(String[] key) {

		  boolean result=false;
		  for (int i=0;i<key.length;i++) {
			  if (settings.isUnconfirmedSettingItem(key[i])) {
				  result=true;
			  }
		  }
		  return result;
	  }

  /**
   * Returns the user directory. (e.g.: ~/tvbrowser/)
   */
  public static String getUserDirectoryName() {
    String dir=System.getProperty("user.home","");
    String fileSeparator=System.getProperty("file.separator");

    if (!"".equals(dir)) {
      dir=dir+fileSeparator+USER_DIR;
    }else{
      dir=USER_DIR;
    }

    return dir;
  }


	public static String getTVDataDirectory() {
		String res=settings.getProperty("tvdatadirectory");
		if (res==null) {
			java.io.File f=new File("tvdata");
			res=f.getAbsolutePath();
		}
		return res;
	}

	public static void setTVDataDirectory(String dir) {
		settings.setProperty("tvdatadirectory",dir);
	}

  /**
   * Store all settings. This method is called on quitting the application.
   */
  public static void storeSettings() throws TvBrowserException {
    File f=new File(getUserDirectoryName());
    if (!f.exists()) {
      f.mkdirs();
    }

    File settingsFile = new File(getUserDirectoryName(), SETTINGS_FILE);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(settingsFile);
      settings.store(out, "settings");
    }
    catch (IOException exc) {
      throw new TvBrowserException(Settings.class, "error.1",
        "Error when saving settings!\n({0})", settingsFile.getAbsolutePath(), exc);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
  }

  /**
   * Reads the settings from settings file. If there is no settings file, default
   * settings are used.
   */
  public static void loadSettings() {
    settings = new TVBrowserProperties();
    try {
      settings.load(new FileInputStream(new File(getUserDirectoryName(),SETTINGS_FILE)));
    }
    catch (IOException evt) {
      mLog.info("No user settings found. using default user settings");
    }

    initSubscribedChannels();
  }



  public static void setSubscribedChannels(Channel[] channels) {
  	String[] entries = new String[channels.length];
  	for (int i=0;i<entries.length;i++) {
      Channel ch=(Channel)channels[i];
      String dsClassName = channels[i].getDataService().getClass().getName();
      entries[i] = dsClassName + ":" + ch.getId();
  	}

	settings.setStringList("subscribedchannels", entries);

  }



  private static void initSubscribedChannels() {
//<<<<<<< Settings.java
  	String[] entries = settings.getStringList("subscribedchannels");
//=======
    if (settings.getProperty("subscribedchannels") == null) {
      // Install by default the first 9 channels of the XmlTvDataService
      TvDataServiceManager mng = TvDataServiceManager.getInstance();
      TvDataService xmltvService = mng.getDataService("xmltvdataservice.XmlTvDataService");
      if (xmltvService != null) {
        Channel[] channelArr = xmltvService.getAvailableChannels();
        Channel[] defaultChannelArr = new Channel[9];
        System.arraycopy(channelArr, 0, defaultChannelArr, 0, defaultChannelArr.length);
        setSubscribedChannels(defaultChannelArr);
      }
    }
    
  //	String[] entries = getStringListProperty("subscribedchannels");
//>>>>>>> 1.26

    for (int i = 0; i < entries.length; i++) {
      String entry = entries[i];
      int pos = entry.indexOf(':');
      String dataServiceClassName = entry.substring(0,pos);
      String id = entry.substring(pos + 1);

      TvDataService dataService
        = TvDataServiceManager.getInstance().getDataService(dataServiceClassName);

      if (dataService != null) {
        ChannelList.subscribeChannel(dataService, Integer.parseInt(id));
      }
    }
  }

  
  
  /**
   * Returns the background mode of the TV table.
   * Possible values are COLUMN, WALLPAPER and NONE
   */
  public static int getTableBGMode() {
    String mode=settings.getProperty("tablebgmode","cols");

    if ("cols".equals(mode)) {
      return SkinPanel.COLUMNS;
    }else if ("wallpaper".equals(mode)) {
      return SkinPanel.WALLPAPER;
    }
    return SkinPanel.NONE;
  }

  public static void setTableBGMode(int mode) {
    if (mode==SkinPanel.COLUMNS) {
      settings.setProperty("tablebgmode","cols");
    }else if (mode==SkinPanel.WALLPAPER) {
      settings.setProperty("tablebgmode","wallpaper");
    }else {
      settings.setProperty("tablebgmode","none");
    }
  }

  /**
   * Returns the background picture of the TV table.
   */
  public static String getTableSkin() {
    return settings.getProperty("tablebackground","imgs/columns.jpg");
  }

  public static void setTableSkin(String value) {
    settings.setProperty("tablebackground",value);
  }

  public static boolean isTimeBtnVisible() {
    return "visible".equals(settings.getProperty("timebutton","visible"));
  }

  public static void setTimeBtnVisible(boolean value) {
    settings.setProperty("timebutton", value ? "visible" : "hidden");
  }


  public static boolean isUpdateBtnVisible() {
    return "visible".equals(settings.getProperty("updatebutton","visible"));
  }

  public static void setUpdateBtnVisible(boolean value) {
    settings.setProperty("updatebutton",value?"visible":"hidden");
  }

  public static boolean isPreferencesBtnVisible() {
    return "visible".equals(settings.getProperty("preferencesbutton","hidden"));
  }

  public static void setPreferencesBtnVisible(boolean value) {
    settings.setProperty("preferencesbutton",value?"visible":"hidden");
  }

  public static int getButtonSettings() {
    String val=settings.getProperty("buttontype","text&icon");
    if ("text&icon".equals(val)) {
      return TEXT_AND_ICON;
    }else if ("text".equals(val)) {
      return TEXT_ONLY;
    }
    return ICON_ONLY;
  }

  public static void setButtonSettings(int type) {
    if (type==TEXT_AND_ICON) {
      settings.setProperty("buttontype","text&icon");
    }else if (type==TEXT_ONLY) {
      settings.setProperty("buttontype","text");
    }else {
      settings.setProperty("buttontype","icon");
    }
  }

  public static String getApplicationSkin() {
    return settings.getProperty("applicationskin","imgs/background.jpg");
  }

  public static void setApplicationSkin(String value) {
    settings.setProperty("applicationskin",value);
  }

  public static boolean useApplicationSkin() {
    String b=settings.getProperty("useapplicationskin","yes");
    return ("yes".equals(b));
  }

  public static void setUseApplicationSkin(boolean value) {
    if (value) {
      settings.setProperty("useapplicationskin","yes");
    }else{
      settings.setProperty("useapplicationskin","no");
    }
  }

  public static void setLookAndFeel(String lf) {

    settings.setProperty("lookandfeel",lf);
  }

  public static String getLookAndFeel() {
    String result=settings.getProperty("lookandfeel");

    if (result==null) {
      result=UIManager.getCrossPlatformLookAndFeelClassName();
    }
    return result;
  }


  public static void setColumnWidth(int width) {
    settings.setProperty("columnwidth",""+width);
  }

  public static int getColumnWidth() {
    String s=settings.getProperty("columnwidth","200");
    int res=0;
    try {
      res=Integer.parseInt(s);
    }catch(NumberFormatException e) {
      return 200;
    }
    return res;
  }



  public static String[] getButtonPlugins() {

	 return settings.getStringList("buttonplugins");
   }

   public static void setButtonPlugins(String[] plugins) {
   	 settings.setStringList("buttonplugins",plugins);
   }



  /**
   * Returns all installed plugins as an array of Strings
   */
  public static String[] getInstalledPlugins() {
    if (settings.getProperty("plugins") == null) {
      // Install by default all plugins
      devplugin.Plugin[] availableArr = PluginManager.getAvailablePlugins();
      String[] classNameArr = new String[availableArr.length];
      for (int i = 0; i < availableArr.length; i++) {
        classNameArr[i] = availableArr[i].getClass().getName();
      }
      setInstalledPlugins(classNameArr);
    }
    
    return settings.getStringList("plugins");
  }



  public static void setInstalledPlugins(String[] plugins) {
	settings.setStringList("plugins", plugins);
  }

  public static void setDownloadPeriod(int period) {
  	settings.setProperty("downloadperiod",""+period);
  }

  public static int getDownloadPeriod() {
  	String period=settings.getProperty("downloadperiod");
  	int result;
  	try {
  		result=Integer.parseInt(period);
  	} catch (NumberFormatException e) {
  		result=0;
  	}
  	return result;

  }
  
  /**
   *  Returns the tvdata lifespan in days
   * 
   **/
  
  public static int getTVDataLifespan() {
  	String lifespan=settings.getProperty("tvdatalifespan");
	int result;
	try {
		result=Integer.parseInt(lifespan);
	} catch (NumberFormatException e) {
		result=3;
	}
	return result;  	
  }

  public static void setTVDataLifespan(int lifespan) {
	settings.setProperty("tvdatalifespan",""+lifespan);
  }
  
  public static boolean getStartupInOnlineMode() {
  	return "yes".equals(settings.getProperty("startupinonlinemode"));
  }
  
  public static void setStartupInOnlineMode(boolean value) {
    if (value) {
    	settings.setProperty("startupinonlinemode","yes");
  	}
  	else {
		settings.setProperty("startupinonlinemode","no");
  	}
  }

	public static int getAutomaticDownloadPeriod() {
		String period=settings.getProperty("autodownloadperiod");
		int result;
		try {
			result=Integer.parseInt(period);
		} catch (NumberFormatException e) {
			result=0;
		}
		return result;  
	}
	
	public static void setAutomaticDownloadPeriod(int period) {
		settings.setProperty("autodownloadperiod",""+period);
	}
	
	public static java.awt.Font getProgramTitleFont() {
		Font f=settings.getFont("programtitlefont");
		if (f==null) {
			f=new Font("Helvetica",Font.BOLD,12);
		}
		return f;
	}
	
	public static void setProgramTitleFont(java.awt.Font f) {
		settings.setFont("programtitlefont",f);
		
	}
	
	public static java.awt.Font getProgramInfoFont() {
		Font f=settings.getFont("programinfofont");
		if (f==null) {
			f=new Font("Helvetica",Font.PLAIN,10);
		}
		return f;
	}
	
	public static void setProgramInfoFont(java.awt.Font f) {
		settings.setFont("programinfofont",f);
	}
	
}