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
package util.misc;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Breaks a text into lines.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TextLineBreakerStringWidth {

  /** Current Character */
  private int mCurrChar;
  /** Line Buffer */
  private StringBuilder mCurrLineBuffer;
  /** Word Buffer */
  private StringBuilder mCurrWordBuffer;
  
  /** Next Word */
  private String mNextWord;
  /** Width of next Word */
  private int mNextWordWidth;
  /** Width of a Space-Character */
  private int mSpaceWidth;
  /** Width of a Minus-Character */
  private int mMinusWidth;
  
  /**
   * Create the LineBreaker
   */
  public TextLineBreakerStringWidth() {
    mCurrLineBuffer = new StringBuilder();
    mCurrWordBuffer = new StringBuilder();
    mSpaceWidth = 1;
    mMinusWidth = 1;
  }

  /**
   * Set the Width of a Space Character
   * @param spaceWidth new Space-Width
   */
  public void setSpaceWidth(int spaceWidth) {
    mSpaceWidth = spaceWidth;
  }
  
  /**
   * Set the Width of a Minus Character
   * @param minusWidth new Minus-Width
   */
  public void setMinusWidth(int minusWidth) {
    mMinusWidth = minusWidth;
  }
  
  /**
   * Break a Text into separate Lines
   * @param textReader Text to separate
   * @param width Max-Width of each Line
   * @return Text split in separate Lines
   * @throws IOException
   */
  public String[] breakLines(Reader textReader, int width) throws IOException {
    return breakLines(textReader, width, Integer.MAX_VALUE);
  }
  
  /**
   * Break a Text into separate Lines
   * @param textReader Text to separate
   * @param width Max-Width of each Line
   * @param maxLines Max. amount of Lines
   * @return Text split in separate Lines
   * @throws IOException
   */
  public String[] breakLines(Reader textReader, int width,
    int maxLines)
    throws IOException
  {
    mNextWordWidth = -1;
    
    if (maxLines == -1) {
      maxLines = Integer.MAX_VALUE;
    }

    ArrayList<String> lineList = new ArrayList<String>();
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
    int lastInx = lineList.size()-1;
    String lastLine = lineList.get(lastInx);
    if (lastLine.trim().length()==0 || lineList.size() > maxLines) {
      lineList.remove(lastInx);
    }
    String[] lineArr = new String[lineList.size()];
    lineList.toArray(lineArr);
    return lineArr;
  }
  
  /**
   * Read the Next Line in TextReader
   * @param textReader get next Line from this Reader 
   * @param maxWidth Max width of each Line
   * @return one Line
   * @throws IOException
   */
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
        while(Character.isSpaceChar(((char) mCurrChar)));

        // Read the next word
        mNextWord = readNextWord(textReader);
        mNextWordWidth = getStringWidth(mNextWord);
      }
      
      int newLineWidth = lineWidth + mNextWordWidth;
      if (lineWidth != 0) {
        newLineWidth += mSpaceWidth;
      }
      
      if (newLineWidth - mSpaceWidth > maxWidth) {
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
          mNextWordWidth = getStringWidth(mNextWord);
          
          return firstPart;
        } else {
          // Make a line break here (and process the word the next time)
          return mCurrLineBuffer.toString();
        }
      } else {
        if (lineWidth != 0) {
          // Add a space, but not if our current word ends with "-"
          char lastChar = mCurrLineBuffer.charAt(mCurrLineBuffer.length() - 1);
          if (lastChar != '/' && (lastChar != '-' || (mCurrLineBuffer.length() >= 2 && mCurrLineBuffer.charAt(mCurrLineBuffer.length() - 2) == ' '))) {
            mCurrLineBuffer.append(' ');
          }
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

  /**
   * Read the next Word in TextReader
   * @param textReader Get next Word from this TextReader
   * @return next Word
   * @throws IOException
   */
  private String readNextWord(Reader textReader)
    throws IOException
  {
    // Clear the current word
    mCurrWordBuffer.delete(0, mCurrWordBuffer.length());
    
    do {
      mCurrWordBuffer.append((char) mCurrChar);
      
      mCurrChar = textReader.read();
    }
    // a word stops at whitespace, line end or if a "-" occurs (but not if a space is in front of the "-")
    while ((! Character.isWhitespace((char) mCurrChar)) && (! isEndOfLine(mCurrChar)) && (mCurrChar != '/') && (mCurrChar != '-' || mCurrWordBuffer.length() < 2));
    if (mCurrChar == '/' || mCurrChar == '-') {
      mCurrWordBuffer.append((char) mCurrChar);
    }

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
      int middle = (left + right + 1) / 2; // +1 to enforce taking the ceiling
      
      // Check whether this substring fits
      String subWord = word.substring(0, middle);
      int subWordWidth = getStringWidth(subWord);
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
  
  /**
   * Get the Width of a String
   * @param str get Width of this String
   * @return Width of this String
   */
  public int getStringWidth(String str) {
    return str.length();
  }
  
  /**
   * Test if the Character is is a EOL-Char 
   * @param ch test this Char
   * @return true if ch is a EOL Char
   */
  private boolean isEndOfLine(int ch) {
    return (ch == '\n') || (ch == -1);
  }

}