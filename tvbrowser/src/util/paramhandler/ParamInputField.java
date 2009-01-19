/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
package util.paramhandler;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * This Class represents a Default-Input-Field for Parameters
 * it contains a Help and a Check-Button
 * 
 * @author bodum
 *
 */
public class ParamInputField extends JPanel {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParamInputField.class);
  
  /** Text-Area for the Parameters in the EMail-Body*/
  private JTextArea mParamText;  
  
  /** The Library to use for Check/Help-Dialog */
  private ParamLibrary mParamLibrary;
  
  /**
   * Create the InputField
   * @param text Text to show in the InputField
   */
  public ParamInputField(String text) {
    this(text, false);
  }
  
  /**
   * Create the InputField
   * @param text Text to show in the InputField
   * @param singleLine set True, if Input-Field should be a Single-Line
   */
  public ParamInputField(String text, boolean singleLine) {
    this(new ParamLibrary(), text, singleLine);
  }  
  
  /**
   * Create the InputField
   * @param library Library to use in the Check/Help Dialogs
   * @param text Text to show in the InputField
   */
  public ParamInputField(ParamLibrary library, String text) {
    this(library, text, false);
  }
  
  /**
   * Create the InputField
   * @param library Library to use in the Check/Help Dialogs
   * @param text Text to show in the InputField
   * @param singleLine set True, if Input-Field should be a Single-Line
   */
  public ParamInputField(ParamLibrary library, String text, boolean singleLine) {
    mParamLibrary = library;
    if (mParamLibrary == null) {
      mParamLibrary = new ParamLibrary();
    }
    createGui(text, singleLine);
  }  
  /**
   * Create the GUI 
   * @param text Text to use in the InputField
   * @param singleLine set True, if Input-Field should be a Single-Line
   */
  private void createGui(String text, boolean singleLine) {
    setLayout(new FormLayout("fill:pref:grow, 3dlu, default, 3dlu, default", 
                 "fill:pref:grow, 3dlu, default"));
    
    CellConstraints cc = new CellConstraints();
    
    mParamText = new JTextArea();
    
    mParamText.setText(text);
    
    if (singleLine) {
      mParamText.setLineWrap(true);
      mParamText.addKeyListener(new KeyAdapter() {

        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                ke.consume();
            }
        }
      });
      
      JScrollPane scroll = new JScrollPane(mParamText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      
      add(scroll, cc.xyw(1,1,5));
    } else {
      add(new JScrollPane(mParamText), cc.xyw(1,1,5));
    }
    
    JButton check = new JButton(mLocalizer.msg("check","Check"));
    
    check.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(ParamInputField.this);
        ParamCheckDialog dialog = new ParamCheckDialog(bestparent,
            mParamLibrary, mParamText.getText());
        dialog.setVisible(true);
      }
      
    });
    
    add(check, cc.xy(3,3));
    
    JButton help = new JButton(Localizer.getLocalization(Localizer.I18N_HELP));
    
    help.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(ParamInputField.this);
        ParamHelpDialog dialog = new ParamHelpDialog(bestparent, mParamLibrary);
        dialog.setVisible(true);
      }
      
    });
    
    add(help, cc.xy(5,3));
  }
  
  /**
   * Get the Text in the InputField
   * @return Text in the InputField
   */
  public String getText() {
    return mParamText.getText();
  }
  
  /**
   * Set the Text in the InputField
   * @param text new Text in the InputField
   */
  public void setText(String text) {
    mParamText.setText(text);
  }
  
  /**
   * Returns the Text in the InputField
   * @return Text in the InputField
   */
  public String toString() {
    return getText();
  }
}