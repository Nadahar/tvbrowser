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

package tvbrowser.ui.update;

import java.util.HashMap;
import java.util.Locale;

import util.exc.TvBrowserException;
import devplugin.Version;

public abstract class SoftwareUpdateItem {
	
  private HashMap mPropertyMap;
  private String mClassName;
  
  public SoftwareUpdateItem(String className) {
    mClassName = className;
    mPropertyMap = new HashMap();
  }
  
  public void addProperty(String key, String value) {
    mPropertyMap.put(key, value);  
  }
  public String getProperty(String key) {
    return (String)mPropertyMap.get(key);
  }
  
  public Version getVersion() {
    String v = getProperty("version");
    if (v==null) {
      return null;
    }
    String[] s = v.split("\\.");
    if (s.length!=2) {
      return null;
    }
    int major, minor;
    boolean stable;
    try {
      major = Integer.parseInt(s[0]);
      minor = Integer.parseInt(s[1]);
    }catch(NumberFormatException e) {
      return null;
    }
    stable = "true".equalsIgnoreCase(getProperty("stable"));
    return new Version(major, minor, stable, getProperty("version.name"));
  }
  
  protected boolean isStable() {
    return "true".equalsIgnoreCase(getProperty("stable"));
  }
  
  public boolean isOnlyUpdate() {
    return "true".equalsIgnoreCase(getProperty("onlyUpdate"));
  }
  
  private Version getVersion(String value) {
    if (value==null)
      return null;
    
    String[] s = value.split("\\.");
    
    if (s.length!=2)
      return null;
    
    int major, minor;
    try {
      major = Integer.parseInt(s[0]);
      minor = Integer.parseInt(s[1]);
    }catch(NumberFormatException e) {
      return null;
    }
    return new Version(major, minor);    
  }
  
  public Version getRequiredVersion() {
    return getVersion(getProperty("requires"));
  }  
  
  public Version getMaximumVersion() {
    return getVersion(getProperty("maximalVersion"));    
  }
  
  public String getName() {
    String n = getProperty("name_de");
    
    if(!Locale.getDefault().equals(Locale.GERMAN))
      n = getProperty("name_en");
    
    if(n != null)
      return n;
    else
      return getClassName();
  }
  
  public String getDescription() {
    String d = getProperty("description");
    
    if(!Locale.getDefault().equals(Locale.GERMAN))
      d = getProperty("description_en");
    
    if(d != null)
      return d;
    else
      return "";
  }
  
  public String getWebsite() {
    String w = getProperty("website");
    
    if(!Locale.getDefault().equals(Locale.GERMAN))
      w = getProperty("website_en");
    
    return w;
  }
   
	public String getClassName() {
    return mClassName;   
  }
	
	public boolean download() throws TvBrowserException {
    String url = getProperty("download");
    if (url == null) {
      throw new TvBrowserException(SoftwareUpdateItem.class, "error.2", "No Url");
    }
    return download(url);
  }
    
  protected abstract boolean download(String url) throws TvBrowserException;
	
	
}