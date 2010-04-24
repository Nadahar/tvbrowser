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

class OrMatcher extends OperandMatcher {

  public OrMatcher(IMatcher left, IMatcher right) {
    super(left, right);
  }

  private OrMatcher(OrMatcher left, OrMatcher right) {
    super(left, right);
  }

  public boolean matches(final String searchTerm) {
    for (IMatcher matcher : subMatcher) {
      if (matcher.matches(searchTerm)) {
        return true;
      }
    }
    return false;
  }

  public IMatcher optimize() {
    // collect all ORs
    for (IMatcher matcher : subMatcher) {
      if (matcher instanceof OrMatcher) {
        return new OrMatcher(this, (OrMatcher) matcher).optimize();
      }
    }

    return super.optimize();
  }

  @Override
  protected String getOperandString() {
    return "OR";
  }
}