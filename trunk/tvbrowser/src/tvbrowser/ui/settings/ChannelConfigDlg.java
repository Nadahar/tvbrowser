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


package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import devplugin.Channel;
import util.ui.UiUtilities;


public class ChannelConfigDlg implements ActionListener {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  
  private Channel[] mChannelList;
  private JDialog mDialog;
  private JButton mOkBt, mCancelBt;
  private JComboBox mCorrectionCB;
    
  public ChannelConfigDlg(Component parent, String title, Channel[] channelList) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(title);
    
    JPanel main=new JPanel(new BorderLayout());
    main.setBorder(BorderFactory.createEmptyBorder(6,6,5,5));
    mDialog.setContentPane(main);
        
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    JPanel dayLightPn=new JPanel();
    dayLightPn.setLayout(new BoxLayout(dayLightPn,BoxLayout.Y_AXIS));
    dayLightPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("DLSTTitle","Day light saving time correction")));
    
    JPanel pn=new JPanel(new BorderLayout());
    JLabel lb=new JLabel(mLocalizer.msg("correction","Correction"));
    lb.setBorder(BorderFactory.createEmptyBorder(0,10,0,50));
    pn.add(lb,BorderLayout.WEST);
    mCorrectionCB=new JComboBox(new String[]{"-1:00","0:00","+1:00"});
    if (channelList.length==1) {
      mCorrectionCB.setSelectedIndex(channelList[0].getDayLightSavingTimeCorrection()+1);
    }
    else {
      mCorrectionCB.setSelectedIndex(1);
    }
    JTextArea txt=new JTextArea(mLocalizer.msg("DLSTNote",""));
    txt.setFocusable(false);
    txt.setLineWrap(true);
    txt.setWrapStyleWord(true);
    txt.setOpaque(false);
    txt.setEditable(false);
    txt.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    
    pn.add(mCorrectionCB,BorderLayout.EAST);
    dayLightPn.add(pn);
    dayLightPn.add(txt);
    
    content.add(dayLightPn);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    
    mOkBt = new JButton(SettingsDialog.mLocalizer.msg("ok", "OK"));
    mCancelBt = new JButton(SettingsDialog.mLocalizer.msg("cancel", "Cancel"));
    
    mOkBt.addActionListener(this);
    mCancelBt.addActionListener(this);
    
    buttonPn.add(mOkBt);
    buttonPn.add(mCancelBt);
    
    main.add(content,BorderLayout.NORTH);
    main.add(buttonPn,BorderLayout.SOUTH);
    
    
    mChannelList=channelList;
    mDialog.setSize(400,200);
    //mDialog.pack();
  }
  
  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }
  
  public void actionPerformed(ActionEvent e) {
    Object o=e.getSource();
    if (o==mOkBt) {
      int correction=mCorrectionCB.getSelectedIndex()-1;
      for (int i=0;i<mChannelList.length;i++) {
         mChannelList[i].setDayLightSavingTimeCorrection(correction);
      }      
      mDialog.hide();
    }
    else if (o==mCancelBt) {
      mDialog.hide();  
    }
    
    
  }
  
  
}