/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mrz 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */

package printplugin.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Scheme {

  private String mName;
  private Settings mSettings;

  protected Scheme(String name) {
    setName(name);
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }

  abstract void store(ObjectOutputStream out) throws IOException;
  abstract void read(ObjectInputStream in) throws IOException, ClassNotFoundException;

  public String toString() {
    return mName;
  }

  public void setSettings(Settings settings) {
    mSettings = settings;
  }

  public Settings getSettings() {
    return mSettings;
  }
}
