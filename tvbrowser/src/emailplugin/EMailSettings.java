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

import java.util.Properties;

import util.settings.PropertyBasedSettings;

import com.l2fprod.util.StringUtils;

/**
 * @author bananeweizen
 *
 */
public final class EMailSettings extends PropertyBasedSettings {

  private static final String KEY_SHOW_EMAIL_OPENED = "showEmailOpened";
  private static final String KEY_PARAMETER = "parameter";
  private static final String KEY_APPLICATION = "application";
  private static final String KEY_USE_DEFAULTAPP = "defaultapp";

  public EMailSettings(final Properties properties) {
    super(properties);
  }

  public boolean getUseDefaultApplication() {
    return get(KEY_USE_DEFAULTAPP, true);
  }

  public String getApplication() {
    return get(KEY_APPLICATION,"");
  }

  public String getParameter() {
    String value = get(KEY_PARAMETER, "{" + EMailParamLibrary.KEY_MAIL_TEXT + "}");
    // replace old parameter system with new param library value
    return StringUtils.replace(value, "{0}", "{" + EMailParamLibrary.KEY_MAIL_TEXT + "}");
  }

  public void setApplication(final String application) {
    set(KEY_APPLICATION, application);  }

  public void setParameter(final String parameter) {
    set(KEY_PARAMETER, parameter);
  }

  public void setDefaultApplication(boolean useDefault) {
    set(KEY_USE_DEFAULTAPP, useDefault);
  }

  public boolean getShowEmailOpened() {
    return get(KEY_SHOW_EMAIL_OPENED, true);
  }

  public void setShowEmailOpened(boolean show) {
    set(KEY_SHOW_EMAIL_OPENED, show);
  }

}
