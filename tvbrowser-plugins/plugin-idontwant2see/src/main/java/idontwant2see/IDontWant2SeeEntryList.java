/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2014 René Mach
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

import java.util.ArrayList;

/**
 * A list that checks against equals method of type.
 * 
 * @author René Mach
 *
 * @param <E> The type of the list entries.
 */
public class IDontWant2SeeEntryList<E> extends ArrayList<E> {
  @Override
  public boolean contains(Object o) {
    for(E value : this) {
      if(value.equals(o)) {
        return true;
      }
    }
    
    return false;
  }
}
