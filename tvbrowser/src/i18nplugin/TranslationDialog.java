package i18nplugin;

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

import tvbrowser.core.icontheme.IconLoader;
import util.ui.LinkButton;
import util.ui.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

    JTree tree = new JTree();
    
    JTable table = new JTable();
    
    JSplitPane split = new JSplitPane();
    split.setLeftComponent(new JScrollPane(tree));
    split.setRightComponent(new JScrollPane(table));
    
    panel.add(split, cc.xyw(2,7,6));
    
    ButtonBarBuilder buttonbar = new ButtonBarBuilder();
    
    JButton save = new JButton(mLocalizer.msg("save","Save"));
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    
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
  
  public static void main(String[] args) {
    new TranslationDialog((JFrame)null).setVisible(true);
    System.exit(0);
  }
  
}
