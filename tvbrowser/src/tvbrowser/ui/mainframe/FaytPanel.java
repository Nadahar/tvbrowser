/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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

package tvbrowser.ui.mainframe;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Date;
import devplugin.ProgramFilter;

/**
 * A Panel for FindAsYouType in program table.
 * <p>
 * @author René Mach
 * @since 3.0.3
 */
public class FaytPanel extends JPanel {
  private ProgramFilter mPreviousFilter;
  private JTextField mTextField;
  private String mText;
  private JButton mSearchCloseBtn;
  
  /**
   * Create an instance of this class.
   */
  public FaytPanel() {
    mTextField = new JTextField();
    
    mTextField.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent e) {
        if(!mText.equals(mTextField.getText()) && mPreviousFilter != null) {
          mText = mTextField.getText();
     
          FaytFilter.getInstance().setSearchString(mText);
          MainFrame.getInstance().setProgramFilter(FaytFilter.getInstance());

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              mTextField.grabFocus();
              
              SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                  SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {
                      mTextField.setSelectionStart(mText.length());
                    }
                  });            
                }
              });
            }
          });
        }
      }
    });

    
    mSearchCloseBtn = new JButton(IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 16));
    mSearchCloseBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    mSearchCloseBtn.setPressedIcon(IconLoader.getInstance().getIconFromTheme(
        "actions", "close-pressed", 16));
    mSearchCloseBtn.setToolTipText(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mSearchCloseBtn.setContentAreaFilled(false);
    mSearchCloseBtn.setFocusable(false);
    mSearchCloseBtn.setOpaque(false);
    
    MouseListener[] ml = mSearchCloseBtn.getMouseListeners();
    
    for(int i = 0; i < ml.length; i++) {
      if(!(ml[i] instanceof ToolTipManager)) {
        mSearchCloseBtn.removeMouseListener(ml[i]);
      }
    }
    setBorder(Borders.DLU2_BORDER);
    setOpaque(false);
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("default,5dlu,100dlu","default"),this);
    
    
    /*
     * Close action for the SearchPanel.
     */
    final Action close = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        closeFayt();
      }
    };
    
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    mTextField.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, "CLOSE_SEARCH");
    mTextField.getActionMap().put("CLOSE_SEARCH", close);

    mSearchCloseBtn.getInputMap(JComponent.WHEN_FOCUSED).put(stroke,
        "CLOSE_SEARCH");
    mSearchCloseBtn.getActionMap().put("CLOSE_SEARCH", close);
    mSearchCloseBtn.addMouseListener(new MouseAdapter() {
      boolean mOver;

      public void mouseEntered(MouseEvent e) {
        mOver = true;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          b.setIcon(IconLoader.getInstance().getIconFromTheme("status",
              "close-over", 16));
        }
      }

      public void mouseExited(MouseEvent e) {
        mOver = false;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
              "process-stop", 16));
        }
      }

      public void mousePressed(MouseEvent e) {
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
              "close-pressed", 16));
        }
      }

      public void mouseReleased(MouseEvent e) {
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
              "process-stop", 16));

          if (mOver) {
            if(SwingUtilities.isLeftMouseButton(e)) {
              closeFayt();
            }
          }
        }        
      }
    });
    
    pb.add(mSearchCloseBtn,new CellConstraints().xy(1,1));
    pb.add(mTextField,new CellConstraints().xy(3,1));
  }
  
  /**
   * Set the text of the search box.
   * <p>
   * @param value The new text for the search box.
   */
  public synchronized void setText(final String value) {
    if(isVisible()) {
      mTextField.setText(mTextField.getText() + value);
    }
    else {
      mPreviousFilter = MainFrame.getInstance().getProgramFilter();
      mText = value;
      mTextField.setText(value);

      FaytFilter.getInstance().setSearchString(mText);
      MainFrame.getInstance().setProgramFilter(FaytFilter.getInstance());
      setVisible(true);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mTextField.setCaretPosition(mText.length());
          mTextField.grabFocus();
          SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
              SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                  mTextField.setSelectionStart(mText.length());
                }
              });            
            }
          });
        }
      });
    }
  }
  
  /**
   * Deletes the last char in the search box.
   */
  public void deleteLastChar() {
    if(mText.length() > 0) {
      mTextField.requestFocus();
      mTextField.setText(mText.substring(0,mText.length()-1));
    }
  }
  
  /**
   * Closes this panel and  set filter back to previous filter.
   */
  public synchronized void closeFayt() {
    setVisible(false);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        MainFrame.getInstance().setProgramFilter(mPreviousFilter);
        if(MainFrame.getInstance().getCurrentSelectedDate().equals(Date.getCurrentDate())) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              MainFrame.getInstance().scrollToNow();    
            }
          });
        }
      }
    });
  }
}
