package printplugin;


import devplugin.Date;

import java.util.*;


import devplugin.*;

public class PrinterFactory {
  
 
  public static Printer createDefaultPrinter(
                            ProgressMonitor monitor,
                            PageRenderer pageRenderer,
                            Date startDate,
                            int dayCount,
                            int dayStartHour,
                            int dayEndHour,
                            Channel[] channelArr,
                            ProgramFilter filter) {
                              
    ArrayList pageModelList = new ArrayList();  
   // monitor.setMaximum(dayCount);                         
    for (int dateInx=0;dateInx<dayCount;dateInx++) {
      Date date=startDate.addDays(dateInx);
      DefaultPageModel pageModel = new DefaultPageModel(date);
      pageModelList.add(pageModel);
      
      for (int chInx=0;chInx<channelArr.length;chInx++) {
        
        
        ArrayList progList = new ArrayList();
        
        // add programs of the current day
        Iterator it = Plugin.getPluginManager().getChannelDayProgram(date,channelArr[chInx]);
        if (it!=null) {
          while (it.hasNext()) {
            Program prog = (Program)it.next();
            if (prog.getHours()>=dayStartHour && prog.getHours()<dayEndHour
               && (filter==null || filter.accept(prog))) {               
                 progList.add(prog);
            }
          }
        }
        
        // add programs of the next day
        if (dayEndHour>=24) {
         
          it = Plugin.getPluginManager().getChannelDayProgram(date.addDays(1),channelArr[chInx]);
          if (it!=null) {
            while (it.hasNext()) {
              Program prog = (Program)it.next();
              if (prog.getHours() < dayEndHour-24 && (filter==null || filter.accept(prog))) {
                progList.add(prog);
              }
            }
          }
        }
        
        Program[] progArr=new Program[progList.size()];
        progList.toArray(progArr);
        
        if (progArr.length>0) {
          pageModel.addChannelDayProgram(channelArr[chInx],progArr);
          System.out.println("adding programs to pageModel");
        }
          
      }      
      
    }
    
       
    PageModel[] pageModelArr = new PageModel[pageModelList.size()];
    pageModelList.toArray(pageModelArr);
    
    return new Printer(pageModelArr, pageRenderer);
    
    
  }
  
}