package imdbplugin;

public class ImdbRating {
  private String mDistribution;
  private int mVotes;
  private int mRating;
  private String mMovieid;

  public ImdbRating(int rating, int votes, String distribution, String movieid) {
    mRating = rating;
    mVotes = votes;
    mDistribution = distribution;
    mMovieid = movieid;
  }

  public int getRating() {
    return mRating;
  }

  public int getVotes() {
    return mVotes;
  }
}
