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
package virtualdataservice.virtual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VirtualChannelManager
{
	private static final String CHANNELS = "channels";

	private String mWorkingDirectory;
	private List<VirtualChannel> mChannels;
	private int mMaxID;

	public VirtualChannelManager(final String workingDirectory)
	{
		mWorkingDirectory = workingDirectory;
		if (!mWorkingDirectory.endsWith(File.separator))
		{
			mWorkingDirectory += File.separator;
		}
		loadChannels();
	}

	public List<VirtualChannel> getChannels()
	{
		return mChannels;
	}

	private void loadChannels()
	{
		mChannels = new ArrayList<VirtualChannel>();
		final File file = new File(mWorkingDirectory + CHANNELS);
		if (file.exists())
		{
			try
			{
			  final ObjectInputStream in = new ObjectInputStream(new FileInputStream(
            file));

			  final int version = in.readInt();

				if (version == 1)
				{
					mMaxID = in.readInt();
					final int count = in.readInt();
					for (int i = 0; i < count; i++)
					{
					  final VirtualChannel channel = (VirtualChannel) in.readObject();
						channel.setWorkingDirectory(mWorkingDirectory);
						mChannels.add(channel);
					}
				}
				in.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void save()
	{
		try
		{
		  final ObjectOutputStream out = new ObjectOutputStream(
          new FileOutputStream(mWorkingDirectory + CHANNELS));

			out.writeInt(1); //version

			out.writeInt(mMaxID);
			out.writeInt(mChannels.size());

			for (VirtualChannel channel : mChannels)
			{
				out.writeObject(channel);
			}
			out.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		for (VirtualChannel channel : mChannels)
		{
			channel.save();
		}
		//TODO: remove old files from deleted channels
	}

	public VirtualChannel addChannel(final String name)
	{
		VirtualChannel channel = getChannel(name);
		if (channel == null)
		{
			channel = new VirtualChannel(mMaxID, name);
			mMaxID++;
			channel.setWorkingDirectory(mWorkingDirectory);
			mChannels.add(channel);
		}
		return channel;
	}

	public VirtualChannel getChannel(final String name)
	{
		for (VirtualChannel channel : mChannels)
		{
			if (channel.getName().equalsIgnoreCase(name))
			{
				return channel;
			}
		}
		return null;
	}

	public VirtualChannel getChannel(final int id)
	{
		for (VirtualChannel channel : mChannels)
		{
			if (channel.getID() == id)
			{
				return channel;
			}
		}
		return null;
	}

	public void removeChannel(final VirtualChannel channel)
	{
		mChannels.remove(channel);
	}
}
