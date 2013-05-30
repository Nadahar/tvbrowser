package switchplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A Stream Reader for the InputStream of a process.
 * 
 * @author René Mach
 *
 */
public class ProcessStreamReader extends Thread{

  private InputStream mIn;
  
  /**
   * @param in The InputStream
   */
  public ProcessStreamReader(InputStream in) {
    mIn = in;
    setPriority(Thread.MIN_PRIORITY);
    start();
  }
  
  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(mIn);
      BufferedReader br = new BufferedReader(isr);
      
      while ((br.readLine()) != null)
        ;
      
    } catch (IOException e) {}  
  }

}
