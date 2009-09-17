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
package recommendationplugin;

import devplugin.Program;

public interface RecommendationWeighting {
  /**
   * get the user presentable name
   * @return
   */
  public String getName();
  /**
   * get the unique id of the filter instance for loading and storing
   * @return
   */
  public String getId();

  /**
   * get the overall (program independent) weighting
   * @return
   */
  public int getWeighting();

  /**
   * set the overall (program independent) weighting
   * @param integer
   */
  public void setWeighting(final int weighting);

  /**
   * get the specific weighting for the given program
   * @param program
   * @return
   */
  public int getWeight(final Program program);
}
