package util.ui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ChannelConfigDlg;
import tvbrowser.ui.settings.ChannelsSettingsTab;

import devplugin.Channel;

public class ChannelContextMenu implements ActionListener{
  
  private static Localizer mLocalizer = Localizer.getLocalizerFor(ChannelContextMenu.class);
  
  private JPopupMenu mMenu;
  private JMenuItem mChAdd, mChConf, mChGoToURL;  
  private Object mSource;
  private Channel mChannel;
  private Component mComponent;
  
  public ChannelContextMenu(MouseEvent e, Channel ch, Object src) {
    mSource = src;
    mChannel = ch;
    mComponent = e.getComponent();
    
    mMenu = new JPopupMenu();
    mChAdd = new JMenuItem(mLocalizer.msg("addChannels","Add/Remove channels"));
    mChConf = new JMenuItem(mLocalizer.msg("configChannel","Setup channel"));
    mChGoToURL = new JMenuItem(mLocalizer.msg("openURL","Open internet page"));
    
    mChAdd.addActionListener(this);
    mChConf.addActionListener(this);
    mChGoToURL.addActionListener(this);
    
    if(!(mSource instanceof ChannelsSettingsTab))
      mMenu.add(mChAdd);
    
    mMenu.add(mChConf);
    mMenu.addSeparator();
    mMenu.add(mChGoToURL);   
    
    mMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void actionPerformed(ActionEvent e) {
   if(e.getSource().equals(mChAdd)) {
     MainFrame.getInstance().showSettingsDialog();
   }
   if(e.getSource().equals(mChConf)) {
     ChannelConfigDlg dialog;
          
     Window w = UiUtilities.getBestDialogParent(mComponent);
     if (w instanceof JDialog) {
       dialog = new ChannelConfigDlg((JDialog)w, mChannel);
     } else {
       dialog = new ChannelConfigDlg((JFrame)w, mChannel);
     }
     dialog.centerAndShow();
     
     if(mSource instanceof tvbrowser.ui.programtable.ChannelLabel)
       ((tvbrowser.ui.programtable.ChannelLabel)mSource).setChannel(mChannel);
     
     if(!(mSource instanceof ChannelsSettingsTab))
       MainFrame.getInstance().updateChannelChooser();
   }
   if(e.getSource().equals(mChGoToURL)) {
     util.ui.BrowserLauncher.openURL(mChannel.getWebpage());
   }
  }
}
