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
  //  private int pos;
 //   public static final int NOT_SUBSCRIBED=-1;

    public Channel(String name, int id) {
        this.name=name;
        this.id=id;
     //   pos=NOT_SUBSCRIBED;

    }

  /*  public void unsubscribe() {
        pos=NOT_SUBSCRIBED;
    }
*/
    public String toString() {
       // return name+" ("+id+") pos: "+pos;
       return name;
    }

  /*  public void setPos(int pos) {
        this.pos=pos;
    }*/

   /* public int getPos() {
        return pos;
    }
*/
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

  /*  public boolean isSubscribed() {
        return pos!=NOT_SUBSCRIBED;
    }*/
}