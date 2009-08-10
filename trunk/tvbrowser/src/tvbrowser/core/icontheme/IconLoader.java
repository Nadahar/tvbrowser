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
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import tvbrowser.core.Settings;
import util.misc.OperatingSystem;
import util.misc.SoftReferenceCache;
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

  /**
   * problems logger 
   */
  private static java.util.logging.Logger mLog = java.util.logging.Logger
    .getLogger(IconLoader.class.getName());
  
  /** Singelton */
  private static IconLoader mInstance;
  /** Icon Themes to Load Icons from*/
  private IconTheme mDefaultIconTheme, mIconTheme;
  /** Default Directory */
  private File mDefaultIconDir;
  /** Icon Cache */
  private SoftReferenceCache<ThemeIcon, ImageIcon> mIconCache;
  /** Icon Cache for Plugins */
  private HashMap<Plugin, SoftReferenceCache<ThemeIcon, ImageIcon>> mPluginIconCache;

  /**
   * Private Constructor
   * 
   * It creates the IconThemes
   */
  private IconLoader() {
    mDefaultIconDir = new File(Settings.getDefaultSettings().getProperty(
        "icontheme", "icons/tango.zip"));

    if (!mDefaultIconDir.exists()) {
      // If the default directory does not exist, try to find another icon theme in the icontheme directory
      File iconDir = new File("icons");
      if (iconDir.exists()) {
        File[] list = iconDir.listFiles();
        if (list.length > 0) {
          mDefaultIconDir = list[0]; // get the first directory
        }
      }
    }

    mDefaultIconTheme = getIconTheme(mDefaultIconDir);
    mDefaultIconTheme.loadTheme();

    String iconTheme = Settings.propIcontheme.getString();
		if (iconTheme != null) {
			if (iconTheme != null && !iconTheme.endsWith(".zip")) {
				if (new File(iconTheme + ".zip").canRead()) {
					Settings.propIcontheme.setString(iconTheme + ".zip");
				}
			}
      File themeFile = getIconThemeFile(iconTheme);

      mLog.info("Loading Icon from " + themeFile.getAbsolutePath());

      loadIconTheme(themeFile);
    } else {
        mIconCache = new SoftReferenceCache<ThemeIcon, ImageIcon>();
        mPluginIconCache = new HashMap<Plugin, SoftReferenceCache<ThemeIcon, ImageIcon>>();
        mIconTheme = mDefaultIconTheme;
    }
  }

  /**
   * Trys to find the Icon Theme File. This method searches in the application directory, the user home and
   * the Directory Library - Application Support - TV-Browser if used on a mac.
   *
   * @param theme name of the theme file
   * @return best file that contains the theme. Attention! The file could not exist!
   * @since 2.7.2
   */
  public File getIconThemeFile(String theme) {
    File themeFile = new File(theme);

    if (!themeFile.exists()) {
      themeFile = new File(Settings.getUserDirectoryName(), theme);
    }

    if (!themeFile.exists() && OperatingSystem.isMacOs()) {
      themeFile = new File("/Library/Application Support/TV-Browser/", theme);
    }

    return themeFile;
  }

  /**
   * Return all available Themes
   * @return all available themes
   */
  public IconTheme[] getAvailableThemes() {
    ArrayList<IconTheme> list = new ArrayList<IconTheme>();
    
    list.addAll(getThemesInDirectory(new File("icons")));
    list.addAll(getThemesInDirectory(new File(Settings.getUserDirectoryName(), "icons")));

    if (OperatingSystem.isMacOs()) {
      list.addAll(getThemesInDirectory(new File("/Library/Application Support/TV-Browser/icons")));
    }
    
    ArrayList<String> zipThemes = new ArrayList<String>(list.size());
    for (IconTheme iconTheme : list) {
    	if (iconTheme instanceof ZipIconTheme) {
				zipThemes.add(iconTheme.getName());
    	}
		}
    for (int i=list.size() - 1; i >= 0; i--) {
    	for (String zipName : zipThemes) {
	    	if (list.get(i).getName().equalsIgnoreCase(zipName) && !(list.get(i) instanceof ZipIconTheme)) {
	    		list.remove(i);
	    	}
			}
    }
    return list.toArray(new IconTheme[list.size()]);
  }

  private ArrayList<IconTheme> getThemesInDirectory(final File directory) {
    final ArrayList<IconTheme> list = new ArrayList<IconTheme>();
    if (directory != null && directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          IconTheme theme = getIconTheme(file);
          if (theme.loadTheme()) {
            list.add(theme);
          }
        }
      }
    }
    return list;
  }

  /**
   * Load the IconTheme from a Directory
   *  
   * @param iconset Directory with IconTheme
   */
  private void loadIconTheme(File iconset) {
    mIconCache = new SoftReferenceCache<ThemeIcon, ImageIcon>();
    mPluginIconCache = new HashMap<Plugin, SoftReferenceCache<ThemeIcon, ImageIcon>>();

    if (!iconset.exists()) {
      iconset = mDefaultIconDir;
    }
    
    if (!mDefaultIconDir.getPath().equals(iconset.getPath())) {
      mIconTheme = getIconTheme(iconset);
      if (!mIconTheme.loadTheme()) {
        mIconTheme = mDefaultIconTheme;
      }
    } else {
      mIconTheme = mDefaultIconTheme;
    }
  }

  /**
   * Creates the IconTheme
   * 
   * @param icon Theme-Location
   * @return IconTheme
   */
  public IconTheme getIconTheme(File icon) {
    if (!icon.exists()) {
      // Return Default Implementation if something goes wrong
      return new DirectoryIconTheme(icon);
    }
    
    if (icon.isDirectory()) {
      return new DirectoryIconTheme(icon);
    } else if (icon.getName().toLowerCase().endsWith(".zip")) {
      return new ZipIconTheme(icon);
    }
    
    // Return Default Implementation if something goes wrong
    return new DirectoryIconTheme(icon);
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
   * @param category Category of the Icon
   * @param icon Name of the Icon without File-Extension
   * @param size Size in Pixel
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(Plugin plugin, String category, String icon, int size) {
    return getIconFromTheme(plugin, new ThemeIcon(category, icon, size));
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
    return getIconFromTheme(null, new ThemeIcon(category, icon, size));
  }

  /**
   * Load a specific Icon in default size (16 pixels)
   * 
   * @param category
   *          Category of the Icon
   * @param icon
   *          Name of the Icon without File-Extension
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(String category, String icon) {
    return getIconFromTheme(null, new ThemeIcon(category, icon, 16));
  }

  /**
   * Load a specific Icon
   * 
   * @param plugin
   *          Plugin that wants to use the Icon
   * @param icon
   *          Icon that should be loaded
   * @return Icon if found, null if no Icon was found
   */
  public ImageIcon getIconFromTheme(Plugin plugin, ThemeIcon icon) {
    // Check the Cache
    ImageIcon imageIcon = mIconCache.get(icon);
    if (imageIcon != null) {
      return imageIcon;
    }
    
    // First Try: Current Icon Theme
    imageIcon = mIconTheme.getIcon(icon);
    
    if (imageIcon != null) {
      mIconCache.put(icon, imageIcon);
      return imageIcon;
    }
    
    // Second Try: Default Icon Theme
    if (mIconTheme != mDefaultIconTheme) {
      imageIcon = mDefaultIconTheme.getIcon(icon);
      
      if (imageIcon != null) {
        mIconCache.put(icon, imageIcon);
        return imageIcon;
      }
    }
 
    if (plugin != null) {
      // Third Try: Plugin Icon Cache
      SoftReferenceCache<ThemeIcon, ImageIcon> pluginCache = mPluginIconCache
          .get(plugin);

      if (pluginCache != null) {
        imageIcon = pluginCache.get(icon);
        if (imageIcon != null) {
          return imageIcon;
        }
      }
      
      // Forth Try: Icon in Plugin-Jar
      StringBuilder buffer = new StringBuilder("/").append(
          plugin.getClass().getPackage().getName()).append("/icons/").append(
          icon.getSize()).append("x").append(icon.getSize()).append("/")
          .append(icon.getCategory()).append("/").append(icon.getName())
          .append(".png");
            
      if (plugin.getClass().getResourceAsStream(buffer.toString()) != null) {
        try {
          imageIcon = ImageUtilities.createImageIconFromJar(buffer.toString(), plugin.getClass()); 
          
          if (imageIcon != null){
            if (pluginCache == null) {
              pluginCache = new SoftReferenceCache<ThemeIcon, ImageIcon>();
              mPluginIconCache.put(plugin, pluginCache);
            }
            pluginCache.put(icon, imageIcon);
            return imageIcon;
          }
        } catch (Exception e) {
        }
      }
    }
    
    // Last Try: Icon in tvbrowser.jar
    StringBuilder buffer = new StringBuilder("/icons/").append(icon.getSize())
        .append("x").append(icon.getSize()).append("/").append(
            icon.getCategory()).append("/").append(icon.getName()).append(
            ".png");
     
    if (getClass().getResourceAsStream(buffer.toString()) != null) {
      imageIcon = ImageUtilities.createImageIconFromJar(buffer.toString(), getClass()); 
      return imageIcon;
    }

    mLog.warning("Missing theme icon " +icon.getCategory() + "/"+ icon.getName() + " for size " + Integer.toString(icon.getSize()));
    // Failed, return null
    return null;
  }

  /**
   * @return Default Icon Theme
   */
  public IconTheme getDefaultTheme() {
    return mDefaultIconTheme;
  }
}