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
   * Konstrukturen: einer f�r einen Frame als Besitzer und einer f�r einen
   * Dialog als Besitzer. Wenn man nun das �berliegende Fenster gar nicht kennt,
   * dann hat man ein Problem (Z.B. Wenn man einen Button schreibt, der
   * manchmal eine Fehlermeldung zeigt). Bisher habe ich einfach den
   * Component-Pfad bis zum obersten Frame verfolgt
   * (@link UiToolkit#getFrameFor(Component)). Das ganze wird dann zum Problem,
   * wenn man in einem modalen Dialog einen nicht-modalen Dialog zeigt.
   * Denn dann kann man den nicht-modalen Dialog n�mlich erst dann wieder
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
   * <p>
   * If there is no visible modal dialog the root frame will be returned.
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
   * <p>
   * If there is no visible modal child the window itself will be returned.
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
   * Gibt einen Button mit Icon und Schrift zur�ck, der so initialisiert ist,
   * da� man ihn gut f�r Symbolleisten nutzen kann (Rahmen nur bei Rollover
   * sichtbar usw.).
   * <P>
   * Wenn text und iconDateiname angegeben sind, dann wird text als TooltipText
   * gesetzt.
   *
   * @param text Der Text des Buttons (Kann null sein, wenn der Button keinen
   *        Text enthalten soll)
   * @param icon Das Icon des Buttons (Kann ebenfalls
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
  
 
  /**
   * Creates a text area that holds a help text.
   * 
   * @param msg The help text.
   * @return A text area containing the help text.
   */
  public static JTextArea createHelpTextArea(String msg) {
    JTextArea descTA = new JTextArea(msg);
    descTA.setEditable(false);
    descTA.setOpaque(false);
    descTA.setWrapStyleWord(true);
    descTA.setLineWrap(true);
    
    return descTA;
  }
  
  /**
   * Moves Selected Items from one List to another 
   * @param fromList Move from this List
   * @param toList Move into this List
   * @return Moved Elements
   */
  public static Object[] moveSelectedItems(JList fromList, JList toList) {
    DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
    DefaultListModel toModel = (DefaultListModel) toList.getModel();
    
    // get the selection
    int[] selection = fromList.getSelectedIndices();



    if (selection.length == 0) {
      return null;
    }

    Object[] objects = new Object[selection.length];
    for (int i=0; i<selection.length; i++) {
      objects[i] = fromModel.getElementAt(selection[i]);
    }

    // get the target insertion position
    int targetPos = toList.getMaxSelectionIndex();
    if (targetPos == -1) {
      targetPos = toModel.getSize();
    } else {
      targetPos++;
    }
    
    // move the elements
    for (int i = selection.length - 1; i >= 0; i--) {
      Object value = fromModel.remove(selection[i]);
      toModel.add(targetPos, value);
    }
    
    // change selection of the fromList
    if (fromModel.getSize() > 0) {
      int newSelection = selection[0];
      if (newSelection >= fromModel.getSize()) {
        newSelection = fromModel.getSize() - 1;
      }
      fromList.setSelectedIndex(newSelection);
    }

    // change selection of the toList
    toList.setSelectionInterval(targetPos, targetPos + selection.length - 1);
    
    // ensure the selection is visible
    toList.ensureIndexIsVisible(toList.getMaxSelectionIndex());
    toList.ensureIndexIsVisible(toList.getMinSelectionIndex());


    return objects;
  }
  
  /**
   * Move selected Items in the JList
   * @param list Move Items in this List
   * @param nrRows Move Items nrRows up/down
   */
  public static void moveSelectedItems(JList list, int nrRows) {
    DefaultListModel model = (DefaultListModel) list.getModel();
    
    // get the selection
    int[] selection = list.getSelectedIndices();
    if (selection.length == 0) {
      return;
    }
    
    System.out.println(selection.length);
    
    // Remove the selected items
    Object[] items = new Object[selection.length];
    for (int i = selection.length - 1; i >= 0; i--) {
      items[i] = model.remove(selection[i]);
    }
    
    // insert the elements at the target position
    int targetPos = selection[0] + nrRows;
    targetPos = Math.max(targetPos, 0);
    targetPos = Math.min(targetPos, model.getSize());
    for (int i = 0; i < items.length; i++) {
      model.add(targetPos + i, items[i]);
    }
    
    // change selection of the toList
    list.setSelectionInterval(targetPos, targetPos + selection.length - 1);
    
    // ensure the selection is visible
    list.ensureIndexIsVisible(list.getMaxSelectionIndex());
    list.ensureIndexIsVisible(list.getMinSelectionIndex());
  }  
  
}
