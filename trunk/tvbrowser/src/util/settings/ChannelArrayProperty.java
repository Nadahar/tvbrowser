/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

import tvbrowser.core.ChannelList;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import devplugin.Channel;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ChannelArrayProperty extends Property {

  private Channel[] mDefaultValue;
  private Channel[] mCachedValue;
  
  
  
  public ChannelArrayProperty(PropertyManager manager, String key,
    Channel[] defaultValue)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }


  public Channel[] getDefault() {
    return mDefaultValue;
  }


  public Channel[] getChannelArray(boolean allowNullValues) {
    if (mCachedValue == null) {
      String asString = getProperty();
  
      if (asString != null) {
        String[] splits = asString.split(",");
        mCachedValue = new Channel[splits.length];
        
        for (int i = 0; i < splits.length; i++) {
          int pos = splits[i].indexOf(':');
          if (pos > 0) {
            String dataServiceClassName = splits[i].substring(0, pos);
            String id = splits[i].substring(pos + 1);
  
            TvDataServiceProxy dataService
              = TvDataServiceProxyManager.getInstance().findDataServiceById(dataServiceClassName);
              
            mCachedValue[i] = ChannelList.getChannel(dataService, id);
          }
          
          if ((! allowNullValues) && (mCachedValue[i] == null)) {
            // invalid entry -> cancel
            mCachedValue = null;
            break;
          }
        }
      }
  
      if (mCachedValue == null) {
        mCachedValue = mDefaultValue;
      }
    }

    return mCachedValue;
  }
  
  
  public void setChannelArray(Channel[] value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    boolean equalsDefault = false;
    if ((mDefaultValue != null) && (value.length == mDefaultValue.length)) {
      equalsDefault = true;
      for (int i = 0; i < value.length; i++) {
        if (value[i] == null || ! value[i].equals(mDefaultValue[i])) {
          equalsDefault = false;
          break;
        }
      }
    }
    
    if (equalsDefault) {
      setProperty(null);
    } else {
      StringBuffer buffer = new StringBuffer();
    
      for (int i = 0; i < value.length; i++) {
        if (value[i] == null)
          continue;
        if (i != 0) {
          buffer.append(',');
        }
        String dsClassName = value[i].getDataService().getClass().getName();
        buffer.append(dsClassName).append(':').append(value[i].getId());
      }
      
      setProperty(buffer.toString());
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mCachedValue = null;
  }

}
