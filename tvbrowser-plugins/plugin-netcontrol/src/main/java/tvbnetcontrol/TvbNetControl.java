package tvbnetcontrol;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

public class TvbNetControl extends Plugin {
  private static final String SOCKET_PORT_KEY = "socketPort";
  private static final String ANSWER_PORT_KEY = "answerPort";
  private static final String ANSWER_ENABLED_KEY = "answerEnabled";
  private static final String ANSWER_NETWORK_KEY = "answerNetwork";
  
  private static final Version VERSION = new Version(0, 6, false);
  private static TvbNetControl INSTANCE;
  
  private Properties mSettings;
  private DatagramSocket mSocket;
  
  private boolean mHasToStart;
  private Robot mRobot;
  
  private WindowStateListener mParentListener;
  private int mParentState;
  
  public TvbNetControl() {
    INSTANCE = this;
    mHasToStart = false;
  }
  
  static TvbNetControl getInstance() {
    return INSTANCE;
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(TvbNetControl.class, "TV-Browser Net Control", "Receives UDP messages to control TV-Browser with.", "René Mach", "GPL");
  }
  
  public void loadSettings(Properties settings) {
    mSettings = settings;
  }
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  public void handleTvBrowserStartFinished() {
    if(mHasToStart) {
      onActivation();
    }
  }
  
  public void onActivation() {
    if(mSettings != null) {
      if(mParentListener == null && getParentFrame() != null) {
        mParentState = getParentFrame().getExtendedState();
        mParentListener = new WindowStateListener() {
          @Override
          public void windowStateChanged(WindowEvent e) {
            if((e.getNewState() & WindowEvent.WINDOW_ICONIFIED) != WindowEvent.WINDOW_ICONIFIED) {
              mParentState = e.getNewState();
            }
          }
        };
        getParentFrame().addWindowStateListener(mParentListener);
      }
      
      String socketPort = mSettings.getProperty(SOCKET_PORT_KEY,"");
      
      if(socketPort.trim().length() == 0) {
        socketPort = String.valueOf(1024 + (int)(Math.random() * 64511));
        mSettings.setProperty(SOCKET_PORT_KEY, socketPort);
      }
      
      try {
        mSocket = new DatagramSocket(Integer.parseInt(socketPort));
      } catch (NumberFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SocketException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      startSocketReadThread();
    }
    else {
      mHasToStart = true;
    }
  }
  
  private void startSocketReadThread() {
    new Thread() {
      public void run() {
        setPriority(Thread.MIN_PRIORITY);
        while(!mSocket.isClosed()) {
          try {
            Thread.sleep(100);
            byte[] b = new byte[16];
            DatagramPacket packet = new DatagramPacket(b,16);
            mSocket.receive(packet);
            String temp = new String(packet.getData()).trim();
            
            boolean send = true;
            
            if(temp.toLowerCase().startsWith("channel")) {
              final int channel = Integer.parseInt(temp.substring(temp.lastIndexOf("_")+1))-1;
              
              final Channel[] subscribedChannels = getPluginManager().getSubscribedChannels();
              
              if(channel < subscribedChannels.length) {
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    UiUtilities.getLastModalChildOf(getParentFrame()).toFront();
                    UiUtilities.getLastModalChildOf(getParentFrame()).requestFocus();
                    getPluginManager().scrollToChannel(subscribedChannels[channel]);
                  }
                });
              }
            }
            else if(temp.toLowerCase().startsWith("running")) {
              final int channel = Integer.parseInt(temp.substring(temp.lastIndexOf("_")+1))-1;
              
              final Channel[] subscribedChannels = getPluginManager().getSubscribedChannels();
              
              if(channel < subscribedChannels.length) {
                Iterator<Program> dayProgram = getPluginManager().getChannelDayProgram(Date.getCurrentDate(), subscribedChannels[channel]);
                
                while(dayProgram.hasNext()) {
                  final Program prog = dayProgram.next();
                  
                  if(prog.isOnAir() || !prog.isExpired()) {
                    SwingUtilities.invokeLater(new Runnable() {
                      @Override
                      public void run() {
                        UiUtilities.getLastModalChildOf(getParentFrame()).toFront();
                        UiUtilities.getLastModalChildOf(getParentFrame()).requestFocus();
                        getPluginManager().selectProgram(prog);
                      }
                    });
                    
                    break;
                  }
                }
              }              
            }
            else if(temp.toLowerCase().startsWith("up")) {
              sendKey(KeyEvent.VK_UP, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("down")) {
              sendKey(KeyEvent.VK_DOWN, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("left")) {
              sendKey(KeyEvent.VK_LEFT, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("right")) {
              sendKey(KeyEvent.VK_RIGHT, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("pageup")) {
              sendKey(KeyEvent.VK_PAGE_UP, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("pagedown")) {
              sendKey(KeyEvent.VK_PAGE_DOWN, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("pageleft")) {
              sendKey(KeyEvent.VK_PAGE_UP, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("pageright")) {
              sendKey(KeyEvent.VK_PAGE_DOWN, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("programup")) {
              sendKey(KeyEvent.VK_UP, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("programdown")) {
              sendKey(KeyEvent.VK_DOWN, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("programright")) {
              sendKey(KeyEvent.VK_RIGHT, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("programleft")) {
              sendKey(KeyEvent.VK_LEFT, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("clearselection")) {
              sendKey(KeyEvent.VK_D, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("tab")) {
              sendKey(KeyEvent.VK_TAB, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("shifttab")) {
              sendKey(KeyEvent.VK_TAB, false, true, false, false);
            }
            else if(temp.toLowerCase().startsWith("now")) {
              sendKey(KeyEvent.VK_F9, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("previousday")) {
              sendKey(KeyEvent.VK_P, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("nextday")) {
              sendKey(KeyEvent.VK_N, true, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("enter")) {
              sendKey(KeyEvent.VK_ENTER, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("programcontext")) {
              sendKey(KeyEvent.VK_R, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("singlelclick")) {
              sendKey(KeyEvent.VK_L, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("singlemclick")) {
              sendKey(KeyEvent.VK_M, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("doublelclick")) {
              sendKey(KeyEvent.VK_D, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("doublemclick")) {
              sendKey(KeyEvent.VK_O, false, false, false, true);
            }
            else if(temp.toLowerCase().startsWith("space")) {
              sendKey(KeyEvent.VK_SPACE, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("esc")) {
              sendKey(KeyEvent.VK_ESCAPE, false, false, false, false);
            }
            else if(temp.toLowerCase().startsWith("focus")) {
              if(!getParentFrame().isVisible() || ((getParentFrame().getExtendedState() & WindowEvent.WINDOW_ICONIFIED) == WindowEvent.WINDOW_ICONIFIED)) {
                getParentFrame().setVisible(true);
                getParentFrame().setExtendedState(mParentState);
              }
              
              UiUtilities.getLastModalChildOf(getParentFrame()).toFront();
              UiUtilities.getLastModalChildOf(getParentFrame()).requestFocus();            
            }
            else if(temp.toLowerCase().startsWith("k_") || temp.toLowerCase().startsWith("kf_")) {
              String[] parts = temp.trim().substring(temp.toLowerCase().startsWith("kf_") ? 3 : 2).split("\\+");
              
              boolean focus = temp.toLowerCase().startsWith("kf_");
              boolean ctrl = false;
              boolean alt = false;
              boolean shift = false;
              String key = "";
              
              for(String part : parts) {
                if(part.equalsIgnoreCase("ctrl")) {
                  ctrl = true;
                }
                else if(part.equalsIgnoreCase("shift")) {
                  shift = true;
                }
                else if(part.equalsIgnoreCase("alt")) {
                  alt = true;
                }
                else {
                  key = part.toUpperCase();
                }
              }
              
              try {
                Field keyField = KeyEvent.class.getField("VK_"+key);
                int keyCode = (Integer)keyField.get(KeyEvent.class);
                
                sendKey(keyCode, ctrl, shift, alt, focus);
              }catch (Exception e1) {
                // TODO: handle exception
                e1.printStackTrace();
              }
            }
            else {
              send = false;
            }
            
            if(send) {
              send(temp);
            }
            else {
              send("unknown");
            }
          }catch(Exception e) {e.printStackTrace();}
        }
      }
    }.start();
  }
  
  private void sendKey(int keyCode, boolean ctrl, boolean shift, boolean alt, boolean requestFocus) {
    if(mRobot == null) {
      try {
        mRobot = new Robot();
      } catch (AWTException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    if(mRobot != null) {
      if(requestFocus) {
        if(!getParentFrame().isVisible() || ((getParentFrame().getExtendedState() & WindowEvent.WINDOW_ICONIFIED) == WindowEvent.WINDOW_ICONIFIED)) {
          getParentFrame().setVisible(true);
          getParentFrame().setExtendedState(mParentState);
        }
        
        UiUtilities.getLastModalChildOf(getParentFrame()).toFront();
        UiUtilities.getLastModalChildOf(getParentFrame()).requestFocus();
      }
      
      if(ctrl) {
        mRobot.keyPress(KeyEvent.VK_CONTROL);
      }
      if(shift) {
        mRobot.keyPress(KeyEvent.VK_SHIFT);
      }
      if(alt) {
        mRobot.keyPress(KeyEvent.VK_ALT);
      }
      
      mRobot.keyPress(keyCode);
      mRobot.keyRelease(keyCode);
      
      if(ctrl) {
        mRobot.keyRelease(KeyEvent.VK_CONTROL);
      }
      if(shift) {
        mRobot.keyRelease(KeyEvent.VK_SHIFT);
      }
      if(alt) {
        mRobot.keyRelease(KeyEvent.VK_ALT);
      }
    }
  }
  
  public void onDeactivation() {
    if(mSocket != null) {
      mSocket.close();
    }
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JSpinner mNetworkPort;
      private JCheckBox mSendAnswer;
      private JTextField mAnswerPort;
      private JTextField mAnswerNetwork;
      
      @Override
      public void saveSettings() {
        mSettings.setProperty(SOCKET_PORT_KEY, String.valueOf(mNetworkPort.getValue()));
        mSettings.setProperty(ANSWER_ENABLED_KEY, String.valueOf(mSendAnswer.isSelected()));
        mSettings.setProperty(ANSWER_PORT_KEY, String.valueOf(mAnswerPort.getText()));
        mSettings.setProperty(ANSWER_NETWORK_KEY, String.valueOf(mAnswerNetwork.getText()));
        onDeactivation();
        onActivation();
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }
      
      @Override
      public JPanel createSettingsPanel() {
        JPanel settings = new JPanel(new FormLayout("5dlu,10dlu,min,3dlu,min","5dlu,default,3dlu,default,default,default"));
        int port = getNetworkPort();
        
        if(port == port-1) {
          port = 1024 + (int)(Math.random() * 64511);
        }
        
        mNetworkPort = new JSpinner(new SpinnerNumberModel(Math.max(1024,Math.min(65535,port)),1024,65535,1));
        
        mSendAnswer = new JCheckBox("Send answer",mSettings.getProperty(ANSWER_ENABLED_KEY,"false").equals("true"));
        mAnswerPort = new JTextField();
        mAnswerNetwork = new JTextField(getAnswerNetwork());
        
        if(mSettings.getProperty(ANSWER_PORT_KEY) != null) {
          mAnswerPort.setText(mSettings.getProperty(ANSWER_PORT_KEY));
        }
        
        mAnswerNetwork.setEnabled(mSendAnswer.isSelected());
        mAnswerPort.setEnabled(mSendAnswer.isSelected());
        
        final JLabel answerPort = new JLabel("Network port:");
        final JLabel answerNet = new JLabel("Network address:");
        
        mSendAnswer.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            mAnswerNetwork.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            mAnswerPort.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            answerPort.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            answerNet.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          }
        });
        
        CellConstraints cc = new CellConstraints();
        
        settings.add(new JLabel("Network port:"), cc.xyw(2, 2, 2));
        settings.add(mNetworkPort, cc.xy(5, 2));
        
        settings.add(mSendAnswer, cc.xyw(2, 4, 2));
        settings.add(answerNet, cc.xy(3, 5));
        settings.add(mAnswerNetwork, cc.xy(5,5));
        settings.add(answerPort, cc.xy(3, 6));
        settings.add(mAnswerPort, cc.xy(5,6));

        return settings;
      }
    };
  }
  
  public int getAnswerPort() {
    return Integer.parseInt(mSettings.getProperty(ANSWER_PORT_KEY, "-1"));
  }
  
  public int getNetworkPort() {
    return Integer.parseInt(mSettings.getProperty(SOCKET_PORT_KEY, "-1"));
  }
  
  public String getAnswerNetwork() {
    return mSettings.getProperty(ANSWER_NETWORK_KEY, "localhost");
  }
  
  private void send(String text) {
    if(mSettings.getProperty(ANSWER_ENABLED_KEY,"false").equals("true")) {
      byte[] buf = new byte[16];
      try {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(getAnswerNetwork());
        buf = text.getBytes();
        
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, getAnswerPort());
        socket.send(packet);
      } catch (Exception e) {
        // TODO Automatisch erstellter Catch-Block
        e.printStackTrace();
      }
    }
  }
}
