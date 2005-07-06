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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * The Window that displays the Input-Field
 * 
 * @author bodum
 */
class FindWindow extends JFrame {
  /** Finder to use */
  private FindAsYouType mFinder;
  /** Text-Field */
  private JTextField mField;
  
  /** Real Background-Color of the TextField */
  private Color mBackground;
  /** Real Foreground-Color of the TextField */
  private Color mForeground;
  
  /**
   * Create the Window
   * @param finder Finder to use
   */
  public FindWindow(FindAsYouType finder) {
    mFinder = finder;
    
    JPanel panel = (JPanel)getContentPane();
    panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    panel.setLayout(new BorderLayout());
    
    mField = new JTextField();
    
    mBackground = mField.getBackground();
    mForeground = mField.getForeground();
    
    mField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        System.out.println("..");
        FindWindow.this.setVisible(false);
      }
    });
    
    mField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        doFind();
      };
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        doFind();
      };
      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        doFind();
      };
    });
    
    mField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F3) {
          mFinder.findNext();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          FindWindow.this.setVisible(false);
        }
      }
    });
    
    panel.add(mField,BorderLayout.CENTER);
    setUndecorated(true);
    pack();
  }

  /**
   * Override setVisible because the Window needs to be resized
   * @param b true, if visible
   */
  public void setVisible(boolean b) {
    if (b) {
      resizeWindow();
    }
    super.setVisible(b);
  }
  
  /**
   * Resize the Window and create new Position under the Text-Component
   */
  private void resizeWindow() {
    JTextComponent comp = mFinder.getTextComponent();
    
    Point pos = comp.getLocationOnScreen();

    if (comp.getParent() instanceof JViewport) {
      pos = comp.getParent().getLocationOnScreen();
    }
    
    Rectangle rect = comp.getVisibleRect();
    
    setSize((int)(rect.width * 0.8), mField.getPreferredSize().height+10);
    int posx = (rect.width-getSize().width) / 2;
    setLocation(pos.x+posx, pos.y+comp.getVisibleRect().height);
  }

  /**
   * Find the next Position of the Text
   */
  public void doFind() {
    if (mField.getText().length() == 0) {
      setVisible(false);
    }
    
    if (mFinder.find(mField.getText())) {
      mField.setBackground(mBackground);
      mField.setForeground(mForeground);
    } else {
      Toolkit.getDefaultToolkit().beep();
      mField.setBackground(Color.RED);
      mField.setForeground(Color.WHITE);
    }
  }

  /**
   * Reset the Search
   */
  public void reset() {
    mField.setText("");
  }

  /**
   * Set the Text to search for
   * @param string Search for this Text
   */
  public void setText(String string) {
    mField.setText(string);
  }
  
}
