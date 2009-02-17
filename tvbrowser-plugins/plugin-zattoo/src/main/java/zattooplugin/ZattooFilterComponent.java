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
package zattooplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

public final class ZattooFilterComponent extends PluginsFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ZattooFilterComponent.class);

  @Override
  public String getUserPresentableClassName() {
    return mLocalizer.msg("name", "Zattoo channels");
  }

  public boolean accept(final Program program) {
    return ZattooPlugin.getInstance().isChannelSupported(program.getChannel());
  }

  public int getVersion() {
    return 0;
  }

  public void read(final ObjectInputStream arg0, final int arg1)
      throws IOException,
      ClassNotFoundException {
    // no filter settings
  }

  public void write(final ObjectOutputStream arg0) throws IOException {
    // no filter settings
  }

  public JPanel getSettingsPanel() {
    final JPanel descPanel = new JPanel();
    descPanel.add(new JLabel(mLocalizer.msg("desc",
        "Accepts all programs of channels which are supported by Zattoo.")));
    return descPanel;
  }

}
