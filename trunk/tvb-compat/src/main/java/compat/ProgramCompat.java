package compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Logger;

import devplugin.Channel;
import devplugin.Date;
import devplugin.ImportanceValue;
import devplugin.Program;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvdataservice.MutableProgram;
import util.program.ProgramUtilities;
import util.settings.StringArrayProperty;

public final class ProgramCompat {
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
}
