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

import tvdataloader.TVDataServiceInterface;
import tvdataloader.SettingsPanel;
import tvbrowser.core.DataLoaderManager;

public class DataServiceConfigDlg implements ActionListener {
	
  private JDialog mDialog;
	private TVDataServiceInterface dataLoader;
	private JButton cancelBtn, okBtn;
	private SettingsPanel configPanel;
	
  
  
	public DataServiceConfigDlg(Component parent, String dataloaderName) {
    mDialog = UiUtilities.createDialog(parent, true);
    
   	mDialog.setTitle("Configure "+dataloaderName);
    
		JPanel contentPane = (JPanel) mDialog.getContentPane();
		
		contentPane.setLayout(new BorderLayout());
		
		dataLoader=DataLoaderManager.getInstance().getDataLoader(dataloaderName);
		
		if (dataLoader!=null) {
			configPanel=dataLoader.getSettingsPanel();
			if (configPanel!=null) {
				contentPane.add(configPanel,BorderLayout.NORTH);
			}else{
				contentPane.add(new JLabel("no config pane available"),BorderLayout.CENTER);
			}
		}else{
			contentPane.add(new JLabel("Error: dataloader '"+dataloaderName+"' not found"),BorderLayout.CENTER);
		}
		
		
		JPanel pushButtonPanel=new JPanel();

		if (configPanel!=null) {
			okBtn=new JButton("OK");
			okBtn.addActionListener(this);
			pushButtonPanel.add(okBtn);
			mDialog.getRootPane().setDefaultButton(okBtn);
		}
		cancelBtn=new JButton("Cancel");
		cancelBtn.addActionListener(this);
		pushButtonPanel.add(cancelBtn);
		
		contentPane.add(pushButtonPanel,BorderLayout.SOUTH);
    
    mDialog.pack();
	}
  
  
  
  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }
  
	
  
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==okBtn) {
			configPanel.ok();
		}else if (e.getSource()==cancelBtn) {
			mDialog.dispose();
		}
		
	}
}