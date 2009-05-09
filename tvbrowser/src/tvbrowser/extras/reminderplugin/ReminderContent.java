package tvbrowser.extras.reminderplugin;

/**
 * Shows the content of a {@link tvbrowser.extras.reminderplugin.ReminderDialog}.
 */
public class ReminderContent {

  private int reminderMinutes;
  private String reminderComment;

  public ReminderContent(int reminderMinutes) {
    this(reminderMinutes, null);
  }

  public ReminderContent(int reminderMinutes, String reminderComment) {
    this.reminderMinutes = reminderMinutes;
    this.reminderComment = reminderComment;
  }

  public int getReminderMinutes() {
    return reminderMinutes;
  }

  public String getReminderComment() {
    return reminderComment;
  }
}
