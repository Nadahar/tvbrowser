/*
 * SettingsPanel.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import util.exc.ErrorHandler;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import devplugin.Channel;
import dvbplugin.Settings.TvbDvbVChannel;

/**
 * @author Probum
 */
class SettingsPanel extends JPanel implements java.awt.event.ActionListener,
        java.awt.event.FocusListener {

  /** field <code>serialVersionUID</code> */
  private static final long serialVersionUID = 1574224277181535370L;

  private static final String FILE_SEP = System.getProperty("file.separator");

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(SettingsPanel.class);

  private static Logger logger = Logger.getLogger(SettingsPanel.class.getName());

  private Channel[] channel;

  private Properties dvbChannels;

  private boolean comboListener;


  /**
   * Creates new form SettingsPanel.
   * If <code>isWindows</code> is true then the real settings panel is used.
   * Otherwise a short text is shown.
   */
  SettingsPanel() {
    if (OperatingSystem.isWindows()) {
      initComponents();
      translateComponents();

      initData();

      reloadAssignmentsPanel();
    } else {
      setLayout(new BorderLayout());
      String msg = localizer.msg("err_not_windows", "Sorry, this plugin only works on Microsoft Windows.");

      JTextArea msgArea =new JTextArea(3,40);
      msgArea.setText(msg);
      msgArea.setLineWrap(true);
      msgArea.setWrapStyleWord(true);
      msgArea.setEditable(false);
      msgArea.setOpaque(false);

      add(msgArea, BorderLayout.WEST);

    }
  }


  /**
   * Fills the Fields of the Dialog
   */
  private void initData() {
    comboListener = true;
    Settings set = Settings.getSettings();
    // show path to DVBViewer
    String path = set.getViewerExePath();
    if (null != path) {
      labelDVB.setText(path);
    }
    // set recordings marker checkbox
    buttonMarker.setSelected(set.isMarkRecordings());

    textfieldBefore.setText(String.valueOf(set.getRecordBefore()));
    textfieldAfter.setText(String.valueOf(set.getRecordAfter()));

    dvbChannels = ChannelGrabber.readChannelProperties();
    if (0 != dvbChannels.size()) {
      String value = (String)dvbChannels.values().iterator().next();
      if (-1 == value.indexOf('|')) {
        HelperClass.error(localizer.msg("err_old_listformat",
                "Channel list has old format, please re-generate the list."));
      }
    }

    // get sorted channels
    TreeMap tm = new TreeMap(dvbChannels);

    // fill combobox with sorted list of DVBViewer names
    for (Iterator it = tm.keySet().iterator(); it.hasNext();) {
      dvbName.addItem(it.next());
    }

    dvbName.repaint();
    dvbName.revalidate();

    // get the subscribed TV-Browser channels
    channel = devplugin.Plugin.getPluginManager().getSubscribedChannels();

    // fill combobox with the subscribed channels
    for (int i = 0; i < channel.length; i++) {
      tvbName.addItem(channel[i].getName());
    }
  }


  /** localize all visible texts in the components */
  private void translateComponents() {
    buttonGenerate.setText(localizer.msg("button_generate", "Read DVBViewer channels"));
    buttonGenerate.setToolTipText(localizer.msg("button_generate_tooltip",
            "Reads in the the channels configuration from DVBViewer"));

    labelGenerate.setText(localizer.msg("label_generate", "Reads channels from DVBViewer"));
    labelGenerate.setToolTipText(localizer.msg("label_generate_tooltip",
            "Reads all channels that are stored in DVBViewer"));

    labelChannelsTvBrowser
            .setText(localizer.msg("label_channels_tvbrowser", "TV-Browser Channels"));

    labelChannelsDvb.setText(localizer.msg("label_channels_dvbviewer", "DVBViewer Channels"));

    dvbName
            .setToolTipText(localizer
                    .msg(
                            "combo_channels_dvbbrowser_tooltip",
                            "ATTENTION!! Channelnames within the DVBViewer are not as in TV-Browser. Please refer to the channelslist in DVBViewer"));

    buttonAssign.setText(localizer.msg("button_assign", "Set"));
    buttonAssign.setToolTipText(localizer.msg("button_assign_tooltip",
            "Sets the TV-Browser channels to localize the DVBViewer channels"));

    labelChannelTvbName.setText(localizer.msg("label_channels_tvbname", "Channel name"));

    labelChannelDvbName.setText(localizer.msg("label_channels_dvbname", "Channel name"));

    labelServicePID.setText(localizer.msg("label_servicepid", "Service PID"));

    buttonSearch.setText(localizer.msg("button_search", "Search"));

    labelDvbViewerPath.setText(localizer.msg("label_dvbviewerpath",
            "Path and application name of DVBViewer"));

    buttonMarker.setText(localizer.msg("button_marker", "Mark programs within TV-Browser?"));
    buttonMarker.setToolTipText(localizer.msg("button_marker_tooltip",
            "May cause a slower startup!"));

    buttonDelete.setText(localizer.msg("button_delete", "Remove ALL settings!!"));
    buttonDelete.setToolTipText(localizer.msg("button_delete_tooltip", "Deletes ALL settings!"));

    buttonImport.setText(localizer.msg("button_import", "Import"));

    buttonExport.setText(localizer.msg("button_export", "Export"));

    labelBefore.setText(localizer.msg("label_before", "Time Before (m):"));

    labelAfter.setText(localizer.msg("label_after", "Time After (m):"));
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

    panelGenerate = new javax.swing.JPanel();
    buttonGenerate = new javax.swing.JButton();
    labelGenerate = new javax.swing.JLabel();
    jSeparator3 = new javax.swing.JSeparator();
    panelDisplay = new javax.swing.JPanel();
    labelChannelsTvBrowser = new javax.swing.JLabel();
    tvbName = new javax.swing.JComboBox();
    labelChannelsDvb = new javax.swing.JLabel();
    dvbName = new javax.swing.JComboBox();
    buttonAssign = new javax.swing.JButton();
    panelTVBrowser = new javax.swing.JPanel();
    labelChannelTvbName = new javax.swing.JLabel();
    textfieldSender = new javax.swing.JTextField();
    panelDVBViewer = new javax.swing.JPanel();
    labelChannelDvbName = new javax.swing.JLabel();
    textfieldDVBName = new javax.swing.JTextField();
    labelServicePID = new javax.swing.JLabel();
    textfieldServicePID = new javax.swing.JTextField();
    zuwScroll = new javax.swing.JScrollPane();
    zuwPanel = new javax.swing.JPanel();
    jSeparator1 = new javax.swing.JSeparator();
    jSeparator2 = new javax.swing.JSeparator();
    buttonMarker = new javax.swing.JCheckBox();
    panelButton = new javax.swing.JPanel();
    buttonDelete = new javax.swing.JButton();
    buttonImport = new javax.swing.JButton();
    buttonExport = new javax.swing.JButton();
    panelBeforeAfter = new javax.swing.JPanel();
    labelBefore = new javax.swing.JLabel();
    textfieldBefore = new javax.swing.JTextField();
    labelAfter = new javax.swing.JLabel();
    textfieldAfter = new javax.swing.JTextField();
    panelAppPath = new javax.swing.JPanel();
    buttonSearch = new javax.swing.JButton();
    labelDVB = new javax.swing.JLabel();
    labelDvbViewerPath = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    setBorder(new javax.swing.border.TitledBorder(""));
    setMinimumSize(new java.awt.Dimension(600, 370));
    setPreferredSize(new java.awt.Dimension(600, 370));
    buttonGenerate.setText("Read DVBViewer channels");
    buttonGenerate.setToolTipText("Reads in the the channels configuration from DVBViewer");
    buttonGenerate.addActionListener(this);

    panelGenerate.add(buttonGenerate);

    labelGenerate.setText("Reads channels from DVBViewer");
    labelGenerate.setToolTipText("Reads all channels that are stored in DVBViewer");
    panelGenerate.add(labelGenerate);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    add(panelGenerate, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 2, 6, 2);
    add(jSeparator3, gridBagConstraints);

    panelDisplay.setLayout(new java.awt.GridBagLayout());

    labelChannelsTvBrowser.setText("TV-Browser Channels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDisplay.add(labelChannelsTvBrowser, gridBagConstraints);

    tvbName.setMinimumSize(new java.awt.Dimension(120, 18));
    tvbName.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
    panelDisplay.add(tvbName, gridBagConstraints);

    labelChannelsDvb.setText("DVBViewer Channels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDisplay.add(labelChannelsDvb, gridBagConstraints);

    dvbName.setToolTipText("Channel name");
    dvbName.setMinimumSize(new java.awt.Dimension(120, 18));
    dvbName.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
    panelDisplay.add(dvbName, gridBagConstraints);

    buttonAssign.setText("Set");
    buttonAssign.setToolTipText("Sets the TV-Browser channels to localize the DVBViewer channels");
    buttonAssign.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    panelDisplay.add(buttonAssign, gridBagConstraints);

    panelTVBrowser.setLayout(new java.awt.GridBagLayout());

    panelTVBrowser.setBorder(new javax.swing.border.TitledBorder("TV-Browser"));
    labelChannelTvbName.setText("Channel name");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelTVBrowser.add(labelChannelTvbName, gridBagConstraints);

    textfieldSender.setEditable(false);
    textfieldSender.setMinimumSize(new java.awt.Dimension(100, 19));
    textfieldSender.setPreferredSize(new java.awt.Dimension(100, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelTVBrowser.add(textfieldSender, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    panelDisplay.add(panelTVBrowser, gridBagConstraints);

    panelDVBViewer.setLayout(new java.awt.GridBagLayout());

    panelDVBViewer.setBorder(new javax.swing.border.TitledBorder("DVBViewer"));
    labelChannelDvbName.setText("DVBViewer Channels");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelDVBViewer.add(labelChannelDvbName, gridBagConstraints);

    textfieldDVBName.setEditable(false);
    textfieldDVBName.setMinimumSize(new java.awt.Dimension(100, 19));
    textfieldDVBName.setPreferredSize(new java.awt.Dimension(100, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    panelDVBViewer.add(textfieldDVBName, gridBagConstraints);

    labelServicePID.setText("Service PID");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelDVBViewer.add(labelServicePID, gridBagConstraints);

    textfieldServicePID.setEditable(false);
    textfieldServicePID.setMinimumSize(new java.awt.Dimension(75, 19));
    textfieldServicePID.setPreferredSize(new java.awt.Dimension(75, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
    panelDVBViewer.add(textfieldServicePID, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
    panelDisplay.add(panelDVBViewer, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(panelDisplay, gridBagConstraints);

    zuwScroll.setMinimumSize(new java.awt.Dimension(210, 150));
    zuwScroll.setPreferredSize(new java.awt.Dimension(210, 150));
    zuwPanel.setLayout(new java.awt.GridLayout(21, 0));

    zuwScroll.setViewportView(zuwPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 2);
    add(zuwScroll, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 2, 6, 2);
    add(jSeparator1, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 2, 6, 2);
    add(jSeparator2, gridBagConstraints);

    buttonMarker.setText("Mark programs within TV-Browser?");
    buttonMarker.setToolTipText("May cause a slower startup!");
    buttonMarker.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    add(buttonMarker, gridBagConstraints);

    buttonDelete.setText("Remove ALL settings!");
    buttonDelete.setToolTipText("Deletes ALL settings!");
    buttonDelete.addActionListener(this);

    panelButton.add(buttonDelete);

    buttonImport.setText("Import");
    buttonImport.addActionListener(this);

    panelButton.add(buttonImport);

    buttonExport.setText("Export");
    buttonExport.addActionListener(this);

    panelButton.add(buttonExport);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    add(panelButton, gridBagConstraints);

    panelBeforeAfter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

    labelBefore.setText("Time Before (m):");
    panelBeforeAfter.add(labelBefore);

    textfieldBefore.setMinimumSize(new java.awt.Dimension(30, 19));
    textfieldBefore.setPreferredSize(new java.awt.Dimension(30, 19));
    textfieldBefore.addFocusListener(this);

    panelBeforeAfter.add(textfieldBefore);

    labelAfter.setText("Time After (m):");
    panelBeforeAfter.add(labelAfter);

    textfieldAfter.setMinimumSize(new java.awt.Dimension(30, 19));
    textfieldAfter.setPreferredSize(new java.awt.Dimension(30, 19));
    textfieldAfter.addFocusListener(this);

    panelBeforeAfter.add(textfieldAfter);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
    add(panelBeforeAfter, gridBagConstraints);

    panelAppPath.setLayout(new java.awt.GridBagLayout());

    buttonSearch.setText("Search");
    buttonSearch.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    panelAppPath.add(buttonSearch, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    panelAppPath.add(labelDVB, gridBagConstraints);

    labelDvbViewerPath.setText("Path and application name of DVBViewer");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    panelAppPath.add(labelDvbViewerPath, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    add(panelAppPath, gridBagConstraints);

  }


  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == buttonGenerate) {
      SettingsPanel.this.onButtonGenerateActionPerformed(evt);
    } else if (evt.getSource() == tvbName) {
      SettingsPanel.this.onComboTvbName(evt);
    } else if (evt.getSource() == dvbName) {
      SettingsPanel.this.onComboDvbName(evt);
    } else if (evt.getSource() == buttonAssign) {
      SettingsPanel.this.onButtonAssign(evt);
    } else if (evt.getSource() == buttonSearch) {
      SettingsPanel.this.onButtonSearch(evt);
    } else if (evt.getSource() == buttonMarker) {
      SettingsPanel.this.onButtonMarker(evt);
    } else if (evt.getSource() == buttonDelete) {
      SettingsPanel.this.onButtonDelete(evt);
    } else if (evt.getSource() == buttonImport) {
      SettingsPanel.this.onButtonImport(evt);
    } else if (evt.getSource() == buttonExport) {
      SettingsPanel.this.onButtonExport(evt);
    }
  }


  public void focusGained(java.awt.event.FocusEvent evt) { /* not used */ }


  public void focusLost(java.awt.event.FocusEvent evt) {
    if (evt.getSource() == textfieldBefore) {
      SettingsPanel.this.onBeforeFocusLost(evt);
    } else if (evt.getSource() == textfieldAfter) {
      SettingsPanel.this.onAfterFocusLost(evt);
    }
  }


  // </editor-fold>//GEN-END:initComponents

  private void onButtonGenerateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_onButtonGenerateActionPerformed
    // create the channel list
    Settings set = Settings.getSettings();
    ChannelGrabber.writeChannelProperties(set.getViewerTimersPath());
    comboListener = false;
    tvbName.removeAllItems();
    dvbName.removeAllItems();
    initData();
  }// GEN-LAST:event_onButtonGenerateActionPerformed


  private void onAfterFocusLost(FocusEvent evt) {// GEN-FIRST:event_onAfterFocusLost
    int i = 0;
    try {
      i = Integer.parseInt(textfieldAfter.getText());
    } catch (Exception e) {
      HelperClass.error(localizer.msg("err_AfterBefore", "Not a number, please type in a number!"));
      textfieldAfter.setText("0");
      textfieldAfter.grabFocus();
    }
    Settings.getSettings().setRecordAfter(i);

  }// GEN-LAST:event_onAfterFocusLost


  private void onBeforeFocusLost(FocusEvent evt) {// GEN-FIRST:event_onBeforeFocusLost
    int i = 0;
    try {
      i = Integer.parseInt(textfieldBefore.getText());
    } catch (Exception e) {
      HelperClass.error(localizer.msg("err_AfterBefore", "Not a number, please type in a number!"));
      textfieldBefore.setText("0");
      textfieldBefore.grabFocus();
    }
    Settings.getSettings().setRecordBefore(i);
  }// GEN-LAST:event_onBeforeFocusLost


  private void onButtonExport(ActionEvent evt) {// GEN-FIRST:event_onButtonExport
    String filename = null;
    JFileChooser chooser = new JFileChooser();
    int returnVal = chooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      OutputStream os = null;
      try {
        filename = chooser.getCurrentDirectory() + FILE_SEP + chooser.getSelectedFile().getName();
        os = new FileOutputStream(filename);
        Settings.getSettings().storeSettings().store(os, "");
      } catch (IOException e) {
        ErrorHandler.handle(localizer.msg("err_export", "Unable to write file {0}",
                            filename), e);
      } finally {
        if (null != os) {
          try {
            os.close();
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not close the file " + filename, e);
          }
        }
      }
    }
  }// GEN-LAST:event_onButtonExport


  private boolean checkSettings(Properties tmp) {
    if (tmp.size() < 3) return false;

    if (!tmp.containsKey("DVBPath")) return false;

    if (!tmp.containsKey("DVBExecute")) return false;

    if (!tmp.containsKey("Mark")) return false;

    return true;
  }


  private void onButtonImport(ActionEvent evt) {// GEN-FIRST:event_onButtonImport
    JFileChooser chooser = new JFileChooser();
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      FileInputStream fi = null;
      String filename = "";
      try {
        Properties tmp = new Properties();
        filename = chooser.getCurrentDirectory() + FILE_SEP + chooser.getSelectedFile().getName();
        fi = new FileInputStream(filename);
        tmp.load(fi);
        if (checkSettings(tmp)) {
          Settings.getSettings().loadSettings(tmp);

          initData();
          reloadAssignmentsPanel();
        } else {
          HelperClass.error(localizer.msg("err_import_format", "File {0} has wrong format",
                  filename));
        }
      } catch (IOException e) {
        HelperClass.error(localizer.msg("err_import_io", "Could not read file {0}, reason: {1}",
                filename, e.getLocalizedMessage()));
      } finally {
        if (null != fi)
        {
          try {
            fi.close();
          } catch (IOException e) {
            // at least we tried it
            logger.log(Level.SEVERE, "Unable to close the properties file "
                    + filename, e);

          }
        }
      }
    }

  }// GEN-LAST:event_onButtonImport


  private void onButtonMarker(ActionEvent evt) {// GEN-FIRST:event_onButtonMarker
    Settings set = Settings.getSettings();
    set.setMarkRecordings(buttonMarker.isSelected());
    if (buttonMarker.isSelected()) {
      if (!set.isValid()) {
        HelperClass.error(localizer.msg("err_missing_DVBPath", "Missing DVBViewer path!"));
        return;
      }

      set.getMarker().mark();
    } else {
      set.getMarker().unmarkAll();
    }
  }// GEN-LAST:event_onButtonMarker


  /**
   * On selection of an dvbName item it sets the DVBName textfield and the
   * ServicePID textfield with the data
   */
  private void onComboDvbName(ActionEvent evt) {// GEN-FIRST:event_onComboDvbName
    if (comboListener) {
      String name = dvbName.getSelectedItem().toString();
      String value = dvbChannels.getProperty(name);
      String ids[] = value.split("\\|");

      textfieldServicePID.setText(ids[0]);
      textfieldDVBName.setText(name);
    }
  }// GEN-LAST:event_onComboDvbName


  /**
   * Sets the Sender textfield with the data selected in the tvbName Combobox
   */
  private void onComboTvbName(ActionEvent evt) {// GEN-FIRST:event_onComboTvbName
    if (comboListener) {
      textfieldSender.setText(tvbName.getSelectedItem().toString());
    }
  }// GEN-LAST:event_onComboTvbName


  private void onButtonSearch(ActionEvent evt) {// GEN-FIRST:event_onButtonSearch

    JFileChooser chooser = new JFileChooser();
    // set the ProgramFiles folder as default, not the user folder
    chooser.setCurrentDirectory(new File(System.getenv("ProgramFiles")));
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      String dvbViewerDir = chooser.getCurrentDirectory() + FILE_SEP;
      String dvbViewerExeName = chooser.getSelectedFile().getName();
      labelDVB.setText(dvbViewerDir + dvbViewerExeName);

      Settings set = Settings.getSettings();
      set.setViewerPath(dvbViewerDir);
      set.setViewerExeName(dvbViewerExeName);
    }
  }// GEN-LAST:event_onButtonSearch


  private void onButtonDelete(ActionEvent evt) {// GEN-FIRST:event_onButtonDelete
    int confirm = JOptionPane.showConfirmDialog(this, localizer.msg("request_delete_all",
            "Really delete ALL?"), localizer.msg("request_delete_title", "Really delete?"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (confirm == 0) {
      Settings.clear();
      labelDVB.setText("");
      reloadAssignmentsPanel();
    }
  }// GEN-LAST:event_onButtonDelete


  private void onButtonAssign(ActionEvent evt) {// GEN-FIRST:event_onButtonAssign
    String tvbrowserName = textfieldSender.getText();
    String dvbviewerName = textfieldDVBName.getText();
    Settings set = Settings.getSettings();
    String value = dvbChannels.getProperty(dvbviewerName);
    String ids[] = value.split("\\|");
    String serviceID = null;
    String audioID = null;
    String tunertype = null;

    switch (ids.length) {
      case 3:
        tunertype = ids[2];
      case 2:
        audioID = ids[1];
      case 1:
        serviceID = ids[0];
    }

    set.addChannel(tvbrowserName, dvbviewerName, serviceID, audioID, tunertype);
    reloadAssignmentsPanel();
  }// GEN-LAST:event_onButtonAssign


  /**
   * Mouse Action in the anzPanel is here catched
   */
  protected void lMouseClicked(MouseEvent evt) {
    // set the background color
    for (int j = 0; j < zuwPanel.getComponentCount(); j++) {
      JLabel jl = (JLabel)zuwPanel.getComponent(j);
      jl.setBackground(new Color(234, 234, 234));
    }

    // highlight the selected element
    JLabel l = (JLabel)evt.getSource();
    l.setBackground(new Color(204, 204, 204));

    // get text and parse
    String s = l.getText().trim();
    String t[] = s.split(" -> ");

    Settings set = Settings.getSettings();

    if (evt.getButton() == MouseEvent.BUTTON1) {
      // on left mousebutton select the values in the comboboxes
      TvbDvbVChannel chnl = set.getChannelByTVBrowserName(t[0]);
      tvbName.setSelectedItem(chnl.getTvBrowserName());
      textfieldSender.setText(chnl.getTvBrowserName());

      dvbName.setSelectedItem(chnl.getDVBChannel().name);
      textfieldDVBName.setText(chnl.getDVBChannel().name);
      textfieldServicePID.setText(chnl.getDVBChannel().serviceID);
    } else if (evt.getButton() == MouseEvent.BUTTON3) {
      // request confirmation for deleting assignment
      int confirm = JOptionPane.showConfirmDialog(this, localizer.msg("request_delete_entry",
              "Really delete entry \"{0}\"?", t[0]), localizer.msg("request_delete_title",
              "Really delete?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
      if (confirm == JOptionPane.YES_OPTION) {
        set.removeChannelByTVBrowserName(t[0]);
        // re initialize list
        reloadAssignmentsPanel();
      }
    }

  }


  /**
   * Displays the settings new
   */
  private void reloadAssignmentsPanel() {
    // clear the panel
    zuwPanel.removeAll();
    Settings set = Settings.getSettings();

    int count = set.getChannelCount();
    if (count > 0) {
      // the layout keeps 10 lines if there are more we need to enhance the
      // layout
      if (count > 10) {
        zuwPanel.setLayout(new GridLayout(count, 0));
      }

      // add the channels
      for (Iterator<TvbDvbVChannel> it = set.getChannelIterator(); it.hasNext();) {
        TvbDvbVChannel chnl = it.next();
        JLabel l = new JLabel();
        l.setBackground(new Color(234, 234, 234));
        l.setText(' ' + chnl.getTvBrowserName() + " -> " + chnl.getDVBChannel().name);
        l.setBorder(new LineBorder(new Color(0, 0, 0)));
        l.setOpaque(true);
        l.setToolTipText(localizer.msg("popup_tooltip", "right mouse button to delete!"));
        l.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            lMouseClicked(evt);
          }
        });
        zuwPanel.add(l);
      }
    }
    repaint();
    revalidate();
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonAssign;

  private javax.swing.JButton buttonDelete;

  private javax.swing.JButton buttonExport;

  private javax.swing.JButton buttonGenerate;

  private javax.swing.JButton buttonImport;

  private javax.swing.JCheckBox buttonMarker;

  private javax.swing.JButton buttonSearch;

  private javax.swing.JComboBox dvbName;

  private javax.swing.JSeparator jSeparator1;

  private javax.swing.JSeparator jSeparator2;

  private javax.swing.JSeparator jSeparator3;

  private javax.swing.JLabel labelAfter;

  private javax.swing.JLabel labelBefore;

  private javax.swing.JLabel labelChannelDvbName;

  private javax.swing.JLabel labelChannelTvbName;

  private javax.swing.JLabel labelChannelsDvb;

  private javax.swing.JLabel labelChannelsTvBrowser;

  private javax.swing.JLabel labelDVB;

  private javax.swing.JLabel labelDvbViewerPath;

  private javax.swing.JLabel labelGenerate;

  private javax.swing.JLabel labelServicePID;

  private javax.swing.JPanel panelAppPath;

  private javax.swing.JPanel panelBeforeAfter;

  private javax.swing.JPanel panelButton;

  private javax.swing.JPanel panelDVBViewer;

  private javax.swing.JPanel panelDisplay;

  private javax.swing.JPanel panelGenerate;

  private javax.swing.JPanel panelTVBrowser;

  private javax.swing.JTextField textfieldAfter;

  private javax.swing.JTextField textfieldBefore;

  private javax.swing.JTextField textfieldDVBName;

  private javax.swing.JTextField textfieldSender;

  private javax.swing.JTextField textfieldServicePID;

  private javax.swing.JComboBox tvbName;

  private javax.swing.JPanel zuwPanel;

  private javax.swing.JScrollPane zuwScroll;
  // End of variables declaration//GEN-END:variables

}
