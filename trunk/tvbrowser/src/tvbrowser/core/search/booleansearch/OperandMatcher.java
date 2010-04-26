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
package tvbrowser.core.search.booleansearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

abstract class OperandMatcher implements IMatcher {

  protected ArrayList<IMatcher> subMatcher = new ArrayList<IMatcher>();

  protected OperandMatcher(final IMatcher left, final IMatcher right) {
    subMatcher.add(left);
    subMatcher.add(right);
  }

  protected OperandMatcher(OperandMatcher leftOp, OperandMatcher rightOp) {
    subMatcher.addAll(leftOp.subMatcher);
    subMatcher.addAll(rightOp.subMatcher);
    subMatcher.remove(leftOp);
    subMatcher.remove(rightOp);
  }

  public IMatcher optimize() {
    // remove duplicates
    ArrayList<IMatcher> nonDuplicates = new ArrayList<IMatcher>(new HashSet<IMatcher>(subMatcher));

    // optimize children
    subMatcher = new ArrayList<IMatcher>(nonDuplicates.size());
    for (IMatcher matcher : nonDuplicates) {
      subMatcher.add(matcher.optimize());
    }

    // sort String matchers to the beginning
    Collections.sort(subMatcher, new Comparator<IMatcher>() {

      @Override
      public int compare(IMatcher leftMatcher, IMatcher rightMatcher) {
        boolean leftIsString = leftMatcher instanceof StringMatcher;
        boolean rightIsString = rightMatcher instanceof StringMatcher;
        if (leftIsString && rightIsString) {
          StringMatcher m1 = (StringMatcher) leftMatcher;
          StringMatcher m2 = (StringMatcher) rightMatcher;
          return -Integer.valueOf(m1.size()).compareTo(m2.size());
        } else if (leftIsString) {
          return -1;
        } else if (rightIsString) {
          return 1;
        }
        return 0;
      }
    });

    return this;
  }

  public String toString() {
    StringBuilder temp = new StringBuilder(100);
    temp.append('(').append(subMatcher.get(0));
    for (int i = 1; i < subMatcher.size(); i++) {
      temp.append(' ').append(getOperandString()).append(' ').append(subMatcher.get(i).toString());
    }
    temp.append(')');
    return temp.toString();
  }

  protected abstract String getOperandString();
}