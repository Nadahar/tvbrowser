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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Tag {

  private static class DeepIterator implements Iterator<Tag> {

    private ArrayList<Tag> list;
    private Iterator<Tag> iterator;

    public DeepIterator(Tag tag) {
      list=new ArrayList<Tag>();
      add(tag);
      iterator=list.iterator();
    }

    private void add(Tag tag) {
      list.add(tag);
      Iterator<Tag> it=tag.shallowIterator();
      while (it.hasNext()) {
        Tag t=it.next();
        add(t);
      }
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Tag next() {
      return iterator.next();
    }

    public void remove() throws UnsupportedOperationException  {
      throw new UnsupportedOperationException();
    }

  }

  protected HashMap<String, String> mAttribMap;
  protected ArrayList<Tag> mSubTags;
  protected String mStrTag;
  protected String mName;
  protected boolean mIsOpenTag, mIsText;

  protected Tag(String txt) {
    this(false,txt);
  }

  protected Tag(boolean isText, String tag) {

    mIsText=isText;
    mAttribMap=new HashMap<String, String>();
    mSubTags=new ArrayList<Tag>();

    if (isText) {
      mName=tag;
     // System.out.println("--->"+tag);
    }
    else {

      mStrTag=tag;
      String[] s=tag.split(" ");
      //System.out.println("{"+s[0]+"}");
      mName=s[0].substring(0,s[0].length()).toLowerCase();
      for (int i=1;i<s.length;i++) {
        int inx=s[i].indexOf('=');
        if (inx>0) {
          String key=s[i].substring(0,inx).toLowerCase();
          String val=s[i].substring(inx+1,s[i].length());

          if (val.length()>2 && val.charAt(0)=='"' && val.charAt(val.length()-1)=='"') {
            val=val.substring(1,val.length()-1);
          }


          mAttribMap.put(key,val);
        }
      }
      mIsOpenTag=(mName.charAt(0)!='/');
      if (!mIsOpenTag) {
        mName=mName.substring(1,mName.length());
      }

      }
    //System.out.println("new"+(isText?" text":"")+(isText||mIsOpenTag?"":" close")+" tag: "+mName);

  }

  public boolean isOpenTag() {
      return !mIsText && mIsOpenTag;
    }
    public boolean isCloseTag() {
      return !mIsText && !isOpenTag();
    }

  public boolean isText() {
    return mIsText;
  }

  public String getAttribute(String attrib) {
    return mAttribMap.get(attrib);
  }

  public String getName() {
    if (isText()) {
      return convertString(mName);
    }
    return mName;
  }

  public void append(String txt) {
    if (!mIsText) {
      throw new RuntimeException("Tag must be a text tag.");
    }
    mName=mName+txt;
  }

  public void add(Tag tag) {
    if (tag!=null) {
      mSubTags.add(tag);
    }
  }

  public String getStrTag() {
    return mStrTag;
  }

  public Tag[] getSubtags() {
    Object []o=mSubTags.toArray();
    Tag[] result=new Tag[o.length];
    for (int i=0;i<o.length;i++) {
      result[i]=(Tag)o[i];
    }
    return result;
  }

  public Tag getSubtag(String name, int cntIgnore) {
    java.util.Iterator<Tag> it=shallowIterator();
    int cnt=0;
    while (it.hasNext()) {
      Tag curTag=it.next();
      if (curTag.getName().equals(name)) {
        if (cntIgnore==cnt) {
          return curTag;
        }
        else {
          cnt++;
        }
      }
    }
    return null;
  }

  public String getSubtext() {
    if (isText()) {
      return mName;
    }
    Tag[] t=getSubtags();
    if (t.length==1 && t[0].isText()) {
      return t[0].getName();
    }
    return null;
  }

  public Tag getSubtag(String name) {
    return getSubtag(name, 0);
  }

  public java.util.Iterator<Tag> shallowIterator() {
    return mSubTags.iterator();
  }

  public java.util.Iterator<Tag> deepIterator() {

   return new DeepIterator(this);
  }

  public Tag find(String name) {
    java.util.Iterator<Tag> it=mSubTags.iterator();
    while (it.hasNext()) {
      Tag t=it.next();
      if (name.equals(t.getName())) {
        return t;
      }
      else {
        Tag res=t.find(name);
        if (res!=null) {
          return res;
        }
      }
    }
    return null;
  }

  public static String getNextText(Iterator<Tag> it) {
    while (it.hasNext()) {
      Tag t=it.next();
      if (t.isText()) {
        return t.getName();
      }
    }
    return null;
  }

  public static Tag getTag(String tagName, Iterator<Tag> it) {
    while (it.hasNext()) {
      Tag t=it.next();
      if (t.getName().equals(tagName)) {
        return t;
      }
    }
    return null;
  }

  public String toString() {
    if (isText()) {
      return "text: \""+mName+"\"";
    }
    return "tag: ["+mName+"] "+(mIsOpenTag?"(open)":"(close)");
  }


  private void replace(StringBuilder buf, String s, char c) {
         int inx;
         inx=buf.indexOf(s);
         while (inx>=0) {
           buf.replace(inx, inx + s.length(), Character.toString(c));
           inx=buf.indexOf(s);
         }
       }



  private String convertString(String s) {

    StringBuilder buf = new StringBuilder(s);

    replace(buf,"&nbsp;",' ');
    replace(buf,"&amp;",'&');

    return buf.toString();
  }



}
