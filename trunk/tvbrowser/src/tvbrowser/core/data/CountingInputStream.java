package tvbrowser.core.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that counts the number of read bytes of a nested InputStream.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class CountingInputStream extends InputStream {
  
  private InputStream mNestedStream;
  private int mOffset;


  public CountingInputStream(InputStream stream) {
    mNestedStream = stream;
    mOffset = 0;
  }


  public void resetOffset() {
    mOffset = 0;
  }
  
  
  public int getOffset() {
    return mOffset;
  }
  

  public int read() throws IOException {
    mOffset++;
    
    return mNestedStream.read();
  }


  public int read(byte b[], int off, int len) throws IOException {
    len = mNestedStream.read(b, off, len);
    
    mOffset += len;
    
    return len;
  }


  public long skip(long n) throws IOException {
    long skipped = mNestedStream.skip(n);
    
    mOffset += skipped;
    
    return skipped;
  }


  public int available() throws IOException {
    return mNestedStream.available();
  }


  public void close() throws IOException {
    mNestedStream.close();
  }

}
