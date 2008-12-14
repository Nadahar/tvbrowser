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
import java.util.Properties;

import javax.swing.SwingUtilities;

import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.io.IOUtilities;
import util.paramhandler.ParamParser;
import devplugin.Date;
import devplugin.ProgramReceiveIf;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;

public class ReminderTimerListener {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderTimerListener.class );

  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ReminderTimerListener.class.getName());

  private Properties mSettings;
  private ReminderList mReminderList;

  public ReminderTimerListener(Properties settings, ReminderList reminderList) {
    mSettings = settings;
    mReminderList = reminderList;
  }

  public void timeEvent(ReminderListItem item) {
    if (item.getProgramItem().getProgram().isExpired()){
      return;
    }

    if ("true" .equals(mSettings.getProperty( "usesound" ))) {
      ReminderPlugin.playSound(mSettings.getProperty( "soundfile" ));
    }
    if ("true" .equals(mSettings.getProperty( "usebeep" ))) {
      Toolkit.getDefaultToolkit().beep();
    }

    if ("true" .equals(mSettings.getProperty( "usemsgbox" ))) {
      new ReminderFrame(mReminderList, item,
          getAutoCloseReminderTime(item.getProgram()));
    } else {
      mReminderList.removeWithoutChecking(item.getProgramItem());
      mReminderList.blockProgram(item.getProgram());
    }
    if ("true" .equals(mSettings.getProperty( "useexec" ))) {
      String fName=mSettings.getProperty( "execfile","" ).trim();
      ParamParser parser = new ParamParser();
      String fParam=parser.analyse(mSettings.getProperty("execparam",""), item.getProgram());

      if (!fName.equals("")) {
        try {
          ExecutionHandler executionHandler = new ExecutionHandler(fParam, fName);
          executionHandler.execute();
        } catch (Exception exc) {
          String msg = mLocalizer.msg( "error.2" ,"Error executing reminder program!\n({0})" , fName, exc);
          ErrorHandler.handle(msg, exc);
        }
      }
      else {
        mLog.warning("Reminder program name is not defined!");
      }
    }

    ProgramReceiveTarget[] targets = ReminderPlugin.getInstance().getClientPluginsTargets();
    
    for (ProgramReceiveTarget target : targets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && (plugin.canReceiveProgramsWithTarget() || plugin.canReceivePrograms())) {
        Program[] prArray = { item.getProgram()};
        plugin.receivePrograms(prArray, target);
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