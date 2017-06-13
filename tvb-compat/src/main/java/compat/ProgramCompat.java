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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ImportanceValue;
import devplugin.Program;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.searchplugin.SearchPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTable;
import tvdataservice.MutableProgram;
import util.program.ProgramUtilities;
import util.settings.StringArrayProperty;
import util.ui.Localizer;
import util.ui.menu.MenuUtil;

/**
 * A compatibility class for TV-Browser devplugin.PluginManager class.
 * 
 * @author René Mach
 * @since 0.2
 */
public final class ProgramCompat {

  private static Localizer LOCALIZER = Localizer.getLocalizerFor(ProgramCompat.class);
  private static final Logger mLog = Logger.getLogger(ProgramCompat.class.getName());
  
  /**
   * Gets the program importance of the given program.
   * <p>
   * @param program The program to get the program importance for
   * @return The program importance from 0 to 10 for the given program.
   */
  public static byte getProgramImportance(final Program program) {
    byte result = -1;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,10,true)) >= 0) {
      try {
        Method m = ProgramUtilities.class.getDeclaredMethod("getProgramImportance", Program.class);
        result = (Byte)m.invoke(null, program);
      }catch(Exception e) {}
    }
    
    if(result == -1) {
      result = getProgramImportanceCompat(program);
    }
    
    return result;
  }
  
  /**
   * Gets the program importance of the given program.
   * <p>
   * @param program The program to get the program importance for
   * @return The program importance from 0 to 10 for the given program.
   * @since 3.1
   */
  private static byte getProgramImportanceCompat(final Program program) {
    if (program.getProgramState() == Program.IS_VALID_STATE &&
        Settings.propProgramPanelAllowTransparency.getBoolean()) {
      int count = 0;
      int addValue = 0;

      PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();

      for(PluginProxy plugin : plugins) {
        ImportanceValue value = plugin.getImportanceValueForProgram(program);

        if(value.getWeight() > 0 && value.getTotalImportance() >= Program.MIN_MARK_PRIORITY) {
          count += value.getWeight();
          addValue += value.getTotalImportance();
        }
      }

      if(count > 0) {
        return (byte)Math.max(addValue/count, Program.MIN_MARK_PRIORITY);
      }
    }

    return Program.MAX_PROGRAM_IMPORTANCE;
  }
  
  /**
   * Gets programs.
   * (Well this is stupid but if programs start at the same
   * time on the same channel on the same date they have the same id.)
   *
   * @param date The date when the programs are shown.
   * @param progID The ID of the programs.
   * @return The programs or <code>null</code> if there are no such programs.
   * @since 3.3.3
   */
  public static Program[] getPrograms(final Date date, final String progID) {
    if(TVBrowser.VERSION.compareTo(new Version(3,33,true)) >= 0) {
      try {
        Method m = PluginManagerImpl.class.getDeclaredMethod("getPrograms", Date.class, String.class);
        return (Program[])m.invoke(PluginManagerImpl.getInstance(), date, progID);
      } catch (Exception e) {
        // ignore
      }
    }
    else {
      return getProgramsCompat(date, progID);
    }
    
    return null;
  }
  
  private static Program[] getProgramsCompat(final Date date, final String progID) {
    Channel ch = getChannelFromProgId(progID);
    
    if(checkChannelAccess(ch)) {
      Iterator<Program> dayProg = PluginManagerImpl.getInstance().getChannelDayProgram(date, ch);
      
      if (dayProg != null) {
        final ArrayList<Program> listProgs = new ArrayList<Program>();
        
        while(dayProg.hasNext()) {
          final Program test = dayProg.next();
          
          if(progID.equals(test.getID())) {
            listProgs.add(test);
          }
        }
        
        if (!listProgs.isEmpty()) {
          return listProgs.toArray(new Program[listProgs.size()]);
        }
        else {
          mLog.warning("could not find program with id '"+progID+"' (date: "+date+")");
        }
      }
      else {
        mLog.warning("day program not found: "+progID+"; "+date);
      }
    }

    return null;
  }
  
  /**
   * Gets programs.
   * (Well this is stupid but if programs start at the same time on
   *  the same channel on the same date they have the same UniqueID.)
   * 
   * @param uniqueID The unique ID ({@link Program#getUniqueID()}) of the programs.
   * @return The programs or <code>null</code> if there are no such programs.
   * @since 3.3.3
   */
  public static Program[] getPrograms(final String uniqueID) {
    if(TVBrowser.VERSION.compareTo(new Version(3,33,true)) >= 0) {
      try {
        Method m = PluginManagerImpl.class.getDeclaredMethod("getPrograms", String.class);
        return (Program[])m.invoke(PluginManagerImpl.getInstance(), uniqueID);
      } catch (Exception e) {
        // ignore
      }
    }
    else  {
      return getProgramsCompat(uniqueID);
    }
    
    return null;
  }
  
  private static Program[] getProgramsCompat(final String uniqueID) {
    Object[] values = getDateAndProgIDforUniqueID(uniqueID);
    
    return getPrograms((Date)values[0], (String)values[1]);
  }
  
  private static boolean checkChannelAccess(Channel ch) {
    if(ch != null) {
      boolean isAccessControl = false;
      
      try {
        Method m = Channel.class.getDeclaredMethod("isAccessControl");
        isAccessControl = (Boolean)m.invoke(ch);
      }catch(Exception e) {
        e.printStackTrace();
      }
      
      if(isAccessControl) {
        StringArrayProperty prop = null;
        
        try {
          Field f = Settings.class.getDeclaredField("propAccessControl");
          prop = (StringArrayProperty)f.get(null);
        }catch(Exception e) {
          e.printStackTrace();
        }
        
        if(prop != null && prop.getStringArray().length > 0) {
          StackTraceElement[] stackTace = Thread.currentThread().getStackTrace();
          String[] values = prop.getStringArray();
          
          for(int i = 0; i < Math.min(5, stackTace.length); i++) {
            for(String testValue : values) {
              String className = stackTace[i].getClassName();
              
              if(className.substring(0, className.indexOf(".")).equals(testValue)) {
                return false;
              }
            }
          }
        }
      }
    }
    
    return true;
  }
  
  private static Channel getChannelFromProgId(String progId) {
    // try to avoid the split operation as it is costly
    int strLen = progId.length();
    int count = 0;
    int lastSeparator = 0;
    for (int i = 0; i < strLen; i++) {
      if (progId.charAt(i) == '_') {
        count++;
        lastSeparator = i;
      }
    }
    if (count == 4) {
      Channel channel = ChannelList.getChannel(progId.substring(0, lastSeparator));
      if (channel != null) {
        return channel;
      }
    }

    String[] s = progId.split("_");
    
    if(s.length < 4) {
      return ChannelList.getChannel(null, null, null, s[0]);
    } else if(s.length == 4) {
      return ChannelList.getChannel(s[0], s[1], null, s[2]);
    } else {
      return ChannelList.getChannel(s[0], s[1], s[2], s[3]);
    }
  }
  
  private static Object[] getDateAndProgIDforUniqueID(String uniqueID) {
    String[] id = uniqueID.split("_");
    Date progDate;
    try {
      java.util.Date date = new SimpleDateFormat(MutableProgram.ID_DATE_FORMAT).parse(id[4]);
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date.getTime());
      progDate = new Date(cal);
    } catch (ParseException e) {
      mLog.severe("Couldn't parse date from unique ID");
      return null;
    }
    
    return new Object[] { progDate,new StringBuilder(id[0]).append('_').append(id[1]).append('_').append(id[2]).append('_').append(id[3]).append('_').append(id[5]).toString()};    
  }
  
  /**
   * Selects the program in the program table and scrolls it to visibility.
   * Since TV-Browser 3.2: Also show program table tab if available.
   * <p> 
   * @param program The program to select.
   */
  public static void selectProgram(final Program program) {
    if(program != null) {
      if(TVBrowser.VERSION.compareTo(new Version(3, 11, true)) >= 0) {
        try {
          Method m = MainFrame.class.getDeclaredMethod("selectProgram", Program.class, boolean.class);
          m.invoke(MainFrame.getInstance(), program, true);
        } catch (Exception e) {
          // ignore
        }
      }
      else {
        selectProgramInternal(program);
      }
      
      if(VersionCompat.isCenterPanelSupported()) {
        try {
          Method m = MainFrame.class.getDeclaredMethod("showProgramTableTabIfAvailable");
          m.invoke(MainFrame.getInstance());
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
  
  private static void selectProgramInternal(Program program) {
    ProgramTable table = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
    table.deSelectItem();
    table.selectProgram(program);
  }
  

  /**
   * Creates a TV-Browser context menu for the given removed program.
   *  
   * @param program The program to create the context menu for.
   * @return The context menu for the given removed program
   */
  public static JPopupMenu createRemovedProgramContextMenu(Program program) {
    JPopupMenu result = null;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,20,true)) >= 0) {
      try {
        Method m = ContextMenuManager.class.getDeclaredMethod("createRemovedProgramContextMenu", Program.class);
        result = (JPopupMenu)m.invoke(ContextMenuManager.getInstance(), program);
      } catch (Exception e) {
        // ignore 
      }
    }
    
    if(result == null) {
      result = createRemovedProgramContextMenuLegacy(program);
    }
    
    return result;
  }
  
  private static JPopupMenu createRemovedProgramContextMenuLegacy(final Program program) {
    JPopupMenu menu = new JPopupMenu();
    
    ActionMenu repetitionSearch = SearchPluginProxy.getInstance().getContextMenuActions(program);
    
    if(repetitionSearch != null) {
      menu.add(MenuUtil.createMenuItem(repetitionSearch));
    }
    
    JMenuItem item = new JMenuItem(LOCALIZER.msg("scrollToPlaceOfProgram","Scroll to last place of program in program table"));
    item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().goTo(program.getDate());
          MainFrame.getInstance().showChannel(program.getChannel());

          try {
            Method m = MainFrame.class.getMethod("scrollToTime", int.class, boolean.class);
            m.invoke(MainFrame.getInstance(), program.getStartTime(), false);
          }catch(Exception e2) {
            MainFrame.getInstance().scrollToTime(program.getStartTime());  
          }
          
          try {
            Method m = MainFrame.class.getMethod("showProgramTableTabIfAvailable");
            m.invoke(MainFrame.getInstance());
          }catch(Exception e2) {
            // ignore  
          }      
        }
    });
    
    menu.add(item);
    
    return menu;
  }
}
