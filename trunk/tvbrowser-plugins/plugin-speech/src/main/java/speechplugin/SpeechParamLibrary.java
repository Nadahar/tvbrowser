/*
 * SpeechPlugin for TV-Browser
 * Copyright Michael Keppler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * VCS information:
 *     $Date: 2008-04-05 21:36:41 +0200 (Sa, 05 Apr 2008) $
 *   $Author: Bananeweizen $
 * $Revision: 4469 $
 */
package speechplugin;

import devplugin.Program;
import util.paramhandler.ParamLibrary;
import util.ui.Localizer;

public class SpeechParamLibrary extends ParamLibrary {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SpeechParamLibrary.class);

  private static final String KEY_TEXTTOSPEAK = "text_to_speak";
  private String mTextToSpeak;

  public SpeechParamLibrary(String text) {
    super();
    mTextToSpeak = text;
  }

  @Override
  public String getDescriptionForKey(String key) {
    if (key.equals(KEY_TEXTTOSPEAK)) {
      return mLocalizer.msg("textToSpeak", "Text to speak");
    }
    return super.getDescriptionForKey(key);
  }

  @Override
  public String[] getPossibleKeys() {
    String[] keys = { KEY_TEXTTOSPEAK };
    return concat(super.getPossibleKeys(), keys);
  }

  @Override
  public String getStringForKey(Program prg, String key) {
    if (key.equals(KEY_TEXTTOSPEAK)) {
      return mTextToSpeak;
    }
    return super.getStringForKey(prg, key);
  }

  /**
   * concatenates two String-Arrays
   * @param ar1 Array One 
   * @param ar2 Array Two
   * @return concatenated Version of the two Arrays
   */
  private String[] concat(String[] ar1, String[] ar2) {
    String[] ar3 = new String[ar1.length+ar2.length];
    System.arraycopy(ar1, 0, ar3, 0, ar1.length);
    System.arraycopy(ar2, 0, ar3, ar1.length, ar2.length);
    return ar3;
  }
}
