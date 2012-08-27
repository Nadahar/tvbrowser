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

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TooManyListenersException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.apache.commons.lang.math.RandomUtils;

import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateListener;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.finder.DateSelector;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.finder.calendar.CalendarPanel;
import tvbrowser.ui.finder.calendar.CalendarTablePanel;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.actions.TVBrowserAction;
import tvbrowser.ui.mainframe.actions.TVBrowserActions;
import tvbrowser.ui.mainframe.searchfield.SearchField;
import tvbrowser.ui.mainframe.searchfield.SearchFilter;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.MoreButton;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import tvbrowser.ui.pluginview.PluginView;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.FilterPanel;
import tvbrowser.ui.programtable.KeyboardAction;
import tvbrowser.ui.programtable.ProgramTable;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.programtable.ProgramTableScrollPaneWrapper;
import tvbrowser.ui.settings.BlockedPlugin;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.update.PluginAutoUpdater;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvdataservice.MarkedProgramsList;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import util.ui.view.Node;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;
import devplugin.Version;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class MainFrame extends JFrame implements DateListener,DropTargetListener,PersonaListener,PluginStateListener {

  private static final Logger mLog = java.util.logging.Logger
      .getLogger(tvbrowser.TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MainFrame.class);

  private Node mTimebuttonsNode, mDateNode, mRootNode, mChannelNode;

  private Node mPluginsNode;

  private JDialog mConfigAssistantDialog;

  private SoftwareUpdateItem[] mSoftwareUpdateItems = null;

  private ProgramTableScrollPane mProgramTableScrollPane;

  private DefaultProgramTableModel mProgramTableModel;

  private Thread downloadingThread;

  private JPanel jcontentPane;

  private DefaultToolBarModel mToolBarModel;

  private ToolBar mToolBar;
  private JPanel mToolBarPanel;
  private SearchField mSearchField;

  private StatusBar mStatusBar;
  
  private JPanel mSouthPanel;

  private DateSelector mFinderPanel;

  private static MainFrame mSingleton;

  private ChannelChooserPanel mChannelChooser;

  private MenuBar mMenuBar;

  private Component mCenterComponent;

  private boolean mIsVisible;

  private static boolean mShuttingDown = false;
  private static boolean mStarting = true;

  private Node mMainframeNode;

  private Node mNavigationNode;

  private Node mDateChannelNode;

  private Date mCurrentDay;

  private PluginView mPluginView;

  private int mXPos, mYPos, mWidth, mHeight;

  /** Panel that Displays current Filter-Name */
  private FilterPanel mFilterPanel;

  private TimeChooserPanel mTimeChooserPanel;

  /** Store the Viewposition if a Filter is selected*/
  private Point mStoredViewPosition;

  private String mCurrentFilterName;

  private int mLastTimerMinutesAfterMidnight;

  private static Date[] mChannelDateArr;
  private static int[] mOnAirRowProgramsArr;

  private boolean mSettingsWillBeOpened;

  private long mLastAutoUpdateRun;
  private long mLastAutoUpdateRunBuffer;

  private int mAutoDownloadTimer;

  private boolean mIsAskingUpdate = false;

  private Timer mTimer;

  private UserAwayDetector mAwayDetector = new UserAwayDetector();
  
  private KeyListener mGlobalFindAsYouTypeKeyListener;

  private FaytPanel mFindAsYouType;
  
  private JTabbedPane mCenterTabPane;
  
  private JPanel mCenterPanel;
  
  private ArrayList<PluginCenterPanelWrapper> mCenterPanelWrapperList = new ArrayList<PluginCenterPanelWrapper>(0);
  
  private ProgramTableScrollPaneWrapper mScrollPaneWrapper;
 
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);
    
    setContentPane(new BackgroundPanel());
    
    Persona.getInstance().registerPersonaListener(this);
    
    mFindAsYouType = new FaytPanel();
    
     mGlobalFindAsYouTypeKeyListener = new KeyAdapter() {      
      @Override
      public void keyPressed(final KeyEvent e) {
        if(Settings.propTypeAsYouFindEnabled.getBoolean() && mProgramTableScrollPane != null && !mProgramTableScrollPane.getProgramTable().isSelected()) {
          if(((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != KeyEvent.ALT_DOWN_MASK) &&
              ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != KeyEvent.CTRL_DOWN_MASK)) {
            if(Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_SPACE) {
              mFindAsYouType.setText(String.valueOf(e.getKeyChar()));
            }
            else if(mFindAsYouType.isVisible()) {
              if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                mFindAsYouType.deleteLastChar();
              }
              else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                mFindAsYouType.closeFayt();
              }
            }
          }
        }
      }
    };
    
    mIsVisible = false;
    mSettingsWillBeOpened = false;

    mAutoDownloadTimer = -1;
    mLastTimerMinutesAfterMidnight = -1;
    mLastAutoUpdateRun = System.currentTimeMillis();

    mChannelDateArr = null;
    mOnAirRowProgramsArr = null;
    mStatusBar = new StatusBar(mGlobalFindAsYouTypeKeyListener);
    mStatusBar.setOpaque(false);
    mStatusBar.getProgressBar().setOpaque(false);

    if (OperatingSystem.isMacOs()) {
      /* create the menu bar for MacOS X */
      try {
        Class<?> impl = Class
            .forName("tvbrowser.ui.mainframe.macosx.MacOSXMenuBar");
        Class<? extends MainFrame> mainFrameClass = this.getClass();
        Class<?> jlabelClass = Class.forName("javax.swing.JLabel");
        Constructor<?> cons = impl.getConstructor(new Class[] { mainFrameClass,
            jlabelClass });
        mMenuBar = (MenuBar) cons.newInstance(new Object[] { this,
            mStatusBar.getLabel() });
      } catch (Exception e) {
        if (TVBrowser.isTransportable()) {
          mLog.info("Using default menu bar (instead of MacOSXMenuBar) for transportable version.");
        }
        mLog.warning("Could not instantiate MacOSXMenuBar\n" + e.toString());
        if (e.getCause() != null) {
          StringWriter sw = new StringWriter();
          e.getCause().printStackTrace(new PrintWriter(sw));
          mLog.warning(sw.toString());
        }
        mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
        mMenuBar.setVisible(Settings.propIsMenubarVisible.getBoolean());
        mLog.info("Using default menu bar");
      }

    } else {
      mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
      mMenuBar.setVisible(Settings.propIsMenubarVisible.getBoolean());
    }
    
    // create content
    jcontentPane = (JPanel) getContentPane();
    jcontentPane.setLayout(new BorderLayout());

    JPanel skinPanel = new JPanel();
    skinPanel.addKeyListener(mGlobalFindAsYouTypeKeyListener);
    skinPanel.setOpaque(false);
    skinPanel.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.addKeyListener(mGlobalFindAsYouTypeKeyListener);
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder());
    
    mCenterPanel = new JPanel(new BorderLayout());
    mCenterPanel.setOpaque(false);
    mCenterPanel.setBorder(BorderFactory.createEmptyBorder());
    
    centerPanel.add(mCenterPanel, BorderLayout.CENTER);

    mFilterPanel = new FilterPanel(mGlobalFindAsYouTypeKeyListener);
    mFilterPanel.setVisible(false);
    
    addKeyListener(mGlobalFindAsYouTypeKeyListener);
    mTimeChooserPanel = new TimeChooserPanel(this,mGlobalFindAsYouTypeKeyListener);

    centerPanel.add(mFilterPanel, BorderLayout.NORTH);
    
    Channel[] channelArr = ChannelList.getSubscribedChannels();
    int startOfDay = Settings.propProgramTableStartOfDay.getInt();
    int endOfDay = Settings.propProgramTableEndOfDay.getInt();
    mProgramTableModel = new DefaultProgramTableModel(channelArr, startOfDay,
        endOfDay);
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel,mGlobalFindAsYouTypeKeyListener);
    
    mScrollPaneWrapper = new ProgramTableScrollPaneWrapper(mProgramTableScrollPane);
    
    mCenterTabPane = new JTabbedPane();
    mCenterTabPane.setUI(new BasicTabbedPaneUI() {
      protected  void  paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        if(Persona.getInstance().getHeaderImage() != null && !Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
          g.setColor(Persona.getInstance().getShadowColor());
          
          g.drawString(title,textRect.x+1, textRect.y+1 + metrics.getAscent());
          g.drawString(title,textRect.x+2, textRect.y+2 + metrics.getAscent());
        }
        
        super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
      }
      
      protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
      }
      
      protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
      
      protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        if(Persona.getInstance().getAccentColor() != null && Persona.getInstance().getHeaderImage() != null) {
          Color c = Persona.testPersonaForegroundAgainst(Persona.getInstance().getAccentColor());
          
          int alpha = c.getAlpha();
          
          g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          
          if(isSelected) {
            g.fillRect(x,y,w,h-2);
          } else {
            g.fillRect(x,y,w,h);
          }
        }
        else {
          super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
        }
      }
    });
    
    mCenterTabPane.setBorder(BorderFactory.createEmptyBorder());
    mCenterTabPane.addMouseListener(new MouseAdapter() {
      
      @Override
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }
      
      @Override
      public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }
      
      private void showContextMenu(MouseEvent e) {
        JMenuItem settings = new JMenuItem(mLocalizer.msg("configTabs", "Configure tabs..."));
        settings.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            showSettingsDialog(SettingsItem.CENTERPANELSETUP);
          }
        });
        
        JPopupMenu popup = new JPopupMenu();
        popup.add(settings);
        
        popup.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
      }
    });
        
    mProgramTableScrollPane.setOpaque(false);

    createDateSelector();

    skinPanel.add(centerPanel, BorderLayout.CENTER);

    mChannelChooser = new ChannelChooserPanel(this,mGlobalFindAsYouTypeKeyListener);
    mChannelChooser.setOpaque(false);

    /* create structure */
    mRootNode = new Node(null);

    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mPluginsNode = new Node(mRootNode);
    }
    else {
      mNavigationNode = new Node(mRootNode);
    }

    mMainframeNode = new Node(mRootNode);
    Node programtableNode = new Node(mMainframeNode);

    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mNavigationNode = new Node(mMainframeNode);
    }
    else {
      mPluginsNode = new Node(mMainframeNode);
    }
    
    mTimebuttonsNode = new Node(mNavigationNode);
    mDateChannelNode = new Node(mNavigationNode);
    mDateNode = new Node(mDateChannelNode);
    mChannelNode = new Node(mDateChannelNode);

    mRootNode.setProperty(Settings.propViewRoot);
    mMainframeNode.setProperty(Settings.propViewMainframe);
    mNavigationNode.setProperty(Settings.propViewNavigation);
    mDateChannelNode.setProperty(Settings.propViewDateChannel);

    /* create views */
    programtableNode.setLeaf(skinPanel);
    this.setShowPluginOverview(Settings.propShowPluginView.getBoolean());
    this.setShowTimeButtons(Settings.propShowTimeButtons.getBoolean());
    this.setShowDatelist(Settings.propShowDatelist.getBoolean());
    this.setShowChannellist(Settings.propShowChannels.getBoolean());

    updateToolbar();
    dateChanged(new devplugin.Date(), null, null, false);

    mCenterComponent = mRootNode.getComponent();
    if (mCenterComponent != null) {
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
    }
    
    mSouthPanel = new JPanel(new BorderLayout());
    mSouthPanel.setOpaque(false);
    mSouthPanel.add(mFindAsYouType,BorderLayout.NORTH);
    mFindAsYouType.setVisible(false);
    
    if (Settings.propIsStatusbarVisible.getBoolean()) {
      mSouthPanel.add(mStatusBar, BorderLayout.SOUTH);
    }
    
    jcontentPane.add(mSouthPanel,BorderLayout.SOUTH);
    
    setJMenuBar(mMenuBar);
    addContextMenuMouseListener(mMenuBar);

    // set program filter
    FilterList filterList = FilterList.getInstance();
    
    ProgramFilter filter = filterList
        .getFilterByName(Settings.propLastUsedFilter.getString());
    
    if (filter == null) {
      filter = FilterManagerImpl.getInstance().getDefaultFilter();
    }
    
    setProgramFilter(filter);
    
    // set channel group filter
    String channelGroupName = Settings.propLastUsedChannelGroup.getString();
    if (channelGroupName != null) {
      FilterComponent component = FilterComponentList.getInstance().getFilterComponentByName(channelGroupName);
      if (component != null && component instanceof ChannelFilterComponent) {
        setChannelGroup((ChannelFilterComponent) component);
      }
    }

    addKeyboardAction();

    mTimer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
    mTimer.start();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    //create the drop target for installation of Plugins with Drag'N'Drop on MainFrame
    DropTarget target = new DropTarget();
    try {
      target.addDropTargetListener(this);
    } catch (TooManyListenersException e1) {
      //ignore
    }

    this.setDropTarget(target);
    
    updateCenterPanels();
  }

  /**
   *
   */
  public void createDateSelector() {
    switch (Settings.propViewDateLayout.getInt()) {
    case 1: mFinderPanel = new CalendarTablePanel(mGlobalFindAsYouTypeKeyListener);break;
    case 2: mFinderPanel = new CalendarPanel(mGlobalFindAsYouTypeKeyListener);break;
    default: mFinderPanel = new FinderPanel(mGlobalFindAsYouTypeKeyListener);
    }
    mFinderPanel.setDateListener(this);
  }

  /**
   * Switch the fullscreen mode of TV-Browser
   */
  public void switchFullscreenMode() {
    dispose();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if(isFullScreenMode()) {
          // switch back from fullscreen
          device.setFullScreenWindow(null);
          setUndecorated(false);
          setBounds(mXPos, mYPos, mWidth, mHeight);

          if(mMenuBar != null) {
            mMenuBar.setFullscreenItemChecked(false);
            mMenuBar.setVisible(true);
          }

          if(mToolBarPanel != null) {
            mToolBarPanel.setVisible(Settings.propIsToolbarVisible.getBoolean());
          }

          if(mStatusBar != null) {
            mStatusBar.setVisible(Settings.propIsStatusbarVisible.getBoolean());
          }

          if(mChannelChooser != null) {
            mChannelChooser.setVisible(Settings.propShowChannels.getBoolean());
          }

          if(mFinderPanel != null) {
            mFinderPanel.getComponent().setVisible(Settings.propShowDatelist.getBoolean());
          }

          setVisible(true);

          setShowPluginOverview(Settings.propShowPluginView.getBoolean(),false);
          setShowTimeButtons(Settings.propShowTimeButtons.getBoolean(), false);
          setShowDatelist(Settings.propShowDatelist.getBoolean(), false);
          setShowChannellist(Settings.propShowChannels.getBoolean(), false);
        }
        else {
          // switch into fullscreen
          mXPos = getX();
          mYPos = getY();
          mWidth = getWidth();
          mHeight = getHeight();

          setShowPluginOverview(false, false);
          setShowTimeButtons(false, false);
          setShowDatelist(false, false);
          setShowChannellist(false, false);

          if(mStatusBar != null) {
            mMenuBar.setFullscreenItemChecked(true);
            mStatusBar.setVisible(false);
          }

          if(mChannelChooser != null) {
            mChannelChooser.setVisible(false);
          }

          if(mMenuBar != null) {
            mMenuBar.setVisible(false);
          }

          if(mToolBarPanel != null) {
            mToolBarPanel.setVisible(false);
          }

          if(mFinderPanel != null) {
            mFinderPanel.getComponent().setVisible(false);
          }

          setUndecorated(true);
          final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

          if(device.isFullScreenSupported() && OperatingSystem.isMacOs()) {
            device.setFullScreenWindow(MainFrame.getInstance());
          }
          else {
            setLocation(0,0);
            setSize(screen);
          }

          setVisible(true);
          mProgramTableScrollPane.requestFocusInWindow();

          new Thread("Fullscreen border detection") {
            public void run() {
              setPriority(Thread.MIN_PRIORITY);

              while(isFullScreenMode()) {
                final Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, MainFrame.this);

                if(isActive()) {

                  // mouse pointer is at top
                  if(p.y <= 10) {
                    if(mToolBarPanel != null && mToolBar.getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
                      if(!mToolBarPanel.isVisible()) {
                        UIThreadRunner.invokeLater(new Runnable() {

                          @Override
                          public void run() {
                            mToolBarPanel.setVisible(Settings.propIsToolbarVisible.getBoolean());
                          }
                        });
                      }
                    }

                    if (p.y <= 0) {
                      UIThreadRunner.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                          mMenuBar.setVisible(true && Settings.propIsMenubarVisible.getBoolean());
                        }
                      });
                    }
                  }
                  else if(p.y > (mMenuBar != null && mMenuBar.isVisible() ? mMenuBar.getHeight() : 0) + (Settings.propIsToolbarVisible.getBoolean() ? mToolBarPanel.getHeight() : 0)) {
                    if(mMenuBar.isVisible()) {
                      UIThreadRunner.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                          mMenuBar.setVisible(!isFullScreenMode()&& Settings.propIsMenubarVisible.getBoolean());
                        }
                      });
                    }

                    if(mToolBarPanel != null && mToolBarPanel.isVisible() && mToolBar.getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
                      UIThreadRunner.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                          mToolBarPanel.setVisible(!isFullScreenMode());
                        }
                      });
                    }
                  }

                  // mouse pointer is at the bottom
                  if(p.y >= screen.height - 1 ) {
                    if(mStatusBar != null && !mStatusBar.isVisible()) {
                      UIThreadRunner.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                          mStatusBar.setVisible(Settings.propIsStatusbarVisible.getBoolean());
                        }
                      });
                    }
                  }
                  else if(mStatusBar != null && mStatusBar.isVisible() && p.y < screen.height - mStatusBar.getHeight()) {
                    UIThreadRunner.invokeLater(new Runnable() {

                      @Override
                      public void run() {
                        mStatusBar.setVisible(!isFullScreenMode());
                      }
                    });
                  }

                  // mouse pointer is on the left side
                  if(p.x <= 5) {
                    if(p.x == 0 && mToolBarPanel != null && mToolBar.getToolbarLocation().compareTo(BorderLayout.WEST) == 0) {
                      if(!mToolBarPanel.isVisible()) {
                        UIThreadRunner.invokeLater(new Runnable() {

                          @Override
                          public void run() {
                            mToolBarPanel.setVisible(Settings.propIsToolbarVisible.getBoolean());
                          }
                        });
                      }
                    }

                    if(Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean())  {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(true, false);
                          }
                        });
                      }
                    }
                    else {
                      checkIfToShowTimeDateChannelList();
                    }
                  }
                  else {
                    int toolBarWidth = (mToolBarPanel != null && mToolBarPanel.isVisible() && mToolBar.getToolbarLocation().compareTo(BorderLayout.WEST) == 0) ? mToolBarPanel.getWidth() : 0;

                    if(p.x > toolBarWidth && toolBarWidth != 0) {
                      UIThreadRunner.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                          mToolBarPanel.setVisible(!isFullScreenMode());
                        }
                      });
                    }

                    if(Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean() && mPluginView != null && mPluginView.isVisible() && p.x > mPluginView.getWidth() + toolBarWidth + 25) {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(!isFullScreenMode(), false);
                          }
                        });
                      }
                    }
                    else if(Settings.propShowChannels.getBoolean() ||
                        Settings.propShowDatelist.getBoolean() ||
                        Settings.propShowTimeButtons.getBoolean()) {
                      SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                          if(mChannelChooser != null && mChannelChooser.isVisible() && p.x > mChannelChooser.getWidth()) {
                            setShowChannellist(!isFullScreenMode(), false);
                          }

                          if(mFinderPanel != null && mFinderPanel.getComponent().isVisible() && p.x > mFinderPanel.getComponent().getWidth()) {
                            setShowDatelist(!isFullScreenMode(), false);
                          }

                          if(mTimeChooserPanel != null && mTimeChooserPanel.isVisible() && p.x > mTimeChooserPanel.getWidth()) {
                            setShowTimeButtons(!isFullScreenMode(), false);
                          }
                        }
                      });
                    }
                  }

                  // mouse pointer is on the right side
                  if(p.x >= screen.width - 1) {
                    if(!Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean())  {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(true, false);
                          }
                        });
                      }
                    }
                    else {
                      checkIfToShowTimeDateChannelList();
                    }
                  }
                  else {
                    if(!Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean() && mPluginView != null && mPluginView.isVisible() && p.x < screen.width - mPluginView.getWidth()) {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(!isFullScreenMode(), false);
                          }
                        });
                      }
                    }
                    else if(Settings.propShowChannels.getBoolean() ||
                        Settings.propShowDatelist.getBoolean() ||
                        Settings.propShowTimeButtons.getBoolean()) {
                      SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                          if(mChannelChooser != null && mChannelChooser.isVisible() && p.x < screen.width - mChannelChooser.getWidth()) {
                            setShowChannellist(!isFullScreenMode(), false);
                          }

                          if(mFinderPanel != null && mFinderPanel.getComponent().isVisible() && p.x < screen.width - mFinderPanel.getComponent().getWidth()) {
                            setShowDatelist(!isFullScreenMode(), false);
                          }

                          if(mTimeChooserPanel != null && mTimeChooserPanel.isVisible() && p.x < screen.width - mTimeChooserPanel.getWidth()) {
                            setShowTimeButtons(!isFullScreenMode(), false);
                          }
                        }
                      });
                    }
                  }
                }
                try {
                  Thread.sleep(200);
                }catch(Exception e) {}
              }
            }
          }.start();
        }
      }
    });
  }

  private void checkIfToShowTimeDateChannelList() {
    if(Settings.propShowTimeButtons.getBoolean() ||
        Settings.propShowDatelist.getBoolean() ||
        Settings.propShowChannels.getBoolean()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if(Settings.propShowTimeButtons.getBoolean() && !mTimeChooserPanel.isVisible()) {
            setShowTimeButtons(true, false);
          }

          if(Settings.propShowDatelist.getBoolean() && !mFinderPanel.getComponent().isVisible()) {
            setShowDatelist(true, false);
          }

          if(Settings.propShowChannels.getBoolean() && !mChannelChooser.isVisible()) {
            setShowChannellist(true, false);
          }
        }
      });
    }
  }

  /**
   * Adds the keyboard actions for going to the program table with the keyboard.
   *
   */
  public void addKeyboardAction() {
    mProgramTableScrollPane.deSelectItem();

    // register the global hot keys, so they also work when the main menu is not visible
    for (final TVBrowserAction action : TVBrowserActions.getActions()) {
      KeyStroke keyStroke = action.getAccelerator();
      if (keyStroke != null) {
        rootPane.registerKeyboardAction(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (action.isEnabled()) {
              action.actionPerformed(null);
            }
          }
        }, keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
      }
    }
    
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    rootPane.registerKeyboardAction(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setShowTimeButtons(!mTimeChooserPanel.isVisible());
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    rootPane.registerKeyboardAction(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setShowDatelist(!mFinderPanel.getComponent().isVisible());
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    rootPane.registerKeyboardAction(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setShowChannellist(!mChannelChooser.isVisible());
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    if(!OperatingSystem.isMacOs() || TVBrowser.isTransportable()) {
      stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
      rootPane.registerKeyboardAction(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setShowMenubar(!mMenuBar.isVisible());
        }
      }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_UP), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_UP), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_RIGHT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_RIGHT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_DOWN), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_DOWN), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_LEFT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_LEFT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_CONTEXTMENU), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_CONTEXTMENU), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
    rootPane
        .registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
            KeyboardAction.KEY_DESELECT), stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_SINGLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_DOUBLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_MIDDLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_MIDDLE_DOUBLE_CLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(TVBrowserActions.goToNextDay, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(TVBrowserActions.goToPreviousDay, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // return from full screen using ESCAPE
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        boolean menuCanceled = false;
        
        for(int i = 0; i < mMenuBar.getMenuCount(); i++) {
          JMenu test = mMenuBar.getMenu(i);
          if(test.isSelected()) {
            test.setSelected(false);
            menuCanceled = true;
          }
          if(test.getPopupMenu().isShowing()) {
            test.getPopupMenu().setVisible(false);
            menuCanceled = true;
          }
          
          if(menuCanceled) {
            return;
          }
        }
        
        
        if (isFullScreenMode()) {
          TVBrowserActions.fullScreen.actionPerformed(null);
        }
        else {
          mProgramTableScrollPane.getProgramTable().stopAutoScroll();
          mAutoDownloadTimer = -1;
          mLastTimerMinutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
          TVBrowser.stopAutomaticDownload();
          if (TVBrowserActions.update.isUpdating()) {
            TVBrowserActions.update.actionPerformed(null);
          }
        }
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToLeftSide();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToRightSide();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mProgramTableScrollPane.scrollPageRight();
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mProgramTableScrollPane.scrollPageLeft();
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    this.setRootPane(rootPane);
  }

  protected void goToRightSide() {
    Channel[] channels = MainFrame.getInstance().getProgramTableModel().getShownChannels();
    if (channels != null && channels.length > 0) {
      mProgramTableScrollPane.scrollToChannel(channels[channels.length-1]);
    }
  }

  protected void goToLeftSide() {
    Channel[] channels = MainFrame.getInstance().getProgramTableModel().getShownChannels();
    if (channels != null && channels.length > 0) {
      mProgramTableScrollPane.scrollToChannel(channels[0]);
    }
  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }

  public void updateToolbar() {
    JPanel contentPane = (JPanel) getContentPane();

    if (mToolBarPanel != null) {
      contentPane.remove(mToolBarPanel);
    }

    mToolBarModel = DefaultToolBarModel.getInstance();
    mToolBar = new ToolBar(mToolBarModel, mStatusBar.getLabel());

    String location = mToolBar.getToolbarLocation();

    if (Settings.propIsToolbarVisible.getBoolean()) {
      if (mToolBarPanel == null) {
        mToolBarPanel = new JPanel(new BorderLayout()) {
          public void updateUI() {
            super.updateUI();
            
            if(Persona.getInstance().getHeaderImage() == null) {
              setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getBackground().darker()));
            }
            else {
              int topBorder = Settings.propIsToolbarAdditonalTopSpace.getBoolean() && Settings.propIsToolbarAdditonalBottomSpace.getBoolean() ? 25 : Settings.propIsToolbarAdditonalTopSpace.getBoolean() ? 50 : 0;
              int bottomBorder = Settings.propIsToolbarAdditonalTopSpace.getBoolean() && Settings.propIsToolbarAdditonalBottomSpace.getBoolean() ? 25 : Settings.propIsToolbarAdditonalBottomSpace.getBoolean() ? 50 : 0;
              
              setBorder(BorderFactory.createEmptyBorder((Settings.propIsMenubarVisible.getBoolean() ? 0 : 3) + topBorder, 0, bottomBorder, 0));
            }
          }
        };
        addContextMenuMouseListener(mToolBarPanel);
        mSearchField = new SearchField();
        
        if(Persona.getInstance().getHeaderImage() != null) {
          mToolBarPanel.setOpaque(false);
          mSearchField.setOpaque(false);
        }
        else {
          mToolBarPanel.setOpaque(true);
          mSearchField.setOpaque(true);      
        }
        
        mToolBarPanel.addKeyListener(mGlobalFindAsYouTypeKeyListener);
      } else {
        mToolBarPanel.removeAll();
      }

      if (location.compareTo(BorderLayout.NORTH) == 0) {
        mToolBarPanel.add(MoreButton.wrapToolBar(mToolBar,this,mStatusBar.getLabel()), BorderLayout.CENTER);
        if(Settings.propIsSearchFieldVisible.getBoolean()) {
          mToolBarPanel.add(mSearchField, BorderLayout.EAST);
        }
      } else {
        mToolBarPanel.add(MoreButton.wrapToolBar(mToolBar,this,mStatusBar.getLabel()), BorderLayout.WEST);
        if(Settings.propIsSearchFieldVisible.getBoolean()) {
          mToolBarPanel.add(mSearchField, BorderLayout.SOUTH);
        }
      }
      
      contentPane.add(mToolBarPanel, location);
    }

    contentPane.invalidate();
    contentPane.updateUI();
  }

  private void addContextMenuMouseListener(final JComponent c) {
    c.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          ContextMenu menu = new ContextMenu(c);
          menu.show(e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          ContextMenu menu = new ContextMenu(c);
          menu.show(e.getX(), e.getY());
        }
      }
    });
  }

  public ProgramTableScrollPane getProgramTableScrollPane() {
    return mProgramTableScrollPane;
  }

  public ToolBar getToolbar() {
    return mToolBar;
  }

  public JPanel getToolBarPanel() {
    return mToolBarPanel;
  }

  public DefaultProgramTableModel getProgramTableModel() {
    return mProgramTableModel;
  }

  public static MainFrame getInstance() {
    if (mSingleton == null) {
      mSingleton = new MainFrame();
    }
    return mSingleton;
  }

  public void updateTimeButtons() {
    mToolBar.updateTimeButtons();
    mTimeChooserPanel.updateButtons();
    mMenuBar.updateTimeItems();
  }

  public boolean isShowAllFilterActivated() {
    return (mProgramTableModel == null) || (mProgramTableModel.getProgramFilter() instanceof ShowAllFilter);
  }

  /**
   * check if the default filter is active
   * @return true, if the default filter is active
   * @since 2.6
   */
  public boolean isDefaultFilterActivated() {
    if (mProgramTableModel == null) {
      return true;
    }
    ProgramFilter filter = mProgramTableModel.getProgramFilter();
    return (Settings.propDefaultFilter.getString().equals(filter.getClass().getName() + "###" + filter.getName()));
  }
  
  public synchronized void setProgramFilter(ProgramFilter filter) {
    boolean isDefaultFilter = filter.equals(FilterManagerImpl.getInstance().getDefaultFilter());

    if (!isDefaultFilter) { // Store Position
      mStoredViewPosition = mProgramTableScrollPane.getViewport().getViewPosition();
    }

    if (mProgramTableModel.getProgramFilter() instanceof SearchFilter && !(filter instanceof SearchFilter)) {
      mSearchField.deactivateSearch();
    }
    else if(mProgramTableModel.getProgramFilter() instanceof FaytFilter && !(filter instanceof FaytFilter)) {
      mFindAsYouType.closeFayt();
    }

    mProgramTableScrollPane.deSelectItem();
    mProgramTableModel.setProgramFilter(filter);
    mMenuBar.updateFiltersMenu();

    mToolBarModel.setFilterButtonSelected(!isDefaultFilter);

    updateFilterPanel();

    mToolBar.update();
    addKeyboardAction();

    if(mPluginView != null) {
      mPluginView.repaint();
    }

    if(mCurrentFilterName == null || !mCurrentFilterName.equals(filter.getName())) {
      if ((mStoredViewPosition != null) && (isDefaultFilter)) {
        // Recreate last Position
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (mStoredViewPosition != null) {
              mProgramTableScrollPane.getViewport().setViewPosition(mStoredViewPosition);
            }
          }
        });
      }
      else { // on switching filters go to now, but only if we are at current date
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (getCurrentSelectedDate().equals(Date.getCurrentDate()) && !isStarting()) {
              scrollToNow();
            }
          }
        });
      }
    }

    mStatusBar.getLabel().setText("");
    mCurrentFilterName = filter.getName();
    
    mProgramTableScrollPane.requestFocusInWindow();
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.filterSelected(filter);
    }
  }

  /**
   * Set the active channel group
   * @param channelFilter
   * @since 2.6
   */
  public void setChannelGroup(ChannelFilterComponent channelFilter) {
    mProgramTableModel.setChannelGroup(channelFilter);
    if (channelFilter != null) {
      Settings.propLastUsedChannelGroup.setString(channelFilter.getName());
    }
    else {
      Settings.propLastUsedChannelGroup.setString(null);
    }
    mChannelChooser.setChannelGroup(channelFilter);
    mMenuBar.updateChannelGroupMenu();
  }

  public void updateFilterPanel() {
    ProgramFilter filter = mProgramTableModel.getProgramFilter();
    boolean filterVisible = !filter.equals(FilterManagerImpl.getInstance().getDefaultFilter()) && mMenuBar.isShowFilterPanelEnabled();
    if (filterVisible) {
      mFilterPanel.setCurrentFilter(filter);
    }
    mFilterPanel.setVisible(filterVisible);
    Settings.propShowFilterBar.setBoolean(mMenuBar.isShowFilterPanelEnabled());
  }

  public ProgramFilter getProgramFilter() {
    if (mProgramTableModel == null) {
      return null;
    }
    return mProgramTableModel.getProgramFilter();
  }

  public ChannelFilterComponent getChannelGroup() {
    if (mProgramTableModel == null) {
      return null;
    }
    return mProgramTableModel.getChannelGroup();
  }

  public void quit() {
    String[] options = {mLocalizer.msg("exitConfirmTitle","Exit TV-Browser"),Localizer.getLocalization(Localizer.I18N_CANCEL)};

    if(DontShowAgainOptionBox.showOptionDialog("MainFrame.askForExitConfirm",this.isActive() ? this : null,
        mLocalizer.msg("exitConirmText","Do you really want to quit TV-Browser?"), options[0], JOptionPane.QUESTION_MESSAGE,
        JOptionPane.YES_NO_OPTION, options, options[0], null) != JOptionPane.YES_OPTION) {
      return;
    }

    TVBrowser.removeTray();
    quit(true);
  }

  public void quit(boolean log) {
    quit(log,false);
  }

  private void quit(boolean log, boolean export) {
    mTimer.stop(); // disable the update timer to avoid new update events
    if (log && downloadingThread != null && downloadingThread.isAlive()) {
      final JDialog info = new JDialog(UiUtilities.getLastModalChildOf(this));
      info.setModal(true);
      info.setUndecorated(true);
      info.toFront();

      JPanel main = new JPanel(new FormLayout("5dlu,pref,5dlu","5dlu,pref,5dlu"));
      main.setBorder(BorderFactory.createLineBorder(Color.black));
      main.add(new JLabel(mLocalizer.msg("downloadinfo","A data update is running. TV-Browser will be closed when the update is done.")), new CellConstraints().xy(2,2));

      info.setContentPane(main);
      info.pack();
      info.setLocationRelativeTo(this);

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if(downloadingThread != null && downloadingThread.isAlive()) {
            try {
              downloadingThread.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          info.setVisible(false);
          info.dispose();
        }
      });

      info.setVisible(true);
    }
    if(log && this.isUndecorated()) {
      switchFullscreenMode();
    }
    if (mShuttingDown) {
      return;
    }
    mShuttingDown = true;

    PluginProxyManager.getInstance().shutdownAllPlugins(log);

    if (log) {
      mLog.info("Storing dataservice settings");
    }
    TvDataServiceProxyManager.getInstance().shutDown();

    FavoritesPlugin.getInstance().store();
    ReminderPlugin.getInstance().store();

    TVBrowser.shutdown(log);

    TvDataBase.getInstance().close(log);

    if(export) {
      Settings.propTVDataDirectory.resetToDefault();
      Settings.copyToSystem();
    }

    if (log) {
      mLog.info("Quitting");
      System.exit(0);
    }
  }

  /**
   * Gets if TV-Browser is currently being shutting down.
   * @return True if TV-Browser is shutting down.
   * @since 2.5.3
   */
  public static boolean isShuttingDown() {
    return mShuttingDown;
  }

  /**
   * Gets whether TV-Browser is currently being started.
   * @return True if TV-Browser is currently being started.
   * @since 2.5.3
   */
  public static boolean isStarting() {
    return mStarting;
  }

  /**
   * Handles done TV-Browser start.
   */
  public void handleTvBrowserStartFinished() {
    mStarting = false;
    mMenuBar.updateChannelGroupMenu();
    PluginProxyManager.getInstance().addPluginStateListener(this);
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(Persona.getInstance().getHeaderImage() != null) {
          updatePersona();
        }
      }
    });
  }

  private void runAutoUpdate() {
    ArrayList<TvDataServiceProxy> dataServices = new ArrayList<TvDataServiceProxy>();
    ArrayList<TvDataServiceProxy> checkedServices = new ArrayList<TvDataServiceProxy>(0);

    Channel[] channels = Settings.propSubscribedChannels.getChannelArray();

    for(Channel channel : channels) {
      if(!checkedServices.contains(channel.getDataServiceProxy())) {
        checkedServices.add(channel.getDataServiceProxy());

        if(channel.getDataServiceProxy().supportsAutoUpdate()) {
          dataServices.add(channel.getDataServiceProxy());
        }
      }
    }

    checkedServices.clear();

    if(!dataServices.isEmpty() && licenseForTvDataServicesWasAccepted(dataServices.toArray(new TvDataServiceProxy[dataServices.size()]))) {
      runUpdateThread(14, dataServices.toArray(new TvDataServiceProxy[dataServices.size()]), true);
    }

    mLastAutoUpdateRun = System.currentTimeMillis();
  }

  /**
   * Resets the arrays of on air programs for reloading all.
   */
  public static void resetOnAirArrays() {
    mChannelDateArr = null;
    mOnAirRowProgramsArr = null;
  }

  private void handleTimerEvent() {
    checkAutomaticGotoNow();
    Date date = Date.getCurrentDate();

    if(mLastTimerMinutesAfterMidnight == -1) {
      resetOnAirArrays();
      mAutoDownloadTimer = RandomUtils.nextInt(24 * 60 - 10);
    }

    // Avoid a repaint 6 times a minute (Once a minute is enough)
    try {
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      boolean onAirChanged = false;
      if (minutesAfterMidnight != mLastTimerMinutesAfterMidnight && (downloadingThread == null || !downloadingThread.isAlive())) {
        mLastTimerMinutesAfterMidnight = minutesAfterMidnight;
        Channel[] ch = ChannelList.getSubscribedChannels();

        if(ch != null) {
          /* If no date array is available we have to find
           * the on air programs */
          if(mChannelDateArr == null) {
            onAirChanged = true;
            fillOnAirArrays(ch);
          }
          else {
            /* We have a date array and can test the programs */
            for(int i = 0; i < mChannelDateArr.length; i++) {
              if(mChannelDateArr[i] != null) {
                ChannelDayProgram chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);

                if(chProg != null && chProg.getProgramCount() > 0 && mOnAirRowProgramsArr[i] != -1) {
                  if (mOnAirRowProgramsArr[i] >= chProg.getProgramCount()) {
                    fillOnAirArrays(ch);
                    mLog.warning("Reset of on-air-arrays");
                  }
                  Program p = chProg.getProgramAt(mOnAirRowProgramsArr[i]);

                  if(p.isOnAir()) {
                    p.validateMarking();
                  } else if(p.isExpired()) {
                    onAirChanged = true;
                    p.validateMarking();

                    int n = mOnAirRowProgramsArr[i]+1;

                    if(n < chProg.getProgramCount()) {
                      mOnAirRowProgramsArr[i] = n;
                      chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                    }
                    else {
                      /* The last day program is expired so we have to
                       * look for the on air program on the next day */
                      mChannelDateArr[i] = mChannelDateArr[i].addDays(1);

                      chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);

                      // The next day has no data
                      if(chProg == null || chProg.getProgramCount() < 1) {
                        mOnAirRowProgramsArr[i] = -1;
                      } else {
                        mOnAirRowProgramsArr[i] = 0;
                        chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                      }
                    }
                  }
                }
                else if(mChannelDateArr[i].compareTo(Date.getCurrentDate()) < 0) {
                  /* If the date array for the channel contains a date
                   * earlier than today we have to use today instead */
                  mChannelDateArr[i] = Date.getCurrentDate();
                  onAirChanged = true;

                  chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);

                  if(chProg != null && chProg.getProgramCount() > 0) {
                    mOnAirRowProgramsArr[i] = 0;
                    chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                  }
                }
              }
            }
          }
        }
        
        for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
          wrapper.timeEvent();
        }
      }

      if (onAirChanged) {
        if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK)) {
          mProgramTableScrollPane.getProgramTable().updateLayout();
          mProgramTableScrollPane.updateUI();
        }

        // update filtered view if the "on air" condition changed for any program
        if(!getProgramFilter().equals(FilterManagerImpl.getInstance().getDefaultFilter())) {
          setProgramFilter(getProgramFilter());
        }
      }
    }catch(Exception e) {}

    if (mPluginView != null) {
      mPluginView.repaint();
    }

    if ((mLastAutoUpdateRun + Settings.propDataServiceAutoUpdateTime.getInt() * 60000L) <= System.currentTimeMillis() && !TvDataUpdater.getInstance().isDownloading()) {
      runAutoUpdate();
    }

    if(Settings.propAutoDataDownloadEnabled.getBoolean() && (mAutoDownloadTimer < IOUtilities.getMinutesAfterMidnight() || !date.equals(mCurrentDay)) && mAutoDownloadTimer != -1 && (downloadingThread == null || !downloadingThread.isAlive())) {
      TVBrowser.handleAutomaticDownload();
      mAutoDownloadTimer = -1;
    }

    if (date.equals(mCurrentDay)) {
      return;
    }

    if(mCurrentDay != null) {
      if(mProgramTableModel.getDate().compareTo(Date.getCurrentDate().addDays(-1)) < 0) {
        scrollToNow();
      }

      Thread deletionThread = new Thread("Deferring data deletion") {
        @Override
        public void run() {
          // wait up to an hour to start data deletion
          // this better distributes the server load which is caused by the (plugins) Internet access during the data update
          try {
            sleep((long) (Math.random() * 3600 * 1000));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          // now delete the data
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              mLog.info("Deleting expired TV listings...");
              TvDataBase.getInstance().deleteExpiredFiles(1, true);
            }
          });
        }
      };
      deletionThread.start();
    }

    mLastTimerMinutesAfterMidnight = -1;
    mCurrentDay = date;

    if (mFinderPanel != null) {
      mFinderPanel.updateContent();
    }
    if (mPluginView != null) {
      mPluginView.update();
    }
  }

  private void checkAutomaticGotoNow() {
    if (mAwayDetector.isAway()) {
      scrollToNow();
    }
  }

  private void fillOnAirArrays(Channel[] ch) {
    mChannelDateArr = new Date[ch.length];
    mOnAirRowProgramsArr = new int[ch.length];

    Arrays.fill(mOnAirRowProgramsArr, -1);

    Date currentDate = Date.getCurrentDate();
    for(int i = 0; i < ch.length; i++) {
      ChannelDayProgram chProg = TvDataBase.getInstance().getDayProgram(currentDate,ch[i]);

      if(chProg == null) {
        mChannelDateArr[i] = null;
      } else {
        int n = chProg.getProgramCount();

        for(int j = 0; j < n; j++) {
          Program p = chProg.getProgramAt(j);
          if(p.isOnAir() || !p.isExpired()) {
            p.validateMarking();
            mOnAirRowProgramsArr[i] = j;
            mChannelDateArr[i] = currentDate;
            break;
          }
        }

        if(mOnAirRowProgramsArr[i] == -1) {
          chProg = TvDataBase.getInstance().getDayProgram(currentDate.addDays(1),ch[i]);

          if(chProg != null && chProg.getProgramCount() > 0 && chProg.getProgramAt(0).isOnAir()) {
            chProg.getProgramAt(0).validateMarking();
            mOnAirRowProgramsArr[i] = 0;
          }

          mChannelDateArr[i] = currentDate.addDays(1);
        }
      }
    }
  }

  public void updatePluginsMenu() {
    mMenuBar.updatePluginsMenu();
  }

  public void scrollToProgram(final Program program) {
    scrollToProgram(program, null);
  }

  public void scrollToProgram(final Program program, final Runnable callback) {
    if (!getProgramFilter().accept(program)) {
      int result = JOptionPane.showOptionDialog(this, mLocalizer.msg("programFiltered", "The program {0} is not visible with the filter {1} being active.\nDo you want to deactivate the filter?", program.getTitle(), getProgramFilter().getName()),mLocalizer.msg("programNotVisible","Program not visible"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
      if (result == JOptionPane.YES_OPTION) {
        mStoredViewPosition = null;
        setProgramFilter(FilterManagerImpl.getInstance().getAllFilter());
      }
    }
    mProgramTableScrollPane.resetScrolledTime();
    // invoke scrolling later as the upper filter deactivation may have pending operations for the UI
    // so we currently can't scroll there yet
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramTableScrollPane.scrollToChannel(program.getChannel());
        scrollTo(program.getDate(), program.getStartTime(), callback);
      }});
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.programScrolled(program);
    }
  }
  
  public void selectProgram(final Program program) {
    if(program != null) {
      scrollToProgram(program, new Runnable() {
        public void run() {
          ProgramTable table = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
          table.deSelectItem();
          table.selectProgram(program);
        }});
    }
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.programSelected(program);
    }
  }

  public void scrollToTime(int time) {
    mProgramTableScrollPane.deSelectItem();
    mProgramTableScrollPane.scrollToTime(time);
    mProgramTableScrollPane.requestFocusInWindow();
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.scrolledToTime(time);
    }
  }

  public void scrollToNow() {
    mProgramTableScrollPane.resetScrolledTime();
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    devplugin.Date day = new devplugin.Date();
    scrollTo(day, hour * 60 + cal.get(Calendar.MINUTE));
    mProgramTableScrollPane.requestFocusInWindow();
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.scrolledToNow();
    }
  }
  
  /** Very first scrollToNow should only be called from TVBrowser.java */
  public void scrollToNowFirst() {
    handleTimerEvent();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        scrollToNow();    
      }
    });
    
  }

  private void scrollTo(Date day, int minute) {
    scrollTo(day, minute, null);
  }

  private void scrollTo(Date day, int minute, final Runnable callback) {
    mProgramTableScrollPane.deSelectItem();
    // Choose the day.
    // NOTE: If its early in the morning before the set "day start" we should
    // stay at the last day - otherwise the user won't see the current
    // program. But until when should we stay at the old day?
    // Example: day start: 0:00, day end: 6:00
    // Directly after the day start is not a good choice, because the new
    // day program table will not contain programs that started before 0:00.
    // Directly after the day end is also not a good choice, because a
    // minute before the old day program will not contain the coming programs.
    // So I think the best choice will be the middle, in this case 3:00.
    // If the day start is later as the day end, then the table have to show the
    // old date until the day end is reached.

    int dayStart = Settings.propProgramTableStartOfDay.getInt();
    int dayEnd = Settings.propProgramTableEndOfDay.getInt();
    if ((dayStart >= dayEnd && minute < dayEnd) // no overlapping -> stay on last day until day end
        || (dayStart < dayEnd && minute <= (dayEnd + dayStart) /2)) { // overlapping -> stay until the middle between both times
      day = day.addDays(-1);
      minute += 24 * 60;
    }
    // Change to the shown day program to today if necessary
    // and scroll to "now" afterwards
    final int scrollMinute = minute;
    mFinderPanel.markDate(day, new Runnable() {
      public void run() {
        // Scroll to now
        mProgramTableScrollPane.scrollToTime(scrollMinute);
        if (callback != null) {
          callback.run();
        }
      }
    },false);
  }

  public void runSetupAssistant() {

    ProgressWindow progWin = new ProgressWindow(this, mLocalizer.msg(
        "loadingAssistant", ""));
    final JFrame parent = this;
    progWin.run(new Progress() {
      public void run() {
        try {
          UIThreadRunner.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              mConfigAssistantDialog = new tvbrowser.ui.configassistant.ConfigAssistant(
                  parent);
            }
          });
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

    util.ui.UiUtilities.centerAndShow(mConfigAssistantDialog);
    mConfigAssistantDialog.setVisible(false);
    mConfigAssistantDialog.dispose();
    mConfigAssistantDialog = null;

    Settings.handleChangedSettings();

    boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());

    if (!dataAvailable) {
      askForDataUpdateNoDataAvailable();
    }
  }

  public void copySettingsToSystem() {
    if(TVBrowser.isTransportable()) {
      String[] options = {mLocalizer.msg("copy","Copy"),
                          mLocalizer.msg("dontCopy","Don't copy")};
      String title = mLocalizer.msg("copyToSystemTitle","Copy settings and data to system");
      String msg = mLocalizer.msg("copyToSystemMsg","Should the settings and TV data be copied to the system?\nTV-Browser will therefor will be quit automatically.");

      if(JOptionPane.showOptionDialog(this,msg,title,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[1]) == JOptionPane.YES_OPTION) {
        quit(true,true);
      }
    }
  }

  // public void runTvBrowserUpdateAssistant() {
  // TvBrowserUpdateAssistant dlg = new TvBrowserUpdateAssistant(this);
  // UiUtilities.centerAndShow(dlg);
  // }

  public void storeSettings() {
    mToolBarModel.store();
    mToolBar.storeSettings();
    mRootNode.storeProperties();

    ProgramFilter filter = getProgramFilter();
    if (filter != null) {
      if (!(filter instanceof SearchFilter)) {
        Settings.propLastUsedFilter.setString(mCurrentFilterName);
      } else {
        Settings.propLastUsedFilter.setString(FilterManagerImpl.getInstance().getDefaultFilter().getName());
      }
    } else {
      Settings.propLastUsedFilter.setString(FilterManagerImpl.getInstance().getDefaultFilter().getName());
    }

    ChannelFilterComponent channelGroup = getChannelGroup();
    if (channelGroup != null) {
      Settings.propLastUsedChannelGroup.setString(channelGroup.getName());
    }
    else {
      Settings.propLastUsedChannelGroup.setString(null);
    }
  }

  protected void showPluginInfoDlg() {
    Window parent = UiUtilities.getLastModalChildOf(this);
    PluginInformationDialog dlg = new PluginInformationDialog(parent);

    Settings.layoutWindow("main.pluginInfoDlg",dlg, new Dimension(Sizes.dialogUnitXAsPixel(420,dlg),Sizes.dialogUnitYAsPixel(215,dlg)));

    dlg.setVisible(true);
  }

  private void onDownloadStart() {
    mAutoDownloadTimer = -1;
    TVBrowserActions.update.setUpdating(true);

    if(!Settings.propPluginInfoDialogWasShown.getBoolean()) {
      Date compareDate = Settings.propFirstStartDate.getDate().addDays((int)(Math.random() * 4 + 3));

      if(compareDate.compareTo(Date.getCurrentDate()) <= 0) {
        showPluginInfoDlg();
        Settings.propPluginInfoDialogWasShown.setBoolean(true);
      }
    }

    mLastAutoUpdateRunBuffer = mLastAutoUpdateRun;
    mLastAutoUpdateRun = System.currentTimeMillis() + 3600000;
    mToolBar.updateUpdateButton(true);
    mMenuBar.showStopMenuItem();
    Settings.propLastDownloadDate.setDate(Date.getCurrentDate());
  }

  private void onDownloadDone() {
    TVBrowserActions.update.setUpdating(false);
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);
    mStatusBar.getProgressBar().setVisible(false);

    mToolBar.updateUpdateButton(false);
    mMenuBar.showUpdateMenuItem();

    mLastAutoUpdateRun = mLastAutoUpdateRunBuffer;

    mFinderPanel.updateItems();
    resetOnAirArrays();
    mAutoDownloadTimer = -1;

    DontShowAgainOptionBox
        .showOptionDialog(
            "downloadDone",
            MainFrame.getInstance(),
            mLocalizer
                .msg(
                    "downloaddone.message",
                    "The download is done."),
            mLocalizer.msg("downloaddone.title", "Done"));

  }

  /**
   * Updates the entries of the finder panal.
   * @since 2.2.2/2.5.1
   */
  public void handleChangedTvDataDir() {
    mFinderPanel.updateItems();
    changeDate(Date.getCurrentDate(), null, new Runnable() {
      public void run() {
        scrollToNow();
        resetOnAirArrays();
      }
    },false);
  }

  public void showChannel(Channel ch) {
    mProgramTableScrollPane.scrollToChannel(ch);
    
    for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
      wrapper.scrolledToChannel(ch);
    }
  }

  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV listings was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable(boolean scroll) {
    if(scroll) {
      changeDate(mFinderPanel.getSelectedDate(), null, new Runnable() {
        public void run() {
          scrollToNow();
        }
      },false);
    } else {
      changeDate(mFinderPanel.getSelectedDate(), null, null, true);
    }

    mMenuBar.updateDateItems();
  }

  public void goTo(Date date) {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markDate(date,true);
  }

  public void goToNextDay() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markNextDate();
  }

  public void goToPreviousDay() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markPreviousDate();
  }

  /**
   * show same week day 7 days later
   * @since 2.7
   */
  public void goToNextWeek() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markNextWeek();
  }

  /**
   * show same week day 7 days earlier
   * @since 2.7
   */
  public void goToPreviousWeek() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markPreviousWeek();
  }

  /**
   * @since 2.7
   */
  public void goToToday() {
    goTo(devplugin.Date.getCurrentDate());
  }

  public Date getCurrentSelectedDate() {
    return mFinderPanel.getSelectedDate();
  }

  private void changeDate(final Date date, final ProgressMonitor monitor,
      final Runnable callback, boolean informPluginPanels) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final int currentTime = mProgramTableScrollPane.getScrolledTime();
        mProgramTableScrollPane.deSelectItem();
        mProgramTableModel.setDate(date, monitor, new Runnable() {

          @Override
          public void run() {
            if (callback != null) {
              callback.run();
            }
            if (currentTime >= 0) {
              mProgramTableScrollPane.scrollToTime(currentTime);
            }
          }
        });
      }
    });
    
    if(informPluginPanels) {
      for(PluginCenterPanelWrapper wrapper : mCenterPanelWrapperList) {
        wrapper.scrolledToDate(date);
      }
    }
  }

  /** 
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date,
      devplugin.ProgressMonitor monitor, Runnable callback, boolean informPluginPanels) {
    changeDate(date, monitor, callback, informPluginPanels);
    super.setTitle(TVBrowser.MAINWINDOW_TITLE + " - "
        + date.getLongDateString());
    if (mToolBar != null) {
    	mToolBar.dateChanged(date, monitor, callback);
    }
  }

  public void runUpdateThread(final int daysToDownload,
      final TvDataServiceProxy[] services, final boolean autoUpdate) {
    downloadingThread = new Thread("TV data update") {
      public void run() {
        onDownloadStart();

        final boolean scroll = !autoUpdate && !TvDataBase.getInstance().dataAvailable(Date.getCurrentDate())
        && getProgramTableModel().getDate() != null
        && getProgramTableModel().getDate().compareTo(Date.getCurrentDate()) == 0;

        JProgressBar progressBar = mStatusBar.getProgressBar();
        progressBar.setVisible(true);
        try {
          TvDataUpdater.getInstance().downloadTvData(daysToDownload, services,
              progressBar, mStatusBar.getLabel());
        } catch (Throwable t) {
          String msg = mLocalizer.msg("error.3", "An unexpected error occurred during update.");
          ErrorHandler.handle(msg, t);
        } finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // revalidate markings again
              MarkedProgramsList.getInstance().revalidatePrograms();
              
              onDownloadDone();
              newTvDataAvailable(scroll);

              if((Settings.propLastPluginsUpdate.getDate() == null || Settings.propLastPluginsUpdate.getDate().addDays(7).compareTo(Date.getCurrentDate()) <= 0)) {
                PluginAutoUpdater.searchForPluginUpdates(mStatusBar.getLabel());
              }
            }
          });
        }

      }
    };
    downloadingThread.setPriority(Thread.MIN_PRIORITY);
    downloadingThread.start();
  }

  public void updateChannellist() {
    updateChannelChooser();
    mMenuBar.updateChannelItems();
  }

  public void updateChannelChooser() {
    mChannelChooser.updateChannelChooser();
  }

  /**
   * Starts the TV listings update with the given reason shown in the dialog
   *
   * @param numberOfDays
   * @param reason The reason for initiating the download
   */
  synchronized public void updateTvData(final int numberOfDays, final String reason) {
    if (mIsAskingUpdate) {
      return;
    }
    if (TvDataUpdater.getInstance().isDownloading()) {
      return;
    }
    if (downloadingThread != null && downloadingThread.isAlive()) {
      return;
    }
    mIsAskingUpdate = true;
    try {
      if (ChannelList.getNumberOfSubscribedChannels() == 0) {
        int result = JOptionPane.showOptionDialog(this,
            mLocalizer.msg("subscribeBeforeUpdate.msg", "You have not defined any channels.\n\nDo you want to subscribe to some channels before starting the data update?"),
            mLocalizer.msg("subscribeBeforeUpdate.title", "No subscribed channels"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
            null);
        if (result == JOptionPane.YES_OPTION) {
          showSettingsDialog(SettingsItem.CHANNELS);
        }
      }
      else {
        if (TvDataUpdater.getInstance().isDownloading()) {
          TvDataUpdater.getInstance().stopDownload();
        } else {
          UpdateDlg dlg = new UpdateDlg(this, true, reason);
          if (numberOfDays > 0) {
            dlg.setNumberOfDays(numberOfDays);
          }
          dlg.pack();
          UiUtilities.centerAndShow(dlg);

          int daysToDownload = dlg.getResult();
          if(daysToDownload != UpdateDlg.CANCEL && licenseForTvDataServicesWasAccepted(dlg.getSelectedTvDataServices())) {
            runUpdateThread(daysToDownload, dlg.getSelectedTvDataServices(),false);
          }
        }
      }
    } finally {
      mIsAskingUpdate = false;
    }
  }

  /**
   * Starts the TV listings update without a special reason shown in the dialog
   */
  public void updateTvData() {
    updateTvData(0, null);
  }

  /**
   * Checks if all users services license were accepted.
   *
   * @param updateServices
   *          The service to check for license.
   *
   * @return If all used service licenses were accepted.
   */
  public boolean licenseForTvDataServicesWasAccepted(TvDataServiceProxy[] updateServices) {
    boolean accept = true;
    String[] acceptedFor = Settings.propAcceptedLicenseArrForServiceIds.getStringArray();

    for (TvDataServiceProxy serviceProxy : updateServices) {
      boolean found = false;

      for (String acceptedService : acceptedFor) {
        if(serviceProxy.getId().compareTo(acceptedService) == 0) {
          found = true;
          break;
        }
      }

      if(!found && serviceProxy.getInfo().getLicense() != null) {
        LicenseBox box=new LicenseBox(this, serviceProxy.getInfo().getLicense(), true);
        util.ui.UiUtilities.centerAndShow(box);
        accept = accept && box.agreed();

        if(box.agreed()) {
          String[] oldIds = Settings.propAcceptedLicenseArrForServiceIds.getStringArray();
          String[] newIds = new String[oldIds.length + 1];

          System.arraycopy(acceptedFor,0,newIds,0,oldIds.length);
          newIds[newIds.length-1] = serviceProxy.getId();

          Settings.propAcceptedLicenseArrForServiceIds.setStringArray(newIds);
        }
      }
    }

    return accept;
  }

  /**
   * Shows the settings dialog.
   */
  public void showSettingsDialog() {
    showSettingsDialog(Settings.propLastUsedSettingsPath.getString());
  }

  /**
   * Show Settings Dialog for a specific TabId
   *
   * @param visibleTabId
   *          Id of the specific Tab
   */
  public void showSettingsDialog(final String visibleTabId) {
    if(mSettingsWillBeOpened) {
      return;
    }

    try {
      UIThreadRunner.invokeAndWait(new Runnable() {
        public void run() {
          mSettingsWillBeOpened = true;

          // show busy cursor
          Window comp = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
          ProgramTable programTable = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
          Cursor oldWindowCursor = comp.getCursor();
          Cursor oldTableCursor = programTable.getCursor();
          comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          programTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          SettingsDialog dlg = new SettingsDialog(MainFrame.this, visibleTabId);
          dlg.centerAndShow();

          // restore cursors
          programTable.setCursor(oldTableCursor);
          comp.setCursor(oldWindowCursor);

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              Settings.handleChangedSettings();
              if (mPluginView != null) {
                mPluginView.refreshTree();
              }
            }
          });
          mSettingsWillBeOpened = false;
        }
      });
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*****************************************************************************
   * Show the Settings for a specific Plugin
   *
   * @param plugin
   *          Plugin to show
   */
  public void showSettingsDialog(Plugin plugin) {
    showSettingsDialog(plugin.getId());
  }

  /**
   * Shows the about box
   */
  public void showAboutBox() {
    AboutBox box = new AboutBox(this);
    box.setSize(500, 580);
    UiUtilities.centerAndShow(box);
    box.dispose();
  }

  public void showUpdatePluginsDlg(boolean noQuestion) {
    int answer = JOptionPane.YES_OPTION;

    if(!noQuestion) {
      Object[] options = { mLocalizer.msg("checknow", "Check now"),
          Localizer.getLocalization(Localizer.I18N_CANCEL) };
      String msg = mLocalizer.msg("question.1",
          "do you want to check for new plugins");
      answer = JOptionPane.showOptionDialog(this, msg, mLocalizer.msg(
          "title.1", "update plugins"), JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    if (answer == JOptionPane.YES_OPTION) {
      updatePlugins(PluginAutoUpdater.DEFAULT_PLUGINS_DOWNLOAD_URL, SoftwareUpdater.ALL_TYPE, mStatusBar.getLabel(),false);
    }
  }

  /**
   * Search for updates of plugins.
   *
   * @param baseUrl The url string to load the plugin updates from.
   * @param dialogType Type of the software update dialog.
   * @param infoLabel The label to use to show infos.
   * @param dontShowUpdateDlg If the dialog should not be shown even if updates
   *                          are available. (User has disabled automatically plugin updates.)
   */
  public void updatePlugins(final String baseUrl, final int dialogType, final JLabel infoLabel, final boolean dontShowUpdateDlg) {
    new Thread("Plugin Update Thread") {
      public void run() {
        try {
          infoLabel.setText(mLocalizer.msg("searchForPluginUpdates","Search for plugin updates..."));
          java.net.URL url = new java.net.URL(baseUrl + "/" + PluginAutoUpdater.PLUGIN_UPDATES_FILENAME);
          SoftwareUpdater softwareUpdater = new SoftwareUpdater(url,dialogType,false);
          mSoftwareUpdateItems = softwareUpdater
              .getAvailableSoftwareUpdateItems();
          infoLabel.setText("");
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        if(!dontShowUpdateDlg) {
          if (mSoftwareUpdateItems == null && dialogType != SoftwareUpdater.ONLY_UPDATE_TYPE) {
            JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("error.1",
                "software check failed."));
          } else if (mSoftwareUpdateItems != null && mSoftwareUpdateItems.length == 0 && dialogType != SoftwareUpdater.ONLY_UPDATE_TYPE) {
            JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("error.2",
                "No new items available"));
          } else if(mSoftwareUpdateItems != null && mSoftwareUpdateItems.length > 0) {
            final Window parent = UiUtilities.getLastModalChildOf(MainFrame
                .getInstance());
            try {
              UIThreadRunner.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                  SoftwareUpdateDlg dlg = new SoftwareUpdateDlg(parent, baseUrl,
                      dialogType, mSoftwareUpdateItems);
                  //dlg.setSoftwareUpdateItems(mSoftwareUpdateItems);
                  dlg.setLocationRelativeTo(parent);
                  dlg.setVisible(true);
                }
              });
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }

          }
        }

        BlockedPlugin[] newlyBlocked = Settings.propBlockedPluginArray.getNewBlockedPlugins();

        if(newlyBlocked != null && newlyBlocked.length > 0) {
          StringBuilder message = new StringBuilder();

          for(BlockedPlugin blockedPlugin : newlyBlocked) {
            PluginProxy plugin = PluginProxyManager.getInstance().getPluginForId(blockedPlugin.getPluginId());

            if(plugin == null) {
              TvDataServiceProxy dataService = TvDataServiceProxyManager.getInstance().findDataServiceById(blockedPlugin.getPluginId());

              if(dataService != null && blockedPlugin.isBlockedVersion(dataService.getId(),dataService.getInfo().getVersion())) {
                message.append("\n").append(dataService.getInfo().getName()).append(" (").append(blockedPlugin.getBlockStart()).append(" - ").append(blockedPlugin.getBlockEnd()).append(")");
              }
            }
            else if(blockedPlugin.isBlockedVersion(plugin)){
              message.append("\n").append(plugin.getInfo().getName()).append(" (").append(blockedPlugin.getBlockStart()).append(" - ").append(blockedPlugin.getBlockEnd()).append(")");
            }
          }

          if(message.length() > 0) {
            message.insert(0,mLocalizer.msg("update.blockedInfo","The following Plugins were blocked and cannot be used in their current version:\n"));

            showInfoTextMessage(mLocalizer.msg("update.blockedPlugins","Plugins blocked!"),message.toString(),450);
          }
        }

        Settings.propLastPluginsUpdate.setDate(Date.getCurrentDate());

        infoLabel.setText("");
        mSoftwareUpdateItems = null;
      }
    }.start();
  }

  public void showFromTray(int state) {
    super.setVisible(true);
    toFront();
    setExtendedState(state);
    mIsVisible = true;
  }

  public void setVisible(boolean visible) {
    super.setVisible(visible);
    mIsVisible = visible;
  }

  public void repaint() {
    super.repaint();
    mRootNode.update();
  }

  public void askForDataUpdate(final String message, final int numberOfDays) {
    updateTvData(numberOfDays, message);
  }

  private void askForDataUpdate(final String reason) {
    askForDataUpdate(reason, 0);
  }

  public void askForDataUpdateNoDataAvailable() {
    if(mProgramTableModel.getAvailableChannelCount() > 0) {
      askForDataUpdate(mLocalizer.msg("askforupdatedlg.noData",
        "No TV data for todays program available."));
    }
  }

  public void askForDataUpdateChannelsAdded() {
	  askForDataUpdate(mLocalizer.msg("askforupdatedlg.addedChannels",
      "You have added channels."));
  }

  public void showFilterDialog() {
    SelectFilterDlg dlg = SelectFilterDlg.create(this);
    util.ui.UiUtilities.centerAndShow(dlg);
    mMenuBar.updateFiltersMenu();
  }

  public void updateFilterMenu() {
    mMenuBar.updateFiltersMenu();
  }

  public void showHelpDialog() {

    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();

    java.io.File indexFile = new java.io.File("help/" + language
        + "/index.html");
    if (!indexFile.exists()) {
      indexFile = new java.io.File("help/default/index.html");
    }
    Launch.openURL(indexFile.getAbsolutePath());
  }

  /**
   * Updates the TimeChooser-Buttons
   */
  public void updateButtons() {
    mMenuBar.updateTimeItems();
    if (mTimebuttonsNode.getLeaf() != null) {
      ((TimeChooserPanel) mTimebuttonsNode.getLeaf()).updateButtons();
    }
  }
  
  public void setIsToolbarAdditonalTopSpace(boolean value) {
    Settings.propIsToolbarAdditonalTopSpace.setBoolean(value);
    
    if(mToolBarPanel != null) {
      mToolBarPanel.updateUI();
    }
  }

  public void setIsToolbarAdditonalBottomSpace(boolean value) {
    Settings.propIsToolbarAdditonalBottomSpace.setBoolean(value);
    
    if(mToolBarPanel != null) {
      mToolBarPanel.updateUI();
    }
  }
  
  public void setShowMenubar(boolean visible) {
    if(!visible) {
      DontShowAgainOptionBox.showOptionDialog("mainFrame.menuBarDisabled",this,mLocalizer.msg("menuBarDisabled","You have disabled the menu bar.\nTo show it again press F7 on your keyboard."));
    }
    
    Settings.propIsMenubarVisible.setBoolean(visible);
    mMenuBar.setVisible(visible);
    mMenuBar.updateViewToolbarItem();
    
    if(mToolBarPanel != null) {
      mToolBarPanel.updateUI();
    }
  }
  
  public void setShowToolbar(boolean visible) {
    Settings.propIsToolbarVisible.setBoolean(visible);
    mMenuBar.updateViewToolbarItem();
    updateToolbar();
  }

  public void setShowSearchField(boolean visible) {
    Settings.propIsSearchFieldVisible.setBoolean(visible);
    updateToolbar();
  }

  private void updateViews() {
    if (mIsVisible) {
      jcontentPane = (JPanel) getContentPane();
      jcontentPane.remove(mCenterComponent);
      mCenterComponent = mRootNode.getComponent();
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
      jcontentPane.validate();
      jcontentPane.requestFocus();

      mRootNode.update();
    }
  }

  public void setShowTimeButtons(boolean visible) {
    setShowTimeButtons(visible, true);
  }

  public void setShowTimeButtons(boolean visible, boolean save) {
    if (visible) {
      mTimebuttonsNode.setLeaf(mTimeChooserPanel);
    } else {
      mTimebuttonsNode.setLeaf(null);
    }

    mTimeChooserPanel.setVisible(visible);
    mMenuBar.setTimeCooserItemChecked(visible);
    
    if(save) {
      Settings.propShowTimeButtons.setBoolean(visible);
    }

    updateViews();
  }

  public void setShowDatelist(boolean visible) {
    setShowDatelist(visible, true);
  }

  public void setShowDatelist(boolean visible, boolean save) {
    if (visible) {
      mDateNode.setLeaf(new DateChooserPanel(this, mFinderPanel.getComponent()));
    } else {
      mDateNode.setLeaf(null);
    }

    mFinderPanel.getComponent().setVisible(visible);
    mMenuBar.setDateListItemChecked(visible);

    if(save) {
      Settings.propShowDatelist.setBoolean(visible);
    }

    updateViews();
  }

  public void setShowChannellist(boolean visible) {
    setShowChannellist(visible, true);
  }

  public void setShowChannellist(boolean visible, boolean save) {
    if (visible) {
      mChannelNode.setLeaf(mChannelChooser);
    } else {
      mChannelNode.setLeaf(null);
    }

    mChannelChooser.setVisible(visible);
    mMenuBar.setChannelListItemChecked(visible);

    if(save) {
      Settings.propShowChannels.setBoolean(visible);
    }

    updateViews();
  }

  public void setPluginViewButton(boolean selected) {
    if (mToolBarModel != null) {
      mToolBar.update();
    }
  }

  public void setShowPluginOverview(boolean visible) {
    setShowPluginOverview(visible, true);
  }

  /**
   * Gets if the plugin overview is shown.
   * <p>
   * @return <code>true</code> if the plugin overview is shown, <code>false</code> otherwise.
   * @since 2.2.2
   */
  public boolean isShowingPluginOverview() {
    return mPluginView != null;
  }

  public void setShowPluginOverview(boolean visible, boolean save) {
    if (visible) {
      mPluginView = new PluginView();
    } else {
      mPluginView = null;
    }
    mPluginsNode.setLeaf(mPluginView);
    TVBrowserActions.pluginView.putValue(ToolBar.ACTION_IS_SELECTED, Boolean.valueOf(visible));
    mMenuBar.setPluginViewItemChecked(visible);
    if(save) {
      Settings.propShowPluginView.setBoolean(visible);
    }

    updateViews();
  }

  /**
   * Makes the StatusBar visible
   *
   * @param visible
   *          true if Statusbar should be visible
   */
  public void setShowStatusbar(boolean visible) {
    JPanel contentPane = (JPanel) getContentPane();

    Settings.propIsStatusbarVisible.setBoolean(visible);

    if (visible && !contentPane.isAncestorOf(mStatusBar)) {
      mSouthPanel.add(mStatusBar, BorderLayout.SOUTH);
    } else if (contentPane.isAncestorOf(mStatusBar)) {
      mSouthPanel.remove(mStatusBar);
      System.out.println("hier");
    }

    contentPane.invalidate();
    contentPane.repaint();
  }

  public ProgressMonitor createProgressMonitor() {
    return mStatusBar.createProgressMonitor();
  }

  public void selectChannel(Channel channel) {
    mChannelChooser.selectChannel(channel);
  }

  /**
   * increase/decrease the font of the program table
   *
   * @param offset positive values increase font, negative values decrease font, zero sets to default again
   */
  public void changeFontSize(int offset) {
    if (util.ui.ProgramPanel.updateFonts(offset)) {
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      ProgramTableScrollPane scrollPane = getProgramTableScrollPane();
      scrollPane.forceRepaintAll();
    }
  }

  /**
   * increase/decrease the width of the program table columns
   *
   * @param offset positive values increase column width,
   * negative values decrease column width, zero sets to default again
   */
  public void changeColumnWidth(int offset) {
    int columnWidth = util.ui.ProgramPanel.updateColumnWidth(offset);
    ProgramTableScrollPane scrollPane = getProgramTableScrollPane();
    scrollPane.setColumnWidth(columnWidth);
    scrollPane.forceRepaintAll();
  }

  public StatusBar getStatusBar() {
    return mStatusBar;
  }

  /**
   * get whether the mainframe is currently in full screen mode
   *
   * @return in full screen mode
   * @since 2.5.3
   */
  public boolean isFullScreenMode() {
    return isUndecorated();
  }

  public void updatePluginTree() {
    if (mPluginView != null) {
      mPluginView.refreshTree();
    }
  }

  /**
   * extract the drag and drop targets from the event
   * @param transferable
   * @param dataFlavors
   * @return
   */
  private File[] getDragDropPlugins(final DataFlavor[] dataFlavors, final Transferable transferable) {
    HashSet<File> files = new HashSet<File>();
    for(DataFlavor flavor : dataFlavors) {
      try {
        Object data = transferable.getTransferData(flavor);
        
        if(data instanceof List) {
          for(Object o : ((List)data)) {
            if(o instanceof File) {
              addPluginFile((File)o, files);
            }
          }
          if (!files.isEmpty()) {
            break;
          }
        }
        else if (data instanceof String) {
          String name = ((String) data).trim();
          if (name.toLowerCase().endsWith("jar")) {
            File pluginFile = new File(name);
            if (pluginFile.canRead()) {
              addPluginFile(pluginFile, files);
              if (!files.isEmpty()) {
                break;
              }
            }
            else {
              try {
                URI uri = new URI(name);
                addPluginFile(new File(uri), files);
              } catch (URISyntaxException e) { // ignore
              }
              if (!files.isEmpty()) {
                break;
              }
            }
          }
        }
      } catch (UnsupportedFlavorException e) { //ignore
      } catch (IOException e) { //ignore
      }
    }
    return files.toArray(new File[files.size()]);
  }

  private void addPluginFile(final File file, final HashSet<File> files) {
    if (file.isFile() && file.canRead()) {
      if(file.getName().toLowerCase().endsWith(".jar")) {
        files.add(file);
      }
      else if(file.getName().toLowerCase().endsWith(".zip")) {
        try {
          ZipFile test = new ZipFile(file);
          Enumeration<? extends ZipEntry> entries = test.entries();
          
          File tempDir = new File(System.getProperty("java.io.tmpdir"),"tvbinstplugin");
          tempDir.mkdirs();
          
          while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            if(entry.getName().toLowerCase().endsWith(".jar")) {
              BufferedInputStream bis = new BufferedInputStream(test.getInputStream(entry));
            
              int size;
              byte[] buffer = new byte[2048];
              
              File out = new File(tempDir.getAbsolutePath(),entry.getName());
              
              if(out.isFile()) {
                out.delete();
              }
              
              BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out), buffer.length);
            
              while ((size = bis.read(buffer, 0, buffer.length)) != -1) 
              {
                  bos.write(buffer, 0, size);
              }
              bos.flush();
              bos.close();
              bis.close();
              
              if(out.isFile() && out.canRead()) {
                files.add(out);
              }
            }
          }
          
        } catch (Exception e) {}
      }
    }
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    File[] files = getDragDropPlugins(dtde.getCurrentDataFlavors(), dtde.getTransferable());
    if (files.length > 0) {
      dtde.acceptDrag(dtde.getDropAction());
    }
    else {
      dtde.rejectDrag();
    }
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
    // empty
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    // empty
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    dtde.acceptDrop(dtde.getDropAction());
    File[] files = getDragDropPlugins(dtde.getCurrentDataFlavors(),  dtde.getTransferable());

    try {
      File tmpFile = File.createTempFile("plugins",".txt");
      StringBuilder alreadyInstalled = new StringBuilder();
      StringBuilder notCompatiblePlugins = new StringBuilder();

      for (File jarFile : files) {
        ClassLoader classLoader = null;

        try {
          URL[] urls = new URL[] { jarFile.toURI().toURL() };
          classLoader = URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
        } catch (MalformedURLException exc) {

        }

        if (classLoader != null) {
          // Get the plugin name
          String pluginName = jarFile.getName();
          pluginName = pluginName.substring(0, pluginName.length() - 4);

          try {
            String pluginId = "java." + pluginName.toLowerCase() + "." + pluginName;

            PluginProxy installedPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);
            TvDataServiceProxy service = TvDataServiceProxyManager.getInstance().findDataServiceById(
                pluginName.toLowerCase() + '.' + pluginName);

            Class<?> pluginClass = classLoader.loadClass(pluginName.toLowerCase() + '.' + pluginName);

            Method getVersion = pluginClass.getMethod("getVersion", new Class[0]);

            Version version1 = null;
            try {
              version1 = (Version) getVersion.invoke(pluginClass, new Object[0]);
            } catch (Throwable t1) {
              t1.printStackTrace();
            }

            if (installedPlugin != null && (installedPlugin.getInfo().getVersion().compareTo(version1) > 0 || (installedPlugin.getInfo().getVersion().compareTo(version1) == 0 && version1.isStable()))) {
              alreadyInstalled.append(installedPlugin.getInfo().getName()).append('\n');
            } else if (service != null && (service.getInfo().getVersion().compareTo(version1) > 0 || (service.getInfo().getVersion().compareTo(version1) == 0 && version1.isStable()))) {
              alreadyInstalled.append(service.getInfo().getName()).append('\n');
            } else {
              RandomAccessFile write = new RandomAccessFile(tmpFile, "rw");

              String versionString = Integer.toString(version1.getMajor()) + '.' + (version1.getMinor() / 10) + (version1.getMinor() % 10)
                  + '.' + version1.getSubMinor();

              write.seek(write.length());

              write.writeBytes("[plugin:" + pluginName + "]\n");
              write.writeBytes("name_en=" + pluginName + "\n");
              write.writeBytes("filename=" + jarFile.getName() + "\n");
              write.writeBytes("version=" + versionString + "\n");
              write.writeBytes("stable=" + version1.isStable() + "\n");
              write.writeBytes("download=" + jarFile.toURI().toURL() + "\n");
              write.writeBytes("category=unknown\n");

              write.close();

            }
          } catch (Exception e) {
            notCompatiblePlugins.append(jarFile.getName()).append("\n");
          }
        }
      }

      if (alreadyInstalled.length() > 0) {
        showInfoTextMessage(mLocalizer.msg("update.alreadyInstalled",
            "The following Plugin in current version are already installed:"), alreadyInstalled.toString(), 400);
      }

      if (notCompatiblePlugins.length() > 0) {
        showInfoTextMessage(mLocalizer.msg("update.noTVBPlugin", "This following files are not TV-Browser Plugins:"),
            notCompatiblePlugins.toString(), 400);
      }


      if (tmpFile.length() > 0) {
        java.net.URL url = tmpFile.toURI().toURL();
        SoftwareUpdater softwareUpdater = new SoftwareUpdater(url, SoftwareUpdater.ALL_TYPE, true);
        mSoftwareUpdateItems = softwareUpdater.getAvailableSoftwareUpdateItems();
        dtde.dropComplete(true);

        SoftwareUpdateDlg updateDlg = new SoftwareUpdateDlg(this, SoftwareUpdater.DRAG_AND_DROP_TYPE, mSoftwareUpdateItems);
        updateDlg.setVisible(true);
      } else {
        dtde.rejectDrop();
        dtde.dropComplete(false);
      }

      if (!tmpFile.delete()) {
        tmpFile.deleteOnExit();
      }
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
    // TODO Auto-generated method stub

  }

  private void showInfoTextMessage(String header, String infoText, int width) {
    JTextArea textArea = new JTextArea(infoText);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);

    scrollPane.setPreferredSize(new Dimension(width,150));

    Object[] msg = {header,scrollPane};
    JOptionPane.showMessageDialog(this,msg,Localizer.getLocalization(Localizer.I18N_INFO),JOptionPane.INFORMATION_MESSAGE);
  }

  public void updateChannelGroupMenu(JMenu channelGroupMenu) {
    mMenuBar.updateChannelGroupMenu(channelGroupMenu);
  }

  public boolean getUserRequestCopyToSystem() {
    return mMenuBar.getUserRequestedCopyToSystem();
  }
  
  private class BackgroundPanel extends JPanel {
    protected void paintComponent(Graphics g) {
      if(Persona.getInstance().getAccentColor() != null) {
        g.setColor(Persona.getInstance().getAccentColor());
        g.fillRect(0,0,getWidth(),getHeight());
      }
      else {
        super.paintComponent(g);
      }
      
      BufferedImage headerImage = Persona.getInstance().getHeaderImage();
      BufferedImage footerImage = Persona.getInstance().getFooterImage();
      
      if(headerImage != null) {
        g.drawImage(headerImage,0,0,jcontentPane.getWidth(),headerImage.getHeight()-(mMenuBar.isVisible() ? mMenuBar.getHeight() : 0),headerImage.getWidth()-jcontentPane.getWidth(),mMenuBar.isVisible() ? mMenuBar.getHeight() : 0,headerImage.getWidth(),headerImage.getHeight(),null);
      }
      if(footerImage != null) {
        g.drawImage(footerImage,0,jcontentPane.getHeight()-footerImage.getHeight(),footerImage.getWidth(),jcontentPane.getHeight(),0,0,footerImage.getWidth(),footerImage.getHeight(),null);
      }
    }
  }
  
  /**
   * Updates the search field on Persona change.
   */
  public void updatePersona() {
    repaint();
    if(Persona.getInstance().getHeaderImage() != null) {
      if(mToolBarPanel != null) {
        mToolBarPanel.setOpaque(false);
      }
      if(mSearchField != null) {
        mSearchField.setOpaque(false);
      }
      mCenterTabPane.setOpaque(false);
      mCenterTabPane.setBackground(new Color(0,0,0,0));
      mCenterTabPane.setForeground(Persona.getInstance().getTextColor());
    }
    else {
      if(mToolBarPanel != null) {
        mToolBarPanel.setOpaque(true);
      }
      if(mSearchField != null) {
        mSearchField.setOpaque(true);
      }
      mCenterTabPane.setOpaque(true);
      mCenterTabPane.setBackground(UIManager.getColor("Panel.background"));
      mCenterTabPane.setForeground(UIManager.getColor("List.foreground"));
    }
    if(mToolBarPanel != null) {
      mToolBarPanel.updateUI();
    }
    
    mMenuBar.updatePersona();
    mToolBar.updatePersona();
    
    if(mSearchField != null) {
      mSearchField.updatePersona();
    }
    
    mTimeChooserPanel.updatePersona();
    mStatusBar.updatePersona();
    mProgramTableScrollPane.updatePersona();
    mFilterPanel.updatePersona();
  }
  
  public void updateCenterPanels() {
    try {
      mCenterPanelWrapperList.clear();
      
      PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
      InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
      
      ArrayList<PluginCenterPanel> centerPanelList = new ArrayList<PluginCenterPanel>(1);
      
      centerPanelList.add(mScrollPaneWrapper);
      
      for(PluginProxy plugin : plugins) {
        PluginCenterPanelWrapper wrapper = plugin.getPluginCenterPanelWrapper();
        
        if(wrapper != null) {
          mCenterPanelWrapperList.add(wrapper);
          try {
            PluginCenterPanel[] panels = wrapper.getCenterPanels();
            
            for(PluginCenterPanel panel : panels) {
              if(panel != null && panel.getPanel() != null && panel.getName() != null && panel.getId() != null) {
                centerPanelList.add(panel);
              }
            }
            // Prevent Plugins from making problems.
          }catch(Throwable e) {e.printStackTrace();}
        }
      }
      
      for(InternalPluginProxyIf internalPlugin : internalPlugins) {
        PluginCenterPanelWrapper wrapper = internalPlugin.getPluginCenterPanelWrapper();
        
        if(wrapper != null) {
          mCenterPanelWrapperList.add(wrapper);
          try {
            PluginCenterPanel[] panels = wrapper.getCenterPanels();
            
            for(PluginCenterPanel panel : panels) {
              if(panel != null && panel.getPanel() != null && panel.getName() != null && panel.getId() != null) {
                centerPanelList.add(panel);
              }
            }
            // Prevent Plugins from making problems.
          }catch(Throwable e) {e.printStackTrace();}
        }
      }
      
      ArrayList<PluginCenterPanel> usedCenterPanelList = new ArrayList<PluginCenterPanel>();
      
      ArrayList<String> enabledCenterPanels = new ArrayList<String>(Arrays.asList(Settings.propCenterPanelArr.getStringArray()));
      
      for(String enabledPanel : enabledCenterPanels) {
        for(int i = 0; i < centerPanelList.size(); i++) {
          if(enabledPanel.equals(centerPanelList.get(i).getId())) {
            usedCenterPanelList.add(centerPanelList.remove(i));
            break;
          }
        }
      }
      
      for(String disabledPanel : Settings.propDisabledCenterPanelArr.getStringArray()) {
        for(int i = 0; i < centerPanelList.size(); i++) {
          if(disabledPanel.equals(centerPanelList.get(i).getId())) {
            centerPanelList.remove(i);
            break;
          }
        }
      }
      
      boolean addNew = false;
      
      if(!centerPanelList.isEmpty()) {
        for(PluginCenterPanel centerPanel : centerPanelList) {
          usedCenterPanelList.add(centerPanel);
        }
        
        addNew = true;
      }
      
      mCenterPanel.removeAll();
      mCenterTabPane.removeAll();
      
      if(usedCenterPanelList.isEmpty()) {
        mCenterPanel.add(mProgramTableScrollPane, BorderLayout.CENTER);
      }
      else if(usedCenterPanelList.size() == 1 && !Settings.propAlwaysShowTabBarForCenterPanel.getBoolean()) {
        mCenterPanel.add(usedCenterPanelList.get(0).getPanel(), BorderLayout.CENTER);
      }
      else {
        mCenterPanel.add(mCenterTabPane, BorderLayout.CENTER);
        ArrayList<String> usedIdList = null;
        
        if(addNew) {
          usedIdList = new ArrayList<String>(usedCenterPanelList.size());
        }
        
        for(PluginCenterPanel panel : usedCenterPanelList) {
          mCenterTabPane.addTab(panel.getName(), panel.getPanel());
          
          if(addNew) {
            usedIdList.add(panel.getId());
          }
        }
        
        if(usedIdList != null && !usedIdList.isEmpty()) {
          Settings.propCenterPanelArr.setStringArray(usedIdList.toArray(new String[usedIdList.size()]));
        }
      }
    }catch(Throwable t) {t.printStackTrace();
      // if everything went wrong we use the program table scroll pane
      mCenterPanel.add(mProgramTableScrollPane, BorderLayout.CENTER);
    }
  }
  
  public ProgramTableScrollPaneWrapper getProgramTableScrollPaneWrapper() {
    return mScrollPaneWrapper;
  }

  @Override
  public void pluginActivated(PluginProxy plugin) {   
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        updateCenterPanels();    
      }
    });
  }

  @Override
  public void pluginDeactivated(PluginProxy plugin) {
    if(!isShuttingDown()) {
      SwingUtilities.invokeLater(new Runnable() {
        
        @Override
        public void run() {
          updateCenterPanels();    
        }
      });
    }
  }

  @Override
  public void pluginLoaded(PluginProxy plugin) {}

  @Override
  public void pluginUnloaded(PluginProxy plugin) {}
  
  public void showProgramTableTabIfAvailable() {
    if(mCenterTabPane != null && mScrollPaneWrapper != null) {
      int index = mCenterTabPane.indexOfComponent(mScrollPaneWrapper.getPanel());
      
      if(index >= 0) {
        mCenterTabPane.setSelectedIndex(index);
      }
      else {
        mCenterTabPane.setSelectedIndex(0);
      }
    }
  }
}