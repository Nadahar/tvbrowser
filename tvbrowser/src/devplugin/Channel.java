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

package devplugin;

import java.io.*;

import tvdataloader.TVDataServiceInterface;

public class Channel implements Serializable {

  private TVDataServiceInterface mDataService;
  private String mName;
  private int mId;
    

  
  public Channel(TVDataServiceInterface dataService, String name, int id) {
    mDataService = dataService;
    mName = name;
    mId = id;
  }


  
  /**
   * Serializes this item.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    out.writeObject(mDataService.getClass().getName());
    out.writeObject(mName);
    out.writeInt(mId);
  }

  
  
  /**
   * Deserializes this item.
   */
  private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    String dataServiceClassName = (String) in.readObject();
    
    mDataService = Plugin.getPluginManager().getDataService(dataServiceClassName);    
    mName = (String) in.readObject();
    mId = in.readInt();
  }

    
    
  public TVDataServiceInterface getDataService() {
    return mDataService;
  }

  
  
  public String toString() {
    return mName + " (" + mDataService.getName() + ")";
  }

  
  
  public String getName() {
    return mName;
  }

  
  
  public int getId() {
    return mId;
  }

  
  
  public boolean equals(Object obj) {
    if (obj instanceof Channel) {
      Channel cmp = (Channel) obj;
      return (mDataService == cmp.mDataService) && (mId == cmp.mId);
    }
    
    return false;
  }
  
}
