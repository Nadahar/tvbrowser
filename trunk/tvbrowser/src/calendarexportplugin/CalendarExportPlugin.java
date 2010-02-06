/*
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
package calendarexportplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import calendarexportplugin.exporter.ExporterFactory;
import calendarexportplugin.exporter.ExporterIf;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin exports the Calendar to a external Application or File
 *
 * @author bodo
 */
public class CalendarExportPlugin extends Plugin {
  private static final Version mVersion = new Version(3,0);

  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CalendarExportPlugin.class);

  /**
   * If true, set length to 0 min
   */
  public static final String PROP_NULLTIME = "nulltime";

  /**
   * Category of Item
   */
  public static final String PROP_CATEGORY = "Categorie";

  /**
   * Show Time as Busy or Free - 0 = Busy, 1 = Free
   */
  public static final String PROP_SHOWTIME = "ShowTime";

  /**
   * Classification - 0 = Public, 1 = Private
   */
  public static final String PROP_CLASSIFICATION = "Classification";

  /**
   * Parameters for Text-Creation
   */
  //public static final String PROP_PARAM = "paramToUse";

  /**
   * Use Alarm ?
   */
  public static final String PROP_ALARM = "usealarm";

  /**
   * Minutes before ?
   */
  public static final String PROP_ALARMBEFORE = "alarmbefore";

  /**
   * List of active Exporters
   */
  public static final String PROP_ACTIVE_EXPORTER = "activeexporter";

  /**
   * Mark items if exported
   */
  public static final String PROP_MARK_ITEMS = "markitems";

  /**
   * The Default-Parameters
   */

  private static LocalPluginProgramFormating DEFAULT_CONFIG = new LocalPluginProgramFormating("calendarDefault", mLocalizer.msg("defaultName", "CalendarExportPlugin - Default"), "{channel_name} - {title}", "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n", "UTF-8");

  private AbstractPluginProgramFormating[] mConfigs = null;
  private LocalPluginProgramFormating[] mLocalFormattings = null;

  /**
   * Instance of this Plugin
   */
  private static CalendarExportPlugin mInstance;

  /**
   * Settings
   */
  private CalendarExportSettings mSettings;

  /**
   * Factory for Export-Types
   */
  private ExporterFactory mExporterFactory;

  private PluginInfo mPluginInfo;

  /**
   * The root node of this plugin
   */
  private PluginTreeNode mRootNode = new PluginTreeNode(this, true);
  /**
   * The map between tree nodes and the exporters
   */
  private Map<ExporterIf, PluginTreeNode> mTreeNodes = new HashMap<ExporterIf, PluginTreeNode>();

  /**
   * Create Plugin
   */
  public CalendarExportPlugin() {
    createDefaultConfig();
    createDefaultAvailable();
    mExporterFactory = new ExporterFactory();
    mInstance = this;
  }

  private void createDefaultConfig() {
    mConfigs = new AbstractPluginProgramFormating[1];
    mConfigs[0] = DEFAULT_CONFIG;
  }

  private void createDefaultAvailable() {
    mLocalFormattings = new LocalPluginProgramFormating[1];
    mLocalFormattings[0] = DEFAULT_CONFIG;
  }

  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Calendar export");
      String desc = mLocalizer.msg("description",
              "Exports a Program as a vCal/iCal File. This File can easily imported in other Calendar Applications.");
      String author = "Bodo Tasche, Udo Weigelt";

      mPluginInfo = new PluginInfo(CalendarExportPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "office-calendar", 16);
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    ExporterIf[] activeExporter = mExporterFactory.getActiveExporters();

    if (activeExporter.length == 0) {
      return null;
    }

    Action mainaction = new devplugin.ContextMenuAction();
    mainaction.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Export to Calendar-File"));
    mainaction.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));

    if (mConfigs == null || mConfigs.length <= 1) {
      if (mConfigs == null || mConfigs.length == 0) {
        mConfigs = new AbstractPluginProgramFormating[1];
        mConfigs[0] = DEFAULT_CONFIG;
      }

      Action[] actions = new Action[activeExporter.length];

      int max = activeExporter.length;

      for (int i = 0; i < max; i++) {
        final ExporterIf export = activeExporter[i];

        AbstractAction action = null;
        StringBuilder name = new StringBuilder();
        final PluginTreeNode node = getNodeForExporter(export);

        if(node.contains(program)) {
          action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              node.removeProgram(program);
              getRootNode().update();

              if(getRootNode().contains(program,true)) {
                program.mark(CalendarExportPlugin.getInstance());
              }
            }
          };

          name.append(mLocalizer.msg("contextMenuDeleteText","Remove marking for "));

          action.putValue(Action.SMALL_ICON, TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
        }
        else {
          action = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
              new Thread(new Runnable() {
                public void run() {
                  Program[] programArr = {program};
                  if (export.exportPrograms(programArr, mSettings, mConfigs[0])) {
                    markProgram(program, export);
                    getRootNode().update();
                  }
                }
              }, "Export to calendar").start();
            }
          };

          if (max == 1) {
            name.append(mLocalizer.msg("contextMenuText", "Export to")).append(' ');
          }

          action.putValue(Action.SMALL_ICON, getExporterIcon(export));
        }

        name.append(activeExporter[i].getName());
        action.putValue(Action.NAME, name.toString());

        actions[i] = action;
      }

      if (actions.length == 1) {
        return new ActionMenu(actions[0]);
      }

      return new ActionMenu(mainaction, actions);
    } else {
      ActionMenu[] exporters = new ActionMenu[activeExporter.length];

      for (int i = 0; i < exporters.length; i++) {
        Action[] actions = new Action[mConfigs.length];

        for (int j = 0; j < actions.length; j++) {
          final ExporterIf export = activeExporter[i];
          final int count = j;

          final PluginTreeNode node = getNodeForExporter(export);

          if(node.contains(program)) {
            actions[j] = new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                node.removeProgram(program);
                getRootNode().update();

                if(getRootNode().contains(program,true)) {
                  program.mark(CalendarExportPlugin.getInstance());
                }
              }
            };

            actions[j].putValue(Action.NAME, new StringBuilder(mLocalizer.msg("contextMenuDeleteText","Remove marking for ")).append(mConfigs[j].getName()).toString());
            actions[j].putValue(Action.SMALL_ICON, TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
          }
          else {
            actions[j] = new AbstractAction() {
              public void actionPerformed(final ActionEvent e) {
                new Thread(new Runnable() {
                  public void run() {
                    Program[] programArr = {program};
                    if (export.exportPrograms(programArr, mSettings, mConfigs[count])) {
                      markProgram(program, export);
                      getRootNode().update();
                    }
                  }
                }, "Export to calendar").start();
              }
            };
            actions[j].putValue(Action.NAME, mConfigs[j].getName());
          }
        }

        ContextMenuAction context = new ContextMenuAction(activeExporter[i].getName());
        context.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));

        exporters[i] = new ActionMenu(context, actions);
      }

      return new ActionMenu(mainaction, exporters);
    }
  }

  private ImageIcon getExporterIcon(final ExporterIf export) {
    String iconName = export.getIconName();
    if (iconName != null) {
      return new ImageIcon(getClass().getResource(
          "icons/16x16/apps/" + iconName));
    }
    return createImageIcon("apps", "office-calendar", 16);
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    if (receiveTarget == null) {
      return false;
    }

    ExporterIf[] exporters = mExporterFactory.getActiveExporters();

    if (mConfigs == null || mConfigs.length < 1) {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = DEFAULT_CONFIG;
    }

    for (ExporterIf export : exporters) {
      for (AbstractPluginProgramFormating formating : mConfigs) {
        if (receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, export.getClass().getName() + ";;;" + formating.getId())) {
          if (export.exportPrograms(programArr, mSettings, formating)) {
            for (Program program : programArr) {
              markProgram(program, export);
            }
            getRootNode().update();
          }
          return true;
        }
      }
    }

    return false;
  }

  private void markProgram(Program program, ExporterIf export) {
    if (mSettings.getMarkItems()) {
      PluginTreeNode node = getNodeForExporter(export);
      node.addProgram(program);
    }
  }

  private PluginTreeNode getNodeForExporter(ExporterIf export) {
    PluginTreeNode node = mTreeNodes.get(export);

    if (node == null) {
      node = new PluginTreeNode(export.getName());
      node.getMutableTreeNode().setIcon(getExporterIcon(export));

      createNodeActionForNode(node);

      getRootNode().add(node);
      mTreeNodes.put(export, node);
      getRootNode().update();
    }

    return node;
  }

  private void createNodeActionForNode(final PluginTreeNode node) {
    ContextMenuAction action = new ContextMenuAction(mLocalizer.msg("treeNodeDeleteAction",
    "Delete all markings"),TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));

    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Program[] programs = node.getPrograms();

        node.removeAllChildren();
        node.update();

        for(Program p : programs) {
          if(getRootNode().contains(p,true)) {
            p.mark(CalendarExportPlugin.getInstance());
          }
        }
      }
    });

    node.addAction(action);
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ExporterIf[] exporters = mExporterFactory.getActiveExporters();

    ArrayList<ProgramReceiveTarget> targets = new ArrayList<ProgramReceiveTarget>();

    if (mConfigs == null || mConfigs.length < 1) {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = DEFAULT_CONFIG;
    }

    for (ExporterIf exporter : exporters) {
      for (AbstractPluginProgramFormating formating : mConfigs) {
        targets.add(new ProgramReceiveTarget(this, exporter.getName() + (mConfigs.length > 1 ? " - " + formating.getName() : ""), exporter.getClass().getName() + ";;;" + formating.getId()));
      }
    }

    return targets.toArray(new ProgramReceiveTarget[targets.size()]);
  }

  /**
   * Get Settings-Tab
   *
   * @return SettingsTab
   */
  @Override
  public SettingsTab getSettingsTab() {
    return new CalendarSettingsTab(this, mSettings);
  }

  /**
   * Stores the Settings
   *
   * @return Settings
   */
  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  /**
   * Loads the Settings
   *
   * @param properties Settings for this Plugin
   */
  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new CalendarExportSettings(properties);

    mExporterFactory.setListOfActiveExporters(mSettings.getActiveExporters());

    if (properties.containsKey("paramToUse")) {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = new LocalPluginProgramFormating(mLocalizer.msg("defaultName", "Calendar Export - Default"), "{channel_name} - {title}", properties.getProperty("paramToUse"), "UTF-8");
      mLocalFormattings = new LocalPluginProgramFormating[1];
      mLocalFormattings[0] = (LocalPluginProgramFormating) mConfigs[0];
      DEFAULT_CONFIG = mLocalFormattings[0];

      properties.remove("paramToUse");
    }
  }

  /**
   * @return ExporterFactory
   */
  public ExporterFactory getExporterFactory() {
    return mExporterFactory;
  }

  /**
   * @return Instance of this Plugin
   */
  public static CalendarExportPlugin getInstance() {
    return mInstance;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  /**
   * @return get best Parent-Frame for Dialogs
   */
  public Window getBestParentFrame() {
    return UiUtilities.getBestDialogParent(getParentFrame());
  }

  @Override
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(4); // write version

    if (mConfigs != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for (AbstractPluginProgramFormating config : mConfigs) {
        if (config != null) {
          list.add(config);
        }
      }

      out.writeInt(list.size());

      for (AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    } else {
      out.writeInt(0);
    }

    if (mLocalFormattings != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for (AbstractPluginProgramFormating config : mLocalFormattings) {
        if (config != null) {
          list.add(config);
        }
      }

      out.writeInt(list.size());

      for (AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    }

    if (mSettings.getMarkItems()) {
      final Set<ExporterIf> exporters = mTreeNodes.keySet();
      out.writeInt(exporters.size());

      for (final ExporterIf exp : exporters) {
        out.writeObject(exp.getClass().getName());
        final PluginTreeNode node = mTreeNodes.get(exp);
        final Program[] nodePrograms = node.getPrograms();
        out.writeInt(nodePrograms.length);
        for (final Program p : nodePrograms) {
          if (p != null) {
            p.getDate().writeData((java.io.DataOutput) out);
            out.writeObject(p.getID());
          }
        }
      }
    } else {
      out.writeInt(0);
    }

  }

  @Override
  public void readData(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    try {
      int version = in.readInt();

      if (version >= 3) {
        int n = in.readInt();

        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

        for (int i = 0; i < n; i++) {
          AbstractPluginProgramFormating value = AbstractPluginProgramFormating.readData(in);

          if (value != null) {
            if (value.equals(DEFAULT_CONFIG)) {
              DEFAULT_CONFIG = (LocalPluginProgramFormating) value;
            }

            list.add(value);
          }
        }

        mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);

        mLocalFormattings = new LocalPluginProgramFormating[in.readInt()];

        for (int i = 0; i < mLocalFormattings.length; i++) {
          LocalPluginProgramFormating value = (LocalPluginProgramFormating) AbstractPluginProgramFormating.readData(in);
          LocalPluginProgramFormating loadedInstance = getInstanceOfFormattingFromSelected(value);

          mLocalFormattings[i] = loadedInstance == null ? value : loadedInstance;
        }

        if (version >= 4) {
          int treeNodeCount = in.readInt();
          ArrayList<PluginTreeNode> nodes = new ArrayList<PluginTreeNode>();
          for (int i = 0; i < treeNodeCount; i++) {
            final ExporterIf exporter = findExporter((String) in.readObject());
            PluginTreeNode node = null;
            if (exporter != null) {
              node = new PluginTreeNode(exporter.getName());
              node.getMutableTreeNode().setIcon(getExporterIcon(exporter));

              createNodeActionForNode(node);
              nodes.add(node);
              mTreeNodes.put(exporter, node);
            }

            int progCount = in.readInt();
            for (int v = 0; v < progCount; v++) {
              Date programDate = Date.readData(in);
              String progId = (String) in.readObject();
              if (node != null) {
                Program program = Plugin.getPluginManager().getProgram(programDate, progId);
                node.addProgram(program);
              }
            }
          }
          Collections.sort(nodes, new Comparator<PluginTreeNode>() {
            public int compare(final PluginTreeNode o1, final PluginTreeNode o2) {
              return ((String) o1.getUserObject())
                  .compareToIgnoreCase((String) o2.getUserObject());
            }
          });
          for (PluginTreeNode pluginTreeNode : nodes) {
            getRootNode().add(pluginTreeNode);
          }
        }

        getRootNode().update();
      }
    }
    catch (final Exception e) {
    }
  }

  private ExporterIf findExporter(String className) {
    for (final ExporterIf exp : mExporterFactory.getAllExporters()) {
      if (exp.getClass().getName().equals(className)) {
        return exp;
      }
    }
    return null;
  }

  private LocalPluginProgramFormating getInstanceOfFormattingFromSelected(LocalPluginProgramFormating value) {
    for (AbstractPluginProgramFormating config : mConfigs) {
      if (config.equals(value)) {
        return (LocalPluginProgramFormating) config;
      }
    }

    return null;
  }

  protected static LocalPluginProgramFormating getDefaultFormatting() {
    return new LocalPluginProgramFormating(mLocalizer.msg("defaultName", "ClipboardPlugin - Default"), "{title}", "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n", "UTF-8");
  }

  protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
    return mLocalFormattings;
  }

  protected void setAvailableLocalPluginProgramFormatings(LocalPluginProgramFormating[] value) {
    if (value == null || value.length < 1) {
      createDefaultAvailable();
    } else {
      mLocalFormattings = value;
    }
  }

  protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormattings() {
    return mConfigs;
  }

  protected void setSelectedPluginProgramFormattings(AbstractPluginProgramFormating[] value) {
    if (value == null || value.length < 1) {
      createDefaultConfig();
    } else {
      mConfigs = value;
    }
  }
}