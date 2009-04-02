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

/**
 * Utilities for the CapturePlugin
 * @author bodum
 */
public class CaptureUtilities {
    
    /**
     * Replacing Strings ignoring Case
     * @param inhere Replace in here
     * @param thisone Replace this
     * @param withthis Replace with this
     * @return result
     */
    public static String replaceIgnoreCase(String inhere, String thisone, String withthis) {
        if (inhere == null) {
          return null;
        } else if (thisone == null) {
          return inhere;
        } else if (withthis == null) {
          withthis = "";
        }
        
        StringBuilder result = new StringBuilder();
        int pos = 0;
        
        while ((pos = inhere.toLowerCase().indexOf(thisone.toLowerCase())) > 0) {
            result.append(inhere.substring(0, pos));
            result.append(withthis);
            inhere = inhere.substring(pos + thisone.length());
        }
        
        result.append(inhere);
        
        return result.toString();
    }    
}
