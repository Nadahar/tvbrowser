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
  
  private ReminderList mReminderList;
  private Properties mSettings;
  private HashSet mReminderItemsTrash;
  
  
  /**
   * Creates a new instance of ReminderPlugin.
   */
  public ReminderPlugin() {
    mInstance = this;

    mReminderList = new ReminderList();
    mReminderList.setReminderTimerListener(this);
    mReminderItemsTrash = new HashSet();
  }
  
  
  
  public static ReminderPlugin getInstance() {
    return mInstance;
  }
  
  
  
  public void timeEvent(ReminderListItem item) {
    if ("true" .equals(mSettings.getProperty( "usesound" ))) {
      playSound(mSettings.getProperty( "soundfile" ));
    }
    
    if ("true" .equals(mSettings.getProperty( "usemsgbox" ))) {
      new ReminderFrame(getParentFrame(), mReminderList, item,
                        getAutoCloseReminderTime());
    } else {
      mReminderList.remove(item);
    }
    if ("true" .equals(mSettings.getProperty( "useexec" ))) {
      String fName=mSettings.getProperty( "execfile" );
      try {
        Runtime.getRuntime().exec(fName);
      } catch (IOException exc) {
        String msg = mLocalizer.msg( "error.2" ,"Error executing reminder program!\n({0})" , fName, exc);
        ErrorHandler.handle(msg, exc);
      }
    }
  }


  /**
   * Plays a sound.
   *  
   * @param fileName The file name of the sound to play.
   */
  static void playSound(String fileName) {
    try {
      URL url = new File(fileName).toURL();
      AudioClip clip=Applet.newAudioClip(url);
      clip.play();
    } catch (java.net.MalformedURLException exc) {
      String msg = mLocalizer.msg( "error.1",
        "Error loading reminder sound file!\n({0})" , fileName, exc);
      ErrorHandler.handle(msg, exc);
    }
  }


  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "pluginName" ,"Reminder" );
    String desc = mLocalizer.msg( "description" ,"Eine einfache Implementierung einer Erinnerungsfunktion." );
    String author = "Martin Oberhauser (darras@users.sourceforge.net)" ;
    return new PluginInfo(name, desc, author, new Version(1, 5));
  }
  
  
  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
	// Remove from the old list
    mReminderList.setReminderTimerListener(null);
    
    mReminderList = new ReminderList(in);
    mReminderList.setReminderTimerListener(this);
    if (mReminderList != null) {
      mReminderList.removeExpiredItems();
      mReminderList.setReminderTimerListener(this);
      
      // mark the programs
      Iterator iter = mReminderList.getReminderItems();
      while (iter.hasNext()) {
        ReminderListItem item = (ReminderListItem) iter.next();
        item.getProgram().mark( this );
      }
    }
  }

  
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
  	mReminderList.writeData(out);
  }
  
  
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  
  
  public void loadSettings(Properties settings) {
    if (settings == null ) {
      settings = new Properties();
    }
    if (settings.getProperty("usemsgbox")==null) {
      settings.setProperty("usemsgbox","true");
    }
    mSettings = settings;
    
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
    } catch (Exception exc) {}
    return autoCloseReminderTime;
  }
  
  
  
  public String getContextMenuItemText() {
    return mLocalizer.msg( "contextMenuText" ,"Remind me" );
  }
  
  public String getButtonText() {
    return mLocalizer.msg( "buttonText" ,"Reminder list" );
  }
  
  public devplugin.SettingsTab getSettingsTab() {
    return new ReminderSettingsTab(mSettings);
  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
    if (program.isExpired()) {
      String msg = mLocalizer.msg("programAlreadyExpired",
        "The program is already expired!");
      JOptionPane.showMessageDialog(getParentFrame(), msg);
    } else {
            
      ReminderDialog dlg = new ReminderDialog(getParentFrame(), program, mSettings);
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
    JDialog dlg= new ReminderListDialog(getParentFrame(), mReminderList);
    dlg.setSize(600,350);
    UiUtilities.centerAndShow(dlg);
    dlg.dispose();
  }

  
  
  private void addToReminderList(Program program, int minutes) {
    mReminderList.add(program, minutes);
  }
  
  
  
  public String getMarkIconName() { return "reminderplugin/TipOfTheDay16.gif" ; }
  public String getButtonIconName() { return "reminderplugin/TipOfTheDay16.gif" ; }

  
  public boolean supportMultipleProgramExecution() {
    return true;
  }


  /**
   * Removes the deleted programs from the reminder list.
   * <p>
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   */
  public void handleTvDataDeleted(ChannelDayProgram oldProg) {
    
    // Remove the deleted programs from the reminder list
    // and add it to the trash.
    // When the tv listings update is done, we will restore items for programs
    // that were not deleted but updated.
     for (int i = 0; i < oldProg.getProgramCount(); i++) {
      Program prog = oldProg.getProgramAt(i);
      ReminderListItem item = mReminderList.getItemWithProgram(prog);
      if (item != null) {
        mReminderList.remove(prog);
        mReminderItemsTrash.add(item);
      }
    }
  }
  
  public void handleTvDataChanged() {
    
    Iterator it = mReminderItemsTrash.iterator();
    while (it.hasNext()) {
      ReminderListItem trashItem = (ReminderListItem) it.next();
      // We identify programs by ID
      // TODO: identify programs by name and time range (so we won't lose a program if its start time has changed)
      Program p = Plugin.getPluginManager().getProgram(trashItem.getProgram().getDate(), trashItem.getProgram().getID());
      if (p != null) {
        mReminderList.add(trashItem.getProgram(), trashItem.getReminderMinutes());
        p.mark(this);
      }
    }
    mReminderItemsTrash.clear();   
  }
  

} 