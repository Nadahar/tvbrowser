/**
 * Created on 27.06.2010
 */
package captureplugin.drivers.topfield;

import util.ui.Localizer;

/**
 * The recording mode of a timer.
 * 
 * @author Wolfgang Reh
 */
public enum TopfieldTimerMode {
  /**
   * One time recording.
   */
  ONE_TIME(0) {
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return localizer.msg(ONE_TIME_KEY, ONE_TIME_DEFAULT);
    }
  },
  /**
   * Repeat every day.
   */
  EVERY_DAY(1) {
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return localizer.msg(EVERY_DAY_KEY, EVERY_DAY_DEFAULT);
    }
  },
  /**
   * Repeat every weekend.
   */
  EVERY_WEEKEND(2) {
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return localizer.msg(EVERY_WEEKEND_KEY, EVERY_WEEKEND_DEFAULT);
    }
  },
  /**
   * Repeat on day of week.
   */
  WEEKLY(3) {
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return localizer.msg(WEEKLY_KEY, WEEKLY_DEFAULT);
    }
  },
  /**
   * Repeat on every week day.
   */
  EVERY_WEEKDAY(4) {
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return localizer.msg(EVERY_WEEKDAY_KEY, EVERY_WEEKDAY_DEFAULT);
    }
  };

  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldTimerMode.class);
  private static final String ONE_TIME_KEY = "oneTime";
  private static final String ONE_TIME_DEFAULT = "Once";
  private static final String EVERY_DAY_KEY = "everyDay";
  private static final String EVERY_DAY_DEFAULT = "Every day";
  private static final String EVERY_WEEKEND_KEY = "everyWeekend";
  private static final String EVERY_WEEKEND_DEFAULT = "Every weekend";
  private static final String WEEKLY_KEY = "weekly";
  private static final String WEEKLY_DEFAULT = "Weekly";
  private static final String EVERY_WEEKDAY_KEY = "everyWeekday";
  private static final String EVERY_WEEKDAY_DEFAULT = "Every weekday";

  private int modeNumber;

  /**
   * Create a <code>TopfieldTimerMode</code>.
   * 
   * @param number The mode number on the device
   */
  private TopfieldTimerMode(int number) {
    modeNumber = number;
  }

  /**
   * Create a timer mode from the device mode number.
   * 
   * @param number The timer mode number on the device
   * @return The <code>TopfieldTimerMode</code>
   */
  public static TopfieldTimerMode createFromNumber(int number) {
    switch (number) {
    case 0:
      return ONE_TIME;
    case 1:
      return EVERY_DAY;
    case 2:
      return EVERY_WEEKEND;
    case 3:
      return WEEKLY;
    case 4:
      return EVERY_WEEKDAY;
    default:
      return null;
    }
  }

  /**
   * @return The mode number on the device
   */
  public int toNumber() {
    return modeNumber;
  }
}
