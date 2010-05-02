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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package devplugin;


public class ChannelGroupImpl implements ChannelGroup {

  private String mId, mName;
  protected String mDescription;
  protected String mProvider;

	public ChannelGroupImpl(String id, String name, String description, String provider) {
    mId = id;
    mName = name;
    mDescription = description;
    mProvider = provider;
	}

  public ChannelGroupImpl(String id, String name, String description) {
    this(id, name, description, null);
  }

	public String getName() {
		return mName;
	}

	public String getId() {
		return mId;
	}

	public String getDescription() {
		return mDescription;
	}

  public String getProviderName() {
    return mProvider;
  }

  public boolean equals(Object obj) {
    if (obj instanceof ChannelGroup) {
      ChannelGroup group = (ChannelGroup) obj;
      return group.getId().equalsIgnoreCase(getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getId().toLowerCase().hashCode();
  }

}