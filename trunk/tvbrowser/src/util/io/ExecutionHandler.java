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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles the execution of external processes.
 *
 * @author René Mach
 * @since 2.6.1/2.2.5
 */
public class ExecutionHandler {
  private String[] mParameter;
  private File mRuntimeDirectory;
  private StreamReaderThread mInputStream;
  private StreamReaderThread mErrorStream;

  private Process mProcess;

  /**
   * Creates an instance of this class.
   *
   * @param parameter The parameter to pass to the application.
   * @param programPath The path to the application.
   */
  public ExecutionHandler(String parameter, String programPath) {
    this(parameter, programPath, (File) null);

    if(programPath.contains(File.separator)) {
      String path = programPath.substring(0,programPath.lastIndexOf(File.separator) + 1);

      if(path == null || path.length() < 1 || !(new File(path).isDirectory())) {
        path = System.getProperty("user.dir");
      }

      mRuntimeDirectory = new File(path.trim());
    }
  }

  /**
   * Creates an instance of this class.
   *
   * @param parameter The parameter to parse to the application.
   * @param programPath The path to the application.
   * @param runtimeDirectory The runtime directory for the application.
   */
  public ExecutionHandler(String parameter, String programPath, String runtimeDirectory) {
    this(parameter, programPath, new File(runtimeDirectory.trim()));
  }

  /**
   * Creates an instance of this class.
   *
   * @param parameter The parameter to parse to the application.
   * @param programPath The path to the application.
   * @param runtimeDirectory The runtime directory for the application.
   */
  public ExecutionHandler(String parameter, String programPath, File runtimeDirectory) {
    mParameter = calculateParameter(parameter, programPath);
    mRuntimeDirectory = runtimeDirectory;
  }

  /**
   * Creates an instance of this class.
   *
   * @param parameterWithProgramPath The parameter to parse to the application with the path to the program.
   */
  public ExecutionHandler(String[] parameterWithProgramPath) {
    this(parameterWithProgramPath, (File) null);
  }

  /**
   * Creates an instance of this class.
   *
   * @param parameterWithProgramPath The parameter to parse to the application with the path to the program.
   * @param runtimeDirectory The runtime directory for the application.
   */
  public ExecutionHandler(String[] parameterWithProgramPath, String runtimeDirectory) {
    this(parameterWithProgramPath, new File(runtimeDirectory.trim()));
  }

  /**
   * Creates an instance of this class.
   *
   * @param parameterWithProgramPath The parameter to parse to the application with the path to the program.
   * @param runtimeDirectory The runtime directory for the application.
   */
  public ExecutionHandler(String[] parameterWithProgramPath, File runtimeDirectory) {
    mParameter = parameterWithProgramPath;
    mRuntimeDirectory = runtimeDirectory;
  }

  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute() throws IOException {
    execute(false,false);
  }

  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @param encoding The encoding for the reading of the process console output.
   *
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute(String encoding) throws IOException {
    execute(false,false,encoding);
  }

  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @param logInputStream <code>True</code> means that the console output of the output stream
   * of the process will be logged and can be get after the process was stopped (it's output stream
   * is an input stream for the process starter).
   *
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute(boolean logInputStream) throws IOException {
    execute(logInputStream,false);
  }

  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @param logInputStream <code>True</code> means that the console output of the output stream
   * of the process will be logged and can be get after the process was stopped (it's output stream
   * is an input stream for the process starter).
   * @param encoding The encoding for the reading of the process console output.
   *
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute(boolean logInputStream, String encoding) throws IOException {
    execute(logInputStream,false,encoding);
  }



  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @param logInputStream <code>True</code> means that the console output of the output stream
   * of the process will be logged and can be get after the process was stopped (it's output stream
   * is an input stream for the process starter).
   * @param logErrorStream <code>True</code> means that the console output of the error stream
   * of the process will be logged and can be get after the process was stopped.
   *
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute(boolean logInputStream, boolean logErrorStream) throws IOException {
    execute(logInputStream, logErrorStream, null);
  }

  /**
   * Executes the given application with the given parameters in the given runtime directory.
   * @param logInputStream <code>True</code> means that the console output of the output stream
   * of the process will be logged and can be get after the process was stopped (it's output stream
   * is an input stream for the process starter).
   * @param logErrorStream <code>True</code> means that the console output of the error stream
   * of the process will be logged and can be get after the process was stopped.
   * @param encoding The encoding for the reading of the process console output.
   *
   * @throws IOException Thrown if something went wrong on process building.
   */
  public void execute(boolean logInputStream, boolean logErrorStream, String encoding) throws IOException {
    mProcess = Runtime.getRuntime().exec(mParameter,null,mRuntimeDirectory);

    mInputStream = new StreamReaderThread(mProcess.getInputStream(),logInputStream,encoding);
    mErrorStream = new StreamReaderThread(mProcess.getErrorStream(),logErrorStream,encoding);

    mInputStream.start();
    mErrorStream.start();
  }

  /**
   * Gets the started process.
   *
   * @return The started process or <code>null</code> if the process wasn't started.
   */
  public Process getProcess() {
    return mProcess;
  }

  /**
   * The exit value of the process.
   *
   * @return The exit value of the process.
   * @throws IllegalThreadStateException Thrown if the process wasn't started or is not finished.
   */
  public int exitValue() throws IllegalThreadStateException {
    if(mProcess == null) {
      throw new IllegalThreadStateException("Process wasn't started.");
    }

    return mProcess.exitValue();
  }

  /**
   * Gets the input stream reader thread (it logs the console output of the process).
   *
   * @return The input stream reader thread.
   */
  public StreamReaderThread getInputStreamReaderThread() {
    return mInputStream;
  }

  /**
   * Gets the error stream reader thread.
   *
   * @return The error stream reader thread.
   */
  public StreamReaderThread getErrorStreamReaderThread() {
    return mErrorStream;
  }

  private String[] calculateParameter(String parameter, String programPath) {
    StringBuilder lastString = null;
    ArrayList<String> args = new ArrayList<String>();

    args.add(programPath.trim());

    for (String part : parameter.split(" ")) {
      if (part.length() > 0 && part.charAt(0) == '"') {
        if (part.charAt(part.length()-1) == '"') {
          args.add(part);
        } else {
          lastString = new StringBuilder(part);
          lastString.deleteCharAt(0);
        }
      } else if (lastString != null) {
        lastString.append(' ');
        lastString.append(part);
        if (part.length() > 0 && part.charAt(part.length()-1) == '"') {
          lastString.deleteCharAt(lastString.length() - 1);
          args.add(lastString.toString());
          lastString = null;
        }
      // Add parameter only if it isn't just whitespace. Use quotes
      // to add empty or whitespace only parameters!
      // FSCHAECK - 2008-12-06
      } else if (part.trim().length() > 0) {
        args.add(part);
      }
    }

    return args.toArray(new String[args.size()]);
  }


  /**
   * get console output of the execution handler after execution
   * @return output
   * @since 3.0
   */
  public String getOutput() {
    StreamReaderThread thread = getInputStreamReaderThread();
    try {
      thread.join(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return thread.getOutputString();
  }
  
  /**
   * Get error output of the execution handler after execution 
   * @return The errors
   * @since 3.0.2
   */
  public String getErrors() {
    StreamReaderThread thread = getErrorStreamReaderThread();
    try {
      thread.join(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return thread.getOutputString();
  }
}
