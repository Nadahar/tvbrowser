package virtualdataservice.virtual;

import java.util.Calendar;

public class HelperMethods {
  
  /**
   * Compare two Calendar object, without the time aspect!
   * @param date the day in focus
   * @param test the day to be tested against
   * @return 1 - if date > test, 0 - if date = test, -1 if date < test
   */
  public static int compareDay(Calendar date, Calendar test){
    if (date.get(Calendar.YEAR)>test.get(Calendar.YEAR)){
      return 1;
    }
    if (date.get(Calendar.YEAR)<test.get(Calendar.YEAR)){
      return -1;
    }
    if (date.get(Calendar.MONTH)>test.get(Calendar.MONTH)){
      return 1;
    }
    if (date.get(Calendar.MONTH)<test.get(Calendar.MONTH)){
      return -1;
    }
    if (date.get(Calendar.DAY_OF_MONTH)>test.get(Calendar.DAY_OF_MONTH)){
      return 1;
    }
    if (date.get(Calendar.DAY_OF_MONTH)<test.get(Calendar.DAY_OF_MONTH)){
      return -1;
    }
    return 0;
  }
  

}