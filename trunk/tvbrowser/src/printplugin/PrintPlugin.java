
package printplugin;

import util.ui.UiUtilities;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.awt.print.*;
import javax.print.*;
import javax.print.DocFlavor.*;
import java.io.*;
import java.awt.print.*;
import javax.swing.*;
import devplugin.*;


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
      return "printplugin/Print24.gif";
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

       return new PluginInfo(name, desc, author, new Version(0, 2));
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