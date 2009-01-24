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
package timelineplugin;

import java.awt.Rectangle;
import java.util.Properties;

public class TimelineSettings {
  private static final String VALUE_FALSE = "0";
  private static final String VALUE_TRUE = "1";
  private static final String KEY_CHANNEL_WIDTH = "ChannelWidth";
  private static final String KEY_FOCUS_DELTA = "FocusDelta";
  private static final String KEY_PROGRESS_VIEW = "ProgressView";
  private static final String KEY_SHOW_AT_STARTUP = "ShowAtStartup";
  private static final String KEY_START_WITH_NOW = "StartWithNow";
  private static final String KEY_RESIZE_WITH_MOUSE = "ResizeWithMouse";
  private static final String KEY_CHANNEL_HEIGHT = "ChannelHeight";
  private static final String KEY_SHOW_CHANNEL_ICON = "ShowChannelIcon";
  private static final String KEY_SHOW_CHANNEL_NAME = "ShowChannelName";
  private static final String KEY_HOUR_WIDTH = "HourWidth";
  private static final String DEFAULT_TITLE_FORMAT = "{title}";
  private Properties mProperties;
  private String mTitleFormat = DEFAULT_TITLE_FORMAT;

  public TimelineSettings(Properties prop) {
    if (prop != null) {
      mProperties = prop;
    } else {
      mProperties = new Properties();
    }
  }

  int getFocusDelta() {
    return getProperty(KEY_FOCUS_DELTA, 50);
  }

  int getHourWidth() {
    return getProperty(KEY_HOUR_WIDTH, 120);
  }
  
  void setHourWidth(int width) {
    mProperties.setProperty(KEY_HOUR_WIDTH, Integer.toString(width));
  }

  int getChannelHeight() {
    return getProperty(KEY_CHANNEL_HEIGHT, 20);
  }

  private int getProgressView() {
    return getProperty(KEY_PROGRESS_VIEW, 1);
  }

  boolean showAtStartUp() {
    return getProperty(KEY_SHOW_AT_STARTUP, false);
  }

  boolean startWithNow() {
    return getProperty(KEY_START_WITH_NOW, false);
  }

  boolean resizeWithMouse() {
    return getProperty(KEY_RESIZE_WITH_MOUSE, false);
  }

  boolean showChannelName() {
    return getProperty(KEY_SHOW_CHANNEL_NAME, true);
  }

  boolean showChannelIcon() {
    return getProperty(KEY_SHOW_CHANNEL_ICON, true);
  }
  
  private boolean getProperty(String key, boolean defaultValue) {
    return getProperty(key, defaultValue ? 1 : 0) == 1;
  }

  public Properties storeSettings() {
    mProperties.setProperty(KEY_HOUR_WIDTH, String.valueOf(getHourWidth()));
    mProperties
        .setProperty(KEY_CHANNEL_HEIGHT, String
        .valueOf(getChannelHeight()));
    mProperties.setProperty(KEY_CHANNEL_WIDTH, String.valueOf(getChannelWidth()));
    mProperties.setProperty(KEY_PROGRESS_VIEW, String
        .valueOf(getProgressView()));
    return mProperties;
  }

  boolean showBar() {
    return (getProgressView() & 2) == 2;
  }

  boolean showProgress() {
    return (getProgressView() & 1) == 1;
  }

  private void setProperty(String property, String value) {
    mProperties.setProperty(property, value);
  }

  private void setProperty(String property, boolean value) {
    setProperty(property, value ? VALUE_TRUE : VALUE_FALSE);
  }

  public void setShowChannelName(boolean show) {
    setProperty(KEY_SHOW_CHANNEL_NAME, show);
  }

  public void setShowChannelIcon(boolean show) {
    setProperty(KEY_SHOW_CHANNEL_ICON, show);
  }

  public void setChannelHeight(int height) {
    setProperty(KEY_CHANNEL_HEIGHT, height);
  }

  private void setProperty(String key, int value) {
    setProperty(key, String.valueOf(value));
  }

  public void setResizeWithMouse(boolean resize) {
    setProperty(KEY_RESIZE_WITH_MOUSE, resize);
  }

  public void setStartWithNow(boolean start) {
    setProperty(KEY_START_WITH_NOW, start);
  }

  public void setShowAtStartup(boolean show) {
    setProperty(KEY_SHOW_AT_STARTUP, show);
  }
  public void setFocusDelta(int delta) {
    setProperty(KEY_FOCUS_DELTA, String.valueOf(delta));
  }

  public void setProgressView(int i) {
    setProperty(KEY_PROGRESS_VIEW, i);
  }

  public int getChannelWidth() {
    return getProperty(KEY_CHANNEL_WIDTH, -1);
  }

  private int getProperty(String key, int defaultValue) {
    try {
      return Integer.parseInt(mProperties.getProperty(key, Integer
          .toString(defaultValue)));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return defaultValue;
    }
  }

  public void setTitleFormat(String text) {
    mTitleFormat = text;
  }

  public String getTitleFormat() {
    return mTitleFormat;
  }

  public String getDefaultTitleFormat() {
    return DEFAULT_TITLE_FORMAT;
  }

  public void savePosition(int x, int y, int width, int height) {
    setProperty("xpos", x);
    setProperty("ypos", y);
    setProperty("width", width);
    setProperty("height", height);
  }

  public Rectangle getPosition() {
    int x = getProperty("xpos", 0);
    int y = getProperty("ypos", 0);
    int width = getProperty("width", 620);
    int height = getProperty("height", 390);
    return new Rectangle(x, y, width, height);
  }

  public int getOffset() {
    return getProperty("width", 620);
  }


}
