/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package snarlplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import util.io.ExecutionHandler;

public class JarApplicationExecuter {
  public String mExtractedPath;
  private String mExecutable;

  public JarApplicationExecuter(final String executable) {
    mExecutable = executable;
  }

  private String extractNotifier() {
    File executable;
    try {
      // Get input stream from jar resource
      final InputStream inputStream = getClass().getResource(mExecutable)
          .openStream();

      // Copy resource to file system in a temporary folder with a unique name
      executable = File.createTempFile("tvbrowser_executer_", ".exe");
      final FileOutputStream outputStream = new FileOutputStream(executable);
      final byte[] array = new byte[8192];
      int read = 0;
      while ((read = inputStream.read(array)) > 0) {
        outputStream.write(array, 0, read);
      }
      outputStream.close();

      // Delete on exit
      executable.deleteOnExit();
      // getPluginManager().deleteFileOnNextStart(executable.getPath());
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
    return executable.getPath();
  }
  
  public void execute(final String parameters) {
    if (mExtractedPath == null) {
      mExtractedPath = extractNotifier();
    }
    if (mExtractedPath != null) {
      final ExecutionHandler exec = new ExecutionHandler(parameters,
          mExtractedPath);
      try {
        exec.execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}