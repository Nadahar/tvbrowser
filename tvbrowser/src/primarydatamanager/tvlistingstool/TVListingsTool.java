package primarydatamanager.tvlistingstool;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


import devplugin.Channel;
import devplugin.Date;

import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.FileFormatException;
import tvbrowserdataservice.file.SummaryFile;

public class TVListingsTool {
 
  private String mGroup;
  private String mMirror; 
  private ArrayList mReport;
 
  public TVListingsTool(String mirror, String group) {
    mGroup = group;
    mMirror = mirror;  
    mReport = new ArrayList();  
  }
  
  
  private String getFileContent(String fileName) {
    URL url;
    try {
      url = new URL(mMirror + "/"+fileName);
    } catch (MalformedURLException e) {
      return null;
    }
    InputStream in;
    String result;
    try {
      in = url.openStream();
      BufferedReader reader=new BufferedReader(new InputStreamReader(in));
      result = reader.readLine();
      reader.close();
    }catch(IOException e) {
      return null;
    }
    return result;
  }
  
  public String getMirrorWeight() {
    return getFileContent(mGroup+"_weight");
  }
  
  public String getLastUpdate() {
    return getFileContent(mGroup+"_lastupdate");
  }
  
  private int getLongestChannelName(Channel[] channelArr) {
    int result =0;
    for (int i=0; i<channelArr.length; i++) {
      if (result<channelArr[i].getName().length()) {
        result = channelArr[i].getName().length();
      }
    }
    return result;
  }
  
  public void dumpAvailableDayProgramVersions() throws IOException, FileFormatException {
    URL summaryFileUrl=null;
    URL channelListFileUrl=null;
    try {
      summaryFileUrl = new URL(mMirror + "/" + mGroup + "_summary.gz");
      channelListFileUrl = new URL(mMirror+ "/" +mGroup + "_channellist.gz");
    } catch (MalformedURLException e) {
      System.out.println("invalid url: "+mMirror+" (groupName is "+mGroup+")");
      return;
    }
    
    System.out.println("mirror: "+mMirror+" ("+mGroup+")"+
       "\nweight: "+getMirrorWeight()+
       "; last update: "+getLastUpdate());
    System.out.println("Available TV listings:");
        
    InputStream in = summaryFileUrl.openStream();
    SummaryFile summary = new SummaryFile();
    summary.readFromStream(in);
    in.close();
    
    in = channelListFileUrl.openStream();
    ChannelList channelList=new ChannelList(mGroup);
    channelList.readFromStream(in, null);
    in.close();
    
    Channel[] channelArr = channelList.createChannelArray();
    int colWidth = getLongestChannelName(channelArr);
    for (int i=0; i<channelArr.length; i++) {
      String ch = channelArr[i].getName();
      System.out.print(ch+":");
      boolean warn = false;
      for (int j=0; j<colWidth-ch.length(); j++) System.out.print(" ");
        Date date = Date.getCurrentDate();
        for (int j=0; j<35; j++) {
          if (j%7==0) {
            System.out.print("|");
          }        
          int version = summary.getDayProgramVersion(date.addDays(j), channelArr[i].getCountry(), channelArr[i].getId(), 0);
      
          if (version<0) {
            System.out.print(".");
            if (j<7) {
              warn=true;
            }
          } 
          else if (version<10) System.out.print(version);
          else {
            System.out.print("*");
          } 
        }
        
        
        System.out.println("|");
        if (warn) {
          mReport.add("Missing TV listing(s) for channel "+ch);
        }
      }
    }
  
  private int printReport() {
    for (int i=0; i<mReport.size(); i++) {
      System.out.println(mReport.get(i));
    }
    return mReport.size();
  }
 
  private static void usage() {
    System.out.println("usage: TVListingsViewer [-url mirrorUrl] [-groups groupName:groupName...]");
  }
 
  public static void main(String[] args) throws IOException, FileFormatException {
    
    if (args.length==0) {
      usage();
      System.exit(0);
    }
    
    String groupNameList=null;
    String mirrorUrlName=null;
    boolean quiet=false;
    
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-url")) {
        if ((i + 1) >= args.length) {
          System.out.println("option '-url' without mirror url");
          System.exit(1);
        } else {
          mirrorUrlName = args[i+1];       
        }
      }
      else if (args[i].equalsIgnoreCase("-groups")) {
        if ((i + 1) >= args.length) {
          System.out.println("option '-ggroups' without group name");
          System.exit(1);
        } else {
          groupNameList = args[i+1];
        }
      }
    }
    
    if (mirrorUrlName==null) {
      System.out.println("no mirror url given");
      System.exit(1);
    }
    if (groupNameList==null) {
      System.out.println("no group name given");
      System.exit(1);
    }
    
    String[] groupNames = groupNameList.split(":");
    TVListingsTool[] tools = new TVListingsTool[groupNames.length];
    for (int i=0; i<groupNames.length; i++) {
      tools[i] = new TVListingsTool(mirrorUrlName, groupNames[i]);
      tools[i].dumpAvailableDayProgramVersions();
      System.out.println();
    }
    
    int lines=0;
    for (int i=0; i<tools.length; i++) {
      lines+=tools[i].printReport();
    }
    if (lines==0) {
      System.out.println("\n\nSUCCESS\n");
    }
    else {
      System.out.println("\n\nTHERE WERE ERRORS\n");
    }
    
  }
  
}