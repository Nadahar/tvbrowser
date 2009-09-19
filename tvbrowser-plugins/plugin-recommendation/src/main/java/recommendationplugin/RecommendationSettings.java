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
package recommendationplugin;

import java.util.Properties;

public class RecommendationSettings extends PropertyBasedSettings {

  public RecommendationSettings(Properties properties) {
    super(properties);
  }

  void setWeighting(final RecommendationWeighting weighting) {
    set(weighting.getId(), weighting.getWeighting());
  }

  int getWeighting(final String id) {
    int defaultValue = 0;
    String lowerId = id.toLowerCase();
    if (lowerId.contains("favorite")) {
      defaultValue = 70;
    }
    else if (lowerId.contains("dontwant") || lowerId.contains("nicht sehen")) {
      defaultValue = 100;
    }
    return get(id, defaultValue);
  }
}
