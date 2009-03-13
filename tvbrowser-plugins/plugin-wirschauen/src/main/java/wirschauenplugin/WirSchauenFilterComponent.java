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
package wirschauenplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.ui.Localizer;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class WirSchauenFilterComponent extends PluginsFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(WirSchauenFilterComponent.class);

  @Override
  public String getUserPresentableClassName() {
    return mLocalizer.msg("name", "Missing description");
  }

  public boolean accept(final Program program) {
    final String description = program
        .getTextField(ProgramFieldType.DESCRIPTION_TYPE);
    if (description != null && description.contains("WirSchauen")) {
      return true;
    }
    return false;
  }

  public int getVersion() {
    return 0;
  }

  public void read(final ObjectInputStream stream, final int version)
      throws IOException, ClassNotFoundException {
    // no filter settings
  }

  public void write(final ObjectOutputStream stream) throws IOException {
    // no filter settings
  }

}
