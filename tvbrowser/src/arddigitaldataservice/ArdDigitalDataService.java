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
    
    System.out.println("downloading "+url);
    
    try {
      IOUtilities.download(new URL(url), targetFile);
    }
    catch (Exception exc) {
      // ignore error
      //throw new TvBrowserException(getClass(), "error.1",
      //  "Error downloading '{0}' to '{1}'!", url, targetFile.getAbsolutePath(), exc);
    }
    System.out.println("done!");
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
    
    
    
    System.out.println("parsing...");
    
    Pattern datePattern=Pattern.compile("(.*), (\\d+).(.*) (\\d+)");
    
    Pattern titlePattern;
    int offset;
    if (channel.getId().equals("festival")) {
      titlePattern=Pattern.compile("(.*)(\\t+)(\\d+).(\\d{2})(.*)");
      offset=3;
    }
    else {
      titlePattern=Pattern.compile("^(\\d+).(\\d{2})(.*)");
      offset=1;      
    }
    
    FileReader fileReader;
    
    Calendar cal=date.getCalendar();
    Date curDate;
    int prevHour=0;
    
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
        //System.out.println("LINE: <"+line+">");
        matcher=datePattern.matcher(line);
        if (matcher.find()) {
          System.out.println("found date line: "+line);
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
          if (h<prevHour) { // next day
            curDate=curDate.addDays(1);
          }
          prevHour=h;
          if (curProgram!=null && shortInfo.length()>0) {
            curProgram.setShortInfo(shortInfo.toString());
            shortInfo.setLength(0);
          }
          curProgram=new MutableProgram(channel,curDate, h, min);
          curProgram.setTitle(matcher.group(offset+2).trim());
          curProgram.setShortInfo("");
          programDispatcher.dispatch(curProgram);
          System.out.println(h+":"+min+"    "+curProgram.getTitle()+" ("+curDate+")");
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
      
      
      /*
    MutableProgram currProgram=null, prevProgram=null;  
    BufferedReader reader;  
    int lineNr=0;
    Date curDate;
    int prevMin=-1;
    int prevHour=-1;
    Program prog;
    
    try {
    
      FileReader fileReader = new FileReader(file);
      reader = new BufferedReader(fileReader);
      
      Pattern[] regexPatternArr = new Pattern[] {
      // Example: "00:00  Eins Live Freitag Klubbing" 
      Pattern.compile("(\\d+):(\\d+)\t(.*)"),
      // Example: " Ein Host, ein DJ und die Nacht beginnt"
      Pattern.compile("\t(.*)"),
      // Example: "Samstag, den 17.05.2003"
      Pattern.compile("(.*) (\\d+).(\\d+).(\\d+)")    
      };
      
      String line=reader.readLine();
      lineNr++;
      // ignore first line
      line=reader.readLine();
      lineNr++;
      
      boolean found=false;
      Matcher matcher=null;
      
      found=false;
      boolean newDateFound=false;
      Matcher newDateMatcher=null;
      while (line!=null && !newDateFound) {
        newDateMatcher=regexPatternArr[2].matcher(line);
        newDateFound=newDateMatcher.find();
        line=reader.readLine();
        lineNr++;
        
      }
      
      while (line!=null) {  
        // we're at the beginning of a day program now
          
          int day=Integer.parseInt(newDateMatcher.group(2));
          int month=Integer.parseInt(newDateMatcher.group(3));
          int year=Integer.parseInt(newDateMatcher.group(4));
          
          Calendar cal=Calendar.getInstance();
          cal.set(Calendar.DAY_OF_MONTH, day);
          cal.set(Calendar.MONTH, month - 1);
          cal.set(Calendar.YEAR, year);
          //int daysSince1970 = (int)(cal.getTimeInMillis() / 1000L / 60L / 60L / 24L);
          //curDate=new Date(daysSince1970);
          curDate=new devplugin.Date(cal);
          
        newDateFound=false;
        while (line!=null && !newDateFound) {
      
          found=false;
          while (line != null && !found) {
            matcher = regexPatternArr[0].matcher(line);
            found=matcher.find();
            line=reader.readLine();
            lineNr++;       
          }
          if (found) {
        
            int hour=Integer.parseInt(matcher.group(1));
            int min=Integer.parseInt(matcher.group(2));
        
            currProgram=new MutableProgram(channel, curDate, hour, min);
            currProgram.setTitle(getString(matcher.group(3)));
            
           
            
            int len;
            if (prevProgram!=null) {
              len=(hour*60+min) - (prevHour*60+prevMin);
              if (len<0) {
                len+=1440;
              }
              prevProgram.setLength(len);           
            }
            prevProgram=currProgram;
            prevHour=hour;
            prevMin=min;
          }
      
          // get description
          StringBuffer desc=new StringBuffer();
          String shortInfo=null;
          while (line!=null && found) { 
            matcher=regexPatternArr[1].matcher(line);
            found=matcher.find();
            if (found) {
              desc.append(getString(matcher.group(1)));
              if (shortInfo==null) {
                shortInfo=getString(matcher.group(1));
              }
              line=reader.readLine();
              lineNr++;
            }           
          }
          if (shortInfo==null) {
            shortInfo="";
          }
          
          if (currProgram!=null) {
            currProgram.setDescription(desc.toString());
            currProgram.setShortInfo(shortInfo);
            programDispatcher.dispatch(currProgram);
            currProgram=null;
          }
      
          boolean newProgFound=false;
          newDateFound=false;
            
          Matcher newProgramMatcher;
          while (line!=null && !newProgFound && !newDateFound) {
            newProgramMatcher=regexPatternArr[0].matcher(line);
            newProgFound=newProgramMatcher.find();
            if (!newProgFound) {
              newDateMatcher=regexPatternArr[2].matcher(line);
              newDateFound=newDateMatcher.find();
            }
            if (!newProgFound && !newDateFound) {
              line=reader.readLine();
              lineNr++;
            }
          }
        }
  } 
      
    }catch (Exception exc) {
        throw new TvBrowserException(getClass(), "error.1",
      "Error parsing wdr tv data file line {0}!\n('{1}')",
      new Integer(lineNr), file.getAbsolutePath(), exc);
    } 
    */
    }


  public devplugin.PluginInfo getInfo() {
    return INFO;
  }

  
 }