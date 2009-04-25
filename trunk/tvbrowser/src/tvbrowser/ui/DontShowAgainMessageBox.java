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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import tvbrowser.core.Settings;

public class DontShowAgainMessageBox {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DontShowAgainMessageBox.class);

  
  public static void showMessageDialog(String messageBoxId, Component parent,
      String message, String title, int messageType) {
    if (Settings.propHiddenMessageBoxes.containsItem(messageBoxId)) {
      return;
    }
    // have some space between message and checkbox
    if (!message.endsWith("\n\n")) {
      message = message.concat("\n\n");
    }
    JCheckBox askAgain = new JCheckBox(mLocalizer.msg("dontShowAgain", "Don't show this message again"));
    Object[] shownObjects = new Object[2];
    shownObjects[0] = message;
    shownObjects[1] = askAgain;
    JOptionPane.showMessageDialog(parent, shownObjects, title, messageType);
    if (askAgain.isSelected()) {
      Settings.propHiddenMessageBoxes.addItem(messageBoxId);
    }
  }

  public static void showMessageDialog(String messageBoxId, Component parentComponent,
      String message, String title) {
    showMessageDialog(messageBoxId, parentComponent, message, title,
        JOptionPane.INFORMATION_MESSAGE);
  }

  public static void showMessageDialog(String messageBoxId, Component parentComponent,
      String message) {
    showMessageDialog(messageBoxId, parentComponent, message, UIManager
        .getString("OptionPane.messageDialogTitle"));
  }
}