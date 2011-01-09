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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import util.ui.UIThreadRunner;


/**
 * This error handler should be used to show exceptions (like IOExceptions)
 * to the user. It provides showing a localized message, that says something
 * to the user, having the option to get extra information like the stack trace
 * and nested exceptions. The error is logged, too.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ErrorHandler {

  /** Show only a OK-Button */
  public static final int SHOW_OK = 0;

  /** Show only a Yes/No-Button */
  public static final int SHOW_YES_NO = 1;

  /** OK was pressed */
  public static final int OK_PRESSED = 1;

  /** Yes was pressed */
  public static final int YES_PRESSED = 2;

  /** No was pressed */
  public static final int NO_PRESSED = 3;

  /** The logger for this class. */
  private static final Logger mLog
    = Logger.getLogger(ErrorHandler.class.getName());

  /** The localizer of this class. */
  static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ErrorHandler.class);

  /** The icon to use for error messages. */
  static final Icon ERROR_ICON = UIManager.getIcon("OptionPane.errorIcon");

  /** The parent frame to use for error messages. */
  static JFrame mParent;



  /**
   * Sets the parent frame to use for error messages.
   *
   * @param parentFrame the parent frame to use for error messages.
   */
  public static void setFrame(JFrame parentFrame) {
    mParent = parentFrame;
  }



  /**
   * Handles a TvBrowserException (Shows and logs it).
   *
   * @param tvExc The exception to handle.
   */
  public static void handle(TvBrowserException tvExc) {
    handle(tvExc.getLocalizedMessage(), tvExc);
  }



  /**
   * Handles a Throwable (Shows and logs it).
   *
   * @param msg The localized error message to show to the user.
   * @param throwable The exception to handle.
   */
  public static void handle(final String msg, final Throwable throwable) {
    mLog.log(Level.SEVERE, msg, throwable);
    try {
      UIThreadRunner.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          ErrorWindow errorWindow = new ErrorWindow(mParent, msg, throwable);
          errorWindow.centerAndShow();
        }
      });
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  /**
   * Handles a Throwable (Shows and logs it).
   *
   * @param msg The localized error message to show to the user.
   * @param thr The exception to handle.
   * @param messageType The type of Window to Display:OK
   * @return Value of Button that was pressed: ErrorHandler.YES_PRESSED, ErrorHandler.NO_PRESSED, ErrorHandler.OK_PRESSED
   *
   * @since 2.1
   */
  public static int handle(String msg, Throwable thr, int messageType) {
    mLog.log(Level.SEVERE, msg, thr);

    ErrorWindow errwin = new ErrorWindow(mParent, msg, thr, messageType);
    errwin.centerAndShow();
    return errwin.getReturnValue();
  }

}