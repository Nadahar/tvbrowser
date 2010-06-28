package primarydatamanager.primarydataservice;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;




public class HTMLFilterReader extends FilterReader {
  
  private byte[] mBuf;
  private int mBufPos;
  private int ch;
  private int mCurStyle;
  
 
  
  
  private StringBuffer mBufStr;
  
  public HTMLFilterReader(Reader in) throws IOException {
    super(in);
    mBuf=new byte[0];
    mBufPos=0;
    ch=in.read();
    mCurStyle=Entry.NONE;
  }


  

  public boolean markSupported () {
    return false;
  }
 
  public int read(char[] cbuf, int off, int len) throws IOException {
    
    int character=read();
    if (character==-1) {
      return -1;
    }
    cbuf[off]=(char)character;
    for (int i=1;i<len;i++) {
      character=read();
      cbuf[off+i]=(char)character;
      if (character==-1) {
        return i;
      }
    }
    return len;
    
  }
  
  private String readTag() throws IOException {
    ch=in.read();
    StringBuilder tagBuf = new StringBuilder();
    while (ch!=-1 && ch!='>' && ch!=' ') {
      tagBuf.append(Character.toLowerCase((char)ch));
      ch=in.read();
    }
    return tagBuf.toString();
  }
  
  private String readQuotedAttribute() throws IOException {
    ch=in.read();
    StringBuilder buf = new StringBuilder();
    while (ch!=-1 && ch!='>' && ch!='"') {
      buf.append((char)ch);
      ch=in.read();
    }
    if (ch=='"') {
      ch=in.read();
    }
    return buf.toString();
  }
  
  private String readValueAttribute() throws IOException {
    ch=in.read();
    StringBuilder buf = new StringBuilder();
    while (ch!=-1 && ch!='>' && ch!=' ') {
      buf.append((char)ch);
      ch=in.read();
    }
    if (ch==' ') {
      ch=in.read();
    }
    return buf.toString();
    
  }
  
  private String[] readNextAttribute() throws IOException {
    StringBuilder keyStrBuf = new StringBuilder();
    while (ch==' ') {
      ch=in.read();
    }
    while (ch!=-1 && ch!='>' && ch!=' ' && ch!='=') {   // read key
      keyStrBuf.append(Character.toLowerCase((char)ch));
      ch=in.read();
    }
    
    if (ch=='=') {
      ch=in.read();
      
      
      while (ch==' ') {
        ch=in.read();
      }
      String value=null;
      if (ch=='"') {
        value=readQuotedAttribute();
      }
      else if (ch!=-1 && ch!='>') {
        value=readValueAttribute();
      }
      if (value!=null) {
        return new String[]{
          keyStrBuf.toString(),
          value
        };
      }
      
     
    }
    
    return null;
  }
  
  
 
  
  private boolean fillBuffer() throws IOException {
    
    
    
    mBufStr=new StringBuffer();
  // for (;;) {
    //ch=in.read();
    while (ch!=-1 && ch!='<') {
      ch=in.read();
    }
         
    // begin of a new tag
    if (ch!=-1) {
      String tag=readTag();
      //System.out.println("<"+tag+">");
      if ("b".equals(tag)) {
        mCurStyle=(mCurStyle|Entry.BOLD);
      }
      else if ("/b".equals(tag)) {
        mCurStyle=(byte)(mCurStyle^Entry.BOLD);
      }
      else if ("i".equals(tag)) {
        mCurStyle=(mCurStyle|Entry.ITALIC);
      }
      else if ("/i".equals(tag)) {
        mCurStyle=(byte)(mCurStyle^Entry.ITALIC);
      }
      
      else if ("a".equals(tag)) {
        String[] attrib;
        attrib=readNextAttribute();
        while (attrib!=null) {
          if ("href".equals(attrib[0])) {
            //System.out.println("HREF: "+attrib[1]);
            //mBufStr.append((char)(mCurStyle|LINK));
            mBufStr.append(mCurStyle|Entry.LINK);
            mBufStr.append('\n');
            mBufStr.append(attrib[1]);
            mBufStr.append('\n');
            break;
          }
          attrib=readNextAttribute();
        }
      }
      else if ("img".equals(tag)) {
        //System.out.println("IMG");
        String[] attrib;
        attrib=readNextAttribute();
        while (attrib!=null) {
          if ("alt".equals(attrib[0])) {
            //System.out.println("ALT: "+attrib[1]);
            //mBufStr.append((char)(mCurStyle|IMG_ALT));
            mBufStr.append(mCurStyle|Entry.IMG_ALT);
            mBufStr.append('\n');
            mBufStr.append(attrib[1]);
            mBufStr.append('\n');
            break;
          }
          attrib=readNextAttribute();
        }
      }
      
      while (ch!=-1 && ch!='>') {
        ch=in.read();
      }
      
      
       
    //  mBufStr=new StringBuffer();
      StringBuilder buf = new StringBuilder();
      if (ch!=-1) {
        ch=in.read();
      }
      while (ch!=-1 && ch!='<') {
        if (ch!='\n' && ch!='\r') {
          buf.append((char)ch);
        }
        ch=in.read();
      }
      
      String line=buf.toString().trim();
      if (line.length()>0) {
        //mBufStr.append(mCurStyle);
        //mBufStr.append(createStyleString(style));
        mBufStr.append(mCurStyle);
        mBufStr.append('\n');
        mBufStr.append(line);
        mBufStr.append('\n');
      }
     // if (line.length()>0) System.out.println("["+line+"]");
     //if (line.length()>0) line+='\n';
     // mBuf=line.getBytes();
     // mBufPos=0;
    }
 //  }
 
    
    mBuf=mBufStr.toString().getBytes();
    //mBuf=getNormalizedString(mBufStr).getBytes();
    
    mBufPos=0;
 
    return ch==-1;
  }
 
  public int read() throws IOException {
    
    while (mBufPos>=mBuf.length) {
      if (fillBuffer()) {
        return -1;
      }
    }
    return mBuf[mBufPos++];
    
  /*
    for (;;) {
    
      int ch=in.read();
      if (mState==TEXT) {
        if (ch=='<') {
          mState=TAG;
          mCurTag=new StringBuffer();
          ch=in.read();
          while (ch!=-1 && ch!='>' && ch!=' ') {
            mCurTag.append((char)ch);
            ch=in.read();
          }
          String tag=mCurTag.toString();
          if ("b".equals(tag)) {
            
          }else if ("br".equals(tag)) {
            
          }else if ("img".equals (tag)) {
            
          }else if ("a".equals(tag)) {
            
          }
          
          ch=in.read();
          //return '\n';
        }
        else {
          if (ch!='\n') {
            return ch;
          }
        }
      }
  
      while (mState==TAG) {
        if (ch=='>') {
          mState=TEXT;
          break;
        }
        ch=in.read();
      }
    
    }*/
    
  }



  
  public boolean ready () {
    return true;
  }

}