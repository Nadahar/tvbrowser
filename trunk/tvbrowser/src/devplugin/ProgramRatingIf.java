/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2008-02-05 22:45:05 +0100 (Di, 05 Feb 2008) $
 *   $Author: bananeweizen $
 * $Revision: 4246 $
 */
package devplugin;

import javax.swing.Icon;

/**
 * This Interface makes it possible for plugins to offer rating infos to other
 * plugins
 *
 * @since 2.7
 */
public interface ProgramRatingIf {

  /**
   * Name for this interface that is displayed by other plugins
   * @return Name
   */
  public String getName();

  /**
   * Icon for this interface that is displayed by other plugins
   * @return Icon
   */
  public Icon getIcon();

  /**
   * Returns a rating for this program. The rating must
   * be between 0 and 100, -1 if no rating is available
   *
   * @param program program to get rating for
   * @return rating for the program (between 0 and 100), -1 if no rating is available
   */
  public int getRatingForProgram(final Program program);

  /**
   * Get Icon for the rating.
   *
   * This gives plugins the opportunity to show it's icon for the specific rating
   *
   * @param program program to get icon for
   * @return icon for rating, <code>null</code> if no rating is available
   */
  public Icon getIconForProgram(final Program program);

  /**
   * Has this plugin a dialog that displays details about this rating?
   *
   * @return <code>true</code> if the plugin is able to show details of the rating
   */
  public boolean hasDetailsDialog();

  /**
   * shows a detail dialog for a rating
   * @param program show dialog for this program
   */
  public void showDetailsFor(final Program program);

}