package tvbrowser.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import util.exc.TvBrowserException;

import devplugin.*;
import devplugin.ChannelDayProgram;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * Searches programs in the TV data that match certain regular expressions.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataSearcher {

  /** The singleton. */
  private static TvDataSearcher mSingleton;
  
  /** The comparator that compares two programs by their start time and date */
  private Comparator mStartTimeComparator;
  
  
  /**
   * Creates a new TvDataSearcher instance
   */
  private TvDataSearcher() {
    // Create an comparator that compares two programs by their start time and date
    mStartTimeComparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        Program prog1 = (Program) o1;
        Program prog2 = (Program) o2;
        
        int dateComp = prog1.getDate().compareTo(prog2.getDate());
        if (dateComp == 0) {
          // Both program are at the same date -> Check the start time
          return prog1.getStartTime() - prog2.getStartTime();
        } else {
          return dateComp;
        }
      }
    };
  }


  /**
   * Gets the singleton
   * 
   * @return The singleton
   */
  public static TvDataSearcher getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataSearcher();
    }
    
    return mSingleton;
  }


  /**
   * Searches the TV data for programs which match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param inTitle Should be searched in the title?
   * @param inText Should be searched in the desription?
   * @param caseSensitive Should the search be case sensitive?
   * @param channels The channels to search in.
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   * @return The matching programs.
   * 
   * @deprecated Use {@link #search(String, boolean, ProgramFieldType[], Date, int, Channel[], boolean)}
   *             instead.
   */
  public Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, Channel[] channels, Date startDate, int nrDays)
    throws TvBrowserException
  {
    ProgramFieldType[] fieldArr = getTitleTextFields(inTitle, inText);
    return search(regex, caseSensitive, fieldArr, startDate, nrDays, channels,
                  false);
  }


  /**
   * Searches the TV data base for programs that match a regular expression.
   * 
   * @param regex The regular expression programs must match to.
   * @param caseSensitive Should the search be case sensitive?
   * @param fieldArr The fields to search in
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @param channels The channels to search in.
   * @param sortByStartTime Should the results be sorted by the start time?
   *        If not, the results will be grouped by date and channel and the
   *        search will be faster.
   * @return The matching programs.
   * @throws TvBrowserException
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   */
  public Program[] search(String regex, boolean caseSensitive,
    ProgramFieldType[] fieldArr, Date startDate, int nrDays, Channel[] channels,
    boolean sortByStartTime)
    throws TvBrowserException
  {
    // Should we search in all channels?
    if (channels == null) {
      channels = Settings.propSubscribedChannels.getChannelArray(false);
    }
    
    // Get the flags for the regex
    int flags = Pattern.DOTALL;
    if (! caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
    }

    // Compile the regular expression
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex, flags);
    }
    catch (PatternSyntaxException exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Syntax error in the regualar expression of the search pattern!", exc);
    }

    if (nrDays < 0) {
      // Search in the past
      startDate = startDate.addDays(nrDays);
      nrDays = Math.abs(nrDays);
    }

    // Perform the actual search
    ArrayList hitList = new ArrayList();
    int lastDayWithData = 0;
    for (int day = 0; day <= nrDays; day++) {
      for (int channelIdx = 0; channelIdx < channels.length; channelIdx++) {
        Channel channel = channels[channelIdx];
        ChannelDayProgram dayProg = TvDataBase.getInstance().getDayProgram(startDate, channel);
        if (dayProg != null) {
          // This day has data -> Remember it
          lastDayWithData = day;
          
          // Search this day program
          for (int i = 0; i < dayProg.getProgramCount(); i++) {
            Program prog = (Program) dayProg.getProgramAt(i);
            boolean matches = matches(pattern, prog, fieldArr);

            if (matches) {
              hitList.add(prog);
            }
          }
        }
      }
      
      // Give up if we did not find data for the last 10 days
      if ((day - lastDayWithData) > 10) {
        break;
      }

      // The next day
      startDate = startDate.addDays(1);
    }

    // Convert the list into an array
    Program[] hitArr = new Program[hitList.size()];
    hitList.toArray(hitArr);
    
    // Sort the array if wanted
    if (sortByStartTime) {
      Arrays.sort(hitArr, mStartTimeComparator);
    }

    // return the result
    return hitArr;
  }


  /**
   * @deprecated use {@link #matches(Pattern, Program, ProgramFieldType[])}
   *             instead. 
   */
  public boolean search(Program prog, Pattern pattern, boolean inTitle,
    boolean inText)
  {
    ProgramFieldType[] fieldArr = getTitleTextFields(inTitle, inText);
    
    return matches(pattern, prog, fieldArr);
  }


  /**
   * Gets the title and text fields.
   * 
   * @param inTitle Get the title fields?
   * @param inText Get the text fields?
   * @return
   */
  private ProgramFieldType[] getTitleTextFields(boolean inTitle, boolean inText)
  {
    if (inTitle && inText) {
      return new ProgramFieldType[] {
        ProgramFieldType.TITLE_TYPE,
        ProgramFieldType.SHORT_DESCRIPTION_TYPE,
        ProgramFieldType.DESCRIPTION_TYPE
      };
    }
    else if (inTitle) {
      return new ProgramFieldType[] { ProgramFieldType.TITLE_TYPE };
    }
    else if (inText) {
      return new ProgramFieldType[] {
        ProgramFieldType.SHORT_DESCRIPTION_TYPE,
        ProgramFieldType.DESCRIPTION_TYPE
      };
    }
    else {
      return new ProgramFieldType[0];
    }
  }

  
  /**
   * Returns whether a regular expression matches to a field of a program.
   * 
   * @param pattern The regular expression
   * @param prog The program to check
   * @param typeArr The fields to check
   * @return Whether the regular expression matches to a field of the program
   */
  public boolean matches(Pattern pattern, Program prog,
    ProgramFieldType[] typeArr)
  {
    if (typeArr == null) {
      // Search in nothing? This won't match...
      return false;
    }
    
    for (int i = 0; i < typeArr.length; i++) {
      // Get the field value as String
      String value = null;
      if (typeArr[i] != null) {
        if (typeArr[i].getFormat() == ProgramFieldType.TEXT_FORMAT) {
          value = prog.getTextField(typeArr[i]);
        }
        else if (typeArr[i].getFormat() == ProgramFieldType.INT_FORMAT) {
          value = prog.getIntFieldAsString(typeArr[i]);
        }
        else if (typeArr[i].getFormat() == ProgramFieldType.TIME_FORMAT) {
          value = prog.getTimeFieldAsString(typeArr[i]);
        }
      }
      
      if (value != null) {
        // Check whether the field matches
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
          return true;
        }
      }
    }
    
    // No match found
    return false;
  }

}
