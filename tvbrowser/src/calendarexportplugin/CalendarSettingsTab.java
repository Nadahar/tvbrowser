/*
 * Created on 25.06.2004
 */
package calendarexportplugin;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

  private JComboBox mClassification;

  private JComboBox mShowTime;

  private CalendarExportPlugin mPlugin;

  private JCheckBox mUseAlarm;

  private JSpinner mAlarmMinutes;
  
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
    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,pref:grow, pref,5dlu",
        "5dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref, 3dlu, pref, 5dlu, pref"));
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
    
    pb.addLabel(mLocalizer.msg("Categorie", "Categorie") + ":", cc.xy(2,2));
    pb.add(mCategorie, cc.xyw(4,2,2));
    pb.addLabel(mLocalizer.msg("ShowTime", "Show Time as") + ":", cc.xy(2,4));
    pb.add(mShowTime, cc.xy(4,4));
    pb.addLabel(mLocalizer.msg("Classification", "Classification") + ":", cc.xy(2,6));
    pb.add(mClassification, cc.xy(4,6));
    
    mUseAlarm = new JCheckBox(mLocalizer.msg("reminder", "Use reminder"));
    pb.add(mUseAlarm, cc.xy(2,8));

    SpinnerModel model = new SpinnerNumberModel(0, 0, 180, 1);
    mAlarmMinutes = new JSpinner(model);
    JPanel panel = new JPanel(new BorderLayout());
    
    panel.add(mAlarmMinutes, BorderLayout.WEST);
    
    final JLabel label = new JLabel(mLocalizer.msg("minutesBefore", "Minutes before start."));
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    panel.add(label, BorderLayout.CENTER);

    mUseAlarm.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mAlarmMinutes.setEnabled(mUseAlarm.isSelected());
        label.setEnabled(mUseAlarm.isSelected());
      }
    });

    if (mSettings.getProperty(CalendarExportPlugin.PROP_ALARM, "true").equals("true")) {
      mUseAlarm.setSelected(true);
    }
    
    try {
      mAlarmMinutes.setValue(Integer.parseInt(mSettings.getProperty(CalendarExportPlugin.PROP_ALARMBEFORE, "0")));
    } catch (Exception e) {
    }
    
    mAlarmMinutes.setEnabled(mUseAlarm.isSelected());
    label.setEnabled(mUseAlarm.isSelected());
    
    
    pb.add(panel, cc.xy(4, 8));
    
    pb.add(mNulltime, cc.xyw(2,10,4));
    
    JButton extended = new JButton(mLocalizer.msg("extended", "Extended Settings"));
    
    extended.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showExtendedDialog(pb.getPanel());
      }
    });
    
    pb.add(extended, cc.xy(5,12));

//    pb.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("interface", "Interface:")), cc.xyw(1,14,6));
    
    return pb.getPanel();
  }

  /**
   * Shows the Dialog with the extended Settings
   * @param panel Parent-Panel
   */
  private void showExtendedDialog(JPanel panel) {
    ExtendedDialog dialog;
    
    Window comp = UiUtilities.getBestDialogParent(panel);
    
    if (comp instanceof JFrame) {
      dialog = new ExtendedDialog((JFrame) comp, mSettings);
    } else {
      dialog = new ExtendedDialog((JDialog) comp, mSettings);
    }
    
    UiUtilities.centerAndShow(dialog);
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
    
    mSettings.setProperty(CalendarExportPlugin.PROP_ALARM, mUseAlarm.isSelected()? "true": "false");
    mSettings.setProperty(CalendarExportPlugin.PROP_ALARMBEFORE, mAlarmMinutes.getValue().toString());
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