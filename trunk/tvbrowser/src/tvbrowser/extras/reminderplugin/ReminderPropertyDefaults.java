package tvbrowser.extras.reminderplugin;

import java.util.HashMap;

import util.misc.PropertyDefaults;

public final class ReminderPropertyDefaults {
  static final String KEY_AUTO_CLOSE_BEHAVIOUR = "autoCloseBehaviour";
  static final String KEY_AUTO_CLOSE_REMINDER_TIME = "autoCloseReminderTime";
  static final String KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY = "autoCloseFrameRemindersIfEmpty";
  static final String KEY_SHOW_TIME_COUNTER = "showTimeCounter";
  static final String KEY_FRAME_REMINDERS_TO_FRONT_WHEN_REMINDER_ADDED = "frameRemindersToFrontOnAdd";
  
  static final String VALUE_AUTO_CLOSE_BEHAVIOUR_ON_END = "onEnd";
  static final String VALUE_AUTO_CLOSE_BEHAVIOUR_ON_TIME = "onTime";
  
  public static final String KEY_SOUNDFILE = "soundfile";
  public static final String KEY_FRAME_REMINDERS_SHOW = "showFrameReminders";
  public static final String KEY_REMINDER_WINDOW_SHOW = "usemsgbox";
  public static final String KEY_REMINDER_WINDOW_ALWAYS_ON_TOP = "alwaysOnTop";
  public static final String KEY_REMINDER_WINDOW_POSITION = "reminderWindowPosition";
  public static final String KEY_SCROLL_TIME_TYPE_NEXT = "scrollTimeTypeNext";
  
  private static final HashMap<String, String> DEFAULT_VALUE_MAP;
  private static final PropertyDefaults DEFAULT_VALUES;
  
  static {
    DEFAULT_VALUE_MAP = new HashMap<String, String>();
    
    DEFAULT_VALUE_MAP.put(KEY_SOUNDFILE, "/");
    DEFAULT_VALUE_MAP.put(KEY_FRAME_REMINDERS_SHOW, "true");
    DEFAULT_VALUE_MAP.put(KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true");
    DEFAULT_VALUE_MAP.put(KEY_FRAME_REMINDERS_TO_FRONT_WHEN_REMINDER_ADDED, "true");
    DEFAULT_VALUE_MAP.put(KEY_REMINDER_WINDOW_SHOW, "false");
    DEFAULT_VALUE_MAP.put(KEY_REMINDER_WINDOW_ALWAYS_ON_TOP, "true");
    DEFAULT_VALUE_MAP.put(KEY_REMINDER_WINDOW_POSITION, "6");
    DEFAULT_VALUE_MAP.put(KEY_SCROLL_TIME_TYPE_NEXT, "true");
    
    DEFAULT_VALUES = new PropertyDefaults(DEFAULT_VALUE_MAP);
  }
  
  public static final PropertyDefaults getPropertyDefaults() {
    return DEFAULT_VALUES;
  }
}

