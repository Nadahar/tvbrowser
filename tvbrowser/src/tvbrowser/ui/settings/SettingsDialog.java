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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import util.ui.*;

import devplugin.SettingsTab;
import devplugin.Plugin;

import tvbrowser.core.PluginManager;

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
  private JButton mOkBt, mCancelBt;
  
  
  
  /**
   * Creates a new instance of SettingsDialog.
   */
  public SettingsDialog(Component parent) {
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
    mSettingsPn.setPreferredSize(new Dimension(410, 300));
    mSplitPane.setRightComponent(mSettingsPn);
    
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
        
    mDialog.pack();
    
    showSettingsPanelForSelectedNode();
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
    SettingNode root = new SettingNode(icon, msg);
    
    
    // Channels
    node = new SettingNode(new ChannelsSettingsTab());
    root.add(node);
    
    // Appearance
    
    node = new SettingNode(new AppearanceSettingsTab());
    root.add(node);
    
    node.add(new SettingNode(new ButtonsSettingsTab()));
    node.add(new SettingNode(new LookAndFeelSettingsTab()));
    node.add(new SettingNode(new FontsSettingsTab()));
    node.add(new SettingNode(new ProgramTableSettingsTab()));
    
    // Plugins
    node = new SettingNode(new PluginSettingsTab());
    root.add(node);
    
    Plugin[] pluginArr = PluginManager.getInstalledPlugins();
    for (int i = 0; i < pluginArr.length; i++) {
      node.add(new SettingNode(new ConfigPluginSettingsTab(pluginArr[i])));
    }
    
    // TVDataServices
    node = new SettingNode(new DataServiceSettingsTab());
    root.add(node);
    tvdataservice.TvDataService[] services=tvbrowser.core.TvDataServiceManager.getInstance().getDataServices();
    for (int i=0;i<services.length;i++) {
      node.add(new SettingNode(new ConfigDataServiceSettingsTab(services[i])));
    }
    
    // Advanced
    node = new SettingNode(new AdvancedSettingsTab());
    root.add(node);
    
    node.add(new SettingNode(new ProxySettingsTab()));
    node.add(new SettingNode(new DirectoriesSettingsTab()));
    node.add(new SettingNode(new TVDataSettingsTab()));
     
     
     /*       
    // General section
    
    node = new SettingNode(new TVBrowserSettingsTab());
    root.add(node);    
    node.add(new SettingNode(new ChannelsSettingsTab()));
    node.add(new SettingNode(new SkinLFSettingsTab()));
    
    
    // ProgramTable section
    
    node=new SettingNode(new ProgramTableSettingsTab());
    root.add(node);
    node.add(new SettingNode(new FontsSettingsTab()));
        
    
    
    // TV-Data section
    node = new SettingNode(new TVDataSettingsTab());
    root.add(node);

    node.add(new SettingNode(new ProxySettingsTab()));
    node.add(new SettingNode(new DirectoriesSettingsTab()));
    
    
    
    SettingNode dataServiceNode;
    dataServiceNode=new SettingNode(new DataServiceSettingsTab());
    node.add(dataServiceNode);
    
    tvdataservice.TvDataService[] services=tvbrowser.core.TvDataServiceManager.getInstance().getDataServices();
    for (int i=0;i<services.length;i++) {
      dataServiceNode.add(new SettingNode(new ConfigDataServiceSettingsTab(services[i])));
    }
    
    
    // Plugins section
    node = new SettingNode(new PluginSettingsTab());
    root.add(node);
    
    Plugin[] pluginArr = PluginManager.getInstalledPlugins();
    for (int i = 0; i < pluginArr.length; i++) {
      //SettingsTab tab = pluginArr[i].getSettingsTab();
      node.add(new SettingNode(new ConfigPluginSettingsTab(pluginArr[i])));
    
    }
    */
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

  
  
  private void showSettingsPanelForSelectedNode() {
    mSettingsPn.removeAll();

    TreePath selection = mSelectionTree.getSelectionPath();
    if (selection != null) {
      SettingNode node = (SettingNode) selection.getLastPathComponent();

      JPanel pn = node.getSettingsPanel();
      if (pn != null) {
        mSettingsPn.add(pn);
      }
    }
    
    mSettingsPn.revalidate();
    mSettingsPn.repaint();
  }
  
  
  // inner class SettingNode
  
  
  private class SettingNode extends DefaultMutableTreeNode {

    private Icon mIcon;
    private JPanel mSettingsPn;
    private SettingsTab mSettingsTab;

    
    
    public SettingNode(SettingsTab settingsTab) {
      this(settingsTab.getIcon(), settingsTab.getTitle());
      
      mSettingsTab = settingsTab;
    }
    
    
    
    public SettingNode(Icon icon, String title) {
      super(title);
      
      mIcon = icon;
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
