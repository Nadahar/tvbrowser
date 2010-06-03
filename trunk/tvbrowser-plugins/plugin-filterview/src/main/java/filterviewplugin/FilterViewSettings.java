/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package filterviewplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

import util.settings.PropertyBasedSettings;
import devplugin.Plugin;
import devplugin.ProgramFilter;

class FilterViewSettings extends PropertyBasedSettings {

  private static final String KEY_FILTER_NAMES = "filterNames";
  private static final String SEPARATOR = "|||";
  private static final String KEY_DAYS = "days";
  private static final String KEY_ICON = "icon";
  private HashMap<ProgramFilter, Icon> mIconCache = new HashMap<ProgramFilter, Icon>();
  private ProgramFilter[] mActiveFilters;
  private ProgramFilter[] mActiveFiltersWithIcon;

  FilterViewSettings(final Properties properties) {
    super(properties);
  }

  static String[] getAvailableFilterNames() {
    ArrayList<String> names = new ArrayList<String>();
    for (ProgramFilter filter : getAvailableFilters()) {
      names.add(filter.getName());
    }
    return names.toArray(new String[names.size()]);
  }

  private static ProgramFilter[] getAvailableFilters() {
    ProgramFilter[] allFilters = Plugin.getPluginManager().getFilterManager().getAvailableFilters().clone();
    Arrays.sort(allFilters, new Comparator<ProgramFilter>() {

      public int compare(ProgramFilter f1, ProgramFilter f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
      }
    });
    ArrayList<ProgramFilter> filters = new ArrayList<ProgramFilter>(Arrays.asList(allFilters));
    filters.remove(Plugin.getPluginManager().getFilterManager().getAllFilter());
    return filters.toArray(new ProgramFilter[filters.size()]);
  }

  String[] getActiveFilterNames() {
    return StringUtils.split(get(KEY_FILTER_NAMES, ""), SEPARATOR);
  }

  ProgramFilter[] getActiveFilters() {
    if (mActiveFilters == null) {
      ArrayList<ProgramFilter> list = new ArrayList<ProgramFilter>();
      List<String> filterNames = Arrays.asList(getActiveFilterNames());
      for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
        if (filterNames.contains(filter.getName())) {
          list.add(filter);
        }
      }
      mActiveFilters = list.toArray(new ProgramFilter[list.size()]);
    }
    return mActiveFilters;
  }

  ProgramFilter[] getActiveFiltersWithIcon() {
    if (mActiveFiltersWithIcon == null) {
      ArrayList<ProgramFilter> list = new ArrayList<ProgramFilter>();
      for (ProgramFilter filter : getActiveFilters()) {
        if (getFilterIcon(filter) != null) {
          list.add(filter);
        }
      }
      mActiveFiltersWithIcon = list.toArray(new ProgramFilter[list.size()]);
    }
    return mActiveFiltersWithIcon;
  }

  void setActiveFilterNames(Object[] objects) {
    set(KEY_FILTER_NAMES, StringUtils.join(objects, SEPARATOR));
    mActiveFilters = null;
    mActiveFiltersWithIcon = null;
    FilterViewPlugin.getInstance().updateRootNode();
  }

  int getDays() {
    return get(KEY_DAYS, 3);
  }

  void setDays(int days) {
    set(KEY_DAYS, days);
  }

  public Icon getFilterIcon(ProgramFilter filter) {
    if (!mIconCache.containsKey(filter)) {
      String fileName = getFilterIconName(filter);
      if (!StringUtils.isEmpty(fileName)) {
        mIconCache.put(filter, new ImageIcon(getIconDirectoryName() + File.separatorChar + fileName));
      }
      else {
        mIconCache.put(filter, null);
      }
    }
    return mIconCache.get(filter);
  }

  static String getIconDirectoryName() {
    File dir = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"filtericons");
    return dir.toString();
  }

  String getFilterIconName(ProgramFilter filter) {
    String fileName = get(KEY_ICON + filter.getName());
    if (StringUtils.isEmpty(fileName)) {
      return null;
    }
    return fileName;
  }

  protected ProgramFilter getFilter(final String filterName) {
    for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
      if (filter.getName().equalsIgnoreCase(filterName)) {
        return filter;
      }
    }
    return null;
  }

  public void setFilterIconName(final ProgramFilter filter, final String fileName) {
    mIconCache.clear();
    mActiveFiltersWithIcon = null;
    if (StringUtils.isEmpty(fileName)) {
      set(KEY_ICON + filter.getName(), "");
      return;
    }
    set(KEY_ICON + filter.getName(), fileName);
  }
}
