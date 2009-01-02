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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvdataservice.MutableChannelDayProgram;
import util.exc.TvBrowserException;
import util.ui.FixedSizeIcon;
import util.ui.ImageUtilities;

/**
 * Superclass for all Java-TV-Browser plugins.
 * <p>
 * To create a plugin do the following:
 * <ol>
 * <li>Create a class that extends this class and name its package equal to the
 * class name but with lower case letters. E.g. <code>myplugin.MyPlugin</code>.</li>
 * <li>Write your plugin code in that class.</li>
 * <li>Pack your plugin class including all needed resources in a jar file named
 * equal to your class. E.g. <code>MyPlugin.jar</code>.
 * <li>Put the jar in the <code>plugin</code> directory of your TV-Browser
 * installation.
 * </ol>
 * 
 * @author Martin Oberhauser
 * @author Til Schneider, www.murfman.de
 */
abstract public class Plugin implements Marker, ContextMenuIf, ProgramReceiveIf {

  /**
   * The name to use for the big icon (the 22x22 one for the toolbar) of the
   * button action.
   * 
   * @see #getButtonAction()
   */
  public static final String BIG_ICON = "BigIcon";

  /** The name to use for disabling a menu part for
   *  showing in ProgramInfo. 
   *  @since 2.6 */
  public static final String DISABLED_ON_TASK_MENU = "DISABLED_ON_TASK_MENU";
  
  /**
   * The waiting time for single click performing.
   * @since 2.7
   */
  public static final int SINGLE_CLICK_WAITING_TIME = 200;
  
  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Plugin.class );

  /** The jar file of this plugin. May be used to load resources. */
  private JarFile mJarFile;

  private PluginTreeNode mRootNode;

  /**
   * The plugin manager. It's the plugin's connection to TV-Browser.
   * <p>
   * Every communication between TV-Browser and the plugin is either initiated
   * by TV-Browser or made by using the plugin manager.
   */
  private static PluginManager mPluginManager;

  /** The parent frame. May be used for dialogs. */
  private Frame mParentFrame;

  /** The cached icon to use for marking programs. */
  private Icon mMarkIcon;
  
  /**
   * The old member for the parent frame.
   * <p>
   * Some old plugin may still directly access it. 
   * 
   * @deprecated Use method {@link #getParentFrame()} instead.
   */
  protected Frame parent;

  /**
   * Called by the host-application to provide access to the plugin manager.
   * 
   * @param manager The plugin manager the plugins should use.
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
   * 
   * @return The plugin manager.
   */
  final public static PluginManager getPluginManager() {
    return mPluginManager;
  }


  /**
   * Called by the host-application to provide the jar file.
   *
   * @param jarFile The jar file of this plugin.
   * @throws TvBrowserException If the given file is no jar file.
   */
  final public void setJarFile(File jarFile) throws TvBrowserException {
    try {
      mJarFile = new JarFile(jarFile);
    } catch (java.io.IOException exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Setting file failed!\n({0})", exc);
    }
  }

  /**
   * Gets the jar file of this plugin. May be used to load resources.
   * 
   * @return The jar file of this plugin.
   */  
  final protected JarFile getJarFile() {
    return mJarFile;
  }


  /**
   * Gets the ID of this plugin.
   * 
   * @return The ID of this plugin.
   */
  final public String getId() {
    return getPluginManager().getJavaPluginId(this);
  }


  /**
   * Called by the host-application to provide the parent frame.
   *
   * @param parent The parent frame.
   */
  final public void setParent(Frame parent) {
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
  final protected Frame getParentFrame() {
    return mParentFrame;
  }


  /**
   * Helper method that loads an ImageIcon from the plugin jar file and returns
   * it.
   * 
   * @param fileName The name of the icon file.
   * @return The icon.
   */
  final protected ImageIcon createImageIcon(String fileName) {
    return ImageUtilities.createImageIconFromJar(fileName, getClass());
  }

  /**
   * Helper method that loads an ImageIcon from the file system and returns
   * it.
   * 
   * @param fileName The name of the icon file.
   * @return The icon.
   * @since 2.3
   */
  final protected ImageIcon createImageIconForFileName(String fileName) {
    return ImageUtilities.createImageIconFromJar(fileName, null);
  }
  
  /**
   * Helper method that Loads an ImageIcon from the IconTheme
   * 
   * @param category Category the Icon resists in
   * @param icon Icon to load (without extension)
   * @param size Size of the Icon
   * @return The Icon
   * @since 2.2
   */
  final public ImageIcon createImageIcon(String category, String icon, int size) {
    return getPluginManager().getIconFromTheme(this, category, icon, size);
  }  
  
  /**
   * Helper method that Loads an ImageIcon from the IconTheme
   * 
   * @param icon Icon to load
   *
   * @return The Icon
   * @since 2.2
   */
  final public ImageIcon createImageIcon(ThemeIcon icon) {
    return getPluginManager().getIconFromTheme(this, icon);
  }

  /**
   * Called by the host-application during start-up. 
   * <p>
   * Override this method to load any objects from the file system.
   *
   * @param in The stream to read the objects from.
   * @throws IOException If reading failed.
   * @throws ClassNotFoundException If an object could not be casted correctly.
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
   * Override this method to store any objects to the file system.
   * ATTENTION: Don't use any logger, thread or access to Frames in this method.
   * 
   * @param out The stream to write the objects to
   * @throws IOException If writing failed.
   *
   * @see #readData(ObjectInputStream)
   */
  public void writeData(ObjectOutputStream out) throws IOException {
  }


  /**
   * Called by the host-application during start-up.
   * <p>
   * Override this method to load your plugins settings from the file system.
   * 
   * @param settings The settings for this plugin (May be empty).
   */
  public void loadSettings(Properties settings) {
  }


  /**
   * Called by the host-application during shut-down.
   * <p>
   * Override this method to store your plugins settings to the file system.
   * ATTENTION: Don't use any logger, thread or access to Frames in this method.
   *
   * @return The settings for this plugin or <code>null</code> if this plugin
   *         does not need to save any settings.
   */
  public Properties storeSettings() {
    return null;
  }

  /**
   * Gets the version of this plugin.
   * <p>
   * Override this to provide a check of the plugin
   * for the version to load from main plugins dir
   * or the user plugins dir. The plugin with the highest
   * version will be loaded.
   * 
   * @return The version of this plugin.
   * @since 2.6
   */
  public static Version getVersion() {
    return new Version(0, 0);
  }

  /**
   * Gets the meta information about the plugin.
   * <p>
   * Override this method to provide information about your plugin.
   * 
   * @return The meta information about the plugin.
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "unkown" ,"Unknown" );
    String desc = mLocalizer.msg( "noDescription" ,"No description" );
    String author = mLocalizer.msg( "noAuthor" ,"No author given" );

    return new PluginInfo(name, desc, author, getVersion());
  }


  /**
   * Gets whether the plugin supports receiving programs from other plugins.
   * <p>
   * Override this method and return <code>true</code>, if your plugin is able
   * to receive programs from other plugins.
   * 
   * @return Whether the plugin supports receiving programs from other plugins.
   * @see #receivePrograms(Program[])
   * @deprecated Since 2.5 Use {@link #canReceiveProgramsWithTarget()} instead.
   */
  public boolean canReceivePrograms() {
    // Call the old and deprecated method
    return supportMultipleProgramExecution();
  }


  /**
   * Receives a list of programs from another plugin.
   * <p>
   * Override this method to receive programs from other plugins.
   * 
   * @param programArr The programs passed from the other plugin.
   * @see #canReceivePrograms()
   * @deprecated Since 2.5 Use {@link #receivePrograms(Program[],ProgramReceiveTarget)} instead.
   */
  public void receivePrograms(Program[] programArr) {
    // Call the old and deprecated method
    execute(programArr);
  }




  /**
   * Gets the actions for the context menu of a program.
   * <p>
   * Override this method to provide context menu items for programs (e.g. in
   * the program table). If your plugin shows a context menu only for some
   * programs, but not for all, then you should explicitly return a non-<code>null</code>
   * menu for the example program. Otherwise your context menu will not be shown
   * in the settings dialog for the context menu order.
   * <p>
   * The following action values will be used:
   * <ul>
   * <li><code>Action.NAME</code>: The text for the context menu item.</li>
   * <li><code>Action.SMALL_ICON</code>: The icon for the context menu item.
   * Should be 16x16.</li>
   * </ul>
   * 
   * @param program
   *          The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   * 
   * @see #getProgramFromContextMenuActionEvent(ActionEvent)
   */
  public ActionMenu getContextMenuActions(final Program program) {
    // Check whether the old and deprecated methods are used
    String contextMenuItemText = getContextMenuItemText();
    if (contextMenuItemText != null) {
      // The old and deprecated methods are used -> create an action for them
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          execute(program);
        }
      };
      action.putValue(Action.NAME, contextMenuItemText);
      action.putValue(Action.SMALL_ICON, getMarkIcon());

      //return new Action[] { action };
      return new ActionMenu(action);
    } else {
      // This plugin supports no context menus
      return null;
    }
  }


  /**
   * Gets the Program from the ActionEvent that was passed to a context menu
   * action
   * <p>
   * NOTE: At the moment the Program is passed as ActionEvent source. Please
   *       use this method to get the program and not directly the ActionEvent
   *       source. Because in future versions of TV-Browser the Program may be
   *       passed in another way!!  
   * 
   * @param evt The ActionEvent to get the Program from.
   * @return The Program from the ActionEvent.
   * 
   * @see #getContextMenuActions(Program)
   */
  protected final Program getProgramFromContextMenuActionEvent(ActionEvent evt) {
    return (Program) evt.getSource();
  }


  /**
   * Gets the action to use for the main menu and the toolbar.
   * <p>
   * Override this method to provide a menu item in the main menu and a toolbar
   * button.
   * <p>
   * The following action values will be used:
   * <ul>
   * <li><code>Action.NAME</code>: The text for the main menu item and the
   *     toolbar button.</li>
   * <li><code>Action.SHORT_DESCRIPTION</code>: The description for the button
   *     action. Used as tooltip and for the status bar.</li>
   * <li><code>Action.SMALL_ICON</code>: The icon for the main menu item. Should
   *     be 16x16.</li>
   * <li><code>BIG_ICON</code>: The icon for the toolbar button. Should be
   *     24x24.</li>
   * </ul>
   *
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   */
  public ActionMenu getButtonAction() {
    // Check whether the old and deprecated methods are used
    String buttonText = getButtonText();
    if (buttonText != null) {
      // The old and deprecated methods are used -> create an action for them
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          execute();
        }
      };
      action.putValue(Action.NAME, buttonText);
      action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());
      String iconFileName = getButtonIconName();
      if (iconFileName != null) {
        Icon icon = ImageUtilities.createImageIconFromJar(iconFileName, getClass());
        if (icon != null) {
          action.putValue(Action.SMALL_ICON, icon);
          action.putValue(BIG_ICON, new FixedSizeIcon(24, 24, icon));
        }
      }

      return new ActionMenu(action);
    } else {
      // This plugin supports no button
      return null;
    }
  }


  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Override this method if your plugin provides icons for the program table
   * (shown below the start time). The returned String will be shown in settings
   * dialog (german: Aussehen->Sendungsanzeige->Plugin-Icons).
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText() {
    return null;
  }


  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Override this method to return the icons for the program table (shown below
   * the start time).
   * <p>
   * This method is only called, if the option to show program table icons for
   * this plugin is set in the options.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code> if the plugin
   *         does not provide this feature.
   *
   * @see #getProgramTableIconText()
   */
  public Icon[] getProgramTableIcons(Program program) {
    return null;
  }


  /**
   * Gets the SettingsTab object, which is added to the settings-window.
   * <p>
   * Override this method to provide a settings tab. The settings tab will be
   * shown in the settings dialog in the plugin section.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does not
   *         provide this feature.
   */
  public SettingsTab getSettingsTab() {
    return null;
  }

  /**
   * Gets the icon used for marking programs in the program table.
   * 
   * @return the icon to use for marking programs in the program table.
   * 
   * @see #getMarkIconName()
   */
  public final Icon getMarkIcon() {
    if (mMarkIcon== null ) {
      ThemeIcon icon = getMarkIconFromTheme();
      
      if (icon != null) {
        mMarkIcon = IconLoader.getInstance().getIconFromTheme(this, icon);
      }
      
      if (mMarkIcon == null) {
        String iconFileName = getMarkIconName();
        if (iconFileName != null) {
          mMarkIcon = ImageUtilities.createImageIconFromJar(iconFileName, getClass());
        }
      }
      
    }
    
    return mMarkIcon;
  }
  
  /**
   * Gets the icons used for marking programs in the program table.
   * 
   * @return the icons to use for marking programs in the program table.
   * 
   * @since 2.5
   */
  public final Icon[] getMarkIcons(Program p) {
    if(getMarkIcon() != null)  
      return new Icon[] {getMarkIcon()};
    
    Icon[] icon = getMarkIconsForProgram(p);
    
    if(icon == null)
      return new Icon[0];
    else
      return icon;
  }
  

  /**
   * This gets the mark icons for a Program.
   * 
   * Please cache the icons in the Plugin.
   * 
   * @param p The Program to get the icons for.
   * @return The icons for the Program.
   * @since 2.5
   */
  public Icon[] getMarkIconsForProgram(Program p) {
    return null;
  }

  /**
   * This gets the ThemeIcon containing your mark icon.
   * 
   * This Function uses the Icon-Theme-Function of the TV-Browser. For details
   * see {@link PluginManager#getIconFromTheme(Plugin, String, String, int)}
   * 
   * @return ThemeIcon that identifies the Icon in the Theme
   * @since 2.2
   */
  public ThemeIcon getMarkIconFromTheme() {
    return null;
  }

  /**
   * Gets the name of the file, containing your mark icon (in the jar-File).
   * Should be 16x16.
   * <p>
   * This icon is used for marking programs in the program table.
   * <p>
   * Override this method if your plugin is able to mark programs
   *
   * As an alternative you can use an Icon from the Icon-Theme using {@link #getMarkIconFromTheme()}
   *
   * @return the name of the file, containing your icon for the toolbar or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getMarkIcon()
   * @see Program#mark(Plugin)
   * @see Program#unmark(Plugin)
   */
  protected String getMarkIconName() {
    return null;
  }

  /**
   * This method is automatically called, when the TV data update is finished.
   * <p>
   * Override this method to react on this event.
   * 
   * If you want to read data from the Internet use this method to track if a
   * connection was established. ATTENTION: If you do so take care of the
   * TV-Browser start, at the start this method mustn't use an Internet
   * connection. Use the method handleTvBrowserStartFinished() to track if the
   * TV-Browser start was finished before allowing access to the Internet in
   * this method.
   * 
   * @see #handleTvBrowserStartFinished()
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see devplugin.Plugin#handleTvDataChanged()
   */
  public void handleTvDataUpdateFinished() {
    // Call the old and deprecated method
    handleTvDataChanged();
  }

  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data can be modified by the plugin!
   * <p>
   * Override this method if you want to change/add data. Don't do other things
   * than changing/adding data, use
   * {@link #handleTvDataAdded(ChannelDayProgram)} instead.
   * 
   * @param newProg
   *          The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    
  }

  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data cannot be changed in here because the saving of the data was
   * already done.
   * <p>
   * So use this method if you want to mark or do something else than changing
   * with the program. If you want to change/add data use
   * {@link #handleTvDataAdded(MutableChannelDayProgram)} instead.
   * 
   * @param newProg
   *          The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataAdded(ChannelDayProgram newProg) {
    // Call the old and deprecated method
    handleTvDataChanged(newProg);
  }


  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * <p>
   * Override this method to react on this event.
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataDeleted(ChannelDayProgram oldProg) {
  }


  // The old and deprecated methods


  /**
   * Gets whether the plugin supports execution of multiple programs.
   * 
   * @return Whether the plugin supports execution of multiple programs.
   *
   * @see #execute(Program[])
   * @deprecated Since 1.1. Use {@link #canReceivePrograms()} instead.
   */
  public boolean supportMultipleProgramExecution() {
    return canReceiveProgramsWithTarget();
  }


  /**
   * This method is invoked for multiple program execution.
   * 
   * @param programArr The programs to execute this plugins with.
   *
   * @see #supportMultipleProgramExecution()
   * @deprecated Since 1.1. Use {@link #receivePrograms(Program[])} instead.
   */
  public void execute(Program[] programArr) {
    if(canReceiveProgramsWithTarget())
      receivePrograms(programArr, ProgramReceiveTarget.createDefaultTargetArrayForProgramReceiveIf(this)[0]);
  }


  /**
   * Gets the text to use for the context menu.
   * <p>
   * This method is called by the host-application to show the plugin in the
   * context menu.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return the text to use for the context menu or <code>null</code> if the
   *         plugin does not provide this feature.
   * 
   * @deprecated Since 1.1. Use {@link #getContextMenuActions(Program)} instead.
   */
  public String getContextMenuItemText() {
    return null;
  }

  /**
   * This method is invoked by the host-application if the user has chosen your
   * plugin from the context menu.
   * 
   * @param program
   *          The program from whichs context menu the plugin was chosen.
   * 
   * @deprecated Since 1.1. Use {@link #getContextMenuActions(Program)} instead.
   */
  public void execute(Program program) {
  }


  /**
   * Gets the text to use for the main menu or the toolbar.
   * <p>
   * This method is called by the host-application to show the plugin in the
   * menu or in the toolbar.
   *
   * @return the text to use for the menu or the toolbar or <code>null</code> if
   *         the plugin does not provide this feature.
   * 
   * @deprecated Since 1.1. Use {@link #getButtonAction()} instead.
   */
  protected String getButtonText() {
    return null;
  }


  /**
   * Returns the name of the file, containing your button icon (in the jar-File).
   * <p>
   * This icon is used for the toolbar and the menu. Return <code>null</code>
   * if your plugin does not provide this feature.
   * 
   * @return the name of the file, containing your icon for the main menu and
   *         the toolbar.
   * 
   * @deprecated Since 1.1. Use {@link #getButtonAction()} instead.
   */
  protected String getButtonIconName() {
    return null;
  }

  /**
   * This method is invoked by the host-application if the user has chosen your
   * plugin from the menu or the toolbar.
   * 
   * @deprecated Since 1.1. Use {@link #getButtonAction()} instead.
   */
  public void execute() {
  }


  /**
   * This method is automatically called, when the TV data update is finished.
   * <p>
   * Does by default nothing.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @deprecated Since 1.1. Use {@link #handleTvDataUpdateFinished()} instead.
   */
  public void handleTvDataChanged() {
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
   *
   * @deprecated Since 0.9.7.2. Use
   *             {@link #handleTvDataAdded(ChannelDayProgram)} instead.
   */
  public void handleTvDataChanged(ChannelDayProgram newProg) {
  }

  /**
   * This method is automatically called immediately before the plugin gets
   * activated.
   * 
   * @since 1.1
   */
  public void onActivation() {      
  }

  /**
   * This method is automatically called immediately after deactivating the
   * plugin. ATTENTION: Don't use any logger, thread or access to Frames in this
   * method.
   * 
   * @since 1.1
   */
  public void onDeactivation() {      
  }

  /**
   * Signal whether this plugin participates in the plugin tree view or not.
   * @see #getRootNode()
   * @return true, if the programs of this plugin are handled by the plugin
   *      tree view
   * @since 1.1
   */
  public boolean canUseProgramTree() {
    return false;
  }

  /**
   * This method is called when the TV-Browser start is finished.
   * @since 2.2
   */
  public void handleTvBrowserStartFinished() { 
  }
  
  /**
   * Gets the root node of the plugin for the plugin tree.
   * @see #canUseProgramTree()
   * 
   * @return The root node.
   */
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this);

      ObjectInputStream in;
      File f = new File(Settings.getUserSettingsDirName(),getId()+".node");
      try {
        in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f), 0x2000));
        mRootNode.load(in);
        in.close();
      } catch (FileNotFoundException e) {
        // ignore
      } catch (Exception e) {
        util.exc.ErrorHandler.handle(mLocalizer.msg("error.couldNotReadFile","Reading file '{0}' failed.", f.getAbsolutePath()), e);
      }
    }

    return mRootNode;
  }

  /**
   * Saves the entries under the root node in a file.
   */
  public void storeRootNode() {
    ObjectOutputStream out;
    File f = new File(Settings.getUserSettingsDirName(),getId()+".node");
    try {
      out = new ObjectOutputStream(new FileOutputStream(f));
      if (mRootNode != null) {
        mRootNode.store(out);
      }
      out.close();
    } catch (IOException e) {
      util.exc.ErrorHandler.handle(mLocalizer.msg("error.couldNotWriteFile","Storing file '{0}' failed.", f.getAbsolutePath()), e);
    }
  }
  
  /**
   * Gets whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @return Whether the ProgramReceiveIf supports receiving programs from other plugins with a special target.
   * 
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */  
  public boolean canReceiveProgramsWithTarget() {
    return false;
  }
  
  /**
   * Receives a list of programs from another plugin with a target.
   * 
   * @param programArr The programs passed from the other plugin.
   * @param receiveTarget The receive target of the programs.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   */
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    // check if this should call the old method
    if((receiveTarget == null || ProgramReceiveTarget.isDefaultProgramReceiveTargetForProgramReceiveIf(this,receiveTarget)) && canReceivePrograms()) {
      receivePrograms(programArr);
      return true;
    }
      
    return false;
  }

  /**
   * Receives a list of Strings from another plugin with a target.
   * 
   * @param values
   *          The value array passed from the other plugin.
   * @param receiveTarget
   *          The receive target of the programs.
   * @return <code>true</code> if the value array was handled correct,
   *         </code>false</code> otherwise.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.7
   */
  public boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget) {
    return false;
  }
  
  /**
   * Returns an array of receive target or <code>null</code> if there is no target
   * 
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return ProgramReceiveTarget.createDefaultTargetArrayForProgramReceiveIf(this);
  }

  /**
   * Returns the available program filters that the plugin supports.
   * @since 2.5
   * 
   * @return The available program filters that the plugin supports or <code>null</code> if it supports no filter.
   */
  public PluginsProgramFilter[] getAvailableFilter() {
    return null;
  }

  /**
   * Is used to track if a program filter be deleted.
   * Should be make sure only the plugin itself can delete program filters.
   * 
   * @param programFilter The program filter to delete.
   * @return True if the program filter component can be deleted.
   */
  public boolean isAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter) {
    return false;
  }

  public String toString() {
    return getInfo().getName();
  }
  
  /**
   * Returns the available plugins filter component classes.
   * <br>
   * ATTENTON: Use return <code>(Class<? extends PluginsFilterComponent>[]) new Class[] {MyFilterComponent1.class,MyFilterComponent2.class};</code>
   * because the creation of a class array with generic type didn't work.
   * 
   * @return The available plugins filter components classes or <code>null</code> if no plugins filter components are supported.
   * @since 2.5
   */
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return null;
  }

  /**
   * Gets the mark priority for the given program that this Plugin uses.
   * <p>
   * The mark priority can be
   * <ul>
   * <li>{@link Program#NO_MARK_PRIORITY},</li>
   * <li>{@link Program#MIN_MARK_PRIORITY},</li>
   * <li>{@link Program#LOWER_MEDIUM_MARK_PRIORITY},</li>
   * <li>{@link Program#MEDIUM_MARK_PRIORITY},</li>
   * <li>{@link Program#HIGHER_MEDIUM_MARK_PRIORITY} or</li>
   * <li>{@link Program#MAX_MARK_PRIORITY}.</li>
   * </ul>
   * <p>
   * 
   * @param p
   *          The program to get the mark priority for.
   * @return The mark priority for the given program for this plugin.
   * @since 2.5.1
   */
  public int getMarkPriorityForProgram(Program p) {
    return Settings.propProgramPanelUsedDefaultMarkPriority.getInt();
  }
  
  /**
   *  Says the plugin proxy manager to store the settings and data of this plugin.
   *  <p>
   *  @return <code>True</code> if the settings could be saved successfully.
   *  @since 2.5.3
   */
  protected final boolean saveMe() {
    return PluginProxyManager.getInstance().saveSettings(PluginProxyManager.getInstance().getActivatedPluginForId(getId()));
  }
  
  /**
   * Sets the window position and size for the given window from remembered values for that id.

   * @param windowId The relative id of the window. The ID only needs to unique for this plugin.
   * @param window The window to layout.
   * 
   * @since 2.7
   */
  public final void layoutWindow(String windowId, Window window) {
    layoutWindow(windowId, window, null);
  }
  
  /**
   * Sets the window position and size for the given window from remembered values for that id.

   * @param windowId The relative id of the window. The ID only needs to unique for this plugin. 
   * @param window The window to layout.
   * @param defaultSize The default size for the window.
   * 
   * @since 2.7
   */
  public final void layoutWindow(String windowId, Window window, Dimension defaultSize) {
    Settings.layoutWindow(getId() + "." + windowId, window, defaultSize);
  }

  /**
   * If this plugin can rate programs, this interface makes it possible to offer this ratings
   * to other plugins. You can get all ProgramRatingIfs of all plugins using {@link PluginManager#getAllProgramRatingIfs()}
   *
   * The plugin can return more than one ratingif, e.g. average ratings, user rating ...
   *
   * @return the RatingIfs of this plugin
   * @since 2.7
   */
  public ProgramRatingIf[] getRatingInterfaces() {
    return null;
  }
}
