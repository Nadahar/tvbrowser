package primarydatamanager.primarydataservice;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * An abstract implementation of PrimaryDataService that makes developing
 * PrimaryDataServices easier.
 *
 * @author Til Schneider, www.murfman.de
 */
abstract public class AbstractPrimaryDataService implements PrimaryDataService {
  
  /** The stream to print error messages to. */
  private PrintStream mErr;
  
  /** Is true, if there were errors. */
  private boolean mThereWhereErrors;
  
  private Properties mParameters;
  
  /**
   * The TV-Browser channel group 'main'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup MAIN=createChannelGroup("main");

  /**
   * The TV-Browser channel group 'local'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup LOCAL=createChannelGroup("local");

  /**
   * The TV-Browser channel group 'others'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup OTHERS=createChannelGroup("others");

  /**
   * The TV-Browser channel group 'digital'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup DIGITAL=createChannelGroup("digital");

  /**
   * The TV-Browser channel group 'radio'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup RADIO=createChannelGroup("radio");

  /**
   * The TV-Browser channel group 'austria'.
   * <p>
   * If you are a third party developer please use your own channel groups.
   */
  public static final devplugin.ChannelGroup AUSTRIA=createChannelGroup("austria");

  
  /**
   * Creates a channel group.
   * 
   * @param id The ID of the channel group.
   * @return A channel group.
   */
  private static final devplugin.ChannelGroup createChannelGroup(final String id) {
    return new devplugin.ChannelGroupImpl(id, id, "");
  }


  /**
   * Gets the raw TV data and writes it to a directory
   * 
   * @param dir The directory to write the raw TV data to.
   * @param err The stream to print error messages to.
   * @return Whether there were errors.
   */
  public final boolean execute(String dir, PrintStream err) {
    mErr=err;
    mThereWhereErrors=false;
    execute(dir);
    return mThereWhereErrors;
  }
  


  /**
   * Gets the raw TV data and writes it to a directory
   * 
   * @param dir The directory to write the raw TV data to.
   */
  abstract protected void execute(String dir);

  /**
   * Logs an exception.
   * 
   * @param exc The exception to log.
   * @see #logMessage(String)
   */
  public final void logException(Exception exc) {
    exc.printStackTrace(mErr);
    mThereWhereErrors=true;
  }
  
  
  /**
   * Logs an message. Use this only for informations, for errors use
   * {@link #logException(Exception)}.
   * 
   * @param msg
   * @see #logException(Exception)
   */
  public final void logMessage(String msg) {
    mErr.println(msg);
    mThereWhereErrors=true;
  }
  
  /**
   * Sets parameters that might be read by the PDS.
   * 
   * @param parameters
   */
  public final void setParameters(Properties parameters) {
    mParameters=parameters;
  }
  
  /**
   * Gets the parameters if any have been set.
   * 
   * @return The paramaters
   */
  public Properties getParameters() {
    return mParameters;
  }

  
  /**
   * Gets the number of the current week of the year (german: Kalenderwoche).
   * 
   * @param beginOfWeek The first day of the week. Usually either
   *        {@link Calendar#MONDAY} or {@link Calendar#SUNDAY}.
   * @return The current week of the year
   */
  public static int getCurrentWeekOfYear(int beginOfWeek) {
    Calendar cal = Calendar.getInstance();

    return getWeekOfYear(cal,beginOfWeek);
  }


  /**
   * Gets the number of the current week of the year (german: Kalenderwoche).
   * 
   * @param cal The Calendar object to use.
   * @param beginOfWeek The first day of the week. Usually either
   *        {@link Calendar#MONDAY} or {@link Calendar#SUNDAY}.
   * @return The current week of the year
   */
  public static int getWeekOfYear(Calendar cal, int beginOfWeek) {
    int firstDayOfWeek = cal.getFirstDayOfWeek();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

    int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);

    if (firstDayOfWeek!=beginOfWeek) {
      if (dayOfWeek>=firstDayOfWeek && dayOfWeek<beginOfWeek) {
        weekOfYear--;
      }
    }
    
    return weekOfYear;
  }


  /**
   * Searches a number in a String and returns it. E.g. "g 12qp bla 567" will
   * return 12.
   * <p>
   * Warning: Since the return is a int, "12.54" will return 12, not 12.54
   * 
   * @param s The String to search for a number.
   * @return The found number or -1 if there was no number.
   */
  public static int findNumber(String s) {
    if (s==null) {
      return -1;
    }
    char[] str=s.toCharArray();
    int first=0;
    int last;
    while (first<str.length && !Character.isDigit(str[first])) {
      first++;
    }
    last=first;
    while (last<str.length && Character.isDigit(str[last])) {
      last++;
    }
    if (first<str.length) {
      String resStr=s.substring(first,last);
      return Integer.parseInt(resStr);
    }
    return -1;
  }
  
}