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
 
 package arddigitaldataservice;
 
 import util.tvdataservice.*;
 import tvdataservice.MutableProgram;
 import devplugin.*;
 import util.exc.TvBrowserException;
 import util.io.IOUtilities;
 
 
 import java.io.*;
 import java.net.URL;
import java.util.Calendar;
import java.util.regex.*;
 import java.util.TimeZone;
 
 public class ArdDigitalDataService extends AbstractTvDataService {
  
  
  private static PluginInfo INFO=new PluginInfo(
    "ARDdigital-DataService",
    "Die Sender von ARDdigital (Eins MuXx, Eins Extra, Eins Festival)",
    "Martin Oberhauser",
    new Version(0,9)
    );
  
  
  
  
  /**
     * Gets the default list of the channels that are available by this data
     * service.
     */
    protected  Channel[] getDefaultAvailableChannels() {
      TimeZone zone=TimeZone.getTimeZone("GMT+1"); 
      
      return new Channel[] {
        new Channel(this,"Eins MuXx","muxx",zone),
        new Channel(this,"Eins Extra","extra",zone),
        new Channel(this,"Eins Festival","festival",zone)
      };      
    }



    /**
     * Gets the name of the directory where to download the data service specific
     * files.
     */
    protected  String getDataDirectory() {
      return "arddigitaldata";
    }



    /**
     * Gets the name of the file that contains the data of the specified date.
     */
    protected  String getFileNameFor(devplugin.Date date,
    devplugin.Channel channel) {
      
    Calendar cal=date.getCalendar();
    int weekOfYear=cal.get(Calendar.WEEK_OF_YEAR);
    
    if ((cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY) &&
      (cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY)) {
      weekOfYear--;
    }
    
    weekOfYear++;
    
    String s;
    
    String cid=channel.getId();
    if ("muxx".equals(cid) ||
      "extra".equals(cid) ||
      "festival".equals(cid)) { 
        return cid+"-"+weekOfYear+".rtf";       
    }
    else {
      throw new IllegalArgumentException("invalid channel id: "+cid);
    }
    
    
    }



    /**
     * Downloads the file containing the data for the specified dat and channel.
     *
     * @param date The date to load the data for.
     * @param channel The channel to load the data for.
     * @param targetFile The file where to store the file.
     */
    protected  void downloadFileFor(devplugin.Date date, Channel channel,
    File targetFile) throws TvBrowserException {
    
    
    
    String fileName = getFileNameFor(date, channel);
    String url = "http://www.ard-digital.de/programm/download/" + fileName;
    
    try {
      IOUtilities.download(new URL(url), targetFile);
    }
    catch (Exception exc) {
      // ignore error
      //throw new TvBrowserException(getClass(), "error.1",
      //  "Error downloading '{0}' to '{1}'!", url, targetFile.getAbsolutePath(), exc);
    }

  }

 


    /**
     * Parses the specified file.
     *
     * @param file The file to parse.
     * @param date The date to load the data for
     * @param channel The channel to load the data for
     * @param programDispatcher The ProgramDispatcher where to store the found
     *        programs.
     */
    protected  void parseFile(File file, devplugin.Date date,
    Channel channel, ProgramDispatcher programDispatcher)
    throws TvBrowserException {
      
      
    MutableProgram curProgram=null;
    BufferedReader reader;
    
    
    
    
    Pattern datePattern=Pattern.compile("(.*), (\\d+)[.](.*) (\\d+)");
    
    Pattern titlePattern;
    int offset;
    if (channel.getId().equals("festival")) {
      titlePattern=Pattern.compile("(.*)(\\t+)(\\d+)[.](\\d{2})(.*)");
      offset=3;
    }
    else {
      titlePattern=Pattern.compile("^(\\d+)[.](\\d{2})(.*)");
      offset=1;      
    }
    
    FileReader fileReader;
    
    Calendar cal=date.getCalendar();
    Date curDate;
    int prevHour=0;
    int prevMin=0;
    
    int day=cal.get(Calendar.DAY_OF_WEEK);
    if (day!=Calendar.SATURDAY) {
      curDate=date.addDays(-day);
    }else{
      curDate=date;
    }
    
    StringBuffer shortInfo=new StringBuffer();
    
    try {
      fileReader = new FileReader(file);
      reader = new BufferedReader(new RtfFilterReader(fileReader));
      Matcher matcher;
      
      String line;
      line=reader.readLine();
      
      // find date line
      for(;;) {        
        if (line==null) {
          break;
        }
        line=line.trim();
        if (line.length()==0) {
          line=reader.readLine();
          continue;
        }
        matcher=datePattern.matcher(line);
        if (matcher.find()) {
          break;
        }
        line=reader.readLine();
      }
      
      
      while (line!=null) {
        line=reader.readLine();
        if (line==null) {
          break;
        }
        line=line.trim(); 
        if (line.equals("")) {
          continue; 
        }
      
        matcher=titlePattern.matcher(line);
        if (matcher.find()) {
          int h=Integer.parseInt(matcher.group(offset));
          int min=Integer.parseInt(matcher.group(offset+1));
          int length=(h*60+min) - (prevHour*60+prevMin);
          if (length<0) {
            length+=1440;
            curDate=curDate.addDays(1);  // next day
          }
         // if (h<prevHour) { // next day
         //   curDate=curDate.addDays(1);
         // }
          prevHour=h;
          if (curProgram!=null && shortInfo.length()>0) {
            curProgram.setShortInfo(shortInfo.toString());
            curProgram.setDescription(shortInfo.toString());
            shortInfo.setLength(0);
          }
          curProgram=new MutableProgram(channel,curDate, h, min);
          curProgram.setTitle(matcher.group(offset+2).trim());
          curProgram.setShortInfo("");
          curProgram.setDescription("");
          curProgram.setLength(length);
          programDispatcher.dispatch(curProgram);
        }else{
          matcher=datePattern.matcher(line);
          if (!matcher.find()) {
            shortInfo.append(line+"\n");
          }
        }
      
       
        
      }
        
      
    }catch(IOException e) {
      // ignore 
      
    }  
     
    }


  public devplugin.PluginInfo getInfo() {
    return INFO;
  }

  
 }