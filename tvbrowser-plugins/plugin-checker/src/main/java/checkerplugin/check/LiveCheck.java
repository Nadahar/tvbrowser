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
import devplugin.ProgramInfoHelper;

public class LiveCheck extends AbstractCheck {

	private static final Pattern LIVE_PATTERN = Pattern.compile("\\Wlive\\W");

	@Override
	protected void doCheck(final Program program) {
		if (!ProgramInfoHelper.bitSet(program.getInfo(), Program.INFO_LIVE)) {
			if (!findLiveText(program.getTitle())) {
				if (!findLiveText(program.getDescription())) {
					findLiveText(program.getShortInfo());
				}
			}
		}
	}

	private boolean findLiveText(final String text) {
		if (text == null) {
			return false;
		}
		Matcher matcher = LIVE_PATTERN.matcher(text);
		if (matcher.find()) {
			addError(mLocalizer.msg("live", "Not marked as 'live'."));
			return true;
		}
		return false;
	}

}
