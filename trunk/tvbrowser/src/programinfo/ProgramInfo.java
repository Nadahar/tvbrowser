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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package programinfo;

import devplugin.*;
import java.util.Properties;

public class ProgramInfo extends devplugin.Plugin {

  public String getContextMenuItemText() {
    return "info";
  }

  public PluginInfo getInfo() {
    return new PluginInfo("ProgramInfo","Zeigt Informationen zu einer Sendung an.","Martin Oberhauser",new Version(1,0));

  }

  public String getButtonText() {
    return null;
  }


  public void execute(devplugin.Program program) {
    ProgramInfoDialog dlg=new ProgramInfoDialog(parent, program);
    dlg.show();
    dlg.pack();
  }

  public String getMarkIcon() {
    return null;
  }


}