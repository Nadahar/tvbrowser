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
 *     $Date: 2007-09-21 08:08:54 +0200 (Fr, 21 Sep 2007) $
 *   $Author: troggan $
 * $Revision: 3897 $
 */
package wirschauenplugin;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import devplugin.Program;
import util.ui.Localizer;
import util.ui.WindowClosingIf;
import util.ui.UiUtilities;

import java.awt.Toolkit;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class WirSchauenDialog extends JDialog implements WindowClosingIf {
  private static Logger mLog = java.util.logging.Logger.getLogger(WirSchauenDialog.class.getName());
  /**
   * Localizer
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WirSchauenDialog.class);

  public WirSchauenDialog(JDialog jDialog, Program program) {
    super(jDialog, true);
    createGui(program);
  }

  public WirSchauenDialog(JFrame jFrame, Program program) {
    super(jFrame, true);
    createGui(program);
  }

  private void createGui(Program program) {
    setTitle(mLocalizer.msg("title", "WirSchauen.de suggestion"));

    JPanel panel = (JPanel) getContentPane();

    panel.setBorder(Borders.DLU4_BORDER);

    panel.setLayout(new FormLayout("right:pref,3dlu, pref, fill:10dlu:grow",
            "pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:50dlu, 3dlu, pref,fill:pref:grow, pref"));

    CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("URL","omdb.org-URL") + " :"), cc.xy(1, 1));
    panel.add(new JLabel("http://www.omdb.org/movie/"), cc.xy(3, 1));
    panel.add(new JTextField(), cc.xy(4, 1));

    panel.add(new JLabel(mLocalizer.msg("genre","Genre")+" :"), cc.xy(1, 3));
    panel.add(new JTextField(), cc.xyw(3, 3, 2));

    panel.add(new JLabel(mLocalizer.msg("text","Text")+" :"), cc.xy(1, 5));
    panel.add(new JLabel(mLocalizer.msg("maxChars","(max. 200 characters)")), cc.xy(3, 5));

    final JTextArea description = new JTextArea();
    description.setDocument(new PlainDocument() {
      @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (getLength() + str.length() > 200) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          super.insertString(offs, str, a);

        }
      }
    });

    panel.add(new JScrollPane(description), cc.xyw(3, 7, 2));

    JPanel panelItems = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panelItems.add(new JCheckBox(mLocalizer.msg("subtitle","Untertitel")));
    panelItems.add(new JCheckBox(mLocalizer.msg("OwS", "OwS")));
    panelItems.add(new JCheckBox(mLocalizer.msg("premiere","Television Premiere")));

    panel.add(panelItems, cc.xyw(3,9,2));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok();
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    getRootPane().setDefaultButton(ok);

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[]{ok, cancel});

    panel.add(builder.getPanel(), cc.xyw(1,11,4));

    setSize(Sizes.dialogUnitXAsPixel(300, this),
            Sizes.dialogUnitYAsPixel(180, this));

    UiUtilities.registerForClosing(this);
  }

  private void ok() {
    setVisible(false);
  }

  public void close() {
    setVisible(false);
  }
}
