/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package tvraterplugin;

import java.util.Comparator;

/**
 * This class compares two Ratings. It uses the Title to compare them.
 * @author bodo tasche
 */
public class RatingComparator implements Comparator<Rating> {

	/**
	 * Compares two Ratings
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @param r1 rating 1
	 * @param r2 rating 2
	 * @return Compare-Result
	 */
	public int compare(Rating r1, Rating r2) {
		return r1.toString().compareToIgnoreCase(r2.toString());
	}

}
