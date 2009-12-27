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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import devplugin.Plugin;

/**
 * @author bananeweizen
 * 
 */
public abstract class TVBrowserAction extends AbstractAction {

  /** The localizer for this class. */
  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVBrowserAction.class);

  private ImageIcon mSmallIcon;
  private KeyStroke mAccelerator;

  private String mKey;

  public TVBrowserAction(final String key, final ImageIcon smallIcon, final ImageIcon largeIcon, final int keyCode,
      final int keyMask, final int toolbarActionType) {
    mKey = key;
    mSmallIcon = smallIcon;
    if (keyCode != 0) {
      mAccelerator = KeyStroke.getKeyStroke(keyCode, keyMask);
    }
    else {
      mAccelerator = null;
    }
    putValue(Action.NAME, getToolbarText());
    putValue(Action.SMALL_ICON, smallIcon);
    putValue(Plugin.BIG_ICON, largeIcon);
    putValue(Action.SHORT_DESCRIPTION, getToolbarTip());
    putValue(ToolBar.ACTION_TYPE_KEY, toolbarActionType);
    putValue(ToolBar.ACTION_ID_KEY, "#" + mKey);
    TVBrowserActions.register(this);
  }

  private String getToolbarTip() {
    String text = mLocalizer.msg(getKey() + ".toolbar.tip", "", false);
    if (text.indexOf('[') == 0) {
      text = getMenuHelpText();
    }
    return text;
  }

  public TVBrowserAction(final String key, final int keyCode, final int keyMask) {
    this(key, null, null, keyCode, keyMask, -1);
  }

  public String getToolbarText() {
    String text = mLocalizer.msg(getKey() + ".toolbar", "Toolbar: " + mKey, false);
    // if no toolbar text is available, take the menu text
    if (text.indexOf('[') == 0) {
      text = getMenuText().replace("&", "");
      while (text.endsWith(".")) {
        text = text.substring(0, text.length() - 1);
      }
    }
    return text;
  }

  public Icon getIcon() {
    return mSmallIcon;
  }

  public String getMenuText() {
    return mLocalizer.msg(getKey() + ".menu", "Menu: " + mKey);
  }

  public String getMenuHelpText() {
    return mLocalizer.msg(getKey() + ".menu.info", "");
  }

  public KeyStroke getAccelerator() {
    return mAccelerator;
  }

  public String getToolbarIdentifier() {
    return (String) getValue(ToolBar.ACTION_ID_KEY);
  }

  protected void showPopupMenu() {
    MainFrame.getInstance().getToolbar().showPopupMenu(this);
  }
  
  protected String getKey() {
    return mKey;
  }

  public boolean useEllipsis() {
    return false;
  }
}
