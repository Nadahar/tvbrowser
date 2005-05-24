/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package printplugin.printer;

import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.QueuePrinterSettings;
import printplugin.printer.queueprinter.QueuePrintJob;
import printplugin.printer.dayprogramprinter.DayProgramPrintJob;
import printplugin.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.*;

import devplugin.*;


public class JobFactory {


  public static PrintJob createPrintJob(QueuePrinterSettings settings, PageFormat pageFormat, Program[] programs) {
    if (programs.length == 0) {
      return createEmptyJob();
    }
    PageModel pageModel = createPage(programs);
    PrintJob job = new QueuePrintJob(pageModel, settings, pageFormat);
    return job;
  }

  private static PageModel createPage(Program[] programs) {
    Util.sortProgramsByDateAndChannel(programs);

    DefaultPageModel pageModel = new DefaultPageModel();
    DefaultColumnModel colModel = new DefaultColumnModel("Spalte");
    pageModel.addColumn(colModel);
    for (int i=0; i<programs.length; i++) {
      colModel.addProgram(programs[i]);
    }



    return pageModel;
  }


  public static PrintJob createPrintJob(DayProgramPrinterSettings settings, PageFormat pageFormat) {

    /* We create the print job in two steps:
       First we create page models where each page consists of a number of columns.
       A page model is not a real page.
       Then we have to split each page model in one or more real pages.
    */
    PageModel[] pages = createPages(settings);
    PrintJob job = new DayProgramPrintJob(pages, settings, pageFormat);
    return job;
  }

  private static PageModel[] createPages(DayProgramPrinterSettings settings) {

    ArrayList pageModelList = new ArrayList();
    int dayCount = settings.getNumberOfDays();
    Date startDate = settings.getFromDay();
    int dayStartHour = settings.getDayStartHour();
    int dayEndHour = settings.getDayEndHour();

    Channel[] channelArr = settings.getChannelList();
    if (channelArr == null) {
      channelArr = Plugin.getPluginManager().getSubscribedChannels();
    }
    for (int dateInx=0;dateInx<dayCount;dateInx++) {
      Date date=startDate.addDays(dateInx);
      DefaultPageModel pageModel = new DefaultPageModel(date.getLongDateString());
      pageModelList.add(pageModel);
      for (int chInx=0;chInx<channelArr.length;chInx++) {
        ArrayList progList = new ArrayList();
        addProgramToList(progList, date, channelArr[chInx], dayStartHour, dayEndHour);
        Program[] progArr=new Program[progList.size()];
        progList.toArray(progArr);

        if (progArr.length>0) {
          pageModel.addColumn(new DefaultColumnModel(channelArr[chInx].getName(), progArr));
        }
      }
    }


    PageModel[] pageModelArr = new PageModel[pageModelList.size()];
    pageModelList.toArray(pageModelArr);

    return pageModelArr;

  }



  private static void addProgramToList(ArrayList progList, Date date, Channel channel, int startHour, int endHour) {
    // add programs of the current day
    Iterator it = Plugin.getPluginManager().getChannelDayProgram(date,channel);
    if (it!=null) {
      while (it.hasNext()) {
        Program prog = (Program)it.next();
        if (prog.getHours()>=startHour && prog.getHours()<endHour) {
          progList.add(prog);
        }
      }
    }

    // add programs of the next day
    if (endHour>=24) {
      it = Plugin.getPluginManager().getChannelDayProgram(date.addDays(1),channel);
      if (it!=null) {
        while (it.hasNext()) {
          Program prog = (Program)it.next();
          if (prog.getHours() < endHour-24) {
            progList.add(prog);
          }
        }
      }
    }
  }

  private static PrintJob createEmptyJob() {
    return new PrintJob() {
      public Printable getPrintable() {
        return new Printable(){
          public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)  {
            return NO_SUCH_PAGE;
          }
        };
      }
      public int getNumOfPages() {
        return 0;
      }
    };
  }

}
