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

import java.awt.Frame;
import java.awt.event.ActionEvent;
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
import util.exc.TvBrowserException;
import util.ui.FixedSizeIcon;
import util.ui.ImageUtilities;

/**
 * Superclass for all Java-TV-Browser plugins.
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

  /**
   * The name to use for the big icon (the 24x24 one for the toolbar) of the
   * button action.
   * 
   * @see #getButtonAction()
   */
  public static final String BIG_ICON = "BigIcon";

  /** The localizer used by this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Plugin.class );
  
  /** The jar file of this plugin. May be used to load ressources. */  
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
   * @deprecated Use methode {@link #getParentFrame()} instead.
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
   * Gets the jar file of this plugin. May be used to load ressources.
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
   *
   * @return The settings for this plugin or <code>null</code> if this plugin
   *         does not need to save any settings.
   */
  public Properties storeSettings() {
    return null;
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
    
    return new PluginInfo(name, desc, author, new Version(0, 0));
  }


  /**
   * Gets whether the plugin supports receiving programs from other plugins.
   * <p>
   * Override this method and return <code>true</code>, if your plugin is able
   * to receive programs from other plugins.
   * 
   * @return Whether the plugin supports receiving programs from other plugins.
   * @see #receivePrograms(Program[])
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
   */
  public void receivePrograms(Program[] programArr) {
    // Call the old and deprecated method
    execute(programArr);
  }




  /**
   * Gets the actions for the context menu of a program.
   * <p>
   * Override this method to provide context menu items for programs (e.g. in
   * the program table).
   * <p>
   * The following action values will be used:
   * <ul>
   * <li><code>Action.NAME</code>: The text for the context menu item.</li>
   * <li><code>Action.SMALL_ICON</code>: The icon for the context menu item.
   *     Should be 16x16.</li>
   * </ul>
   * 
   * @param program The program the context menu will be shown for.
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
   * Override this method to provide a seetings tab. The settings tab will be
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
      String iconFileName = getMarkIconName();
      if (iconFileName != null) {
        mMarkIcon = ImageUtilities.createImageIconFromJar(iconFileName, getClass());
      }
    }
    return mMarkIcon;
  }
  
  
  /**
   * Gets the name of the file, containing your mark icon (in the jar-File).
   * Should be 16x16.
   * <p>
   * This icon is used for marking programs in the program table.
   * <p>
   * Override this method if your plugin is able to mark programs
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
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see devplugin.Plugin#handleTvDataChanged()
   */
  public void handleTvDataUpdateFinished() {
    // Call the old and deprecated method
    handleTvDataChanged();
  }


  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin!
   * <p>
   * Override this method to react on this event. You may change the TV data
   * before it will be saved.
   * 
   * @param newProg The new ChannelDayProgram.
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
    return false;
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
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   * 
   * @param program The program from whichs context menu the plugin was chosen.
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
   * This method is invoked by the host-application if the user has choosen your
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
   * This method is automatically called immediatly before the plugin gets
   * activated.
   *
   * @since 1.1
   */
  public void onActivation() {      
  }
  
  /**
   * This method is automatically called immediatly after deactivating
   * the plugin.
   *
   * @since 1.1
   */
  public void onDeactivation() {      
  }
  
  /**
   * 
   * @return true, if the programs of this plugin are handled by the plugin
   *      tree view
   * @since 1.1
   */
  public boolean canUseProgramTree() {
    return false;
  }
  
  /*public TreeNode[] getTreeNodes() {
    return null;
  }*/
  
 /* public ProgramContainer getProgramContainer() {
    return Plugin.getPluginManager().getProgramContainer(getId());
  }*/
   public PluginTreeNode getRootNode() {
     if (mRootNode == null) {
       //mRootNode = new PluginTreeNode(getInfo().getName());
       mRootNode = new PluginTreeNode(this);
       ObjectInputStream in;
       try {
        in = new ObjectInputStream(new FileInputStream(new File(Settings.getUserDirectoryName(),getId()+".node")));
        mRootNode.load(in);
        in.close();
       } catch (FileNotFoundException e) {
         
       } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }    
     }
       
     return mRootNode;
   }
   
   public void storeRootNode() {
     ObjectOutputStream out;
     try {
       out = new ObjectOutputStream(new FileOutputStream(new File(Settings.getUserDirectoryName(),getId()+".node")));
       mRootNode.store(out);
       out.close();
     } catch (IOException e) {
        e.printStackTrace();
     }    
   }


   public String toString() {
     return getInfo().getName();
   }

}
