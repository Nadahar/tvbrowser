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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.UiUtilities;
import devplugin.Channel;


public class ChannelConfigDlg implements ActionListener {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  
  private Channel[] mChannelList;
  private JDialog mDialog;
  private JButton mCloseBt, mOKBt;
  private JComboBox mCorrectionCB;

  /** FileName for Icon */
  private JTextField mIconFileName;
  /** Button to Change FileName */
  private JButton mChangeIcon;
  /** use User-Icon */
  private JCheckBox mUseUserIcon;
  /** Text in front of Icon-Location */
  private JLabel mIconLabel;

  
  public ChannelConfigDlg(Component parent, String title, Channel[] channelList) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(title);
    
    JPanel main=new JPanel(new BorderLayout());
    main.setBorder(BorderFactory.createEmptyBorder(6,6,5,5));
        
    JPanel content=new JPanel(new GridBagLayout());
    
    JPanel panel1=new JPanel();
    panel1.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("DLSTTitle","Day light saving time correction")));
    
    panel1.setLayout(new GridBagLayout());
    
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
    
    GridBagConstraints c = new GridBagConstraints();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    panel1.add(pn, c);
    panel1.add(txt, c);

    c = new GridBagConstraints();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    content.add(panel1, c);
    
    
    // Configuration for Icons 
    JPanel iconPanel = new JPanel();
    iconPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channelIcon","Channel Icon")));
    iconPanel.setLayout(new BorderLayout(5, 2));
    mIconFileName = new JTextField();
    mChangeIcon = new JButton(mLocalizer.msg("change","Change"));
    mChangeIcon.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            File file = new File(mIconFileName.getText());
            JFileChooser fileChooser = new JFileChooser(file.getParent());
            String[] extArr = { ".jpg", ".jpeg", ".gif", ".png"};
            fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
            fileChooser.showOpenDialog(mDialog);
            File selection = fileChooser.getSelectedFile();
            if (selection != null) {
                mIconFileName.setText(selection.getAbsolutePath());
            }
          }
    
    });
    
    mUseUserIcon = new JCheckBox(mLocalizer.msg("useIcon","Use own channel-icon"));
    
    mUseUserIcon.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            mIconLabel.setEnabled(mUseUserIcon.isSelected());
            mIconFileName.setEnabled(mUseUserIcon.isSelected());
            mChangeIcon.setEnabled(mUseUserIcon.isSelected());
        }
    });
    
    mIconLabel = new JLabel(mLocalizer.msg("icon","Icon"));
    iconPanel.add(mUseUserIcon, BorderLayout.NORTH);
    iconPanel.add(mIconLabel, BorderLayout.WEST);
    iconPanel.add(mIconFileName, BorderLayout.CENTER);
    iconPanel.add(mChangeIcon, BorderLayout.EAST);
    
    if (channelList.length == 1) {
        mUseUserIcon.setSelected(channelList[0].isUsingUserIcon());
        
        mIconLabel.setEnabled(mUseUserIcon.isSelected());
        mIconFileName.setEnabled(mUseUserIcon.isSelected());
        mChangeIcon.setEnabled(mUseUserIcon.isSelected());
        
        mIconFileName.setText(channelList[0].getIconFileName());
        content.add(iconPanel, c);
    }
    
    
    // Configuration for Icons
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    
    mOKBt=new JButton(mLocalizer.msg("ok","OK"));
    mOKBt.addActionListener(this);
    
    mCloseBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCloseBt.addActionListener(this);
    
    buttonPn.add(mOKBt);
    buttonPn.add(mCloseBt);
    
    main.add(content,BorderLayout.CENTER);
    main.add(buttonPn,BorderLayout.SOUTH);
    
    mChannelList=channelList;
    mDialog.getContentPane().add(main);
    mDialog.pack();
    mDialog.pack(); // This hack is needed because of re-calculation for JTextArea
    
   if (mDialog.getWidth() < 300) {
        mDialog.setSize(300, mDialog.getHeight());
    }
  
  }
  
  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }
  
  public void actionPerformed(ActionEvent e) {
    Object o=e.getSource();
    if (o==mOKBt) {
      int correction=mCorrectionCB.getSelectedIndex()-1;
      for (int i=0;i<mChannelList.length;i++) {
         mChannelList[i].setDayLightSavingTimeCorrection(correction);
      }
      
      if (mChannelList.length == 1) {
          mChannelList[0].useUserIcon(mUseUserIcon.isSelected());
          mChannelList[0].setIconFileName(mIconFileName.getText());
      }
      mDialog.hide();
    }
    else if (o==mCloseBt) {
      mDialog.hide();
    }
    
    
  }
  
  
}