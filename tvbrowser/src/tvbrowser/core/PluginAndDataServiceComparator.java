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
package tvbrowser.core;

import java.util.Comparator;

import devplugin.InfoIf;


/**
 * A Comparator for sorting plugins and data services (sorting alphabetically by name)
 * 
 * @author René Mach
 */
public class PluginAndDataServiceComparator implements Comparator<InfoIf> {

  public int compare(InfoIf info1, InfoIf info2) {
    return info1.getInfo().getName().compareTo(info2.getInfo().getName());
  }
}
