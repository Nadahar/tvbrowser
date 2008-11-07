package twitterplugin;

import devplugin.Program;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.Frame;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import util.ui.Localizer;
import util.ui.WindowClosingIf;
import util.ui.UiUtilities;
import util.paramhandler.ParamParser;

public class TwitterDialog extends JDialog implements WindowClosingIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterDialog.class);
  private JEditorPane mMessage = new JEditorPane();
  private boolean mOkWasPressed = false;

  public TwitterDialog(Frame parentFrame, Program program) {
    super(parentFrame, true);
    setTitle(mLocalizer.msg("title", "Enter Twitter Message"));
    createGui(program);
    setLocationRelativeTo(parentFrame);
  }

  private void createGui(Program program) {
    final JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    final FormLayout layout = new FormLayout("3dlu, fill:min:grow, 3dlu");
    panel.setLayout(layout);

    int currentRow = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    final CellConstraints cc = new CellConstraints();
    panel.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("message", "Twitter Message")), cc.xyw(1, currentRow, 3));

    layout.appendRow(RowSpec.decode("fill:pref:grow"));
    layout.appendRow(RowSpec.decode("5dlu"));

    panel.add(new JScrollPane(mMessage), cc.xy(2, currentRow += 2));

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    final JLabel warningCounter = new JLabel("");
    panel.add(warningCounter, cc.xy(2, currentRow += 2));

    final ButtonBarBuilder buttonBar = new ButtonBarBuilder();
    buttonBar.addGlue();

    final JButton ok = new JButton (Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed();
      }
    });
    final JButton cancel = new JButton (Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    buttonBar.addGriddedButtons(new JButton[]{ok, cancel});

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    panel.add(buttonBar.getPanel(), cc.xyw(1, currentRow += 2, 3));

    setSize(Sizes.dialogUnitXAsPixel(200, this),
            Sizes.dialogUnitYAsPixel(200, this));

    UiUtilities.registerForClosing(this);
    getRootPane().setDefaultButton(cancel);

    mMessage.getDocument().addDocumentListener(new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {
        updateWarning();
      }

      public void removeUpdate(DocumentEvent e) {
        updateWarning();
      }

      public void changedUpdate(DocumentEvent e) {
        updateWarning();
      }

      private void updateWarning() {
        int num = 140 - mMessage.getText().trim().length();
        warningCounter.setText(mLocalizer.msg("counter", "{0} chars left", num));
        if (num < 0) {
          warningCounter.setForeground(Color.RED);
        } else {
          warningCounter.setForeground(new JLabel().getForeground());
        }
        ok.setEnabled(num >= 0);
      }

    });

    mMessage.setText(new ParamParser().analyse(TwitterPlugin.getInstance().getSettings().getProperty("paramForProgram", TwitterPlugin.DEFAULT_FORMAT), program));
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
