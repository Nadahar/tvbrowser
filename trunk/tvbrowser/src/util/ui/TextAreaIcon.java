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

package util.ui;

import java.util.ArrayList;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * An icon that displays multiline mText.
 *
 * @author Martin Oberhauser
 */
public class TextAreaIcon implements Icon {

  /** The helper label. */  
  private static final JLabel HELPER_LABEL = new JLabel();
  
  private int mWidth;
  private String mText;
  private ArrayList mTextLineList;
  private Font mFont;
  private FontMetrics mFontMetrics;
  private int mMaxLineCount = -1;



  /**
   * Creates a TextAreaIcon with the specified mText, mFont and mWidth.
   */
  public TextAreaIcon(String text, Font font, int width) {
    mWidth = width;
    mFont = font;
    mFontMetrics = HELPER_LABEL.getFontMetrics(font);
    
    mTextLineList = new ArrayList();
    setText(text);
  }
  
  
  
  public void setMaximumLineCount(int maxLineCount) {
    mMaxLineCount = maxLineCount;
  }



  public void setText(String text) {
    mText = text;

    // Es gibt zwei Dinge in der Java API, die daran Schuld, dass dieses Icon
    // nicht signifikant beschleunigt werden kann:
    //
    // 1. Man kann mit der FontMetrics-Klasse keine Teile eines String messen,
    //    sondern nur Teile eines char[]
    // 2. Man kann mit der Graphics-Klasse keine Teile eines Strings malen
    //    und man kann zwar Teile eines char[] malen aber die Implementierung
    //    von Graphics.drawChars(char data[], int offset, int length, int x, int y)
    //    sieht folgendermassen aus :-(( :
    //    drawString(new String(data, offset, length), x, y);
    //
    // Das heiﬂt man kann nur malen, ohne Objekte zu erzeugen, indem man den
    // Text vorher in Zeilen aufteilt. Und dabei muss man leider tempor‰r ein
    // char[] aus dem Text erzeugen, damit man Teile des Textes vermessen kann.
    
    mTextLineList.clear();
    
    if (text != null) {
      int inx = 0;
      char[] textData = text.toCharArray();
      while (inx < text.length()) {
        int start = getNextLineStart(textData, inx);
        inx = getNextLineEnd(textData, start, mWidth, mFontMetrics);
        String line = text.substring(start, inx);

        if ((mMaxLineCount != -1) && (mTextLineList.size() + 1 >= mMaxLineCount)) {
          if (inx < text.length()) {
            line += "...";
          }
          mTextLineList.add(line);
          break;
        }
        mTextLineList.add(line);
      }
      
      if (mMaxLineCount != -1 && mTextLineList.size() > mMaxLineCount) {
        System.out.println("mMaxLineCount: " + mMaxLineCount
          + ", mTextLineList.size(): " + mTextLineList.size());
      }
    }
  }



  private static int getNextDelimiterPos(char[] text, int inx) {
    while (inx < text.length) {
      if ((text[inx] == ' ') || (text[inx] == '\n')) {
        return inx;
      }
      else if (text[inx] == '-') {
        return inx + 1;
      }
      
      inx++;
    }

    // The end of text has been reached
    return text.length;
  }

  
  
  private static int getNextLineStart(char[] text, int inx) {
    // ignore blanks
    while ((inx < text.length) && (text[inx] == ' ')) {
      inx++;
    }
    
    return inx;
  }
  
  

  private static int getNextLineEnd(char[] text, int inx, int maxWidth,
    FontMetrics fontMetrics)
  {
    int start = inx;
    int width = 0;
    int widthIdx = start; // The index up to which the width is already calculated
    while (inx < text.length) {
      int inxOld = inx;
      inx = getNextDelimiterPos(text, inx + 1);

      if ((inx < text.length) && (text[inx] == '\n')) {
        // force line break
        return inx;
      }

      width += fontMetrics.charsWidth(text, widthIdx, inx - widthIdx);
      widthIdx = inx;
      if (width > maxWidth) {
        if (start == inxOld) {
          return inx;
        } else {
          return inxOld;
        }
      }
    }
    
    // The end of the text has been reached
    return text.length;
  }
  
  
  // implements Icon
  
  
  /**
   * Returns the icon's height.
   *
   * @return an int specifying the fixed height of the icon.
   */
  public int getIconHeight() {
    return mFont.getSize() * mTextLineList.size();
  }  
  
  
  
  /**
   * Returns the icon's mWidth.
   *
   * @return an int specifying the fixed mWidth of the icon.
   */
  public int getIconWidth() {
    return mWidth;
  }
  
  
  
  /**
   * Draw the icon at the specified location.  Icon implementations
   * may use the Component argument to get properties useful for
   * painting, e.g. the foreground or background color.
   */
  public void paintIcon(Component comp, Graphics grp, int x, int y) {
    grp.setFont(mFont);
    
    int fontSize = mFont.getSize();

    for (int i = 0; i < mTextLineList.size(); i++) {
      y += fontSize;
      String line = (String) mTextLineList.get(i);
      grp.drawString(line, x, y);
    }
  }
  
}