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

import java.util.ArrayList;
import java.util.HashSet;

import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;

public class DescriptionRepetitionCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
		// sport events often have lists of similar challenges, which can lead to wanted repetitions
		if (!ProgramInfoHelper.bitSet(program.getInfo(), Program.INFO_LIVE)) {
			checkDescriptionField(program, program.getDescription(),
					ProgramFieldType.DESCRIPTION_TYPE.getLocalizedName());
			checkDescriptionField(program, program.getShortInfo(),
					ProgramFieldType.SHORT_DESCRIPTION_TYPE.getLocalizedName());
		}
	}

	private void checkDescriptionField(final Program program, String desc,
			final String fieldName) {
		if (desc == null) {
			return;
		}
		desc = desc.trim();
		// search duplicated lines
		String[] lines = desc.split("\n");
		ArrayList<String> list = new ArrayList<String>(lines.length);
		for (String line : lines) {
			if (!line.trim().isEmpty()) {
				list.add(line);
			}
		}
		HashSet<String> set = new HashSet<String>(list.size());
		set.addAll(list);
		if (set.size() != list.size()) {
			addError(mLocalizer.msg("desc.duplicate",
					"{0} has duplicate parts", fieldName));
		}
		int size = 50;
		if (desc.length() < size) {
			return;
		}
		// search long duplications even without line break
		int index = desc.indexOf(desc.substring(0, size), size);
		if (index >= size) {
			if (desc.indexOf(desc.substring(0, index - 1).trim(), index) == index) {
				addError(mLocalizer.msg("desc.duplicate",
						"{0} has duplicate parts", fieldName));
			}
		}
	}

}
