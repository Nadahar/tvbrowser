package tvbrowser.core.filters;

import java.io.*;
import java.util.*;

import tvbrowser.core.Settings;

import devplugin.ProgramFilter;

public class FilterList {
  
  private static FilterList mInstance;
  private File mFilterDirectory;
  private ProgramFilter[] mFilterArr;
  private final String FILTER_INDEX="filter.index";
  public static final String FILTER_DIRECTORY=Settings.getUserDirectoryName()+"/filters";

  public FilterList() {
    
           
  }
  
  
 
  
  public void create() {
    mFilterDirectory=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY);
    if (!mFilterDirectory.exists()) {
      mFilterDirectory.mkdirs();
    } 
    mFilterArr = createFilterList();
  }
  
  private ProgramFilter[] createFilterList() {
    ArrayList filterList = new ArrayList();
    
    /* Add default filters. The user may not remove them. */
    filterList.add(new ShowAllFilter());
    filterList.add(new PluginFilter());
    
    
    /* Read the available filters from the file system and add them to the array */
    if (mFilterDirectory==null) {
      throw new NullPointerException("directory is null");
    }
   
   
    File[] fileList = getFilterFiles();
    
    
    if (fileList!=null) {
      for (int i=0;i<fileList.length;i++) {
        filterList.add(new UserFilter(fileList[i]));
      }
    }
    
    /* Create the array */
    ProgramFilter[] filterArr = new ProgramFilter[filterList.size()];
    filterList.toArray(filterArr);
    
    /* Sort the list*/
    File inxFile=new File(mFilterDirectory,FILTER_INDEX);
    BufferedReader inxIn=null;
    try {
      inxIn=new BufferedReader(new FileReader(inxFile));
      int cnt=0;
      String curFilterName=null;
      curFilterName=inxIn.readLine();
      while (curFilterName!=null){
        for (int i=cnt;i<filterArr.length;i++) {
          if (filterArr[i].toString().equalsIgnoreCase(curFilterName)) {
            //swap i<-->cnt
            ProgramFilter h = filterArr[cnt];
            filterArr[cnt] = filterArr[i];
            filterArr[i] = h;
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
    }finally{
      try{ if (inxIn!=null) inxIn.close();}catch(IOException e){};
    }    
    
    return filterArr;
    
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
      if (mFilterArr[i].toString().equalsIgnoreCase(filterName)) {
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
  }
}