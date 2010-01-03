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

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxyManager;
import util.misc.StringPool;

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
     * Creates an instance of PluginInfo with the
     * default values and the given name.
     * <p>
     * @param name The name of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String)} instead.
     */
    public PluginInfo(String name) {
      this(Class.class,name,"");
    }

    /**
     * Creates an instance of PluginInfo with the
     * default values and the given name and description.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String)} instead.
     */
    public PluginInfo(String name, String desc) {
      this(Class.class,name,desc,"");       
    }
    
    /**
     * Creates an instance of PluginInfo with the
     * default values and the given name, description and author.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String)} instead.
     */
    public PluginInfo(String name, String desc, String author) {
      this(Class.class,name,desc,author,null,null,null);        
    }
    
    /**
     * Creates an instance of PluginInfo with the
     * default values and the given name, description, author and version.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @deprecated since 2.6 Use {@link #PluginInfo(Class, String, String, String)} instead
     * and if this is for a Plugin let your Plugin hide {@link Plugin#getVersion()}.
     */
    public PluginInfo(String name, String desc, String author, Version version) {
      this(Class.class,name,desc,author,null,null,version);
    }

  /**
   * Creates an instance of PluginInfo with the default values and the given
   * name, description, author, version and license.
   * <p>
   * 
   * @param name
   *          The name of the plugin.
   * @param desc
   *          The description for the plugin.
   * @param author
   *          The author of the plugin.
   * @param version
   *          The version of the plugin.
   * @param license
   *          The license of the plugin.
   * @deprecated since 2.6 Use
   *             {@link #PluginInfo(Class, String, String, String, String, String)}
   *             instead and if this is for a Plugin let your Plugin hide
   *             {@link Plugin#getVersion()}.
   */
    public PluginInfo(String name, String desc, String author, Version version, String license) {
      this(Class.class,name,desc,author,license,null,version);      
    }
    
    /**
     * Creates the default PluginVersion instance.
     */
    public PluginInfo() {
      this(Class.class,"");
    }
    
    /**
     * Creates an instance of PluginInfo with the
     * default values and the given name.
     * If the caller class is a Plugin/TvDataService it will be tried to get
     * the version from the static method getVersion() of Plugin/AbstractTvDataService.
     * <p>
     * @param caller The class that want to create this PluginInfo
     * @param name The name of the plugin.
     * @since 2.6
     */
    public PluginInfo(Class caller, String name) {
      this(caller, name,"");
    }
    
    /**
     * Creates an instance of PluginInfo with the
     * default values and the given name and description.
     * If the caller class is a Plugin/TvDataService it will be tried to get
     * the version from the static method getVersion() of Plugin/AbstractTvDataService.
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
     * Creates an instance of PluginInfo with the
     * default values and the given name, description and author.
     * If the caller class is a Plugin/TvDataService it will be tried to get
     * the version from the static method getVersion() of Plugin/AbstractTvDataService.
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
   * Creates an instance of PluginInfo with the default values and the given
   * name, description, author, the help url and license. If the caller class is
   * a Plugin/TvDataService it will be tried to get the version from the static
   * method getVersion() of Plugin/AbstractTvDataService.
   * <p>
   * 
   * @param caller
   *          The class that want to create this PluginInfo
   * @param name
   *          The name of the plugin.
   * @param desc
   *          The description for the plugin.
   * @param author
   *          The author of the plugin.
   * @param license
   *          The license of the plugin.
   * 
   *          since 2.6
   */
    public PluginInfo(Class caller, String name, String desc, String author, String license) {      
      this(caller,name,desc,author,license,null,null);
    }

  /**
   * Creates an instance of PluginInfo with the default values and the given
   * name, description, author, the help url and license. If the caller class is
   * a Plugin/TvDataService it will be tried to get the version from the static
   * method getVersion() of Plugin/AbstractTvDataService.
   * <p>
   * 
   * @param caller
   *          The class that want to create this PluginInfo
   * @param name
   *          The name of the plugin.
   * @param desc
   *          The description for the plugin.
   * @param author
   *          The author of the plugin.
   * @param helpUrl
   *          The url where to find help for the plugin.
   * @param license
   *          The license of the plugin.
   * 
   *          since 2.6
   */
    public PluginInfo(Class caller, String name, String desc, String author, String license, String helpUrl) {      
      this(caller,name,desc,author,license,helpUrl,null);
    }
    
    private PluginInfo(Class caller, String name, String desc, String author, String license, String helpUrl, Version version) {      
      mName = name;
      mDescription = desc;
      mAuthor = author;
      mHelpUrl = helpUrl;
      
      if(caller != null && caller.getSuperclass() != null && 
          (caller.getSuperclass().equals(Plugin.class) || 
              caller.getSuperclass().equals(AbstractTvDataService.class))) {
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
      
      mLicense = StringPool.getString(license);
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
    
    /**
     * get the help URL (in the wiki) for a plugin
     * 
     * @param id plugin id
     * @return generic help URL
     * @since 2.6.1
     */
    public static String getHelpUrl(String id) {
      PluginAccess plugin = PluginProxyManager.getInstance().getPluginForId(id);
      String url = null;
      if (plugin != null) {
        url = plugin.getInfo().getHelpUrl();
      }
      if (url == null) {
        url = "http://www.tvbrowser.org/showHelpFor.php?id="
          + id + "&lang="
          + Settings.propLanguage.getString();
        System.out.println(url);
      }
      return url;
    }
}