/*
 * RecordingsPanel.java
 * Copyright (C) 2006 Probum
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
package dvbplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;

import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;
import dvbplugin.dvbviewer.DvbViewerTimers;
import dvbplugin.dvbviewer.ProcessHandler;
import dvbplugin.dvbviewer.ScheduledRecording;

/**
 * @author Probum
 */
public class RecordingsPanel extends JPanel implements java.awt.event.ActionListener,
        java.awt.event.MouseListener, javax.swing.event.ChangeListener {
  static final String DVBPLUGIN_RELOAD_BIGICON = "/dvbplugin/reloadIcon24.gif";

  /** field <code>serialVersionUID</code> */
  private static final long serialVersionUID = 651529285692734752L;

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(RecordingsPanel.class);

  /**  */
  private Program mProgram;

  /** */
  private JTable table;

  private List<ScheduledRecording> entries;

  private boolean startDVB;

  private boolean menu;


  /**
   * Creates new RecordingsPanel
   */
  public RecordingsPanel() {
    this(null);
    menu = true;

    buttonSwitch.setEnabled(false);
    buttonDelete.setEnabled(false);
  }


  /**
   * Creates new RecordingsPanel
   *
   * @param program
   */
  public RecordingsPanel(Program program) {
    menu = false;
    mProgram = program;

    initComponents();
    translateComponents();

    if (null == program) {
      buttonOK.setEnabled(false);
    }

    fillFields();
    showRecordings();
  }


  /**
   * Fills the Fields of the Dialog
   */
  private void fillFields() {
    Settings set = Settings.getSettings();
    if (!menu) {
      if (null != mProgram) {
        fieldDate.setText(HelperClass.date(mProgram));
        fieldChannel.setText(mProgram.getChannel().getName());
        labelChannelIcon.setIcon(mProgram.getChannel().getIcon());
        fieldTitle.setText(mProgram.getTitle());
        fieldStart.setText(HelperClass.calcStartTime(mProgram, set.getRecordBefore()));
        fieldEnd.setText(HelperClass.calcEndTime(mProgram, set.getRecordAfter()));
      }
    }
    comboActions.removeAllItems();
    comboActions.addItem(localizer.msg("action_none", "No action"));
    comboActions.addItem(localizer.msg("action_shutdown", "Force shutdown"));
    comboActions.addItem(localizer.msg("action_standby", "Force Standby"));
    comboActions.addItem(localizer.msg("action_hibernate", "Force hibernate"));
    comboActions.addItem(localizer.msg("action_close", "Close DVBViewer"));
    comboActions.addItem(localizer.msg("action_playlist", "Start Playlist"));
    comboActions.addItem(localizer.msg("action_viewerstandby", "DVBViewer Standby"));

    comboRecordingActions.removeAllItems();
    comboRecordingActions.addItem(localizer.msg("recordaction_record", "Record"));
    comboRecordingActions.addItem(localizer.msg("recordaction_setchannel", "Switch to channel"));
    comboRecordingActions.addItem(localizer.msg("recordaction_audiorec", "Audiorecord Plugin"));
    comboRecordingActions.addItem(localizer.msg("recordaction_videorec", "Videorecord Plugin"));

    buttonReload.setToolTipText(localizer.msg("reload_tooltip",
            "Reloads the table if DVBViewers recording list is changed."
                    + "\n(DVBViewer needs to be closed to add the recordings to the list)"));

    checkboxAVDisable.setSelected(set.isDefAvDisabled());
    comboActions.setSelectedIndex(set.getDefAfterAction());
    comboRecordingActions.setSelectedIndex(set.getDefRecAction());
  }


  /**
   * Creates the Tabel wich holds the scheduled recordings
   */
  private void showRecordings() {
    Settings set = Settings.getSettings();
    Marker marker = new Marker();
    set.setMarker(marker);

    if (set.isMarkRecordings()) {
      marker.mark();
    }

    panelRecordingsTable.removeAll();

    Vector<String> colNames = new Vector<String>();
    colNames.add(localizer.msg("timertable_column_channel", "Channel"));
    colNames.add(localizer.msg("timertable_column_date", "Date"));
    colNames.add(localizer.msg("timertable_column_start", "Start"));
    colNames.add(localizer.msg("timertable_column_end", "End"));
    colNames.add(localizer.msg("timertable_column_title", "Description/Title"));

    // get the recordings from DVBViewers timers list
    Vector<Vector<String>> data = new Vector<Vector<String>>();
    if (set.isValid()) {
      entries = DvbViewerTimers.getEntries(set.getViewerTimersPath());
      for (Iterator<ScheduledRecording> it = entries.iterator(); it.hasNext();) {
        ScheduledRecording rec = it.next();

        Vector<String> rows = new Vector<String>(5);
        // channel
        rows.add(rec.getDvbViewerChannel());
        // date
        rows.add(rec.getStartDate());
        // start
        rows.add(rec.getStartTime());
        // end
        rows.add(rec.getStopTime());
        // description / title
        rows.add(rec.getProgramTitle());

        data.add(rows);
      }
    }

    // create table, add mouse listener (for context menu)
    table = new JTable(data, colNames);
    table.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        tableMouseClicked(evt);
      }
    });

    // configure table
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getColumnModel().getColumn(0).setPreferredWidth(70);
    table.getColumnModel().getColumn(1).setPreferredWidth(70);
    table.getColumnModel().getColumn(2).setPreferredWidth(60);
    table.getColumnModel().getColumn(3).setPreferredWidth(60);
    table.getColumnModel().getColumn(4).setPreferredWidth(255);

    // make the table scrollable
    JScrollPane sc = new JScrollPane(table);
    Dimension dim = new Dimension(panelRecordingsTable.getPreferredSize().width,
            panelRecordingsTable.getPreferredSize().height - 10);
    sc.setPreferredSize(dim);
    panelRecordingsTable.add(sc);
    panelRecordingsTable.revalidate();
  }


  /**
   * removes the selected Rows from the Table
   */
  private void removeRows() {
    int response = HelperClass.confirm(localizer.msg("timertable_delete",
            "Really delete selected recordings?"), this);
    if (response == JOptionPane.YES_OPTION) {
      Settings set = Settings.getSettings();
      int[] selectedRows = table.getSelectedRows();
      for (int i = selectedRows.length - 1; i >= 0; i--) {
        entries.remove(selectedRows[i]);

        if (set.isMarkRecordings()) {
          set.getMarker().unmark(selectedRows[i]);
        }
      }

      // write remaining entries to DVBViewer file
      DvbViewerTimers.setEntries(set.getViewerTimersPath(), entries);

      if (ProcessHandler.isDVBViewerActive(set)) {
        // force DVBViewer to re-read the recordings list
        ProcessHandler.updateDvbViewer(set);
      } else {
        // update the scheduler if it exists
        ProcessHandler.runDvbSchedulerUpdate(set);
      }

      // clear table
      clear();

      // recreate table
      showRecordings();
    }
  }


  private void clear() {
    // clear everything
    fieldDate.setText("");
    fieldChannel.setText("");
    labelChannelIcon.setIcon(null);
    fieldTitle.setText("");
    fieldStart.setText("");
    fieldEnd.setText("");
    comboRecordingActions.setSelectedIndex(0);
    comboActions.setSelectedIndex(0);
    checkboxAVDisable.setSelected(false);
    checkboxMonday.setSelected(false);
    checkboxTuesday.setSelected(false);
    checkboxWednesday.setSelected(false);
    checkboxThursday.setSelected(false);
    checkboxFriday.setSelected(false);
    checkboxSaturday.setSelected(false);
    checkboxSunday.setSelected(false);
    buttonSwitch.setEnabled(false);
    buttonDelete.setEnabled(false);
    buttonOK.setEnabled(false);
  }


  /** localize all visible texts in the components */
  private void translateComponents() {
    labelDate.setText(localizer.msg("label_date", "Date:"));
    labelStart.setText(localizer.msg("label_start", "Start:"));
    labelEnd.setText(localizer.msg("label_end", "End:"));
    checkboxMonday.setText(localizer.msg("checkbox_monday", "Mo"));
    checkboxMonday.setToolTipText(localizer.msg("checkbox_monday_tooltip",
            "Repeat the recording every monday."));
    checkboxTuesday.setText(localizer.msg("checkbox_tuesday", "Tu"));
    checkboxTuesday.setToolTipText(localizer.msg("checkbox_tuesday_tooltip",
            "Repeat the recording every tuesday."));
    checkboxWednesday.setText(localizer.msg("checkbox_wednesday", "We"));
    checkboxWednesday.setToolTipText(localizer.msg("checkbox_wednesday_tooltip",
            "Repeat the recording every wednesday."));
    checkboxThursday.setText(localizer.msg("checkbox_thursday", "Th"));
    checkboxThursday.setToolTipText(localizer.msg("checkbox_thursday_tooltip",
            "Repeat the recording every thursday."));
    checkboxFriday.setText(localizer.msg("checkbox_friday", "Fr"));
    checkboxFriday.setToolTipText(localizer.msg("checkbox_friday_tooltip",
            "Repeat the recording every friday."));
    checkboxSaturday.setText(localizer.msg("checkbox_saturday", "Sa"));
    checkboxSaturday.setToolTipText(localizer.msg("checkbox_saturday_tooltip",
            "Repeat the recording every saturday."));
    checkboxSunday.setText(localizer.msg("checkbox_sunday", "Su"));
    checkboxSunday.setToolTipText(localizer.msg("checkbox_sunday_tooltip",
            "Repeat the recording every sunday."));
    labelChannel.setText(localizer.msg("label_channel", "Channel:"));
    labelTitle.setText(localizer.msg("label_title", "Title:"));
    comboActions.setToolTipText(localizer.msg("action_tooltip",
            "The action that is performed AFTER the recording is done"));
    comboRecordingActions.setToolTipText(localizer.msg("recordaction_tooltip",
            "The action performed with the start of the recording"));
    checkboxAVDisable.setText(localizer.msg("checkbox_avdisable", "Deactivate AV"));
    checkboxAVDisable.setToolTipText(localizer.msg("checkbox_avdisable_tooltip",
            "Disables the video output during the record"));
    checkboxStartDVBViewer.setText(localizer.msg("checkbox_startdvbviewer", "start DVBViewer"));
    checkboxStartDVBViewer.setToolTipText(localizer.msg("checkbox_startdvbviewer_tooltip",
            "activate to start the DVBViewer while adding of the program"));
    buttonOK.setText(localizer.msg("button_ok", "OK"));
    buttonOK.setToolTipText(localizer.msg("button_ok_tooltip", "Add/Change"));
    buttonClose.setText(localizer.msg("button_close", "Close"));
    buttonDelete.setText(localizer.msg("button_delete", "delete selection"));
    buttonDelete.setToolTipText(localizer.msg("button_delete_tooltip",
            "delete entries that are selected in the table"));
    buttonSwitch.setText(localizer.msg("button_switch", "Switch"));
    buttonSwitch.setToolTipText(localizer.msg("button_switch_tooltip", "Switch/Start DVBViewer"));
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code
  // ">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    panelRecordingsTable = new javax.swing.JPanel();
    buttonReload = new javax.swing.JButton();
    jSeparator3 = new javax.swing.JSeparator();
    panelDateTime = new javax.swing.JPanel();
    labelDate = new javax.swing.JLabel();
    fieldDate = new javax.swing.JTextField();
    labelStart = new javax.swing.JLabel();
    fieldStart = new javax.swing.JTextField();
    labelEnd = new javax.swing.JLabel();
    fieldEnd = new javax.swing.JTextField();
    panelRepetitionDays = new javax.swing.JPanel();
    checkboxMonday = new javax.swing.JCheckBox();
    checkboxTuesday = new javax.swing.JCheckBox();
    checkboxWednesday = new javax.swing.JCheckBox();
    checkboxThursday = new javax.swing.JCheckBox();
    checkboxFriday = new javax.swing.JCheckBox();
    checkboxSaturday = new javax.swing.JCheckBox();
    checkboxSunday = new javax.swing.JCheckBox();
    jSeparator1 = new javax.swing.JSeparator();
    panelProgram = new javax.swing.JPanel();
    labelChannel = new javax.swing.JLabel();
    labelChannelIcon = new javax.swing.JLabel();
    fieldChannel = new javax.swing.JTextField();
    labelTitle = new javax.swing.JLabel();
    fieldTitle = new javax.swing.JTextField();
    jSeparator2 = new javax.swing.JSeparator();
    panelSettings = new javax.swing.JPanel();
    comboActions = new javax.swing.JComboBox();
    comboRecordingActions = new javax.swing.JComboBox();
    checkboxAVDisable = new javax.swing.JCheckBox();
    checkboxStartDVBViewer = new javax.swing.JCheckBox();
    panelButtons = new javax.swing.JPanel();
    buttonOK = new javax.swing.JButton();
    buttonClose = new javax.swing.JButton();
    buttonDelete = new javax.swing.JButton();
    buttonSwitch = new javax.swing.JButton();

    setLayout(new java.awt.GridBagLayout());

    setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    panelRecordingsTable.setPreferredSize(new java.awt.Dimension(520, 180));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    add(panelRecordingsTable, gridBagConstraints);

    buttonReload
            .setIcon(new javax.swing.ImageIcon(getClass().getResource(DVBPLUGIN_RELOAD_BIGICON)));
    buttonReload.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    add(buttonReload, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSeparator3, gridBagConstraints);

    panelDateTime.setLayout(new java.awt.GridBagLayout());

    labelDate.setText("Date:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDateTime.add(labelDate, gridBagConstraints);

    fieldDate.setEditable(false);
    fieldDate.setPreferredSize(new java.awt.Dimension(75, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDateTime.add(fieldDate, gridBagConstraints);

    labelStart.setText("Start:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelDateTime.add(labelStart, gridBagConstraints);

    fieldStart.setPreferredSize(new java.awt.Dimension(60, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelDateTime.add(fieldStart, gridBagConstraints);

    labelEnd.setText("End:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDateTime.add(labelEnd, gridBagConstraints);

    fieldEnd.setPreferredSize(new java.awt.Dimension(60, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDateTime.add(fieldEnd, gridBagConstraints);

    checkboxMonday.setText("Mo");
    checkboxMonday.setToolTipText("Repeat the recording every monday.");
    panelRepetitionDays.add(checkboxMonday);

    checkboxTuesday.setText("Tu");
    checkboxTuesday.setToolTipText("Repeat the recording every tuesday.");
    panelRepetitionDays.add(checkboxTuesday);

    checkboxWednesday.setText("We");
    checkboxWednesday.setToolTipText("Repeat the recording every wednesday.");
    panelRepetitionDays.add(checkboxWednesday);

    checkboxThursday.setText("Th");
    checkboxThursday.setToolTipText("Repeat the recording every thursday.");
    panelRepetitionDays.add(checkboxThursday);

    checkboxFriday.setText("Fr");
    checkboxFriday.setToolTipText("Repeat the recording every friday.");
    panelRepetitionDays.add(checkboxFriday);

    checkboxSaturday.setText("Sa");
    checkboxSaturday.setToolTipText("Repeat the recording every saturday.");
    panelRepetitionDays.add(checkboxSaturday);

    checkboxSunday.setText("Su");
    checkboxSunday.setToolTipText("Repeat the recording every sunday.");
    panelRepetitionDays.add(checkboxSunday);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    panelDateTime.add(panelRepetitionDays, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
    add(panelDateTime, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSeparator1, gridBagConstraints);

    panelProgram.setLayout(new java.awt.GridBagLayout());

    panelProgram.setPreferredSize(new java.awt.Dimension(370, 50));
    labelChannel.setText("Channel:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelProgram.add(labelChannel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    panelProgram.add(labelChannelIcon, gridBagConstraints);

    fieldChannel.setEditable(false);
    fieldChannel.setMinimumSize(new java.awt.Dimension(60, 19));
    fieldChannel.setPreferredSize(new java.awt.Dimension(60, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelProgram.add(fieldChannel, gridBagConstraints);

    labelTitle.setText("Title:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelProgram.add(labelTitle, gridBagConstraints);

    fieldTitle.setEditable(false);
    fieldTitle.setMinimumSize(new java.awt.Dimension(250, 19));
    fieldTitle.setPreferredSize(new java.awt.Dimension(250, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelProgram.add(fieldTitle, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
    add(panelProgram, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(jSeparator2, gridBagConstraints);

    panelSettings.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    comboActions.setToolTipText("The action that is performed AFTER the recording is done");
    panelSettings.add(comboActions);

    comboRecordingActions.setToolTipText("The action performed with the start of the recording");
    panelSettings.add(comboRecordingActions);

    checkboxAVDisable.setText("Disable AV");
    checkboxAVDisable.setToolTipText("Disables the video output during the record");
    panelSettings.add(checkboxAVDisable);

    checkboxStartDVBViewer.setText("start DVBViewer");
    checkboxStartDVBViewer
            .setToolTipText("activate to start the DVBViewer while adding of the program");
    checkboxStartDVBViewer.addChangeListener(this);

    panelSettings.add(checkboxStartDVBViewer);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
    add(panelSettings, gridBagConstraints);

    buttonOK.setText("OK");
    buttonOK.setToolTipText("Add/Change");
    buttonOK.addActionListener(this);

    panelButtons.add(buttonOK);

    buttonClose.setText("Close");
    buttonClose.addActionListener(this);

    panelButtons.add(buttonClose);

    buttonDelete.setText("delete selection");
    buttonDelete.setToolTipText("delete entries that are selected in the table");
    buttonDelete.addMouseListener(this);

    panelButtons.add(buttonDelete);

    buttonSwitch.setText("Switch");
    buttonSwitch.setToolTipText("Switch/Start DVBViewer");
    buttonSwitch.addActionListener(this);

    panelButtons.add(buttonSwitch);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    add(panelButtons, gridBagConstraints);

  }


  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == buttonReload) {
      RecordingsPanel.this.buttonReloadActionPerformed(evt);
    } else if (evt.getSource() == buttonOK) {
      RecordingsPanel.this.buttonOKActionPerformed(evt);
    } else if (evt.getSource() == buttonClose) {
      RecordingsPanel.this.buttonCloseActionPerformed(evt);
    } else if (evt.getSource() == buttonSwitch) {
      RecordingsPanel.this.buttonSwitchActionPerformed(evt);
    }
  }


  public void mouseClicked(java.awt.event.MouseEvent evt) {
    if (evt.getSource() == buttonDelete) {
      RecordingsPanel.this.buttonDeleteMouseClicked(evt);
    }
  }


  public void mouseEntered(java.awt.event.MouseEvent evt) { /* not used */ }


  public void mouseExited(java.awt.event.MouseEvent evt) { /* not used */ }


  public void mousePressed(java.awt.event.MouseEvent evt) { /* not used */ }


  public void mouseReleased(java.awt.event.MouseEvent evt) { /* not used */ }


  public void stateChanged(javax.swing.event.ChangeEvent evt) {
    if (evt.getSource() == checkboxStartDVBViewer) {
      RecordingsPanel.this.checkboxStartDVBViewerStateChanged(evt);
    }
  }


  // </editor-fold>//GEN-END:initComponents

  private void buttonReloadActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonReloadActionPerformed
    showRecordings();
  }// GEN-LAST:event_buttonReloadActionPerformed


  private void buttonCloseActionPerformed(ActionEvent evt) {// GEN-FIRST:event_buttonCloseActionPerformed
    ((JDialog)getTopLevelAncestor()).dispose();
  }// GEN-LAST:event_buttonCloseActionPerformed


  /**
   * start the DVBViewer in the selected channel without adding the selected
   * mProgram to the recording list
   */
  private void buttonSwitchActionPerformed(ActionEvent evt) {// GEN-FIRST:event_buttonSwitchActionPerformed
    Settings set = Settings.getSettings();
    ProcessHandler.runDvbViewer(set, set.getChannelByTVBrowserName(fieldChannel.getText()));
  }// GEN-LAST:event_buttonSwitchActionPerformed


  protected void miAlActionPerformed(ActionEvent evt) {
    if (table.getSelectedRows().length > 0) removeRows();
  }


  private void fillDataFields() {
    buttonOK.setEnabled(true);
    buttonDelete.setEnabled(true);
    buttonSwitch.setEnabled(true);
    int selection = table.getSelectedRow();

    ScheduledRecording rec = entries.get(selection);
    fieldTitle.setText(rec.getProgramTitle());
    fieldDate.setText(rec.getStartDate());
    fieldStart.setText(rec.getStartTime());
    fieldEnd.setText(rec.getStopTime());

    String days = rec.getRepetitionDays();
    checkboxMonday.setSelected(days.charAt(0) == 'T');
    checkboxTuesday.setSelected(days.charAt(1) == 'T');
    checkboxWednesday.setSelected(days.charAt(2) == 'T');
    checkboxThursday.setSelected(days.charAt(3) == 'T');
    checkboxFriday.setSelected(days.charAt(4) == 'T');
    checkboxSaturday.setSelected(days.charAt(5) == 'T');
    checkboxSunday.setSelected(days.charAt(6) == 'T');

    comboActions.setSelectedIndex(rec.getAfterAction());
    comboRecordingActions.setSelectedIndex(rec.getRecAction());
    checkboxAVDisable.setSelected(rec.isAvDisable());

    // find TV-Browser channel name
    fieldChannel.setText(Settings.getSettings()
            .getChannelByDVBViewerName(rec.getDvbViewerChannel()).getTvBrowserName());

    // find icon for the channel
    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
    for (int i = 0; i < channels.length; i++) {
      if (channels[i].getName().equals(fieldChannel.getText())) {
        labelChannelIcon.setIcon(channels[i].getIcon());
        break;
      }
    }
  }


  protected void tableMouseClicked(MouseEvent evt) {
    if (evt.getButton() == MouseEvent.BUTTON1) {
      // fill table row data into fields
      fillDataFields();
    }
    if (evt.getButton() == MouseEvent.BUTTON3) {
      // show popup menu
      if (table.getSelectedRows().length <= 1) {
        int row = table.rowAtPoint(evt.getPoint());
        table.setRowSelectionInterval(row, row);
      }
      fillDataFields();
      JPopupMenu tablePopup = new JPopupMenu("Table PopupMenu");
      JMenuItem miAl = new JMenuItem(localizer.msg("menuitem_delete", "remove"));
      miAl.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          miAlActionPerformed(event);
        }
      });
      tablePopup.add(miAl);

      tablePopup.setInvoker(table);

      // show popup at mouse cursor position
      Point scr = table.getLocationOnScreen();
      tablePopup.setLocation(scr.x + evt.getX(), scr.y + evt.getY());
      tablePopup.setVisible(true);
    }
  }


  private void buttonDeleteMouseClicked(MouseEvent evt) {// GEN-FIRST:event_buttonDeleteMouseClicked
    if (table.getSelectedRows().length > 0) removeRows();
  }// GEN-LAST:event_buttonDeleteMouseClicked


  private void checkboxStartDVBViewerStateChanged(ChangeEvent evt) {// GEN-FIRST:event_checkboxStartDVBViewerStateChanged
    startDVB = !startDVB;
  }// GEN-LAST:event_checkboxStartDVBViewerStateChanged


  private String getRepeat() {
    StringBuffer rep = new StringBuffer(7);
    rep.append(checkboxMonday.isSelected() ? 'T' : '-');
    rep.append(checkboxTuesday.isSelected() ? 'T' : '-');
    rep.append(checkboxWednesday.isSelected() ? 'T' : '-');
    rep.append(checkboxThursday.isSelected() ? 'T' : '-');
    rep.append(checkboxFriday.isSelected() ? 'T' : '-');
    rep.append(checkboxSaturday.isSelected() ? 'T' : '-');
    rep.append(checkboxSunday.isSelected() ? 'T' : '-');

    return rep.toString();
  }


  private void buttonOKActionPerformed(ActionEvent evt) {// GEN-FIRST:event_buttonOKActionPerformed
    Settings set = Settings.getSettings();
    if (set.getChannelCount() == 0) {
      // no settings, tell user and close dialog
      HelperClass.error(localizer.msg("err_no_channelassignment",
              "No channels!\nPlease add some channels first!"));
      ((JDialog)getTopLevelAncestor()).dispose();
      return;
    }

    Settings.TvbDvbVChannel dummy = set.getChannelByTVBrowserName(fieldChannel.getText());
    if (!dummy.isValid()) {
      // channel not assigned
      HelperClass.error(localizer.msg("err_missing_assignment",
              "Channel not available, please add it!"));
      return;
    }

    // create the output
    ScheduledRecording rec = new ScheduledRecording();
    rec.setProgramTitle(fieldTitle.getText());
    rec.setDvbViewerChannel(dummy.getDVBChannel().name);
    rec.setAudioID(Integer.parseInt(dummy.getDVBChannel().audioID));
    rec.setServiceID(Integer.parseInt(dummy.getDVBChannel().serviceID));
    rec.setTunerType(Integer.parseInt(dummy.getDVBChannel().tunerType));
    rec.setStartDate(fieldDate.getText());
    rec.setStartTime(fieldStart.getText());
    rec.setStopTime(fieldEnd.getText());
    rec.setRecAction(comboRecordingActions.getSelectedIndex());
    rec.setRepetitionDays(getRepeat());
    rec.setAfterAction(comboActions.getSelectedIndex());
    rec.setAvDisable(checkboxAVDisable.isSelected());

    boolean isRunning = ProcessHandler.isDVBViewerActive(set);

    int requestRunningProgramReply = JOptionPane.NO_OPTION;

    if (table.getSelectedRowCount() > 0) {
      // element selected and changed
      entries.set(table.getSelectedRow(), rec);
    } else {
      if (!entries.contains(rec)) {
        // entry does not exist
        entries.add(rec);

        if (null != mProgram && set.isMarkRecordings()) {
          // we came from context menu so there is a program
          mProgram.mark(DVBPlugin.getInstance());
          set.getMarker().addProgram(mProgram);
          if (mProgram.isOnAir() && !isRunning) {
            requestRunningProgramReply = HelperClass.confirm(localizer.msg("request_startviewer",
                    "Program is running, start DVBViewer?"), this);
          }
        }
      }
    }

    // write out the data
    DvbViewerTimers.setEntries(set.getViewerTimersPath(), entries);

    if (startDVB || requestRunningProgramReply == JOptionPane.YES_OPTION) {
      ProcessHandler.runDvbViewer(set, set.getChannelByTVBrowserName(fieldChannel.getText()));
    } else if (isRunning) {
      // force DVBViewer to re-read the recordings list
      ProcessHandler.updateDvbViewer(set);
    } else {
      // update the scheduler if it exists
      ProcessHandler.runDvbSchedulerUpdate(set);
    }

    // window remains but all actions are done so clear the entries
    clear();

    showRecordings(); // recreate table
  }// GEN-LAST:event_buttonOKActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonClose;

  private javax.swing.JButton buttonDelete;

  private javax.swing.JButton buttonOK;

  private javax.swing.JButton buttonReload;

  private javax.swing.JButton buttonSwitch;

  private javax.swing.JCheckBox checkboxAVDisable;

  private javax.swing.JCheckBox checkboxFriday;

  private javax.swing.JCheckBox checkboxMonday;

  private javax.swing.JCheckBox checkboxSaturday;

  private javax.swing.JCheckBox checkboxStartDVBViewer;

  private javax.swing.JCheckBox checkboxSunday;

  private javax.swing.JCheckBox checkboxThursday;

  private javax.swing.JCheckBox checkboxTuesday;

  private javax.swing.JCheckBox checkboxWednesday;

  private javax.swing.JComboBox comboActions;

  private javax.swing.JComboBox comboRecordingActions;

  private javax.swing.JTextField fieldChannel;

  private javax.swing.JTextField fieldDate;

  private javax.swing.JTextField fieldEnd;

  private javax.swing.JTextField fieldStart;

  private javax.swing.JTextField fieldTitle;

  private javax.swing.JSeparator jSeparator1;

  private javax.swing.JSeparator jSeparator2;

  private javax.swing.JSeparator jSeparator3;

  private javax.swing.JLabel labelChannel;

  private javax.swing.JLabel labelChannelIcon;

  private javax.swing.JLabel labelDate;

  private javax.swing.JLabel labelEnd;

  private javax.swing.JLabel labelStart;

  private javax.swing.JLabel labelTitle;

  private javax.swing.JPanel panelButtons;

  private javax.swing.JPanel panelDateTime;

  private javax.swing.JPanel panelProgram;

  private javax.swing.JPanel panelRecordingsTable;

  private javax.swing.JPanel panelRepetitionDays;

  private javax.swing.JPanel panelSettings;
  // End of variables declaration//GEN-END:variables

}
