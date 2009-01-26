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

import devplugin.PluginTreeNode;
import devplugin.Program;

public class RatingNode extends PluginTreeNode implements
    Comparable<RatingNode> {

  private int mRating;
  private Program mProgram;

  public RatingNode(ImdbRating rating, Program program) {
    super(rating.getRatingText() + " " + program.getTitle());
    mRating = rating.getRating();
    mProgram = program;
  }

  public int compareTo(RatingNode other) {
    if (mRating < other.mRating) {
      return -1;
    } else if (mRating > other.mRating) {
      return 1;
    } else
      return mProgram.getTitle().compareTo(other.mProgram.getTitle());
  }

}
