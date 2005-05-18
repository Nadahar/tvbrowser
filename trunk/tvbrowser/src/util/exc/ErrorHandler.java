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

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import javax.swing.*;

import util.io.IOUtilities;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;

/**
 * This error handler should be used to show exceptions (like IOExceptions)
 * to the user. It provides showing a localized message, that says something
 * to the user, having the option to get extra information like the stack trace
 * and nested exceptions. The error is logged, too.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ErrorHandler {
  
  /** The logger for this class. */  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ErrorHandler.class.getName());

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ErrorHandler.class);

  /** The icon to use for error messages. */  
  private static final Icon ERROR_ICON = UIManager.getIcon("OptionPane.errorIcon");
  
  /** The parent frame to use for error messages. */  
  private static JFrame mParent;
  
  

  /**
   * Sets the parent frame to use for error messages.
   *
   * @param parentFrame the parent frame to use for error messages.
   */  
  public static void setFrame(JFrame parentFrame) {
    mParent = parentFrame;
  }
  
  
  
  /**
   * Handles a TvBrowserException (Shows and loggs it).
   *
   * @param tvExc The exception to handle.
   */  
  public static void handle(TvBrowserException tvExc) {
    handle(tvExc.getLocalizedMessage(), tvExc);
  }
  
  
  
  /**
   * Handles a Throwable (Shows and loggs it).
   *
   * @param msg The localized error message to show to the user.
   * @param thr The exception to handle.
   */  
  public static void handle(String msg, Throwable thr) {
    mLog.log(Level.SEVERE, msg, thr);
    
    new ErrorWindow(mParent, msg, thr).centerAndShow();
  }
  
  
  // inner class ErrorWindow
  
  
  /**
   * The dialog used to show exceptions.
   * <p>
   * This implementation shows the stacktrace and the nested exceptions as well.
   */  
  private static class ErrorWindow {
    
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
    
    /** The OK button. */
    private JButton mOkBt;
    
    /** The button that shows and hides the details. */    
    private JButton mDetailBt;
    
    /** Holds whether the deatails are currently visible. */    
    private boolean mDetailsVisible;
    
    /** The throwable whichs details are shown by the dialog. */    
    private Throwable mThrowable;
    
    
    
    /**
     * Creates a new instance of ErrorDialog.
     *
     * @param parent A component in the component tree the dialog should be
     *        created for.
     * @param msg The localized error message.
     * @param thr The throwable whichs details are shown by the dialog.
     */    
    public ErrorWindow(Component parent, String errorMsg, Throwable thr) {
      String msg;
      
      mThrowable = thr;
      
      Frame root = JOptionPane.getFrameForComponent(parent);
      boolean useFrame = (root == null) || (! root.isVisible());
      if (useFrame) {
        mFrame = new JFrame();
        Image iconImage = ImageUtilities.createImage("imgs/tvbrowser16.gif");
        mFrame.setIconImage(iconImage);
      } else {
        mDialog = UiUtilities.createDialog(mParent, true);
      }
      
      msg = mLocalizer.msg("title", "Error");
      if (mDialog != null) {
        mDialog.setTitle(msg);
      } else {
        mFrame.setTitle(msg);
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

      JLabel errorLb = new JLabel(ERROR_ICON);
      mMessagePn.add(errorLb, BorderLayout.WEST);
      
      mErrorMessageTA = UiUtilities.createHelpTextArea(errorMsg);
      mMessagePn.add(mErrorMessageTA);
      
      // The buttons
      JPanel buttonPn = new JPanel();
      mMessagePn.add(buttonPn, BorderLayout.SOUTH);
      
      msg = mLocalizer.msg("ok", "OK");
      mOkBt = new JButton(msg);
      mOkBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          getWindow().dispose();
        }
      });
      buttonPn.add(mOkBt);
      if (mDialog != null) {
        mDialog.getRootPane().setDefaultButton(mOkBt);
      } else {
        mFrame.getRootPane().setDefaultButton(mOkBt);
      }
      
      msg = mLocalizer.msg("details", "Details");
      mDetailBt = new JButton(msg);
      mDetailBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          setDetailsVisible(! mDetailsVisible);
        }
      });
      buttonPn.add(mDetailBt);
      
      // Workaround: To give the dialog a minimum width we change the preferred
      //             size of the button panel. Otherwise the message text area
      //             will be very narrow and heigh for long messages
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
     * Creates the details panel containg the details of the throwable and all
     * its nested throwables (causes).
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
          
          tabbedPane.addTab("" + (i + 1), detailComp);
          
          thr = thr.getCause();
        }
      }
      
      msg = mLocalizer.msg("copyToClipboard", "Copy to clipboard");
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
      StringBuffer buffer = new StringBuffer(mErrorMessageTA.getText());
      
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
      }
      catch (Exception exc) {
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
          
          mDetailBt.setIcon(new ImageIcon("imgs/up16.gif"));
        } else {
          if (mDetailPn != null) {
            mDetailPn.setVisible(false);
          }
          
          mDetailBt.setIcon(new ImageIcon("imgs/down16.gif"));
        }

        getWindow().pack();
      }
    }
    
  } // inner class ErrorWindow
  
}
