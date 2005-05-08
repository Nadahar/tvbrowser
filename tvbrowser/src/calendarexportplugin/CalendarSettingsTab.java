/*
 * Created on 25.06.2004
 */
package calendarexportplugin;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.paramhandler.ParamInputField;
import util.ui.ImageUtilities;
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

  /**
   * Creates the Tab
   * 
   * @param settings
   */
  public CalendarSettingsTab(Properties settings) {
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {
    final JPanel toppanel = new JPanel(new GridBagLayout());

    toppanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Settings", "Settings")));

    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(5, 0, 0, 5);

    GridBagConstraints l = new GridBagConstraints();

    l.insets = new Insets(5, 0, 0, 5);
    l.anchor = GridBagConstraints.NORTHWEST;

    toppanel.add(new JLabel(mLocalizer.msg("Categorie", "Categorie") + ":"), l);

    mCategorie = new JTextField();

    mCategorie.setText(mSettings.getProperty("Categorie", ""));

    toppanel.add(mCategorie, c);

    toppanel.add(new JLabel(mLocalizer.msg("ShowTime", "Show Time as") + ":"), l);

    String[] values = { mLocalizer.msg("Busy", "Busy"), mLocalizer.msg("Free", "Free") };

    mShowTime = new JComboBox(values);

    try {
      mShowTime.setSelectedIndex(Integer.parseInt(mSettings.getProperty("ShowTime", "0")));
    } catch (Exception e) {
    }

    toppanel.add(mShowTime, c);

    toppanel.add(new JLabel(mLocalizer.msg("Classification", "Classification") + ":"), l);

    String[] val2 = { mLocalizer.msg("Public", "Public"), mLocalizer.msg("Private", "Private"),
        mLocalizer.msg("Confidential", "Confidential") };

    mClassification = new JComboBox(val2);

    try {
      mClassification.setSelectedIndex(Integer.parseInt(mSettings.getProperty("Classification", "0")));
    } catch (Exception e) {
    }

    toppanel.add(mClassification, c);

    mNulltime = new JCheckBox(mLocalizer.msg("nullTime", "Set length to 0 Minutes"));

    if (mSettings.getProperty("nulltime", "false").equals("true")) {
      mNulltime.setSelected(true);
    }

    toppanel.add(mNulltime, c);

    JPanel panel = new JPanel(new BorderLayout());
    
    panel.add(toppanel, BorderLayout.NORTH);

    mParamText = new ParamInputField(mSettings.getProperty("paramToUse", CalendarExportPlugin.DEFAULT_PARAMETER));
    
    mParamText.setBorder(BorderFactory.createTitledBorder(
        mLocalizer.msg("description", "Description")));
    
    panel.add(mParamText, BorderLayout.CENTER);
    
    return panel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings() {

    if (mNulltime.isSelected()) {
      mSettings.setProperty("nulltime", "true");
    } else {
      mSettings.setProperty("nulltime", "false");
    }

    mSettings.setProperty("Categorie", mCategorie.getText());
    mSettings.setProperty("ShowTime", Integer.toString(mShowTime.getSelectedIndex()));
    mSettings.setProperty("Classification", Integer.toString(mClassification.getSelectedIndex()));
    mSettings.setProperty("paramToUse", mParamText.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon() {
    return ImageUtilities.createImageIconFromJar("calendarexportplugin/calendar.png", getClass());
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