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
import devplugin.ProgramFieldType;

public class RepetitionCheck extends AbstractCheck {

	private static final Pattern REPETITION_PATTERN = Pattern
			.compile("(Wiederholung|" + Pattern.quote("Wh.") + ")\\s(vom|von)\\s(Vortag|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonnabend|Sonntag|Wochenende|\\d)");

	private static final Pattern REPETITION_ON_PATTERN = Pattern
			.compile("(Wiederholung|" + Pattern.quote("Wh.") + ")\\s(am|um)\\s");

	@Override
	protected void doCheck(final Program program) {
		String repetition = program
				.getTextField(ProgramFieldType.REPETITION_OF_TYPE);
		if (repetition == null || repetition.isEmpty()) {
			String description = program.getDescription();
			if (description != null && !description.isEmpty()) {
				Matcher matcher = REPETITION_PATTERN.matcher(description);
				if (matcher.find()) {
					addError(mLocalizer.msg("repetitionOf",
							"Not marked as repetition of"));
				} else {
					String[] lines = description.split("\n");
					for (String line : lines) {
						if (line.equals("Wiederholung")) {
							addError(mLocalizer.msg("repetitionOf",
									"Not marked as repetition of"));
						}
					}
				}
			}
		}
		repetition = program.getTextField(ProgramFieldType.REPETITION_ON_TYPE);
		if (repetition == null || repetition.isEmpty()) {
			String description = program.getDescription();
			if (description != null && !description.isEmpty()) {
				Matcher matcher = REPETITION_ON_PATTERN.matcher(description);
				if (matcher.find()) {
					addError(mLocalizer.msg("repetitionOn",
							"Not marked as repetition on"));
				}
			}
		}
	}
}