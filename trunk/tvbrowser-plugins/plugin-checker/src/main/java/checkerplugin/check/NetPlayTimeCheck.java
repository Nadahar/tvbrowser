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

public class NetPlayTimeCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
		final int netTime = program
				.getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
		if (netTime != -1) {
			final int duration = program.getLength();
			if (netTime > duration) {
				if (duration > 0) {
					addError(mLocalizer.msg("netTime",
							"Net playing time is longer than duration ({0} min.)", netTime
									- duration));
				} else {
					addWarning(mLocalizer.msg("netTimeAvailable",
							"Net play time is set, but duration missing."));
				}
			}
		}
	}

}
