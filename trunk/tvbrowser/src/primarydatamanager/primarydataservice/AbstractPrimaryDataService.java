package primarydatamanager.primarydataservice;

import java.util.Calendar;

abstract public class AbstractPrimaryDataService implements PrimaryDataService {
  
  private java.io.PrintStream mErr;
  private boolean mThereWhereErrors;
  
  public boolean execute(String dir, java.io.PrintStream err) {
    mErr=err;
    mThereWhereErrors=false;
    execute(dir);
    return mThereWhereErrors;
  }
   
  abstract protected void execute(String dir);
  
  protected final void logException(Exception e) {
    e.printStackTrace(mErr);
    mThereWhereErrors=true;
  }
  
  protected final void logMessage(String msg) {
    mErr.println(msg);
    mThereWhereErrors=true;
  }
   
  public static int getCurrentWeekOfYear(int beginOfWeek) {
    Calendar cal = Calendar.getInstance();

    int firstDayOfWeek = cal.getFirstDayOfWeek();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

    int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);

    if (dayOfWeek < beginOfWeek && dayOfWeek >= firstDayOfWeek) {
    }
    else {
      weekOfYear++;
    }
    return weekOfYear;
      
  }
   
  public static int findNumber(String s) {    
    if (s==null) return -1;
    char[] str=s.toCharArray();
    int first=0;
    int last;
    while (first<str.length && !Character.isDigit(str[first])) {
      first++;  
    }
    last=first;
    while (last<str.length && Character.isDigit(str[last])) {
      last++;
    }
    if (first<str.length) {
      String resStr=s.substring(first,last);
      return Integer.parseInt(resStr); 
    }    
    return -1;  
  }
  
}