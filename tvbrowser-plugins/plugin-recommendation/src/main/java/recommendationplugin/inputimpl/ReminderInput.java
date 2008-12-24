package recommendationplugin.inputimpl;

import devplugin.Marker;
import devplugin.Program;
import recommendationplugin.RecommendationInputIf;


public class ReminderInput implements RecommendationInputIf {
  private int mWeight;

  public ReminderInput(final int weight) {
    mWeight = weight;
  }

  public String getName() {
    return "[Reminder]";
  }

  public int getWeight() {
    return mWeight;
  }

  public void setWeight(int weight) {
    mWeight = weight;
  }

  public int calculate(Program p) {
    for (Marker m : p.getMarkerArr()) {
      if (m.getId().equals("reminderplugin.ReminderPlugin")) {
        return mWeight;
      }
    }
    return 0;
  }
}
