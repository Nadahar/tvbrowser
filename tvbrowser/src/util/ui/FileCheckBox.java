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

import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.awt.event.*;

/**
 * This class provides a new JComponent containing three components: JCheckBox,
 * JTextField and JButton
 *
 * @author Martin Oberhauser
 */
public class FileCheckBox extends JComponent {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FileCheckBox.class);
  
  protected String title;
  protected File file;
  protected JCheckBox checkbox;
  protected JTextField textfield;
  protected JButton choosebtn;
  protected JFileChooser fileChooser;

  
  
  public FileCheckBox(String title, File file, int tab) {
    this.title=title;
    this.file=file;

    this.setLayout(new BorderLayout(15,0));

    checkbox=new JCheckBox(title);
    if (file!=null) {
      textfield=new JTextField(file.getAbsolutePath());
    }else{
      textfield=new JTextField("");
    }

    if (tab>0) {
      Dimension dim=textfield.getPreferredSize();
      checkbox.setPreferredSize(new Dimension(tab,(int)dim.getHeight()));
    }

    choosebtn=new JButton(mLocalizer.msg("change", "Change"));

    add(checkbox,BorderLayout.WEST);
    add(textfield,BorderLayout.CENTER);
    add(choosebtn,BorderLayout.EAST);

    checkbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setSelected(checkbox.isSelected());
      }
    }
    );

    choosebtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (fileChooser==null) {
          fileChooser=new JFileChooser();
        }
        fileChooser.showOpenDialog(getParent());
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          textfield.setText(f.getAbsolutePath());
        }
      }
    }
    );
  }

  public void setFileChooser(JFileChooser chooser) {
    fileChooser=chooser;
  }


  public void setSelected(boolean value) {
    checkbox.setSelected(value);
    textfield.setEnabled(value);
    choosebtn.setEnabled(value);
  }

  public boolean isSelected() {
    return checkbox.isSelected();
  }

  public void setEnabled(boolean value) {
    checkbox.setEnabled(value);
    textfield.setEnabled(value);
    choosebtn.setEnabled(value);
  }

  public boolean isEnabled() {
    return textfield.isEnabled();
  }

  public JTextField getTextField() {
    return textfield;
  }

  public JCheckBox getCheckBox() {
    return checkbox;
  }

  public JButton getButton() {
    return choosebtn;
  }

}