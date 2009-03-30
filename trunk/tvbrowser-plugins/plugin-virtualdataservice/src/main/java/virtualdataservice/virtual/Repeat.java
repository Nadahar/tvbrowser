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


public abstract class Repeat
{
	private Calendar mEndDate = null;

	public Calendar getEndDate()
	{
		return mEndDate;
	}

	public void setEndDate(final Calendar endDate)
	{
		mEndDate = endDate;
	}

	public abstract int getID();

	public abstract boolean isDayProgram(Calendar date, Calendar programStart);

	public abstract void readData(ObjectInput in) throws IOException, ClassNotFoundException;

	public abstract void writeData(ObjectOutput out) throws IOException;

	public static Repeat createRepeater(final int id, final ObjectInput in)
      throws IOException, ClassNotFoundException
	{
		Repeat result = null;

		switch (id)
		{
			case 1:
				result = new DailyRepeater();
				break;
			case 2:
				result = new WeeklyRepeater();
				break;
			case 3:
				result = new MonthlyRepeater();
				break;
			case 4:
				result = new MonthlyExRepeater();
				break;
			case 5:
				result = new YearlyRepeater();
				break;
			default:
				return null;
		}
		result.readData(in);

		return result;
	}

	public boolean isBevorEnd(final Calendar date)
	{
		if (mEndDate != null && mEndDate.compareTo(date) > 0)
			{
				return false;
			}
		return true;
	}

	protected long diffDayPeriods(final Calendar start, final Calendar end)
	{
	  final long startMillis = cloneDayOnly(start).getTimeInMillis();
    final long endMillis = cloneDayOnly(end).getTimeInMillis();

	  final long endL = endMillis + end.getTimeZone().getOffset(endMillis);
    final long startL = startMillis
        + start.getTimeZone().getOffset(startMillis);
		return (endL - startL) / (24 * 60 * 60 * 1000);
	}

	protected Calendar cloneDayOnly(final Calendar date)
	{
	  final Calendar cal = (Calendar) date.clone();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}
}
