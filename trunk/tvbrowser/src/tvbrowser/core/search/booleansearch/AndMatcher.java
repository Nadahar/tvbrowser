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

class AndMatcher extends OperandMatcher {

  protected AndMatcher(IMatcher left, IMatcher right) {
    super(left, right);
  }

  private AndMatcher(AndMatcher left, AndMatcher right) {
    super(left, right);
  }

  public boolean matches(final String searchTerm) {
    for (IMatcher matcher : subMatcher) {
      if (!matcher.matches(searchTerm)) {
        return false;
      }
    }
    return true;
  }

  public IMatcher optimize() {
    // collect all ANDs
    for (IMatcher matcher : subMatcher) {
      if (matcher instanceof AndMatcher) {
        return new AndMatcher(this, (AndMatcher) matcher).optimize();
      }
    }

    return super.optimize();
  }

  @Override
  protected String getOperandString() {
    return "AND";
  }
}