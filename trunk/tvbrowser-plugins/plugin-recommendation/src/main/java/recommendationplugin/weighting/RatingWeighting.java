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
package recommendationplugin.weighting;

import recommendationplugin.RecommendationWeighting;
import util.ui.Localizer;
import devplugin.Program;
import devplugin.ProgramRatingIf;

public class RatingWeighting extends AbstractWeighting implements RecommendationWeighting {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RatingWeighting.class);

  private ProgramRatingIf mRating;
  public RatingWeighting(ProgramRatingIf rating) {
    mRating = rating;
  }

  public String getName() {
    return mLocalizer.msg("name", "Rating: {0}", mRating.getName());
  }

  public int getWeight(final Program p) {
    final int value = mRating.getRatingForProgram(p);

    if (value == -1) {
      return 0;
    }

    return (value * mWeight) / 100;
  }
  
  @Override
  public String getId() {
    return super.getId() + '@' + mRating.getName();
  }
}
