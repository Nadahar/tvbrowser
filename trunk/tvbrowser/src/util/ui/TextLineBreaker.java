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

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * Breaks a text into lines.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TextLineBreaker {

  /** The helper label. */  
  private static final JLabel HELPER_LABEL = new JLabel();

  private FontMetrics mFontMetrics;
  
  private int mCurrChar;
  
  private StringBuffer mCurrLineBuffer;

  private StringBuffer mCurrWordBuffer;
  
  private int mSpaceWidth;
  private int mMinusWidth;

  private String mNextWord;
  private int mNextWordWidth;


  public TextLineBreaker() {
    mCurrLineBuffer = new StringBuffer();
    mCurrWordBuffer = new StringBuffer();
  }
  
  
  public String[] breakLines(Reader textReader, Font font, int width,
    int maxLines)
    throws IOException
  {
    mFontMetrics = HELPER_LABEL.getFontMetrics(font);
    mSpaceWidth = mFontMetrics.charWidth(' ');
    mMinusWidth = mFontMetrics.charWidth('-');
    mNextWordWidth = -1;
    
    if (maxLines == -1) {
      maxLines = Integer.MAX_VALUE;
    }

    ArrayList lineList = new ArrayList();
    boolean allProcessed;
    do {
      String line = readNextLine(textReader, width);
      
      allProcessed = (mCurrChar == -1) && (mNextWordWidth == -1);
      if (((lineList.size() + 1) == maxLines) && (! allProcessed)
        && (line.length() != 0))
      {
        // Add three dots if we stop because of the maxLines rule
        line += "...";
      }

      lineList.add(line);
    }
    while ((lineList.size() < maxLines) && (! allProcessed));
    
    String[] lineArr = new String[lineList.size()];
    lineList.toArray(lineArr);
    return lineArr;
  }
  
  
  private String readNextLine(Reader textReader, int maxWidth)
    throws IOException
  {
    // Clear the current line
    mCurrLineBuffer.delete(0, mCurrLineBuffer.length());
    
    int lineWidth = 0;
    while (true) {
      // Check whether there is a word that has to be processed first
      if (mNextWordWidth == -1) {
        // There is no unprocessed word any more -> Read to the next word
        // (A length of -1 means it was processed)

        // Ignore white space 
        do {
          mCurrChar = textReader.read();

          // Check whether we have to force a line break
          if (isEndOfLine(mCurrChar)) {
            // Force line break
            return mCurrLineBuffer.toString();
          }
        }
        while(isWhiteSpace((char) mCurrChar));

        // Read the next word
        mNextWord = readNextWord(textReader);
        mNextWordWidth = mFontMetrics.stringWidth(mNextWord);
      }
      
      int newLineWidth = lineWidth + mNextWordWidth;
      if (lineWidth != 0) {
        newLineWidth += mSpaceWidth;
      }
      
      if (newLineWidth > maxWidth) {
        // The next word does not fit
        if (lineWidth == 0) {
          // The line is empty -> Break the word
          int breakPos = findBreakPos(mNextWord, maxWidth);
          
          String firstPart = mNextWord.substring(0, breakPos);
          if (firstPart.length()==0) {
            mNextWordWidth = -1;
            return "";
          }
          
          // Append a minus if the last character is a letter or digit
          char lastChar = firstPart.charAt(firstPart.length() - 1);
          if (Character.isLetterOrDigit(lastChar)) {
            firstPart += "-";
          }
          
          mNextWord = mNextWord.substring(breakPos);
          mNextWordWidth = mFontMetrics.stringWidth(mNextWord);
          
          return firstPart;
        } else {
          // Make a line break here (and process the word the next time)
          return mCurrLineBuffer.toString();
        }
      } else {
        if (lineWidth != 0) {
          // Add a space
          mCurrLineBuffer.append(' ');
          lineWidth += mSpaceWidth;
        }

        // The next word fits -> Add it
        mCurrLineBuffer.append(mNextWord);
        lineWidth += mNextWordWidth;
        mNextWordWidth = -1; // Mark the word as processed

        // Check whether we have to force a line break
        if (isEndOfLine(mCurrChar)) {
          // Force line break
          return mCurrLineBuffer.toString();
        }
      }
    }
  }


  private String readNextWord(Reader textReader)
    throws IOException
  {
    // Clear the current word
    mCurrWordBuffer.delete(0, mCurrWordBuffer.length());
    
    do {
      mCurrWordBuffer.append((char) mCurrChar);
      
      mCurrChar = textReader.read();
    }
    while ((! isWhiteSpace(mCurrChar)) && (! isEndOfLine(mCurrChar)));

    return mCurrWordBuffer.toString();
  }


  /**
   * Finds the best position to break the word in order to fit into a maximum
   * width.
   * 
   * @param word The word to break
   * @param maxWidth The maximum width of the word
   * @return The position where to break the word
   */
  private int findBreakPos(String word, int maxWidth) {
    // Reserve some space for the minus
    maxWidth -= mMinusWidth;
    
    // Binary search for the last fitting character
    int left = 0;
    int right = word.length() - 1;
    while (left < right) {
      int middle = (left + right + 1) / 2; // +1 to enforce taking the ceil
      
      // Check whether this substring fits
      String subWord = word.substring(0, middle);
      int subWordWidth = mFontMetrics.stringWidth(subWord);
      if (subWordWidth < maxWidth) {
        // It fits -> go on with the right side
        left = middle;
      } else {
        // It fits not -> go on with the left side
        right = middle - 1;
      }
    }
    int lastFittingPos = left;
    
    // Try to find a char that is no letter or digit
    // E.g. if the word is "Stadt-Land-Fluss" we try to break it in
    // "Stadt-" and "Land-Fluss" rather than "Stadt-La" and "nd-Fluss"
    for (int i = lastFittingPos - 1; i >= (lastFittingPos / 2); i--) {
      char ch = word.charAt(i);
      if (! Character.isLetterOrDigit(ch)) {
        // This char is no letter or digit -> break here
        return i + 1;
      }
    }

    // We did not find a better break char -> break at the last fitting char
    return lastFittingPos;
  }
  
  
  private boolean isWhiteSpace(int ch) {
    return Character.isSpaceChar((char) ch);
  }
  
  
  private boolean isEndOfLine(int ch) {
    return (ch == '\n') || (ch == -1);
  }

}
