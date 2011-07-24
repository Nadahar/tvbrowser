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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;
import util.io.IOUtilities;
import util.ui.CaretPositionCorrector;
import util.ui.ChannelLabel;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.TimeFormatter;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsItem;

/**
 * Creates a Dialog with a List of Programs
 *
 * @author bodo
 */
public class ListViewDialog extends JDialog implements WindowClosingIf {

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
  private final static String[] TIMETEXT = { mLocalizer.msg("now", "Now"),
      mLocalizer.msg("15min", "in 15 minutes"),
    mLocalizer.msg("30min", "in 30 minutes") };

  /** Select for mRuns */
  private JComboBox mBox;

  /** channel filter selection */
  private JComboBox mChannels;

  /** Times */
  private int[] mTimes;

  /** Plugin that created the Dialog */
  private Plugin mPlugin;

  /** Timer for Updates */
  private Timer mTimer;

  /** Settings for this Plugin */
  private Properties mSettings;

  private Thread mLeftClickThread;
  private boolean mPerformingSingleClick;

  private Thread mMiddleSingleClickThread;
  private boolean mPerformingMiddleSingleClick;

  private JComboBox mFilterBox;
  private ProgramFilter mCurrentFilter;

	protected int mTimeSelectionIndex;
  
  /**
   * Creates the Dialog
   *
   * @param frame Frame for modal
   * @param plugin Plugin for reference
   * @param settings The settings of the ListViewPlugin
   */
  public ListViewDialog(Frame frame, Plugin plugin, Properties settings) {
    super(frame, true);
    setTitle(mLocalizer.msg("viewList", "View List:"));
    mPlugin = plugin;
    mSettings = settings;
    mTimes = Plugin.getPluginManager().getTvBrowserSettings().getTimeButtonTimes();
    mModel = new ListTableModel();
    mPerformingSingleClick = false;
    
    mCurrentFilter = Plugin.getPluginManager().getFilterManager().getCurrentFilter();
    
    generateList(new Date(), getCurrentTime());
    createGUI();
    addChangeTimer();
    UiUtilities.registerForClosing(this);
  }

  /**
   * Create Change-Thread that updates the Dialog every 10 seconds
   */
  private void addChangeTimer() {
    int delay = 2000;   // delay for 2 sec.
    int period = 2000;  // repeat every 2 secs.
    mTimer = new Timer();

    mTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (mRuns.isSelected()) {
          refreshView();
        }
      }
    }, delay, period);
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
   * Generates the List of Programs
   */
  private void generateList(Date date, int time) {
    // If Time > 24 try next Day
    if (time > 60 * 24) {
      date = date.addDays(1);
      time -= 60 * 24;
    }

    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
    if ((mChannels != null) && (mChannels.getSelectedIndex() > 0)) {
      FilterComponent component = FilterComponentList.getInstance().getFilterComponentByName(mChannels.getSelectedItem().toString());
      if (component instanceof ChannelFilterComponent) {
        channels = ((ChannelFilterComponent) component).getChannels();
      }
    }

    for (Channel channel : channels) {

      Program prg = findProgram(date, time, channel, false);
      Program nprg = null;

      if (prg == null) {
        prg = findProgram(date.addDays(-1), time + 60 * 24, channel, false);
      }

      if (prg != null) {
        nprg = findNextProgram(prg);
      } else {
        Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel);

        if (it.hasNext()) {
          Program p = it.next();
          
          if (p.getStartTime() > time && mCurrentFilter.accept(p)) {
            nprg = p;
          } else {
            nprg = findProgram(date, time + 60, channel, true);
          }
        } else {
          nprg = findProgram(date, time + 60, channel, true);
        }

        if(nprg == null) {
          it = Plugin.getPluginManager().getChannelDayProgram(date.addDays(1), channel);
          
          while(it.hasNext() && nprg == null) {
            Program p = it.next();
            
            if(!p.isExpired() && mCurrentFilter.accept(p)) {
              nprg = p;
            }
          }
        }
      }
      
      mModel.updateRow(channel, prg, nprg);
    }
  }

  /**
   * Finds the program after the given Program
   *
   * @param prg Search Program after this
   * @return following Program
   */
  private Program findNextProgram(Program prg) {
    Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(prg.getDate(), prg.getChannel());

    Program nprg = null;
    boolean last = false;

    while (it.hasNext()) {
      Program p = it.next();

      if (prg.equals(p) && it.hasNext()) {
        while (it.hasNext()) {
          Program test = it.next();
          
          if(!test.isExpired() && mCurrentFilter.accept(test)) {
            return test;
          }
        }
        
        last = true;
      } else if (prg.equals(p) && !it.hasNext()) {
        last = true;
      }
    }

    if (last) {
      it = Plugin.getPluginManager().getChannelDayProgram(prg.getDate().addDays(1), prg.getChannel());

      while (it.hasNext()) {
        Program p = it.next();
        
        if(!p.isExpired() && mCurrentFilter.accept(p)) {
          return p;
        }
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
  private Program findProgram(Date date, int time, Channel channel, boolean next) {
    for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel); it.hasNext();) {
      Program program = it.next();

      int start = program.getStartTime();
      int ende = program.getStartTime() + program.getLength();
      
      if (((!next && (start <= time) && (ende > time)) || (next && start > IOUtilities.getMinutesAfterMidnight())) && mCurrentFilter.accept(program)) {
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
    } else if (selectedIndex > 2 && selectedIndex < (mBox.getItemCount() - 1)) {
      return mTimes[selectedIndex - 3];
    }

    return time;
  }

  /**
   * Creates the GUI
   */
  private void createGUI() {
    JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    content.setBorder(UiUtilities.DIALOG_BORDER);

    Vector<String> data = new Vector<String>();

    for (int i = 0; i < TIMETEXT.length; i++) {
      data.add(TIMETEXT[i]);
		}
    ArrayList<Integer> separators = new ArrayList<Integer>();
    separators.add(data.size());

    TimeFormatter formatter = new TimeFormatter();

    for (int time : mTimes) {
      int h = time / 60;
      int m = time % 60;
      StringBuilder builder = new StringBuilder();
      builder.append(mLocalizer.msg("at", "at"));
      builder.append(' ');
      builder.append(formatter.formatTime(h, m));
      data.add(builder.toString());
    }
    separators.add(data.size());

    data.add(mLocalizer.ellipsisMsg("configureTimes","Configure Times"));

    mBox = new JComboBox(data);
    UiUtilities.addSeparatorsAfterIndexes(mBox, separators.toArray(new Integer[separators.size()]));

    mBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (mBox.getSelectedIndex() == mBox.getItemCount()-1) {
          mBox.setSelectedIndex(mTimeSelectionIndex);
          Plugin.getPluginManager().showSettings(SettingsItem.TIMEBUTTONS);
        } else {
          mTimeSelectionIndex = mBox.getSelectedIndex();
          int time = calcTimeForSelection(mBox.getSelectedIndex());
          generateList(new Date(), time);
        }
      }

    });

    ButtonGroup group = new ButtonGroup();
    group.add(mRuns);
    group.add(mOn);

    JPanel datetimeselect = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    Vector<Date> dates = new Vector<Date>();

    Date currentDate = Date.getCurrentDate();
    for (int i = 0; i < 14; i++) {
      dates.add(currentDate.addDays(i));
    }

    mDate = new JComboBox(dates);

    datetimeselect.add(mDate);

    datetimeselect.add(new JLabel(" " + mLocalizer.msg("at", "at") + " "));

    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTimeSpinner, Settings.getTimePattern());

    mTimeSpinner.setEditor(dateEditor);

    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);

    datetimeselect.add(mTimeSpinner);

    Vector<String> filters = new Vector<String>();
    filters.add(mLocalizer.msg("filterAll", "all channels"));
    for (String filterName : FilterComponentList.getInstance().getChannelFilterNames()) {
      filters.add(filterName);
    }
    filters.add(mLocalizer.ellipsisMsg("filterDefine", "define filter"));
    mChannels = new JComboBox(filters);
    datetimeselect.add(new JLabel("    "));
    datetimeselect.add(mChannels);

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

    mChannels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // user defined selection
        if (mChannels.getSelectedIndex() == mChannels.getItemCount()-1) {
          EditFilterComponentDlg dlg = new EditFilterComponentDlg((JFrame)null, null, ChannelFilterComponent.class);
          FilterComponent rule = dlg.getFilterComponent();
          if (rule == null) {
            return;
          }
          if (! (rule instanceof ChannelFilterComponent)) {
            return;
          }
          FilterComponentList.getInstance().add(rule);
          FilterComponentList.getInstance().store();
          String filterName = rule.getName();
          mChannels.removeAllItems();
          mChannels.addItem(mLocalizer.msg("filterAll", "all channels"));
          for (String channel : FilterComponentList.getInstance().getChannelFilterNames()) {
            mChannels.addItem(channel);
          }
          mChannels.addItem(mLocalizer.ellipsisMsg("filterDefine", "define filter"));
          mChannels.setSelectedItem(filterName);
        }
        mModel.removeAllRows();
        refreshView();
      }
    });

    mRuns.setSelected(true);
    mDate.setEnabled(false);
    mTimeSpinner.setEnabled(false);

    JLabel filterLabel = new JLabel("Filter:");
    filterLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    mFilterBox = new JComboBox(Plugin.getPluginManager().getFilterManager().getAvailableFilters());
    mFilterBox.setSelectedItem(Plugin.getPluginManager().getFilterManager().getCurrentFilter());
    mFilterBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mCurrentFilter = (ProgramFilter)e.getItem();
          refreshView();
        }
      }
    });
    
    // Upper Panel

    JPanel topPanel = new JPanel(new FormLayout("pref, 3dlu, pref, 15dlu, pref, 3dlu, pref, 3dlu, pref", "pref, 1dlu, pref, 3dlu"));

    CellConstraints cc = new CellConstraints();
    
    topPanel.add(mRuns, cc.xy(1, 1));
    topPanel.add(mBox, cc.xy(3,1));
    topPanel.add(mOn, cc.xy(5,1));
    topPanel.add(datetimeselect, cc.xy(7,1));
    
    topPanel.add(filterLabel, cc.xy(1,3));
    topPanel.add(mFilterBox, cc.xyw(3,3,5));

    content.add(topPanel, BorderLayout.NORTH);

    // Rest of the GUI
    mProgramTable = new ListTable(mModel);
    mProgramTable.getTableHeader().setReorderingAllowed(false);
    mProgramTable.getTableHeader().setResizingAllowed(false);
    mProgramTable.setToolTipText("");

    mProgramTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      @Override
      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        mouseClickedOnTable(e);
      }

    });

    // Dispatch the KeyEvent to the RootPane for Closing the Dialog.
    // Needed for Java 1.4.
    mProgramTable.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        mProgramTable.getRootPane().dispatchEvent(e);
      }
    });

    setTableColumProperties();

    JScrollPane scroll = new JScrollPane(mProgramTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    content.add(scroll, BorderLayout.CENTER);

    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    content.add(buttonPn, BorderLayout.SOUTH);

    JButton closeButton = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });

    JPanel p = new JPanel(new FormLayout("pref,5dlu,pref,5dlu,pref", "pref"));
    JButton settings = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    settings.setToolTipText(mLocalizer.msg("settings","Open settings"));

    settings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
        Plugin.getPluginManager().showSettings(ListViewPlugin.getInstance());
      }
    });

    final JCheckBox showAtStartup = new JCheckBox(mLocalizer.msg("showAtStart", "Show at start"));
    showAtStartup.setSelected(mSettings.getProperty("showAtStartup", "false").equals("true"));

    showAtStartup.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (showAtStartup.isSelected()) {
          mSettings.setProperty("showAtStartup", "true");
        } else {
          mSettings.setProperty("showAtStartup", "false");
        }
      }
    });

    p.add(settings, cc.xy(1, 1));
    p.add(showAtStartup, cc.xy(3, 1));

    buttonPn.add(p, BorderLayout.WEST);

    buttonPn.add(closeButton, BorderLayout.EAST);
    getRootPane().setDefaultButton(closeButton);

  }

  /**
   * Sets the Table-Properties
   */
  private void setTableColumProperties() {
    ListTableCellRenderer renderer = new ListTableCellRenderer(mModel.getRowCount());
    int width = ListViewPlugin.PROGRAMTABLEWIDTH;
    for (int i = 0; i <= 2; i++) {
      TableColumn column = mProgramTable.getColumnModel().getColumn(i);
      column.setCellRenderer(renderer);
      if (i > 0) {
        column.setMinWidth(width);
      }
      else {
      	Channel[] channels = mModel.getChannels();
      	width = UiUtilities.getChannelIconWidth();
      	for (Channel channel : channels) {
					ChannelLabel label = new ChannelLabel(channel);
					label.validate();
					width = Math.max(width, (int)label.getPreferredSize().getWidth());
				}
      	column.setPreferredWidth(width);
      	column.setMaxWidth(250);
      	column.setMinWidth(UiUtilities.getChannelIconWidth());
      }
    }
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
  private void mouseClickedOnTable(final MouseEvent e) {
    final Program prg = getProgramByClick(e);

    if (prg == null) {
      return;
    }
    if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && e.getModifiersEx() == 0) {
      mLeftClickThread = new Thread("Single left click") {
        @Override
        public void run() {
          try {
            mPerformingSingleClick = false;
            sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
            mPerformingSingleClick = true;

            Plugin.getPluginManager().handleProgramSingleClick(prg, mPlugin);
            mPerformingSingleClick = false;
          } catch (InterruptedException e) { // ignore
          }
        }
      };

      mLeftClickThread.setPriority(Thread.MIN_PRIORITY);
      mLeftClickThread.start();
    }
    else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
      if(!mPerformingSingleClick && mLeftClickThread != null && mLeftClickThread.isAlive()) {
        mLeftClickThread.interrupt();
      }
      
      if(!mPerformingSingleClick) {
        devplugin.Plugin.getPluginManager().handleProgramDoubleClick(prg, mPlugin);
      }
    }
    else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
      mMiddleSingleClickThread = new Thread("Single click") {
        @Override
        public void run() {
          try {
            mPerformingMiddleSingleClick = false;
            sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
            mPerformingMiddleSingleClick = true;

            Plugin.getPluginManager().handleProgramMiddleClick(prg, mPlugin);
            mPerformingMiddleSingleClick = false;
          } catch (InterruptedException e) { // ignore
          }
        }
      };

      mMiddleSingleClickThread.setPriority(Thread.MIN_PRIORITY);
      mMiddleSingleClickThread.start();
    }
    else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 2)) {
      if(!mPerformingMiddleSingleClick && mMiddleSingleClickThread != null && mMiddleSingleClickThread.isAlive()) {
        mMiddleSingleClickThread.interrupt();
      }
      
      if(!mPerformingMiddleSingleClick) {
        devplugin.Plugin.getPluginManager().handleProgramMiddleDoubleClick(prg, mPlugin);
      }
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
   * @param e Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    Program prg = getProgramByClick(e);

    if (prg == null) {
      return;
    }

    JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(prg, mPlugin);
    menu.show(mProgramTable, e.getX() - 15, e.getY() - 15);
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    mTimer.cancel();
  }

  public void close() {
    dispose();
  }
}