package recommendationplugin.weighting;

import recommendationplugin.RecommendationWeighting;
import util.ui.Localizer;
import devplugin.Marker;
import devplugin.Program;

public class FavoriteWeighting extends AbstractWeighting implements RecommendationWeighting {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FavoriteWeighting.class);

  public String getName() {
    return mLocalizer.msg("name", "Favorite");
  }

  public int getWeight(final Program p) {
    for (Marker m : p.getMarkerArr()) {
      if (m.getId().equals("favoritesplugin.FavoritesPlugin")) {
        return mWeight;
      }
    }
    return 0;
  }
}
