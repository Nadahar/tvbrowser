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
package genreplugin;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import util.ui.Localizer;
import devplugin.ContextMenuAction;

public class HideGenreAction extends ContextMenuAction implements Action {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(HideGenreAction.class);

  private String genre;

  public HideGenreAction(final String genre) {
    this.genre = genre;
    setText(mLocalizer.msg("label","Hide genre"));
    setSmallIcon(GenrePlugin.getInstance().createImageIcon("actions", "list-remove", 16));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    GenrePlugin.getInstance().hideGenre(genre);
    GenrePlugin.getInstance().updateRootNode();
  }

}
