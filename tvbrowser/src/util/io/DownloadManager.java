package util.io;

import java.util.LinkedList;
import java.util.logging.Level;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Til Schneider, www.murfman.de
 */
public class DownloadManager {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(DownloadManager.class.getName());
  
  private String mServerUrl;
  
  private int mConcurrentDownloads;
  
  private LinkedList mJobList;
  
  private int mActiveThreadCount;
  
  private Thread mWaitingThread;



  public DownloadManager(String serverUrl) {
    this(serverUrl, 10);
  }
  
  

  public DownloadManager(String serverUrl, int concurrentDownloads) {
    if (! serverUrl.startsWith("http://")) {
      serverUrl = "http://" + serverUrl;
    }
    
    mServerUrl = serverUrl;
    mConcurrentDownloads = concurrentDownloads;
    
    mJobList = new LinkedList();
  }
  
  
  
  public void addDownloadJob(String fileName, DownloadHandler handler) {
    synchronized(mJobList) {
      mJobList.add(new DownloadJob(fileName, handler));
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
    
    for (int i = 0; i < mConcurrentDownloads; i++) {
      Thread downloadThread = new Thread() {
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
          job = (DownloadJob) mJobList.removeFirst();
        }
      }
      
      if (job != null) {
        String url = mServerUrl + "/" + job.getFileName();
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
            job.getDownloadHandler().handleDownload(job.getFileName(), stream);
          }
          catch (Throwable thr) {
            mLog.log(Level.WARNING, "Error downloading " + url, thr);
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
  
  
  
  private class DownloadJob {
    
    private String mFileName;
    private DownloadHandler mHandler;
    
    public DownloadJob(String fileName, DownloadHandler handler) {
      mFileName = fileName;
      mHandler = handler;
    }
    
    public String getFileName() {
      return mFileName;
    }
    
    public DownloadHandler getDownloadHandler() {
      return mHandler;
    }
    
  }

}
