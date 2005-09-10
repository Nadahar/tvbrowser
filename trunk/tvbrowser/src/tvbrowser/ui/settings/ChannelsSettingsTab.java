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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelListCellRenderer;
import util.ui.LinkButton;
import util.ui.TransferEntries;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.ChannelGroup;

/**
 * This Class represents the Channel-Settings-Tab
 * 
 * @author Bodo Tasche
 */
public class ChannelsSettingsTab implements devplugin.SettingsTab,DragGestureListener,DropTargetListener {



  /** Translation */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTab.class);

  /** Show all Buttons ?*/
  private boolean mShowAllButtons;

  /** The List with all Channels */
  private JList mAllChannels;

  /** The List with the current subscribed Channels */
  private JList mSubscribedChannels;

  /** Name of the current selected Channel*/
  private JLabel mChannelName;

   /** Country of the current selected Channel*/
  private JLabel mChannelCountry;

  /** TimeZone of the current selected Channel*/
  private JLabel mChannelTimeZone;

  /** Provider of the current selected Channel*/
  private JLabel mChannelProvider;

  /** Comboboxes for filtering */
  private JComboBox mCategoryCB, mProviderCB, mTimezoneCB, mCountryCB;

  /** Model of the list boxes */
  private ChannelListModel mChannelListModel;
  
  
  private Rectangle2D mCueLine = new Rectangle2D.Float();
  private int mOldIndex = -1;
  private boolean mSwitched = false;
  
  /**
   * Creates the SettingsTab
   */
  public ChannelsSettingsTab() {
    this(true);
  }

  /**
   * Create the SettingsTab
   * 
   * @param showAllButtons true, if the refresh/config-Buttons should be displayed
   */
  public ChannelsSettingsTab(boolean showAllButtons) {
    mShowAllButtons = showAllButtons;
    mChannelListModel = new ChannelListModel();
  }  

  /**
   * Create the SettingsPanel
   * @return the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new BorderLayout());


    JPanel northPn = new JPanel(new GridLayout(1,2));
    JPanel centerPn = new JPanel(new GridLayout(1,2));
    JPanel southPn = new JPanel(new BorderLayout());

    panel.add(northPn, BorderLayout.NORTH);
    panel.add(centerPn, BorderLayout.CENTER);
    panel.add(southPn, BorderLayout.SOUTH);

    northPn.add(new JLabel(mLocalizer.msg("availableChannels", "Available channels")), BorderLayout.NORTH);
    northPn.add(new JLabel(mLocalizer.msg("subscribedChannels", "Subscribed channels")), BorderLayout.NORTH);


    // left list box
    JPanel listBoxPnLeft = new JPanel(new BorderLayout());
    mAllChannels = new JList(new DefaultListModel());
    mAllChannels.setCellRenderer(new ChannelListCellRenderer());
    
    // DropTarget for dropping the channels
    new DropTarget(mAllChannels, this);    

    listBoxPnLeft.add(new JScrollPane(mAllChannels), BorderLayout.CENTER);

    centerPn.add(listBoxPnLeft);

    // Buttons in the Middle
    JButton rightBt = new JButton(new ImageIcon("imgs/Forward24.gif"));
    rightBt.setMargin(UiUtilities.ZERO_INSETS);

    rightBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveChannelsToRight();
      }
    });

    JButton leftBt = new JButton(new ImageIcon("imgs/Back24.gif"));
    leftBt.setMargin(UiUtilities.ZERO_INSETS);

    leftBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { 
        moveChannelsToLeft();
      }
    });

    listBoxPnLeft.add(createButtonPn(rightBt, leftBt), BorderLayout.EAST);

    // right list box
    JPanel listBoxPnRight = new JPanel(new BorderLayout());
    SortableItemList channelList = new SortableItemList();

    mSubscribedChannels = channelList.getList();    
    mSubscribedChannels.setCellRenderer(new ChannelListCellRenderer());
    
    // DropTarget for dropping the channels
    new DropTarget(mSubscribedChannels, this);
        
    
      // Remove the MouseListeners and the MouseMotionListeners
      // of the lists.
    MouseListener[] ml = mAllChannels.getMouseListeners();
    for(int i = 0; i < ml.length; i++)
      mAllChannels.removeMouseListener(ml[i]);
    
    MouseMotionListener[] m2 =  mAllChannels.getMouseMotionListeners();
    for(int i = 0; i < m2.length; i++)
      mAllChannels.removeMouseMotionListener(m2[i]);

    ml = mSubscribedChannels.getMouseListeners();
    for(int i = 0; i < ml.length; i++)
      mSubscribedChannels.removeMouseListener(ml[i]);
    
    m2 =  mSubscribedChannels.getMouseMotionListeners();
    for(int i = 0; i < m2.length; i++)
      mSubscribedChannels.removeMouseMotionListener(m2[i]);

      // Set up the DragSources
    (new DragSource()).createDefaultDragGestureRecognizer(mSubscribedChannels,DnDConstants.ACTION_MOVE,this);    
    (new DragSource()).createDefaultDragGestureRecognizer(mAllChannels,DnDConstants.ACTION_MOVE,this);
    
    mAllChannels.addMouseListener(new MouseAdapter() {
        private int lastSelectedIndex = 0;
        
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
              int index = mAllChannels.locationToIndex(e.getPoint());
              mAllChannels.setSelectedIndex(index);
              moveChannelsToRight();
              lastSelectedIndex = index;
            }
        }
        
         // Rebuild the selection of the JList with the needed
         // functions.
        public void mousePressed(MouseEvent e) {          
          if(mAllChannels.getSelectedIndices().length <= 1 
              && !e.isShiftDown() &&
              !e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
            int index = mAllChannels.locationToIndex(e.getPoint());
            mAllChannels.setSelectedIndex(index);
            lastSelectedIndex = index;
          }
          else if(e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
            int index = mAllChannels.locationToIndex(e.getPoint());
            mAllChannels.setSelectionInterval(index,lastSelectedIndex);
          } 
          else if(!e.isControlDown() &&
              !e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
            DefaultListSelectionModel model = (DefaultListSelectionModel)mAllChannels.getSelectionModel();
            int index = mAllChannels.locationToIndex(e.getPoint());
            if(!model.isSelectedIndex(index)) {
              mAllChannels.setSelectedIndex(index);
              lastSelectedIndex = index;
            }
          }
        }
 
        // Rebuild the selection of the JList with the needed
        // functions.
        public void mouseReleased(MouseEvent e) {
          int index = mAllChannels.locationToIndex(e.getPoint());
          
          if(e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {          
            DefaultListSelectionModel model = (DefaultListSelectionModel)mAllChannels.getSelectionModel();
            
            if(model.isSelectedIndex(index))
              model.removeSelectionInterval(index,index);
            else
              model.addSelectionInterval(index,index);
            
            lastSelectedIndex = index;
          }
          else if(!e.isShiftDown() && !e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
            mAllChannels.setSelectedIndex(index);
            lastSelectedIndex = index;
          }
        }
    });

    mSubscribedChannels.addMouseListener(new MouseAdapter() {
      private int lastSelectedIndex = 0;
      
      public void mouseClicked(MouseEvent e) {
          if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            int index = mSubscribedChannels.locationToIndex(e.getPoint());
            mSubscribedChannels.setSelectedIndex(index);
            moveChannelsToLeft();
            lastSelectedIndex = index;
          }
      }
 
      // Rebuild the selection of the JList with the needed
      // functions.
      public void mousePressed(MouseEvent e) {          
        if(mSubscribedChannels.getSelectedIndices().length <= 1 
            && !e.isControlDown() &&
            !e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
          int index = mSubscribedChannels.locationToIndex(e.getPoint());
          mSubscribedChannels.setSelectedIndex(index);
          lastSelectedIndex = index;
        }        
        else if(e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
          int index = mSubscribedChannels.locationToIndex(e.getPoint());
          mSubscribedChannels.setSelectionInterval(index,lastSelectedIndex);
        }
        else if(!e.isControlDown() &&
            !e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
          DefaultListSelectionModel model = (DefaultListSelectionModel)mSubscribedChannels.getSelectionModel();
          int index = mSubscribedChannels.locationToIndex(e.getPoint());
          if(!model.isSelectedIndex(index)) {
            mSubscribedChannels.setSelectedIndex(index);
            lastSelectedIndex = index;
          }
        }
        
      }
      
      // Rebuild the selection of the JList with the needed
      // functions.     
      public void mouseReleased(MouseEvent e) {
        int index = mSubscribedChannels.locationToIndex(e.getPoint());
        
        if(e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {          
          DefaultListSelectionModel model = (DefaultListSelectionModel)mSubscribedChannels.getSelectionModel();
          
          if(model.isSelectedIndex(index))
            model.removeSelectionInterval(index,index);
          else
            model.addSelectionInterval(index,index);
          
          lastSelectedIndex = index;
        }
        else if(!e.isShiftDown() && !e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
          mSubscribedChannels.setSelectedIndex(index);
          lastSelectedIndex = index;
        }
      }
    });
    
    listBoxPnRight.add(new JScrollPane(mSubscribedChannels), BorderLayout.CENTER);

    final JButton configureChannels = new JButton(mLocalizer.msg("configSelectedChannels","Configure selected channels"));
    configureChannels.setEnabled(false);
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


    listBoxPnRight.add(createButtonPn(channelList.getUpButton(), channelList.getDownButton()), BorderLayout.EAST);

    centerPn.add(listBoxPnRight);

    // south panel (filter, channel details, buttons)
    JPanel pn1 = new JPanel(new GridLayout(1,2));
    pn1.add(createFilterPanel());
    pn1.add(createDetailsPanel());

    southPn.add(pn1, BorderLayout.CENTER);

    fillSubscribedChannelsListBox();
    fillAvailableChannelsListBox();

    JPanel result = new JPanel(new BorderLayout());
    result.add(panel, BorderLayout.CENTER);

    if (mShowAllButtons) {
      JButton refreshList = new JButton(mLocalizer.msg("updateChannelList","Update channel list"),new ImageIcon("imgs/Refresh16.gif"));

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

      JPanel pn2 = new JPanel(new GridLayout(1,2));
      pn2.setBorder(BorderFactory.createEmptyBorder(3,0,2,0));
      pn2.add(refreshList);
      pn2.add(configureChannels);
      southPn.add(pn2, BorderLayout.SOUTH);

      LinkButton urlLabel = new LinkButton(
         mLocalizer.msg("addMoreChannels","Ihnen fehlt Ihr Lieblings-Sender? Clicken Sie hier fï¿½r eine Liste weiterer Sender."),
         mLocalizer.msg("addMoreChannelsUrl", "http://wiki.tvbrowser.org/index.php/Senderliste"));

      result.add(urlLabel, BorderLayout.SOUTH);
    }

    result.setBorder(BorderFactory.createEmptyBorder(9,9,9,4));
    return result;
  }


  private JPanel createButtonPn(JButton btn1, JButton btn2) {
    JPanel result = new JPanel(new GridLayout(2,1));
    JPanel topPn = new JPanel(new BorderLayout());
    JPanel bottomPn = new JPanel(new BorderLayout());
    topPn.add(btn1, BorderLayout.SOUTH);
    bottomPn.add(btn2, BorderLayout.NORTH);
    result.add(topPn);
    result.add(bottomPn);
    result.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
    return result;

  }

  /**
   * Create the FilterPanel.
   * 
   * @return the FilterPanel
   */
  private Component createFilterPanel() {

    JPanel panel = new JPanel();

    mCountryCB = new JComboBox();
    mTimezoneCB = new JComboBox();
    mCategoryCB = new JComboBox();
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("allCategories","All Categories"), null));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryTV","TV"), new Integer(Channel.CATEGORY_TV)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryCinema","Kino"), new Integer(Channel.CATEGORY_CINEMA)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryEvents","Events"), new Integer(Channel.CATEGORY_EVENTS)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryDigital","Digitale"), new Integer(Channel.CATEGORY_DIGITAL)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categorySpecial","Alle SpartenkanÃ¤le"), new Integer(Channel.CATEGORY_SPECIAL_MUSIC | Channel.CATEGORY_SPECIAL_NEWS | Channel.CATEGORY_SPECIAL_OTHER | Channel.CATEGORY_SPECIAL_SPORT)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryMusic","Musik"), new Integer(Channel.CATEGORY_SPECIAL_MUSIC)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categorySport", "Sport"), new Integer(Channel.CATEGORY_SPECIAL_SPORT)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNews", "Nachrichten"), new Integer(Channel.CATEGORY_SPECIAL_NEWS)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryOthers","Sonstige Sparten"), new Integer(Channel.CATEGORY_SPECIAL_OTHER)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryRadio","Radio"), new Integer(Channel.CATEGORY_RADIO)));
    mCategoryCB.addItem(new FilterItem(mLocalizer.msg("categoryNone","Not categorized"), new Integer(Channel.CATEGORY_NONE)));


    mProviderCB = new JComboBox();

    mCountryCB.addItemListener(mFilterItemListener);
    mTimezoneCB.addItemListener(mFilterItemListener);
    mCategoryCB.addItemListener(mFilterItemListener);
    mProviderCB.addItemListener(mFilterItemListener);

    panel.setLayout(new FormLayout("default, 3dlu, default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));

    CellConstraints c = new CellConstraints();

    panel.add(new JLabel("Filter:"), c.xyw(1, 1, 3));
    panel.add(mCountryCB, c.xy(1, 3));
    panel.add(mTimezoneCB, c.xy(1, 5));
    panel.add(mProviderCB, c.xy(1, 7));
    panel.add(mCategoryCB, c.xy(1, 9));

    updateFilterPanel();

    return panel;
  }



  private void updateFilterPanel() {
    Channel[] allChannels = ChannelList.getAvailableChannels();

    HashMap groups = new HashMap();
    HashSet countries = new HashSet();
    for (int i=0; i<allChannels.length; i++) {
      ChannelGroup group = allChannels[i].getGroup();
      String groupKey;
      if (group != null) {
        groupKey = group.getProviderName();
        if (groupKey == null) {
          groupKey = allChannels[i].getDataService().getClass().getName()+"_"+group.getId();
        }
      } else {
        groupKey = allChannels[i].getDataService().getClass().getName();
      }
      String s = getProviderName(allChannels[i]);
      if (s != null) {
        groups.put(groupKey, s);
      }

      String country = allChannels[i].getCountry();
      if (country != null) {
        countries.add(country);
      }
    }

    mCountryCB.removeAllItems();
    mCountryCB.addItem(new FilterItem(mLocalizer.msg("allCountries","All Countries"), null));
    Iterator it = countries.iterator();
    while (it.hasNext()) {
      String country = (String)it.next();
      Locale locale = new Locale(Locale.getDefault().getLanguage(), country);
      mCountryCB.addItem(new FilterItem(locale.getDisplayCountry(), country));
    }

    mTimezoneCB.removeAllItems();
    mTimezoneCB.addItem(new FilterItem(mLocalizer.msg("allTimezones","All Timezones"), null));
    TimeZone zone = TimeZone.getDefault();
    mTimezoneCB.addItem(new FilterItem(zone.getDisplayName(), zone));
    for (int i=-12; i<12; i++) {
      zone = new SimpleTimeZone(i*1000*60*60, "GMT"+(i>=0?"+":"")+i);
      mTimezoneCB.addItem(new FilterItem(zone.getDisplayName(), zone));
    }

    mProviderCB.removeAllItems();
    mProviderCB.addItem(new FilterItem(mLocalizer.msg("allProviders","All Providers"), null));
    Object[] providerNames = groups.values().toArray();
    for (int i=0; i<providerNames.length; i++) {
      mProviderCB.addItem(new FilterItem((String)providerNames[i], providerNames[i]));
    }
  }

  private ItemListener mFilterItemListener = new ItemListener(){
    public void itemStateChanged(ItemEvent e) {
      fillAvailableChannelsListBox();
    }
  };

  /**
   * Create the Details Panel for the Channels
   * 
   * @return Details-Panel
   */
  private Component createDetailsPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new FormLayout("default, 3dlu, default:grow",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));

    CellConstraints c = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("details","Details")+":"), c.xyw(1, 1, 3));

    panel.add(new BoldLabel(mLocalizer.msg("channel","Kanal")+":"), c.xy(1, 3));

    mChannelName = new JLabel();
    panel.add(mChannelName, c.xy(3, 3));

    panel.add(new BoldLabel(mLocalizer.msg("country","Land")+":"), c.xy(1, 5));

    mChannelCountry = new JLabel();
    panel.add(mChannelCountry, c.xy(3, 5));

    panel.add(new BoldLabel(mLocalizer.msg("timezone","Zeitzone")+":"), c.xy(1, 7));

    mChannelTimeZone = new JLabel();
    panel.add(mChannelTimeZone, c.xy(3, 7));

    panel.add(new BoldLabel(mLocalizer.msg("provider","Betreiber")+":"), c.xy(1, 9));

    mChannelProvider = new JLabel();
    panel.add(mChannelProvider, c.xy(3, 9));



    return panel;
  }

  /**
   * Update the Details-Panel with the current selected Channel
   */
  private void updateDetailPanel() {
    Object[] ch = mSubscribedChannels.getSelectedValues();

    if ((ch != null) && (ch.length == 1)) {
      Channel channel = (Channel) ch[0];
      if (channel != null) {
        mChannelName.setText(channel.getName());
        Locale loc = new Locale(Locale.getDefault().getLanguage(), channel.getCountry());
        mChannelCountry.setText(loc.getDisplayCountry());
        mChannelTimeZone.setText(channel.getTimeZone().getDisplayName());
        if (channel.getGroup() != null) {
          mChannelProvider.setText(channel.getGroup().getProviderName());
        }
        else {
          mChannelProvider.setText("-");
        }
      }
    } else {
      mChannelName.setText("");
      mChannelCountry.setText("");
      mChannelTimeZone.setText("");
      mChannelProvider.setText("");
    }

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
    Settings.propSubscribedChannels.setChannelArray(channelArr);

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
    return mLocalizer.msg("channels","Channels");
  }

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


  private void fillAvailableChannelsListBox() {
    FilterItem selectedTimeZone = (FilterItem)mTimezoneCB.getSelectedItem();
    FilterItem selectedCountry = (FilterItem)mCountryCB.getSelectedItem();
    FilterItem selectedProvider = (FilterItem)mProviderCB.getSelectedItem();
    FilterItem selectedCategory = (FilterItem)mCategoryCB.getSelectedItem();

    if (selectedTimeZone == null || selectedCountry == null || selectedProvider == null || selectedCategory == null) {
      return;
    }

    TimeZone timeZone = (TimeZone)(selectedTimeZone).getValue();
    String country = (String)(selectedCountry).getValue();
    String providerName = (String)(selectedProvider).getValue();
    Integer categoryInt = (Integer)(selectedCategory).getValue();
    int categories = 0;
    if (categoryInt != null) {
      categories = categoryInt.intValue();
    }

    ChannelFilter filter = new ChannelFilter(timeZone, country, providerName, categories);
    ((DefaultListModel) mAllChannels.getModel()).removeAllElements();

    // Split the channels in subscribed and available
    Channel[] channels = mChannelListModel.getAvailableChannels();
    ArrayList availableChannelList = new ArrayList();

    for (int i = 0; i < channels.length; i++) {
      Channel channel = channels[i];
      if (!((DefaultListModel) mSubscribedChannels.getModel()).contains(channel) && filter.accept(channel)) {
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
  }

  /**
   * Creates a Comparator that is able to compare Channels
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
              services[i].checkForAvailableChannels(null, win);
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
  private void configChannels() {
    Object[] o=mSubscribedChannels.getSelectedValues();

    Channel[] channelList=new Channel[o.length];
    for (int i=0;i<o.length;i++) {
      channelList[i]=(Channel)o[i];
    }
    
    if (channelList.length == 1) {
      ChannelConfigDlg dialog;
      
      Window w = UiUtilities.getBestDialogParent(mAllChannels);
      if (w instanceof JDialog) {
        dialog = new ChannelConfigDlg((JDialog)w, channelList[0]);
      } else {
        dialog = new ChannelConfigDlg((JFrame)w, channelList[0]);
      }
      dialog.centerAndShow();
    } else if (channelList.length > 1) {
      MultiChannelConfigDlg dialog;
      
      Window w = UiUtilities.getBestDialogParent(mAllChannels);
      if (w instanceof JDialog) {
        dialog = new MultiChannelConfigDlg((JDialog)w, channelList);
      } else {
        dialog = new MultiChannelConfigDlg((JFrame)w, channelList);
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
    for (int i=0; i<objects.length; i++) {
      mChannelListModel.subscribeChannel((Channel)objects[i]);
    }

  }

  /**
   * Move Channels from the subscribed Channels
   *
   */
  private void moveChannelsToLeft() {
    Object[] objects = UiUtilities.moveSelectedItems(mSubscribedChannels, mAllChannels);
    for (int i=0; i<objects.length; i++) {
      mChannelListModel.unsubscribeChannel((Channel)objects[i]);
    }
    fillAvailableChannelsListBox();
  }

  private String getProviderName(Channel ch) {
    ChannelGroup group = ch.getGroup();
    if (group == null) {
      return ch.getDataService().getInfo().getName();
    }

    return group.getProviderName();
  }



  private class ChannelFilter {
    private TimeZone mTimezone;
    private String mCountry;
    private String mProviderName;
    private int mCategories;
    public ChannelFilter(TimeZone timeZone, String country, String providerName, int categories) {
      mTimezone = timeZone;
      mCountry = country;
      mProviderName = providerName;
      mCategories = categories;
    }

    public boolean accept(Channel channel) {
      if (mTimezone != null) {
        if (channel.getTimeZone().getRawOffset() != mTimezone.getRawOffset()) {
          return false;
        }
      }

      if (mCountry != null) {
        String country = channel.getCountry();
        if (country!=null) {
          if (!country.equals(mCountry)) {
            return false;
          }
        }
        else {
          return false;
        }
      }

      if (mProviderName != null) {
        String provName = getProviderName(channel);
        if (provName == null || !provName.equals(mProviderName)) {
          return false;
        }
      }

      if (mCategories > 0) {
        if ((channel.getCategories() & mCategories) == 0) {
          return false;
        }
      }

      return true;
    }
  }

  private class FilterItem {

    private String mName;
    private Object mValue;

    public FilterItem(String name, Object value) {
      mName = name;
      mValue = value;
    }

    public String getName() {
      return mName;
    }

    public Object getValue() {
      return mValue;
    }

    public String toString() {
      return mName;
    }



  }





  private class ChannelListModel {

    private HashSet mSubscribedChannels;
    private Channel[] mAvailableChannels;

    public ChannelListModel() {
      mSubscribedChannels = new HashSet();
      refresh();
    }
    
    public void subscribeChannel(Channel ch) {
      mSubscribedChannels.add(ch);
    }

    public void unsubscribeChannel(Channel ch) {
      mSubscribedChannels.remove(ch);
    }

    public void refresh() {
      ChannelList.create();
      Channel[] channels = ChannelList.getSubscribedChannels();
      mAvailableChannels = ChannelList.getAvailableChannels();
      mSubscribedChannels.clear();
      for (int i=0; i<channels.length; i++) {
        subscribeChannel(channels[i]);
      }
    }

    public boolean isSubscribed(Channel ch) {
      return mSubscribedChannels.contains(ch);
    }

    public Channel[] getAvailableChannels() {
      return mAvailableChannels;
    }

    public Collection getSubscribedChannels() {
      return mSubscribedChannels;
    }

  }

  class BoldLabel extends JLabel {

    public BoldLabel(String text) {
      super(text);
      setFont(getFont().deriveFont(Font.BOLD));
    }

  }

  public void dragGestureRecognized(DragGestureEvent e) {    
    if(e.getComponent().equals(mAllChannels))
      e.startDrag(null,new TransferEntries(mAllChannels.getSelectedIndices(),"mAllChannels","Channel"));
    if(e.getComponent().equals(mSubscribedChannels))    
      e.startDrag(null,new TransferEntries(mSubscribedChannels.getSelectedIndices(),"mSubscribedChannels","Channel"));
  }
  

  public void dragEnter(DropTargetDragEvent e) {

    
  }

  public void dragOver(DropTargetDragEvent e) {
    DataFlavor[] flavors = e.getCurrentDataFlavors();
    if(flavors != null && flavors.length == 2 &&
        flavors[0].getHumanPresentableName().equals("Channel") &&
        flavors[1].getHumanPresentableName().equals("Source")) {
      e.acceptDrag(e.getDropAction());
    }
    else {
      e.rejectDrag();
      return;
    }
    if(((DropTarget)e.getSource()).getComponent().equals(mSubscribedChannels)) {
      Point p = e.getLocation();
      Rectangle rect = mSubscribedChannels.getVisibleRect();
      int i = mSubscribedChannels.locationToIndex(p);
      
      if(i != -1) {      
        Rectangle listRect = mSubscribedChannels.getCellBounds(mSubscribedChannels.locationToIndex(p),
                                                               mSubscribedChannels.locationToIndex(p));   
        Graphics2D g2 = (Graphics2D) mSubscribedChannels.getGraphics(); 
        boolean paint = false;
        
        if(listRect != null) {
          listRect.setSize(listRect.width,listRect.height/2);
          if(!listRect.contains(e.getLocation()) && !mSwitched && i == mOldIndex) {
            mSubscribedChannels.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y + (listRect.height * 2) - 1,listRect.width,2);
            mSwitched = true;
            paint = true;
          }
          else if(listRect.contains(e.getLocation()) && i == mOldIndex && mSwitched) {
            mSubscribedChannels.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y - 1,listRect.width,2);
            mSwitched = false;
            paint = true;
          }
          else if(i != mOldIndex && listRect.contains(e.getLocation())) {
            mSubscribedChannels.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y - 1,listRect.width,2);
            mSwitched = false;
            mOldIndex = i;
            paint = true;
          }
          else if(i != mOldIndex && !listRect.contains(e.getLocation())) {
            mSubscribedChannels.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y + (listRect.height * 2) - 1,listRect.width,2);
            mSwitched = true;
            mOldIndex = i;
            paint = true;
          }
          if(paint) {
            Color c = new Color(255,0,0,120);
            g2.setColor(c);
            g2.fill(mCueLine);
          }
        }        
      }
      else {
        mOldIndex = -1;
        mSubscribedChannels.paintImmediately(mCueLine.getBounds());
      } 
      
      if(p.y + 20 > rect.y + rect.height)
        mSubscribedChannels.scrollRectToVisible(new Rectangle(p.x,p.y + 15,1,1));
      if(p.y - 20 < rect.y)
        mSubscribedChannels.scrollRectToVisible(new Rectangle(p.x,p.y - 15,1,1));
    }
  }

  public void dropActionChanged(DropTargetDragEvent e) {
   
    
  }

  public void dragExit(DropTargetEvent e) {
    if(((DropTarget)e.getSource()).getComponent().equals(mSubscribedChannels)) {
      mOldIndex = -1;
      mSubscribedChannels.paintImmediately(mCueLine.getBounds());
    }
  }

  public void drop(DropTargetDropEvent e) { 
    e.acceptDrop(e.getDropAction());
    Transferable tr = e.getTransferable();
      
    DataFlavor[] flavors = tr.getTransferDataFlavors(); 
    
    // If theTransferable is a TransferEntries drop it
    if(flavors != null && flavors.length == 2 &&
        flavors[0].getHumanPresentableName().equals("Channel") &&
        flavors[1].getHumanPresentableName().equals("Source")) {
      try {
        JList target = (JList)((DropTarget)e.getSource()).getComponent();
        int x = target.locationToIndex(e.getLocation());        
        
        Rectangle rect = target.getCellBounds(x,x);
        if(rect != null) {
          rect.setSize(rect.width,rect.height/2);
        
          if(!rect.contains(e.getLocation()))
            x++;
        }
        else
          x = 0;
        
        if(target.equals(mSubscribedChannels) && tr.getTransferData(flavors[1]).equals("mAllChannels")) {
            moveChannels(x);
        }
        else if(target.equals(mAllChannels) && tr.getTransferData(flavors[1]).equals("mSubscribedChannels")) {
          moveChannelsToLeft();
        }
        else if(target.equals(mSubscribedChannels) && tr.getTransferData(flavors[1]).equals("mSubscribedChannels")) {
            UiUtilities.moveSelectedItems(target,x,true);
        }
      }catch(Exception ee) {ee.printStackTrace();}
    }  
  }
    
  //Move the channels to the row where the dropping was.
  private void moveChannels(int row) {
      Object[] objects = UiUtilities.moveSelectedItems(mAllChannels, mSubscribedChannels, row);
      for (int i=0; i<objects.length; i++) {
        mChannelListModel.subscribeChannel((Channel)objects[i]);
      }
  }
}