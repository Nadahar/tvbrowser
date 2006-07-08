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
  
  private CaretPositionCorrector(JFormattedTextField field, char[] jumpCharacters, char startIndexChar, int startPosition) {
    mJumpCharacters = jumpCharacters;
    mStartIndexChar = startIndexChar;
    mStartPosition = startPosition;
    mClickLocation = null;
    mCaretPosition = -1;
    
    createCaretListener(field);
    
    addKeyListenerToField(field);
    addMouseListenerToField(field);
    addFocusListenerToField(field);
  }
  
  /**
   * Creates an instance of the JSpinnerCaretPositionCorrector.
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
        
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              String text = field.getText();
            
              if(mCaretPosition > 0 && (mCaretPosition >= text.length() ||
                  hasToMoveCaret(text, mCaretPosition))) {
                field.setCaretPosition(--mCaretPosition);
              }
            }
          });
        }
      }
    };
  }
  
  private boolean hasToMoveCaret(String text, int pos) {    
    for(char value : mJumpCharacters)
      if(text.charAt(pos) == value)
        return true;
    
    return false;
  }
  
  /** Add key listener to the field to handle correct moving throw the text of the field */
  private void addKeyListenerToField(final JFormattedTextField field) {
    field.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(KeyEvent.VK_RIGHT == e.getKeyCode()) {
          int pos = field.getCaretPosition() + 1;
          if(pos < field.getText().length() && pos > 0 && 
              field.getSelectedText() == null &&
              hasToMoveCaret(field.getText(), pos)) {
            field.setCaretPosition(pos);
          }
        }
      }
    });
  }
  
  /** Add mouse listener to the field to track clicks and get the location of it */
  private void addMouseListenerToField(final JFormattedTextField field) {
    field.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mClickLocation = e.getPoint();
      }
    });
  }
  
  /**  Add focus listener to set the location of the caret to the right place */
  private void addFocusListenerToField(final JFormattedTextField field) {
    field.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        if(mCaretPosition == -1) {
          if(mStartIndexChar != '\0')
            mCaretPosition = field.getText().indexOf(String.valueOf(mStartIndexChar)) + 1;
          else if(mStartPosition != -1)
            mCaretPosition = mStartPosition;
          else
            mCaretPosition = 0;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {              
            if(mClickLocation != null)
              mCaretPosition = field.getUI().viewToModel(field, mClickLocation);
            
            mClickLocation = null;
            
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
