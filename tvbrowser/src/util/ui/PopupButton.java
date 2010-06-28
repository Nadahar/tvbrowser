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
 *     $Date: 2009-10-10 14:10:29 +0200 (Sa, 10 Okt 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5994 $
 */
package util.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

/**
 * This is a Button that has a popup click filter. This is needed for
 * Mac OS. On Mac OS the popup is activated with command+click. If a button
 * has a mouse action that should show a popup, the normal action will be called
 * aswell.
 *
 * With this button the normal action handling will be prevented if a popup-click
 * is detected.
 */
public class PopupButton extends JButton implements MouseListener {
  boolean filtered = false;
  boolean pressedPopup = false;
  public PopupButton() {
    addMouseListener(this);
  }

  @Override
  protected void fireActionPerformed(ActionEvent event) {
    if (!filtered) {
      super.fireActionPerformed(event);
    } else {
      filtered = false;
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    pressedPopup = e.isPopupTrigger();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    filtered = e.isPopupTrigger() || pressedPopup;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
