/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2003-10-12 18:48:26 +0200 (So, 12 Okt 2003) $
 *   $Author: til132 $
 * $Revision: 236 $
 */
package primarydatamanager.primarydataservice;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class MeasuringInputStream extends InputStream {
  
  private InputStream mDelegate;
  private int mTotalReadBytes;
  
  
  public MeasuringInputStream(InputStream delegate) {
    mDelegate = delegate;
  }


  public int read() throws IOException {
    mTotalReadBytes++;
    return mDelegate.read();
  }


  public int read(byte b[]) throws IOException {
    int readBytes = mDelegate.read(b);
    mTotalReadBytes += readBytes;
    return readBytes;
  }


  public int read(byte b[], int off, int len) throws IOException {
    int readBytes = mDelegate.read(b, off, len);
    mTotalReadBytes += readBytes;
    return readBytes;
  }


  public long skip(long n) throws IOException {
    return mDelegate.skip(n);
  }


  public int available() throws IOException {
    return mDelegate.available();
  }


  public void close() throws IOException {
    mDelegate.close();
  }
  

  public synchronized void mark(int readlimit) {
    mDelegate.mark(readlimit);
  }


  public synchronized void reset() throws IOException {
    mDelegate.reset();
  }


  public boolean markSupported() {
    return mDelegate.markSupported();
  }
  
  
  public int getReadBytesCount() {
    return mTotalReadBytes;
  }

}
