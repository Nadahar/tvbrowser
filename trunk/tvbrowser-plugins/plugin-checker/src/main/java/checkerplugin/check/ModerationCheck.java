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

public class ModerationCheck extends AbstractCheck {
	private static final Pattern MODERATION_PATTERN = Pattern
			.compile("(Moderation|Moderator):");

	@Override
	protected void doCheck(final Program program) {
		String moderation = program.getTextField(ProgramFieldType.MODERATION_TYPE);
		if (moderation == null || moderation.isEmpty()) {
			String description = program.getDescription();
			if (description != null && !description.isEmpty()) {
				Matcher matcher = MODERATION_PATTERN.matcher(description);
				if (matcher.find()) {
					addError(mLocalizer.msg("moderation",
							"Moderation field not set"));
				}
			}
		}
	}
}
