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
package tvbrowser.core.settings;

import java.awt.GraphicsEnvironment;

import util.settings.PropertyManager;
import util.settings.StringProperty;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * theme property with lazy default calculation for better performance
 * 
 * @author Bananeweizen
 * 
 */
public class JGoodiesThemeProperty extends StringProperty {
  
  private String mCachedValue;

  public JGoodiesThemeProperty(final PropertyManager manager, final String key) {
    super(manager, key, "");
  }

  @Override
  public String getDefault() {
    if (mCachedValue == null) {
      if (!GraphicsEnvironment.isHeadless()) {
        mCachedValue = PlasticLookAndFeel.createMyDefaultTheme().getClass()
            .getName();
      }
    }
    if (mCachedValue == null) {
      mCachedValue = "";
    }
    return mCachedValue;
  }
}
