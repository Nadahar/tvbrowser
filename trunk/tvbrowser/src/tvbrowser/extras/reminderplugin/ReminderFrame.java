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

package tvbrowser.extras.reminderplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.ui.mainframe.MainFrame;
import util.io.IOUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramPanel;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Date;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderFrame implements WindowClosingIf, ChangeListener {
  public static final util.ui.Localizer LOCALIZER
    = util.ui.Localizer.getLocalizerFor(ReminderFrame.class);

  /**
   * The frame that shows this reminder. The reminder is shown in a frame if
   * there is no modal dialog open.
   * <p>
   * Is <code>null</code> when the reminder is shown in a dialog.
   */
  private JDialog mDialog;

  private ReminderList mGlobalReminderList;

  private JComboBox<RemindValue> mReminderCB;
  private JButton mCloseBt;
  private String mCloseBtText;

  private Timer mAutoCloseTimer;
  private int mRemainingSecs;

  /**
   * automatically close when current time is larger than this point in time
   */
  private long mAutoCloseAtMillis;

  /**
   * label showing the textual program state ("has just begun", "already finished")
   */

  private JLabel mHeader;

  /**
   * currently shown reminders
   */
  private AbstractList<ReminderListItem> mReminderItems;

  /**
   * remember layout for hiding expired programs
   */
  private Hashtable<ReminderListItem, Integer[]> mPanelRange = new Hashtable<ReminderListItem, Integer[]>();

  private FormLayout mLayout;

  private Hashtable<ReminderListItem, List<JComponent>> mComponents = new Hashtable<ReminderListItem, List<JComponent>>();

  /**
   * Creates a new instance of ReminderFrame.
   *
   * @param list
   *          The list of all reminders.
   * @param reminders
   *          The reminders to show.
   * @param autoCloseSecs
   *          The number seconds to wait before auto-closing the window. -1
   *          disables auto-closing.
   */
  public ReminderFrame(final ReminderList list,
      final AbstractList<ReminderListItem> reminders,
      final int autoCloseSecs)
  {
    mGlobalReminderList = list;
    mReminderItems = reminders;

    // Check whether we have to use a frame or dialog
    // Workaround: If there is a modal dialog open a frame is not usable. All
    //             user interaction will be ignored.
    //             -> If there is a modal dialog open, we show this reminder as
    //                dialog, otherwise as frame.
    final Window parent = UiUtilities.getLastModalChildOf(MainFrame
        .getInstance());
    String title = LOCALIZER.msg("title", "Reminder");

    // if this is a favorite, change the title to the name of the favorite
    if (reminders.size() == 1) {
      boolean found = false;
      for (Favorite favorite : FavoriteTreeModel.getInstance().getFavoriteArr()) {
        for (Program program : favorite.getPrograms()) {
          if (program.equals(reminders.get(0).getProgram())) {
            title = favorite.getName();
            found = true;
            break;
          }
        }
        if (found) {
          break;
        }
      }
    }

    mDialog = new JDialog(parent, title);
    UiUtilities.registerForClosing(this);

    final JPanel jcontentPane = new JPanel(new BorderLayout(0, 10));
    jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    mDialog.setContentPane(jcontentPane);

    mLayout = new FormLayout("pref:grow,3dlu,pref","pref,3dlu");
    final PanelBuilder programsPanel = new PanelBuilder(mLayout);
    CellConstraints cc = new CellConstraints();

    final Date today = Date.getCurrentDate();
    programsPanel.add(mHeader = new JLabel(""), cc.xyw(1, 1, 3));
    programsPanel.setRow(3);
    int remainingMinutesMax = 0;
    int runningMinutes = 0;
    int maxLength = 0;

    ArrayList<ProgramPanel> panels = new ArrayList<ProgramPanel>(reminders.size());

    for (ReminderListItem reminder : reminders) {
      Program program = reminder.getProgram();
      
      maxLength = Math.max(maxLength,program.getLength());
      
      mGlobalReminderList.blockProgram(program);
      // text label
      String msg;
      final int progMinutesAfterMidnight = program.getStartTime();
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight() + 1440
          * (program.getDate().getNumberOfDaysSince(Date.getCurrentDate()));
      int remainingMinutes = 0;
      
      if(program.isOnAir()) {
        runningMinutes = Math.max(runningMinutes, (minutesAfterMidnight - progMinutesAfterMidnight));
      }
      else if(program.isExpired()) {
        runningMinutes = -1;
      }
      
      if (today.compareTo(program.getDate()) >= 0
          && minutesAfterMidnight > progMinutesAfterMidnight) {
        msg = updateRunningTime();
      } else {
        msg = LOCALIZER.msg("soonStarts", "Soon starts");
        remainingMinutes = ReminderPlugin.getTimeToProgramStart(program);
      }
      
      
      mHeader.setText(msg);
      remainingMinutesMax = Math.max(remainingMinutesMax, remainingMinutes);

      List<JComponent> componentList = new ArrayList<JComponent>();
      mComponents.put(reminder, componentList);

      final ProgramPanel panel = new ProgramPanel(program,
          new ProgramPanelSettings(new PluginPictureSettings(
              PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false,
              ProgramPanelSettings.X_AXIS, false, true, false));
      componentList.add(panel);
      panels.add(panel);
      panel.setMinimumSize(new Dimension(300,50));
      panel.setWidth(300);
      // register panel with tooltip manager
      panel.setToolTipText("");
      panel.addPluginContextMenuMouseListener(ReminderPluginProxy
          .getInstance());

      final JPanel channelPanel = new JPanel(new BorderLayout());
      componentList.add(channelPanel);
      if (program.getLength() > 0) {
        final JLabel endTime = new JLabel(LOCALIZER.msg("endTime",
            "until {0}", program.getEndTimeString()));
        channelPanel.add(endTime, BorderLayout.PAGE_START);
      }
      
      String sortNumber = "";
      
      if(Settings.propShowSortNumberInProgramLists.getBoolean() && program.getChannel().getSortNumber().trim().length() > 0) {
        sortNumber = program.getChannel().getSortNumber() + ". ";
      }
      
      String channelName = sortNumber + program.getChannel().getName();
      JLabel channelLabel = new JLabel();
      channelLabel.setToolTipText(channelName);
      channelLabel.setIcon(UiUtilities.createChannelIcon(program.getChannel()
          .getIcon()));
      channelLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
      channelPanel.add(channelLabel, BorderLayout.CENTER);
      channelLabel = new JLabel(channelName);
      channelPanel.add(channelLabel, BorderLayout.PAGE_END);

      int layoutStartRow = programsPanel.getRowCount();
      mLayout.appendRow(RowSpec.decode("pref"));
      programsPanel.add(panel, cc.xy(1, programsPanel.getRow(), CellConstraints.FILL, CellConstraints.FILL));
      programsPanel.add(channelPanel, cc.xy(3, programsPanel.getRow(), CellConstraints.LEFT, CellConstraints.TOP));
      programsPanel.nextRow();

      String comment = reminder.getComment();
      if (comment != null && comment.length() > 0) {
        mLayout.appendRow(RowSpec.decode("2dlu"));
        mLayout.appendRow(RowSpec.decode("pref"));
        mLayout.appendRow(RowSpec.decode("2dlu"));
        JLabel commentLabel = new JLabel(comment);
        componentList.add(commentLabel);
        programsPanel.add(commentLabel, cc.xyw(1, programsPanel.getRow() + 1, 3));
        programsPanel.nextRow(3);
      }
      int layoutEndRow = programsPanel.getRowCount();
      mPanelRange.put(reminder, new Integer[] {layoutStartRow, layoutEndRow});
    }

    // initialize close button with full text, so it can show the countdown later without size problems
    mRemainingSecs = autoCloseSecs;
    mAutoCloseAtMillis = System.currentTimeMillis() + 1000 * autoCloseSecs;
    final JPanel btnPanel = new JPanel(new BorderLayout(10, 0));
    mCloseBtText = Localizer.getLocalization(Localizer.I18N_CLOSE);
    int seconds = mRemainingSecs;
    if (ReminderPlugin.getInstance().getSettings().getProperty("showTimeCounter","false").compareTo("true") == 0) {
      seconds = 10;
    }
    mCloseBt = new JButton(getCloseButtonText(seconds));
    mDialog.getRootPane().setDefaultButton(mCloseBt);

    for (ReminderListItem reminder : reminders) {
      if (reminder.getMinutes() < remainingMinutesMax) {
        remainingMinutesMax = reminder.getMinutes();
      }
    }
    
    mReminderCB = new JComboBox<>();
    
    int i=0;
    
    while (i < ReminderConstants.REMIND_AFTER_VALUE_ARR.length && runningMinutes-Math.abs(ReminderConstants.REMIND_AFTER_VALUE_ARR[i].getMinutes()) < 0) {
      if(runningMinutes >= 0 && Math.abs(ReminderConstants.REMIND_AFTER_VALUE_ARR[i].getMinutes()) < maxLength) {
        mReminderCB.addItem(ReminderConstants.REMIND_AFTER_VALUE_ARR[i]);
      }
      
      i++;
    }
    
    mReminderCB.addItem(ReminderConstants.DONT_REMIND_AGAIN_VALUE);
    mReminderCB.setSelectedItem(ReminderConstants.DONT_REMIND_AGAIN_VALUE);
    
    i = 0;
    
    while (i < ReminderConstants.REMIND_BEFORE_VALUE_ARR.length
        && ReminderConstants.REMIND_BEFORE_VALUE_ARR[i].getMinutes() < remainingMinutesMax) {
      mReminderCB.addItem(ReminderConstants.REMIND_BEFORE_VALUE_ARR[i++]);
    }
    // don't show reminder selection if it contains only the
    // entry "don't remind me"
    mReminderCB.setVisible(mReminderCB.getItemCount() > 1);

    btnPanel.add(mReminderCB, BorderLayout.WEST);
    btnPanel.add(mCloseBt, BorderLayout.EAST);

    final JScrollPane scrollPane = new JScrollPane(programsPanel.getPanel());
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    jcontentPane.add(scrollPane, BorderLayout.CENTER);
    jcontentPane.add(btnPanel,BorderLayout.SOUTH);

    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent event) {
        close();
      }
    });

    if (mRemainingSecs > 0) {
      updateCloseBtText();
      mAutoCloseTimer = new Timer(1000, new ActionListener() {
        public void actionPerformed(final ActionEvent evt) {
          handleTimerEvent();
        }
      });
      mAutoCloseTimer.start();
    }

    for (ProgramPanel programPanel : panels) {
      programPanel.setMinimumSize(new Dimension(300,50));
    }
    mDialog.pack();
    
    int height = Math.min(mDialog.getHeight(), Toolkit.getDefaultToolkit().getScreenSize().height - 60);
    
    mDialog.setSize(mDialog.getWidth() + scrollPane.getVerticalScrollBar().getPreferredSize().width, height+scrollPane.getHorizontalScrollBar().getPreferredSize().height);
    
    mCloseBt.setText(mCloseBtText);
    mDialog.setAlwaysOnTop(ReminderPlugin.getInstance().getSettings().getProperty("alwaysOnTop","true").equalsIgnoreCase("true"));
    
    int windowLocation = Integer.parseInt(ReminderPropertyDefaults.getPropertyDefaults().getValueFromProperties(ReminderPropertyDefaults.KEY_REMINDER_WINDOW_POSITION, ReminderPlugin.getInstance().getSettings()));
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    switch(windowLocation) {
      case 0: mDialog.setLocation(0, 0);break;
      case 1: mDialog.setLocation(screenSize.width/2-mDialog.getWidth()/2, 0);break;
      case 2: mDialog.setLocation(screenSize.width-mDialog.getWidth(), 0);break;
      case 3: mDialog.setLocation(screenSize.width/4-mDialog.getWidth()/2, screenSize.height/4-mDialog.getHeight()/2);break;
      case 4: mDialog.setLocation(screenSize.width/4*3-mDialog.getWidth()/2, screenSize.height/4-mDialog.getHeight()/2);break;
      case 5: mDialog.setLocation(0, screenSize.height/2-mDialog.getHeight()/2);break;
      case 6: mDialog.setLocation(screenSize.width/2-mDialog.getWidth()/2, screenSize.height/2-mDialog.getHeight()/2);break;
      case 7: mDialog.setLocation(screenSize.width-mDialog.getWidth(), screenSize.height/2-mDialog.getHeight()/2);break;
      case 8: mDialog.setLocation(screenSize.width/4-mDialog.getWidth()/2, screenSize.height/4*3-mDialog.getHeight()/2);break;
      case 9: mDialog.setLocation(screenSize.width/4*3-mDialog.getWidth()/2, screenSize.height/4*3-mDialog.getHeight()/2);break;
      case 10: mDialog.setLocation(0, screenSize.height-mDialog.getHeight());break;
      case 11: mDialog.setLocation(screenSize.width/2-mDialog.getWidth()/2, screenSize.height-mDialog.getHeight());break;
      case 12: mDialog.setLocation(screenSize.width-mDialog.getWidth(), screenSize.height-mDialog.getHeight());break;
    }
    
    mDialog.setVisible(true);
    
    mDialog.toFront();

    if(mDialog.isAlwaysOnTop()) {
      mDialog.addWindowFocusListener(new WindowFocusListener() {
        public void windowGainedFocus(final WindowEvent e) {
        }

        public void windowLostFocus(final WindowEvent e) {
          mDialog.setAlwaysOnTop(false);
          UiUtilities.getLastModalChildOf(MainFrame.getInstance()).toFront();
        }
      });
    }
    
    mDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    mDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(final WindowEvent e) {
        close();
      }
    });

    for (ReminderListItem reminder : reminders) {
      reminder.getProgram().addChangeListener(this);
    }
  }


  private String updateRunningTime() {
    String msg = null;

    // find first still running program, hide all other programs
    ReminderListItem reminder = mReminderItems.get(0);
    for (ReminderListItem r : mReminderItems) {
      if (r.getProgram().isOnAir()) {
        reminder = r;
      }
      else {
        if (r.getProgram().isExpired()) {
          // hide expired programs, first set their layout rows to zero height
          Integer[] range = mPanelRange.get(r);
          for (int i = range[0]; i < range[1]; i++) {
            mLayout.setRowSpec(i, RowSpec.decode("0"));
          }
          // then make the components invisible
          List<JComponent> componentList = mComponents.get(r);
          if (componentList != null) {
            for (JComponent component : componentList) {
              component.setVisible(false);
            }
          }
        }
      }
    }

    Program program = reminder.getProgram();
    if (program.isOnAir()) {
      final int progMinutesAfterMidnight = program.getHours() * 60
          + program.getMinutes();
      int minutesRunning = IOUtilities.getMinutesAfterMidnight() - progMinutesAfterMidnight;
      if (minutesRunning < 0) {
        minutesRunning += 24 * 60;
      }
      if (minutesRunning == 0) {
        msg = LOCALIZER.msg("alreadyRunning", "Just started");
      }
      else if (minutesRunning == 1) {
        msg = LOCALIZER.msg("alreadyRunningMinute", "Already running {0} minute", minutesRunning);
      }
      else {
        msg = LOCALIZER.msg("alreadyRunningMinutes", "Already running {0} minutes", minutesRunning);
      }
    } else if (program.isExpired()) {
      msg = LOCALIZER.msg("ended", "Program elapsed");
    }
    else {
      msg = LOCALIZER.msg("soonStarts", "Soon starts");
    }

    if (mHeader != null) {
      mHeader.setText(msg);
    }
    return msg;
  }

  /**
   * Although this is called once a second, we don't want to count each second
   * individually. If the whole system is under heavy load, or hibernated the
   * gaps between two calls may increase dramatically. The interval of 1000 ms
   * only ensures, that this is not polled more often than once a second.
   */
  private void handleTimerEvent() {
    mRemainingSecs = Math.max(0, (int)(mAutoCloseAtMillis - System.currentTimeMillis()) / 1000);

    if (mRemainingSecs <= 0) {
      close();
    } else {
      updateCloseBtText();
      updateRunningTime();
    }
  }



  private void updateCloseBtText() {
    if(mRemainingSecs <= 10 || ReminderPlugin.getInstance().getSettings().getProperty("showTimeCounter","false").compareTo("true") == 0) {
      mCloseBt.setText(getCloseButtonText(mRemainingSecs));
    }
  }

  private String getCloseButtonText(int seconds) {
    final StringBuilder builder = new StringBuilder(mCloseBtText);
    builder.append(" (");
    if (seconds <= 60) {
      builder.append(seconds);
    }
    else {
      if (seconds >= 3600) {
        final int hours = seconds / 3600;
        builder.append(hours).append(":");
        seconds = seconds - 3600 * hours;
      }
      final int minutes = seconds / 60;
      if (minutes < 10 && minutes > -10) {
        builder.append("0");
      }
      builder.append(minutes).append(":");
      seconds = seconds - 60 * minutes;
      if (seconds < 10 && seconds > -10) {
        builder.append("0");
      }
      builder.append(seconds);
    }
    return builder.append(")").toString();
  }

  public void close() {
    int minutes = ((RemindValue)mReminderCB.getSelectedItem()).getMinutes();
    
    for (ReminderListItem reminder : mReminderItems) {
      mGlobalReminderList.removeWithoutChecking(reminder.getProgramItem());
      if (minutes != ReminderConstants.DONT_REMIND_AGAIN) {
        Program program = reminder.getProgram();
        mGlobalReminderList.add(program, new ReminderContent(minutes, reminder
            .getComment()));
        mGlobalReminderList.unblockProgram(program);
      }
    }

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ReminderPlugin.getInstance().updateRootNode(true);
      }
    });

    if (mAutoCloseTimer != null) {
      mAutoCloseTimer.stop();
    }

    mDialog.dispose();
    ReminderListDialog.updateReminderList();
  }

  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }
  
  public void stateChanged(final ChangeEvent e) {
    updateRunningTime();
  }
}