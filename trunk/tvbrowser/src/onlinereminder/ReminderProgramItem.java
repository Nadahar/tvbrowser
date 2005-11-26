package onlinereminder;

import devplugin.Program;
import devplugin.ProgramItem;

public class ReminderProgramItem extends ProgramItem {

  int mReminderTime = 0;
  
  public ReminderProgramItem(Program prog, int reminderTime) {
    super(prog);
    setMinutes(reminderTime);
  }

  public void setMinutes(int reminderTime) {
    setProperty("minutes", Integer.toString(reminderTime));
    mReminderTime = reminderTime;
  }

  public int getMinutes() {
    return mReminderTime;
  }

  
}