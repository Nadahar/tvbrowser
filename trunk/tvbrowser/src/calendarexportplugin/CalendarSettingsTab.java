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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;
import calendarexportplugin.exporter.ExporterIf;

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

  private SelectableItemList mExporterList;
  private JCheckBox mMarkItems;

  /**
   * Creates the Tab
   *
   * @param plugin Plugin-Instance
   * @param settings Settings for this Plugin
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
    final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,default:grow, pref,5dlu",
        "5dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref, 3dlu, pref, 5dlu, pref, 3dlu,pref, 3dlu, fill:default:grow, 3dlu, pref"));
    CellConstraints cc = new CellConstraints();
    
    mCategorie = new JTextField(mSettings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, ""));
    
    String[] values = { mLocalizer.msg("Busy", "Busy"), mLocalizer.msg("Free", "Free") };

    mShowTime = new JComboBox(values);

    try {
      mShowTime.setSelectedIndex(Integer.parseInt(mSettings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0")));
    } catch (Exception e) {
        // Empty
    }
    
    String[] val2 = { mLocalizer.msg("Public", "Public"), mLocalizer.msg("Private", "Private"),
        mLocalizer.msg("Confidential", "Confidential") };

    mClassification = new JComboBox(val2);

    try {
      mClassification.setSelectedIndex(Integer.parseInt(mSettings.getProperty(CalendarExportPlugin.PROP_CLASSIFICATION, "0")));
    } catch (Exception e) {
        // empty
    }
    
    mNulltime = new JCheckBox(mLocalizer.msg("nullTime", "Set length to 0 Minutes"));

    if (mSettings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true")) {
      mNulltime.setSelected(true);
    }
    
    pb.addLabel(mLocalizer.msg("Categorie", "Categorie") + ':', cc.xy(2,2));
    pb.add(mCategorie, cc.xyw(4,2,2));
    pb.addLabel(mLocalizer.msg("ShowTime", "Show Time as") + ':', cc.xy(2,4));
    pb.add(mShowTime, cc.xy(4,4));
    pb.addLabel(mLocalizer.msg("Classification", "Classification") + ':', cc.xy(2,6));
    pb.add(mClassification, cc.xy(4,6));
    
    mUseAlarm = new JCheckBox(mLocalizer.msg("reminder", "Use reminder"));
    pb.add(mUseAlarm, cc.xy(2,8));

    SpinnerModel model = new SpinnerNumberModel(0, 0, 1440, 1);
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
        // empty
    }
    
    mAlarmMinutes.setEnabled(mUseAlarm.isSelected());
    label.setEnabled(mUseAlarm.isSelected());
    
    
    pb.add(panel, cc.xyw(4, 8, 2));
    
    pb.add(mNulltime, cc.xyw(2,10,4));

    mMarkItems = new JCheckBox(mLocalizer.msg("markItems", "Mark items when exported"));
    if (mSettings.getProperty(CalendarExportPlugin.PROP_MARK_ITEMS, "true").equals("true")) {
      mMarkItems.setSelected(true);
    }
    pb.add(mMarkItems, cc.xyw(2,12,4));

    JButton extended = new JButton(mLocalizer.msg("extended", "Extended Settings"));
    
    extended.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showExtendedDialog(pb.getPanel());
      }
    });
    
    pb.add(extended, cc.xy(5,14));

    pb.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("interface", "Interface:")), cc.xyw(1,16,6));
   
    mExporterList = new SelectableItemList(mPlugin.getExporterFactory().getActiveExporters(), mPlugin.getExporterFactory().getAllExporters());
    pb.add(mExporterList, cc.xyw(2,18,4));

    final JButton settings = new JButton(Localizer.getLocalization(Localizer.I18N_SETTINGS));
    settings.setEnabled(false);
    
    mExporterList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        Object[] ob = mExporterList.getListSelection();
        if ((ob.length == 1) && (((ExporterIf)ob[0]).hasSettingsDialog())) {
          settings.setEnabled(true);
        } else
          settings.setEnabled(false);
      }
    });
    
    settings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] ob = mExporterList.getListSelection();
        if (ob.length == 1) {
          ((ExporterIf)ob[0]).showSettingsDialog(mSettings);
        }
      }
    });

    JPanel btnpanel = new JPanel(new BorderLayout());
    btnpanel.add(settings, BorderLayout.EAST);
    
    pb.add(btnpanel, cc.xyw(2,20,4));
    
    return pb.getPanel();
  }

  /**
   * Shows the Dialog with the extended Settings
   * @param panel Parent-Panel
   */
  private void showExtendedDialog(JPanel panel) {
    Window parent = UiUtilities.getBestDialogParent(panel);
    ExtendedDialog dialog = new ExtendedDialog(parent);
    
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
    mSettings.setProperty(CalendarExportPlugin.PROP_MARK_ITEMS, mMarkItems.isSelected()? "true": "false");

    Object[] selection = mExporterList.getSelection();
    
    ExporterIf[] exporter = new ExporterIf[selection.length];
    
    for (int i=0;i<selection.length;i++) {
      exporter[i] = (ExporterIf) selection[i];
    }
    
    mPlugin.getExporterFactory().setActiveExporters(exporter);
    
    mSettings.setProperty(CalendarExportPlugin.PROP_ACTIVE_EXPORTER, mPlugin.getExporterFactory().getListOfActiveExporters());
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