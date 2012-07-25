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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JMenu;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import tvbrowser.ui.filter.dlgs.FilterNode;
import tvbrowser.ui.filter.dlgs.FilterTreeModel;
import tvbrowser.ui.mainframe.searchfield.SearchFilter;
import util.io.stream.BufferedReaderProcessor;
import util.io.stream.StreamUtilities;
import devplugin.PluginAccess;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramFilter;

public class FilterList {
  private static FilterList mInstance;
  private static File mFilterDirectory;
  private File mFilterDat;
  private final static String FILTER_INDEX = "filter.index";
  protected static final String FILTER_DIRECTORY = Settings
      .getUserSettingsDirName()
      + "/filters";

  private FilterTreeModel mFilterTreeModel;
  private final static String FILTER_TREE_DAT = "filters.dat";

  private static final Logger mLog
          = Logger.getLogger(FilterList.class.getName());

  private FilterList() {
    create();
  }
  
  /**
   * Localizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FilterList.class);

  
  public static File getFilterDirectory() {
    return mFilterDirectory;
  }

  public void create() {
    mFilterDirectory = new File(tvbrowser.core.filters.FilterList.FILTER_DIRECTORY);
    mFilterDat = new File(mFilterDirectory,FILTER_TREE_DAT);
    if (!mFilterDirectory.exists()) {
      mFilterDirectory.mkdirs();
    }
    createFilterList();
  }

  public static FilterList getInstance() {
    if (mInstance == null) {
      mInstance = new FilterList();
    }
    return mInstance;
  }

  private void createFilterList() {try {
    if(mFilterDat.isFile()) {
      try {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(mFilterDat));
        
        mFilterTreeModel = FilterTreeModel.initInstance(in);
        
        in.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      mFilterTreeModel = FilterTreeModel.initInstance(new ProgramFilter[0]);
    }
    final HashMap<String, ProgramFilter> filterList = new HashMap<String, ProgramFilter>();
 

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

    /* Sort the list*/
    try {
      File filterFile = new File(mFilterDirectory, FILTER_INDEX);
      if (filterFile.canRead()) {
          StreamUtilities.bufferedReader(filterFile,
          new BufferedReaderProcessor() {
            public void process(BufferedReader inxIn) throws IOException {
              String curFilterName = inxIn.readLine();
              while (curFilterName != null) {
                if (curFilterName.equals("[SEPARATOR]")) {
                  mFilterTreeModel.addFilter(new SeparatorFilter());
                } else {
                  ProgramFilter filter = filterList.get(curFilterName);

                  if (filter != null) {
                    if (!containsFilter(curFilterName)) {
                      mFilterTreeModel.addFilter(filter);
                    }
                    filterList.remove(curFilterName);
                  }
                }

                curFilterName = inxIn.readLine();
              }
            }
          });
      }
    } catch (FileNotFoundException e) {
      // ignore
    } catch (IOException e) {
      e.printStackTrace();
    }


    if (filterList.size() > 0) {
      for (ProgramFilter programFilter : filterList.values()) {
        if (!containsFilter(programFilter.getName())) {
          mFilterTreeModel.addFilter(programFilter);
        }
      }
    }
    
    /* Add default filters. The user may not remove them. */
    ProgramFilter showAll = new ShowAllFilter();
    if (!containsFilter(showAll.getName())) {
      mFilterTreeModel.addFilter(showAll);
    }
    ProgramFilter pluginFilter = new PluginFilter();
    if (!containsFilter(pluginFilter.getName())) {
      mFilterTreeModel.addFilter(pluginFilter);
    }
    
    //add default attributes
    String attributesDir = mLocalizer.msg("ProgramAttributes", "program attributes");
    
    addInfoBitFilter("[SUBTITLE_FILTER]", attributesDir);
    addInfoBitFilter("[AUDIO_DESCRIPTION_FILTER]", attributesDir);
    addInfoBitFilter("[ORIGINAL_AUDIO_FILTER]", attributesDir);
    addInfoBitFilter("[HD_FILTER]", attributesDir);
    addInfoBitFilter("[NEW_FILTER]", attributesDir);
    
    //add default categories
    String categoriesDir = mLocalizer.msg("ProgramCategories", "program categories");
    addInfoBitFilter("[MOVIE_FILTER]", categoriesDir);
    addInfoBitFilter("[SERIES_FILTER]", categoriesDir);
    addInfoBitFilter("[SHOW_FILTER]", categoriesDir);
    addInfoBitFilter("[DOCUMENTARY_FILTER]", categoriesDir);
    addInfoBitFilter("[MAGAZINE_FILTER]", categoriesDir);
    addInfoBitFilter("[NEWS_FILTER]", categoriesDir);
    addInfoBitFilter("[SPORTS_FILTER]", categoriesDir);
    addInfoBitFilter("[ARTS_FILTER]", categoriesDir);
    addInfoBitFilter("[CHILDRENS_FILTER]", categoriesDir);
    addInfoBitFilter("[OTHERS_FILTER]", categoriesDir);
    addInfoBitFilter("[UNCATEGORIZED_FILTER]", categoriesDir);

    
    }catch(Throwable t) {t.printStackTrace();}
    
    mFilterTreeModel.addPluginsProgramFilters();
  }
  
  private void addInfoBitFilter(String name, String directoryName) {
    FilterNode root = (FilterNode)mFilterTreeModel.getRoot();
    FilterNode directoryNode = mFilterTreeModel.getDirectoryNode(directoryName, root);

    ProgramFilter filter = new InfoBitFilter(name);
    if (!containsFilter(filter.getName())) {
      if (directoryNode == null) {
        directoryNode = mFilterTreeModel.addDirectory(directoryName, root);
      }
      directoryNode.addFilter(filter);
    }
  }

  private File[] getFilterFiles() {
    return mFilterDirectory.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.getAbsolutePath().endsWith(".filter");
      }
    });
  }
  
  public void createFilterMenu(JMenu filterMenu, ProgramFilter curFilter) {
    mFilterTreeModel.createMenu(filterMenu,curFilter);
  }

  public ProgramFilter[] getFilterArr() {
    ProgramFilter[] mFilterArr = mFilterTreeModel.getAllFilters();
    
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

    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
      if (filter instanceof PluginsProgramFilter) {
        if (((PluginsProgramFilter) filter).getPluginAccessOfFilter().equals(plugin)) {
          list.add((PluginsProgramFilter) filter);
        }
      }
    }

    return list.toArray(new PluginsProgramFilter[list.size()]);
  }

  public UserFilter[] getUserFilterArr() {
    ArrayList<UserFilter> filterList = new ArrayList<UserFilter>();
    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
      if (filter instanceof UserFilter) {
        filterList.add((UserFilter) filter);
      }
    }

    return filterList.toArray(new UserFilter[filterList.size()]);
  }


  public boolean containsFilter(String filterName) {
    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
      if (filter.getName().equalsIgnoreCase(filterName)) {
        return true;
      }
    }
    return false;
  }

  public void setProgramFilterArr(ProgramFilter[] filterArr) {
    //TODO
    //mFilterArr = filterArr;
  }

  public void addProgramFilter(ProgramFilter filter) {    
    mFilterTreeModel.addFilter(filter);

    store();
  }

  public void remove(ProgramFilter filter) {
    mFilterTreeModel.deleteFilter(filter);
    store();
  }

  public void store() {
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mFilterDat));
      
      mFilterTreeModel.storeData(out);
      
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    /* delete all filters*/
    File[] fileList = getFilterFiles();
    if (fileList != null) {
      for (File file : fileList) {
        file.delete();
      }
    }

    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
      if (filter instanceof UserFilter) {
        ((UserFilter) filter).store();
      }
    }
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

    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
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
    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
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
    try {
    ProgramFilter allFilter = null;

    String filterId = Settings.propDefaultFilter.getString();
    String filterName = null;

    if (StringUtils.isNotEmpty(filterId)) {
      String[] filterValues = filterId.split("###");
      filterId = shortFilterClassName(filterValues[0]);
      filterName = filterValues[1];
    }

    for (ProgramFilter filter : mFilterTreeModel.getAllFilters()) {
      if (filter.getClass().getName().equals("tvbrowser.core.filters.ShowAllFilter")) {
        allFilter = filter;
      } else
      if (filterName != null && shortFilterClassName(filter.getClass().getName()).equals(filterId) && filter.getName().equals(filterName)) {
        return filter;
      }
    }

    if (allFilter != null) {
      return allFilter;
    }
    }catch(Throwable t) {t.printStackTrace();}
    return new ShowAllFilter();
  }

  private String shortFilterClassName(final String className) {
    int index = className.lastIndexOf('$');
    if (index > 0) {
      return className.substring(0, index);
    }
    return className;
  }
  
  public FilterTreeModel getFilterTreeModel() {
    return mFilterTreeModel;
  }
}