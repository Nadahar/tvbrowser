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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import util.ui.UiUtilities;

import tvdataservice.TvDataService;
import tvdataservice.SettingsPanel;

public class DeleteTVDataDlg implements ActionListener {
	
  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DeleteTVDataDlg.class);
 
  
  
  private JDialog mDialog;
	private TvDataService dataService;
	private JButton deleteBtn, closeBtn;
	private SettingsPanel configPanel;
	private JSpinner mDaySp;
	
  
  
	public DeleteTVDataDlg(Component parent) {
	mDialog = UiUtilities.createDialog(parent, true);
  
  
	mDialog.setTitle(mLocalizer.msg("title", "Delete tv data manually"));
    
	JPanel contentPane = (JPanel) mDialog.getContentPane();
	
	
	JPanel content=new JPanel();
	content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
	
	
	JPanel panel1=new JPanel();
	
  
  
	panel1.add(new JLabel(mLocalizer.msg("range", "Delete tv data manually")));
	mDaySp=new JSpinner(new SpinnerNumberModel(5,1,9999,1));
	panel1.add(mDaySp);
	panel1.add(new JLabel(mLocalizer.msg("days", "days")));
	content.add(panel1);
	
		
	contentPane.setLayout(new BorderLayout());
		
		
		JPanel pushButtonPanel=new JPanel();

		closeBtn=new JButton(mLocalizer.msg("close", "close"));
		closeBtn.addActionListener(this);
		pushButtonPanel.add(closeBtn);
		mDialog.getRootPane().setDefaultButton(closeBtn);
		
		deleteBtn=new JButton(mLocalizer.msg("deletenow", "delete now!"));
		deleteBtn.addActionListener(this);
		pushButtonPanel.add(deleteBtn);
		contentPane.add(content,BorderLayout.NORTH);
		
		contentPane.add(pushButtonPanel,BorderLayout.SOUTH);
    
	mDialog.pack();
	}
  
  
  
  public void centerAndShow() {
	UiUtilities.centerAndShow(mDialog);
  }
  
	
  
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==deleteBtn) {
			Integer i=(Integer)mDaySp.getValue();
			tvbrowser.core.DataService.deleteExpiredFiles(i.intValue());
			mDialog.dispose();
			
		}else if (e.getSource()==closeBtn) {
			mDialog.dispose();
		}
		
	}
}