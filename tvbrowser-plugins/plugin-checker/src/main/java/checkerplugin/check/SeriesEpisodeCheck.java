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
import devplugin.ProgramInfoHelper;

public class SeriesEpisodeCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
    if (!ProgramInfoHelper.bitSet(program.getInfo(),
        Program.INFO_CATEGORIE_SERIES)) {
      final String episode = program
          .getTextField(ProgramFieldType.EPISODE_TYPE);
      if (episode != null) {
        addWarning(mLocalizer.msg("seriesEpisode",
            "Episode title is set, but category series is not set."));
      }
      final String original = program
          .getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
      if (original != null) {
        addWarning(mLocalizer.msg("seriesOriginal",
            "Original episode title is set, but category series is not set."));
      }
      final int episodeNumber = program
          .getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
      if (episodeNumber != -1) {
        addWarning(mLocalizer.msg("seriesNumber",
            "Episode number is set, but category series is not set."));
      }
      final int total = program
          .getIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE);
      if (total != -1) {
        addWarning(mLocalizer.msg("seriesTotal",
            "Total episode number is set, but category series is not set."));
      }
    }
	}

}
