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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
 

package tvbrowser.ui.update;

import java.util.*;

public class UpdateItemList {
	
	private HashMap mMap;
	
	
	public UpdateItemList() {
		mMap=new HashMap();
	}
	
	public void addUpdateItem(UpdateItem item) {
		ArrayList list=(ArrayList)mMap.get(item.getName());		
		if (list==null) {
			list=new ArrayList();
			mMap.put(item.getName(),list);		
		}
		list.add(item);			
	}
	
	public int size() {
		return mMap.size();
	}
	
	public boolean isEmpty() {
		return mMap.isEmpty();
	}
	
	public Iterator iterator() {
		return mMap.values().iterator();
	}
	
	public Object[] toArray() {
		return mMap.values().toArray();
	}
	
	public String getName(Object entry) {
		if (entry instanceof ArrayList) {
			ArrayList list=(ArrayList)entry;
			Object o=list.get(0);
			if (o instanceof UpdateItem) {
				return ((UpdateItem)o).getName();
			}
		}
		return null;
	}
	
	public int getType(Object entry) {
			if (entry instanceof ArrayList) {
				ArrayList list=(ArrayList)entry;
				Object o=list.get(0);
				if (o instanceof UpdateItem) {
					return ((UpdateItem)o).getType();
				}
			}
			return -1;
		}
	
	
	
}