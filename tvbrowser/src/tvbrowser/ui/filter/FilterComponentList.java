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


package tvbrowser.ui.filter;

import java.io.*;
import java.util.*;


import tvbrowser.ui.filter.filters.*;


public class FilterComponentList {
  
  private static HashSet mComponents;
  
  public static void init () {
    
    mComponents=new HashSet();
    
    File filterCompFile=new File(tvbrowser.core.Settings.getFilterDirectory(),"filter.comp");
    ObjectInputStream in=null;
    if (filterCompFile.exists() && filterCompFile.isFile()) {
      try {
        in=new ObjectInputStream(new FileInputStream(filterCompFile));
        read(in);
      }catch (FileNotFoundException e) {
      }catch(IOException e) {
      }
      if (in!=null) {
        try {
         in.close();
        }catch (IOException e) {
        e.printStackTrace();}
      }
    }
    
  }
  
  public static void store() {
    File filterCompFile=new File(tvbrowser.core.Settings.getFilterDirectory(),"filter.comp");
    
    try {
      ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(filterCompFile));
      store(out);
      if (out!=null) {
        out.close();
      }
    }catch(FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {      
      e.printStackTrace();
    }
  }
  
  private static void read(ObjectInputStream in) {
    mComponents=new HashSet();
    try {
      int version=in.readInt();
      int compCnt=in.readInt();
      for (int i=0;i<compCnt;i++) {
        String className=(String)in.readObject();
      
        if ("tvbrowser.ui.filter.filters.KeywordFilterComponent".equals(className)) {
          add(new KeywordFilterComponent(in));
        }
        else if ("tvbrowser.ui.filter.filters.PluginFilterComponent".equals(className)) {
          add(new PluginFilterComponent(in));
        }
        else {
          System.out.println("error reading filter component "+className);
        }
      }     
    }catch(IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {      
      e.printStackTrace();
    }
    
  }
  
  private static void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1);
    int cnt=mComponents.size();
    out.writeInt(cnt);
    Iterator it=mComponents.iterator();
    while (it.hasNext()) {
      FilterComponent comp=(FilterComponent)it.next();
      out.writeObject(comp.getClass().getName());
      comp.store(out);
    }    
  }
  
  public static Iterator iterator() {
    return mComponents.iterator();
  }
  
  public static void add(FilterComponent component) {
    mComponents.add(component);
  }
  
  public static void remove(FilterComponent component) {
    mComponents.remove(component);
  }
  
  public static boolean componentExists(String name) {
    Iterator it=mComponents.iterator();
    while (it.hasNext()) {
      FilterComponent comp=(FilterComponent)it.next();
      if (comp.getName().equalsIgnoreCase(name)) {
          return true;
      }
    }
    return false;
  }
  
}