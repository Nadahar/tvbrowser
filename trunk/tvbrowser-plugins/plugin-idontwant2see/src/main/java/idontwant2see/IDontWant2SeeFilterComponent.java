/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package idontwant2see;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * Filter component class of this plugin.
 * 
 * @author René Mach
 */
public final class IDontWant2SeeFilterComponent extends PluginsFilterComponent {
  public String getUserPresentableClassName() {
    return IDontWant2See.mLocalizer.msg("name","I don't want to see!");
  }

  public boolean accept(Program program) {
    if(IDontWant2See.getInstance() != null) {
      return IDontWant2See.getInstance().acceptInternal(program);
    }
    
    return true;
  }

  public int getVersion() {
    return 0;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    // no filter settings to read, as they are handled by the plugin
  }

  public void write(ObjectOutputStream out) throws IOException {
    // no filter settings to store, as they are handled by the plugin
  }
}
