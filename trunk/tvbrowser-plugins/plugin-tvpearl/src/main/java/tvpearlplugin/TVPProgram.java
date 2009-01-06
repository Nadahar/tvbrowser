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

import java.text.DateFormat;
import java.util.Calendar;

import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

public class TVPProgram implements Comparable<TVPProgram>
{
	private String mAuthor;
	private Calendar mCreateDate;
	private String mContentUrl;
	private String mTitle;
	private String mChannel;
	private Calendar mStart;
	private String mInfo;
	private String mProgramID;
	private boolean mSendTo = false;
	private int mStatus = IProgramStatus.STATUS_NOT_FOUND;

	public int compareTo(TVPProgram o)
	{
		return mStart.compareTo(o.getStart());
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(DateFormat.getDateInstance().format(mStart.getTime()));
		buffer.append(" · ");
		buffer.append(DateFormat.getTimeInstance().format(mStart.getTime()));
		buffer.append(" · ");
		buffer.append(mChannel);
		buffer.append(" · ");
		buffer.append(mTitle);

		return buffer.toString();
	}

	public String getAuthor()
	{
		return mAuthor;
	}

	public void setAuthor(String author)
	{
		this.mAuthor = author;
	}

	public String getChannel()
	{
		return mChannel;
	}

	public void setChannel(String channel)
	{
		this.mChannel = channel;
	}

	public String getContentUrl()
	{
		return mContentUrl;
	}

	public void setContentUrl(String contentUrl)
	{
		this.mContentUrl = contentUrl;
	}

	public Calendar getCreateDate()
	{
		return mCreateDate;
	}

	public void setCreateDate(Calendar createDate)
	{
		this.mCreateDate = createDate;
	}

	public String getInfo()
	{
		return mInfo;
	}

	public void setInfo(String info)
	{
		this.mInfo = info.trim();
	}

	public String getProgramID()
	{
		return mProgramID;
	}

	public void setProgramID(String programID)
	{
		this.mProgramID = programID.trim();
		if (this.mProgramID != null && this.mProgramID.length() > 0)
		{
			setStatus(IProgramStatus.STATUS_FOUND_PROGRAM);
		}
	}

	public Calendar getStart()
	{
		return mStart;
	}

	public void setStart(Calendar start)
	{
		this.mStart = start;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String title)
	{
		this.mTitle = title;
	}

	public boolean getSendTo()
	{
		return mSendTo;
	}

	public void setSendTo(boolean sendTo)
	{
		mSendTo = sendTo;
	}

	public int getStatus()
	{
		return mStatus;
	}

	public void setStatus(Integer status)
	{
		if (this.mStatus < status)
		{
			this.mStatus = status;
		}
	}

	public void resetStatus()
	{
		mProgramID = "";
		mStatus = IProgramStatus.STATUS_NOT_FOUND;
	}

	/**
	 * was program pearl found in TV-Browser?
	 * 
	 * @return
	 */
	public boolean wasFound()
	{
		return getStatus() == IProgramStatus.STATUS_FOUND_PROGRAM;
	}

	/**
	 * get date of this program pearl
	 * 
	 * @return
	 */
	public Date getDate()
	{
		return new Date(getStart());
	}

	/**
	 * get the TV-Browser program for this pearl or null, if no program exists
	 * with the given date and program ID
	 * 
	 * @return
	 */
	public Program getProgram()
	{
		final String id = getProgramID();
		if (id != null && id.length() > 0)
		{
			return Plugin.getPluginManager().getProgram(getDate(), id);
		}
		return null;
	}

	/**
	 * connect the TV pearl program to the given TV-Browser program
	 * 
	 * @param program
	 */
	public void setProgram(Program program)
	{
		setProgramID(program.getID());
	}

  public boolean isSubscribedChannel() {
    return getStatus() == IProgramStatus.STATUS_FOUND_CHANNEL || wasFound();
  }
}
