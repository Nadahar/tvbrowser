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
package tvraterplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 * 
 */
public final class TVRaterSettings extends PropertyBasedSettings {

  private static final int UPDATE_MANUALLY = 3;
  private static final int UPDATE_ON_START = 2;
  private static final int UPDATE_ON_RATING = 1;
  private static final int UPDATE_ON_DATAUPDATE = 0;
  private static final String KEY_NAME = "name";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_PREFER_OWN_RATING = "ownRating";
  private static final String KEY_UPDATE_INTERVALL = "updateIntervall";

  public TVRaterSettings(final Properties properties) {
    super(properties);
  }

  public String getName() {
    return get(KEY_NAME, "");
  }

  public String getPassword() {
    return get(KEY_PASSWORD, "");
  }

  public boolean getPreferOwnRating() {
    return get(KEY_PREFER_OWN_RATING, true);
  }

  public void setName(final String name) {
    set(KEY_NAME, name);
  }

  public void setPassword(final String password) {
    set(KEY_PASSWORD, password);
  }

  public void setPreferOwnRating(final boolean prefer) {
    set(KEY_PREFER_OWN_RATING, prefer);
  }

  public UpdateInterval getUpdateInterval() {
    int interval = get(KEY_UPDATE_INTERVALL, UPDATE_ON_DATAUPDATE);
    switch (interval) {
    case UPDATE_ON_DATAUPDATE: return UpdateInterval.OnDataUpdate;
    case UPDATE_ON_RATING: return UpdateInterval.OnRating;
    case UPDATE_ON_START: return UpdateInterval.OnStart;
    case UPDATE_MANUALLY: return UpdateInterval.Manually;
    }
    return UpdateInterval.OnDataUpdate;
  }

  public void setUpdateInterval(final UpdateInterval interval) {
    
    switch (interval) {
    case OnDataUpdate: {
      set(KEY_UPDATE_INTERVALL, UPDATE_ON_DATAUPDATE);
      break;
    }
    case OnRating: {
      set(KEY_UPDATE_INTERVALL, UPDATE_ON_RATING);
      break;
    }
    case OnStart: {
      set(KEY_UPDATE_INTERVALL, UPDATE_ON_START);
      break;
    }
    case Manually: {
      set(KEY_UPDATE_INTERVALL, UPDATE_MANUALLY);
      break;
    }
    default : {
      set(KEY_UPDATE_INTERVALL, UPDATE_ON_DATAUPDATE);
    }
    }
  }




}
