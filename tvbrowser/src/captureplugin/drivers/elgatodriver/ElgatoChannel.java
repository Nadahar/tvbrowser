/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver;

/**
 * This Class represents a Channel in the Elgato EyeTV
 * 
 * @author bodum
 */
public class ElgatoChannel {
    /** Number of the Channel */
    private int mNumber;
    /** Name of the Channel */
    private String mName;

    /**
     * Create the Channel
     * @param number Number of the Channel
     * @param name Name of the Channel
     */
    public ElgatoChannel(int number, String name) {
        mNumber = number;
        mName = name;
    }

    /**
     * @return get the Name of the Channel
     */
    public String getName() {
        return mName;
    }

    /**
     * @param name Set the Name of the Channel
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return get the Number of the Channel
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * @param number Set the Number of the Channel
     */
    public void setNumber(int number) {
        mNumber = number;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return mNumber + ". " + mName;
    }
}
