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

package tvbrowser.ui.configassistant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class TvdataImportDlg extends JDialog {
 
  private static final util.ui.Localizer mLocalizer
             = util.ui.Localizer.getLocalizerFor(TvdataImportDlg.class); 
 
  private JButton mOkBt, mCancelBt;
  private JTextField mDirectoryTF;
  private int mResult;
  
  public static final int CANCEL=0, OK=1, ERROR=2;
  
  public TvdataImportDlg(String title, final String fromDirName, final String toDirName) {
    super((JFrame)null,true);
    
    setTitle(title);
    
    mResult=CANCEL;
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
    
    JPanel btnPn=new JPanel();
    btnPn.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        
    mCancelBt=new JButton(mLocalizer.msg("cancel","Cancel"));
    mCancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResult=CANCEL;
        hide();
      }
    });
        
    mOkBt=new JButton(mLocalizer.msg("ok","OK"));
    mOkBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        try {
          importDirectory(mDirectoryTF.getText(),toDirName);
          mResult=OK;
          hide();
        }catch(Exception e) {
          JOptionPane.showMessageDialog(null,e.getMessage());
          mResult=ERROR;
        }
      }
    });
        
    btnPn.add(mCancelBt);
    btnPn.add(mOkBt);    
        
    JPanel dlgPn=new JPanel();
    dlgPn.setLayout(new BoxLayout(dlgPn,BoxLayout.Y_AXIS));
    dlgPn.add(new JLabel(mLocalizer.msg("instruction","Please enter the path of the directory '{0}'",fromDirName)));
    dlgPn.add(new JLabel(mLocalizer.msg("instruction-note","(Note: the directory '{0}' must be included)",fromDirName)));
    
    JPanel directoryInputPn=new JPanel(new BorderLayout());
    directoryInputPn.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
    mDirectoryTF=new JTextField();
    JButton browseBt=new JButton(mLocalizer.msg("browse","browse"));
    
    browseBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        JFileChooser fc =new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setApproveButtonText("Ok");
        fc.setCurrentDirectory(new File("."));
        int retVal=fc.showOpenDialog(getParent());
        if (retVal==JFileChooser.APPROVE_OPTION) {
          File f=fc.getSelectedFile();
          mDirectoryTF.setText(f.getAbsolutePath());
        }
      }
    
    });
    
    directoryInputPn.add(mDirectoryTF,BorderLayout.CENTER);
    directoryInputPn.add(browseBt,BorderLayout.EAST);
    
    
    dlgPn.add(directoryInputPn);
    
    contentPane.add(dlgPn,BorderLayout.CENTER);
    contentPane.add(btnPn,BorderLayout.SOUTH);
    
    pack();
  }
  
  public int getResult() {
    return mResult;
  }
  
  private void importDirectory(String fromDirName, String toDirName) throws Exception {
    
    File fromDir=new File(fromDirName);
    if (!fromDir.exists() || !fromDir.isDirectory()) {
      throw new FileNotFoundException(mLocalizer.msg("error.1","{0} is not a valid directory",fromDirName));
    }
    
    File toDir=new File(toDirName);
    if (! toDir.exists()) {
      if (! toDir.mkdirs()) {
        throw new Exception(mLocalizer.msg("error.2","Could not create directory '{0}'",toDir.getAbsolutePath()));
      }
    }
    
    copyDirectory(fromDir,toDir);
    
    
    
  }
  
  
  private void copyDirectory(File fromDir, File toDir) throws IOException {
    File[] files=fromDir.listFiles();
    for (int i=0;i<files.length;i++) {
      if (files[i].isDirectory()) {
        copyDirectory(files[i],new File(toDir,files[i].getName()));
      }
      else {
        util.io.IOUtilities.copy(files[i],new File(toDir,files[i].getName()));
      }
    }
  }
  
  
  
}