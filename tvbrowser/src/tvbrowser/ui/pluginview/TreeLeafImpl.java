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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.pluginview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

import devplugin.Date;
import devplugin.Program;
import devplugin.TreeLeaf;



public class TreeLeafImpl implements TreeLeaf {

  private Program mProgram;  
  private HashMap mProperties;
    
  
  
  public TreeLeafImpl(Program prog) {
    mProgram = prog;
    mProperties = new HashMap();
  }
  
  public TreeLeafImpl() {
    this(null);   
  }
   
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    Date date = new Date(in);
    String id = (String)in.readObject();
    mProgram = devplugin.Plugin.getPluginManager().getProgram(date, id);
    int numOfProperties = in.readInt();
    for (int i=0; i<numOfProperties; i++) {
      String key = (String)in.readObject();
      String value = (String)in.readObject();
      setProperty(key, value);
    }
    
  }
  
  public void write(ObjectOutputStream out) throws IOException {
    mProgram.getDate().writeData(out);
    out.writeObject(mProgram.getID());
    Set keys = mProperties.keySet();
    Object[]o = new Object[keys.size()];
    keys.toArray(o);
    out.writeInt(o.length);
    for (int i=0; i<o.length; i++) {
      out.writeObject(o[i]);
      out.writeObject(mProperties.get(o[i]));
    }
  }
   
  public Program getProgram() {
    return mProgram;
  }
  
  public String toString() {
    return mProgram.getTitle();
  }

  public void removeProperty(String key) {
    String s = getProperty(key);
    if (s!=null) {
      mProperties.remove(s);
    }
  }
  
  public void setProperty(String key, String value) {
    mProperties.put(key, value);
  }

  public String getProperty(String key) {
    return (String)mProperties.get(key);
  }
    
    
}