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

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


public class Html2TxtConverter extends FilterReader {


  private Reader mBuf;
  public static final int HIDE_ALL=0;
  public static final int A=1 << 1;
  public static final int IMG=1 << 2;
  public static final int I=1 << 3;

  private String mEncoding;


  public Html2TxtConverter(int mode, Reader in, String encoding) throws IOException {
    super(in);

    in.read(); // unused

    StringWriter out=new StringWriter();
    convert(mode, in,out,encoding);
    StringBuffer sb=out.getBuffer();
    mBuf=new StringReader(sb.toString());

  }

	public Html2TxtConverter(int mode, Reader in) throws IOException {
		this(mode, in, null);
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

  public int read() throws IOException {
    int result=mBuf.read();
    if (result==160) {
      result=' ';
    }
    return result;
  }

  private String encode(String s) {
    if (s==null) {
      return null;
    }

    String result=s;
    try {
			result = new String(s.getBytes(), mEncoding);
		} catch (UnsupportedEncodingException e) {
			// ignore
		}
    return result;
  }

  public static String convert(int mode, String in) {
    return convert(mode, in, null);
  }

  public static String convert(int mode, String in, String encoding) {
    StringBuffer content = new StringBuffer();
    StringReader input = new StringReader(in);

    try {
    Html2TxtConverter txtReader = new Html2TxtConverter(mode, input, encoding);
    BufferedReader reader = new BufferedReader(txtReader);

    String line = reader.readLine();
    while (line != null) {
      content.append(line).append("\n");
      line = reader.readLine();
    }
    } catch (IOException e) {
      //can't happen since it's no real IO operation
    }
   return content.toString();
  }

  public static void convert(int mode, Reader in, Writer out) throws IOException {
    convert(mode, in, out, null);
  }

	public static void convert(int mode, Reader in, Writer out, String encoding) throws IOException {
    TagReader reader=new TagReader(in);
    if (encoding!=null) {
      reader.setEncoding(encoding);
    }
    PrintWriter writer=new PrintWriter(out);
    Tag tag=reader.next();
    while (tag!=null) {


      if (tag.isTextTag()) {
        writer.print(tag.getName());
      }

      else if (tag.getTagName()!=null && tag.getTagName().startsWith("br")) {
        writer.print("\n");
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
	
  static final public String replaceUnsupportedChars(String text) {
    String result = text;
   
    result = result.replace((char) 45, '-');
    result = result.replace((char) 128, (char) 8364); // Euro-Zeichen
    result = result.replace((char) 130, ',');
    result = result.replace((char) 132, (char) 8222); // doppeltes low-9-Zeichen rechts
    result = result.replace(String.valueOf((char) 133), "...");
    result = result.replace((char) 145, (char) 8216); //einfaches Anführungszeichen links
    result = result.replace((char) 146, (char) 8217); //einfaches Anführungszeichen rechts
    result = result.replace((char) 147, (char) 8220); // doppeltes Anführungszeichen links
    result = result.replace((char) 148, (char) 8221); // doppeltes Anführungszeichen rechts
    result = result.replace((char) 150, '-');
    result = result.replace((char) 153, (char) 8482); //TM-Zeichen   
    result = result.replace((char) 8211, '-');
   
    return result;   
  } 
}

