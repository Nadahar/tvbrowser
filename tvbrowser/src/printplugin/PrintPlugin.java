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

import printplugin.dlgs.PrintDialog;

import util.ui.UiUtilities;

import java.awt.print.*;
import java.util.Iterator;
import java.util.Properties;
import javax.print.*;
import javax.swing.*;
import devplugin.*;

/**
 * Provides a dialog for printing programs.
 *
 * @author Robert Inzinger
 * @author Martin Oberhauser
 */

public class PrintPlugin extends devplugin.Plugin
{
   /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PrintPlugin.class);

   public PrintService[] mAllServices;

   public PrintPlugin()
   {
   }

   public String getButtonIconName()
   {
      return "printplugin/imgs/Print16.gif";
   }
   public String getMarkIconName()
   {
      return "printplugin/imgs/Print16.gif";
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

       return new PluginInfo(name, desc, author, new Version(0, 5));
   }



  public void execute() {
    
    
    
    PrinterJob printJob = PrinterJob.getPrinterJob();
    
    PrintDialog dlg = new PrintDialog(getParentFrame(), printJob);
    util.ui.UiUtilities.centerAndShow(dlg);
    
    Printer printer = dlg.getPrinter();
    
    if (printer!=null) {
      printJob.setPrintable(printer, dlg.getPageFormat());    
      try {
        printJob.print();
      } catch (PrinterException e) {
        e.printStackTrace();
      }
    }
    
  }


  public void executeOLD() {
    
    PrinterJob printJob = PrinterJob.getPrinterJob();
    System.out.println("printservice: "+printJob.getPrintService().getName());
     
    printJob.printDialog();
     
    System.out.println("printservice: "+printJob.getPrintService().getName());     
     
    Channel[] channelList = getPluginManager().getSubscribedChannels();
    PageFormat pageFormat = printJob.defaultPage();
    Date date = Date.getCurrentDate();
    int dayCount = 3;
    double zoom = 0.4;
     
  /*  Printer printer = new Printer(PageFactory.createPages(pageFormat, zoom, channelList, date, dayCount));
     
    printJob.setPrintable(printer);
     
    try {
      printJob.print();
    } catch (PrinterException e) {
      e.printStackTrace();
    }
    */
  }



  

   public void loadSettings(Properties settings)
   {
      mAllServices = PrintServiceLookup.lookupPrintServices(null, null);
   }

}