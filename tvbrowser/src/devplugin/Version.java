/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package devplugin;

public final class Version implements Comparable {

  private int major, minor;
  private boolean stable;
  private String name;

  public Version(int major, int minor) {
    this.major=major;
    this.minor=minor;
    name=null;
    stable=true;
  }
  
  public Version(int major, int minor, boolean stable) {
  	this(major,minor);
  	this.stable=stable;
  }
  
  public Version(int major, int minor, boolean stable, String name) {
  	this(major, minor, stable);
  	this.name=name;
  }

  public String toString() {
  	if (name==null) {
      return major+(minor<10?".0":".")+minor+(stable?"":"beta");
  	}
  	return name;
  }


  public boolean isStable() {
  	return stable;
  }
  

  public int getMajor() { return major; }
  public int getMinor() { return minor; }
  
  public int compareTo(Object obj) throws ClassCastException {
  	Version v=(Version)obj;
  	
  	if (major>v.major) {
  		return 1;
  	}else if (major<v.major) {
  		return -1;  		
  	}else {  // major is equals
  		if (minor>v.minor) {
  			return 1;
  		} else if (minor<v.minor) {
  			return -1;
  		}else {  // minor is equals
  			if (stable && !v.stable) {
  				return 1;
  			}
  			else if (!stable && v.stable){
  				return -1;				
  			}
  			else {
  				return 0;
  			}
  		}   		
  	} 	
  }
  
  
  public boolean equals(Object obj) {
    if (obj instanceof Version) {
      Version ver = (Version) obj;
      return (major == ver.major) && (minor == ver.minor) && (stable == ver.stable);
    } else {
      return false;
    }
  }
  
}