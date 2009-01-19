/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *
 */
package tvbrowser.ui.waiting.dlgs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This Dialog is shown while the Settings-Dialog is loading
 * the Channels.
 * 
 * Since 2.3 we changed the group-concept. All groups are activated at startup, so we
 * needed a mechanism to load not needed channels during a later phase.
 * 
 * The Settings-Dialog now waits till the channels are loaded completely and then shows itself
 * 
 * 
 * @author bodum
 *
 */
public class SettingsWaitingDialog extends JDialog {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SettingsWaitingDialog.class);

  public SettingsWaitingDialog(Window dialog) {
    super(dialog);
    setModal(true);
    createGui();
  }

  private void createGui () {
    setUndecorated(true);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    panel.setLayout(new FormLayout("3dlu, pref, 3dlu", "3dlu, pref, 3dlu, pref, 3dlu"));
    CellConstraints cc = new CellConstraints();

    JLabel header = new JLabel(mLocalizer.msg("waitingHeader", "Listing the not subscribed channels"));
    header.setFont(header.getFont().deriveFont(Font.BOLD));

    panel.add(header, cc.xy(2, 2));

    panel.add(new JLabel(mLocalizer.msg("pleaseWait", "Please wait for the completing of the list.")), cc.xy(2, 4));

    pack();
  }

}
