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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Provides utilities for UI stuff.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class UiUtilities {

  /** The helper label. */  
  private static final JLabel HELPER_LABEL = new JLabel();
  
  /** The border to use for dialogs. */  
  public static final Border DIALOG_BORDER
    = BorderFactory.createEmptyBorder(10, 10, 0, 10);

  public static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);
  

  
  /**
   * Centers a window to its parent frame and shows it.
   * <p>
   * If the window has no parent frame it will be centered to the screen.
   *
   * @param win The window to center and show.
   */  
  public static void centerAndShow(Window win) {
    Dimension wD = win.getSize();
    Dimension frameD;
    Point framePos;
    Frame frame = JOptionPane.getFrameForComponent(win);

    // Should this window be centered to its parent frame?
    boolean centerToParentFrame = (frame != null) && (frame != win) && frame.isShowing();
    
    // Center to parent frame or to screen
    if (centerToParentFrame) {
      frameD = frame.getSize();
      framePos = frame.getLocation();
    } else {
      frameD = Toolkit.getDefaultToolkit().getScreenSize();
      framePos = new Point(0, 0);
    }

    Point wPos = new Point(framePos.x + (frameD.width - wD.width) / 2,
                           framePos.y + (frameD.height - wD.height) / 2);
    wPos.x = Math.max(0, wPos.x); // Make x > 0
    wPos.y = Math.max(0, wPos.y); // Make y > 0
    win.setLocation(wPos);
    win.show();
  }

  
  
  /**
   * Der {@link JDialog} hat einen Riesennachteil: er hat zwei verschiedenen
   * Konstrukturen: einer für einen Frame als Besitzer und einer für einen
   * Dialog als Besitzer. Wenn man nun das überliegende Fenster gar nicht kennt,
   * dann hat man ein Problem (Z.B. Wenn man einen Button schreibt, der
   * manchmal eine Fehlermeldung zeigt). Bisher habe ich einfach den
   * Component-Pfad bis zum obersten Frame verfolgt
   * (@link UiToolkit#getFrameFor(Component)). Das ganze wird dann zum Problem,
   * wenn man in einem modalen Dialog einen nicht-modalen Dialog zeigt.
   * Denn dann kann man den nicht-modalen Dialog nämlich erst dann wieder
   * bedienen, wenn der modale zu ist.
   *
   * @param parent A component in the component tree where the dialog should be created for.
   * @param modal Should the new dialog be modal?
   * @return A new JDialog.
   */
  public static JDialog createDialog(Component parent, boolean modal) {
    Window parentWin = getBestDialogParent(parent);

    JDialog dlg;
    if ((parentWin instanceof Frame) || (parentWin == null)) {
      dlg = new JDialog((Frame) parentWin, modal);
    } else if (parentWin instanceof Dialog) {
      dlg = new JDialog((Dialog) parentWin, modal);
    } else {
      throw new IllegalArgumentException("parent has surrounding Window of "
        + "unknown type: " + parentWin.getClass());
    }

    return dlg;
  }

  
  
  /**
   * Gets the best dialog parent for a new JDialog. The best parent is the last
   * visible modal dialog in the component tree.
   *
   * @param parent One component of the component tree.
   * @return the best dialog parent for a new JDialog.
   */  
  public static Window getBestDialogParent(Component parent) {
    Frame root = JOptionPane.getFrameForComponent(parent);
    return getLastModalChildOf(root);
  }

  
  
  /**
   * Gets the last visible modal child dialog of the specified window.
   *
   * @param parent The window to get the child from.
   * @return the last visible modal child dialog of the specified window.
   */  
  public static Window getLastModalChildOf(Window parent) {
    Window[] children = parent.getOwnedWindows();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof Dialog) {
        Dialog dlg = (Dialog) children[i];
        if (dlg.isVisible() && dlg.isModal()) {
          return getLastModalChildOf(dlg);
        }
      }
    }
    
    // this is the last window
    return parent;
  }

  
  
  /**
   * Gibt einen Button mit Icon und Schrift zurück, der so initialisiert ist,
   * daß man ihn gut für Symbolleisten nutzen kann (Rahmen nur bei Rollover
   * sichtbar usw.).
   * <P>
   * Wenn text und iconDateiname angegeben sind, dann wird text als TooltipText
   * gesetzt.
   *
   * @param text Der Text des Buttons (Kann null sein, wenn der Button keinen
   *        Text enthalten soll)
   * @param iconDateiname Der Dateiname des Icons des Buttons (Kann ebenfalls
   *        null sein, wenn der Button kein Icon enthalten soll).
   */
  public static JButton createToolBarButton(String text, Icon icon) {
    final JButton btn;
    if (icon == null) {
      btn = new JButton(text);
    } else {
      btn = new JButton(icon);
      btn.setToolTipText(text);
      btn.setBorderPainted(false);
      btn.setMargin(ZERO_INSETS);

      btn.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          if (btn.isEnabled()) {
            btn.setBorderPainted(true);
          }
        }
        public void mouseExited(MouseEvent e) {
          btn.setBorderPainted(false);
        }
      });
    }
    btn.setFocusPainted(false);

    return btn;
  }

  
  
  
  /**
   * Gets the width of the specified String.
   *
   * @param str The String to get the width for.
   * @param font The font being the base of the measure.
   * @return the width of the specified String.
   */
  public static int getStringWidth(Font font, String str) {
    if (str == null) {
      return 0;
    }
    
    FontMetrics metrics = HELPER_LABEL.getFontMetrics(font);
    return metrics.stringWidth(str);
  }

  
  
  /**
   * Gets the width of the specified char array.
   *
   * @param chars The char array to get the width for.
   * @param offset The offset where to start.
   * @param length The length of the measure.
   * @param font The font being the base of the measure.
   * @return the width of the specified char array.
   */
  public static int getCharsWidth(Font font, char[] chars, int offset, int length) {
    if (chars == null) {
      return 0;
    }
    
    FontMetrics metrics = HELPER_LABEL.getFontMetrics(font);
    return metrics.charsWidth(chars, offset, length);
  }
  
 
  
}
