/*
 * DayParser.java
 *
 * Created on March 7, 2005, 11:59 AM
 */

package swedb;

import tvdataservice.*;
import devplugin.*;

import javax.xml.parsers.*;
import java.util.Vector;
import org.xml.sax.Attributes;
import java.io.InputStream;
import java.io.FileInputStream;
import org.xml.sax.SAXParseException;
import java.net.*;
import org.xml.sax.InputSource;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 *
 * @author  pumpkin
 */
public class DayParser extends org.xml.sax.helpers.DefaultHandler{
  
  private MutableChannelDayProgram addTo;
  
  private devplugin.Date start;
  private String desc;
  private String title;
  private String genre;
  private int hour;
  private int min;
  
  private final int STATUS_WAITING = 0;
  private final int STATUS_PROG = 1;
  private final int STATUS_TITLE = 2;
  private final int STATUS_DESC = 3;
  private final int STATUS_GENRE = 4;
  private int state = 0;

  
  /** Creates a new instance of DayParser */
  private DayParser(MutableChannelDayProgram prog) {
    addTo = prog;
  }
  
  public InputSource resolveEntity(String publicId, String systemId){
    System.out.println ("testing");
    byte[] temp = new byte[0];
    //strib...
    return new InputSource (new ByteArrayInputStream (temp));//this.getClass().getClassLoader().getResourceAsStream("xmltv.dtd"));
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attributes){
    switch (state){
      case (STATUS_PROG):{
        if ("title".equals(qName)){
          state = STATUS_TITLE;
          title = "";
        }
        if ("desc".equals(qName)){
          state = STATUS_DESC;
          desc = "";
        }
        if ("category".equals(qName)){
          state = STATUS_GENRE;
          desc = "";
        }
        break;
      }
      case(STATUS_WAITING):{
        if ("programme".equals(qName)){
          try {
            desc = "";
            title = "";
            genre = "";
            String time = attributes.getValue("start");
            String year = time.substring(0,4);
            String month = time.substring(4,6);
            String day = time.substring(6,8);
            String hourString = time.substring(8,10);
            String minString = time.substring(10,12);
            hour = Integer.parseInt(hourString);
            min = Integer.parseInt(minString);
            start = new devplugin.Date (Integer.parseInt (year),Integer.parseInt (month),Integer.parseInt (day));
            state = STATUS_PROG;
          } catch (Exception E){
            System.out.println ("invalid format: "+attributes.getValue("start"));
          }
        }
        break;
      }
    }
  }
  
  
  public void characters(char[] ch, int start, int length){
    switch (state){
      case (STATUS_TITLE):{
        title = title + new String (ch,start,length);
        break;
      }
      case (STATUS_DESC):{
        desc = desc + new String (ch,start,length);
        break;
      }
      case (STATUS_GENRE):{
        genre = genre + new String (ch,start,length);
        break;
      }
    }
  }
  
  public void endElement(String uri, String localName, String qName){
    switch (state){
      case (STATUS_TITLE):{
        if ("title".equals(qName)){
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_DESC):{
        if ("desc".equals(qName)){
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_GENRE):{
        if ("category".equals(qName)){
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_PROG):{
        if ("programme".equals(qName)){
          state = STATUS_WAITING;
          MutableProgram prog = new MutableProgram (addTo.getChannel(),start,hour,min);
          prog.setTitle (title);
          prog.setDescription (desc);
          
          prog.setTextField(ProgramFieldType.GENRE_TYPE, genre);
          addTo.addProgram(prog);
        }
        break;
      }
    }
  }
  
  public void fatalError(SAXParseException e){
  }

  public void error(SAXParseException e){
  }

  public void warning(SAXParseException e){
  }
  
  public static MutableChannelDayProgram parse(InputStream in, Channel ch, devplugin.Date day) throws Exception {
    MutableChannelDayProgram prog = new MutableChannelDayProgram(day,ch);
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    InputSource input = new InputSource (in);
    input.setSystemId(new File("/").toURL().toString());
    sax.parse(input, new DayParser(prog));
    return prog;
  }
  
}
