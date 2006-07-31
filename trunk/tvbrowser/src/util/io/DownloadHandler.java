package util.io;

import java.io.InputStream;

import util.exc.TvBrowserException;

/**
 * @author Til Schneider, www.murfman.de
 */
public interface DownloadHandler {

  public void handleDownload(DownloadJob job, InputStream stream)
    throws TvBrowserException;

  public void handleFileNotFound(String fileName) throws TvBrowserException;
}
