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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.extras.favoritesplugin.FavoritesMarkingsSettingsTab;
import tvbrowser.extras.favoritesplugin.FavoritesPicturesSettingsTab;
import tvbrowser.extras.favoritesplugin.FavoritesSettingTab;
import tvbrowser.extras.programinfo.ProgramInfoDesignSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoFontSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoFunctionsSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoOrderSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoPicturesSettingsTab;
import tvbrowser.extras.reminderplugin.ReminderMarkingsSettingsTab;
import tvbrowser.extras.reminderplugin.ReminderPicturesSettingsTab;
import tvbrowser.extras.reminderplugin.ReminderSettingsTab;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.ui.Localizer;
import tvbrowser.extras.searchplugin.SearchPictureSettingsTab;
import tvbrowser.extras.searchplugin.SearchSettingsTab;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginAccess;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * 
 * @author Til Schneider, www.murfman.de
 */
public class SettingsDialog implements WindowClosingIf {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SettingsDialog.class);

  private JDialog mDialog;

  private JTree mSelectionTree;

  private JPanel mSettingsPn;

  // private JScrollPane mSettingsPane;
  private TreeNode mRootNode;

  /** Location of this Dialog */
  private Point mLocation = new Point();

  /** Dimension of this Dialog */
  private Dimension mSize = new Dimension();

  /** Node for PluginSettings */
  private SettingNode mPluginSettingsNode;

  /** Instance of the SettingsDialog */
  private static SettingsDialog mInstance;
  
  private JButton mHelpBt;
  
  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent, String selectedTabId) {
    mInstance = this;
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(Localizer.getLocalization(Localizer.I18N_SETTINGS));

    UiUtilities.registerForClosing(this);

    JPanel main = new JPanel(new FormLayout("fill:min:grow", "fill:min:grow, 3dlu, pref"));
    CellConstraints cc = new CellConstraints();

    main.setBorder(Borders.DLU4_BORDER);
    mDialog.setContentPane(main);

    final JSplitPane splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    main.add(splitPane, cc.xy(1, 1));

    mRootNode = createSelectionTree();
    mSelectionTree = new JTree(mRootNode);
    mSelectionTree.setRootVisible(true);
    mSelectionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    mSelectionTree.setCellRenderer(new SettingNodeCellRenderer());
    mSelectionTree.setSelectionRow(1);
    mSelectionTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent evt) {
        showSettingsPanelForSelectedNode();
      }
    });
    JScrollPane scrollPane = new JScrollPane(mSelectionTree);
    scrollPane.setMinimumSize(new Dimension(150, 0));
    scrollPane.setBorder(null);
    splitPane.setLeftComponent(scrollPane);
    
    splitPane.setDividerLocation(Settings.propSettingsDialogDividerLocation.getInt());
    
    int categoryCount = mRootNode.getChildCount();
    // Let the tree collapse
    for (int i = 1; i <= categoryCount; i++) {
      mSelectionTree.collapseRow(i);
    }

    mSettingsPn = new JPanel(new BorderLayout());
    splitPane.setRightComponent(mSettingsPn);

    ButtonBarBuilder builder = new ButtonBarBuilder();

    mHelpBt = new JButton(mLocalizer.msg("help","Online help"));
    mHelpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Launch.openURL(mHelpBt.getToolTipText());
      }
    });

    builder.addGridded(mHelpBt);
    
    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettingsTab();
        saveSettings();
        invalidateTree();
        close();
      }
    });
    mDialog.getRootPane().setDefaultButton(okBt);

    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        close();
      }
    });

    JButton applyBt = new JButton(mLocalizer.msg("apply", "Apply"));
    applyBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        invalidateTree();
        Settings.handleChangedSettings();
        showSettingsPanelForSelectedNode();
      }
    });

    builder.addGlue();
    builder.addGriddedButtons(new JButton[] { okBt, cancelBt, applyBt });
    main.add(builder.getPanel(), cc.xy(1, 3));

    mDialog.pack();

    if (selectedTabId == null) {
      selectedTabId = SettingsItem.CHANNELS;
    }

    SettingNode n = findSettingNodeById((SettingNode) mRootNode, selectedTabId);
    if (n == null) {
      n = findSettingNodeByPath((SettingNode) mRootNode, selectedTabId);
    }
    if (n != null) {
      showSettingsPanelForNode(n);
      TreePath selectedPath = new TreePath(n.getPath());
      mSelectionTree.setSelectionPath(selectedPath);
      mSelectionTree.expandPath(selectedPath);
    } else {
      showSettingsPanelForSelectedNode();
    }

    mDialog.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentMoved(ComponentEvent e) {
        e.getComponent().getLocation(mLocation);
        Settings.propSettingsWindowX.setInt(mLocation.x);
        Settings.propSettingsWindowY.setInt(mLocation.y);
      }

      public void componentResized(ComponentEvent e) {
        e.getComponent().getSize(mSize);
        Settings.propSettingsWindowWidth.setInt(mSize.width);
        Settings.propSettingsWindowHeight.setInt(mSize.height);
      }
    });
    
    mDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        Settings.propSettingsDialogDividerLocation.setInt(splitPane.getDividerLocation());
      }
      
      public void windowClosed(WindowEvent e) {
        Settings.propSettingsDialogDividerLocation.setInt(splitPane.getDividerLocation());
      }
    });
  }

  protected void saveSettingsTab() {
    TreePath selection = mSelectionTree.getSelectionPath();
    if (selection != null) {
      String path = "";
      for (int i = 0; i < selection.getPathCount(); i++) {
        path = path + selection.getPathComponent(i);
      }
      Settings.propLastUsedSettingsPath.setString(path);
    }
  }

  void invalidateTree() {
    ((SettingNode) mRootNode).invalidate();
  }

  private SettingNode findSettingNodeById(SettingNode root, String tabId) {

    if (tabId.equals(root.getId())) {
      return root;
    }
    int cnt = root.getChildCount();
    for (int i = 0; i < cnt; i++) {
      SettingNode result = findSettingNodeById((SettingNode) root.getChildAt(i), tabId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private SettingNode findSettingNodeByPath(SettingNode root, String searchPath) {
    String pathString = "";
    TreeNode[] rootPath = root.getPath();
    for (TreeNode treeNode : rootPath) {
      pathString = pathString + ((SettingNode)treeNode).toString();
    }
    if (searchPath.equals(pathString)) {
      return root;
    }
    int cnt = root.getChildCount();
    for (int i = 0; i < cnt; i++) {
      SettingNode result = findSettingNodeByPath((SettingNode) root.getChildAt(i), searchPath);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public void centerAndShow() {
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	  
    if ((Settings.propSettingsWindowX.getInt() < 0) || (Settings.propSettingsWindowX.getInt() > screen.width) || (Settings.propSettingsWindowY.getInt() < 0) || (Settings.propSettingsWindowY.getInt() > screen.height)) {
      mDialog.setSize(750,600);
      UiUtilities.centerAndShow(mDialog);
    } else if ((Settings.propSettingsWindowWidth.getInt() < 200) || (Settings.propSettingsWindowWidth.getInt() < 200)) {
      mDialog.setSize(750,600);
      mLocation = new Point(Settings.propSettingsWindowX.getInt(), Settings.propSettingsWindowY.getInt());
      mDialog.setLocation(mLocation);
      mDialog.setVisible(true);
    } else {
      mLocation = new Point(Settings.propSettingsWindowX.getInt(), Settings.propSettingsWindowY.getInt());
      mSize = new Dimension(Settings.propSettingsWindowWidth.getInt(), Settings.propSettingsWindowHeight.getInt());
      mDialog.setLocation(mLocation);
      mDialog.setSize(mSize);
      mDialog.setVisible(true);
    }
  }

  private TreeNode createSelectionTree() {
    Icon icon;
    SettingNode node;

    icon = IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16);    
    SettingNode root = new SettingNode(new DefaultSettingsTab(Localizer.getLocalization(Localizer.I18N_SETTINGS), icon));

    SettingNode generalSettings = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("general", "General"), null));
    root.add(generalSettings);

    SettingNode technicalSettings = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("technical", "Technical"),
        null));
    root.add(technicalSettings);

    if (TVBrowser.isUsingSystemTray()) {
      SettingNode traySettings = new SettingNode(new TrayBaseSettingsTab(), SettingsItem.TRAY);
      root.add(traySettings);

      traySettings.add(new SettingNode(new TrayImportantSettingsTab()));
      traySettings.add(new SettingNode(new TrayNowSettingsTab()));
      traySettings.add(new SettingNode(new TraySoonSettingsTab()));
      traySettings.add(new SettingNode(new TrayOnTimeSettingsTab()));
      traySettings.add(new SettingNode(new TrayProgramsChannelsSettingsTab()));
    }

    SettingNode programtableNode = new SettingNode(new ChannelsSettingsTab(), SettingsItem.CHANNELS);
    root.add(programtableNode);

    SettingNode appearanceNode = new SettingNode(new ProgramTableSettingsTab(), SettingsItem.PROGRAMTABLELOOK);
    programtableNode.add(appearanceNode);

    generalSettings.add(new SettingNode(new LocaleSettingsTab()));
    generalSettings.add(new SettingNode(new LookAndFeelSettingsTab(), SettingsItem.LOOKANDFEEL));
    generalSettings.add(new SettingNode(new ContextmenuSettingsTab(), SettingsItem.CONTEXTMENU));
    generalSettings.add(new SettingNode(new MausSettingsTab()));
    generalSettings.add(new SettingNode(new GlobalPluginProgramFormatingSettings(), SettingsItem.PLUGINPROGRAMFORMAT));

    generalSettings.add(new SettingNode(new StartupSettingsTab(), SettingsItem.STARTUP));
    generalSettings.add(new SettingNode(new CloseSettingsTab()));

    programtableNode.add(new SettingNode(new RefreshDataSettingsTab()));
    programtableNode.add(new SettingNode(new ButtonsSettingsTab(), SettingsItem.TIMEBUTTONS));

    SettingNode programPanelNode = new SettingNode(new ProgramPanelSettingsTab(), SettingsItem.PROGRAMPANELLOOK);    
    programPanelNode.add(new SettingNode(new MarkingsSettingsTab(), SettingsItem.PROGRAMPANELMARKING));

    appearanceNode.add(programPanelNode);
    appearanceNode.add(new SettingNode(new PictureSettingsTab()));
    appearanceNode.add(new SettingNode(new ChannelListSettingsTab(), SettingsItem.CHANNELLISTLOOK));
    appearanceNode.add(new SettingNode(new FontsSettingsTab()));

    technicalSettings.add(new SettingNode(new NetworkSettingsTab()));
    technicalSettings.add(new SettingNode(new ProxySettingsTab()));
    
    if(!TVBrowser.isTransportable()) {
      technicalSettings.add(new SettingNode(new DirectoriesSettingsTab()));
    }
    
    technicalSettings.add(new SettingNode(new WebbrowserSettingsTab(), SettingsItem.WEBBROWSER));    

    SettingNode search = new SettingNode(new SearchSettingsTab(),SettingsItem.SEARCH);
    search.add(new SettingNode(new SearchPictureSettingsTab()));
    root.add(search);
    
    SettingNode programInfo = new SettingNode(new ProgramInfoOrderSettingsTab(), SettingsItem.PROGRAMINFO);
    programInfo.add(new SettingNode(new ProgramInfoPicturesSettingsTab()));
    programInfo.add(new SettingNode(new ProgramInfoFontSettingsTab()));
    programInfo.add(new SettingNode(new ProgramInfoDesignSettingsTab()));
    programInfo.add(new SettingNode(new ProgramInfoFunctionsSettingsTab()));

    root.add(programInfo);
    
    SettingNode favoritesNode = new SettingNode(new FavoritesSettingTab(), SettingsItem.FAVORITE);    
    favoritesNode.add(new SettingNode(new FavoritesPicturesSettingsTab()));
    favoritesNode.add(new SettingNode(new FavoritesMarkingsSettingsTab()));
    
    root.add(favoritesNode);
    
    SettingNode reminderNode = new SettingNode(new ReminderSettingsTab(), SettingsItem.REMINDER);    
    reminderNode.add(new SettingNode(new ReminderPicturesSettingsTab()));
    reminderNode.add(new SettingNode(new ReminderMarkingsSettingsTab()));
    
    root.add(reminderNode);

    // Plugins
    mPluginSettingsNode = new SettingNode(new PluginSettingsTab(this), SettingsItem.PLUGINS);
    root.add(mPluginSettingsNode);

    createPluginTreeItems(false);

    // TVDataServices
    node = new SettingNode(new DataServiceSettingsTab(), SettingsItem.TVDATASERVICES);
    root.add(node);
    TvDataServiceProxy[] services = tvbrowser.core.tvdataservice.TvDataServiceProxyManager.getInstance()
        .getDataServices();
    for (TvDataServiceProxy dataService : services) {
      node.add(new SettingNode(new ConfigDataServiceSettingsTab(dataService)));
    }

    return root;
  }

  /**
   * Removes all Items from the PluginSettingsNode and recreates the Child Nodes
   * 
   * @param refresh If true, the Tree will be refreshed
   */
  private void createPluginTreeItems(boolean refresh) {
    mPluginSettingsNode.removeAllChildren();

    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getAllPlugins();

    ArrayList<SettingNode> nodeList=new ArrayList<SettingNode>();
    for (PluginProxy plugin : pluginArr) {
    	ConfigPluginSettingsTab tab=new ConfigPluginSettingsTab(plugin);
    	nodeList.add(new SettingNode(tab,plugin.getId()));
    }
    SettingNode[] nodes = new SettingNode[nodeList.size()];
    nodeList.toArray(nodes);
    Arrays.sort(nodes, new Comparator<SettingNode>() {

		public int compare(SettingNode o1, SettingNode o2) {
			return o1.getSettingsTab().getTitle().compareTo(o2.getSettingsTab().getTitle());
		}});
    for (SettingNode node : nodes) {
        mPluginSettingsNode.add(node);
	}
    if (mSelectionTree != null) {
      ((DefaultTreeModel) mSelectionTree.getModel()).reload(mPluginSettingsNode);
    }
  }

  /**
   * Removes all Items from the PluginSettingsNode and recreates the Child Nodes
   */
  public void createPluginTreeItems() {
    createPluginTreeItems(true);
  }

  /**
   * Returns the current Dialog
   * 
   * @return Dialog
   */
  public JDialog getDialog() {
    return mDialog;
  }

  private void saveSettings() {
    saveSettings((SettingNode) mSelectionTree.getModel().getRoot());
  }

  private void saveSettings(SettingNode node) {
    node.saveSettings();

    for (int i = 0; i < node.getChildCount(); i++) {
      saveSettings((SettingNode) node.getChildAt(i));
    }
  }

  private void showSettingsPanelForNode(SettingNode node) {
    JPanel pn = node.getSettingsPanel();
    
    if (pn != null) {    
      String help = node.getHelpUrl();
      
      if(help != null) {
        mHelpBt.setToolTipText(help);
        mHelpBt.setEnabled(true);
      }
      else {
        mHelpBt.setToolTipText(mLocalizer.msg("noHelp", "No help available"));
        mHelpBt.setEnabled(false);
      }
      
      mSettingsPn.add(pn);
    }
    else {
      mHelpBt.setToolTipText(mLocalizer.msg("noHelp", "No help available"));
      mHelpBt.setEnabled(false);
    }
  }

  private void showSettingsPanelForSelectedNode() {
    mSettingsPn.removeAll();

    TreePath selection = mSelectionTree.getSelectionPath();
    if (selection != null) {
      SettingNode node = (SettingNode) selection.getLastPathComponent();
      showSettingsPanelForNode(node);
    }

    mSettingsPn.revalidate();
    mSettingsPn.repaint();
  }

  private class DefaultSettingsTab implements devplugin.SettingsTab {

    private String mTitle;

    private Icon mIcon;
    
    public DefaultSettingsTab(String title, Icon icon) {
      mTitle = title;
      mIcon = icon;
    }

    public JPanel createSettingsPanel() {
      JPanel contentPanel = new JPanel();

      contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
      contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      JLabel titleLb = new JLabel(mTitle);
      titleLb.setFont(new Font("Dialog", Font.PLAIN, 32));
      JLabel lb = new JLabel(mLocalizer.msg("selectCategory", "Please select a category on the left."));
      lb.setFont(new Font("Dialog", Font.PLAIN, 14));
      lb.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

      contentPanel.add(titleLb);
      contentPanel.add(lb);
      return contentPanel;
    }

    public void saveSettings() {

    }

    public Icon getIcon() {

      return mIcon;
    }

    public String getTitle() {
      return mTitle;
    }

  }

  // inner class SettingNode

  private class SettingNode extends DefaultMutableTreeNode {    

    private Icon mIcon;

    private JPanel mSettingsPn;

    private SettingsTab mSettingsTab;

    private String mId;
    
    private String mHelpUrl;

    public SettingNode(Icon icon, String title, String id, String helpUrl) {
      super(title);
      mIcon = icon;
      mId = id;
      mHelpUrl = helpUrl;
    }
    
    public SettingNode(Icon icon, String title, String id) {
      this(icon,title,id,null);
    }

    public SettingsTab getSettingsTab() {
      return mSettingsTab;
    }

    public SettingNode(SettingsTab settingsTab, String id) {
      this(settingsTab.getIcon(), settingsTab.getTitle(), id);
      mSettingsTab = settingsTab;
    }

    public SettingNode(SettingsTab settingsTab) {
      this(settingsTab, null);
    }

    public String getId() {
      return mId;
    }

    public boolean isLoaded() {
      return (mSettingsPn != null);
    }

    public void invalidate() {
      if (mSettingsTab instanceof ConfigPluginSettingsTab) {
        ((ConfigPluginSettingsTab) mSettingsTab).invalidate();
      }

      mSettingsPn = null;
      Enumeration e = children();
      while (e.hasMoreElements()) {
        SettingNode node = (SettingNode) e.nextElement();
        node.invalidate();
      }
    }

    public void saveSettings() {
      if (isLoaded()) {
        mSettingsTab.saveSettings();
      }
    }

    public JPanel getSettingsPanel() {
      if (!isLoaded()) {
        if (mSettingsTab != null) {
          try {
            mSettingsPn = mSettingsTab.createSettingsPanel();
          }catch(Exception e) {
            ErrorHandler.handle(mLocalizer.msg("loadError","An error occurred during loading of {0}",mSettingsTab.getTitle()),e);
          }
        }
      }

      return mSettingsPn;
    }

    public Icon getIcon() {
      return mIcon;
    }
    
    public String getHelpUrl() {
      String url = mHelpUrl;
      
      if (url == null || url =="") {
        if(mSettingsTab instanceof ConfigPluginSettingsTab) {
          PluginAccess plugin = PluginProxyManager.getInstance().getPluginForId(mId);
          
          url = plugin.getInfo().getHelpUrl();
          
          if(url == null) {
            url = "http://www.tvbrowser.org/showHelpFor.php?id=" + plugin.getId() + "&lang=" + System.getProperty("user.language");
          }
        }
        else {
          url = mLocalizer.msg("settingsUrl", "http://enwiki.tvbrowser.org/index.php/Settings {0}",mSettingsTab.getTitle()); 
        }
      }
      
      return url;
    }

  } // class SettingNode

  // inner class SettingNodeCellRenderer

  /**
   * A cell renderer that sets the icon of the SettingNode it renders.
   */
  public class SettingNodeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
        boolean leaf, int rowIndex, boolean hasFocus) {
      JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, rowIndex, hasFocus);

      if (value instanceof SettingNode) {
        SettingNode node = (SettingNode) value;
        Icon icon = node.getIcon();

        if (icon != null) {
          label.setIcon(icon);
        }
      }

      return label;
    }

  } // class SettingNodeCellRenderer

  public void close() {
    mDialog.dispose();
  }

  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }

  /**
   * Show SettingsTab with specific ID
   * @param id ID to show (see devplugin.SettingsItem)
   */
  public void showSettingsTab(String id) {
    SettingNode node = findSettingNodeById((SettingNode) mRootNode, id);
    if (node != null) {
      TreePath selectedPath = new TreePath(node.getPath());
      mSelectionTree.setSelectionPath(selectedPath);
      
      mSettingsPn.removeAll();
      showSettingsPanelForNode(node);
      mSettingsPn.revalidate();
      mSettingsPn.repaint();
    }
  }
  
  /**
   * @return Instance of this Dialog
   */
  public static SettingsDialog getInstance() {
    return mInstance;
  }
}
