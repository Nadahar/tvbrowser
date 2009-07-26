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
package util.ui;

import util.misc.StringPool;

/**
 * localizer using the String pool for all messages. You should use this instead
 * of the standard localizer if your object will be instantiated many times with
 * the same constant strings
 * 
 * @author bananeweizen
 * 
 */
public class PooledLocalizer extends Localizer {
  
  private PooledLocalizer(Class clazz) {
    super(clazz);
  }

  public static Localizer getLocalizerFor(Class clazz) {
    Localizer localizer = getCachedLocalizerFor(clazz);
    
    if (localizer == null) {
      localizer = new PooledLocalizer(clazz);
      addLocalizerToCache(clazz, localizer);
    }
    
    return localizer;
  }
  
  private String pool(String msg) {
    return StringPool.getString(msg);
  }

  @Override
  public String msg(String key, String defaultMsg) {
    return pool(super.msg(key, defaultMsg));
  }

  @Override
  public String msg(String key, String defaultMsg, boolean warn) {
    return pool(super.msg(key, defaultMsg, warn));
  }

  @Override
  public String msg(String key, String defaultMsg, Object arg1) {
    return pool(super.msg(key, defaultMsg, arg1));
  }
  
  @Override
  public String msg(String key, String defaultMsg, Object arg1, Object arg2) {
    return pool(super.msg(key, defaultMsg, arg1, arg2));
  }
  
  @Override
  public String msg(String key, String defaultMsg, Object arg1, Object arg2, Object arg3) {
    return pool(super.msg(key, defaultMsg, arg1, arg2, arg3));
  }
  
  @Override
  public String msg(String key, String defaultMsg, Object[] args) {
    return pool(super.msg(key, defaultMsg, args));
  }
}
