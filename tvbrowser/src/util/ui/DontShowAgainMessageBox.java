/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2008-05-17 10:29:03 +0200 (Sa, 17 Mai 2008) $
 *   $Author: troggan $
 * $Revision: 4683 $
 */
package util.ui;

import java.awt.Component;

import tvbrowser.core.Settings;
import devplugin.Plugin;

/**
 * This class offers a messagebox that shows a dialog with a checkbox to disable the dialog
 *
 * @since 2.7
 */
public class DontShowAgainMessageBox {
  /**
   * Shows a messagebox with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a messagebox
   * @param messageBoxId the unique id for the messagebox
   * @param parent parent frame for the messagebox
   * @param message message for the box
   * @param title title for the box
   * @param messageType MessageType, values are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}
   * @since 2.7
   */
  public static void dontShowAgainMessageBox(Plugin plugin, String messageBoxId, Component parent, String message, String title, int messageType) {
    tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ messageBoxId, parent, message, title, messageType);
  }

  /**
   * Shows a messagebox with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a messagebox
   * @param messageBoxId the unique id for the messagebox
   * @param parentComponent parent frame for the messagebox
   * @param message message for the box
   * @param title title for the box
   * @since 2.7
   */
  public static void dontShowAgainMessageBox(Plugin plugin, String messageBoxId, Component parentComponent, String message, String title) {
    tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ messageBoxId, parentComponent, message, title);
  }

  /**
   * Shows a messagebox with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a messagebox
   * @param messageBoxId the unique id for the messagebox
   * @param parentComponent parent frame for the messagebox
   * @param message message for the box
   * @since 2.7
   */
  public static void dontShowAgainMessageBox(Plugin plugin, String messageBoxId, Component parentComponent, String message) {
    tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ messageBoxId, parentComponent, message);
  }

  /**
   * @param plugin plugin that uses the messagebox
   * @param messageBoxId check this messageBoxId
   * @return true, if the messageboxid is set and no dialog will be shown if {#dontShowAgainMessageBox} is called
   * @since 2.7
   */
  public static boolean isMessageBoxIdSet(Plugin plugin, String messageBoxId) {
    return Settings.propHiddenMessageBoxes.containsItem(plugin.getId() + "."+ messageBoxId);
  }

  /**
   * Set the value for the messageBoxId. This manipulates if a dialog will be shown if {#dontShowAgainMessageBox} is called
   *
   * @param plugin plugin that uses the messagebox
   * @param messageBoxId check this messageBoxId
   * @param value new value, <code>true</code> to disable the dialog, <code>false</code>, to enable the dialog
   * @since 2.7
   */
  public static void setMessageBoxId(Plugin plugin, String messageBoxId, boolean value) {
    if (value) {
      Settings.propHiddenMessageBoxes.removeItem(plugin.getId() + "."+ messageBoxId);
    } else {
      Settings.propHiddenMessageBoxes.addItem(plugin.getId() + "."+ messageBoxId);
    }
  }

  private DontShowAgainMessageBox() {
    // create this message box via static functions only
  }
}
