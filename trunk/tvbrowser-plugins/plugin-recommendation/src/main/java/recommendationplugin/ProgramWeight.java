package recommendationplugin;

import devplugin.Program;

public class ProgramWeight implements Comparable<ProgramWeight> {
  private Program mProgram;
  private int mWeight;

  public ProgramWeight(final Program p, final int weight) {
    mProgram = p;
    mWeight = weight;
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

}
