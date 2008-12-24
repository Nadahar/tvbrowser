package recommendationplugin.inputimpl;

import recommendationplugin.RecommendationInputIf;
import devplugin.Program;
import devplugin.Marker;

public class FavoriteInput implements RecommendationInputIf {
  private int mWeight;

  public FavoriteInput(final int weight) {
    mWeight = weight;
  }

  public String getName() {
    return "[Favorite]";
  }

  public int getWeight() {
    return mWeight;
  }

  public int calculate(Program p) {
    for (Marker m : p.getMarkerArr()) {
      if (m.getId().equals("favoritesplugin.FavoritesPlugin")) {
        return mWeight;
      }
    }
    return 0;
  }
}
