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
package reminderplugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.paramhandler.ParamInputField;

/**
 * The Dialog for the Settings of the executable
 * 
 * @author bodum
 */
public class ExecuteSettingsDialog extends JDialog {
  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ExecuteSettingsDialog.class);
  
  private String mExecParam;
  private String mExecFile;
  private ParamInputField mParam;
  private JTextField mFile;
  
  private boolean mOkPressed = false;
  
  /**
   * Create the Dialog
   * 
   * @param parent Parent
   * @param execFile File to execute
   * @param execParam Params for the File
   */
  public ExecuteSettingsDialog(JDialog parent, String execFile, String execParam) {
    super(parent, true);
    mExecFile = execFile;
    mExecParam =execParam;
    createGui();
  }

  /**
   * Create the Dialog
   * 
   * @param parent Parent
   * @param execFile File to execute
   * @param execParam Params for the File
   */
  public ExecuteSettingsDialog(JFrame parent, String execFile, String execParam) {
    super(parent, true);
    mExecFile = execFile;
    mExecParam =execParam;
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    setTitle(mLocalizer.msg("execSettings", "Executable Settings"));
    
    JPanel panel = (JPanel) getContentPane();
    
    panel.setLayout(new BorderLayout());
    
    JPanel filePanel = new JPanel(new BorderLayout(5,0));
    
    mFile = new JTextField(mExecFile);
    
    filePanel.add(mFile, BorderLayout.CENTER);
    
    JButton chsFile = new JButton("...");
    
    chsFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        changeFile();
      }
    });
    
    filePanel.add(chsFile, BorderLayout.EAST);
    
    panel.add(filePanel, BorderLayout.NORTH);
    
    filePanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("program", "Program")));
    
    mParam = new ParamInputField(mExecParam, true);
    
    mParam.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("parameters", "Parameters")));
    
    panel.add(mParam, BorderLayout.CENTER);
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        okPressed();
        setVisible(false);
      }
      
    });
    
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancel.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
      
    });
    
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(ok);
    buttons.add(cancel);
    panel.add(buttons, BorderLayout.SOUTH);
    
    setSize(350, 300);
    setLocationRelativeTo(getParent());
  }
  
  /**
   * Call the FileChooser for the executable
   */
  private void changeFile() {
    JFileChooser chooser = new JFileChooser(new File(mFile.getText()));
    int returnVal = chooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      mFile.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }
  
  /**
   * OK was pressed
   */
  private void okPressed() {
    mExecFile = mFile.getText();
    mExecParam = mParam.getText();
    mOkPressed = true;
  }
  
  /**
   * Was OK pressed ?
   * @return true, if OK was pressed
   */
  public boolean wasOKPressed() {
    return mOkPressed;
  }
  
  /**
   * The Parameters
   * @return parameters
   */
  public String getParameters() {
    return mExecParam;
  }
  
  /**
   * The Executable
   * @return Executable
   */
  public String getExecutable() {
    return mExecFile;
  }
}
