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


public class Html2TxtConverter extends FilterReader {
 
 
  private Reader mBuf;
  public static final int HIDE_ALL=0;
  public static final int A=1 << 1;
  public static final int IMG=1 << 2;
  public static final int I=1 << 3;
    
  
	public Html2TxtConverter(int mode, Reader in) throws IOException {
		super(in);	
    int ch=in.read();
    
    StringWriter out=new StringWriter();
    convert(mode, in,out);
    StringBuffer sb=out.getBuffer();
    mBuf=new StringReader(sb.toString());
	}


  public int read(char[] cbuf, int off, int len) throws IOException {
     return mBuf.read(cbuf,off,len);    
   }
   
  public int read() throws IOException {
    return mBuf.read();
  }

 

	public static void convert(int mode, Reader in, Writer out) throws IOException {
    TagReader reader=new TagReader(in);
    PrintWriter writer=new PrintWriter(out);
    Tag tag=reader.next();
    while (tag!=null) {
      
            
      if (tag.isTextTag()) {
        writer.print(tag.getName());
      }
     
      else if (tag.getTagName()!=null && tag.getTagName().startsWith("br")) {
        writer.print("\t\t"); 
      }
      else if ("p".equals(tag.getName())) {
        writer.println();
      }
      else if ((mode & Html2TxtConverter.I)>0 && "i".equals(tag.getName())) {
        writer.print("@@I");
      }
      else if ((mode & Html2TxtConverter.I)>0 && "/i".equals(tag.getName())) {
        writer.print("@@/I");
      }
      
      else if ((mode & Html2TxtConverter.A)>0 && "a".equals(tag.getTagName())) {
        String href=tag.getAttribute("href");
        if (href!=null) {
          writer.print("@@URL="+href+"@@");
        }
      }
      else if ("td".equals(tag.getTagName())) {
        writer.print("\t");
      }
      else if ("/tr".equals(tag.getTagName())) {
        writer.print("\n");
      }
      else if ((mode & Html2TxtConverter.IMG)>0 && "img".equals(tag.getTagName())) {
        String src=tag.getAttribute("src");
        if (src!=null) {
          writer.print("@@IMG="+src+"@@");
        }
      }      
      tag=reader.next();
    }
  }
  
  
  
}  
  
