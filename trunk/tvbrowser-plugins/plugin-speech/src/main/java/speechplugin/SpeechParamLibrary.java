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
package speechplugin;

import util.paramhandler.ParamLibrary;
import util.ui.Localizer;
import devplugin.Program;

public class SpeechParamLibrary extends ParamLibrary {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SpeechParamLibrary.class);

  private static final String KEY_TEXTTOSPEAK = "text_to_speak";
  private String mTextToSpeak;

  public SpeechParamLibrary(final String text) {
    super();
    mTextToSpeak = text;
  }

  @Override
  public String getDescriptionForKey(final String key) {
    if (key.equals(KEY_TEXTTOSPEAK)) {
      return mLocalizer.msg("textToSpeak", "Text to speak");
    }
    return super.getDescriptionForKey(key);
  }

  @Override
  public String[] getPossibleKeys() {
    final String[] keys = { KEY_TEXTTOSPEAK };
    return concat(super.getPossibleKeys(), keys);
  }

  @Override
  public String getStringForKey(final Program prg, final String key) {
    if (key.equals(KEY_TEXTTOSPEAK)) {
      return mTextToSpeak;
    }
    return super.getStringForKey(prg, key);
  }

  /**
   * concatenates two String-Arrays
   * 
   * @param ar1
   *          Array One
   * @param ar2
   *          Array Two
   * @return concatenated Version of the two Arrays
   */
  private String[] concat(final String[] ar1, final String[] ar2) {
    final String[] ar3 = new String[ar1.length + ar2.length];
    System.arraycopy(ar1, 0, ar3, 0, ar1.length);
    System.arraycopy(ar2, 0, ar3, ar1.length, ar2.length);
    return ar3;
  }
}
