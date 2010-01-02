/*
 * DVBViewerChannelGroup.java
 * Copyright (C) 2008 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */
package dvbviewerdataservice;

import devplugin.ChannelGroup;


/**
 * TODO short description for DVBViewerChannelGroup.
 * <p>
 * Long description for DVBViewerChannelGroup.
 *
 * @author pollaehne
 * @version $Revision: $
 */
public class DVBViewerChannelGroup implements ChannelGroup {

  String descript = "";


  /**
   * @param description
   */
  public DVBViewerChannelGroup(String description) {
    descript = description;
  }


  /**
   * @see devplugin.ChannelGroup#getDescription()
   */
  public String getDescription() {
    return descript;
  }


  /**
   * @see devplugin.ChannelGroup#getId()
   */
  public String getId() {
    return "DVBViewerEPG";
  }


  /**
   * @see devplugin.ChannelGroup#getName()
   */
  public String getName() {
    return "DVBViewer EPG";
  }


  /**
   * @see devplugin.ChannelGroup#getProviderName()
   */
  public String getProviderName() {
    return "DVBViewer Pro";
  }


  public boolean equals(Object obj) {

    if (obj instanceof ChannelGroup) {
        return ((ChannelGroup)obj).getId().equalsIgnoreCase(getId());
    }
    return false;

  }
}



