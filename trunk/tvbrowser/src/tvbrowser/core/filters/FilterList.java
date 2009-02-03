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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.searchfield.SearchFilter;
import util.io.stream.BufferedReaderProcessor;
import util.io.stream.BufferedWriterProcessor;
import util.io.stream.StreamUtilities;
import devplugin.PluginAccess;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

public class FilterList {

  private static FilterList mInstance;
  private File mFilterDirectory;
  private ProgramFilter[] mFilterArr;
  private final static String FILTER_INDEX = "filter.index";
  public static final String FILTER_DIRECTORY = Settings.getUserSettingsDirName() + "/filters";


  private static java.util.logging.Logger mLog
          = java.util.logging.Logger.getLogger(FilterList.class.getName());

  private FilterList() {
    create();
  }

  public void create() {
    mFilterDirectory = new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY);
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
    final HashMap<String, ProgramFilter> filterList = new HashMap<String, ProgramFilter>();

    /* Add default filters. The user may not remove them. */

    ProgramFilter showAll = new ShowAllFilter();
    filterList.put(showAll.getName(), showAll);
    ProgramFilter pluginFilter = new PluginFilter();
    filterList.put(pluginFilter.getName(), pluginFilter);
    ProgramFilter subtitleFilter = new SubtitleFilter();
    filterList.put(subtitleFilter.getName(), subtitleFilter);
    ProgramFilter audioDescriptionFilter = new AudioDescriptionFilter();
    filterList.put(audioDescriptionFilter.getName(), audioDescriptionFilter);

    /* Read the available filters from the file system and add them to the array */
    if (mFilterDirectory == null) {
      throw new NullPointerException("directory is null");
    }


    File[] fileList = getFilterFiles();


    if (fileList != null) {
      for (File file : fileList) {
        UserFilter filter = null;
        try {
          filter = new UserFilter(file);
        } catch (ParserException e) {
          mLog.warning("error parsing filter from file " + file + "; exception: " + e);
        }
        if (filter != null) {
          filterList.put(filter.getName(), filter);
        }
      }
    }

    PluginAccess[] plugins = PluginManagerImpl.getInstance().getActivatedPlugins();

    for (PluginAccess plugin : plugins) {
      PluginsProgramFilter[] filters = plugin.getAvailableFilter();

      if (filters != null)
        for (PluginsProgramFilter filter : filters)
          filterList.put(filter.getName(), filter);
    }

    final ArrayList<ProgramFilter> filterArr = new ArrayList<ProgramFilter>();

    /* Sort the list*/
    try {
      StreamUtilities.bufferedReader(new File(mFilterDirectory, FILTER_INDEX),
          new BufferedReaderProcessor() {
            public void process(BufferedReader inxIn) throws IOException {
              String curFilterName = inxIn.readLine();
              while (curFilterName != null) {
                if (curFilterName.equals("[SEPARATOR]")) {
                  filterArr.add(new SeparatorFilter());
                } else {
                  ProgramFilter filter = filterList.get(curFilterName);

                  if (filter != null) {
                    filterArr.add(filter);
                    filterList.remove(curFilterName);
                  }
                }

                curFilterName = inxIn.readLine();
              }
            }
          });
    } catch (FileNotFoundException e) {
      // ignore
    } catch (IOException e) {
      e.printStackTrace();
    }


    if (filterList.size() > 0) {
      for (ProgramFilter programFilter : filterList.values()) {
        filterArr.add(programFilter);
      }
    }

    return filterArr.toArray(new ProgramFilter[filterArr.size()]);
  }

  private File[] getFilterFiles() {
    return mFilterDirectory.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getAbsolutePath().endsWith(".filter");
      }
    });
  }

  public ProgramFilter[] getFilterArr() {
    if (SearchFilter.getInstance().isActive()) {
      ProgramFilter[] filter = new ProgramFilter[mFilterArr.length + 1];
      System.arraycopy(mFilterArr, 0, filter, 0, mFilterArr.length);
      filter[mFilterArr.length] = SearchFilter.getInstance();
      return filter;
    }
    return mFilterArr;
  }

  public PluginsProgramFilter[] getPluginsProgramFiltersForPlugin(PluginAccess plugin) {
    ArrayList<PluginsProgramFilter> list = new ArrayList<PluginsProgramFilter>();

    for (ProgramFilter filter : mFilterArr) {
      if (filter instanceof PluginsProgramFilter) {
        if (((PluginsProgramFilter) filter).getPluginAccessOfFilter().equals(plugin))
          list.add((PluginsProgramFilter) filter);
      }
    }

    return list.toArray(new PluginsProgramFilter[list.size()]);
  }

  public UserFilter[] getUserFilterArr() {
    ArrayList<UserFilter> filterList = new ArrayList<UserFilter>();
    for (ProgramFilter filter : mFilterArr) {
      if (filter instanceof UserFilter) {
        filterList.add((UserFilter) filter);
      }
    }

    return filterList.toArray(new UserFilter[filterList.size()]);
  }


  public boolean containsFilter(String filterName) {
    for (ProgramFilter filter : mFilterArr) {
      if (filter.getName().equalsIgnoreCase(filterName)) {
        return true;
      }
    }
    return false;
  }

  public void setProgramFilterArr(ProgramFilter[] filterArr) {
    mFilterArr = filterArr;
  }

  public void addProgramFilter(ProgramFilter filter) {
    ProgramFilter[] newFilterArr = new ProgramFilter[mFilterArr.length + 1];

    System.arraycopy(mFilterArr, 0, newFilterArr, 0, mFilterArr.length);
    newFilterArr[newFilterArr.length - 1] = filter;

    mFilterArr = newFilterArr;
    store();
  }

  public void remove(ProgramFilter filter) {
    ArrayList<ProgramFilter> filterList = new ArrayList<ProgramFilter>();
    for (ProgramFilter programFilter : mFilterArr) {
      if (!programFilter.equals(filter)) {
        filterList.add(programFilter);
      }
    }
    mFilterArr = filterList.toArray(new ProgramFilter[filterList.size()]);
    store();
  }

  public void store() {
    /* delete all filters*/
    File[] fileList = getFilterFiles();
    if (fileList != null) {
      for (File file : fileList) {
        file.delete();
      }
    }

    for (ProgramFilter filter : mFilterArr) {
      if (filter instanceof UserFilter) {
        ((UserFilter) filter).store();
      }
    }

    File inxFile = new File(mFilterDirectory, FILTER_INDEX);
    StreamUtilities.bufferedWriterIgnoringExceptions(inxFile,
        new BufferedWriterProcessor() {
          public void process(BufferedWriter writer) throws IOException {
            for (ProgramFilter filter : mFilterArr) {
              writer.write(filter.getName() + "\n");
            }
            writer.close();
          }
        });
  }

  /**
   * Returns the Filter named "name"
   *
   * @param name Name of Filter to return
   * @return Filter with Name "name" or null if not found
   */
  public ProgramFilter getFilterByName(String name) {
    if (name == null) {
      return null;
    }

    for (ProgramFilter filter : mFilterArr) {
      if (filter.getName().equals(name)) {
        return filter;
      }
    }

    return null;
  }

  /**
   * Gets the "ShowAll" filter
   *
   * @return The "ShowAll" filter
   * @since 2.6
   */
  protected ProgramFilter getAllFilter() {
    for (ProgramFilter filter : mFilterArr) {
      if (filter.getClass().getName().equals("tvbrowser.core.filters.ShowAllFilter")) {
        return filter;
      }
    }

    return new ShowAllFilter();
  }

  /**
   * Returns the Default-Filter.
   *
   * @return the Default-Filter
   */
  protected ProgramFilter getDefaultFilter() {
    ProgramFilter allFilter = null;

    String filterId = Settings.propDefaultFilter.getString();
    String filterName = null;

    if (filterId != null && !filterId.equals("")) {
      String[] filterValues = filterId.split("###");
      filterId = filterValues[0];
      filterName = filterValues[1];
    }

    for (ProgramFilter filter : mFilterArr) {
      if (filter.getClass().getName().equals("tvbrowser.core.filters.ShowAllFilter")) {
        allFilter = filter;
      } else
      if (filterName != null && filter.getClass().getName().equals(filterId) && filter.getName().equals(filterName)) {
        return filter;
      }
    }

    if (allFilter != null) {
      return allFilter;
    }

    return new ShowAllFilter();
  }
}