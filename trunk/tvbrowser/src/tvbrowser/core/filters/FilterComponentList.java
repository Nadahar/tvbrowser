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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

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
import tvbrowser.core.filters.filtercomponents.SingleTitleFilterComponent;
import tvbrowser.core.filters.filtercomponents.TimeFilterComponent;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
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
              in.readInt(); // version not yet used
              int compCnt = in.readInt();
              for (int i = 0; i < compCnt; i++) {
                FilterComponent comp = null;
                try {
                  comp = readComponent(in);
                } catch (IOException e) {
                  mLog.warning("error reading filter component: " + e);
                } catch (ClassNotFoundException e) {
                  e.printStackTrace();
                }
                if (comp != null) {
                  mComponentList.add(comp);
                }
              }
            }
          });
      }
  }


  public void store() {
    File filterCompFile=new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY,"filter.comp");
    StreamUtilities.objectOutputStreamIgnoringExceptions(filterCompFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            out.writeInt(1); // version
            out.writeInt(mComponentList.size());
            Iterator<FilterComponent> it = mComponentList.iterator();
            while (it.hasNext()) {
              FilterComponent comp = it.next();
              writeComponent(out, comp);
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
    return mComponentList.toArray(new FilterComponent[mComponentList.size()]);
  }


  public FilterComponent getFilterComponentByName(String name) {
    for(FilterComponent c : mComponentList) {
      if(c.getName().compareTo(name) == 0) {
        return c;
      }
    }

    return null;
  }


  public static FilterComponentList getInstance() {
    if (mInstance == null) {
      mInstance = new FilterComponentList();
    }
    return mInstance;
  }

  public void add(FilterComponent comp) {
    mComponentList.add(comp);
    //mComponentMap.put(comp.getName().toUpperCase(), comp);
  }

  public boolean exists(String name) {
    return getFilterComponentByName(name) != null;
    //return mComponentMap.containsKey(name.toUpperCase());
  }
  public void remove(String filterCompName) {
    mComponentList.remove(getFilterComponentByName(filterCompName));
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
}