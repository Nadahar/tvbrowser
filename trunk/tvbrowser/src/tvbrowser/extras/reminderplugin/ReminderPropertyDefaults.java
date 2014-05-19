package tvbrowser.extras.reminderplugin;

import java.util.HashMap;

import util.misc.PropertyDefaults;

public final class ReminderPropertyDefaults {
  public static final String SOUNDFILE_KEY = "soundfile";
  public static final String REMINDER_WINDOW_SHOW = "usemsgbox";
  public static final String REMINDER_WINDOW_ALWAYS_ON_TOP = "alwaysOnTop";
  public static final String REMINDER_WINDOW_POSITION = "reminderWindowPosition";
  
  private static final HashMap<String, String> DEFAULT_VALUE_MAP;
  private static final PropertyDefaults DEFAULT_VALUES;
  
  static {
    DEFAULT_VALUE_MAP = new HashMap<String, String>();
    
    DEFAULT_VALUE_MAP.put(SOUNDFILE_KEY, "/");
    DEFAULT_VALUE_MAP.put(REMINDER_WINDOW_SHOW, "false");
    DEFAULT_VALUE_MAP.put(REMINDER_WINDOW_ALWAYS_ON_TOP, "true");
    DEFAULT_VALUE_MAP.put(REMINDER_WINDOW_POSITION, "6");
    
    DEFAULT_VALUES = new PropertyDefaults(DEFAULT_VALUE_MAP);
  }
  
  public static final PropertyDefaults getPropertyDefaults() {
    return DEFAULT_VALUES;
  }
}
