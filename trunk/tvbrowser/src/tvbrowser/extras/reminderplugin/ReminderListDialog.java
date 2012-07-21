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
package tvbrowser.extras.reminderplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderListDialog extends JDialog implements WindowClosingIf {
  static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ReminderListDialog.class);
  

  
  
  private ReminderListPanel mReminderListPanel;
  
  private static ReminderListDialog mInstance;

  public ReminderListDialog(Window parent, ReminderList list) {
    super(parent);
    setModal(true);
    UiUtilities.registerForClosing(this);

    setTitle(mLocalizer.msg("title", "Reminder"));
    createGui(list);
  }
  
  private void createGui(ReminderList list) {
    mInstance = this;
    setLayout(new BorderLayout());
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mReminderListPanel.stopCellEditing();
        dispose();
      }
    });
    
    mReminderListPanel = new ReminderListPanel(list,ok);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        mInstance = null;
      }
    });
    
    add(mReminderListPanel, BorderLayout.CENTER);

    getRootPane().setDefaultButton(ok);
    
    Settings.layoutWindow("extras.reminderListDlg", this, new Dimension(550,350));
  }
  
  public static ReminderListDialog getInstance() {
    return mInstance;
  }

  public void close() {
    mInstance = null;
    dispose();
  }
  
  /**
   * Updates the list of the dialog with the new list.
   * 
   * @since 2.7.2
   */
  public static void updateReminderList() {
    ReminderListDialog dlg = mInstance;
    
    if(dlg != null) {
      dlg.mReminderListPanel.updateTableEntries();
    }
  }
}