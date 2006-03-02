/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import tvbrowser.ui.settings.channel.ChannelFilter;
import tvbrowser.ui.settings.channel.ChannelJList;
import tvbrowser.ui.settings.channel.ChannelListModel;
import tvbrowser.ui.settings.channel.FilterItem;
import tvbrowser.ui.settings.channel.FilteredChannelListCellRenderer;
import tvbrowser.ui.settings.channel.MultiChannelConfigDlg;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelContextMenu;
import util.ui.ChannelListCellRenderer;
import util.ui.DragAndDropMouseListener;
import util.ui.LinkButton;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This Class represents the Channel-Settings-Tab
 * 
 * @author Bodo Tasche
 */
public class ChannelsSettingsTab implements devplugin.SettingsTab/* ,DragGestureListener,DropTargetListener */,
    ListDropAction {

  /** Translation */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTab.class);

  /** The List with all Channels */
  private JList mAllChannels;

  /** The List with the current subscribed Channels */
  private JList mSubscribedChannels;

  /** Model of the list boxes */
  private ChannelListModel mChannelListModel;

  /** Drag n Drop Support */
  private DragAndDropMouseListener mAllChannelListener, mSubscribedChannelListener;

  /** Drag n Drop Support */
  private ListDragAndDropHandler mDnDHandler;

  /** Comboboxes for filtering */
  private JComboBox mCategoryCB, mCountryCB;

  /** Filter for Channelname */
  private JTextField mChannelName;

  /** Filter for Channels */
  private ChannelFilter mFilter;
  
  /**
   * Create the SettingsTab
   */
  public ChannelsSettingsTab() {
    mChannelListModel = new ChannelListModel();
  }

  /**
   * Create the SettingsPanel
   * 
   * @return the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel northPn = new JPanel(new GridLayout(1, 2));
    JPanel centerPn = new JPanel(new GridLayout(1, 2));
    JPanel southPn = new JPanel(new BorderLayout());
    southPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    panel.add(northPn, BorderLayout.NORTH);
    panel.add(centerPn, BorderLayout.CENTER);
    panel.add(southPn, BorderLayout.SOUTH);

    JLabel available = new JLabel(mLocalizer.msg("availableChannels", "Available channels") + ":");
    JLabel subscribed = new JLabel(mLocalizer.msg("subscribedChannels", "Subscribed channels") + ":");

    available.setFont(available.getFont().deriveFont(Font.BOLD));
    subscribed.setFont(subscribed.getFont().deriveFont(Font.BOLD));

    northPn.add(available, BorderLayout.NORTH);
    northPn.add(subscribed, BorderLayout.NORTH);

    // left list box
    JPanel listBoxPnLeft = new JPanel(new BorderLayout());
    mAllChannels = new ChannelJList(new DefaultListModel());
    mAllChannels.setCellRenderer(new ChannelListCellRenderer(true, true));

    listBoxPnLeft.add(new JScrollPane(mAllChannels), BorderLayout.CENTER);

    mAllChannels.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if ((mAllChannels.getModel().getSize() == 1) && (mAllChannels.getSelectedIndex() >= 0) && (mAllChannels.getSelectedValue() instanceof String)) {
          mAllChannels.setSelectedIndices(new int[]{});
        }
      }
    });
    
    centerPn.add(listBoxPnLeft);

    // Buttons in the Middle
    JButton rightBt = new JButton(IconLoader.getInstance().getIconFromTheme("action", "go-next", 24));
    rightBt.setMargin(UiUtilities.ZERO_INSETS);

    rightBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveChannelsToRight();
      }
    });

    JButton leftBt = new JButton(IconLoader.getInstance().getIconFromTheme("action", "go-previous", 24));
    leftBt.setMargin(UiUtilities.ZERO_INSETS);

    leftBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveChannelsToLeft();
      }
    });

    listBoxPnLeft.add(createButtonPn(rightBt, leftBt), BorderLayout.EAST);

    // right list box
    JPanel listBoxPnRight = new JPanel(new BorderLayout());
    SortableItemList channelList = new SortableItemList(new ChannelJList());

    mSubscribedChannels = channelList.getList();
    mFilter = new ChannelFilter();
    mSubscribedChannels.setCellRenderer(new FilteredChannelListCellRenderer(mFilter));

    // Register DnD on the lists.
    mDnDHandler = new ListDragAndDropHandler(mAllChannels, mSubscribedChannels, this);
    mDnDHandler.setPaintCueLine(false, true);

    // Register the listener for DnD on the lists.
    mAllChannelListener = new DragAndDropMouseListener(mAllChannels, mSubscribedChannels, this, mDnDHandler);
    mSubscribedChannelListener = new DragAndDropMouseListener(mSubscribedChannels, mAllChannels, this, mDnDHandler);

    restoreForPopup();

    listBoxPnRight.add(new JScrollPane(mSubscribedChannels), BorderLayout.CENTER);

    final JButton configureChannels = new JButton(mLocalizer.msg("configSelectedChannels",
        "Configure selected channels"));
    configureChannels.setEnabled(false);

    configureChannels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configChannels();
      }
    });

    mSubscribedChannels.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (mSubscribedChannels.getSelectedValues().length > 0) {
          configureChannels.setEnabled(true);
        } else {
          configureChannels.setEnabled(false);
        }
      }
    });

    listBoxPnRight.add(createButtonPn(channelList.getUpButton(), channelList.getDownButton()), BorderLayout.EAST);

    centerPn.add(listBoxPnRight);

    JPanel result = new JPanel(new BorderLayout());

    result.add(createFilterPanel(), BorderLayout.NORTH);

    result.add(panel, BorderLayout.CENTER);

    LinkButton urlLabel = new LinkButton(mLocalizer.msg("addMoreChannels",
        "Ihnen fehlt Ihr Lieblings-Sender? Clicken Sie hier f�r eine Liste weiterer Sender."), mLocalizer.msg(
        "addMoreChannelsUrl", "http://wiki.tvbrowser.org/index.php/Senderliste"));

    JPanel buttonsPanel = new JPanel(new BorderLayout());

    // buttonsPanel.add(pn2, BorderLayout.EAST);
    buttonsPanel.add(urlLabel, BorderLayout.SOUTH);

    result.add(buttonsPanel, BorderLayout.SOUTH);

    JButton refreshList = new JButton(mLocalizer.msg("updateChannelList", "Update channel list"), IconLoader
        .getInstance().getIconFromTheme("actions", "view-refresh", 16));

    refreshList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshChannelList();
      }
    });

    southPn.add(refreshList, BorderLayout.WEST);
    southPn.add(configureChannels, BorderLayout.EAST);

    updateFilterPanel();
    fillSubscribedChannelsListBox();
    fillAvailableChannelsListBox();

    result.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 4));
    return result;
  }

  /**
   * Create the Panel with the Filter-Interface
   * 
   * @return Panel with Filter Interface
   */
  private JPanel createFilterPanel() {
    JPanel filter = new JPanel(new FormLayout("fill:pref:grow", "pref, 3dlu, pref, 3dlu"));

    CellConstraints cc = new CellConstraints();

    JLabel header = new JLabel(mLocalizer.msg("channelFilter", "Channel Filter")+":");
    header.setFont(header.getFont().deriveFont(Font.BOLD));

    filter.add(header, cc.xy(1, 1));

    JPanel filterPanel = new JPanel(new FormLayout("pref, 3dlu, pref:grow, fill:60dlu, 3dlu, pref, 3dlu, pref:grow, pref",
        "pref, 3dlu, pref"));

    ItemListener filterItemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fillAvailableChannelsListBox();
      }
    };

    mCountryCB = new JComboBox();
    mCountryCB.addItemListener(filterItemListener);
    filterPanel.add(new JLabel(mLocalizer.msg("country", "Country") + ":"), cc.xy(1, 1));
    filterPanel.add(mCountryCB, cc.xyw(3, 1, 2));

    mCategoryCB = new JComboBox();
    mCategoryCB.addItemListener(filterItemListener);
    filterPanel.add(new JLabel(mLocalizer.msg("category", "Category") + ":"), cc.xy(6, 1));
    filterPanel.add(mCategoryCB, cc.xyw(8, 1, 2));

    JPanel namePanel = new JPanel(new BorderLayout());
    
    namePanel.add(new JLabel(mLocalizer.msg("filterText", "With the following Text") + ": "), BorderLayout.WEST);

    mChannelName = new JTextField();
    mChannelName.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        fillAvailableChannelsListBox();
      }

      public void insertUpdate(DocumentEvent e) {
        fillAvailableChannelsListBox();
      }

      public void removeUpdate(DocumentEvent e) {
        fillAvailableChannelsListBox();
      }
    });

    namePanel.add(mChannelName, BorderLayout.CENTER);

    filterPanel.add(namePanel, cc.xyw(1,3,4));
    JButton reset = new JButton(mLocalizer.msg("reset","Reset"));

    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mChannelName.setText("");
        mCategoryCB.setSelectedIndex(0);
        mCountryCB.setSelectedIndex(0);
      }
    });

    filterPanel.add(reset, cc.xy(9, 3));

    filter.add(filterPanel, cc.xy(1, 3));

    return filter;
  }

  /**
   * Updates the FilterPanel and inserts Values in the Comboboxes
   */
  private void updateFilterPanel() {
    Channel[] allChannels = ChannelList.getAvailableChannels();

    mCategoryCB.removeAllItems();
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("allCategories","All Categories"), null));

    if (channelListContains(allChannels, Channel.CATEGORY_RADIO))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryRadio","Radio"), new Integer(Channel.CATEGORY_RADIO)));

    if (channelListContains(allChannels, Channel.CATEGORY_EVENTS))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryEvents","Events"), new Integer(Channel.CATEGORY_EVENTS)));

    if (channelListContains(allChannels, Channel.CATEGORY_CINEMA))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryCinema","Kino"), new Integer(Channel.CATEGORY_CINEMA)));

    if (channelListContains(allChannels, Channel.CATEGORY_TV))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryTVAll","TV"), new Integer(Channel.CATEGORY_TV)));
    if (channelListContains(allChannels, Channel.CATEGORY_TV))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNotSpecial","TV"), new Integer(Channel.CATEGORY_TV * -1)));
    if (channelListContains(allChannels, Channel.CATEGORY_DIGITAL))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryDigital","Digitale"), new Integer(Channel.CATEGORY_DIGITAL)));
    if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_MUSIC))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryMusic","Musik"), new Integer(Channel.CATEGORY_SPECIAL_MUSIC)));
    if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_SPORT))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categorySport", "Sport"), new Integer(Channel.CATEGORY_SPECIAL_SPORT)));
    if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_NEWS))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNews", "Nachrichten"), new Integer(Channel.CATEGORY_SPECIAL_NEWS)));
    if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_OTHER))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryOthers","Sonstige Sparten"), new Integer(Channel.CATEGORY_SPECIAL_OTHER)));
    if (channelListContains(allChannels, Channel.CATEGORY_NONE))
      mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNone","Not categorized"), new Integer(Channel.CATEGORY_NONE)));

    HashSet countries = new HashSet();

    for (int i = 0; i < allChannels.length; i++) {
      String country = allChannels[i].getCountry();
      if (country != null) {
        countries.add(country);
      }
    }

    mCountryCB.removeAllItems();
    mCountryCB.addItem(new FilterItem(mLocalizer.msg("allCountries", "All Countries"), null));
    Iterator it = countries.iterator();
    while (it.hasNext()) {
      String country = (String) it.next();
      Locale locale = new Locale(Locale.getDefault().getLanguage(), country);
      mCountryCB.addItem(new FilterItem(locale.getDisplayCountry(), country));
    }

  }

  /**
   * Checks if a Channellist contains a specific Category
   * @param allChannels Check in this List of Channels
   * @param category Search for this Category
   * @return true if category is present
   */
  private boolean channelListContains(Channel[] allChannels, int category) {
    for (int i=allChannels.length-1;i>= 0;i--) {
      if ((allChannels[i].getCategories() & category) > 0)
        return true;
      else if ((allChannels[i].getCategories() == 0) && (category == 0)) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Adds a Mouselistener to the Subscribed Channels List
   */
  private void restoreForPopup() {
    mSubscribedChannels.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e))
          mSubscribedChannels.setSelectedIndex(mSubscribedChannels.locationToIndex(e.getPoint()));
        showPopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        showPopup(e);
      }
    });
  }

  /**
   * Show the Popup for the subscribed Channel
   * 
   * @param e Mouse Event
   */
  private void showPopup(MouseEvent e) {
    if (e.isPopupTrigger())
      new ChannelContextMenu(e, (Channel) mSubscribedChannels.getModel().getElementAt(
          mSubscribedChannels.locationToIndex(e.getPoint())), this);
  }

  /**
   * Create the ButtonPanel for up/down Buttons
   * @param btn1 Button 1
   * @param btn2 Button 2
   * @return ButtonPanel
   */
  private JPanel createButtonPn(JButton btn1, JButton btn2) {
    JPanel result = new JPanel(new GridLayout(2, 1));
    JPanel topPn = new JPanel(new BorderLayout());
    JPanel bottomPn = new JPanel(new BorderLayout());
    topPn.add(btn1, BorderLayout.SOUTH);
    bottomPn.add(btn2, BorderLayout.NORTH);
    result.add(topPn);
    result.add(bottomPn);
    result.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    return result;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {

    Object[] list = ((DefaultListModel) mSubscribedChannels.getModel()).toArray();

    // Convert the list into a Channel[] and fill channels
    Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      channelArr[i] = (Channel) list[i];
    }

    ChannelList.setSubscribeChannels(channelArr);
    ChannelList.storeAllSettings();
    Settings.propSubscribedChannels.setChannelArray(channelArr);

    if (!Settings.propShowProgramsInTrayWasConfigured.getBoolean()) {
      Channel[] tempArr = new Channel[channelArr.length > 10 ? 10 : channelArr.length];
      for (int i = 0; i < tempArr.length; i++)
        tempArr[i] = channelArr[i];

      Settings.propNowRunningProgramsInTrayChannels.setChannelArray(tempArr);
    }
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
    return mLocalizer.msg("channels", "Channels");
  }

  /**
   * Fills the List with the subscribed CHannels
   */
  private void fillSubscribedChannelsListBox() {
    ((DefaultListModel) mSubscribedChannels.getModel()).removeAllElements();
    Collection subscribedChannels = mChannelListModel.getSubscribedChannels();

    int subscribedChannelCount = subscribedChannels.size();
    Channel[] subscribedChannelArr = new Channel[subscribedChannelCount];
    Channel[] channels = mChannelListModel.getAvailableChannels();
    for (int i = 0; i < channels.length; i++) {
      Channel channel = channels[i];
      if (ChannelList.isSubscribedChannel(channel)) {
        int pos = ChannelList.getPos(channel);
        subscribedChannelArr[pos] = channel;
      }
    }

    // Add the subscribed channels
    for (int i = 0; i < subscribedChannelArr.length; i++) {
      ((DefaultListModel) mSubscribedChannels.getModel()).addElement(subscribedChannelArr[i]);
    }
  }

  /**
   * Fills the List with the available Channels
   */
  private void fillAvailableChannelsListBox() {
    FilterItem selectedCountry = (FilterItem) mCountryCB.getSelectedItem();
    FilterItem selectedCategory = (FilterItem) mCategoryCB.getSelectedItem();
    if (selectedCountry == null || selectedCategory == null) {
      return;
    }

    String country = (String) (selectedCountry).getValue();

    Integer categoryInt = (Integer) (selectedCategory).getValue();
    int categories = Integer.MAX_VALUE;
    if (categoryInt != null) {
      categories = categoryInt.intValue();
    }
    mFilter.setFilter(country, categories, mChannelName.getText());
    ((DefaultListModel) mAllChannels.getModel()).removeAllElements();

    // Split the channels in subscribed and available
    Channel[] channels = mChannelListModel.getAvailableChannels();
    ArrayList availableChannelList = new ArrayList();

    for (int i = 0; i < channels.length; i++) {
      Channel channel = channels[i];
      if (!((DefaultListModel) mSubscribedChannels.getModel()).contains(channel) && mFilter.accept(channel)) {
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
    
    if (mAllChannels.getModel().getSize() == 0) {
      ((DefaultListModel) mAllChannels.getModel()).addElement(mLocalizer.msg("noChannelFound", "No Channel Found"));
    }
    mSubscribedChannels.updateUI();
  }

  /**
   * Creates a Comparator that is able to compare Channels
   * 
   * @return ChannelComparator
   */
  private Comparator createChannelComparator() {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
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
        TvDataServiceProxy services[] = TvDataServiceProxyManager.getInstance().getDataServices();
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
            mChannelListModel.refresh();
            updateFilterPanel();
            fillSubscribedChannelsListBox();
            fillAvailableChannelsListBox();
          }

        });
      }

    });
  }

  /**
   * Display the Config-Channel
   */
  public void configChannels() {
    Object[] o = mSubscribedChannels.getSelectedValues();

    Channel[] channelList = new Channel[o.length];
    for (int i = 0; i < o.length; i++) {
      channelList[i] = (Channel) o[i];
    }

    if (channelList.length == 1) {
      ChannelConfigDlg dialog;

      Window w = UiUtilities.getBestDialogParent(mAllChannels);
      if (w instanceof JDialog) {
        dialog = new ChannelConfigDlg((JDialog) w, channelList[0]);
      } else {
        dialog = new ChannelConfigDlg((JFrame) w, channelList[0]);
      }
      dialog.centerAndShow();
      MainFrame.getInstance().getProgramTableScrollPane().updateChannelLabelForChannel(channelList[0]);
    } else if (channelList.length > 1) {
      MultiChannelConfigDlg dialog;

      Window w = UiUtilities.getBestDialogParent(mAllChannels);
      if (w instanceof JDialog) {
        dialog = new MultiChannelConfigDlg((JDialog) w, channelList);
      } else {
        dialog = new MultiChannelConfigDlg((JFrame) w, channelList);
      }
      dialog.centerAndShow();
    }

    mSubscribedChannels.updateUI();
  }

  /**
   * Move Channels to the subscribed Channels
   */
  private void moveChannelsToRight() {
    Object[] objects = UiUtilities.moveSelectedItems(mAllChannels, mSubscribedChannels);
    for (int i = 0; i < objects.length; i++) {
      mChannelListModel.subscribeChannel((Channel) objects[i]);
    }

  }

  /**
   * Move Channels from the subscribed Channels
   */
  private void moveChannelsToLeft() {
    Object[] objects = UiUtilities.moveSelectedItems(mSubscribedChannels, mAllChannels);
    for (int i = 0; i < objects.length; i++) {
      mChannelListModel.unsubscribeChannel((Channel) objects[i]);
    }
    fillAvailableChannelsListBox();
  }

  // Move the channels to the row where the dropping was.
  private void moveChannels(int row) {
    Object[] objects = UiUtilities.moveSelectedItems(mAllChannels, mSubscribedChannels, row);
    for (int i = 0; i < objects.length; i++) {
      mChannelListModel.subscribeChannel((Channel) objects[i]);
    }
  }

  public void drop(JList source, JList target, int n, boolean move) {
    if (source.equals(mAllChannels) && !source.equals(target) && move) {
      moveChannelsToRight();
    } else if (source.equals(mSubscribedChannels) && !source.equals(target) && move) {
      moveChannelsToLeft();
    } else if (source.equals(mSubscribedChannels) && target.equals(mSubscribedChannels)) {
      UiUtilities.moveSelectedItems(target, n, true);
    } else if (source.equals(mAllChannels) && target.equals(mSubscribedChannels)) {
      moveChannels(n);
    } else if (source.equals(mSubscribedChannels) && target.equals(mAllChannels)) {
      moveChannelsToLeft();
    }
  }
}