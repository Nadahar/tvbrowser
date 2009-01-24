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
package simplemarkerplugin;

import java.util.Properties;

public class SimpleMarkerSettings {

  private static final String KEY_SHOW_DELETED_PROGRAM = "showDeletedProgram";
  private static final String KEY_SPLIT_POSITION = "splitPosition";
  /**
   * show plugin nodes by title and date
   */
  private static final int GROUPING_BY_TITLE_AND_DATE = 0;
  /**
   * show plugin nodes by titles only
   */
  private static final int GROUPING_BY_TITLE = 1;
  /**
   * show plugin nodes by date only
   */
  private static final int GROUPING_BY_DATE = 2;
  private static final String KEY_GROUPING = "grouping";

  private Properties mProperties;

  public SimpleMarkerSettings(Properties prop) {
    if (prop != null) {
      mProperties = prop;
    } else {
      mProperties = new Properties();
    }
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public boolean showDeletedPrograms() {
    return mProperties.getProperty(KEY_SHOW_DELETED_PROGRAM, "true").equals("true");
  }

  public void setShowDeletedPrograms(boolean show) {
    mProperties.setProperty(KEY_SHOW_DELETED_PROGRAM, String.valueOf(show));
  }

  public int getSplitPosition() {
    return Integer.parseInt(mProperties.getProperty(KEY_SPLIT_POSITION, "-1"));
  }

  public void setSplitPosition(int dividerLocation) {
    mProperties.setProperty(KEY_SPLIT_POSITION, String.valueOf(dividerLocation));
  }

  public void setNodeGroupingByBoth() {
    setNodeGrouping(GROUPING_BY_TITLE_AND_DATE);
  }

  private void setNodeGrouping(int grouping) {
    mProperties.setProperty(KEY_GROUPING, Integer.toString(grouping));
  }

  public boolean isGroupingByBoth() {
    return isGrouping(GROUPING_BY_TITLE_AND_DATE);
  }

  private boolean isGrouping(int grouping) {
    return Integer.parseInt(mProperties.getProperty(KEY_GROUPING, "0")) == grouping;
  }

  public void setNodeGroupingByTitle() {
    setNodeGrouping(GROUPING_BY_TITLE);
  }

  public boolean isGroupingByTitle() {
    return isGrouping(GROUPING_BY_TITLE);
  }

  public void setNodeGroupingByDate() {
    setNodeGrouping(GROUPING_BY_DATE);
  }

  public boolean isGroupingByDate() {
    return isGrouping(GROUPING_BY_DATE);
  }

}
