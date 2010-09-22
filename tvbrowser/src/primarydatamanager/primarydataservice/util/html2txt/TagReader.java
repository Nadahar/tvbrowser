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

package primarydatamanager.primarydataservice.util.html2txt;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;


/**
 * this reader reads html tokens (tags and text) from an underlying reader.
 */
public class TagReader {

  /**
   * the underlying reader to read from.
   */
  private Reader mIn;

  /**
   * the encoding of the underlying reader.
   */
  private String mEncoding;

  /**
   * the char that was last read on the last call to next().
   */
  private int mLastReadChar = -1;


  /**
   * creates a new TagReader with the given input.
   *
   * @param in the reader to read from
   */
  public TagReader(final Reader in) {
    mIn = in;
  }


  /**
   * @param encoding the encoding of the input reader
   * @throws UnsupportedEncodingException if the encoding is not supported by the jvm
   */
  public void setEncoding(final String encoding) throws UnsupportedEncodingException {
    new String("".getBytes(), encoding); // create a string to test the encoding
    mEncoding = encoding;
  }


  /**
   * @return the next element (tag or text) or null if no more elements are available
   */
  public Tag next() {
    try {
      //if next was never called before, mLastReadChar is -1. in that case
      //we have to read the first char from the stream. mLastReadChar may
      //also be -1 if the end of stream was reached during the last call to
      //next. then the next call to read will return -1 as well.
      //if mLastReadChar != -1 this method was called before and we have to
      //deal with the last read char (which is not in the mIn-reader anymore).
      int nextChar = mLastReadChar == -1 ? mIn.read() : mLastReadChar;

      //if the stream ended, return
      if (nextChar == -1) {
        return null;
      }

      //check, whether we have to deal with a tag or with text
      HTMLTag tag;
      if (nextChar == '<') {
        tag = new HTMLTag(true);

        //ignore the < so jump to the next char
        nextChar = mIn.read();
        //read all chars until '>'
        while (nextChar != -1 && nextChar != '>') {
          tag.append((char) nextChar);
          nextChar = mIn.read();
        }

        //the > belongs to the tag but we want to ignore it so jump to the next char
        nextChar = mIn.read();
      } else {
        tag = new HTMLTag(false, mEncoding);

        //read all chars until '<'
        while (nextChar != -1 && nextChar != '<') {
          tag.append((char) nextChar);
          nextChar = mIn.read();
        }
      }

      //remember the last read char which is not in the input reader anymore!
      mLastReadChar = nextChar;

      //ignore whitespace
      if (StringUtils.isBlank(tag.getName())) {
        return next();
      } else {
        return tag;
      }

    } catch (final IOException e) {
      return null;
    }
  }
}


class HTMLTag implements Tag {

  private StringBuffer mBuf;
  private boolean mIsTag;
  private char mPrevChar;
  private boolean mIsConverted;
  private String mEncoding;

  public HTMLTag(boolean isTag) {
    mBuf = new StringBuffer();
    mIsTag = isTag;
    mPrevChar = 0;
    mIsConverted = true;
  }

  public HTMLTag(boolean isTag, String encoding) {
    this(isTag);
    mEncoding = encoding;
  }

  public boolean isTextTag() {
    return !mIsTag;
  }

  public String getTagName() {
    if (mIsTag) {
      String res = mBuf.toString().toLowerCase();
      int p = res.indexOf(' ');
      if (p > 0) {
        return res.substring(0, p);
      }
      return res;
    }
    return "";
  }

  public String getName() {
    if (!mIsConverted && !mIsTag) {
      mBuf = convert(mBuf);
      mIsConverted = true;
    }
    return mBuf.toString();
  }


  public String getAttribute(String attributeName) {

    if (mIsTag) {
      String[] attributes = mBuf.toString().split(" ");
      if (attributes.length > 1) {
        for (int i = 1; i < attributes.length; i++) {
          int p = attributes[i].indexOf('=');
          if (p > 0) {
            String key = attributes[i].substring(0, p);
            String val = attributes[i].substring(p + 1, attributes[i].length());
            if (key.trim().equalsIgnoreCase(attributeName)) {
              return val.trim();
            }
          }
        }
      }
    }
    return null;
  }

  public void append(char ch) {
    mIsConverted = false;
    if (Character.isWhitespace(ch)) {
      if (mPrevChar != ' ') {
        mBuf.append(' ');
        mPrevChar = ' ';
      }
    }
    else {
      mBuf.append(ch);
      mPrevChar = ch;
    }
  }


  private StringBuffer convert(StringBuffer line) {
    StringBuffer result = null;
    if (mEncoding != null) {
      try {
        result = new StringBuffer(new String(line.toString().getBytes(mEncoding)));
      } catch (final UnsupportedEncodingException e) {
        // ignore
        result = new StringBuffer();
      }
    }
    else {
      result = new StringBuffer(line.toString());
    }

    return new StringBuffer(StringEscapeUtils.unescapeHtml(result.toString()));
  }


  @Override
  public String toString() {
    return getName();
  }
}
