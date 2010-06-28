package recommendationplugin;

import devplugin.Program;

public class ProgramWeight implements Comparable<ProgramWeight> {
  private Program mProgram;
  private short mWeight;

  private static int maxWeight;

  public ProgramWeight(final Program p, final short weight) {
    mProgram = p;
    mWeight = weight;
    if (mWeight > maxWeight) {
      maxWeight = weight;
    }
  }

  public Program getProgram() {
    return mProgram;
  }

  public int getWeight() {
    return mWeight;
  }

  public int compareTo(ProgramWeight programWeight) {
    if (mWeight < programWeight.getWeight()) {
      return 1;
    } else if (mWeight > programWeight.getWeight()) {
      return -1;
    }

    return mProgram.getTitle().compareTo(programWeight.mProgram.getTitle());
  }

  static int getMaxWeight() {
    return maxWeight;
  }

  static void resetMaxWeight() {
    maxWeight = 0;
  }
}
