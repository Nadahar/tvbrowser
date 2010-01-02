/*
 * DVBViewerSettingsPanel.java
 * Copyright (C) 2008 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
package dvbviewerdataservice;


import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvdataservice.SettingsPanel;
import util.ui.Localizer;


/**
 * Panel to make the EPG fetching settings user modifyable
 * <p>
 * This Panel will be displayed in the TV data services category of the TV-Browser
 * settings to give the user a chance to change the EPG update of the DVBViewer
 *
 * @author pollaehne
 * @version $Revision: $
 */
public class DVBViewerSettingsPanel extends SettingsPanel implements ChangeListener {

  /** field <code>serialVersionUID</code> */
  private static final long serialVersionUID = -3673034576060364140L;

  private static final Localizer localizer = Localizer.getLocalizerFor(DVBViewerSettingsPanel.class);

  private DVBViewerDataService.Settings settings;

  private JCheckBox updateEPG;
  private JSpinner fetchTime;
  private SpinnerNumberModel fetchTimeModel;
  private JSpinner time2Record;
  private SpinnerNumberModel time2RecordModel;


  /**
   * Costruct the panel and insert the default data given by <code>epgSettings</code>
   *
   * @param epgSettings the settings to show and modify
   */
  public DVBViewerSettingsPanel(DVBViewerDataService.Settings epgSettings) {

    settings = epgSettings;

    // 2 columns, 3 rows
    setLayout(new FormLayout("pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref"));
    setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();

    add(new JLabel(localizer.msg("updateepg", "Update DVBViewer EPG") + ":"), cc.xy(1, 1));
    updateEPG = new JCheckBox();
    updateEPG.setSelected(settings.updateEPG);
    updateEPG.setToolTipText(localizer.msg("updateepg.tooltip", "Update DVBViewer EPG"));
    updateEPG.addChangeListener(this);
    add(updateEPG, cc.xy(3, 1));

    add(new JLabel(localizer.msg("fetchtime", "Fetchtime") + ":"), cc.xy(1, 3));
    fetchTimeModel = new SpinnerNumberModel(settings.fetchTime, 1, 1000, 1);
    fetchTimeModel.addChangeListener(this);
    fetchTime = new JSpinner(fetchTimeModel);
    fetchTime.setToolTipText(localizer.msg("fetchtime.tooltip", "Fetchtime"));
    fetchTime.setEnabled(settings.updateEPG);
    add(fetchTime, cc.xy(3, 3));

    add(new JLabel(localizer.msg("time2record", "Time before recording") + ":"), cc.xy(1, 5));
    time2RecordModel = new SpinnerNumberModel(settings.timeBeforeRecording, settings.fetchTime, 1000, 1);
    time2Record = new JSpinner(time2RecordModel);
    time2Record.setToolTipText(localizer.msg("time2record.tooltip", "Time before recording"));
    time2Record.setEnabled(settings.updateEPG);
    add(time2Record, cc.xy(3, 5));
  }


  /**
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == fetchTimeModel) {
      Integer min = (Integer)fetchTimeModel.getNumber();
      time2RecordModel.setMinimum(min);
      Integer curr = (Integer)time2RecordModel.getNumber();
      if (0 < min.compareTo(curr)) {
        time2RecordModel.setValue(min);
      }
    } else if (e.getSource() == updateEPG) {
      boolean enable = updateEPG.isSelected();
      fetchTime.setEnabled(enable);
      time2Record.setEnabled(enable);
    }
  }


  /**
   * @see tvdataservice.SettingsPanel#ok()
   */
  @Override
  public void ok() {
    settings.updateEPG = updateEPG.isSelected();
    if (settings.updateEPG) {
      settings.fetchTime = fetchTimeModel.getNumber().intValue();
      settings.timeBeforeRecording = time2RecordModel.getNumber().intValue();
    }

    fetchTimeModel.removeChangeListener(this);
    updateEPG.removeChangeListener(this);
  }
}



