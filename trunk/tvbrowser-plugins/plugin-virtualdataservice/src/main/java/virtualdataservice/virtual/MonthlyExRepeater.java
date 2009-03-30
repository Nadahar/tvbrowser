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

public class MonthlyExRepeater extends Repeat
{
	private int mDayOfWeek;
	private int mWeekOfMonth;

	public int getID()
	{
		return 4;
	}

	public boolean isDayProgram(final Calendar date, final Calendar programStart)
	{
		if (validOptions() && programStart.compareTo(date) <= 0 && isBevorEnd(date))
		{
			if (date.get(Calendar.WEEK_OF_MONTH) == mWeekOfMonth && date.get(Calendar.DAY_OF_WEEK) == mDayOfWeek)
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
			mDayOfWeek = in.readInt();
			mWeekOfMonth = in.readInt();
			setEndDate((Calendar) in.readObject());
		}
	}

	public void writeData(final ObjectOutput out) throws IOException
	{
		out.writeInt(1);
		out.writeInt(mDayOfWeek);
		out.writeInt(mWeekOfMonth);
		out.writeObject(getEndDate());
	}

	private boolean validOptions()
	{
		if (mDayOfWeek < 1 || mDayOfWeek > 7 || mWeekOfMonth < 0 || mWeekOfMonth > 5)
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return getID() + " " + mWeekOfMonth + " " + mDayOfWeek + " " + (getEndDate() == null ? "noEnd" : getEndDate().getTime());
	}

	public int getDayOfWeek()
	{
		return mDayOfWeek;
	}

	public void setDayOfWeek(final int day)
	{
		mDayOfWeek = day;
	}

	public int getWeekOfMonth()
	{
		return mWeekOfMonth;
	}

	public void setWeekOfMonth(final int week)
	{
		mWeekOfMonth = week;
	}
}
