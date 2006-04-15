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

import java.awt.Frame;
import java.awt.Image;
import java.util.Properties;

import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;

public class ReminderTimerListener {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ReminderTimerListener.class );

  private Properties mSettings;
  private ReminderList mReminderList;
  private Frame mParentFrame;

  public ReminderTimerListener(Frame parentFrame, Properties settings, ReminderList reminderList) {
    mParentFrame = parentFrame;
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

    if ("true" .equals(mSettings.getProperty( "usemsgbox" ))) {
      Image iconImage = null;

      if (mParentFrame != null) {
        iconImage = mParentFrame.getIconImage();
      }

      new ReminderFrame(mParentFrame, mReminderList, item,
          getAutoCloseReminderTime(), iconImage);
    } else {
      mReminderList.remove(item.getProgramItem());
      mReminderList.blockProgram(item.getProgram());
    }
    if ("true" .equals(mSettings.getProperty( "useexec" ))) {
      String fName=mSettings.getProperty( "execfile" );
      ParamParser parser = new ParamParser();
      String fParam=parser.analyse(mSettings.getProperty("execparam",""), item.getProgram());

      try {
        Runtime.getRuntime().exec(fName + " " +  fParam);
      } catch (Exception exc) {
        String msg = mLocalizer.msg( "error.2" ,"Error executing reminder program!\n({0})" , fName, exc);
        ErrorHandler.handle(msg, exc);
      }
    }

    String[] pluginIds = ReminderPlugin.getInstance().getClientPluginIds();
    
    
    for(int i = 0; i < pluginIds.length; i++) {
      PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(pluginIds[i]);
      if (plugin != null && plugin.canReceivePrograms()) {
        Program[] prArray = { item.getProgram()};
        plugin.receivePrograms(prArray);
      }
    }
  }


  /**
     * Gets the time (in seconds) after which the reminder frame closes
     * automatically.
     */
    private int getAutoCloseReminderTime() {
      int autoCloseReminderTime = 0;
      try {
        String asString = mSettings.getProperty("autoCloseReminderTime", "0");
        autoCloseReminderTime = Integer.parseInt(asString);
      } catch (Exception exc) {
        // ignore
      }
      return autoCloseReminderTime;
    }


}