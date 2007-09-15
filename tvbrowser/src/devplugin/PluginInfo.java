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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package devplugin;

import java.lang.reflect.Method;

/**
 * This class provides information about a plugin.
 */
public final class PluginInfo {

    private Version mVersion = null;
    private String mName="";
    private String mDescription="";
    private String mAuthor="";
    private String mLicense=null;
    private String mHelpUrl=null;

    /**
     * Creates the default PluginVersion instance.
     */
    public PluginInfo() {
      this(Class.class,"");
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name.
     * If the caller class is a Plugin it will be tried to get
     * the version from the static method getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name) {
      this(caller, name,"");
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name.
     * <p>
     * @param name The name of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String)} instead.
     */
    public PluginInfo(String name) {
      this(name,"");
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name and description.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String)} instead.
     */
    public PluginInfo(String name, String desc) {
      this(name,desc,"");       
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name and description.
     * If the caller class is a Plugin it will be tried to get
     * the version from the static method getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name, String desc) {
      this(caller,name,desc,"");       
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description and author.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String)} instead.
     */
    public PluginInfo(String name, String desc, String author) {
      this(null,name,desc,author,null,null,null);        
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description and author.
     * If the caller class is a Plugin it will be tried to get
     * the version from the static method getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name, String desc, String author) {
      this(caller, name,desc,author,null,null,null);        
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author and version.
     * If the caller class is a Plugin the version will be compared to
     * the static method getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo.
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @since 2.6 
     */
    public PluginInfo(Class caller, String name, String desc, String author, Version version) {
      this(name,desc,author,null,version,null);
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author and version.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String, Version)} instead
     * and if this is for a Plugin let your Plugin hide {@link Plugin#getVersion()}.
     */
    public PluginInfo(String name, String desc, String author, Version version) {
      this(name,desc,author,null,version,null);
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * version and license. If the caller class
     * is a Plugin the version will be compared to the static method
     * getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo.
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @param license The lincense of the plugin.
     * @since 2.6 
     */
    public PluginInfo(Class caller, String name, String desc, String author, Version version, String license) {
      this(name,desc,author,null,version,license);      
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * version and license. If the caller class
     * is a Plugin the version will be compared to the static method
     * getVersion() of Plugin.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @param license The lincense of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String, Version, String)} instead
     * and if this is for a Plugin let your Plugin hide {@link Plugin#getVersion()}.
     */
    public PluginInfo(String name, String desc, String author, Version version, String license) {
      this(name,desc,author,null,version,license);      
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author
     * and the help url. If the caller class
     * is a Plugin it will be tried to get the version from the static method
     * getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param helpUrl The url where to find help for the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name, String desc, String author, String helpUrl) {
       this(caller,name,desc,author,helpUrl,null,null);
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * the help url, version and license. If the caller class
     * is a Plugin the version will be compared to the static method
     * getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param helpUrl The url where to find help for the plugin.
     * @param license The lincense of the plugin.
     * 
     * since 2.6
     */
    public PluginInfo(Class caller, String name, String desc, String author, String helpUrl, String license) {      
      this(caller,name,desc,author,helpUrl,null,license);
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * the help url, version and license.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param helpUrl The url where to find help for the plugin.
     * @param version The version of the plugin.
     * @param license The lincense of the plugin.
     * 
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String, String, Version, String)} instead
     * and if this is for a Plugin let your Plugin hide {@link Plugin#getVersion()}.
     */
    public PluginInfo(String name, String desc, String author, String helpUrl, Version version, String license) {      
      this(null,name,desc,author,helpUrl,version,license);
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * the help url, version and license. If the caller class
     * is a Plugin the version will be compared to the static method
     * getVersion() of Plugin.
     * <p>
     * @param caller The class that want to create this PluginInfo.
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param helpUrl The url where to find help for the plugin.
     * @param version The version of the plugin.
     * @param license The lincense of the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name, String desc, String author, String helpUrl, Version version, String license) {      
      mName = name;
      mDescription = desc;
      mAuthor = author;
      mHelpUrl = helpUrl;
      
      if(caller != null && caller.getSuperclass() != null && caller.getSuperclass().equals(Plugin.class)) {
        try {
          Method m = caller.getMethod("getVersion", new Class[0]);
          Version pluginVersion = (Version)m.invoke(caller,new Object[0]);
          
          if(version != null && version.compareTo(pluginVersion) > 0) {
            mVersion = version;
          }
          else {
            mVersion = pluginVersion;
          }
          
        }catch(Exception e) {e.printStackTrace();
          mVersion = version;
        }
      }
      else {
        mVersion = version;
      }
      
      mLicense = license;
    }
    
    /**
     * Gets the name of the plugin.
     * <p>
     * @return The name of the plugin.
     */
    public String getName() {
      return mName; 
    }
    
    /**
     * Gets the description of the plugin.
     * <p>
     * @return The description for this plugin.
     */
    public String getDescription() {
      return mDescription;
    }
    
    /**
     * Gets the author of the plugin.
     * <p>
     * @return The author of the plugin.
     */
    public String getAuthor() {
      return mAuthor;
    }
    
    /**
     * Gets the help url for the plugin.
     * <p>
     * @return The help url for the plugin.
     * @since 2.6
     */
    public String getHelpUrl() {
      return mHelpUrl;
    }
    
    /**
     * Gets the version of the plugin.
     * <p>
     * @return The version of the plugin.
     */
    public Version getVersion() { 
      return mVersion;
    }    
    
    /**
     * Gets the license of the plugin.
     * <p>
     * @return The license of the plugin.
     */
    public String getLicense() {
      return mLicense;
    }
}