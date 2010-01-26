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

package primarydatamanager.tvlistingstool;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import primarydatamanager.mirrorupdater.UpdateException;
import primarydatamanager.mirrorupdater.data.DataSource;
import primarydatamanager.mirrorupdater.data.DataTarget;
import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.SummaryFile;
import devplugin.Channel;
import devplugin.Date;




public class HtmlMirrorVisualizer implements MirrorVisualizer {

  private static final Logger mLog
    = Logger.getLogger(HtmlMirrorVisualizer.class.getName());
    
  private static final int DAY_COUNT = 25; 
  private static Date TODAY = Date.getCurrentDate();
  
  private DataSource mSource;
  private DataTarget mTarget;
  private String[] mGroups;
  private PrintStream mOut;
  private ByteArrayOutputStream mBuf;
  
  private static final String STYLE = 
        "p { font-size : 14pt;font-family : Verdana, Arial, Sans-serif; }" +
        ".header_date { font-size : 8pt;  font-family : Verdana, Arial, Sans-serif; white-space:nowrap;  width:100px; }" +
        ".header_channel { font-size : 8pt; font-family : Verdana, Arial, Sans-serif; white-space:nowrap;  background-color : #FFDDDD;  }" +
        ".group_header { font-size : 12pt; font-family : Verdana, Arial, Sans-serif;font-weight: bold;white-space:nowrap;background-color : #FF8080; }" +
        ".group_info { font-size : 8pt; font-family : Verdana, Arial, Sans-serif; white-space:nowrap; text-align : right; }" +
        ".group_summary { font-size : 8pt; font-family : Verdana, Arial, Sans-serif; white-space:nowrap; #background-color : #FF8080; }" +
        " .table_content { font-size : 8pt; font-family : Verdana, Arial, Sans-serif; white-space:nowrap; background-color : #80FF80; }" +
        " .table_empty { font-size : 8pt; font-family : Verdana, Arial, Sans-serif; white-space:nowrap; background-color : #FF8080; }" +
        " .error { font-size : 12pt; font-family : Verdana, Arial, Sans-serif; font-weight: bold; color : #FF0000; white-space:nowrap; }";  
  
  public HtmlMirrorVisualizer(DataSource source, DataTarget target, String[] groups) {
    mSource = source;
    mTarget = target;
    mGroups = groups;
    mBuf = new ByteArrayOutputStream();
    mOut = new PrintStream(mBuf);
  }
  
  private String formatDate(Date date) {
    return date.getDayOfMonth()+". "+date.getMonth();
  }
  
  private void printError(String error) {
    mOut.println("<tr><td class=\"error\" colspan=\""+ (DAY_COUNT+1) + "\">ERROR: " + error + "</td></tr>");
  }
  
  
  public void visualize() {
    
    header();
    
    for (int i=0; i<mGroups.length; i++) {
      byte[] data;
      SummaryFile summary=null;
      ChannelList channelList=null;
      String groupId=null;
      String lastUpdateFileName=null;  
        
      groupId = mGroups[i];
      
      String groupFileName = mGroups[i]+"_info";     
      try {
        data = mSource.loadFile(groupFileName);
      }catch(Exception e) {
        printError("Could not load group file '"+groupFileName+"' for group '"+groupId+"'");
      }
        
      String summaryFileName = mGroups[i]+"_summary.gz";
      try {
        data = mSource.loadFile(summaryFileName);
        summary = new SummaryFile();
        summary.readFromStream(new ByteArrayInputStream(data), null);
      } catch (Exception e) {
        printError("Could not load summary file '"+summaryFileName+"' for group '"+groupId+"'");

      }
      
      String channelFileName = mGroups[i]+"_channellist.gz";
      try {
        data = mSource.loadFile(channelFileName);
        channelList = new ChannelList(mGroups[i]);
        channelList.readFromStream(new ByteArrayInputStream(data), null);
      } catch (Exception e) {
          printError("Could not load channel file '"+channelFileName+"' for group '"+groupId+"'");
      }
      
      try {
        lastUpdateFileName = mGroups[i] + "_lastupdate";
        data = mSource.loadFile(lastUpdateFileName);
      } catch (Exception e) {
        //printError("Could not load lastupdate file '"+lastUpdateFileName+"' for group '"+groupId+"'");

      }
      
      visualize(groupId,summary, channelList);
      
    }    
    footer();
    
    try {
      mTarget.writeFile("index.html",mBuf.toByteArray());
    } catch (UpdateException e) {
        mLog.severe("Could not write 'index.html'");
    }
  }
  
  
  private void visualize(String group, SummaryFile summaryFile, ChannelList channelList) {
    
    if (summaryFile == null || channelList == null) {
      return;
    }
      
    mOut.println("<tr><td class=\"group_header\">"+group.toUpperCase()+"</td><td colspan=\""+DAY_COUNT+"\"></td></tr>");
    
    mOut.println("<tr><td class=\"header_channel\">" +
            "<table width=\"100%\">" +
            "<tr><td class=\"group_info\"><strong># of channels:</strong></td><td class=\"group_info\">"+channelList.getChannelCount()+"</td></tr></table>" +
            "</td><td colspan=\""+DAY_COUNT+"\"></td></tr>");
    
    for (int ch = 0; ch < channelList.getChannelCount(); ch++) {
      Channel channel = channelList.getChannelAt(ch);
      mOut.print  ("<tr><th class=\"header_channel\" align=\"right\">" + channel + "</th>");
        
      for (int i=0; i< DAY_COUNT; i++) {          
        int version = summaryFile.getDayProgramVersion(TODAY.addDays(i), channel.getCountry(), channel.getId(), 0);
        if (version >= 0) {
          mOut.print("<td class=\"table_content\"></td>");     
        }
        else {
          mOut.print("<td class=\"table_empty\"></td>");   
        }
      }
    
      mOut.println("</tr>");
    }
    
    
    mOut.println("<tr><td colspan=\""+(DAY_COUNT + 1) + "\"></td></tr>");
    mOut.println();
  }

  
  protected void header() {
    mOut.println("<html>");
    //mOut.println("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"></head");
    mOut.println("<head><style type=\"text/css\">"+ STYLE + "</style></head>");
    mOut.println("<body>");
    
    mOut.println("<p>This server provides TV listings for the <a href=\"http://www.tvbrowser.org\">TV-Browser</a> project.<br />");
    mOut.println("The data provided here may only be used within TV-Browser. Any other use is illegal!</p>");
    
    mOut.println("<table width=\"1000\">");
    mOut.print("<tr><th width=\"200\"></th>");  
    for (int i=0; i < DAY_COUNT; i++) {
      String month = String.valueOf(TODAY.addDays(i).getMonth());
      String day = String.valueOf(TODAY.addDays(i).getDayOfMonth());
      
      if(month.length() == 1)
        month = "0" + month;
      if(day.length() == 1)
        day = "0" + day;
      
      mOut.print("<th class=\"header_date\">"+TODAY.addDays(i).getYear()+"-<br>"+month+"-"+day+"</th>");
    }    
    mOut.println("</tr>");
    
  }
  
  protected void footer() {
    mOut.println("</table>");    
    mOut.println("</body>");
    mOut.println("</html>");   
  }


	
}