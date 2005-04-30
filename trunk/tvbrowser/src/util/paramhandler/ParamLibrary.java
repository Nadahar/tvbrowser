/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package util.paramhandler;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Calendar;

import util.misc.TextLineBreakerStringWidth;
import util.ui.Localizer;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * The Default-ParamLibrary. If you want to add new Params or Functions for your
 * Plugin, extend this Plugin and overwrite the public Methods. For an example
 * see the Code in the CapturePlugin
 * 
 * @author bodum
 */
public class ParamLibrary {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParamLibrary.class);

  /** True if an Error occured */
  private boolean mError = false;

  /** The Error */
  private String mErrorString = new String();

  /**
   * Has an Error occured ?
   * 
   * @return true if an error occured
   */
  public boolean hasErrors() {
    return mError;
  }

  /**
   * Set the Error-Boolean
   * 
   * @param errors True, if an error occured
   */
  public void setErrors(boolean errors) {
    mError = errors;
  }

  /**
   * Returns the Error, empty if none occured
   */
  public String getErrorString() {
    return mErrorString;
  }

  /**
   * Set the Error
   * 
   * @param error the Error
   */
  public void setErrorString(String error) {
    mErrorString = error;
  }

  /**
   * Get the possible Keys
   * 
   * @return Array with possible Keys
   */
  public String[] getPossibleKeys() {
    String[] str = { "title", "original_title", "start_day", "start_month", "start_year", "start_hour", "start_minute",
        "end_month", "end_year", "end_day", "end_hour", "end_minute", "length_minutes", "length_sec", "short_info",
        "description", "episode", "original_episode", "channel_name" };
    return str;
  }

  /**
   * Get the description for one Key
   * 
   * @return description for one key
   */
  public String getDescriptionForKey(String key) {
    String translation = mLocalizer.msg("parameter_" + key, "");
    if (translation.startsWith("[ParamLibrary.parameter")) {
      return mLocalizer.msg("noDescription", "No Description available");
    }
    return translation;
  }

  /**
   * Get the List of possible Functions
   * 
   * @return List of possible Functions
   */
  public String[] getPossibleFunctions() {
    String[] str = { "isset", "urlencode", "concat", "clean", "leadingZero", "splitAt" };
    return str;
  }

  /**
   * Get the description for a specific Funtion
   * 
   * @return
   */
  public String getDescriptionForFunctions(String function) {

    String translation = mLocalizer.msg("function_" + function, "");
    if (translation.startsWith("[ParamLibrary.function")) {
      return mLocalizer.msg("noDescription", "No Description available");
    }

    return translation;
  }

  /**
   * Get the String for a key
   * 
   * @param prg Program to use
   * @param key Key to use
   * @return Value of key in prg
   */
  public String getStringForKey(Program prg, String key) {
    if (key.equals("title")) {
      return prg.getTitle();
    } else if (key.equals("original_title")) {
      return removeNull(prg.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
    } else if (key.equals("start_day")) {
      return "" + prg.getDate().getDayOfMonth();
    } else if (key.equals("start_month")) {
      return "" + prg.getDate().getMonth();
    } else if (key.equals("start_year")) {
      return "" + prg.getDate().getYear();
    } else if (key.equals("end_day")) {
      return "" + getEndTimeFieldInProgram(prg, Calendar.DAY_OF_MONTH);
    } else if (key.equals("end_month")) {
      return "" + (getEndTimeFieldInProgram(prg, Calendar.MONTH) + 1);
    } else if (key.equals("end_year")) {
      return "" + getEndTimeFieldInProgram(prg, Calendar.YEAR);
    } else if (key.equals("start_hour")) {
      return "" + prg.getHours();
    } else if (key.equals("start_minute")) {
      return "" + prg.getMinutes();
    } else if (key.equals("end_hour")) {
      return "" + getEndTimeFieldInProgram(prg, Calendar.HOUR_OF_DAY);
    } else if (key.equals("end_minute")) {
      return "" + getEndTimeFieldInProgram(prg, Calendar.MINUTE);
    } else if (key.equals("length_minutes")) {
      return "" + prg.getLength();
    } else if (key.equals("length_sec")) {
      return "" + (prg.getLength() * 60);
    } else if (key.equals("short_info")) {
      return removeNull(prg.getShortInfo());
    } else if (key.equals("description")) {
      return removeNull(prg.getDescription() + "\n" + prg.getChannel().getCopyrightNotice());
    } else if (key.equals("episode")) {
      return removeNull(prg.getTextField(ProgramFieldType.EPISODE_TYPE));
    } else if (key.equals("original_episode")) {
      return removeNull(prg.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
    } else if (key.equals("channel_name")) {
      return removeNull(prg.getChannel().getName());
    }

    mError = true;
    mErrorString = mLocalizer.msg("unkownParam", "Unknown Parameter") + ": '" + key + "'";

    return null;
  }

  /**
   * If the string is null, it returns "".
   * 
   * @param str String
   * @return "" if str is null, otherwise str
   */
  private String removeNull(String str) {
    if (str == null) {
      str = "";
    }
    return str;
  }

  /**
   * Returns a Calendar-Field of the End-Time from a Program
   * 
   * @param prg Program
   * @param field Calendar-Field to return
   * @return int-Value
   */
  private int getEndTimeFieldInProgram(Program prg, int field) {
    Calendar c = (Calendar) prg.getDate().getCalendar().clone();

    c.set(Calendar.HOUR_OF_DAY, prg.getHours());
    c.set(Calendar.MINUTE, prg.getMinutes());
    c.add(Calendar.MINUTE, prg.getLength());
    c.set(Calendar.SECOND, 0);
    return c.get(field);
  }

  /**
   * Returns the Value of a function
   * 
   * @param prg Program to use
   * @param function Function to use
   * @param params Params for the Function
   * @return Return-Value of Function
   */
  public String getStringForFunction(Program prg, String function, String[] params) {
    if (function.equals("isset")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = mLocalizer.msg("isset2Params", "isset needs 2 Parameters");
        return null;
      }

      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[0];
      }

      return params[1];
    } else if (function.equals("urlencode")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = mLocalizer.msg("urlencode2Params", "urlencode needs 2 Parameters");
        return null;
      }

      try {
        return URLEncoder.encode(params[0], params[1]);
      } catch (Exception e) {
        mError = true;
        mErrorString = mLocalizer.msg("urlencodeProblems", "Problems with encoding : ") + e.toString();
        return null;
      }
    } else if (function.equals("concat")) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; i < params.length; i++) {
        buffer.append(params[i]);
      }
      return buffer.toString();
    } else if (function.equals("clean")) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; i < params.length; i++) {
        buffer.append(clean(params[i]));
      }
      return buffer.toString();
    } else if (function.equals("leadingZero")) {
      if (params.length > 2) {
        mError = true;
        mErrorString = mLocalizer.msg("leadingZero2Params", "leadingZero has max. 2 Parameters");
        return null;
      }

      int num = 2;

      if (params.length == 2) {
        try {
          num = Integer.parseInt(params[1]);
        } catch (Exception ex) {
          mError = true;
          mErrorString = mLocalizer.msg("leadingZeroProblems", "Could not parse Number") + " : " + params[1];
          return null;
        }
      }

      return addLeadingZeros(params[0], num);
    } else if (function.equals("splitAt")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = mLocalizer.msg("splitAt2Params", "splitAt needs 2 Parameters");
        return null;
      }

      int num = 2;

      try {
        num = Integer.parseInt(params[1]);
      } catch (Exception ex) {
        mError = true;
        mErrorString = mLocalizer.msg("splitAtNumberProblems", "Could not parse Number") + " : " + params[1];
        return null;
      }
      
      TextLineBreakerStringWidth breaker = new TextLineBreakerStringWidth();

      StringBuffer result = new StringBuffer();
      
      try {
        String[] lines = breaker.breakLines(new StringReader(params[0]), num);
        
        for (int i=0;i<lines.length;i++) {
          result.append(lines[i]);
          result.append("\n");
        }
        
      } catch (Exception ex) {
        mError = true;
        mErrorString = mLocalizer.msg("splitAtSplitProblems", "Could not split String") + " :\n " + ex.toString();
        return null;
      }
      
      return result.toString();
    }

    mError = true;
    mErrorString = mLocalizer.msg("unknownFunction", "Unkown function : {0}", function);

    return null;
  }

  /**
   * Adds leading Zeros to the String
   * 
   * @param string
   * @param num
   * @return
   */
  private String addLeadingZeros(String string, int num) {
    StringBuffer buffer = new StringBuffer(string);

    while (buffer.length() < num) {
      buffer.insert(0, '0');
    }

    return buffer.toString();
  }

  /**
   * Clean a String. Replace every non A-Za-z0-9 Char into a "_"
   * 
   * @param clean String to clean
   * @return cleaned String
   */
  private String clean(String clean) {
    StringBuffer buffer = new StringBuffer();
    char[] chars = clean.toCharArray();

    for (int i = 0; i < chars.length; i++) {
      if ((chars[i] >= 'A') && (chars[i] <= 'Z')) {
        buffer.append(chars[i]);
      } else if ((chars[i] >= 'a') && (chars[i] <= 'z')) {
        buffer.append(chars[i]);
      } else if ((chars[i] >= '0') && (chars[i] <= '9')) {
        buffer.append(chars[i]);
      } else {
        buffer.append('_');
      }
    }

    return buffer.toString();
  }

}