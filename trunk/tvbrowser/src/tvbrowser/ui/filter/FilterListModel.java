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

import javax.swing.*;

import tvbrowser.core.Settings;

import java.io.*;
import java.util.*;

public class FilterListModel extends DefaultListModel {
  
  private File mFilterDirectory;
  private final String FILTER_INDEX="filter.index";
  private static FilterListModel mInstance=null;
  
  private FilterListModel() {
    String filterDir = Settings.propFilterDirectory.getString();
    mFilterDirectory = new File(filterDir);
    if (!mFilterDirectory.exists()) {
      mFilterDirectory.mkdirs();
    }
    addElement(new ShowAllFilter());
    addElement(new PluginFilter());
    readFilters();
    sort();
  }
  
  public static FilterListModel getInstance() {
    if (mInstance==null) {
      mInstance=new FilterListModel();
    }
    return mInstance;
  }
  
  private void sort() {
    
    File inxFile=new File(mFilterDirectory,FILTER_INDEX);
    BufferedReader inxIn=null;
    try {
      inxIn=new BufferedReader(new FileReader(inxFile));
      int cnt=0;
      int size=getSize();
      String curFilterName=null;
      curFilterName=inxIn.readLine();
      while (curFilterName!=null){
        for (int i=cnt;i<size;i++) {
          AbstractFilter f=(AbstractFilter)getElementAt(i);
          if (f.getName().equalsIgnoreCase(curFilterName)) {
            //swap i<-->cnt
            Object o=getElementAt(cnt);
            setElementAt(f,cnt);
            setElementAt(o,i);
            cnt++;
            break;
          }
        }
        curFilterName=inxIn.readLine();
      }
    }catch (FileNotFoundException e) {
      // ignore
    }catch (IOException e) {
      e.printStackTrace();
    }
    finally{
      try {
        if (inxIn != null) inxIn.close();
      } catch(IOException exc) {}
    }    
  }
  
  private void readFilters() {
   
   if (mFilterDirectory==null) {
     throw new RuntimeException("directory is null");
   }
   
    File[] list=mFilterDirectory.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getAbsolutePath().endsWith(".filter");
      }            
    });
    
    if (list!=null) {
      for (int i=0;i<list.length;i++) {
        AbstractFilter f=new UserFilter(list[i]);
        addElement(f);
      }
    }
  }
  
  public void store() {
    String filterDir = Settings.propFilterDirectory.getString();
    File inxFile=new File(filterDir, FILTER_INDEX);
   
    PrintWriter inxOut=null;
    try {
      inxOut=new PrintWriter(new FileWriter(inxFile)); 
    }catch(IOException e) {
        return;
    }
    
    Object o[]=toArray();
    for (int i=0;i<o.length;i++) {
      if (o[i] instanceof UserFilter) {
        UserFilter f=(UserFilter)o[i];
        f.store();
      }
        inxOut.println(((AbstractFilter)o[i]).getName());
    }
    inxOut.close();    
  }
  
  public Iterator getUserFilterIterator() {
    ArrayList list=new ArrayList();
    Object o[]=toArray();
    for (int i=0;i<o.length;i++) {
      if (o[i] instanceof UserFilter) {
        list.add(o[i]);
      }
    }
    return list.iterator();
  }
  
  public boolean containsFilter(String name) {
    Object o[]=toArray();
    for (int i=0;i<o.length;i++) {
      if (((AbstractFilter)o[i]).getName().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
  
  public void remove(Object filter) {
    if (filter instanceof UserFilter) {
      UserFilter f=(UserFilter)filter;
      f.delete();
    }
    super.removeElement(filter);
  }
  
}