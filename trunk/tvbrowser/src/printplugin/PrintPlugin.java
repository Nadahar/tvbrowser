/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * PrintPlugin
 * Copyright (C) 08-2003 Robert Inzinger (yxterwd@users.sourceforge.net)
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

package printplugin;

import util.ui.UiUtilities;

import java.util.Iterator;
import java.util.Properties;
import javax.print.*;
import javax.swing.*;
import devplugin.*;

/**
 * Provides a dialog for printing programs.
 *
 * @author Robert Inzinger
 */

public class PrintPlugin extends devplugin.Plugin
{
   /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);

   private static PrintPlugin mInstance;

   public PrintService[] mAllServices;

   public PrintPlugin()
   {
      mInstance=this;
   }

   public String getButtonIconName()
   {
      return "printplugin/Print16.gif";
   }
   public String getMarkIconName()
   {
      return "printplugin/Print16.gif";
   }

   public String getButtonText()
   {
      return mLocalizer.msg("printProgram", "Print program");
   }

   public PluginInfo getInfo()
   {
       String name = mLocalizer.msg("printProgram" ,"Print program");
       String desc = mLocalizer.msg("printdescription" ,"Allows printing programs.");
       String author = "Robert Inzinger";

       return new PluginInfo(name, desc, author, new Version(0, 4));
   }

// public void execute(Program program)
// {
//    PrintDialog dlg = new PrintDialog(super.parent);
//    UiUtilities.centerAndShow(dlg);
// }
   public void execute()
   {
      boolean start = true;

      if (mAllServices.length == 0)
      {
         mAllServices = PrintServiceLookup.lookupPrintServices(null, null);
      }

      Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();

      if (channels.length > 0)
      {
         Iterator programIter = Plugin.getPluginManager().getChannelDayProgram(new Date(), channels[0]);
         if (programIter == null)
         {
            JOptionPane.showMessageDialog(null,
                                          mLocalizer.msg("nodata" ,"No data"),
                                          mLocalizer.msg("printProgram" ,"Print program"),
                                          JOptionPane.ERROR_MESSAGE);
            start = false;
         }
      }
      else
      {
            JOptionPane.showMessageDialog(null,
                                          mLocalizer.msg("nochannel" ,"No Channels"),
                                          mLocalizer.msg("printProgram" ,"Print program"),
                                          JOptionPane.ERROR_MESSAGE);
            start = false;
      }

      if (mAllServices.length == 0)
      {
         JOptionPane.showMessageDialog(null,
                                       mLocalizer.msg("noprinter" ,"No printer"),
                                       mLocalizer.msg("printProgram" ,"Print program"),
                                       JOptionPane.ERROR_MESSAGE);
         start = false;
      }

      if(start)
      {
         PrintDialog dlg = new PrintDialog(super.parent, this);
         UiUtilities.centerAndShow(dlg);
      }


   }

   public void loadSettings(Properties settings)
   {
      mAllServices = PrintServiceLookup.lookupPrintServices(null, null);
   }

}