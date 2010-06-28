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
 
package util.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DirectoryChooserPanel extends JPanel {
 
  /** The localizer for this class. */
         private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(DirectoryChooserPanel.class);
 
  private JTextField mTextField;
  private JButton mBtn;
  private JLabel mLabel;

  /**
   * Creates a Directory Chooser with a 13pixel Border on the Left
   * 
   * @param title Title of the Chooser
   * @param text Text of the Chooser-Button
   */
  public DirectoryChooserPanel(String title, String text) {
    this(title, text, true);
  }
  
  /**
   * Creates a Directory Chooser
   * 
   * @param title Title of the Chooser
   * @param text Text of the Chooser-Button
   * @param leftBorder create a Border on the Left ?
   * @since 2.2
   */
  public DirectoryChooserPanel(String title, String text, boolean leftBorder) {
    setLayout(new BorderLayout(7,0));
    mLabel=new JLabel(title);
    if (leftBorder) {
      mLabel.setBorder(BorderFactory.createEmptyBorder(0,13,0,0));
    }
    add(mLabel,BorderLayout.WEST);
    
    mTextField=new JTextField(text);
    add(mTextField,BorderLayout.CENTER);
    
    mBtn=new JButton(mLocalizer.msg("change","change"));
    mBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fc =new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setApproveButtonText("Ok");
        fc.setCurrentDirectory(new File(mTextField.getText()));
        int retVal=fc.showOpenDialog(getParent());
        if (retVal==JFileChooser.APPROVE_OPTION) {
          File f=fc.getSelectedFile();
          mTextField.setText(f.getAbsolutePath());
        }
      }
    });
    
    add(mBtn,BorderLayout.EAST);
  }
  
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mTextField.setEnabled(enabled);
    mBtn.setEnabled(enabled);
    mLabel.setEnabled(enabled);
  }
  
  public String getText() {
    return mTextField.getText();
  }
}