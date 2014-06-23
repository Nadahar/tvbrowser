/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import devplugin.Marker;
import devplugin.Program;

/**
 * A map with programs and the markers for them
 * that are marked after TV-Browser loaded plugins
 * and the generic filters to make sure markings are
 * filtered by generic filters after program start.
 * <p>
 * @author René Mach
 */
public final class PendingMarkings {
  private static Map<Program, ArrayList<Marker>> PENDING_MAP = Collections.synchronizedMap(new ProgramMap());
  private static boolean USE_PENDING = true;
  
  public static synchronized void addMarker(Program p, Marker marker) {
    if(p != null) {
      if(USE_PENDING) {
        ArrayList<Marker> list = PENDING_MAP.get(p);
        
        if(list == null) {
          list = new ArrayList<Marker>();
          PENDING_MAP.put(p, list);
        }
        
        if(!list.contains(marker)) {
          list.add(marker);
        }
      }
      else {
        p.mark(marker);
      }
    }
  }
  
  private static final class ProgramMap extends HashMap<Program, ArrayList<Marker>> {
    @Override
    public ArrayList<Marker> get(Object key) {
      ArrayList<Marker> list = super.get(key);
      
      if(list == null && !isEmpty()) {
        String test = null;
        
        if(key instanceof Program) {
          test = ((Program)key).getID();
        }
        else if(key instanceof String) {
          test = (String)key;
        }
        
        if(test != null) {
          for(Program p : keySet()) {
            if(p.getID().equals(test)) {
              list = super.get(p);
              break;
            }
          }
        }
      }
      
      return list;
    }
  }
  
  public static synchronized void markMapEntries() {
    USE_PENDING = false;
    
    for(Program p : PENDING_MAP.keySet()) {
      ArrayList<Marker> list = PENDING_MAP.get(p);
      
      if(list != null) {
        for(Marker marker : list) {
          p.mark(marker);
        }
      }
    }
    
    PENDING_MAP.clear();
  }
  
  public static boolean usePending() {
    return USE_PENDING;
  }
}
