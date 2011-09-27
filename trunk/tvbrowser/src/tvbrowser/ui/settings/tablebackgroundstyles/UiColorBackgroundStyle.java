/*
* TV-Browser
* Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
*     $Date$
*   $Author$
* $Revision$
*/

package tvbrowser.ui.settings.tablebackgroundstyles;

import javax.swing.JPanel;

/**
 * @author René Mach
 */
public class UiColorBackgroundStyle implements TableBackgroundStyle {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(UiColorBackgroundStyle.class);

  public UiColorBackgroundStyle() {

  }

  public boolean hasContent() {
    return false;
  }

  public JPanel createSettingsContent() {
    return null;
  }

  public void storeSettings() {
  }

  public String getName() {
    return mLocalizer.msg("style","Theme color");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "uiColor";
  }

}
