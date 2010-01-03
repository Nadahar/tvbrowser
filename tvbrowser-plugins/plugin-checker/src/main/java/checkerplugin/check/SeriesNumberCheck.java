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
import devplugin.ProgramFieldType;

public class SeriesNumberCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
		final int episodeNumber = program
				.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
		if (episodeNumber != -1 && episodeNumber < 1) {
			addError(mLocalizer.msg("episodeLess",
					"Episode number is less than 1."));
		}
		final int total = program
				.getIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE);
		if (total != -1) {
			if (total < 1) {
				addError(mLocalizer.msg("totalLess",
						"Total episode is less than 1."));
			}
			if (episodeNumber != -1 && episodeNumber > total) {
				addError(mLocalizer.msg("episodeToLarge",
						"Episode number is larger than total episode count."));
			}
		}

	}

}
