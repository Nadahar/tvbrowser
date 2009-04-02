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
package util.exc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import tvbrowser.core.icontheme.IconLoader;
import util.io.IOUtilities;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The dialog used to show exceptions.
 * <p>
 * This implementation shows the stacktrace and the nested exceptions as well.
 * 
 * This class was moved from ErrorHandler into a seperate Class in Version 2.1.
 * Please use ErrorHandler, do not call this directly
 */
class ErrorWindow {

  /** The dialog. Is null, when there is no parent frame. */
  private JDialog mDialog;

  /** The frame. Is null, when there is a parent frame. */
  private JFrame mFrame;

  /** The main panel. */
  private JPanel mMainPn;

  /** The panel containing the message, the icon and the buttons. */
  private JPanel mMessagePn;

  /** The text area containing the error message. */
  private JTextArea mErrorMessageTA;

  /** The panel containing the details (the stacktrace). */
  private JPanel mDetailPn;

  /** The Buttons. */
  private JButton mOkBt, mYesBt, mNoBt;

  /** The button that shows and hides the details. */
  private JButton mDetailBt;

  /** Holds whether the deatails are currently visible. */
  private boolean mDetailsVisible;

  /** The throwable whichs details are shown by the dialog. */
  private Throwable mThrowable;

  /** The Button that was pressed */
  private int mReturn = ErrorHandler.NO_PRESSED;

  /**
   * Creates a new instance of ErrorDialog.
   * 
   * @param parent A component in the component tree the dialog should be
   *          created for.
   * @param errorMsg The localized error message.
   * @param thr The throwable whichs details are shown by the dialog.
   */
  public ErrorWindow(Component parent, String errorMsg, Throwable thr) {
    this(parent, errorMsg, thr, ErrorHandler.SHOW_OK);
  }

  /**
   * Creates a new instance of ErrorDialog.
   * 
   * @param parent A component in the component tree the dialog should be
   *          created for.
   * @param errorMsg The localized error message.
   * @param thr The throwable whichs details are shown by the dialog.
   * @param messageType The type of Window to Display: ErrorHandler.SHOW_OK, ErrorHandler.SHOW_YES_NO
   * 
   * @since 2.1
   */
  public ErrorWindow(Component parent, String errorMsg, Throwable thr, int messageType) {
    String msg;

    mThrowable = thr;

    Frame root = JOptionPane.getFrameForComponent(parent);
    boolean useFrame = (root == null) || (!root.isVisible());
    if (useFrame) {
      mFrame = new JFrame();
      Image iconImage = ImageUtilities.createImage("imgs/tvbrowser16.png");
      mFrame.setIconImage(iconImage);
    } else {
      mDialog = UiUtilities.createDialog(ErrorHandler.mParent, true);
    }

    
    if (mDialog != null) {
      mDialog.setTitle(Localizer.getLocalization(Localizer.I18N_ERROR));
    } else {
      mFrame.setTitle(Localizer.getLocalization(Localizer.I18N_ERROR));
    }

    mMainPn = new JPanel(new BorderLayout());
    mMainPn.setBorder(UiUtilities.DIALOG_BORDER);
    if (mDialog != null) {
      mDialog.setContentPane(mMainPn);
    } else {
      mFrame.setContentPane(mMainPn);
    }

    // message panel
    mMessagePn = new JPanel(new BorderLayout(10, 10));
    mMainPn.add(mMessagePn, BorderLayout.NORTH);

    JLabel errorLb = new JLabel(ErrorHandler.ERROR_ICON);
    mMessagePn.add(errorLb, BorderLayout.WEST);

    mErrorMessageTA = UiUtilities.createHelpTextArea(errorMsg);
    mMessagePn.add(mErrorMessageTA);

    // The buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    mMessagePn.add(buttonPn, BorderLayout.SOUTH);

    if (messageType == ErrorHandler.SHOW_YES_NO) {

      mYesBt = new JButton(ErrorHandler.mLocalizer.msg("yes", "Yes"));
      mYesBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mReturn = ErrorHandler.YES_PRESSED;
          getWindow().dispose();
        }
      });
      buttonPn.add(mYesBt);

      mNoBt = new JButton(ErrorHandler.mLocalizer.msg("no", "No"));
      mNoBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mReturn = ErrorHandler.NO_PRESSED;
          getWindow().dispose();
        }
      });
      buttonPn.add(mNoBt);

    } else {
      mOkBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
      mOkBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mReturn = ErrorHandler.OK_PRESSED;
          getWindow().dispose();
        }
      });
      buttonPn.add(mOkBt);
    }

    if (mDialog != null) {
      mDialog.getRootPane().setDefaultButton(mOkBt);
    } else {
      mFrame.getRootPane().setDefaultButton(mOkBt);
    }

    msg = ErrorHandler.mLocalizer.msg("details", "Details");
    mDetailBt = new JButton(msg);
    mDetailBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        setDetailsVisible(!mDetailsVisible);
      }
    });
    buttonPn.add(mDetailBt);

    // Workaround: To give the dialog a minimum width we change the preferred
    // size of the button panel. Otherwise the message text area
    // will be very narrow and heigh for long messages
    Dimension size = buttonPn.getPreferredSize();
    size.width = 350;
    buttonPn.setPreferredSize(size);

    mDetailsVisible = true;
    setDetailsVisible(false);

    getWindow().pack();
  }

  private Window getWindow() {
    if (mDialog != null) {
      return mDialog;
    } else {
      return mFrame;
    }
  }

  /**
   * Centers and shows the error dialog.
   */
  public void centerAndShow() {
    UiUtilities.centerAndShow(getWindow());
  }

  /**
   * Returns the Value of the Button that was pressed.
   * 
   * @return ErrorHandler.YES_PRESSED, ErrorHandler.NO_PRESSED, ErrorHandler.OK_PRESSED
   * @since 2.1
   */
  public int getReturnValue() {
    return mReturn;
  }

  /**
   * Creates the details panel containg the details of the throwable and all its
   * nested throwables (causes).
   * 
   * @return the details panel.
   */
  private JPanel createDetailPanel() {
    String msg;

    JPanel detailPn = new JPanel(new BorderLayout(0, 10));

    detailPn.add(new JSeparator(), BorderLayout.NORTH);
    detailPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

    if (mThrowable.getCause() == null) {
      JComponent detailComp = createDetailComponent(mThrowable);
      detailPn.add(detailComp, BorderLayout.CENTER);
    } else {
      JTabbedPane tabbedPane = new JTabbedPane();
      detailPn.add(tabbedPane, BorderLayout.CENTER);

      Throwable thr = mThrowable;
      for (int i = 0; thr != null; i++) {
        JComponent detailComp = createDetailComponent(thr);

        tabbedPane.addTab(Integer.toString(i + 1), detailComp);

        thr = thr.getCause();
      }
    }

    msg = ErrorHandler.mLocalizer.msg("copyToClipboard", "Copy to clipboard");
    JButton copyToClipBoardBt = new JButton(msg);
    copyToClipBoardBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        copyDetailsToClipBoard();
      }
    });
    detailPn.add(copyToClipBoardBt, BorderLayout.SOUTH);

    return detailPn;
  }

  private void copyDetailsToClipBoard() {
    StringBuilder buffer = new StringBuilder(mErrorMessageTA.getText());

    buffer.append("\n\n----- Start of stacktrace -----\n");
    Throwable thr = mThrowable;
    while (thr != null) {
      if (thr != mThrowable) {
        buffer.append("\n\nCaused by:\n");
      }

      buffer.append(getStackTrace(thr));

      thr = thr.getCause();
    }

    // Remove trailing newlines
    while (buffer.charAt(buffer.length() - 1) == '\n') {
      buffer.deleteCharAt(buffer.length() - 1);
    }

    buffer.append("\n----- End of stacktrace -----\n");

    StringSelection content = new StringSelection(buffer.toString());
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
  }

  /**
   * Creates a detail panel for one throwable.
   * 
   * @param thr The throwable to create the details panel for.
   * @return a detail panel for the specified throwable.
   */
  private JComponent createDetailComponent(Throwable thr) {
    String detailText = getStackTrace(thr);
    JTextArea textArea = new JTextArea(detailText);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(200, 200));

    return scrollPane;
  }

  /**
   * Gets the stacktrace of a Throwable
   * 
   * @param thr The Throwable to get the stacktrace for
   * @return The stacktrace of the Throwable
   */
  private String getStackTrace(Throwable thr) {
    try {
      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);

      thr.printStackTrace(printer);

      printer.close();
      writer.close();

      IOUtilities.replace(writer.getBuffer(), "\t", "  ");
      IOUtilities.replace(writer.getBuffer(), "\r\n", "\n");

      return writer.toString();
    } catch (Exception exc) {
      // This won't happen since we are writing to a StringWriter
      return "";
    }
  }

  /**
   * Sets whether the details should be visible.
   * 
   * @param detailsVisible whether the details should be visible.
   */
  private void setDetailsVisible(boolean detailsVisible) {
    if (detailsVisible != mDetailsVisible) {
      mDetailsVisible = detailsVisible;

      if (mDetailsVisible) {
        if (mDetailPn == null) {
          mDetailPn = createDetailPanel();
          mMainPn.add(mDetailPn, BorderLayout.CENTER);
        }
        mDetailPn.setVisible(true);
        
        mDetailBt.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "go-up", 16));
      } else {
        if (mDetailPn != null) {
          mDetailPn.setVisible(false);
        }

        mDetailBt.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "go-down", 16));
      }

      getWindow().pack();
    }
  }

}