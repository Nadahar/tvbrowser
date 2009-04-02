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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.exc.ErrorHandler;
import util.io.ZipUtil;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.SingleAndDoubleClickTreeUI;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;

/**
 * The Dialog for the Translation-Tool
 * 
 * Attention:   This Plugin uses some Core-Stuff, but "normal" Plugins are not allowed
 *              to do this !
 * 
 * @author bodum
 */
final public class TranslationDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TranslationDialog.class);

  private static final String EDITOR = "EDITOR"; 
  private static final String HELP = "HELP";

  private JTree mTree;

  private PropertiesTreeCellRenderer mTreeRenderer;

  private TranslatorEditor mEditor;

  private PathNode mRoot;

  private Vector<Locale> mCurrentLocales;

  private JComboBox mLanguageCB;

  private JSplitPane mSplitpane; 
  
  private JTextField mFilterTF;
  
  private JButton mClearFilterB;
  
  public TranslationDialog(final Window owner, final int splitPos) {
    super(owner);
    setModal(true);
    createGui(splitPos);
  }
  
  private void createGui(final int splitPos) {
    setTitle(mLocalizer.msg("title","Translation Tool"));

    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    
    panel.setLayout(new FormLayout("3dlu, left:pref, 3dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu", "pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 5dlu, fill:10dlu:grow, 3dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("chooseLanguage", "Choose Language")), cc.xyw(1,1,8));
    
    panel.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));

    mCurrentLocales = new Vector<Locale>();
    Locale[] locales = mLocalizer.getAllAvailableLocales();
    
    for (Locale locale : locales) {
      if (!locale.getLanguage().equals("en"))
        mCurrentLocales.add(locale);
    }
    
    mLanguageCB = new JComboBox(mCurrentLocales);
    mLanguageCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Locale loc = (Locale) mLanguageCB.getSelectedItem();
        
        mTreeRenderer.setCurrentLocale(loc);
        mEditor.save();
        mEditor.setCurrentLocale(loc);
        filterChanged(mFilterTF.getText());
        mTree.repaint();
      }
    });
    
    mLanguageCB.setRenderer(new DefaultListCellRenderer() {
      public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, ((Locale)value).getDisplayName(), index, isSelected, cellHasFocus);
      }
    });
    
    panel.add(mLanguageCB, cc.xy(4,3));
    
    JButton newButton = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "document-new", 16));
    newButton.setToolTipText(mLocalizer.msg("newLanguage", "Add new language"));
    
    newButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addLanguage();
      }
    });
    
    panel.add(newButton, cc.xy(6,3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("translate", "Translate")), cc.xyw(1,5,8));
    
    panel.add(new JLabel(mLocalizer.msg("search", "Search:")), cc.xy(2,7));

    mFilterTF = new JTextField();
    panel.add(mFilterTF, cc.xy(4,7));
    
    mClearFilterB = new JButton(IconLoader.getInstance().getIconFromTheme("action", "process-stop", 16));
    mClearFilterB.setEnabled(false);
    mClearFilterB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mFilterTF.setText(null);
      }
    });
    panel.add(mClearFilterB, cc.xy(6,7));
    
    DefaultMutableTreeNode root = createRootNode();
    
    mTree = new JTree(root) {
      public void updateUI() {
        setUI(new SingleAndDoubleClickTreeUI(SingleAndDoubleClickTreeUI.AUTO_COLLAPSE_EXPAND, getSelectionPath()));
        invalidate();
      }
    };
    
    mTreeRenderer = new PropertiesTreeCellRenderer(Locale.GERMAN);
    mTree.setCellRenderer(mTreeRenderer);
    
    mSplitpane = new JSplitPane();
    mSplitpane.setLeftComponent(new JScrollPane(mTree));
    
    panel.add(mSplitpane, cc.xyw(2,9,6));
    
    mEditor = new TranslatorEditor(Locale.GERMAN);
    final JPanel cardPanel = new JPanel(new CardLayout());
    
    cardPanel.add(mEditor, EDITOR);
    
    JEditorPane help = new JEditorPane("text/html",mLocalizer.msg("helpText", "<h1>Help missing</h1>"));
    help.setEditable(false);

    cardPanel.add(new JScrollPane(help, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), HELP);
    
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
    
    mSplitpane.setRightComponent(cardPanel);
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mSplitpane.setDividerLocation(splitPos);
      }
    });
    
    ButtonBarBuilder buttonbar = new ButtonBarBuilder();
    
    JButton save = new JButton(mLocalizer.msg("save","Save"));
    
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        close();
      }
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
    
    panel.add(buttonbar.getPanel(), cc.xyw(2,11,6));

    getRootPane().setDefaultButton(cancel);
    UiUtilities.registerForClosing(this);
    
    setSize(Sizes.dialogUnitXAsPixel(500, this), Sizes.dialogUnitYAsPixel(410, this));
    
    mLanguageCB.setSelectedIndex(0);
    
    mFilterTF.getDocument().addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
        filterChanged(mFilterTF.getText());
      }
      public void insertUpdate(DocumentEvent e) {
        filterChanged(mFilterTF.getText());
      }
      public void changedUpdate(DocumentEvent e) {
        filterChanged(mFilterTF.getText());
      }
    });

  }

  /**
   * Shows a Dialog with a List of all available Languages
   * and adds a selected Language
   */
  protected void addLanguage() {
    LanguageChooser dialog = new LanguageChooser(this, mCurrentLocales);
    UiUtilities.centerAndShow(dialog);
    
    Locale locale = dialog.getSelectedLocale();
    
    if (locale != null) {
      mCurrentLocales.add(locale);
      
      Collections.sort(mCurrentLocales, new Comparator<Locale>() {
        public int compare(Locale o1, Locale o2) {
          return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
      });
      
      StringBuilder buffer = new StringBuilder(Settings
          .getUserSettingsDirName());
      buffer.append("/lang/tvbrowser/tvbrowser");

      buffer.append('_').append(locale.getLanguage());
      
      if (locale.getCountry().length() > 0)
        buffer.append('_').append(locale.getCountry());

      if (locale.getVariant().length() > 0)
        buffer.append('_').append(locale.getVariant());

      buffer.append(".properties");

      try {
        File file = new File(buffer.toString());
        file.getParentFile().mkdirs();
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      mLanguageCB.setModel(new DefaultComboBoxModel(mCurrentLocales));
      mLanguageCB.setSelectedItem(dialog.getSelectedLocale());
    }
  }

  /**
   * Saves the current editor field and stores the changes of the user to his user directory
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
    int retVal = fileChooser.showSaveDialog(this);
    
    if (retVal == JFileChooser.APPROVE_OPTION) {
      File f=fileChooser.getSelectedFile();
      if (f!=null) {
        
        ZipUtil zip = new ZipUtil();
        
        StringBuilder dir = new StringBuilder(Settings.getUserSettingsDirName())
            .append(File.separatorChar).append("lang").append(
                File.separatorChar);
        
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
   * @return new RootNOde
   */
  private DefaultMutableTreeNode createRootNode() {
    mRoot = new PathNode(mLocalizer.msg("translations", "Translations"));
    File jar = new File("tvbrowser.jar");

    if (!jar.exists()) {
      URL url = Channel.class.getProtectionDomain().getCodeSource().getLocation();
      jar = new File(url.getFile());
    }

    if (!jar.exists()) {
      JOptionPane.showConfirmDialog(null, "Jar not found : " + jar.getAbsolutePath());
    }

    mRoot.add(new TranslationNode("TV-Browser", jar));
    
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
   * @param treenode Add to this TreeNode
   * @param dir add all Jars in this directory
   */
  private void addJarFiles(DefaultMutableTreeNode treenode, File dir) {
    File[] files = dir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
          return pathname.getName().toLowerCase().endsWith(".jar");
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

  /**
   * @return Location of the Devider
   */
  public int getDeviderLocation() {
    return mSplitpane.getDividerLocation();
  }
  
  /**
   * Triggers the filtering of the tree.
   * 
   * @param filterText
   */
  private void filterChanged(final String filterText) {
    SwingUtilities.invokeLater(new Runnable() {
    
      public void run() {
        String filter = filterText;
        if (filter.trim().length() == 0) {
          filter = null;
        }
        mClearFilterB.setEnabled(filter != null);
        if (mRoot != null && mLanguageCB != null) {
          DefaultTreeModel model = (DefaultTreeModel) mTree.getModel();

          TreePath[] selectionPaths = mTree.getSelectionPaths();
          Enumeration<TreePath> expandedDescendants = mTree.getExpandedDescendants(new TreePath(model.getRoot()));

          mRoot.setFilter((Locale) mLanguageCB.getSelectedItem(), filter);
          model.reload();

          while (expandedDescendants.hasMoreElements()) {
            mTree.expandPath(expandedDescendants.nextElement());
          } // while

          mTree.setSelectionPaths(selectionPaths);
        }
      }
    });
  }

}