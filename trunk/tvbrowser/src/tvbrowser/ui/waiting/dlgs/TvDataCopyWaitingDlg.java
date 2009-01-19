package tvbrowser.ui.waiting.dlgs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This dialog is shown when TV data is copied or imported.
 * It blocks all other dialogs.
 * 
 * @author René Mach
 * @since 2.2.2/2.5.1
 */
public class TvDataCopyWaitingDlg extends JDialog {
  
  /**
   * Creates an instance of this class.
   * 
   * @param parent The parent dialog for this dialog.
   * @param copy If the message should contains copy for <code>true</code> or import instead if it is <code>false</code>.
   */
  public TvDataCopyWaitingDlg(Window parent, boolean copy) {
    super(parent);
    setModal(true);
    createGUI(parent, copy);
  }

  private void createGUI(Window parent, boolean copy) {
    setUndecorated(true);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    
    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    panel.setLayout(new FormLayout("3dlu, pref, 3dlu", "3dlu, pref, 3dlu, pref, 3dlu"));
    CellConstraints cc = new CellConstraints();

    Localizer localizer = Localizer.getLocalizerFor(TvDataCopyWaitingDlg.class);
    
    String msg = localizer.msg("waitingHeader", "Importing TV data (this can need some minutes).");
    
    if(copy)
      msg = localizer.msg("waitingHeaderCopy", "Copying TV data (this can need some minutes).");
    
    JLabel header = new JLabel(msg);
    header.setFont(header.getFont().deriveFont(Font.BOLD));

    panel.add(header, cc.xy(2, 2));

    panel.add(new JLabel(localizer.msg("pleaseWait", "Please wait until the TV data were copied.")), cc.xy(2, 4));

    pack();
    setLocationRelativeTo(parent);
  }
}
