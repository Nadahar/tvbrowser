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
  private JButton mCloseBt, mApplyTimeCorrectionBt;
  private JComboBox mCorrectionCB;
    
  public ChannelConfigDlg(Component parent, String title, Channel[] channelList) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(title);
    
    JPanel main=new JPanel(new BorderLayout());
    main.setBorder(BorderFactory.createEmptyBorder(6,6,5,5));
    mDialog.setContentPane(main);
        
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    JPanel dayLightPn=new JPanel(new BorderLayout());
    
    JPanel panel1=new JPanel();
    panel1.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("DLSTTitle","Day light saving time correction")));
    
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    
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
    panel1.add(pn);
    panel1.add(txt);
    
    dayLightPn.add(panel1,BorderLayout.CENTER);
    JPanel p1=new JPanel(new BorderLayout());
    mApplyTimeCorrectionBt=new JButton(mLocalizer.msg("apply","Apply"));
    mApplyTimeCorrectionBt.addActionListener(this);
    p1.add(mApplyTimeCorrectionBt,BorderLayout.SOUTH);
    dayLightPn.add(p1,BorderLayout.EAST);
    content.add(dayLightPn);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    
    mCloseBt = new JButton(mLocalizer.msg("close", "Close"));
    
    mCloseBt.addActionListener(this);
    
    buttonPn.add(mCloseBt);
    
    main.add(content,BorderLayout.NORTH);
    main.add(buttonPn,BorderLayout.SOUTH);
    
    
    mChannelList=channelList;
    mDialog.setSize(400,200);
  }
  
  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }
  
  public void actionPerformed(ActionEvent e) {
    Object o=e.getSource();
    if (o==mApplyTimeCorrectionBt) {
      int correction=mCorrectionCB.getSelectedIndex()-1;
      for (int i=0;i<mChannelList.length;i++) {
         mChannelList[i].setDayLightSavingTimeCorrection(correction);
      }      
    }
    else if (o==mCloseBt) {
      mDialog.hide();  
    }
    
    
  }
  
  
}