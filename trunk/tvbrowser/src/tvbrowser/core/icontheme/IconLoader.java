/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package tvbrowser.core.icontheme;

import java.io.File;

import javax.swing.ImageIcon;

import tvbrowser.core.Settings;
import util.ui.ImageUtilities;
import devplugin.Plugin;
import devplugin.ThemeIcon;

/**
 * The IconLoader manages the IconThemes and loads an Icon.
 * 
 * If the Icon was not found the the current Icon-Theme it tries to
 * load the Icon in the Default-IconTheme.
 * 
 * IconThemes are Directories that are based on the Icon-Theme Specifications
 * 
 * http://standards.freedesktop.org/icon-theme-spec/icon-theme-spec-latest.html
 * 
 * The Icon-Names are based on the Freedesktop Icon Naming Spec:
 * http://cvs.freedesktop.org/[*]checkout[*]/icon-theme/default-icon-theme/spec/icon-naming-spec.xml
 * (please remove the [ ])
 */
public class IconLoader {
  /** Singelton */
  private static IconLoader mInstance;
  /** Icon Themes to Load Icons from*/
  private IconTheme mDefaultIconTheme, mIconTheme;
  
  /**
   * Private Constructor
   * 
   * It creates the IconThemes
   */
  private IconLoader() {
    File defaultIconDir = new File(Settings.getDefaultSettings().getProperty("icontheme", "icons/tango"));
    
    File iconDir = new File(Settings.propIcontheme.toString());
    
    if (!iconDir.exists() || !iconDir.isFile()) {
      iconDir = defaultIconDir;
    }
    
    mDefaultIconTheme = new IconTheme(defaultIconDir);
    mDefaultIconTheme.loadTheme();
    
    if (defaultIconDir != iconDir) {
      mIconTheme = new IconTheme(iconDir);
      if (!mIconTheme.loadTheme()) {
        mIconTheme = mDefaultIconTheme;
      }
    } else {
      mIconTheme = mDefaultIconTheme;
    }
    
  }
  
  /**
   * Get an Instance of the IconLoader
   * @return Instance
   */
  public static IconLoader getInstance() {
    if (mInstance == null) {
      mInstance = new IconLoader();
    }
    
    return mInstance;
  }

  /**
   * Load a specific Icon
   * 
   * @param plugin Plugin that wants to use the Icon
   * @param icon Name of the Icon without File-Extension
   * @param size Size in Pixel
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(Plugin plugin, ThemeIcon icon, int size) {
    return getIconFromTheme(plugin, icon.getCategory(), icon.getName(), size);
  }  
    
  
  /**
   * Load a specific Icon
   * 
   * @param category Category of the Icon
   * @param icon Name of the Icon without File-Extension
   * @param size Size in Pixel
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(ThemeIcon icon, int size) {
    return getIconFromTheme(null, icon.getCategory(), icon.getName(), size);
  }  
  
  /**
   * Load a specific Icon
   * 
   * @param category Category of the Icon
   * @param icon Name of the Icon without File-Extension
   * @param size Size in Pixel
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(String category, String icon, int size) {
    return getIconFromTheme(null, category, icon, size);
  }
  
  /**
   * Load a specific Icon
   *
   * @param plugin Plugin that wants to use the Icon
   * @param category Category of the Icon
   * @param icon Name of the Icon without File-Extension
   * @param size Size in Pixel
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(Plugin plugin, String category, String icon, int size) {
    
    // First Try: Current Icon Theme
    ImageIcon imageIcon = mIconTheme.getIcon(category, icon, size);
    
    if (imageIcon != null) {
      return imageIcon;
    }
    
    // Second Try: Default Icon Theme
    if (mIconTheme != mDefaultIconTheme) {
      imageIcon = mDefaultIconTheme.getIcon(category, icon, size);
      
      if (imageIcon != null) {
        return imageIcon;
      }
    }
 
    // Third Try: Icon in Plugin-Jar
    if(plugin != null) {
      StringBuffer buffer = new StringBuffer("/").append(plugin.getClass().getPackage().getName()).append("/icons/").append(size).append("x").append(size).append("/").append(category).append("/").append(icon).append(".png");
            
      if (plugin.getClass().getResourceAsStream(buffer.toString()) != null) {
        try {
          imageIcon = ImageUtilities.createImageIconFromJar(buffer.toString(), plugin.getClass()); 
          
          if (imageIcon != null)
            return imageIcon;
        } catch (Exception e) {
        }
      }
    }
    
    // Last Try: Icon in tvbrowser.jar
    StringBuffer buffer = new StringBuffer("/icons/").append(size).append("x").append(size).append("/").append(category).append("/").append(icon).append(".png");
     
    if (getClass().getResourceAsStream(buffer.toString()) != null) {
      imageIcon = ImageUtilities.createImageIconFromJar(buffer.toString(), getClass()); 
      return imageIcon;
    }

    // Failed, return null
    return null;
  }
}