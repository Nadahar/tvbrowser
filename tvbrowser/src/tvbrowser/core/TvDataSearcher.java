package tvbrowser.core;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import util.exc.TvBrowserException;

import devplugin.ChannelDayProgram;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataSearcher {

  /** The singleton. */
  private static TvDataSearcher mSingleton;
  
  
  private TvDataSearcher() {
  }


  public static TvDataSearcher getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataSearcher();
    }
    
    return mSingleton;
  }


  /**
   * Searches the data for programs which match a regular expression.
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
   */
  public devplugin.Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, devplugin.Channel[] channels,
    devplugin.Date startDate, int nrDays)
    throws TvBrowserException
  {
    int flags = 0;
    if (! caseSensitive) {
      flags |= Pattern.CASE_INSENSITIVE;
    }

    Pattern pattern;
    try {
      pattern = Pattern.compile(regex, flags);
    }
    catch (PatternSyntaxException exc) {
      throw new TvBrowserException(getClass(), "error.8",
        "Syntax error in the regualar expression of the search pattern!", exc);
    }

    if (nrDays < 0) {
      //startDate.addDays(nrDays);
      /*startDate=*/startDate.addDays(nrDays);
      nrDays = 0 - nrDays;
    }

    ArrayList hitList = new ArrayList();
    int missingDataCount = 0;
    for (int day = 0; day <= nrDays; day++) {
      for (int channelIdx = 0; channelIdx < channels.length; channelIdx++) {
        devplugin.Channel channel = channels[channelIdx];
        ChannelDayProgram dayProg = TvDataBase.getInstance().getDayProgram(startDate, channel);
        if (dayProg == null) {
          // Give up if we didn't get data for tenth time
          missingDataCount++;
          if (missingDataCount > 10) {
            // There is no more data -> stop
            day = nrDays;
          }
        } else {
          for (int i = 0; i < dayProg.getProgramCount(); i++) {
            Program prog = (Program) dayProg.getProgramAt(i);
            boolean matches = search(prog, pattern, inTitle, inText);

            if (matches) {
              hitList.add(prog);
            }
          }
        }
      }

      // The next day
      startDate = startDate.addDays(1);
    }

    Program[] hitArr = new Program[hitList.size()];
    hitList.toArray(hitArr);

    return hitArr;
  }


  public boolean search(Program prog, Pattern pattern, boolean inTitle,
    boolean inText)
  {
    boolean matches = false;

    if (inTitle && prog.getTitle()!=null) {
      Matcher matcher = pattern.matcher(prog.getTitle());
      matches = matcher.matches();
    }
  
    if ((! matches) && inText && prog.getDescription()!=null) {
      Matcher matcher = pattern.matcher(prog.getDescription());
      matches = matcher.matches();
    }
  
    return matches;
  }

}
