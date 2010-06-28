package util.io;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Til Schneider, www.murfman.de
 */
public class DownloadManager {

  private static final Logger mLog
    = Logger.getLogger(DownloadManager.class.getName());
    
  private int mConcurrentDownloads;
  
  private LinkedList<DownloadJob> mJobList;
  
  private int mActiveThreadCount;
  
  private Thread mWaitingThread;



  
  public DownloadManager() {
    this(10);
  }
  
  
  public DownloadManager(int concurrentDownloads) {
     
      mConcurrentDownloads = concurrentDownloads;
    
      mJobList = new LinkedList<DownloadJob>();
    }
  
  

  
  public void addDownloadJob(String serverUrl, String fileName, DownloadHandler handler) {
    synchronized(mJobList) {
      mJobList.add(new DownloadJob(serverUrl, fileName, handler));
    }
  }



  public void removeAllDownloadJobs() {
    synchronized(mJobList) {
      mJobList.clear();
    }
  }


  public int getDownloadJobCount() {
    synchronized(mJobList) {
      return mJobList.size();
    }
  }
  
  
  public void runDownload() {
    mActiveThreadCount = 0;
    if (mConcurrentDownloads < 1) {
      mConcurrentDownloads = 1;
    }
    mWaitingThread = Thread.currentThread();

    // Set the max. connections
    if (mConcurrentDownloads > 5) {
      System.setProperty("http.maxConnections", Integer.toString(mConcurrentDownloads));
    } else {
      // This is the default
      System.setProperty("http.maxConnections", "5");
    }
    
    for (int i = 0; i < mConcurrentDownloads; i++) {
      Thread downloadThread = new Thread("Download manager") {
        public void run() {
          downloadThreadRun();
        }
      };
      downloadThread.start();
    }

    // Wait until all jobs are processed
    boolean isFinished;
    do {
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException exc) {}
      
      synchronized (mJobList) {
        isFinished = mJobList.isEmpty() && (mActiveThreadCount == 0);
      }
      
    } while (! isFinished);
  }



  private void downloadThreadRun() {
    mActiveThreadCount++;

    boolean isFinished = false;
    do {
      // Get the next job
      DownloadJob job = null;
      
      synchronized (mJobList) {
        if (mJobList.isEmpty()) {
          isFinished = true;
        } else {
          job = mJobList.removeFirst();
        }
      }
      
      if (job != null) {
        String url = job.getServerUrl() + (job.getServerUrl().endsWith("/") ? "" : "/") + job.getFileName();
        mLog.info("Loading " + url + "...");
        InputStream stream = null;
        try {
          stream = IOUtilities.getStream(new URL(url));
        }
        catch (Throwable thr) {
          if (isFileNotFound(thr)) {
            try {
              job.getDownloadHandler().handleFileNotFound(job.getFileName());
            }
            catch (Throwable thr2) {
              mLog.log(Level.WARNING, "File not found " + url, thr2);
            }
          } else {
            mLog.log(Level.WARNING, "Error getting file " + url, thr);
          }
        }

        if (stream != null) {
          try {
            job.getDownloadHandler().handleDownload(job, stream);
            stream.close();
          }
          catch (Throwable thr) {
            mLog.log(Level.WARNING, "Error downloading " + url, thr);
          }
          finally {
            if (stream != null) {
              try { stream.close(); } catch (Throwable thr) {}
            }
          }
        }
      }
    } while (! isFinished);
    
    mActiveThreadCount--;

    mWaitingThread.interrupt();
  }



  private boolean isFileNotFound(Throwable thr) {
    while (thr != null) {
      if (thr instanceof FileNotFoundException) {
        return true;
      }
      thr = thr.getCause();
    }
    
    return false;
  }
}
