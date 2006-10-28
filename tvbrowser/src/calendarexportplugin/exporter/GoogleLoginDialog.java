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
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin.exporter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login-Dialog for Google
 * 
 * @author bodum
 */
public class GoogleLoginDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GoogleLoginDialog.class);
 
  /** Which Button was pressed ?*/
  private int mReturnValue = JOptionPane.CANCEL_OPTION;
  
  private String mUsername;
  private String mPassword;
  private boolean mStore;

  private JTextField mNameField;
  private JPasswordField mPasswordField;
  private JCheckBox mStorePassword;

  /**
   * Create Dialog
   * @param owner Parent-Dialog
   * @param username Username
   * @param password Password
   * @param storePassword store password ?
   */
  public GoogleLoginDialog(Dialog owner, String username, String password, boolean storePassword) {
    super(owner, true);
    mUsername = username;
    mPassword = password;
    mStore = storePassword;
    createGui();
  }

  /**
   * Create Dialog
   * @param owner Parent-Frame
   * @param username Username
   * @param password Password
   * @param storePassword store password ?
   */
  public GoogleLoginDialog(Frame owner, String username, String password, boolean storePassword) {
    super(owner, true);
    mUsername = username;
    mPassword = password;
    mStore = storePassword;
    createGui();
  }
  
  /**
   * Create Gui
   */
  private void createGui() {
    setTitle(mLocalizer.msg("login", "Google Login"));

    UiUtilities.registerForClosing(this);
    
    JPanel content = (JPanel)getContentPane();
    
    content.setLayout(new FormLayout("right:pref:grow(0.5), 3dlu, 100dlu, fill:pref:grow(0.5), 5dlu", 
        "30dlu, 5dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow, pref, 5dlu"));

    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(new FormLayout("7dlu, pref, fill:pref:grow", "7dlu, center:21dlu, 2dlu, 1px"));
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    panel.setForeground(Color.BLACK);
    
    JLabel top = new JLabel(mLocalizer.msg("title", "Google Calendar Login"));
    top.setFont(top.getFont().deriveFont(Font.BOLD, 20));
    
    panel.add(top, cc.xy(2,2));
    JPanel black = new JPanel();
    black.setBackground(Color.BLACK);
    panel.add(black, cc.xyw(1,4,3));
    
    content.add(panel, cc.xyw(1,1,5));

    JLabel name = new JLabel(mLocalizer.msg("user", "Username")+":");
    content.add(name, cc.xy(1, 3));
    
    mNameField = new JTextField();
    content.add(mNameField, cc.xy(3,3));

    mNameField.setText(mUsername);
    
    JLabel password = new JLabel(mLocalizer.msg("password", "Password")+":");
    content.add(password, cc.xy(1, 5));
    
    mPasswordField = new JPasswordField();
    content.add(mPasswordField, cc.xy(3,5));

    mPasswordField.setText(mPassword);
    
    mStorePassword = new JCheckBox(mLocalizer.msg("storePassword", "Store Password"));
    content.add(mStorePassword, cc.xy(3,7));
    
    mStorePassword.setSelected(mStore);
    
    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mReturnValue = JOptionPane.OK_OPTION;
        setVisible(false);
      }
    });
    
    getRootPane().setDefaultButton(ok);
    
    JButton cancel = new JButton (Localizer.getLocalization(Localizer.I18N_CANCEL));
    
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    builder.addGriddedButtons(new JButton[] {ok, cancel});

    content.add(builder.getPanel(), cc.xyw(1,9,4));
    
    setSize(Sizes.dialogUnitXAsPixel(200, this), Sizes.dialogUnitYAsPixel(140, this));
  }

  /**
   * Dialog was closed
   */
  public void close() {
    mReturnValue = JOptionPane.CANCEL_OPTION;
    setVisible(false);
  }  
  
  /**
   * Show the Dialog
   * @return Which Button was pressed ? (JOptionpane.OK_OPTION / CANCEL_OPTION)
   */
  public int askLogin() {
    UiUtilities.centerAndShow(this);
    return mReturnValue;
  }
 
  public String getUsername() {
    return mNameField.getText();
  }
  
  public String getPassword() {
    return new String(mPasswordField.getPassword());
  }
  
  public boolean storePasswords() {
    return mStorePassword.isSelected();
  }
  
}