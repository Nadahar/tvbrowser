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


public class TagReader {

  private Reader mIn;
  private String mEncoding;

  private boolean mExpectTag;
  private int mChar;

  public TagReader(Reader in) throws IOException {
    mIn=in;
    mChar=in.read();

    mExpectTag=true;
  }

  public void setEncoding(String encoding) throws UnsupportedEncodingException {
     new String("".getBytes(), encoding); // create a string to test the encoding
     mEncoding=encoding;
  }

  public Tag next() {
    HTMLTag tag;
    try {
      if (mExpectTag) {
        tag=new HTMLTag(true);
        while (mChar!=-1 && mChar!='<') {
          mChar=mIn.read();
        }
      mChar=mIn.read();

      while (mChar!=-1 && mChar!='>') {
        tag.append((char)mChar);
        mChar=mIn.read();
      }
      mChar=mIn.read();

    }
    else {
      tag=new HTMLTag(false, mEncoding);
      while (mChar!=-1 && mChar!='<') {
        tag.append((char)mChar);
        mChar=mIn.read();
      }
    }

    mExpectTag=!mExpectTag;

    if (mChar==-1) return null;

    }catch(IOException e) {
      return null;
    }

    if (StringUtils.isBlank(tag.getName())) {
      return next();
    }else{
      return tag;
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
    mBuf=new StringBuffer();
    mIsTag=isTag;
    mPrevChar=0;
    mIsConverted=true;
  }

  public HTMLTag(boolean isTag, String encoding) {
    this(isTag);
    mEncoding=encoding;
  }

  public boolean isTextTag() {
    return !mIsTag;
  }

  public String getTagName() {
    if (mIsTag) {
      String res=mBuf.toString().toLowerCase();
      int p = res.indexOf(' ');
      if (p>0) {
        return res.substring(0,p);
      }
      return res;
    }
    return "";
  }

  public String getName() {
    if (!mIsConverted && !mIsTag) {
      mBuf=convert(mBuf);
      mIsConverted=true;
    }
    return mBuf.toString();
  }


  public String getAttribute(String attributeName) {

    if (mIsTag) {
      String[] attributes=mBuf.toString().split(" ");
      if (attributes.length>1) {
        for (int i=1;i<attributes.length;i++) {
          int p = attributes[i].indexOf('=');
          if (p>0) {
            String key=attributes[i].substring(0,p);
            String val=attributes[i].substring(p+1,attributes[i].length());
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
    mIsConverted=false;
    if (Character.isWhitespace(ch)) {
      if (mPrevChar!=' ') {
        mBuf.append(' ');
        mPrevChar=' ';
      }
    }
    else {
      mBuf.append(ch);
      mPrevChar=ch;
    }


  }


   private StringBuffer convert(StringBuffer line) {
     StringBuffer result=null;
     if (mEncoding!=null) {
       try {
				 result=new StringBuffer(new String(line.toString().getBytes(mEncoding)));
       } catch (UnsupportedEncodingException e) {
      	 // ignore
				 result = new StringBuffer();
			 }
     }
     else {
       result=new StringBuffer(line.toString());
     }

      return new StringBuffer(StringEscapeUtils.unescapeHtml(result.toString()));
    }


  public String toString() {
    return getName();
  }

}