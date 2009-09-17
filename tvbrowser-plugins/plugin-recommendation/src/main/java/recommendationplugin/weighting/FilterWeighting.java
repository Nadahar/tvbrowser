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
import devplugin.ProgramFilter;

public class FilterWeighting extends AbstractWeighting implements RecommendationWeighting {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterWeighting.class);
  private ProgramFilter mFilter;

  public FilterWeighting(final ProgramFilter filter) {
    mFilter = filter;
  }

  public String getName() {
    return mLocalizer.msg("name", "Filter: {0}", mFilter.getName());
  }

  public int getWeight(final Program program) {
    if (mFilter.accept(program)) {
      return mWeight;
    }
    return 0;
  }

  @Override
  public String getId() {
    return super.getId() + mFilter.getName();
  }
}
