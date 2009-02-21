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
	
  public TVPProgram(final String author, final String contentUrl,
      final Calendar cal, final String title, final String channel,
      final Calendar start, final String info, final String programID) {
    this.mAuthor = author;
    this.mContentUrl = contentUrl;
    this.mCreateDate = cal;
    this.mTitle = title;
    this.mChannel = channel;
    this.mStart = start;
    this.mInfo = info.trim();
    setProgramID(programID);
  }

  public String toString()
	{
    final StringBuffer buffer = new StringBuffer();
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

	public String getChannel()
	{
		return mChannel;
	}

	public String getContentUrl()
	{
		return mContentUrl;
	}

	public Calendar getCreateDate()
	{
		return mCreateDate;
	}

	public String getInfo()
	{
		return mInfo;
	}

	public String getProgramID()
	{
		return mProgramID;
	}

	public void setProgramID(final String programID)
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

	public String getTitle()
	{
		return mTitle;
	}

	public boolean getSendTo()
	{
		return mSendTo;
	}

	public void setSendTo(final boolean sendTo)
	{
		mSendTo = sendTo;
	}

	public int getStatus()
	{
		return mStatus;
	}

	public void setStatus(final int status)
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
	public void setProgram(final Program program)
	{
		setProgramID(program.getID());
	}

  public boolean isSubscribedChannel() {
    return getStatus() == IProgramStatus.STATUS_FOUND_CHANNEL || wasFound();
  }

  public int compareTo(final TVPProgram other) {
    if (this == other) {
      return 0;
    }
    // sort by start time first
    final int result = mStart.compareTo(other.getStart());
    if (result != 0) {
      return result;
    }
    // then by title
    return mTitle.compareTo(other.mTitle);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof TVPProgram)) {
      return false;
    }
    final TVPProgram other = (TVPProgram) object;
    return mTitle.equals(other.mTitle) && mChannel.equals(other.mChannel)
        && mAuthor.equals(other.mAuthor) && mStart.equals(other.mStart);
  }

}
