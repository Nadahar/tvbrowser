/*
 * exec.java
 *
 * Created on February 10, 2005, 5:59 PM
 */

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Displayable;

/**
 * Dieser Thread ist wiederverwendbar.
 * @author  pumpkin
 */

public class exec extends Thread {
  
  private boolean doInit = false;
  private tv TV;
  private Command c;
  private Displayable d;
  public boolean stop = false;
  
  /** Creates a new instance of exec */
  public exec(tv Tv) {
    doInit = true;
    TV = Tv;
  }
  
  public synchronized void execute(Command C, Displayable D){
    c = C;
    d = D;
    notify();
  }
  
  
  public void run(){
    if (doInit){
      TV.init();
    }
    while (!stop){
      synchronized (this){
        try {
          this.wait();
        } catch (Exception E){}
        if (c!=null){
          if (!TV.commandActionSlow(c)){
            try {
              AlertType.ERROR.playSound(Display.getDisplay(TV));
              Display.getDisplay(TV).setCurrent(d);
            } catch (Error E){
            }
          }
        }
      }
    }
  }
}
