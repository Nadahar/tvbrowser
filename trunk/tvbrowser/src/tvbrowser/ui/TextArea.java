/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.ui;


import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


public class TextArea  {

  class Substring {
    private int begin;
    private int end;
    private char[] text;

    public Substring(char[] text, int begin, int end) {
      init(text,begin,end);
    }

    public Substring() {
    }

    public void init(char[] text, int begin, int end) {
      this.begin=begin;
      this.end=end;
      this.text=text;
    }

    public String toString() {
      return new String(text,begin,end-begin+1);
    }
  }

  private int width=0;
  private int fontSize=0;
  private char[] text;
  private Object[] lines;
  private Font font;

  private static FontRenderContext frc=new FontRenderContext(new AffineTransform(),false,false);

  /**
   * Creates a TextArea with the specified text, font and width.
   */
  public TextArea(char[] text, Font font, int width) {

    this.width=width;
    this.text=text;
    this.font=font;
    fontSize=font.getSize();
    ArrayList linesList=new ArrayList();

    int inx=0;
    Substring curSubstring;
    do {
      curSubstring=new Substring();
      inx=getNextSubstring(text,inx,curSubstring);
      if (inx==-1) {
        break;
      }else{
        linesList.add(curSubstring);
      }

    }while(true);
    lines=linesList.toArray();

  }

  private int getWidth(char[] text, int start, int end) {
    Rectangle2D rect;
    try {
      rect=font.getStringBounds(text,start,end,frc);
    }catch(IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException("start: "+start+", end: "+end);
    }
    return (int)rect.getWidth();
  }

  private int getNextDelimiterPos(char[] text, int inx) {

    if (inx>=text.length) {
      return -1;
    }

    while (inx<text.length && text[inx]!=' ' && text[inx]!='-' && text[inx]!='\n') {
      inx++;
    }

    if (inx<text.length && text[inx]=='-') return inx;

    return inx-1;
  }

  private int getNextSubstring(char[] text, int inx, Substring substring) {

    if (inx>=text.length-1) {
      return -1;
    }

    while (inx<text.length && text[inx]==' ') {   // ignore blanks
      inx++;
    }

    int start=inx;
    int inxOld=-1;
    do {
      if (inxOld==inx) {
        throw new RuntimeException("inxOld==inx=="+inx);
      }
      inxOld=inx;
      inx=getNextDelimiterPos(text,inx+1);
      if (inx==-1) {
        substring.init(text,start,inxOld-1);
        return inxOld;
      }

      if (inx+1<text.length && text[inx+1]=='\n') {  // force line break
        substring.init(text,start,inx);
        return inx+2;
      }

      int w=getWidth(text,start,inx);
      if (w>width) {
        if (start==inxOld) {
          substring.init(text,start,inx);
          return inx+1;
        }else{
          substring.init(text,start,inxOld-1);
          return inxOld;
        }
      }
      inx++;

    }while(true);


  }

  /**
   * Returns the height of the TextArea. You can't changed the height.
   */
  public int getHeight() {
    return fontSize*lines.length;
  }

  /**
   * Paints the TextArea at the specified position.
   */
  public void paint(Graphics g, int x, int y) {

    g.setFont(font);

    for (int i=0;i<lines.length;i++) {
      String line=((Substring)lines[i]).toString();
      g.drawString(line,x,y+(i+1)*fontSize);
    }
  }

}