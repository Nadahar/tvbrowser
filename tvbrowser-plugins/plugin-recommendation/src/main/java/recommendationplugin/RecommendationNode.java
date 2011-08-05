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

import devplugin.PluginTreeNode;

public class RecommendationNode extends PluginTreeNode/* implements Comparable<RecommendationNode>*/ {

  private ProgramWeight mProgramWeight;

  public RecommendationNode(final ProgramWeight programWeight) {
    super(getLabel(programWeight));
    mProgramWeight = programWeight;
    setGroupingByDateEnabled(false);
  }

  private static String getLabel(final ProgramWeight programWeight) {
    return programWeight.getProgram().getTitle() + " (" + 100 * programWeight.getWeight() / ProgramWeight.getMaxWeight() + "%)";
  }

  public int compareTo(final RecommendationNode other) {
    // sort descending by weight
    return mProgramWeight.compareTo(other.mProgramWeight);
  }

}
