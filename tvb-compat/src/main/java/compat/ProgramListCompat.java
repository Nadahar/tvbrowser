/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;

import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.Program;
import devplugin.Version;
import tvbrowser.TVBrowser;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.ProgramList;

/**
 * A compatibility class for TV-Browser util.ui.ProgramList 
 * 
 * @author René Mach
 * @since 0.2
 */
public final class ProgramListCompat {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramListCompat.class);
  
  /**
   * Add a Mouse-Listener for the Popup-Box
   * 
   * The caller ContextMenuIfs menus are not shown, if you want to have all
   * available menus just use <code>null</code> for caller.
   * 
   * @param list The ProgramList to use.
   * @param caller The ContextMenuIf that called this.
   */
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
  
  /**
   * Adds date separators to this list.
   * This needs to be called every time the list elements are changed.
   * <p>
   * @param list The ProgramList to use.
   * @throws TvBrowserException Thrown if used ListModel is not {@link #javax.swing.DefaultListModel} or a child class of it.
   * @since 3.2.2
   */
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
  
  /**
   * Gets the new index of a row after adding of date separators.
   * <p>
   * @param list The ProgramList to use.
   * @param index The old index of the row.
   * @return The new index or the given index if no separators were added.
   * @since 3.2.2
   */
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

  /**
   * Scrolls the list to previous day from
   * the current view position (if previous
   * day is available)
   * <p>
   * @param list The ProgramList to use.
   */
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
  
  /**
   * Scrolls the list to next day from
   * the current view position (if next
   * day is available)
   * <p>
   * @param list The ProgramList to use.
   */
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
   * @param list The ProgramList to use.
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
   * @param list The ProgramList to use.
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
   * @param list The ProgramList to use.
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
  
  /**
   * Checks if date separators are supported by the used TV-Browser version.
   * 
   * @return <code>true</code> if date separators are supported by the
   * used TV-Browser version, <code>false</code> otherwise.
   */
  public static boolean isDateSeparatorSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,22,true)) >= 0;
  }
}
