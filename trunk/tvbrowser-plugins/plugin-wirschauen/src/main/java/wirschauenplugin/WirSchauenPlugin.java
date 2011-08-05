/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wirschauenplugin;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ChannelDayProgram;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * With this plugin it is possible to add information about a program and send it to wirschauen.de.
 */
public final class WirSchauenPlugin extends Plugin
{
  /**
   * the url to the wirschauen server.
   */
  public static final String BASE_URL = "http://www.wirschauen.de/events/";

  /**
   * Localizer.
   */
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(WirSchauenPlugin.class);

  /**
   * true, if this plugin is stable. false otherwise. used for the version-object.
   */
  private static final boolean IS_STABLE = true;

  /**
   * the version of this plugin.
   */
  private static final Version VERSION = new Version(0, 20, 3, IS_STABLE);

  /**
   * this class is a singleton. kind of. the constructor is not restricted so
   * there might be more than one instance. but the getInstance method will
   * always return the last created instance.
   */
  private static WirSchauenPlugin mInstance;

  /**
   * the plugin info. see getInfo. lazy init.
   */
  private static PluginInfo mPluginInfo;

  /**
   * the app icon for the context menu, lazy init.
   */
  private static Icon mIcon;

  /**
   * the icon for a missing description, shown in the program table.
   */
  private static Icon mMissingDescriptionIcon;

  /**
   * the plugin is restricted to specific channels, ie vg media. this might be
   * removed in future versions.
   */
  private static ArrayList<String> mAllowedChannels = new ArrayList<String>(21);
  static
  {
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:rtl2");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:superrtl");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:pro7");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:sat1");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:vox");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:COMEDYCENTRAL");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:dsf");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:kabel1");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:MTV");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:n24");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:NICK");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:NTV");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:VIVA");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:puls4");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLPASSION");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLLIVING");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:RTLCRIME");
    mAllowedChannels.add("tvbrowserdataservice.TvBrowserDataService:sixx");
  }

  /**
   * all the filters defined by this plugin (for now its one). lazy init, see getAvailableFilter.
   */
  private static PluginsProgramFilter mFilter;

  /**
   * the filter implementation. lazy init, see getAvailableFilter.
   */
  private static WirSchauenFilterComponent mComponent;

  /**
   * the root node for the plugin tree. overrides Plugin with a marked tree.
   * this field is lazy loaded, so NEVER access it directly but use getRootNode().
   */
  private PluginTreeNode mRootNode;

  /**
   * this list holds all the program-ids that are linked with the omdb. its
   * necessary to keep track of these programs cause the tvb will not mark
   * programs at startup. so we have to persist this data and do the marking
   * ourselves.
   */
  private ArrayList<ProgramId> mLinkedPrograms = new ArrayList<ProgramId>();

  /**
   * all settings of this plugin.
   */
  private WirSchauenSettings mSettings;




  /**
   * Creates the Plugin.
   */
  public WirSchauenPlugin()
  {
    mInstance = this;
  }



  /**
   * Returns the last created Instance of the Plugin.
   *
   * @return Plugin-Instance
   */
  public static WirSchauenPlugin getInstance()
  {
    return mInstance;
  }


  /**
   * Gets the version of this plugin.
   *
   * @return the version
   * @see devplugin.Plugin#getVersion
   */
  public static Version getVersion()
  {
    return VERSION;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public PluginInfo getInfo()
  {
    if (mPluginInfo == null)
    {
      mPluginInfo = new PluginInfo(WirSchauenPlugin.class, LOCALIZER.msg("name", "WirSchauenPlugin"), LOCALIZER.msg("desc", "Makes it possible to add descriptions to programs"), "TV-Browser Team", "GPL 3");
    }
    return mPluginInfo;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public ActionMenu getContextMenuActions(final Program program)
  {
    if (isProgramAllowed(program))
    {
      @SuppressWarnings("serial")
      final AbstractAction action = new AbstractAction()
      {
        public void actionPerformed(final ActionEvent event)
        {
          new DialogController(UiUtilities.getLastModalChildOf(getParentFrame())).startDialogs(program);
        }
      };
      action.putValue(Action.NAME, LOCALIZER.msg("contextMenu", "Recommend text for this program"));
      action.putValue(Action.SMALL_ICON, getIcon());

      return new ActionMenu(action);
    }
    return null;
  }

  /**
   * helper method for lazy init.
   *
   * @return the icon for the context menu.
   */
  public Icon getIcon()
  {
    if (mIcon == null)
    {
      mIcon = createImageIcon("apps", "wirschauen", 16);
    }
    return mIcon;
  }


  /**
   * helper method for lazy init.
   *
   * @return the icon for the missing description marker.
   */
  private Icon getMissingDescriptionIcon()
  {
    if (mMissingDescriptionIcon == null)
    {
      mMissingDescriptionIcon = createImageIcon("apps", "wirschauen_noDesc", 16);
    }
    return mMissingDescriptionIcon;
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  @Override
  public ThemeIcon getMarkIconFromTheme()
  {
    return new ThemeIcon("apps", "wirschauen", 16);
  }


  /**
   * all programs with an omdb link will marked with prio 1. all programs
   * which the user linked to omdb will be marked with prio 2.
   *
   * @param program the program to mark
   * @return the mark priority
   * @see devplugin.Plugin#getMarkPriorityForProgram(devplugin.Program)
   */
  @Override
  public int getMarkPriorityForProgram(final Program program)
  {
    int prio = Program.NO_MARK_PRIORITY;
    if (getRootNode().contains(program))
    {
      //mark all programs which were linked by the user (those are in the tree)
      prio = mSettings.getMarkPriorityForOwnOmdbLink();
    }
    else if (program.getTextField(ProgramFieldType.URL_TYPE) != null)
    {
      //mark all programs with omdb-link
      prio = mSettings.getMarkPriorityForOmdbLink();
    }
    return prio;
  }


  /**
   * {@inheritDoc}
   * @return true
   * @see devplugin.Plugin#canUseProgramTree()
   */
  @Override
  public boolean canUseProgramTree()
  {
    return true;
  }


  /**
   * marks this program and puts it into the plugin tree.
   *
   * @param program the program to mark
   */
  public void updateTreeAndMarks(final Program program)
  {
    getRootNode().addProgram(program);
    getRootNode().update();
    if (mSettings.getMarkPrograms())
    {
      program.mark(this);
      program.validateMarking();
    }
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#handleTvDataAdded(ChannelDayProgram)
   */
  @Override
  public void handleTvDataAdded(final ChannelDayProgram newProg)
  {
    //this method is called when a program was added. it is also called when
    //a program was changed (ie deleted + added again). everytime a program
    //was added, check if it has a omdb link. if so, mark it and remember that
    //we marked the program.
    Iterator<Program> programIterator = newProg.getPrograms();
    Program program;
    while (programIterator.hasNext())
    {
      program = programIterator.next();
      //if the program is in vgmedia and has an omdb-link, mark it and remember that we marked it
      if (isProgramAllowed(program) && program.getTextField(ProgramFieldType.URL_TYPE) != null && !"".equals(program.getTextField(ProgramFieldType.URL_TYPE)))
      {
        if (mSettings.getMarkPrograms())
        {
          program.mark(this);
        }
        mLinkedPrograms.add(new ProgramId(program.getDate(), program.getID()));
      }
    }
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#handleTvDataDeleted(devplugin.ChannelDayProgram)
   */
  @Override
  public void handleTvDataDeleted(final ChannelDayProgram oldProg)
  {
    if (oldProg == null)
    {
      return;
    }
    //this is also called when a program was changed, ie deleted + added.
    //remove the program from the list of marked programs. if its changed,
    //handleTvDataAdded will add it again (if its linked to omdb).
    //CAUTION: this mehtod will NOT be called, if programs are removed by
    //the tvb at startup.
    Iterator<Program> programIterator = oldProg.getPrograms();
    Program program;
    ProgramId programId = new ProgramId(); //reusable (needed to delete the program)
    while (programIterator.hasNext())
    {
      program = programIterator.next();
      //if the program was in vgmedia and it was linked to omdb, remove it from the linked programs
      if (isProgramAllowed(program) && program.getTextField(ProgramFieldType.URL_TYPE) != null && !"".equals(program.getTextField(ProgramFieldType.URL_TYPE)))
      {
        program.unmark(this);
        mLinkedPrograms.remove(programId.setValues(program.getDate(), program.getID()));
      }
    }
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getRootNode()
   */
  @Override
  public PluginTreeNode getRootNode()
  {
    //lazy loading of the serialized root node
    if (mRootNode == null)
    {
      mRootNode = new PluginTreeNode(this, false);
      loadTree();
    }
    return mRootNode;
  }


  /**
   * saves the tree to a file. this is the same as Plugin.storeRootNode. it
   * was copied because storeRootNode is protected and cant be called.
   * TODO use Plugin.storeRootNode after 3.0 was released
   */
  private void storeTree()
  {
    File file = new File(Settings.getUserSettingsDirName(), getId() + ".node");
    if (getRootNode() == null || getRootNode().isEmpty())
    {
      file.delete();
      return;
    }
    try
    {
      ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
      getRootNode().store(out);
      out.close();
    }
    catch (final IOException e)
    {
      util.exc.ErrorHandler.handle(util.ui.Localizer.getLocalizerFor(Plugin.class).msg("error.couldNotWriteFile", "Storing file '{0}' failed.", file.getAbsolutePath()), e);
    }
  }


  /**
   * loads the tree from a file. this is the same as Plugin.loadRootNode. it
   * was copied for backward compatibility.
   * TODO use Plugin.loadRootNode after 3.0 was released
   */
  private void loadTree()
  {
    File file = new File(Settings.getUserSettingsDirName(), getId() + ".node");
    try
    {
      if (file.canRead())
      {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        mRootNode.load(in);
        in.close();
      }
    }
    catch (final IOException e)
    {
      util.exc.ErrorHandler.handle(LOCALIZER.msg("error.couldNotReadFile", "Reading file '{0}' failed.", file.getAbsolutePath()), e);
    }
  /*  catch (final ClassNotFoundException e)
    {
      util.exc.ErrorHandler.handle(LOCALIZER.msg("error.couldNotReadFile", "Reading file '{0}' failed.", file.getAbsolutePath()), e);
    }*/
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#writeData(java.io.ObjectOutputStream)
   */
  @Override
  public void writeData(final ObjectOutputStream out) throws IOException
  {
    storeTree();
    //write version (for future compatibility issues)
    out.writeInt(1);
    //save the linked programs so we can mark them on startup
    out.writeObject(mLinkedPrograms);
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#readData(java.io.ObjectInputStream)
   */
  @Override
  public void readData(final ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    try
    {
      //read version (for future compatibility issues)
      int version = in.readInt();
      if (version == 1)
      {
        //load the linked programs and mark them
        mLinkedPrograms = (ArrayList<ProgramId>) in.readObject();
        // do not remove invalid programs here, but in handleStartFinished (for performance)
      }
    }
    catch (final EOFException e)
    {
      //thrown if the file for the data was not found. we can ignore that
      //cause on exit the tvb will create the file.
    }
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#loadSettings(java.util.Properties)
   */
  @Override
  public void loadSettings(final Properties properties)
  {
    mSettings = new WirSchauenSettings(properties);
  }

  /**
   * (un)marks programs which are (not) in the plugin tree or in mLinkedPrograms.
   *
   * @param markPrograms true to mark the programs, false to unmark them
   */
  void updateMarkings(final boolean markPrograms) {
    changeMarkingOfLinkedPrograms(markPrograms);
    changeMarkingOfProgramsInTree(markPrograms);
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#storeSettings()
   */
  @Override
  public Properties storeSettings()
  {
    return mSettings.storeSettings();
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getAvailableFilterComponentClasses()
   */
  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses()
  {
    return new Class[] {WirSchauenFilterComponent.class};
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getAvailableFilter()
   */
  @Override
  public PluginsProgramFilter[] getAvailableFilter()
  {
    if (mFilter == null)
    {
      //map filter to filter component
      mComponent = new WirSchauenFilterComponent();
      mFilter = new PluginsProgramFilter(this) {
        @Override
        public String getSubName() {
          return mComponent.getUserPresentableClassName();
        }

        public boolean accept(final Program program) {
          return mComponent.accept(program);
        }
      };
    }
    return new PluginsProgramFilter[] {mFilter};
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getProgramTableIcons(devplugin.Program)
   */
  @Override
  public Icon[] getProgramTableIcons(final Program program)
  {
    // show the icon for a missing description
    if (isProgramAllowed(program)
        && (program.getShortInfo() == null
        || (program.getShortInfo().indexOf("keine Beschreibung") != -1
        && program.getShortInfo().indexOf("WirSchauen") != -1)))
    {
      return new Icon[] {getMissingDescriptionIcon()};
    }
    if (program == getPluginManager().getExampleProgram())
    {
      return new Icon[] {getMissingDescriptionIcon()};
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getProgramTableIconText()
   */
  @Override
  public String getProgramTableIconText()
  {
    return LOCALIZER.msg("icon", "WirSchauen: Description for this program is missing");
  }


  /**
   * {@inheritDoc}
   * @see devplugin.Plugin#getSettingsTab()
   */
  @Override
  public SettingsTab getSettingsTab()
  {
    return new WirSchauenSettingsTab(mSettings);
  }


  /**
   * checks if a program is allowed to be processed by wirschauen. the method
   * checks, if the programs channel is in the vg media. if so, the program is
   * allowed. im not sure if this restriction makes sense anymore.
   *
   * @param program the program to be checked
   * @return true, if the program is allowed to be processed by wirschauen
   */
  private static boolean isProgramAllowed(final Program program)
  {
    if (getPluginManager().getExampleProgram().equals(program)) {
      return true;
    }
    String name = "";

    if (program.getChannel().getDataServiceProxy() != null)
    {
      name = program.getChannel().getDataServiceProxy().getId() + ':';
    }

    name += program.getChannel().getId();

    return mAllowedChannels.contains(name);
  }


  /**
   * (un)marks a program.
   *
   * @param program the program to (un)mark
   * @param mark true if the program is to be marked, false otherwise
   */
  private void changeMarkingOfProgram(final Program program, final boolean mark)
  {
    if (mark)
    {
      program.mark(this);
    }
    else
    {
      program.unmark(this);
    }
  }


  /**
   * this walks through all the linkedPrograms (mLinkedPrograms) and (un)marks them.
   *
   * @param mark true to mark the programs, false otherwise
   */
  private void changeMarkingOfLinkedPrograms(final boolean mark)
  {
    for (ProgramId programId : mLinkedPrograms)
    {
      Program program = getPluginManager().getProgram(programId.getDate(), programId.getId());
      if (program != null)
      {
        changeMarkingOfProgram(program, mark);
      }
    }
  }


  /**
   * this walks through plugin tree and (un)marks all programs.
   *
   * @param mark true to mark the programs, false otherwise
   */
  private void changeMarkingOfProgramsInTree(final boolean mark)
  {
    for (Program program : getRootNode().getPrograms())
    {
      changeMarkingOfProgram(program, mark);
    }
  }


  /**
   * this method is called after the program table was drawn. we postpone some
   * cleanup work (ie removing programs from the 'marked' list that are no longer
   * available and mark linked programs) until now so the program table is displayed
   * earlier.
   *
   * @see devplugin.Plugin#handleTvBrowserStartFinished()
   */
  @Override
  public void handleTvBrowserStartFinished() {
    //remove programs from the list which are no longer available. crawl backwards
    //through the list so removed items will not interfere.
    for (int i = mLinkedPrograms.size() - 1; i >= 0; i--)
    {
      ProgramId programId = mLinkedPrograms.get(i);
      if (programId == null || getPluginManager().getProgram(programId.getDate(), programId.getId()) == null)
      {
        mLinkedPrograms.remove(i);
      }
    }
    // mark the linked programs. do this here instead of directly after loading settings for better startup performance
    if (mSettings.getMarkPrograms()) {
      updateMarkings(true);
    }
    //FIXME this is a workaround. the plugin tree is empty after startup of the tvb!
    getRootNode().update();
  }


  /**
   * this walks through the linked programs and updates the markings.
   * call this method, if the mark prio (i.e. color) for linked
   * programs changes.
   */
  void updateMarkingOfLinkedPrograms()
  {
    for (ProgramId programId : mLinkedPrograms)
    {
      Program program = getPluginManager().getProgram(programId.getDate(), programId.getId());

      if (program != null) {
        program.validateMarking();
      }
    }
  }


  /**
   * this walks through plugin tree and updates the markings.
   * call this method, if the mark prio (i.e. color) for linked
   * programs (linked by the user himself) changes.
   */
  void updateMarkingOfProgramsInTree()
  {
    for (Program program : getRootNode().getPrograms())
    {
      program.validateMarking();
    }
  }

  WirSchauenSettings getSettings() {
    return mSettings;
  }
}

