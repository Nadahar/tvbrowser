package tvbrowser.core.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.KeywordFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramInfoFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramLengthFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;

public class FilterComponentList {
  
  private static FilterComponentList mInstance;
  
  private static HashMap mComponentMap;
  
  private FilterComponentList() {
    mComponentMap = new HashMap();
    ObjectInputStream in=null;
    
    try {
    
    
      File filterCompFile=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"filter.comp");
      
      if (filterCompFile.exists() && filterCompFile.isFile()) {
      
        in=new ObjectInputStream(new FileInputStream(filterCompFile));        
        int version=in.readInt();
        int compCnt=in.readInt();
        for (int i=0; i<compCnt; i++) {
          FilterComponent comp = readComponent(in);
          if (comp != null) {
            mComponentMap.put(comp.getName().toUpperCase(), comp);                     
          }
        }
        in.close();
      }
      
    }catch (FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {
      e.printStackTrace();
    }catch(ClassNotFoundException e) {
      e.printStackTrace();
    }finally {
      if (in!=null) {
        try { in.close(); } catch(IOException exc) {}      
      }
    }     
  }
  
  
  public void store() {
    System.out.println("storing components...");
    File filterCompFile=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"filter.comp");
    try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filterCompFile));
		  out.writeInt(1);
      out.writeInt(mComponentMap.size());
      Iterator it = mComponentMap.values().iterator();
      while (it.hasNext()) {
        FilterComponent comp = (FilterComponent)it.next();
        writeComponent(out,comp);
      }
      out.close();
    
    
    
    } catch (IOException e) {
			e.printStackTrace();
		}
    
  }
  
  private void writeComponent(ObjectOutputStream out, FilterComponent comp) throws IOException {
    out.writeObject(comp.getClass().getName());
    out.writeInt(1);
    out.writeObject(comp.getName());
    out.writeObject(comp.getDescription());
    comp.write(out);
    System.out.println("component "+comp.getName()+" written");
  }
  
  private FilterComponent readComponent(ObjectInputStream in) throws IOException, ClassNotFoundException {
        
    
    String className=(String)in.readObject();
    int version = in.readInt();
    String name = (String)in.readObject();
    String description = (String)in.readObject();
    FilterComponent filterComponent = null;
    if (className.endsWith(".KeywordFilterComponent")) {
      filterComponent = new KeywordFilterComponent(name, description);
    }
    else if (className.endsWith(".PluginFilterComponent")) {
      filterComponent = new PluginFilterComponent(name, description);
    }
    else if (className.endsWith(".ChannelFilterComponent")) {
      filterComponent = new ChannelFilterComponent(name, description);
    }
    else if (className.endsWith(".TimeFilterComponent")) {
        filterComponent = new TimeFilterComponent(name, description);
    }
    else if (className.endsWith(".ProgramInfoFilterComponent")) {
        filterComponent = new ProgramInfoFilterComponent(name, description);
  }
    else if (className.endsWith(".ProgramLengthFilterComponent")) {
        filterComponent = new ProgramLengthFilterComponent(name, description);
  }
    else {
      throw new IOException("error reading filter component: "+className+" unknown");      
    }
   
    if (filterComponent!=null) {
      filterComponent.read(in, version);
    } 
    return filterComponent; 
  }
  
  
  public FilterComponent[] getAvailableFilterComponents() {
    
    FilterComponent[] result = new FilterComponent[mComponentMap.size()];
    mComponentMap.values().toArray(result);
    
    return result;
  }
  
  
  public FilterComponent getFilterComponentByName(String name) {
    return (FilterComponent)mComponentMap.get(name.toUpperCase());
  }
  
  
  public static FilterComponentList getInstance() {
    if (mInstance == null) {
      mInstance = new FilterComponentList();
    }
    return mInstance;
  }
  
  public void add(FilterComponent comp) {
    
    mComponentMap.put(comp.getName().toUpperCase(), comp); 
  }
  
  public void remove(String filterCompName) {
    mComponentMap.remove(filterCompName.toUpperCase());
  }
  
  public boolean exists(String name) {
    return mComponentMap.containsKey(name.toUpperCase());  
  }
  
  

}