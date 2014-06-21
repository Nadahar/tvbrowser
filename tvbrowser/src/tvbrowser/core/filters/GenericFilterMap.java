/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.core.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;

public class GenericFilterMap {
  private static File mGenericFilterDirectory;
  private File mGenericFilterProp;
  
  protected static final String GENERIC_PLUGIN_FILTER_DIRECTORY = Settings
      .getUserSettingsDirName()
      + "/genericfilters";
  private final static String GENRIC_FILTER_PROP = "genericfilters.prop";
  
  private HashMap<String, GenericFilterHolder> mGenericPluginFilterMap;
  
  private static GenericFilterMap INSTANCE;
  
  private GenericFilterMap() {
    mGenericPluginFilterMap = new HashMap<String, GenericFilterHolder>();
    create();
  }
  
  private void create() {
    mGenericFilterDirectory = new File(GENERIC_PLUGIN_FILTER_DIRECTORY);
    mGenericFilterProp = new File(mGenericFilterDirectory,GENRIC_FILTER_PROP);
    
    if (!mGenericFilterDirectory.exists()) {
      mGenericFilterDirectory.mkdirs();
    }
  }
  
  public static final synchronized GenericFilterMap getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new GenericFilterMap();
    }
    
    return INSTANCE;
  }
  
  public void loadFilters() {
    try {
      if(mGenericFilterProp.isFile()) {
        Properties prop = new Properties();
        
        try {
          FileInputStream in = new FileInputStream(mGenericFilterProp);          
          prop.load(in);
          in.close();
        }catch(IOException e1) {}
        
        Enumeration<Object> keys = prop.keys();
        
        while(keys.hasMoreElements()) {
          String key = (String)keys.nextElement();
          
          String activatedValue = prop.getProperty(key, null);
          
          if(activatedValue != null) {
            boolean activated = Boolean.parseBoolean(activatedValue);
            
            UserFilter filter = new UserFilter(new File(mGenericFilterDirectory,key+".filter"));
            
            GenericFilterHolder holder = new GenericFilterHolder(activated, filter);
            
            mGenericPluginFilterMap.put(key, holder);
          }
        }
      }
    }catch(Throwable t) {t.printStackTrace();}
  }
  
  public void updateGenericPluginFilterActivated(PluginProxy plugin, boolean activated) {
    GenericFilterHolder holder = mGenericPluginFilterMap.get(plugin.getId());
    
    if(holder != null) {
      holder.setActivated(activated);
    }
  }
  
  public void updateGenericPluginFilter(PluginProxy plugin, UserFilter filter, boolean activated) {
    if(filter != null) {
      GenericFilterHolder holder = mGenericPluginFilterMap.get(plugin.getId());
      
      if(holder == null) {
        holder = new GenericFilterHolder();
        mGenericPluginFilterMap.put(plugin.getId(), holder);
      }
      
      holder.setActivated(activated);
      holder.setFilter(filter);
    }
    else {
      mGenericPluginFilterMap.remove(plugin.getId());
    }
    
    storeGenericFilters();
  }
  
  public UserFilter getGenericPluginFilter(PluginProxy plugin, boolean onlyActivated) {
    GenericFilterHolder holder = mGenericPluginFilterMap.get(plugin.getId());
    
    if(holder != null && (!onlyActivated || holder.isActivated())) {
      return holder.getFilter();
    }
    
    return null;
  }
  
  public void storeGenericFilters() {
    Set<String> keys = mGenericPluginFilterMap.keySet();
    
    Properties prop = new Properties();
    
    for(String key : keys) {
      GenericFilterHolder holder = mGenericPluginFilterMap.get(key);
      
      prop.setProperty(key, String.valueOf(holder.isActivated()));
      
      holder.getFilter().store(GENERIC_PLUGIN_FILTER_DIRECTORY, key);
    }
    
    FileOutputStream out;
    try {
      out = new FileOutputStream(mGenericFilterProp);
      prop.store(out, "Generic plugin filters");
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public PluginProxy[] getActivatedGenericPluginFilterProxies() {
    ArrayList<PluginProxy> proxyList = new ArrayList<PluginProxy>();
    
    Set<String> keys = mGenericPluginFilterMap.keySet();
    
    for(String key : keys) {
      GenericFilterHolder holder = mGenericPluginFilterMap.get(key);
      
      if(holder.isActivated()) {
        PluginProxy proxy = PluginProxyManager.getInstance().getActivatedPluginForId(key);
        
        if(proxy != null) {
          proxyList.add(proxy);
        }
      }
    }
    
    return proxyList.toArray(new PluginProxy[proxyList.size()]);
  }
  
  private static final class GenericFilterHolder {
    private UserFilter mGenericFilter;
    private boolean mIsActivated;
    
    public GenericFilterHolder() {
      mGenericFilter = null;
      mIsActivated = false;
    }
    
    public GenericFilterHolder(boolean activated, UserFilter filter) {
      mIsActivated = activated;
      mGenericFilter = filter;
    }
    
    public boolean isActivated() {
      return mIsActivated;
    }
    
    public void setActivated(boolean activated) {
      mIsActivated = activated;
    }
    
    public UserFilter getFilter() {
      return mGenericFilter;
    }
    
    public void setFilter(UserFilter filter) {
      mGenericFilter = filter;
    }
  }
}
