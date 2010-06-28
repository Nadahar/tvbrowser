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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package webplugin;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import util.paramhandler.ParamCheckDialog;
import util.paramhandler.ParamHelpDialog;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Dialog for editing the WebAddress
 */
public class WebAddressEditDialog extends JDialog {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WebAddressEditDialog.class);

  /** The Address to Edit */
  private WebAddress mWebAddress;

  /** Name for the Address */
  private JTextField mName;

  /** Url for the Address */
  private JTextArea mUrl;

  /** The return-value for this Dialog */
  private int returnValue;
  
  private JButton okButton;

  /**
   * Creates the Dialog
   * 
   * @param parent Parent
   * @param adr Address to edit
   */
  public WebAddressEditDialog(Window parent, WebAddress adr) {
    super(parent);
    setModal(true);
    mWebAddress = adr;

    createGui();
  }

  /**
   * creates the Gui
   */
  private void createGui() {

    setTitle(mLocalizer.msg("EditWebAddress", "Edit WebAddress"));

    final JPanel panel = (JPanel) getContentPane();

    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("default, 3dlu, default:grow, 3dlu, default",
        "default, 3dlu, default, 3dlu, default, fill:default:grow, 3dlu, default"));

    CellConstraints cc = new CellConstraints();

    JLabel nameLabel = new JLabel(mLocalizer.msg("Name", "Name") + ":");
    panel.add(nameLabel, cc.xy(1, 1));

    mName = new JTextField();
    mName.setText(mWebAddress.getName());
    mName.addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
			keysReleased();
		}
    });

    panel.add(mName, cc.xyw(3, 1, 3));

    panel.add(new JLabel(mLocalizer.msg("Url", "Url") + ":"), cc.xy(1, 3));

    mUrl = new JTextArea();
    mUrl.addKeyListener(new KeyAdapter() {

      public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
          ke.consume();
        }
      }
	@Override
	public void keyReleased(KeyEvent e) {
		keysReleased();
	}
      
    });

    mUrl.setText(mWebAddress.getUrl());
    mUrl.setLineWrap(true);
    
    JScrollPane scroll = new JScrollPane(mUrl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    panel.add(scroll, cc.xywh(3, 3, 1, 4));

    JButton test = new JButton(mLocalizer.msg("check", "Check"));
    
    test.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(panel);
        
        ParamCheckDialog dialog;
        dialog = new ParamCheckDialog(bestparent, mUrl.getText());
        dialog.setVisible(true);
      }
      
    });
        
    panel.add(test, cc.xy(5, 3));

    JButton help = new JButton(Localizer.getLocalization(Localizer.I18N_HELP));
    
    help.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(panel);
        
        ParamHelpDialog dialog;
        dialog = new ParamHelpDialog(bestparent);
        dialog.setVisible(true);
      }
      
    });
    
    panel.add(help, cc.xy(5, 5));

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        okPressed();
      }

    });

    JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }

    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel, cc.xyw(1, 8, 5));

    getRootPane().setDefaultButton(okButton);

    pack();

    if (getSize().width < 600) {
      Dimension dim = getSize();
      dim.width = 600;
      setSize(dim);
    }

    if (getSize().height < 300) {
      Dimension dim = getSize();
      dim.height = 300;
      setSize(dim);
    }

    keysReleased();
  }

  private void keysReleased() {
	  okButton.setEnabled((mName.getText().trim().length() > 0) && (mUrl.getText().trim().length() > 0));
  }

/**
   * OK was pressed
   */
  private void okPressed() {
    mWebAddress.setName(mName.getText());
    mWebAddress.setUrl(mUrl.getText());
    returnValue = JOptionPane.OK_OPTION;
    setVisible(false);
  }

  /**
   * Cancel was pressed
   */
  private void cancelPressed() {
    returnValue = JOptionPane.CANCEL_OPTION;
    setVisible(false);
  }

  /**
   * Returns the Button that was pressed (JOptionPane.OK_OPTION /
   * JOptionPane.CANCEL_OPTION)
   * 
   * @return Button that was pressed
   */
  public int getReturnValue() {
    return returnValue;
  }
}