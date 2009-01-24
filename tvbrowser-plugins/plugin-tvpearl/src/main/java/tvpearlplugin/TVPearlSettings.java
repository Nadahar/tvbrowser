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
package tvpearlplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Properties;

import devplugin.Program;

/**
 * @author Bananeweizen
 * 
 */
public class TVPearlSettings {

  private static final String VALUE_FALSE = "0";
  private static final String VALUE_TRUE = "1";
  private static final String KEY_VIEW_OPTION = "ViewOption";
  private static final String KEY_MARK_PRIORITY = "MarkPriority";
  private static final String KEY_SHOW_INFO_MODAL = "ShowInfoModal";
  private static final String KEY_UPDATE_AFTER_UPDATE_FINISHED = "UpdateAfterUpdateFinished";
  private static final String KEY_UPDATE_AT_START = "UpdateAtStart";
  private static final String KEY_MARK_PEARL = "MarkPearl";
  private static final String KEY_SHOW_ENABLE_FILTER = "ShowEnableFilter";
  private static final String KEY_UPDATE_MANUAL = "UpdateManual";
  private static final String KEY_SHOW_FILTER = "ShowFilter";
  private static final String DEFAULT_URL = "http://hilfe.tvbrowser.org/viewtopic.php?t=1470";
  
  private final static int SHOW_ALL_PEARLS = 1;
  private final static int SHOW_SUBSCRIBED_CHANNELS = 2;
  private final static int SHOW_FOUND_PEARLS = 3;
  
  private static final int FILTER_INCLUDING = 0;
  private static final int FILTER_EXCLUDING = 1;

  /**
   * properties where the settings are stored. They are initialized with the
   * properties loaded by the plugin instance
   */
  private Properties mProperties;

  public TVPearlSettings(Properties properties) {
    if (properties != null) {
      mProperties = properties;
    } else {
      mProperties = new Properties();
    }
  }

  private int getPropertyShowFilter() {
    return getPropertyInteger(KEY_SHOW_FILTER, FILTER_INCLUDING);
  }

  public boolean getUpdatePearlsManually() {
    return getPropertyBoolean(KEY_UPDATE_MANUAL, true);
  }

  public boolean getFilterEnabled() {
    return getPropertyBoolean(KEY_SHOW_ENABLE_FILTER, false);
  }

  public boolean getMarkPearls() {
    return getPropertyBoolean(KEY_MARK_PEARL, true);
  }

  public boolean getUpdatePearlsAfterStart() {
    return getPropertyBoolean(KEY_UPDATE_AT_START, false);
  }

  public boolean getUpdatePearlsAfterDataUpdate() {
    return getPropertyBoolean(KEY_UPDATE_AFTER_UPDATE_FINISHED, true);
  }

  public boolean getShowInfoModal() {
    return getPropertyBoolean(KEY_SHOW_INFO_MODAL, false);
  }

  public Integer getMarkPriority() {
    return getPropertyInteger(KEY_MARK_PRIORITY, Program.MIN_MARK_PRIORITY);
  }

  private int getPropertyViewOption() {
    return getPropertyInteger(KEY_VIEW_OPTION, SHOW_ALL_PEARLS);
  }

  private boolean getPropertyBoolean(String property, boolean defaultValue) {
    try {
      return (Integer.parseInt(mProperties.getProperty(property,
          defaultValue ? VALUE_TRUE : VALUE_FALSE)) == 1);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private int getPropertyInteger(String property, int defaultValue) {
    try {
      return Integer.parseInt(mProperties.getProperty(property, Integer
          .toString(defaultValue)));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private void setProperty(String property, boolean value)
  {
  	mProperties.setProperty(property, value ? VALUE_TRUE : VALUE_FALSE);
  }

  private void setProperty(String property, Integer value)
  {
  	mProperties.setProperty(property, value.toString());
  }

  public void setUpdatePearlsAfterStart(boolean update) {
    setProperty(KEY_UPDATE_AT_START, update);
  }

  public void setUpdatePearlsAfterDataUpdate(boolean update) {
    setProperty(KEY_UPDATE_AFTER_UPDATE_FINISHED, update);
  }

  public void setUpdatePearlsManually(boolean update) {
    setProperty(KEY_UPDATE_MANUAL, update);
  }

  private void setPropertyViewOption(int i) {
    setProperty(KEY_VIEW_OPTION, i);
  }

  public void setMarkPearls(boolean mark) {
    setProperty(KEY_MARK_PEARL, mark);
  }

  public void setMarkPriority(int priority) {
    setProperty(KEY_MARK_PRIORITY, priority);
  }

  public void setShowInfoModal(boolean modal) {
    setProperty(KEY_SHOW_INFO_MODAL, modal);
  }

  public void setFilterEnabled(boolean enabled) {
    setProperty(KEY_SHOW_ENABLE_FILTER, enabled);
  }

  private void setPropertyShowFilter(int i) {
    setProperty(KEY_SHOW_FILTER, i);
  }

  protected Properties storeSettings() {
    return mProperties;
  }

  public void storeDialog(String dialogKey, Point location, Dimension size) {
    if (location != null) {
      mProperties.setProperty(dialogKey + ".X", Integer.toString(location.x));
      mProperties.setProperty(dialogKey + ".Y", Integer.toString(location.y));
    }
    if (size != null) {
      mProperties.setProperty(dialogKey + ".Width", Integer
          .toString(size.width));
      mProperties.setProperty(dialogKey + ".Height", Integer
          .toString(size.height));
    }
  }

  public Dimension getDialogSize(String dialogKey) {
    try {
      return new Dimension(Integer.parseInt(mProperties.getProperty(dialogKey
          + ".Width")), Integer.parseInt(mProperties.getProperty(dialogKey
          + ".Height")));
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  public Point getDialogDimension(String dialogKey) {
    try {
      return new Point(Integer
          .parseInt(mProperties.getProperty(dialogKey + ".X")), Integer
          .parseInt(mProperties.getProperty(dialogKey + ".Y")));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public String getUrl() {
    return mProperties.getProperty("Url", DEFAULT_URL);
  }

  public void setUrl(String url) {
    mProperties.setProperty("Url", url);
  }

  public boolean getFilterIncluding() {
    return getPropertyShowFilter() == FILTER_INCLUDING;
  }

  public boolean getFilterExcluding() {
    return getPropertyShowFilter() == FILTER_EXCLUDING;
  }

  public void setShowAllPearls() {
    setPropertyViewOption(SHOW_ALL_PEARLS);
  }

  public void setShowSubscribedChannels() {
    setPropertyViewOption(SHOW_SUBSCRIBED_CHANNELS);
  }

  public void setShowFoundPearls() {
    setPropertyViewOption(SHOW_FOUND_PEARLS);
  }

  public boolean getShowAllPearls() {
    return getPropertyViewOption() == SHOW_ALL_PEARLS;
  }

  public boolean getShowSubscribedChannels() {
    return getPropertyViewOption() == SHOW_SUBSCRIBED_CHANNELS;
  }

  public boolean getShowFoundPearls() {
    return getPropertyViewOption() == SHOW_FOUND_PEARLS;
  }

  public void setFilterIncluding() {
    setPropertyShowFilter(FILTER_INCLUDING);
  }

  public void setFilterExcluding() {
    setPropertyShowFilter(FILTER_EXCLUDING);
  }
}
