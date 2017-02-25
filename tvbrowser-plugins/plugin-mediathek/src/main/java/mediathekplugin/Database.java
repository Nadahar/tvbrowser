/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import org.apache.commons.lang3.StringUtils;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;

public class Database {
  /** The logger for this class */
  private static final Logger LOG = Logger.getLogger(Database.class.getName());

  /**
   * pattern for a single line
   */  
  private static final String QUOTEFREE_PATTERN = "[^\"\\\\]*+(?:\\\\.(?:[^\"\\\\])*+)*";
  private static final Pattern ITEM_PATTERN = Pattern.compile(
      "\\s*\"("
      + QUOTEFREE_PATTERN
      + ")\"\\s*:\\s*\\[\\s*\"("
      + QUOTEFREE_PATTERN
      + "(?:\"\\s*,\\s*\""
      + QUOTEFREE_PATTERN
      + ")*)\"\\s*\\]");
  
  /**
   * pattern for data separation
   */
  private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\"\\s*,\\s*\"");

  /**
   * headline for Channel
   */
  private static final String HEAD_CHANNEL = "Sender";
  /**
   * headline for Theme
   */
  private static final String HEAD_THEME = "Thema";
  /**
   * headline for Title
   */
  private static final String HEAD_TITLE = "Titel";
  /**
   * headline for Channel
   */
  private static final String HEAD_DATE = "Datum";
  /**
   * headline for URL
   */
  private static final String HEAD_URL = "Url";
  /**
   * headline for URL
   */
  private static final String HEAD_URL_LOW = "Url Klein";
  /**
   * headline for URL
   */
  private static final String HEAD_URL_HD = "Url HD";
  
  /**
   * columns
   */
  private int mColChannel;
  private int mColTheme;
  private int mColTitle;
  private int mColDate;
  private int mColUrl;
  private int mColUrlLow;
  private int mColUrlHD;
  private int mColMax;
  
  /**
   * association of programs and file lines for each channel group
   */
  private HashMap<String, HashMap<Long, ArrayList<MediathekProgramItem>>> mChannelItems = new HashMap<String, HashMap<Long, ArrayList<MediathekProgramItem>>>();

  /**
   * marker for channel map, if a channel does not exist in TV-Browser
   */
  private static final Object NON_EXISTING_CHANNEL = new Object();
  /**
   * mapping of mediathek channel names to our channels, used for speedup
   */
  private HashMap<String, Object> mNameMapping = new HashMap<String, Object>(20);
  private CRC32 mCRC = new CRC32();
  private String mFileName;
  private MediathekQuality mDefaultQuality;

  public Database(final String fileName, MediathekQuality quality) {
    mFileName = fileName;
    mDefaultQuality = quality;
    readFile();
  }

	private void readFile() {
    int programCount = 0;
    if (StringUtils.isEmpty(mFileName)) {
      String separator = System.getProperty("file.separator");
      mFileName = System.getProperty("user.home") + separator + ".mediathek3" + separator + "filme.json";
    }
    File file = new File(mFileName);
    if (!file.canRead()) {
      return;
    }
    try {
      mChannelItems = new HashMap<String, HashMap<Long, ArrayList<MediathekProgramItem>>>(mChannelItems.size());
      
      String channelName = "";
      String topic = "";
      
      BufferedReader in = new BufferedReader(new FileReader(mFileName));
      String lineEncoded;
      while ((lineEncoded = in.readLine()) != null){
        String line = new String(lineEncoded.getBytes(), "UTF-8");

        Matcher itemMatcher = ITEM_PATTERN.matcher(line);
        if (itemMatcher.find()) {          
          String[] entry = SEPARATOR_PATTERN.split(itemMatcher.group(2));
          
          if (itemMatcher.group(1).equals("Filmliste") && (entry.length > 5)) { //heading
            mColMax = entry.length;
            for(int i=0;i<entry.length;i++){
              if (entry[i].equals(HEAD_CHANNEL)){
                mColChannel = i;
              }
              if (entry[i].equals(HEAD_THEME)){
                mColTheme = i;
              }
              if (entry[i].equals(HEAD_TITLE)){
                mColTitle = i;
              }
              if (entry[i].equals(HEAD_URL)){
                mColUrl = i;
              }
              if (entry[i].equals(HEAD_URL_LOW)){
                mColUrlLow = i;
              }
              if (entry[i].equals(HEAD_URL_HD)){
                mColUrlHD = i;
              }
              if (entry[i].equals(HEAD_DATE)){
                mColDate = i;
              }
            }
          } else { //normal entry          
            if (entry.length<mColMax) continue; //invalid line          
          
            if (!entry[mColChannel].isEmpty()) { //if empty: use last one
              channelName = unifyChannelName(entry[mColChannel].trim());
            }
            Channel channel = findChannel(channelName);
            if (channel != null) {
              HashMap<Long, ArrayList<MediathekProgramItem>> programs = mChannelItems.get(channelName);
              if (programs == null) {
                programs = new HashMap<Long, ArrayList<MediathekProgramItem>>(500);
                mChannelItems.put(channelName, programs);
              }
              if (!entry[mColTheme].isEmpty()){
                topic =  entry[mColTheme].trim();
              }
              long key = getKey(topic);
              // store the URLs byte offset inside the file
              ArrayList<MediathekProgramItem> list = programs.get(key);
              if (list == null) {
                list = new ArrayList<MediathekProgramItem>();
                programs.put(key, list);
              }
              programCount++;
              list.add(parseDatabaseEntry(entry));
            }
          }
        }
      }
      in.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    LOG.info("Found " + programCount + " programs in Mediathek");
  }
	
	private MediathekProgramItem parseDatabaseEntry(String[] entry){
    String itemTitle = entry[mColTitle].trim();
    String itemUrl = entry[mColUrl].trim();
    String itemDate = entry[mColDate].trim();
    
    MediathekQuality quality = MediathekQuality.NORM;
    
    if (mDefaultQuality!=MediathekQuality.NORM){
      String itemUrlAlt = "";
      if (mDefaultQuality==MediathekQuality.HD){
        itemUrlAlt = entry[mColUrlHD].trim();
      } else {
        itemUrlAlt = entry[mColUrlLow].trim();
      }
      String[] urlParts = itemUrlAlt.split("\\|");
      if (urlParts.length>1) {
        int offset = Integer.valueOf(urlParts[0]);
        quality = mDefaultQuality;
        itemUrl = itemUrl.substring(0, offset) + urlParts[1];
      } else if (!urlParts[0].equals("")){
        quality = mDefaultQuality;
        if (urlParts[0].startsWith("/")){ //special case BR
          itemUrl = itemUrl + urlParts[0];
        } else {
          itemUrl = urlParts[0];
        }
      }
      
    }
    
    return new MediathekProgramItem(itemTitle, itemUrl, itemDate, quality);
	}

  private String unifyChannelName(String channelName) {
    if (channelName.equalsIgnoreCase("Ard.Podcast")) {
      channelName = "Ard";
    }
    if (channelName.equalsIgnoreCase("Arte.DE")) {
      channelName = "Arte";
    }
    if (channelName.equalsIgnoreCase("Arte.FR")) {
      channelName = "Arte";
    }
    String result = channelName.toLowerCase();
    return result;
  }

  private long getKey(String topic) {
    mCRC.reset();
    mCRC.update(topic.getBytes());
    return mCRC.getValue();
  }

  private Channel findChannel(final String mediathekChannelName) {
    Object result = mNameMapping.get(mediathekChannelName);
    if (result == NON_EXISTING_CHANNEL) {
      return null;
    }
    if (result == null) {
      Channel[] allChannels = Plugin.getPluginManager().getSubscribedChannels();
      for (Channel channel : allChannels) {
        if (channel.getName().equalsIgnoreCase(mediathekChannelName)
            || StringUtils.startsWithIgnoreCase(channel.getName(), mediathekChannelName + " ")) {
          mNameMapping.put(mediathekChannelName, channel);
          return channel;
        }
        String namePart = StringUtils.substringBetween(channel.getName(), "(", ")");
        if (StringUtils.isNotEmpty(namePart)) {
          mNameMapping.put(mediathekChannelName, channel);
          return channel;
        }
      }
      mNameMapping.put(mediathekChannelName, NON_EXISTING_CHANNEL);
      LOG.info("Ignored Mediathek channel: " + mediathekChannelName);
      return null;
    }
    return (Channel) result;
  }

  public ArrayList<MediathekProgramItem> getMediathekPrograms(final Program program) {
    ArrayList<MediathekProgramItem> result = new ArrayList<MediathekProgramItem>();
    String channelName = unifyChannelName(program.getChannel().getName());
    HashMap<Long, ArrayList<MediathekProgramItem>> programsMap = mChannelItems.get(channelName);
    // search parts in brackets like for ARD
    if (programsMap == null && channelName.contains("(")) {
      String bracketPart = StringUtils.substringBetween(channelName, "(", ")");
      programsMap = mChannelItems.get(bracketPart);
    }
    // search for partial name, if full name is not found
    if (programsMap == null && channelName.contains(" ")) {
      String firstPart = StringUtils.substringBefore(channelName, " ");
      programsMap = mChannelItems.get(firstPart);
    }
    if (programsMap == null) {
      for (Entry<String, HashMap<Long, ArrayList<MediathekProgramItem>>> entry : mChannelItems.entrySet()) {
        if (StringUtils.startsWithIgnoreCase(channelName, entry.getKey())) {
          programsMap = entry.getValue();
          break;
        }
      }
    }
    if (programsMap == null) {
      return result;
    }
    String title = program.getTitle();
    ArrayList<MediathekProgramItem> programs = programsMap.get(getKey(title));
    if (programs == null && title.endsWith(")") && title.contains("(")) {
      String newTitle = StringUtils.substringBeforeLast(title, "(").trim();
      programs = programsMap.get(getKey(newTitle));
    }
    if (programs == null && title.endsWith("...")) {
      String newTitle = title.substring(0, title.length() - 3).trim();
      programs = programsMap.get(getKey(newTitle));
    }
    if (programs == null) {
      return result;
    }
    return programs;
  }
}