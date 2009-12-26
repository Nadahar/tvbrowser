/*
 * TV-Browser
 * Copyright (C) TV-Browser-Tream (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Component;

import javax.swing.JOptionPane;

import tvbrowser.core.Settings;
import devplugin.Plugin;

/**
 * This class offers an option box that shows a dialog with a checkbox to disable the dialog
 *
 * @since 3.0
 */
public class DontShowAgainOptionBox {
  /**
   * Shows an option box with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a option box
   * @param optionBoxId the unique id for the option box
   * @param parent parent frame for the option box
   * @param message message for the box
   * @param title title for the box
   * @param messageType MessageType, values are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}
   * @param optionType The option type of the option dialog, values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @param options The options to show on the option buttons.
   * @param initialValue The option initially selected.
   * @param dontShowAgainLabel The label for the check box or, <code>null</code> for the default label.
   * 
   * @return The result of the option dialog, possible values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @since 3.0
   */
  public static int dontShowAgainMessageBox(Plugin plugin, String optionBoxId, Component parent, String message, String title, int messageType, int optionType, Object[] options,
      Object initialValue, String dontShowAgainLabel) {
    return tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ optionBoxId, parent, message, title, messageType, optionType, options, initialValue, dontShowAgainLabel);
  }
  
  /**
   * Shows an option box with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a option box
   * @param optionBoxId the unique id for the option box
   * @param parent parent frame for the option box
   * @param message message for the box
   * @param title title for the box
   * @param messageType MessageType, values are {#javax.swing.JOptionPane.ERROR_MESSAGE}, {#javax.swing.JOptionPane.INFORMATION_MESSAGE}, {#javax.swing.JOptionPane.WARNING_MESSAGE}
   * 
   * @return The result of the option dialog, possible values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @since 3.0
   */
  public static int dontShowAgainMessageBox(Plugin plugin, String optionBoxId, Component parent, String message, String title, int messageType) {
    return tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ optionBoxId, parent, message, title, messageType);
  }

  /**
   * Shows a option box with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a option box
   * @param optionBoxId the unique id for the option box
   * @param parentComponent parent frame for the option box
   * @param message message for the box
   * @param title title for the box
   * 
   * @return The result of the option dialog, possible values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @since 2.7
   */
  public static int dontShowAgainMessageBox(Plugin plugin, String optionBoxId, Component parentComponent, String message, String title) {
    return tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ optionBoxId, parentComponent, message, title);
  }

  /**
   * Shows a option box with a checkbox that enables the user to hide the dialog.
   *
   * If the user sets the checkbox, this function will never show the dialog again. You
   * can simply call this method and don't have to deal with the fact if the dialog has
   * to be shown or not
   *
   * @param plugin Plugin that wants to show a option box
   * @param optionBoxId the unique id for the option box
   * @param parentComponent parent frame for the option box
   * @param message message for the box
   * 
   * @return The result of the option dialog, possible values are {@link JOptionPane#YES_OPTION}, {@link JOptionPane#YES_NO_OPTION}, {@link JOptionPane#YES_NO_CANCEL_OPTION}, {@link JOptionPane#OK_OPTION},
   *                   {@link JOptionPane#OK_CANCEL_OPTION}, {@link JOptionPane#DEFAULT_OPTION}.
   * @since 3.0
   */
  public static int dontShowAgainMessageBox(Plugin plugin, String optionBoxId, Component parentComponent, String message) {
    return tvbrowser.ui.DontShowAgainOptionBox.showOptionDialog(plugin.getId() + "."+ optionBoxId, parentComponent, message);
  }

  /**
   * @param plugin plugin that uses the option box
   * @param optionBoxId check this optionBoxId
   * @return true, if the optionBoxId is set and no dialog will be shown if {#dontShowAgainOptionBox} is called
   * @since 3.0
   */
  public static boolean isOptionBoxIdSet(Plugin plugin, String optionBoxId) {
    return Settings.propHiddenMessageBoxes.containsItem(plugin.getId() + "."+ optionBoxId);
  }

  /**
   * Set the value for the optionBoxId. This manipulates if a dialog will be shown if {#dontShowAgainOptionBox} is called
   *
   * @param plugin plugin that uses the option box
   * @param optionBoxId check this optionBoxId
   * @param value new value, <code>true</code> to disable the dialog, <code>false</code>, to enable the dialog
   * @since 3.0
   */
  public static void setOptionBoxId(Plugin plugin, String optionBoxId, boolean value) {
    if (value) {
      Settings.propHiddenMessageBoxes.removeItem(plugin.getId() + "."+ optionBoxId);
    } else {
      Settings.propHiddenMessageBoxes.addItem(plugin.getId() + "."+ optionBoxId);
    }
  }
}
