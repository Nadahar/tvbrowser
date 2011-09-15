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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvdataservice.MutableChannelDayProgram;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.ui.ImageUtilities;
import util.ui.TVBrowserIcons;

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
  public static final String ALL_CATEGORY = "all";
  public static final String REMOTE_CONTROL_SOFTWARE_CATEGORY = "remote_soft";
  public static final String REMOTE_CONTROL_HARDWARE_CATEGORY = "remote_hard";
  public static final String ADDITONAL_DATA_SERVICE_SOFTWARE_CATEGORY = "datasources_soft";
  public static final String ADDITONAL_DATA_SERVICE_HARDWARE_CATEGORY = "datasources_hard";
  public static final String RATINGS_CATEGORY = "ratings";
  public static final String OTHER_CATEGORY = "misc";

  /**
   * logger for this class.
   */
  private static final Logger LOGGER = Logger.getLogger(Plugin.class.getName());

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
  public static final int SINGLE_CLICK_WAITING_TIME;

  static {
    int doubleClickTime = 200;
    try {
      Object property = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
      if (property != null) {
        doubleClickTime = (Integer) property;
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (doubleClickTime < 50 || doubleClickTime > 2000) {
      doubleClickTime = 200;
    }
    SINGLE_CLICK_WAITING_TIME = doubleClickTime;
  }

  /** The localizer used by this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Plugin.class );

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
   * Helper method that Loads an ImageIcon from the IconTheme with default size
   *
   * @param category Category the Icon resists in
   * @param icon Icon to load (without extension)
   * @return The Icon
   * @since 2.2
   */
  final public ImageIcon createImageIcon(String category, String icon) {
    return getPluginManager().getIconFromTheme(this, category, icon, TVBrowserIcons.SIZE_SMALL);
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

    return new PluginInfo(getClass(),name, desc, author);
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
    // This plugin supports no context menus for programs
    return null;
  }


  /**
   * Gets the actions for the context menu of a channel.
   * <p>
   * Override this method to provide context menu items for channels.
   * <p>
   * The following action values will be used:
   * <ul>
   * <li><code>Action.NAME</code>: The text for the context menu item.</li>
   * <li><code>Action.SMALL_ICON</code>: The icon for the context menu item.
   * Should be 16x16.</li>
   * </ul>
   *
   * @param channel The channel the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   * @since 3.0
   *
   * @see #getContextMenuActions(Program)
   */
  public ActionMenu getContextMenuActions(final Channel channel) {
    // This plugin supports no context menus for channels
    return null;
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
    // This plugin supports no button
    return null;
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
   * @param program The program to get the icons for.
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
    if(getMarkIcon() != null) {
      return new Icon[] {getMarkIcon()};
    }

    Icon[] icon = getMarkIconsForProgram(p);

    if(icon == null) {
      return new Icon[0];
    } else {
      return icon;
    }
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
   * @see #handleTvDataTouched(ChannelDayProgram, ChannelDayProgram)
   */
  public void handleTvDataUpdateFinished() {
    // do nothing
  }

  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data can be modified by the plugin!
   * <p>
   * Override this method if you want to change/add data fields of the programs.
   * If you only want to access the programs without changing any fields, please use
   * {@link #handleTvDataAdded(ChannelDayProgram)} instead.
   *
   * @param newProg
   *          The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataTouched(ChannelDayProgram, ChannelDayProgram)
   */
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    return;
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
   * @see #handleTvDataTouched(ChannelDayProgram, ChannelDayProgram)
   */
  public void handleTvDataAdded(ChannelDayProgram newProg) {
    // do nothing
  }


  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update). It is not called however, if programs
   * are removed at startup of the TVB due to their age!
   * <p>
   * Override this method to react on this event.
   *
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataTouched(ChannelDayProgram, ChannelDayProgram)
   */
  public void handleTvDataDeleted(ChannelDayProgram oldProg) {
  }

  /**
   * This method is automatically called, when TV data was touched (that means something was done with it).
   * (E.g. after an update).
   * <p>
   * @param removedDayProgram The old ChannelDayProgram.
   * @param addedDayProgram The new ChannelDayProgram.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   * @since 2.7.3
   */
  public void handleTvDataTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram) {

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

      loadRootNode(mRootNode);
    }

    return mRootNode;
  }

  protected void loadRootNode(final PluginTreeNode node) {
    if (node == null) {
      return;
    }
    File nodeFile = new File(Settings.getUserSettingsDirName(), getId()
        + ".node");
    if (nodeFile.canRead()) {
      try {
        StreamUtilities.objectInputStream(nodeFile,
            new ObjectInputStreamProcessor() {
              public void process(ObjectInputStream inputStream)
                  throws IOException {
                node.load(inputStream);
                inputStream.close();
              }
            });
      } catch (Exception e) {
        LOGGER.severe(mLocalizer.msg("error.couldNotReadFile",
            "Reading file '{0}' failed.", nodeFile.getAbsolutePath()));
        node.removeAllChildren();
      }
    }
  }

  /**
   * Saves the entries under the root node in a file.
   */
  public void storeRootNode() {
    storeRootNode(mRootNode);
  }

  protected void storeRootNode(final PluginTreeNode node) {
    File f = new File(Settings.getUserSettingsDirName(), getId() + ".node");
    if (node == null || node.isEmpty()) {
      f.delete();
      return;
    }
    try {
      StreamUtilities.objectOutputStream(f, new ObjectOutputStreamProcessor() {
        public void process(ObjectOutputStream out) throws IOException {
          if (node != null) {
            node.store(out);
          }
          out.close();
        }
      });
    } catch (IOException e) {
      LOGGER.severe(mLocalizer.msg("error.couldNotWriteFile","Storing file '{0}' failed.", f.getAbsolutePath()));
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

  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
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

  @Override
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

  @Override
  public int compareTo(ProgramReceiveIf o) {
    return getInfo().getName().compareTo(o.toString());
  }

  /**
   * Gets the importance value of a program.The importance of all active plugins is used to determinate
   * the opacity of the used colors of a program, therefor a mean value of all values is used.
   * <p>
   * The importance value can be created as a weighted total of the following values.
   * <ul>
   * <li>{@link Program#DEFAULT_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#MIN_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#LOWER_MEDIUM_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#MEDIUM_PROGRAM_IMPORTANCE},</li>
   * <li>{@link Program#HIGHER_MEDIUM_PROGRAM_IMPORTANCE} or</li>
   * <li>{@link Program#MAX_PROGRAM_IMPORTANCE}.</li>
   * </ul>
   * <p>
   * @param p The program to get the importance value for.
   * @return The importance value for the given program.
   * @since 3.0
   */
  public ImportanceValue getImportanceValueForProgram(Program p) {
    return new ImportanceValue((byte)1,Program.DEFAULT_PROGRAM_IMPORTANCE);
  }
  
  /**
   * Gets the category of this plugin.
   * <p>
   * The category can be one of this values.
   * Note: Don't use the NO_CATEGORY it's only for backward compatibility.
   * <ul>
   * <li>{@link #ALL_CATEGORY}</li>
   * <li>{@link #REMOTE_CONTROL_SOFTWARE_CATEGORY}</li>
   * <li>{@link #REMOTE_CONTROL_HARDWARE_CATEGORY}</li>
   * <li>{@link #ADDITONAL_DATA_SERVICE_SOFTWARE_CATEGORY}</li>
   * <li>{@link #ADDITONAL_DATA_SERVICE_HARDWARE_CATEGORY}</li>
   * <li>{@link #RATINGS_CATEGORY}</li>
   * <li>{@link #OTHER_CATEGORY}</li>
   * </ul>
   * <p>
   * @return The category of this plugin.
   * @since 3.0.2
   */
  public String getPluginCategory() {
    return ALL_CATEGORY;
  }
}
