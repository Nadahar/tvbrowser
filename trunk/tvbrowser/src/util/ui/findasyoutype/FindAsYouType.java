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
package util.ui.findasyoutype;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A Find-As-You-Type System
 * 
 * @author bodum
 */
public class FindAsYouType {
  /** The Text-Component to search in*/
  private JTextComponent mTextComp;
  /** Current Search-Text */
  private String mCurrentSearch;
  /** Current Position in Text */
  private int mCurPos;
  /** The Window with the Input-Field */
  private FindWindow mFindWindow;
  
  /**
   * Create the Find-As-You-Type-System for the Text-Component.
   * 
   * If the User types Text, the Window will appear and try to search for the Text.
   * At the moment this System is <em>not usable</em> for editable TextComponents.  
   * 
   * @param comp Textcomponent to use
   */
  public FindAsYouType(JTextComponent comp) {
    mTextComp = comp;

    mTextComp.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if ((e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) && ((int)e.getKeyChar() > 27)){
          if (mFindWindow == null) {
            mFindWindow = new FindWindow(FindAsYouType.this);
          }
          mFindWindow.reset();
          mFindWindow.setText(""+e.getKeyChar());
          mFindWindow.setVisible(true);
          e.consume();
        }
      }
    });
  }

  /**
   * Search for the given String
   * @param string String to search
   * @return Position in Text, -1 if not found
   */
  private int searchInTextComp(String string) {
    return searchInTextComp(string, 0);
  }

  /**
   * Search for the given String
   * @param string String to search
   * @param start Start-Position in Text
   * @return Position in Text, -1 if not found
   * @return
   */
  private int searchInTextComp(String string, int start) {
    Document doc = mTextComp.getDocument();
    try {
      String text = doc.getText(0, doc.getLength()).toLowerCase();
      string = string.toLowerCase();
      
      int pos = text.indexOf(string, start);
      if ((start > 0) && (pos < 0)) {
        pos = text.indexOf(string);
        if (pos >= 0) {
          Toolkit.getDefaultToolkit().beep();
        }
      }
      
      if (pos > -1) {
        mTextComp.select(pos, string.length());
        mTextComp.moveCaretPosition(pos+string.length());
        return pos;
      } else {
        mTextComp.select(0, 0);
      }
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Find the String in the Text
   * @param text Text to find
   * @return true, if text was found
   */
  public boolean find(String text) {
    mCurrentSearch = text;
    mCurPos = searchInTextComp(mCurrentSearch);
    return (mCurPos >= 0);
  }

  /**
   * Find next Position of the String
   * @return true, if next text was found
   */
  public boolean findNext() {
    mCurPos = searchInTextComp(mCurrentSearch, mCurPos+mCurrentSearch.length());
    return (mCurPos >= 0);
  }

  /**
   * Get the Text-Component this System uses
   * @return
   */
  public JTextComponent getTextComponent() {
    return mTextComp;
  }

}