package recommendationplugin;

import devplugin.Program;

public interface RecommendationInputIf {
  public String getName();
  public int getWeight();
  int calculate(Program p);
}
