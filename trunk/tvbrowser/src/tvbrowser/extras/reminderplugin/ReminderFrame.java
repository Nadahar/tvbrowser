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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.ui.mainframe.MainFrame;
import util.io.IOUtilities;
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
  public static final String[] REMIND_MSG_ARR = {
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
  };

  /**
   * The values for the choosable options how long before a program start the
   * reminder should appear.
   */
  public static final int[] REMIND_VALUE_ARR
    = { -1, 0, 1, 2, 3, 5, 10, 15, 30, 60, 90, 120, 240, 480, 720, 1440 };
  
  /**
   * The frame that shows this reminder. The reminder is shown in a frame if
   * there is no modal dialog open.
   * <p>
   * Is <code>null</code> when the reminder is shown in a dialog.
   */
/*  private JFrame mFrame;
  /**
   * The dialog that shows this reminder. The reminder is shown in a frame if
   * there is a modal dialog open.
   * <p>
   * Is <code>null</code> when the reminder is shown in a frame.
   */
  private JDialog mDialog;

  private ReminderList mReminderList;
  private Program mProgram;

  private JComboBox mReminderCB;
  private JButton mCloseBt;
  private String mCloseBtText;
  
  private Timer mAutoCloseTimer;
  private int mRemainingSecs;

  private JLabel mHeader;

  /**
   * Creates a new instance of ReminderFrame.
   *
   * @param list The list of all reminders.
   * @param item The reminder to show.
   * @param autoCloseSecs The number seconds to wait before auto-closing the
   *                      window. -1 disables auto-closing.
   */
  public ReminderFrame(ReminderList list,
    ReminderListItem item, int autoCloseSecs)
  {
    // Check whether we have to use a frame or dialog
    // Workaround: If there is a modal dialog open a frame is not usable. All
    //             user interaction will be ignored.
    //             -> If there is a modal dialog open, we show this reminder as
    //                dialog, otherwise as frame.
    Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    String title = mLocalizer.msg("title", "Reminder");
    
    if (parent instanceof JDialog)
      mDialog = new JDialog((JDialog) parent, title);
    else
      mDialog = new JDialog((JFrame) parent, title);
    
    UiUtilities.registerForClosing(this);
    
    mReminderList = list;
    
    list.removeWithoutChecking(item.getProgramItem());
    list.blockProgram(item.getProgram());
    
    mProgram = item.getProgram();    
    
    JPanel jcontentPane = new JPanel(new BorderLayout(0,10));
    mDialog.setContentPane(jcontentPane);
    
    jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    JPanel progPanel=new JPanel(new BorderLayout(5, 10));

    // text label
    String msg;
    int progMinutesAfterMidnight = mProgram.getHours() * 60 + mProgram.getMinutes();
    int remainingMinutes = 0;
    Date today = Date.getCurrentDate();
    if (today.compareTo(mProgram.getDate())>=0 && IOUtilities.getMinutesAfterMidnight() > progMinutesAfterMidnight) {
      msg = mLocalizer.msg("alreadyRunning", "Already running");
    }
    else {
      msg = mLocalizer.msg("soonStarts", "Soon starts");
      remainingMinutes = IOUtilities.getMinutesAfterMidnight() - progMinutesAfterMidnight;
      if (remainingMinutes < 0) {
    	  remainingMinutes = 0;
      }
    }
    
    progPanel.add(mHeader = new JLabel(msg), BorderLayout.NORTH);
    
    JLabel channelLabel=new JLabel(mProgram.getChannel().getName());
    channelLabel.setIcon(UiUtilities.createChannelIcon(mProgram.getChannel().getIcon()));
    channelLabel.setVerticalTextPosition(JLabel.BOTTOM);
    channelLabel.setHorizontalTextPosition(JLabel.CENTER);
    progPanel.add(channelLabel,BorderLayout.EAST);
    
    ProgramPanel panel = new ProgramPanel(mProgram, ProgramPanel.X_AXIS, ReminderPlugin.getInstance().getProgramPanelSettings(false));
    panel.addPluginContextMenuMouseListener(ReminderPluginProxy.getInstance());
    progPanel.add(panel, BorderLayout.CENTER);
    
    JPanel btnPanel = new JPanel(new BorderLayout(10,0));
    mCloseBtText = Localizer.getLocalization(Localizer.I18N_CLOSE);
    mCloseBt = new JButton(mCloseBtText);
    mDialog.getRootPane().setDefaultButton(mCloseBt);
    
    mReminderCB = new JComboBox();
    int i=0;
    while(i<REMIND_VALUE_ARR.length && REMIND_VALUE_ARR[i]<item.getMinutes() && REMIND_VALUE_ARR[i]< remainingMinutes) {
      mReminderCB.addItem(REMIND_MSG_ARR[i]);
      i++;
    }
    
    btnPanel.add(mReminderCB, BorderLayout.WEST);
    btnPanel.add(mCloseBt, BorderLayout.EAST);
    
    jcontentPane.add(progPanel,BorderLayout.NORTH);
    jcontentPane.add(btnPanel,BorderLayout.SOUTH);
    
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        close();
      }
    });
    
    mRemainingSecs = autoCloseSecs;
    if (mRemainingSecs > 0) {
      updateCloseBtText();
      mAutoCloseTimer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          handleTimerEvent();
        }
      });
      mAutoCloseTimer.start();
    }

    mDialog.pack();
    mDialog.setAlwaysOnTop(true);
    
    UiUtilities.centerAndShow(mDialog);
    mDialog.toFront();
    
    mDialog.addWindowFocusListener(new WindowFocusListener() {
      public void windowGainedFocus(WindowEvent e) {}

      public void windowLostFocus(WindowEvent e) {
        mDialog.setAlwaysOnTop(false);
        UiUtilities.getLastModalChildOf(MainFrame.getInstance()).toFront();
      }
    });
    
    mProgram.addChangeListener(this);
  }
  
  
  private void handleTimerEvent() {
    mRemainingSecs--;
    
    if (mRemainingSecs == 0) {
      close();
    } else {
      updateCloseBtText();
    }
  }

  
  
  private void updateCloseBtText() {
    if(mRemainingSecs <= 10 || ReminderPlugin.getInstance().getSettings().getProperty("showTimeCounter","false").compareTo("true") == 0)
      mCloseBt.setText(mCloseBtText + " (" + mRemainingSecs + ")");
  }
  
  
  
  public void close() {
    int inx = mReminderCB.getSelectedIndex();
    int minutes = REMIND_VALUE_ARR[inx];
    if (minutes != -1) {
      mReminderList.add(mProgram, minutes);
      mReminderList.unblockProgram(mProgram);
      ReminderPlugin.getInstance().updateRootNode();
    }
    
    if (mAutoCloseTimer != null) {
      mAutoCloseTimer.stop();
    }

    mDialog.dispose();
  }

  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }
  
  public static String getStringForMinutes(int minutes) {
    for (int i = 0; i < REMIND_VALUE_ARR.length; i++)
      if(REMIND_VALUE_ARR[i] == minutes)
        return REMIND_MSG_ARR[i];
    
    return null;
  }
  
  public static int getValueForMinutes(int minutes) {
    for(int i = 0; i < REMIND_VALUE_ARR.length; i++)
      if(REMIND_VALUE_ARR[i] == minutes)
        return i - 1;
    
    return -1;
  }

  public static int getMinutesForValue(int index) {
    return REMIND_VALUE_ARR[index + 1];
  }
  

  public void stateChanged(ChangeEvent e) {
    if(mProgram.isOnAir())
      mHeader.setText(mLocalizer.msg("alreadyRunning", "Already running"));
    else if(mProgram.isExpired())
      mHeader.setText(mLocalizer.msg("ended", "Program elapsed"));
  }
}