package recommendationplugin;

import devplugin.Program;

public interface RecommendationInputIf {
  public String getName();

  public int getWeight();

  public void setWeight(int integer);

  public int calculate(Program p);
}
