/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
package captureplugin.drivers.defaultdriver;

import java.util.Calendar;

import util.paramhandler.ParamLibrary;
import util.ui.Localizer;
import devplugin.Program;

/**
 * This Class extends the ParamLibrary with Capture-Plugin-Specific Parameters.  
 * 
 * @author bodum
 */
public class CaptureParamLibrary extends ParamLibrary {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CaptureParamLibrary.class);  
  /** Program-Time Values */
  private ProgramTime mPrgTime;
  /** Configuration */
  private DeviceConfig mConfig;
  
  /** Start-Time */
  private Calendar mEndTime;
  /** End-Time */
  private Calendar mStartTime;
  
  /**
   * Create the ParamLibrary
   * @param config Configuration
   */
  public CaptureParamLibrary(DeviceConfig config) {
    mConfig = config;
  }  
  
  /**
   * Create the ParamLibrary
   * @param config Configuration
   * @param prgTime ProgramTime to use for calculating start/endtime
   */
  public CaptureParamLibrary(DeviceConfig config, ProgramTime prgTime) {
    mConfig = config;
    setProgramTime(prgTime);
  }

  /**
   * Set the ProgramTime 
   * @param prgTime ProgramTime to use for calculating start/endtime
   */
  public void setProgramTime(ProgramTime prgTime) {
    mPrgTime = prgTime;
    mEndTime = Calendar.getInstance();
    mEndTime.setTime(prgTime.getEnd());
    mStartTime = Calendar.getInstance();
    mStartTime.setTime(prgTime.getStart());
  }
  
  /**
   * Get the ProgramTime this ParamLibrary uses to calculate start/endtime
   * @return ProgramTime
   */
  public ProgramTime getProgramTime() {
    return mPrgTime;
  }
  
  /* (non-Javadoc)
   * @see util.paramhandler.ParamLibrary#getDescriptionForKey(java.lang.String)
   */
  public String getDescriptionForKey(String key) {
    String translation = mLocalizer.msg("parameter_" + key, "");
    if (translation.startsWith("[CaptureParamLibrary.parameter")) {
      return super.getDescriptionForKey(key);
    }
    
    return translation;
  }


  /* (non-Javadoc)
   * @see util.paramhandler.ParamLibrary#getPossibleKeys()
   */
  public String[] getPossibleKeys() {
    String[] additionalKeys = {"channel_name_external", "device_username", "device_password"};
    
    return concat(super.getPossibleKeys(), additionalKeys);
  }

  /* (non-Javadoc)
   * @see util.paramhandler.ParamLibrary#getStringForKey(devplugin.Program, java.lang.String)
   */
  public String getStringForKey(Program prg, String key) {
    
    if (key.equals("start_day")) {
      return ""+mStartTime.get(Calendar.DAY_OF_MONTH);
    } else if (key.equals("start_month")) {
      return ""+(mStartTime.get(Calendar.MONTH)+1);
    } else if (key.equals("start_year")) {
      return ""+mStartTime.get(Calendar.YEAR);
    } else if (key.equals("start_hour")) {
      return ""+mStartTime.get(Calendar.HOUR_OF_DAY);
    } else if (key.equals("start_minute")) {
      return ""+mStartTime.get(Calendar.MINUTE);
    } else if (key.equals("end_day")) {
      return ""+mEndTime.get(Calendar.DAY_OF_MONTH);
    } else if (key.equals("end_month")) {
      return ""+(mEndTime.get(Calendar.MONTH)+1);
    } else if (key.equals("end_year")) {
      return ""+mEndTime.get(Calendar.YEAR);
    } else if (key.equals("end_hour")) {
      return ""+mEndTime.get(Calendar.HOUR_OF_DAY);
    } else if (key.equals("end_minute")) {
      return ""+mEndTime.get(Calendar.MINUTE);
    } else if (key.equals("channel_name_external")) {
      return getExternalChannelName(prg);
    } else if (key.equals("device_username")) {
      return getUserName();
    } else if (key.equals("device_password")) {
      return getPassword();
    }
    
    return super.getStringForKey(prg, key);
  }

  /**
   * Get the UserName
   * @return UserName
   */
  private String getUserName() {
    if ((mConfig.getUserName() == null) || (mConfig.getUserName().length() == 0)) {
      setErrors(true);
      setErrorString(mLocalizer.msg("NoUser", "Please specify Username!"));      
      return null;
    }
    
    return mConfig.getUserName();
  }

  /**
   * Get the Password
   * @return Password
   */
  private String getPassword() {
    if ((mConfig.getPassword() == null) || (mConfig.getPassword().length() == 0)) {
      setErrors(true);
      setErrorString(mLocalizer.msg("NoPwd", "Please specify Password!"));      
      return null;
    }
    
    return mConfig.getPassword();
  }
  
  /**
   * Get the external ChannelName 
   * @param prg Program to get the external ChannelName for
   * @return external ChannelName
   */
  private String getExternalChannelName(Program prg) {
    
    if ((mConfig.getChannels().get(prg.getChannel()) == null) || (((String)mConfig.getChannels().get(prg.getChannel())).length() == 0)) {
      setErrors(true);
      setErrorString(mLocalizer.msg("NoExternal", "No external Name exists for channel {0}.", prg.getChannel().getName()));      
      return null;
    }
    
    return (String) mConfig.getChannels().get(prg.getChannel());
  }
  
  /**
   * Concats two String-Arrays
   * @param ar1 Array One 
   * @param ar2 Array Two
   * @return concated Version of the two Arrays
   */
  private String[] concat(String[] ar1, String[] ar2) {
    String[] ar3 = new String[ar1.length+ar2.length];
    System.arraycopy(ar1, 0, ar3, 0, ar1.length);
    System.arraycopy(ar2, 0, ar3, ar1.length, ar2.length);
    return ar3;
  }
}