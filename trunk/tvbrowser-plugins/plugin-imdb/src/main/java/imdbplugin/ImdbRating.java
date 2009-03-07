package imdbplugin;

import java.text.DecimalFormat;

public final class ImdbRating {
  
  private static final DecimalFormat RATING_TEXT_FORMAT = new DecimalFormat(
      "##.0");

  public static final int MAX_RATING_NORMALIZATION = 100;
  
  private String mDistribution;
  private int mVotes;
  private int mRating;

  public ImdbRating(final int rating, final int votes,
      final String distribution, final String movieid) {
    mRating = rating;
    mVotes = votes;
    mDistribution = distribution;
  }

  public int getRating() {
    return mRating;
  }

  public int getVotes() {
    return mVotes;
  }

  public String getDistribution() {
    return mDistribution;
  }

  public String getRatingText() {
    return RATING_TEXT_FORMAT.format((double) getRating() / 10);
  }
}
