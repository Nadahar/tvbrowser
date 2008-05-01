/*
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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package util.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import util.io.ExecutionHandler;

/**
 * This class is the Interface to the AppleScript-System. It runs the Scripts
 *
 * @author bodum
 * @since 2.2.1
 */
public class AppleScriptRunner {
  /**
   * Default TimeOut in Seconds
   */
  private int mTimeOut = 60;

  /**
   * Creates the Runner with a default Timeout of 30 secs
   */
  public AppleScriptRunner() {
  }

  /**
   * Create the Runner
   *
   * @param timeout Timeout in Seconds for the execution of the Scripts
   */
  public AppleScriptRunner(int timeout) {
    mTimeOut = timeout;
  }

  /**
   * Executes the AppleScripts
   *
   * @param script Script to execute
   * @return Output if exec was successfull, null if Error occured
   * @throws IOException
   */
  public String executeScript(String script) throws IOException {
    File file = File.createTempFile("osascript", "temp");
    file.deleteOnExit();
    
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    writer.write(script);
    writer.close();

    ExecutionHandler executionHandler = new ExecutionHandler(file.getAbsolutePath(), "osascript");
    executionHandler.execute(true, "UTF-8");

    int time = 0;

    // wait until the process has exited, max MaxTimouts

    if (mTimeOut > 0) {
      while (time < mTimeOut * 1000) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
        }
        time += 100;
        try {
          executionHandler.exitValue();
          break;
        } catch (IllegalThreadStateException e) {
        }
      }
    } else {
      while (true) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
        }
        try {
          executionHandler.exitValue();
          break;
        } catch (IllegalThreadStateException e) {
        }
      }
    }

    while (time < mTimeOut * 1000) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e1) {
      }
      time += 100;
      try {
        executionHandler.exitValue();
        break;
      } catch (IllegalThreadStateException e) {
      }
    }

    // get the process output
    String output = "";

    if (!executionHandler.getInputStreamReaderThread().isAlive())
      output = executionHandler.getInputStreamReaderThread().getOutput();

    if (executionHandler.exitValue() >= 0)
      return output;

    file.delete();

    return null;
  }

  /**
   * Formats a string and escapes all problematic characters
   *
   * @param string Input-String
   * @return string with escaped characters
   */
  public String formatTextAsParam(String string) {
    return string.replaceAll("\"", "\\\\\\\\\"").replace('\n', ' ');
  }
}