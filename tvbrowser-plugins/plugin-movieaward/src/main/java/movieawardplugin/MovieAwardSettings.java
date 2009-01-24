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
package movieawardplugin;

import java.util.Properties;

public class MovieAwardSettings {
  /**
   * show plugin nodes by award and date
   */
  private static final int GROUPING_BY_AWARD_AND_DATE = 0;
  /**
   * show plugin nodes by awards only
   */
  private static final int GROUPING_BY_AWARD = 1;
  /**
   * show plugin nodes by date only
   */
  private static final int GROUPING_BY_DATE = 2;
  private static final String KEY_GROUPING = "grouping";
  private Properties mProperties;
  
  public MovieAwardSettings(Properties properties) {
    if (properties != null) {
      mProperties = properties;
    } else {
      mProperties = new Properties();
    }
  }

  public void setNodeGroupingBoth() {
    setNodeGrouping(GROUPING_BY_AWARD_AND_DATE);
  }

  private void setNodeGrouping(int grouping) {
    mProperties.setProperty(KEY_GROUPING, Integer.toString(grouping));
  }

  public boolean isGroupingByBoth() {
    return isGrouping(GROUPING_BY_AWARD_AND_DATE);
  }

  private boolean isGrouping(int grouping) {
    return Integer.parseInt(mProperties.getProperty(KEY_GROUPING, "0")) == grouping;
  }

  public void setNodeGroupingAward() {
    setNodeGrouping(GROUPING_BY_AWARD);
  }

  public boolean isGroupingByAward() {
    return isGrouping(GROUPING_BY_AWARD);
  }

  public void setNodeGroupingDate() {
    setNodeGrouping(GROUPING_BY_DATE);
  }

  public boolean isGroupingByDate() {
    return isGrouping(GROUPING_BY_DATE);
  }

  public Properties storeSettings() {
    return mProperties;
  }
}
