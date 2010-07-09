package primarydatamanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import primarydatamanager.primarydataservice.PrimaryDataService;


public class PDSRunner {
  
  private LinkedList<PrimaryDataService> mPDSList;
  private int mActiveThreadCount;
  private Thread mWaitingThread;
  private File mLogDir, mRawDir;
  private static Properties mProperties;
  
  private static final int CONCURRENT_DOWNLOADS=5;
  
  private static final Logger mLog = Logger.getLogger(PDSRunner.class.getName());
  
  
  public PDSRunner(File baseDir) {
    Logger.getLogger("sun.awt.X11.timeoutTask.XToolkit").setLevel(Level.INFO);
    mPDSList=new LinkedList<PrimaryDataService>();
    mRawDir=new File(baseDir,"raw");
    mLogDir=new File(baseDir,"pdslog");
    mProperties=new Properties();
  }
  
  public void addPDS(PrimaryDataService pds) {
    mPDSList.add(pds);
  }
  
  public void setLogDir(File logDir) {
    mLogDir=logDir;
  }
  
  public void setRawDir(File rawDir) {
    mRawDir=rawDir;
  }
  
  public void runAllPrimaryDataServices() throws PreparationException {
     
    if (!mRawDir.exists()) {
      if (!mRawDir.mkdirs()) {
        throw new PreparationException("Could not create directory "+mRawDir.getAbsolutePath());
      }
    }
     
    if (!mLogDir.exists()) {
      if (!mLogDir.mkdirs()) {
        throw new PreparationException("Could not create directory "+mLogDir.getAbsolutePath());
      }
    }
     
     
    
    mActiveThreadCount =0;
    mWaitingThread = Thread.currentThread();
    
    //  Set the max. connections
    if (CONCURRENT_DOWNLOADS > 5) {
      System.setProperty("http.maxConnections", Integer.toString(CONCURRENT_DOWNLOADS));
    }
    
    for (int i=0;i<CONCURRENT_DOWNLOADS;i++) {
      Thread downloadThread = new Thread() {
        public void run() {
          PDSThreadRun();
        }
      };
      downloadThread.start();
    }
    
    //  Wait until all jobs are processed
    boolean isFinished;
    do {
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException exc) {}
      
      synchronized (mPDSList) {
        isFinished = mPDSList.isEmpty() && (mActiveThreadCount == 0);
      }
    } while (! isFinished);
    
  }
  
  


  private void PDSThreadRun() {
    mActiveThreadCount++;

    boolean isFinished = false;
    do {
      // Get the next job
      PrimaryDataService pds = null;
      
      synchronized (mPDSList) {
        if (mPDSList.isEmpty()) {
          isFinished = true;
        } else {
          pds = mPDSList.removeFirst();
        }
      }
      
      if (pds != null) {
        String dir = mRawDir.getAbsolutePath();
        File logFile=new File(mLogDir,pds.getClass().getName()+".txt");
        try {
          FileOutputStream out=new FileOutputStream(logFile);
          PrintStream errOut=new PrintStream(out);
          if (mProperties != null) {
            pds.setParameters(mProperties);
          }
          boolean thereWereErrors = pds.execute(dir, errOut);          
          if (thereWereErrors) {
            mLog.warning("There were errors during the execution of primary "
                + "data service " + pds.getClass().getName() + ". See log file: "
                + logFile.getAbsolutePath());
          }else{
            mLog.fine(pds.getClass().getName()+ " terminated normally");
            logFile.delete();
          }
          errOut.close();
        }catch(Exception exc) {
          mLog.log(Level.SEVERE, "Error executing primary data service "+pds.getClass().getName(), exc);
        }
      }
    } while (! isFinished);
    
    mActiveThreadCount--;

    mWaitingThread.interrupt();
  }

  
  
  public static void main(String[] args) throws PreparationException {
    
    PDSRunner pdsRunner=new PDSRunner(new File("."));
    
    if (args.length==0) {
      System.out.println("usage: PDSRunner [-raw directory] [-log directory] [-PDSparameterX valueX] [...] pds ...");
      System.exit(1);
    }
    
    for (int i=0;i<args.length;i++) {
      
      if ("-raw".equalsIgnoreCase(args[i])) {
        if ((i + 1) >= args.length) {
           System.out.println("You have to specify the raw directory");
           System.exit(1);
        } else {
           i++;
           pdsRunner.setRawDir(new File(args[i]));
        }
      }
      else if ("-log".equalsIgnoreCase(args[i])) {
        if ((i + 1) >= args.length) {
          System.out.println("You have to specify the log directory");
          System.exit(1);
        }
        else {
          i++;
          pdsRunner.setLogDir(new File(args[i]));
        }
      }
      else if (args[i].startsWith("-")) {
        if ((i + 1) >= args.length) {
          System.out.println("You have to specify a value for the parameter "+args[i]);
          System.exit(1);
        }
        else {
          mProperties.setProperty(args[i].substring(1), args[i+1]);
          i++;
        }
      }
      else {
        String[] classes = args[i].split(",");
        for (final String clas:classes) {
            try {
                pdsRunner.addPDS(PrimaryDataManager.createPrimaryDataService(clas));
            } catch (PreparationException e) {
                mLog.log(Level.SEVERE, "A PDS Class with the name " + clas + " could not be initialised ", e);
            }
        }
      }
    }
    
    
    pdsRunner.runAllPrimaryDataServices();
    
  }
  
  
}