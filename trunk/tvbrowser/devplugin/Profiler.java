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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package devplugin;

/**
 * A little helper class that allows a simple profiling (time measuring).
 *
 * @author  Til Schneider, www.murfman.de
 */
public class Profiler {
  
  private static Profiler mDefaultProfiler;
  
  private long mLastMillis = -1;
  
  
  
  /**
   * Creates a new instance of Profiler.
   */
  public Profiler() {
  }
  
  
  
  /**
   * Gets the default profiler.
   */
  public static Profiler getDefault() {
    if (mDefaultProfiler == null) {
      mDefaultProfiler = new Profiler();
    }
    return mDefaultProfiler;
  }
  
  
  
  /**
   * Shows the stopped time. And starts a new time measure.
   * <p>
   * The printing of the result is not measured, of corse.
   *
   * @param text A text that allows you to identify the location in the code.
   */
  public void show(String text) {
    long delta = System.currentTimeMillis() - mLastMillis;

    if (mLastMillis == -1) {
      System.out.println("Profiler: " + text);
    } else {
      long millis = delta % 1000;
      delta /= 1000;
      long secs = delta %= 60;
      delta /= 60;
      long minutes = delta;
      
      System.out.print("Profiler: " + text + ": ");
      if (minutes > 0) {
        System.out.print(minutes + " min ");
      }
      if ((minutes > 0) || (secs > 0)) {
        System.out.print(secs + " sec ");
      }
      System.out.println(millis + " millis");
    }
    
    mLastMillis = System.currentTimeMillis();
  }
  
}
