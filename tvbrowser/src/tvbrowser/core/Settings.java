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
import java.awt.Point;
import java.io.*;
import javax.swing.*;

import util.exc.TvBrowserException;
import util.io.IOUtilities;

import tvdataservice.TvDataService;
import devplugin.Channel;
import devplugin.Plugin;
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
    if (s==null) {
      return new String[0];
    }
    
    return s.split(",");
    
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
			int fStyle;			
			int fSize;
			try {
				fStyle=Integer.parseInt(fStr[1]);
				fSize=Integer.parseInt(fStr[2]);
			}catch(NumberFormatException e) {
				fStyle=Font.PLAIN;
				fSize=12;
			}
			return new Font(fName,fStyle,fSize);			
		}
		return null;	
	}

  
  public void setBoolean(String key, boolean value) {
    setProperty(key, value ? "true" : "false");
  }

  
  
  public boolean getBoolean(String key, boolean defaultValue) {
    String asString = getProperty(key);
    
    if ("true".equalsIgnoreCase(asString)) {
      return true;
    }
    else if ("false".equalsIgnoreCase(asString)) {
      return false;
    }
    else {
      return defaultValue;
    }
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

  private static final long PROXY_PASSWORD_SEED = 6528587292713416704L;

  public static final int TABLE_LAYOUT_COMPACT = 1;
  public static final int TABLE_LAYOUT_TIME_SYNCHRONOUS = 2;

  private static TVBrowserProperties settings=null;
  public static final int GET_DATA_FROM_SERVER=0, GET_DATA_FROM_LOCAL_DISK=1;
  public static final int TEXT_ONLY=0, ICON_ONLY=1, TEXT_AND_ICON=2;

  public static final int NEVER=0, ONSTARTUP=1;
  
  private static final String SETTINGS_FILE="settings.prop";
  private static final String OLD_USER_DIR = "tvbrowser";
  private static final String USER_DIR = ".tvbrowser";
  
  public static final String TVDATA_DIR="tvdata";
  public static final String DATASERVICECACHE_DIR=".";
  
  public static final String FILTERS_DIR="filters";
  
  private static final Font PROGRAMTITLEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMINFOFONT=new Font("Dialog",Font.PLAIN,10);
  private static final Font CHANNELNAMEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMTIMEFONT=new Font("Dialog",Font.BOLD,12); 
  
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
   * Returns the user directory. (e.g.: ~/.tvbrowser/)
   */
  public static String getUserDirectoryName() {
    String dir = System.getProperty("user.home", "");
    String oldDir = dir;
    
    if (dir.length() != 0) {
      dir += File.separator + USER_DIR;
      oldDir += File.separator + OLD_USER_DIR;
    } else {
      dir = USER_DIR;
      oldDir = OLD_USER_DIR;
    }

    // The user directory used to be "tvbrowser". Now it is ".tvbrowser"
    // (hidden on UNIX systems). -> Rename the old directory if it still exists.
    File oldUserDir = new File(oldDir);
    if (oldUserDir.exists()) {
      oldUserDir.renameTo(new File(dir));
    }
    
    return dir;
  }
  
  
  public static void setUseDefaultFonts(boolean val) {
    settings.setProperty("usedefaultfonts",val?"yes":"no");
  }
  
  public static boolean getUseDefaultFonts() {
    String val=settings.getProperty("usedefaultfonts");
    return (!"no".equals(val));
  }
  
 
  

  public static void setUseDefaultDirectories(boolean val) {
    settings.setProperty("usedefaultdirectories",val?"yes":"no");
  }

  public static boolean getUseDefaultDirectories() {
    String val=settings.getProperty("usedefaultdirectories");
    return (!"no".equals(val));
  }

	public static String getTVDataDirectory() {
		String res=settings.getProperty("directory.tvdata");
		if (res==null) {
			//java.io.File f=new File(TVDATA_DIR);
			//res=f.getAbsolutePath();
      return TVDATA_DIR;
		}
		return res;
	}

	public static void setTVDataDirectory(String dir) {
		settings.setProperty("directory.tvdata",dir);
	}
  
  public static String getDataServiceCacheDirectory() {
    String res=settings.getProperty("directory.dataservicecache");
    if (res==null) {
      return DATASERVICECACHE_DIR;      
    }
    return res;
  }
  
  public static void setDataServiceCacheDirectory(String dir) {
    settings.setProperty("directory.dataservicecache",dir);
  }
  
  public static String getFilterDirectory() {
    String res=settings.getProperty("directory.filters");
    if (res==null) {
      return getUserDirectoryName()+File.separator+FILTERS_DIR;      
    }
    return res;
  }
  
  public static void setFilterDirectory(String dir) {
      settings.setProperty("directory.filters",dir);
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
    	File f=new File(getUserDirectoryName(),SETTINGS_FILE);
      	settings.load(new FileInputStream(f));
    }
    catch (IOException evt) {
      mLog.info("No user settings found. using default user settings");
    }

    initSubscribedChannels();

    updateProxySettings(getHttpProxySettings(), getFtpProxySettings());
  }



  public static void setSubscribedChannels(Channel[] channels) {
  	String[] entries = new String[channels.length];
  	for (int i=0;i<entries.length;i++) {
      String dsClassName = channels[i].getDataService().getClass().getName();
      entries[i] = dsClassName + ":" + channels[i].getId();
  	}

	settings.setStringList("subscribedchannels", entries);

  }



  private static void initSubscribedChannels() {
    String[] entries = settings.getStringList("subscribedchannels");
    if (settings.getProperty("subscribedchannels") == null) {
      System.out.println("no subscribed channels");
      return;
    }
    
    
    for (int i = 0; i < entries.length; i++) {
      String entry = entries[i];
      int pos = entry.indexOf(':');
      if (pos<0) continue;  // invalid entry
      String dataServiceClassName = entry.substring(0,pos);
      String id = entry.substring(pos + 1);

      TvDataService dataService
        = TvDataServiceManager.getInstance().getDataService(dataServiceClassName);

      if (dataService != null) {
        ChannelList.subscribeChannel(dataService, id);
      }
    }
    ChannelList.loadDayLightSavingTimeCorrections();
  }


  public static int getEarlyTime() {
    String time=settings.getProperty("timebutton.early","240");
    return Integer.parseInt(time);
  }
  
  public static void setEarlyTime(int minutes) {
    settings.setProperty("timebutton.early",""+minutes);
  }

  
  
    
    
    public static int getMiddayTime() {
      String time=settings.getProperty("timebutton.midday","720");
      return Integer.parseInt(time);
    }
  
    public static void setMiddayTime(int minutes) {
      settings.setProperty("timebutton.midday",""+minutes);
    }
    
    public static int getAfternoonTime() {
      String time=settings.getProperty("timebutton.afternoon","1080");
      return Integer.parseInt(time);
    }
    
    public static void setMorningTime(int minutes) {
      settings.setProperty("timebutton.afternoon",""+minutes);
    }
    
  public static int getEveningTime() {
      String time=settings.getProperty("timebutton.evening","1260");
      return Integer.parseInt(time);
    }
  
    public static void setEveningTime(int minutes) {
      settings.setProperty("timebutton.evening",""+minutes);
    }


  public static int getTableLayout() {
    String layout = settings.getProperty("table.layout", "timeSynchronous");
    
    if (layout.equals("compact")) {
      return TABLE_LAYOUT_COMPACT;
    } else {
      return TABLE_LAYOUT_TIME_SYNCHRONOUS;
    }
  }



  public static void setTableLayout(int layout) {
    if (layout == TABLE_LAYOUT_COMPACT) {
      settings.setProperty("table.layout", "compact");
    } else {
      settings.setProperty("table.layout", "timeSynchronous");
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
  
  public static boolean getShowAssistant() {
    boolean result=!"false".equals(settings.getProperty("showassistant"));
    return result;
  }
  
  public static void setShowAssistant(boolean val) {
    settings.setBoolean("showassistant",val);
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
  
  public static boolean isSkinLFEnabled() {
    return settings.getBoolean("skinLF.enabled",false);
  }
  
  public static void setSkinLFEnabled(boolean val) {
    settings.setBoolean("skinLF.enabled",val);
  }
  
  public static String getSkinLFThemepack() {
    return settings.getProperty("skinLF.themepack","themepacks/themepack.zip");
  }
  
  public static void setSkinLFThemepack(String val) {
    settings.setProperty("skinLF.themepack",val);
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



  //public static String[] getHiddenButtonPlugins() {

	// return settings.getStringList("hiddenbuttonplugins");
  // }

  // public static void setHiddenButtonPlugins(String[] plugins) {
  // 	 settings.setStringList("hiddenbuttonplugins",plugins);
  // }
   
   public static void setPluginButtonVisible(devplugin.Plugin plugin, boolean visible) {
     settings.setProperty("pluginbuttonvisible."+plugin.getClass().getName(),""+visible);
   }
   
   public static boolean getPluginButtonVisible(devplugin.Plugin plugin) {
     String res=settings.getProperty("pluginbuttonvisible."+plugin.getClass().getName(),"true");
     return !("false".equals(res));
   }
   

   public static String getDefaultContextMenuPlugin() {
     return settings.getProperty("contextmenudefaultplugin","programinfo.ProgramInfo");
   }
   
   public static void setDefaultContextMenuPlugin(String pluginName) {
     settings.setProperty("contextmenudefaultplugin",pluginName);
   }

   public static void setContextMenuItemPlugins(String[] plugins) {
     settings.setStringList("contextmenuitemplugins",plugins);
   }


  public static String[] getContextMenuItemPlugins() {
    if (settings.getProperty("contextmenuitemplugins") == null) {
      return getInstalledPlugins();
    }
    else {
      return settings.getStringList("contextmenuitemplugins");
    }
  }


  /**
   * Returns all installed plugins as an array of Strings
   */
  public static String[] getInstalledPlugins() {
    if (settings.getProperty("plugins") == null) {
      // Install by default all plugins
      Plugin[] availableArr = PluginManager.getInstance().getAvailablePlugins();
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
  /*
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
  */
  /*
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
  */
  public static int getAutomaticDownload() {
    String autoDL=settings.getProperty("autodownload");
    if ("startup".equals(autoDL)) {
      return ONSTARTUP;
    }
    else {
      return NEVER;
    }
  }
  
  public static void setAutomaticDownload(String autoDL) {
    settings.setProperty("autodownload",autoDL);
  }
  
  public static java.awt.Font getProgramTitleFont() {
    if (getUseDefaultFonts()) {
      return PROGRAMTITLEFONT;
    }
    Font f=settings.getFont("font.programtitle");
    if (f==null) {
      return PROGRAMTITLEFONT;
    }
    return f;
  }
  
  public static void setProgramTitleFont(java.awt.Font f) {
    settings.setFont("font.programtitle",f);
  }
  
  public static java.awt.Font getProgramInfoFont() {
    if (getUseDefaultFonts()) {
      return PROGRAMINFOFONT;
    }
    Font f=settings.getFont("font.programinfo");
    if (f==null) {
      return PROGRAMINFOFONT;
    }
    return f;
  }
  
  public static void setProgramInfoFont(java.awt.Font f) {
    settings.setFont("font.programinfo",f);
  }
  
  
  public static java.awt.Font getChannelNameFont() {
    if (getUseDefaultFonts()) {
      return CHANNELNAMEFONT;
    }
    Font f=settings.getFont("font.channelname");
    if (f==null) {
      return CHANNELNAMEFONT;
    }
    return f;
  }
  
  public static void setChannelNameFont(java.awt.Font f) {
    settings.setFont("font.channelname",f);
  }
  
  public static java.awt.Font getProgramTimeFont() {
    if (getUseDefaultFonts()) {
      return PROGRAMTIMEFONT;
    }
    Font f=settings.getFont("font.programtime");
    if (f==null) {
      return PROGRAMTIMEFONT;
    }
    return f;
  }
  
  public static void setProgramTimeFont(java.awt.Font f) {
    settings.setFont("font.programtime",f);
  }
  
  public static boolean isWindowMaximized() {
    return settings.getBoolean("window.isMaximized", false);
  }
  
  public static void setWindowIsMaximized(boolean maximized) {
    settings.setBoolean("window.isMaximized", maximized);
  }
  
  
  public static java.awt.Dimension getWindowSize() {
    String dimStr[]=settings.getStringList("windowsize");
    if (dimStr.length>=2) {
      try {
        int width=Integer.parseInt(dimStr[0]);
        int height=Integer.parseInt(dimStr[1]);
        return new java.awt.Dimension(width,height);
      }catch (NumberFormatException e) {
        return new java.awt.Dimension(700,500);
      }
    }
    return new java.awt.Dimension(700,500);
  }
  
  public static void setWindowSize(java.awt.Dimension dim) {
    String dimStr[]=new String[2];
    dimStr[0]=""+dim.width;
    dimStr[1]=""+dim.height;
    settings.setStringList("windowsize",dimStr);
  }
  
  
  
  public static Point getWindowLocation() {
    String locStr[] = settings.getStringList("windowlocation");
    if (locStr.length != 2) {
      return null;
    } else {
      int x = Integer.parseInt(locStr[0]);
      int y = Integer.parseInt(locStr[1]);
      
      return new Point(x, y);
    }
  }
  
  
  
  public static void setWindowLocation(Point location) {
    String locStr[] = new String[] {
      Integer.toString(location.x),
      Integer.toString(location.y)
    };
    settings.setStringList("windowlocation", locStr);
  }
  
  
  public static void setProgramTableStartOfDay(int StartTimeInMinutes) {
    settings.setProperty("programtable.startofday",""+StartTimeInMinutes);
  }
  
  public static int getProgramTableStartOfDay() {
    String val=settings.getProperty("programtable.startofday");
    if (val==null) {
      return 0;
    }
    return Integer.parseInt(val);
  }
  
  public static void setProgramTableEndOfDay(int StartTimeInMinutes) {
      settings.setProperty("programtable.endofday",""+StartTimeInMinutes);
    }
  
    public static int getProgramTableEndOfDay() {
      String val=settings.getProperty("programtable.endofday");
      if (val==null) {
        return 5*60;
      }
      return Integer.parseInt(val);
    }
  
  
  
  public static void setProxySettings(ProxySettings httpSettings,
    ProxySettings ftpSettings)
  {
    setProxySettings(httpSettings, "proxy.http.");
    setProxySettings(ftpSettings, "proxy.ftp.");
    
    updateProxySettings(httpSettings, ftpSettings);
  }

  
  
  private static void updateProxySettings(ProxySettings httpSettings,
    ProxySettings ftpSettings)
  {
    boolean proxySet = httpSettings.mUseProxy || ftpSettings.mUseProxy;
    
    System.setProperty("proxySet", proxySet ? "true" : "false");
    
    updateProxySettings(httpSettings, "http.");
    updateProxySettings(ftpSettings, "ftp.");
  }

  
  
  private static void updateProxySettings(ProxySettings httpSettings,
    String prefix)
  {
    String proxyHost, proxyPort, proxyUser, proxyPassword;
    if (httpSettings.mUseProxy) {
      proxyHost = httpSettings.mHost;
      proxyPort = httpSettings.mPort;
      
      if (httpSettings.mAuthentifyAtProxy) {
        proxyUser = httpSettings.mUser;
        proxyPassword = httpSettings.mPassword;
      } else {
        proxyUser = proxyPassword = "";
      }
    } else {
      proxyHost = proxyPort = proxyUser = proxyPassword = "";
    }
    
    System.setProperty(prefix + "proxyHost", proxyHost);
    System.setProperty(prefix + "proxyPort", proxyPort);
    System.setProperty(prefix + "proxyUser", proxyUser);
    System.setProperty(prefix + "proxyPassword", proxyPassword);
  }
  
  
  
  public static ProxySettings getHttpProxySettings() {
    return getProxySettings("proxy.http.");
  }

  
  
  public static ProxySettings getFtpProxySettings() {
    return getProxySettings("proxy.ftp.");
  }
  
  
  
  private static void setProxySettings(ProxySettings proxySettings, String prefix) {
    settings.setBoolean(prefix + "useProxy", proxySettings.mUseProxy);
    settings.setProperty(prefix + "host", proxySettings.mHost);
    settings.setProperty(prefix + "port", proxySettings.mPort);
    settings.setBoolean(prefix + "authentifyAtProxy", proxySettings.mAuthentifyAtProxy);
    settings.setProperty(prefix + "user", proxySettings.mUser);
    
    // We use a simple XOR encoding.
    // Because this project is open source we can't make a better encoding.
    // But this way the password is at least no plain text.
    String decodedPw = proxySettings.mPassword;
    String encodedPw = null;
    if (decodedPw != null) {
      encodedPw = IOUtilities.xorEncode(decodedPw, PROXY_PASSWORD_SEED);
    }
    settings.setProperty(prefix + "password", encodedPw);
  }

  
  
  private static ProxySettings getProxySettings(String prefix) {
    ProxySettings proxySettings = new ProxySettings();

    proxySettings.mUseProxy = settings.getBoolean(prefix + "useProxy", false);
    proxySettings.mHost = settings.getProperty(prefix + "host", "");
    proxySettings.mPort = settings.getProperty(prefix + "port", "");
    proxySettings.mAuthentifyAtProxy = settings.getBoolean(prefix + "authentifyAtProxy", false);
    proxySettings.mUser = settings.getProperty(prefix + "user", "");
    
    String encodedPw = settings.getProperty(prefix + "password", "");
    String decodedPw = null;
    if (encodedPw != null) {
      decodedPw = IOUtilities.xorDecode(encodedPw, PROXY_PASSWORD_SEED);
    }
    proxySettings.mPassword = decodedPw;
    
    return proxySettings;
  }
  
  
  // inner class ProxySettings
  
  
  /**
   * Holds the settings for a proxy
   */
  public static class ProxySettings {
    public boolean mUseProxy;
    public String mHost;
    public String mPort;
    public boolean mAuthentifyAtProxy;
    public String mUser;
    public String mPassword;
  }
	
}