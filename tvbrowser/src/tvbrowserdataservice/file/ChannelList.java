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

import java.awt.Image;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvbrowserdataservice.TvBrowserDataService;
import tvdataservice.TvDataService;
import util.ui.ImageUtilities;

import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ChannelList {

  public static final String FILE_NAME = "channellist.gz";
  
  private ArrayList mChannelList;
  
  private ChannelGroup mGroup;
  
  
  public ChannelList(final String groupName) {
    mChannelList = new ArrayList();
    mGroup=new ChannelGroupImpl(groupName, groupName, "");
  }
  
  public ChannelList(ChannelGroup group) {
    mChannelList = new ArrayList();
    mGroup=group;
  }
  
  
  public void addChannel(Channel channel) {
    mChannelList.add(new ChannelItem(channel, null));
  }
  
  public void addChannel(Channel channel, String iconUrl) {
    mChannelList.add(new ChannelItem(channel, iconUrl));  
  }
  
  
  public int getChannelCount() {
    return mChannelList.size();
  }
  
  
  public Channel getChannelAt(int index) {
    ChannelItem item = (ChannelItem) mChannelList.get(index);
    return item.getChannel();
  }
  
  
  public Channel[] createChannelArray() {
    Channel[] channelArr = new Channel[mChannelList.size()];
    for (int i=0; i<channelArr.length; i++) {
      channelArr[i] = ((ChannelItem)mChannelList.get(i)).getChannel();
    }
    return channelArr;
  }
  
  
  public void readFromStream(InputStream stream, TvDataService dataService)
    throws IOException, FileFormatException
  {
    GZIPInputStream gIn = new GZIPInputStream(stream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(gIn));
    
    String line;
    int lineCount = 1;
    
    File dataDir = TvBrowserDataService.getInstance().getWorkingDirectory();
    IconLoader iconLoader = new IconLoader(mGroup.getId(), dataDir);
    
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        // This is not an empty line -> read it
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() < 4) {
          throw new FileFormatException("Syntax error in mirror file line "
            + lineCount + ": '" + line + "'");
        }

        String country=null, timezone=null, id=null, name=null, copyright=null, webpage=null, iconUrl=null;
        try {
          country = tokenizer.nextToken().trim();
          timezone = tokenizer.nextToken().trim();
          id = tokenizer.nextToken().trim();
          name = tokenizer.nextToken().trim();
          copyright = tokenizer.nextToken().trim();
          webpage = tokenizer.nextToken().trim();
          iconUrl = tokenizer.nextToken();
        }catch (java.util.NoSuchElementException e) {
          // ignore
        }
        
        Channel channel = new Channel(dataService, name, id,
          TimeZone.getTimeZone(timezone), country,copyright,webpage, mGroup);
        if (iconUrl != null) {
          Icon icon = iconLoader.getIcon(id, iconUrl);
          if (icon != null) {
            channel.setIcon(icon);
          }        
        }
        addChannel(channel);
      }
      lineCount++;
    }
    
    gIn.close();
    iconLoader.close();
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
      ChannelItem channelItem = (ChannelItem)mChannelList.get(i);
      Channel channel = channelItem.getChannel();
      writer.println(channel.getCountry()
        + ";" + channel.getTimeZone().getID()
        + ";" + channel.getId()
        + ";" + channel.getName()
        + ";" + channel.getCopyrightNotice()
        + ";" + (channel.getWebpage()==null?"http://tvbrowser.sourceforge.net":channel.getWebpage())
        + ";" + (channelItem.getIconUrl()==null?"":channelItem.getIconUrl()));
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


  
  class IconLoader {
    private File mDir;
    private File mIconDir;
    private File mIconIndexFile;
    private String mGroup;
    private Properties mProperties;
    
    public IconLoader(String group, File dir) throws IOException {
      mDir = dir;
      mGroup = group;
      mIconDir = new File(dir+"/icons_"+mGroup);
      if (!mIconDir.exists()) {
          mIconDir.mkdirs();
      }
      mIconIndexFile = new File(mIconDir,"index.txt");
      mProperties = new Properties();
      if (mIconIndexFile.exists()) {
        mProperties.load(new FileInputStream(mIconIndexFile)); 
      }      
    }
    
    public Icon getIcon(String channelId, String url) throws IOException {
      String key = new StringBuffer("icons_").append(mGroup)
                                             .append("_")
                                             .append(channelId)
                                             .toString();
      String prevUrl = (String)mProperties.get(key);
      Icon icon = null;
      File iconFile = new File(mIconDir,channelId);
      if (url.equals(prevUrl)) {
        //the url hasn't changed; we should have the icon locally 
        icon = getIconFromFile(iconFile);
      }
      if (icon == null) {
        //download the icon
        util.io.IOUtilities.download(new URL(url), iconFile);
        icon = getIconFromFile(iconFile);  
      }
      if (icon != null) {
        mProperties.setProperty(key, url);
      }
      
      return icon;
    }
    
    private Icon getIconFromFile(File file) {
      Image img = ImageUtilities.createImage(file.getAbsolutePath());
      if (img != null) {
        return new ImageIcon(img);
      }
      return null;
    }
    
    private void close() throws IOException {
        mProperties.store(new FileOutputStream(mIconIndexFile), null);
    }
  }
    
  
  class ChannelItem {
    private Channel mChannel;
    private String mIconUrl;
    
    public ChannelItem(Channel ch, String iconUrl) {
      mChannel = ch;
      mIconUrl = iconUrl;
      //mChannel.setIcon(getIcon());
    }
    
    public Channel getChannel() {
      return mChannel;
    }
    
    public String getIconUrl() {
      return mIconUrl;
    }
    
  /*  public Icon getIcon() {
      // TODO: implement me!
      return null;
    }*/
  }
}
