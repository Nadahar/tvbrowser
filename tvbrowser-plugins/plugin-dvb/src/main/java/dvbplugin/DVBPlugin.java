/*
 * DVBPlugin.java
 * Copyright (C) 2006 Probum
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */

package dvbplugin;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import util.misc.OperatingSystem;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import dvbplugin.dvbviewer.ProcessHandler;

/**
 * @author Probum
 */
public class DVBPlugin extends devplugin.Plugin {
  /** field <code>PROGRAMRECEIVE_REMOVE</code> */
  private static final String PROGRAMRECEIVE_REMOVE = "REMOVE";

  /** field <code>PROGRAMRECEIVE_ADD</code> */
  private static final String PROGRAMRECEIVE_ADD = "ADD";

  private static final Version VERSION = new Version(2, 02, 2);

  /** this plugin inherently only works on windows */
  private static final boolean isWindows = OperatingSystem.isWindows();

  /** the icon for the fast remove action */
  private static final String DVBPLUGIN_FASTREM_SMALLICON = "/dvbplugin/fastRemIcon16.gif";

  /** the icon for the fast add action */
  private static final String DVBPLUGIN_FASTADD_SMALLICON = "/dvbplugin/fastIcon16.gif";

  /** the large icon for the plugin */
  private static final String DVBPLUGIN_BIGICON = "/dvbplugin/icon24.png";

  /** the small icon for the plugin */
  private static final String DVBPLUGIN_SMALLICON = "/dvbplugin/icon16.png";

  /** Translator */
  protected static final Localizer localizer = Localizer.getLocalizerFor(DVBPlugin.class);

  /** the logger */
  private static final Logger logger = Logger.getLogger(DVBPlugin.class.getName());

  /** the parent action of all context menu actions */
  private ContextAction contextAction;

  /** the action to change/activate a channel in DVBViewer */
  private SwitchAction switchAction;

  /** the action to fast add a program to the list of scheduled recordings */
  private FastAddAction fastAddAction;

  /** the action to fast remove a program from the list of scheduled recordings */
  private FastRemoveAction fastRemoveAction;

  /** the action for showing the list of scheduled recordings */
  private DialogAction dialogAction;

  /** the main menu action */
  private ActionMenu mainMenuAction;

  /** contains the receive targets for programs sent by another plugin */
  private ProgramReceiveTarget[] receiveTargets;

  /** a cache for the plugin info */
  private PluginInfo pluginInfo;

  /**
   * singleton
   */
  private static DVBPlugin instance;



  /**
   * Creates a new instance of DVBPlugin
   */
  public DVBPlugin() {
    instance = this;
  }


  /**
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    if (null == pluginInfo) {
      // plugin name
      String name = "DVBViewer Plugin";

      // plugin description
      String desc = localizer.msg("desc", "Manage DVBViewer Pro record list. Allow start and" +
                                          " switching direct from context menu of a program.");

      // plugin author(s)
      String author = localizer.msg("author", "Ullrich Poll\u00E4hne, Tobias B\u00FCrner");

      pluginInfo = new PluginInfo(DVBPlugin.class, name, desc, author);
    }

    return pluginInfo;
  }

  public static Version getVersion() {
      return VERSION;
  }


  /**
   * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
   */
  public ActionMenu getContextMenuActions(Program program) {
    if (!isWindows) {
        return null;
    }

    logger.finer("getting context menu for program " + program.toString());

    boolean valid = Settings.getSettings().getChannelByTVBrowserName(program.getChannel().getName()).isValid();
    if (!valid)  {
      // not a known channel so check for the example program (used to configure the context menu)
      Program example = getPluginManager().getExampleProgram();

      if (example != program) { // <-- the example is created only once so this is not a mistake
        if (null != dialogAction) {
          // clear the previous program
          dialogAction.setProgram(null);
        }

        // no, not the example so do not show a context menu entry
        return null;
      }
    }

    // if necessary initialize the actions
    initActions();

    // if necessary create the menu actions
    return createContextMenuActions(program);
  }


  /**
   * @see devplugin.Plugin#getButtonAction()
   */
  public ActionMenu getButtonAction() {
    if (!isWindows) { return null; }

    if (null == mainMenuAction) {
      initActions();
      mainMenuAction = new ActionMenu(dialogAction);
    }

    return mainMenuAction;
  }


  /**
   * @see devplugin.Plugin#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    Settings set = Settings.getSettings();
    boolean pathIsSet = set.loadSettings(settings);

    Marker marker = new Marker();
    set.setMarker(marker);

    if (pathIsSet) {
      if (set.isMarkRecordings()) {
        marker.mark();
      }
    } else if (set.isMarkRecordings() && 0 != set.getChannelCount()) {
      // there are channels but we do not have a DVBViewer path, this is wrong
      HelperClass.error(localizer.msg("err_missing_DVBPath", "Missing DVBViewer path!"));
    }
  }


  /**
   * @see devplugin.Plugin#storeSettings()
   */
  public Properties storeSettings() {
    return Settings.getSettings().storeSettings();
  }


  /**
   * @see devplugin.Plugin#getMarkIconName()
   */
  public String getMarkIconName() {
    return DVBPLUGIN_SMALLICON;
  }


  /**
   * @see devplugin.Plugin#getSettingsTab()
   */
  public SettingsTab getSettingsTab() {
    DVBPluginSettingsTab set = new DVBPluginSettingsTab();
    return set;
  }


  /**
   * @see devplugin.Plugin#canReceiveProgramsWithTarget()
   */
  @Override
  public boolean canReceiveProgramsWithTarget() {
      return true;
  }


  /**
   * @see devplugin.Plugin#receivePrograms(devplugin.Program[], devplugin.ProgramReceiveTarget)
   */
  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
      if (receiveTarget == null || receiveTarget.getTargetId() == null) {
          return false;
      }

      String id = receiveTarget.getTargetId();

      if (null == programArr || 0 == programArr.length) {
        logger.warning("Received no programs from another plugin");
        return false;
      }

      boolean result = false;
//      PluginTreeNode node = getRootNode(); // disabled until ui refactoring
      for (Program program : programArr)
      {
        if (PROGRAMRECEIVE_ADD.equals(id)) {
          logger.finer("Adding program " + program);
          FastAddRemove.add(program);
//          node.addProgram(program); // disabled until ui refactoring
          result = true;
        } else if (PROGRAMRECEIVE_REMOVE.equals(id)) {
          logger.finer("Removing program " + program);
          FastAddRemove.remove(program);
//          node.removeProgram(program); // disabled until ui refactoring
          result = true;
        }
//        node.update();// disabled until ui refactoring
      }

      return result;
  }


  /**
   * @see devplugin.Plugin#getProgramReceiveTargets()
   */
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    logger.finer("DVBPlugin was asked for program receive targets");
    if (null == receiveTargets) {
      receiveTargets = new ProgramReceiveTarget[2];
      receiveTargets[0] = new ProgramReceiveTarget(this, Localizer.getLocalization(Localizer.I18N_ADD), PROGRAMRECEIVE_ADD);
      receiveTargets[1] = new ProgramReceiveTarget(this, Localizer.getLocalization(Localizer.I18N_DELETE), PROGRAMRECEIVE_REMOVE);
    }

     return receiveTargets;
  }


  /**
   * @see devplugin.Plugin#canUseProgramTree()
   */
  public boolean canUseProgramTree() {
//    return true; // disabled until ui refactoring
    return false;
  }


  private ActionMenu createContextMenuActions(Program current) {
    logger.finer("creating context menu");

    // check which items will be in the context menu (add/remove)
    boolean remove = false;
    String id = getId();

    for (devplugin.Marker element : current.getMarkerArr()) {
      if (element.getId().equals(id)) {
        remove = true;
        break;
      }
    }

    int actIndex = 0;
    AbstractAction[] subActions = new AbstractAction[3];
    if(remove) {
      subActions[actIndex++] = fastRemoveAction;
    } else {
      subActions[actIndex++] = fastAddAction;
    }
    subActions[actIndex++] = dialogAction;
    subActions[actIndex] = switchAction;

    // set the current program
    switchAction.setProgram(current);
    fastRemoveAction.setProgram(current);
    fastAddAction.setProgram(current);
    dialogAction.setProgram(current);

    // return the action menu
    return new ActionMenu(contextAction, subActions);
  }


  /**
   * Gets the parent frame.
   * <p>
   * The parent frame may be used for showing dialogs.
   *
   * @return The parent frame.
   */
  final Frame getParent() {
    // we need to do it this way since Plugin#getParentFrame() is protected
    return getParentFrame();
  }


  /**
   * Creates all menu and toolbar actions
   */
  private final void initActions()
  {
    if (null != contextAction) { return; }

    logger.fine("Creating menu actions");

    ImageIcon smallIcon = createImageIcon(DVBPLUGIN_SMALLICON);
    // the main item in the context menu
    contextAction = new ContextAction(smallIcon);

    // the action to show the dialog for scheduled recordings
    dialogAction = new DialogAction(this, createImageIcon(DVBPLUGIN_BIGICON), smallIcon);

    // the action to show a channel in DVBViewer
    switchAction = new SwitchAction();

    // the action to add a program to the list of scheduled recordings
    fastAddAction = new FastAddAction(this, createImageIcon(DVBPLUGIN_FASTADD_SMALLICON));

    // the action to remove a program from the list of scheduled recordings
    fastRemoveAction = new FastRemoveAction(this, createImageIcon(DVBPLUGIN_FASTREM_SMALLICON));
  }



  /**
   * ContextAction is a dummy action class to get a parent menu item for a sub menu
   */
  private final static class ContextAction extends AbstractAction {
    /**
     * Initialize the action
     *
     * @param largeIcon
     * @param smallIcon
     */
    public ContextAction(ImageIcon smallIcon) {
      // name of the action, shown in menu and symbol bar
      putValue(Action.NAME, "DVBViewer");
      // add a tooltip to the action
      putValue(Action.SHORT_DESCRIPTION,
              localizer.msg("action_context_tooltip", "Actions for viewing and recording with DVBViewer"));
      // small icon for menu (should be 16x16)
      putValue(Action.SMALL_ICON, smallIcon);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
      // this is only a dummy to get a sub menu
    }
  }


  /**
   * DialogAction is an action for showing the list of scheduled recordings.
   */
  private final static class DialogAction extends AbstractAction implements ComponentListener {
    transient DVBPlugin plugin;
    String name;
    transient Program program;
    transient ImageIcon smallIcon;

    /**
     * Initialize the action
     * @param aPlugin a reference to the DVBPlugin instance
     * @param largeIcon the large icon for this action (toolbar)
     * @param aSmallIcon the small icon for this action (menu)
     */
    public DialogAction(DVBPlugin aPlugin, ImageIcon largeIcon, ImageIcon aSmallIcon) {
      plugin = aPlugin;
      smallIcon = aSmallIcon;
      name = localizer.msg("action_scheduled", "Scheduled recordings...");

      // name of the action, shown in menu and symbol bar
      putValue(Action.NAME, name);
      // add a tooltip to the action
      putValue(Action.SHORT_DESCRIPTION,
              localizer.msg("action_scheduled_tooltip", "Opens the list of scheduled recordings"));
      // small icon for menu (should be 16x16)
      putValue(Action.SMALL_ICON, smallIcon);
      // large icon for symbol bar (should be 24x24)
      putValue(BIG_ICON, largeIcon);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
      JDialog dlg = util.ui.UiUtilities.createDialog(plugin.getParent(), true);
      dlg.setTitle(localizer.msg("scheduled_title", "DVBViewer Plugin scheduled recordings"));

      Program current = null;
      // check if this is main or context menu
      if (name.equals(evt.getActionCommand())) {
        // name equals action command on context menu activation
        current = program;
      }

      // fill in the panel
      dlg.add(new RecordingsPanel(current));
      dlg.pack();

      // position the frame
      Settings set = Settings.getSettings();
      dlg.setLocation(set.getLastXofRecordingsPanel(), set.getLastYofRecordingsPanel());
      dlg.addComponentListener(this);

      dlg.setVisible(true);
    }

    /**
     * Store the current position of the dialog
     *
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent e) {
      Object obj = e.getSource();
      if (obj instanceof JDialog) {
        Settings set = Settings.getSettings();
        int lastx = set.getLastXofRecordingsPanel();
        int lasty = set.getLastYofRecordingsPanel();
        Point newxy = ((JDialog)obj).getLocation();
        if (lastx != newxy.x) {
          set.setLastXofRecordingsPanel(newxy.x);
        }
        if (lasty != newxy.y) {
          set.setLastYofRecordingsPanel(newxy.y);
        }
      }
    }

    public void componentHidden(ComponentEvent e) { /* unused */ }

    public void componentResized(ComponentEvent e) { /* unused */ }

    public void componentShown(ComponentEvent e) { /* unused */ }

    /**
     * Set the currently selected program
     * @param aProgram currently selected program
     */
    void setProgram(Program aProgram) {
      program = aProgram;
    }
  }


  /**
   * SwitchAction is an action to change/activate a channel in DVBViewer
   */
  private final static class SwitchAction extends AbstractAction {
    transient Program program;

    /**
     * Initialize the action
     */
    public SwitchAction() {
      putValue(Action.NAME, localizer.msg("action_switch", "Switch"));
      putValue(Action.SHORT_DESCRIPTION,
              localizer.msg("action_switch_tooltip", "Shows this channel in DVBViewer"));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
      Settings set = Settings.getSettings();
      if (set.getChannelCount() == 0) {
        HelperClass.error(localizer.msg("error_switchchannel", "Settings unavailable!"));
      } else if (null != program){
        String name = program.getChannel().getName();
        ProcessHandler.runDvbViewer(set, set.getChannelByTVBrowserName(name));
      }
    }

    /**
     * Set the currently selected program
     * @param aProgram currently selected program
     */
    void setProgram(Program aProgram) {
      program = aProgram;
      if (null != program) {
        Icon icon = program.getChannel().getIcon();

        if(icon != null) {
          if (icon.getIconWidth() > 16) {
            icon = UiUtilities.scaleIcon(icon, 16);
          }
          putValue(Action.SMALL_ICON, icon);
        }
      }
    }
  }


  /**
   * FastAddAction is an action to fast add a program to the list of scheduled recordings
   */
  private final static class FastAddAction extends AbstractAction {
    transient Plugin plugin;
    transient Program program;

    /**
     * Initialize the action
     * @param aPlugin a reference to the plugin instance
     * @param smallIcon the small icon for this action (menu)
     */
    public FastAddAction(Plugin aPlugin, ImageIcon smallIcon) {
      plugin = aPlugin;
      putValue(Action.NAME, localizer.msg("action_add", "Add"));
      putValue(Action.SMALL_ICON, smallIcon);
      putValue(Action.SHORT_DESCRIPTION,
              localizer.msg("action_add_tooltip", "Adds this program to the list of scheduled recordings"));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
      Settings set = Settings.getSettings();
      if (set.getChannelCount() == 0) {
        HelperClass.error(localizer.msg("error_missing_settings",
                "Settings unavailable!\nPlease do the settings first!"));
        return;
      }

      if (null != program) {
        FastAddRemove.add(program);
      }

    }

    /**
     * Set the currently selected program
     * @param aProgram currently selected program
     */
    void setProgram(Program aProgram) {
      program = aProgram;
    }
  }


  /**
   * FastRemoveAction is an action to fast remove a program from the list of scheduled recordings
   */
  private final static class FastRemoveAction extends AbstractAction {
    transient Plugin plugin;
    transient Program program;

    /**
     * Initialize the action
     * @param aPlugin a reference to the plugin instance
     * @param smallIcon the small icon for this action (menu)
     */
    public FastRemoveAction(Plugin aPlugin, ImageIcon smallIcon) {
      plugin = aPlugin;
      putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_DELETE));
      putValue(Action.SMALL_ICON, smallIcon);
      putValue(Action.SHORT_DESCRIPTION, localizer.msg("action_remove_tooltip",
                "Removes this program from the list of scheduled recordings"));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
      Settings set = Settings.getSettings();
      if (set.getChannelCount() == 0) {
        HelperClass.error(localizer.msg("error_missing_settings",
                "Settings unavailable!\nPlease do the settings first!"));
        return;
      }

      if (null != plugin && null != program && set.isMarkRecordings()) {
        FastAddRemove.remove(program);
        program.unmark(plugin);
      }
    }


    /**
     * Set the currently selected program
     * @param aProgram currently selected program
     */
    void setProgram(Program aProgram) {
      program = aProgram;
    }
  }

  public Icon getSmallIcon() {
    return ImageUtilities.createImageIconFromJar(DVBPlugin.DVBPLUGIN_SMALLICON, getClass());
  }


  public static DVBPlugin getInstance() {
    return instance;
  }
}
