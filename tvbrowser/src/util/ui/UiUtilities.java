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

import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.misc.OperatingSystem;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Provides utilities for UI stuff.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class UiUtilities {

  /** The helper label. */
  private static final JLabel HELPER_LABEL = new JLabel();

  /** The border to use for dialogs. */
  public static final Border DIALOG_BORDER = BorderFactory.createEmptyBorder(
      10, 10, 0, 10);

  public static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

  /**
   * Centers a window to its parent frame and shows it.
   * <p>
   * If the window has no parent frame it will be centered to the screen.
   * 
   * @param win
   *          The window to center and show.
   */
  public static void centerAndShow(Window win) {
    Dimension wD = win.getSize();
    Dimension frameD;
    Point framePos;
    Frame frame = JOptionPane.getFrameForComponent(win);

    // Should this window be centered to its parent frame?
    boolean centerToParentFrame = (frame != null) && (frame != win)
        && frame.isShowing();

    // Center to parent frame or to screen
    if (centerToParentFrame) {
      frameD = frame.getSize();
      framePos = frame.getLocation();
    } else {
      GraphicsEnvironment ge = GraphicsEnvironment
          .getLocalGraphicsEnvironment();
      GraphicsDevice[] gs = ge.getScreenDevices();
      // dual head, use first screen
      if (gs.length > 1) {
        try {
          GraphicsDevice gd = gs[0];
          GraphicsConfiguration config = gd.getConfigurations()[0];
          frameD = config.getBounds().getSize();
          framePos = config.getBounds().getLocation();
        } catch (RuntimeException e) {
          frameD = Toolkit.getDefaultToolkit().getScreenSize();
          framePos = new Point(0, 0);
        }
      }
      // single screen only
      else {
        frameD = Toolkit.getDefaultToolkit().getScreenSize();
        framePos = new Point(0, 0);
      }
    }

    Point wPos = new Point(framePos.x + (frameD.width - wD.width) / 2,
        framePos.y + (frameD.height - wD.height) / 2);
    wPos.x = Math.max(0, wPos.x); // Make x > 0
    wPos.y = Math.max(0, wPos.y); // Make y > 0
    win.setLocation(wPos);
    win.setVisible(true);
  }

  /**
   * Der {@link JDialog} hat einen Riesennachteil: er hat zwei verschiedenen
   * Konstrukturen: einer f�r einen Frame als Besitzer und einer f�r einen
   * Dialog als Besitzer. Wenn man nun das �berliegende Fenster gar nicht
   * kennt, dann hat man ein Problem (Z.B. Wenn man einen Button schreibt, der
   * manchmal eine Fehlermeldung zeigt). Bisher habe ich einfach den
   * Component-Pfad bis zum obersten Frame verfolgt (@link
   * UiToolkit#getFrameFor(Component)). Das ganze wird dann zum Problem, wenn
   * man in einem modalen Dialog einen nicht-modalen Dialog zeigt. Denn dann
   * kann man den nicht-modalen Dialog n�mlich erst dann wieder bedienen, wenn
   * der modale zu ist.
   * 
   * @param parent
   *          A component in the component tree where the dialog should be
   *          created for.
   * @param modal
   *          Should the new dialog be modal?
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
   * @param parent
   *          One component of the component tree.
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
   * @param parent
   *          The window to get the child from.
   * @return the last visible modal child dialog of the specified window.
   */
  public static Window getLastModalChildOf(Window parent) {
    Window[] children = parent.getOwnedWindows();
    for (Window child : children) {
      if (child instanceof Dialog) {
        Dialog dlg = (Dialog) child;
        if (dlg.isVisible() && dlg.isModal()) {
          return getLastModalChildOf(dlg);
        }
      }
    }

    // this is the last window
    return parent;
  }

  /**
   * Gets if a dialog child of the given window is modal.
   * 
   * @param parent
   *          The window to check the children of.
   * @return <code>True</code> if a child is modal, <code>false</code>
   *         otherwise.
   * 
   * @since 2.7
   */
  public static boolean containsModalDialogChild(Window parent) {
    Window[] children = parent.getOwnedWindows();

    for (Window child : children) {
      if (containsModalDialogChild(child)) {
        return true;
      }
    }

    return (parent instanceof JDialog && parent.isVisible() && ((JDialog) parent)
        .isModal());
  }

  /**
   * Gibt einen Button mit Icon und Schrift zur�ck, der so initialisiert ist,
   * da� man ihn gut f�r Symbolleisten nutzen kann (Rahmen nur bei Rollover
   * sichtbar usw.).
   * <P>
   * Wenn text und iconDateiname angegeben sind, dann wird text als TooltipText
   * gesetzt.
   * 
   * @param text
   *          Der Text des Buttons (Kann null sein, wenn der Button keinen Text
   *          enthalten soll)
   * @param icon
   *          Das Icon des Buttons (Kann ebenfalls null sein, wenn der Button
   *          kein Icon enthalten soll).
   * @return button
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
        @Override
        public void mouseEntered(MouseEvent e) {
          if (btn.isEnabled()) {
            btn.setBorderPainted(true);
          }
        }

        @Override
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
   * @param str
   *          The String to get the width for.
   * @param font
   *          The font being the base of the measure.
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
   * @param chars
   *          The char array to get the width for.
   * @param offset
   *          The offset where to start.
   * @param length
   *          The length of the measure.
   * @param font
   *          The font being the base of the measure.
   * @return the width of the specified char array.
   */
  public static int getCharsWidth(Font font, char[] chars, int offset,
      int length) {
    if (chars == null) {
      return 0;
    }

    FontMetrics metrics = HELPER_LABEL.getFontMetrics(font);
    return metrics.charsWidth(chars, offset, length);
  }

  /**
   * Creates a text area that holds a help text.
   * 
   * @param msg
   *          The help text.
   * @return A text area containing the help text.
   */
  public static JTextArea createHelpTextArea(String msg) {
    JTextArea descTA = new JTextArea(msg);
    descTA.setBorder(BorderFactory.createEmptyBorder());
    descTA.setFont(new JLabel().getFont());
    descTA.setEditable(false);
    descTA.setOpaque(false);
    descTA.setWrapStyleWord(true);
    descTA.setLineWrap(true);
    descTA.setFocusable(false);
    
    Color bg = new JPanel().getBackground();
    
    descTA.setBackground(new Color(bg.getRed(),bg.getGreen(),bg.getBlue()));

    return descTA;
  }

  /**
   * Creates a Html EditorPane that holds a HTML-Help Text
   * 
   * Links will be displayed and are clickable
   * 
   * @param html
   *          HTML-Text to display
   * @return EditorPane that holds a Help Text
   * @since 2.2
   */
  public static JEditorPane createHtmlHelpTextArea(String html) {
    return createHtmlHelpTextArea(html, new JPanel().getBackground());
  }
  
  /**
   * Creates a Html EditorPane that holds a HTML-Help Text
   * 
   * Links will be displayed and are clickable
   * 
   * @param html
   *          HTML-Text to display
   * @param background The color for the background.
   * @return EditorPane that holds a Help Text
   * @since 2.7.2
   */
  public static JEditorPane createHtmlHelpTextArea(String html, Color background) {
    return createHtmlHelpTextArea(html, new HyperlinkListener() {
      private String mTooltip;

      public void hyperlinkUpdate(HyperlinkEvent evt) {
        JEditorPane pane = (JEditorPane) evt.getSource();
        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          mTooltip = pane.getToolTipText();
          pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          if (evt.getURL() != null) {
            pane.setToolTipText(evt.getURL().toExternalForm());
          }
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
          pane.setCursor(Cursor.getDefaultCursor());
          pane.setToolTipText(mTooltip);
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    },background);
  }

  /**
   * Creates a Html EditorPane that holds a HTML-Help Text.
   * 
   * Add a Listener if you want to have clickable Links
   * 
   * @param html
   *          HTML-Text to display
   * @param listener
   *          Link-Listener for this HelpText
   * @return EditorPane that holds a Help Text
   * @since 2.2
   */
  public static JEditorPane createHtmlHelpTextArea(String html,
      HyperlinkListener listener) {
    return createHtmlHelpTextArea(html,listener,new JPanel().getBackground());
  }
  
  /**
   * Creates a Html EditorPane that holds a HTML-Help Text.
   * 
   * Add a Listener if you want to have clickable Links
   * 
   * @param html
   *          HTML-Text to display
   * @param listener
   *          Link-Listener for this HelpText
   * @param background The color for the background.
   * @return EditorPane that holds a Help Text
   * @since 2.7.2
   */
  public static JEditorPane createHtmlHelpTextArea(String html,
      HyperlinkListener listener, Color background) {
    // Quick "hack". Remove HTML-Code and replace it with Code that includes the
    // correct Font
    if (html.indexOf("<html>") >= 0) {
      html = html
          .substring(html.indexOf("<html>") + 6, html.indexOf("</html>"));
    }
    Font font = new JLabel().getFont();
    
    html = "<html><div style=\"color:#000000;font-family:" + font.getName()
        + "; font-size:" + font.getSize() +";background-color:rgb(" + background.getRed() + "," + background.getGreen() + "," + background.getBlue() + ");\">" + html + "</div></html>";
    
    final JEditorPane pane = new JEditorPane("text/html", html);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.setEditable(false);
    pane.setFont(font);
    pane.setOpaque(false);
    pane.setFocusable(false);

    if (listener != null) {
      pane.addHyperlinkListener(listener);
    }
    return pane;
  }

  /**
   * Moves Selected Items from one List to another
   * 
   * @param fromList
   *          Move from this List
   * @param toList
   *          Move into this List
   * @return Moved Elements
   */
  public static Object[] moveSelectedItems(JList fromList, JList toList) {
    DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
    DefaultListModel toModel = (DefaultListModel) toList.getModel();

    // get the selection
    int[] selection = fromList.getSelectedIndices();

    if (selection.length == 0) {
      return new Object[] {};
    }

    Object[] objects = new Object[selection.length];
    for (int i = 0; i < selection.length; i++) {
      objects[i] = fromModel.getElementAt(selection[i]);
    }

    // get the target insertion position
    int targetPos = toList.getMaxSelectionIndex();
    if (targetPos == -1) {
      targetPos = toModel.getSize();
    } else {
      targetPos++;
    }

    // suppress updates on both lists
    if (selection.length >= 5) {
      fromList.setModel(new DefaultListModel());
      toList.setModel(new DefaultListModel());
    }

    // move the elements
    for (int i = selection.length - 1; i >= 0; i--) {
      Object value = fromModel.remove(selection[i]);
      toModel.add(targetPos, value);
    }

    if (selection.length >= 5) {
      fromList.setModel(fromModel);
      toList.setModel(toModel);
    }

    // change selection of the fromList
    if (fromModel.getSize() > 0) {
      int newSelection = selection[0];
      if (newSelection >= fromModel.getSize()) {
        newSelection = fromModel.getSize() - 1;
      }
      fromList.setSelectedIndex(newSelection);
    }

    if (selection.length >= 5) {
      fromList.repaint();
      fromList.revalidate();
      toList.repaint();
      toList.revalidate();
    }

    // change selection of the toList
    toList.setSelectionInterval(targetPos, targetPos + selection.length - 1);

    // ensure the selection is visible
    toList.ensureIndexIsVisible(toList.getMaxSelectionIndex());
    toList.ensureIndexIsVisible(toList.getMinSelectionIndex());

    return objects;
  }

  /**
   * Moves Selected Items from one List to another
   * 
   * @param fromList
   *          Move from this List
   * @param toList
   *          Move into this List
   * @param row
   *          The target row where to insert
   * @return Moved Elements
   */
  public static Object[] moveSelectedItems(JList fromList, JList toList, int row) {
    DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
    DefaultListModel toModel = (DefaultListModel) toList.getModel();

    // get the selection
    int[] selection = fromList.getSelectedIndices();

    if (selection.length == 0) {
      return new Object[] {};
    }

    Object[] objects = new Object[selection.length];
    for (int i = 0; i < selection.length; i++) {
      objects[i] = fromModel.getElementAt(selection[i]);
    }

    // move the elements
    for (int i = selection.length - 1; i >= 0; i--) {
      Object value = fromModel.remove(selection[i]);
      toModel.insertElementAt(value, row);
    }

    // change selection of the fromList
    if (fromModel.getSize() > 0) {
      int newSelection = selection[0];
      if (newSelection >= fromModel.getSize()) {
        newSelection = fromModel.getSize() - 1;
      }
      // fromList.setSelectedIndex(-1);
    }

    // change selection of the toList
    toList.setSelectionInterval(row, row + selection.length - 1);

    // ensure the selection is visible
    toList.ensureIndexIsVisible(toList.getMaxSelectionIndex());
    toList.ensureIndexIsVisible(toList.getMinSelectionIndex());

    return objects;
  }

  /**
   * Move selected Items in the JList
   * 
   * @param list
   *          Move Items in this List
   * @param row
   *          The target row where to insert
   * @param sort
   *          Dummy parameter, does nothing
   */
  public static void moveSelectedItems(JList list, int row, boolean sort) {
    DefaultListModel model = (DefaultListModel) list.getModel();

    // get the selection
    int[] selection = list.getSelectedIndices();
    if (selection.length == 0) {
      return;
    }

    boolean lower = false;
    // Remove the selected items
    Object[] items = new Object[selection.length];
    for (int i = selection.length - 1; i >= 0; i--) {
      if (selection[i] < row && !lower) {
        row = row - i - 1;
        lower = true;
      }
      items[i] = model.remove(selection[i]);
    }

    for (int i = items.length - 1; i >= 0; i--) {
      model.insertElementAt(items[i], row);
    }

    // change selection of the toList
    list.setSelectionInterval(row, row + selection.length - 1);

    // ensure the selection is visible
    list.ensureIndexIsVisible(list.getMaxSelectionIndex());
    list.ensureIndexIsVisible(list.getMinSelectionIndex());
  }

  /**
   * Move selected Items in the JList
   * 
   * @param list
   *          Move Items in this List
   * @param nrRows
   *          Move Items nrRows up/down
   */
  public static void moveSelectedItems(JList list, int nrRows) {
    DefaultListModel model = (DefaultListModel) list.getModel();

    // get the selection
    int[] selection = list.getSelectedIndices();
    if (selection.length == 0) {
      return;
    }

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

  /**
   * Scale Icons to a specific width. The aspect ratio is kept.
   * 
   * @param icon
   *          The icon to scale.
   * @param newWidth
   *          The new width of the icon.
   * @return The scaled Icon.
   */
  public static Icon scaleIcon(Icon icon, int newWidth) {
    return scaleIcon(icon, newWidth, (int) ((newWidth / (float) icon
        .getIconWidth()) * icon.getIconHeight()));
  }

  /**
   * Scales Icons to a specific size
   * 
   * @param icon
   *          Icon that should be scaled
   * @param x
   *          new X-Value
   * @param y
   *          new Y-Value
   * @return Scaled Icon
   */
  public static Icon scaleIcon(Icon icon, int x, int y) {
    int currentWidth = icon.getIconWidth();
    int currentHeight = icon.getIconHeight();
    if ((currentWidth == x) && (currentHeight == y)) {
      return icon;
    }
    try {
      // Create Image with Icon
      BufferedImage iconimage = new BufferedImage(x, y,
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = iconimage.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
      AffineTransform z = g2.getTransform();
      z.scale((double) x / currentWidth, (double) y / currentHeight);
      g2.setTransform(z);
      icon.paintIcon(null, g2, 0, 0);
      g2.dispose();

      // Return new Icon
      return new ImageIcon(iconimage);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return icon;
  }

  /**
   * Scales an image to a specific size and returns an BufferedImage
   * 
   * @param img
   *          Scale this IMage
   * @param x
   *          new X-Value
   * @param y
   *          new Y-Value
   * @return Scaled BufferedImage
   * 
   * @since 2.5
   */
  public static BufferedImage scaleIconToBufferedImage(BufferedImage img,
      int x, int y) {
    return scaleIconToBufferedImage(img, x, y, img.getType());
  }
  
  /**
   * Scales an image to a specific size and returns an BufferedImage
   * 
   * @param img
   *          Scale this IMage
   * @param x
   *          new X-Value
   * @param y
   *          new Y-Value
   * @param type The type of the image.
   * @return Scaled BufferedImage
   * 
   * @since 2.7
   */
  public static BufferedImage scaleIconToBufferedImage(BufferedImage img,
      int x, int y, int type) {
    // Scale Image
    Image image = img.getScaledInstance(x, y, Image.SCALE_SMOOTH);

    BufferedImage im = new BufferedImage(x, y, type);

    Graphics2D g2 = im.createGraphics();
    g2.drawImage(image, null, null);
    g2.dispose();

    im.flush();
    return im;
  }

  /**
   * Creates a scaled Version of the Icon.
   * 
   * The scaled Version will have a Background and a Border.
   * 
   * @param ic
   * @return ImageIcon
   * @since 2.1
   */
  public static ImageIcon createChannelIcon(Icon ic) {
    BufferedImage img = new BufferedImage(42, 22, BufferedImage.TYPE_INT_RGB);

    if (ic == null) {
      ic = new ImageIcon("./imgs/tvbrowser16.png");
    }

    int height = 20;
    int width = 40;

    if ((ic.getIconWidth() != 0) && (ic.getIconHeight() != 0)) {
      double iWidth = ic.getIconWidth();
      double iHeight = ic.getIconHeight();
      if (iWidth / iHeight < 2.0) {
        width = new Double(iWidth * (20.0 / iHeight)).intValue();
      } else {
        height = new Double(iHeight * (40.0 / iWidth)).intValue();
      }
    }
    ic = scaleIcon(ic, width, height);

    Graphics2D g = img.createGraphics();

    g.setColor(Color.WHITE);
    g.fillRect(1, 1, 40, 20);

    int x = 1 + 20 - ic.getIconWidth() / 2;
    int y = 1 + 10 - ic.getIconHeight() / 2;

    ic.paintIcon(null, g, x, y);

    g.setColor(Color.BLACK);
    g.drawRect(0, 0, 42, 22);

    return new ImageIcon(img);
  }

  /**
   * Registers the escape key as close key for a component.
   * 
   * @param component
   *          The component to close on pressing escape key.
   */
  public static void registerForClosing(final WindowClosingIf component) {
    Action a = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        component.close();
      }
    };

    if (OperatingSystem.isMacOs()) {
      // Add MacOS Apple+W for Closing of Dialogs
      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_W,
          InputEvent.META_DOWN_MASK);
      component.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(stroke, "CLOSE_ON_APPLE_W");
      component.getRootPane().getActionMap().put("CLOSE_ON_APPLE_W", a);

      stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK);
      component.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(stroke, "CLOSE_COMPLETE_ON_APPLE");
      component.getRootPane().getActionMap().put("CLOSE_COMPLETE_ON_APPLE",
          new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              MainFrame.getInstance().quit();
            }
          });
    }

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    component.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        stroke, "CLOSE_ON_ESCAPE");
    component.getRootPane().getActionMap().put("CLOSE_ON_ESCAPE", a);
  }

  /**
   * set the size of a dialog, but never sizes it smaller than the preferred
   * size
   * 
   * @param dialog
   *          dialog to be sized
   * @param width
   *          wanted width
   * @param height
   *          wanted height
   */
  public static void setSize(JDialog dialog, int width, int height) {
    dialog.pack();
    Dimension size = dialog.getMinimumSize();
    if (width > size.width) {
      size.width = width;
    }
    if (height > size.height) {
      size.height = height;
    }
    dialog.setSize(size);
  }
}
