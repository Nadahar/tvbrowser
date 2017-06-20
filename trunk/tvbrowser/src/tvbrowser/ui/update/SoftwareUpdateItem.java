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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.update;

import java.util.HashMap;
import java.util.Locale;

import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.exc.TvBrowserException;
import util.misc.OperatingSystem;
import devplugin.PluginAccess;
import devplugin.Version;

/**
 * Contains informations about a software update.
 */
public abstract class SoftwareUpdateItem {
  private static final String VERSION_KEY = "version";
  private static final String STABLE_KEY = "stable";
  private static final String VERSION_NAME_KEY = "version.name";
  private static final String ONLY_UPDATE_KEY = "onlyUpdate";
  private static final String OS_NAME_KEY = "os.name";
  private static final String REQUIRES_KEY = "requires";
  private static final String MAXIMAL_VERSION_KEY = "maximalVersion";
  private static final String ESSENTIAL_KEY = "essential";
  private static final String NAME_DE_KEY = "name_de";
  private static final String NAME_EN_KEY = "name_en";
  private static final String DESCRIPTION_DE_KEY = "description";
  private static final String DESCRIPTION_EN_KEY = "description_en";
  private static final String WEBSITE_DE_KEY = "website";
  private static final String WEBSITE_EN_KEY = "website_en";
  private static final String DOWNLOAD_TYPE_KEY = "downloadtype";
  private static final String DOWNLOAD_KEY = "download";
  private static final String FILE_NAME_KEY = "filename";
  private static final String KATEGORY_KEY = "category";
  private static final String ACCESS_KEY = "access"; 
    
  private static final String DOWNLOAD_TYPE_MIRROR_VALUE = "mirrors";
  private static final String KATEGORY_UNKNOWN_VALUE = "unknown";
  
  private static final char WINDOWS_OS_VALUE = 'w';
  private static final char OSX_OS_VALUE = 'm';
  private static final char LINUX_OS_VALUE = 'l';
  private static final char OTHER_OS_VALUE = 'o';

  private HashMap<String, String> mPropertyMap;
  private String mClassName;
  private boolean mPreSelected;

  /**
   * Creates an instances of this class.
   *
   * @param className The class name of the software to update.
   */
  public SoftwareUpdateItem(String className) {
    mClassName = className;
    mPropertyMap = new HashMap<String, String>();
    mPreSelected = false;
  }

  /**
   * Adds a property to this item.
   *
   * @param key The key of the property.
   * @param value The value for the key.
   */
  public void addProperty(String key, String value) {
    mPropertyMap.put(key, value);
  }

  /**
   * Gets a property for the given key.
   *
   * @param key The key to get the property for.
   * @return The property for the key, or <code>null</code>
   * if the key was not found.
   */
  public String getProperty(String key) {
    return mPropertyMap.get(key);
  }

  /**
   * Gets the version of this update item
   *
   * @return The version of this update item.
   */
  public Version getVersion() {
    String v = getProperty(VERSION_KEY);
    if (v==null) {
      return null;
    }
    String[] s = v.split("\\.");
    if (s.length<2) {
      return null;
    }

    int major, minor, subMinor = 0;
    boolean stable;
    try {
      major = Integer.parseInt(s[0]);
      minor = Integer.parseInt(s[1]);

      if(s.length == 3) {
        subMinor = Integer.parseInt(s[2]);
      }
    }catch(NumberFormatException e) {
      return null;
    }
    stable = "true".equalsIgnoreCase(getProperty(STABLE_KEY));
    return new Version(major, minor, subMinor, stable, getProperty(VERSION_NAME_KEY));
  }

  /**
   * Gets if this update item is a stable version.
   *
   * @return <code>True</code> if this update item is a stable version,
   * <code>false</code> otherwise.
   */
  public boolean isStable() {
    return "true".equalsIgnoreCase(getProperty(STABLE_KEY));
  }

  /**
   * Gets if this is an only update item.
   *
   * @return <code>True</code> if this is an
   * only update item, <code>false</code> otherwise.
   */
  public boolean isOnlyUpdate() {
    return "true".equalsIgnoreCase(getProperty(ONLY_UPDATE_KEY));
  }

  /**
   * Gets if the plugin supports the current OS.
   *
   * @return <code>True</code> if the current OS
   * is supported by the plugin, <code>false</code> otherwise.
   * @since 2.2.4/2.6
   */
  public boolean isSupportingCurrentOs() {
    String prop = getProperty(OS_NAME_KEY);

    if(prop == null) {
      return true;
    }
    else if (prop.indexOf(WINDOWS_OS_VALUE) != -1 && OperatingSystem.isWindows()) {
      return true;
    }
    else if (prop.indexOf(OSX_OS_VALUE) != -1 && OperatingSystem.isMacOs()) {
      return true;
    }
    else if (prop.indexOf(LINUX_OS_VALUE) != -1 && OperatingSystem.isLinux()) {
      return true;
    }
    else if (prop.indexOf(OTHER_OS_VALUE) != -1 && OperatingSystem.isOther()) {
      return true;
    }

    return false;
  }

  private Version getVersion(String value) {
    if (value==null) {
      return null;
    }

    String[] s = value.split("\\.");

    if (s.length<2) {
      return null;
    }

    int major, minor, subMinor = 0;
    try {
      major = Integer.parseInt(s[0]);
      minor = Integer.parseInt(s[1]);

      if(s.length == 3) {
        subMinor = Integer.parseInt(s[2]);
      }
    }catch(NumberFormatException e) {
      return null;
    }
    return new Version(major, minor, subMinor);
  }

  /**
   * Gets the TV-Browser version that is required
   * to support this update item.
   *
   * @return The required TV-Browser version.
   */
  public Version getRequiredVersion() {
    return getVersion(getProperty(REQUIRES_KEY));
  }

  /**
   * Gets the maximum supported TV-Browser version.
   *
   * @return The maximum supported TV-Browser version.
   */
  public Version getMaximumVersion() {
    return getVersion(getProperty(MAXIMAL_VERSION_KEY));
  }
  
  /**
   * Gets the version for which this software update is essential.
   * 
   * @return The TV-Browser version this software update is essential for
   *          or <code>null</code> if there is no essential version.
   * @since 3.3
   */
  public Version getEssentialTvbVersion() {
    return getVersion(getProperty(ESSENTIAL_KEY));
  }
  
  /**
   * Sets the preselected state of this update.
   * <p>
   * @param preSelected <code>true</code> if this update should be preselected.
   * @since 3.4.3
   */
  public void setPreSelected(boolean preSelected) {
    mPreSelected = preSelected;
  }
  
  /**
   * Gets if this update should be preselected.
   * <p>
   * @return <code>true</code> if this update should be preselected.
   * @since 3.4.3
   */
  public boolean isPreSelected() {
    return mPreSelected;
  }

  /**
   * Gets the name of this update item.
   *
   * @return The name of this update item.
   */
  public String getName() {
    String n = getProperty(NAME_DE_KEY);

    if(!isLocaleGerman()) {
      n = getProperty(NAME_EN_KEY);
    }

    if(n != null) {
      return n;
    } else {
      return getClassName();
    }
  }

  /**
   * Gets the description of this update item.
   *
   * @return The description of this update item.
   */
  public String getDescription() {
    String d = getProperty(DESCRIPTION_DE_KEY);

    if(!isLocaleGerman()) {
      d = getProperty(DESCRIPTION_EN_KEY);
    }

    if(d != null) {
      return d;
    } else {
      return "";
    }
  }

  /**
   * Gets the website of this update item.
   *
   * @return The website of this update item.
   */
  public String getWebsite() {
    String w = getProperty(WEBSITE_DE_KEY);

    if(!isLocaleGerman()) {
      w = getProperty(WEBSITE_EN_KEY);
    }

    return w;
  }

  /**
   * @return true if current locale language is german
   */
  private boolean isLocaleGerman() {
    return Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage());
  }

  /**
   * Gets the class name of this update item.
   *
   * @return The class name of this update item.
   */
	public String getClassName() {
    return mClassName;
  }

	/**
	 * Downloads the file for this software update item.
	 *
	 * @param downloadUrl A donwload URL to use, or <code>null</code>
	 * if the default url should be used.
	 *
	 * @return <code>True</code> if the download was successfull,
   *         <code>false</code> otherwise.
	 * @throws TvBrowserException Thrown if something went wrong.
	 */
	public boolean download(String downloadUrl) throws TvBrowserException {
    String url = getProperty(DOWNLOAD_TYPE_KEY) == null
        || !getProperty(DOWNLOAD_TYPE_KEY).equalsIgnoreCase(DOWNLOAD_TYPE_MIRROR_VALUE) ? getProperty(DOWNLOAD_KEY)
        : downloadUrl + "/" + getProperty(FILE_NAME_KEY);

    if (url == null) {
      throw new TvBrowserException(SoftwareUpdateItem.class, "error.2", "No Url");
    }
    return downloadFrom(url);
  }

  protected abstract boolean downloadFrom(String url) throws TvBrowserException;

  /**
   * Gets the currently installed version of this software.
   *
   * @return The installed version of this software or
   * <code>null</code> if the software represented by this
   * update item is not installed.
   */
	public Version getInstalledVersion() {
    for (PluginAccess plugin : PluginProxyManager.getInstance().getAllPlugins()) {
      if (plugin.getInfo().getName().equalsIgnoreCase(getName()) || plugin.getId().endsWith(getClassName())) {
        return plugin.getInfo().getVersion();
      }
    }
    for (TvDataServiceProxy service : TvDataServiceProxyManager.getInstance().getDataServices()) {
      if (service.getInfo().getName().equalsIgnoreCase(getName()) || service.getId().endsWith(getClassName())) {
        return service.getInfo().getVersion();
      }
    }
    return null;
  }

	/**
	 * check whether this item is already installed or not
	 * @return true if item is already installed
	 */
	public boolean isAlreadyInstalled() {
		return getInstalledVersion() != null;
	}

	/**
	 * Gets the category of this update item.
	 * <p>
	 * @since 2.7
	 * @return The category of this update item.
	 */
	public String getCategory() {
	  if(mPropertyMap.containsKey(KATEGORY_KEY)) {
	    return mPropertyMap.get(KATEGORY_KEY);
	  }
	  else {
	    return KATEGORY_UNKNOWN_VALUE;
	  }
	}
	
	public boolean isAccessControl() {
	  if(mPropertyMap.containsKey(ACCESS_KEY)) {
	    return mPropertyMap.get(ACCESS_KEY).toLowerCase().equals("true");
	  }
	  else {
	    return false;
	  }
	}
}