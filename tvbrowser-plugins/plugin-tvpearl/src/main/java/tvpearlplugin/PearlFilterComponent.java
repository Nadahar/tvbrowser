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
package tvpearlplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.ui.Localizer;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

public class PearlFilterComponent extends PluginsFilterComponent {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PearlFilterComponent.class);

	@Override
	public String getUserPresentableClassName() {
		return mLocalizer.msg("name", "TV Pearls");
	}

	public boolean accept(final Program program) {
		return TVPearlPlugin.getInstance().hasPearl(program);
	}

	public int getVersion() {
		return 1;
	}

	public void read(ObjectInputStream in, int version) throws IOException,
			ClassNotFoundException {
		// nothing to read
	}

	public void write(ObjectOutputStream out) throws IOException {
		// nothing to store
	}

}
