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

public class RatingNode extends PluginTreeNode implements java.lang.Comparable<devplugin.PluginTreeNode> {

  private byte mRating;
  private Program mProgram;

  public RatingNode(final ImdbRating rating, final Program program) {
    super(rating.getRatingText() + " " + program.getTitle());
    mRating = rating.getRating();
    mProgram = program;
    setGroupingByDateEnabled(false);
  }

  public int compareTo(final PluginTreeNode other) {
    if (other instanceof RatingNode) {
      RatingNode otherRating = (RatingNode) other;
      if (mRating < otherRating.mRating) {
        return -1;
      } else if (mRating > otherRating.mRating) {
        return 1;
      } else {
        return mProgram.getTitle().compareTo(otherRating.mProgram.getTitle());
      }

    }
    // ToDo: activate the next line and remove the other following lines if 
    // TVB 3.0 goes final
    //return super.compareTo(other);
    final Object mObject = getUserObject();
    final Object otherUserObject = other.getUserObject();
    
    if (mObject instanceof String && otherUserObject instanceof String) {
      return ((String) mObject).compareToIgnoreCase((String) otherUserObject);
    }
    if (mObject instanceof String) {
      return 1;
    }
    return -1;
  }

}
