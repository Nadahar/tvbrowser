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

package tvbrowser.core.filters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tvbrowser.core.Settings;
import devplugin.ProgramFilter;

public class FilterList {
  
  private static FilterList mInstance;
  private File mFilterDirectory;
  private ProgramFilter[] mFilterArr;
  private final String FILTER_INDEX="filter.index";
  public static final String FILTER_DIRECTORY=Settings.getUserDirectoryName()+"/filters";


  private static java.util.logging.Logger mLog
      = java.util.logging.Logger.getLogger(FilterList.class.getName());

  private FilterList() {
    create();      
  }
  
  public void create() {
    mFilterDirectory=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY);
    if (!mFilterDirectory.exists()) {
      mFilterDirectory.mkdirs();
    } 
    mFilterArr = createFilterList();     
  }
  
  public static FilterList getInstance() {
    if (mInstance == null) {
      mInstance = new FilterList();
    }
    return mInstance;
  }
  
  private ProgramFilter[] createFilterList() {
    HashMap filterList = new HashMap();
    
    /* Add default filters. The user may not remove them. */
    
    ProgramFilter showAll = new ShowAllFilter(); 
    filterList.put(showAll.getName(), showAll);
    ProgramFilter pluginFilter = new PluginFilter(); 
    filterList.put(pluginFilter.getName(), pluginFilter);
    ProgramFilter subtitleFilter = new SubtitleFilter();    
    filterList.put(subtitleFilter.getName(), subtitleFilter);
    
    /* Read the available filters from the file system and add them to the array */
    if (mFilterDirectory==null) {
      throw new NullPointerException("directory is null");
    }
   
   
    File[] fileList = getFilterFiles();
    
    
    if (fileList!=null) {
      for (int i=0;i<fileList.length;i++) {
        UserFilter filter=null;
        try {
          filter = new UserFilter(fileList[i]);
        }catch(ParserException e) {
          mLog.warning("error parsing filter from file "+fileList[i]+"; exception: "+e);
        }
        if (filter!=null) {
          filterList.put(filter.getName(), filter);
        }
      }
    }
    
    ArrayList filterArr = new ArrayList();
    
    /* Sort the list*/
    File inxFile=new File(mFilterDirectory,FILTER_INDEX);
    BufferedReader inxIn=null;
    try {
      inxIn=new BufferedReader(new FileReader(inxFile));
      int cnt=0;
      String curFilterName=null;
      curFilterName=inxIn.readLine();
      while (curFilterName!=null){
        if (curFilterName.equals("[SEPARATOR]")) {
          filterArr.add(new SeparatorFilter());
        } else {
          ProgramFilter filter = (ProgramFilter) filterList.get(curFilterName);
          
          if (filter != null) {
            filterArr.add(filter);
            filterList.remove(curFilterName);
          }
        }

        curFilterName=inxIn.readLine();
      }
    }catch (FileNotFoundException e) {
      // ignore
    }catch (IOException e) {
      e.printStackTrace();
    }finally{
      try{ if (inxIn!=null) inxIn.close();}catch(IOException e){};
    }    
    
    
    if (filterList.size() > 0) {
      Iterator it = filterList.values().iterator();
      while (it.hasNext()) {
        filterArr.add(it.next());
      }
    }
    
    return (ProgramFilter[]) filterArr.toArray(new ProgramFilter[0]);
    
  }
  
  private File[] getFilterFiles() {
    File[] fileList=mFilterDirectory.listFiles(new FileFilter() {
          public boolean accept(File f) {
            return f.getAbsolutePath().endsWith(".filter");
          }            
        });
    return fileList;
  }
  
  public ProgramFilter[] getFilterArr() {
    return mFilterArr;    
  }
  
  public UserFilter[] getUserFilterArr() {
    ArrayList filterList = new ArrayList();
    for (int i=0; i<mFilterArr.length; i++) {
      if (mFilterArr[i] instanceof UserFilter) {
        filterList.add(mFilterArr[i]);
      }
    }
    UserFilter[] result = new UserFilter[filterList.size()];
    filterList.toArray(result);
    return result;
  }


  public boolean containsFilter(String filterName) {
    for (int i=0; i<mFilterArr.length;i++) {
      if (mFilterArr[i].getName().equalsIgnoreCase(filterName)) {
        return true;
      }
    }
    return false;
  }
  
  public void setProgramFilterArr(ProgramFilter[] filterArr) {
    mFilterArr = filterArr;
  }
  
  public void remove(ProgramFilter filter) {
    ArrayList filterList = new ArrayList();
    for (int i=0; i<mFilterArr.length; i++) {
      if (!mFilterArr[i].equals(filter)) {
        filterList.add(mFilterArr[i]);
      }
    }
    ProgramFilter[] mFilterArr = new ProgramFilter[filterList.size()];
    filterList.toArray(mFilterArr);
    
  }
  
  public void store() {
    /* delete all filters*/
    File[] fileList=getFilterFiles();
    for (int i=0;i<fileList.length; i++) {
      fileList[i].delete();
    }
    
    for (int i=0;i<mFilterArr.length;i++) {
      if (mFilterArr[i] instanceof UserFilter) {
        ((UserFilter)mFilterArr[i]).store();   
      }
    }

    File inxFile=new File(mFilterDirectory,FILTER_INDEX);
    BufferedWriter inxOut=null;
    
    try {
        inxOut = new BufferedWriter(new FileWriter(inxFile));
        
        for (int i = 0; i < mFilterArr.length; i++) {
            inxOut.write(mFilterArr[i].getName()+ "\n");
        }
        inxOut.close();
    } catch (Exception e) {
        
    }
  }

  /**
   * Returns the Filter named "name"
   * @param name Name of Filter to return
   * @return Filter with Name "name" or null if not found
   */
  public ProgramFilter getFilterByName(String name) {
     if (name == null) {
         return null;
     }
     
     for (int i = 0; i < mFilterArr.length; i++) {
         if (mFilterArr[i].getName().equals(name)) {
             return mFilterArr[i];
         }
     }
      
     return null;
  }

  /**
   * Returns the Default-Filter ("ShowAll")
   * @return the Default-Filter
   */
  public ProgramFilter getDefaultFilter() {

      for (int i = 0; i < mFilterArr.length; i++) {
          if (mFilterArr[i].getClass().getName().equals("tvbrowser.core.filters.ShowAllFilter")) {
              return mFilterArr[i];
          }
      }
      
      return new ShowAllFilter();
  }
}