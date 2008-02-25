/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvdataservice.SettingsPanel;
import util.ui.UiUtilities;
import devplugin.AbstractTvDataService;

public class DataServiceConfigDlg implements ActionListener {

  private JDialog mDialog;

  private JButton cancelBtn, okBtn;

  private SettingsPanel configPanel;

  public DataServiceConfigDlg(Component parent, AbstractTvDataService dataService) {
    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle("Configure " + dataService.getInfo().getName());

    JPanel contentPane = (JPanel) mDialog.getContentPane();

    contentPane.setLayout(new BorderLayout());

    configPanel = dataService.getSettingsPanel();
    if (configPanel != null) {
      contentPane.add(configPanel, BorderLayout.NORTH);
    } else {
      contentPane.add(new JLabel("no config pane available"), BorderLayout.CENTER);
    }

    JPanel pushButtonPanel = new JPanel();

    if (configPanel != null) {
      okBtn = new JButton("OK");
      okBtn.addActionListener(this);
      pushButtonPanel.add(okBtn);
      mDialog.getRootPane().setDefaultButton(okBtn);
    }
    cancelBtn = new JButton("Cancel");
    cancelBtn.addActionListener(this);
    pushButtonPanel.add(cancelBtn);

    contentPane.add(pushButtonPanel, BorderLayout.SOUTH);

    mDialog.pack();
  }

  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okBtn) {
      configPanel.ok();
    } else if (e.getSource() == cancelBtn) {
      mDialog.dispose();
    }

  }
}