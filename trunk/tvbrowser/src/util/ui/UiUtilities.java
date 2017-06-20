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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.StringUtils;

import devplugin.Program;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.misc.OperatingSystem;
import util.ui.persona.Persona;

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
    Window frame = win != null && win.getParent() instanceof Window ? (Window)win.getParent() : null ;
    
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
      // dual head, use first screen
      if (ge.getScreenDevices().length > 1) {
        try {
          GraphicsDevice gd = ge.getDefaultScreenDevice();
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
   * @param parent
   *          A component in the component tree where the dialog should be
   *          created for.
   * @param modal
   *          Should the new dialog be modal?
   * @return A new JDialog.
   */
  public static JDialog createDialog(Component parent, final boolean modal) {
    final AtomicReference<JDialog> result = new AtomicReference<JDialog>();
    final Window parentWin = getBestDialogParent(parent);
    try {
      UIThreadRunner.invokeAndWait(() -> {
        JDialog dialog = new JDialog(parentWin);
        
        if(modal) {
          dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
        }
        
        result.set(dialog);
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return result.get();
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
   * Gibt einen Button mit Icon und Schrift zurï¿½ck, der so initialisiert ist,
   * daï¿½ man ihn gut fï¿½r Symbolleisten nutzen kann (Rahmen nur bei Rollover
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
    return createHtmlHelpTextArea(html, UIManager.getColor("Label.foreground"), background);
  }

  /**
   * Creates a Html EditorPane that holds a HTML-Help Text
   *
   * Links will be displayed and are clickable
   *
   * @param html
   *          HTML-Text to display
   * @param foreground The color of the text.
   * @param background The color for the background.
   * @return EditorPane that holds a Help Text
   * @since 3.3.4
   */
  public static JEditorPane createHtmlHelpTextArea(String html, Color foreground, Color background) {
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
    },foreground,background);
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
    return createHtmlHelpTextArea(html,listener,UIManager.getColor("Panel.background"));
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
    return createHtmlHelpTextArea(html, listener, UIManager.getColor("Label.foreground"), background); 
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
   * @param foreground The color of the text. 
   * @param background The color for the background.
   * @return EditorPane that holds a Help Text
   * @since 3.3.4
   */
  public static JEditorPane createHtmlHelpTextArea(String html,
      HyperlinkListener listener, Color foreground, Color background) {
    // Quick "hack". Remove HTML-Code and replace it with Code that includes the
    // correct Font
    final JEditorPane pane = new JEditorPane("text/html", "");
    pane.setBackground(background);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.setEditable(false);
    pane.setOpaque(false);
    pane.setFocusable(false);

    if (listener != null) {
      pane.addHyperlinkListener(listener);
    }

    updateHtmlHelpTextArea(pane, html, foreground, background);
    return pane;
  }
  
  /**
   * @param helpTextArea The editor pane to update.
   * @param html The text for the editor pane.
   * @param foreground The foreground color. 
   * @param background The background color.
   * @since 3.3.4
   */
  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String html, Color foreground, Color background) {
    if (html.indexOf("<html>") >= 0) {
      html = StringUtils.substringBetween(html, "<html>", "</html>");
    }
    
    Font font = UIManager.getFont("Label.font");
    html = "<html><div style=\"color:" + UiUtilities.getHTMLColorCode(foreground)+";font-family:" + font.getName()
        + "; font-size:" + font.getSize() +";background-color:rgb(" + background.getRed() + "," + background.getGreen() + "," + background.getBlue() + ");\">" + html + "</div></html>";

    helpTextArea.setFont(font);
    helpTextArea.setText(html);
  }

  /**
   * @param helpTextArea The help text area.
   * @param html The html string.
   * @param background The background color.
   * @since 3.0.2
   */
  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String html, Color background) {
    updateHtmlHelpTextArea(helpTextArea, html, UIManager.getColor("Label.foreground"), background);
  }

  public static void updateHtmlHelpTextArea(final JEditorPane helpTextArea, String description) {
    updateHtmlHelpTextArea(helpTextArea, description, UIManager.getColor("Panel.background"));
  }



  /**
   * returns a color code as used in HTML, e.g. #FF0000 for pure red
   * @param color The color to get HTML color code for.
   * @return HTML color code
   */
  public static String getHTMLColorCode(Color color) {
    return '#' + StringUtils.leftPad(Integer.toString(color.getRed(), 16), 2, '0')
        + StringUtils.leftPad(Integer.toString(color.getGreen(), 16), 2, '0')
        + StringUtils.leftPad(Integer.toString(color.getBlue(), 16), 2, '0');
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
  @SuppressWarnings("rawtypes")
  public static Object[] moveSelectedItems(JList fromList, JList toList) {
    return moveSelectedItems(fromList,toList,(Class)null);
  }
  
  /**
   * Moves Selected Items from one List to another
   *
   * @param fromList
   *          Move from this List
   * @param toList
   *          Move into this List
   * @param typeRemoveOnly Classes of types that should only be removed.
   * @return Moved Elements
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Object[] moveSelectedItems(JList fromList, JList toList, Class... typeRemoveOnly) {
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

    int moveCount = 0;
    
    // move the elements
    for (int i = selection.length - 1; i >= 0; i--) {
      Object value = fromModel.remove(selection[i]);
      
      boolean move = true;
      
      if(typeRemoveOnly != null) {
        for(Class exclude : typeRemoveOnly) {
          if(exclude != null && exclude.equals(value.getClass())) {
            move = false;
            break;
          }
        }
      }
      
      if(move) {
        toModel.add(targetPos, value);
        moveCount++;
      }
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
    toList.setSelectionInterval(targetPos, targetPos + moveCount - 1);

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
  @SuppressWarnings({ "rawtypes", "unchecked" })
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
  @SuppressWarnings({ "rawtypes", "unchecked" })
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
  @SuppressWarnings({ "rawtypes", "unchecked" })
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
    if (icon == null) {
      return null;
    }
    return scaleIcon(icon, newWidth, (int) ((newWidth / (float) icon
        .getIconWidth()) * icon.getIconHeight()));
  }

  /**
   * Scales Icons to a specific size
   *
   * @param icon
   *          Icon that should be scaled
   * @param width
   *          scaled width
   * @param height
   *          scaled height
   * @return Scaled Icon
   */
  public static Icon scaleIcon(Icon icon, int width, int height) {
    if (icon == null) {
      return null;
    }
    int currentWidth = icon.getIconWidth();
    int currentHeight = icon.getIconHeight();
    if (((currentWidth == width) && (currentHeight == height)) || currentWidth <= 0 || currentHeight <= 0) {
      return icon;
    }
    try {
      // Create Image with Icon
      BufferedImage iconImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = iconImage.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
      AffineTransform z = g2.getTransform();
      g2.setTransform(z);
      icon.paintIcon(null, g2, 0, 0);
      g2.dispose();
      BufferedImage scaled = scaleIconToBufferedImage(iconImage, width, height);
      // Return new Icon
      return new ImageIcon(scaled);
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
   * @param width
   *          new width
   * @param height
   *          new height
   * @return Scaled BufferedImage
   *
   * @since 2.5
   */
  public static BufferedImage scaleIconToBufferedImage(BufferedImage img,
      int width, int height) {
    return scaleIconToBufferedImage(img, width, height, img.getType());
  }

  /**
   * Scales an image to a specific size and returns an BufferedImage
   *
   * @param img
   *          Scale this image
   * @param width
   *          new width
   * @param height
   *          new height
   * @param type The type of the image.
   * @return Scaled BufferedImage
   *
   * @since 2.7
   */
  public static BufferedImage scaleIconToBufferedImage(BufferedImage img,
      int width, int height, int type) {
    return scaleIconToBufferedImage(img, width, height, type, null);
  }

  /**
   * Scales an image to a specific size and returns an BufferedImage
   *
   * @param img
   *          Scale this image
   * @param targetWidth
   *          new width
   * @param targetHeight
   *          new height
   * @param type The type of the image.
   * @param backgroundColor The background color.
   * @return Scaled BufferedImage
   *
   * @since 3.0
   */
  public static BufferedImage scaleIconToBufferedImage(BufferedImage img,
      int targetWidth, int targetHeight, int type, Color backgroundColor) {
    BufferedImage result = img;

    int w, h;
    if (img.getWidth() > targetWidth && img.getHeight() > targetHeight) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = img.getWidth();
      h = img.getHeight();
    } else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      w = targetWidth;
      h = targetHeight;
    }

    do {
      w /= 2;
      if (w < targetWidth) {
        w = targetWidth;
      }
      h /= 2;
      if (h < targetHeight) {
        h = targetHeight;
      }

      BufferedImage tmp = new BufferedImage(w, h, type);
      Graphics2D g2 = tmp.createGraphics();
      if (backgroundColor != null) {
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0 , w, h);
      }
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(result, 0, 0, w, h, null);
      g2.dispose();

      result = tmp;
    } while (w != targetWidth || h != targetHeight);

    return result;
  }

  /**
   * Creates a scaled Version of the Icon.
   *
   * The scaled Version will have a Background and a Border.
   *
   * @param ic The icon
   * @return ImageIcon
   * @since 2.1
   */
  public static ImageIcon createChannelIcon(Icon ic) {
    BufferedImage img = new BufferedImage(getChannelIconWidth(), getChannelIconHeight(), BufferedImage.TYPE_INT_RGB);
    
    if (ic == null) {
      ic = TVBrowserIcons.defaultChannelLogo();
    }

    int height = 20;
    int width = 40;

    if ((ic.getIconWidth() > 0) && (ic.getIconHeight() > 0)) {
      double iWidth = ic.getIconWidth();
      double iHeight = ic.getIconHeight();
      if (iWidth / iHeight < 2.0) {
        width = (int) (iWidth * (20.0 / iHeight));
      } else {
        height = (int) (iHeight * (40.0 / iWidth));
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

	public static int getChannelIconHeight() {
		return 22;
	}

	public static int getChannelIconWidth() {
		return 42;
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

	public static void addSeparatorsAfterIndexes(final JComboBox<Object> combo,
			int[] indexes) {
		combo.setRenderer(new ComboSeparatorsRenderer(combo.getRenderer(), indexes));
	}

	public static void addSeparatorsAfterIndexes(final JComboBox<Object> combo,
			Integer[] indexes) {
		int[] primitives = new int[indexes.length];
		for (int i = 0; i < primitives.length; i++) {
			primitives[i] = indexes[i];
		}
		combo.setRenderer(new ComboSeparatorsRenderer(combo.getRenderer(), primitives));
	}

	private static class ComboSeparatorsRenderer implements ListCellRenderer<Object>{
		private ListCellRenderer<Object> mOldRenderer;
		private JPanel mSeparatorPanel = new JPanel(new BorderLayout());
		private JSeparator mSeparator = new JSeparator();
		private ArrayList<Integer> mIndexes;

		public ComboSeparatorsRenderer(ListCellRenderer<Object> delegate,
				final int[] indexes) {
			mOldRenderer = delegate;
			mIndexes = new ArrayList<Integer>(indexes.length);
			for (int index : indexes) {
				mIndexes.add(index);
			}
		}

    @Override
    public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component comp = mOldRenderer.getListCellRendererComponent(list, value,index, isSelected, cellHasFocus);
      if (index != -1 && mIndexes.contains(index + 1)) { // index==1 if renderer is
                                                      // used to paint current
                                                      // value in combo
        mSeparatorPanel.removeAll();
        mSeparatorPanel.add(comp, BorderLayout.CENTER);
        mSeparatorPanel.add(mSeparator, BorderLayout.SOUTH);
        return mSeparatorPanel;
      } else {
        return comp;
      }
    }
	}

  /**
   * creates a new file chooser. It is guaranteed that this happens in the UI thread.
   * @param fileFilter file filter or <code>null</code>
   * @return file chooser
   */
  public static JFileChooser createNewFileChooser(final FileFilter fileFilter) {
    final AtomicReference<JFileChooser> fileChooser = new AtomicReference<JFileChooser>();
    try {
      UIThreadRunner.invokeAndWait(() -> {
        JFileChooser select = new JFileChooser();
        fileChooser.set(select);
        if (fileFilter != null) {
          select.addChoosableFileFilter(fileFilter);
        }
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    return fileChooser.get();
  }

  /**
   * Tests if the given colors are equal in the given maximum difference.
   * <p>
   * @param c1 The first color.
   * @param c2 The second color.
   * @param maxDiff The maximum difference that counts as equal range.
   * @return <code>true</code> if both colors are in the equal range.
   */
  public static boolean colorsInEqualRange(final Color c1, final Color c2, int maxDiff) {
    return Math.abs(c1.getRed() - c2.getRed()) <= maxDiff
      && Math.abs(c1.getBlue() - c2.getBlue()) <= maxDiff
      && Math.abs(c1.getGreen() - c2.getGreen()) <= maxDiff;
  }
  
  /**
   * Creates a panel that has a semi-transparent background
   * created of Persona colors.
   * <p>
   * @return The created JPanel.
   * @since 3.2
   */
  public static JPanel createPersonaBackgroundPanel() {
    JPanel panel = new JPanel(new BorderLayout()){
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getAccentColor() != null && Persona.getInstance().getHeaderImage() != null) {
         
          Color c = Persona.testPersonaForegroundAgainst(Persona.getInstance().getAccentColor());
          
          int alpha = c.getAlpha();
          
          g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          g.fillRect(0,0,getWidth(),getHeight());
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    panel.setOpaque(false);
    
    return panel;
  }
  
  /**
   * @return If the current LookAndFeel is Nimbus.
   * @since 3.2
   */
  public static boolean isNimbusLookAndFeel() {
    return UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel") ||
    UIManager.getLookAndFeel().getClass().getCanonicalName().equals("javax.swing.plaf.nimbus.NimbusLookAndFeel");
  }
  
  /**
   * @return If the current LookAndFeel is GTK+.
   * @since 3.2
   */
  public static boolean isGTKLookAndFeel() {
    return UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
  }
  
  /**
   * 
   * @param excludeGTK <code>true</code> if the GTK+ look and feel should not
   * be considered to be the default look and feel.
   * 
   * @return The default LookAndFeel class name
   * @since 3.4.2
   */
  public static String getDefaultLookAndFeelClassName(boolean excludeGTK) {
    String lnf = UIManager.getSystemLookAndFeelClassName();
    
    if(excludeGTK && lnf.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
      lnf = UIManager.getCrossPlatformLookAndFeelClassName();
    }
    
    if (StringUtils.containsIgnoreCase(lnf, "metal")) {
      LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
      if (lnfs != null) {
        for (LookAndFeelInfo lookAndFeel : lnfs) {
          if (StringUtils.containsIgnoreCase(lookAndFeel.getName(),"Nimbus")) {
            lnf = lookAndFeel.getClassName();
            break;
          }
        }
      }
    }
    return lnf;
  }

  /**
   * Creates an JOptionPane that shows an input dialog
   * with a document modality for the given parentComponent.
   * 
   * @param parentComponent The parent of the dialog
   * @param message The message to show
   * @param initialSelectionValue The initial input value.
   * @return The user input or <code>null</code>
   * @since 3.4.5
   */
  public static final String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) {
    JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null);
    pane.setInitialSelectionValue(initialSelectionValue);
    pane.setWantsInput(true);
    pane.selectInitialValue();
    
    Locale l = (parentComponent == null) ? Locale.getDefault() : parentComponent.getLocale();
    String title = UIManager.getString("OptionPane.inputDialogTitle",l);
    
    JDialog dialog = pane.createDialog(parentComponent, title);
    dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
    dialog.setVisible(true);
    
    Object value = pane.getInputValue();

    if (value == JOptionPane.UNINITIALIZED_VALUE) {
        return null;
    }
    
    return (String)value;
  }
  
  /**
   * Adds support for rotating through list with up and down keys.
   * <p>
   * @param list The list to add the rotation to
   * @since 3.4.5
   */
  public static void addKeyRotation(final JList<?> list) {
    list.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(list.getModel().getSize() > 1) {
          if(e.getKeyCode() == KeyEvent.VK_DOWN && e.getModifiersEx() == 0 && list.getSelectedIndex() == list.getModel().getSize()-1) {
            list.setSelectedIndex(0);
            list.ensureIndexIsVisible(0);
            e.consume();
          }
          else if(e.getKeyCode() == KeyEvent.VK_UP && e.getModifiersEx() == 0 && list.getSelectedIndex() == 0) {
            list.setSelectedIndex(list.getModel().getSize()-1);
            list.ensureIndexIsVisible(list.getModel().getSize()-1);
            e.consume();
          }
        }
      }
    });
  }

  /**
   * Adds support for rotating through table with up and down keys.
   * <p>
   * @param table The table to add the rotation to
   * @since 3.4.5
   */
  public static void addKeyRotation(final JTable table) {
    addKeyRotation(table, null);
  }
  
  /**
   * Adds support for rotating through table with up and down keys.
   * <p>
   * @param table The table to add the rotation to
   * @param programGetter If date separators should not be selected the programs in the
   * table have to be checked for the example program.
   * @since 3.4.5
   */
  public static void addKeyRotation(final JTable table, final ProgramGetter programGetter) {
    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(table.getRowCount() > 1) {
          if(e.getModifiersEx() == 0) {
            final AtomicInteger index = new AtomicInteger(table.getSelectedRow());
            
            if(e.getKeyCode() == KeyEvent.VK_DOWN) {
              if(index.get() == table.getRowCount()-1) {
                index.set(0);
              }
              else {
                index.incrementAndGet();
              }
            }
            else if(e.getKeyCode() == KeyEvent.VK_UP) {
              if(index.get() == 0) {
                index.set(table.getRowCount()-1);
              }
              else {
                index.decrementAndGet();
              }
            }
            
            if(programGetter != null) {
              final Program test = programGetter.getProgram(index.get());
              
              if(test != null && test.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
                
                if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                  index.incrementAndGet();
                  
                  if(index.get() > table.getRowCount()-1) {
                    index.set(0);
                    
                    final Program test2 = programGetter.getProgram(index.get());
                    
                    if(test2 != null && test2.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
                      index.incrementAndGet();
                    }
                  }
                }
                else if(e.getKeyCode() == KeyEvent.VK_UP) {
                  index.decrementAndGet();
                  
                  if(index.get() == -1) {
                    index.set(table.getRowCount()-1);
                  }
                }
              }
            }
            
            if(index.get() != -1 && index.get() != table.getSelectedRow()) {
              table.getSelectionModel().setSelectionInterval(index.get(), index.get());
              
              SwingUtilities.invokeLater(() -> {
                final Rectangle rect = table.getCellRect(index.get(), index.get(), false);
                
                if(programGetter != null && index.get() > 0) {
                  final Program test = programGetter.getProgram(index.get()-1);
                  
                  if(test != null && test.equals(PluginManagerImpl.getInstance().getExampleProgram())) {
                    final Rectangle x = table.getCellRect(index.get()-1, index.get()-1, false);
                    
                    rect.y = x.y;
                    rect.height += x.height;
                  }
                }
                
                table.scrollRectToVisible(rect);
              });
              
              e.consume();
            }
          }
        }
      }
    });
  }
  

  /**
   * Registers the escape key, the R key and the context menu key as close key for a JPopupMenu.
   *
   * @param popupMenu The popup menu to add the closing keys.
   * @since 3.4.5
   */
  public static void registerForClosing(JPopupMenu popupMenu) {
    if(popupMenu != null) {
      final String key = "UiUtilities:close_window";
      
      popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, ContextMenuManager.NO_MOUSE_MODIFIER_EX), key);
      popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, ContextMenuManager.NO_MOUSE_MODIFIER_EX), key);
      popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, ContextMenuManager.NO_MOUSE_MODIFIER_EX), key);
      
      popupMenu.getActionMap().put(key, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          popupMenu.setVisible(false);
        }
      });

    }
  }
}
