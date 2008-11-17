/*
 * ToolTipControlPlugin
 * Copyright (C) 12-2007 René Mach
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
 */
package tooltipcontrolplugin;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.ToolTipManager;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * A plugin class for TV-Browser to control the tooltip settings.
 * 
 * @author René Mach
 */
public class ToolTipControlPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ToolTipControlPlugin.class);
  private Properties mSettings;
  
  protected static ToolTipControlPlugin mInstance;
  
  public static Version getVersion() {
    return new Version(0,21,false);
  }
  
  /**
   * Creates an instance of this plugin.
   */
  public ToolTipControlPlugin() {
    mInstance = this;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(ToolTipControlPlugin.class,mLocalizer.msg("pluginName","Tooltip control"),mLocalizer.msg("description","Controls the function of all tooltips."),"René Mach","GPL");
  }
    
  public SettingsTab getSettingsTab() {
    return new ToolTipControlPluginSettingsTab(mSettings);
  }
  
  public void loadSettings(Properties settings) {
    if(settings == null) {
      mSettings = new Properties();
    }
    else {
      mSettings = settings;
    }
    
    if(!mSettings.containsKey("dismissDelay")) {
      mSettings.setProperty("isEnabled", String.valueOf(ToolTipManager.sharedInstance().isEnabled()));
      mSettings.setProperty("dismissDelay",String.valueOf(ToolTipManager.sharedInstance().getDismissDelay()));
      mSettings.setProperty("initialDelay",String.valueOf(ToolTipManager.sharedInstance().getInitialDelay()));
      mSettings.setProperty("reshowDelay",String.valueOf(ToolTipManager.sharedInstance().getReshowDelay()));        
    }
    
    ToolTipManager.sharedInstance().setEnabled(mSettings.getProperty("isEnabled","true").equals("true"));
    ToolTipManager.sharedInstance().setInitialDelay(Integer.parseInt(mSettings.getProperty("initialDelay","500")));
    ToolTipManager.sharedInstance().setDismissDelay(Integer.parseInt(mSettings.getProperty("dismissDelay","500")));
    ToolTipManager.sharedInstance().setReshowDelay(Integer.parseInt(mSettings.getProperty("reshowDelay","500")));
  }
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  protected static void save() {
    try {
      Method m = mInstance.getClass().getMethod("saveMe", new Class[0]);
      m.invoke(mInstance, new Object[0]);
    } catch (Throwable t) { // Ignore
      }
  }
}
