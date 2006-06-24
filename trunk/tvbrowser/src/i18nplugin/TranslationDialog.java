/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.ui.LinkButton;
import util.ui.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The Dialog for the Translation-Tool
 * 
 * Attention:   This Plugin uses some Core-Stuff, but "normal" Plugins are not allowed
 *              to do this !
 * 
 * @author bodum
 */
public class TranslationDialog extends JDialog {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TranslationDialog.class);

  public TranslationDialog(JDialog owner) {
    super(owner, true);
    createGui();
  }
  
  public TranslationDialog(JFrame owner) {
    super(owner, true);
    createGui();
  }

  private void createGui() {
    setTitle(mLocalizer.msg("title","Translation Tool"));

    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    
    panel.setLayout(new FormLayout("3dlu, left:pref, 3dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu", "pref, 5dlu, pref, 3dlu, pref, 5dlu, fill:pref:grow, 3dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("chooseLanguage", "Choose Language")), cc.xyw(1,1,8));
    
    panel.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));
    
    panel.add(new JComboBox(new String[] {"Deutsch", "Blubberlutsch"}), cc.xy(4,3));
    
    JButton newButton = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "document-new", 16));
    
    newButton.setToolTipText(mLocalizer.msg("newLanguage", "Add new language"));
    
    panel.add(newButton, cc.xy(6,3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("translate", "Translate")), cc.xyw(1,5,8));

    DefaultMutableTreeNode root = createRooNode();
    
    JTree tree = new JTree(root);
    
    JTable table = new JTable();
    
    JSplitPane split = new JSplitPane();
    split.setLeftComponent(new JScrollPane(tree));
    split.setRightComponent(new JScrollPane(table));
    
    panel.add(split, cc.xyw(2,7,6));
    
    ButtonBarBuilder buttonbar = new ButtonBarBuilder();
    
    JButton save = new JButton(mLocalizer.msg("save","Save"));
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setVisible(false);
      };
    });
    
    JButton export = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "document-save-as", 16)); 
    export.setToolTipText(mLocalizer.msg("export", "Export Translations to File"));
    
    LinkButton link = new LinkButton(mLocalizer.msg("getHelp", "Get Help"), mLocalizer.msg("getHelpUrl", "http://enwiki.tvbrowser.org"));

    buttonbar.addFixed(export);
    buttonbar.addGlue();
    buttonbar.addFixed(link);
    buttonbar.addGlue();
    buttonbar.addGriddedButtons(new JButton[] {save, cancel});
    
    panel.add(buttonbar.getPanel(), cc.xyw(2,9,6));
    
    pack();
  }

  /**
   * Creates the Root-Node for the Tree on the Left.
   * 
   * The Tree has two Nodes:
   * 
   *    1. TV-Browser
   *    2. All Plugins
   * 
   * @return
   */
  private DefaultMutableTreeNode createRooNode() {
    
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Translations");
    
    root.add(new TranslationNode("TV-Browser", new File("tvbrowser.jar")));
    
    DefaultMutableTreeNode plugins = new DefaultMutableTreeNode("Plugins");

    addJarFiles(plugins, new File(Settings.propPluginsDirectory.getString()));
    addJarFiles(plugins, new File(PluginProxyManager.PLUGIN_DIRECTORY));
    addJarFiles(plugins, new File(TvDataServiceProxyManager.PLUGIN_DIRECTORY));
    
    root.add(plugins);
    
    return root;
  }

  /**
   * Adds all Jar-Files in a Directory to a Tree
   * 
   * @param treenode
   * @param file
   */
  private void addJarFiles(DefaultMutableTreeNode treenode, File dir) {
    File[] files = dir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        
        if (pathname.getName().toLowerCase().endsWith(".jar"))
          return true;
        
        return false;
      }
    });
    
    if (files != null) {
      for (File file : files) {
        TranslationNode node = new TranslationNode(file.getName(), file);
        if (node.getChildCount() > 0)
          treenode.add(node);
      }
    }
    
  }

}
