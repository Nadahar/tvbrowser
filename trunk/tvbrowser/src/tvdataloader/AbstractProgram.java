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

package tvdataloader;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.io.*;

import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import util.io.IOUtilities;

import devplugin.Channel;

/**
 * Represents a program object.
 *
 * @author Martin Oberhauser
 */
public abstract class AbstractProgram implements Serializable, devplugin.Program {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(AbstractProgram.class.getName());
  
  /** Contains all listeners that listen for events from this program. */
  transient EventListenerList mListenerList;
  
  /** Containes all Plugins that mark this program. */
  transient HashSet mMarkedBySet;
  
  /** Contains whether this program is currently on air. */
  transient boolean mOnAir;
  
  
  
  public AbstractProgram() {
    init();
  }
  
  
  
  /**
   * This method handles the deserialization.
   */
  private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    init();
  }
  
  

  /**
   * Initializes the program.
   */
  private void init() {
    mListenerList = new EventListenerList();
    mMarkedBySet = new HashSet();
    mOnAir = false;
  }

  
  
  /**
   * Adds a ChangeListener to the program.
   *
   * @param listener the ChangeListener to add
   * @see #fireStateChanged
   * @see #removeChangeListener
   */
  public void addChangeListener(ChangeListener listener) {
    // TODO: The ProgramPanels to not unregister themselves
    /*
    mLog.info("mListenerList.getListenerCount(): " + mListenerList.getListenerCount());
    if (mListenerList.getListenerCount() != 0) {
      throw new RuntimeException("test");
    }
    */
    
    mListenerList.add(ChangeListener.class, listener);
  }
  
  
  
  /**
   * Removes a ChangeListener from the program.
   *
   * @param listener the ChangeListener to remove
   * @see #fireStateChanged
   * @see #addChangeListener
   */
  public void removeChangeListener(ChangeListener listener) {
    mListenerList.remove(ChangeListener.class, listener);
  }

  
  
  /**
   * Send a ChangeEvent, whose source is this program, to each listener.
   * 
   * @see #addChangeListener
   * @see EventListenerList
   */
  protected void fireStateChanged() {
    Object[] listeners = mListenerList.getListenerList();
    ChangeEvent changeEvent = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i]==ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
      }
    }
  }   



  public final String getTimeString() {
    return getHours()+":"+((getMinutes()<10)?"0":"")+getMinutes();
  }


  public final String getDateString() {
    return getDate().toString();
  }


  public String toString() {
    return "title: "+getTitle()+", time: "+getTimeString()+", date: "+getDateString()+", channel: "+getChannel();
  }


  /**
   * Sets whether this program is marked as "on air".
   */
  public void markAsOnAir(boolean onAir) {
    // avoid unnessesary calls of fireStateChanged()
    // call fireStateChanged() anyway if we are "on air"
    // (for updating the "progress bar" painted by the ProgramPanel)
    if (onAir || (onAir != mOnAir)) {
      mOnAir = onAir;
      fireStateChanged();
    }  
  }

  
  
  /**
   * Gets whether this program is marked as "on air".
   */
  public boolean isOnAir() {
    return mOnAir;
  }
  


  public final void mark(devplugin.Plugin plugin) {
    mMarkedBySet.add(plugin);
    fireStateChanged();
  }



  public final void unmark(devplugin.Plugin plugin) {
    mMarkedBySet.remove(plugin);
    fireStateChanged();
  }



  /**
   * Gets an iterator for all {@link devplugin.Program}s that have marked
   * this program.
   */
  public Iterator getMarkedByIterator() {
    return mMarkedBySet.iterator();
  }

  

  /**
   * Gets whether this program is expired.
   */
  public boolean isExpired() {
    int currentDaysSince1970 = IOUtilities.getDaysSince1970();
    int programDaysSince1970 = getDate().getDaysSince1970();
    
    if (programDaysSince1970 < currentDaysSince1970) {
      return true;
    }
    if (programDaysSince1970 > currentDaysSince1970) {
      return false;
    }
    
    // This program is (or was) today -> We've got to check the time
    int currentMinutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    int programMinutesAfterMidnight = getHours() * 60 + getMinutes() + getLength();
    
    return (programMinutesAfterMidnight < currentMinutesAfterMidnight);
  }
  
}