package tvbrowser.extras.reminderplugin;

import javax.swing.JComboBox;

import devplugin.Date;
import devplugin.Program;
import util.io.IOUtilities;

public final class ReminderConstants {
  public static final int DONT_REMIND_AGAIN = -31;
  public static final int NO_REMINDER = -42;
  public static final RemindValue DONT_REMIND_AGAIN_VALUE = new RemindValue(DONT_REMIND_AGAIN);

  static final RemindValue[] REMIND_AFTER_VALUE_ARR = {
    new RemindValue(-30),
    new RemindValue(-20),
    new RemindValue(-10),
    new RemindValue(-5),
    new RemindValue(-1),
  };

  static final RemindValue[] REMIND_BEFORE_VALUE_ARR = {
    new RemindValue(0),
    new RemindValue(1),
    new RemindValue(2),
    new RemindValue(3),
    new RemindValue(5),
    new RemindValue(10),
    new RemindValue(15),
    new RemindValue(30),
    new RemindValue(60),
    new RemindValue(90),
    new RemindValue(120),
    new RemindValue(240),
    new RemindValue(480),
    new RemindValue(720),
    new RemindValue(1440),
    new RemindValue(7 * 1440),
  };
  
  final static String getStringForMinutes(final int minutes) {
    if(minutes == ReminderConstants.DONT_REMIND_AGAIN) {
      return ReminderConstants.DONT_REMIND_AGAIN_VALUE.toString();
    }
    
    for (int i = 0; i < ReminderConstants.REMIND_BEFORE_VALUE_ARR.length; i++) {
      if(ReminderConstants.REMIND_BEFORE_VALUE_ARR[i].getMinutes() == minutes) {
        return ReminderConstants.REMIND_BEFORE_VALUE_ARR[i].toString();
      }
    }

    for (int i = 0; i < ReminderConstants.REMIND_AFTER_VALUE_ARR.length; i++) {
      if(ReminderConstants.REMIND_AFTER_VALUE_ARR[i].getMinutes() == minutes) {
        return ReminderConstants.REMIND_AFTER_VALUE_ARR[i].toString();
      }
    }
    
    return null;
  }
  
  public final static JComboBox<RemindValue> getPreReminderMinutesSelection(int selectedMinutes) {
    final JComboBox<RemindValue> selection = new JComboBox<>();
    
    if(selectedMinutes < 0) {
      selectedMinutes = ReminderPlugin.getInstance().getDefaultReminderTime();
    }
    
    for(RemindValue value : ReminderConstants.REMIND_BEFORE_VALUE_ARR) {
      selection.addItem(value);
      
      if(value.getMinutes() == selectedMinutes) {
        selection.setSelectedIndex(selection.getItemCount()-1);
      }
    }
    
    return selection;
  }

  public final static int getReminderMinutesSelected(JComboBox<RemindValue> selection) {
    int result = ReminderConstants.DONT_REMIND_AGAIN;
    
    Object item = selection.getSelectedItem();
    
    if(item != null && item instanceof RemindValue) {
      result = ((RemindValue)item).getMinutes();
    }
    
    return result;
  }
  
  /**
   * Gets the time (in seconds) after which the reminder frame closes
   * automatically.
   */
  static int getAutoCloseReminderTime(Program p) {
    int autoCloseReminderTime = 0;
    try {
      if(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_BEHAVIOUR,ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_END).equals(ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_END)) {
        int endTime = p.getStartTime() + p.getLength();

        int currentTime = IOUtilities.getMinutesAfterMidnight();
        int dateDiff = p.getDate().compareTo(Date.getCurrentDate());
        if (dateDiff == -1) { // program started yesterday
          currentTime += 1440;
        }
        else if (dateDiff == 1) { // program starts the next day
          endTime += 1440;
        }
        autoCloseReminderTime = (endTime - currentTime) * 60;
      }
      else if(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_BEHAVIOUR,ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_TIME).equals(ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_TIME)){
        String asString = ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_REMINDER_TIME, "10");
        autoCloseReminderTime = Integer.parseInt(asString);
      } else {
        autoCloseReminderTime = 0;
      }
    } catch (Exception exc) {
      // ignore
    }
    return autoCloseReminderTime;
  }
}
