/*
 * channelParser.java
 *
 * Created on March 5, 2005, 8:15 PM
 */

package swedb;

import javax.xml.parsers.*;
import java.util.Vector;
import org.xml.sax.Attributes;
import java.io.InputStream;
import java.io.FileInputStream;

import java.net.*;

/**
 *
 * @author  pumpkin
 */
public class ChannelParser extends org.xml.sax.helpers.DefaultHandler{
  
  private Vector saveIn;
  private final int STATUS_CONSTRUCTION = 0;
  private final int STATUS_CONSTRUCTION_NAME = 1;
  private final int STATUS_CONSTRUCTION_URL = 2;
  private final int STATUS_WAITING = 3;
  private int state = STATUS_WAITING;
  
  private String name;
  private String url;
  private String id;
  
  /** Creates a new instance of channelParser */
  private ChannelParser(Vector v) {
    saveIn = v;
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attributes){
    switch (state){ 
      case (STATUS_WAITING):{
        if ("channel".equals(qName)){
          state = STATUS_CONSTRUCTION;
          id = attributes.getValue("id");
          name = "";
          url = "";
        }
        break;
      }
      case (STATUS_CONSTRUCTION):{
        if ("display-name".equals(qName)){
          state = STATUS_CONSTRUCTION_NAME;
        }
        if ("base-url".equals(qName)){
          state = STATUS_CONSTRUCTION_URL;
        }
        break;
      }
    }
  }
  
  public void characters(char[] ch, int start, int length){
    switch (state){
      case (STATUS_CONSTRUCTION_NAME):{
        name = name + new String(ch,start,length);
        break;
      }
      case (STATUS_CONSTRUCTION_URL):{
        url = url + new String(ch,start,length);
        break;
      }
    }
  }
  
  public void endElement(String uri, String localName, String qName){
    switch (state){
      case (STATUS_CONSTRUCTION_NAME):{
        if ("display-name".equals(qName)){
          state = STATUS_CONSTRUCTION;
        }
        break;
      }
      case (STATUS_CONSTRUCTION_URL):{
        if ("base-url".equals(qName)){
          state = STATUS_CONSTRUCTION;
        }
        break;
      }
      case (STATUS_CONSTRUCTION):{
        if ("channel".equals(qName)){
          if ((id.length()!=0) && (name.length()!=0) && (url.length()!=0)){
            ChannelContainer cc = new ChannelContainer(id,name,url,0);
            saveIn.add(cc);
          }
          state = this.STATUS_WAITING;
        }
        break;
      }
    }
  }
  
  public static ChannelContainer[] parse(InputStream in) throws Exception{
    Vector v = new Vector();
    ChannelParser handler = new ChannelParser(v);
    SAXParserFactory.newInstance().newSAXParser().parse(in,handler);
    return (ChannelContainer[])v.toArray(new ChannelContainer[v.size()]);
  }
  
}
