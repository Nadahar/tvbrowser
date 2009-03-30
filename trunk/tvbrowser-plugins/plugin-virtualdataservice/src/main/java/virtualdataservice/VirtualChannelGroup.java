/*
 * VirtualDataService by Reinhard Lehrbaum
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
 */
package virtualdataservice;

public class VirtualChannelGroup implements devplugin.ChannelGroup
{

	private String mName;
	private String mId;
	private String mDescription;
	private String mProviderName;

	public VirtualChannelGroup(final String name, final String id,
      final String description, final String providername)
	{
		mName = name;
		mId = id;
		mDescription = description;
		mProviderName = providername;
	}

	public String getName()
	{
		return mName;
	}

	public String getId()
	{
		return mId;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public String getProviderName()
	{
		return mProviderName;
	}

	public boolean equals(final Object obj)
	{
		if (obj instanceof devplugin.ChannelGroup)
		{
		  final devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
			return group.getId().equalsIgnoreCase(mId);
		}
		return false;
	}
}
