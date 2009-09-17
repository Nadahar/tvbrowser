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
import devplugin.Program;

public class FacadeWeighting extends AbstractWeighting {
  private RecommendationWeighting mInnerWeighting;

  public FacadeWeighting(RecommendationWeighting recommendationWeighting) {
    mInnerWeighting = recommendationWeighting;
    mWeight = recommendationWeighting.getWeighting();
  }

  @Override
  public String getId() {
    return mInnerWeighting.getId();
  }

  public String getName() {
    return mInnerWeighting.getName();
  }

  public int getWeight(final Program program) {
    return 0;
  }
}
