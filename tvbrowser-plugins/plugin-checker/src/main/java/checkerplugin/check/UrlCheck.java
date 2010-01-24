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

public class UrlCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
    final String url = program.getTextField(ProgramFieldType.URL_TYPE);
    if (url != null) {
      if (url.isEmpty()) {
      	addError(mLocalizer.msg("urlEmpty", "URL length is zero."));
      } else {
        if (!Character.isLetterOrDigit(url.charAt(0))) {
          addError(mLocalizer.msg("urlFormat",
              "URL not correctly formatted."));
        }
      }
    }
	}

}
