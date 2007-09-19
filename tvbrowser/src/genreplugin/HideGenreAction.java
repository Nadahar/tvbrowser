/*
 * GenrePlugin by Michael Keppler
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * VCS information:
 *     $Date: 2007-09-15 19:13:12 +0200 (Sa, 15 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 1 $
 */
package genreplugin;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import util.ui.Localizer;

import devplugin.ContextMenuAction;

public class HideGenreAction extends ContextMenuAction implements Action {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(HideGenreAction.class);

  private String genre;

  public HideGenreAction(String genre) {
    this.genre = genre;
    setText(mLocalizer.msg("label","Hide genre"));
    setSmallIcon(GenrePlugin.getInstance().createImageIcon("actions", "view-filter", 16));
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    GenrePlugin.getInstance().hideGenre(genre);
    GenrePlugin.getInstance().updateRootNode();
  }

}
