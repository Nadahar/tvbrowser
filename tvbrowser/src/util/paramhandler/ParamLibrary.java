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

import com.l2fprod.util.StringUtils;

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
   * @return error message
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
        "genre", "start_unix", "end_unix", "custom"};
    return str;
  }

  /**
   * Get the description for one Key
   * @param key
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
   * @param function parameter function
   *
   * @return localized description string
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
   * @param program Program to use
   * @param key Key to use
   * @return Value of key in program
   */
  public String getStringForKey(Program program, String key) {
    if (key.equalsIgnoreCase("title")) {
      return program.getTitle();
    } else if (key.equalsIgnoreCase("original_title")) {
      return removeNull(program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
    } else if (key.equalsIgnoreCase("start_day")) {
      return String.valueOf(program.getDate().getDayOfMonth());
    } else if (key.equalsIgnoreCase("start_month")) {
      return String.valueOf(program.getDate().getMonth());
    } else if (key.equalsIgnoreCase("start_year")) {
      return String.valueOf(program.getDate().getYear());
    } else if (key.equalsIgnoreCase("end_day")) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.DAY_OF_MONTH));
    } else if (key.equalsIgnoreCase("end_month")) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.MONTH) + 1);
    } else if (key.equalsIgnoreCase("end_year")) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.YEAR));
    } else if (key.equalsIgnoreCase("start_hour")) {
      return String.valueOf(program.getHours());
    } else if (key.equalsIgnoreCase("start_minute")) {
      return String.valueOf(program.getMinutes());
    } else if (key.equalsIgnoreCase("end_hour")) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.HOUR_OF_DAY));
    } else if (key.equalsIgnoreCase("end_minute")) {
      return String.valueOf(getEndTimeFieldInProgram(program, Calendar.MINUTE));
    } else if (key.equalsIgnoreCase("length_minutes")) {
      return String.valueOf(program.getLength());
    } else if (key.equalsIgnoreCase("length_sec")) {
      return String.valueOf(program.getLength() * 60);
    } else if (key.equalsIgnoreCase("short_info")) {
      return removeNull(program.getShortInfo());
    } else if (key.equalsIgnoreCase("description")) {
      String res = removeNull(program.getDescription());
      String copyright = program.getChannel().getCopyrightNotice();
	    if (copyright != null) {
        return new StringBuilder(res).append('\n').append(copyright).toString();
      }
      return res;
    } else if (key.equalsIgnoreCase("episode")) {
      return removeNull(program.getTextField(ProgramFieldType.EPISODE_TYPE));
    } else if (key.equalsIgnoreCase("original_episode")) {
      return removeNull(program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
    } else if (key.equalsIgnoreCase("channel_name")) {
      return removeNull(program.getChannel().getName());
    } else if (key.equalsIgnoreCase("url")) {
      return removeNull(program.getTextField(ProgramFieldType.URL_TYPE));
    } else if (key.equalsIgnoreCase("start_day_of_week")) {
      SimpleDateFormat format = new SimpleDateFormat("EEEE");
      return format.format(new java.util.Date(program.getDate().getCalendar().getTimeInMillis()));
    } else if (key.equalsIgnoreCase("start_month_name")) {
      SimpleDateFormat format = new SimpleDateFormat("MMMM");
      return format.format(new java.util.Date(program.getDate().getCalendar().getTimeInMillis()));
    } else if (key.equalsIgnoreCase("start_unix")) {
      return Long.toString(createStartTime(program).getTimeInMillis() / 1000);
    } else if (key.equalsIgnoreCase("end_unix")) {
      return Long.toString(createEndTime(program).getTimeInMillis() / 1000);
    } else if (key.equalsIgnoreCase("custom")) {
      return removeNull(program.getTextField(ProgramFieldType.CUSTOM_TYPE));
    } else if (key.equalsIgnoreCase("genre")) {
      return removeNull(program.getTextField(ProgramFieldType.GENRE_TYPE));
    } else if (key.equalsIgnoreCase("episode_number")) {
      int epNum = program.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
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
    if (function.equalsIgnoreCase("isset")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = mLocalizer.msg("isset2Params", "isset needs 2 Parameters");
        return null;
      }

      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[0];
      }

      return params[1];
    } else if (function.equalsIgnoreCase("testparam")) {
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
    } else if (function.equalsIgnoreCase("urlencode")) {
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
    } else if (function.equalsIgnoreCase("concat")) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(param);
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase("clean")) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(clean(param));
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase("cleanLess")) {
      StringBuilder buffer = new StringBuilder();

      for (String param : params) {
        buffer.append(cleanLess(param));
      }
      return buffer.toString();
    } else if (function.equalsIgnoreCase("leadingZero")) {
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
    } else if (function.equalsIgnoreCase("splitAt")) {
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

        for (String line : lines) {
          result.append(line);
          result.append('\n');
        }

      } catch (Exception ex) {
        mError = true;
        mErrorString = mLocalizer.msg("splitAtSplitProblems", "Could not split String") + " :\n " + ex.toString();
        return null;
      }

      return result.toString().trim();
    } else if (function.equalsIgnoreCase("maxlength")) {
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

    for (char c : chars) {
      if ((c >= 'A') && (c <= 'Z')) {
        buffer.append(c);
      } else if ((c >= 'a') && (c <= 'z')) {
        buffer.append(c);
      } else if ((c >= '0') && (c <= '9')) {
        buffer.append(c);
      } else {
        buffer.append('_');
      }
    }

    String retStr = buffer.toString();

    while (retStr.indexOf("__") >= 0) {
      retStr = StringUtils.replace(retStr, "__", "_");
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

    for (char c : chars) {
      if (Character.isDigit(c) || Character.isLetter(c)) {
        buffer.append(c);
      } else {
        buffer.append('_');
      }
    }

    String retStr = buffer.toString();

    while (retStr.indexOf("__") >= 0) {
      retStr = StringUtils.replace(retStr, "__", "_");
    }

    return retStr;
  }

}