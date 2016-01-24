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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.filtercomponents.AcceptNoneFilterComponent;
import tvbrowser.core.filters.filtercomponents.AgeLimitFilterComponent;
import tvbrowser.core.filters.filtercomponents.BeanShellFilterComponent;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.DateFilterComponent;
import tvbrowser.core.filters.filtercomponents.DayFilterComponent;
import tvbrowser.core.filters.filtercomponents.FavoritesFilterComponent;
import tvbrowser.core.filters.filtercomponents.KeywordFilterComponent;
import tvbrowser.core.filters.filtercomponents.MassFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginFilterComponent;
import tvbrowser.core.filters.filtercomponents.PluginIconFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramInfoFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramLengthFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramMarkingPriorityFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramRunningFilterComponent;
import tvbrowser.core.filters.filtercomponents.ProgramTypeFilterComponent;
import tvbrowser.core.filters.filtercomponents.ReminderFilterComponent;
import tvbrowser.core.filters.filtercomponents.SingleChannelFilterComponent;
import tvbrowser.core.filters.filtercomponents.SingleTitleFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.Channel;
import devplugin.PluginAccess;
import devplugin.PluginsFilterComponent;

public class FilterComponentList {

  private static FilterComponentList mInstance;

  private static ArrayList<FilterComponent> mComponentList;

  private static final Logger mLog = Logger.getLogger(FilterComponentList.class.getName());

  private FilterComponentList() {
    mComponentList = new ArrayList<FilterComponent>();

      File filterCompFile=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"filter.comp");

      if (filterCompFile.exists() && filterCompFile.isFile()) {
        StreamUtilities.objectInputStreamIgnoringExceptions(filterCompFile,
          0x1000, new ObjectInputStreamProcessor() {

          @Override
            public void process(final ObjectInputStream in) throws IOException {
              int version = in.readInt(); // version not yet used
              int compCnt = in.readInt();
              
              for (int i = 0; i < compCnt; i++) {
                if(version == 1) {
                  FilterComponent comp = null;
                  try {
                    comp = readComponent(in);
                  } catch (IOException e) {
                    mLog.warning("error reading filter component: " + e);
                  } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                  }
                  if (comp != null) {
                    synchronized (mComponentList) {
                      mComponentList.add(comp);
                    }
                  }
                }
                else if(version == 2) {
                  String key = in.readUTF();
                  
                  File componentFile = new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"java."+key+".dat");
                  
                  StreamUtilities.objectInputStreamIgnoringExceptions(componentFile,
                    0x1000, new ObjectInputStreamProcessor() {
                      @Override
                      public void process(ObjectInputStream inputStream) throws IOException {
                        inputStream.readInt();
                        int size = inputStream.readInt();
                        
                        for(int j = 0; j < size; j++) {
                          FilterComponent comp = null;
                          try {
                            comp = readComponent(inputStream);
                          } catch (IOException e) {
                            mLog.warning("error reading filter component: " + e);
                          } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                          }
                          if (comp != null) {
                            synchronized (mComponentList) {
                              mComponentList.add(comp);
                            }
                          }
                        }
                      }
                    }
                  );
                }
              }
              
              in.close();
            }
          });
      }
      //updateChannels(ChannelList.getSubscribedChannels());
  }
  
  public ArrayList<SingleChannelFilterComponent> updateChannels(Channel[] channels) {
    ArrayList<SingleChannelFilterComponent> toRemove = new ArrayList<SingleChannelFilterComponent>();
    ArrayList<Channel> toAdd = new ArrayList<Channel>();
    toAdd.addAll(Arrays.asList(channels));
    
    ArrayList<String> acceptNoneFilterComponentNames = new ArrayList<String>();
    ArrayList<SingleChannelFilterComponent> chammelNameUpdateList = new ArrayList<SingleChannelFilterComponent>();
    
    for(Iterator<FilterComponent> it = mComponentList.iterator(); it.hasNext(); ) {
      FilterComponent test = it.next();
      
      if(test instanceof SingleChannelFilterComponent) {
        boolean found = false;        
        
        for(Channel ch : channels) {
          if(((SingleChannelFilterComponent)test).containsChannel(ch)) {
            toAdd.remove(ch);
            
            found = true;
            break;
          }
        }
        
        if(!found) {
          toRemove.add((SingleChannelFilterComponent)test);
        } else if(((SingleChannelFilterComponent)test).isNameToUpdate()) {
          chammelNameUpdateList.add((SingleChannelFilterComponent)test);
        }
      }
      else if(test instanceof AcceptNoneFilterComponent) {
        acceptNoneFilterComponentNames.add(test.getName());
      }
    }
    
    for(SingleChannelFilterComponent remove : toRemove) {
      mComponentList.remove(remove);
    }
    
    for(Channel ch : toAdd) {
      SingleChannelFilterComponent test = new SingleChannelFilterComponent(ch);
      
      if(acceptNoneFilterComponentNames.contains(test.getName())) {
        remove(test.getName());
      }
      
      mComponentList.add(test);
    }
    
    return chammelNameUpdateList;
  }
  
  public void store() {
    final HashMap<String,ArrayList<FilterComponent>> filterTable =new HashMap<String,ArrayList<FilterComponent>>();
    
    Iterator<FilterComponent> it = mComponentList.iterator();
    
    while(it.hasNext()) {
      FilterComponent component = it.next();
      
      ArrayList<FilterComponent> componentList = filterTable.get(component.getClass().getCanonicalName());
      
      if(componentList == null) {
        componentList = new ArrayList<FilterComponent>();
        filterTable.put(component.getClass().getCanonicalName(), componentList);
      }
      
      componentList.add(component);
    }
    
    File filterCompFile=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"filter.comp");
    StreamUtilities.objectOutputStreamIgnoringExceptions(filterCompFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            out.writeInt(2); // version
            out.writeInt(filterTable.size());
            
            Set<String> filterKeys = filterTable.keySet();
            
            for(String key : filterKeys) {
              out.writeUTF(key);
              File componentFile = new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"java."+key+".dat");
              
              final ArrayList<FilterComponent> list = filterTable.get(key);
              
              StreamUtilities.objectOutputStreamIgnoringExceptions(componentFile,
                new ObjectOutputStreamProcessor() {
                  @Override
                  public void process(ObjectOutputStream outputStream) throws IOException {
                    outputStream.writeInt(1); // version for future use
                    outputStream.writeInt(list.size());
                    
                    Iterator<FilterComponent> it = list.iterator();
                    while (it.hasNext()) {
                      FilterComponent comp = it.next();
                      writeComponent(outputStream, comp);
                    }
                    
                    outputStream.close();
                  }
              });
            }
            
            out.close();
          }
        });
  }

  private void writeComponent(ObjectOutputStream out, FilterComponent comp) throws IOException {
    out.writeObject(comp.getClass().getName());
    out.writeInt(comp.getVersion());
    out.writeObject(comp.getName());
    out.writeObject(comp.getDescription());
    comp.write(out);
  }

  private FilterComponent readComponent(ObjectInputStream in) throws IOException, ClassNotFoundException {
    String className=(String)in.readObject();
    int version = in.readInt();
    String name = (String)in.readObject();
    String description = (String)in.readObject();
    FilterComponent filterComponent = null;
    if (className.endsWith(".AgeLimitFilterComponent")) {
      filterComponent = new AgeLimitFilterComponent(name, description);
    } else if (className.endsWith(".BeanShellFilterComponent")) {
      filterComponent = new BeanShellFilterComponent(name, description);
    } else if (className.endsWith(".ChannelFilterComponent")) {
      filterComponent = new ChannelFilterComponent(name, description);
    } else if (className.endsWith(".DayFilterComponent")) {
      filterComponent = new DayFilterComponent(name, description);
    } else if (className.endsWith(".DateFilterComponent")) {
      filterComponent = new DateFilterComponent(name, description);
    } else if (className.endsWith(".FavoritesFilterComponent")) {
      filterComponent = new FavoritesFilterComponent(name, description);
    } else if (className.endsWith(".KeywordFilterComponent")) {
      filterComponent = new KeywordFilterComponent(name, description);
    } else if (className.endsWith(".MassFilterComponent")) {
      filterComponent = new MassFilterComponent(name, description);
    } else if (className.endsWith(".PluginFilterComponent")) {
      filterComponent = new PluginFilterComponent(name, description);
    } else if (className.endsWith(".PluginIconFilterComponent")) {
      filterComponent = new PluginIconFilterComponent(name, description);
    } else if (className.endsWith(".ProgramInfoFilterComponent")) {
      filterComponent = new ProgramInfoFilterComponent(name, description);
    } else if (className.endsWith(".ProgramLengthFilterComponent")) {
      filterComponent = new ProgramLengthFilterComponent(name, description);
    } else if (className.endsWith(".ProgramMarkingPriorityFilterComponent")) {
      filterComponent = new ProgramMarkingPriorityFilterComponent(name,
          description);
    } else if (className.endsWith(".ProgramRunningFilterComponent")) {
      filterComponent = new ProgramRunningFilterComponent(name, description);
    } else if (className.endsWith(".ProgramTypeFilterComponent")) {
      filterComponent = new ProgramTypeFilterComponent(name, description);
    } else if (className.endsWith(".ReminderFilterComponent")) {
      filterComponent = new ReminderFilterComponent(name, description);
    } else if (className.endsWith(".SingleTitleFilterComponent")) {
      filterComponent = new SingleTitleFilterComponent(name, description);
    } else if (className.endsWith(".TimeFilterComponent")) {
      filterComponent = new TimeFilterComponent(name, description);
    } else if (className.endsWith(".SingleChannelFilterComponent")) {
      filterComponent = new SingleChannelFilterComponent(null);
    } else if (className.endsWith(".AcceptNoneFilterComponent")) {
      filterComponent = new AcceptNoneFilterComponent(name);
    }
    else {
      try {
        PluginAccess[] plugins = PluginManagerImpl.getInstance().getActivatedPlugins();

        for(PluginAccess plugin : plugins) {
          Class<? extends PluginsFilterComponent>[] clazzes = plugin.getAvailableFilterComponentClasses();

          if(clazzes != null) {
            for(Class<? extends PluginsFilterComponent> clazz : clazzes) {
              if(clazz.getName().compareTo(className) == 0) {
                filterComponent = clazz.newInstance();
                break;
              }
            }
          }
        }

        filterComponent.setName(name);
        filterComponent.setDescription(description);
      }catch(Exception e) {
        //throw new IOException("error reading filter component: "+className+" unknown");
        mLog.warning("error reading filter component: "+className+" unknown");
        return null;
      }
    }

    if (filterComponent!=null) {
      filterComponent.read(in, version);

      /*
       * If the FilterComponent is for a Plugin we have to check
       * if it was a Plugin that is in the core now. If it is so
       * we have to change the FilterComponent to the right one.
       */
      if(filterComponent instanceof PluginFilterComponent) {
        String pluginId = ((PluginFilterComponent)filterComponent).getPluginId();

        if(pluginId.compareTo("java.reminderplugin.ReminderPlugin") == 0) {
          filterComponent = new ReminderFilterComponent(name, description);
        } else if(pluginId.compareTo("java.favoritesplugin.FavoritesPlugin") == 0) {
          filterComponent = new FavoritesFilterComponent(name, description);
        }
      }
    }
    return filterComponent;
  }


  public FilterComponent[] getAvailableFilterComponents() {
    synchronized (mComponentList) {
      return mComponentList.toArray(new FilterComponent[mComponentList.size()]);
    }
  }
  public FilterComponent getFilterComponentByName(String name) {
    synchronized (mComponentList) {
      for(FilterComponent c : mComponentList) {
        if(c.getName().compareTo(name) == 0) {
          return c;
        }
      }
    }
    
    return null;
  }


  public static synchronized FilterComponentList getInstance() {
    if (mInstance == null) {
      mInstance = new FilterComponentList();
      Settings.updateChannelFilters(ChannelList.getSubscribedChannels(),false);
    }
    
    return mInstance;
  }

  public void add(FilterComponent comp) {
    synchronized (mComponentList) {
      final String name = comp.getName();
      
      for(int i = 0; i < mComponentList.size(); i++) {
        if(name.equals(mComponentList.get(i).getName()) && mComponentList.get(i) instanceof AcceptNoneFilterComponent) {
          mComponentList.remove(i);
          break;
        }
      }
      
      mComponentList.add(comp);
    }
    
    store();
    
    if(comp instanceof ChannelFilterComponent) {
      ChannelFilterList.getInstance().fireChannelFilterAdded((ChannelFilterComponent)comp);
    }
    //mComponentMap.put(comp.getName().toUpperCase(), comp);
  }

  public boolean exists(String name) {
    FilterComponent c = getFilterComponentByName(name);
    
    return  c != null && !(c instanceof AcceptNoneFilterComponent);
    //return mComponentMap.containsKey(name.toUpperCase());
  }
  
  public void remove(String filterCompName) {
    FilterComponent filterComp = getFilterComponentByName(filterCompName);
    
    synchronized (mComponentList) {
      if(mComponentList.remove(filterComp)) {
        if(filterComp instanceof ChannelFilterComponent) {
          ChannelFilterList.getInstance().fireChannelFilterRemoved((ChannelFilterComponent)filterComp);
        }
        
        String key = filterComp.getClass().getCanonicalName();
        
        File componentFile = new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"java."+key+".dat");
        
        if(componentFile.isFile()) {
          if(!componentFile.delete()) {
            componentFile.deleteOnExit();
          }
        }
      }
    }
    
    store();
    //mComponentMap.remove(filterCompName.toUpperCase());
  }
    
  public String[] getChannelFilterNames() {
    ArrayList<String> channelFilters = new ArrayList<String>();
    for (FilterComponent component : getAvailableFilterComponents()) {
      if (component instanceof ChannelFilterComponent) {
        channelFilters.add(component.getName());
      }
    }
    String[] sortedArray = new String[channelFilters.size()];
    channelFilters.toArray(sortedArray);
    Arrays.sort(sortedArray);
    return sortedArray;
  }
  
  private static final int TYPE_BROKEN_FILTER_COMPONENT_NONE = 0;
  private static final int TYPE_BROKEN_FILTER_COMPONENT_PARTLY = 1;
  private static final int TYPE_BROKEN_FILTER_COMPONENT_COMPLETELY = 2;
  
  public static final String getLabelForComponent(FilterComponent component, String label) {
    String result = component != null && label == null ? component.getName() : (label != null ? label : null);
    int type = component instanceof AcceptNoneFilterComponent ? TYPE_BROKEN_FILTER_COMPONENT_COMPLETELY : TYPE_BROKEN_FILTER_COMPONENT_NONE;
    
    if(type == TYPE_BROKEN_FILTER_COMPONENT_NONE) {
      try {
        final Method isBrokenCompletely = component.getClass().getMethod("isBrokenCompletely");
        Object brokenResult = isBrokenCompletely.invoke(component);
        
        if(brokenResult instanceof Boolean && ((Boolean)brokenResult).booleanValue()) {
          type = TYPE_BROKEN_FILTER_COMPONENT_COMPLETELY;          
        }
        else {
          final Method isBrokenPartially = component.getClass().getMethod("isBrokenPartially");
          brokenResult = isBrokenPartially.invoke(component);
          
          if(brokenResult instanceof Boolean && ((Boolean)brokenResult).booleanValue()) {
            type = TYPE_BROKEN_FILTER_COMPONENT_PARTLY;
          }
        }
        
      }catch(Exception e) {
        //e.printStackTrace();
      }
    }
    
    switch(type) {
      case TYPE_BROKEN_FILTER_COMPONENT_COMPLETELY: result = "<html><span style=\"color:orange;text-decoration:line-through\">"+result+"</span></html>";break;
      case TYPE_BROKEN_FILTER_COMPONENT_PARTLY: result = "<html><span style=\"color:orange;text-decoration:underline\">"+result+"</span></html>";break;
    }
    
    return result;
  }
}