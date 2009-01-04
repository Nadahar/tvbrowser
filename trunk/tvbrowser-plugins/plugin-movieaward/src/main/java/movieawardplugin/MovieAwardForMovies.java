package movieawardplugin;

import devplugin.Program;

public class MovieAwardForMovies extends MovieAward {

  public MovieAwardForMovies(MovieDatabase database) {
    super(database);
  }

  @Override
  public boolean containsAwardFor(Program program) {
    boolean contains = super.containsAwardFor(program);
    if (!contains) {
      return false;
    }
    // we already know we found an award, so lets see if this is the right movie
    return isValid(program);
  }

  private boolean isValid(Program program) {
    int info = program.getInfo();
    if (info > 0) {
      if (bitSet(info, Program.INFO_CATEGORIE_MOVIE)) {
        return true;
      }
      if (bitSet(info, Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT)
          || bitSet(info, Program.INFO_CATEGORIE_NEWS)
          || bitSet(info, Program.INFO_CATEGORIE_SERIES)
          || bitSet(info, Program.INFO_CATEGORIE_SHOW)
          || bitSet(info, Program.INFO_CATEGORIE_SPORTS)) {
        return false;
      }
    }
    final String description = program.getDescription();
    if (description != null && description.length() > 0
        && description.contains("serie")) {
      return false;
    }
    // if we cannot find any other criteria, assume this program is valid for
    // this award
    return true;
  }

  @Override
  public Award[] getAwardsFor(Program program) {
    Award[] awards = super.getAwardsFor(program);
    if (awards == null || awards.length == 0) {
      return awards;
    }
    if (isValid(program)) {
      return awards;
    } else {
      return new Award[0];
    }
  }

  private boolean bitSet(int info, int bit) {
    return (info & bit) == bit;
  }

}
