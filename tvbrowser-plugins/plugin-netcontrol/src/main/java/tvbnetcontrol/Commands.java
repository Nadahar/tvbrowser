package tvbnetcontrol;

import java.util.HashMap;

public class Commands {
  public static final String CHANNEL = "channel";
  public static final String RUNNING = "running";
  public static final String UP = "up";
  public static final String DOWN = "down";
  public static final String LEFT = "left";
  public static final String RIGHT = "right";
  public static final String PAGE_UP = "pageup";
  public static final String PAGE_DOWN = "pagedown";
  public static final String PAGE_LEFT = "pageleft";
  public static final String PAGE_RIGHT = "pageright";
  public static final String PROGRAM_UP = "programup";
  public static final String PROGRAM_DOWN = "programdown";
  public static final String PROGRAM_LEFT = "programleft";
  public static final String PROGRAM_RIGHT = "programright";
  public static final String CLEARSELECTION = "clearselection";
  public static final String TAB = "tab";
  public static final String SHIFT_TAB = "shifttab";
  public static final String NOW = "now";
  public static final String PREVIOUS_DAY = "previousday";
  public static final String NEXT_DAY = "nextday";
  public static final String ENTER = "enter";
  public static final String PROGRAM_CONTEXT = "programcontext";
  public static final String SINGLE_LEFT_CLICK = "singlelclick";
  public static final String SINGLE_MIDDLE_CLICK = "singlemclick";
  public static final String DOUBLE_LEFT_CLICK = "doublelclick";
  public static final String DOUBLE_MIDDLE_CLICK = "doublemclick";
  public static final String SPACE = "space";
  public static final String ESC = "esc";
  public static final String FOCUS = "focus";
  public static final String KEY = "k_";
  public static final String KEY_FOCUS = "kf_";
  public static final String PING = "ping";
  public static final String DEFAULT_FILTER = "defaultfilter";
  
  public static final String SHIFT = "shift";
  public static final String CTRL = "ctrl";
  public static final String ALT = "alt";
  
  private static final String[] KEY_CMD_ARR = {UP,DOWN,LEFT,RIGHT,PAGE_UP,PAGE_DOWN,PAGE_LEFT,PAGE_RIGHT,PROGRAM_UP,
    PROGRAM_DOWN,PROGRAM_LEFT,PROGRAM_RIGHT,CLEARSELECTION,TAB,SHIFT_TAB,NOW,PREVIOUS_DAY,NEXT_DAY,ENTER,PROGRAM_CONTEXT,SINGLE_LEFT_CLICK,
    SINGLE_MIDDLE_CLICK,DOUBLE_LEFT_CLICK,DOUBLE_MIDDLE_CLICK,SPACE,ESC,DEFAULT_FILTER};
  
  private static final HashMap<String, KeyCommand> EVENT_MAP = new HashMap<String, KeyCommand>();
  
  static {
    EVENT_MAP.put(UP, new KeyCommand("UP"));
    EVENT_MAP.put(DOWN, new KeyCommand("DOWN"));
    EVENT_MAP.put(LEFT, new KeyCommand("LEFT"));
    EVENT_MAP.put(RIGHT, new KeyCommand("RIGHT"));
    EVENT_MAP.put(PAGE_UP, new KeyCommand("PAGE_UP"));
    EVENT_MAP.put(PAGE_DOWN, new KeyCommand("PAGE_DOWN"));
    EVENT_MAP.put(PAGE_LEFT, new KeyCommand("LEFT",KeyCommand.SHIFT+KeyCommand.FOCUS));
    EVENT_MAP.put(PAGE_RIGHT, new KeyCommand("RIGHT",KeyCommand.SHIFT+KeyCommand.FOCUS));
    EVENT_MAP.put(PROGRAM_UP, new KeyCommand("UP",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(PROGRAM_DOWN, new KeyCommand("DOWN",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(PROGRAM_LEFT, new KeyCommand("LEFT",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(PROGRAM_RIGHT, new KeyCommand("RIGHT",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(CLEARSELECTION, new KeyCommand("D",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(TAB, new KeyCommand("TAB"));
    EVENT_MAP.put(SHIFT_TAB, new KeyCommand("TAB",KeyCommand.SHIFT));
    EVENT_MAP.put(NOW, new KeyCommand("F9",KeyCommand.FOCUS));
    EVENT_MAP.put(PREVIOUS_DAY, new KeyCommand("P",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(NEXT_DAY, new KeyCommand("N",KeyCommand.CTRL+KeyCommand.FOCUS));
    EVENT_MAP.put(ENTER, new KeyCommand("ENTER"));
    EVENT_MAP.put(PROGRAM_CONTEXT, new KeyCommand("R",KeyCommand.FOCUS));
    EVENT_MAP.put(SINGLE_LEFT_CLICK, new KeyCommand("L",KeyCommand.FOCUS));
    EVENT_MAP.put(SINGLE_MIDDLE_CLICK, new KeyCommand("M",KeyCommand.FOCUS));
    EVENT_MAP.put(DOUBLE_LEFT_CLICK, new KeyCommand("D",KeyCommand.FOCUS));
    EVENT_MAP.put(DOUBLE_MIDDLE_CLICK, new KeyCommand("O",KeyCommand.FOCUS));
    EVENT_MAP.put(SPACE, new KeyCommand("SPACE"));
    EVENT_MAP.put(ESC, new KeyCommand("ESCAPE"));
    EVENT_MAP.put(DEFAULT_FILTER, new KeyCommand("A",KeyCommand.CTRL + KeyCommand.FOCUS));
  }
  
  public static String getCommandForCommand(String receivedCommand) {
    if(receivedCommand != null) {
      for(String keyCmd : KEY_CMD_ARR) {
        if(receivedCommand.toLowerCase().startsWith(keyCmd)) {
          return EVENT_MAP.get(keyCmd).getCommandString();
        }
      }
      
      if(receivedCommand.toLowerCase().startsWith(KEY) || receivedCommand.toLowerCase().startsWith(KEY_FOCUS)) {
        return receivedCommand;
      }
    }
    
    return null;
  }
  
  public static String getRunningNumber(String receivedCommand) {
    if(receivedCommand != null && receivedCommand.toLowerCase().startsWith(RUNNING)) {
      return receivedCommand.substring(receivedCommand.lastIndexOf("_")+1);
    }
    
    return null;
  }
  
  public static String getChannelNumber(String receivedCommand) {
    if(receivedCommand != null && receivedCommand.toLowerCase().startsWith(CHANNEL)) {
      return receivedCommand.substring(receivedCommand.lastIndexOf("_")+1);
    }

    return null;
  }
  
  public static boolean isFocus(String receivedCommand) {
    return receivedCommand != null && receivedCommand.toLowerCase().startsWith(FOCUS);
  }
  
  public static boolean isPing(String receivedCommand) {
    return receivedCommand != null && receivedCommand.toLowerCase().startsWith(PING);
  }
}
