/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package twitterplugin;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;

public final class TwitterDialog extends JDialog implements WindowClosingIf {
  private static final int TWEET_MAX_CHARS = 140;
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterDialog.class);
  private JTextArea mMessage;
  private boolean mOkWasPressed = false;

  public TwitterDialog(final Window parentDialog, final Program program, final String format) {
    super(parentDialog);
    setModal(true);
    initialize(parentDialog, program, format);
  }

  private void initialize(final Window parent, final Program program, final String format) {
    setTitle(mLocalizer.msg("title", "Enter Twitter Message"));
    createGui(program, format);
    setLocationRelativeTo(parent);
  }

  private void createGui(final Program program, final String format) {
    final JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    final FormLayout layout = new FormLayout("3dlu, fill:min:grow, 3dlu");
    panel.setLayout(layout);

    int currentRow = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    final CellConstraints cc = new CellConstraints();
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("message", "Twitter Message")), cc
        .xyw(1, currentRow, 3));

    layout.appendRow(RowSpec.decode("fill:pref:grow"));
    layout.appendRow(RowSpec.decode("5dlu"));

    mMessage = new JTextArea();
    mMessage.setLineWrap(true);
    mMessage.setWrapStyleWord(true);
    panel.add(new JScrollPane(mMessage), cc.xy(2, currentRow += 2));

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    final JLabel warningCounter = new JLabel("");
    panel.add(warningCounter, cc.xy(2, currentRow += 2));

    final ButtonBarBuilder buttonBar = new ButtonBarBuilder();
    buttonBar.addGlue();

    final JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        okPressed();
      }
    });
    final JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        close();
      }
    });

    buttonBar.addGriddedButtons(new JButton[] { ok, cancel });

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    panel.add(buttonBar.getPanel(), cc.xyw(1, currentRow += 2, 3));

    setSize(Sizes.dialogUnitXAsPixel(200, this), Sizes.dialogUnitYAsPixel(200, this));

    UiUtilities.registerForClosing(this);
    getRootPane().setDefaultButton(cancel);

    mMessage.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(final DocumentEvent e) {
        updateWarning();
      }

      public void removeUpdate(final DocumentEvent e) {
        updateWarning();
      }

      public void changedUpdate(final DocumentEvent e) {
        updateWarning();
      }

      private void updateWarning() {
        final int num = TWEET_MAX_CHARS - mMessage.getText().trim().length();
        warningCounter.setText(mLocalizer.msg("counter", "{0} chars left", num));
        if (num < 0) {
          warningCounter.setForeground(Color.RED);
        } else {
          warningCounter.setForeground(new JLabel().getForeground());
        }
        ok.setEnabled(num >= 0 && num != TWEET_MAX_CHARS);
      }

    });
    mMessage.setText(new ParamParser().analyse(format, program));
  }

  private void okPressed() {
    setVisible(false);
    mOkWasPressed = true;
  }

  public void close() {
    setVisible(false);
  }

  public boolean wasOkPressed() {
    return mOkWasPressed;
  }

  public String getMessage() {
    return mMessage.getText().trim();
  }
}
