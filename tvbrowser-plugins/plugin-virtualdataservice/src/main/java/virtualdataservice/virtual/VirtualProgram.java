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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;

public class VirtualProgram implements Externalizable, Comparable<VirtualProgram>
{
	private static final long serialVersionUID = -2157351926383423498L;
//  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(VirtualProgram.class.getName());

	private String mTitle;
	private Calendar mStart;
	private int mLength;
	private Repeat mRepeat = null;

	public VirtualProgram()
	{

	}

	public int getLength()
	{
		return mLength;
	}

	public void setLength(final int length)
	{
		mLength = length;
	}

	public Repeat getRepeat()
	{
		return mRepeat;
	}

	public void setRepeat(final Repeat repeat)
	{
		mRepeat = repeat;
	}

	public Calendar getStart()
	{
		return mStart;
	}

	public void setStart(final Calendar start)
	{
		mStart = start;
		clearTime();
}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(final String title)
	{
		mTitle = title;
	}

	public void readExternal(final ObjectInput in) throws IOException,
      ClassNotFoundException
	{
	  final int version = in.readInt();
		if (version == 1)
		{
			mTitle = (String) in.readObject();
			mStart = (Calendar) in.readObject();
			mLength = in.readInt();
			final int repeat = in.readInt();
			mRepeat = Repeat.createRepeater(repeat, in);

			clearTime();
		}
	}

	public void writeExternal(final ObjectOutput out) throws IOException
	{
		out.writeInt(1); //version
		out.writeObject(mTitle);
		out.writeObject(mStart);
		out.writeInt(mLength);
		if (mRepeat == null)
		{
			out.writeInt(0);//mRepeat);
		}
		else
		{
			out.writeInt(mRepeat.getID());
			mRepeat.writeData(out);
		}
	}

	public String toString()
	{
		return mTitle + ": " + mStart.getTime() + " (" + mLength + ") " + (mRepeat != null ? mRepeat : 0);
	}

	public boolean isActive()
	{
	  final Calendar limit = Calendar.getInstance();
		limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) - 1);
		limit.set(Calendar.HOUR_OF_DAY, 0);
		limit.set(Calendar.MINUTE, 0);
		limit.set(Calendar.SECOND, 0);
		limit.set(Calendar.MILLISECOND, 0);

		if (mRepeat == null)
		{
			if (mStart.compareTo(limit) >= 0)
			{
				return true;
			}
		}
		else
		{
			if (mRepeat.isBevorEnd(limit))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isDayProgram(final Calendar day)
	{
		if (mRepeat == null)
		{
		  if (day.get(Calendar.YEAR) == mStart.get(Calendar.YEAR) && day.get(Calendar.MONTH) == mStart.get(Calendar.MONTH) && day.get(Calendar.DAY_OF_MONTH) == mStart.get(Calendar.DAY_OF_MONTH))
			{
				return true;
			}
		}
		else
		{
			if (mRepeat.isDayProgram(day, mStart))
			{
				return true;
			}
		}
		return false;
	}

	public int compareTo(final VirtualProgram p)
	{
	  final int compare = mStart.compareTo(p.getStart());
		if (compare == 0)
		{
			return mTitle.compareTo(p.getTitle());
		}
		return compare;
	}

	private void clearTime()
	{
		mStart.set(Calendar.SECOND, 0);
		mStart.set(Calendar.MILLISECOND, 0);
	}
}
