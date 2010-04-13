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
package tvbrowser.ui.mainframe.actions;

import javax.swing.ImageIcon;


public abstract class TVBrowserUpdateAction extends TVBrowserAction {

  private boolean mUpdate = false;

  public TVBrowserUpdateAction(final String key, final ImageIcon smallIcon, final ImageIcon largeIcon, final int keyCode,
      final int keyMask, final int toolbarActionType) {
    super(key, smallIcon, largeIcon, keyCode, keyMask, toolbarActionType);
  }

  @Override
  protected String getKey() {
    if (mUpdate) {
      return "stopUpdate";
    }
    return super.getKey();
  }

  public void setUpdating(boolean updating) {
    mUpdate = updating;
  };

  public boolean isUpdating() {
    return mUpdate;
  }
}
