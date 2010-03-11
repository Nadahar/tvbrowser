/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 * SVN information:
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import devplugin.Program;
import devplugin.ProgramRatingIf;
import javax.swing.Icon;
import util.ui.Localizer;

public class GolemRating implements ProgramRatingIf {
  static final Localizer mLocalizer = Localizer.getLocalizerFor(GolemPlugin.class);

  public String getName() {
    return mLocalizer.msg("name", "Golem.de watches");
  }

  public Icon getIcon() {
    return GolemPlugin.getInstance().getPluginIcon();
  }

  public int getRatingForProgram(Program program) {
    if (GolemPlugin.getInstance().getProgramList().contains(program)) {
      return 10;
    }

    return 0;
  }

  public Icon getIconForProgram(Program program) {
    if (GolemPlugin.getInstance().getProgramList().contains(program)) {
      return GolemPlugin.getInstance().getPluginIcon();
    }
    return null;
  }

  public boolean hasDetailsDialog() {
    return false;
  }

  public void showDetailsFor(Program program) {
  }
}
