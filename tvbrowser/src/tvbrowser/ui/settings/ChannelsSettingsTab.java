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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.tvdataservice.ChannelGroupManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.channel.ChannelConfigDlg;
import tvbrowser.ui.settings.channel.ChannelFilter;
import tvbrowser.ui.settings.channel.ChannelJList;
import tvbrowser.ui.settings.channel.ChannelListModel;
import tvbrowser.ui.settings.channel.FilterItem;
import tvbrowser.ui.settings.channel.FilteredChannelListCellRenderer;
import tvbrowser.ui.settings.channel.MultiChannelConfigDlg;
import util.io.NetworkUtilities;
import util.ui.ChannelContextMenu;
import util.ui.ChannelListCellRenderer;
import util.ui.DragAndDropMouseListener;
import util.ui.LinkButton;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.SettingsTab;

/**
 * This Class represents the Channel-Settings-Tab
 *
 * @author Bodo Tasche
 */
public class ChannelsSettingsTab implements SettingsTab, ListDropAction {

  /**
   * Translation
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelsSettingsTab.class);

  /**
   * The List with all Channels
   */
  private JList mAllChannels;

  /**
   * The List with the current subscribed Channels
   */
  private JList mSubscribedChannels;

  /**
   * Model of the list boxes
   */
  private ChannelListModel mChannelListModel;

  /**
   * Drag n Drop Support
   */
  private DragAndDropMouseListener mSubscribedChannelListener;

  /**
   * Drag n Drop Support
   */
  private ListDragAndDropHandler mDnDHandler;

  /**
   * Comboboxes for filtering
   */
  private JComboBox mCategoryCB, mCountryCB, mPluginCB;

  /**
   * Filter for channel name
   */
  private JTextField mChannelName;

  /**
   * Filter for Channels
   */
  private ChannelFilter mFilter;

  /**
   * True, if currently updating Lists
   */
  private boolean mListUpdating = false;

  /** MS after the last input of text field */
  private final static int REFRESH_AFTER_MS = 200;

  private JComponent mAvailableSeparator;

  private JComponent mSubscribedSeparator;

  private Timer mRefreshListTimer;

  /** If no channel was found, ask to download them. But only once.*/
  private boolean mInitChannelsAsked = false;

  private JButton mLeftButton;

  private JButton mRightButton;
  
  private boolean mShowPlugins = (TvDataServiceProxyManager
  .getInstance().getDataServices().length > 1);

  /**
   * Create the SettingsTab
   */
  public ChannelsSettingsTab() {
  }

  /**
   * Create the SettingsPanel
   *
   * @return the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    mChannelListModel = new ChannelListModel();
    JPanel panel = new JPanel(new BorderLayout());

    JPanel northPn = new JPanel(new GridLayout(1, 2));
    JPanel centerPn = new JPanel(new GridLayout(1, 2));
    JPanel southPn = new JPanel(new BorderLayout());
    southPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    panel.add(northPn, BorderLayout.NORTH);
    panel.add(centerPn, BorderLayout.CENTER);
    panel.add(southPn, BorderLayout.SOUTH);

    mAvailableSeparator = DefaultComponentFactory.getInstance()
        .createSeparator(
            mLocalizer.msg("availableChannels", "Available channels") + ":");
    mAvailableSeparator.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

    mSubscribedSeparator = DefaultComponentFactory.getInstance()
        .createSeparator(
            mLocalizer.msg("subscribedChannels", "Subscribed channels") + ":");
    mSubscribedSeparator.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

    northPn.add(mAvailableSeparator, BorderLayout.NORTH);
    northPn.add(mSubscribedSeparator, BorderLayout.NORTH);

    // left list box
    JPanel listBoxPnLeft = new JPanel(new BorderLayout());
    mAllChannels = new ChannelJList(new DefaultListModel());
    mAllChannels.setCellRenderer(new ChannelListCellRenderer(true, true, true, true));

    listBoxPnLeft.add(new JScrollPane(mAllChannels), BorderLayout.CENTER);
/*
    mAllChannels.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if ((mAllChannels.getModel().getSize() == 1)
            && (mAllChannels.getSelectedIndex() >= 0)
            && (mAllChannels.getSelectedValue() instanceof String)) {
          mAllChannels.setSelectedIndices(new int[] {});
        }
      }
    });
*/
    centerPn.add(listBoxPnLeft);

    mRightButton = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE));

    mRightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveChannelsToRight();
      }
    });

    mLeftButton = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));

    mLeftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveChannelsToLeft();
      }
    });

    JPanel btnPanel = createButtonPn(mRightButton, mLeftButton);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(0, Sizes
        .dialogUnitXAsPixel(3, btnPanel), 0, Sizes.dialogUnitXAsPixel(3,
        btnPanel)));
    listBoxPnLeft.add(btnPanel, BorderLayout.EAST);

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
    new DragAndDropMouseListener(mAllChannels, mSubscribedChannels, this,
        mDnDHandler);
    mSubscribedChannelListener = new DragAndDropMouseListener(
        mSubscribedChannels, mAllChannels, this, mDnDHandler);

    restoreForPopup();

    listBoxPnRight.add(new JScrollPane(mSubscribedChannels),
        BorderLayout.CENTER);

    final JButton configureChannels = new JButton(mLocalizer.msg(
        "configSelectedChannels", "Configure selected channels"));
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

    // use INSERT key on left side to move selected channels to active channel
    // list
    mAllChannels.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_INSERT) {
          moveChannelsToRight();
        }
      }
    });

    // use DELETE key on right side to remove selected channels
    mSubscribedChannels.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          moveChannelsToLeft();
        }
      }
    });

    JPanel btnPanel2 = createButtonPn(channelList.getTopButton(), channelList
        .getUpButton(), channelList.getDownButton(), channelList
        .getBottomButton());
    btnPanel2.setBorder(BorderFactory.createEmptyBorder(0, Sizes
        .dialogUnitXAsPixel(3, btnPanel2), 0, 0));
    listBoxPnRight.add(btnPanel2, BorderLayout.EAST);

    centerPn.add(listBoxPnRight);

    final JPanel result = new JPanel(new BorderLayout());
    result.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentHidden(ComponentEvent e) {
        if (e.getComponent() == result) {
          mRefreshListTimer = null;
        }
      }

    });

    result.add(createFilterPanel(), BorderLayout.NORTH);

    result.add(panel, BorderLayout.CENTER);

    LinkButton urlLabel = new LinkButton(
        mLocalizer
            .msg(
                "addMoreChannels",
                "You want to add your own channels? Click here!"),
        mLocalizer.msg("addMoreChannelsUrl",
            "http://enwiki.tvbrowser.org/index.php/Available_stations"));

    JPanel buttonsPanel = new JPanel(new BorderLayout());

    // buttonsPanel.add(pn2, BorderLayout.EAST);
    buttonsPanel.add(urlLabel, BorderLayout.SOUTH);

    result.add(buttonsPanel, BorderLayout.SOUTH);

    JButton refreshList = new JButton(mLocalizer.msg("updateChannelList",
        "Update channel list"), TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));

    refreshList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshChannelList();
      }
    });

    southPn.add(refreshList, BorderLayout.WEST);
    southPn.add(configureChannels, BorderLayout.EAST);

    mListUpdating = true;
    updateFilterPanel();
    fillSubscribedChannelsListBox();
    fillAvailableChannelsListBox();
    mListUpdating = false;

    panel.addAncestorListener(new AncestorListener() {
      public void ancestorRemoved(AncestorEvent event) {
        Settings.propSelectedChannelCategoryIndex.setByte((byte)mCategoryCB.getSelectedIndex());
        String country = "";
        if (mCountryCB.getSelectedIndex() >= 0) {
          Object object = ((FilterItem)mCountryCB.getSelectedItem()).getValue();
          if (object != null) {
            country = object.toString();
          }
        }
        Settings.propSelectedChannelCountry.setString(country);
      }

      public void ancestorAdded(AncestorEvent event) {
        if (!mInitChannelsAsked && mChannelListModel.getAvailableChannels().length == 0){
          mInitChannelsAsked = true;
          int ret = JOptionPane.showConfirmDialog(result,
              mLocalizer.msg("loadChannelsQuestion", "Should I download the channel list?"),
              mLocalizer.msg("loadChannelsTitle", "No channels found"),
              JOptionPane.YES_NO_OPTION);
          if (ret == JOptionPane.YES_OPTION) {
            refreshChannelList();
          }
        }
      }

      public void ancestorMoved(AncestorEvent event) {}
    });

    result.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
    return result;
  }

  /**
   * Create the Panel with the Filter-Interface
   *
   * @return Panel with Filter Interface
   */
  private JPanel createFilterPanel() {
    JPanel filter = new JPanel(new FormLayout("fill:pref:grow",
        "pref, 3dlu, pref, 3dlu"));

    CellConstraints cc = new CellConstraints();

    JComponent filterSeparator = DefaultComponentFactory.getInstance()
        .createSeparator(
            mLocalizer.msg("channelFilter", "Channel Filter") + ":");
    filterSeparator.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

    filter.add(filterSeparator, cc.xy(1, 1));

    JPanel filterPanel = new JPanel(new FormLayout(
        "pref, 3dlu, pref:grow, fill:60dlu, 3dlu, pref, 3dlu, pref:grow, pref",
        "pref, 3dlu, pref, pref"));

    mCountryCB = new JComboBox();
    filterPanel.add(new JLabel(mLocalizer.msg("country", "Country") + ":"), cc
        .xy(1, 1));
    filterPanel.add(mCountryCB, cc.xyw(3, 1, 2));

    mCategoryCB = new JComboBox();
    mCategoryCB.setMaximumRowCount(20);

    filterPanel.add(new JLabel(mLocalizer.msg("category", "Category") + ":"),
        cc.xy(6, 1));
    filterPanel.add(mCategoryCB, cc.xyw(8, 1, 2));

    JPanel namePanel = new JPanel(new BorderLayout());

    namePanel.add(new JLabel(mLocalizer.msg("filterText",
        "With the following Text")
        + ": "), BorderLayout.WEST);

    mChannelName = new JTextField();
    namePanel.add(mChannelName, BorderLayout.CENTER);

    filterPanel.add(namePanel, cc.xyw(1, 3, 4));
    
    mPluginCB = new JComboBox();
    mPluginCB.setMaximumRowCount(20);

    if (mShowPlugins) {
      filterPanel.add(new JLabel(mLocalizer.msg("plugin", "Plugin") + ":"),
        cc.xy(6, 3));
      filterPanel.add(mPluginCB, cc.xyw(8, 3, 2));
    }
    
    JButton reset = new JButton(mLocalizer.msg("reset", "Reset"));

    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mChannelName.setText("");
        mCategoryCB.setSelectedIndex(1);
        mCountryCB.setSelectedIndex(0);
        mPluginCB.setSelectedIndex(0);
      }
    });

    if (mShowPlugins) {
      filterPanel.add(reset, cc.xy(9, 4));
    } else {
      filterPanel.add(reset, cc.xy(9, 3));
    }

    filter.add(filterPanel, cc.xy(1, 3));

    final ItemListener filterItemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if ((e == null) || (e.getStateChange() == ItemEvent.SELECTED)) {
          if (!mListUpdating) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                mListUpdating = true;
                fillAvailableChannelsListBox();
                mListUpdating = false;
              }
            });
          }
        }
      }
    };

    mCountryCB.addItemListener(filterItemListener);
    mCategoryCB.addItemListener(filterItemListener);
    mPluginCB.addItemListener(filterItemListener);

    mChannelName.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        startTimer();
      }

      public void insertUpdate(DocumentEvent e) {
        startTimer();
      }

      public void removeUpdate(DocumentEvent e) {
        startTimer();
      }
    });

    return filter;
  }

  /**
   * restart list refresh timer
   */
  protected void startTimer() {
    if (mRefreshListTimer != null) {
      mRefreshListTimer.cancel();
    }
    mRefreshListTimer = new Timer("Refresh channel list");
    mRefreshListTimer.schedule(new TimerTask() {
      public void run() {
        if (!mListUpdating) {
          mListUpdating = true;
          fillAvailableChannelsListBox();
          mListUpdating = false;
          mRefreshListTimer = null;
        } else {
          // restart as update is currently not possible
          startTimer();
        }
      }
    }, REFRESH_AFTER_MS);
  }

  /**
   * Updates the FilterPanel and inserts Values in the Comboboxes
   */
  private void updateFilterPanel() {
    Channel[] allChannels = ChannelList.getAvailableChannels();

    mCategoryCB.removeAllItems();
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("allCategories",
        "All Categories"), null));

    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("allExceptEventCinema",
        "All except Event/Cinema"), new Integer[] { Channel.CATEGORY_TV,
        Channel.CATEGORY_RADIO, Channel.CATEGORY_NONE }));

    if (channelListContains(allChannels, Channel.CATEGORY_TV)) {
      addCategoryFilter(Channel.CATEGORY_TV);

      if (channelListContains(allChannels, Channel.CATEGORY_TV)) {
        mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNotSpecial",
            "TV"), new Integer[] { Channel.CATEGORY_TV,
          Channel.CATEGORY_SPECIAL_MUSIC * -1, Channel.CATEGORY_SPECIAL_SPORT * -1,
          Channel.CATEGORY_SPECIAL_NEWS * -1, Channel.CATEGORY_SPECIAL_OTHER * -1 }));
      }
      if (channelListContains(allChannels, Channel.CATEGORY_DIGITAL)) {
        addCategoryFilter(Channel.CATEGORY_DIGITAL);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_MUSIC)) {
        addCategoryFilter(Channel.CATEGORY_SPECIAL_MUSIC);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_SPORT)) {
        addCategoryFilter(Channel.CATEGORY_SPECIAL_SPORT);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_NEWS)) {
        addCategoryFilter(Channel.CATEGORY_SPECIAL_NEWS);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_SPECIAL_OTHER)) {
        addCategoryFilter(Channel.CATEGORY_SPECIAL_OTHER);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_TV)) {
        mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryFreeTv",
            "TV"), new Integer[] { Channel.CATEGORY_TV, Channel.CATEGORY_PAY_TV * -1}));
      }
      if (channelListContains(allChannels, Channel.CATEGORY_PAY_TV)) {
        addCategoryFilter(Channel.CATEGORY_PAY_TV);
      }
      if (channelListContains(allChannels, Channel.CATEGORY_RADIO)) {
        addCategoryFilter(Channel.CATEGORY_RADIO);
      }
    }
    if (channelListContains(allChannels, Channel.CATEGORY_CINEMA)) {
      addCategoryFilter(Channel.CATEGORY_CINEMA);
    }
    if (channelListContains(allChannels, Channel.CATEGORY_EVENTS)) {
      addCategoryFilter(Channel.CATEGORY_EVENTS);
    }
    if (channelListContains(allChannels, Channel.CATEGORY_PAYED_DATA_TV)) {
      addCategoryFilter(Channel.CATEGORY_PAYED_DATA_TV);
    }
    if (channelListContains(allChannels, Channel.CATEGORY_NONE)) {
      addCategoryFilter(Channel.CATEGORY_NONE);
    }

    if(mCategoryCB.getItemCount() > Settings.propSelectedChannelCategoryIndex.getByte()) {
      mCategoryCB.setSelectedIndex(Settings.propSelectedChannelCategoryIndex.getByte());
    }

    HashSet<String> countries = new HashSet<String>();

    for (Channel allChannel : allChannels) {
      String country = allChannel.getCountry();
      if (country != null) {
        countries.add(country.toLowerCase());
      }
    }

    mCountryCB.removeAllItems();
    mCountryCB.addItem(new FilterItem(mLocalizer.msg("allCountries",
        "All Countries"), null));
    ArrayList<FilterItem> items = new ArrayList<FilterItem>(countries.size());
    for (String country : countries) {
      Locale locale = new Locale(Locale.getDefault().getLanguage(), country);
      items.add(new FilterItem(locale.getDisplayCountry(), country));
    }
    Collections.sort(items);

    String defaultCountry = Settings.propSelectedChannelCountry.getString();
    for (FilterItem item : items) {
      mCountryCB.addItem(item);
      // select last used country (or default country of this system)
      if (!defaultCountry.isEmpty() && defaultCountry.equalsIgnoreCase(item.getValue().toString())) {
        mCountryCB.setSelectedIndex(mCountryCB.getItemCount() - 1);
      }
    }

    // select "all countries" if nothing else matches
    if(mCountryCB.getSelectedIndex() == -1) {
      mCountryCB.setSelectedIndex(0);
    }
    
    
    mPluginCB.removeAllItems();
        
    mPluginCB.addItem(new FilterItem(mLocalizer.msg("allPlugins",
    "All Plugins"), null));
    items = new ArrayList<FilterItem>();
    for(TvDataServiceProxy dataService : TvDataServiceProxyManager.getInstance().getDataServices()) {
      String name =dataService.getInfo().getName();
      items.add(new FilterItem(name,name));
    }
    Collections.sort(items);

    String defaultPlugin = Settings.propSelectedChannelPlugin.getString();
    for (FilterItem item : items) {
      mPluginCB.addItem(item);
      // select last used plugin
      if (!defaultPlugin.isEmpty() && defaultPlugin.equalsIgnoreCase(item.getValue().toString())) {
        mPluginCB.setSelectedIndex(mPluginCB.getItemCount() - 1);
      }
    }

    // select "all plugins" if nothing else matches
    if(mPluginCB.getSelectedIndex() == -1 || !mShowPlugins) {
      mPluginCB.setSelectedIndex(0);
    }
    
  }

  private void addCategoryFilter(final int category) {
    mCategoryCB.addItem(new FilterItem(Channel.getLocalizedCategory(category),
        category));
  }

  /**
   * Checks if a Channellist contains a specific Category
   *
   * @param allChannels
   *          Check in this List of Channels
   * @param category
   *          Search for this Category
   * @return true if category is present
   */
  private boolean channelListContains(Channel[] allChannels, int category) {
    for (int i = allChannels.length - 1; i >= 0; i--) {
      if ((allChannels[i].getCategories() & category) > 0) {
        return true;
      } else if ((allChannels[i].getCategories() == 0) && (category == 0)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Adds a mouse listener to the channel list
   */
  private void restoreForPopup() {
    final MouseAdapter listener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          final ChannelJList channelJList = (ChannelJList) e.getSource();
          channelJList.setSelectedIndex(channelJList
              .locationToIndex(e.getPoint()));
        }
        showPopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        showPopup(e);
      }
    };
    mSubscribedChannels.addMouseListener(listener);
    mAllChannels.addMouseListener(listener);
  }

  /**
   * Show the context menu for the channel list
   *
   * @param e
   *          Mouse Event
   */
  private void showPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      final ChannelJList channelJList = (ChannelJList) e.getSource();
      new ChannelContextMenu(e, (Channel) channelJList.getModel().getElementAt(
          channelJList.locationToIndex(e.getPoint())),
          this);
    }
  }

  /**
   * Create the ButtonPanel for up/down Buttons
   *
   * @param btn1
   *          Button 1
   * @param btn2
   *          Button 2
   * @return ButtonPanel
   */
  private JPanel createButtonPn(JButton btn1, JButton btn2) {
    JPanel result = new JPanel(new FormLayout("pref",
        "fill:pref:grow, pref, 3dlu, pref, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();

    result.add(btn1, cc.xy(1, 2));
    result.add(btn2, cc.xy(1, 4));

    return result;
  }

  /**
   * Create the ButtonPanel for up/down Buttons
   *
   * @param btn1
   *          Button 1
   * @param btn2
   *          Button 2
   * @return ButtonPanel
   */
  private JPanel createButtonPn(JButton btn1, JButton btn2, JButton btn3,
      JButton btn4) {
    JPanel result = new JPanel(
        new FormLayout("pref",
            "fill:pref:grow, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();

    result.add(btn1, cc.xy(1, 2));
    result.add(btn2, cc.xy(1, 4));
    result.add(btn3, cc.xy(1, 6));
    result.add(btn4, cc.xy(1, 8));

    return result;
  }

  private void saveSettingsInternal(boolean autoUpdate) {
    Object[] list = ((DefaultListModel) mSubscribedChannels.getModel())
        .toArray();

    // Convert the list into a Channel[] and fill channels
    ArrayList<String> groups = new ArrayList<String>();

    Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      channelArr[i] = (Channel) list[i];

      if (!groups.contains(channelArr[i].getGroup().getId())) {
        groups
            .add(new StringBuilder(channelArr[i].getDataServiceProxy()
            .getId())
                .append('.').append(channelArr[i].getGroup().getId())
                .toString());
      }
    }

    ChannelList.setSubscribeChannels(channelArr, autoUpdate);
    ChannelList.storeAllSettings();
    Settings.propSubscribedChannels.setChannelArray(channelArr);
    Settings.propUsedChannelGroups.setStringArray(groups
        .toArray(new String[groups.size()]));

    Settings.propChannelsWereConfigured.setBoolean(ChannelList.getNumberOfSubscribedChannels() > 0);

    if (!Settings.propTrayUseSpecialChannels.getBoolean()) {
      Channel[] tempArr = new Channel[channelArr.length > 10 ? 10
          : channelArr.length];
      System.arraycopy(channelArr, 0, tempArr, 0, tempArr.length);
      Settings.propTraySpecialChannels.setChannelArray(tempArr);
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    saveSettingsInternal(true);
  }


  public void saveSettingsWithoutDataUpdate() {
    saveSettingsInternal(false);
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "scroll-to-channel", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_CHANNELS);
  }

  /**
   * Fills the List with the subscribed CHannels
   */
  private void fillSubscribedChannelsListBox() {
    Channel[] subscribedChannelArr;

    subscribedChannelArr = new Channel[mChannelListModel.getSubscribedChannels().size()];
    ((DefaultListModel) mSubscribedChannels.getModel()).clear();

    Channel[] channels = mChannelListModel.getAvailableChannels();
    for (Channel channel : channels) {
      int pos = ChannelList.getPos(channel);

      if (pos != -1 && pos < subscribedChannelArr.length) {
        subscribedChannelArr[pos] = channel;
      }
    }

    for (Channel aSubscribedChannelArr : subscribedChannelArr) {
      if(aSubscribedChannelArr != null) {
        ((DefaultListModel) mSubscribedChannels.getModel())
            .addElement(aSubscribedChannelArr);
      }
    }
    mLeftButton.setEnabled(mSubscribedChannels.getModel().getSize() > 0);

    updateChannelNumbers();
  }

  private void updateChannelNumbers() {
    String text = mLocalizer.msg("channelCount", "subscribed to {0} of {1} channels", mSubscribedChannels.getModel().getSize(), mAllChannels.getModel().getSize());
    mAvailableSeparator.setToolTipText(text);
    mSubscribedSeparator.setToolTipText(text);
  }

  /**
   * Fills the List with the available Channels
   */
  private void fillAvailableChannelsListBox() {
    Object oldSelectedChannel = mAllChannels.getSelectedValue();
    FilterItem selectedCountry = (FilterItem) mCountryCB.getSelectedItem();
    FilterItem selectedCategory = (FilterItem) mCategoryCB.getSelectedItem();
    FilterItem selectedPlugin = (FilterItem) mPluginCB.getSelectedItem();
    if (selectedCountry == null || selectedCategory == null 
        || selectedPlugin == null) {
      return;
    }

    String country = (String) (selectedCountry).getValue();
    String plugin = (String) (selectedPlugin).getValue();

    if ((selectedCategory).getValue() instanceof Integer[]) {
      Integer[] categoryInt = (Integer[]) (selectedCategory).getValue();

      int[] categories = new int[categoryInt.length];

      int max = categoryInt.length;

      for (int i = 0; i < max; i++) {
        if (categoryInt[i] != null) {
          categories[i] = categoryInt[i];
        } else {
          categories[i] = Integer.MAX_VALUE;
        }
      }

      mFilter.setFilter(country, categories, mChannelName.getText(), plugin);
    } else {
      Integer categoryInt = (Integer) (selectedCategory).getValue();
      int categories = Integer.MAX_VALUE;
      if (categoryInt != null) {
        categories = categoryInt;
      }
      mFilter.setFilter(country, categories, mChannelName.getText(), plugin);
    }

    // Split the channels in subscribed and available
    Channel[] channels = mChannelListModel.getAvailableChannels();
    ArrayList<Channel> availableChannelList = new ArrayList<Channel>();

    for (Channel channel : channels) {
      if (!((DefaultListModel) mSubscribedChannels.getModel())
          .contains(channel)
          && mFilter.accept(channel)) {
        availableChannelList.add(channel);
      }
    }

    // Sort the available channels
    Channel[] availableChannelArr = new Channel[availableChannelList.size()];
    availableChannelList.toArray(availableChannelArr);
    Arrays.sort(availableChannelArr, createChannelComparator());

    DefaultListModel newModel = new DefaultListModel();
    // Add the available channels
    for (Channel anAvailableChannelArr : availableChannelArr) {
      newModel.addElement(anAvailableChannelArr);
    }
    mAllChannels.setModel(newModel);

    mRightButton.setEnabled(!newModel.isEmpty());
    mAllChannels.setEnabled(!newModel.isEmpty());
    if (mAllChannels.getModel().getSize() == 0) {
      ((DefaultListModel) mAllChannels.getModel()).addElement(mLocalizer.msg(
          "noChannelFound", "No Channel Found"));
    }
    else {
      Object newSelection = availableChannelArr[0];
      if (oldSelectedChannel != null) {
        if (newModel.contains(oldSelectedChannel)) {
          newSelection = oldSelectedChannel;
        }
      }
      mAllChannels.setSelectedValue(newSelection, true);
    }
    mSubscribedChannels.repaint();
    mSubscribedChannelListener.restore();
    restoreForPopup();
    updateChannelNumbers();
  }

  /**
   * Creates a Comparator that is able to compare Channels
   *
   * @return ChannelComparator
   */
  private Comparator<Channel> createChannelComparator() {
    return new Comparator<Channel>() {
      public int compare(Channel o1, Channel o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    };
  }

  /**
   * Refresh the ChannelList
   */
  private void refreshChannelList() {
    if (!NetworkUtilities.checkConnection()) {
      JOptionPane.showMessageDialog(null,
          mLocalizer.msg("noConnection.message", "No connection to the Internet established.\n\nThe channel list can only be updated if a connection\nto the Internet is available."),
          mLocalizer.msg("noConnection.title", "No connection!"),
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    final ProgressWindow win = new ProgressWindow(
        tvbrowser.ui.mainframe.MainFrame.getInstance());

    win.run(new Progress() {
      public void run() {
        Channel[] channels = mChannelListModel.getAvailableChannels();
        // make a copy of the channel list
        final ArrayList<Channel> before = new ArrayList<Channel>();
        for (Channel channel : channels) {
          before.add(channel);
        }
        ChannelGroupManager.getInstance().checkForAvailableGroupsAndChannels(
            win);

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            mChannelListModel.refresh();
            updateFilterPanel();
            fillSubscribedChannelsListBox();
            fillAvailableChannelsListBox();
            showChannelChanges(before);
          }

        });
      }
    });
  }

  private void showChannelChanges(ArrayList<Channel> channelsBefore) {
    Channel[] channels = mChannelListModel.getAvailableChannels();
    Channel[] channelsAfterArr = new Channel[channels.length];
    System.arraycopy(channels, 0, channelsAfterArr, 0, channelsAfterArr.length);
    List<Channel> channelsAfter = Arrays.asList(channelsAfterArr);
    ChannelListChangesDialog.showChannelChanges(SettingsDialog.getInstance().getDialog(), channelsBefore, channelsAfter);
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

      Window parent = UiUtilities.getBestDialogParent(mAllChannels);
      dialog = new ChannelConfigDlg(parent, channelList[0]);
      dialog.centerAndShow();
      MainFrame.getInstance().getProgramTableScrollPane()
          .updateChannelLabelForChannel(channelList[0]);
    } else if (channelList.length > 1) {
      MultiChannelConfigDlg dialog;

      Window parent = UiUtilities.getBestDialogParent(mAllChannels);
      dialog = new MultiChannelConfigDlg(parent, channelList);
      dialog.centerAndShow();
    }

    mSubscribedChannels.repaint();
    mSubscribedChannelListener.restore();
    restoreForPopup();
  }

  /**
   * Move Channels to the subscribed Channels
   */
  private void moveChannelsToRight() {
    Object[] objects = UiUtilities.moveSelectedItems(mAllChannels,
        mSubscribedChannels);
    boolean missingIcon = false;

    for (Object object : objects) {
      if (object instanceof Channel) {
        Channel channel = (Channel) object;
        mChannelListModel.subscribeChannel(channel);
        if (channel.getIcon() == null) {
          missingIcon = true;
        }
      }
    }

    if (missingIcon) {
      DontShowAgainOptionBox
          .showOptionDialog(
              "missingIcon",
              MainFrame.getInstance(),
              mLocalizer
                  .msg(
                      "noIconAvailable.message",
                      "You have added a channel without channel icon. Due to copyright reasons we cannot provide icons for each channel.\nFor better visual differentiation you can add your icon to the channel using the right mouse menu in the channel list."),
              mLocalizer.msg("noIconAvailable.title", "No channel icon"));
    }
  }

  /**
   * Move Channels from the subscribed Channels
   */
  private void moveChannelsToLeft() {
    Object[] objects = UiUtilities.moveSelectedItems(mSubscribedChannels,
        mAllChannels);
    for (Object object : objects) {
      mChannelListModel.unsubscribeChannel((Channel) object);
    }
    mLeftButton.setEnabled(mSubscribedChannels.getModel().getSize() > 0);
    fillAvailableChannelsListBox();
  }

  // Move the channels to the row where the dropping was.
  private void moveChannels(int row) {
    Object[] objects = UiUtilities.moveSelectedItems(mAllChannels,
        mSubscribedChannels, row);
    for (Object object : objects) {
      mChannelListModel.subscribeChannel((Channel) object);
    }
  }

  public void drop(JList source, JList target, int n, boolean move) {
    if (source.equals(mAllChannels) && !source.equals(target) && move) {
      moveChannelsToRight();
    } else if (source.equals(mSubscribedChannels) && !source.equals(target)
        && move) {
      moveChannelsToLeft();
    } else if (source.equals(mSubscribedChannels)
        && target.equals(mSubscribedChannels)) {
      UiUtilities.moveSelectedItems(target, n, true);
    } else if (source.equals(mAllChannels)
        && target.equals(mSubscribedChannels)) {
      moveChannels(n);
    } else if (source.equals(mSubscribedChannels)
        && target.equals(mAllChannels)) {
      moveChannelsToLeft();
    }
  }
}