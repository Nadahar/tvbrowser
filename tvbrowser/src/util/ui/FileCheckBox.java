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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * This class provides a new JComponent containing three components: JCheckBox,
 * JTextField and JButton
 *
 * @author Martin Oberhauser
 */
public class FileCheckBox extends JComponent {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FileCheckBox.class);

  protected String mTitle;
  protected JCheckBox mCheckbox;
  protected JTextField mTextfield;
  protected JButton mChoosebtn;
  protected JFileChooser mFileChooser;



  public FileCheckBox(String title, File file, int tab, boolean addButton) {
    mTitle=title;
    setLayout(new BorderLayout(5,0));

    mCheckbox=new JCheckBox(title);
    if (file!=null) {
      mTextfield=new JTextField(file.getAbsolutePath());
    }else{
      mTextfield=new JTextField("");
    }

    if (tab>0) {
      Dimension dim=mTextfield.getPreferredSize();
      mCheckbox.setPreferredSize(new Dimension(tab,(int)dim.getHeight()));
    }

    mChoosebtn=new JButton(mLocalizer.ellipsisMsg("change", "Change"));

    add(mCheckbox,BorderLayout.WEST);
    add(mTextfield,BorderLayout.CENTER);

    if(addButton) {
      add(mChoosebtn,BorderLayout.EAST);
    }

    mCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setSelected(mCheckbox.isSelected());
      }
    }
    );

    mChoosebtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (mFileChooser==null) {
          mFileChooser=new JFileChooser();
        }
        int retVal = mFileChooser.showOpenDialog(getParent());
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File f=mFileChooser.getSelectedFile();
          if (f!=null) {
            mTextfield.setText(f.getAbsolutePath());

            if(mTextfield.getKeyListeners().length == 1) {
              mTextfield.getKeyListeners()[0].keyReleased(null);
            }
          }
        }
      }
    }
    );
  }

  public void setFileChooser(JFileChooser chooser) {
    mFileChooser=chooser;
    String temp = mTextfield.getText();

    if(temp.indexOf(File.separator) != -1) {
      mFileChooser.setCurrentDirectory(new File(temp.substring(0,temp.lastIndexOf(File.separator)+1)));
    }
  }


  public void setSelected(boolean value) {
    mCheckbox.setSelected(value);
    mTextfield.setEnabled(value);
    mChoosebtn.setEnabled(value);
  }

  public boolean isSelected() {
    return mCheckbox.isSelected();
  }

  public void setFile(File f) {
    if (f!=null) {
      mTextfield.setText(f.getAbsolutePath());
    }else{
      mTextfield.setText("");
    }
  }

  public File getFile() {
    return new File(mTextfield.getText());
  }

  public void setEnabled(boolean value) {
    mCheckbox.setEnabled(value);
    mTextfield.setEnabled(value);
    mChoosebtn.setEnabled(value);
  }

  public boolean isEnabled() {
    return mTextfield.isEnabled();
  }

  public JTextField getTextField() {
    return mTextfield;
  }

  public JCheckBox getCheckBox() {
    return mCheckbox;
  }

  public JButton getButton() {
    return mChoosebtn;
  }

  public JButton removeButton() {
    remove(mChoosebtn);
    return mChoosebtn;
  }

}