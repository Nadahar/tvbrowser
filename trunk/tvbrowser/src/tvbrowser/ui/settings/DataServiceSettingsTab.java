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
 */

package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import tvbrowser.core.*;



/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */


public class DataServiceSettingsTab extends devplugin.SettingsTab implements ActionListener {
	
	private String[] dataDeleteComboBoxEntries={"after 2 days","after 3 days", "after 4 days", "after 5 days", "after 6 days","after 1 week", "after 2 weeks", "manually"};
	private String[] autoDownloadComboBoxEntries={"never","when tvbrowser starts up","every 30 minutes", "every hour"};
	private String[] browserModeComboboxEntries={"online mode","offline mode"};
	
	private JComboBox serviceComboBox;
	private JButton configBtn;
	private JButton changeDataDirBtn;
	private JTextField tvDataTextField;
	
	public DataServiceSettingsTab() {
		
		setLayout(new BorderLayout());
		
		JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
		
		JPanel tvDataPanel=new JPanel();
		tvDataPanel.setLayout(new BoxLayout(tvDataPanel,BoxLayout.Y_AXIS));
		
		
		JPanel browserModePanel=new JPanel();
		browserModePanel.setLayout(new BoxLayout(browserModePanel,BoxLayout.Y_AXIS));
		
		JPanel dataServicePanel=new JPanel();
		dataServicePanel.setLayout(new BoxLayout(dataServicePanel,BoxLayout.Y_AXIS));
		
		tvDataPanel.setBorder(BorderFactory.createTitledBorder("TV data"));
		browserModePanel.setBorder(BorderFactory.createTitledBorder("Browser mode"));
		dataServicePanel.setBorder(BorderFactory.createTitledBorder("TV data loader"));
		
		content.add(tvDataPanel);
		content.add(browserModePanel);
		content.add(dataServicePanel);
		
		JPanel delPanel=new JPanel(new GridLayout(0,2,20,0));
		delPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		delPanel.add(new JLabel("Delete tv data",JLabel.RIGHT));
		delPanel.add(new JComboBox(dataDeleteComboBoxEntries));
		
		JPanel dataDirPanel=new JPanel(new GridLayout(0,2,20,20));
		dataDirPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		dataDirPanel.add(new JLabel("tv data folder",JLabel.RIGHT));
		JPanel panel1=new JPanel(new BorderLayout());
		tvDataTextField=new JTextField(Settings.getTVDataDirectory());
		panel1.add(tvDataTextField,BorderLayout.CENTER);
		changeDataDirBtn=new JButton("...");
		changeDataDirBtn.addActionListener(this);
		panel1.add(changeDataDirBtn,BorderLayout.EAST);
		
		
		dataDirPanel.add(panel1);
	
		
		JPanel autoPanel=new JPanel(new GridLayout(0,2,20,0));
		autoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		autoPanel.add(new JLabel("Download automatically",JLabel.RIGHT));
		autoPanel.add(new JComboBox(autoDownloadComboBoxEntries));
		
		JPanel modePanel=new JPanel(new GridLayout(0,2,20,0));
		modePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		modePanel.add(new JLabel("Start in",JLabel.RIGHT));
		modePanel.add(new JComboBox(browserModeComboboxEntries));
		
		JPanel servicePanel=new JPanel(new GridLayout(0,2,20,0));
		servicePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		servicePanel.add(new JLabel("Configure tv data loaders:",JLabel.RIGHT));
		
		JPanel serviceConfigPanel=new JPanel(new BorderLayout());
		
		serviceComboBox=new JComboBox(DataLoaderManager.getInstance().getDataLoaders());
		serviceConfigPanel.add(serviceComboBox,BorderLayout.CENTER);
		configBtn=new JButton("configure...");
		final String curSelectedService;
		serviceComboBox.addActionListener(this);
		
		
		serviceConfigPanel.add(configBtn,BorderLayout.EAST);
		
		servicePanel.add(serviceConfigPanel);
		
		tvDataPanel.add(delPanel);
		tvDataPanel.add(dataDirPanel);
		browserModePanel.add(autoPanel);
		browserModePanel.add(modePanel);
		dataServicePanel.add(servicePanel);
				
		JPanel panel2=new JPanel();
		panel2.add(content);
		add(panel2,BorderLayout.NORTH);
		configBtn.addActionListener(this);
		final Frame parent=(Frame)getParent();
		configBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String item=(String)serviceComboBox.getSelectedItem();
				JDialog dlg=new DataServiceConfigDlg(parent,item);
				dlg.pack();
				util.ui.UiUtilities.centerAndShow(dlg);
				dlg.dispose();
				
			}
		}
		);
		
	}


	public void ok() {
		System.out.println("OK");
		Settings.setTVDataDirectory(tvDataTextField.getText());	
	}

	public String getName() {
		return "TV Data";
	}
	
	public void actionPerformed(ActionEvent event) {
		
		Object source=event.getSource();
		if (source==serviceComboBox) {
			String item=(String)serviceComboBox.getSelectedItem();
			tvdataloader.TVDataServiceInterface curSelectedService
              = DataLoaderManager.getInstance().getDataLoader(item);
			configBtn.setEnabled(item!=null && curSelectedService!=null && curSelectedService.hasSettingsPanel());
		}else if (source==changeDataDirBtn){
			JFileChooser fc =new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setApproveButtonText("OK");
			fc.setCurrentDirectory(new File(tvDataTextField.getText()));
			int retVal=fc.showOpenDialog(getParent());
			if (retVal==JFileChooser.APPROVE_OPTION) {
				File f=fc.getSelectedFile();
				tvDataTextField.setText(f.getAbsolutePath());
			}
			
		}
		
		
	}
	
}
