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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.swing.Icon;

import util.exc.TvBrowserException;
import util.ui.ImageUtilities;

/**
 * Superclass for all TV-Browser plugins.
 * <p>
 * To create a plugin do the following:
 * <ol>
 * <li>Create a class that extends this class and name its package equal to the
 *     class name but with lowercase letters.
 *     E.g. <code>myplugin.MyPlugin</code>.</li>
 * <li>Write your plugin code in that class.</li>
 * <li>Pack your plugin class including all needed ressources in a jar file
 *     named equal to your class. E.g. <code>MyPlugin.jar</code>.
 * <li>Put the jar in the <code>plugin</code> directory of your TV-Browser
 *     installation.
 * </ol>
 * 
 * @author Martin Oberhauser
 * @author Til Schneider, www.murfman.de
 */
abstract public class Plugin {

  /** The localizer used by this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Plugin.class );

  /** The jar file of this plugin. May be used to load ressources. */  
  private JarFile mJarFile;
  /**
   * The plugin manager. It's the plugin's connection to TV-Browser.
   * <p>
   * Every communication between TV-Browser and the plugin is either initiated
   * by TV-Browser or made by using the plugin manager.
   */
  private static PluginManager mPluginManager;
  /** The parent frame. May be used for dialogs. */
  private java.awt.Frame mParentFrame;
  /** The cached icon to use for the toolbar and the menu. */
  private Icon mButtonIcon;
  /** The cached icon to use for marking programs. */
  private Icon mMarkIcon;
  
  
  /**
   * @deprecated Use methode getParentFrame() instead.
   */
  protected java.awt.Frame parent;
  
  /**
   * Called by the host-application to provide access to the plugin manager.
   */
  final public static void setPluginManager(PluginManager manager) {
    if (mPluginManager == null ) {
      mPluginManager = manager;
    }
  }
  
  
  /**
   * Use this method to call methods of the plugin manager.
   * <p>
   * The plugin manager is your connection to TV-Browser. Every communication
   * between TV-Browser and the plugin is either initiated by TV-Browser or made
   * by using the plugin manager.
   */
  final public static PluginManager getPluginManager() {
    return mPluginManager;
  }
  

  /**
   * Gets the jar file of this plugin. May be used to load ressources.
   */  
  final protected JarFile getJarFile() {
    return mJarFile;
  }
  
  
  /**
   * Called by the host-application to provide the parent frame.
   */
  final public void setParent(java.awt.Frame parent) {
    this.mParentFrame = parent;
    
    this.parent=parent;
  }
  
  
  /**
   * Gets the parent frame.
   * <p>
   * The parent frame may be used for showing dialogs.
   * 
   * @return The parent frame.
   */
  final protected java.awt.Frame getParentFrame() {
    return mParentFrame;
  }
  
  
  /**
   * Called by the host-application to provide the jar file.
   */
  final public void setJarFile(java.io.File jarFile) throws TvBrowserException {
    try {
      this .mJarFile = new JarFile(jarFile);
    } catch (java.io.IOException exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Setting file failed!\n({0})", exc);
    }
  }
  

  
  /**
   * Called by the host-application during start-up. 
   * <p>
   * Implement this method to load any objects from the file system.
   *
   * @see #writeData(ObjectOutputStream)
   */
  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
  }

  
  
  /**
   * Counterpart to loadData. Called when the application shuts down.
   * <p>
   * Implement this method to store any objects to the file system.
   *
   * @see #readData(ObjectInputStream)
   */
  public void writeData(ObjectOutputStream out) throws IOException {
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
   * Implement this method to provide information about your plugin.
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
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   */
  public String getContextMenuItemText() {
    return null;
  }
  
  
  
  /**
   * This method is called by the host-application to show the plugin in the
   * menu or in the toolbar.
   */
  public String getButtonText() {
    return mLocalizer.msg( "newPlugin" ,"New plugin" );
  }
  
  
  
  /**
   * Returns a new SettingsTab object, which is added to the settings-window.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   */
  public SettingsTab getSettingsTab() {
    return null;
  }
  
  
  
  /**
   * Gets whether the plugin supports execution of multiple programs.
   * 
   * @return Whether the plugin supports execution of multiple programs.
   * @see #execute(Program[])
   */
  public boolean supportMultipleProgramExecution() {
    return false;
  }
  
  
  
  /**
   * This method is invoked for multiple program execution.
   * 
   * @see #supportMultipleProgramExecution()
   */
  public void execute(Program[] programArr) {
  }

  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu or the toolbar.
   */
  public void execute() {
  }
  
  
  /**
   * Gets the icon used for marking programs in the program table.
   * 
   * @see #getMarkIconName()
   */
  public final Icon getMarkIcon() {
    if (mMarkIcon== null ) {
      String iconFileName = getMarkIconName();
      if (iconFileName != null) {
        mMarkIcon = ImageUtilities.createImageIconFromJar(iconFileName, getClass());
      }
    }
    return mMarkIcon;
  }
  
  
  /**
   * Gets the icon used for the toolbar and the menu.
   * 
   * @see #getButtonIconName()
   */
  public final Icon getButtonIcon() {
    if (mButtonIcon == null ) {
      String iconFileName = getButtonIconName();
      if (iconFileName != null) {
        mButtonIcon = ImageUtilities.createImageIconFromJar(iconFileName, getClass());
      }
    }
    return mButtonIcon;
  }
  
  
  /**
   * Returns the name of the file, containing your mark icon (in the jar-File).
   * <p>
   * This icon is used for marking programs in the program table.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @see #getMarkIcon()
   */
  abstract public String getMarkIconName();
  

  /**
   * Returns the name of the file, containing your button icon (in the jar-File).
   * <p>
   * This icon is used for the toolbar and the menu.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @see #getButtonIcon()
   */
  abstract public String getButtonIconName();


  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons.
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText() {
    return null;
  }
  
  
  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   * @see #getProgramTableIconText()
   */
  public Icon[] getProgramTableIcons(Program program) {
    return null;
  }
  
  
  /**
   * This method is automatically called, when the TV data has changed.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin!
   * <p>
   * Does by default nothing.
   * 
   * @param newProg The new ChannelDayProgram.
   * @deprecated Since 0.9.7.2 Use
   *             {@link #handleTvDataAdded(ChannelDayProgram)}
   *             instead.
   */
  public void handleTvDataChanged(ChannelDayProgram newProg) {
  }

  
  /**
   * This method is automatically called, when the TV data update is finished.
   * <p>
   * Does by default nothing.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  public void handleTvDataChanged() {
  }


  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin!
   * <p>
   * Does by default nothing. Use this method to mark programs that are of
   * interest for the plugin.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataAdded(ChannelDayProgram newProg) {
    // Call the old and deprecated methods
    handleTvDataChanged(newProg);
  }


  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * <p>
   * Does by default nothing. Use this method to unmark programs that were of
   * interest for the plugin.
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataDeleted(ChannelDayProgram oldProg) {
  }


  /**
   * Gets the name of the plugin.
   * <p>
   * This way Plugin objects may be used directly in GUI components like JLists.
   */  
  final public String toString() {
      return getInfo().getName();
  }
  
}
