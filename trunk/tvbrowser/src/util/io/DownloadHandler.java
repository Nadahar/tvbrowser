package util.io;

import java.io.InputStream;

import util.exc.TvBrowserException;

/**
 * @author Til Schneider, www.murfman.de
 */
public interface DownloadHandler {

  public void handleDownload(String fileName, InputStream stream)
    throws TvBrowserException;

  public void handleFileNotFound(String fileName) throws TvBrowserException;

}
