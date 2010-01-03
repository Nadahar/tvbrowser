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
package checkerplugin.check;

import devplugin.Program;

public class ShortDescriptionLengthCheck extends AbstractCheck {

	private static final int MAX_LENGTH = 200;

	@Override
	protected void doCheck(final Program program) {
    final String desc = program.getShortInfo();
    if (desc != null && desc.length() > MAX_LENGTH) {
      addWarning(mLocalizer.msg("shortDescription",
          "Short description contains more than {0} characters", MAX_LENGTH));
    }
	}

}
