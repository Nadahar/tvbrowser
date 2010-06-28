package util.ui;

import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * A class for correction of the caret position of a JFormattedTextField.
 * 
 * @author René Mach
 * 
 */
public class CaretPositionCorrector {
    
  private Point mClickLocation;
  private int mCaretPosition, mStartPosition;
  private CaretListener mCaretListener;
  private char[] mJumpCharacters;
  private char mStartIndexChar;
  private boolean mMouseDown;
  
  private CaretPositionCorrector(JFormattedTextField field, char[] jumpCharacters, char startIndexChar, int startPosition) {
    mJumpCharacters = jumpCharacters;
    mStartIndexChar = startIndexChar;
    mStartPosition = startPosition;
    mClickLocation = null;
    mCaretPosition = -1;
    
    if(jumpCharacters != null && jumpCharacters.length > 0) {
      createCaretListener(field);
    
      addKeyListenerToField(field);
    }
    
    addMouseListenerToField(field);
    addFocusListenerToField(field);
  }
  
  /**
   * Creates an instance of the JSpinnerCaretPositionCorrector.
   * 
   * Use <code>null</code> for jumpCharacters if you only want to
   * let the position set to the click position.
   * 
   * @param field The JFormattedTextField to correct the caret position of.
   * @param jumpCharacters The character to jump from if the caret is in front of it.
   * @param startIndexChar The character that is used to find the start position of the caret.
   * 
   */
  public static void createCorrector(JFormattedTextField field, char[] jumpCharacters, char startIndexChar) {
    new CaretPositionCorrector(field, jumpCharacters, startIndexChar, -1);
  }
  
  /**
   * Creates an instance of the JSpinnerCaretPositionCorrector.
   * 
   * Use <code>null</code> for jumpCharacters if you only want to
   * let the position set to the click position.
   * 
   * @param field The JFormattedTextField to correct the caret position of.
   * @param jumpCharacters The character to jump from if the caret is in front of it.
   * @param startPosition The start position for the caret.
   * 
   */
  public static void createCorrector(JFormattedTextField field, char[] jumpCharacters, int startPosition) {
    new CaretPositionCorrector(field, jumpCharacters, '\0', startPosition);
  }
  
  /** Create caret listener for making sure of editing the right value.*/
  private void createCaretListener(final JFormattedTextField field) {
    mCaretListener = new CaretListener() {
      public void caretUpdate(final CaretEvent e) {
        if(mCaretPosition != -1 && field.getSelectedText() == null) {
          mCaretPosition = field.getCaretPosition();
        }
      }
    };
  }
  
  private boolean hasToMoveCaret(String text, int pos) {
    for(char value : mJumpCharacters) {
      if(text.charAt(pos) == value) {
        return true;
      }
    }
    
    return false;
  }
  
  /** Add key listener to the field to handle correct moving throw the text of the field */
  private void addKeyListenerToField(final JFormattedTextField field) {
    field.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(KeyEvent.VK_UP == e.getKeyCode() || KeyEvent.VK_DOWN == e.getKeyCode()) {
          mCaretPosition = field.getCaretPosition();
          if(mCaretPosition <= field.getText().length() && mCaretPosition > 0 &&
              field.getSelectedText() == null &&
              (mCaretPosition >= field.getText().length() || hasToMoveCaret(field.getText(), mCaretPosition))) {
            field.setCaretPosition(--mCaretPosition);
          }
        }
      }
    });
  }
  
  /** Add mouse listener to the field to track clicks and get the location of it */
  private void addMouseListenerToField(final JFormattedTextField field) {
    field.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mMouseDown = true;
        mClickLocation = e.getPoint();
      }
      
      public void mouseReleased(MouseEvent e) {
        mMouseDown = false;
        mCaretPosition = field.getUI().viewToModel(field, e.getPoint());
        
        if(mCaretPosition <= field.getText().length() && mCaretPosition > 0 &&
            field.getSelectedText() == null &&
            (mCaretPosition >= field.getText().length() || hasToMoveCaret(field.getText(), mCaretPosition))) {
          field.setCaretPosition(--mCaretPosition);
        }
      }
    });
  }
  
  /**  Add focus listener to set the location of the caret to the right place */
  private void addFocusListenerToField(final JFormattedTextField field) {
    field.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        if(mCaretPosition == -1) {
          if(mStartIndexChar != '\0') {
            mCaretPosition = field.getText().indexOf(String.valueOf(mStartIndexChar)) + 1;
          } else if(mStartPosition != -1) {
            mCaretPosition = mStartPosition;
          } else {
            mCaretPosition = 0;
          }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if(mClickLocation != null) {
              mCaretPosition = field.getUI().viewToModel(field, mClickLocation);
            }
            
            mClickLocation = null;
            
            if(mCaretPosition <= field.getText().length() && mCaretPosition > 0 &&
                field.getSelectedText() == null && !mMouseDown &&
                (mCaretPosition >= field.getText().length() || hasToMoveCaret(field.getText(), mCaretPosition))) {
              mCaretPosition--;
            }
            
            field.setCaretPosition(mCaretPosition);
            field.addCaretListener(mCaretListener);
          }
        });
      }

      public void focusLost(FocusEvent e) {
        mCaretPosition = field.getCaretPosition();
        field.removeCaretListener(mCaretListener);
      }
    });
  }
}
