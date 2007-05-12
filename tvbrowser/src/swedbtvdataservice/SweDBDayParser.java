/*
 * SweDBDayParser.java
 *
 * Created on March 7, 2005, 11:59 AM
 */

package swedbtvdataservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import devplugin.Channel;
import devplugin.ProgramFieldType;

/**
 *
 * @author  Inforama
 */
public class SweDBDayParser extends org.xml.sax.helpers.DefaultHandler{

//  private MutableChannelDayProgram addTo;

  private static int MAX_SHORT_DESCRIPTION_LENGTH = 150;

  private devplugin.Date start;
  private devplugin.Date end;
  private String desc;
  private String title;
  private String genre;
  private String episode;
  private String actors;
  private String directors;
  private int startHour;
  private int startMin;
  private int endHour;
  private int endMin;
  private Hashtable<String, MutableChannelDayProgram> mDayProgsHashTable;
  private MutableChannelDayProgram mMcdp;
  private Channel mChannel;
  private final static int STATUS_WAITING = 0;
  private final static int STATUS_PROG = 1;
  private final static int STATUS_TITLE = 2;
  private final static int STATUS_DESC = 3;
  private final static int STATUS_GENRE = 4;
  private final static int STATUS_EPISODE = 5;
  private final static int STATUS_ACTORS = 6;
  private final static int STATUS_DIRECTORS = 7;
  
  private int state = 0;
  
  private boolean multipleGenres=false;
  private boolean multipleActors = false;
  private boolean multipleDirectors = false;


  /** Creates a new instance of SweDBDayParser */
  private SweDBDayParser(Channel ch, Hashtable<String, MutableChannelDayProgram> lht) {
    mChannel = ch;
    mDayProgsHashTable=lht;
//    mDayProgsHashTable.clear();
  }

  public InputSource resolveEntity(String publicId, String systemId){
    System.out.println("testing");
    byte[] temp = new byte[0];
    //strib...
    return new InputSource(new ByteArrayInputStream(temp));//this.getClass().getClassLoader().getResourceAsStream("xmltv.dtd"));
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
          if (multipleGenres) {
              genre = genre + ", ";
          } else {
              genre = "";
          }
        }
        if ("episode-num".equals(qName)){
            if("onscreen".equals(attributes.getValue("system"))) {
                state = STATUS_EPISODE;
            }
        }
        if ("actor".equals(qName)){
            state = STATUS_ACTORS;
            if (multipleActors) {
                actors = actors + ", ";
            } else {
                actors = "";
            }
        }
        if ("director".equals(qName)){
            state = STATUS_DIRECTORS;
            if (multipleDirectors){
                directors = directors + ", ";
            }else{
                directors = "";
            }
        }
        break;
      }
      case(STATUS_WAITING):{
        if ("programme".equals(qName)){
          desc = "";
          title = "";
          genre = "";
          episode = "";
          actors = "";
          directors = "";
          
          multipleGenres = false;
          multipleActors = false;
          multipleDirectors = false;
          try {
            String time = attributes.getValue("start");
            String year = time.substring(0,4);
            String month = time.substring(4,6);
            String day = time.substring(6,8);
            String hourString = time.substring(8,10);
            String minString = time.substring(10,12);
            startHour = Integer.parseInt(hourString);
            startMin = Integer.parseInt(minString);
            start = new devplugin.Date(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));
            state = STATUS_PROG;
          } catch (Exception E){
            System.out.println("invalid start time format: "+attributes.getValue("start"));
          }
          try {
            String time = attributes.getValue("stop");
            if (time.length()>0) {
                String year = time.substring(0,4);
                String month = time.substring(4,6);
                String day = time.substring(6,8);
                String hourString = time.substring(8,10);
                String minString = time.substring(10,12);
                endHour = Integer.parseInt(hourString);
                endMin = Integer.parseInt(minString);
            }
          } catch (Exception E){
//            System.out.println("invalid stop time format: "+attributes.getValue("stop"));
          }
        }
        break;
      }
    }
  }


  public void characters(char[] ch, int start, int length){
    switch (state){
      case (STATUS_TITLE):{
        title = title + new String(ch,start,length);
        break;
      }
      case (STATUS_DESC):{
        desc = desc + new String(ch,start,length);
        break;
      }
      case (STATUS_GENRE):{
        genre = genre + new String(ch,start,length);
        break;
      }
      case (STATUS_EPISODE):{
        episode = episode + new String(ch,start,length);
        break;
      }
      case (STATUS_ACTORS):{
        actors = actors + new String(ch,start,length);
        break;
      }
      case (STATUS_DIRECTORS):{
        directors = directors + new String(ch,start,length);
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
          multipleGenres=true;
        }
        break;
      }
      case (STATUS_EPISODE):{
        if ("episode-num".equals(qName)){
            state =STATUS_PROG;
        }
        break;
      }
      case (STATUS_ACTORS):{
        if ("actor".equals(qName)){
            state =STATUS_PROG;
            multipleActors = true;
        }
        break;
      }
      case (STATUS_DIRECTORS):{
        if ("director".equals(qName)){
            state =STATUS_PROG;
            multipleDirectors = true;
        }
        break;
      }
      

      case (STATUS_PROG):{
        if ("programme".equals(qName)){
          state = STATUS_WAITING;
          if (!mDayProgsHashTable.containsKey(start.toString())) {
              mMcdp = new MutableChannelDayProgram(start, mChannel);
              mDayProgsHashTable.put(start.toString(),mMcdp);
          } else {
              mMcdp = mDayProgsHashTable.get(start.toString());
          }
          MutableProgram prog = new MutableProgram(mMcdp.getChannel(),start,startHour,startMin,true);
          
          int progLength = (endHour*60) + endMin - (startHour*60) - startMin;
          //Assumption: If the program length is less than 0, the program spans midnight
          if (progLength<0){
              progLength += 24*60; //adding 24 hours to the length
          }
          // Only allow program length for 12 hours.... This will take care of possible DST problems
          if ((progLength>0) && (progLength < 12*60)) { 
              prog.setLength(progLength);
          }
          prog.setTitle(title);
          prog.setDescription(desc);
          // Since there is no short description available, we have to create one ourselves
          String shortDesc = desc;
          if (shortDesc.length() > MAX_SHORT_DESCRIPTION_LENGTH) {
            int lastSpacePos = shortDesc.lastIndexOf(' ', MAX_SHORT_DESCRIPTION_LENGTH-3);
            if (lastSpacePos == -1) {
              shortDesc = shortDesc.substring(0, MAX_SHORT_DESCRIPTION_LENGTH-3)+"...";
            } else {
              shortDesc = shortDesc.substring(0, lastSpacePos)+"...";
            }
          }

          prog.setShortInfo(shortDesc);

          if (genre.length()>0){
              prog.setTextField(ProgramFieldType.GENRE_TYPE, genre);
          }
          if (actors.length()>0){
              prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, actors);
          }
          if (directors.length()>0){
              prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, directors);
          }
          prog.setTextField(ProgramFieldType.EPISODE_TYPE, episode);
          prog.setProgramLoadingIsComplete();
          mMcdp.addProgram(prog);
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

  public static void parseNew(InputStream in, Channel ch, devplugin.Date day, Hashtable<String, MutableChannelDayProgram> ht) throws Exception {
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    InputSource input = new InputSource(in);
    input.setSystemId(new File("/").toURI().toURL().toString());
    sax.parse(input, new SweDBDayParser(ch,ht));
  }
  
  public static MutableChannelDayProgram[] parse(InputStream in, Channel ch, devplugin.Date day) throws Exception {
//    MutableChannelDayProgram prog = new MutableChannelDayProgram(day,ch);
    Hashtable<String, MutableChannelDayProgram> ht=new Hashtable<String, MutableChannelDayProgram>();
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    InputSource input = new InputSource(in);
    input.setSystemId(new File("/").toURI().toURL().toString());
    sax.parse(input, new SweDBDayParser(ch,ht));
    return ht.values().toArray(new MutableChannelDayProgram[ht.size()]);
  }

}
