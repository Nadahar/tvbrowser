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
package tvbrowser.core.data;

import java.lang.ref.SoftReference;
import java.util.logging.Level;

import tvdataservice.MutableProgram;
import devplugin.Channel;
import devplugin.ProgramFieldType;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class OnDemandProgram extends MutableProgram {

  /** The logger for this class. */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(OnDemandProgram.class.getName());

  /**
   * The Object that represents null-values in the cache. Used to distinct
   * null-values from unloaded or forgotten values.
   */  
  private Object NULL_VALUE = "<null>";

  /** The file to load values from, when they are demanded */  
  private OnDemandDayProgramFile mOnDemandFile;
  

  /**
   * Creates a new instance of MutableProgram.
   * <p>
   * The parameters channel, date, hours and minutes build the ID. That's why they
   * are not mutable.
   *
   * @param channel The channel object of this program.
   * @param localDate The date of this program.
   */
  public OnDemandProgram(Channel channel, devplugin.Date localDate,
    OnDemandDayProgramFile onDemandFile)
  {
    super(channel, localDate, true);
    
    mOnDemandFile = onDemandFile;
  }


  public void setLargeField(ProgramFieldType type, long position) {
    OnDemandValue onDemandValue = new OnDemandValue(position);
    
    super.setField(type, type.getFormat(), onDemandValue);
  }


  protected Object getField(ProgramFieldType type, int fieldFormat) {
    Object value = super.getField(type, fieldFormat);
    
    if (value instanceof OnDemandValue) {
      // If the value is from a large field then load it if needed
      OnDemandValue onDemandValue = (OnDemandValue) value;
      return onDemandValue.getValue(type);
    } else {
      return value;
    }
  }


  private class OnDemandValue {
    
    private long mPosition;
    private SoftReference<Object> mValue;
    
    
    OnDemandValue(long position) {
      mPosition = position;
    }
    
    
    Object getValue(ProgramFieldType type) {
      // Try to load the cached value
      Object value = null;
      if (mValue != null) {
        value = mValue.get();
      }
      
      // Load the value from disk if necessary
      if (value == null) {
        try {
          value = mOnDemandFile.loadFieldValue(mPosition, type);
          
          if (value == null) {
            value = NULL_VALUE;
          }
          
          // Put the value into cache
          mValue = new SoftReference<Object>(value);
          
          if (mLog.isLoggable(Level.FINE)) {
            mLog.fine("Loaded value on demand for field " + type.getName());
          }
        }
        catch (Exception exc) {
          mLog.log(Level.WARNING, "Loading value on demand for field "
            + type.getName() + " failed (channel: " + getChannel()
            + ", date: " + getDateString() + ")", exc);
        }
      }
      
      // Return the value
      if (value == NULL_VALUE) {
        return null;
      } else {
        return value;
      }
    }
  
  }

}
