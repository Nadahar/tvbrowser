/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package listviewplugin;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Creates a Dialog with a List of Programs
 * 
 * @author bodo
 */
public class ListViewDialog extends JDialog {

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewDialog.class);

  /** The Table */
  private ListTable mProgramTable;

  /** The Table-Model */
  private ListTableModel mModel;

  /** Runs at ... */
  private JRadioButton mRuns = new JRadioButton(mLocalizer.msg("runs", "Running"));

  /** Runs on ... */
  private JRadioButton mOn = new JRadioButton(mLocalizer.msg("on", "On"));

  /** Date-Select for mOn */
  private JComboBox mDate;

  /** Time-Spinner for mOn */
  private JSpinner mTimeSpinner = new JSpinner(new SpinnerDateModel());

  /** Text for mRuns */
  final static String[] TIMETEXT = { mLocalizer.msg("now", "Now"), mLocalizer.msg("15min", "in 15 minutes"),
      mLocalizer.msg("30min", "in 30 minutes") };

  /** Select for mRuns */
  private JComboBox mBox;

  /** Times */
  private int[] mTimes = Settings.propTimeButtons.getIntArray();

  /** Plugin that created the Dialog */
  private Plugin mPlugin;

  /**
   * Creates the Dialog
   * 
   * @param frame Frame for modal
   * @param plugin Plugin for reference
   */
  public ListViewDialog(Frame frame, Plugin plugin) {
    super(frame, true);
    setTitle(mLocalizer.msg("viewList", "View List:"));
    mPlugin = plugin;
    generateList(new Date(), getCurrentTime());
    createGUI();
  }

  /**
   * Returns the current Time in minutes
   * 
   * @return Time in minutes
   */
  private int getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * Genereates the List of Programs
   */
  private void generateList(Date date, int time) {
    mModel = new ListTableModel();

    // If Time > 24 try next Day
    if (time > 60 * 24) {
      date = date.addDays(1);
      time -= 60 * 24;
    }

    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

    for (int i = 0; i < channels.length; i++) {

      Program prg = findProgram(date, time, channels[i]);
      Program nprg = null;

      if (prg == null) {
        prg = findProgram(date.addDays(-1), time + 60 * 24, channels[i]);
      }

      if (prg != null) {
        nprg = findNextProgram(prg);
      } else {
        Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channels[i]);

        if ((it != null) && (it.hasNext())) {
          Program p = (Program) it.next();
          if (p.getStartTime() > time) {
            nprg = p;
          } else {
            nprg = findProgram(date, time + 60, channels[i]);
          }
        } else {

          nprg = findProgram(date, time + 60, channels[i]);
        }

      }

      mModel.addRow(channels[i], prg, nprg);

    }

    if (mProgramTable != null) {
      mProgramTable.setModel(mModel);
      setTableColumProperties();
    }

  }

  /**
   * Finds the program after the given Program
   * 
   * @param prg Search Program after this
   * @return following Program
   */
  private Program findNextProgram(Program prg) {
    Iterator it = Plugin.getPluginManager().getChannelDayProgram(prg.getDate(), prg.getChannel());

    Program nprg = null;
    boolean last = false;

    while ((it != null) && (it.hasNext())) {
      Program p = (Program) it.next();

      if (prg.equals(p) && it.hasNext()) {
        return (Program) it.next();
      } else if (prg.equals(p) && !it.hasNext()) {
        last = true;
      }
    }

    if (last) {
      it = Plugin.getPluginManager().getChannelDayProgram(prg.getDate().addDays(1), prg.getChannel());

      if ((it != null) && (it.hasNext())) {
        Program p = (Program) it.next();

        return p;
      }

    }

    return nprg;
  }

  /**
   * Finds a Program for a Date/time on a certain Channel
   * 
   * @param date Date
   * @param time Time
   * @param channel Channel
   * @return added a Program
   */
  private Program findProgram(Date date, int time, Channel channel) {
    Iterator it = Plugin.getPluginManager().getChannelDayProgram(date, channel);
    while ((it != null) && (it.hasNext())) {
      Program program = (Program) it.next();

      int start = program.getStartTime();
      int ende = program.getStartTime() + program.getLength();

      if ((start <= time) && (ende > time)) {
        return program;
      }
    }
    return null;
  }

  /**
   * Calculates the Time based on the current selection
   * 
   * @param selectedIndex # of selection
   * @return calculated Time
   */
  private int calcTimeForSelection(int selectedIndex) {
    int time = getCurrentTime();

    if (selectedIndex == 1) {
      return time + 15;
    } else if (selectedIndex == 2) {
      return time + 30;
    } else if (selectedIndex > 2) {
      return mTimes[selectedIndex - 3];
    }

    return time;
  }

  /**
   * Creates the GUI
   * 
   * @param plugin
   */
  private void createGUI() {
    JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    content.setBorder(UiUtilities.DIALOG_BORDER);

    Vector data = new Vector();

    data.add(TIMETEXT[0]);
    data.add(TIMETEXT[1]);
    data.add(TIMETEXT[2]);

    for (int i = 0; i < mTimes.length; i++) {
      int h = mTimes[i] / 60;
      int m = mTimes[i] % 60;
      String title = mLocalizer.msg("at", "at") + " " + h + ":" + (m < 10 ? "0" : "") + m;
      data.add(title);
    }

    mBox = new JComboBox(data);

    mBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int time = calcTimeForSelection(mBox.getSelectedIndex());
        generateList(new Date(), time);
      }

    });

    ButtonGroup group = new ButtonGroup();
    group.add(mRuns);
    group.add(mOn);

    JButton refreshButton = new JButton(new ImageIcon(ImageUtilities.createImage("imgs/Refresh16.gif")));

    JPanel datetimeselect = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    Vector dates = new Vector();

    for (int i = 0; i < 14; i++) {
      dates.add(Date.getCurrentDate().addDays(i));
    }
    mDate = new JComboBox(dates);

    datetimeselect.add(mDate);

    datetimeselect.add(new JLabel(" " + mLocalizer.msg("at", "at") + " "));

    String timePattern = mLocalizer.msg("timePattern", "HH:mm");

    mTimeSpinner.setEditor(new JSpinner.DateEditor(mTimeSpinner, timePattern));

    datetimeselect.add(mTimeSpinner);

    // Event-Handler

    mRuns.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        refreshView();
      }

    });

    mOn.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        refreshView();
      }

    });

    mDate.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        refreshView();

      }
    });

    mTimeSpinner.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent arg0) {
        refreshView();

      }
    });

    refreshButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        refreshView();

      }
    });

    mRuns.setSelected(true);
    mDate.setEnabled(false);
    mTimeSpinner.setEnabled(false);

    // Upper Panel

    JPanel topPanel = new JPanel(new GridBagLayout());

    topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridheight = 2;
    c.weighty = 1.0;

    c.insets = new Insets(0, 0, 5, 0);

    topPanel.add(refreshButton, c);

    c.insets = new Insets(0, 5, 5, 0);
    c.gridheight = 1;
    c.weighty = 0;

    topPanel.add(mBox, c);

    c.gridwidth = GridBagConstraints.REMAINDER;

    topPanel.add(mRuns, c);

    c.gridwidth = 1;

    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;

    topPanel.add(datetimeselect, c);

    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    topPanel.add(mOn, c);

    content.add(topPanel, BorderLayout.NORTH);

    // Rest of the GUI
    mProgramTable = new ListTable(mModel);
    mProgramTable.getTableHeader().setReorderingAllowed(false);
    mProgramTable.getTableHeader().setResizingAllowed(false);

    mProgramTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      public void mouseClicked(MouseEvent e) {
        mouseClickedOnTable(e);
      }

    });

    setTableColumProperties();

    JScrollPane scroll = new JScrollPane(mProgramTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    content.add(scroll, BorderLayout.CENTER);

    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 5));
    content.add(buttonPn, BorderLayout.SOUTH);

    JButton closeButton = new JButton(mLocalizer.msg("close", "Close"));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(closeButton);
    getRootPane().setDefaultButton(closeButton);

  }

  /**
   * Sets teh Table-Properties
   */
  private void setTableColumProperties() {
    mProgramTable.getColumnModel().getColumn(0).setCellRenderer(new ListTabelCellRenderer());
    mProgramTable.getColumnModel().getColumn(1).setCellRenderer(new ListTabelCellRenderer());
    mProgramTable.getColumnModel().getColumn(2).setCellRenderer(new ListTabelCellRenderer());
    int width = Settings.propColumnWidth.getInt();
    mProgramTable.getColumnModel().getColumn(2).setMinWidth(width);
    mProgramTable.getColumnModel().getColumn(2).setMinWidth(width);
    mProgramTable.getColumnModel().getColumn(2).setMinWidth(width);

  }

  /**
   * Refresh the List with current settings
   */
  private void refreshView() {

    mBox.setEnabled(mRuns.isSelected());
    mDate.setEnabled(mOn.isSelected());
    mTimeSpinner.setEnabled(mOn.isSelected());

    if (mRuns.isSelected()) {
      int time = calcTimeForSelection(mBox.getSelectedIndex());
      generateList(new Date(), time);
    } else {
      java.util.Date startTime = (java.util.Date) mTimeSpinner.getValue();
      Calendar cal = Calendar.getInstance();
      cal.setTime(startTime);
      int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
      generateList((Date) mDate.getSelectedItem(), minutes);
    }
  }

  /**
   * Called when a Mouse-Event occurs
   * 
   * @param e Event
   */
  private void mouseClickedOnTable(MouseEvent e) {

    Program prg = getProgramByClick(e);
    
    if (prg == null) {
      return;
    }
    
    if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
      devplugin.Plugin.getPluginManager().handleProgramDoubleClick(prg, mPlugin);
    }
  }

  /**
   * Gets the Program the User has clicked on
   * @param e MouseEvent to determine the Program
   * @return Program the User has clicked on
   */
  private Program getProgramByClick(MouseEvent e) {
    int col = mProgramTable.getColumnModel().getColumnIndexAtX(e.getX());
    int row = mProgramTable.rowAtPoint(e.getPoint());
    mProgramTable.setRowSelectionInterval(row, row);

    Program prg = null;

    if (col == 1) {
      prg = mModel.getProgram(row);
    } else if (col == 2) {
      prg = mModel.getNextProgram(row);
    }

    return prg;
  }

  /**
   * Shows the Popup
   * @param evt Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    Program prg = getProgramByClick(e);
    
    if (prg == null) {
      return;
    }
    
    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(prg, mPlugin);
    menu.show(mProgramTable, e.getX() - 15, e.getY() - 15);
  }
}