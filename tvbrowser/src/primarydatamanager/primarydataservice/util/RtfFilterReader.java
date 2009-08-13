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

package primarydatamanager.primarydataservice.util;


import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;

public class RtfFilterReader extends FilterReader {

  private int depth = 0;
  private static int DEPTH = 2;
  private int minDepth;
  private ArrayList<Integer> buffer = new ArrayList<Integer>();
  private int bufferCursor = 0;

  public RtfFilterReader(Reader in) {
    this(in, DEPTH);
  }

  public RtfFilterReader(Reader in, int depth) {
    super(in);
    this.depth = 0;
    minDepth = depth;
  }

  public boolean markSupported() {
    return false;
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    int character = read();
    if (character == -1) {
      return -1;
    }
    cbuf[off] = (char) character;
    for (int i = 1; i < len; i++) {
      character = read();
      cbuf[off + i] = (char) character;
      if (character == -1) {
        return i;
      }
    }
    return len;

  }

  public int read() throws IOException {
    if (buffer.size() > 0) {
      int value = buffer.get(bufferCursor);
      bufferCursor++;
      if (bufferCursor == buffer.size()) {
        buffer = new ArrayList<Integer>();
      }
      return value;
    }


    int ch = getNext();

    while (ch == '\\' && ch != -1) {

      StringBuilder buf = new StringBuilder();

      ch = getNext();

      if (ch == '\'') {
        buf.append((char) getNext());
        buf.append((char) getNext());
        String specChar = buf.toString();

        if (specChar.matches("[a-z0-9]{2}")) {
          // returns '\u00ab' etc.
          BigInteger unicodeValue = new BigInteger("00"+specChar, 16);
          return (char)unicodeValue.intValue();
        }
      } else {
        do {
          buf.append((char) ch);
          ch = getNext();
        } while (ch != -1 && ch != ' ');

        String specChar = buf.toString();

        int translated = translateSpecChar(specChar);
        if (translated != 0) {
          return translated;
        }
      }

      ch = getNext();
    }
    return ch;
  }


  protected char translateSpecChar(String specChar) {
    if (specChar.equals("tab")) {
      return '\t';
    }
    if (specChar.equals("par")) {
      return '\n';
    }
    if (specChar.equals("line")) {
      return '\n';
    }

    return 0;
  }

  private int getNext() throws IOException {
    int ch;
    StringBuilder command = new StringBuilder();
    do {
      do {
        ch = in.read();
        command.append((char) ch);

        // Ignore newlines and carriage returns
        while ((ch == '\n') || (ch == '\r')) {
          ch = in.read();
          command.append((char) ch);
        }

        if (ch == '{') {
          depth++;
        } else if (ch == '}') {
          depth--;
        }
      } while (ch == '{' || ch == '}');
    } while (depth != minDepth && ch != -1);

    String str = parseCommand(command.toString());
    if (str != null && str.length() > 0) {
      for (byte b : str.getBytes()) {
        buffer.add((int) b);
      }
      bufferCursor = 0;
    }

    return ch;
  }

  public String parseCommand(String command) {
    return null;
  }

  public int readOLD() throws IOException {
    int ch = in.read();

    for (; ;) {
      if (ch == '{' || ch == '}') {
        ch = in.read();
        continue;
      }

      while (ch == '\\') {
        ch = in.read();

        while (ch != -1 && ch != ' ' && ch != '{') {
          ch = in.read();
        }
        if (ch == '{') {
          ch = in.read();
        }
      }

      return ch;

    }

  }

  public boolean ready() {
    return true;
  }

}