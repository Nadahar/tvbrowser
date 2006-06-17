/*
 * Created on 25.06.2004
 */
package calendarexportplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;
import devplugin.SettingsTab;

/**
 * Settings Tab for Calendar Export
 * 
 * @author bodo
 */
public class CalendarSettingsTab implements SettingsTab {
  /** Translation */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CalendarSettingsTab.class);

  /** Settings */
  private Properties mSettings;

  /** Length of Program */
  private JCheckBox mNulltime;

  private JTextField mCategorie;

  private ParamInputField mParamText;

  private JComboBox mClassification;

  private JComboBox mShowTime;

  private CalendarExportPlugin mPlugin;
  
  /**
   * Creates the Tab
   * 
   * @param settings
   */
  public CalendarSettingsTab(CalendarExportPlugin plugin, Properties settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,pref:grow,5dlu",
        "5dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref,15dlu,pref,fill:default:grow,5dlu"));
    CellConstraints cc = new CellConstraints();
    
    mCategorie = new JTextField(mSettings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, ""));
    
    String[] values = { mLocalizer.msg("Busy", "Busy"), mLocalizer.msg("Free", "Free") };

    mShowTime = new JComboBox(values);

    try {
      mShowTime.setSelectedIndex(Integer.parseInt(mSettings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0")));
    } catch (Exception e) {
    }
    
    String[] val2 = { mLocalizer.msg("Public", "Public"), mLocalizer.msg("Private", "Private"),
        mLocalizer.msg("Confidential", "Confidential") };

    mClassification = new JComboBox(val2);

    try {
      mClassification.setSelectedIndex(Integer.parseInt(mSettings.getProperty(CalendarExportPlugin.PROP_CLASSIFICATION, "0")));
    } catch (Exception e) {
    }
    
    mNulltime = new JCheckBox(mLocalizer.msg("nullTime", "Set length to 0 Minutes"));

    if (mSettings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true")) {
      mNulltime.setSelected(true);
    }
    
    mParamText = new ParamInputField(mSettings.getProperty(CalendarExportPlugin.PROP_PARAM, CalendarExportPlugin.DEFAULT_PARAMETER));
    
    pb.add(mNulltime, cc.xyw(2,2,3));
    pb.addLabel(mLocalizer.msg("Categorie", "Categorie") + ":", cc.xy(2,4));
    pb.add(mCategorie, cc.xy(4,4));
    pb.addLabel(mLocalizer.msg("ShowTime", "Show Time as") + ":", cc.xy(2,6));
    pb.add(mShowTime, cc.xy(4,6));
    pb.addLabel(mLocalizer.msg("Classification", "Classification") + ":", cc.xy(2,8));
    pb.add(mClassification, cc.xy(4,8));
        
    pb.addLabel(mLocalizer.msg("createDescription", "Description to create for each Program") + ":", cc.xyw(2,10,3));
    pb.add(mParamText, cc.xyw(2,11,3));
        
    return pb.getPanel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings() {

    if (mNulltime.isSelected()) {
      mSettings.setProperty(CalendarExportPlugin.PROP_NULLTIME, "true");
    } else {
      mSettings.setProperty(CalendarExportPlugin.PROP_NULLTIME, "false");
    }

    mSettings.setProperty(CalendarExportPlugin.PROP_CATEGORIE, mCategorie.getText());
    mSettings.setProperty(CalendarExportPlugin.PROP_SHOWTIME, Integer.toString(mShowTime.getSelectedIndex()));
    mSettings.setProperty(CalendarExportPlugin.PROP_CLASSIFICATION, Integer.toString(mClassification.getSelectedIndex()));
    mSettings.setProperty(CalendarExportPlugin.PROP_PARAM, mParamText.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon() {
    return mPlugin.createImageIcon("apps", "office-calendar", 16);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getTitle()
   */
  public String getTitle() {
    return mLocalizer.msg("tabName", "Calendar Export");
  }

}