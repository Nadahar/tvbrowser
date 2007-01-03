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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowserdataservice.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import util.io.IOUtilities;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class Mirror {

  public static final String MIRROR_LIST_FILE_NAME = "mirrorlist.gz";

  public static final int DEFAULT_WEIGHT = 100;

  private String mUrl;

  private int mWeight;

  /**
   * @param url
   * @param weight
   */
  public Mirror(String url, int weight) {
    // Escape spaces in the URL
    mUrl = IOUtilities.replace(url, " ", "%20");
    mWeight = weight;
  }

  public Mirror(String url) {
    this(url, DEFAULT_WEIGHT);
  }

  public String getUrl() {
    return mUrl;
  }

  public int getWeight() {
    return mWeight;
  }

  public void setWeight(int weight) {
    mWeight = weight;
  }

  public static Mirror[] readMirrorListFromStream(InputStream stream) throws IOException, FileFormatException {
    GZIPInputStream gIn = new GZIPInputStream(stream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(gIn));

    ArrayList<Mirror> list = new ArrayList<Mirror>();
    String line;
    int lineCount = 1;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        // This is not an empty line -> read it

        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() < 2) {
          throw new FileFormatException("Syntax error in mirror file line " + lineCount + ": '" + line + "'");
        }

        String url = tokenizer.nextToken();
        String weightAsString = tokenizer.nextToken();
        int weight;
        try {
          weight = Integer.parseInt(weightAsString);
        } catch (Exception exc) {
          throw new FileFormatException("Syntax error in mirror file line " + lineCount + ": wieght is not a number: '"
              + weightAsString + "'");
        }

        list.add(new Mirror(url, weight));
      }
      lineCount++;
    }

    gIn.close();

    Mirror[] mirrorArr = new Mirror[list.size()];
    list.toArray(mirrorArr);

    return mirrorArr;
  }

  public static Mirror[] readMirrorListFromFile(File file) throws IOException, FileFormatException {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 0x2000);

      return readMirrorListFromStream(stream);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  public static void writeMirrorListToStream(OutputStream stream, Mirror[] mirrorArr) throws IOException {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    PrintWriter writer = new PrintWriter(gOut);
    for (int i = 0; i < mirrorArr.length; i++) {
      writer.print(mirrorArr[i].getUrl());
      writer.print(";");
      writer.println(String.valueOf(mirrorArr[i].getWeight()));
    }
    writer.close();

    gOut.close();
  }

  public static void writeMirrorListToFile(File file, Mirror[] mirrorArr) throws IOException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    // outer block.

    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);

        writeMirrorListToStream(stream, mirrorArr);
      } finally {
        // Close the file in every case
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException exc) {
          }
        }
      }
    } catch (IOException exc) {
      file.delete();
      throw exc;
    }
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((mUrl == null) ? 0 : mUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Mirror other = (Mirror) obj;
    if (mUrl == null) {
      if (other.mUrl != null)
        return false;
    } else if (!mUrl.equals(other.mUrl))
      return false;
    return true;
  }

}
