/*
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
package blogthisplugin;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import util.paramhandler.ParamHelpDialog;
import util.paramhandler.ParamLibrary;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

/**
 * The Dialog for the Extended Settings.
 * 
 * This Settings should not be visible in the Settings-Tab. They are too complicated 
 * 
 * @author bodum
 */
public class ExtendedDialog extends JDialog {
  /** Translator */
  private static final Localizer mLocalizer = Localizer
          .getLocalizerFor(ExtendedDialog.class);

  /** Settings for the Plugin */
  private Properties mSettings;
  /** Content-Text */
  private JTextArea mContent;
  /** Title-Text */
  private JTextField mTitle;
  
  /**
   * Creates the Dialog
   * @param frame Parent-Frame
   * @param settings Settings of this Plugin
   */
  public ExtendedDialog(JFrame frame, Properties settings) {
    super(frame, mLocalizer.msg("settings", "Extended Settings"), true);
    mSettings = settings;
    createGui();
  }

  /**
   * Creates the Dialog
   * @param dialog Parent-Dialog
   * @param settings Settings of this Plugin
   */
  public ExtendedDialog(JDialog dialog, Properties settings) {
    super(dialog, mLocalizer.msg("settings", "Extended Settings"), true);
    mSettings = settings;
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    JPanel panel = (JPanel) getContentPane();
    
    panel.setLayout(new FormLayout("pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, 3dlu, fill:default:grow, 3dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    JLabel title = new JLabel(mLocalizer.msg("title", "Title")+":");
    mTitle = new JTextField();
    mTitle.setText(mSettings.getProperty("Title", BlogThisPlugin.DEFAULT_TITLE));
    mTitle.setCaretPosition(0);
    
    panel.add(title, cc.xy(1, 1));
    panel.add(mTitle, cc.xy(3, 1));
    
    JLabel content = new JLabel(mLocalizer.msg("content", "Content")+":");
    
    panel.add(content, cc.xy(1, 3));
    
    mContent = new JTextArea();
    mContent.setText(mSettings.getProperty("Content", BlogThisPlugin.DEFAULT_CONTENT));
    mContent.setCaretPosition(0);
    
    panel.add(new JScrollPane(mContent), cc.xyw(1, 5, 3));
    
    ButtonBarBuilder builder = new ButtonBarBuilder();

    JButton preview = new JButton(mLocalizer.msg("preview", "Preview"));
    preview.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showPreview();
      }
    });
    
    JButton help = new JButton(mLocalizer.msg("help", "Help"));
    help.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ParamHelpDialog dialog = new ParamHelpDialog(ExtendedDialog.this, new ParamLibrary());
        dialog.setVisible(true);
      }      
    });
    
    JButton def = new JButton(mLocalizer.msg("default", "Default")); 
    def.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        defaultPressed();
      }
    });
    
    builder.addGriddedButtons(new JButton[]{preview, def, help});
    
    builder.addGlue();
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed();
      }
    });
    
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }
    });
    
    builder.addGriddedButtons(new JButton[]{ok, cancel});
    
    panel.add(builder.getPanel(), cc.xyw(1, 7, 3));
    
    panel.setBorder(Borders.DLU4_BORDER);
    
    getRootPane().setDefaultButton(ok);
    
    setSize(550, 400);
  }

  /**
   * Show a Preview of the HTML that will be generated
   */
  protected void showPreview() {
    ParamParser parser = new ParamParser();
    String title  = parser.analyse(mTitle.getText(), BlogThisPlugin.getPluginManager().getExampleProgram()).trim();
    String content = parser.analyse(mContent.getText(), BlogThisPlugin.getPluginManager().getExampleProgram()).trim();
    
    final JDialog dialog = new JDialog(this, mLocalizer.msg("preview", "Preview"), true);
    JPanel contentPanel = (JPanel) dialog.getContentPane();
    
    contentPanel.setLayout(new FormLayout("fill:default:grow, pref", "fill:default:grow, 3dlu, pref"));
    contentPanel.setBorder(Borders.DLU4_BORDER);
    
    JEditorPane example = new JEditorPane("text/html", "<h1>"+title+"</h1>\n"+content.replaceAll("\n", "<br>\n")+"<br>"+mLocalizer.msg("yourText", "Your Text goes here..."));
    example.setEditable(false);
    example.setCaretPosition(0);
    
    CellConstraints cc = new CellConstraints();
    
    contentPanel.add(new JScrollPane(example), cc.xyw(1, 1, 2));
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    dialog.getRootPane().setDefaultButton(ok);
    
    contentPanel.add(ok, cc.xy(2, 3));
    
    dialog.setSize(500, 400);
    UiUtilities.centerAndShow(dialog);
  }

  /**
   * Default was pressed.
   * The Settings will be set to default-values after a confirm dialog
   */
  protected void defaultPressed() {
    int ret = JOptionPane.showConfirmDialog(ExtendedDialog.this, 
        mLocalizer.msg("reset", "Reset to default Settings?"), 
        mLocalizer.msg("resetTitle", "Default"), JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.YES_OPTION) {
      mTitle.setText(BlogThisPlugin.DEFAULT_TITLE);
      mContent.setText(BlogThisPlugin.DEFAULT_CONTENT);
    }
  }
  
  /**
   * Cancel was pressed
   */
  private void cancelPressed() {
    setVisible(false);
  }

  /**
   * OK was pressed, the Settings will be saved
   */
  private void okPressed() {
    if (!mTitle.getText().trim().equals(BlogThisPlugin.DEFAULT_TITLE.trim())) {
      mSettings.setProperty("Title", mTitle.getText().trim());
    } else {
      mSettings.remove("Title");
    }
    
    if (!mContent.getText().trim().equals(BlogThisPlugin.DEFAULT_CONTENT.trim())) {
      mSettings.setProperty("Content", mContent.getText().trim());
    } else {
      mSettings.remove("Content");
    }
    
    setVisible(false);
  }
 
}