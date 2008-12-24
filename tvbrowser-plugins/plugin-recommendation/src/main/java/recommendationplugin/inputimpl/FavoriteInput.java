package recommendationplugin.inputimpl;

import devplugin.Marker;
import devplugin.Program;
import recommendationplugin.RecommendationInputIf;

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

  public void setWeight(int weight) {
    mWeight = weight;
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
