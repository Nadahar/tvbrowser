/*
 * exec.java
 *
 * Created on February 10, 2005, 5:59 PM
 */

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;

/**
 * Dieser Thread ist wiederverwendbar.
 * @author  pumpkin
 */

public class exec extends Thread {
  
  private tv TV;
  private Command C;
  private boolean cont = true;
  
  public exec(tv Tv){
    TV = Tv;
  }
  
  protected synchronized void execute(Command c){
    C = c;
    notifyAll();
  }
  
  public void run(){
    try {
      if (TV.loadConfig()){
        if (TV.firstStart()){
          TV.copyData();
          TV.endFirstStart();
        }
        TV.startApp();
      }
    } catch (Exception E){
      Form F = new Form("Error");
      F.append("Loading config failed\n"+E.toString());
      TV.CMD_EXIT = new Command("exit",Command.EXIT,0);
      F.addCommand(TV.CMD_EXIT);
      F.setCommandListener(TV);
      Display.getDisplay(TV).setCurrent(F);
      return;
    }
    TV.loadIcons();
    while (cont){
      synchronized (this){
        try {
          wait();
        } catch (Exception E){
        }
        if (C != null){
          TV.commandActionSlow(C);
          C = null;
        }
      }
    }
  }
  
  public synchronized void stopExecution(){
    cont = false;
    notify();
  }
}
