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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.ui.customizableitems.SortableItemList;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelListCellRenderer;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This Class represents the Channel-Settings-Tab
 * 
 * @author Bodo Tasche
 */
public class ChannelsSettingsTabNew3 implements devplugin.SettingsTab {

  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTabNew3.class);

  /** Show all Buttons ?*/
  private boolean mShowAllButtons;

  /** The List with all Channels */
  private JList mAllChannels;

  /** The List with the current subscribed Channels */
  private JList mSubscribedChannels;

  /** Left/Right Buttons */
  private JButton mLeftBtn, mRightBtn;

  /** Name of the current selected Channel*/
  private JLabel mChannelName;

  /** Category of the current selected Channel*/
  private JLabel mChannelCategory;

  /** Country of the current selected Channel*/
  private JLabel mChannelCountry;

  /** TimeZone of the current selected Channel*/
  private JLabel mChannelTimeZone;

  /** Provider of the current selected Channel*/
  private JLabel mChannelProvider;
  
  /**
   * Creates the SettingsTab
   */
  public ChannelsSettingsTabNew3() {
    this(true);
  }

  /**
   * Create the SettingsTab
   * 
   * @param showAllButtons true, if the refresh/config-Buttons should be displayed
   */
  public ChannelsSettingsTabNew3(boolean showAllButtons) {
    mShowAllButtons = showAllButtons;
  }

  /**
   * Create the SettingsPanel
   * @return the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel();
    // FormDebugPanel panel = new FormDebugPanel();

    FormLayout layout = new FormLayout("default:grow(0.5), 3dlu, default, 3dlu, default:grow(0.5), 3dlu, default",
        "default, 3dlu, default:grow, default, 3dlu, default, default:grow, 3dlu, top:default, 3dlu, default, 3dlu, default");

    panel.setLayout(layout);
    panel.setBorder(Borders.DLU4_BORDER);
    CellConstraints c = new CellConstraints();

    // Left Box
    panel.add(new JLabel("Verfügbare Kanäle:"), c.xy(1, 1));

    mAllChannels = new JList(new DefaultListModel());
    mAllChannels.setCellRenderer(new ChannelListCellRenderer());

    panel.add(new JScrollPane(mAllChannels), c.xywh(1, 3, 1, 5));

    panel.add(createFilterPanel(), c.xy(1, 9));

    // Buttons in the Middle

    JButton rightBt = new JButton(new ImageIcon("imgs/Forward24.gif"));
    rightBt.setToolTipText(mLocalizer.msg("tooltip.right", "Move selected rows in right list"));
    rightBt.setMargin(UiUtilities.ZERO_INSETS);

    JButton leftBt = new JButton(new ImageIcon("imgs/Back24.gif"));
    leftBt.setToolTipText(mLocalizer.msg("tooltip.left", "Move selected rows in left list"));
    leftBt.setMargin(UiUtilities.ZERO_INSETS);

    panel.add(rightBt, c.xy(3, 4));
    panel.add(leftBt, c.xy(3, 6));

    // Right Box

    panel.add(new JLabel("Ausgewählte Kanäle:"), c.xy(5, 1));

    SortableItemList channelList = new SortableItemList();

    mSubscribedChannels = channelList.getList();
    mSubscribedChannels.setCellRenderer(new ChannelListCellRenderer());

    panel.add(new JScrollPane(mSubscribedChannels), c.xywh(5, 3, 1, 5));

    final JButton configureChannels = new JButton("Ausgewählte Kanäle konfig.");
    
    mSubscribedChannels.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent e) {
        updateDetailPanel();
        if (mSubscribedChannels.getSelectedValues().length > 0) {
          configureChannels.setEnabled(true);
        } else {
          configureChannels.setEnabled(false);
        }
      }

    });

    panel.add(createDetailsPanel(), c.xyw(5, 9, 3));

    panel.add(channelList.getUpButton(), c.xy(7, 4));
    panel.add(channelList.getDownButton(), c.xy(7, 6));

    // Bottom Buttons

    if (mShowAllButtons) {
      JButton refreshList = new JButton("Senderliste aktualisieren");

      refreshList.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          refreshChannelList();
        }
      });

      configureChannels.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          configChannels();
        }
      });
      
      panel.add(refreshList, c.xy(1, 13));
      panel.add(configureChannels, c.xyw(5, 13, 3));
    }

    fillChannelListBox();

    return panel;
  }

  /**
   * Create the FilterPanel.
   * 
   * @return the FilterPanel
   */
  private Component createFilterPanel() {

    // FormDebugPanel panel = new FormDebugPanel();
    JPanel panel = new JPanel();

    panel.setLayout(new FormLayout("default, 3dlu, default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));

    CellConstraints c = new CellConstraints();

    panel.add(new JLabel("Filter:"), c.xyw(1, 1, 3));
    panel.add(new JLabel("Land"), c.xy(1, 3));
    panel.add(new JComboBox(), c.xy(3, 3));
    panel.add(new JLabel("Zeitzone"), c.xy(1, 5));
    panel.add(new JComboBox(), c.xy(3, 5));
    panel.add(new JLabel("Anbieter"), c.xy(1, 7));
    panel.add(new JComboBox(), c.xy(3, 7));
    panel.add(new JLabel("Name"), c.xy(1, 9));
    panel.add(new JTextField(), c.xy(3, 9));

    return panel;
  }

  /**
   * Create the Details Panel for the Channels
   * 
   * @return Details-Panel
   */
  private Component createDetailsPanel() {

    // FormDebugPanel panel = new FormDebugPanel();
    JPanel panel = new JPanel();
    panel.setLayout(new FormLayout("default, 3dlu, default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));

    CellConstraints c = new CellConstraints();

    panel.add(new JLabel("Details:"), c.xyw(1, 1, 3));

    panel.add(new JLabel("Kanal:"), c.xy(1, 3));

    mChannelName = new JLabel();
    panel.add(mChannelName, c.xy(3, 3));

    panel.add(new JLabel("Kategorie:"), c.xy(1, 5));

    mChannelCategory = new JLabel();
    panel.add(mChannelCategory, c.xy(3, 5));

    panel.add(new JLabel("Land:"), c.xy(1, 7));

    mChannelCountry = new JLabel();
    panel.add(mChannelCountry, c.xy(3, 7));

    panel.add(new JLabel("Zeitzone:"), c.xy(1, 9));

    mChannelTimeZone = new JLabel();
    panel.add(mChannelTimeZone, c.xy(3, 9));

    panel.add(new JLabel("Betreiber:"), c.xy(1, 11));

    mChannelProvider = new JLabel();
    panel.add(mChannelProvider, c.xy(3, 11));

    return panel;
  }

  /**
   * Update the Details-Panel with the current selected Channel
   */
  private void updateDetailPanel() {
    Object[] ch = (Object[]) mSubscribedChannels.getSelectedValues();

    if ((ch != null) && (ch.length == 1)) {
      Channel channel = (Channel) ch[0];
      mChannelName.setText(channel.getName());
      mChannelCategory.setText("Not Defined");
      mChannelCountry.setText(channel.getCountry());
      mChannelTimeZone.setText(channel.getTimeZone().getDisplayName());
      mChannelProvider.setText("Not Defined");
    } else {
      mChannelName.setText("");
      mChannelCategory.setText("");
      mChannelCountry.setText("");
      mChannelTimeZone.setText("");
      mChannelProvider.setText("");
    }

  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return "Kanäle";
  }

  /**
   * Fill the ChannelListBoxes with the Channels
   */
  private void fillChannelListBox() {

    ((DefaultListModel) mSubscribedChannels.getModel()).removeAllElements();
    ((DefaultListModel) mAllChannels.getModel()).removeAllElements();

    // Split the channels in subscribed and available
    Channel[] channels = ChannelList.getAvailableChannels();
    int subscribedChannelCount = ChannelList.getNumberOfSubscribedChannels();
    Channel[] subscribedChannelArr = new Channel[subscribedChannelCount];
    ArrayList availableChannelList = new ArrayList();

    for (int i = 0; i < channels.length; i++) {
      Channel channel = channels[i];

      if (ChannelList.isSubscribedChannel(channel)) {
        int pos = ChannelList.getPos(channel);
        ChannelList.getSubscribedChannels()[pos].copySettingsToChannel(channel);
        subscribedChannelArr[pos] = channel;
      } else {
        availableChannelList.add(channel);
      }
    }

    // Sort the available channels
    Channel[] availableChannelArr = new Channel[availableChannelList.size()];
    availableChannelList.toArray(availableChannelArr);
    Arrays.sort(availableChannelArr, createChannelComparator());

    // Add the available channels
    for (int i = 0; i < availableChannelArr.length; i++) {
      ((DefaultListModel) mAllChannels.getModel()).addElement(availableChannelArr[i]);
    }

    // Add the subscribed channels
    for (int i = 0; i < subscribedChannelArr.length; i++) {
      ((DefaultListModel) mSubscribedChannels.getModel()).addElement(subscribedChannelArr[i]);
    }
  }

  /**
   * Creates a Comparator that is able to compare Channels
   * @return ChannelComparator
   */
  private Comparator createChannelComparator() {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };
  }

  /**
   * Refresh the ChannelList
   */
  private void refreshChannelList() {
    final ProgressWindow win = new ProgressWindow(tvbrowser.ui.mainframe.MainFrame.getInstance());

    win.run(new Progress() {
      public void run() {
        tvdataservice.TvDataService services[] = TvDataServiceManager.getInstance().getDataServices();
        for (int i = 0; i < services.length; i++) {
          if (services[i].supportsDynamicChannelList()) {
            try {
              services[i].checkForAvailableChannels(win);
            } catch (TvBrowserException exc) {
              ErrorHandler.handle(exc);
            }
          }
        }

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ChannelList.create();
            fillChannelListBox();
          }

        });
      }

    });
  }

  /**
   * Display the Config-Channel
   */
  private void configChannels() {
    Object[] o=mSubscribedChannels.getSelectedValues();
    
    Channel[] channelList=new Channel[o.length];
    for (int i=0;i<o.length;i++) {
      channelList[i]=(Channel)o[i];
    }
    ChannelConfigDlg dlg=new ChannelConfigDlg(mSubscribedChannels,mLocalizer.msg("configSelectedChannels","Configure selected channels"),channelList);
    dlg.centerAndShow();
    mSubscribedChannels.updateUI();
  }
  
}