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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.exc.ErrorHandler;
import util.io.ZipUtil;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * The Dialog for the Translation-Tool
 * 
 * Attention:   This Plugin uses some Core-Stuff, but "normal" Plugins are not allowed
 *              to do this !
 * 
 * @author bodum
 */
public class TranslationDialog extends JDialog implements WindowClosingIf{
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TranslationDialog.class);

  private static final String EDITOR = "EDITOR"; 
  private static final String HELP = "HELP";

  private JTree mTree;

  private PropertiesTreeCellRenderer mTreeRenderer;

  private TranslatorEditor mEditor;

  private PathNode mRoot; 
  
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

    final JComboBox languageSelection = new JComboBox(new Locale[] {Locale.GERMAN, new Locale("sv")}); 

    languageSelection.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mTreeRenderer.setCurrentLocale((Locale) languageSelection.getSelectedItem());
        mEditor.save();
        mEditor.setCurrentLocale((Locale) languageSelection.getSelectedItem());
        mTree.updateUI();
      }
    });
    
    languageSelection.setRenderer(new DefaultListCellRenderer() {
      public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, ((Locale)value).getDisplayName(), index, isSelected, cellHasFocus);
      };
    });
    
    panel.add(languageSelection, cc.xy(4,3));
    
    JButton newButton = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "document-new", 16));
    
    newButton.setToolTipText(mLocalizer.msg("newLanguage", "Add new language"));
    
    panel.add(newButton, cc.xy(6,3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("translate", "Translate")), cc.xyw(1,5,8));

    DefaultMutableTreeNode root = createRooNode();
    
    mTree = new JTree(root);
    mTreeRenderer = new PropertiesTreeCellRenderer(Locale.GERMAN);
    mTree.setCellRenderer(mTreeRenderer);
    
    final JSplitPane split = new JSplitPane();
    split.setLeftComponent(new JScrollPane(mTree));
    
    panel.add(split, cc.xyw(2,7,6));
    
    mEditor = new TranslatorEditor(Locale.GERMAN);
    final JPanel cardPanel = new JPanel(new CardLayout());
    
    cardPanel.add(mEditor, EDITOR);
    
    JEditorPane help = new JEditorPane("text/html", "<h1>Help</h1><p>This is the Help!</p>");
    help.setEditable(false);
    
    cardPanel.add(new JScrollPane(help), HELP);
    
    mTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        CardLayout cl = (CardLayout)(cardPanel.getLayout());

        if (mTree.getSelectionPath() != null) {
          Object node = mTree.getSelectionPath().getLastPathComponent();
          
          if (node instanceof PropertiesEntryNode) {
            mEditor.save();
            mEditor.setSelectedProperties((PropertiesEntryNode) node);
            cl.show(cardPanel, EDITOR);
          } else {
            cl.show(cardPanel, HELP);
          }
        } else {
          cl.show(cardPanel, HELP);
        }
        
      }
    });
    
    mTree.setSelectionPath(new TreePath(root));
    
    split.setRightComponent(new JScrollPane(cardPanel));
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        split.setDividerLocation(0.3);
      }
    });
    
    ButtonBarBuilder buttonbar = new ButtonBarBuilder();
    
    JButton save = new JButton(mLocalizer.msg("save","Save"));
    
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
    
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        close();
      };
    });
    
    JButton export = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "document-save-as", 16)); 
    export.setToolTipText(mLocalizer.msg("export", "Export Translations to File"));
    
    export.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        export();
      }
    });
    
    LinkButton link = new LinkButton(mLocalizer.msg("getHelp", "Get Help"), mLocalizer.msg("getHelpUrl", "http://enwiki.tvbrowser.org"));

    buttonbar.addFixed(export);
    buttonbar.addGlue();
    buttonbar.addFixed(link);
    buttonbar.addGlue();
    buttonbar.addGriddedButtons(new JButton[] {save, cancel});
    
    panel.add(buttonbar.getPanel(), cc.xyw(2,9,6));

    getRootPane().setDefaultButton(cancel);
    UiUtilities.registerForClosing(this);
    
    setSize(Sizes.dialogUnitXAsPixel(400, this), Sizes.dialogUnitYAsPixel(350, this));
  }

  /**
   * Saves the current Editorfield and saves the changes by the user to his user-directory
   */
  protected void save() {
    mEditor.save();
    try {
      mRoot.save();
    } catch (Exception e) {
      e.printStackTrace();
      ErrorHandler.handle(mLocalizer.msg("problemWhileSaving","Problems while storing translations."), e);
    }
    close();
  }

  /**
   * Opens a FileChooser and stores all User-Settings in a zip file
   */
  private void export() {
    JFileChooser fileChooser=new JFileChooser();
    fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".zip","Zip (*.zip)"));
    int retVal = fileChooser.showOpenDialog(this);
    
    if (retVal == JFileChooser.APPROVE_OPTION) {
      File f=fileChooser.getSelectedFile();
      if (f!=null) {
        
        ZipUtil zip = new ZipUtil();
        
        StringBuffer dir = new StringBuffer(Settings.getUserSettingsDirName()).append(File.separatorChar).append("lang").append(File.separatorChar);
        
        try {
          zip.zipDirectory(f, new File(dir.toString()));
          JOptionPane.showMessageDialog(this, mLocalizer.msg("exportDone", "Export Done!"));
        } catch (IOException e) {
          e.printStackTrace();
          ErrorHandler.handle(mLocalizer.msg("exportFailure", "Error while saving zip file."), e);
        }
        
      }
    }
    
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
    mRoot = new PathNode(mLocalizer.msg("translations", "Translations"));
    mRoot.add(new TranslationNode("TV-Browser", new File("tvbrowser.jar")));
    
    PathNode plugins = new PathNode("Plugins");

    addJarFiles(plugins, new File(Settings.propPluginsDirectory.getString()));
    addJarFiles(plugins, new File(PluginProxyManager.PLUGIN_DIRECTORY));
    addJarFiles(plugins, new File(TvDataServiceProxyManager.PLUGIN_DIRECTORY));
    
    mRoot.add(plugins);
    
    return mRoot;
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

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    setVisible(false);
  }

}