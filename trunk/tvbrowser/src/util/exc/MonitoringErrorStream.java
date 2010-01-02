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
package util.exc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import util.io.IOUtilities;

/**
 * An error stream that monitors what is written to it. Every detected exception
 * is passed to the ErrorHandler
 * 
 * @author Til Schneider, www.murfman.de
 * @see ErrorHandler
 */
public class MonitoringErrorStream extends OutputStream {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(MonitoringErrorStream.class);
  
  private static final long TIME_TO_WAIT_BEFORE_ERROR_HANDLING = 500;
  
  private StringBuffer mCurrentLineBuffer;
  private StringBuffer mErrorMessageBuffer;
  
  private boolean mLastLineWasStacktrace;

  private boolean mCaptureThreadIsRunning;
  
  private long mLastLineArrivalMillis;
  

  public MonitoringErrorStream() {
    mCurrentLineBuffer = new StringBuffer();
    mErrorMessageBuffer = new StringBuffer();
    
    mLastLineWasStacktrace = true;
    mCaptureThreadIsRunning = false;
  }


  public void write(int b) throws IOException {
    if ((b == '\n') || (b == '\r')) {
      processLine(mCurrentLineBuffer.toString());
      IOUtilities.clear(mCurrentLineBuffer);
    } else {
      mCurrentLineBuffer.append((char) b);
    }
  }
  
  
  private void processLine(String line) {
    if (line.length() > 0) {
      mLastLineArrivalMillis = System.currentTimeMillis();
      
      if (line.startsWith("\t")) {
        // This is part of the stacktrace
        mLastLineWasStacktrace = true;
      } else {
        if (mLastLineWasStacktrace && (! line.startsWith("Caused by: "))) {
          // Here begins a new exception
          // -> Wait until the capture thread is done
          while (mCaptureThreadIsRunning) {
            try {
              Thread.sleep(100);
            } catch (Exception exc) {}
          }
          
          // This thread logs the error when no more lines arrived after a
          // waiting time.
          Thread pollThread = new Thread("Error logging") {
            public void run() {
              mCaptureThreadIsRunning = true;
  
              // Wait until the last line has arrived some time ago
              long deadline;
              do {
                try {
                  sleep(10);
                } catch (Exception exc) {}
                deadline = mLastLineArrivalMillis + TIME_TO_WAIT_BEFORE_ERROR_HANDLING;
              } while (System.currentTimeMillis() < deadline);
              
              handleError();
  
              mCaptureThreadIsRunning = false;
            }
          };
          pollThread.start();
  
          // Warten, bis der PollThread läuft
          while (! mCaptureThreadIsRunning) {
            try {
              Thread.sleep(10);
            } catch (Exception exc) {}
          }
        }

        mLastLineWasStacktrace = false;
      }

      mErrorMessageBuffer.append(line);
      mErrorMessageBuffer.append('\n');
    }
  }


  private void handleError() {
    final String stuffToPrint = mErrorMessageBuffer.toString();
    
    Throwable fakeException = new Throwable() {
      public void printStackTrace(PrintStream stream) {
        stream.println(stuffToPrint);
      }
      
      public void printStackTrace(PrintWriter writer) {
        writer.println(stuffToPrint);
      }
    };
    
    String msg = mLocalizer.msg("error.1", "An unhandled error has occured");
    ErrorHandler.handle(msg, fakeException); // TODO
    
    IOUtilities.clear(mErrorMessageBuffer);
  }
  
}
