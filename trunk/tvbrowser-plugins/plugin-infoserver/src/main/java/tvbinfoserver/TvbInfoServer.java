package tvbinfoserver;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvdataservice.MarkedProgramsList;
import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;
import util.ui.TimeFormatter;
import util.ui.UiUtilities;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramSearcher;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

public class TvbInfoServer extends Plugin {
  private static final String PORT_KEY = "serverPort";
  private static final Version VERSION = new Version(0,6,0,false);
  
  private ServerSocket mSocket;
  private Thread mServerThread;
  private boolean mServerIsRunning;
  private Properties mSettings;
  
  public static Version getVersion() {
    return VERSION;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(TvbInfoServer.class, "TV-Browser Info Server", "Receives searches and answers with the matching programs.", "René Mach", "GPL");
  }
  
  public void onActivation() {
    mSettings = new Properties();
    try {
      mSocket = new ServerSocket(getPort());
      mServerIsRunning = true;
      startThread();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void loadSettings(Properties prop) {
    mSettings = prop;
  }
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  public int getPort() {
    return Integer.parseInt(mSettings.getProperty(PORT_KEY,"8080"));
  }
  
  public void setPort(int port) {
    mSettings.setProperty(PORT_KEY, String.valueOf(port));
    try {
      mServerIsRunning = false;
      mSocket.close();
      mSocket = new ServerSocket(port);
      mServerIsRunning = true;
      startThread();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("status", "network-transmit-receive", 16);
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JSpinner mPortSelection;
      
      @Override
      public void saveSettings() {
        setPort((Integer)mPortSelection.getValue());
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return createImageIcon("status", "network-transmit-receive", 16);
      }
      
      @Override
      public JPanel createSettingsPanel() {
        PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,3dlu,default","5dlu,default"));
        
        mPortSelection = new JSpinner(new SpinnerNumberModel(getPort(), 1024, 65535, 1));
        
        pb.addLabel("Server port:", CC.xy(2, 2));
        pb.add(mPortSelection, CC.xy(4, 2));
        
        return pb.getPanel();
      }
    };
  }
  
  private void startThread() {
    if(mServerThread == null || !mServerThread.isAlive()) {
      mServerThread = new Thread() {
        public void run() {
          Socket connection = null;
          
          while(mServerIsRunning) {
            try {
              connection = mSocket.accept();
              BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              OutputStream out = new BufferedOutputStream(connection.getOutputStream());
              PrintStream pout = new PrintStream(out);
              
              try {
                if(!connection.getInetAddress().isLoopbackAddress()) {
                  errorReport(pout, connection, "403", "Forbidden",
                      "You don't have permission to access the requested URL.");
                }
                else {
                  // read first line of request (ignore the rest)
                  String request = in.readLine();
                  
                  if (request==null)
                      continue;
                  //log(connection, request);
                  while (true) {
                      String misc = in.readLine();
                      if (misc==null || misc.length()==0)
                          break;
                  }
                  
                  // parse the line
                  if (!request.startsWith("GET") || request.length()<14 ||
                      !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                      // bad request
                      errorReport(pout, connection, "400", "Bad Request", 
                                  "Your browser sent a request that " + 
                                  "this server could not understand.");
                  } else {
                    String req = URLDecoder.decode(request.substring(4, request.length()-9).trim(), "UTF-8");
                    
                    if(req.equals("/search=") || req.equals("/search=running")) {
                      sendRunning(pout);
                    }
                    else if(req.startsWith("/show=") && req.trim().length() > 10) {
                      String id = req.substring(req.indexOf("=")+1);
                      
                      if(id.trim().length() > 5 && id.contains("_")) {
                        final Program p = getPluginManager().getProgram(id);
                        
                        if(p != null) {
                          SwingUtilities.invokeLater(new Runnable() {
                            
                            @Override
                            public void run() {
                              // TODO Auto-generated method stub
                              ProgramInfo.getInstance().showProgramInformation(p);    
                            }
                          });
                        }
                        pout.print("shown\r\n");
                      }
                    }
                    else if(req.startsWith("/search=") && req.trim().length() > 8) {
                      String value = req.substring(req.indexOf("=")+1);
                                            
                      if(value.startsWith("\\")) {
                        if(value.length() > 1) {
                          value = value.substring(1);
                          
                          if(value.startsWith("\\")) {
                            if(value.length() > 1) {
                              value = value.substring(1);
                              
                              int index = value.indexOf(":");
                              
                              int time = 0;
                              
                              if(index != -1) {
                                if(value.length() == 5) {
                                  try {
                                    time = Integer.parseInt(value.substring(0,index)) * 60 + Integer.parseInt(value.substring(index+1));
                                  }catch(NumberFormatException e1) {}
                                }
                              }
                              else if(value.length() == 4) {
                                try {
                                  time = Integer.parseInt(value.substring(0,2)) * 60 + Integer.parseInt(value.substring(2));
                                }catch(NumberFormatException e1) {}
                              }
                              else if(value.length() == 2) {
                                try {
                                  time = Integer.parseInt(value) * 60;
                                }catch(NumberFormatException e1) {}
                              }
                              
                              findProgramInTime(time,pout);
                            }
                          }
                          else {
                            searchChannels(value,pout);
                          }
                        }
                      }
                      else if(value.startsWith("+")) {
                        if(value.length() > 1) {
                          value = value.substring(1);
                          
                          if(value.startsWith("+")) {
                            if(value.length() > 1) {
                              value = value.substring(1);
                              
                              searchForMarkerId(ReminderPluginProxy.getInstance().getId(), value, pout);
                            }
                            else if(value.length() == 1) {
                              searchForMarkerId(ReminderPluginProxy.getInstance().getId(), "", pout);
                            }
                          }
                          else {
                            searchForMarkerId(FavoritesPluginProxy.getInstance().getId(), value, pout);
                          }
                        }
                        else {
                          searchForMarkerId(FavoritesPluginProxy.getInstance().getId(), "", pout);
                        }
                      }
                      else {
                        searchPrograms(value,pout);
                      }
                    }
                    else if(req.startsWith("/searchFavorites=") && req.trim().length() > 17) {
                      String value = req.substring(req.indexOf("=")+1);
                      
                      searchForMarkerId(FavoritesPluginProxy.getInstance().getId(),value,pout);
                    }
                    else if(req.startsWith("/searchReminder=") && req.trim().length() > 16) {
                      String value = req.substring(req.indexOf("=")+1);
                      
                      searchForMarkerId(ReminderPluginProxy.getInstance().getId(),value,pout);
                    }
                    else if(req.startsWith("/searchForMarkerId=") && req.contains("&")) {
                      String value = req.substring(req.indexOf("=")+1);
                      
                      String id = value.substring(0, value.indexOf("&"));
                      value = value.substring(value.indexOf("&")+1);
                      
                      searchForMarkerId(id,value,pout);
                    }
                    else if(req.startsWith("/searchTime=") && req.trim().length() > 13) {
                      String value = req.substring(req.indexOf("=")+1);
                      
                      String id = value.substring(0, value.indexOf("&"));
                      value = value.substring(value.indexOf("&")+1);
                      
                      searchForMarkerId(id,value,pout);
                    }
                    else if(req.startsWith("/channelIcon=") && req.trim().length() > 13) {
                      String id = req.substring(req.indexOf("=")+1);
                      
                      for(Channel channel :getPluginManager().getSubscribedChannels()) {
                        if(channel.getUniqueId().equals(id)) {
                          Icon icon = channel.getIcon();
                          
                          int size = 64;
                          
                          if(icon.getIconHeight() > icon.getIconWidth()) {
                            size = (int)((64 * icon.getIconWidth())/(float)icon.getIconHeight());
                          }
                          
                          icon = UiUtilities.scaleIcon(icon,size);
                          
                          BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
                          image.getGraphics().setColor(Color.white);
                          image.getGraphics().fillRect(0, 0, 64, 64);
                          image.getGraphics().setColor(Color.black);
                          image.getGraphics().drawRect(0, 0, 64, 64);
                          image.getGraphics().setColor(Color.white);
                          icon.paintIcon(null, image.getGraphics(), 64/2 - icon.getIconWidth()/2, 64/2 - icon.getIconHeight()/2);
                          
                          sendImage(image,pout,out);
                          
                          break;
                        }
                      }
                    }
                    else if(req.startsWith("/picture=") && req.trim().length() > 9) {
                      String id = req.substring(req.indexOf("=")+1);
                      int resize = -1;
                      
                      if(id.indexOf("&") != -1) {
                        resize = Integer.parseInt(id.trim().substring(id.indexOf("&")+1));
                        id = id.trim().substring(0,id.indexOf("&"));
                      }
                      
                      Program prog = getPluginManager().getProgram(id);
                      
                      if(prog != null && prog.hasFieldValue(ProgramFieldType.PICTURE_TYPE)) {                    
                        byte[] picture = prog.getBinaryField(ProgramFieldType.PICTURE_TYPE);
                        
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(picture));
                        
                        if(resize != -1) {
                          Icon icon = UiUtilities.scaleIcon(new ImageIcon(image), resize);
                          
                          image = new BufferedImage(icon.getIconWidth(), resize, BufferedImage.TYPE_INT_ARGB);
                          image.getGraphics().setColor(new Color(255,255,255,0));
                          
                          icon.paintIcon(null, image.getGraphics(), resize/2 - icon.getIconWidth()/2, resize/2 - icon.getIconHeight()/2);
                        }
                        
                        sendImage(image,pout,out);
                      }
                    }
                    
                    
                   /// info(pout,connection);
                  }
                }
              }catch(Exception e2) {
                // catch all exceptions to prevent crash of server
              }
              out.flush();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            try {
              if (connection != null) connection.close(); 
            } catch (IOException e) { System.err.println(e); }
          }
        }
      };
      mServerThread.start();
    }
  }
  
  private void searchForMarkerId(String id, String value, PrintStream pout) {
    pout.print("<?xml version=\"1.0\"?>\r\n");
    pout.print("<TvbSearch>\r\n");
    
    Program[] marked = MarkedProgramsList.getInstance().getMarkedPrograms();
    
    for(Program prog : marked) {
      if(!prog.isExpired()) {
        Marker[] markers = prog.getMarkerArr();
        
        for(Marker marker : markers) {
          if(marker.getId().equals(id) && (value.trim().length() == 0 || prog.getTitle().toLowerCase().contains(value.toLowerCase()))) {
            sendItem(prog,pout);
          }
        }
      }
    }
    
    pout.print("</TvbSearch>\r\n");
  }
  
  private void searchProgramsInternal(String value, PrintStream pout) {
    SearchFormSettings settings = new SearchFormSettings(value);
    
    try {
      ProgramSearcher search = settings.createSearcher(value);
      Program[] progs = search.search(new ProgramFieldType[] {ProgramFieldType.TITLE_TYPE}, Date.getCurrentDate(), 1, getPluginManager().getSubscribedChannels(), true, null, null);
      
      for(Program prog : progs) {
        if(!prog.isExpired()) {
          sendItem(prog,pout);
        }
      }
    } catch (TvBrowserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void searchPrograms(String value, PrintStream pout) {
    pout.print("<?xml version=\"1.0\"?>\r\n");
    pout.print("<TvbSearch>\r\n");
    
    searchProgramsInternal(value,pout);
    
    pout.print("</TvbSearch>\r\n");
  }
  
  private void searchChannels(String value, PrintStream pout) {
    pout.print("<?xml version=\"1.0\"?>\r\n");
    pout.print("<TvbSearch>\r\n");
    
    for(Channel ch : getPluginManager().getSubscribedChannels()) {
      if(ch.getName().toLowerCase().contains(value.toLowerCase())) {
        findRunningAndFollowers(pout,ch,2);
      }
    }
    
    pout.print("</TvbSearch>\r\n");
  }
  
  private void findProgramInTime(int startTime, PrintStream pout) {
    pout.print("<?xml version=\"1.0\"?>\r\n");
    pout.print("<TvbSearch>\r\n");
    
    Channel[] channels = getPluginManager().getSubscribedChannels();
    
    for(Channel ch : channels) {
      Iterator<Program> channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate(), ch);
  
      if(channelDayProg == null) {
        channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(-1), ch);
      }
      
      if(channelDayProg != null) {
        while(channelDayProg.hasNext()) {
          Program prog = channelDayProg.next();
          
          if(prog.getStartTime() <= startTime && (prog.getStartTime()+prog.getLength()) > startTime) {
            sendItem(prog, pout);
            break;
          }
        }
      }
    }
    
    pout.print("</TvbSearch>\r\n");
  }
  
  private void findRunningAndFollowers(PrintStream pout, Channel ch, int followers) {
    Iterator<Program> channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate(), ch);

    if(channelDayProg != null) {
      if(channelDayProg.hasNext()) {
        Program p = channelDayProg.next();
        
        // The first program on the day is not expired and not running -> the running program must be on yesterday
        if(!p.isExpired() && !p.isOnAir()) {
          channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(-1), ch);
        }
        else if(p.isOnAir()) {
          sendItem(p, pout);
          
          while(channelDayProg.hasNext() && followers > 0) {
            followers--;
            sendItem(channelDayProg.next(), pout);
          }
          return;
        }
      }
    }
    
    if(channelDayProg == null) {
      channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(-1), ch);
    }
    
    if(channelDayProg != null) {
      while(channelDayProg.hasNext()) {
        Program p = channelDayProg.next();
        
        if(p.isOnAir()) {
          sendItem(p,pout);
          
          while(channelDayProg.hasNext() && followers > 0) {
            followers--;
            sendItem(channelDayProg.next(), pout);
          }
          
          if(followers > 0) {
            channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(1), ch);
            
            while(channelDayProg != null && channelDayProg.hasNext() && followers > 0) {
              followers--;
              sendItem(channelDayProg.next(), pout);
            }
          }
          
          break;
        }
      }
    }
  }
  
  
  private void sendImage(BufferedImage img, PrintStream pout,OutputStream out) {
    try { 
      // send file
      pout.print("HTTP/1.0 200 OK\r\n" +
                 "Content-Type: image/png\r\n" +
                 "Date: " + new java.util.Date() + "\r\n" +
                 "Server: FileServer 1.0\r\n\r\n");

      ImageIO.write(img, "png", out);
      //sendFile(file, out); // send raw file 
      //log(connection, "200 OK");
  } catch (IOException e) { e.printStackTrace();
      // file not found
      /*errorReport(pout, connection, "404", "Not Found",
                  "The requested URL was not found on this server.");*/
  }
  }
  
  
  private void sendRunning(PrintStream pout) {
    pout.print("<?xml version=\"1.0\"?>\r\n");
    pout.print("<TvbSearch>\r\n");
    
    Channel[] channels = getPluginManager().getSubscribedChannels();
    
    for(Channel channel : channels) {
      findRunningAndFollowers(pout, channel, 0);
      /*Iterator<Program> channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate(), channel);

      if(channelDayProg != null) {
        if(channelDayProg.hasNext()) {
          Program p = channelDayProg.next();
          
          // The first program on the day is not expired and not running -> the running program must be on yesterday
          if(!p.isExpired() && !p.isOnAir()) {
            channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(-1), channel);
          }
        }
      }
      
      if(channelDayProg == null) {
        channelDayProg = getPluginManager().getChannelDayProgram(Date.getCurrentDate().addDays(-1), channel);
      }
      
      if(channelDayProg != null) {
        
        while(channelDayProg.hasNext()) {
          Program p = channelDayProg.next();
          
          if(p.isOnAir()) {
            sendItem(p,pout);
            break;
          }
        }
      }*/
    }
    
    pout.print("</TvbSearch>\r\n");
  }
  
  
  private void sendItem(Program p, PrintStream pout) {
    pout.print("<Item>\r\n");
    
    pout.print("  <UniqueID>");
    pout.print(p.getUniqueID());
    pout.print("</UniqueID>\r\n");  
    
    pout.print("  <Title>");
    try {
      pout.print(URLEncoder.encode(p.getTitle(),"UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    pout.print("</Title>\r\n");
    
    String description = null;
    
    if(p.hasFieldValue(ProgramFieldType.SHORT_DESCRIPTION_TYPE)) {
      description = p.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
    }
    else if(p.getDescription() != null) {
      description = p.getDescription();
    }
    
    if(p.getDescription() != null) {
      pout.print("  <ShortDescription>");
      try {
        pout.print(URLEncoder.encode(description.substring(0, Math.min(description.length(), 100)),"UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      pout.print("</ShortDescription>\r\n");
    }
    pout.print("  <StartDate>");
    pout.print(p.getDateString());
    pout.print("</StartDate>\r\n");
    pout.print("  <StartTime>");
    
    TimeFormatter timeFormatter = new TimeFormatter();
    
    pout.print(timeFormatter.formatTime(p.getStartTime() / 60,p.getStartTime() % 60));
    pout.print("</StartTime>\r\n");
    pout.print("  <EndTime>");
    
    pout.print(timeFormatter.formatTime((p.getStartTime() + p.getLength()) / 60,(p.getStartTime() + p.getLength()) % 60));
    pout.print("</EndTime>\r\n");
    pout.print("  <Channel>");
    try {
      pout.print(URLEncoder.encode(p.getChannel().getName(),"UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    pout.print("</Channel>\r\n");
    pout.print("  <ChannelIcon>");
    pout.print(p.getChannel().getUniqueId());
    pout.print("</ChannelIcon>\r\n");

    pout.print("  <Picture>");
    pout.print(p.hasFieldValue(ProgramFieldType.PICTURE_TYPE));
    pout.print("</Picture>\r\n");
    
    pout.print("</Item>\r\n");
  }
  
  private static void errorReport(PrintStream pout, Socket connection,
      String code, String title, String msg) {
    pout.print("HTTP/1.0 " + code + " " + title + "\r\n" +
    "\r\n" +
    "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
    "<TITLE>" + code + " " + title + "</TITLE>\r\n" +
    "</HEAD><BODY>\r\n" +
    "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n" +
    "<HR><ADDRESS>FileServer 1.0 at " + 
    connection.getLocalAddress().getHostName() + 
    " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" +
    "</BODY></HTML>\r\n");
    //log(connection, code + " " + title);
  }
  
  public void onDeactivation() {
    if(!mSocket.isClosed()) {
      try {
        mSocket.close();
        mServerIsRunning = false;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  
}