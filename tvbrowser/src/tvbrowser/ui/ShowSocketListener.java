package tvbrowser.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tvbrowser.ui.mainframe.MainFrame;

public class ShowSocketListener {
  
  private int mState;
  private static DatagramSocket mSocket = null;
  
  private ShowSocketListener(int c) {
    try {
      mSocket = new DatagramSocket(4587);
    }catch(Exception e){}
    mState = MainFrame.getInstance().getExtendedState();
    MainFrame.getInstance().addComponentListener(new ComponentListener() {

      public void componentResized(ComponentEvent e) {
        int state = MainFrame.getInstance().getExtendedState();
        if ((state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
          mState = JFrame.MAXIMIZED_BOTH;
        } else {
          mState = JFrame.NORMAL;
        }
      }

      public void componentHidden(ComponentEvent e) {}

      public void componentMoved(ComponentEvent e) {}

      public void componentShown(ComponentEvent e) {}
    });

    Thread t = new Thread() {
      public void run() {
        while(true) {        
          if(mSocket != null) {
            try{
              byte[] buf = new byte[4];
              DatagramPacket packet = new DatagramPacket(buf, buf.length);
              mSocket.receive(packet);
              String value = new String(packet.getData());
              
              if(value.indexOf("show") != -1)
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    MainFrame.getInstance().showFromTray(mState);
                  }
                });
            
            }catch(Exception ee) {ee.printStackTrace();}
          }
        }
      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    
    if(mSocket != null)
      t.start();
  }
  
  public static void sendSow() {
    try {
      mSocket = new DatagramSocket(4586);    
      byte[] buf = new byte[4];
    
      InetAddress address = InetAddress.getByName("localhost");
      buf = "show".getBytes();
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4587);
      mSocket.send(packet);
      mSocket.close();
      
    }catch(Exception e){}
  }
  
  public static void startSocketListener() {
    new ShowSocketListener(1);
  }  
}
