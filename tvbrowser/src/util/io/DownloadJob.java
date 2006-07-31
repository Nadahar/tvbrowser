package util.io;

public class DownloadJob {
  
  private String mFileName;
  private DownloadHandler mHandler;
  private String mServerUrl;
  
  public DownloadJob(String serverUrl, String fileName, DownloadHandler handler) {
    mFileName = fileName;
    mHandler = handler;
    mServerUrl = serverUrl;
  }
  
  public String getFileName() {
    return mFileName;
  }
  
  public String getServerUrl() {
    return mServerUrl;
  }
  
  public DownloadHandler getDownloadHandler() {
    return mHandler;
  }
}
