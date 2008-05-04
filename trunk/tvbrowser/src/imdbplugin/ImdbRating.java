package imdbplugin;

public class ImdbRating {
  private String mDistribution;
  private int mVotes;
  private int mRating;

  public ImdbRating(int rating, int votes, String distribution) {
    mRating = rating;
    mVotes = votes;
    mDistribution = distribution;
  }

  public int getRating() {
    return mRating;
  }
}
