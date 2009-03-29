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

import java.io.*;
import java.util.*;


public class DailyRepeater extends Repeat
{
	private int mDays;

	public int getID()
	{
		return 1;
	}

	public Boolean isDayProgram(Calendar date, Calendar programStart)
	{
		if (validOptions() && programStart.compareTo(date) <= 0 && isBevorEnd(date))
		{
			long delta = diffDayPeriods(programStart, date);
			if (delta % mDays == 0)
			{
				return true;
			}
		}
		return false;
	}

	public void readData(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int version = in.readInt();
		if (version == 1)
		{
			mDays = in.readInt();
			setEndDate((Calendar) in.readObject());
		}
	}

	public void writeData(ObjectOutput out) throws IOException
	{
		out.writeInt(1);
		out.writeInt(mDays);
		out.writeObject(getEndDate());
	}

	public int getDays()
	{
		return mDays;
	}

	public void setDays(int days)
	{
		mDays = days;
	}

	private Boolean validOptions()
	{
		if (mDays < 0)
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return getID() + " " + mDays + " " + (getEndDate() == null ? "noEnd" : getEndDate().getTime());
	}
}
