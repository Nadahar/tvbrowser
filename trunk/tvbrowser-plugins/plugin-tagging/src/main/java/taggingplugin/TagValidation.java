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
package taggingplugin;

/**
 * shared component for validating tags in plugin and on server
 * @author bananeweizen
 *
 */
public class TagValidation {
  private static final int MIN_TAG_LENGTH = 3;
  private static final int MAX_TAG_LENGTH = 40;

  static String makeValidTag(final String tag) {
    if (tag == null) {
      return null;
    }
    
    // don't allow HTML
    if (tag.indexOf('<') >= 0 || tag.indexOf('>') >= 0) {
      return null;
    }
    
    // no blanks at beginning or end
    String result = tag.trim();

    // everything up to first blank only
    int blank = result.indexOf(' ');
    if (blank >= 0) {
      result = result.substring(0, blank);
    }

    // must be long and short enough
    if (result.isEmpty() || result.length() < MIN_TAG_LENGTH || result.length() > MAX_TAG_LENGTH) {
      return null;
    }
    return result;
  }

}
