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
package tvbrowser.core.plugin;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import tvbrowser.core.PluginLoader;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MutableChannelDayProgram;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.ContextMenuAction;
import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramRatingIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;

/**
 * A plugin proxy for Java plugins.
 *
 * @author Til Schneider, www.murfman.de
 */
public class JavaPluginProxy extends AbstractPluginProxy {

  /** The logger for this class */
  private static final Logger mLog
    = Logger.getLogger(JavaPluginProxy.class.getName());

  /** The plugin itself. */
  private Plugin mPlugin;

  /** The ID of this plugin. */
  private String mId;

  private PluginInfo mPluginInfo;

  private String mPluginFileName;

  /** plugin icon, only used if the plugin is not active */
  private Icon mPluginIcon;

  /**
   * file name of the icon of this plugin proxy, used for lazy loading
   */
  private String mIconFileName;
  
  private String mPluginCategory;


  public JavaPluginProxy(Plugin plugin, String pluginFileName) {
    mPlugin = plugin;
    mPluginFileName = pluginFileName;
  }

  public JavaPluginProxy(PluginInfo info, String pluginFileName, String pluginId, Icon pluginIcon, String category) {
    mPluginInfo = info;
    mPluginFileName = pluginFileName;
    mId = pluginId;
    mPluginIcon = pluginIcon;
    mPluginCategory = category;
  }

  public JavaPluginProxy(PluginInfo info, String pluginFileName, String pluginId, String iconFileName, String category) {
    mPluginInfo = info;
    mPluginFileName = pluginFileName;
    mId = pluginId;
    mIconFileName = iconFileName;
    mPluginCategory = category;
  }

  /**
   * Gets the ID of the given Java plugin.
   *
   * @param javaPlugin The Java plugin to get the ID for.
   * @return The ID of the given Java plugin.
   */
  public static String getJavaPluginId(Plugin javaPlugin) {
    return "java." + javaPlugin.getClass().getName();
  }


  /**
   * Gets the ID of this plugin.
   *
   * @return The ID of this plugin.
   */
  public String getId() {
    if (mId == null) {
      mId = getJavaPluginId(mPlugin);
    }
    return mId;
  }


  /**
   * Sets the parent frame to the plugin.
   *
   * @param parent The parent frame to set.
   */
  void setParentFrame(Frame parent) {
    if (mPlugin != null) {
      mPlugin.setParent(parent);
    }
  }


  /**
   * Really loads the settings for this plugin.
   *
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If loading failed.
   */
  protected void doLoadSettings(File userDirectory) throws TvBrowserException {
    String pluginClassName = mPlugin.getClass().getName();

    // Get all the file names
    File oldDatFile = new File(userDirectory, pluginClassName + ".dat");
    File oldPropFile = new File(userDirectory, pluginClassName + ".prop");
    File datFile = new File(userDirectory, getId() + ".dat");
    File propFile = new File(userDirectory, getId() + ".prop");

    // Rename the old data and settings file if they still exist
    oldDatFile.renameTo(datFile);
    oldPropFile.renameTo(propFile);

    // load plugin data
    if (datFile.exists()) {
      ObjectInputStream in = null;
      try {
        in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(datFile), 0x4000));
        mPlugin.readData(in);
      }
      catch (Throwable thr) {
        throw new TvBrowserException(getClass(), "error.3",
            "Loading data for plugin {0} failed.\n({1})",
            getInfo().getName(), datFile.getAbsolutePath(), thr);
      }
      finally {
        if (in != null) {
          try { in.close(); } catch (IOException exc) {
            // ignore
          }
        }
      }
    }

    // load plugin settings
    BufferedInputStream in = null;
    try {
      if (propFile.exists()) {
        Properties prop = new Properties();
        in = new BufferedInputStream(new FileInputStream(propFile), 0x4000);
        prop.load(in);
        in.close();
        mPlugin.loadSettings(prop);
      } else {
        mPlugin.loadSettings(new Properties());
      }
    }
    catch (Throwable thr) {
      throw new TvBrowserException(getClass(), "error.4",
          "Loading settings for plugin {0} failed.\n({1})",
          getInfo().getName(), propFile.getAbsolutePath(), thr);
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }
  }


  /**
   * Really saves the settings for this plugin.
   *
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If saving failed.
   */
  protected void doSaveSettings(File userDirectory, boolean log) throws TvBrowserException {
    if(log) {
      mLog.info("Storing plugin settings for " + getId() + "...");
    }

    // save the plugin data in a temp file
    File tmpDatFile = new File(userDirectory, getId() + ".dat.temp");
    try {
      StreamUtilities.objectOutputStream(tmpDatFile,
          new ObjectOutputStreamProcessor() {
            public void process(ObjectOutputStream out) throws IOException {
              mPlugin.writeData(out);
              out.close();
            }
          });

      // Saving succeeded -> Delete the old file and rename the temp file
      File datFile = new File(userDirectory, getId() + ".dat");
      datFile.delete();
      tmpDatFile.renameTo(datFile);
    }
    catch(Throwable thr) {
      throw new TvBrowserException(getClass(), "error.5",
          "Saving data for plugin {0} failed.\n({1})",
          getInfo().getName(), tmpDatFile.getAbsolutePath(), thr);
    }

    // save the plugin settings in a temp file
    FileOutputStream fOut = null;
    File tmpPropFile = new File(userDirectory, getId() + ".prop.temp");
    try {
      Properties prop = mPlugin.storeSettings();
      if (prop != null) {
        fOut = new FileOutputStream(tmpPropFile);
        prop.store(fOut, "Settings for plugin " + getInfo().getName());
        fOut.close();
      }

      // Saving succeeded -> Delete the old file and rename the temp file
      File propFile = new File(userDirectory, getId() + ".prop");
      propFile.delete();
      tmpPropFile.renameTo(propFile);
    }
    catch (Throwable thr) {
      throw new TvBrowserException(getClass(), "error.6",
          "Saving settings for plugin {0} failed.\n({1})",
          getInfo().getName(), tmpPropFile.getAbsolutePath(), thr);
    }
    finally {
      if (fOut != null) {
        try { fOut.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }
  }


  /**
   * Really gets the meta information about the plugin.
   *
   * @return The meta information about the plugin.
   */
  protected PluginInfo doGetInfo() {
    if (mPluginInfo != null) {
      return mPluginInfo;
    }

    PluginInfo info = null;

    try {
      info = mPlugin.getInfo();
    }catch(java.lang.NoSuchMethodError e) {
      String name = devplugin.Plugin.mLocalizer.msg( "unkown" ,"Unknown" );
      String desc = devplugin.Plugin.mLocalizer.msg( "noDescription" ,"No description" );
      String author = devplugin.Plugin.mLocalizer.msg( "noAuthor" ,"No author given" );

      return new PluginInfo(devplugin.Plugin.class,name, desc, author);
    }

    return info;
  }


  /**
   * Really gets the SettingsTab object, which is added to the settings-window.
   *
   * @return the SettingsTab object or <code>null</code> if the plugin does not
   *         provide this feature.
   */
  protected SettingsTabProxy doGetSettingsTab() {
    SettingsTab tab = mPlugin.getSettingsTab();
    if (tab == null) {
      return null;
    }
    return new SettingsTabProxy(tab);
  }


  /**
   * Gets the actions for the context menu of a program.
   *
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  protected ActionMenu doGetContextMenuActions(Program program) {
    return mPlugin.getContextMenuActions(program);
  }


  /**
   * Gets the actions for the context menu of a channel.
   *
   * @param channel The channel the context menu will be shown for.
   * @return the actions this plugin provides for the given channel or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  protected ActionMenu doGetContextMenuActions(final Channel channel) {
    return mPlugin.getContextMenuActions(channel);
  }

  /**
   * Really gets the action to use for the main menu and the toolbar.
   *
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   */
  protected ActionMenu doGetButtonAction() {
    if (mPlugin != null) {
      ActionMenu actMenu = mPlugin.getButtonAction();
      if (actMenu != null) {
        Action action = actMenu.getAction();
        if (action != null && !(action instanceof ContextMenuAction)) {
          if (action.getValue(Action.SMALL_ICON) == null) {
            mLog.warning("Small icon missing for button action "
                + action.getValue(Action.NAME));
            action.putValue(Action.SMALL_ICON, TVBrowserIcons.warning(TVBrowserIcons.SIZE_SMALL));
          }
          if (action.getValue(Plugin.BIG_ICON) == null) {
            mLog.warning("Big icon missing for button action "
                + action.getValue(Action.NAME));
            action.putValue(Plugin.BIG_ICON, TVBrowserIcons.warning(TVBrowserIcons.SIZE_LARGE));
          }
        }
      }
      return actMenu;
    }
    return null;
  }


  /**
   * Really gets the icons to use for marking programs in the program table.
   *
   * @return the icons to use for marking programs in the program table.
   */
  protected Icon[] doGetMarkIcons(Program p) {
    if (mPlugin != null) {
      return mPlugin.getMarkIcons(p);
    }
    return null;
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
  protected String doGetProgramTableIconText() {
    return mPlugin.getProgramTableIconText();
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
  protected Icon[] doGetProgramTableIcons(Program program) {
    return mPlugin.getProgramTableIcons(program);
  }


  /**
   * This method is automatically called, when the TV data update is finished.
   *
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  protected void doHandleTvDataUpdateFinished() {
    mPlugin.handleTvDataUpdateFinished();
  }


  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   *
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected void doHandleTvDataAdded(ChannelDayProgram newProg) {
    mPlugin.handleTvDataAdded(newProg);
  }


  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   *
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected void doHandleTvDataDeleted(ChannelDayProgram oldProg) {
    mPlugin.handleTvDataDeleted(oldProg);
  }

  /**
   * This method is automatically called, when the TV-Browser start is complete.
   */
  protected void doHandleTvBrowserStartFinished() {
    mPlugin.handleTvBrowserStartFinished();
  }

  public void doOnActivation() {
    plugin().onActivation();
  }

  private Plugin plugin() {
    if (mPlugin == null) {
      mPlugin = (Plugin) PluginLoader.getInstance().loadPlugin(new File(mPluginFileName), false);
    }
    return mPlugin;
  }

  public void doOnDeactivation() {
    mPlugin.onDeactivation();
  }

  public boolean doCanUseProgramTree() {
    return mPlugin.canUseProgramTree();
  }

  public PluginTreeNode getRootNode() {
    return mPlugin.getRootNode();
  }

  /**
   * Really gets whether the plugin supports receiving programs from other
   * plugins with target.
   *
   * @return Whether the plugin supports receiving programs from other plugins with target.
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  protected boolean doCanReceiveProgramsWithTarget() {
    return mPlugin.canReceiveProgramsWithTarget();
  }

  /**
   * Really receives a list of programs from another plugin with target.
   *
   * @param programArr The programs passed from the other plugin with target.
   * @param receiveTarget The target of the programs.
   * @see #canReceiveProgramsWithTarget()
   * @since 2.5
   */
  protected boolean doReceivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    boolean value = mPlugin.receivePrograms(programArr, receiveTarget);

    if(!value) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),mLocalizer.msg("error.noTarget","The programs for the target \"{0}\" couldn't be processed by \"{1}\".",receiveTarget,mPlugin.getInfo().getName()),Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
    }

    return value;
  }

  /**
   * Really return an array of receive target or <code>null</code> if there is no target
   *
   * @return The supported receive targets.
   * @see #canReceiveProgramsWithTarget()
   * @see #receivePrograms(Program[],ProgramReceiveTarget)
   * @since 2.5
   */
  protected ProgramReceiveTarget[] doGetProgramReceiveTargets() {
    ProgramReceiveTarget[] targets = mPlugin.getProgramReceiveTargets();

    return targets != null ? targets : ProgramReceiveTarget.createDefaultTargetArrayForProgramReceiveIf(mPlugin);
  }

  /**
   * Really returns the available program filters that the plugin supports.
   *
   * @return The available program filters that the plugin supports or <code>null</code> if it supports no filter.
   * @since 2.5
   */
  protected PluginsProgramFilter[] doGetAvailableFilter() {
    return mPlugin.getAvailableFilter();
  }

  /**
   * Really return if a program filter can be deleted.
   *
   * @param programFilter The program filter to delete.
   * @return True if the program filter component can be deleted.
   * @since 2.5
   */
  protected boolean doIsAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter) {
    return mPlugin.isAllowedToDeleteProgramFilter(programFilter);
  }

  /**
   * Really gets the available filter component classes.
   *
   * @return The available plugins filter components classes or <code>null</code> if no plugins filter components are supported.
   * @since 2.5
   */
  protected  Class<? extends PluginsFilterComponent>[] doGetAvailableFilterComponentClasses() {
    return mPlugin.getAvailableFilterComponentClasses();
  }

  /**
   * Really gets the mark priority for the given Program.
   * <p>
   * @param p The program to get the mark priority for.
   * @return The mark priority for the given program.
   */
  protected int doGetMarkPriorityForProgram(Program p) {
    return mPlugin.getMarkPriorityForProgram(p);
  }

  public String getPluginFileName() {
    return mPluginFileName;
  }

  /**
   * connect a lazy loaded plugin to its proxy
   * @param plugin
   */
  public void setPlugin(Plugin plugin, String fileName) {
    mPlugin = plugin;
    mPluginFileName = fileName;
  }

  @Override
  public Icon getPluginIcon() {
    // if only the proxy was loaded, then mPluginIcon contains the copied plugin icon
    if (mPluginIcon == null && mIconFileName != null) {
      File iconFile = new File(mIconFileName);
      if (iconFile.canRead()) {
        mPluginIcon = IOUtilities.readImageIconFromFile(iconFile);
      }
    }
    if (mPluginIcon != null) {
      return mPluginIcon;
    }
    // otherwise get the icon as always
    return super.getPluginIcon();
  }

  public ProgramRatingIf[] getProgramRatingIfs() {
    return mPlugin.getRatingInterfaces();
  }

  public String getButtonActionDescription() {
    return mPlugin.getInfo().getDescription();
  }

  @Override
  protected void doHandleTvDataAdded(MutableChannelDayProgram newProg) {
    mPlugin.handleTvDataAdded(newProg);
  }

  @Override
  protected boolean doReceiveValues(String[] values,
      ProgramReceiveTarget receiveTarget) {

    return mPlugin.receiveValues(values,receiveTarget);
  }

  @Override
  protected void doHandleTvDataTouched(ChannelDayProgram removedDayProgram,
      ChannelDayProgram addedDayProgram) {
    mPlugin.handleTvDataTouched(removedDayProgram,addedDayProgram);
  }

  @Override
  protected ImportanceValue doGetImportanceValueForProgram(Program p) {
    return mPlugin.getImportanceValueForProgram(p);
  }

  @Override
  public String getPluginCategory() {
    if(mPlugin != null) {
      return mPlugin.getPluginCategory();
    }
    
    return mPluginCategory;
  }

  @Override
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mPlugin.getPluginCenterPanelWrapper();
  }
}
