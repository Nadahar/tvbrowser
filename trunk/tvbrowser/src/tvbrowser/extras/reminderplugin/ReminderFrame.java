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
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.AbstractList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import devplugin.Date;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderFrame implements WindowClosingIf, ChangeListener {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderFrame.class);

  /**
   * The UI texts for the choosable options how long before a program start the
   * reminder should appear.
   */
  static final String[] REMIND_MSG_ARR = {
    mLocalizer.msg("remind.-1", "Don't remind me"),
    mLocalizer.msg("remind.0", "Remind me when the program begins"),
    mLocalizer.msg("remind.1", "Remind me one minute before"),
    mLocalizer.msg("remind.2", "Remind me 2 minutes before"),
    mLocalizer.msg("remind.3", "Remind me 3 minutes before"),
    mLocalizer.msg("remind.5", "Remind me 5 minutes before"),
    mLocalizer.msg("remind.10", "Remind me 10 minutes before"),
    mLocalizer.msg("remind.15", "Remind me 15 minutes before"),
    mLocalizer.msg("remind.30", "Remind me 30 minutes before"),
    mLocalizer.msg("remind.60", "Remind me one hour before"),
    mLocalizer.msg("remind.90", "Remind me 1.5 hours before"),
    mLocalizer.msg("remind.120", "Remind me 2 hours before"),
    mLocalizer.msg("remind.240", "Remind me 4 hours before"),
    mLocalizer.msg("remind.480", "Remind me 8 hours before"),
    mLocalizer.msg("remind.720", "Remind me 12 hours before"),
    mLocalizer.msg("remind.1440", "Remind me one day before"),
    mLocalizer.msg("remind.week", "Remind me one week before"),
  };

  /**
   * The values for the choosable options how long before a program start the
   * reminder should appear.
   */
  static final int[] REMIND_VALUE_ARR
    = { -1, 0, 1, 2, 3, 5, 10, 15, 30, 60,
      90, 120, 240, 480, 720, 1440, 7 * 1440 };
  
  /**
   * The frame that shows this reminder. The reminder is shown in a frame if
   * there is no modal dialog open.
   * <p>
   * Is <code>null</code> when the reminder is shown in a dialog.
   */
  private JDialog mDialog;

  private ReminderList mReminderList;

  private JComboBox mReminderCB;
  private JButton mCloseBt;
  private String mCloseBtText;
  
  private Timer mAutoCloseTimer;
  private int mRemainingSecs;

  private JLabel mHeader;
  private AbstractList<ReminderListItem> mListItem;

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
    // Check whether we have to use a frame or dialog
    // Workaround: If there is a modal dialog open a frame is not usable. All
    //             user interaction will be ignored.
    //             -> If there is a modal dialog open, we show this reminder as
    //                dialog, otherwise as frame.
    final Window parent = UiUtilities.getLastModalChildOf(MainFrame
        .getInstance());
    String title = mLocalizer.msg("title", "Reminder");
    
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
    
    mReminderList = list;
    mListItem = reminders;
    
    final JPanel jcontentPane = new JPanel(new BorderLayout(0, 10));
    mDialog.setContentPane(jcontentPane);
    final JPanel programsPanel = new JPanel(new GridLayout(0, 1));
    
    jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    final JPanel progPanel = new JPanel(new BorderLayout(5, 10));

    int remainingMinutesMax = 0;
    for (ReminderListItem reminder : reminders) {
      Program program = reminder.getProgram();
      mReminderList.blockProgram(program);
      // text label
      String msg;
      final int progMinutesAfterMidnight = program.getStartTime();
      int remainingMinutes = 0;
      final Date today = Date.getCurrentDate();
      if (today.compareTo(program.getDate()) >= 0
          && IOUtilities.getMinutesAfterMidnight() > progMinutesAfterMidnight) {
        msg = updateRunningTime();
      } else {
        msg = mLocalizer.msg("soonStarts", "Soon starts");
        remainingMinutes = ReminderPlugin.getTimeToProgramStart(program);
      }
      remainingMinutesMax = Math.max(remainingMinutesMax, remainingMinutes);
      progPanel.add(mHeader = new JLabel(msg), BorderLayout.NORTH);

      final JPanel channelPanel = new JPanel(new BorderLayout());
      if (program.getLength() > 0) {
        final JLabel endTime = new JLabel(mLocalizer.msg("endTime",
            "until {0}", program.getEndTimeString()));
        channelPanel.add(endTime, BorderLayout.NORTH);
      }
      final JLabel channelLabel = new JLabel(program.getChannel().getName());
      channelLabel.setIcon(UiUtilities.createChannelIcon(program.getChannel()
          .getIcon()));
      channelLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
      channelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      channelPanel.add(channelLabel, BorderLayout.CENTER);
      progPanel.add(channelPanel, BorderLayout.EAST);

      final ProgramPanel panel = new ProgramPanel(program,
          new ProgramPanelSettings(new PluginPictureSettings(
              PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false,
              ProgramPanelSettings.X_AXIS));
      // register panel with tooltip manager
      panel.setToolTipText("");
      panel
          .addPluginContextMenuMouseListener(ReminderPluginProxy.getInstance());
      progPanel.add(panel, BorderLayout.CENTER);

      String comment = reminder.getComment();
      if (comment != null && comment.length() > 0) {
        progPanel.add(new JLabel(comment), BorderLayout.SOUTH);
      }
      programsPanel.add(progPanel);
    }

    // initialize close button with full text, so it can show the countdown later without size problems
    mRemainingSecs = autoCloseSecs;
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
    
    mReminderCB = new JComboBox();
    int i=0;
    while (i < REMIND_VALUE_ARR.length
        && REMIND_VALUE_ARR[i] < remainingMinutesMax) {
      mReminderCB.addItem(REMIND_MSG_ARR[i]);
      i++;
    }
    // don't show reminder selection if it contains only the 
    // entry "don't remind me"
    mReminderCB.setVisible(mReminderCB.getItemCount() > 1);
    
    btnPanel.add(mReminderCB, BorderLayout.WEST);
    btnPanel.add(mCloseBt, BorderLayout.EAST);
    
    jcontentPane.add(programsPanel, BorderLayout.NORTH);
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

    mDialog.pack();
    
    mCloseBt.setText(mCloseBtText);
    mDialog.setAlwaysOnTop(ReminderPlugin.getInstance().getSettings().getProperty("alwaysOnTop","true").equalsIgnoreCase("true"));
    
    UiUtilities.centerAndShow(mDialog);
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
    ReminderListItem reminder = mListItem.get(0);
    Program program = reminder.getProgram();
    if (program.isOnAir()) {
      final int progMinutesAfterMidnight = program.getHours() * 60
          + program.getMinutes();
      int minutesRunning = IOUtilities.getMinutesAfterMidnight() - progMinutesAfterMidnight;
      if (minutesRunning < 0) {
        minutesRunning += 24 * 60;
      }
      if (minutesRunning == 0) {
        msg = mLocalizer.msg("alreadyRunning", "Just started");
      }
      else if (minutesRunning == 1) {
        msg = mLocalizer.msg("alreadyRunningMinute", "Already running {0} minute", minutesRunning);
      }
      else {
        msg = mLocalizer.msg("alreadyRunningMinutes", "Already running {0} minutes", minutesRunning);
      }
    } else if (program.isExpired()) {
      msg = mLocalizer.msg("ended", "Program elapsed");
    }
    else {
      msg = mLocalizer.msg("soonStarts", "Soon starts");
    }

    if (mHeader != null) {
      mHeader.setText(msg);
    }
    return msg;
  }
  
  
  private void handleTimerEvent() {
    mRemainingSecs--;
    
    if (mRemainingSecs == 0) {
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
      if (minutes < 10) {
        builder.append("0");
      }
      builder.append(minutes).append(":");
      seconds = seconds - 60 * minutes;
      if (seconds < 10) {
        builder.append("0");
      }
      builder.append(seconds);
    }
    return builder.append(")").toString();
  }
  
  public void close() {
    final int minutes = REMIND_VALUE_ARR[mReminderCB.getSelectedIndex()];
    for (ReminderListItem reminder : mListItem) {
      mReminderList.removeWithoutChecking(reminder.getProgramItem());
      if (minutes != -1) {
        Program program = reminder.getProgram();
        mReminderList.add(program, new ReminderContent(minutes, reminder
            .getComment()));
        mReminderList.unblockProgram(program);
      }
    }
    
    if (minutes != -1) {
      ReminderPlugin.getInstance().updateRootNode(true);
    }
    
    if (mAutoCloseTimer != null) {
      mAutoCloseTimer.stop();
    }

    mDialog.dispose();
    ReminderListDialog.updateReminderList();
  }

  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }
  
  public static String getStringForMinutes(final int minutes) {
    for (int i = 0; i < REMIND_VALUE_ARR.length; i++) {
      if(REMIND_VALUE_ARR[i] == minutes) {
        return REMIND_MSG_ARR[i];
      }
    }
    
    return null;
  }
  
  public static int getValueForMinutes(final int minutes) {
    for(int i = 0; i < REMIND_VALUE_ARR.length; i++) {
      if(REMIND_VALUE_ARR[i] == minutes) {
        return i - 1;
      }
    }
    
    return -1;
  }

  public static int getMinutesForValue(final int index) {
    return REMIND_VALUE_ARR[index + 1];
  }
  

  public void stateChanged(final ChangeEvent e) {
    updateRunningTime();
  }
}