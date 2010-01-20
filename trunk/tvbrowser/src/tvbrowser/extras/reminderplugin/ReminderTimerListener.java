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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.extras.reminderplugin;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.io.IOUtilities;
import util.paramhandler.ParamParser;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

public class ReminderTimerListener {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderTimerListener.class );

  private static final Logger mLog = Logger.getLogger(ReminderTimerListener.class.getName());

  private Properties mSettings;
  private ReminderList mReminderList;

  public ReminderTimerListener(Properties settings, ReminderList reminderList) {
    mSettings = settings;
    mReminderList = reminderList;
  }

  public void timeEvent(ArrayList<ReminderListItem> reminders) {
    // filter expired items, just for safety
    ArrayList<ReminderListItem> notExpired = new ArrayList<ReminderListItem>(
        reminders.size());
    for (ReminderListItem item : reminders) {
      if (!item.getProgramItem().getProgram().isExpired()) {
        notExpired.add(item);
      }
    }
    reminders = notExpired;
    if (reminders.isEmpty()) {
      return;
    }

    if ("true" .equals(mSettings.getProperty( "usesound" ))) {
      ReminderPlugin.playSound(mSettings.getProperty( "soundfile" ));
    }
    if ("true" .equals(mSettings.getProperty( "usebeep" ))) {
      Toolkit.getDefaultToolkit().beep();
    }

    if ("true" .equals(mSettings.getProperty( "usemsgbox" ))) {
      new ReminderFrame(mReminderList, reminders,
          getAutoCloseReminderTime(reminders));
    } else {
      for (ReminderListItem reminder : reminders) {
        mReminderList.removeWithoutChecking(reminder.getProgramItem());
        mReminderList.blockProgram(reminder.getProgram());
      }
    }
    if ("true".equals(mSettings.getProperty("useexec"))) {
      String fName = mSettings.getProperty("execfile", "").trim();
      if (!fName.equals("")) {
        for (ReminderListItem reminder : reminders) {
          ParamParser parser = new ParamParser();
          String fParam = parser.analyse(
              mSettings.getProperty("execparam", ""), reminder.getProgram());

          try {
            ExecutionHandler executionHandler = new ExecutionHandler(fParam,
                fName);
            executionHandler.execute();
          } catch (Exception exc) {
            String msg = mLocalizer.msg("error.2",
                "Error executing reminder program!\n({0})", fName, exc);
            ErrorHandler.handle(msg, exc);
          }
        }
      } else {
        mLog.warning("Reminder program name is not defined!");
      }
    }
    
    // send to receiving plugins
    ProgramReceiveTarget[] targets = ReminderPlugin.getInstance()
        .getClientPluginsTargets();

    ArrayList<Program> programs = new ArrayList<Program>();
    for (ReminderListItem reminder : reminders) {
      programs.add(reminder.getProgram());
    }

    for (ProgramReceiveTarget target : targets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
        plugin.receivePrograms(programs.toArray(new Program[programs.size()]),
            target);
      }
    }
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new Thread("Update reminder tree") {
          public void run() {
            setPriority(Thread.MIN_PRIORITY);
            ReminderPlugin.getInstance().updateRootNode(true);
          }
        }.start();
      }
    });
  }


  private int getAutoCloseReminderTime(ArrayList<ReminderListItem> reminders) {
    int result = 0;
    for (ReminderListItem reminder : reminders) {
      result = Math
          .max(result, getAutoCloseReminderTime(reminder.getProgram()));
    }
    return result;
  }

  /**
     * Gets the time (in seconds) after which the reminder frame closes
     * automatically.
     */
    private int getAutoCloseReminderTime(Program p) {
      int autoCloseReminderTime = 0;
      try {        
        if(mSettings.getProperty("autoCloseBehaviour","onEnd").equals("onEnd")) {
          int endTime = p.getStartTime() + p.getLength();
          
          int currentTime = IOUtilities.getMinutesAfterMidnight();
          int dateDiff = p.getDate().compareTo(Date.getCurrentDate()); 
          if (dateDiff == -1) { // program started yesterday
            currentTime += 1440;
          }
          else if (dateDiff == 1) { // program starts the next day
            endTime += 1440;
          }
          autoCloseReminderTime = (endTime - currentTime) * 60;
        }
        else if(mSettings.getProperty("autoCloseBehaviour","onTime").equals("onTime")){
          String asString = mSettings.getProperty("autoCloseReminderTime", "10");
          autoCloseReminderTime = Integer.parseInt(asString);          
        } else {
          autoCloseReminderTime = 0;
        }
      } catch (Exception exc) {
        // ignore
      }
      return autoCloseReminderTime;
    }


}