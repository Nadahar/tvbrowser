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

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;
import calendarexportplugin.exporter.ExporterIf;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

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
  private CalendarExportSettings mSettings;

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
  public CalendarSettingsTab(CalendarExportPlugin plugin, CalendarExportSettings settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder pb = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ","
        + FormFactory.PREF_COLSPEC.encode() + "," + FormFactory.RELATED_GAP_COLSPEC.encode() + ",default:grow,"
        + FormFactory.RELATED_GAP_COLSPEC.encode() + "," + FormFactory.PREF_COLSPEC.encode());
    CellConstraints cc = new CellConstraints();
    
    mCategorie = new JTextField(mSettings.getCategory());
    
    String[] reservedValues = { mLocalizer.msg("Busy", "Busy"), mLocalizer.msg("Free", "Free") };

    mShowTime = new JComboBox(reservedValues);
    if (mSettings.isShowBusy()) {
      mShowTime.setSelectedIndex(0);
    }
    else if (mSettings.isShowFree()) {
      mShowTime.setSelectedIndex(1);
    }
    
    String[] classificationValues = { mLocalizer.msg("Public", "Public"), mLocalizer.msg("Private", "Private"),
        mLocalizer.msg("Confidential", "Confidential") };

    mClassification = new JComboBox(classificationValues);

    if (mSettings.isClassificationPublic()) {
      mClassification.setSelectedIndex(0);
    }
    else if (mSettings.isClassificationPrivate()) {
      mClassification.setSelectedIndex(1);
    }
    else if (mSettings.isClassificationConfidential()) {
      mClassification.setSelectedIndex(2);
    }
    
    mNulltime = new JCheckBox(mLocalizer.msg("nullTime", "Set length to 0 Minutes"));

    if (mSettings.getNullTime()) {
      mNulltime.setSelected(true);
    }
    
    pb.addRow();
    pb.addLabel(mLocalizer.msg("Categorie", "Categorie") + ':', cc.xy(2,pb.getRow()));
    pb.add(mCategorie, cc.xyw(4,pb.getRow(), pb.getColumnCount() - 3));

    pb.addRow();
    pb.addLabel(mLocalizer.msg("ShowTime", "Show Time as") + ':', cc.xy(2,pb.getRow()));
    pb.add(mShowTime, cc.xyw(4,pb.getRow(), pb.getColumnCount() - 3));
    
    pb.addRow();
    pb.addLabel(mLocalizer.msg("Classification", "Classification") + ':', cc.xy(2,pb.getRow()));
    pb.add(mClassification, cc.xyw(4,pb.getRow(), pb.getColumnCount() - 3));
    
    mUseAlarm = new JCheckBox(mLocalizer.msg("reminder", "Use reminder"));
    pb.addRow();
    pb.add(mUseAlarm, cc.xy(2,pb.getRow()));

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

    if (mSettings.getUseAlarm()) {
      mUseAlarm.setSelected(true);
    }
    
    try {
      mAlarmMinutes.setValue(mSettings.getAlarmMinutes());
    } catch (Exception e) {
        // empty
    }
    
    mAlarmMinutes.setEnabled(mUseAlarm.isSelected());
    label.setEnabled(mUseAlarm.isSelected());
    
    
    pb.add(panel, cc.xyw(4, pb.getRow(), 2));
    
    pb.addRow();
    pb.add(mNulltime, cc.xyw(2,pb.getRow(),4));

    mMarkItems = new JCheckBox(mLocalizer.msg("markItems", "Mark items when exported"));
    if (mSettings.getMarkItems()) {
      mMarkItems.setSelected(true);
    }

    pb.addRow();
    pb.add(mMarkItems, cc.xyw(2,pb.getRow(),4));

    pb.addParagraph(mLocalizer.msg("interface", "Interface"));
   
    mExporterList = new SelectableItemList(mPlugin.getExporterFactory().getActiveExporters(), mPlugin.getExporterFactory().getAllExporters());
    pb.addRow("120");
    pb.add(mExporterList, cc.xyw(2,pb.getRow(),3));

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

    pb.add(settings, cc.xy(6,pb.getRow(), CellConstraints.RIGHT, CellConstraints.TOP));
    
    pb.addParagraph(mLocalizer.msg("formattings", "Formattings"));

    JButton extended = new JButton(mLocalizer.msg("formattings", "Formattings"));
    
    extended.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showExtendedDialog(pb.getPanel());
      }
    });
    
    pb.addRow();
    pb.add(extended, cc.xy(2,pb.getRow()));
    
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
  
  public void saveSettings() {
    mSettings.setNullTime(mNulltime.isSelected());

    mSettings.setCategory(mCategorie.getText());
    if (mShowTime.getSelectedIndex() == 1) {
      mSettings.setReservation(Reservation.Free);
    }
    else {
      mSettings.setReservation(Reservation.Busy);
    }
    switch (mClassification.getSelectedIndex()) {
    case 1:
      mSettings.setClassification(Classification.Private);
      break;
    case 2:
      mSettings.setClassification(Classification.Confidential);
      break;
    default:
      mSettings.setClassification(Classification.Public);
    }
    
    mSettings.setUseAlarm(mUseAlarm.isSelected());
    mSettings.setAlarmMinutes((Integer)mAlarmMinutes.getValue());
    mSettings.setMarkItems(mMarkItems.isSelected());

    Object[] selection = mExporterList.getSelection();
    
    ExporterIf[] exporter = new ExporterIf[selection.length];
    
    for (int i=0;i<selection.length;i++) {
      exporter[i] = (ExporterIf) selection[i];
    }
    
    mPlugin.getExporterFactory().setActiveExporters(exporter);
    
    mSettings.setActiveExporters(mPlugin.getExporterFactory().getListOfActiveExporters());
  }

  public Icon getIcon() {
    return mPlugin.createImageIcon("apps", "office-calendar", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("tabName", "Calendar Export");
  }

}