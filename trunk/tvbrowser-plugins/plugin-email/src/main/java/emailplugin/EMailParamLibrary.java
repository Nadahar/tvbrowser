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
package emailplugin;

import java.util.ArrayList;
import java.util.Arrays;

import util.paramhandler.ParamLibrary;
import util.ui.Localizer;
import devplugin.Program;

public class EMailParamLibrary extends ParamLibrary {
  static final String KEY_MAIL_TEXT = "content";
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailParamLibrary.class);
  private String mContent;
public EMailParamLibrary(final String mailContent) {
  mContent = mailContent;
}
  @Override
  public String[] getPossibleKeys() {
    ArrayList<String> keys = new ArrayList<String>();
    keys.addAll(Arrays.asList(super.getPossibleKeys()));
    keys.add(KEY_MAIL_TEXT);
    return keys.toArray(new String[keys.size()]);
  }
  
  @Override
  public String getDescriptionForKey(String key) {
    if (key.equalsIgnoreCase(KEY_MAIL_TEXT)) {
      return mLocalizer.msg("contentParam", "Mail content");
    }
    return super.getDescriptionForKey(key);
  }
  
  @Override
  public String getStringForKey(final Program program, String key) {
    if (key.equalsIgnoreCase(KEY_MAIL_TEXT)) {
      return mContent;
    }
    return super.getStringForKey(program, key);
  }
}
