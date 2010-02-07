package clockplugin;

import java.awt.Frame;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The clock for the title bar.
 *
 * @author René Mach
 */
public class TitleBarClock extends Thread{

  private boolean mRun;

  /**
   * The default constructor for this class.
   */
  public TitleBarClock() {
    mRun = true;
    setPriority(Thread.MIN_PRIORITY);
    start();
  }

  /**
   * Stop the title bar clock.
   */
  public void stopp() {
    mRun = false;
  }

  public void run() {
    DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
    String lasttime = null;
    while(mRun) {
      try {
        Thread.sleep(1000);
        Frame parent = ClockPlugin.getInstance().getSuperFrame();
        if(parent != null && parent.isVisible()) {
          String title = parent.getTitle();
          int pos = title.lastIndexOf(":");
          String time = df.format(new Date(System.currentTimeMillis()));

          if(pos != -1) {
            try {
              Integer.parseInt(title.substring(pos + 1,pos + 3));
              title = title.substring(0,title.length() - time.length() - 3) + " - " + time;
            }catch(NumberFormatException e) {
              title += " - " + time;
              parent.setTitle(title);
            }
          } else {
            title += " - " + time;
          }

          if(lasttime == null || !time.equals(lasttime) || pos == -1) {
            parent.setTitle(title);
            lasttime = time;
          }
        }
      }catch(InterruptedException e){}
    }

    if(ClockPlugin.getInstance().getSuperFrame() != null) {
      ClockPlugin.getInstance().getSuperFrame().setTitle(getTitle(true));
    }
  }

  private String getTitle(boolean cut) {
    DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
    Frame parent = ClockPlugin.getInstance().getSuperFrame();

    if(parent != null) {
      String title = parent.getTitle();
      int pos = title.lastIndexOf(":");
      String time = df.format(new Date(System.currentTimeMillis()));

      if(pos != -1) {
        try {
          Integer.parseInt(title.substring(pos + 1,pos + 3));
          if(!cut) {
            title = title.substring(0,title.length() - time.length() - 3) + " - " + time;
          } else {
            title = title.substring(0,title.length() - time.length() - 3);
          }
        }catch(NumberFormatException e) {
          if(!cut) {
            title += " - " + time;
          }
        }
      }
      else if(!cut) {
        title += " - " + time;
      }

      return title;
    }
    return null;
  }
}
