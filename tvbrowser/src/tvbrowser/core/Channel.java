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

package tvbrowser.core;

public class Channel implements java.io.Serializable, devplugin.Channel {

    private String name;
    private int id;
    private String dataService;
    
    /*
    public Channel(String name, int id, String dataService) {
        this.name=name;
        this.id=id;
        this.dataService=dataService;
    }*/
    
  /*  public Channel(String name, int id) {
    	this.name=name;
    	this.id=id;
    }*/
    
	public void init(String name, int id) {
		this.name=name;
		this.id=id;
    }
    
    
   public Channel(String dataService) {
   	this.dataService=dataService;
   }
    
   

	public String getDataServiceName() {
		return dataService;
	}

    public String toString() {
       return name+" ("+dataService+")";
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean equals(devplugin.Channel ch) {
        if (ch==null) return false;
        return (ch.getId()==id);
    }
}
