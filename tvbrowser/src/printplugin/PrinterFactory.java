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

package printplugin;


import devplugin.Date;

import java.util.*;


import devplugin.*;

public class PrinterFactory {
  
  
  public static Printer createTimeSortedPrinter(
                            ProgressMonitor monitor,
                            PageRenderer pageRenderer,
                            Date startDate,
                            int dayCount,
                            int dayStartHour,
                            int dayEndHour,
                            Channel[] channelArr,
                            ProgramFilter filter) {

    
    TimeSortedPageModel pageModel = new TimeSortedPageModel();
    for (int i=0; i< dayCount; i++) {
      ArrayList list = new ArrayList();
      Date curDate = startDate.addDays(i);
      
      
      for (int chInx=0; chInx<channelArr.length; chInx++) {
       
        Channel channel = channelArr[i];
        addProgramToList(list, curDate, channelArr[chInx], dayStartHour, dayEndHour, filter);
        
      }
      
            
      Program[] prog = new Program[list.size()];
      list.toArray(prog);
      
      Arrays.sort(prog,new Comparator(){

				public int compare(Object obj1, Object obj2) {
          
          Program p1 = (Program)obj1;
          Program p2 = (Program)obj2;
          
          int dateComp = p1.getDate().compareTo(p2.getDate());
          if (dateComp==0) {
            int m1 = p1.getHours()*60 + p1.getMinutes();
            int m2 = p2.getHours()*60 + p2.getMinutes();
            if (m1<m2) {
              return -1;
            }
            else if (m1>m2) {
              return 1;
            }
            else {
              return 0;
            }
          }else{
            return dateComp;
          }
          
          
				}});
      
      pageModel.addDayProgram(curDate, prog);                    
    }
    return new Printer(new PageModel[]{pageModel}, pageRenderer);
  }
 
  public static Printer createDefaultPrinter(
                            ProgressMonitor monitor,
                            PageRenderer pageRenderer,
                            Date startDate,
                            int dayCount,
                            int dayStartHour,
                            int dayEndHour,
                            Channel[] channelArr,
                            ProgramFilter filter
                            ) {
                              
    ArrayList pageModelList = new ArrayList();  
    for (int dateInx=0;dateInx<dayCount;dateInx++) {
      Date date=startDate.addDays(dateInx);
      DefaultPageModel pageModel = new DefaultPageModel(date);
      pageModelList.add(pageModel);
      
      for (int chInx=0;chInx<channelArr.length;chInx++) {
        
        
        ArrayList progList = new ArrayList();
        
        addProgramToList(progList, date, channelArr[chInx], dayStartHour, dayEndHour, filter);
        
        
        
        Program[] progArr=new Program[progList.size()];
        progList.toArray(progArr);
        
        if (progArr.length>0) {
          pageModel.addChannelDayProgram(channelArr[chInx],progArr);
        }
          
      }      
      
    }
    
       
    PageModel[] pageModelArr = new PageModel[pageModelList.size()];
    pageModelList.toArray(pageModelArr);
    
    return new Printer(pageModelArr, pageRenderer);
    
    
  }
  
  private static void addProgramToList(ArrayList progList, Date date, Channel channel, int startHour, int endHour, ProgramFilter filter) {
    // add programs of the current day
    Iterator it = Plugin.getPluginManager().getChannelDayProgram(date,channel);
    if (it!=null) {
      while (it.hasNext()) {
        Program prog = (Program)it.next();
        if (prog.getHours()>=startHour && prog.getHours()<endHour
                && (filter==null || filter.accept(prog))) {               
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
          if (prog.getHours() < endHour-24 && (filter==null || filter.accept(prog))) {
            progList.add(prog);
          }
        }
      }
    }  
  }
  
}