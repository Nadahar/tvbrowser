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
 
 package wdrdataservice;
 
 import util.tvdataservice.*;
 import tvdataservice.MutableProgram;
 import devplugin.*;
 import util.exc.TvBrowserException;
 import util.io.IOUtilities;
 
 import java.io.*;
 import java.net.URL;
import java.util.Calendar;
import java.util.regex.*;
 
 public class WdrDataService extends AbstractTvDataService {
 	
	
	 public String getName() {
	 	return "WDR";
	 }

	public devplugin.Version getVersion() {
		return new devplugin.Version(1,1);
	  }
	
	
	/**
	   * Gets the default list of the channels that are available by this data
	   * service.
	   */
	  protected  Channel[] getDefaultAvailableChannels() {
	  	Channel[] list=new Channel[7];
	  	list[0]=new Channel(this,"1live");
	  	list[1]=new Channel(this,"WDR2");
		list[2]=new Channel(this,"WDR3");
		list[3]=new Channel(this,"WDR4");
		list[4]=new Channel(this,"WDR5");
		list[5]=new Channel(this,"Funkhaus Europa");
		list[6]=new Channel(this,"WDR");
		return list;	
	  	
	  }



	  /**
	   * Gets the name of the directory where to download the data service specific
	   * files.
	   */
	  protected  String getDataDirectory() {
	  	return "wdrdata";
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
		//System.out.println("returning "+"woche"+weekOfYear+".zip");
		//return "woche"+weekOfYear+".zip";
		String s;
		
		String cid=channel.getId();
		if ("1live".equals(cid) ||
			"wdr2".equals(cid) ||
			"wdr3".equals(cid) ||
			"wdr4".equals(cid) ||
			"radio5".equals(cid) ||
			"europa".equals(cid) ||
			"wdr-fs".equals(cid)) {
				
			return weekOfYear+cid+".txt";
		}
		else {
			throw new IllegalArgumentException("invalid channel id");
		}
		
		/*
		switch (channel.getId()) {
			case 0: s="1live"; break;
			case 1: s="wdr2"; break;
			case 2: s="wdr3"; break;
			case 3: s="wdr4"; break;
			case 4: s="radio5"; break;
			case 5: s="europa"; break;
			case 6: s="wdr-fs"; break;
			default : throw new IllegalArgumentException("invalid channel id");
		}
		
		return weekOfYear+s+".txt";
		*/
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
		String url = "http://www.wdr.de/epg/download/dl/" + fileName;
		try {
			IOUtilities.download(new URL(url), targetFile);
		}
		catch (Exception exc) {
			// ignore error
			//throw new TvBrowserException(getClass(), "error.1",
			//	"Error downloading '{0}' to '{1}'!", url, targetFile.getAbsolutePath(), exc);
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
			// Example: "00:00	Eins Live Freitag Klubbing"	
			Pattern.compile("(\\d+):(\\d+)\t(.*)"),
			// Example: "	Ein Host, ein DJ und die Nacht beginnt"
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
					int daysSince1970 = (int)(cal.getTimeInMillis() / 1000L / 60L / 60L / 24L);
					curDate=new Date(daysSince1970);
					
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
						currProgram.setTitle(matcher.group(3));
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
							desc.append(matcher.group(1));
							if (shortInfo==null) {
								shortInfo=matcher.group(1);
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
		
		}




 	
 }