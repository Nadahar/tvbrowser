/*
 * TV-Pearl by Reinhard Lehrbaum
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
package tvpearlplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;

public class TVPearl
{
	static final int MILLIS_PER_HOUR = 60 * 60 * 1000;

	private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TVPearlPlugin.class.getName());

	private String mUrl;
	private List<TVPProgram> mProgramList;
	private Calendar mLastUpdate;
	private boolean mReindexAll = true;

	public TVPearl()
	{
		mProgramList = new ArrayList<TVPProgram>();
		mLastUpdate = Calendar.getInstance();
		mLastUpdate.set(Calendar.HOUR_OF_DAY, mLastUpdate.get(Calendar.HOUR_OF_DAY) - 13);
	}

	public boolean getReindexAll()
	{
		return mReindexAll;
	}

	public void setReindexAll(boolean reindexAll)
	{
		mReindexAll = reindexAll;
	}

	public void setUrl(String url)
	{
		mUrl = url;
	}

	public String getUrl()
	{
		return mUrl;
	}

	public void update()
	{
		if (canUpdate())
		{
			mLastUpdate = Calendar.getInstance();

			TVPGrabber grabber = new TVPGrabber();
			List<TVPProgram> programList = grabber.parse(mUrl);
			mUrl = grabber.getLastUrl();

			for (TVPProgram program : programList)
			{
				addProgram(program);
			}
			Calendar limit = getViewLimit();
			int i = 0;
			while (i < mProgramList.size())
			{
				TVPProgram p = mProgramList.get(i);
				if (p.getStart().compareTo(limit) < 0)
				{
					mProgramList.remove(i);
					i--;
				}
				i++;
			}
			Collections.sort(mProgramList);
			updateProgramMark();
		}
	}

	private void addProgram(TVPProgram program)
	{
		if (indexOf(program) == -1)
		{
			setProgramID(program, false);
			mProgramList.add(program);
		}
	}

	private void setProgramID(TVPProgram program, boolean reindex)
	{
		boolean found = false;
		if (program.getProgramID().length() == 0 || reindex)
		{
			program.resetStatus();

			List<Channel> channelList = getChannelFromName(program.getChannel());
			for (Channel channel : channelList)
			{
				program.setStatus(1);
				if (program.getStart().compareTo(getViewLimit()) > 0)
				{
					Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(new devplugin.Date(program.getStart()), channel);
					while ((it != null) && (it.hasNext()))
					{
						Program p = (Program) it.next();
						if (compareTitle(p.getTitle(), program.getTitle()) && p.getHours() == program.getStart().get(Calendar.HOUR_OF_DAY) && p.getMinutes() == program.getStart().get(Calendar.MINUTE))
						{
							program.setProgramID(p.getID());
							found = true;
							break;
						}
					}
					if (found)
					{
						break;
					}
				}
				else
				{
					break;
				}
			}
		}
	}

	private boolean compareTitle(String title1, String title2)
	{
		String t1 = title1.toLowerCase();
		String t2 = title2.toLowerCase();

		return t1.equals(t2) || t1.indexOf(t2) >= 0 || t2.indexOf(t1) >= 0;
	}

	private Calendar getViewLimit()
	{
		Calendar limit = Calendar.getInstance();
		limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) - 1);
		return limit;
	}

	private Integer indexOf(TVPProgram program)
	{
		Integer index = -1;
		for (int i = 0; i < mProgramList.size(); i++)
		{
			TVPProgram p = mProgramList.get(i);
			if (p.getAuthor().equals(program.getAuthor()) && p.getStart().equals(program.getStart()) && p.getChannel().equals(program.getChannel()) && p.getTitle().equals(program.getTitle()))
			{
				index = i;
				break;
			}
		}
		return index;
	}

	private List<Channel> getChannelFromName(String channelName)
	{
		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
		List<Channel> result = new ArrayList<Channel>();
		Pattern pattern = Pattern.compile("^(.*[ ()])?" + channelName + "([ ()].*)?$");
		for (Channel channel : channels)
		{
			Matcher matcher = pattern.matcher(channel.getDefaultName());
			if (matcher.find())
			{
				result.add(channel);
			}
		}
		return result;
	}

	private boolean canUpdate()
	{
		Calendar now = Calendar.getInstance();

		long hours = Math.round((double) (now.getTimeInMillis() - mLastUpdate.getTimeInMillis()) / MILLIS_PER_HOUR);

    return hours > 12;
	}

	public TVPProgram getPerle(Program program)
	{
		TVPProgram result = null;
		for (TVPProgram p : mProgramList)
		{
			if (p.getProgramID().equalsIgnoreCase(program.getID()) && program.getDate().equals(new devplugin.Date(p.getStart())))

			{
				result = p;
				break;
			}
		}
		return result;
	}

	public TVPProgram[] getPerlenList()
	{
		List<TVPProgram> result = new ArrayList<TVPProgram>();
		result.addAll(mProgramList);

		switch (TVPearlPlugin.getInstance().getPropertyInteger("ViewOption"))
		{
			case 2:
			case 3:
				Integer threshold = TVPearlPlugin.getInstance().getPropertyInteger("ViewOption") - 1;
				int i = 0;
				while (i < result.size())
				{
					if (result.get(i).getStatus() < threshold)
					{
						result.remove(i);
					}
					else
					{
						i++;
					}
				}
				break;
			default:
				break;
		}
		Calendar limit = getViewLimit();
		int i = 0;
		while (i < result.size())
		{
			if (result.get(i).getStart().compareTo(limit) < 0)
			{
				result.remove(i);
				i--;
			}
			i++;
		}
		if (TVPearlPlugin.getInstance().getPropertyBoolean("ShowEnableFilter"))
		{
			i = 0;
			while (i < result.size())
			{
				if (!TVPProgramFilter.showProgram(result.get(i)))
				{
					result.remove(i);
					i--;
				}
				i++;
			}
		}

		return result.toArray(new TVPProgram[result.size()]);
	}

	public void recheckProgramID()
	{
		for (int i = 0; i < mProgramList.size(); i++)
		{
			TVPProgram program = mProgramList.get(i);
			setProgramID(program, mReindexAll);
		}
	}

	@SuppressWarnings("unchecked")
	public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		Calendar limit = getViewLimit();

		int version = in.readInt();

		if (version >= 1)
		{
			mLastUpdate = Calendar.getInstance();
			mLastUpdate.setTime((Date) in.readObject());

			int size = in.readInt();
			for (int i = 0; i < size; i++)
			{
				TVPProgram p = new TVPProgram();
				p.setAuthor((String) in.readObject());
				Calendar cal = Calendar.getInstance();
				cal.setTime((Date) in.readObject());
				p.setCreateDate(cal);
				p.setContentUrl((String) in.readObject());
				p.setChannel((String) in.readObject());
				cal.setTime((Date) in.readObject());
				p.setStart(cal);
				p.setTitle((String) in.readObject());
				p.setInfo((String) in.readObject());
				p.setProgramID((String) in.readObject());
				if (p.getStart().compareTo(limit) > 0)
				{
					addProgram(p);
				}
			}
			if (version == 2)
			{
				TVPearlPlugin.getInstance().setComposers((Vector<String>) in.readObject());
			}
		}

		//updateProgramMark();
	}

	public void writeData(ObjectOutputStream out) throws IOException
	{
		out.writeInt(2); // version
		out.writeObject(mLastUpdate.getTime());
		out.writeInt(mProgramList.size());
		for (TVPProgram program : mProgramList)
		{
			out.writeObject(program.getAuthor());
			out.writeObject(program.getCreateDate().getTime());
			out.writeObject(program.getContentUrl());
			out.writeObject(program.getChannel());
			out.writeObject(program.getStart().getTime());
			out.writeObject(program.getTitle());
			out.writeObject(program.getInfo());
			out.writeObject(program.getProgramID());
		}
		out.writeObject(TVPearlPlugin.getInstance().getComposers());
	}

	public void updateProgramMark()
	{
		try
		{
			for (TVPProgram program : mProgramList)
			{
				if (program.getStatus() == 2)
				{
					Program p = Plugin.getPluginManager().getProgram(new devplugin.Date(program.getStart()), program.getProgramID());
					if (p != null)
					{
						if (TVPearlPlugin.getInstance().getPropertyBoolean("MarkPearl") && TVPProgramFilter.showProgram(program))
						{
							p.mark(TVPearlPlugin.getInstance());
						}
						else
						{
							p.unmark(TVPearlPlugin.getInstance());
						}
						p.validateMarking();
					}
				}
			}
		}
		catch (Exception ex)
		{
			mLog.warning(ex.getMessage());
			mLog.warning("Additional Info:\nProgram list:" + (mProgramList != null ? mProgramList.size() : "null"));
		}
	}

	public String getInfo()
	{
		String msg = "";
		msg += "Url: " + mUrl + "\n";
		msg += "Program count: " + mProgramList.size() + "\n";
		if (mLastUpdate != null)
		{
			msg += "Last update: " + mLastUpdate.getTime().toString() + "\n";
		}
		return msg.trim();
	}
}
