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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.Settings;
import util.ui.UiUtilities;
import devplugin.SettingsTab;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class SettingsDialog {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SettingsDialog.class);

  private JDialog mDialog;

  private JSplitPane mSplitPane;
  private JTree mSelectionTree;
  private JPanel mSettingsPn;
  private JScrollPane mSettingsPane;

  private JButton mOkBt, mCancelBt, mApplyBt;



  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent, String selectedTabId) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(mLocalizer.msg("settings", "Settings"));

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    mDialog.setContentPane(main);

    mSplitPane = new JSplitPane();
    main.add(mSplitPane, BorderLayout.CENTER);

    TreeNode root = createSelectionTree();
    mSelectionTree = new JTree(root);
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
    mSplitPane.setLeftComponent(scrollPane);

    // Make the viewport as big as the tree when all nodes are expanded
    int categoryCount = root.getChildCount();
    for (int i = categoryCount; i >= 1; i--) {
      mSelectionTree.expandRow(i);
    }
    scrollPane.getViewport().setPreferredSize(mSelectionTree.getPreferredSize());
    // Let the tree collapse
    for (int i = 1; i <= categoryCount; i++) {
      mSelectionTree.collapseRow(i);
    }

    mSettingsPn = new JPanel(new BorderLayout());

    // TODO: do we need a JScrollPane here?
    mSettingsPane = new JScrollPane(mSettingsPn);
    mSettingsPane.setPreferredSize(new Dimension(410, 300));
    mSplitPane.setRightComponent(mSettingsPane);
 //   mSplitPane.setRightComponent(mSettingsPn);
 //  mSettingsPn.setPreferredSize(new Dimension(410, 300));

    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    mOkBt = new JButton(mLocalizer.msg("ok", "OK"));
    mOkBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        mDialog.dispose();
      }
    });
    mDialog.getRootPane().setDefaultButton(mOkBt);
    buttonPn.add(mOkBt);

    mCancelBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mDialog.dispose();
      }
    });
    buttonPn.add(mCancelBt);

    mApplyBt = new JButton(mLocalizer.msg("apply", "Apply"));
    mApplyBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveSettings();
        Settings.handleChangedSettings();
      }
    });
    buttonPn.add(mApplyBt);

    mDialog.pack();

    if (selectedTabId == null) {

    }
    else {
      SettingNode n = findSettingNode((SettingNode)root, selectedTabId);
      if (n!=null) {
        showSettingsPanelForNode(n);
        TreePath selectedPath = new TreePath(n.getPath());
        mSelectionTree.setSelectionPath(selectedPath);
      }
      else {
        showSettingsPanelForSelectedNode();
      }
    }
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
    UiUtilities.centerAndShow(mDialog);
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

    node = new SettingNode(new ChannelsSettingsTabNew());
    root.add(node);

    node = new SettingNode(new ChannelsSettingsTabNew2());
    root.add(node);


    ContextmenuSettingsTab contextmenuSettingsTab=new ContextmenuSettingsTab();
    PluginSettingsTab pluginSettingsTab=new PluginSettingsTab();
    //pluginSettingsTab.addSettingsChangeListener(contextmenuSettingsTab);

    // Appearance
    node = new SettingNode(
    new DefaultSettingsTab(mLocalizer.msg("appearance","appearance"),null));
    root.add(node);

    node.add(new SettingNode(new ButtonsSettingsTab()));
    node.add(new SettingNode(new LookAndFeelSettingsTab()));
    node.add(new SettingNode(new FontsSettingsTab()));
    node.add(new SettingNode(new ProgramTableSettingsTab()));
    node.add(new SettingNode(new ProgramPanelSettingsTab()));
    node.add(new SettingNode(contextmenuSettingsTab));
    node.add(new SettingNode(new ToolbarSettingsTab(),"#toolbar"));

    // Plugins
    node = new SettingNode(pluginSettingsTab);
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
      pluginSettingsTab.addSettingsChangeListener(tab);
    }

    // TVDataServices
    node = new SettingNode(new DataServiceSettingsTab());
    root.add(node);
    tvdataservice.TvDataService[] services=tvbrowser.core.TvDataServiceManager.getInstance().getDataServices();
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
        // Make the panel as wide as the scrollpane viewport
        Dimension viewportSize = mSettingsPane.getViewport().getSize();
        Dimension pnSize = pn.getPreferredSize();
        if (pnSize.width > viewportSize.width) {
          pnSize.width = viewportSize.width;
          pn.setPreferredSize(pnSize);
        }

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




 /* private void showSettingsPanelForSelectedNode() {
    mSettingsPn.removeAll();

    TreePath selection = mSelectionTree.getSelectionPath();
    if (selection != null) {
      SettingNode node = (SettingNode) selection.getLastPathComponent();

      JPanel pn = node.getSettingsPanel();
      if (pn != null) {
        // Make the panel as wide as the scrollpane viewport
        Dimension viewportSize = mSettingsPane.getViewport().getSize();
        Dimension pnSize = pn.getPreferredSize();
        if (pnSize.width > viewportSize.width) {
          pnSize.width = viewportSize.width;
          pn.setPreferredSize(pnSize);
        }

        mSettingsPn.add(pn);
      }
    }

    mSettingsPn.revalidate();
    mSettingsPn.repaint();
  } */


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
