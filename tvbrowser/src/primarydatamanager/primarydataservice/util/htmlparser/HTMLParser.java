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

package primarydatamanager.primarydataservice.util.htmlparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

public class HTMLParser {

  private InputStream in;
  private int la;
  private int ch;

  private HTMLParser(InputStream in) throws IOException {
    this.in=in;
    la=in.read();
  }

  private void next() throws IOException {
    ch=la;
    if (ch!=-1) {
      la=in.read();
    }
    else {
      la=-1;
    }
  }


  private String getText() throws IOException {

    StringBuilder buf = new StringBuilder();
    while (ch=='\n') {
       next();
    }

    boolean wasSpace=false;
    while(ch!=-1 && ch!='<') {

      if (Character.isWhitespace((char)ch)) {
        if (wasSpace) {
          // ignore character
        }
        else {
          wasSpace=true;
          buf.append(' ');
        }
      }
      else {
        wasSpace=false;
        buf.append((char)ch);
      }
      next();
    }

    String text=buf.toString().trim();
    if (StringUtils.isEmpty(text)) {
      return null;
    }
    return buf.toString();



  }

  private String getTag() throws IOException {
    StringBuilder buf = new StringBuilder();

    if (ch!='<') {
      //throw new RuntimeException("'<' expected.");
      return null;
    }
    while (la!=-1 && la!='>') {
      buf.append((char)la);
      next();
    }
    next();
    next();
    String res=buf.toString();
    if (StringUtils.isEmpty(res)) {
      return null;
    }
    return res;


  }


  private Iterator<Tag> getTags() throws IOException {
    ArrayList<Tag> tags=new ArrayList<Tag>();
    Tag curTextTag=null;
    String txt;
    do {
      txt=getText();
      if (txt!=null) {
        if (curTextTag==null) {
          curTextTag=new Tag(true, txt);
          tags.add(curTextTag);
        }
        else {
          curTextTag.append(txt);
        }
      }
      txt=getTag();
      if (txt!=null) {

        Tag t=new Tag(txt);
        if (curTextTag!=null && ("br".equals(t.getName()) || "p".equals(t.getName()))) {
          curTextTag.append("\n");
        }
        else if ((t.getName().startsWith("!--"))) {
          // ignore
        }
        else {
          //tags.add(new Tag(txt));

          //tags.add(t);
          if (txt.endsWith("/")) {
            txt=txt.substring(0,txt.length()-1);
            tags.add(new Tag(txt));
            tags.add(new Tag("/"+txt));
          }
          else {
            tags.add(t);
          }
          curTextTag=null;
        }
      }
    }while (txt!=null);
    //Object []o=tags.toArray();
    //for (int i=0;i<o.length;i++) {
    //  System.out.println(o[i]);
    //}
    return tags.iterator();

  }


  private Tag addTag(int depth, Tag openTag, Iterator<Tag> it) {

      while (it.hasNext()) {
        Tag curTag=it.next();
        if (curTag.isText()) {
          openTag.add(curTag);
        }
        else if (curTag.isCloseTag()) {
          return curTag;
        }
        else if (curTag.isOpenTag()) {
          openTag.add(curTag);
          Tag t=addTag(depth+1,curTag,it);
          if (!curTag.getName().equals(t.getName())) {
            return t;
          }
        }
      }
      return null;
    }



  public static Tag parse(InputStream in) throws IOException {

    HTMLParser parser=new HTMLParser(in);
    Iterator<Tag> it=parser.getTags();
    Tag result=it.next();
    parser.addTag(0,result,it);
    return result;
  }

  public static void dumpTag(PrintStream out, Tag tag) {
    dumpTag(out,0,tag);
  }

  private static void dumpTag(PrintStream out, int depth, Tag t) {
    for (int i=0;i<depth;i++) out.print(" ");
    out.println(t.toString());
    Tag[] list=t.getSubtags();
    for (int i=0;i<list.length;i++) {
      dumpTag(out,depth+1,list[i]);
    }

  }




}