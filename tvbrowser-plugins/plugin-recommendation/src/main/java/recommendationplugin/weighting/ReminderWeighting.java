package recommendationplugin.weighting;

import recommendationplugin.RecommendationWeighting;
import util.ui.Localizer;
import devplugin.Marker;
import devplugin.Program;


public class ReminderWeighting extends AbstractWeighting implements RecommendationWeighting {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ReminderWeighting.class);

  public String getName() {
    return mLocalizer.msg("name", "Reminder");
  }

  public int getWeight(final Program p) {
    for (Marker m : p.getMarkerArr()) {
      if (m.getId().equals("reminderplugin.ReminderPlugin")) {
        return mWeight;
      }
    }
    return 0;
  }
}
