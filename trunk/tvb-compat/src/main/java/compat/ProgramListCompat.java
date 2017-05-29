package compat;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;

import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.Program;
import devplugin.Version;
import tvbrowser.TVBrowser;
import util.ui.Localizer;
import util.ui.ProgramList;

public final class ProgramListCompat {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramListCompat.class);
  
  public static void addMouseAndKeyListeners(final ProgramList list, final ContextMenuIf caller) {
    if(TVBrowser.VERSION.compareTo(new Version(3,31)) >= 0) {
      try {
        Method addMouseAndKeyListeners = ProgramList.class.getDeclaredMethod("addMouseAndKeyListeners", ContextMenuIf.class);
        addMouseAndKeyListeners.invoke(list, caller);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      list.addMouseListeners(caller);
    }
  }
  
  public static void addDateSeparators(final ProgramList list) {
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method addDateSeparators = ProgramList.class.getDeclaredMethod("addDateSeparators");
        addDateSeparators.invoke(list);
      } catch (Exception e) {
        // ignore
      }
    }
  }
  
  public static int getNewIndexForOldIndex(final ProgramList list, int index) {
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method getNewIndexForOldIndex = ProgramList.class.getDeclaredMethod("getNewIndexForOldIndex", int.class);
        index = (Integer)getNewIndexForOldIndex.invoke(list, index);
      } catch (Exception e) {
        // ignore
      }
    }
    
    return index;
  }
  
  /**
   * @return The tool tip text for the previous scroll action,
   */
  public static String getPreviousActionTooltip() {
    String result = LOCALIZER.msg("prevTooltip", "Scrolls to previous day from current view position (if there is previous day in the list)");
    
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method getPreviousActionTooltip = ProgramList.class.getDeclaredMethod("getPreviousActionTooltip");
        result = (String)getPreviousActionTooltip.invoke(null);
      } catch (Exception e) {
        // ignore
      }
    }
    
    return result;
  }
  
  /**
   * @return The tool tip text for the next scroll action,
   */
  public static String getNextActionTooltip() {
    String result = LOCALIZER.msg("nextTooltip", "Scrolls to next day from current view position (if there is next day in the list)");
    
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method getNextActionTooltip = ProgramList.class.getDeclaredMethod("getNextActionTooltip");
        result = (String)getNextActionTooltip.invoke(null);
      } catch (Exception e) {
        // ignore
      }
    }
    
    return result;
  }

  
  public static void scrollToPreviousDayIfAvailable(final ProgramList list) {
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method scrollToPreviousDayIfAvailable = ProgramList.class.getDeclaredMethod("scrollToPreviousDayIfAvailable");
        scrollToPreviousDayIfAvailable.invoke(list);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      scrollToPreviousDayIfAvailableCompat(list);
    }
  }
  
  public static void scrollToNextDayIfAvailable(final ProgramList list) {
    if(TVBrowser.VERSION.compareTo(new Version(3,22)) >= 0) {
      try {
        Method scrollToNextDayIfAvailable = ProgramList.class.getDeclaredMethod("scrollToNextDayIfAvailable");
        scrollToNextDayIfAvailable.invoke(list);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      scrollToNextDayIfAvailableCompat(list);
    }
  }
  
  /**
   * Scrolls the list to given date (if date is available)
   * <p>
   * @param date The date to scroll to.
   */
  public static void scrollToNextDateIfAvailable(final ProgramList list, final Date date) {
    if(TVBrowser.VERSION.compareTo(new Version(3,34)) >= 0) {
      try {
        Method scrollToNextDateIfAvailable = ProgramList.class.getDeclaredMethod("scrollToNextDateIfAvailable", Date.class);
        scrollToNextDateIfAvailable.invoke(list, date);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      scrollToNextDateIfAvailableCompat(list, date);
    }
  }
  
  /**
   * Scrolls the list to the first occurrence of the given time from the current view
   * backward if time is smaller than the current views first time, forward if time is
   * bigger than the current views first time. 
   * <p>
   * @param time The time in minutes from midnight to scroll to.
   */
  public static void scrollToTimeFromCurrentViewIfAvailable(final ProgramList list, final int time) {
    if(TVBrowser.VERSION.compareTo(new Version(3,34)) >= 0) {
      try {
        Method scrollToTimeFromCurrentViewIfAvailable = ProgramList.class.getDeclaredMethod("scrollToTimeFromCurrentViewIfAvailable", int.class);
        scrollToTimeFromCurrentViewIfAvailable.invoke(list, time);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      scrollToTimeFromCurrentViewIfAvailableCompat(list, time);
    }
  }
  
  /**
   * Scrolls the list to the first occurrence of the given time from the current view onward (if time is available)
   * <p>
   * @param time The time in minutes from midnight.
   */
  public static void scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable(final ProgramList list, final int time) {
    if(TVBrowser.VERSION.compareTo(new Version(3,34)) >= 0) {
      try {
        Method scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable = ProgramList.class.getDeclaredMethod("scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable", int.class);
        scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable.invoke(list, time);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailableCompat(list, time);
    }
  }
  
  /**
   * Scrolls the list to given date (if date is available)
   * <p>
   * @param date The date to scroll to.
   * @since 3.3.4
   */
  private static void scrollToNextDateIfAvailableCompat(final ProgramList list, final Date date) {
    for(int i = 0; i < list.getModel().getSize(); i++) {
      Object test = list.getModel().getElementAt(i);
      
      if(test instanceof Program && date.compareTo(((Program)test).getDate()) == 0) {
        Point p = list.indexToLocation(i);
        
        if(list.getVisibleRect() != null) {
          list.scrollRectToVisible(new Rectangle(p.x,p.y,1,list.getVisibleRect().height));
          list.repaint();
        }
        return;
      }
    }
  }
  

  /**
   * Scrolls the list to the first occurrence of the given time from the current view
   * backward if time is smaller than the current views first time, forward if time is
   * bigger than the current views first time. 
   * <p>
   * @param time The time in minutes from midnight to scroll to.
   * @since 3.3.4
   */
  private static void scrollToTimeFromCurrentViewIfAvailableCompat(final ProgramList list, final int time) {
    int index = list.locationToIndex(list.getVisibleRect().getLocation());
    
    if(index < list.getModel().getSize() - 1) {
      Object o = list.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = list.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < list.getModel().getSize() - 1) {
        Date current = ((Program)o).getDate();
        
        int i = index + 1;
        
        boolean down = time <= ((Program)o).getStartTime();
        
        if(down) {
          i = index - 1;
        }
        
        Point scrollPoint = null;
        
        if(down && ((Program)o).getStartTime() == time) {
          if(i > 0 && (list.getModel().getElementAt(i) instanceof String) ) {
            scrollPoint = list.indexToLocation(i);
          }
          else {
            scrollPoint = list.indexToLocation(i+1);
          }
        }
        
        while(down ? i >= 0 : i < list.getModel().getSize()) {
          Object test = list.getModel().getElementAt(i);
          
          if(test instanceof Program) {
            Program prog = (Program)test;
            int startTime = prog.getStartTime();
            
            if(prog.getDate().compareTo(current) == 0) {
              if((down ? startTime < time : startTime >= time)) {
                if(scrollPoint == null) {
                  if(i > 0 && (list.getModel().getElementAt(i-1) instanceof String) || startTime > time) {
                    scrollPoint = list.indexToLocation(i - 1);
                  }
                  else {
                    scrollPoint = list.indexToLocation(i);
                  }
                }
                break;
              }
              else if(down && startTime == time) {
                if(i > 0 && list.getModel().getElementAt(i-1) instanceof String) {
                  scrollPoint = list.indexToLocation(i - 1);
                  break;
                }
                else {
                  scrollPoint = list.indexToLocation(i);
                }
              }
            }
            else if(scrollPoint == null) {
              if(down && i < list.getModel().getSize()-1) {
                scrollPoint = list.indexToLocation(i+1);
              }
              else if(!down && i > 0) {
                scrollPoint = list.indexToLocation(i-1);
              }
              else {
                scrollPoint = list.indexToLocation(i);
              }
              
              break;
            }
          }
          else if(test instanceof String && scrollPoint == null) {
            if(down || i == 0) {
              scrollPoint = list.indexToLocation(i);
            }
            else {
              scrollPoint = list.indexToLocation(i-1);
            }
            
            break;
          }
          
          if(down) {
            i--;
          }
          else {
            i++;
          }
        }
        
        if(scrollPoint == null) {
          if(down) {
            if(list.getModel().getSize() > 0) {
              scrollPoint = list.indexToLocation(0);
            }
          }
          else {
            if(list.getModel().getSize() > 0) {
              scrollPoint = list.indexToLocation(list.getModel().getSize()-1);
            }
          }
        }
        
        if(scrollPoint != null && list.getVisibleRect() != null) {
          list.scrollRectToVisible(new Rectangle(scrollPoint.x,scrollPoint.y,1,list.getVisibleRect().height));
          list.repaint();
        }
      }
    }
  }
  
  /**
   * Scrolls the list to the first occurrence of the given time from the current view onward (if time is available)
   * <p>
   * @param time The time in minutes from midnight.
   * @since 3.3.4
   */
  private static void scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailableCompat(final ProgramList list, int time) {
    int index = list.locationToIndex(list.getVisibleRect().getLocation());
    
    if(index < list.getModel().getSize() - 1) {
      Object o = list.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = list.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < list.getModel().getSize() - 1 && ((Program)o).getStartTime() != time) {
        Date current = ((Program)o).getDate();
                
        if(((Program)o).getStartTime() > time) {
          time += 1440;
        }
        
        for(int i = index + 1; i < list.getModel().getSize(); i++) {
          Object test = list.getModel().getElementAt(i);
          
          if(test instanceof Program) {
            Program prog = (Program)test;
            int startTime = prog.getStartTime();
                        
            if(prog.getDate().compareTo(current) > 0) {
              startTime += 1440;
            }
            
            if(prog.getDate().compareTo(current) >= 0 && startTime >= time) {
              Point p = list.indexToLocation(i);
              
              if(i > 0 && (list.getModel().getElementAt(i-1) instanceof String) || startTime > time) {
                p = list.indexToLocation(i - 1);
              }
              
              if(list.getVisibleRect() != null) {
                list.scrollRectToVisible(new Rectangle(p.x,p.y,1,list.getVisibleRect().height));
                list.repaint();
              }
              
              return;
            }
          }
        }            
      }
    }
  }
  
  private static void scrollToPreviousDayIfAvailableCompat(final ProgramList list) {
    int index = list.locationToIndex(list.getVisibleRect().getLocation())-1;
    
    if(index > 0) {
      Object o = list.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = list.getModel().getElementAt(index-1);
        index--;
      }
      
      if(index > 0) {
        Date current = ((Program)o).getDate();
        
        for(int i = index-1; i >= 0; i--) {
          Object test = list.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) > 0) {
            list.ensureIndexIsVisible(i+1);
            return;
          }
        }
      }
    }
    
    if(list.getModel().getSize() > 0) {
      list.ensureIndexIsVisible(0);
    }
  }
  
  private static void scrollToNextDayIfAvailableCompat(final ProgramList list) {
    int index = list.locationToIndex(list.getVisibleRect().getLocation());
    
    if(index < list.getModel().getSize() - 1) {
      Object o = list.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = list.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < list.getModel().getSize() - 1) {
        Date current = ((Program)o).getDate();
        
        for(int i = index + 1; i < list.getModel().getSize(); i++) {
          Object test = list.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) < 0) {
            Point p = list.indexToLocation(i);
            
            if(list.getVisibleRect() != null) {
              list.scrollRectToVisible(new Rectangle(p.x,p.y,1,list.getVisibleRect().height));
            }
            
            return;
          }
        }            
      }
    }
  }
  
  public static boolean isDateSeparatorSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,22,true)) >= 0;
  }
}
