package onlinereminder;


public class ReminderValues {

  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(ReminderValues.class);
  
  private static final String[] REMIND_MSG_ARR = {
    mLocalizer.msg("remind.0", "Remind me when the program begins"),
    mLocalizer.msg("remind.1", "Remind me one minute before"),
    mLocalizer.msg("remind.2", "Remind me 2 minutes before"),
    mLocalizer.msg("remind.3", "Remind me 3 minutes before"),
    mLocalizer.msg("remind.5", "Remind me 5 minutes before"),
    mLocalizer.msg("remind.10", "Remind me 10 minutes before"),
    mLocalizer.msg("remind.15", "Remind me 15 minutes before"),
    mLocalizer.msg("remind.30", "Remind me 30 minutes before"),
    mLocalizer.msg("remind.60", "Remind me one hour before"),
    mLocalizer.msg("remind.90", "Remind me 1.5 hours before"),
    mLocalizer.msg("remind.120", "Remind me 2 hours before"),
    mLocalizer.msg("remind.240", "Remind me 4 hours before"),
    mLocalizer.msg("remind.480", "Remind me 8 hours before"),
    mLocalizer.msg("remind.1440", "Remind me one day before"),
  };
  
  private static final int[] REMIND_MIN_ARR = {
    0, 1, 2, 3, 5, 10, 15, 30, 60, 90, 120, 240, 480, 1440
  };
  
  public static String[] getAllValues() {
    return REMIND_MSG_ARR;
  }
  
  public static int getMinutesForValue(int number) {
    return REMIND_MIN_ARR[number];
  }
  
  public static int getValueForMinutes(int minute) {
    int max = REMIND_MIN_ARR.length;
    
    for (int i=0;i<max;i++) {
      if (REMIND_MIN_ARR[i] == minute) {
        return i;
      }
    }
    
    return -1;
  }
  
  public static String getStringForMinutes(int minute) {
    return REMIND_MSG_ARR[getValueForMinutes(minute)];
  }
  
}