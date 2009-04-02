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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import util.misc.TextLineBreakerStringWidth;
import util.ui.Localizer;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * The default ParamLibrary. If you want to add new parameters or functions for your
 * plugin, extend this class and overwrite the public methods. For an example
 * see the code in the CapturePlugin
 * 
 * @author bodum
 */
public class ParamLibrary {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParamLibrary.class);

  /** True if an Error occurred */
  private boolean mError = false;

  /** The Error */
  private String mErrorString = "";

  /**
   * Has an Error occurred ?
   * 
   * @return true if an error occurred
   */
  public boolean hasErrors() {
    return mError;
  }

  /**
   * Set the Error-Boolean
   * 
   * @param errors True, if an error occurred
   */
  public void setErrors(boolean errors) {
    mError = errors;
  }

  /**
   * Returns the Error, empty if none occurred
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
        "description", "episode", "original_episode", "episode_number", "channel_name", "url", "start_day_of_week", "start_month_name",
        "genre", "start_unix", "end_unix"};
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
    String[] str = { "isset", "urlencode", "concat", "clean", "cleanLess", "leadingZero", "splitAt", "testparam", "maxlength" };
    return str;
  }

  /**
   * Get the description for a specific Function
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
   * @return Value of key in program
   */
  public String getStringForKey(Program prg, String key) {
    if (key.equals("title")) {
      return prg.getTitle();
    } else if (key.equals("original_title")) {
      return removeNull(prg.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
    } else if (key.equals("start_day")) {
      return String.valueOf(prg.getDate().getDayOfMonth());
    } else if (key.equals("start_month")) {
      return String.valueOf(prg.getDate().getMonth());
    } else if (key.equals("start_year")) {
      return String.valueOf(prg.getDate().getYear());
    } else if (key.equals("end_day")) {
      return String.valueOf(getEndTimeFieldInProgram(prg, Calendar.DAY_OF_MONTH));
    } else if (key.equals("end_month")) {
      return String.valueOf(getEndTimeFieldInProgram(prg, Calendar.MONTH) + 1);
    } else if (key.equals("end_year")) {
      return String.valueOf(getEndTimeFieldInProgram(prg, Calendar.YEAR));
    } else if (key.equals("start_hour")) {
      return String.valueOf(prg.getHours());
    } else if (key.equals("start_minute")) {
      return String.valueOf(prg.getMinutes());
    } else if (key.equals("end_hour")) {
      return String.valueOf(getEndTimeFieldInProgram(prg, Calendar.HOUR_OF_DAY));
    } else if (key.equals("end_minute")) {
      return String.valueOf(getEndTimeFieldInProgram(prg, Calendar.MINUTE));
    } else if (key.equals("length_minutes")) {
      return String.valueOf(prg.getLength());
    } else if (key.equals("length_sec")) {
      return String.valueOf(prg.getLength() * 60);
    } else if (key.equals("short_info")) {
      return removeNull(prg.getShortInfo());
    } else if (key.equals("description")) {
      String res = removeNull(prg.getDescription());
      String copyright = prg.getChannel().getCopyrightNotice();
	    if (copyright != null) {
        return new StringBuilder(res).append('\n').append(copyright).toString();
      }
      return res;
    } else if (key.equals("episode")) {
      return removeNull(prg.getTextField(ProgramFieldType.EPISODE_TYPE));
    } else if (key.equals("original_episode")) {
      return removeNull(prg.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
    } else if (key.equals("channel_name")) {
      return removeNull(prg.getChannel().getName());
    } else if (key.equals("url")) {
      return removeNull(prg.getTextField(ProgramFieldType.URL_TYPE));
    } else if (key.equals("start_day_of_week")) {
      SimpleDateFormat format = new SimpleDateFormat("EEEE"); 
      return format.format(new java.util.Date(prg.getDate().getCalendar().getTimeInMillis()));    
    } else if (key.equals("start_month_name")) {
      SimpleDateFormat format = new SimpleDateFormat("MMMM"); 
      return format.format(new java.util.Date(prg.getDate().getCalendar().getTimeInMillis()));
    } else if (key.equals("start_unix")) {
      return Long.toString(createStartTime(prg).getTimeInMillis() / 1000);
    } else if (key.equals("end_unix")) {
      return Long.toString(createEndTime(prg).getTimeInMillis() / 1000);
    } else if (key.equals("genre")) {
      return removeNull(prg.getTextField(ProgramFieldType.GENRE_TYPE));
    } else if (key.equals("episode_number")) {
      int epNum = prg.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
      if (epNum == -1) {
        return "";
      }
      return Integer.toString(epNum);
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
    Calendar c = createEndTime(prg);
    return c.get(field);
  }

  /**
   * Creates a calendar instance containing the start time
   *
   * @param prg get Start-Time of this Item
   * @return Start-Time
   */
  private Calendar createStartTime(Program prg) {
    Calendar c = (Calendar) prg.getDate().getCalendar().clone();
    c.set(Calendar.HOUR_OF_DAY, prg.getHours());
    c.set(Calendar.MINUTE, prg.getMinutes());
    c.set(Calendar.SECOND, 0);
    return c;
  }

  /**
   * Creates a calendar instance containing the end time
   *
   * @param prg get End-Time of this Item
   * @return End-Time
   */
  private Calendar createEndTime(Program prg) {
    Calendar c = (Calendar) prg.getDate().getCalendar().clone();

    c.set(Calendar.HOUR_OF_DAY, prg.getHours());
    c.set(Calendar.MINUTE, prg.getMinutes());
    c.add(Calendar.MINUTE, prg.getLength());
    c.set(Calendar.SECOND, 0);
    return c;
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
    } else if (function.equals("testparam")) {
      if ((params.length < 2) || ((params.length > 3))) {
        mError = true;
        mErrorString = mLocalizer.msg("testparam2Params", "testparam needs 2-3 Parameters");
        return null;
      }

      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[1];
      }

      if (params.length == 3) {
        return params[2];
      }
      
      return "";
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
      StringBuilder buffer = new StringBuilder();

      for (int i = 0; i < params.length; i++) {
        buffer.append(params[i]);
      }
      return buffer.toString();
    } else if (function.equals("clean")) {
      StringBuilder buffer = new StringBuilder();

      for (int i = 0; i < params.length; i++) {
        buffer.append(clean(params[i]));
      }
      return buffer.toString();
    } else if (function.equals("cleanLess")) {
      StringBuilder buffer = new StringBuilder();

      for (int i = 0; i < params.length; i++) {
        buffer.append(cleanLess(params[i]));
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

      StringBuilder result = new StringBuilder();
      
      try {
        String[] lines = breaker.breakLines(new StringReader(params[0]), num);
        
        for (int i=0;i<lines.length;i++) {
          result.append(lines[i]);
          result.append('\n');
        }
        
      } catch (Exception ex) {
        mError = true;
        mErrorString = mLocalizer.msg("splitAtSplitProblems", "Could not split String") + " :\n " + ex.toString();
        return null;
      }
      
      return result.toString().trim();
    } else if (function.equals("maxlength")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = mLocalizer.msg("maxlength2Params", "maxlength needs 2 Parameters");
        return null;
      }

      int num = -1;

      try {
        num = Integer.parseInt(params[1]);
      } catch (Exception ex) {
        mError = true;
        mErrorString = mLocalizer.msg("maxlengthNumberProblems", "Could not parse Number") + " : " + params[1];
        return null;
      }

      String result = params[0];

      if (result.length() > num) {
        result = result.substring(0, num);
      }

      return result;
    }

    mError = true;
    mErrorString = mLocalizer.msg("unknownFunction", "Unknown function : {0}", function);

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
    StringBuilder buffer = new StringBuilder(string);

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
    StringBuilder buffer = new StringBuilder();
    char[] chars = clean.trim().toCharArray();

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

    String retStr = buffer.toString();
    
    while (retStr.indexOf("__") >= 0) {
      retStr = retStr.replaceAll("__", "_");
    }
    
    return retStr;
  }

  /**
   * Clean a String. Replace every non non Char/Digit into "_". ÄÖÜ and
   * other Locale Letters will remain.
   * 
   * @param clean String to clean
   * @return cleaned String
   */
  private String cleanLess(String clean) {
    StringBuilder buffer = new StringBuilder();
    char[] chars = clean.trim().toCharArray();

    for (int i = 0; i < chars.length; i++) {
      if (Character.isDigit(chars[i]) || Character.isLetter(chars[i])) {
        buffer.append(chars[i]);
      } else {
        buffer.append('_');
      }
    }

    String retStr = buffer.toString();
    
    while (retStr.indexOf("__") >= 0) {
      retStr = retStr.replaceAll("__", "_");
    }
    
    return retStr;
  }  
  
}