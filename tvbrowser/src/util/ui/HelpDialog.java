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
 */

package util.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import util.exc.ErrorHandler;
import util.io.IOUtilities;

/**
 * A help dialog which is able to show HTML pages that are located in a jar
 * file.
 *
 * @author Til Schneider, www.murfman.de
 */
public class HelpDialog implements ActionListener, HyperlinkListener {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(HelpDialog.class);
  
  /** The minimum size of the help dialog. */
  public static final Dimension MIN_HELP_DIALOG_SIZE = new Dimension(350, 400);
  
  /** The maximum size of the help dialog. */
  public static final Dimension MAX_HELP_DIALOG_SIZE = new Dimension(500, 10000);

  /**
   * Dialog-Singleton: Es wird immer nur ein Dialog auf einmal angezeigt.
   * Wenn eine neue Seite angezeigt werden soll, so passiert das im selben
   * Dialog.
   */
  private static HelpDialog mHelpDialogSingleton;

  /** The dialog. */
  private JDialog mDialog;

  /** The history. */
  private Stack<HistoryStackElement> mHistoryStack;

  /**
   * Quellpfad der aktuellen Seite. Kann <CODE>null</CODE> sein, wenn der Text
   * nicht aus einer Datei stammt.
   */
  private String mSourcePath;
  
  /** A class of the jar where the currently shown site is located. */
  private Class mSourceClass;

  private JEditorPane mEditorPane;
  private JScrollPane mScrollPane;
  private JButton mBackButton, mCloseButton;



  private HelpDialog(Component parent, String filename) {
    mDialog = UiUtilities.createDialog(UiUtilities.getBestDialogParent(parent),
        false);

    initUi();
  }



  /**
   * Shows the specified site in the help dialog. If there is currently no help
   * dialog shown, one is created.
   *
   * @param parent If there is currently no help dialog shown, this parent is
   *        used for creating a new one.
   * @param filename The name of the HTML file to show.
   * @param clazz A class in the jar where the HTML file is in.
   */
  public static void showHelpPage(Component parent, String filename, Class clazz) {
    if ((mHelpDialogSingleton == null)
      || (! mHelpDialogSingleton.mDialog.isShowing()))
    {
      mHelpDialogSingleton = new HelpDialog(parent, filename);
    } else {
      mHelpDialogSingleton.addThisSiteToHistory();
    }
    
    mHelpDialogSingleton.openSite(filename, clazz);
    mHelpDialogSingleton.show();
  }



  private void initUi() {
    mHistoryStack = new Stack<HistoryStackElement>();

    mDialog.setTitle(Localizer.getLocalization(Localizer.I18N_HELP));

    JPanel main = new JPanel(new BorderLayout());
    mDialog.setContentPane(main);
    main.setBorder(UiUtilities.DIALOG_BORDER);

    mEditorPane = new JEditorPane();
    mEditorPane.setContentType("text/html");
    mEditorPane.addHyperlinkListener(this);
    mEditorPane.setEditable(false);
    
    main.add(mScrollPane = new JScrollPane(mEditorPane));

    // buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);
    
    buttonPn.add(mBackButton = new JButton(Localizer.getLocalization(Localizer.I18N_BACK)));
    mBackButton.addActionListener(this);
    mBackButton.setEnabled(false);
    
    buttonPn.add(mCloseButton = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE)));
    mCloseButton.addActionListener(this);
    mDialog.getRootPane().setDefaultButton(mCloseButton);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Window vaterWindow = mDialog.getOwner();
    Point vaterLocation = vaterWindow.getLocation();
    Dimension vaterSize = vaterWindow.getSize();

    // Preferred
    Point hilfeLocation = new Point(vaterLocation.x + vaterSize.width, vaterLocation.y);
    Dimension hilfeSize = new Dimension(screenSize.width - hilfeLocation.x, vaterSize.height);

    // Prüfen, ob die Größe OK ist
    if (hilfeSize.width < MIN_HELP_DIALOG_SIZE.width) {
      hilfeSize.width = MIN_HELP_DIALOG_SIZE.width;
      hilfeLocation.x = screenSize.width - hilfeSize.width;

      // Passt nicht daneben -> Vaterfenster noch ein Bißchen nach links schieben
      vaterLocation.x = screenSize.width - MIN_HELP_DIALOG_SIZE.width - vaterSize.width;
      if (vaterLocation.x < 10) {
        vaterLocation.x = 10;
      }
      vaterWindow.setLocation(vaterLocation);
    }
    if (hilfeSize.width > MAX_HELP_DIALOG_SIZE.width) {
      hilfeSize.width = MAX_HELP_DIALOG_SIZE.width;
    }
    if (hilfeSize.height < MIN_HELP_DIALOG_SIZE.height) {
      hilfeSize.height = MIN_HELP_DIALOG_SIZE.height;
      hilfeLocation.y = (screenSize.height - hilfeSize.height) / 2;
    }
    if (hilfeSize.height > MAX_HELP_DIALOG_SIZE.height) {
      hilfeSize.height = MAX_HELP_DIALOG_SIZE.height;
    }

    mDialog.setSize(hilfeSize);
    mDialog.setLocation(hilfeLocation);
  }



  public void show() {
    mDialog.setVisible(true);
  }


  // Hilfsmethoden


  /**
   * Sets the text currently show.
   *
   * @param text The HTML text.
   * @param updateScrollBarTo The relative position to scroll to. (Between 0 and 1)
   */
  protected void setEditorText(String text, double updateScrollBarTo) {
    mEditorPane.setText(text);

    SwingUtilities.invokeLater(new ScrollBarUpdater(updateScrollBarTo));
  }


  /**
   * Hilfsmethode für {@link #hyperlinkUpdate(HyperlinkEvent) hyperlinkUpdate}.
   * <p>
   * Fügt die aktuelle Seite zum mHistoryStack hinzu.
   */
  protected void addThisSiteToHistory() {
    // Neues HistoryStackElement erstellen
    HistoryStackElement newSite = new HistoryStackElement();
    newSite.mText = mEditorPane.getText();
    newSite.mSourcePath = mSourcePath;
    newSite.mSourceClass = mSourceClass;

    JScrollBar scrollBar = mScrollPane.getVerticalScrollBar();
    newSite.mVerticalScrollBarRelValue = ((double) scrollBar.getValue()) / ((double) scrollBar.getMaximum());

    // Neues HistoryStackElement auf Stack werfen
    mHistoryStack.push(newSite);

    // zurück-Button sichtbar machen
    mBackButton.setEnabled(true);
  }



  /**
   * Hilfsmethode für {@link #actionPerformed(ActionEvent)}.
   * <P>
   * Ruft die oberste Seite des mHistoryStack wieder auf.
   */
  protected void popFromHistory() {
    HistoryStackElement lastSite = mHistoryStack.pop();

    double updateScrollBarTo = lastSite.mVerticalScrollBarRelValue;
    setEditorText(lastSite.mText, updateScrollBarTo);
    mSourcePath = lastSite.mSourcePath;
    mSourceClass = lastSite.mSourceClass;

    mScrollPane.validate();

    if (mHistoryStack.empty()) {
      mBackButton.setEnabled(false);
    }
  }



  /**
   * Lädt eine HTML-Seite und gibt deren Inhalt als String zurück.
   * <p>
   * Die Seite kann auch in der Jar-Datei sein, in dem diese Klasse ist.
   *
   * @param clazz A class in the jar where the HTML site is located.
   * @param filename Der Dateiname der HTML-Seite.
   */
  protected void openSite(String filename, Class clazz) {
    String text;

    try {
      text = new String(IOUtilities.loadFileFromJar(filename, clazz));
    }
    catch (IOException exc) {
      String msg = mLocalizer.msg("error.1", "Can't open site \"{0}\"!",
        filename);
      ErrorHandler.handle(msg, exc);

      text = "";
    }

    setEditorText(text, 0);
    mSourcePath = extractPath(filename);
    mSourceClass = clazz;
  }



  /**
   * Extrahiert aus einem gegebenen Dateinamen den Pfad. Ist kein Pfad
   * vorhanden, so wird null zurückgegeben.
   *
   * @param dateiname Der Dateiname.
   */
  protected static String extractPath(String dateiname) {
    int lastSlashPos = Math.max(dateiname.lastIndexOf('/'), dateiname.lastIndexOf('\\'));
    if (lastSlashPos == -1) {
      return null;
    } else {
      return dateiname.substring(0, lastSlashPos);
    }
  }


  // implements ActionListener


  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == mBackButton) {
      popFromHistory();
    }

    else if (evt.getSource() == mCloseButton) {
      mDialog.dispose();
    }
  }


  // implements HyperlinkListener


  public void hyperlinkUpdate(HyperlinkEvent evt) {
    if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      if (evt instanceof HTMLFrameHyperlinkEvent) {
        HTMLFrameHyperlinkEvent frameEvt = (HTMLFrameHyperlinkEvent) evt;
        HTMLDocument doc = (HTMLDocument) mEditorPane.getDocument();
        doc.processHTMLFrameHyperlinkEvent(frameEvt);
      } else {
        addThisSiteToHistory();
        String filename = evt.getDescription();

        // Falls in Link ein Anker vorkommt, diesen ignorieren
        int anchorPos = filename.indexOf('#');
        if (anchorPos != -1) {
          filename = filename.substring(0, anchorPos);
        }

        // Quellpfad der aktuellen Seite voranstellen
        if (mSourcePath != null) {
          filename = mSourcePath + "/" + filename;
        }
        this.mSourcePath = extractPath(filename);

        // Datei laden
        openSite(filename, mSourceClass);
      }
    }
  }
  
  
  // inner class ScrollBarUpdater
  
  
  protected class ScrollBarUpdater implements Runnable {
    
    private double mUpdateScrollBarTo;
    
    public ScrollBarUpdater (double updateScrollBarTo) {
      mUpdateScrollBarTo = updateScrollBarTo;
    }
    
    public void run() {
      JScrollBar bar = mScrollPane.getVerticalScrollBar();
      bar.setValue((int) (mUpdateScrollBarTo * bar.getMaximum()));
    }
    
  }


  // inner class HistoryStackElement
  

  protected static class HistoryStackElement {
    /**
     * Quellpfad der zuletzt besuchten Seite. Kann <CODE>null</CODE> sein, wenn
     * der Text nicht aus einer Datei stammt.
     */
    public String mSourcePath;
    
    /** The class of the jar, where the page is in. */
    public Class mSourceClass;

    /** Der Text der zuletzt besuchten Seite. */
    public String mText;

    /** Die letzte relative Position der vertikalen ScrollBar. (value / maximum) */
    public double mVerticalScrollBarRelValue;
  }

}
