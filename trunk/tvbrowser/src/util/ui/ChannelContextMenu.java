package util.ui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ChannelsSettingsTab;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import util.browserlauncher.Launch;
import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A class that builds a PopupMenu for a Channel.
 * 
 * @author René Mach
 * 
 */
public class ChannelContextMenu implements ActionListener {

  /**
   * The localizer for this class.
   */
  public static Localizer mLocalizer = Localizer
      .getLocalizerFor(ChannelContextMenu.class);

  private JPopupMenu mMenu;
  private JMenuItem mChAdd, mChConf, mChGoToURL, mFilterChannels;
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
    
    // dynamically create filters from available channel filter components
    mFilterChannels = new JMenu(mLocalizer.msg("filterChannels", "Channel filter"));
    JMenuItem menuItem = new JMenuItem(mLocalizer.msg("filterAll", "All channels"));
    menuItem.addActionListener(this);
    mFilterChannels.add(menuItem);
    String[] channelFilterNames = FilterComponentList.getInstance().getChannelFilterNames();
    for (String filterName : channelFilterNames) {
      menuItem = new JMenuItem(filterName);
      menuItem.addActionListener(this);
      mFilterChannels.add(menuItem);
    }

    mChAdd.addActionListener(this);
    mChConf.addActionListener(this);
    mChGoToURL.addActionListener(this);

    mMenu.add(mChGoToURL);
    mMenu.add(mChConf);
    if (!(mSource instanceof ChannelsSettingsTab)) {
      mMenu.add(mFilterChannels);
      mMenu.addSeparator();
      mMenu.add(mChAdd);
    }

    mMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(mChAdd)) {
      MainFrame.getInstance().showSettingsDialog();
    }
    else if (e.getSource().equals(mChConf)) {
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
    else if (e.getSource().equals(mChGoToURL)) {
      Launch.openURL(mChannel.getWebpage());
    }
    else {
      if (e.getSource() instanceof JMenuItem) {
        JMenuItem channelItem = (JMenuItem) e.getSource();
        String channelName = channelItem.getText();
        final FilterComponent component = FilterComponentList.getInstance().getFilterComponentByName(channelName);
        if (component instanceof ChannelFilterComponent) {
          ProgramFilter filter = new ProgramFilter() {
            public String getName() {
              return component.getName();
            }
          
            public boolean accept(Program program) {
              return component.accept(program);
            }
          };
          MainFrame.getInstance().setProgramFilter(filter); 
        }
        else {
          MainFrame.getInstance().setProgramFilter(FilterList.getInstance().getDefaultFilter()); 
        }
      }
    }
  }
}
