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

import java.util.*;
import java.io.*;
import tvbrowser.core.Settings;

public class FilterList {
    
    public static final String FILTER_DIRECTORY=Settings.getUserDirectoryName()+"/filters";
    private static final String FILTER_INDEX="filter.index";
    
    private static ArrayList mFilterList;
    
    public static void load() {
      mFilterList=new ArrayList();
      mFilterList.add(new ShowAllFilter());  // default filter
    
      File[] list=new File(FILTER_DIRECTORY).listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.getAbsolutePath().endsWith(".filter");
            }            
      });
      if (list!=null) {
        for (int i=0;i<list.length;i++) {
          Filter f=new Filter(list[i]);
          mFilterList.add(f);
        }
      }        
   
   
      File inxFile=new File(FILTER_DIRECTORY,FILTER_INDEX);
      BufferedReader inxIn=null;
      try {
        inxIn=new BufferedReader(new FileReader(inxFile));
        int cnt=0;
        int size=mFilterList.size();
        String curFilterName=null;
        curFilterName=inxIn.readLine();
        while (curFilterName!=null){
          
          for (int i=cnt;i<size;i++) {
            Filter f=(Filter)mFilterList.get(i);
            if (f.getName().equalsIgnoreCase(curFilterName)) {
              //swap i<-->cnt
              Object o=mFilterList.get(cnt);
              mFilterList.set(cnt,f);
              mFilterList.set(i,o);
              cnt++;
              break;
            }
          }
          
          curFilterName=inxIn.readLine();
        }
          
      }catch (IOException e) {
          e.printStackTrace();
      }
      finally{
        try{ if (inxIn!=null) inxIn.close();}catch(IOException e){};
      }
   
        
    }
    
    public static void store() {
      File directory=new File(FILTER_DIRECTORY);
      if (!directory.exists()) {
        directory.mkdir();
      }
      
      File inxFile=new File(FILTER_DIRECTORY,FILTER_INDEX);
      
      PrintWriter inxOut=null;
      try {
        inxOut=new PrintWriter(new FileWriter(inxFile)); 
      }catch(IOException e) {
        
      }
      for (int i=0;i<mFilterList.size();i++) {
        Filter f=(Filter)mFilterList.get(i);
        if (!(f instanceof ShowAllFilter)) {  // don't store default filter
          f.store(directory);
        }
        
        System.out.println("storing "+f.getName());
        if (inxOut!=null) {
          System.out.println("ok");
         inxOut.println(f.getName());
        }
      }
      
      if (inxOut!=null) {
        inxOut.close();
      }
       
       
       
    }
    
    public static Filter[] getFilterList() {
      if (mFilterList==null) {
        load();
      }
      int size=mFilterList.size();
      Filter[] result=new Filter[size];
        
      for (int i=0;i<size;i++) {
        result[i]=(Filter)mFilterList.get(i);    
      }
      return result;
    }
    
    
    public static void clear() {
      mFilterList.clear();
    }
    
    public static void add(Filter filter) {
      
       if (!mFilterList.contains(filter)) {
          mFilterList.add(filter);
        }
    }
    
    public static void remove(Filter filter) {
        filter.delete();
        mFilterList.remove(filter);
    }
    
    public static boolean containsFilter(String name) {
      Iterator it=mFilterList.iterator();
      while (it.hasNext()) {
        Filter f=(Filter)it.next();
        if (f.getName().equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }
}
