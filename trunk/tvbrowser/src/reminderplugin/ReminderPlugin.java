/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceoforge.net)
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
import java.io.*;
import java.util.*;
import java.applet.*;
import java.net.URL;
import util.exc.*;
import util.ui.UiUtilities;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderPlugin extends Plugin implements ReminderTimerListener {
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderPlugin. class );
  
  private static ReminderPlugin mInstance;
  
  private ReminderList reminderList= null;
  private Properties settings;
  private Icon icon= null;
  
  
  
  /**
   * Creates a new instance of ReminderPlugin.
   */
  public ReminderPlugin() {
    mInstance = this;
  }
  
  
  
  public static ReminderPlugin getInstance() {
    return mInstance;
  }
  
  
  
  public void timeEvent(ReminderListItem item) {
    if ("true" .equals(settings.getProperty( "usesound" ))) {
      String fName=settings.getProperty( "soundfile" );
      try {
        URL url = new File(fName).toURL();
        AudioClip clip=Applet.newAudioClip(url);
        clip.play();
      } catch (java.net.MalformedURLException exc) {
        String msg = mLocalizer.msg( "error.1" ,"Error loading reminder sound file!\n({0})" , fName, exc);
        ErrorHandler.handle(msg, exc);
      }
    }
    
    if ("true" .equals(settings.getProperty( "usemsgbox" ))) {
      new ReminderFrame(reminderList, item);
    } else {
      reminderList.remove(item);
    }
    if ("true" .equals(settings.getProperty( "useexec" ))) {
      String fName=settings.getProperty( "execfile" );
      try {
        Runtime.getRuntime().exec(fName);
      } catch (IOException exc) {
        String msg = mLocalizer.msg( "error.2" ,"Error executing reminder program!\n({0})" , fName, exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }
  
  
  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "pluginName" ,"Reminder" );
    String desc = mLocalizer.msg( "description" ,"Eine einfache Implementierung einer Erinnerungsfunktion." );
    String author = "Martin Oberhauser (darras@users.sourceforge.net)" ;
    return new PluginInfo(name, desc, author, new Version(1, 1));
    
  }
  
  
  public void loadData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    reminderList = (ReminderList) in.readObject();
    if (reminderList != null) {
      reminderList.removeExpiredItems();
      reminderList.setReminderTimerListener( this );
      
      // mark the programs
      Iterator iter = reminderList.getReminderItems();
      while (iter.hasNext()) {
        ReminderListItem item = (ReminderListItem) iter.next();
        item.getProgram().mark( this );
      }
    }
  }

  
  
  public void storeData(ObjectOutputStream out) throws IOException {
    out.writeObject(reminderList);
  }
  
  
  
  public Properties storeSettings() {
    return settings;
  }
  
  public void loadSettings(Properties settings) {
    // if (this.settings != null) throw new NullPointerException("sdfkg");
    if (settings == null ) {
      settings = new Properties();
    }
    
    this.settings = settings;
  }
  
  public String getContextMenuItemText() {
    return mLocalizer.msg( "contextMenuText" ,"Remind me" );
  }
  
  public String getButtonText() {
    return mLocalizer.msg( "buttonText" ,"Reminder list" );
  }
  
  public devplugin.SettingsTab getSettingsTab() {
    return new ReminderSettingsTab(settings);
  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
    if (program.isExpired()) {
      String msg = mLocalizer.msg("programAlreadyExpired",
        "The program is already expired!");
      JOptionPane.showMessageDialog(parent, msg);
    } else {
      ReminderDialog dlg = new ReminderDialog(parent, program);
      UiUtilities.centerAndShow(dlg);
      if (dlg.getOkPressed()) {
        int minutes = dlg.getReminderMinutes();
        addToReminderList(program, minutes);
      }
      dlg.dispose();
    }
  }
    
    
    
  /**
   * This method is invoked for multiple program execution.
   */
  public void execute(Program[] programArr) {
    // multiple program execution
    int minutes = 3;
    for (int i = 0; i < programArr.length; i++) {
      addToReminderList(programArr[i], minutes);
    }
  }

  

  public void execute() {
    JDialog dlg= new ReminderListDialog(parent, reminderList);
    dlg.setSize(600,350);
    UiUtilities.centerAndShow(dlg);
    dlg.dispose();
  }

  
  
  private void addToReminderList(Program program, int minutes) {
    if (reminderList == null ) {
      reminderList = new ReminderList();
      reminderList.setReminderTimerListener(this);
    }
    
    reminderList.add(program, minutes);
  }
  
  
  
  public String getMarkIconName() { return "reminderplugin/TipOfTheDay16.gif" ; }
  public String getButtonIconName() { return "reminderplugin/TipOfTheDay16.gif" ; }

  public boolean supportMultipleProgramExecution() {
    return true;
  }
  
} 