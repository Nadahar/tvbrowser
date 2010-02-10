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

import java.awt.Font;

import util.settings.FontProperty;
import util.settings.PropertyManager;

/**
 * font property which only starts calculation of the preferred default font if
 * the settings value is not available to improve performance
 * 
 * @author Bananeweizen
 * 
 */
public class DeferredFontProperty extends FontProperty {

  /**
   * if any of these fonts is found on the system then it is used as default
   * font
   */
  private static final String[] KNOWN_GOOD_FONTS = { "Tahoma", "Trebuchet MS",
      "Arial Narrow" };

  private Font mDefFont;
  private Font mCachedValue;

  public DeferredFontProperty(final PropertyManager manager, final String key,
      final Font defaultFont) {
    super(manager, key, defaultFont);
    mDefFont = defaultFont;
  }

  public Font getDefault() {
    if (mCachedValue == null) {
      // do not use the getAllFontFamilyNames() method as that leads to scanning
      // the system for all font files
      for (String knownFont : KNOWN_GOOD_FONTS) {
        final Font font = new Font(knownFont, mDefFont.getStyle(), mDefFont
            .getSize());
        // comparing the font name and Font.DIALOG is not really correct.
        // font family name would be more precise, but takes too much time
        if (font != null && !font.getName().startsWith(Font.DIALOG)) {
          mCachedValue = font;
          break;
        }
      }
    }
    // we found no font, so set the default
    if (mCachedValue == null) {
      mCachedValue = mDefFont;
    }
    return mCachedValue;
  }
}
