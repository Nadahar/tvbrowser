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

package devplugin;

import java.util.Properties;
import javax.swing.Timer;
import java.util.jar.*;
import javax.swing.Icon;
import java.io.InputStream;
import java.awt.event.*;
import util.exc.*;

abstract public class Plugin {
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(Plugin.class.getName());
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Plugin.class );
  
  protected JarFile jarFile;
  private static PluginManager pluginManager;
  protected Timer timer;
  protected java.awt.Frame parent;
  private Icon buttonIcon;
  private Icon markIcon;
  
  
  
  /**
   * Called by the host-application to provide access to the plugin manager
   */
  final public static void setPluginManager(PluginManager manager) {
    if (pluginManager == null ) {
      pluginManager = manager;
    }
  }
  
  
  
  /**
   * Use this method to call methods of the plugin manager
   */
  final public static PluginManager getPluginManager() {
    return pluginManager;
  }
  
  
  
  /**
   * Called by the host-application.
   */
  final public void setParent(java.awt.Frame parent) {
    this.parent = parent;
  }
  
  
  
  /**
   * Called by the host-application
   */
  final public void setJarFile(java.io.File jarFile) throws TvBrowserException {
    try {
      this .jarFile = new JarFile(jarFile);
    } catch (java.io.IOException exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Setting file failed!\n({0})", exc);
    }
  }
  

  
  /**
   * Called by the host-application during start-up. Implement this method to load any objects
   * from the file system.
   */
  public void loadData(java.io.ObjectInputStream in) {
  }

  
  
  /**
   * Gegenstueck zu loadData. Beim Beenden der Applikation wird storeData
   * aufgerufen. Das zurÙckgegebene Objekt wird von der Applikation gespeichert
   * und beim n?chsten Programmstart mit loadData wieder an das Plug-In
   * Ùbergeben.
   *
   * Called by the host-application during shut-down. Implement this method to store any objects
   * to the file system.
   */
  public Object storeData() {
    return null;
  }
  
  
  
  /**
   * Called by the host-application during start-up. Implements this method to
   * load your plugins settings from the file system.
   */
  public void loadSettings(Properties settings) {
  }
  
  
  
  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your plugins settings to the file system.
   */
  public Properties storeSettings() {
    return null;
  }
  
  
  
  /**
   * Implement this function to provide information about your plugin.
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "unkown" ,"Unknown" );
    String desc = mLocalizer.msg( "noDescription" ,"No description" );
    String author = mLocalizer.msg( "noAuthor" ,"No author given" );
    
    return new PluginInfo(name, desc, author, new Version(0, 0));
  }
  
  
  
  /**
   * This method is called by the host-application to show the plugin in the
   * context menu.
   * Let getContextMenuItemText return null if your plugin does not provide
   * this feature.
   */
  public String getContextMenuItemText() {
    return null;
  }
  
  
  
  /**
   * This method is called by the host-application to show the plugin in the
   * menu (or in a button in further versions).
   * Let getContextMenuItemText return null if your plugin does not provide
   * this feature.
   *
   */
  public String getButtonText() {
    return mLocalizer.msg( "newPlugin" ,"New plugin" );
  }
  
  
  
  /**
   * Returns a new SettingsTab object, which is added to the settings-window.
   *
   */
  public SettingsTab getSettingsTab() {
    return null;
  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program[] program) {
  }
  
  
  
  public boolean supportMultipleProgramExecution() {
    return false;
  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
  }
  
  
  
  /**
   * Returns an Icon representing your plugin. You don't have to implement this
   * method. Implement the getMarkIcon method.
   *
   */
  final private Icon createIcon(String iconName) {
    Icon result= null ;
    if (iconName== null ) {
      return null ;
    }
    
    JarEntry entry=jarFile.getJarEntry(iconName);
    if (entry== null ) {
      System.out.println( "could not find icon '" +iconName+ "'" ); return null ;
    }
    try {
      InputStream in=jarFile.getInputStream(entry); byte [] b= new byte [( int )entry.getSize()];
      in.read(b);
      in.close();
      result= new javax.swing.ImageIcon(b);
    } catch (java.io.IOException exc) {
      String msg = mLocalizer.msg( "error.1" ,"Unable to load plugin icon from Jar.\n({0})" ,
      jarFile.getName(), exc);
      ErrorHandler.handle(msg, exc);
    } return result;
  }
  
  
  
  
  public final Icon getMarkIcon() {
    if (markIcon== null ) {
      markIcon=createIcon(getMarkIconName());
    }
    return markIcon;
  }
  
  
  
  public final Icon getButtonIcon() {
    if (buttonIcon== null ) {
      buttonIcon=createIcon(getButtonIconName());
    }
    return buttonIcon;
  }
  
  
  
  /**
   * Returns the name of the file, containing your plugin icon (in the jar-File).
   */
  abstract public String getMarkIconName();
  
  abstract public String getButtonIconName();
  
  
  
  /**
   * This method is automatically called, when the TV data has changed.
   * (E.g. after an update).
   * <p>
   * Does by default nothing.
   */
  public void handleTvDataChanged() {
  }
  
}
