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

import java.io.*;

import primarydatamanager.primarydataservice.util.Entities;

public class TagReader {
  
  private Reader mIn;
  private HTMLTag mCurTag;
 
  private boolean mExpectTag;
  private int mChar;
  
  public TagReader(Reader in) throws IOException {
    mIn=in;
    mChar=in.read();
    
    mExpectTag=true;
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
      tag=new HTMLTag(false);
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
    
    if (tag.getName().trim().length()==0) {
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
  
  public HTMLTag(boolean isTag) {
    mBuf=new StringBuffer();
    mIsTag=isTag;
    mPrevChar=0;
    mIsConverted=true;
  }
  
  public boolean isTextTag() {
    return !mIsTag;
  }
  
  public String getTagName() {
    if (mIsTag) {
      String res=mBuf.toString();
      int p=res.indexOf(" ");
      if (p>0) {
        return res.substring(0,p);
      }
      return res;
    }
    return "";
  }
  
  public String getName() {
    if (!mIsConverted && !mIsTag) {
      convert(mBuf);
      mIsConverted=true;
    }
    return mBuf.toString();
  }
  
 
  public String getAttribute(String attributeName) {
    
    if (mIsTag) {
      String[] attributes=mBuf.toString().split(" ");
      if (attributes.length>1) {
        for (int i=1;i<attributes.length;i++) {
          int p=attributes[i].indexOf("=");
          if (p>0) {
            String key=attributes[i].substring(0,p);
            String val=attributes[i].substring(p+1,attributes[i].length());
            if (key.trim().equals(attributeName)) {
              return val.trim();
            }
          }          
        }        
      }    
    }
    return null;
  }
  
  public void append(char ch) {
    char next;
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

  private void replaceEntity(StringBuffer line, int from, int to) {
     String entity=line.substring(from,to+1);
     line.replace(from,to+1,Entities.decode(entity));     
   }
  
   private void convert(StringBuffer line) {
     int from=0;
     while (from<line.length()) {      
       if (line.charAt(from)=='&') {
         int to=from+1;
         while (to<line.length()) {
           if (line.charAt(to)==';') {
             replaceEntity(line,from,to);
             break;
           }
           to++;
         }        
       }
       from++;
      
     }
    }


  public String toString() {
    return getName();
  }
  
}