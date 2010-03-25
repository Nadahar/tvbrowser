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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;

public class WeeklyRepeater extends Repeat
{
	private int mWeeks;
	private int mWeekDays;

	public int getID()
	{
		return 2;
	}

	public boolean isDayProgram(final Calendar date, final Calendar programStart)
	{
		if (validOptions() && HelperMethods.compareDay(programStart, date) <= 0 && isBevorEnd(date))
		{
		  final long delta = diffDayPeriods(programStart, date) / 7;
      final int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
			if (delta % mWeeks == 0 && getDay(dayOfWeek))//(mWeekDays & dayOfWeek ) == dayOfWeek)
			{
				return true;
			}
		}
		return false;
	}

	public void readData(final ObjectInput in) throws IOException,
      ClassNotFoundException
	{
	  final int version = in.readInt();
		if (version == 1)
		{
			mWeeks = in.readInt();
			mWeekDays = in.readInt();
			setEndDate((Calendar) in.readObject());
		}
	}

	public void writeData(final ObjectOutput out) throws IOException
	{
		out.writeInt(1);
		out.writeInt(mWeeks);
		out.writeInt(mWeekDays);
		out.writeObject(getEndDate());
	}

	private boolean validOptions()
	{
		if (mWeeks < 0 || mWeekDays <= 0 || mWeekDays >= 256)
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return getID() + " " + mWeeks + " " + mWeekDays + " " + (getEndDate() == null ? "noEnd" : getEndDate().getTime());
	}

	public int getWeekDays()
	{
		return mWeekDays;
	}

	public void setWeekDays(final int weekDays)
	{
		mWeekDays = weekDays;
	}

	public int getWeeks()
	{
		return mWeeks;
	}

	public void setWeeks(final int weeks)
	{
		mWeeks = weeks;
	}

	private void setDay(final int day, final boolean set)
	{
		if (set)
		{
			mWeekDays |= (int) Math.pow(2, day);
		}
		else
		{
		  final int d = (int) Math.pow(2, day);
			if ((mWeekDays & d) == d)
			{
				mWeekDays ^= d;
			}
		}
	}

	private boolean getDay(final int day)
	{
	  final int d = (int) Math.pow(2, day);
		return (mWeekDays & d) == d;
	}

	public boolean getMonday()
	{
		return getDay(Calendar.MONDAY);
	}

	public void setMonday(final boolean set)
	{
		setDay(Calendar.MONDAY, set);
	}

	public boolean getTuesday()
	{
		return getDay(Calendar.TUESDAY);
	}

	public void setTuesday(final boolean set)
	{
		setDay(Calendar.TUESDAY, set);
	}

	public boolean getWednesday()
	{
		return getDay(Calendar.WEDNESDAY);
	}

	public void setWednesday(final boolean set)
	{
		setDay(Calendar.WEDNESDAY, set);
	}

	public boolean getThursday()
	{
		return getDay(Calendar.THURSDAY);
	}

	public void setThursday(final boolean set)
	{
		setDay(Calendar.THURSDAY, set);
	}

	public boolean getFriday()
	{
		return getDay(Calendar.FRIDAY);
	}

	public void setFriday(final boolean set)
	{
		setDay(Calendar.FRIDAY, set);
	}

	public boolean getSaturday()
	{
		return getDay(Calendar.SATURDAY);
	}

	public void setSaturday(final boolean set)
	{
		setDay(Calendar.SATURDAY, set);
	}

	public boolean getSunday()
	{
		return getDay(Calendar.SUNDAY);
	}

	public void setSunday(final boolean set)
	{
		setDay(Calendar.SUNDAY, set);
	}
}
