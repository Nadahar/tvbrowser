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
 *     $Date: 2008-08-21 08:24:11 +0200 (Do, 21 Aug 2008) $
 *   $Author: Bananeweizen $
 * $Revision: 4913 $
 */
package util.misc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import util.io.stream.OutputStreamProcessor;
import util.io.stream.StreamUtilities;

/**
 * properties implementation which tracks changes and only stores itself to disk
 * if there were changes in the properties collection
 *
 * @author Bananeweizen
 *
 */
public class ChangeTrackingProperties extends Properties {

  private boolean mChanged = false;

  @Override
  public synchronized Object setProperty(String key, String value) {
    mChanged = true;
    return super.setProperty(key, value);
  }

  /**
   * whether or not the properties stored in this object have changed since
   * object creation
   *
   * @return changed
   */
  synchronized public boolean changed() {
    return mChanged;
  }

  public void store(File file) throws IOException {
    if (changed()) {
      StreamUtilities.outputStream(file, new OutputStreamProcessor() {
        @Override
        public void process(OutputStream outputStream) throws IOException {
          store(outputStream, null);
        }
      });
    }
  }

}
