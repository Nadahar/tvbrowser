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
package tvbrowser.extras.programinfo;

import java.awt.Font;
import java.io.IOException;
import java.util.Properties;

import tvbrowser.extras.common.ConfigurationHandler;
import util.program.CompoundedProgramFieldType;
import util.program.ProgramTextCreator;
import util.settings.PluginPictureSettings;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.ProgramFieldType;

class ProgramInfoSettings {

  private static final String KEY_FIELD_ORDER = "order";
  private static final String KEY_LOOK = "look";
  private static final int DEFAULT_BODY_FONT_SIZE = 11;
  private static final int DEFAULT_TITLE_FONT_SIZE = 18;
  private static final String DEFAULT_BODY_FONT_NAME = "Verdana";
  private static final String DEFAULT_TITLE_FONT_NAME = "Verdana";
  private static final String KEY_LEFT_SPLIT_WIDTH = "LeftSplit.Width";
  private static final String KEY_LEFT_SPLIT_HEIGHT = "LeftSplit.Height";
  private static final String KEY_SETUPWASDONE = "setupwasdone";
  private static final String KEY_SHOW_SEARCH = "showSearch";
  private static final String KEY_PICTURE_SETTINGS = "pictureSettings";
  private static final String KEY_ENABLE_SEARCH = "enableSearch";
  private static final String KEY_ZOOM_VALUE = "zoomValue";
  private static final String KEY_ZOOM_ENABLED = "zoom";
  private static final String KEY_USERFONT_ENABLED = "userfont";
  private static final String KEY_SHOW_TEXT_SEARCH_BUTTON = "showTextSearchButton";
  private static final String KEY_SHOW_FUNCTIONS = "showFunctions";
  private static final String KEY_BODYFONT_SIZE = "small";
  private static final String KEY_TITLEFONT_SIZE = "title";
  private static final String KEY_ANTIALIASING = "antialiasing";
  private static final String KEY_TITLEFONT_NAME = "titlefont";
  private static final String KEY_BODYFONT_NAME = "bodyfont";
  private static final String KEY_ACTOR_SEARCH_DEFAULT = "actorSearchDefault";
  private Properties mProperties;

  protected ProgramInfoSettings(final Properties properties) {
    mProperties = properties;
  }

  protected boolean getShowFunctions() {
    return getProperty(KEY_SHOW_FUNCTIONS, true);
  }

  protected boolean getShowSearchButton() {
    return getProperty(KEY_SHOW_TEXT_SEARCH_BUTTON, true);
  }

  protected boolean getAntialiasing() {
    return getProperty(KEY_ANTIALIASING, true);
  }

  private String getProperty(final String key, final String defaultValue) {
    return mProperties.getProperty(key, defaultValue);
  }

  protected boolean getUserFont() {
    return getProperty(KEY_USERFONT_ENABLED, false);
  }

  protected int getTitleFontSize() {
    return getProperty(KEY_TITLEFONT_SIZE, DEFAULT_TITLE_FONT_SIZE);
  }

  protected int getBodyFontSize() {
    return getProperty(KEY_BODYFONT_SIZE, DEFAULT_BODY_FONT_SIZE);
  }

  protected String getTitleFontName() {
    return getProperty(KEY_TITLEFONT_NAME, DEFAULT_TITLE_FONT_NAME);
  }

  protected String getBodyFontName() {
    return getProperty(KEY_BODYFONT_NAME, DEFAULT_BODY_FONT_NAME);
  }

  protected void setAntialiasing(final boolean value) {
    setProperty(KEY_ANTIALIASING, Boolean.toString(value));
  }

  private void setProperty(final String key, final String value) {
    mProperties.setProperty(key, value);
  }

  protected void setTitleFontName(final String name) {
    setProperty(KEY_TITLEFONT_NAME, name);
  }

  protected void setTitleFontSize(final int size) {
    setProperty(KEY_TITLEFONT_SIZE, Integer.toString(size));
  }

  protected void setBodyFontName(final String name) {
    setProperty(KEY_BODYFONT_NAME, name);
  }

  protected void setUserFont(final boolean selected) {
    setProperty(KEY_USERFONT_ENABLED, String.valueOf(selected));
  }

  protected void setBodyFontSize(final int size) {
    setProperty(KEY_BODYFONT_SIZE, String.valueOf(size));
  }

  protected String getActorSearch() {
    return getProperty(KEY_ACTOR_SEARCH_DEFAULT, "internalWikipedia");
  }

  protected void setActorSearch(final String value) {
    setProperty(KEY_ACTOR_SEARCH_DEFAULT, value);
  }

  protected void setZoomEnabled(final boolean enabled) {
    setProperty(KEY_ZOOM_ENABLED, String.valueOf(enabled));
  }

  protected void setZoomValue(final int value) {
    setProperty(KEY_ZOOM_VALUE, String.valueOf(value));
  }

  protected void setShowFunctions(final boolean show) {
    setProperty(KEY_SHOW_FUNCTIONS, String.valueOf(show));
  }

  protected void setShowSearchButton(final boolean show) {
    setProperty(KEY_SHOW_TEXT_SEARCH_BUTTON, String.valueOf(show));
  }

  protected boolean getZoomEnabled() {
    return getProperty(KEY_ZOOM_ENABLED, false);
  }

  protected int getZoomValue() {
    return getProperty(KEY_ZOOM_VALUE, 100);
  }

  protected boolean getEnableSearch() {
    return getProperty(KEY_ENABLE_SEARCH, true);
  }

  protected void setEnableSearch(final boolean enable) {
    setProperty(KEY_ENABLE_SEARCH, String.valueOf(enable));
  }

  protected void setPictureSettings(final int type) {
    setProperty(KEY_PICTURE_SETTINGS, String.valueOf(type));
  }

  protected int getPictureSettings() {
    return Integer.parseInt(getProperty(KEY_PICTURE_SETTINGS, String
        .valueOf(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE)));
  }

  protected void setWidth(final int width) {
    setProperty(KEY_LEFT_SPLIT_WIDTH, Integer.toString(width));
  }

  protected void setHeight(final int height) {
    setProperty(KEY_LEFT_SPLIT_HEIGHT, Integer.toString(height));
  }

  protected int getWidth() {
    return getProperty(KEY_LEFT_SPLIT_WIDTH, 0);
  }

  private int getProperty(final String key, final int defaultValue) {
    return Integer.parseInt(getProperty(key, String
        .valueOf(defaultValue)));
  }

  protected int getHeight() {
    return getProperty(KEY_LEFT_SPLIT_HEIGHT, 0);
  }

  protected boolean getExpanded(final String key) {
    return getProperty(key, true);
  }

  private boolean getProperty(final String key, final boolean defaultValue) {
    return Boolean.valueOf(getProperty(key, String.valueOf(defaultValue)));
  }

  protected void setExpanded(final String key, final boolean value) {
    setProperty(key, String.valueOf(value));
  }

  protected void setSetupwasdone(final boolean b) {
    setProperty(KEY_SETUPWASDONE, Boolean.toString(b));
  }

  protected boolean getSetupwasdone() {
    return getProperty(KEY_SETUPWASDONE, false);
  }
  
  protected void setShowSearch(final boolean show) {
    setProperty(KEY_SHOW_SEARCH, String.valueOf(show));
  }

  protected boolean getShowSearch() {
    return getProperty(KEY_SHOW_SEARCH, false);
  }

  protected void storeSettings(final ConfigurationHandler configurationHandler)
      throws IOException {
    configurationHandler.storeSettings(mProperties);
  }

  protected Font getUsedTitleFont() {
    return new Font(getUserFont() ? getTitleFontName()
        : DEFAULT_TITLE_FONT_NAME, Font.BOLD,
        getUserFont() ? getTitleFontSize() : DEFAULT_TITLE_FONT_SIZE);
  }

  protected Font getUsedBodyFont() {
    return new Font(getUserFont() ? getBodyFontName() : DEFAULT_BODY_FONT_NAME,
        Font.PLAIN, getUserFont() ? getBodyFontSize() : DEFAULT_BODY_FONT_SIZE);
  }

  protected String getLook() {
    return getProperty(KEY_LOOK, LookAndFeelAddons.getBestMatchAddonClassName());
  }

  protected void setLook(final String name) {
    setProperty(KEY_LOOK, name);
  }
  
  protected Object[] getFieldOrder() {
    if (!getSetupwasdone()) {
      return ProgramTextCreator.getDefaultOrder();
    }
    final String[] id = getProperty(KEY_FIELD_ORDER, "").trim().split(";");
    final Object[] result = new Object[id.length];
    for (int i = 0; i < id.length; i++) {
      final int parsedId = Integer.parseInt(id[i]);
      if (parsedId == ProgramFieldType.UNKOWN_FORMAT) {
        result[i] = ProgramTextCreator.getDurationTypeString();
      } else if (parsedId >= 0) {
        result[i] = ProgramFieldType.getTypeForId(parsedId);
      } else {
        result[i] = CompoundedProgramFieldType
            .getCompoundedProgramFieldTypeForId(parsedId);
      }
    }
    return result;
  }

  protected void setFieldOrder(final Object[] order) {
    final StringBuilder temp = new StringBuilder();

    for (Object object : order) {
      if (object instanceof String) {
        temp.append(ProgramFieldType.UNKOWN_FORMAT).append(';');
      } else if (object instanceof CompoundedProgramFieldType) {
        temp.append(((CompoundedProgramFieldType) object).getId()).append(';');
      } else {
        temp.append(((ProgramFieldType) object).getTypeId()).append(';');
      }
    }
    setProperty(KEY_FIELD_ORDER, temp.toString());
  }
}
