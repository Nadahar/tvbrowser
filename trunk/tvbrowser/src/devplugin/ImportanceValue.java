/*
* TV-Browser
* Copyright (C) 2003-2010 TV-Browser-Team (dev@tvbrowser.org)
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
*     $Date$
*   $Author$
* $Revision$
*/
package devplugin;

/**
 * A class with a weighted importance value to calculate the program opacity.
 * 
 * @author René Mach
 * @since 3.0
 */
public class ImportanceValue {
  private byte mWeight;
  private short mTotalImportance;
  
  /**
   * Creates an instance of this class.
   * <p>
   * EXAMPLE: If you give a weight of 4 you mean the totalImportance
   *          contains a sum of 4 importance values.
   * NOTE: This will ignore values that are not reachable with the possible
   *       single importance of a Program. So for instance, if you give
   *       a weight of 4 and a totalImportance of 50 this will be ignored
   *       as 40 ist the highest possible amount for a weight of 4.
   * <p>
   * @param weight Is the count of importance values given.
   * @param totalImportance Is the sum of the count importance values
   */
  public ImportanceValue(byte weight, short totalImportance) {
    mWeight = weight;
    mTotalImportance = totalImportance;
    
    if(totalImportance / weight < Program.MIN_PROGRAM_IMPORTANCE || 
        totalImportance / weight > weight * Program.MAX_PROGRAM_IMPORTANCE) {
      weight = 0;
      mTotalImportance = Program.DEFAULT_PROGRAM_IMPORTANCE;
    }
  }
  
  /**
   * Gets the weight for this importance value.
   * <p>
   * @return The weight of this importance value.
   */
  public byte getWeight() {
    return mWeight;
  }
  
  /**
   * Gets the total importance of this importance value
   * <p>
   * @return The total importance of this importance value.
   */
  public short getTotalImportance() {
    return mTotalImportance;
  }
}
