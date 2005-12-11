package onlinereminder;


public class Updater implements Runnable{

  private Configuration mConf;
  
  public Updater(Configuration conf) {
    mConf = conf;
  }
  
  public void run() {
    System.out.println("Create XML");
    
    
    
    System.out.println("1. Added:");
    
    
    
    System.out.println("2. Removed:");
    
    System.out.println("Send XML");
    
    System.out.println("Receive XML");
  }
 
}
