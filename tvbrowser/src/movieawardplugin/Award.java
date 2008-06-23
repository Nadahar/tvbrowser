package movieawardplugin;

public class Award {
  private int mProductionYear;
  private String mCategorie;
  private String mStatus;
  private String mMovieId;

  public Award(String categorie, String status, String movieId, int productionYear) {
    mCategorie = categorie;
    mStatus = status;
    mMovieId = movieId;
    mProductionYear = productionYear;
  }

  public String getMovieId() {
    return mMovieId;
  }
}
