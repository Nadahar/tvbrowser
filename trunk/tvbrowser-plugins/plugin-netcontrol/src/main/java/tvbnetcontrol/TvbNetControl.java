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
  private static final String PAKET_SIZE_KEY = "packetSize";
  
  private static final Version VERSION = new Version(0, 8, 0, false);
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
            byte[] b = new byte[getPaketSize()];
            DatagramPacket packet = new DatagramPacket(b,getPaketSize());
            mSocket.receive(packet);
            String temp = new String(packet.getData()).trim();
            
            boolean send = true;
            
            String channel = Commands.getRunningNumber(temp);
            
            if(channel != null) {
              selectRunning(channel);
            }
            else {
              channel = Commands.getChannelNumber(temp);
              
              if(channel != null) {
                scrollToChannel(channel);
              }
              else if(Commands.isFocus(temp)) {
                focusWindow();
              }
              else if(!Commands.isPing(temp)) {
                String cmd = Commands.getCommandForCommand(temp);
                
                if(cmd != null) {
                  sendKeyCommand(cmd);
                }
                else {
                  send = false;
                }
              }
            }
            
            if(send) {
              send(temp);
            }
            else {
              send(Commands.UNKNOWN);
            }
          }catch(Exception e) {e.printStackTrace();}
        }
      }
    }.start();
  }
  
  private void sendKeyCommand(String cmd) {
    String[] parts = Commands.getKeyCommandParts(cmd);
    
    boolean focus = Commands.isKeyFocus(cmd);
    boolean ctrl = false;
    boolean alt = false;
    boolean shift = false;
    String key = "";
    
    for(String part : parts) {
      if(Commands.isCtrl(part)) {
        ctrl = true;
      }
      else if(Commands.isShift(part)) {
        shift = true;
      }
      else if(Commands.isAlt(part)) {
        alt = true;
      }
      else {
        key = part.toUpperCase();
      }
    }
    
    try {
      int keyCode = -1;
      
      if(key.length() > 0) {
        Field keyField = KeyEvent.class.getField("VK_"+key);
        keyCode = (Integer)keyField.get(KeyEvent.class);
      }
      
      sendKey(keyCode, ctrl, shift, alt, focus);
    }catch (Exception e1) {
      // TODO: handle exception
      e1.printStackTrace();
    }
  }
  
  private void focusWindow() {
    if(!getParentFrame().isVisible() || ((getParentFrame().getExtendedState() & WindowEvent.WINDOW_ICONIFIED) == WindowEvent.WINDOW_ICONIFIED)) {
      getParentFrame().setVisible(true);
      getParentFrame().setExtendedState(mParentState);
    }
    
    UiUtilities.getLastModalChildOf(getParentFrame()).toFront();
    UiUtilities.getLastModalChildOf(getParentFrame()).requestFocus();  
  }
  
  private void selectRunning(String ch) {
    final int channel = Integer.parseInt(ch)-1;
    
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
  
  private void scrollToChannel(String ch) {
    final int channel = Integer.parseInt(ch)-1;
    
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
        focusWindow();
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
      
      if(keyCode != -1) {
        mRobot.keyPress(keyCode);
        mRobot.keyRelease(keyCode);
      }
      
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
      private JSpinner mPacketSize;
      private JCheckBox mSendAnswer;
      private JTextField mAnswerPort;
      private JTextField mAnswerNetwork;
      
      @Override
      public void saveSettings() {
        mSettings.setProperty(SOCKET_PORT_KEY, String.valueOf(mNetworkPort.getValue()));
        mSettings.setProperty(ANSWER_ENABLED_KEY, String.valueOf(mSendAnswer.isSelected()));
        mSettings.setProperty(ANSWER_PORT_KEY, String.valueOf(mAnswerPort.getText()));
        mSettings.setProperty(ANSWER_NETWORK_KEY, String.valueOf(mAnswerNetwork.getText()));
        mSettings.setProperty(PAKET_SIZE_KEY, String.valueOf(mPacketSize.getValue()));
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
        JPanel settings = new JPanel(new FormLayout("5dlu,10dlu,default,3dlu,min","5dlu,default,default,3dlu,default,default,default"));
        int port = getNetworkPort();
        
        if(port == port-1) {
          port = 1024 + (int)(Math.random() * 64511);
        }
        
        mNetworkPort = new JSpinner(new SpinnerNumberModel(Math.max(1024,Math.min(65535,port)),1024,65535,1));
        mPacketSize = new JSpinner(new SpinnerNumberModel(getPaketSize(),16,128,2));
        
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
        
        settings.add(new JLabel("Packet size (Byte):"), cc.xyw(2, 3, 2));
        settings.add(mPacketSize, cc.xy(5, 3));
        
        settings.add(mSendAnswer, cc.xyw(2, 5, 2));
        settings.add(answerNet, cc.xy(3, 6));
        settings.add(mAnswerNetwork, cc.xy(5,6));
        settings.add(answerPort, cc.xy(3, 7));
        settings.add(mAnswerPort, cc.xy(5,7));
        
        return settings;
      }
    };
  }
  
  public int getAnswerPort() {
    return Integer.parseInt(mSettings.getProperty(ANSWER_PORT_KEY, "-1"));
  }
  
  public byte getPaketSize() {
    return Byte.parseByte(mSettings.getProperty(PAKET_SIZE_KEY, "16"));
  }
  
  public int getNetworkPort() {
    return Integer.parseInt(mSettings.getProperty(SOCKET_PORT_KEY, "-1"));
  }
  
  public String getAnswerNetwork() {
    return mSettings.getProperty(ANSWER_NETWORK_KEY, "localhost");
  }
  
  private void send(String text) {
    if(mSettings.getProperty(ANSWER_ENABLED_KEY,"false").equals("true")) {
      byte[] buf = new byte[getPaketSize()];
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
