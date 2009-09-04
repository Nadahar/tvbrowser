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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.utils;

import java.util.Comparator;

import devplugin.Program;
import devplugin.ProgramFieldType;


/**
 * This Class compares two Programs by Date/Time.
 * If the 2 Programs are equal, 1 is returned
 * @author bodum
 */
public class ProgramTimeComparator implements Comparator<Program> {

    public int compare(Program p1, Program p2) {
        int dc = p1.getDate().compareTo(p2.getDate());
        if (dc != 0) {
          return dc;
        }
        
        int t1 = p1.getTimeField(ProgramFieldType.START_TIME_TYPE);
        int t2 = p2.getTimeField(ProgramFieldType.START_TIME_TYPE);
        
        if (t1 < t2) {
            return -1;
        } else if (t1 > t2) {
            return 1;
        }
        
        return 1;
    }

}