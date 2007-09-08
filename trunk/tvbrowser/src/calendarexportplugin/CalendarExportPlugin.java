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
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
     * The Default-Parameters
     */
    //public static final String DEFAULT_PARAMETER = "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n";

    
    private static LocalPluginProgramFormating DEFAULT_CONFIG = new LocalPluginProgramFormating("calendarDefault", mLocalizer.msg("defaultName","CalendarExportPlugin - Default"),"{channel_name} - {title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
    
    private AbstractPluginProgramFormating[] mConfigs = null;
    private LocalPluginProgramFormating[] mLocalFormatings = null;
    
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
      mLocalFormatings = new LocalPluginProgramFormating[1];
      mLocalFormatings[0] = DEFAULT_CONFIG;        
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
        String helpUrl = mLocalizer.msg("helpUrl", "http://enwiki.tvbrowser.org/index.php/Calendar_Export");
        
        return new PluginInfo(name, desc, author, helpUrl, new Version(0, 8));
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

        if(mConfigs == null || mConfigs.length <= 1) {
          if(mConfigs == null || mConfigs.length == 0) {
            mConfigs = new AbstractPluginProgramFormating[1];
            mConfigs[0] = DEFAULT_CONFIG;
          }
          
          Action[] actions = new Action[activeExporter.length];

          int max = activeExporter.length;

          for (int i = 0; i < max; i++) {
            final ExporterIf export = activeExporter[i];
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    new Thread(new Runnable() {
                        public void run() {
                            Program[] programArr = {program};
                            export.exportPrograms(programArr, mSettings,mConfigs[0]);
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
        else {
          ActionMenu[] exporters = new ActionMenu[activeExporter.length];
          
          for(int i = 0; i < exporters.length; i++) {
            Action[] actions = new Action[mConfigs.length];            
            
            for(int j = 0; j < actions.length; j++) {
              final ExporterIf export = activeExporter[i];
              final int count = j;
              
              actions[j] = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                  new Thread(new Runnable() {
                    public void run() {
                      Program[] programArr = {program};
                      export.exportPrograms(programArr, mSettings, mConfigs[count]);
                    }
                  }).start();
                }
              };
              actions[j].putValue(Action.NAME, mConfigs[j].getName());
            }
            
            ContextMenuAction context = new ContextMenuAction(activeExporter[i].getName());
            context.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));
            
            exporters[i] = new ActionMenu(context, actions);
          }
          
          return new ActionMenu(mainaction, exporters);
        }
    }

    @Override
    public boolean canReceiveProgramsWithTarget() {
        return true;
    }

    @Override
    public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
        if(receiveTarget == null)
          return false;
      
        ExporterIf[] exporters = mExporterFactory.getActiveExporters();
        
        if(mConfigs == null || mConfigs.length < 1) {
          mConfigs = new AbstractPluginProgramFormating[1];
          mConfigs[0] = DEFAULT_CONFIG;
        }

        for (ExporterIf export:exporters) {
          for(AbstractPluginProgramFormating formating : mConfigs) {
            if (receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this,export.getClass().getName() + ";;;" + formating.getId())) {
              export.exportPrograms(programArr, mSettings, formating);
              return true;              
            }
          }
        }

        return false;
    }

    @Override
    public ProgramReceiveTarget[] getProgramReceiveTargets() {
        ExporterIf[] exporters = mExporterFactory.getActiveExporters();

        ArrayList<ProgramReceiveTarget> targets = new ArrayList<ProgramReceiveTarget>();

        if(mConfigs == null || mConfigs.length < 1) {
          mConfigs = new AbstractPluginProgramFormating[1];
          mConfigs[0] = DEFAULT_CONFIG;
        }
        
        for (ExporterIf exporter : exporters) {
          for(AbstractPluginProgramFormating formating : mConfigs)
            targets.add(new ProgramReceiveTarget(this, exporter.getName() + (mConfigs.length > 1 ? " - " + formating.getName() : ""), exporter.getClass().getName() + ";;;" + formating.getId()));
        }

        return targets.toArray(new ProgramReceiveTarget[targets.size()]);
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
        
        if(settings != null && settings.containsKey("paramToUse")) {
          mConfigs = new AbstractPluginProgramFormating[1];
          mConfigs[0] = new LocalPluginProgramFormating(mLocalizer.msg("defaultName","Calendar Export - Default"),"{channel_name} - {title}",settings.getProperty("paramToUse"),"UTF-8");
          mLocalFormatings = new LocalPluginProgramFormating[1];
          mLocalFormatings[0] = (LocalPluginProgramFormating)mConfigs[0];
          DEFAULT_CONFIG = mLocalFormatings[0];
          
          settings.remove("paramToUse");
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

    /**
     * @return get best Parent-Frame for Dialogs
     */
    public Window getBestParentFrame() {
        return UiUtilities.getBestDialogParent(getParentFrame());
  }
    
    public void writeData(ObjectOutputStream out) throws IOException {
      out.writeInt(3); // write version
      
      if(mConfigs != null) {
        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
        
        for(AbstractPluginProgramFormating config : mConfigs)
          if(config != null)
            list.add(config);
        
        out.writeInt(list.size());
        
        for(AbstractPluginProgramFormating config : list)
          config.writeData(out);
      }
      else
        out.writeInt(0);
      
      if(mLocalFormatings != null) {
        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
        
        for(AbstractPluginProgramFormating config : mLocalFormatings)
          if(config != null)
            list.add(config);
        
        out.writeInt(list.size());
        
        for(AbstractPluginProgramFormating config : list)
          config.writeData(out);      
      }
      
    }
    
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
      try {
        int version = in.readInt();
      
        if(version == 3) {
          int n = in.readInt();
      
          ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
          
          for(int i = 0; i < n; i++) {
            AbstractPluginProgramFormating value = AbstractPluginProgramFormating.readData(in);
        
            if(value != null) { 
              if(value.equals(DEFAULT_CONFIG))
                DEFAULT_CONFIG = (LocalPluginProgramFormating)value;
          
              list.add(value);
            }
          }
      
          mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);
      
          mLocalFormatings = new LocalPluginProgramFormating[in.readInt()];
      
          for(int i = 0; i < mLocalFormatings.length; i++) {
            LocalPluginProgramFormating value = (LocalPluginProgramFormating)LocalPluginProgramFormating.readData(in);
            LocalPluginProgramFormating loadedInstance = getInstanceOfFormatingFromSelected(value);
        
            mLocalFormatings[i] = loadedInstance == null ? value : loadedInstance;
          }
        }
      }catch(Exception e) {}
    }
    
    private LocalPluginProgramFormating getInstanceOfFormatingFromSelected(LocalPluginProgramFormating value) {
      for(AbstractPluginProgramFormating config : mConfigs)
        if(config.equals(value))
          return (LocalPluginProgramFormating)config;
      
      return null;
    }
    
    protected static LocalPluginProgramFormating getDefaultFormating() {    
      return new LocalPluginProgramFormating(mLocalizer.msg("defaultName","CliboardPlugin - Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
    }

    protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
      return mLocalFormatings;
    }
    
    protected void setAvailableLocalPluginProgramFormatings(LocalPluginProgramFormating[] value) {
      if(value == null || value.length < 1)
        createDefaultAvailable();
      else
        mLocalFormatings = value;
    }

    protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormatings() {
      return mConfigs;
    }
    
    protected void setSelectedPluginProgramFormatings(AbstractPluginProgramFormating[] value) {
      if(value == null || value.length < 1)
        createDefaultConfig();
      else
        mConfigs = value;
    }
}