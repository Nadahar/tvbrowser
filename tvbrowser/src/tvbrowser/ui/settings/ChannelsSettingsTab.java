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
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import tvbrowser.core.ChannelList;
import tvbrowser.core.DummyChannel;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
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
import tvbrowser.ui.settings.util.LineButton;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
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

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.PluginAccess;
import devplugin.PluginCommunication;
import devplugin.SettingsTab;

/**
 * This Class represents the Channel-Settings-Tab
 *
 * @author Bodo Tasche
 */
public class ChannelsSettingsTab implements SettingsTab, ListDropAction<Object> {

  /**
   * Translation
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelsSettingsTab.class);

  /**
   * The List with all Channels
   */
  private JList<Object> mAllChannels;

  /**
   * The List with the current subscribed Channels
   */
  private JList<Object> mSubscribedChannels;

  /**
   * Model of the list boxes
   */
  private ChannelListModel mChannelListModel;

  /**
   * Drag n Drop Support
   */
  private DragAndDropMouseListener<Object> mSubscribedChannelListener;

  /**
   * Drag n Drop Support
   */
  private ListDragAndDropHandler mDnDHandler;

  /**
   * Comboboxes for filtering
   */
  private JComboBox<FilterItem> mCategoryCB, mCountryCB, mPluginCB;

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
  
  private JButton mButtonAddSeparator;
  private JButton mButtonDeleteSeparator;
  
  private boolean mShowPlugins = (TvDataServiceProxyManager
  .getInstance().getDataServices().length > 1);

  private JButton mImExportChannels;
  private PluginCommunication mSyncCommunication;
  private long mLastImExportChannelsPopupClosed = 0;
  
  private boolean mIsWizard = false;
  
  /**
   * Create the SettingsTab
   */
  public ChannelsSettingsTab() {
    this(false);
  }
  
  public ChannelsSettingsTab(boolean isWizard) {
    mIsWizard = isWizard;
  }

  /**
   * Create the SettingsPanel
   *
   * @return the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    mChannelListModel = new ChannelListModel();
    final JPanel panel = new JPanel(new BorderLayout());

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
    mAllChannels = new ChannelJList(new DefaultListModel<Object>());
    mAllChannels.setCellRenderer(new ChannelListCellRenderer(true, true, true, true, false, true));

    listBoxPnLeft.add(new JScrollPane(mAllChannels), BorderLayout.CENTER);
    
    centerPn.add(listBoxPnLeft);

    mRightButton = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE));

    mRightButton.addActionListener(e -> {
      moveChannelsToRight();
    });

    mLeftButton = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));

    mLeftButton.addActionListener(e -> {
      moveChannelsToLeft();
    });
    
    mButtonAddSeparator = new LineButton();
    mButtonAddSeparator.setToolTipText(mLocalizer.msg("addSeparator", "Add separator"));
    mButtonAddSeparator.addActionListener(e -> {
      int index = mSubscribedChannels.getSelectedIndex()+1;
      Object test = mSubscribedChannels.getSelectedValue();
      
      if(test instanceof Channel && ((Channel) test).getJointChannel() != null) {
        index++;
      }
      
      ((DefaultListModel<Object>)mSubscribedChannels.getModel()).insertElementAt(Channel.SEPARATOR,index);
    });
    
    mButtonAddSeparator.setSize(TVBrowserIcons.SIZE_LARGE, TVBrowserIcons.SIZE_LARGE);
    
    (new DragSource()).createDefaultDragGestureRecognizer(mButtonAddSeparator,
        DnDConstants.ACTION_MOVE, new DragGestureListener() {
          @Override
          public void dragGestureRecognized(DragGestureEvent dge) {            
            dge.startDrag(null, new StringSelection(Channel.SEPARATOR));
            dge.getComponent().dispatchEvent(new MouseEvent(dge.getComponent(), MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 0, 0, 0, false, MouseEvent.NOBUTTON));
          }
        });
    
    mButtonDeleteSeparator = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    mButtonDeleteSeparator.setToolTipText(mLocalizer.msg("deleteSeparator", "Delete selected separator"));
    mButtonDeleteSeparator.addActionListener(e -> {
      int index = mSubscribedChannels.getSelectedIndex();
      
      ((DefaultListModel<Object>)mSubscribedChannels.getModel()).remove(index);
      mButtonDeleteSeparator.setEnabled(false);
      
      if(index < mSubscribedChannels.getModel().getSize()) {
        mSubscribedChannels.setSelectedIndex(index);
      }
    });
    
    mButtonDeleteSeparator.setEnabled(false);
    
    JPanel btnPanel = createButtonPn(mRightButton, mLeftButton, mButtonAddSeparator, mButtonDeleteSeparator);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(0, Sizes
        .dialogUnitXAsPixel(3, btnPanel), 0, Sizes.dialogUnitXAsPixel(3,
        btnPanel)));
    listBoxPnLeft.add(btnPanel, BorderLayout.EAST);

    // right list box
    JPanel listBoxPnRight = new JPanel(new BorderLayout());
    SortableItemList<Object> channelList = new SortableItemList<>(new ChannelJList());

    mSubscribedChannels = channelList.getList();
    mFilter = new ChannelFilter();
    mSubscribedChannels.setCellRenderer(new FilteredChannelListCellRenderer(mFilter));
    
    // Register DnD on the lists.
    mDnDHandler = new ListDragAndDropHandler(mAllChannels, mSubscribedChannels, this, false, true);
    mDnDHandler.setPaintCueLine(false, true);

    // Register the listener for DnD on the lists.
    new DragAndDropMouseListener<Object>(mAllChannels, mSubscribedChannels, this,
        mDnDHandler);
    mSubscribedChannelListener = new DragAndDropMouseListener<Object>(
        mSubscribedChannels, mAllChannels, this, mDnDHandler);
    
    restoreForPopup();
    
    mImExportChannels = new JButton(mLocalizer.msg("imExportChannels", "Export/import channels"));
    mImExportChannels.addActionListener(e -> {
      showImExportSelection();
    });
    
    loadSyncCommunication();
    
    listBoxPnRight.add(mImExportChannels, BorderLayout.NORTH);
    
    listBoxPnRight.add(new JScrollPane(mSubscribedChannels),
        BorderLayout.CENTER);

    final JButton setSortNumbers = new JButton(mLocalizer.msg("setSortNumbers", "Set sort numbers"));
    setSortNumbers.setEnabled(false);
    
    setSortNumbers.addActionListener(e -> {
      setSortNumbers();
    });
    
    final JButton configureChannels = new JButton(mLocalizer.msg(
        "configSelectedChannels", "Configure selected channels"));
    configureChannels.setEnabled(false);

    configureChannels.addActionListener(e -> {
      configChannels();
    });
    
    mSubscribedChannels.addListSelectionListener(e -> {
      if (mSubscribedChannels.getSelectedValuesList().size() > 0 && !(mSubscribedChannels.getSelectedValue() instanceof DummyChannel) && !(mSubscribedChannels.getSelectedValue() instanceof String)) {
        configureChannels.setEnabled(true);
        setSortNumbers.setEnabled(true);
        mButtonDeleteSeparator.setEnabled(false);
      } else {
        configureChannels.setEnabled(false);
        setSortNumbers.setEnabled(false);
        
        if(mSubscribedChannels.getSelectedValue() instanceof String) {
          mButtonDeleteSeparator.setEnabled(true);
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

    refreshList.addActionListener(e -> {
      refreshChannelList();
    });

    southPn.add(refreshList, BorderLayout.WEST);
    
    JPanel buttons = new JPanel(new FormLayout("default,3dlu,default","default"));
    buttons.add(setSortNumbers, CC.xy(1, 1));
    buttons.add(configureChannels, CC.xy(3, 1));
    
    southPn.add(buttons, BorderLayout.EAST);

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

    if(mIsWizard) {
      SwingUtilities.invokeLater(() -> {
        askForSynchronization(mSyncCommunication == null);
      });
    }
    
    result.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
    return result;
  }
  
  private void loadSyncCommunication() {
    PluginAccess androidSync = PluginManagerImpl.getInstance().getActivatedPluginForId("java.androidsync.AndroidSync");
    
    if(androidSync != null) {
      mSyncCommunication = androidSync.getCommunicationClass();
    }
  }
  
  private void askForSynchronization(boolean installPlugin) {try {
    PluginProxy plugin = null;
    
    if(installPlugin) {
      plugin = PluginProxyManager.getInstance().getPluginForId("java.androidsync.AndroidSync");
      
      if(plugin == null) {
        if(JOptionPane.showConfirmDialog(null, mLocalizer.msg("syncInstallPluginMsg","You can synchronize your channels with the AndroidSync plugin, therefor it needs to be installed.\n\nDo you want to install the AndroidSync plugin now and synchronize the channels?"), mLocalizer.msg("syncInstallPluginTitle","Install AndroidSync plugin?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
          File target = new File(Settings.propPluginsDirectory.getString(),"AndroidSync.jar");
          boolean error = false;
          
          try {
            IOUtilities.download(new URL("http://www.tvbrowser.org/scripts/download-plugin.php?plugin=1381591864575_786"), target);
            
            PluginLoader.getInstance().loadPlugin(target, true);
            
            plugin = PluginProxyManager.getInstance().getPluginForId("java.androidsync.AndroidSync");
            PluginProxyManager.getInstance().activatePlugin(plugin);
            
            loadSyncCommunication();
            
            if(!synchronizeChannels()) {
              JOptionPane.showMessageDialog(null, mLocalizer.msg("syncNotPossibleMsg", "Channels could not be synchronized.\n\nPlease select the channels manually"), mLocalizer.msg("syncNotPossibleTitle", "Channels were not synchronized"), JOptionPane.ERROR_MESSAGE);
            }
          } catch (MalformedURLException e) {
            error = true;
            e.printStackTrace();
          } catch (IOException e) {
            error = true;
            e.printStackTrace();
          }
          
          if(error) {
            JOptionPane.showMessageDialog(null, mLocalizer.msg("syncNotInstalledMsg", "Plugin could not be installed.\n\nPlease select the channels manually"), mLocalizer.msg("syncNotInstalledTitle", "AndroidSync could not be installed"), JOptionPane.ERROR_MESSAGE);
          }
        }
      }
      else if(!plugin.isActivated() && JOptionPane.showConfirmDialog(null, mLocalizer.msg("syncActivateMsg", "You can synchronize your channels with the AndroidSync plugin, therefor it needs to be activated.\n\nDo you want to activate the AndroidSync plugin now?"), mLocalizer.msg("syncActivateTitle", "Activate AndroidSync?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
        try {
          PluginProxyManager.getInstance().activatePlugin(plugin);
          
          loadSyncCommunication();
          
          if(!synchronizeChannels()) {
            JOptionPane.showMessageDialog(null, mLocalizer.msg("syncNotPossibleMsg", "Channels could not be synchronized.\n\nPlease select the channels manually"), mLocalizer.msg("syncNotPossibleTitle", "Channels were not synchronized"), JOptionPane.ERROR_MESSAGE);
          }
        } catch (TvBrowserException e) {
          JOptionPane.showMessageDialog(null, mLocalizer.msg("syncNotActivatedMsg", "Plugin could not be activated.\n\nPlease select the channels manually"), mLocalizer.msg("syncNotActivatedTitle", "AndroidSync could not be activated"), JOptionPane.ERROR_MESSAGE);
        }
      }
    }
    }catch(Throwable t)  {t.printStackTrace();}
  }
  
  private void showImExportSelection() {
    if(mLastImExportChannelsPopupClosed + 200 < System.currentTimeMillis()) {
      JPopupMenu imExportChannelsPopup = new JPopupMenu();
      
      imExportChannelsPopup.addPopupMenuListener(new PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          mLastImExportChannelsPopupClosed = System.currentTimeMillis();
        }
        
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}        
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
      });
      
      if(mSyncCommunication != null) {
        JMenuItem item = new JMenuItem(mLocalizer.msg("synchronize", "Synchronize channels with AndroidSync"));
        item.addActionListener(e -> {
          synchronizeChannels();
        });
        
        imExportChannelsPopup.add(item);
        imExportChannelsPopup.addSeparator();
      }
      
      JMenuItem item = new JMenuItem(mLocalizer.msg("exportChannelsBtn", "Export channels to file"));
      item.addActionListener(e -> {
        exportChannelsToFile();
      });
      
      imExportChannelsPopup.add(item);
      
      item = new JMenuItem(mLocalizer.msg("importChannelsBtn", "Import channels from file"));
      item.addActionListener(e -> {
        importChannelsFromFile();
      });
      
      imExportChannelsPopup.add(item);
      
      imExportChannelsPopup.show(mImExportChannels, 0, mImExportChannels.getHeight());
    }
  }
  
  private void importChannelsFromFile() {
    FileNameExtensionFilter filter = new FileNameExtensionFilter(mLocalizer.msg("textFileType", "Text files"), "txt");
    
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogTitle(mLocalizer.msg("exportChannels", "Export channels"));
    chooser.addChoosableFileFilter(filter);
    chooser.setFileFilter(filter);
    chooser.setSelectedFile(new File(IOUtilities.translateRelativePath(Settings.propLastChannelExportFile.getString())));
    chooser.setAcceptAllFileFilterUsed(false);
    
    if(chooser.showOpenDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance())) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      
      if(file != null && file.isFile()) {
        ArrayList<String> channelList = new ArrayList<String>();
        
        BufferedReader in = null;
        
        try {
          in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
          
          String line = null;
          
          while((line = in.readLine()) != null) {
            channelList.add(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }finally {
          if(in != null) {
            try {
              in.close();
            } catch (IOException e) {}
          }
        }
        
        if(!channelList.isEmpty() && importChannels(channelList.toArray(new String[channelList.size()]))) {
          JOptionPane.showMessageDialog(null, mLocalizer.msg("synched", "Channels were successfully synchronized."), mLocalizer.msg("importSuccess", "Channels were imported successfully."), JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
  }
  
  private void exportChannelsToFile() {
    FileNameExtensionFilter filter = new FileNameExtensionFilter(mLocalizer.msg("textFileType", "Text files"), "txt");
    
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogTitle(mLocalizer.msg("exportChannels", "Export channels"));
    chooser.addChoosableFileFilter(filter);
    chooser.setFileFilter(filter);
    chooser.setSelectedFile(new File(IOUtilities.translateRelativePath(Settings.propLastChannelExportFile.getString())));
    chooser.setAcceptAllFileFilterUsed(false);
    
    if(chooser.showSaveDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance())) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      
      if(!file.getName().toLowerCase().endsWith(".txt")) {
        file = new File(file.getAbsolutePath()+".txt");
      }
      
      boolean save = true;
      
      if(file.isFile() && JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("exportOverrideMsg", "The file already exists.\nDo you want to override it?"), mLocalizer.msg("exportOverrideTitle", "Override file?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
        save = false;
      }
      
      if(save) {
        Settings.propLastChannelExportFile.setString(IOUtilities.checkForRelativePath(file.getAbsolutePath()));
        
        BufferedWriter out = null;
        
        try {
          FileOutputStream fOut = new FileOutputStream(file);
          fOut.getChannel().truncate(0);
          
          out = new BufferedWriter(new OutputStreamWriter(fOut,"UTF-8"));
          
          for(int i = 0; i < mSubscribedChannels.getModel().getSize(); i++) {
            final Object value = mSubscribedChannels.getModel().getElementAt(i);
            
            if(value instanceof Channel) {
              final Channel ch = (Channel)value;
              
              out.write(ch.getDataServicePackageName());
              out.write(":");
              
              if(!ch.getDataServicePackageName().equals("epgdonatedata")) {
                out.write(ch.getGroup().getId());
                out.write(":");
              }
              
              out.write(ch.getId());
              
              if(ch.getSortNumber().trim().length() > 0) {
                out.write(":");
                out.write(ch.getSortNumber());
              }
              
              out.write("\n");
            }
          }
          
          out.flush();
        }catch(IOException ioe) {
          ioe.printStackTrace();
        }finally {
          if(out != null) {
            try {
              out.close();
            }catch(Throwable t) {}
          }
        }
      }
    }
  }
  
  private boolean importChannels(String[] channels) {
    boolean result = false;
    
    if(channels != null && channels.length > 0) {
      Channel[] available = ChannelList.getAvailableChannels();
      
      for(String syncChannel : channels) {
        for(Channel ch : available) {
          String[] parts = syncChannel.split(":");
          
          int index = -1;
          
          if(ch.getDataServicePackageName().equals(parts[0])) {
            if(parts[0].equals("epgdonatedata")) {
              if(ch.getId().equals(parts[1])) {
                index = ((DefaultListModel<Object>)mAllChannels.getModel()).indexOf(ch);
                
                if(parts.length > 2) {
                  ch.setSortNumber(parts[2]);
                }
              }
            }
            else {
              if(ch.getGroup().getId().equals(parts[1]) && ch.getId().equals(parts[2])) {
                index = ((DefaultListModel<Object>)mAllChannels.getModel()).indexOf(ch);
                if(parts.length > 3) {
                  ch.setSortNumber(parts[3]);
                }
              }
            }
          }
          
          if(index != -1) {
            mAllChannels.setSelectedIndex(index);
            UiUtilities.moveSelectedItems(mAllChannels, mSubscribedChannels);
            result = true;
            break;
          }
        }
      }
    }
    
    return result;
  }
  
  private boolean synchronizeChannels() {
    boolean methodResult = false;
    try {  
      if(mSyncCommunication != null) {
        try {
          Method getStoredChannels = mSyncCommunication.getClass().getMethod("getStoredChannels", new Class<?>[0]);
          Object result = getStoredChannels.invoke(mSyncCommunication, new Object[0]);
          
          if(result != null) {
            mCountryCB.setSelectedIndex(0);
            mCategoryCB.setSelectedIndex(0);
            mPluginCB.setSelectedItem(0);
            
            fillAvailableChannelsListBox();
            
            if(mSubscribedChannels.getModel().getSize() > 0) {
              mSubscribedChannels.setSelectionInterval(0, mSubscribedChannels.getModel().getSize()-1);
              UiUtilities.moveSelectedItems(mSubscribedChannels, mAllChannels);
            }
            
            methodResult = importChannels((String[])result);
            
            if(methodResult) {
              JOptionPane.showMessageDialog(null, mLocalizer.msg("synched", "Channels were successfully synchronized."), mLocalizer.msg("syncSuccess", "Synchronization success"), JOptionPane.INFORMATION_MESSAGE);
            }
          }
        } catch (SecurityException e1) {
          e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
          e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
          e1.printStackTrace();
        } catch (IllegalAccessException e1) {
          e1.printStackTrace();
        } catch (InvocationTargetException e1) {
          e1.printStackTrace();
        }
      }
    }catch(Throwable t) {t.printStackTrace();}
    
    return methodResult;
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

    mCountryCB = new JComboBox<>();
    filterPanel.add(new JLabel(mLocalizer.msg("country", "Country") + ":"), cc
        .xy(1, 1));
    filterPanel.add(mCountryCB, cc.xyw(3, 1, 2));

    mCategoryCB = new JComboBox<>();
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
    
    mPluginCB = new JComboBox<>();
    mPluginCB.setMaximumRowCount(20);

    if (mShowPlugins) {
      filterPanel.add(new JLabel(mLocalizer.msg("plugin", "Plugin") + ":"),
        cc.xy(6, 3));
      filterPanel.add(mPluginCB, cc.xyw(8, 3, 2));
    }
    
    JButton reset = new JButton(mLocalizer.msg("reset", "Reset"));

    reset.addActionListener(e -> {
      mChannelName.setText("");
      mCategoryCB.setSelectedIndex(1);
      mCountryCB.setSelectedIndex(0);
      mPluginCB.setSelectedIndex(0);
    });

    if (mShowPlugins) {
      filterPanel.add(reset, cc.xy(9, 4));
    } else {
      filterPanel.add(reset, cc.xy(9, 3));
    }

    filter.add(filterPanel, cc.xy(1, 3));

    final ItemListener filterItemListener = e -> {
      if ((e == null) || (e.getStateChange() == ItemEvent.SELECTED)) {
        if (!mListUpdating) {
          SwingUtilities.invokeLater(() -> {
            mListUpdating = true;
            fillAvailableChannelsListBox();
            mListUpdating = false;
          });
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
      String[] allCountries = allChannel.getAllCountries();
      if(allCountries != null) {
        for(String country : allCountries) {
          if (country != null) {
            countries.add(country.toLowerCase());
          }
        }
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
/*
  /**
   * Create the ButtonPanel for up/down Buttons
   *
   * @param btn1
   *          Button 1
   * @param btn2
   *          Button 2
   * @return ButtonPanel
   */
/*  private JPanel createButtonPn(JButton btn1, JButton btn2) {
    JPanel result = new JPanel(new FormLayout("pref",
        "fill:pref:grow, pref, 3dlu, pref, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();

    result.add(btn1, cc.xy(1, 2));
    result.add(btn2, cc.xy(1, 4));

    return result;
  }*/

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
    saveChannels(((DefaultListModel<Object>) mSubscribedChannels.getModel())
        .toArray(),autoUpdate);
    MainFrame.getInstance().updateChannelChooser();
  }
  
  static void saveChannels(Object[] list, boolean autoUpdate) {
 // Convert the list into a Channel[] and fill channels
    ArrayList<String> groups = new ArrayList<String>();
    ArrayList<Channel> channels = new ArrayList<Channel>();
    ArrayList<String> separators = new ArrayList<String>();
    
    String lastChannelId = "";
    
    //Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      if(list[i] instanceof Channel) {
        channels.add((Channel) list[i]);
  
        if (!groups.contains(channels.get(channels.size()-1).getGroup().getId())) {
          groups
              .add(new StringBuilder(channels.get(channels.size()-1).getDataServiceId())
                  .append('.').append(channels.get(channels.size()-1).getGroup().getId())
                  .toString());
        }
        
        if(lastChannelId.endsWith(Channel.SEPARATOR)) {
          separators.add(lastChannelId+";"+channels.get(channels.size()-1).getUniqueId());
          lastChannelId = "";
        }
        
        lastChannelId = channels.get(channels.size()-1).getUniqueId();
      }
      else if(list[i] instanceof String && !lastChannelId.endsWith(Channel.SEPARATOR)) {
        lastChannelId += ";"+Channel.SEPARATOR;
      }
    }
    
    if(lastChannelId.endsWith(Channel.SEPARATOR)) {
      separators.add(lastChannelId);
    }

    Channel[] channelArr = channels.toArray(new Channel[channels.size()]);
    
    ChannelList.setSubscribeChannels(channelArr, autoUpdate);
    ChannelList.storeAllSettings();
    Settings.propSubscribedChannelsSeparators.setStringArray(separators.toArray(new String[separators.size()]));
    Settings.propSubscribedChannels.setChannelArray(channelArr);
    Settings.propUsedChannelGroups.setStringArray(groups
        .toArray(new String[groups.size()]));

    Settings.propChannelsWereConfigured.setBoolean(ChannelList.getNumberOfSubscribedChannels() > 0);
    
    Settings.updateChannelFilters(channelArr);
    
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
    ((DefaultListModel<Object>) mSubscribedChannels.getModel()).clear();

    Channel[] channels = mChannelListModel.getAvailableChannels();
    for (Channel channel : channels) {
      int pos = ChannelList.getPos(channel);

      if (pos != -1 && pos < subscribedChannelArr.length) {
        subscribedChannelArr[pos] = channel;
      }
    }

    String[] separatorArr = Settings.propSubscribedChannelsSeparators.getStringArray();
    
    Channel previousChannel = null;
    int lastSeparatorIndex = 0;
    
    for (Channel aSubscribedChannelArr : subscribedChannelArr) {
      if(aSubscribedChannelArr != null) {
        for(int i = lastSeparatorIndex; i < separatorArr.length; i++) {
          String separator = separatorArr[i];
          
          if(separator.endsWith(aSubscribedChannelArr.getUniqueId()) && 
              previousChannel != null && separator.startsWith(previousChannel.getUniqueId()) ) {
            ((DefaultListModel<Object>) mSubscribedChannels.getModel()).addElement(Channel.SEPARATOR);
            lastSeparatorIndex = i+1;
          }
        }
        
        previousChannel = aSubscribedChannelArr;
        ((DefaultListModel<Object>) mSubscribedChannels.getModel())
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
      if (!((DefaultListModel<Object>) mSubscribedChannels.getModel())
          .contains(channel)
          && mFilter.accept(channel)) {
        availableChannelList.add(channel);
      }
    }

    // Sort the available channels
    Channel[] availableChannelArr = new Channel[availableChannelList.size()];
    availableChannelList.toArray(availableChannelArr);
    Arrays.sort(availableChannelArr, createChannelComparator());

    DefaultListModel<Object> newModel = new DefaultListModel<>();
    // Add the available channels
    for (Channel anAvailableChannelArr : availableChannelArr) {
      newModel.addElement(anAvailableChannelArr);
    }
    mAllChannels.setModel(newModel);

    mRightButton.setEnabled(!newModel.isEmpty());
    mAllChannels.setEnabled(!newModel.isEmpty());
    if (mAllChannels.getModel().getSize() == 0) {
      ((DefaultListModel<Object>) mAllChannels.getModel()).addElement(mLocalizer.msg(
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

        SwingUtilities.invokeLater(() -> {
          mChannelListModel.refresh();
          updateFilterPanel();
          fillSubscribedChannelsListBox();
          fillAvailableChannelsListBox();
          showChannelChanges(before);
        });
      }
    });
  }

  private void showChannelChanges(ArrayList<Channel> channelsBefore) {
    Channel[] channels = mChannelListModel.getAvailableChannels();
    Channel[] channelsAfterArr = new Channel[channels.length];
    System.arraycopy(channels, 0, channelsAfterArr, 0, channelsAfterArr.length);
    List<Channel> channelsAfter = Arrays.asList(channelsAfterArr);
    ChannelListChangesDialog.showChannelChanges(SettingsDialog.getInstance().getDialog(), channelsBefore, channelsAfter, false);
    
    Settings.updateChannelFilters(ChannelList.getSubscribedChannels());
  }
  
  private void setSortNumbers() {
    int smallest = mSubscribedChannels.getSelectedIndex();
    
    if(smallest > 1) {
      Channel ch = (Channel)mSubscribedChannels.getModel().getElementAt(smallest-1);
      
      if(ch.getSortNumber().trim().length() > 0) {
        smallest = Integer.parseInt(ch.getSortNumber());
      }
    }
    
    smallest++;
    
    JSpinner start = new JSpinner(new SpinnerNumberModel(smallest, 1, 10000, 1));
    
    Object[] message = new Object[] {mLocalizer.msg("numberChannels", "Create channel numbers (up), start with:"),start};
    
    if(JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), message, mLocalizer.msg("setSortNumbers", "Set sort numbers"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
      List<Object> list = mSubscribedChannels.getSelectedValuesList();
      
      int value = (Integer)start.getValue();
      
      Channel previous = null;
      
      for(Object channel : list) {
        if(!(channel instanceof DummyChannel) && !(channel instanceof String)) {
          if(previous != null && (previous.getJointChannel() != null && previous.getJointChannel().equals(channel))) {
            value--;
          }
          
          ((Channel)channel).setSortNumber(String.valueOf(value++));
          
          previous = (Channel)channel;
        }
      }
      
      mSubscribedChannels.repaint();
      MainFrame.getInstance().getProgramTableScrollPane().updateChannelPanel();
      MainFrame.getInstance().updateChannelChooser();
    }
  }

  /**
   * Display the Config-Channel
   */
  public void configChannels() {
    List<Object> list = mSubscribedChannels.getSelectedValuesList();

    ArrayList<Channel> channelList = new ArrayList<Channel>();
    
    for (Object o : list) {
      if(!(o instanceof DummyChannel)) {
        channelList.add((Channel) o);
      }
    }

    if (channelList.size() == 1) {
      ChannelConfigDlg dialog;

      Window parent = UiUtilities.getBestDialogParent(mAllChannels);
      dialog = new ChannelConfigDlg(parent, channelList.get(0));
      dialog.centerAndShow();
      ChannelList.checkForJointChannels();
      MainFrame.getInstance().getProgramTableScrollPane()
          .updateChannelLabelForChannel(channelList.get(0));
    } else if (channelList.size() > 1) {
      MultiChannelConfigDlg dialog;

      Window parent = UiUtilities.getBestDialogParent(mAllChannels);
      dialog = new MultiChannelConfigDlg(parent, channelList.toArray(new Channel[channelList.size()]));
      
      Settings.layoutWindow("multiChannelConfigDlg", dialog);
      
      dialog.setVisible(true);
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
        mAllChannels,String.class);
    for (Object object : objects) {
      if(object instanceof Channel) {
        mChannelListModel.unsubscribeChannel((Channel) object);
      }
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

  public void drop(JList<Object> source, JList<Object> target, int n, boolean move) {
    if (source.equals(mAllChannels) && !source.equals(target) && move) {
      moveChannelsToRight();
    } else if (source.equals(mSubscribedChannels) && !source.equals(target)
        && move) {
      moveChannelsToLeft();
    } else if (source.equals(mSubscribedChannels)
        && target.equals(mSubscribedChannels)) {
      if(n > 0) {
        Object test = mSubscribedChannels.getSelectedValue();
        
        if(test instanceof String) {
          Object targetObject = mSubscribedChannels.getModel().getElementAt(n-1);
          Object targetObject2 = n < mSubscribedChannels.getModel().getSize()-1 ? mSubscribedChannels.getModel().getElementAt(n) : null;
          
          if(targetObject instanceof Channel && ((Channel)targetObject).getJointChannel() != null 
              && targetObject2 instanceof Channel && ((Channel)targetObject2).getBaseChannel() != null
              && ((Channel)targetObject2).getBaseChannel().equals(targetObject)) {
            List<Object> values = mSubscribedChannels.getSelectedValuesList();
            
            boolean containsChannel = false;
            
            for(Object value : values) {
              if(value instanceof Channel) {
                containsChannel = true;
                break;
              }
            }
            
            if(!containsChannel) {
              n++;
            }
          }
        }
      }
      
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