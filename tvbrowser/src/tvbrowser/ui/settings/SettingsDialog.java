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
import java.awt.FlowLayout;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.ui.UiUtilities;
import devplugin.SettingsTab;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class SettingsDialog {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SettingsDialog.class);

  public static final String TAB_ID_TOOLBAR = "#toolbar";
  public static final String TAB_ID_TIMEBUTTONS = "#timebuttons";
  public static final String TAB_ID_PLUGINS = "#plugins";


  private JDialog mDialog;

  private JTree mSelectionTree;
  private JPanel mSettingsPn;
  //private JScrollPane mSettingsPane;
  private TreeNode mRootNode;

  /** Location of this Dialog */
  private Point mLocation = new Point();
  /** Dimension of this Dialog */
  private Dimension mSize = new Dimension();
  
  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent, String selectedTabId) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(mLocalizer.msg("settings", "Settings"));

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    mDialog.setContentPane(main);

    JSplitPane splitPane = new JSplitPane();
    main.add(splitPane, BorderLayout.CENTER);

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
    scrollPane.setMinimumSize(new Dimension(150,0));
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

    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    JButton okBt = new JButton(mLocalizer.msg("ok", "OK"));
    okBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        invalidateTree();
        mDialog.dispose();
      }
    });
    mDialog.getRootPane().setDefaultButton(okBt);
    buttonPn.add(okBt);

    JButton cancelBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mDialog.dispose();
      }
    });
    buttonPn.add(cancelBt);

    JButton applyBt = new JButton(mLocalizer.msg("apply", "Apply"));
    applyBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        invalidateTree();
        Settings.handleChangedSettings();
        showSettingsPanelForSelectedNode();
      }
    });
    buttonPn.add(applyBt);

    mDialog.pack();
    
    if (selectedTabId == null) {

    }
    else {
      SettingNode n = findSettingNode((SettingNode)mRootNode, selectedTabId);
      if (n!=null) {
        showSettingsPanelForNode(n);
        TreePath selectedPath = new TreePath(n.getPath());
        mSelectionTree.setSelectionPath(selectedPath);
      }
      else {
        showSettingsPanelForSelectedNode();
      }
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
    ((SettingNode)mRootNode).invalidate();
  }


  private SettingNode findSettingNode(SettingNode root, String tabId) {

    if (tabId.equals(root.getId())) {
      return root;
    }
    int cnt = root.getChildCount();
    for (int i=0; i<cnt; i++) {
      SettingNode result = findSettingNode((SettingNode)root.getChildAt(i), tabId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent) {
    this(parent, null);
  }


  public void centerAndShow() {
    if ((Settings.propSettingsWindowX.getInt() == -1) && (Settings.propSettingsWindowY.getInt() == -1)) {
      mDialog.pack();
      UiUtilities.centerAndShow(mDialog);
    } else if ((Settings.propSettingsWindowWidth.getInt() == -1) && (Settings.propSettingsWindowWidth.getInt() == -1)) {
      mDialog.pack();
      mLocation = new Point(Settings.propSettingsWindowX.getInt(), Settings.propSettingsWindowY.getInt());
      mDialog.setLocation(mLocation);
      mDialog.setVisible(true);
    }
    else {
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

    icon = new ImageIcon("imgs/Preferences16.gif");
    msg = mLocalizer.msg("settings", "Settings");
    SettingNode root = new SettingNode(new DefaultSettingsTab(msg,icon));

    // Channels
    node = new SettingNode(new ChannelsSettingsTab());
    root.add(node);

    node = new SettingNode(new ChannelGroupSettingsTab());
    root.add(node);

    

    ContextmenuSettingsTab contextmenuSettingsTab=new ContextmenuSettingsTab();
    PluginSettingsTab pluginSettingsTab=new PluginSettingsTab(this);

    // Appearance
    node = new SettingNode(
    new DefaultSettingsTab(mLocalizer.msg("appearance","appearance"),null));
    root.add(node);

    node.add(new SettingNode(new ButtonsSettingsTab(), TAB_ID_TIMEBUTTONS));
    node.add(new SettingNode(new LookAndFeelSettingsTab()));
    node.add(new SettingNode(new FontsSettingsTab()));
    node.add(new SettingNode(new ProgramTableSettingsTab()));
    node.add(new SettingNode(new ProgramPanelSettingsTab()));
    node.add(new SettingNode(contextmenuSettingsTab));
 //   node.add(new SettingNode(new ToolbarSettingsTab(),TAB_ID_TOOLBAR));

    // Plugins
    node = new SettingNode(pluginSettingsTab, TAB_ID_PLUGINS);
    root.add(node);

    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getAllPlugins();

    Arrays.sort(pluginArr, new Comparator() {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }

    });

    for (int i = 0; i < pluginArr.length; i++) {
      ConfigPluginSettingsTab tab = new ConfigPluginSettingsTab(pluginArr[i]);
      node.add(new SettingNode(tab, pluginArr[i].getId()));
    }

    // TVDataServices
    node = new SettingNode(new DataServiceSettingsTab());
    root.add(node);
    TvDataServiceProxy[] services=tvbrowser.core.tvdataservice.TvDataServiceProxyManager.getInstance().getDataServices();
    for (int i=0;i<services.length;i++) {
      node.add(new SettingNode(new ConfigDataServiceSettingsTab(services[i])));
    }

    // Advanced
    node = new SettingNode(new DefaultSettingsTab(mLocalizer.msg("advanced","advanced"),null));
    root.add(node);

    node.add(new SettingNode(new ProxySettingsTab()));
    node.add(new SettingNode(new DirectoriesSettingsTab()));
    node.add(new SettingNode(new TVDataSettingsTab()));

    return root;
  }

  /**
   * Returns the current Dialog
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
      mTitle=title;
      mIcon=icon;
    }

    public JPanel createSettingsPanel() {
      JPanel contentPanel=new JPanel();

      contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
      contentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
      JLabel titleLb=new JLabel(mTitle);
      titleLb.setFont(new Font("Dialog",Font.PLAIN,32));
      JLabel lb=new JLabel(mLocalizer.msg("selectCategory","Please select a category on the left."));
      lb.setFont(new Font("Dialog",Font.PLAIN,14));
      lb.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));


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
          ((ConfigPluginSettingsTab)mSettingsTab).invalidate();
      }

      mSettingsPn = null;
      Enumeration e = children();
      while (e.hasMoreElements()) {
        SettingNode node = (SettingNode)e.nextElement();
        node.invalidate();
      }
    }

    public void saveSettings() {
      if (isLoaded()) {
        mSettingsTab.saveSettings();
      }
    }



    public JPanel getSettingsPanel() {
      if (! isLoaded()) {
        if (mSettingsTab!=null) {
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

    public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int rowIndex, boolean hasFocus)
    {
      JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel,
        expanded, leaf, rowIndex, hasFocus);

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

}
