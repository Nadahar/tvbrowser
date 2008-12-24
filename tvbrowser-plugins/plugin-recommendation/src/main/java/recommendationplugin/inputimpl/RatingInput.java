package recommendationplugin.inputimpl;

import devplugin.Program;
import devplugin.ProgramRatingIf;
import recommendationplugin.RecommendationInputIf;

public class RatingInput implements RecommendationInputIf {

  private ProgramRatingIf mRating;
  private int mWeight;

  public RatingInput(ProgramRatingIf rating, int weight) {
    mRating = rating;
    mWeight = weight;
  }

  public String getName() {
    return mRating.getName();
  }

  public int getWeight() {
    return mWeight;
  }

  public void setWeight(int weight) {
    mWeight = weight;
  }

  public int calculate(Program p) {
    final int value = mRating.getRatingForProgram(p);

    if (value == -1) {
      return 0;
    }

    return (value * mWeight) / 100;
  }
}
