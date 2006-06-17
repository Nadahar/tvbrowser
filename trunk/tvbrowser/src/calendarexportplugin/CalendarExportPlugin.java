/*
 * Created on 18.06.2004
 */
package calendarexportplugin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin exports vCal and iCal Files
 * 
 * @author bodo
 */
public class CalendarExportPlugin extends Plugin {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CalendarExportPlugin.class);

    /** If true, set length to 0 min */
    public static final String PROP_NULLTIME = "nulltime";
    /** Category of Item */
    public static final String PROP_CATEGORIE = "Categorie";
    /** Show Time as Busy or Free - 0 = Busy, 1 = Free */
    public static final String PROP_SHOWTIME = "ShowTime";
    /** Classification - 0 = Public, 1 = Private */
    public static final String PROP_CLASSIFICATION = "Classification";
    /** Parameters for Text-Creation */
    public static final String PROP_PARAM = "paramToUse";
    /** Use Alarm ? */
    public static final String PROP_ALARM = "usealarm";
    /** Minutes before ? */
    public static final String PROP_ALARMBEFORE = "alarmbefore";
    
    /** The Exporter to use */
    private CalendarExporter mExport = new CalendarExporter();

    /** Path for saving the File */
    private String mSavePath;
    
    /** Settings */
    private Properties mSettings;
    
    /** The Default-Parameters */
    public static final String DEFAULT_PARAMETER = "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n";
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "Calendar export");
        String desc = mLocalizer.msg("description",
                "Exports a Program as a vCal/iCal File. This File can easily imported in other Calendar Applications.");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(0, 3));
    }

    public ThemeIcon getMarkIconFromTheme() {
      return new ThemeIcon("apps", "office-calendar", 16);
    }
    
    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                Program[] programArr = { program };
                doExport(programArr);
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("contextMenuText","Export to Calendar-File"));
        action.putValue(Action.SMALL_ICON, createImageIcon("apps", "office-calendar", 16));
        
        return new ActionMenu(action);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#canReceivePrograms()
     */
    public boolean canReceivePrograms() {
        return true;
    }    
    
    /**
     * This method is invoked for multiple program execution.
     * 
     * @see #canReceivePrograms()
     */
    public void receivePrograms(Program[] programArr) {
        doExport(programArr);
    }

    /**
     * Get Settings-Tab
     * @return SettingsTab
     */
    public SettingsTab getSettingsTab() {
        return new CalendarSettingsTab(this, mSettings);
    }    
    
    /**
     * Stores the Settings
     * @return Settings
     */
    public Properties storeSettings() {
        return mSettings;
    }

    /**
     * Loads the Settings
     * @param settings Settings for this Plugin
     */
    public void loadSettings(Properties settings) {
        if (settings == null) {
            settings = new Properties();
        }

        this.mSettings = settings;
    }    
    
    /**
     * Called by the host-application during start-up.
     * 
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        try {
            int version = in.readInt();
            mSavePath = (String) in.readObject();
        } catch (Exception e) {
           // e.printStackTrace();
            mSavePath = "";
        }
    }

    /**
     * Counterpart to loadData. Called when the application shuts down.
     * 
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(1);
        out.writeObject(mSavePath);
    }
    
    
    /**
     * Starts the Export
     * @param programArr Array of Programs to export
     */
    private void doExport(Program[] programArr) {
        File file = chooseFile();
        if (file != null) {
            
            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(getParentFrame(), 
                        mLocalizer.msg("overwriteMessage", "The File \n{0}\nalready exists. Overwrite it?", file.getAbsolutePath()),
                        mLocalizer.msg("overwriteTitle", "Overwrite?"),
                        JOptionPane.YES_NO_OPTION
                        );
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            mSavePath = file.getAbsolutePath();
    
            if (file.getAbsolutePath().toLowerCase().endsWith(".vcs")) {
              new VCalExporter().exportVCal(file, programArr, mSettings);
            } else if (file.getAbsolutePath().toLowerCase().endsWith(".ics")) {
              new ICalExporter().exportICal(file, programArr, mSettings);
            }
        }
    }

    /**
     * Shows a Filechooser for vCal and iCal Files.
     * 
     * @return selected File
     */
    private File chooseFile() {
        JFileChooser select = new JFileChooser();

        ExtensionFileFilter vCal = new ExtensionFileFilter("vcs", "vCal (*.vcs)");
        ExtensionFileFilter iCal = new ExtensionFileFilter("ics", "iCal (*.ics)");
        select.addChoosableFileFilter(vCal);
        select.addChoosableFileFilter(iCal);
        
        if (mSavePath != null) {
            select.setSelectedFile(new File (mSavePath));

            if (mSavePath.toLowerCase().endsWith(".vcs")) {
                select.setFileFilter(vCal);
            } else {
                select.setFileFilter(iCal);
            }
        }
        
        if (select.showSaveDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION) {

            String filename = select.getSelectedFile().getAbsolutePath();

            String ext;

            if (select.getFileFilter() == vCal) {
                ext = ".vcs";
            } else {
                ext = ".ics";
            }

            if (!filename.toLowerCase().endsWith(ext)) {

                if (filename.endsWith(".")) {
                    filename = filename.substring(0, filename.length() - 1);
                }

                filename = filename + ext;
            }

            return new File(filename);
        }

        return null;
    }
}