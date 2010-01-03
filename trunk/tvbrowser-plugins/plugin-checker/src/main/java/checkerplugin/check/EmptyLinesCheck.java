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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devplugin.Program;

public class EmptyLinesCheck extends AbstractCheck {

	private static final int MAX_BLANK_LINES = 1;
	private static final Pattern BLANK_LINES_PATTERN = Pattern.compile("(\\r\\n){" + (MAX_BLANK_LINES + 2) + ",}");

	@Override
	protected void doCheck(final Program program) {
		String description = program.getDescription();
		if (description != null && !description.isEmpty()) {
			Matcher matcher = BLANK_LINES_PATTERN.matcher(description);
			if (matcher.find()) {
				addWarning(mLocalizer.msg("blankLines", "More than {0} blank lines", MAX_BLANK_LINES));
			}
		}
	}

}
