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
package showviewplugin;

import java.util.Iterator;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import devplugin.*;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgramFieldType;

/**
 * Plugin that tries to calculate the Showview numbers for new TV data. 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ShowviewPlugin extends Plugin {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ShowviewPlugin.class);

  
  public ShowviewPlugin() {
  }


  public String getButtonText() {
    return null;
  }

  public String getMarkIconName() {
    return null;
  }


  public String getButtonIconName() {
    return null;
  }


  /**
   * Tries to calculate the showview number for each program where it is missing. 
   * <p>
   * This method is automatically called, when the TV data has changed.
   * (E.g. after an update).
   * 
   * @param newProg The new ChannelDayProgram.
   */
  public void handleTvDataChanged(MutableChannelDayProgram newProg) {
    Iterator iterator = newProg.getPrograms();
    while (iterator.hasNext()) {
      MutableProgram prog = (MutableProgram) iterator.next();
      
      String showview = prog.getTextField(ProgramFieldType.SHOWVIEW_NR_TYPE);
      if (showview == null) {
        try {
          showview = ShowviewEncoder.getInstance().getShowviewNumberFor(prog);
          prog.setTextField(ProgramFieldType.SHOWVIEW_NR_TYPE, showview);
        }
        catch (Exception exc) {
          // We tried it...
        }
      }
    }
  }


  public PluginInfo getInfo() {
    String name = mLocalizer.msg("showviewCalculater", "Showview number calculator");
    String desc = mLocalizer.msg("description",
      "Tries to calculate the showview numbers for new TV data." );
    String author = "Til Schneider, www.murfman.de" ;
    
    return new PluginInfo(name, desc, author, new Version(1, 0));
  }

}
