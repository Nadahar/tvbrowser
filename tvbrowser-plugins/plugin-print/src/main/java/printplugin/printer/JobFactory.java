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
 *     $Date: 2010-05-25 22:34:16 +0200 (Di, 25 Mai 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6647 $
 */

package printplugin.printer;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import printplugin.printer.dayprogramprinter.DayProgramPrintJob;
import printplugin.printer.queueprinter.QueuePrintJob;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.QueuePrinterSettings;
import util.program.ProgramUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;


public class JobFactory {


  public static PrintJob createPrintJob(QueuePrinterSettings settings, PageFormat pageFormat, Program[] programs) {
    if (programs.length == 0) {
      return createEmptyJob(pageFormat);
    }
    PageModel pageModel = createPage(programs);
    PrintJob job = new QueuePrintJob(pageModel, settings, pageFormat);
    return job;
  }

  private static PageModel createPage(Program[] programs) {
    Arrays.sort(programs, ProgramUtilities.getProgramComparator());

    DefaultPageModel pageModel = new DefaultPageModel();
    DefaultColumnModel colModel = new DefaultColumnModel("Spalte");
    pageModel.addColumn(colModel);
    for (Program program : programs) {
      colModel.addProgram(program);
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

    ArrayList<PageModel> pageModelList = new ArrayList<PageModel>();
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
      for (Channel element : channelArr) {
        ArrayList<Program> progList = new ArrayList<Program>();
        addProgramToList(progList, date, element, dayStartHour, dayEndHour, settings.getProgramFilter());
        Program[] progArr=new Program[progList.size()];
        progList.toArray(progArr);

        if (progArr.length>0) {
          pageModel.addColumn(new DefaultColumnModel(element.getName(), progArr));
        }
      }
    }


    PageModel[] pageModelArr = new PageModel[pageModelList.size()];
    pageModelList.toArray(pageModelArr);

    return pageModelArr;

  }



  private static void addProgramToList(ArrayList<Program> progList, Date date, Channel channel, int startHour, int endHour, ProgramFilter filter) {
    for (int dateOffset = -1; dateOffset <= 1; dateOffset++) {
      for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date.addDays(dateOffset),channel); it.hasNext();) {
        Program prog = it.next();
        if (prog.getDate().getNumberOfDaysSince(date) == 0 && prog.getHours()>=startHour && prog.getHours()<endHour && filter.accept(prog)) {
          progList.add(prog);
        }
        else if (prog.getDate().getNumberOfDaysSince(date) == 1 && prog.getHours()>=startHour-24 && prog.getHours()<endHour-24 && filter.accept(prog)) {
          progList.add(prog);
        }
      }
    }
  }

  private static PrintJob createEmptyJob(final PageFormat format) {
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

      public PageFormat getPageFormat() {
        return format;
      }
    };
  }

}
