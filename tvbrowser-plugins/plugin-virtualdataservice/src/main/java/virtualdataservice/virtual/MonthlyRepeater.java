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

public class MonthlyRepeater extends Repeat
{
	private int mDay;

	public int getID()
	{
		return 3;
	}

	public boolean isDayProgram(final Calendar date, final Calendar programStart)
	{
		if (validOptions() && HelperMethods.compareDay(programStart, date) <= 0 && isBevorEnd(date))
		{
			if (date.get(Calendar.DAY_OF_MONTH) == mDay)
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
			mDay = in.readInt();
			setEndDate((Calendar) in.readObject());
		}
	}

	public void writeData(final ObjectOutput out) throws IOException
	{
		out.writeInt(1);
		out.writeInt(mDay);
		out.writeObject(getEndDate());
	}

	public int getDay()
	{
		return mDay;
	}

	public void setDay(final int day)
	{
		mDay = day;
	}

	private boolean validOptions()
	{
		if (mDay < 0 || mDay > 31)
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return getID() + " " + mDay + " " + (getEndDate() == null ? "noEnd" : getEndDate().getTime());
	}

}
