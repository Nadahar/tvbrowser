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
package tvbrowser.core.plugin;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import tvbrowser.core.Settings;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import devplugin.ActionMenu;
import devplugin.ChannelDayProgram;
import devplugin.ContextMenuIf;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;

/**
 * An abstract implementation of a plugin proxy. Encapsulates all calls to the
 * plugin.
 * <p>
 * This means that
 * <ul>
 * <li>All RuntimeExceptions thrown by the plugin are catched</li>
 * <li>An error message will be shown, if an operation is called on an inactive
 * plugin that is only allowed with active plugins.</li>
 * </ul>
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractPluginProxy implements PluginProxy, ContextMenuIf {

  public static final String DEFAULT_PLUGIN_ICON_NAME = "imgs/Jar16.gif";

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(AbstractPluginProxy.class);

  /**
   * Holds whether the plugin is currently activated.
   * <p>
   * The {@link PluginProxyManager} holds the real state of each plugin. This
   * variable is only for speeding up the activated test.
   * 
   * @see #assertActivatedState()
   */
  private boolean mIsActivated = false;

  private PluginTreeNode mArtificialRootNode = null;

  protected AbstractPluginProxy() {
    super();
    mArtificialRootNode = new PluginTreeNode(this, false);
  }

  /**
   * Gets whether the plugin is currently activated.
   * 
   * @return whether the plugin is currently activated.
   * @see #setActivated(boolean)
   * @see #assertActivatedState()
   */
  public final boolean isActivated() {
    return mIsActivated;
  }

  /**
   * Sets whether the plugin is currently activated.
   * <p>
   * This method may only be called by the {@link PluginProxyManager} (that's
   * why it is package private).
   * 
   * @param activated Whether the plugin is currently activated.
   * @see #isActivated()
   * @see #assertActivatedState()
   */
  final void setActivated(boolean activated) {
    mIsActivated = activated;
  }

  /**
   * Sets the parent frame to the plugin.
   * 
   * @param parent The parent frame to set.
   */
  abstract void setParentFrame(Frame parent);

  /**
   * Loads the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If loading failed.
   */
  final void loadSettings(File userDirectory) throws TvBrowserException {
    try {
      doLoadSettings(userDirectory);
    } catch (RuntimeException exc) {
      throw new TvBrowserException(AbstractPluginProxy.class,

          "error.loading.runtimeException", "The plugin {0} caused an error when loading the plugin settings.", getInfo()
          .getName(), exc);
    }
  }

  /**
   * Really loads the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If loading failed.
   */
  protected abstract void doLoadSettings(File userDirectory) throws TvBrowserException;

  /**
   * Saves the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If saving failed.
   */
  final synchronized void saveSettings(File userDirectory, boolean log) throws TvBrowserException {
    // Check whether the plugin is activated
    if (!mIsActivated) {
      throw new TvBrowserException(AbstractPluginProxy.class, "error.saving.notActivated",
          "The plugin {0} can't save its settings, because it is not activated.", getInfo().getName());
    }

    // Try to save the settings
    try {
      doSaveSettings(userDirectory, log);
    } catch (Throwable t) {
      throw new TvBrowserException(AbstractPluginProxy.class, "error.saving.runtimeException",
          "The plugin {0} caused an error when saving the plugin settings.", getInfo().getName(), t);
    }
  }

  /**
   * Really saves the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If saving failed.
   */
  protected abstract void doSaveSettings(File userDirectory, boolean log) throws TvBrowserException;

  /**
   * Gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  public final PluginInfo getInfo() {
    try {
      return doGetInfo();
    } catch (RuntimeException exc) {
      // NOTE: In this case we can't use the handleError method because this
      // would cause a cyclic calling.

      String msg = mLocalizer.msg("error.getInfo", "The plugin {0} caused an "
          + "error when getting the plugin information.", getClass().getName());
      ErrorHandler.handle(msg, exc);

      return new PluginInfo();
    }
  }

  /**
   * Really gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  protected abstract PluginInfo doGetInfo();

  /**
   * Gets the SettingsTab object, which is added to the settings-window.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does
   *         not provide this feature.
   */
  public final SettingsTabProxy getSettingsTab() {
    try {
      assertActivatedState();
      return doGetSettingsTab();
    } catch (Throwable t) {
      handlePluginException(t);
      return null;
    }
  }

  /**
   * Rally gets the SettingsTab object, which is added to the settings-window.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does
   *         not provide this feature.
   */
  protected abstract SettingsTabProxy doGetSettingsTab();

  /**
   * Gets the actions for the context menu of a program.
   * 
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  public final ActionMenu getContextMenuActions(Program program) {
    try {
      ActionMenu menu = goGetContextMenuActions(program);

      if (menu != null) {
        return new ActionMenuProxy(this, menu);
      } else {
        return null;
      }
    } catch (Throwable exc) {
      handlePluginException(exc);
      return null;
    }
  }

  /**
   * Really gets the actions for the context menu of a program.
   * 
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   * 
   * @see #getContextMenuActions(Program)
   */
  protected abstract ActionMenu goGetContextMenuActions(Program program);

  /**
   * Gets the action to use for the main menu and the toolbar.
   * 
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   */
  public final ActionMenu getButtonAction() {
    try {
      ActionMenu menu = doGetButtonAction();

      if (menu != null) {
        return new ActionMenuProxy(this, menu);
      } else {
        return null;
      }
    } catch (Throwable exc) {
      handlePluginException(exc);
      return null;
    }
  }

  /**
   * Really gets the action to use for the main menu and the toolbar.
   * 
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   * 
   * @see #getButtonAction()
   */
  protected abstract ActionMenu doGetButtonAction();

  /**
   * Gets the icons to use for marking programs in the program table.
   */
  public Icon[] getMarkIcons(Program p) {
    try {
      return doGetMarkIcons(p);
    } catch (Throwable exc) {
      handlePluginException(exc);
      return null;
    }
  }

  /**
   * Gets the icon to use for marking programs in the program table.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  public Icon getMarkIcon() {
    Icon[] icon = getMarkIcons(null);

    if(icon != null && icon.length > 0) {
      return icon[0];
    } else {
      return null;
    }
  }

  /**
   * Really gets the icon to use for marking programs in the program table.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  protected abstract Icon[] doGetMarkIcons(Program p);

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   * 
   * @see #getProgramTableIcons(Program)
   */
  public final String getProgramTableIconText() {
    try {
      return doGetProgramTableIconText();
    } catch (Throwable exc) {
      handlePluginException(exc);
      return null;
    }
  }

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   * 
   * @see #getProgramTableIcons(Program)
   */
  protected abstract String doGetProgramTableIconText();

  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   * 
   * @see #getProgramTableIconText()
   */
  public final Icon[] getProgramTableIcons(Program program) {
    try {
      assertActivatedState();
      return doGetProgramTableIcons(program);
    } catch (Throwable t) {
      handlePluginException(t);
      return null;
    }
  }

  /**
   * Really gets the icons this Plugin provides for the given program. These
   * icons will be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   * 
   * @see #getProgramTableIconText()
   */
  protected abstract Icon[] doGetProgramTableIcons(Program program);

  /**
   * This method is automatically called, when the TV data update is finished.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  public final void handleTvDataUpdateFinished() {
    try {
      assertActivatedState();
      doHandleTvDataUpdateFinished();
    } catch (Throwable t) {
      handlePluginException(t);
    }
  }

  /**
   * This method is automatically called, when the TV data update is finished.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  protected abstract void doHandleTvDataUpdateFinished();

  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data can be modified by the plugin!
   * <p>
   * Override this method if you want to change/add data.
   * Don't do other things than changing/adding data, 
   * use {@link #handleTvDataAdded(ChannelDayProgram)} istead.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    try {
      assertActivatedState();
      doHandleTvDataAdded(newProg);
    } catch (Throwable t) {
      handlePluginException(t);
    }
  }
  
  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data can be modified by the plugin!
   * <p>
   * Override this method if you want to change/add data.
   * Don't do other things than changing/adding data, 
   * use {@link #handleTvDataAdded(ChannelDayProgram)} istead.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataChanged()
   */
  protected abstract void doHandleTvDataAdded(MutableChannelDayProgram newProg);

  
  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public final void handleTvDataAdded(ChannelDayProgram newProg) {
    try {
      assertActivatedState();
      doHandleTvDataAdded(newProg);
    } catch (Throwable t) {
      handlePluginException(t);
    }
  }

  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected abstract void doHandleTvDataAdded(ChannelDayProgram newProg);
  
  /**
   * This method is automatically called, when TV data was deleted. (E.g. after
   * an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public final void handleTvDataDeleted(ChannelDayProgram oldProg) {
    try {
      assertActivatedState();
      doHandleTvDataDeleted(oldProg);
    } catch (Throwable exc) {
      handlePluginException(exc);
    }
  }

  /**
   * This method is automatically called, when TV data was deleted. (E.g. after
   * an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected abstract void doHandleTvDataDeleted(ChannelDayProgram oldProg);

  /**
   * This method is automatically called, when TV data was touched (that means something was done with it).
   * (E.g. after an update).
   * <p>
   * @param removedDayProgram The old ChannelDayProgram.
   * @param addedDayProgram The new ChannelDayProgram.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public final void handleTvDataTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram) {
    try {
      assertActivatedState();
      doHandleTvDataTouched(removedDayProgram,addedDayProgram);
    } catch (Throwable t) {
      handlePluginException(t);
    }
  }

  /**
   * This method is automatically called, when TV data was added. (E.g. after an
   * update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected abstract void doHandleTvDataTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram);
  
  
  
  public void onActivation() {
    try {
      doOnActivation();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }
  }

  protected abstract void doOnActivation();

  public void onDeactivation() {
    try {
      doOnDeactivation();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }
  }

  protected abstract void doOnDeactivation();

  public boolean canUseProgramTree() {
    try {
      boolean canUse = doCanUseProgramTree();
      // deactivate artificial plugin tree for plugins providing their own implementation
      if (canUse && mArtificialRootNode != null) {
        mArtificialRootNode = null;
      }
      return canUse || (mArtificialRootNode != null && mArtificialRootNode.size() < 100);
    } catch (Throwable exc) {
      handlePluginException(exc);
      return false;
    }
  }

  /**
   * This method is called when the TV-Browser start is complete.
   * @since 2.2
   */
  public void handleTvBrowserStartFinished() {
    try {
      doHandleTvBrowserStartFinished();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }
  }

  protected abstract void doHandleTvBrowserStartFinished();

  protected abstract boolean doCanUseProgramTree();

  /**
   * Gets the name of the plugin.
   * <p>
   * This way Plugin objects may be used directly in GUI components like JLists.
   * 
   * @return the name of the plugin.
   */
  @Override
  final public String toString() {
    return getInfo().getName();
  }

  /**
   * Handles a runtime exception that was caused by the plugin.
   * 
   * @param t The exception to handle
   */
  public void handlePluginException(final Throwable t) {
    final String msg = mLocalizer.msg("error.runtimeExceptionAskDeactivation",
        "The plugin {0} caused an error. Should it be deactivated?", getInfo().getName());
    final PluginProxy proxy = this;

    // run the error handler in UI thread as the exception may occur in any thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (ErrorHandler.handle(msg, t, ErrorHandler.SHOW_YES_NO) == ErrorHandler.YES_PRESSED) {
          try {
            // deactivate Plugin
            PluginProxyManager.getInstance().deactivatePlugin(proxy);
          } catch (Throwable e) {
          }

          // Update the settings
          String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
          Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);
        }
      }});
  }

  /**
   * Gets whether the plugin supports receiving programs from other plugins with target.
   * 
   * @return Whether the plugin supports receiving programs from other plugins with target.
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  public final boolean canReceiveProgramsWithTarget() {
    try {
      return doCanReceiveProgramsWithTarget();
    } catch (Throwable exc) {
      handlePluginException(exc);
      return false;
    }
  }

  /**
   * Really gets whether the plugin supports receiving programs from other
   * plugins with target.
   * 
   * @return Whether the plugin supports receiving programs from other plugins with target.
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  protected abstract boolean doCanReceiveProgramsWithTarget();

  /**
   * Receives a list of programs from another plugin with a target.
   * 
   * @param programArr The programs passed from the other plugin.
   * @param receiveTarget The target of the programs.
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   */
  public final boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    try {
      assertActivatedState();
      return doReceivePrograms(programArr,receiveTarget);
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return false;
  }

  /**
   * Really receives a list of programs from another plugin with target.
   * 
   * @param programArr The programs passed from the other plugin with target.
   * @param receiveTarget The target of the programs.
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   */
  protected abstract boolean doReceivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget);

  /**
   * Receives a list of Strings from another plugin with a target.
   * 
   * @param values The value array passed from the other plugin.
   * @param target The receive target of the programs.
   * @return <code>True</code> if the value array was handled correct, 
   * </code>false</code> otherwise.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.7
   */
  public final boolean receiveValues(String[] values, ProgramReceiveTarget receiveTarget) {
    try {
      assertActivatedState();
      return doReceiveValues(values,receiveTarget);
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return false;
  }
  
  /**
   * Really receives a list of Strings from another plugin with a target.
   * 
   * @param values The value array passed from the other plugin.
   * @param target The receive target of the programs.
   * @return <code>True</code> if the value array was handled correct, 
   * </code>false</code> otherwise.
   * 
   * @see #canReceiveProgramsWithTarget()
   * @since 2.7
   */
  protected abstract boolean doReceiveValues(String[] values, ProgramReceiveTarget receiveTarget);
  
  /**
   * Returns an array of receive target or <code>null</code> if there is no target
   * 
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  public final ProgramReceiveTarget[] getProgramReceiveTargets() {
    try {
      assertActivatedState();
      return doGetProgramReceiveTargets();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return null;
  }

  /**
   * Really return an array of receive target or <code>null</code> if there is no target
   * 
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  protected abstract ProgramReceiveTarget[] doGetProgramReceiveTargets();

  /**
   * Returns the available program filters that the plugin supports.
   * 
   * @return The available program filters that the plugin supports or <code>null</code> if it supports no filter.
   * @since 2.5
   */
  public PluginsProgramFilter[] getAvailableFilter() {
    try {
      assertActivatedState();
      return doGetAvailableFilter();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return null;
  }

  /**
   * Really returns the available program filters that the plugin supports.
   * 
   * @return The available program filters that the plugin supports or <code>null</code> if it supports no filter.
   * @since 2.5
   */
  protected abstract PluginsProgramFilter[] doGetAvailableFilter();

  /**
   * Is used to track if a program filter be deleted.
   * Should be make sure only the plugin itself can delete program filters.
   * 
   * @param programFilter The program filter to delete.
   * @return True if the program filter component can be deleted.
   * @since 2.5
   */
  public boolean isAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter) {
    try {
      assertActivatedState();
      return doIsAllowedToDeleteProgramFilter(programFilter);
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return false;
  }

  /**
   * Really return if a program filter can be deleted.
   * 
   * @param programFilter The program filter to delete.
   * @return True if the program filter component can be deleted.
   * @since 2.5
   */
  protected abstract boolean doIsAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter);

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
    try {
      assertActivatedState();
      return doGetAvailableFilterComponentClasses();
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return null;
  }

  /**
   * Really gets the available filter component classes.
   * 
   * @return The available plugins filter components classes or <code>null</code> if no plugins filter components are supported.
   * @since 2.5
   */
  protected abstract  Class<? extends PluginsFilterComponent>[]  doGetAvailableFilterComponentClasses();

  /**
   * Gets the mark priority for the given program that this Plugin uses.
   * <p>
   * The mark priority can be {@link Program#NO_MARK_PRIORITY}, {@link Program#MIN_MARK_PRIORITY}, {@link Program#LOWER_MEDIUM_MARK_PRIORITY},
   * {@link Program#MEDIUM_MARK_PRIORITY}, {@link Program#HIGHER_MEDIUM_MARK_PRIORITY} or
   * {@link Program#MAX_MARK_PRIORITY}.
   * <p>
   * @param p The program to get the mark prioriy for.
   * @return The mark priority for the given program for this plugin.
   * @since 2.5.1
   */
  public int getMarkPriorityForProgram(Program p) {
    try {
      assertActivatedState();
      return doGetMarkPriorityForProgram(p);
    } catch (Throwable exc) {
      handlePluginException(exc);
    }

    return Program.MIN_MARK_PRIORITY;
  }

  /**
   * Really gets the mark priority for the given Program.
   * <p>
   * @param p The program to get the mark priority for.
   * @return The mark priority for the given program.
   */
  protected abstract int doGetMarkPriorityForProgram(Program p);

  /**
   * Checks whether the plugin is activated. If it is not an error message is
   * shown.
   * 
   * @throws TvBrowserException If the plugin is not activated
   */
  protected void assertActivatedState() throws TvBrowserException {
    if (!isActivated()) {
      throw new TvBrowserException(AbstractPluginProxy.class, "error.notActive",
          "It was attempted to call an operation of the inactive plugin {0} that "
          + "may only be called on activated plugins.", getInfo().getName());
    }
  }

  public Icon getPluginIcon() {
    // first try button icon
    ActionMenu actionMenu = getButtonAction();
    Icon icon = getMenuIcon(actionMenu);

    // then try the mark icon
    if (icon == null) {
      icon = getMarkIcon();
    }
    
    // and then the context menu
    if (icon == null && isActivated()) {
      actionMenu = getContextMenuActions(PluginManagerImpl.getInstance()
          .getExampleProgram());
      icon = getMenuIcon(actionMenu);
    }
    
    if (icon != null) {
      return icon;
    }
    
    return new ImageIcon(DEFAULT_PLUGIN_ICON_NAME);
  }

  private Icon getMenuIcon(ActionMenu actionMenu) {
    Action action;
    if (actionMenu != null) {
      action = actionMenu.getAction();
      if (action != null) {
        return (Icon) action.getValue(Action.SMALL_ICON);
      }
    }
    return null;
  }

  final public boolean hasArtificialPluginTree() {
    return mArtificialRootNode != null;
  }

  final public void addToArtificialPluginTree(MutableProgram program) {
    if (mArtificialRootNode != null) {
      mArtificialRootNode.addProgram(program);
    }
  }

  final public PluginTreeNode getArtificialRootNode() {
    return mArtificialRootNode;
  }

  final public void removeArtificialPluginTree() {
    mArtificialRootNode = null;
  }
}
