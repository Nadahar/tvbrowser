/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package imdbplugin;

import java.text.DecimalFormat;

public final class ImdbRating {
  
  private static final DecimalFormat RATING_TEXT_FORMAT = new DecimalFormat(
      "##.0");

  public static final byte MAX_RATING_NORMALIZATION = 100;
  
  private String mDistribution;
  private int mVotes;
  private byte mRating;

  public ImdbRating(final byte rating, final int votes,
      final String distribution, final String movieid) {
    mRating = rating;
    mVotes = votes;
    mDistribution = distribution;
  }

  public ImdbRating(final int rating, final int votes,
      final String distribution, final String movieid) {
    this((byte)rating, votes, distribution, movieid);
  }

  public byte getRating() {
    return mRating;
  }
  
  public double getRatingRelative() {
    return (double) mRating / MAX_RATING_NORMALIZATION;
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
