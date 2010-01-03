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

public class TitleFormatCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
		final String title = program.getTitle();
		if (title.indexOf('\n') >= 0) {
			addError(mLocalizer.msg("linebreak",
					"Title contains line break."));
		} else {
			for (int i = 0; i < title.length(); i++) {
				if (Character.isWhitespace(title.charAt(i)) && (title.charAt(i) != ' ')) {
					addError(mLocalizer.msg("whitespace",
							"Title contains white space which is no space character."));
				}
			}
		}
		if (title.length() >= 2) {
			char lastChar = title.charAt(title.length() - 1);
			if (title.charAt(title.length() - 2) == ' '
					&& !Character.isLetterOrDigit(lastChar) && lastChar != '?'
					&& lastChar != '!') {
				addWarning(mLocalizer.msg("titleEnd",
						"Title seems to contain confusing end character."));
			}
		}

	}

}
