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
import java.util.Collection;

import util.paramhandler.ParamLibrary;
import util.ui.Localizer;
import captureplugin.drivers.utils.ProgramTime;
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
    
    if (mConfig.useTimeZone()) {
      mEndTime.setTimeZone(mConfig.getTimeZone());
      mStartTime.setTimeZone(mConfig.getTimeZone());
    }
  }
  
  /**
   * Get the ProgramTime this ParamLibrary uses to calculate start/endtime
   * @return ProgramTime
   */
  public ProgramTime getProgramTime() {
    return mPrgTime;
  }
  
  public String getDescriptionForFunctions(String function) {
    String translation = mLocalizer.msg("function_" + function, "", false);
    if (translation.startsWith("[CaptureParamLibrary.function")) {
      return super.getDescriptionForFunctions(function);
    }
    
    return translation;
  }

  public String[] getPossibleFunctions() {
    String[] additionalKeys = {"variable"};
    
    return concat(super.getPossibleFunctions(), additionalKeys);
  }

  public String getStringForFunction(Program prg, String function, String[] params) {
    
    if (function.equals("variable")) {
      if (params.length != 1) {
        setErrors(true);
        setErrorString(mLocalizer.msg("variable_Wrong_Usage", "Wrong usage of command variable. Only one Param is allowed"));
        return null;
      }
      
      try {
        int i = Integer.parseInt(params[0]);
        final Collection<Variable> variables = mConfig.getVariables();
        Variable[] varArray = variables.toArray(new Variable[variables.size()]);
        
        if (varArray.length < i) {
          return "";
        }
        
        return varArray[i-1].getValue();
      } catch (Exception e) {
        setErrors(true);
        setErrorString(mLocalizer.msg("variable_Not_A_Number", "The variable-Command needs a Number."));
        return null;
      }
      
    }
    
    return super.getStringForFunction(prg, function, params);
  }

  public String getDescriptionForKey(String key) {
    String translation = mLocalizer.msg("parameter_" + key, "", false);
    if (translation.startsWith("[CaptureParamLibrary.parameter")) {
      return super.getDescriptionForKey(key);
    }
    
    return translation;
  }


  public String[] getPossibleKeys() {
    String[] additionalKeys = {"channel_name_external","channel_name_external_quiet" , "device_username", "device_password"};
    
    return concat(super.getPossibleKeys(), additionalKeys);
  }

  public String getStringForKey(Program prg, String key) {
    
    if (key.equals("title")) {
      return mPrgTime.getTitle();
    } else if (key.equals("start_day")) {
      return Integer.toString(mStartTime.get(Calendar.DAY_OF_MONTH));
    } else if (key.equals("start_month")) {
      return Integer.toString(mStartTime.get(Calendar.MONTH) + 1);
    } else if (key.equals("start_year")) {
      return Integer.toString(mStartTime.get(Calendar.YEAR));
    } else if (key.equals("start_hour")) {
      return Integer.toString(mStartTime.get(Calendar.HOUR_OF_DAY));
    } else if (key.equals("start_minute")) {
      return Integer.toString(mStartTime.get(Calendar.MINUTE));
    } else if (key.equals("end_day")) {
      return Integer.toString(mEndTime.get(Calendar.DAY_OF_MONTH));
    } else if (key.equals("end_month")) {
      return Integer.toString(mEndTime.get(Calendar.MONTH) + 1);
    } else if (key.equals("end_year")) {
      return Integer.toString(mEndTime.get(Calendar.YEAR));
    } else if (key.equals("end_hour")) {
      return Integer.toString(mEndTime.get(Calendar.HOUR_OF_DAY));
    } else if (key.equals("end_minute")) {
      return Integer.toString(mEndTime.get(Calendar.MINUTE));
    } else if (key.equals("channel_name_external")) {
      return getExternalChannelName(prg, true);
    } else if (key.equals("channel_name_external_quiet")) {
      return getExternalChannelName(prg, false);
    } else if (key.equals("device_username")) {
      return getUserName();
    } else if (key.equals("device_password")) {
      return getPassword();
    } else if (key.equals("length_minutes")) {
      int length = (int) ((mEndTime.getTimeInMillis() + 59000) / 60000 - (mStartTime.getTimeInMillis() + 59000) / 60000);
      return Integer.toString(length);
    } else if (key.equals("length_sec")) {
      int length = (int) ((mEndTime.getTimeInMillis() + 59000) / 60000 - (mStartTime.getTimeInMillis() + 59000) / 60000);
      return Integer.toString((length) * 60);
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
   * @param showError If true it returns an Error if the ChannelName is not set, otherwise it returns a empty String
   * @return external ChannelName
   */
  private String getExternalChannelName(Program prg, boolean showError) {
    
    if ((mConfig.getChannels().get(prg.getChannel()) == null) || ((mConfig.getChannels().get(prg.getChannel())).length() == 0)) {

      if (showError) {
        setErrors(true);
        setErrorString(mLocalizer.msg("NoExternal", "No external Name exists for channel {0}.", prg.getChannel().getName()));
        return null;
      } else {
        return "";
      }
    }
    
    return mConfig.getChannels().get(prg.getChannel());
  }
  
  /**
   * concatenates two String-Arrays
   * @param ar1 Array One
   * @param ar2 Array Two
   * @return concatenated Version of the two Arrays
   */
  private String[] concat(String[] ar1, String[] ar2) {
    String[] ar3 = new String[ar1.length+ar2.length];
    System.arraycopy(ar1, 0, ar3, 0, ar1.length);
    System.arraycopy(ar2, 0, ar3, ar1.length, ar2.length);
    return ar3;
  }
}