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
package util.ui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * run the given runnables always in the UI thread, regardless of calling thread
 * 
 * @author Bananeweizen
 * 
 */
public class UIThreadRunner {
  public static void invokeAndWait(final Runnable runnable)
      throws InterruptedException, InvocationTargetException {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeAndWait(runnable);
    }
  };

  public static void invokeLater(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  };
  
}
