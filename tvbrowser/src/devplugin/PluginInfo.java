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
      this("");
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name.
     * <p>
     * @param name The name of the plugin.
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
     */
    public PluginInfo(String name, String desc) {
      this(name,desc,"");       
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description and author.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     */
    public PluginInfo(String name, String desc, String author) {
      this(name,desc,author,null,null,null);        
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author and version.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     */
    public PluginInfo(String name, String desc, String author, Version version) {
      this(name,desc,author,null,version,null);
    }

    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author,
     * version and license.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param version The version of the plugin.
     * @param license The lincense of the plugin.
     */
    public PluginInfo(String name, String desc, String author, Version version, String license) {
      this(name,desc,author,null,version,license);      
    }
    
    /**
     * Creates an instance of PluginVersion with the
     * default values and the given name, description, author
     * and the help url.
     * <p>
     * @param name The name of the plugin.
     * @param desc The description for the plugin.
     * @param author The author of the plugin.
     * @param helpUrl The url where to find help for the plugin.
     * @since 2.6
     */
    public PluginInfo(String name, String desc, String author, String helpUrl) {
       this(name,desc,author,helpUrl,null,null);
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
     * @since 2.6
     */
    public PluginInfo(String name, String desc, String author, String helpUrl, Version version) {
      this(name,desc,author,helpUrl,version,null);
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
     * @since 2.6
     */
    public PluginInfo(String name, String desc, String author, String helpUrl, Version version, String license) {      
      mName = name;
      mDescription = desc;
      mAuthor = author;
      mHelpUrl = helpUrl;
      mVersion = version;
      mLicense = license;
    }
    
    /**
     * Gets the name of the plugin.
     * <p>
     * @return The name of the pluign.
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