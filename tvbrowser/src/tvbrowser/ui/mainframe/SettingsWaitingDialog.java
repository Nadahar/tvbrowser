package tvbrowser.ui.mainframe;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SettingsWaitingDialog extends JDialog {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SettingsWaitingDialog.class);

  public SettingsWaitingDialog(JDialog dialog) {
    super(dialog, true);
    createGui();
  }

  public SettingsWaitingDialog(JFrame frame) {
    super(frame, true);
    createGui();
  }
  
  private void createGui () {
    setUndecorated(true);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    panel.setLayout(new FormLayout("3dlu, pref, 3dlu", "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
    CellConstraints cc = new CellConstraints();

    JLabel header = new JLabel(mLocalizer.msg("waitingHeader", "Listing the not subscribed channels"));
    header.setFont(header.getFont().deriveFont(Font.BOLD));

    panel.add(header, cc.xy(2, 2));

    panel.add(new JLabel(mLocalizer.msg("pleaseWait", "Please wait for the completing of the list.")), cc.xy(2, 4));

    pack();
  }

}
