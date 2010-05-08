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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import util.settings.PropertyBasedSettings;
import devplugin.Plugin;
import devplugin.ProgramFilter;

class FilterViewSettings extends PropertyBasedSettings {

  private static final String KEY_FILTER_NAMES = "filterNames";
  private static final String SEPARATOR = "|||";
  private static final String KEY_DAYS = "days";

  public FilterViewSettings(final Properties properties) {
    super(properties);
  }

  public String[] getAvailableFilterNames() {
    ArrayList<String> names = new ArrayList<String>();
    for (ProgramFilter filter : getAvailableFilters()) {
      names.add(filter.getName());
    }
    return names.toArray(new String[names.size()]);
  }

  private ProgramFilter[] getAvailableFilters() {
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

  public String[] getActiveFilterNames() {
    return StringUtils.split(get(KEY_FILTER_NAMES, ""), SEPARATOR);
  }

  public ProgramFilter[] getActiveFilters() {
    ArrayList<ProgramFilter> result = new ArrayList<ProgramFilter>();
    List<String> filterNames = Arrays.asList(getActiveFilterNames());
    for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
      if (filterNames.contains(filter.getName())) {
        result.add(filter);
      }
    }
    return result.toArray(new ProgramFilter[result.size()]);
  }

  public void setActiveFilterNames(Object[] objects) {
    set(KEY_FILTER_NAMES, StringUtils.join(objects, SEPARATOR));
    FilterViewPlugin.getInstance().updateRootNode();
  }

  public int getDays() {
    return get(KEY_DAYS, 3);
  }

  public void setDays(int days) {
    set(KEY_DAYS, days);
  }

}
