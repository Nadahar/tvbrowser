import primarydatamanager.primarydataservice.AbstractPrimaryDataService;
import devplugin.Channel;
import devplugin.Date;
import primarydatamanager.primarydataservice.PrimaryDataService;
import java.util.TimeZone;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;
import primarydatamanager.primarydataservice.util.html2txt.Html2TxtConverter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import primarydatamanager.primarydataservice.ProgramFrameDispatcher;
import tvbrowserdataservice.file.ProgramFrame;
import tvbrowserdataservice.file.ProgramField;
import devplugin.ProgramFieldType;
import tvbrowserdataservice.file.FileFormatException;

public class SimplePDS extends AbstractPrimaryDataService {

  private static int DAYS_TO_DOWNLOAD=7;
  private static final String CHANNEL_GROUP_NAME="myfirstgroup";
  private Channel[] mChannelList;


  public SimplePDS() {
    TimeZone timeZone = TimeZone.getTimeZone("MET"); // middle european time
    devplugin.ChannelGroup group = new devplugin.ChannelGroupImpl(CHANNEL_GROUP_NAME, null, null);
    mChannelList = new Channel[] {
        new Channel(null, "Kanal12", "kanal12", timeZone, "de", "[copyright notice]",
                    "http://www.tvbrowser.org", group),
        new Channel(null, "Quiz 44", "quiz44", timeZone, "de", "[copyright notice]",
                    "http://www.tvbrowser.org", group),
        new Channel(null, "Einkauf 1-2-3", "einkauf", timeZone, "de", "[copyright notice]",
                    "http://www.tvbrowser.org", group),
    };

  }

  protected void execute(String dir) {


    ProgramFrameDispatcher[] dispatcher = new ProgramFrameDispatcher[mChannelList.length];
    for (int i = 0; i<dispatcher.length; i++) {
      dispatcher[i] = new ProgramFrameDispatcher(mChannelList[i]);
    }

    Date date = Date.getCurrentDate();
    for (int d = 0; d < DAYS_TO_DOWNLOAD; d++) {
      for (int ch = 0; ch<mChannelList.length; ch++) {
        try {
          extract(date, mChannelList[ch], dispatcher[ch]);
        }catch(Exception e) {
          logException(e);
        }
      }
      date = date.addDays(1);
    }

    for (int i=0; i<dispatcher.length; i++) {
      try {
        dispatcher[i].store(dir);
      }
      catch (IOException e) {
        logException(e);
      }
      catch (FileFormatException e) {
        logException(e);
      }
    }

  }


  private void extract(Date date, Channel channel, ProgramFrameDispatcher dispatcher) throws MalformedURLException,
      IOException {

    StringBuffer buf = new StringBuffer("http://www.tvbrowser.org/pdsstarterkit/");
    buf.append("pgv.php?day=")
       .append(date.getDayOfMonth())
       .append("&month=")
       .append(date.getMonth())
       .append("&year=")
       .append(date.getYear())
       .append("&channel=")
       .append(channel.getId());
   System.out.println(buf);
    URL url = new URL(buf.toString());
    
    HttpURLConnection con=(HttpURLConnection)url.openConnection();
    InputStream in=con.getInputStream();

    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
    
    Matcher matcher;
    Pattern pattern = Pattern.compile("<tr><td>(\\d+):(\\d+)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td></tr>");
    
    
    String line = reader.readLine();
    while (line != null) {
      
      matcher = pattern.matcher(line);
      if (matcher.find()) {
        // System.out.println("h: "+matcher.group(1)+"; m: "+matcher.group(2)+"; title: "+matcher.group(3)+"; short: "+matcher.group(4)+"; desc: "+matcher.group(5));
        int h = Integer.parseInt(matcher.group(1));
        int m = Integer.parseInt(matcher.group(2));
        String title = matcher.group(3);
        String shortInfo = matcher.group(4);
        String description = matcher.group(5);
        
        ProgramFrame frame = new ProgramFrame();
        frame.addProgramField(ProgramField.create(ProgramFieldType.START_TIME_TYPE, h*60+m));
        frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, title));
        frame.addProgramField(ProgramField.create(ProgramFieldType.SHORT_DESCRIPTION_TYPE, shortInfo));
        frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, description));
        dispatcher.dispatchProgramFrame(frame, date);
        
      }
      line = reader.readLine();
    }
    

  }

  public Channel[] getAvailableChannels() {
    return mChannelList;
  }


  public int getReadBytesCount() {
    return 0;
  }

  // for testing only
  public static void main(String[] args) {
    PrimaryDataService pds=new SimplePDS();
    pds.execute("temp",System.out);
    
  }

}
