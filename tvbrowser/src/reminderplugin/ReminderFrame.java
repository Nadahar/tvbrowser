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

package reminderplugin;

import devplugin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import util.ui.UiUtilities;
import util.ui.ProgramPanel;
import util.io.IOUtilities;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderFrame {

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
    mLocalizer.msg("remind.1440", "Remind me one day before"),
  };

  /**
   * The values for the choosable options how long before a program start the
   * reminder should appear.
   */
  public static final int[] REMIND_VALUE_ARR
    = { -1, 0, 1, 2, 3, 5, 10, 15, 30, 60, 90, 120, 240, 480, 1440 };
  
  /**
   * The frame that shows this reminder. The reminder is shown in a frame if
   * there is no modal dialog open.
   * <p>
   * Is <code>null</code> when the reminder is shown in a dialog.
   */
  private JFrame mFrame;
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


  /**
   * Creates a new instance of ReminderFrame.
   * 
   * @param comp A component in the parent window.
   * @param list The list of all reminders.
   * @param item The reminder to show.
   * @param autoCloseSecs The number seconds to wait before auto-closing the
   *                      window. -1 disables auto-closing.
   * @param iconImage The icon image to use for the reminder frame.
   */
  public ReminderFrame(Component comp, ReminderList list,
    ReminderListItem item, int autoCloseSecs, Image iconImage)
  {
    // Check whether we have to use a frame or dialog
    // Workaround: If there is a modal dialog open a frame is not usable. All
    //             user interaction will be ignored.
    //             -> If there is a modal dialog open, we show this reminder as
    //                dialog, otherwise as frame.
    Window parent = UiUtilities.getBestDialogParent(comp);
    String title = mLocalizer.msg("title", "Reminder");
    if (parent instanceof Dialog) {
      mDialog = new JDialog((Dialog) parent, title);
    } else {
      mFrame = new JFrame(title);
      mFrame.setIconImage(iconImage);
    }
    
    mReminderList = list;
    
    list.remove(item.getProgramItem());
    mProgram = item.getProgram();
    JPanel jcontentPane = new JPanel(new BorderLayout(0,10));
    if (mDialog != null) {
      mDialog.setContentPane(jcontentPane);
    } else {
      mFrame.setContentPane(jcontentPane);
    }
    jcontentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    JPanel progPanel=new JPanel(new BorderLayout(5, 10));

    // text label
    String msg;
    int progMinutesAfterMidnight = mProgram.getHours() * 60 + mProgram.getMinutes();
    Date today = Date.getCurrentDate();
    if (today.compareTo(mProgram.getDate())>=0 && IOUtilities.getMinutesAfterMidnight() > progMinutesAfterMidnight) {
      msg = mLocalizer.msg("alreadyRunning", "Already running");
    }
    else {
      msg = mLocalizer.msg("soonStarts", "Soon starts");
    }
    
    progPanel.add(new JLabel(msg), BorderLayout.NORTH);
    
    JLabel channelLabel=new JLabel(mProgram.getChannel().getName());
    progPanel.add(channelLabel,BorderLayout.EAST);
    
    ProgramPanel panel = new ProgramPanel(mProgram);
    panel.addPluginContextMenuMouseListener(ReminderPlugin.getInstance());
    progPanel.add(panel, BorderLayout.CENTER);
    
    JPanel btnPanel = new JPanel(new BorderLayout(10,0));
    mCloseBtText = mLocalizer.msg("close", "Close");
    mCloseBt = new JButton(mCloseBtText);
    if (mDialog != null) {
      mDialog.getRootPane().setDefaultButton(mCloseBt);
    } else {
      mFrame.getRootPane().setDefaultButton(mCloseBt);
    }
    
    mReminderCB = new JComboBox();
    int i=0;
    while(i<REMIND_VALUE_ARR.length && REMIND_VALUE_ARR[i]<item.getMinutes()) {
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

    getWindow().pack();
    UiUtilities.centerAndShow(getWindow());
  }


  private Window getWindow() {
    if (mDialog != null) {
      return mDialog;
    } else {
      return mFrame;
    }
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
    mCloseBt.setText(mCloseBtText + " (" + mRemainingSecs + ")");
  }
  
  
  
  private void close() {
    int inx = mReminderCB.getSelectedIndex();
    int minutes = REMIND_VALUE_ARR[inx];
    if (minutes != -1) {
      mReminderList.add(mProgram, minutes);
    }
    
    if (mAutoCloseTimer != null) {
      mAutoCloseTimer.stop();
    }

    getWindow().dispose();
  }

}