/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package tvbrowserdataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * A class that contains news for a channel group.
 * <p>
 * @author René Mach
 */
public class GroupNews implements Comparable<GroupNews> {
  private static final String XML_ELEMENT_ROOT= "news";
  private static final String XML_ATTRIBUTE_DATE= "date";
  
  private static final String XML_ELEMENT_TITLE_DE = "title-de";
  private static final String XML_ELEMENT_TITLE_EN = "title-en";
  
  private static final String XML_ELEMENT_TEXT_DE = "text-de";
  private static final String XML_ELEMENT_TEXT_EN = "text-en";
  
  private static final String XML_ELEMENT_CHANNELS = "channels";
  private static final String XML_ELEMENT_CHANNEL = "channel";
  
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private static final DateFormat VISIBLE_DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  
  
  private String mNewsTitleDe;
  private String mNewsTitleEn;
  
  private String mNewsTextDe;
  private String mNewsTextEn;
  
  private String mDate;
  
  private String[] mChannelIds;
  
  public GroupNews() {
    this(DATE_FORMAT.format(new Date()));
  }
  
  public GroupNews(String date) {
    mDate = date;
    mChannelIds = new String[0];
  }
  
  public boolean isValid() {
    return (mDate != null && (mNewsTitleDe != null && mNewsTitleDe.trim().length() > 0) || (mNewsTitleEn != null && mNewsTitleEn.trim().length() > 0) &&
        (mNewsTextDe != null && mNewsTextDe.trim().length() > 0) || (mNewsTextEn != null && mNewsTextEn.trim().length() > 0)); 
  }
  
  public String getTitleDe() {
    return mNewsTitleDe != null ? mNewsTitleDe : "";
  }
  
  public String getTitleEn() {
    return mNewsTitleEn != null ? mNewsTitleEn : "";
  }
  
  public String getTextDe() {
    return mNewsTextDe != null ? mNewsTextDe : "";
  }
  
  public String getTextEn() {
    return mNewsTextEn != null ? mNewsTextEn : "";
  }
  
  public String[] getChannelIds() {
    return mChannelIds;
  }
  
  public Date getDate() {
    Date now = new Date();
    
    if(mDate != null) {
      try {
        now = DATE_FORMAT.parse(mDate);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return now;
  }
  
  public void setTitleDe(String titleDe) {
    mNewsTitleDe = titleDe;
  }
  
  public void setTitleEn(String titleEn) {
    mNewsTitleEn = titleEn;
  }
  
  public void setTextDe(String textDe) {
    mNewsTextDe = textDe;
  }
  
  public void setTextEn(String textEn) {
    mNewsTextEn = textEn;
  }
  
  public void setRestrictedChannelIds(String[] channelIds) {
    mChannelIds = channelIds;
  }
  
  public void writeNews(XMLStreamWriter writer) throws XMLStreamException, UnsupportedEncodingException {
    if(isValid()) {
      writer.writeStartElement(XML_ELEMENT_ROOT);
        writer.writeAttribute(XML_ATTRIBUTE_DATE, DATE_FORMAT.format(getDate()));
      
        if(mNewsTitleEn != null) {
          writer.writeStartElement(XML_ELEMENT_TITLE_EN);
            writer.writeCharacters(URLEncoder.encode(mNewsTitleEn,"UTF-8"));
          writer.writeEndElement();
        }
        if(mNewsTitleDe != null) {
          writer.writeStartElement(XML_ELEMENT_TITLE_DE);
            writer.writeCharacters(URLEncoder.encode(mNewsTitleDe,"UTF-8"));
          writer.writeEndElement();
        }
        if(mNewsTextEn != null) {
          writer.writeStartElement(XML_ELEMENT_TEXT_EN);
            writer.writeCharacters(URLEncoder.encode(mNewsTextEn,"UTF-8"));
          writer.writeEndElement();
        }
        if(mNewsTextDe != null) {
          writer.writeStartElement(XML_ELEMENT_TEXT_DE);
            writer.writeCharacters(URLEncoder.encode(mNewsTextDe,"UTF-8"));
          writer.writeEndElement();
        }
        
        if(mChannelIds != null && mChannelIds.length > 0) {
          writer.writeStartElement(XML_ELEMENT_CHANNELS);
            
            for(String channelId : mChannelIds) {
              writer.writeStartElement(XML_ELEMENT_CHANNEL);
                writer.writeCharacters(channelId);
              writer.writeEndElement();
            }
          
          writer.writeEndElement();
        }
      writer.writeEndElement();
    }
  }

  @Override
  public int compareTo(GroupNews other) {
    return getDate().compareTo(other.getDate());
  }
  
  public boolean isChannelRestricted(String channelId) {
    for(String chId : mChannelIds) {
      if(chId.equals(channelId)) {
        return true;
      }
    }
    
    return false;
  }
  
  @Override
  public String toString() {
    String date = VISIBLE_DATE_FORMAT.format(getDate());
    String newsTitle = mNewsTitleEn;
    
    if(newsTitle == null || (mNewsTitleDe != null && Locale.getDefault().getLanguage().equals("de"))) {
      newsTitle = mNewsTitleDe;
    }
    
    return date + ": " + newsTitle;
  }
  
  public static GroupNews[] loadNews(File source) {
    ArrayList<GroupNews> newsList = new ArrayList<GroupNews>();
    
    if(source.isFile()) {
      GZIPInputStream in = null;
          
      try {
        in = new GZIPInputStream(new FileInputStream(source));
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser  = factory.createXMLStreamReader( in );
        
        String tagName = null;
        GroupNews current = null;
        ArrayList<String> channelRestriction = new ArrayList<String>();
        
        while(parser.hasNext()) {
          switch(parser.getEventType()) {
//            case XMLStreamConstants.START_DOCUMENT: System.out.println( "START_DOCUMENT: " + parser.getVersion() + " " + parser.getEncoding());break;
            case XMLStreamConstants.START_ELEMENT:
            {
              tagName = parser.getLocalName();
              
              if(tagName.equals(GroupNews.XML_ELEMENT_ROOT)) {
                current = new GroupNews(parser.getAttributeValue(null, GroupNews.XML_ATTRIBUTE_DATE));
              }
            }break;
            case XMLStreamConstants.CHARACTERS:
            {
              if(current != null) {
                if(tagName.equals(GroupNews.XML_ELEMENT_TITLE_EN)) {
                  current.setTitleEn(URLDecoder.decode(parser.getText(), "UTF-8"));
                }
                else if(tagName.equals(GroupNews.XML_ELEMENT_TITLE_DE)) {
                  current.setTitleDe(URLDecoder.decode(parser.getText(), "UTF-8"));
                }
                else if(tagName.equals(GroupNews.XML_ELEMENT_TEXT_EN)) {
                  current.setTextEn(URLDecoder.decode(parser.getText(), "UTF-8"));
                }
                else if(tagName.equals(GroupNews.XML_ELEMENT_TEXT_DE)) {
                  current.setTextDe(URLDecoder.decode(parser.getText(), "UTF-8"));
                }
                else if(tagName.equals(GroupNews.XML_ELEMENT_CHANNEL)) {
                  channelRestriction.add(parser.getText());
                }
              }
            }break;
            case XMLStreamConstants.END_ELEMENT:
            {
              if(parser.getLocalName().equals(GroupNews.XML_ELEMENT_ROOT)) {
                if(current != null) {
                  current.setRestrictedChannelIds(channelRestriction.toArray(new String[channelRestriction.size()]));
                  
                  if(current.isValid()) {
                    newsList.add(current);
                  }
                  
                  current = null;
                  channelRestriction.clear();
                }
              }
            }break;
          }
          
          parser.next();
        }
        
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (XMLStreamException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      
    }
    
    return newsList.toArray(new GroupNews[newsList.size()]);
  }
  
  public GroupNews copy() {
    GroupNews copy = new GroupNews(mDate);
    
    copy.mChannelIds = mChannelIds;
    copy.mNewsTextDe = mNewsTextDe;
    copy.mNewsTextEn = mNewsTextEn;
    copy.mNewsTitleDe = mNewsTitleDe;
    copy.mNewsTitleEn = mNewsTitleEn;
    
    return copy;
  }
}
