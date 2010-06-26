/*
 * Copyright Michael Keppler
 *
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

public class ImdbHistogram {
  private static final int MIN_VOTES_HISTOGRAM = 100;

  private int[] mDistribution;
  private int mCount = 0;

  public ImdbHistogram() {
    mDistribution = new int[ImdbRating.MAX_RATING_NORMALIZATION + 1];
  }

  public ImdbHistogram(final ImdbSettings settings) {
    mDistribution = settings.getHistogram();
    mCount = 0;
    for (int i = 0; i < mDistribution.length; i++) {
      mCount += mDistribution[i];
    }
  }

  void addRating(final int rating, final int votes) {
    if (votes >= MIN_VOTES_HISTOGRAM) {
      mDistribution[rating]++;
      mCount++;
    }
  }

  public int[] getData() {
    return mDistribution;
  }

  public boolean isValid() {
    return mCount > 0;
  }

  public int getPercentile(final ImdbRating rating) {
    int movies = 0;
    for (int i = 0; i < rating.getRating(); i++) {
      movies += mDistribution[i];
    }
    return 100 * movies / mCount;
  }
}
