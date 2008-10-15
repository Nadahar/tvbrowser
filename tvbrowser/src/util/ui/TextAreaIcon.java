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

import util.misc.TextLineBreakerFontWidth;

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
  private int mLineSpace;

  /**
   * Creates a TextAreaIcon with the specified text, font and width.
   */
  public TextAreaIcon(String text, Font font, int width, int lineSpace) {
    mWidth = width;
    mFont = font;
    mLineSpace = lineSpace;
    setText(text);
  }

  /**
   * Creates a TextAreaIcon with the specified text, font and width.
   */
  public TextAreaIcon(String text, Font font, int width) {
    this(text, font, width, 0);
  }
  
  /**
   * Sets the maximum Linecount
   * @param maxLineCount Max Count of Lines
   */
  public void setMaximumLineCount(int maxLineCount) {
    if (mMaxLineCount != maxLineCount) {
      mMaxLineCount = maxLineCount;
      if ((mTextLineArr != null) && (mTextLineArr.length >= mMaxLineCount)) {
        setText(mText);
      }
    }
  }
  
  /**
   * Get the maximum LineCount
   * @return Maximum LineCount
   */
  public int getMaximumLineCount() {
    return mMaxLineCount;
  }


  /**
   * Set the Text of this Icon
   * @param text Text in this Icon
   */
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

  /**
   * Set the Text of this Icon
   * @param textReader Text in this Icon
   * @throws IOException 
   */
  public void setText(Reader textReader) throws IOException {
    if (textReader == null) {
      mTextLineArr = null;
    } else {
      TextLineBreakerFontWidth breaker = new TextLineBreakerFontWidth(mFont);
      mTextLineArr = breaker.breakLines(textReader, mWidth, mMaxLineCount);
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
      return (mFont.getSize() + mLineSpace) * mTextLineArr.length + 2 * mLineSpace;
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
   * @return The number of used lines.
   */
  public int getLineCount() {
    return mTextLineArr.length;
  }
  
  /**
   * Draw the icon at the specified location.  Icon implementations
   * may use the Component argument to get properties useful for
   * painting, e.g. the foreground or background color.
   */
  public void paintIcon(Component comp, Graphics grp, int x, int y) {
    if (mTextLineArr != null) {
      /* For debugging of the marking problem after a data update */
      if(comp != null) {
        grp.setColor(comp.getForeground());
      }
      
      grp.setFont(mFont);
      
      int fontSize = mFont.getSize();
      for (String textLine : mTextLineArr) {
        y += fontSize + mLineSpace;
        grp.drawString(textLine, x, y);
      }
    }
  }

  /**
   * has the text in this icon been cut because it was longer than maxLines?
   * 
   * @return <code>true</code> if the text was cut
   * @since 3.0
   */
  public boolean isTextCut() {
    return mMaxLineCount > 0 && mMaxLineCount == mTextLineArr.length
        && mTextLineArr[mMaxLineCount - 1].endsWith("...");
  }
  
}