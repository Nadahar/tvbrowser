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

public class TvdataAssistantDlg extends JDialog {
  
  private static final util.ui.Localizer mLocalizer
           = util.ui.Localizer.getLocalizerFor(TvdataAssistantDlg.class); 
  
  private JRadioButton mImportRB, mAssistantRB, mIgnoreRB;
  private JButton mOkBt;
  
  public static final int IMPORT_DATA=0, RUN_ASSISTANT=1, DO_NOTHING=2;
  
  
  public TvdataAssistantDlg() {
    super((JFrame)null, true);
    setTitle(tvbrowser.TVBrowser.MAINWINDOW_TITLE);
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,25));
    
    JPanel btnPn=new JPanel(new BorderLayout());
    btnPn.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    mOkBt=new JButton(mLocalizer.msg("ok","OK"));
    mOkBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        hide();
      }
    });
    
    btnPn.add(mOkBt,BorderLayout.EAST);
    
    JPanel dlgPn=new JPanel();
    dlgPn.setLayout(new BoxLayout(dlgPn,BoxLayout.Y_AXIS));
    
    JLabel lb1=new JLabel(mLocalizer.msg("msg1","There were no tv data found."));
    JLabel lb2=new JLabel(mLocalizer.msg("msg2","How do you want to proceed?"));
    lb2.setBorder(BorderFactory.createEmptyBorder(15,0,5,0));
    
    dlgPn.add(lb1);
    dlgPn.add(lb2);
    
    mImportRB=new JRadioButton(mLocalizer.msg("import","import data from previous TV-Browser version"));
    mAssistantRB=new JRadioButton(mLocalizer.msg("import-note","run setup assistant to re-configure TV-Browser"));
    mIgnoreRB=new JRadioButton(mLocalizer.msg("ignore","ignore"));
    
     
    dlgPn.add(mImportRB);
    dlgPn.add(mAssistantRB);
    dlgPn.add(mIgnoreRB);
    
        
    ButtonGroup btnGroup=new ButtonGroup();
    btnGroup.add(mImportRB);
    btnGroup.add(mAssistantRB);
    btnGroup.add(mIgnoreRB);
    
    mImportRB.setSelected(true);
    
    contentPane.add(dlgPn,BorderLayout.CENTER);
    contentPane.add(btnPn,BorderLayout.SOUTH);
    
    pack();
  }
  
  
  public int getSelection() {
    if (mImportRB.isSelected()) {
      return IMPORT_DATA;
    }
    else if (mAssistantRB.isSelected()) {
      return RUN_ASSISTANT;
    }
    else return DO_NOTHING;
    
  }
  
}