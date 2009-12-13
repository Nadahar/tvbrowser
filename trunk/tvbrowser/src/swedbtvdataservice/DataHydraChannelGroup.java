/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
 * 
 */

package swedbtvdataservice;

import util.misc.StringPool;

/**
 * Each channel should belong to exactly one channel group. The ChannelGroup interface
 * represents a channel goup.
 */
class DataHydraChannelGroup implements devplugin.ChannelGroup {

  private String mId;
  private String mProvider;
  private String mCopyright;
  private String mUrl;
  private String mChannelFile;
  private String mCountry;
  private boolean mShowRegister;

  protected DataHydraChannelGroup(String id, String provider, String copyright, String url, String channelFile, String country) {
    this(id, provider, copyright, url, channelFile, country, true);
  }

  protected DataHydraChannelGroup(String id, String provider, String copyright, String url, String channelFile, String country, boolean showRegister) {
    mId = id;
    mProvider = provider;
    mCopyright = StringPool.getString(copyright);
    mUrl = url;
    mChannelFile = channelFile;
    mCountry = StringPool.getString(country);
    mShowRegister = showRegister;
  }

  /**
   * @return the title of this group
   */
  public String getName() {
    return "DataHydra";
  }

  /**
   * @return unique identifier of this group
   */
  public String getId() {
    return mId;
  }

  /**
   * @return short description of this group
   */
  public String getDescription() {
    return "DataHydra";
  }

  /**
   * @return the name of the provider
   * @since 1.1
   */
  public String getProviderName() {
    return mProvider;
  }

  public boolean equals(Object obj) {

    if (obj instanceof devplugin.ChannelGroup) {
      devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
      return group.getId().equalsIgnoreCase(mId);
    }
    return false;

  }

  public String getCopyright() {
    return mCopyright;
  }

  public String getUrl() {
    return mUrl;
  }

  public String getChannelFile() {
    return mChannelFile;
  }

  public String getCountry() {
    return mCountry;
  }

  public boolean isShowRegister() {
    return mShowRegister;
  }
}