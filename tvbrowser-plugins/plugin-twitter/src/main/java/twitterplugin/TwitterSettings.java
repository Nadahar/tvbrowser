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
package twitterplugin;

import java.util.Properties;

import twitter4j.http.AccessToken;

final public class TwitterSettings extends PropertyBasedSettings {

  private static final String STORE_PASSWORD = "STOREPASSWORD";
  private static final String USERNAME = "USERNAME";
  private static final String PASSWORD = "PASSWORD";
  private static final String FORMAT = "paramForProgram";
  private static final String DEFAULT_FORMAT = "{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}. {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {channel_name} - {title}";
  private static final String USE_OAUTH = "useOAuth";
  private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
  private static final String ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";

  public TwitterSettings(final Properties properties) {
    super(properties);
  }

  public void setFormat(final String format) {
    set(TwitterSettings.FORMAT, format);
  }

  public String getFormat() {
    return get(TwitterSettings.FORMAT, TwitterSettings.DEFAULT_FORMAT);
  }

  public String getUsername() {
    return get(TwitterSettings.USERNAME, "");
  }

  public boolean getStorePassword() {
    return get(TwitterSettings.STORE_PASSWORD, false);
  }

  public void setUsername(final String username) {
    set(TwitterSettings.USERNAME, username);
  }

  public String getPassword() {
    return get(TwitterSettings.PASSWORD, "");
  }

  public void setPassword(final String password) {
    set(TwitterSettings.PASSWORD, password);
  }

  public void setStorePassword(final boolean store) {
    set(TwitterSettings.STORE_PASSWORD, store);
  }

  public boolean getUseOAuth() {
    return get(USE_OAUTH, true);
  }

  public String getConsumerKey() {
    return "EnDHGxEpgokHy46VCnWEA";
  }

  public String getConsumerSecret() {
    return "C7thwG7lJNg2MUJLQLF4LCfZIc5HjxFPjrzjEdPvLFU";
  }

  public void setAccessToken(final AccessToken token) {
    set(ACCESS_TOKEN, token.getToken());
    set(ACCESS_TOKEN_SECRET, token.getTokenSecret());
  }

  public AccessToken getAccessToken() {
    String token = get(ACCESS_TOKEN);
    String secret = get(ACCESS_TOKEN_SECRET);
    if (token == null || secret == null || token.length() == 0 || secret.length() == 0) {
      return null;
    }
    return new AccessToken(token, secret);
  }

  public void setUseOAuth(final boolean use) {
    set(USE_OAUTH, use);
  }

  public void clearAuthentication() {
    setStorePassword(false);
    setPassword("");
    set(ACCESS_TOKEN, "");
    set(ACCESS_TOKEN_SECRET, "");
  }

}
