 /*
 * PDA-Plugin 
 * Copyright (C) 2004 gilson laurent pumpkin@gmx.de
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
package microtvbrowserplugin;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;


import util.exc.TvBrowserException;
import util.ui.ImageUtilities;

import devplugin.*;
import tvbrowser.ui.customizableitems.*;

import java.awt.*;
import javax.swing.*;
import util.ui.Localizer;

public class Settings implements SettingsTab, java.awt.event.ActionListener{

	JPanel lastPanel;
	
	JList allChanelList;
	JList selectedChanelList;
	
	CustomizableItemsPanel items;
	
	JCheckBox showIconsInProgList;
	
	JCheckBox showChannelNameInNowList;
	
	MicroTvBrowserPlugin belongs;
	MicroTvSettingsPanel options;
	
	static Localizer mLocalizer = Localizer.getLocalizerFor(Settings.class);
	
	
	public Settings (MicroTvBrowserPlugin Belongs){
		belongs = Belongs;
	}
	
	public JPanel createSettingsPanel(){
	
		items = CustomizableItemsPanel.createCustomizableItemsPanel(
			mLocalizer.msg("available","available")
			,mLocalizer.msg("included","included"));
		items.setBorder (BorderFactory.createTitledBorder(mLocalizer.msg("channels","channels")));
		items.clearLeft();
		items.clearRight();
		String[] alreadyIncluded = belongs.getChannelList();
		Channel[] CH = belongs.getPluginManager().getSubscribedChannels();
		
		Vector All = new Vector();
		for (int i =0;i<CH.length;i++){
			All.add (CH[i].getName());
		}
		
		Vector included = new Vector();
		
		for (int i =0;i<alreadyIncluded.length;i++){
			if (All.contains (alreadyIncluded[i])){
				items.addElementRight(alreadyIncluded[i]);
				included.add(alreadyIncluded[i]);
			}
		}
		
		for (int i=0;i<CH.length;i++){
			if (!included.contains(CH[i].getName())){
				items.addElementLeft(CH[i].getName());
			}
		}
 
		options = new MicroTvSettingsPanel();
		options.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("MIDletsettings","MIDletsettings")));
		
		
		options.sliderDays.setValue(belongs.getDaysToExport());
		options.sliderExport.setValue(belongs.getExportLevel());
		options.radioNanoEdition.setSelected(belongs.isUseNanoEdition());
		options.radioNanoEdition.addActionListener (this);
		options.radioMicroEdition.addActionListener (this);
		
		JPanel debug = new JPanel ();
		debug.setLayout(new GridLayout (0,1));
		debug.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Options","Options"))); 
		showIconsInProgList = new JCheckBox (mLocalizer.msg("include icons in prog list","include icons in prog list"));
		showIconsInProgList.setSelected(belongs.isUseIconsInProgList());
		
		showChannelNameInNowList = new JCheckBox (mLocalizer.msg("include channelname in now list","include channelname in now list"));
		showChannelNameInNowList.setSelected(belongs.isChannelNameInNowList());
		
		
		debug.add (showIconsInProgList);
		debug.add (showChannelNameInNowList);
		
		JPanel wrap = new JPanel();
		wrap.setLayout(new BorderLayout(5,5));
		wrap.add (items, BorderLayout.CENTER);
		wrap.add (options, BorderLayout.NORTH);
		wrap.add (debug,BorderLayout.SOUTH);
		return wrap;
	}
	
	public Icon getIcon(){
		return belongs.getButtonIcon();
	}

	public String getTitle(){
		return "microTVBrowser";
	}
	
	public void saveSettings(){
		Object[] O = items.getElementsRight();
		String[] names = new String[O.length];
		for (int i=0;i<O.length;i++){
			names[i] = O[i].toString();
		}
		belongs.setChannelList(names);
		belongs.setUseNanoEdition(options.radioNanoEdition.isSelected());
		belongs.setDaysToExport(options.sliderDays.getValue());
		belongs.setExportLevel(options.sliderExport.getValue());
		belongs.setUseIconsInProgList(showIconsInProgList.isSelected());
		belongs.setChannelNameInNowList(showChannelNameInNowList.isSelected());
	}
	
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (options.radioNanoEdition.isSelected()){
			options.sliderExport.setEnabled(false);
			showChannelNameInNowList.setEnabled(false);
			showIconsInProgList.setEnabled(false);
		} else {
			options.sliderExport.setEnabled(true);			
			showChannelNameInNowList.setEnabled(true);
			showIconsInProgList.setEnabled(true);
		}
	}
	
	/*
	
	public static void main (String[] A){
		Settings S = new Settings (null);
		JFrame F = new JFrame();
		F.setContentPane (S.createSettingsPanel());
		F.pack();
		F.setSize (300,300);
		F.show();
	}*/
	
 
} 
