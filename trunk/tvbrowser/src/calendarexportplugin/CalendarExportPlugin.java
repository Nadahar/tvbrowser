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

import calendarexportplugin.exporter.ExporterFactory;
import calendarexportplugin.exporter.ExporterIf;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 * This Plugin exports the Calendar to a external Application or File
 *
 * @author bodo
 */
public class CalendarExportPlugin extends Plugin {
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
    public static final String PROP_CATEGORIE = "Categorie";

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
    public static final String PROP_PARAM = "paramToUse";

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
     * The Default-Parameters
     */
    public static final String DEFAULT_PARAMETER = "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n";

    /**
     * Instance of this Plugin
     */
    private static CalendarExportPlugin mInstance;

    /**
     * Settings
     */
    private Properties mSettings;

    /**
     * Factory for Export-Types
     */
    private ExporterFactory mExporterFactory;

    /**
     * Create Plugin
     */
    public CalendarExportPlugin() {
        mExporterFactory = new ExporterFactory();
        mInstance = this;
    }

    /*
    * (non-Javadoc)
    *
    * @see devplugin.Plugin#getInfo()
    */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "Calendar export");
        String desc = mLocalizer.msg("description",
                "Exports a Program as a vCal/iCal File. This File can easily imported in other Calendar Applications.");
        String author = "Bodo Tasche, Udo Weigelt";
        return new PluginInfo(name, desc, author, new Version(0, 3));
    }

    /*
    * (non-Javadoc)
    * @see devplugin.Plugin#getMarkIconFromTheme()
    */
    public ThemeIcon getMarkIconFromTheme() {
        return new ThemeIcon("apps", "office-calendar", 16);
    }

    /*
    * (non-Javadoc)
    *
    * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
    */
    public ActionMenu getContextMenuActions(final Program program) {
        ExporterIf[] activeExporter = mExporterFactory.getActiveExporters();

        if (activeExporter.length == 0) {
            return null;
        }

        Action mainaction = new devplugin.ContextMenuAction();
        mainaction.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Export to Calendar-File"));
        mainaction.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));

        Action[] actions = new Action[activeExporter.length];

        int max = activeExporter.length;

        for (int i = 0; i < max; i++) {
            final ExporterIf export = activeExporter[i];
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    new Thread(new Runnable() {
                        public void run() {
                            Program[] programArr = {program};
                            export.exportPrograms(programArr, mSettings);
                        }
                    }).start();
                }
            };

            StringBuilder name = new StringBuilder();

            if (max == 1) {
                name.append(mLocalizer.msg("contextMenuText", "Export to")).append(' ');
            }

            name.append(activeExporter[i].getName());

            action.putValue(Action.NAME, name.toString());
            action.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));

            actions[i] = action;
        }

        if (actions.length == 1) {
            return new ActionMenu(actions[0]);
        }

        return new ActionMenu(mainaction, actions);
    }

    @Override
    public boolean canReceiveProgramsWithTarget() {
        return true;
    }

    @Override
    public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
        ExporterIf[] exporters = mExporterFactory.getActiveExporters();

        for (ExporterIf export:exporters) {
            if (export.getClass().getName().equals(receiveTarget.getTargetId())) {
                export.exportPrograms(programArr, mSettings);
                return true;
            }
        }

        return false;
    }

    @Override
    public ProgramReceiveTarget[] getProgramReceiveTargets() {
        ExporterIf[] exporters = mExporterFactory.getActiveExporters();

        ProgramReceiveTarget[] targets = new ProgramReceiveTarget[exporters.length];

        for (int i =0;i<exporters.length;i++) {
            targets[i] = new ProgramReceiveTarget(this, exporters[i].getName(), exporters[i].getClass().getName());
        }

        return targets;
    }

    /**
     * Get Settings-Tab
     *
     * @return SettingsTab
     */
    public SettingsTab getSettingsTab() {
        return new CalendarSettingsTab(this, mSettings);
    }

    /**
     * Stores the Settings
     *
     * @return Settings
     */
    public Properties storeSettings() {
        return mSettings;
    }

    /**
     * Loads the Settings
     *
     * @param settings Settings for this Plugin
     */
    public void loadSettings(Properties settings) {
        if (settings == null) {
            settings = new Properties();
        }
        mSettings = settings;

        mExporterFactory.setListOfActiveExporters(mSettings.getProperty(PROP_ACTIVE_EXPORTER));
    }

    /**
     * Called by the host-application during start-up.
     *
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            int version = in.readInt();
        } catch (Exception e) {
        }
    }

    /**
     * Counterpart to loadData. Called when the application shuts down.
     *
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(2);
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

    /**
     * @return get best Parent-Frame for Dialogs
     */
    public Window getBestParentFrame() {
        return UiUtilities.getBestDialogParent(getParentFrame());
  }
}