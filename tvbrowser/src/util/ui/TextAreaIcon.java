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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;

import javax.swing.Icon;

/**
 * An icon that displays multiline text.
 *
 * @author Martin Oberhauser
 */
public class TextAreaIcon implements Icon {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TextAreaIcon.class.getName());
  
  private int mWidth;
  private String[] mTextLineArr;
  private Font mFont;
  private int mMaxLineCount = -1;
  private String mText;


  /**
   * Creates a TextAreaIcon with the specified text, font and width.
   */
  public TextAreaIcon(String text, Font font, int width) {
    mWidth = width;
    mFont = font;
    setText(text);
  }
  
  public void setFont(Font font) {
    mFont = font;
    if (mTextLineArr!=null) System.out.print("changed from "+mTextLineArr.length);
    setText(mText);
    if (mTextLineArr!=null) System.out.println(" to "+mTextLineArr.length);
    
  }
  
  public void setMaximumLineCount(int maxLineCount) {
    mMaxLineCount = maxLineCount;
  }
  
  
  
  public int getMaximumLineCount() {
    return mMaxLineCount;
  }


  public void setText(String text) {
    mText = text;
    StringReader reader;
    
    if (text == null) {
      reader = null;
    } else {
      reader = new StringReader(text);
    }

    try {
      setText(reader);
    }
    catch (IOException exc) {
      // A StringReader never throws an IOException
      mLog.log(Level.WARNING, "Reading String failed: '" + text + "'", exc);
    }
  }


  public void setText(Reader textReader) throws IOException {
    if (textReader == null) {
      mTextLineArr = null;
    } else {
      TextLineBreaker breaker = new TextLineBreaker();
      mTextLineArr = breaker.breakLines(textReader, mFont, mWidth, mMaxLineCount);
    }
  }
  
  
  // implements Icon
  
  
  /**
   * Returns the icon's height.
   *
   * @return an int specifying the fixed height of the icon.
   */
  public int getIconHeight() {
    if (mTextLineArr == null) {
      return 0;
    } else {
      return mFont.getSize() * mTextLineArr.length;
    }
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
    if (mTextLineArr != null) {
      grp.setFont(mFont);
      
      int fontSize = mFont.getSize();
      for (int i = 0; i < mTextLineArr.length; i++) {
        y += fontSize;
        grp.drawString(mTextLineArr[i], x, y);
      }
    }
  }
  
}