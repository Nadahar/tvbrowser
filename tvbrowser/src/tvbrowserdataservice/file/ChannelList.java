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
package tvbrowserdataservice.file;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tvdataservice.TvDataService;

import devplugin.Channel;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ChannelList {

  public static final String FILE_NAME = "channellist.gz";
  
  private ArrayList mChannelList;
  
  
  public ChannelList() {
    mChannelList = new ArrayList();
  }
  
  
  public void addChannel(Channel channel) {
    mChannelList.add(channel);
  }
  
  
  public int getChannelCount() {
    return mChannelList.size();
  }
  
  
  public Channel getChannelAt(int index) {
    return (Channel) mChannelList.get(index);
  }
  
  
  public Channel[] createChannelArray() {
    Channel[] channelArr = new Channel[mChannelList.size()];
    mChannelList.toArray(channelArr);
    return channelArr;
  }
  
  
  public void readFromStream(InputStream stream, TvDataService dataService)
    throws IOException, FileFormatException
  {
    GZIPInputStream gIn = new GZIPInputStream(stream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(gIn));
    
    String line;
    int lineCount = 1;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        // This is not an empty line -> read it
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() < 4) {
          throw new FileFormatException("Syntax error in mirror file line "
            + lineCount + ": '" + line + "'");
        }

        String country = tokenizer.nextToken().trim();
        String timezone = tokenizer.nextToken().trim();
        String id = tokenizer.nextToken().trim();
        String name = tokenizer.nextToken().trim();
        String copyright = tokenizer.nextToken().trim();
        
        Channel channel = new Channel(dataService, name, id,
          TimeZone.getTimeZone(timezone), country,copyright);
          
        addChannel(channel);
      }
      lineCount++;
    }
    
    gIn.close();
  }



  public void readFromFile(File file, TvDataService dataService)
    throws IOException, FileFormatException
  {
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      
      readFromStream(stream, dataService);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    PrintWriter writer = new PrintWriter(gOut);
    for (int i = 0; i < getChannelCount(); i++) {
      Channel channel = getChannelAt(i);
      writer.println(channel.getCountry()
        + ";" + channel.getTimeZone().getID()
        + ";" + channel.getId()
        + ";" + channel.getName()
        + ";" + channel.getCopyrightNotice());
    }
    writer.close();
    
    gOut.close();
  }



  public void writeToFile(File file) throws IOException, FileFormatException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    //       outer block.
    
    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);
        
        writeToStream(stream);
      }
      finally {
        // Close the file in every case
        if (stream != null) {
          try { stream.close(); } catch (IOException exc) {}
        }
      }
    }
    catch (IOException exc) {
      file.delete();
      throw exc;
    }
    catch (FileFormatException exc) {
      file.delete();
      throw new FileFormatException("Writing file failed "
        + file.getAbsolutePath(), exc);
    }
  }

}
