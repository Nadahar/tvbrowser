/*
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

/**
 * this filter accepts all prgrams that have no description, ie where
 * the description field contains 'WirSchauen'.
 */
public class WirSchauenFilterComponent extends PluginsFilterComponent
{
  /**
   * the localizer.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(WirSchauenFilterComponent.class);


  /**
   * {@inheritDoc}
   * @see devplugin.PluginsFilterComponent#getUserPresentableClassName()
   */
  @Override
  public String getUserPresentableClassName()
  {
    return LOCALIZER.msg("name", "Missing description");
  }


  /**
   * returns true if the description field of the program contains 'WirSchauen'.
   * {@inheritDoc}
   * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
   */
  public boolean accept(final Program program) {
    final String description = program.getTextField(ProgramFieldType.DESCRIPTION_TYPE);
    if (description != null && description.contains("WirSchauen")) {
      return true;
    }
    return false;
  }


  /**
   * {@inheritDoc}
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream, int)
   */
  public void read(final ObjectInputStream stream, final int version) throws IOException, ClassNotFoundException {
    // no filter settings
  }

  /**
   * {@inheritDoc}
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(final ObjectOutputStream stream) throws IOException {
    // no filter settings
  }

}
