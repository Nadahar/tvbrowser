package clockplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * The clock dialog. License: GPL
 * 
 * @author René Mach
 */
public class Clock extends JDialog implements Runnable, MouseListener, MouseMotionListener {

  private static final long serialVersionUID = 1L;

  private Thread mClockThread;
  private int mShowTime;
  private JLabel mTime;
  private DateFormat mTimeFormat;
  private Point mDraggingPoint;
  private Properties mProperties;
  private boolean mShowForever, mStop, mDontStop;
  private JPanel mTimePanel;

  /**
   * The default construktor of the class.
   * 
   * @param show
   *          The time to show the dialog in seconds.
   * @param config
   *          The properties for this.
   */
  public Clock(int show, Properties config) {
    super(ClockPlugin.getInstance().getSuperFrame());
    mClockThread = new Thread(this);
    mClockThread.setPriority(Thread.MIN_PRIORITY);
    mShowTime = show * 1000;
    mProperties = config;
    mStop = false;
    mDontStop = false;
    this.addMouseMotionListener(this);
    
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    
    String showForever = config.getProperty("showForever");
    if (showForever == null || showForever.length() == 0) {
      mShowForever = false;
      config.setProperty("showForever", false + "");
    } else if (showForever.equals("true")) {
      mShowForever = true;
    } else {
      mShowForever = false;
    }

    int fontsize = Integer.parseInt(config.getProperty("fontsize","28"));
    int oldFontSize = Integer.parseInt(config.getProperty("oldFontSize", "-1"));
    
    Font f = new Font("Arial", Font.BOLD, fontsize);

    mProperties.setProperty("oldFontSize", fontsize + "");

    mTimePanel = new JPanel();
    if (ClockPlugin.getInstance().getShowBorder()) {
      mTimePanel.setBorder(BorderFactory.createEtchedBorder());
    }
    mTimePanel.addMouseListener(this);
    mTimePanel.addMouseMotionListener(this);
    mTimePanel.setLayout(new GridLayout());
    
    mTime = new JLabel() {
      private Object mPersonaObj = new String();
      protected void paintComponent(Graphics g) {
        if(ClockPlugin.getInstance().isUsingPersonaColors()) {
          if(mPersonaObj != null && mPersonaObj instanceof String) {
            try {
              Class persona = Class.forName("util.ui.persona.Persona");
              
              Method m = persona.getMethod("getInstance", new Class<?> [0]);
              mPersonaObj = m.invoke(persona,new Object[0]);
            } catch (Exception e) {
              mPersonaObj = null;
            }
          }
          
          Color textColor = null;
          Color shadowColor = null;
          
          if(mPersonaObj != null) {
            Method m;
            try {
              m = mPersonaObj.getClass().getMethod("getTextColor", new Class<?>[0]);
              textColor = (Color) m.invoke(mPersonaObj,new Object[0]);
                           
              m = mPersonaObj.getClass().getMethod("getShadowColor", new Class<?>[0]);
              shadowColor = (Color) m.invoke(mPersonaObj,new Object[0]);
            } catch (Exception e) {e.printStackTrace();}
          }
          
          FontMetrics metrics = g.getFontMetrics(getFont());
          int textWidth = metrics.stringWidth(getText());
          int baseLine = metrics.getAscent();
        
          if(textColor != null && shadowColor != null && !textColor.equals(shadowColor)) {
            g.setColor(shadowColor);
            
            g.drawString(getText(),getWidth()/2-textWidth/2+1,baseLine+1);
            g.drawString(getText(),getWidth()/2-textWidth/2+2,baseLine+2);
          }
          
          if(textColor != null) {
            g.setColor(textColor);
            g.drawString(getText(),getWidth()/2-textWidth/2,baseLine);
          }
          else {
            super.paintComponent(g);
          }
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    mTime.setFont(f);
    mTime.addMouseListener(this);
    mTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM,Locale.getDefault());
    mTime.addMouseMotionListener(this);
    mTimePanel.add(mTime);

    int xPos = Integer.parseInt(config.getProperty("xPos", "-1000"));
    int yPos = Integer.parseInt(config.getProperty("yPos", "-1000"));
    int width = Integer.parseInt(config.getProperty("xWidth", "-1000"));
    int height = Integer.parseInt(config.getProperty("yHeight", "-1000"));
    
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.X_AXIS));
    this.getContentPane().add(Box.createHorizontalGlue());
    this.getContentPane().add(mTimePanel);
    this.getContentPane().addMouseListener(this);
    this.getContentPane().addMouseMotionListener(this);
    this.setUndecorated(true);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    mTime.setText(mTimeFormat.format(new Date(System.currentTimeMillis())));
    ((JPanel)getContentPane()).setOpaque(false);
    
    if (width < 15 || height < 5 || fontsize != oldFontSize) {
      this.pack();
      this.setSize(this.getWidth() + 7, this.getHeight());
    } else {
      this.setSize(width, height);
    }
    
    if ((xPos - width < (width * -1)) || (xPos - width > d.width) ||
        (yPos - height < (height * -1)) || (yPos - height > d.height)) {
      this.setLocation(d.width - this.getSize().width, 0);
    } else {
      this.setLocation(xPos, yPos);
    }

    if (ClockPlugin.getInstance().getSuperFrame() != null) {
      this.setVisible(true);
    }
    mClockThread.start();
    
    ClockPlugin.getInstance().getSuperFrame().toFront();
  }

  /**
   * 
   * @return The clock thread.
   */
  public synchronized Thread getThread() {
    return mClockThread;
  }

  /**
   * Stop the Thread.
   * 
   */
  public void stopp() {
    mStop = true;
  }

  /**
   * @param value
   *          The clock should be shown forever.
   */
  public void setShowForever(boolean value) {
    mShowForever = value;
  }

  public void run() {
    try {
      do {
        Thread.sleep(500);
        if (!mShowForever) {
          mShowTime -= 500;
        }
        mTime.setText(mTimeFormat.format(new Date(System.currentTimeMillis())));
      } while (mShowTime > 0 && !mStop);
    } catch (InterruptedException e) {
    }
    
    try {
      while(mDontStop) {
        Thread.sleep(200);
      }
    }catch(InterruptedException e) {
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    mDraggingPoint = e.getPoint();
    mDontStop = true;
  }

  public void mouseReleased(MouseEvent e) {
    mDraggingPoint = null;
    mDontStop = false;
    if(ClockPlugin.getInstance().getSuperFrame().isVisible()) {
      ClockPlugin.getInstance().getSuperFrame().toFront();
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  /**
   * Set up the border of the clock.
   * 
   * @param value
   */
  public void setBorder(boolean value) {
    if (value) {
      mTimePanel.setBorder(BorderFactory.createEtchedBorder());
    } else {
      mTimePanel.setBorder(BorderFactory.createEmptyBorder());
    }
  }

  public void mouseDragged(MouseEvent e) {
    if (mDraggingPoint != null && !e.isShiftDown()) {
      int xP = e.getX();
      int yP = e.getY();
      int x = mDraggingPoint.x - xP;
      int y = mDraggingPoint.y - yP;
      
      if(x != 0 || y != 0) {
        setLocation(getX() - x,getY() - y);
        
        mProperties.setProperty("xPos",getX() + "");
        mProperties.setProperty("yPos",getY() + "");
        mProperties.setProperty("xWidth",getWidth() + "");
        mProperties.setProperty("yHeight",getHeight() + "");
      }
    }
  }

  public void mouseMoved(MouseEvent e) {}
  
  /**
   * Sets the if the clock background should be transparent.
   * <p>
   * @param value <code>true</code> if the clock background should be transparent.
   */
  public void setTransparentBackground(boolean value) {
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    GraphicsConfiguration config = devices[0].getDefaultConfiguration();
    
    try {
      Class<?> awtUtilities = Class.forName("com.sun.awt.AWTUtilities");
      Method m = awtUtilities.getMethod("isTranslucencyCapable",new Class<?>[] {GraphicsConfiguration.class});
      
      if((Boolean)m.invoke(awtUtilities, new Object[] {config})) {
        m = awtUtilities.getMethod("setWindowOpaque",new Class<?>[] {Window.class,boolean.class});
        m.invoke(awtUtilities, new Object[] {this,!value});
        mTimePanel.setOpaque(!value);
      }
    } catch (Exception e) {e.printStackTrace();
      
      
      try {
        Method m = config.getClass().getMethod("isTranslucencyCapable()",new Class<?>[] {GraphicsConfiguration.class});
        
        if((Boolean)m.invoke(config,new Object[0])) {
          m = this.getClass().getMethod("setOpacity",new Class<?>[] {float.class});
          m.invoke(this,new Object[] {(float)(value ? 0 : 1)});
          mTimePanel.setOpaque(!value);
        }
      } catch (Exception e1) {}
    }    
  }
}
