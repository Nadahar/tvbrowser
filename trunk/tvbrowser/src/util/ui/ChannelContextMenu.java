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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ChannelsSettingsTab;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import util.browserlauncher.Launch;
import devplugin.Channel;
import devplugin.SettingsItem;

/**
 * A class that builds a PopupMenu for a Channel.
 * 
 * @author Ren� Mach
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

  private JRadioButtonMenuItem layoutBoth;

  private JRadioButtonMenuItem layoutLogo;

  private JRadioButtonMenuItem layoutName;

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
    String channelFilterName = Settings.propLastUsedChannelGroup.getString();
    mFilterChannels = new JMenu(mLocalizer.msg("filterChannels",
        "Channel filter"));
    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(mLocalizer.msg(
        "filterAll", "All channels"));
    menuItem.setSelected(channelFilterName == null);
    menuItem.addActionListener(this);
    mFilterChannels.add(menuItem);
    String[] channelFilterNames = FilterComponentList.getInstance()
        .getChannelFilterNames();
    for (String filterName : channelFilterNames) {
      menuItem = new JRadioButtonMenuItem(filterName);
      menuItem.addActionListener(this);
      mFilterChannels.add(menuItem);
      if (channelFilterName != null && filterName.equals(channelFilterName)) {
        menuItem.setSelected(true);
      }
    }
    mFilterChannels.add(new JSeparator());
    JMenuItem menuItemAdd = new JMenuItem(mLocalizer.msg("filterNew",
        "Add channel filter"));
    menuItemAdd.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        EditFilterComponentDlg dlg = new EditFilterComponentDlg(null, null,
            ChannelFilterComponent.class);
        FilterComponent rule = dlg.getFilterComponent();
        if ((rule != null) && (rule instanceof ChannelFilterComponent)) {
          FilterComponentList.getInstance().add(rule);
          FilterComponentList.getInstance().store();
          setChannelGroup((ChannelFilterComponent) rule);
        }
      }
    });
    mFilterChannels.add(menuItemAdd);

    mChAdd.addActionListener(this);
    mChConf.addActionListener(this);
    mChGoToURL.addActionListener(this);

    mMenu.add(mChGoToURL);
    mMenu.add(mChConf);
    if (!(mSource instanceof ChannelsSettingsTab)) {
      mMenu.add(mFilterChannels);
      mMenu.addSeparator();
      mMenu.add(mChAdd);
      JMenu configureLayout = new JMenu(mLocalizer.msg("layout", "Layout"));
      layoutBoth = new JRadioButtonMenuItem(mLocalizer.msg("layoutBoth",
          "Logo and name"));
      layoutLogo = new JRadioButtonMenuItem(mLocalizer
          .msg("layoutLogo", "Logo"));
      layoutName = new JRadioButtonMenuItem(mLocalizer
          .msg("layoutName", "Name"));
      configureLayout.add(layoutBoth);
      configureLayout.add(layoutLogo);
      configureLayout.add(layoutName);
      mMenu.add(configureLayout);

      layoutBoth.addActionListener(this);
      layoutLogo.addActionListener(this);
      layoutName.addActionListener(this);

      if (Settings.propShowChannelIconsInChannellist.getBoolean()
          && Settings.propShowChannelNamesInChannellist.getBoolean()) {
        layoutBoth.setSelected(true);
      } else if (Settings.propShowChannelIconsInChannellist.getBoolean()) {
        layoutLogo.setSelected(true);
      } else {
        layoutName.setSelected(true);
      }
    }
    mMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(mChAdd)) {
      MainFrame.getInstance().showSettingsDialog(SettingsItem.CHANNELS);
    } else if (e.getSource().equals(mChConf)) {
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
        if (mSource instanceof tvbrowser.ui.programtable.ChannelLabel) {
          ((tvbrowser.ui.programtable.ChannelLabel) mSource)
              .setChannel(mChannel);
        }
      }

      if (!(mSource instanceof tvbrowser.ui.programtable.ChannelLabel)) {
        MainFrame.getInstance().getProgramTableScrollPane()
            .updateChannelLabelForChannel(mChannel);
      }
      MainFrame.getInstance().updateChannelChooser();
      ChannelList.storeAllSettings();
    } else if (e.getSource().equals(mChGoToURL)) {
      Launch.openURL(mChannel.getWebpage());
    } else {
      if (e.getSource() instanceof JRadioButtonMenuItem) {
        if (e.getSource() == layoutBoth || e.getSource() == layoutName
            || e.getSource() == layoutLogo) {
          Settings.propShowChannelNamesInChannellist
              .setBoolean(e.getSource() == layoutBoth
                  || e.getSource() == layoutName);
          Settings.propShowChannelIconsInChannellist
              .setBoolean(e.getSource() == layoutBoth
                  || e.getSource() == layoutLogo);
          MainFrame.getInstance().updateChannelChooser();
        } else {
          JRadioButtonMenuItem filterItem = (JRadioButtonMenuItem) e
              .getSource();
          String filterName = filterItem.getText();
          final FilterComponent component = FilterComponentList.getInstance()
              .getFilterComponentByName(filterName);
          if (component != null && component instanceof ChannelFilterComponent) {
            setChannelGroup((ChannelFilterComponent) component);
          } else {
            setChannelGroup(null);
          }
        }
      }
    }
  }

  private void setChannelGroup(final ChannelFilterComponent channelGroup) {
    MainFrame.getInstance().setChannelGroup(channelGroup);
  }
}
