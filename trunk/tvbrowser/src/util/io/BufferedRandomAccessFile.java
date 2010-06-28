/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * buffered RandomAccessFile for faster reading
 * @author mikepple
 *
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

  private byte[] buffer;

  private int buf_end = 0;

  private int buf_pos = 0;

  private long real_pos = 0;

  private static int BUF_SIZE = 32000;

  public BufferedRandomAccessFile(File file, String mode) throws IOException {
    super(file, mode);
    init();
  }

  public BufferedRandomAccessFile(String filename, String mode)
      throws IOException {
    super(filename, mode);
    init();
  }

  private void init() throws IOException {
    invalidate();
    buffer = new byte[BUF_SIZE];
  }

  public final int read() throws IOException {
    if (buf_pos >= buf_end) {
      if (fillBuffer() < 0) {
        return -1;
      }
    }
    if (buf_end == 0) {
      return -1;
    } else {
      int res = buffer[buf_pos++];
      if (res < 0) {
        res += 256;
      }
      return res;
    }
  }

  private int fillBuffer() throws IOException {
    int n = super.read(buffer, 0, BUF_SIZE);
    if (n >= 0) {
      real_pos += n;
      buf_end = n;
      buf_pos = 0;
    }
    return n;
  }

  private void invalidate() throws IOException {
    buf_end = 0;
    buf_pos = 0;
    real_pos = super.getFilePointer();
  }

  public int read(byte b[], int off, int len) throws IOException {
    int leftover = buf_end - buf_pos;
    if (len <= leftover) {
      System.arraycopy(buffer, buf_pos, b, off, len);
      buf_pos += len;
      return len;
    }
    for (int i = 0; i < len; i++) {
      int c = this.read();
      if (c != -1) {
        b[off + i] = (byte) c;
      } else {
        return i;
      }
    }
    return len;
  }

  public long getFilePointer() throws IOException {
    long l = real_pos;
    return (l - buf_end + buf_pos);
  }

  public void seek(long pos) throws IOException {
    int n = (int) (real_pos - pos);
    if (n >= 0 && n <= buf_end) {
      buf_pos = buf_end - n;
    } else {
      super.seek(pos);
      invalidate();
    }
  }

}
