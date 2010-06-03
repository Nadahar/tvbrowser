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
package filterviewplugin;

import java.awt.event.ActionEvent;

import util.ui.Localizer;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.ProgramFilter;

class SetFilterAction extends ContextMenuAction {

  private ProgramFilter mFilter;
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SetFilterAction.class);

  SetFilterAction(final ProgramFilter filter) {
    super(mLocalizer.msg("filter", "Set as active filter"));
    mFilter = filter;
  }

  public void actionPerformed(ActionEvent e) {
    Plugin.getPluginManager().getFilterManager().setCurrentFilter(mFilter);
  }

}
