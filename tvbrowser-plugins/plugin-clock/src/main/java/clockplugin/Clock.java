package clockplugin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
    
    mTime = new JLabel();
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
}
