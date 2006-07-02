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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
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
import tvbrowser.extras.favoritesplugin.FavoritesSettingTab;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.extras.programinfo.ProgramInfoDesignSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoFontSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoFunctionsSettingsTab;
import tvbrowser.extras.programinfo.ProgramInfoOrderSettingsTab;
import tvbrowser.extras.reminderplugin.ReminderSettingsTab;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
  
  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent, String selectedTabId) {
    mInstance = this;
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(mLocalizer.msg("settings", "Settings"));

    UiUtilities.registerForClosing(this);

    JPanel main = new JPanel(new FormLayout("fill:min:grow", "fill:min:grow, 3dlu, pref"));
    CellConstraints cc = new CellConstraints();

    main.setBorder(Borders.DLU4_BORDER);
    mDialog.setContentPane(main);

    JSplitPane splitPane = new JSplitPane();
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

    // Make the viewport as big as the tree when all nodes are expanded
    int categoryCount = mRootNode.getChildCount();
    for (int i = categoryCount; i >= 1; i--) {
      mSelectionTree.expandRow(i);
    }
    scrollPane.getViewport().setPreferredSize(mSelectionTree.getPreferredSize());
    // Let the tree collapse
    for (int i = 1; i <= categoryCount; i++) {
      mSelectionTree.collapseRow(i);
    }

    mSettingsPn = new JPanel(new BorderLayout());
    splitPane.setRightComponent(mSettingsPn);

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();

    JButton okBt = new JButton(mLocalizer.msg("ok", "OK"));
    okBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        invalidateTree();
        mDialog.dispose();
      }
    });
    mDialog.getRootPane().setDefaultButton(okBt);

    JButton cancelBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mDialog.dispose();
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

    builder.addGriddedButtons(new JButton[] { okBt, cancelBt, applyBt });

    main.add(builder.getPanel(), cc.xy(1, 3));

    mDialog.pack();

    if (selectedTabId == null) {
      selectedTabId = SettingsItem.CHANNELS;
    }

    SettingNode n = findSettingNode((SettingNode) mRootNode, selectedTabId);
    if (n != null) {
      showSettingsPanelForNode(n);
      TreePath selectedPath = new TreePath(n.getPath());
      mSelectionTree.setSelectionPath(selectedPath);
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

  }

  void invalidateTree() {
    ((SettingNode) mRootNode).invalidate();
  }

  private SettingNode findSettingNode(SettingNode root, String tabId) {

    if (tabId.equals(root.getId())) {
      return root;
    }
    int cnt = root.getChildCount();
    for (int i = 0; i < cnt; i++) {
      SettingNode result = findSettingNode((SettingNode) root.getChildAt(i), tabId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public void centerAndShow() {
    if ((Settings.propSettingsWindowX.getInt() == -1) && (Settings.propSettingsWindowY.getInt() == -1)) {
      mDialog.setSize(700,600);
      UiUtilities.centerAndShow(mDialog);
    } else if ((Settings.propSettingsWindowWidth.getInt() == -1) && (Settings.propSettingsWindowWidth.getInt() == -1)) {
      mDialog.setSize(700,600);
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
    String msg;
    SettingNode node;

    icon = IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16);
    msg = mLocalizer.msg("settings", "Settings");
    SettingNode root = new SettingNode(new DefaultSettingsTab(msg, icon));

    SettingNode generalSettings = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("general", "General"), null));
    root.add(generalSettings);

    SettingNode technicalSettings = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("technical", "Technical"),
        null));
    root.add(technicalSettings);

    if (TVBrowser.isUsingSystemTray()) {
      SettingNode traySettings = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("tray", "Tray Settings"), null));
      root.add(traySettings);

      traySettings.add(new SettingNode(new TrayBaseSettingsTab(), SettingsItem.TRAY));
      traySettings.add(new SettingNode(new TrayImportantSettingsTab()));
      traySettings.add(new SettingNode(new TrayNowSettingsTab()));
      traySettings.add(new SettingNode(new TraySoonSettingsTab()));
      traySettings.add(new SettingNode(new TrayOnTimeSettingsTab()));
      traySettings.add(new SettingNode(new TrayProgramsChannelsSettingsTab()));
    }

    SettingNode programtableNode = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("channelstable",
        "Channelstable"), null));
    root.add(programtableNode);

    SettingNode appearanceNode = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("appearance", "Appearance"),
        null));
    programtableNode.add(appearanceNode);

    generalSettings.add(new SettingNode(new LocaleSettingsTab()));
    generalSettings.add(new SettingNode(new LookAndFeelSettingsTab(), SettingsItem.LOOKANDFEEL));
    generalSettings.add(new SettingNode(new ContextmenuSettingsTab(), SettingsItem.CONTEXTMENU));
    generalSettings.add(new SettingNode(new MausSettingsTab()));

    generalSettings.add(new SettingNode(new StartupSettingsTab(), SettingsItem.STARTUP));

    programtableNode.add(new SettingNode(new ChannelsSettingsTab(), SettingsItem.CHANNELS));
    programtableNode.add(new SettingNode(new RefreshDataSettingsTab()));
    programtableNode.add(new SettingNode(new ButtonsSettingsTab(), SettingsItem.TIMEBUTTONS));

    appearanceNode.add(new SettingNode(new ProgramTableSettingsTab(), SettingsItem.PROGRAMTABLELOOK));
    appearanceNode.add(new SettingNode(new ChannelListSettingsTab(), SettingsItem.CHANNELLISTLOOK));
    appearanceNode.add(new SettingNode(new FontsSettingsTab()));
    appearanceNode.add(new SettingNode(new ProgramPanelSettingsTab()));

    technicalSettings.add(new SettingNode(new ProxySettingsTab()));
    technicalSettings.add(new SettingNode(new DirectoriesSettingsTab()));
    technicalSettings.add(new SettingNode(new WebbrowserSettingsTab(), SettingsItem.WEBBROWSER));

    SettingNode programInfo = new SettingNode(new DefaultSettingsTab(ProgramInfo.getInstance().toString(), null));
    programInfo.add(new SettingNode(new ProgramInfoOrderSettingsTab(), SettingsItem.PROGRAMINFO));
    programInfo.add(new SettingNode(new ProgramInfoFontSettingsTab()));
    programInfo.add(new SettingNode(new ProgramInfoDesignSettingsTab()));
    programInfo.add(new SettingNode(new ProgramInfoFunctionsSettingsTab()));

    root.add(programInfo);

    root.add(new SettingNode(new FavoritesSettingTab(), SettingsItem.FAVORITE));
    root.add(new SettingNode(new ReminderSettingsTab(), SettingsItem.REMINDER));

    // Plugins
    mPluginSettingsNode = new SettingNode(new PluginSettingsTab(this), SettingsItem.PLUGINS);
    root.add(mPluginSettingsNode);

    createPluginTreeItems(false);

    // TVDataServices
    node = new SettingNode(new DataServiceSettingsTab());
    root.add(node);
    TvDataServiceProxy[] services = tvbrowser.core.tvdataservice.TvDataServiceProxyManager.getInstance()
        .getDataServices();
    for (int i = 0; i < services.length; i++) {
      node.add(new SettingNode(new ConfigDataServiceSettingsTab(services[i])));
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

    Arrays.sort(pluginArr, new Comparator<PluginProxy>() {
      public int compare(PluginProxy o1, PluginProxy o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });

    for (int i = 0; i < pluginArr.length; i++) {
      ConfigPluginSettingsTab tab = new ConfigPluginSettingsTab(pluginArr[i]);
      mPluginSettingsNode.add(new SettingNode(tab, pluginArr[i].getId()));
    }
    if (mSelectionTree != null)
      ((DefaultTreeModel) mSelectionTree.getModel()).reload(mPluginSettingsNode);
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
      mSettingsPn.add(pn);
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

    public SettingNode(Icon icon, String title, String id) {
      super(title);
      mIcon = icon;
      mId = id;
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
          mSettingsPn = mSettingsTab.createSettingsPanel();
        }
      }

      return mSettingsPn;
    }

    public Icon getIcon() {
      return mIcon;
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
    SettingNode node = findSettingNode((SettingNode) mRootNode, id);
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
