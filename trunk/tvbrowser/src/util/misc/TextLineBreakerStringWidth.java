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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.davidashen.text.Hyphenator;
import net.davidashen.util.ErrorHandler;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import util.io.stream.InputStreamProcessor;
import util.io.stream.StreamUtilities;

/**
 * Breaks a text into lines.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TextLineBreakerStringWidth {

  private static final String HYPHEN_DICT_FILENAME = "hyphen/dehyphx.tex";

  private static final Logger mLog
    = Logger.getLogger(TextLineBreakerStringWidth.class.getName());

  /**
   * ellipsis used for shortened titles and descriptions<br>
   * unicode character representing "..."
   */
  public static final String ELLIPSIS = "\u2026";
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

  private static Hyphenator hyphenator;
  /**
   * don't use hyphenator if it can not be initialized correctly
   */
  private static boolean useHyphenator = false;

  /**
   * Create the LineBreaker
   */
  public TextLineBreakerStringWidth() {
    mCurrLineBuffer = new StringBuilder();
    mCurrWordBuffer = new StringBuilder();
    mSpaceWidth = 1;
    mMinusWidth = 1;

    if (Settings.propProgramPanelHyphenation.getBoolean()) {
      initializeHyphenator();
    }
  }

  private void initializeHyphenator() {
    if (hyphenator != null) {
      return;
    }
    hyphenator=new Hyphenator();
    hyphenator.setErrorHandler(new ErrorHandler() {

      @Override
      public void debug(String arg0, String arg1) {
      }

      @Override
      public void error(String arg0) {
        mLog.severe(arg0);
      }

      @Override
      public void exception(String arg0, Exception arg1) {
        mLog.severe(arg0);
      }

      @Override
      public void info(String arg0) {
        mLog.info(arg0);
      }

      @Override
      public boolean isDebugged(String arg0) {
        return false;
      }

      @Override
      public void warning(String arg0) {
        mLog.warning(arg0);
      }});
    try {
      File dictionary = new File(HYPHEN_DICT_FILENAME);
      if (dictionary.exists()) {
        StreamUtilities.inputStream(HYPHEN_DICT_FILENAME, new InputStreamProcessor() {

          @Override
          public void process(InputStream input) throws IOException {
            hyphenator.loadTable(input);
            useHyphenator = true;
          }
        });
      }
      else {
        mLog.warning("Hyphenation dictionary not found at " + HYPHEN_DICT_FILENAME);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    if (width <= 0) {
      width = Settings.propColumnWidth.getInt();
    }

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
        line += ELLIPSIS;
      }

      lineList.add(line);
    }
    while ((lineList.size() < maxLines) && (! allProcessed));
    int lastInx = lineList.size()-1;
    String lastLine = lineList.get(lastInx);
    if (StringUtils.isBlank(lastLine) || lineList.size() > maxLines) {
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
    mCurrLineBuffer.setLength(0);

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

      int lineLength = mCurrLineBuffer.length();
      if (newLineWidth - mSpaceWidth > maxWidth) {
        // The next word does not fit
        if (lineWidth == 0 || (maxWidth - lineWidth > 20)) {
          // The line is empty -> Break the word
          int breakPos = findBreakPos(mNextWord, maxWidth - lineWidth, lineWidth == 0);

          if (breakPos <= 0) {
            if (mCurrLineBuffer.length() > 0) {  // avoid returning empty lines, leading to endless loops
              return mCurrLineBuffer.toString();
            }
            else {
              breakPos = Math.min(2, mNextWordWidth);
            }
          }
          String firstPart = mNextWord.substring(0, breakPos);
          if (lineLength > 0 && (mCurrLineBuffer.charAt(lineLength - 1) != '-' || (lineLength > 1 && mCurrLineBuffer.charAt(lineLength - 2) == ' '))) {
            mCurrLineBuffer.append(' ');
          }
          mCurrLineBuffer.append(firstPart);

          // Append a minus if the last character is a letter or digit
          char lastChar = firstPart.charAt(firstPart.length() - 1);
          if (Character.isLetterOrDigit(lastChar)) {
            mCurrLineBuffer.append('-');
          }

          mNextWord = mNextWord.substring(breakPos);
          mNextWordWidth = getStringWidth(mNextWord);

          return mCurrLineBuffer.toString();
        } else {
          // Make a line break here (and process the word the next time)
          return mCurrLineBuffer.toString();
        }
      } else {
        if (lineWidth != 0) {
          // Add a space, but not if our current word ends with "-"
          char lastChar = mCurrLineBuffer.charAt(lineLength - 1);
          if (lastChar != '/' && (lastChar != '-' || (lineLength >= 2 && mCurrLineBuffer.charAt(lineLength - 2) == ' '))) {
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
    mCurrWordBuffer.setLength(0);

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
   * @param mustBreak this word must break, even if no hyphenation is found
   * @return The position where to break the word
   */
  private int findBreakPos(final String word, int maxWidth, boolean mustBreak) {
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

    if (useHyphenator) {
      int endCharacters;
      if (Character.isLetter(word.charAt(word.length() - 1))) {
        endCharacters = 2;
      }
      else {
        endCharacters = 3; // some words end in punctuation, so make sure at least 2 letters stay together
      }
      int startCharacters = 2;
      if (word.length() >= startCharacters + endCharacters) {
        final String hyphenated = hyphenator.hyphenate(word, endCharacters, startCharacters);
        if (hyphenated != null && hyphenated.length() > word.length()) {
          int characters = 0;
          int lastHyphen = 0;
          for (int i = 0; i < hyphenated.length(); i++) {
            if (hyphenated.charAt(i) != '\u00AD') {
              if (++characters > lastFittingPos) {
                return lastHyphen;
              }
            }
            else {
              lastHyphen = characters;
            }
          }
        }
      }
    }

    // We did not find a better break char -> break at the last fitting char
    if (mustBreak) {
      return lastFittingPos;
    }

    return 0;
  }

  /**
   * Get the Width of a String
   * @param str get Width of this String
   * @return Width of this String
   */
  public int getStringWidth(final String str) {
    return str.length();
  }

  /**
   * Test if the character is a EOL-Char
   * @param ch test this Char
   * @return true if ch is a EOL Char
   */
  private boolean isEndOfLine(final int ch) {
    return (ch == '\n') || (ch == -1);
  }

  /**
   * to be used by Settings.handleChangedSettings()
   */
  public static void resetHyphenator() {
    hyphenator = null;
    useHyphenator = false;
  }

}