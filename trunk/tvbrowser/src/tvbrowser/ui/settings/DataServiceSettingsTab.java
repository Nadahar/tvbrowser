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



/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */


public class DataServiceSettingsTab extends devplugin.SettingsTab {
	
	private String[] dataDeleteComboBoxEntries={"after 2 days","after 3 days", "after 4 days", "after 5 days", "after 6 days","after 1 week", "after 2 weeks", "manually"};
	private String[] autoDownloadComboBoxEntries={"never","when tvbrowser starts up","every 30 minutes", "every hour"};
	private String[] browserModeComboboxEntries={"online mode","offline mode"};
	
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
		
		JPanel delPanel=new JPanel(new GridLayout(0,2));
		delPanel.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		delPanel.add(new JLabel("Delete tv data"));
		delPanel.add(new JComboBox(dataDeleteComboBoxEntries));
		
		JPanel dataDirPanel=new JPanel(new GridLayout(0,2));
		dataDirPanel.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		dataDirPanel.add(new JLabel("tv data folder"));
		JPanel panel1=new JPanel(new BorderLayout());
		panel1.add(new JTextField("tvdata/"),BorderLayout.CENTER);
		panel1.add(new JButton("change"),BorderLayout.EAST);
		dataDirPanel.add(panel1);
	
		
		JPanel autoPanel=new JPanel(new GridLayout(0,2));
		autoPanel.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		autoPanel.add(new JLabel("Download automatically"));
		autoPanel.add(new JComboBox(autoDownloadComboBoxEntries));
		
		JPanel modePanel=new JPanel(new GridLayout(0,2));
		modePanel.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		modePanel.add(new JLabel("Sart in"));
		modePanel.add(new JComboBox(browserModeComboboxEntries));
		
		JPanel servicePanel=new JPanel(new GridLayout(0,2));
		servicePanel.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		servicePanel.add(new JLabel("Configure tv data loaders:"));
		servicePanel.add(new JComboBox(),BorderLayout.EAST);
		
		tvDataPanel.add(delPanel);
		tvDataPanel.add(dataDirPanel);
		browserModePanel.add(autoPanel);
		browserModePanel.add(modePanel);
		dataServicePanel.add(servicePanel);
				
		add(content,BorderLayout.NORTH);
		
	}


	public void ok() {
	
	}

	public String getName() {
		return "TV Data";
	}
	
	
}
