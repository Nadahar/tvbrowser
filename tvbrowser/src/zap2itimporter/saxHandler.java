/*
 * saxHandler.java
 *
 * Created on February 23, 2005, 2:49 PM
 */

package zap2itimporter;

import devplugin.ProgramFieldType;
import devplugin.Date;
import tvdataservice.MutableProgram;
import java.util.Vector;
import java.util.Hashtable;
import javax.swing.JOptionPane;


/**
 *
 * @author  pumpkin
 */
public class saxHandler extends org.xml.sax.helpers.DefaultHandler{
  
  
  public static final String WEBSERVICE_URI = "http://datadirect.webservices.zap2it.com/tvlistings/xtvdService";
  private static final String HTTP_METHOD = "POST";
  
  
  private zap2itimporter dataService;
  
  private Hashtable channels = new Hashtable();
  
  private Hashtable progs = new Hashtable();
  
  private Hashtable progToChannel = new Hashtable();
  
  private int status = STATUS_BASE;
  
        /*
         *	startElement: station
         *	attributes: L:id V:10139
         *	startElement: callSign
         *	endElement: callSign
         *	startElement: name
         *	endElement: name
         *	startElement: affiliate
         *	endElement: affiliate
         *	endElement: station
         **/
  
  private static final int STATUS_CHANNELS = 1;
  private static final int STATUS_CONSTRUCT_CHANNEL = 2;
  private static final int STATUS_CONSTRUCT_CHANNEL_CALLSIGN = 3;
  private static final int STATUS_CONSTRUCT_CHANNEL_NAME = 4;
  
  private static final int STATUS_SCHEDULES = 5;
  private static final int STATUS_PROGRAMS = 6;
  private static final int STATUS_CONSTRUCT_PROGRAM = 7;
  
  private static final int STATUS_MESSAGE = 8;
  /*
  private static final int STATUS_CONSTRUCT_PROGRAM_TITLE = 8;
  private static final int STATUS_CONSTRUCT_PROGRAM_DESCRIPTION = 9;
  private static final int STATUS_CONSTRUCT_PROGRAM_SHOWTYPE = 10;
  private static final int STATUS_CONSTRUCT_PROGRAM_SERIES = 11;
  private static final int STATUS_CONSTRUCT_PROGRAM_ORG_AIRTIME = 12;
   **/
  private static final int STATUS_BASE = 0;
  
  
  private Hashtable mapping = new Hashtable();
  private String last_started_element;
  private String tempvalue;
  
  //STATUS_CONSTRUCT_CHANNEL
  private String channelName;
  private String channelID;
  private String channelCallSign;
  
  private String message = "";
  
  
  //STATUS_CONSTRUCT_PROGRAM
  private MutableProgram[] toWorkWith;
  
  
  private Date from;
  private int length;
  private devplugin.ProgressMonitor monitor;
  
  private int counter = 0;
  private int counter2 = 0;
  
  /** Creates a new instance of saxHandler */
  public saxHandler(zap2itimporter service, Date from, int days, devplugin.ProgressMonitor monitor) {
    dataService = service;
    this.monitor = monitor;
    if (monitor != null){
      monitor.setMaximum(4);
    }
    this.from = from;
    length = days;
  }
  
  public Hashtable doWork() throws Exception {
    mapping.put("title",ProgramFieldType.TITLE_TYPE);
    mapping.put("description",ProgramFieldType.SHORT_DESCRIPTION_TYPE);
    mapping.put("showType",ProgramFieldType.GENRE_TYPE);
    mapping.put("series",ProgramFieldType.EPISODE_TYPE);
    mapping.put("originalAirDate",ProgramFieldType.DESCRIPTION_TYPE);
    mapping.put("syndicatedEpisodeNumber",ProgramFieldType.EPISODE_TYPE);
    mapping.put("subtitle",ProgramFieldType.TITLE_TYPE);
    mapping.put("year",ProgramFieldType.PRODUCTION_YEAR_TYPE);
    
    if (monitor != null){
      monitor.setMessage("connection to server");
    }
    javax.xml.parsers.SAXParser parser = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
    parser.parse(this.getInputStream(),this);
    if (message.length()!=0){
      JOptionPane.showMessageDialog(null,message,"Message from zap2it",JOptionPane.INFORMATION_MESSAGE);
    }
    return progToChannel;
  }
  
  private java.io.InputStream getInputStream() throws Exception{
        /*
        System.out.println ("opening file");
        return new FileInputStream ("/home/pumpkin/log2_bakup");
         */
    
    java.net.URL connectTo = new java.net.URL(WEBSERVICE_URI);
    java.net.HttpURLConnection httpConnection = (java.net.HttpURLConnection) connectTo.openConnection();
    if (monitor != null){
      monitor.setMessage("getting nonce");
    }
    httpConnection.setRequestMethod( HTTP_METHOD );
    httpConnection.connect();
    httpConnection.disconnect();
    String auth = httpConnection.getHeaderField( "WWW-Authenticate" );
    
    //String auth = "Digest realm=\"TMSWebServiceRealm\", nonce=\"PDExMDk5MzExNTU2MjkuNjVmZThlM2VjMDU1MjkxZmYyNGU2YWFhMTM2MTkzZTRAcHJpbWV0aW1lPg==\", opaque=\"dafed5f7ba22e232\", algorithm=MD5, qop=\"auth\"";
    //System.out.println (httpConnection.getContentEncoding());
    //System.out.println("auth: "+auth);
    auth = auth.trim();
    if (auth.toLowerCase().startsWith("digest")){
      String data = auth.substring(auth.indexOf(' ')).trim();
      String[] splitted = data.split(",\\s");
      //System.out.println("data: "+data);
      Hashtable props = new Hashtable();
      for (int i =0;i<splitted.length;i++){
        //System.out.println(splitted[i]);
        int breakout = splitted[i].indexOf('=');
        String name = splitted[i].substring(0,breakout).toLowerCase();
        String dat = splitted[i].substring(breakout+1);
        if ((dat.charAt(0)== '"') && (dat.charAt(dat.length()-1)== '"')){
          dat = dat.substring(1,dat.length()-1);
        }
        //System.out.println("name: "+name);
        //System.out.println("dat: "+dat);
        props.put(name,dat);
      }
      String username = dataService.username;
      String password = dataService.password;
      
      String uri = "/tvlistings/xtvdService";
      String nc = "00000001";
      props.put("uri",uri);
      props.put("nc",nc);
      
      String cnonce = encode(Long.toString(System.currentTimeMillis()).getBytes());
      props.put("cnonce",cnonce);
      
      String realm = (String) props.get("realm");
      String nonce = (String) props.get("nonce");
      String qop = (String) props.get("qop");
      String method = (String) props.get("methodname");
      String algorithm = (String) props.get("algorithm");
      props.put("methodname","POST");
      
      java.security.MessageDigest md5Helper = java.security.MessageDigest.getInstance(algorithm);
      String a1 = username+":"+realm+":"+password;
      //System.out.println("a1 \""+a1+"\"");
      String hashA1 = encode(md5Helper.digest(a1.getBytes("ISO-8859-1")));
      //System.out.println("hashA1 \""+hashA1+"\"");
      md5Helper.reset();
      //System.out.println("a2 \""+HTTP_METHOD+":"+uri+"\"");
      String hashA2 = encode(md5Helper.digest((HTTP_METHOD+":"+uri).getBytes("ISO-8859-1")));
      //System.out.println("hashA2 \""+hashA2+"\"");
      md5Helper.reset();
      String add = hashA1+":"+nonce+":"+nc+":"+cnonce+":"+qop+":"+hashA2;
      //System.out.println("add \""+add+"\"");
      add = "\""+encode(md5Helper.digest((add.getBytes("ISO-8859-1"))))+"\"";
      
      //System.out.println("add \""+add+"\"");
      
      
      String response = "Digest "+data+", response="+add
      +", cnonce=\""+cnonce+"\", username=\""+username+"\", nc="+nc+", uri=\""+uri+"\"";
      
      //System.out.println("auth  "+response);
      
      if (monitor != null){
        monitor.setMessage("logging in");
      }
      
      httpConnection = (java.net.HttpURLConnection) connectTo.openConnection();
      
      
      httpConnection.setRequestProperty("Authorization",response);
      httpConnection.setRequestProperty( "Accept-Encoding", "gzip" );
      httpConnection.setRequestProperty( "SOAPAction", "urn:TMSWebServices:xtvdWebService#download" );
      httpConnection.setRequestMethod( HTTP_METHOD );
      httpConnection.setDoInput( true );
      httpConnection.setDoOutput( true );
      httpConnection.setUseCaches( false );
      
      String request =
      "<?xml version='1.0' encoding='UTF-8'?>"+
      "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'\n"+
      "xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'\n"+
      "xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n"+
      "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"+
      "xmlns:SOAP-ENC='http://schemas.xmlsoap.org/soap/encoding/'>\n"+
      "<SOAP-ENV:Body>\n"+
      "<tms:download xmlns:tms='urn:TMSWebServices'>\n"+
      "<startTime xsi:type='tms:dateTime'>";
      
      request += from.getYear()+"-";
      if (from.getMonth() < 10){
        request += "0";
      }
      request += from.getMonth() +"-";
      if (from.getDayOfMonth() < 10){
        request += "0";
      }
      request += from.getDayOfMonth() +"T00:00:00Z";
      //2005-02-23T08:39:11Z
      
      request += "</startTime>\n<endTime xsi:type='tms:dateTime'>";
      
      //2005-02-24T08:39:11Z
      from = from.addDays(length);
      request += from.getYear()+"-";
      if (from.getMonth() < 10){
        request += "0";
      }
      request += from.getMonth() +"-";
      if (from.getDayOfMonth() < 10){
        request += "0";
      }
      request += from.getDayOfMonth() +"T00:00:00Z";
      request += "</endTime>\n</tms:download>\n</SOAP-ENV:Body></SOAP-ENV:Envelope>";
      
      java.io.PrintWriter writer = new java.io.PrintWriter( httpConnection.getOutputStream() );
      writer.println( request );
      writer.flush();
      writer.close();
      
      
      if (monitor != null){
        monitor.setMessage("starting download");
      }
      httpConnection.connect();
      if (httpConnection.getContentEncoding() !=null){
        if (httpConnection.getContentEncoding().toLowerCase().indexOf("gzip")!=-1){
          
          //InputStream in = new GZIPInputStream(httpConnection.getInputStream());
          //FileOutputStream fout = new FileOutputStream ("/home/pumpkin/log2");
          //int temp = in.read();
          //while (temp!=-1){
          //   fout.write (temp);
          // fout.flush();
          //temp = in.read();
          //System.out.print (".");
          //}
          //System.out.print ("*******************************************");
          //fout.flush();
          //fout.flush();
          //fout.close();
          //in.close();
          //return null; //new GZIPInputStream(httpConnection.getInputStream());
          return new java.util.zip.GZIPInputStream(httpConnection.getInputStream());
        }
      }
      return httpConnection.getInputStream();
    }
    return null;
    
  }
  
  private static String encode(byte[] connectTo) {
    java.math.BigInteger big = new java.math.BigInteger(1,connectTo);
    return big.toString(16);
  }
  
  
  
  public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes){
    switch (status){
      case (STATUS_BASE):{
        if ("message".equals(qName)){
          if (monitor != null){
            monitor.setMessage("getting stationslist");
            monitor.setValue(1);
          }
          status = STATUS_MESSAGE;
        }
        if ("stations".equals(qName)){
          if (monitor != null){
            monitor.setMessage("getting stationslist");
            monitor.setValue(1);
          }
          status = STATUS_CHANNELS;
        }
        if (monitor != null){
          monitor.setMessage("getting shedules");
          monitor.setValue(2);
        }
        if ("schedules".equals(qName)){
          status = STATUS_SCHEDULES;
        }
        if (monitor != null){
          monitor.setMessage("getting data");
          monitor.setValue(3);
        }
        if ("programs".equals(qName)){
          status = STATUS_PROGRAMS;
        }
        break;
      }
      case (STATUS_PROGRAMS):{
        if ("program".equals(qName)){
          String programID = null;
          for (int i=0;i<attributes.getLength();i++){
            if ("id".equals(attributes.getQName(i))){
              programID = attributes.getValue(i);
              break;
            }
          }
          if (programID!=null){
            Vector save = (Vector) progs.get(programID);
            
            if (save!=null){
              toWorkWith = (MutableProgram[])save.toArray(new MutableProgram[save.size()]);
              status = STATUS_CONSTRUCT_PROGRAM;
              last_started_element = "";
              tempvalue = null;
                                                        /*
                                                        if ((toWorkWith.getTitle()!=null) && (toWorkWith.getTitle().length()!=0)){
                                                                System.out.println ("*********************program hat schon einen Title ? "+programID);
                                                        }
                                                         */
            } else {
              System.out.println("kein program gefunden für "+programID);
            }
            
          }
        }
        break;
      }
      case (STATUS_CONSTRUCT_PROGRAM):{
        last_started_element = qName;
        tempvalue = null;
        break;
      }
      case (STATUS_SCHEDULES):{
        if ("schedule".equals(qName)){
          //get channel-ID
          String channel = null;
          for (int i=0;i<attributes.getLength();i++){
            if ("station".equals(attributes.getQName(i))){
              channel = attributes.getValue(i);
              break;
            }
          }
          if (channel!=null){
            devplugin.Channel addTo = (devplugin.Channel) this.channels.get(channel);
            if (addTo !=null){
              String date = null;
              String programmID = null;
              long info = -1;
              for (int i=0;i<attributes.getLength();i++){
                if ("program".equals(attributes.getQName(i))){
                  programmID = attributes.getValue(i);
                }
                if ("time".equals(attributes.getQName(i))){
                  date = attributes.getValue(i);
                }
                if ("stereo".equals(attributes.getQName(i))){
                  info = devplugin.Program.INFO_AUDIO_STEREO;
                }
              }
              if ((date!=null) && (programmID!=null)){
                Date day = parseDay(date);
                if (day!=null){
                  try {
                    //2005-02-23T08:39:11Z
                    MutableProgram prog = new MutableProgram(addTo,day,Integer.parseInt(date.substring(11,13)),Integer.parseInt(date.substring(14,16)));
                    Vector save = (Vector)progs.get(programmID);
                    if (save==null){
                      save = new Vector();
                      progs.put(programmID,save);
                    }
                    save.add(prog);
                    
                    counter2++;
                    
                    Vector list = (Vector)progToChannel.get(addTo);
                    if (list==null){
                      list = new Vector();
                      progToChannel.put(addTo,list);
                    }
                    list.add(prog);
                  } catch (Exception E){
                    E.printStackTrace();
                  }
                } else {
                  System.out.println("day-parsing fehlgeschlagen");
                }
              } else {
                System.out.println("keine Zeit gefunden ?");
              }
            } else {
              System.out.println("Schedule ohne channel ?");
            }
          } else {
            System.out.println("shedule ohne channel-string ? " +channel);
          }
        }
        break;
      }
      case (STATUS_CHANNELS):{
        if ("station".equals(qName)){
          status = STATUS_CONSTRUCT_CHANNEL;
          for (int i=0;i<attributes.getLength();i++){
            if ("id".equals(attributes.getQName(i))){
              channelID = attributes.getValue(i);
              if (monitor != null){
                monitor.setMessage("getting channel: "+channelID);
              }
            }
          }
        }
        break;
      }
      case (STATUS_CONSTRUCT_CHANNEL):{
        if ("callSign".equals(qName)){
          status = STATUS_CONSTRUCT_CHANNEL_CALLSIGN;
        }
        if ("name".equals(qName)){
          status = STATUS_CONSTRUCT_CHANNEL_NAME;
        }
        break;
      }
    }
  }
  
  public void characters(char[] ch, int start, int length){
    try {
      switch (status){
        case (STATUS_MESSAGE):{
          message = message + new String (ch,start,length);
          break;
        }
        case (STATUS_CONSTRUCT_CHANNEL_CALLSIGN):{
          channelCallSign = new String(ch,start,length);
          break;
        }
        case (STATUS_CONSTRUCT_CHANNEL_NAME):{
          channelName = new String(ch,start,length);
          break;
        }
        case (STATUS_CONSTRUCT_PROGRAM):{
          System.out.println("STATUS_CONSTRUCT_PROGRAM : "+last_started_element);
          ProgramFieldType field = (ProgramFieldType) mapping.get(last_started_element);
          if (field != null){
            if (field.getFormat() == ProgramFieldType.TEXT_FORMAT) {
              for (int i=0;i<toWorkWith.length;i++){
                String text = toWorkWith[i].getTextField(field );
                if (text == null){
                  text = "";
                }
                text = text + new String(ch,start,length);
                toWorkWith[i].setTextField(field , text);
              }
            } else if (field.getFormat() == ProgramFieldType.INT_FORMAT) {
              if (tempvalue ==null){
                tempvalue ="";
              }
              tempvalue = tempvalue + new String(ch,start,length);
              try {
                int nummer = Integer.parseInt(tempvalue);
                for (int i=0;i<toWorkWith.length;i++){
                  toWorkWith[i].setIntField(field , nummer);
                }
              } catch (Exception E){
              }
            }
          }
          break;
        }
      }
    } catch (Exception E){
      E.printStackTrace();
      
      //System.exit(-1);
    }
  }
  
  public void endElement(String uri,String localName,String qName){
    try {
      switch (status){
        case (STATUS_CONSTRUCT_CHANNEL):{
          if ("station".equals(qName)){
            status = STATUS_CHANNELS;
            System.out.println("found channel: "+this.channelID+" name: "+channelName+" callSign: "+channelCallSign);
            devplugin.Channel ch = new devplugin.Channel(dataService, channelCallSign, channelName, java.util.TimeZone.getDefault(), "us", "(c) zap2it-labs");
            channels.put(channelID,ch);
            channelID = null;
            channelCallSign = null;
            channelName = null;
            status = this.STATUS_CHANNELS;
          }
          break;
        }
        case (STATUS_CONSTRUCT_PROGRAM):{
          if ("program".equals(qName)){
            this.toWorkWith = null;
            status = STATUS_PROGRAMS;
          } else {
            ProgramFieldType field = (ProgramFieldType) mapping.get(last_started_element);
            if (field != null){
              if (field.getFormat() == ProgramFieldType.TEXT_FORMAT) {
                for (int i=0;i<toWorkWith.length;i++){
                  String text = toWorkWith[i].getTextField(field );
                  if (text != null){
                    text += " ";
                    toWorkWith[i].setTextField(field , text);
                  }
                }
              }
            }
          }
          break;
        }
        case (STATUS_CONSTRUCT_CHANNEL_CALLSIGN):{
          if ("callSign".equals(qName)){
            status = STATUS_CONSTRUCT_CHANNEL;
          }
          break;
        }
        case (STATUS_MESSAGE):{
          if ("message".equals(qName)){
            status = STATUS_BASE;
            
          }
        }
        case (STATUS_CONSTRUCT_CHANNEL_NAME):{
          if ("name".equals(qName)){
            status = STATUS_CONSTRUCT_CHANNEL;
          }
          break;
        }
        case (STATUS_CHANNELS):{
          if ("stations".equals(qName)){
            status = STATUS_BASE;
          }
          break;
        }
        case (STATUS_SCHEDULES):{
          if ("schedules".equals(qName)){
            status = STATUS_BASE;
          }
          break;
        }
        case (STATUS_PROGRAMS):{
          if ("programs".equals(qName)){
            status = STATUS_BASE;
          }
          break;
        }
      }
      
    } catch (Exception E){
      E.printStackTrace();
    }
  }
  
  private Date parseDay(String dctime){
    try {
      Integer I1 = new Integer(dctime.substring(0,4));
      Integer I2 = new Integer(dctime.substring(5,7));
      Integer I3 = new Integer(dctime.substring(8,10));
      return new Date(I1.intValue(),I2.intValue(),I3.intValue());
    } catch (Exception E1){
      System.out.println("invalid dctime: "+dctime);
      System.out.flush();
      E1.printStackTrace();
    }
    return null;
  }
}