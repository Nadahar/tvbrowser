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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class TextFieldFormatCheck extends AbstractCheck {

  private static final Pattern HTML_PATTERN = Pattern.compile(Pattern.quote("&")
      + "\\w+" + Pattern.quote(";"));

  private static final Pattern MISSING_WHITE_PATTERN = Pattern
      .compile("\\s\\p{Lower}+\\p{Upper}");

	@Override
	protected void doCheck(final Program program) {
		final Iterator<ProgramFieldType> it = ProgramFieldType.getTypeIterator();
		while (it.hasNext()) {
			final ProgramFieldType fieldType = it.next();
			if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
				final String content = program.getTextField(fieldType);
				if (content != null) {
					if (content.trim().length() < content.length()) {
						if (content.trim().length() == 0) {
							addError(mLocalizer.msg("whitespaceOnly",
									"Text field {0} contains only whitespace.", fieldType
											.getLocalizedName()));
						} else {
							addError(mLocalizer.msg("trim",
									"Text field {0} has whitespace at beginning or end.",
									fieldType.getLocalizedName()));
						}
					}
					Matcher matcher = HTML_PATTERN.matcher(content);
					if (matcher.find()) {
						addError(mLocalizer.msg("entity",
								"Text field {0} contains HTML entity: {1}", fieldType
										.getLocalizedName(), matcher.group()));
					}
					if (fieldType != ProgramFieldType.ACTOR_LIST_TYPE
							&& fieldType != ProgramFieldType.DIRECTOR_TYPE
							&& fieldType != ProgramFieldType.CAMERA_TYPE
							&& fieldType != ProgramFieldType.CUTTER_TYPE
							&& fieldType != ProgramFieldType.MODERATION_TYPE
							&& fieldType != ProgramFieldType.ADDITIONAL_PERSONS_TYPE) {
						if (MISSING_WHITE_PATTERN.matcher(content).find()) {
							addWarning(mLocalizer.msg("missingWhitespace",
									"Text field {0} probably misses whitespace.", fieldType
											.getLocalizedName()));
						}
					}
					int closing = 0;
					int opening = 0;
					for (int i = 0; i < content.length(); i++) {
						if (content.charAt(i) == '(') {
							opening++;
						} else if (content.charAt(i) == ')') {
							closing++;
						}
					}
					if (closing != opening) {
						addError(mLocalizer.msg("braces",
								"Opening and closing braces don't match in {0}.", fieldType
										.getLocalizedName()));
					}
					if (content.contains("\\-")) {
						addError(mLocalizer.msg("dash", "Escaped dash in {0}.",
								fieldType.getLocalizedName()));
					}
				}
			}
		}
	}

}
