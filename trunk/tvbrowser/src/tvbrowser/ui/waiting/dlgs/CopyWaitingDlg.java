package tvbrowser.ui.waiting.dlgs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This dialog is shown when something is copied or imported.
 * It blocks all other dialogs.
 * 
 * @author René Mach
 * @since 2.2.2/2.5.1
 */
public class CopyWaitingDlg extends JDialog {
  /** The message key for copy messages */
  public static final byte COPY_MSG = 0;
  /** The message key for import messages */
  public static final byte IMPORT_MSG = 1;
  /** The message key for appdata messages */
  public static final byte APPDATA_MSG = 2;
  /** The message key for settings import messages */
  public static final byte IMPORT_SETTINGS_MSG = 3;
  /** The message key for settings export */
  public static final byte EXPORT_SETTINGS_MSG = 4;
  
  /**
   * Creates an instance of this class.
   * 
   * @param parent The parent dialog for this dialog.
   * @param messageType The message type for this dialog.
   */
  public CopyWaitingDlg(Window parent, byte messageType) {
    super(parent);
    setModal(true);
    createGUI(parent, messageType);
  }

  private void createGUI(Window parent, byte messageType) {
    setUndecorated(true);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    
    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    panel.setLayout(new FormLayout("3dlu, pref, 3dlu", "3dlu, pref, 3dlu, pref, 3dlu"));
    CellConstraints cc = new CellConstraints();

    Localizer localizer = Localizer.getLocalizerFor(CopyWaitingDlg.class);
    
    String msg = localizer.msg("waitingHeader", "Importing TV data (this can take some minutes).");
    JTextArea header = new JTextArea();
    header.setPreferredSize(new Dimension(370,40));
    
    if(messageType == IMPORT_MSG) {
      msg = localizer.msg("waitingHeaderCopy", "Copying TV data (this can take some minutes).");
    }
    else if(messageType == APPDATA_MSG) {
      msg = localizer.msg("appdataHeaderMsg", "Copying TV data (this can need some minutes).\nThe data and the settings are copied to the\n" +
      		"Windows appdata directory. You can delete the old TV-Browser settings directory\n if you don't use an old version of TV-Browser.");
      header.setPreferredSize(new Dimension(370,90));
    }
    else if(messageType == EXPORT_SETTINGS_MSG) {
      msg = localizer.msg("exportHeader","Copying TV data (this can need some minutes).\nThe data and the settings are copied to the\n"+
          "system settings directory.");
      header.setPreferredSize(new Dimension(370,60));
    }
    else {
      msg = localizer.msg("waitingSettingsHeader","Importing the settings of a previous version (this may take some minutes).");
    }
    
    header.setText(msg);
    header.setEditable(false);
    header.setLineWrap(true);
    header.setWrapStyleWord(true);
    header.setBorder(null);
    header.setOpaque(false);
    
    JLabel label = new JLabel(localizer.msg("pleaseWait", "Please wait until the files were copied."));
    
    header.setFont(label.getFont());
    header.setFont(header.getFont().deriveFont(Font.BOLD,(float)13));
    
    label.setFont(header.getFont());

    panel.add(header, cc.xy(2, 2));
    
    panel.add((label), cc.xy(2, 4));

    pack();
    setLocationRelativeTo(parent);
  }
}
