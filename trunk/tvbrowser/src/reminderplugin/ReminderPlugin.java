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


package reminderplugin;

import devplugin.*;
import javax.swing.*;
import java.io.*;

import java.util.*;
import java.applet.*;
import java.net.URL;


public class ReminderPlugin extends Plugin implements ReminderTimerListener {

  private ReminderList reminderList=null;
  private Properties settings;
  private Icon icon=null;


  public void timeEvent(ReminderListItem item) {

    if ("true".equals(settings.getProperty("usesound"))) {
      String fName=settings.getProperty("soundfile");
      try {
        URL url=new URL("file:"+fName);
        AudioClip clip=Applet.newAudioClip(url);
        clip.play();
      }catch(java.net.MalformedURLException e) {
        JOptionPane.showMessageDialog(parent,"Could not open file '"+fName+"'");
        e.printStackTrace();
      }
    }

    if ("true".equals(settings.getProperty("usemsgbox"))) {
      new ReminderFrame(reminderList, item);
    }

    if ("true".equals(settings.getProperty("useexec"))) {
      String fName=settings.getProperty("execfile");
      try {
        Runtime.getRuntime().exec(fName);
      }catch(IOException e) {
        JOptionPane.showMessageDialog(parent,"Could not open '"+fName+"");
        e.printStackTrace();
      }
    }
    
    // remove the item
    if (item.getReminderMinutes() <= 0) {
      item.getProgram().unmark(this);
      reminderList.remove(item);
    }
  }


  public PluginInfo getInfo() {
    return new PluginInfo("Reminder","Eine einfache Implementierung einer Erinnerungsfunktion",
                          "Martin Oberhauser",new Version(1,0));

  }



  public void loadData(ObjectInputStream in) {
    if (in!=null) {
      try {
        reminderList=(ReminderList)in.readObject();
        in.close();
      }catch(ClassNotFoundException e) {
        e.printStackTrace();
      }catch(IOException e) {
        e.printStackTrace();
      }

      reminderList.removeExpiredItems();
      reminderList.setReminderTimerListener(this);
      
      // mark the programs
      Iterator iter = reminderList.getReminderItems();
      while (iter.hasNext()) {
        ReminderListItem item = (ReminderListItem) iter.next();
        item.getProgram().mark(this);
      }
    }
  }



  public Object storeData() {
    return reminderList;
  }

  public Properties storeSettings() {
    return settings;
  }

  public void loadSettings(Properties settings) {
    if (settings==null) {
      settings=new Properties();
    }
    this.settings=settings;
  }

  public String getContextMenuItemText() {
    return "remind me";
  }

  public String getButtonText() {
    return "Reminder";
  }

  public devplugin.SettingsTab getSettingsTab() {
    return new ReminderSettingsTab(settings);
  }

  public void execute(devplugin.Program program) {
util.io.Profiler.getDefault().show("3.1");
    ReminderDialog dlg=new ReminderDialog(parent,program);
util.io.Profiler.getDefault().show("3.2");
    if (dlg.ok()) {
      program.mark(this);
      if (reminderList==null) {
        reminderList=new ReminderList();
      }
      reminderList.add(new ReminderListItem(program,dlg.getReminderSelection()));
    }else {

    }
    dlg.dispose();
  }

  public void execute() {
    JDialog dlg=new ReminderListDialog(parent, reminderList);
    dlg.setSize(600,350);
    dlg.show();
    dlg.dispose();
  }

  public String getMarkIcon() {
    return "reminderplugin/TipOfTheDay16.gif";
  }

}



