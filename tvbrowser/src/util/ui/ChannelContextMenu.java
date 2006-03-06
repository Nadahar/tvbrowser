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

import tvbrowser.core.ChannelList;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ChannelsSettingsTab;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import util.browserlauncher.Launch;
import devplugin.Channel;

/**
 * A class that builds a PopupMenu for a Channel.
 * 
 * @author René Mach
 * 
 */
public class ChannelContextMenu implements ActionListener {

  private static Localizer mLocalizer = Localizer
      .getLocalizerFor(ChannelContextMenu.class);

  private JPopupMenu mMenu;
  private JMenuItem mChAdd, mChConf, mChGoToURL;
  private Object mSource;
  private Channel mChannel;
  private Component mComponent;

  /**
   * Constructs the PopupMenu.
   * 
   * @param e
   *          The event that requested the PopupMenu.
   * @param ch
   *          The Channel on which the event was.
   * @param src
   *          The source Object.
   */
  public ChannelContextMenu(MouseEvent e, Channel ch, Object src) {
    mSource = src;
    mChannel = ch;
    mComponent = e.getComponent();

    mMenu = new JPopupMenu();
    mChAdd = new JMenuItem(mLocalizer.msg("addChannels", "Add/Remove channels"));
    mChConf = new JMenuItem(mLocalizer.msg("configChannel", "Setup channel"));
    mChGoToURL = new JMenuItem(mLocalizer.msg("openURL", "Open internet page"));

    mChAdd.addActionListener(this);
    mChConf.addActionListener(this);
    mChGoToURL.addActionListener(this);

    mMenu.add(mChGoToURL);
    mMenu.add(mChConf);
    if (!(mSource instanceof ChannelsSettingsTab)) {
      mMenu.addSeparator();
      mMenu.add(mChAdd);
    }

    mMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(mChAdd)) {
      MainFrame.getInstance().showSettingsDialog();
    }
    if (e.getSource().equals(mChConf)) {
      if ((mSource instanceof ChannelsSettingsTab)) {
        ((ChannelsSettingsTab) mSource).configChannels();
      } else {
        ChannelConfigDlg dialog;

        Window w = UiUtilities.getBestDialogParent(mComponent);
        if (w instanceof JDialog) {
          dialog = new ChannelConfigDlg((JDialog) w, mChannel);
        } else {
          dialog = new ChannelConfigDlg((JFrame) w, mChannel);
        }
        dialog.centerAndShow();

        // If from a ChannelLabel update it
        if (mSource instanceof tvbrowser.ui.programtable.ChannelLabel)
          ((tvbrowser.ui.programtable.ChannelLabel) mSource)
              .setChannel(mChannel);
      }
      
      if (!(mSource instanceof tvbrowser.ui.programtable.ChannelLabel)) {
        MainFrame.getInstance().getProgramTableScrollPane().updateChannelLabelForChannel(mChannel);
      }
      MainFrame.getInstance().updateChannelChooser();
      ChannelList.storeAllSettings();
    }
    if (e.getSource().equals(mChGoToURL)) {
      Launch.openURL(mChannel.getWebpage());
    }
  }
}
