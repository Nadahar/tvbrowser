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
 *     $Date: 2004-08-22 13:13:53 +0200 (So, 22 Aug 2004) $
 *   $Author: troggan $
 * $Revision: 652 $
 */
package util.ui.progress;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import devplugin.ProgressMonitor;

/**
 * This inputstream can display the current position of a file using a
 * ProgressMonitor.
 *
 * @since 2.7
 */
public class ProgressInputStream extends FilterInputStream {
  private ProgressMonitor mMonitor;
  private int mPosition;

  /**
   * Creates the Stream
   *
   * @param in the underlying input stream
   * @param monitor display the progress in this monitor
   */
  public ProgressInputStream(InputStream in, ProgressMonitor monitor) {
    this(in, monitor, 0);
  }

  /**
   * Creates the Stream
   *
   * @param in the underlying input stream
   * @param monitor display the progress in this monitor
   * @param position start at this position
   */
  public ProgressInputStream(InputStream in, ProgressMonitor monitor, int position) {
    super(in);
    mMonitor = monitor;
    mPosition = position;
  }

  @Override
  public int read() throws IOException {
    mPosition += 1;
    mMonitor.setValue(mPosition);
    return super.read();
  }

  @Override
  public int read(byte b[]) throws IOException {
    int read = super.read(b);
    mPosition += read;
    mMonitor.setValue(mPosition);
    return read;
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    int read = super.read(b, off, len);
    mPosition += read;
    mMonitor.setValue(mPosition);
    return read;
  }

  @Override
  public long skip(long n) throws IOException {
    long skip = super.skip(n);
    mPosition += skip;
    mMonitor.setValue(mPosition);
    return skip;
  }

  @Override
  public void close() throws IOException {
    super.close();
  }

  public int getCurrentPosition() {
    return mPosition;
  }
}
